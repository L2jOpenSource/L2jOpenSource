package com.l2jfrozen.gameserver.model.quest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.managers.QuestManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ConfirmDlg;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.scripting.ManagedScript;
import com.l2jfrozen.gameserver.scripting.ScriptManager;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author Luis Arias
 */
public class Quest extends ManagedScript
{
	protected static final Logger LOGGER = Logger.getLogger(Quest.class);
	private static final String INSERT_CHARACTER_QUEST = "INSERT INTO character_quests (char_id,name,var,value) VALUES (?,?,?,?)";
	private static final String UPDATE_CHARACTER_QUEST = "UPDATE character_quests SET value=? WHERE char_id=? AND name=? AND var = ?";
	
	private static final String DELETE_CHARACTER_QUEST_VAR = "DELETE FROM character_quests WHERE char_id=? AND name=? AND var=?";
	private static final String DELETE_CHARACTER_QUEST = "DELETE FROM character_quests WHERE char_id=? AND name=?";
	
	private static final String SELECT_GLOBAL_QUEST_VALUE = "SELECT value FROM quest_global_data WHERE quest_name = ? AND var = ?";
	private static final String DELETE_QUEST_GLOBAL_DATA_BY_QUEST_NAME = "DELETE FROM quest_global_data WHERE quest_name = ?";
	private static final String DELETE_QUEST_GLOBAL_DATA_BY_QUEST_NAME_AND_VAR = "DELETE FROM quest_global_data WHERE quest_name=? AND var=?";
	private static final String REPLACE_QUEST_GLOBAL_DATA = "REPLACE INTO quest_global_data (quest_name,var,value) VALUES (?,?,?)";
	
	/** HashMap containing events from String value of the event */
	private static Map<String, Quest> allEventsS = new HashMap<>();
	/** HashMap containing lists of timers from the name of the timer */
	private final Map<String, ArrayList<QuestTimer>> allEventTimers = new HashMap<>();
	
	private final int questId;
	private final String name;
	private final String prefixPath; // used only for admin_quest_reload
	private final String descr;
	private State initialState;
	private final Map<String, State> states;
	private List<Integer> questItemIds;
	
	/**
	 * Return collection view of the values contains in the allEventS
	 * @return Collection<Quest>
	 */
	public static Collection<Quest> findAllEvents()
	{
		return allEventsS.values();
	}
	
	/**
	 * (Constructor)Add values to class variables and put the quest in HashMaps.
	 * @param questId : int pointing out the ID of the quest
	 * @param name    : String corresponding to the name of the quest
	 * @param descr   : String for the description of the quest
	 */
	public Quest(final int questId, final String name, final String descr)
	{
		this.questId = questId;
		this.name = name;
		this.descr = descr;
		states = new HashMap<>();
		
		// Given the quest instance, create a string representing the path and questName
		// like a simplified version of a canonical class name. That is, if a script is in
		// DATAPACK_PATH/scripts/quests/abc the result will be quests.abc
		// Similarly, for a script in DATAPACK_PATH/scripts/ai/individual/myClass.py
		// the result will be ai.individual.myClass
		// All quests are to be indexed, processed, and reloaded by this form of pathname.
		StringBuffer temp = new StringBuffer(getClass().getCanonicalName());
		temp.delete(0, temp.indexOf(".scripts.") + 9);
		temp.delete(temp.indexOf(getClass().getSimpleName()), temp.length());
		prefixPath = temp.toString();
		
		if (questId != 0)
		{
			QuestManager.getInstance().addQuest(Quest.this);
		}
		else
		{
			allEventsS.put(name, this);
		}
		
		init_LoadGlobalData();
		
		temp = null;
	}
	
	/**
	 * The function init_LoadGlobalData is, by default, called by the constructor of all quests. Children of this class can implement this function in order to define what variables to load and what structures to save them in. By default, nothing is loaded.
	 */
	protected void init_LoadGlobalData()
	{
		
	}
	
	/**
	 * The function saveGlobalData is, by default, called at shutdown, for all quests, by the QuestManager. Children of this class can implement this function in order to convert their structures into <var, value> tuples and make calls to save them to the database, if needed. By default, nothing is
	 * saved.
	 */
	public void saveGlobalData()
	{
		
	}
	
	public static enum QuestEventType
	{
		/**
		 * control the first dialog shown by NPCs when they are clicked (some quests must override the default npc action)
		 */
		NPC_FIRST_TALK(false),
		
		/** onTalk action from start npcs */
		QUEST_START(true),
		
		/** onTalk action from npcs participating in a quest */
		QUEST_TALK(true),
		
		/**
		 * onAttack action triggered when a mob gets attacked by someone<br>
		 * <font color=red><b>USE: ON_ATTACK</b></font>
		 */
		@Deprecated
		MOBGOTATTACKED(true),
		
		/**
		 * onKill action triggered when a mob gets killed.<br>
		 * <font color=red><b>USE: ON_KILL</b></font>
		 */
		@Deprecated
		MOBKILLED(true),
		
		/** Call a faction for support */
		ON_FACTION_CALL(true),
		
		/**
		 * onSkillUse action triggered when a character uses a skill on a mob<br>
		 * <font color=red><b>USE: ON_SKILL_USE</b></font>
		 */
		@Deprecated
		MOB_TARGETED_BY_SKILL(true),
		
		/** on spell finished action when npc finish casting skill */
		ON_SPELL_FINISHED(true),
		
		/** a person came within the Npc/Mob's range */
		ON_AGGRO_RANGE_ENTER(true),
		
		/** OnSpawn РґРµР№СЃС‚РІРёРµ РїСЂРё СЃРїР°СѓРЅРµ РјРѕР±Р° */
		ON_SPAWN(true),
		
		/** OnSkillUse РґРµР№СЃС‚РІРёРµ РїСЂРё РёСЃРїРѕР»СЊР·РѕРІР°РЅРёРё СЃРєРёР»Р»Р° (MOB_TARGETED_BY_SKILL) */
		ON_SKILL_USE(true),
		
		/** OnKill РґРµР№СЃС‚РІРёРµ РїСЂРё СѓР±РёР№СЃС‚РІРµ (MOBKILLED) */
		ON_KILL(true),
		
		/** OnAttack РґРµР№СЃС‚РІРёРµ РїСЂРё Р°С‚Р°РєРµ (MOBGOTATTACKED) */
		ON_ATTACK(true);
		
		// control whether this event type is allowed for the same npc template in multiple quests
		// or if the npc must be registered in at most one quest for the specified event
		private boolean allowMultipleRegistration;
		
		QuestEventType(final boolean allowMultipleRegistration)
		{
			this.allowMultipleRegistration = allowMultipleRegistration;
		}
		
		public boolean isMultipleRegistrationAllowed()
		{
			return allowMultipleRegistration;
		}
		
	}
	
	/**
	 * Return ID of the quest
	 * @return int
	 */
	public int getQuestIntId()
	{
		return questId;
	}
	
	/**
	 * Set the initial state of the quest with parameter "state"
	 * @param state
	 */
	public void setInitialState(final State state)
	{
		initialState = state;
	}
	
	/**
	 * Add a new QuestState to the database and return it.
	 * @param  player
	 * @return        QuestState : QuestState created
	 */
	public QuestState newQuestState(final L2PcInstance player)
	{
		final QuestState qs = new QuestState(this, player, getInitialState(), false);
		player.setQuestState(qs);
		Quest.createQuestInDb(qs);
		return qs;
	}
	
	/**
	 * Return initial state of the quest
	 * @return State
	 */
	public State getInitialState()
	{
		return initialState;
	}
	
	/**
	 * Return name of the quest
	 * @return String
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Return name of the prefix path for the quest, down to the last "." For example "quests." or "ai.individual."
	 * @return String
	 */
	public String getPrefixPath()
	{
		return prefixPath;
	}
	
	/**
	 * Return description of the quest
	 * @return String
	 */
	public String getDescr()
	{
		return descr;
	}
	
	/**
	 * Add a state to the quest
	 * @param  state
	 * @return       state added
	 */
	public State addState(final State state)
	{
		states.put(state.getName(), state);
		return state;
	}
	
	/**
	 * @param name
	 * @param time
	 * @param npc
	 * @param player
	 */
	public void startQuestTimer(final String name, final long time, final L2NpcInstance npc, final L2PcInstance player)
	{
		startQuestTimer(name, time, npc, player, false);
	}
	
	/**
	 * Add a timer to the quest, if it doesn't exist already. If the timer is repeatable, it will auto-fire automatically, at a fixed rate, until explicitly canceled.
	 * @param name      name of the timer (also passed back as "event" in onAdvEvent)
	 * @param time      time in ms for when to fire the timer
	 * @param npc       npc associated with this timer (can be null)
	 * @param player    player associated with this timer (can be null)
	 * @param repeating indicates if the timer is repeatable or one-time.
	 */
	public synchronized void startQuestTimer(final String name, final long time, final L2NpcInstance npc, final L2PcInstance player, final boolean repeating)
	{
		if (Config.DEBUG)
		{
			LOGGER.info("StartingQuestTimer for Quest " + getName());
			
			String info = "Event:" + name + " Time:" + time;
			if (npc != null)
			{
				info = info + " Npc:" + npc.getName();
			}
			
			if (player != null)
			{
				info = info + " Player:" + player.getName();
			}
			
			LOGGER.info(info + " Repeat:" + repeating);
		}
		
		synchronized (allEventTimers)
		{
			
			ArrayList<QuestTimer> timers = allEventTimers.get(name);
			
			// no timer exists with the same name, at all
			if (timers == null)
			{
				
				timers = new ArrayList<>();
				timers.add(new QuestTimer(this, name, time, npc, player, repeating));
				
				// a timer with this name exists, but may not be for the same set of npc and player
			}
			else
			{
				
				QuestTimer timer = null;
				for (int i = 0; i < timers.size(); i++)
				{
					
					final QuestTimer tmp = timers.get(i);
					
					if (tmp != null && tmp.isMatch(this, name, npc, player))
					{
						timer = tmp;
						break;
					}
					
				}
				
				// if there exists a timer with this name, allow the timer only if the [npc, player] set is unique
				// nulls act as wildcards
				if (timer == null)
				{
					timers.add(new QuestTimer(this, name, time, npc, player, repeating));
				}
				
			}
			
			allEventTimers.put(name, timers);
			
		}
		
	}
	
	public QuestTimer getQuestTimer(final String name, final L2NpcInstance npc, final L2PcInstance player)
	{
		if (name == null)
		{
			return null;
		}
		
		synchronized (allEventTimers)
		{
			final ArrayList<QuestTimer> qt = allEventTimers.get(name);
			
			if (qt == null || qt.isEmpty())
			{
				return null;
			}
			
			for (int i = 0; i < qt.size(); i++)
			{
				
				final QuestTimer timer = qt.get(i);
				
				if (timer != null && timer.isMatch(this, name, npc, player))
				{
					return timer;
				}
			}
			return null;
		}
	}
	
	public void cancelQuestTimer(final String name, final L2NpcInstance npc, final L2PcInstance player)
	{
		if (name == null)
		{
			return;
		}
		
		synchronized (allEventTimers)
		{
			final ArrayList<QuestTimer> qt = allEventTimers.get(name);
			if (qt == null || qt.isEmpty())
			{
				return;
			}
			
			for (int i = 0; i < qt.size(); i++)
			{
				
				final QuestTimer timer = qt.get(i);
				if (timer != null && timer.isMatch(this, name, npc, player))
				{
					timer.cancel(false);
					qt.remove(timer);
					return;
				}
			}
		}
	}
	
	public void cancelQuestTimers(final String name)
	{
		if (name == null)
		{
			return;
		}
		
		synchronized (allEventTimers)
		{
			final ArrayList<QuestTimer> timers = allEventTimers.get(name);
			
			if (timers == null)
			{
				return;
			}
			
			for (final QuestTimer timer : timers)
			{
				if (timer != null)
				{
					timer.cancel(false);
					
				}
			}
			
			timers.clear();
			
		}
	}
	
	public void removeQuestTimer(final QuestTimer timer)
	{
		if (timer == null)
		{
			return;
		}
		
		synchronized (allEventTimers)
		{
			final ArrayList<QuestTimer> timers = allEventTimers.get(timer.getName());
			
			if (timers == null)
			{
				return;
			}
			
			timers.remove(timer);
		}
	}
	
	// these are methods to call from java
	public final boolean notifyAttack(final L2NpcInstance npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		String res = null;
		
		try
		{
			res = onAttack(npc, attacker, damage, isPet);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return showError(attacker, e);
		}
		
		return showResult(attacker, res);
	}
	
	public final boolean notifyDeath(final L2Character killer, final L2Character victim, final QuestState qs)
	{
		String res = null;
		
		try
		{
			res = onDeath(killer, victim, qs);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return showError(qs.getPlayer(), e);
		}
		
		return showResult(qs.getPlayer(), res);
	}
	
	public final boolean notifyEvent(final String event, final L2NpcInstance npc, final L2PcInstance player)
	{
		String res = null;
		
		try
		{
			res = onAdvEvent(event, npc, player);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return showError(player, e);
		}
		
		return showResult(player, res);
	}
	
	public final boolean notifyKill(final L2NpcInstance npc, final L2PcInstance killer, final boolean isPet)
	{
		String res = null;
		
		try
		{
			res = onKill(npc, killer, isPet);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return showError(killer, e);
		}
		
		return showResult(killer, res);
	}
	
	public final boolean notifyTalk(final L2NpcInstance npc, final QuestState qs)
	{
		String res = null;
		
		try
		{
			res = onTalk(npc, qs.getPlayer());
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return showError(qs.getPlayer(), e);
		}
		
		qs.getPlayer().setLastQuestNpcObject(npc.getObjectId());
		
		return showResult(qs.getPlayer(), res);
	}
	
	// override the default NPC dialogs when a quest defines this for the given NPC
	public final boolean notifyFirstTalk(final L2NpcInstance npc, final L2PcInstance player)
	{
		String res = null;
		
		try
		{
			res = onFirstTalk(npc, player);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return showError(player, e);
		}
		
		player.setLastQuestNpcObject(npc.getObjectId());
		
		// if the quest returns text to display, display it. Otherwise, use the default npc text.
		if (res != null && res.length() > 0)
		{
			return showResult(player, res);
		}
		
		npc.showChatWindow(player);
		
		return true;
	}
	
	public final boolean notifySkillUse(final L2NpcInstance npc, final L2PcInstance caster, final L2Skill skill)
	{
		String res = null;
		
		try
		{
			res = onSkillUse(npc, caster, skill);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return showError(caster, e);
		}
		
		return showResult(caster, res);
	}
	
	public final boolean notifySpellFinished(final L2NpcInstance npc, final L2PcInstance player, final L2Skill skill)
	{
		String res = null;
		try
		{
			res = onSpellFinished(npc, player, skill);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public final boolean notifyFactionCall(final L2NpcInstance npc, final L2NpcInstance caller, final L2PcInstance attacker, final boolean isPet)
	{
		String res = null;
		try
		{
			res = onFactionCall(npc, caller, attacker, isPet);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return showError(attacker, e);
		}
		return showResult(attacker, res);
	}
	
	public final boolean notifyAggroRangeEnter(final L2NpcInstance npc, final L2PcInstance player, final boolean isPet)
	{
		String res = null;
		
		try
		{
			res = onAggroRangeEnter(npc, player, isPet);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public final boolean notifySpawn(final L2NpcInstance npc)
	{
		String res = null;
		
		try
		{
			res = onSpawn(npc);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return showError(npc, e);
		}
		return showResult(npc, res);
	}
	
	// these are methods that java calls to invoke scripts
	public String onAttack(final L2NpcInstance npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		return null;
	}
	
	public String onDeath(final L2Character killer, final L2Character victim, final QuestState qs)
	{
		if (killer instanceof L2NpcInstance)
		{
			return onAdvEvent("", (L2NpcInstance) killer, qs.getPlayer());
		}
		return onAdvEvent("", null, qs.getPlayer());
	}
	
	public String onAdvEvent(final String event, final L2NpcInstance npc, final L2PcInstance player)
	{
		if (player == null)
		{
			return null;
		}
		
		// if not overriden by a subclass, then default to the returned value of the simpler (and older) onEvent override
		// if the player has a state, use it as parameter in the next call, else return null
		final QuestState qs = player.getQuestState(getName());
		
		if (qs != null)
		{
			return onEvent(event, qs);
		}
		
		return null;
	}
	
	public void sendDlgMessage(final String text, final L2PcInstance player)
	{
		player.dialog = this;
		final ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1.getId());
		dlg.addString(text);
		player.sendPacket(dlg);
	}
	
	public void onDlgAnswer(final L2PcInstance player)
	{
	}
	
	public String onEvent(final String event, final QuestState qs)
	{
		return null;
	}
	
	public String onKill(final L2NpcInstance npc, final L2PcInstance killer, final boolean isPet)
	{
		return null;
	}
	
	public String onTalk(final L2NpcInstance npc, final L2PcInstance talker)
	{
		return null;
	}
	
	public String onFirstTalk(final L2NpcInstance npc, final L2PcInstance player)
	{
		return null;
	}
	
	public String onSkillUse(final L2NpcInstance npc, final L2PcInstance caster, final L2Skill skill)
	{
		return null;
	}
	
	public String onSpellFinished(final L2NpcInstance npc, final L2PcInstance player, final L2Skill skill)
	{
		return null;
	}
	
	public String onFactionCall(final L2NpcInstance npc, final L2NpcInstance caller, final L2PcInstance attacker, final boolean isPet)
	{
		return null;
	}
	
	public String onAggroRangeEnter(final L2NpcInstance npc, final L2PcInstance player, final boolean isPet)
	{
		return null;
	}
	
	public String onSpawn(final L2NpcInstance npc)
	{
		return null;
	}
	
	/**
	 * Show message error to player who has an access level greater than 0
	 * @param  object
	 * @param  t      : Throwable
	 * @return        boolean
	 */
	public boolean showError(final L2Character object, final Throwable t)
	{
		if (getScriptFile() != null)
		{
			LOGGER.warn(getScriptFile().getAbsolutePath(), t);
		}
		
		if (object == null)
		{
			return false;
		}
		
		if (object instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) object;
			if (player.getAccessLevel().isGm())
			{
				final StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				
				t.printStackTrace(pw);
				pw.close();
				pw = null;
				
				final String res = "<html><body><title>Script error</title>" + sw.toString() + "</body></html>";
				
				return showResult(player, res);
			}
			
			player = null;
		}
		else
		{
			final StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			
			t.printStackTrace(pw);
			pw.close();
			pw = null;
			
			final String res = "Script error: " + sw.toString();
			GmListTable.broadcastMessageToGMs(res);
			
			return showResult(object, res);
		}
		return false;
	}
	
	/**
	 * Show a message to player.<BR>
	 * <BR>
	 * <U><I>Concept : </I></U><BR>
	 * 3 cases are managed according to the value of the parameter "res" :<BR>
	 * <LI><U>"res" ends with string ".html" :</U> an HTML is opened in order to be shown in a dialog box</LI>
	 * <LI><U>"res" starts with "<html>" :</U> the message hold in "res" is shown in a dialog box</LI>
	 * <LI><U>otherwise :</U> the message hold in "res" is shown in chat box</LI>
	 * @param  object
	 * @param  res    : String pointing out the message to show at the player
	 * @return        boolean
	 */
	private boolean showResult(final L2Character object, final String res)
	{
		if (res == null)
		{
			return true;
		}
		
		if (object instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) object;
			
			if (res.endsWith(".htm"))
			{
				showHtmlFile(player, res);
			}
			else if (res.startsWith("<html>"))
			{
				NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
				npcReply.setHtml(res);
				npcReply.replace("%playername%", player.getName());
				player.sendPacket(npcReply);
				npcReply = null;
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString(res);
				player.sendPacket(sm);
				sm = null;
			}
			
			player = null;
		}
		
		return false;
	}
	
	/**
	 * Add the quest to the NPC's startQuest
	 * @param  npcId
	 * @return       L2NpcTemplate : Start NPC
	 */
	public L2NpcTemplate addStartNpc(final int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.QUEST_START);
	}
	
	/**
	 * Add the quest to the NPC's first-talk (default action dialog)
	 * @param  npcId
	 * @return       L2NpcTemplate : Start NPC
	 */
	public L2NpcTemplate addFirstTalkId(final int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.NPC_FIRST_TALK);
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for Attack Events.<BR>
	 * <BR>
	 * @param  attackId
	 * @return          int : attackId
	 */
	public L2NpcTemplate addAttackId(final int attackId)
	{
		return addEventId(attackId, Quest.QuestEventType.ON_ATTACK);
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for Kill Events.<BR>
	 * <BR>
	 * @param  killId
	 * @return        int : killId
	 */
	public L2NpcTemplate addKillId(final int killId)
	{
		return addEventId(killId, Quest.QuestEventType.ON_KILL);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Talk Events.<BR>
	 * <BR>
	 * @param  talkId : ID of the NPC
	 * @return        int : ID of the NPC
	 */
	public L2NpcTemplate addTalkId(final int talkId)
	{
		return addEventId(talkId, Quest.QuestEventType.QUEST_TALK);
	}
	
	public L2NpcTemplate addFactionCallId(final int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_FACTION_CALL);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Skill-Use Events.<BR>
	 * <BR>
	 * @param  npcId : ID of the NPC
	 * @return       int : ID of the NPC
	 */
	public L2NpcTemplate addSkillUseId(final int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_SKILL_USE);
	}
	
	public L2NpcTemplate addSpellFinishedId(final int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_SPELL_FINISHED);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Character See Events.<BR>
	 * <BR>
	 * @param  npcId ID of the NPC
	 * @return       int ID of the NPC
	 */
	public L2NpcTemplate addAggroRangeEnterId(final int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_AGGRO_RANGE_ENTER);
	}
	
	public L2NpcTemplate addSpawnId(final int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_SPAWN);
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for the specified Event type.<BR>
	 * <BR>
	 * @param  npcId     : id of the NPC to register
	 * @param  eventType : type of event being registered
	 * @return           L2NpcTemplate : Npc Template corresponding to the npcId, or null if the id is invalid
	 */
	public L2NpcTemplate addEventId(final int npcId, final QuestEventType eventType)
	{
		try
		{
			final L2NpcTemplate t = NpcTable.getInstance().getTemplate(npcId);
			
			if (t != null)
			{
				t.addQuestEvent(eventType, this);
			}
			
			return t;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Add quests to the L2PCInstance of the player.<BR>
	 * <BR>
	 * <U><I>Action : </U></I><BR>
	 * Add state of quests, drops and variables for quests in the HashMap quest of L2PcInstance
	 * @param player : Player who is entering the world
	 */
	public final static void playerEnter(final L2PcInstance player)
	{
		if (Config.DEBUG)
		{
			LOGGER.info("Quest.playerEnter " + player.getName());
			
		}
		
		if (Config.ALT_DEV_NO_QUESTS)
		{
			return;
		}
		
		Connection con = null;
		try
		{
			// Get list of quests owned by the player from database
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			
			PreparedStatement invalidQuestData = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND name=?");
			PreparedStatement invalidQuestDataVar = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND name=? AND var=?");
			
			statement = con.prepareStatement("SELECT name,value FROM character_quests WHERE char_id=? AND var=?");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, "<state>");
			ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				
				// Get ID of the quest and ID of its state
				final String questId = rs.getString("name");
				final String stateId = rs.getString("value");
				
				// Search quest associated with the ID
				final Quest q = QuestManager.getInstance().getQuest(questId);
				
				if (q == null)
				{
					if (Config.DEVELOPER)
					{
						LOGGER.info("Unknown quest " + questId + " for player " + player.getName());
					}
					if (Config.AUTODELETE_INVALID_QUEST_DATA)
					{
						invalidQuestData.setInt(1, player.getObjectId());
						invalidQuestData.setString(2, questId);
						invalidQuestData.executeUpdate();
					}
					continue;
				}
				
				// Identify the state of the quest for the player
				boolean completed = false;
				
				if (stateId.equals("Completed"))
				{
					completed = true;
				}
				
				// Create an object State containing the state of the quest
				final State state = q.states.get(stateId);
				if (state == null)
				{
					if (Config.DEVELOPER)
					{
						LOGGER.info("Unknown state in quest " + questId + " for player " + player.getName());
					}
					if (Config.AUTODELETE_INVALID_QUEST_DATA)
					{
						invalidQuestData.setInt(1, player.getObjectId());
						invalidQuestData.setString(2, questId);
						invalidQuestData.executeUpdate();
					}
					continue;
				}
				
				// Create a new QuestState for the player that will be added to the player's list of quests
				final QuestState qs = new QuestState(q, player, state, completed);
				player.setQuestState(qs);
				
			}
			
			rs.close();
			invalidQuestData.close();
			DatabaseUtils.close(statement);
			statement = null;
			rs = null;
			
			// Get list of quests owned by the player from the DB in order to add variables used in the quest.
			statement = con.prepareStatement("SELECT name,var,value FROM character_quests WHERE char_id=?");
			statement.setInt(1, player.getObjectId());
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				String questId = rs.getString("name");
				String var = rs.getString("var");
				String value = rs.getString("value");
				
				// Get the QuestState saved in the loop before
				QuestState qs = player.getQuestState(questId);
				
				if (qs == null)
				{
					if (Config.DEVELOPER)
					{
						LOGGER.info("Lost variable " + var + " in quest " + questId + " for player " + player.getName());
					}
					
					if (Config.AUTODELETE_INVALID_QUEST_DATA)
					{
						invalidQuestDataVar.setInt(1, player.getObjectId());
						invalidQuestDataVar.setString(2, questId);
						invalidQuestDataVar.setString(3, var);
						invalidQuestDataVar.executeUpdate();
					}
					continue;
				}
				// Add parameter to the quest
				qs.setInternal(var, value);
				
				questId = null;
				var = null;
				value = null;
				qs = null;
			}
			
			rs.close();
			invalidQuestDataVar.close();
			DatabaseUtils.close(statement);
			statement = null;
			rs = null;
			invalidQuestDataVar = null;
			
		}
		
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("could not insert char quest:", e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
		
		// events
		for (final String name : allEventsS.keySet())
		{
			player.processQuestEvent(name, "enter");
		}
	}
	
	/**
	 * Insert (or Update) in the database variables that need to stay persistant for this quest after a reboot. This function is for storage of values that do not related to a specific player but are global for all characters. For example, if we need to disable a quest-gatekeeper until a certain time
	 * (as is done with some grand-boss gatekeepers), we can save that time in the DB.
	 * @param var   : String designating the name of the variable for the quest
	 * @param value : String designating the value of the variable for the quest
	 */
	public final void saveGlobalQuestVar(String var, String value)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(REPLACE_QUEST_GLOBAL_DATA))
		{
			statement.setString(1, getName());
			statement.setString(2, var);
			statement.setString(3, value);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Quest.saveGlobalQuestVar : Could not insert global quest variable:", e);
		}
	}
	
	/**
	 * Read from the database a previously saved variable for this quest. Due to performance considerations, this function should best be used only when the quest is first loaded. Subclasses of this class can define structures into which these loaded values can be saved. However, on-demand usage of this
	 * function throughout the script is not prohibited, only not recommended. Values read from this function were entered by calls to "saveGlobalQuestVar"
	 * @param  var : String designating the name of the variable for the quest
	 * @return     String : String representing the loaded value for the passed var, or an empty string if the var was invalid
	 */
	public final String loadGlobalQuestVar(String var)
	{
		String result = "";
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_GLOBAL_QUEST_VALUE))
		{
			statement.setString(1, getName());
			statement.setString(2, var);
			
			try (ResultSet rs = statement.executeQuery())
			{
				if (rs.first())
				{
					result = rs.getString("value");
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Quest.loadGlobalQuestVar : Could not load global quest variable:", e);
		}
		
		return result;
	}
	
	/**
	 * Permanently delete from the database a global quest variable that was previously saved for this quest.
	 * @param var : String designating the name of the variable for the quest
	 */
	public final void deleteGlobalQuestVar(String var)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_QUEST_GLOBAL_DATA_BY_QUEST_NAME_AND_VAR))
		{
			statement.setString(1, getName());
			statement.setString(2, var);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.warn("Quest.deleteGlobalQuestVar : Could not delete global quest variable:", e);
		}
	}
	
	/**
	 * Permanently delete from the database all global quest variables that was previously saved for this quest.
	 */
	public final void deleteAllGlobalQuestVars()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_QUEST_GLOBAL_DATA_BY_QUEST_NAME))
		{
			statement.setString(1, getName());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Quest.deleteAllGlobalQuestVars:Could not delete global quest variables:", e);
		}
	}
	
	/**
	 * Insert in the database the quest for the player.
	 * @param qs    : QuestState pointing out the state of the quest
	 * @param var   : String designating the name of the variable for the quest
	 * @param value : String designating the value of the variable for the quest
	 */
	public static void createQuestVarInDb(final QuestState qs, final String var, final String value)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_CHARACTER_QUEST))
		{
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.setString(4, value);
			statement.executeUpdate();
		}
		catch (final SQLException e)
		{
			// Duplicate entry
			if (e.getErrorCode() == 1062)
			{
				updateQuestVarInDb(qs, var, value);
				return;
			}
			LOGGER.error("could not insert char quest:", e);
		}
	}
	
	/**
	 * Update the value of the variable "var" for the quest.<BR>
	 * <BR>
	 * <U><I>Actions :</I></U><BR>
	 * The selection of the right record is made with :
	 * <LI>char_id = qs.getPlayer().getObjectID()</LI>
	 * <LI>name = qs.getQuest().getName()</LI>
	 * <LI>var = var</LI> <BR>
	 * <BR>
	 * The modification made is :
	 * <LI>value = parameter value</LI>
	 * @param qs    : Quest State
	 * @param var   : String designating the name of the variable for quest
	 * @param value : String designating the value of the variable for quest
	 */
	public static void updateQuestVarInDb(final QuestState qs, final String var, final String value)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER_QUEST);)
		{
			statement.setString(1, value);
			statement.setInt(2, qs.getPlayer().getObjectId());
			statement.setString(3, qs.getQuestName());
			statement.setString(4, var);
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			LOGGER.error("could not update char quest:", e);
		}
	}
	
	/**
	 * Delete a variable of player's quest from the database.
	 * @param qs  : object QuestState pointing out the player's quest
	 * @param var : String designating the variable characterizing the quest
	 */
	public static void deleteQuestVarInDb(final QuestState qs, final String var)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_CHARACTER_QUEST_VAR))
		{
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			LOGGER.error("Quest.deleteQuestVarInDb : Could not delete char quest for player " + qs.getPlayer().getName() + " with object id " + qs.getPlayer().getObjectId(), e);
		}
	}
	
	/**
	 * Delete the player's quest from database.
	 * @param qs : QuestState pointing out the player's quest
	 */
	public static void deleteQuestInDb(final QuestState qs)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_CHARACTER_QUEST))
		{
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Quest.deleteQuestIbDb : Could not delete char quest for player " + qs.getPlayer().getName() + " with object id " + qs.getPlayer().getObjectId(), e);
		}
	}
	
	/**
	 * Create a record in database for quest.<BR>
	 * <BR>
	 * <U><I>Actions :</I></U><BR>
	 * Use fucntion createQuestVarInDb() with following parameters :<BR>
	 * <LI>QuestState : parameter sq that puts in fields of database :
	 * <UL type="square">
	 * <LI>char_id : ID of the player</LI>
	 * <LI>name : name of the quest</LI>
	 * </UL>
	 * </LI>
	 * <LI>var : string "&lt;state&gt;" as the name of the variable for the quest</LI>
	 * <LI>val : string corresponding at the ID of the state (in fact, initial state)</LI>
	 * @param qs : QuestState
	 */
	public static void createQuestInDb(final QuestState qs)
	{
		createQuestVarInDb(qs, "<state>", qs.getStateId());
	}
	
	/**
	 * Update informations regarding quest in database.<BR>
	 * <U><I>Actions :</I></U><BR>
	 * <LI>Get ID state of the quest recorded in object qs</LI>
	 * <LI>Test if quest is completed. If true, add a star (*) before the ID state</LI>
	 * <LI>Save in database the ID state (with or without the star) for the variable called "&lt;state&gt;" of the quest</LI>
	 * @param qs : QuestState
	 */
	public static void updateQuestInDb(final QuestState qs)
	{
		String val = qs.getStateId();
		// if (qs.isCompleted())
		// val = "*" + val;
		updateQuestVarInDb(qs, "<state>", val);
		val = null;
	}
	
	// returns a random party member's L2PcInstance for the passed player's party
	// returns the passed player if he has no party.
	public L2PcInstance getRandomPartyMember(final L2PcInstance player)
	{
		// NPE prevention. If the player is null, there is nothing to return
		if (player == null)
		{
			return null;
		}
		
		if (player.getParty() == null || player.getParty().getPartyMembers().size() == 0)
		{
			return player;
		}
		
		final L2Party party = player.getParty();
		
		return party.getPartyMembers().get(Rnd.get(party.getPartyMembers().size()));
	}
	
	/**
	 * Auxiliary function for party quests. Note: This function is only here because of how commonly it may be used by quest developers. For any variations on this function, the quest script can always handle things on its own
	 * @param  player the instance of a player whose party is to be searched
	 * @param  value  the value of the "cond" variable that must be matched
	 * @return        L2PcInstance: L2PcInstance for a random party member that matches the specified condition, or null if no match.
	 */
	public L2PcInstance getRandomPartyMember(final L2PcInstance player, final String value)
	{
		return getRandomPartyMember(player, "cond", value);
	}
	
	/**
	 * Auxiliary function for party quests. Note: This function is only here because of how commonly it may be used by quest developers. For any variations on this function, the quest script can always handle things on its own
	 * @param  player the instance of a player whose party is to be searched
	 * @param  var    a tuple specifying a quest condition that must be satisfied for a party member to be considered.
	 * @param  value
	 * @return        L2PcInstance: L2PcInstance for a random party member that matches the specified condition, or null if no match. If the var is null, any random party member is returned (i.e. no condition is applied). The party member must be within 1500 distance from the target of the reference
	 *                player, or if no target exists, 1500 distance from the player itself.
	 */
	public L2PcInstance getRandomPartyMember(final L2PcInstance player, final String var, final String value)
	{
		// if no valid player instance is passed, there is nothing to check...
		if (player == null)
		{
			return null;
		}
		
		// for null var condition, return any random party member.
		if (var == null)
		{
			return getRandomPartyMember(player);
		}
		
		// normal cases...if the player is not in a party, check the player's state
		QuestState temp = null;
		L2Party party = player.getParty();
		
		// if this player is not in a party, just check if this player instance matches the conditions itself
		if (party == null || party.getPartyMembers().size() == 0)
		{
			temp = player.getQuestState(getName());
			if (temp != null && temp.get(var) != null && temp.get(var).equalsIgnoreCase(value))
			{
				return player; // match
			}
			
			return null; // no match
		}
		
		// if the player is in a party, gather a list of all matching party members (possibly
		// including this player)
		final ArrayList<L2PcInstance> candidates = new ArrayList<>();
		
		// get the target for enforcing distance limitations.
		L2Object target = player.getTarget();
		if (target == null)
		{
			target = player;
		}
		
		for (final L2PcInstance partyMember : party.getPartyMembers())
		{
			temp = partyMember.getQuestState(getName());
			if (temp != null && temp.get(var) != null && temp.get(var).equalsIgnoreCase(value) && partyMember.isInsideRadius(target, 1500, true, false))
			{
				candidates.add(partyMember);
			}
		}
		
		// if there was no match, return null...
		if (candidates.size() == 0)
		{
			return null;
		}
		
		temp = null;
		target = null;
		party = null;
		
		// if a match was found from the party, return one of them at random.
		return candidates.get(Rnd.get(candidates.size()));
	}
	
	/**
	 * Auxiliary function for party quests. Note: This function is only here because of how commonly it may be used by quest developers. For any variations on this function, the quest script can always handle things on its own
	 * @param  player the instance of a player whose party is to be searched
	 * @param  state  the state in which the party member's queststate must be in order to be considered.
	 * @return        L2PcInstance: L2PcInstance for a random party member that matches the specified condition, or null if no match. If the var is null, any random party member is returned (i.e. no condition is applied).
	 */
	public L2PcInstance getRandomPartyMemberState(final L2PcInstance player, final State state)
	{
		// if no valid player instance is passed, there is nothing to check...
		if (player == null)
		{
			return null;
		}
		
		// for null var condition, return any random party member.
		if (state == null)
		{
			return getRandomPartyMember(player);
		}
		
		// normal cases...if the player is not in a partym check the player's state
		QuestState temp = null;
		L2Party party = player.getParty();
		// if this player is not in a party, just check if this player instance matches the conditions itself
		if (party == null || party.getPartyMembers().size() == 0)
		{
			temp = player.getQuestState(getName());
			if (temp != null && temp.getState() == state)
			{
				return player; // match
			}
			
			return null; // no match
		}
		
		// if the player is in a party, gather a list of all matching party members (possibly
		// including this player)
		final ArrayList<L2PcInstance> candidates = new ArrayList<>();
		
		// get the target for enforcing distance limitations.
		L2Object target = player.getTarget();
		if (target == null)
		{
			target = player;
		}
		
		for (final L2PcInstance partyMember : party.getPartyMembers())
		{
			temp = partyMember.getQuestState(getName());
			
			if (temp != null && temp.getState() == state && partyMember.isInsideRadius(target, 1500, true, false))
			{
				candidates.add(partyMember);
			}
		}
		
		// if there was no match, return null...
		if (candidates.size() == 0)
		{
			return null;
		}
		
		temp = null;
		party = null;
		target = null;
		
		// if a match was found from the party, return one of them at random.
		return candidates.get(Rnd.get(candidates.size()));
	}
	
	/**
	 * Show HTML file to client
	 * @param  player
	 * @param  fileName
	 * @return          String : message sent to client
	 */
	public String showHtmlFile(final L2PcInstance player, final String fileName)
	{
		final String questId = getName();
		
		// Create handler to file linked to the quest
		final String directory = getDescr().toLowerCase();
		String content = HtmCache.getInstance().getHtm("data/scripts/" + directory + "/" + questId + "/" + fileName);
		
		if (content == null)
		{
			content = HtmCache.getInstance().getHtmForce("data/scripts/quests/" + questId + "/" + fileName);
		}
		
		if (player != null)
		{
			if (player.getTarget() != null)
			{
				content = content.replaceAll("%objectId%", String.valueOf(player.getTarget().getObjectId()));
			}
			
			// Send message to client if message not empty
			if (content != null)
			{
				final NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
				npcReply.setHtml(content);
				npcReply.replace("%playername%", player.getName());
				player.sendPacket(npcReply);
			}
		}
		return content;
	}
	
	// =========================================================
	// QUEST SPAWNS
	// =========================================================
	public L2NpcInstance addSpawn(final int npcId, final L2Character cha)
	{
		return QuestSpawn.getInstance().addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, 0);
	}
	
	public L2NpcInstance addSpawn(final int npcId, final int x, final int y, final int z, final int heading, final boolean randomOffset, final int despawnDelay)
	{
		return QuestSpawn.getInstance().addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay);
	}
	
	public void registerItem(final int itemId)
	{
		if (questItemIds == null)
		{
			questItemIds = new ArrayList<>();
		}
		
		questItemIds.add(itemId);
	}
	
	public List<Integer> getRegisteredItemIds()
	{
		return questItemIds;
	}
	
	@Override
	public ScriptManager<?> getScriptManager()
	{
		return QuestManager.getInstance();
	}
	
	@Override
	public boolean unload()
	{
		saveGlobalData();
		// cancel all pending timers before reloading.
		// if timers ought to be restarted, the quest can take care of it
		// with its code (example: save global data indicating what timer must
		// be restarted).
		
		synchronized (allEventTimers)
		{
			
			for (final ArrayList<QuestTimer> timers : allEventTimers.values())
			{
				for (final QuestTimer timer : timers)
				{
					timer.cancel(false);
				}
			}
			allEventTimers.clear();
			
		}
		
		return QuestManager.getInstance().removeQuest(this);
	}
	
	@Override
	public boolean reload()
	{
		unload();
		return super.reload();
	}
	
	@Override
	public String getScriptName()
	{
		return getName();
	}
	
	/**
	 * This is used to register all monsters contained in mobs for a particular script<BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method register ID for all QuestEventTypes<BR>
	 * Do not use for group_template AIs</B></FONT><BR>
	 * @param mobs
	 * @see        #registerMobs(int[], QuestEventType...)
	 */
	public void registerMobs(final int[] mobs)
	{
		for (final int id : mobs)
		{
			addEventId(id, QuestEventType.ON_ATTACK);
			addEventId(id, QuestEventType.ON_KILL);
			addEventId(id, QuestEventType.ON_SPAWN);
			addEventId(id, QuestEventType.ON_SPELL_FINISHED);
			addEventId(id, QuestEventType.ON_FACTION_CALL);
			addEventId(id, QuestEventType.ON_AGGRO_RANGE_ENTER);
		}
	}
	
	/**
	 * This is used to register all monsters contained in mobs for a particular script event types defined in types.
	 * @param mobs
	 * @param types
	 */
	public void registerMobs(final int[] mobs, final QuestEventType... types)
	{
		for (final int id : mobs)
		{
			for (final QuestEventType type : types)
			{
				addEventId(id, type);
			}
		}
	}
	
}
