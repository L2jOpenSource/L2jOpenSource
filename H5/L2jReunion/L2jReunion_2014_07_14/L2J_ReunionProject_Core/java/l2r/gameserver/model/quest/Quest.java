/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.quest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.GameTimeController;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.datatables.SpawnTable;
import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.datatables.xml.DoorData;
import l2r.gameserver.datatables.xml.ItemData;
import l2r.gameserver.enums.QuestEventType;
import l2r.gameserver.enums.QuestSound;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.instance.L2DoorInstance;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2TrapInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.base.AcquireSkillType;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.model.holders.ItemHolder;
import l2r.gameserver.model.interfaces.IIdentifiable;
import l2r.gameserver.model.interfaces.IPositionable;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.model.itemcontainer.PcInventory;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.olympiad.CompetitionType;
import l2r.gameserver.model.quest.AITasks.AggroRangeEnter;
import l2r.gameserver.model.quest.AITasks.SeeCreature;
import l2r.gameserver.model.quest.AITasks.SkillSee;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.stats.Stats;
import l2r.gameserver.model.zone.L2ZoneType;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.NpcQuestHtmlMessage;
import l2r.gameserver.network.serverpackets.SpecialCamera;
import l2r.gameserver.network.serverpackets.StatusUpdate;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.scripting.ManagedScript;
import l2r.gameserver.scripting.ScriptManager;
import l2r.gameserver.util.MinionList;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quest main class.
 * @author Luis Arias
 */
public class Quest extends ManagedScript implements IIdentifiable
{
	public static final Logger _log = LoggerFactory.getLogger(Quest.class);
	
	/** Map containing lists of timers from the name of the timer. */
	private final Map<String, List<QuestTimer>> _allEventTimers = new ConcurrentHashMap<>();
	private final Set<Integer> _questInvolvedNpcs = new HashSet<>();
	private final ReentrantReadWriteLock _rwLock = new ReentrantReadWriteLock();
	private final WriteLock _writeLock = _rwLock.writeLock();
	private final ReadLock _readLock = _rwLock.readLock();
	
	private final int _questId;
	private final String _name;
	private final String _descr;
	private final byte _initialState = State.CREATED;
	protected boolean _onEnterWorld = false;
	private boolean _isCustom = false;
	private boolean _isOlympiadUse = false;
	
	public int[] questItemIds = null;
	
	private static final String DEFAULT_NO_QUEST_MSG = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
	private static final String DEFAULT_ALREADY_COMPLETED_MSG = "<html><body>This quest has already been completed.</body></html>";
	
	private static final String QUEST_DELETE_FROM_CHAR_QUERY = "DELETE FROM character_quests WHERE charId=? AND name=?";
	private static final String QUEST_DELETE_FROM_CHAR_QUERY_NON_REPEATABLE_QUERY = "DELETE FROM character_quests WHERE charId=? AND name=? AND var!=?";
	
	private static final int RESET_HOUR = 6;
	private static final int RESET_MINUTES = 30;
	
	/**
	 * @return the reset hour for a daily quest, could be overridden on a script.
	 */
	public int getResetHour()
	{
		return RESET_HOUR;
	}
	
	/**
	 * @return the reset minutes for a daily quest, could be overridden on a script.
	 */
	public int getResetMinutes()
	{
		return RESET_MINUTES;
	}
	
	/**
	 * The Quest object constructor.<br>
	 * Constructing a quest also calls the {@code init_LoadGlobalData} convenience method.
	 * @param questId int pointing out the Id of the quest.
	 * @param name String corresponding to the name of the quest.
	 * @param descr String for the description of the quest.
	 */
	public Quest(int questId, String name, String descr)
	{
		_questId = questId;
		_name = name;
		_descr = descr;
		if (questId > 0)
		{
			QuestManager.getInstance().addQuest(this);
		}
		else
		{
			QuestManager.getInstance().addScript(this);
		}
		
		loadGlobalData();
	}
	
	/**
	 * This method is, by default, called by the constructor of all scripts.<br>
	 * Children of this class can implement this function in order to define what variables to load and what structures to save them in.<br>
	 * By default, nothing is loaded.
	 */
	protected void loadGlobalData()
	{
		
	}
	
	/**
	 * The function saveGlobalData is, by default, called at shutdown, for all quests, by the QuestManager.<br>
	 * Children of this class can implement this function in order to convert their structures<br>
	 * into <var, value> tuples and make calls to save them to the database, if needed.<br>
	 * By default, nothing is saved.
	 */
	public void saveGlobalData()
	{
		
	}
	
	/**
	 * Trap actions:<br>
	 * <ul>
	 * <li>Triggered</li>
	 * <li>Detected</li>
	 * <li>Disarmed</li>
	 * </ul>
	 */
	public static enum TrapAction
	{
		TRAP_TRIGGERED,
		TRAP_DETECTED,
		TRAP_DISARMED
	}
	
	/**
	 * Gets the quest ID.
	 * @return the quest ID
	 */
	@Override
	public int getId()
	{
		return _questId;
	}
	
	/**
	 * Add a new quest state of this quest to the database.
	 * @param player the owner of the newly created quest state
	 * @return the newly created {@link QuestState} object
	 */
	public QuestState newQuestState(L2PcInstance player)
	{
		return new QuestState(this, player, _initialState);
	}
	
	/**
	 * Get the specified player's {@link QuestState} object for this quest.<br>
	 * If the player does not have it and initIfNode is {@code true},<br>
	 * create a new QuestState object and return it, otherwise return {@code null}.
	 * @param player the player whose QuestState to get
	 * @param initIfNone if true and the player does not have a QuestState for this quest,<br>
	 *            create a new QuestState
	 * @return the QuestState object for this quest or null if it doesn't exist
	 */
	public QuestState getQuestState(L2PcInstance player, boolean initIfNone)
	{
		final QuestState qs = player.getQuestState(_name);
		if ((qs != null) || !initIfNone)
		{
			return qs;
		}
		return newQuestState(player);
	}
	
	/**
	 * @return the initial state of the quest
	 */
	public byte getInitialState()
	{
		return _initialState;
	}
	
	/**
	 * @return the name of the quest
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * @return the description of the quest
	 */
	public String getDescr()
	{
		return _descr;
	}
	
	/**
	 * Add a timer to the quest (if it doesn't exist already) and start it.
	 * @param name the name of the timer (also passed back as "event" in {@link #onAdvEvent(String, L2Npc, L2PcInstance)})
	 * @param time time in ms for when to fire the timer
	 * @param npc the npc associated with this timer (can be null)
	 * @param player the player associated with this timer (can be null)
	 * @see #startQuestTimer(String, long, L2Npc, L2PcInstance, boolean)
	 */
	public void startQuestTimer(String name, long time, L2Npc npc, L2PcInstance player)
	{
		startQuestTimer(name, time, npc, player, false);
	}
	
	/**
	 * Add a timer to the quest (if it doesn't exist already) and start it.
	 * @param name the name of the timer (also passed back as "event" in {@link #onAdvEvent(String, L2Npc, L2PcInstance)})
	 * @param time time in ms for when to fire the timer
	 * @param npc the npc associated with this timer (can be null)
	 * @param player the player associated with this timer (can be null)
	 * @param repeating indicates whether the timer is repeatable or one-time.<br>
	 *            If {@code true}, the task is repeated every {@code time} milliseconds until explicitly stopped.
	 */
	public void startQuestTimer(String name, long time, L2Npc npc, L2PcInstance player, boolean repeating)
	{
		final List<QuestTimer> timers = _allEventTimers.computeIfAbsent(name, k -> new ArrayList<>());
		// if there exists a timer with this name, allow the timer only if the [npc, player] set is unique
		// nulls act as wildcards
		if (getQuestTimer(name, npc, player) == null)
		{
			_writeLock.lock();
			try
			{
				timers.add(new QuestTimer(this, name, time, npc, player, repeating));
			}
			finally
			{
				_writeLock.unlock();
			}
		}
	}
	
	/**
	 * Get a quest timer that matches the provided name and parameters.
	 * @param name the name of the quest timer to get
	 * @param npc the NPC associated with the quest timer to get
	 * @param player the player associated with the quest timer to get
	 * @return the quest timer that matches the parameters of this function or {@code null} if nothing was found
	 */
	public QuestTimer getQuestTimer(String name, L2Npc npc, L2PcInstance player)
	{
		final List<QuestTimer> timers = _allEventTimers.get(name);
		if (timers != null)
		{
			_readLock.lock();
			try
			{
				for (QuestTimer timer : timers)
				{
					if (timer != null)
					{
						if (timer.isMatch(this, name, npc, player))
						{
							return timer;
						}
					}
				}
			}
			finally
			{
				_readLock.unlock();
			}
		}
		return null;
	}
	
	/**
	 * Cancel all quest timers with the specified name.
	 * @param name the name of the quest timers to cancel
	 */
	public void cancelQuestTimers(String name)
	{
		final List<QuestTimer> timers = _allEventTimers.get(name);
		if (timers != null)
		{
			_writeLock.lock();
			try
			{
				for (QuestTimer timer : timers)
				{
					if (timer != null)
					{
						timer.cancel();
					}
				}
				timers.clear();
			}
			finally
			{
				_writeLock.unlock();
			}
		}
	}
	
	public Map<String, List<QuestTimer>> getQuestTimers()
	{
		return _allEventTimers;
	}
	
	/**
	 * Cancel the quest timer that matches the specified name and parameters.
	 * @param name the name of the quest timer to cancel
	 * @param npc the NPC associated with the quest timer to cancel
	 * @param player the player associated with the quest timer to cancel
	 */
	public void cancelQuestTimer(String name, L2Npc npc, L2PcInstance player)
	{
		final QuestTimer timer = getQuestTimer(name, npc, player);
		if (timer != null)
		{
			timer.cancelAndRemove();
		}
	}
	
	/**
	 * Remove a quest timer from the list of all timers.<br>
	 * Note: does not stop the timer itself!
	 * @param timer the {@link QuestState} object to remove
	 */
	public void removeQuestTimer(QuestTimer timer)
	{
		if (timer != null)
		{
			final List<QuestTimer> timers = _allEventTimers.get(timer.getName());
			if (timers != null)
			{
				_writeLock.lock();
				try
				{
					timers.remove(timer);
				}
				finally
				{
					_writeLock.unlock();
				}
			}
		}
	}
	
	/**
	 * @param npc
	 * @param player
	 * @return {@code true} if player can see this npc, {@code false} otherwise.
	 */
	public final boolean notifyOnCanSeeMe(L2Npc npc, L2PcInstance player)
	{
		try
		{
			return onCanSeeMe(npc, player);
		}
		catch (Exception e)
		{
			_log.warn("Exception on onCanSeeMe() in notifyOnCanSeeMe(): " + e.getMessage(), e);
		}
		return false;
	}
	
	// These are methods to call within the core to call the quest events.
	
	/**
	 * @param npc the NPC that was attacked
	 * @param attacker the attacking player
	 * @param damage the damage dealt to the NPC by the player
	 * @param isSummon if {@code true}, the attack was actually made by the player's summon
	 * @param skill the skill used to attack the NPC (can be null)
	 */
	public final void notifyAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onAttack(npc, attacker, damage, isSummon, skill);
		}
		catch (Exception e)
		{
			showError(attacker, e);
			return;
		}
		showResult(attacker, res);
	}
	
	/**
	 * @param killer the character that killed the {@code victim}
	 * @param victim the character that was killed by the {@code killer}
	 * @param qs the quest state object of the player to be notified of this event
	 */
	public final void notifyDeath(L2Character killer, L2Character victim, QuestState qs)
	{
		String res = null;
		try
		{
			res = onDeath(killer, victim, qs);
		}
		catch (Exception e)
		{
			showError(qs.getPlayer(), e);
		}
		showResult(qs.getPlayer(), res);
	}
	
	/**
	 * @param item
	 * @param player
	 */
	public final void notifyItemUse(L2Item item, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onItemUse(item, player);
		}
		catch (Exception e)
		{
			showError(player, e);
		}
		showResult(player, res);
	}
	
	/**
	 * @param instance
	 * @param player
	 * @param skill
	 */
	public final void notifySpellFinished(L2Npc instance, L2PcInstance player, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onSpellFinished(instance, player, skill);
		}
		catch (Exception e)
		{
			showError(player, e);
		}
		showResult(player, res);
	}
	
	/**
	 * Notify quest script when something happens with a trap.
	 * @param trap the trap instance which triggers the notification
	 * @param trigger the character which makes effect on the trap
	 * @param action 0: trap casting its skill. 1: trigger detects the trap. 2: trigger removes the trap
	 */
	public final void notifyTrapAction(L2TrapInstance trap, L2Character trigger, TrapAction action)
	{
		String res = null;
		try
		{
			res = onTrapAction(trap, trigger, action);
		}
		catch (Exception e)
		{
			if (trigger.getActingPlayer() != null)
			{
				showError(trigger.getActingPlayer(), e);
			}
			_log.warn("Exception on onTrapAction() in notifyTrapAction(): " + e.getMessage(), e);
			return;
		}
		if (trigger.getActingPlayer() != null)
		{
			showResult(trigger.getActingPlayer(), res);
		}
	}
	
	/**
	 * @param npc the spawned NPC
	 */
	public final void notifySpawn(L2Npc npc)
	{
		try
		{
			onSpawn(npc);
		}
		catch (Exception e)
		{
			_log.warn("Exception on onSpawn() in notifySpawn(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * @param event
	 * @param npc
	 * @param player
	 * @return {@code false} if there was an error or the message was sent, {@code true} otherwise
	 */
	public final boolean notifyEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onAdvEvent(event, npc, player);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	/**
	 * @param player the player entering the world
	 */
	public final void notifyEnterWorld(L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onEnterWorld(player);
		}
		catch (Exception e)
		{
			showError(player, e);
		}
		showResult(player, res);
	}
	
	/**
	 * @param npc
	 * @param killer
	 * @param isSummon
	 */
	public final void notifyKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		String res = null;
		try
		{
			res = onKill(npc, killer, isSummon);
		}
		catch (Exception e)
		{
			showError(killer, e);
		}
		showResult(killer, res);
	}
	
	/**
	 * @param npc
	 * @param qs
	 * @return {@code false} if there was an error or the message was sent, {@code true} otherwise
	 */
	public final boolean notifyTalk(L2Npc npc, QuestState qs)
	{
		String res = null;
		try
		{
			res = onTalk(npc, qs.getPlayer());
		}
		catch (Exception e)
		{
			return showError(qs.getPlayer(), e);
		}
		qs.getPlayer().setLastQuestNpcObject(npc.getObjectId());
		return showResult(qs.getPlayer(), res);
	}
	
	/**
	 * Override the default NPC dialogs when a quest defines this for the given NPC.<br>
	 * Note: If the default html for this npc needs to be shown, onFirstTalk should call npc.showChatWindow(player) and then return null.
	 * @param npc the NPC whose dialogs to override
	 * @param player the player talking to the NPC
	 */
	public final void notifyFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onFirstTalk(npc, player);
		}
		catch (Exception e)
		{
			showError(player, e);
		}
		showResult(player, res);
	}
	
	/**
	 * TODO: Remove and replace with listeners.
	 * @param npc
	 * @param player
	 */
	public final void notifyAcquireSkillList(L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onAcquireSkillList(npc, player);
		}
		catch (Exception e)
		{
			showError(player, e);
		}
		showResult(player, res);
	}
	
	/**
	 * Notify the quest engine that an skill info has been acquired.
	 * @param npc the NPC
	 * @param player the player
	 * @param skill the skill
	 */
	public final void notifyAcquireSkillInfo(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onAcquireSkillInfo(npc, player, skill);
		}
		catch (Exception e)
		{
			showError(player, e);
		}
		showResult(player, res);
	}
	
	/**
	 * Notify the quest engine that an skill has been acquired.
	 * @param npc the NPC
	 * @param player the player
	 * @param skill the skill
	 * @param type the skill learn type
	 */
	public final void notifyAcquireSkill(L2Npc npc, L2PcInstance player, L2Skill skill, AcquireSkillType type)
	{
		String res = null;
		try
		{
			res = onAcquireSkill(npc, player, skill, type);
		}
		catch (Exception e)
		{
			showError(player, e);
		}
		showResult(player, res);
	}
	
	/**
	 * @param item
	 * @param player
	 * @return
	 */
	public final boolean notifyItemTalk(L2ItemInstance item, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onItemTalk(item, player);
			if (res != null)
			{
				if (res.equalsIgnoreCase("true"))
				{
					return true;
				}
				else if (res.equalsIgnoreCase("false"))
				{
					return false;
				}
			}
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	/**
	 * @param item
	 * @param player
	 * @return
	 */
	public String onItemTalk(L2ItemInstance item, L2PcInstance player)
	{
		return null;
	}
	
	/**
	 * @param item
	 * @param player
	 * @param event
	 * @return
	 */
	public final boolean notifyItemEvent(L2ItemInstance item, L2PcInstance player, String event)
	{
		String res = null;
		try
		{
			res = onItemEvent(item, player, event);
			if (res != null)
			{
				if (res.equalsIgnoreCase("true"))
				{
					return true;
				}
				else if (res.equalsIgnoreCase("false"))
				{
					return false;
				}
			}
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	/**
	 * @param npc
	 * @param caster
	 * @param skill
	 * @param targets
	 * @param isSummon
	 */
	public final void notifySkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		ThreadPoolManager.getInstance().executeAi(new SkillSee(this, npc, caster, skill, targets, isSummon));
	}
	
	/**
	 * @param npc
	 * @param caller
	 * @param attacker
	 * @param isSummon
	 */
	public final void notifyFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isSummon)
	{
		String res = null;
		try
		{
			res = onFactionCall(npc, caller, attacker, isSummon);
		}
		catch (Exception e)
		{
			showError(attacker, e);
		}
		showResult(attacker, res);
	}
	
	/**
	 * @param npc
	 * @param player
	 * @param isSummon
	 */
	public final void notifyAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		ThreadPoolManager.getInstance().executeAi(new AggroRangeEnter(this, npc, player, isSummon));
	}
	
	/**
	 * @param npc the NPC that sees the creature
	 * @param creature the creature seen by the NPC
	 * @param isSummon
	 */
	public final void notifySeeCreature(L2Npc npc, L2Character creature, boolean isSummon)
	{
		ThreadPoolManager.getInstance().executeAi(new SeeCreature(this, npc, creature, isSummon));
	}
	
	/**
	 * @param eventName - name of event
	 * @param sender - NPC, who sent event
	 * @param receiver - NPC, who received event
	 * @param reference - L2Object to pass, if needed
	 */
	public final void notifyEventReceived(String eventName, L2Npc sender, L2Npc receiver, L2Object reference)
	{
		try
		{
			onEventReceived(eventName, sender, receiver, reference);
		}
		catch (Exception e)
		{
			_log.warn("Exception on onEventReceived() in notifyEventReceived(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * @param character
	 * @param zone
	 */
	public final void notifyEnterZone(L2Character character, L2ZoneType zone)
	{
		L2PcInstance player = character.getActingPlayer();
		String res = null;
		try
		{
			res = onEnterZone(character, zone);
		}
		catch (Exception e)
		{
			if (player != null)
			{
				showError(player, e);
			}
		}
		if (player != null)
		{
			showResult(player, res);
		}
	}
	
	/**
	 * @param character
	 * @param zone
	 */
	public final void notifyExitZone(L2Character character, L2ZoneType zone)
	{
		L2PcInstance player = character.getActingPlayer();
		String res = null;
		try
		{
			res = onExitZone(character, zone);
		}
		catch (Exception e)
		{
			if (player != null)
			{
				showError(player, e);
			}
		}
		if (player != null)
		{
			showResult(player, res);
		}
	}
	
	/**
	 * @param winner
	 * @param type {@code false} if there was an error, {@code true} otherwise
	 */
	public final void notifyOlympiadWin(L2PcInstance winner, CompetitionType type)
	{
		try
		{
			onOlympiadWin(winner, type);
		}
		catch (Exception e)
		{
			showError(winner, e);
		}
	}
	
	/**
	 * @param loser
	 * @param type {@code false} if there was an error, {@code true} otherwise
	 */
	public final void notifyOlympiadLose(L2PcInstance loser, CompetitionType type)
	{
		try
		{
			onOlympiadLose(loser, type);
		}
		catch (Exception e)
		{
			showError(loser, e);
		}
	}
	
	/**
	 * @param npc
	 */
	public final void notifyMoveFinished(L2Npc npc)
	{
		try
		{
			onMoveFinished(npc);
		}
		catch (Exception e)
		{
			_log.warn("Exception on onMoveFinished() in notifyMoveFinished(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * @param npc
	 */
	public final void notifyNodeArrived(L2Npc npc)
	{
		try
		{
			onNodeArrived(npc);
		}
		catch (Exception e)
		{
			_log.warn("Exception on onNodeArrived() in notifyNodeArrived(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * @param npc
	 */
	public final void notifyRouteFinished(L2Npc npc)
	{
		try
		{
			onRouteFinished(npc);
		}
		catch (Exception e)
		{
			_log.warn("Exception on onRouteFinished() in notifyRouteFinished(): " + e.getMessage(), e);
		}
	}
	
	// These are methods that java calls to invoke scripts.
	
	/**
	 * This function is called in place of {@link #onAttack(L2Npc, L2PcInstance, int, boolean, L2Skill)} if the former is not implemented.<br>
	 * If a script contains both onAttack(..) implementations, then this method will never be called unless the script's {@link #onAttack(L2Npc, L2PcInstance, int, boolean, L2Skill)} explicitly calls this method.
	 * @param npc this parameter contains a reference to the exact instance of the NPC that got attacked the NPC.
	 * @param attacker this parameter contains a reference to the exact instance of the player who attacked.
	 * @param damage this parameter represents the total damage that this attack has inflicted to the NPC.
	 * @param isSummon this parameter if it's {@code false} it denotes that the attacker was indeed the player, else it specifies that the damage was actually dealt by the player's pet.
	 * @return
	 */
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		return null;
	}
	
	/**
	 * This function is called whenever a player attacks an NPC that is registered for the quest.<br>
	 * If is not overridden by a subclass, then default to the returned value of the simpler (and older) {@link #onAttack(L2Npc, L2PcInstance, int, boolean)} override.<br>
	 * @param npc this parameter contains a reference to the exact instance of the NPC that got attacked.
	 * @param attacker this parameter contains a reference to the exact instance of the player who attacked the NPC.
	 * @param damage this parameter represents the total damage that this attack has inflicted to the NPC.
	 * @param isSummon this parameter if it's {@code false} it denotes that the attacker was indeed the player, else it specifies that the damage was actually dealt by the player's summon
	 * @param skill parameter is the skill that player used to attack NPC.
	 * @return
	 */
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, L2Skill skill)
	{
		return onAttack(npc, attacker, damage, isSummon);
	}
	
	/**
	 * This function is called whenever an <b>exact instance</b> of a character who was previously registered for this event dies.<br>
	 * The registration for {@link #onDeath(L2Character, L2Character, QuestState)} events <b>is not</b> done via the quest itself, but it is instead handled by the QuestState of a particular player.
	 * @param killer this parameter contains a reference to the exact instance of the NPC that <b>killed</b> the character.
	 * @param victim this parameter contains a reference to the exact instance of the character that got killed.
	 * @param qs this parameter contains a reference to the QuestState of whomever was interested (waiting) for this kill.
	 * @return
	 */
	public String onDeath(L2Character killer, L2Character victim, QuestState qs)
	{
		return onAdvEvent("", ((killer instanceof L2Npc) ? ((L2Npc) killer) : null), qs.getPlayer());
	}
	
	/**
	 * This function is called whenever a player clicks on a link in a quest dialog and whenever a timer fires.<br>
	 * If is not overridden by a subclass, then default to the returned value of the simpler (and older) {@link #onEvent(String, QuestState)} override.<br>
	 * If the player has a quest state, use it as parameter in the next call, otherwise return null.
	 * @param event this parameter contains a string identifier for the event.<br>
	 *            Generally, this string is passed directly via the link.<br>
	 *            For example:<br>
	 *            <code>
	 *            &lt;a action="bypass -h Quest 626_ADarkTwilight 31517-01.htm"&gt;hello&lt;/a&gt;
	 *            </code><br>
	 *            The above link sets the event variable to "31517-01.htm" for the quest 626_ADarkTwilight.<br>
	 *            In the case of timers, this will be the name of the timer.<br>
	 *            This parameter serves as a sort of identifier.
	 * @param npc this parameter contains a reference to the instance of NPC associated with this event.<br>
	 *            This may be the NPC registered in a timer, or the NPC with whom a player is speaking, etc.<br>
	 *            This parameter may be {@code null} in certain circumstances.
	 * @param player this parameter contains a reference to the player participating in this function.<br>
	 *            It may be the player speaking to the NPC, or the player who caused a timer to start (and owns that timer).<br>
	 *            This parameter may be {@code null} in certain circumstances.
	 * @return the text returned by the event (may be {@code null}, a filename or just text)
	 */
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (player != null)
		{
			final QuestState qs = player.getQuestState(getName());
			if (qs != null)
			{
				return onEvent(event, qs);
			}
		}
		return null;
	}
	
	/**
	 * This function is called in place of {@link #onAdvEvent(String, L2Npc, L2PcInstance)} if the former is not implemented.<br>
	 * If a script contains both {@link #onAdvEvent(String, L2Npc, L2PcInstance)} and this implementation, then this method will never be called unless the script's {@link #onAdvEvent(String, L2Npc, L2PcInstance)} explicitly calls this method.
	 * @param event this parameter contains a string identifier for the event.<br>
	 *            Generally, this string is passed directly via the link.<br>
	 *            For example:<br>
	 *            <code>
	 *            &lt;a action="bypass -h Quest 626_ADarkTwilight 31517-01.htm"&gt;hello&lt;/a&gt;
	 *            </code><br>
	 *            The above link sets the event variable to "31517-01.htm" for the quest 626_ADarkTwilight.<br>
	 *            In the case of timers, this will be the name of the timer.<br>
	 *            This parameter serves as a sort of identifier.
	 * @param qs this parameter contains a reference to the quest state of the player who used the link or started the timer.
	 * @return the text returned by the event (may be {@code null}, a filename or just text)
	 */
	public String onEvent(String event, QuestState qs)
	{
		return null;
	}
	
	/**
	 * This function is called whenever a player kills a NPC that is registered for the quest.
	 * @param npc this parameter contains a reference to the exact instance of the NPC that got killed.
	 * @param killer this parameter contains a reference to the exact instance of the player who killed the NPC.
	 * @param isSummon this parameter if it's {@code false} it denotes that the attacker was indeed the player, else it specifies that the killer was the player's pet.
	 * @return the text returned by the event (may be {@code null}, a filename or just text)
	 */
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		return null;
	}
	
	/**
	 * This function is called whenever a player clicks to the "Quest" link of an NPC that is registered for the quest.
	 * @param npc this parameter contains a reference to the exact instance of the NPC that the player is talking with.
	 * @param talker this parameter contains a reference to the exact instance of the player who is talking to the NPC.
	 * @return the text returned by the event (may be {@code null}, a filename or just text)
	 */
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		return null;
	}
	
	/**
	 * This function is called whenever a player talks to an NPC that is registered for the quest.<br>
	 * That is, it is triggered from the very first click on the NPC, not via another dialog.<br>
	 * <b>Note 1:</b><br>
	 * Each NPC can be registered to at most one quest for triggering this function.<br>
	 * In other words, the same one NPC cannot respond to an "onFirstTalk" request from two different quests.<br>
	 * Attempting to register an NPC in two different quests for this function will result in one of the two registration being ignored.<br>
	 * <b>Note 2:</b><br>
	 * Since a Quest link isn't clicked in order to reach this, a quest state can be invalid within this function.<br>
	 * The coder of the script may need to create a new quest state (if necessary).<br>
	 * <b>Note 3:</b><br>
	 * The returned value of onFirstTalk replaces the default HTML that would have otherwise been loaded from a sub-folder of DatapackRoot/game/data/html/.<br>
	 * If you wish to show the default HTML, within onFirstTalk do npc.showChatWindow(player) and then return ""<br>
	 * @param npc this parameter contains a reference to the exact instance of the NPC that the player is talking with.
	 * @param player this parameter contains a reference to the exact instance of the player who is talking to the NPC.
	 * @return the text returned by the event (may be {@code null}, a filename or just text)
	 * @since <a href="http://trac.l2jserver.com/changeset/771">Jython AI support for "onFirstTalk"</a>
	 */
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return null;
	}
	
	/**
	 * @param item
	 * @param player
	 * @param event
	 * @return
	 */
	public String onItemEvent(L2ItemInstance item, L2PcInstance player, String event)
	{
		return null;
	}
	
	/**
	 * This function is called whenever a player request a skill list.<br>
	 * TODO: Re-implement, since Skill Trees rework it's support was removed.
	 * @param npc this parameter contains a reference to the exact instance of the NPC that the player requested the skill list.
	 * @param player this parameter contains a reference to the exact instance of the player who requested the skill list.
	 * @return
	 */
	public String onAcquireSkillList(L2Npc npc, L2PcInstance player)
	{
		return null;
	}
	
	/**
	 * This function is called whenever a player request a skill info.
	 * @param npc this parameter contains a reference to the exact instance of the NPC that the player requested the skill info.
	 * @param player this parameter contains a reference to the exact instance of the player who requested the skill info.
	 * @param skill this parameter contains a reference to the skill that the player requested its info.
	 * @return
	 */
	public String onAcquireSkillInfo(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}
	
	/**
	 * This function is called whenever a player acquire a skill.<br>
	 * TODO: Re-implement, since Skill Trees rework it's support was removed.
	 * @param npc this parameter contains a reference to the exact instance of the NPC that the player requested the skill.
	 * @param player this parameter contains a reference to the exact instance of the player who requested the skill.
	 * @param skill this parameter contains a reference to the skill that the player requested.
	 * @param type the skill learn type
	 * @return
	 */
	public String onAcquireSkill(L2Npc npc, L2PcInstance player, L2Skill skill, AcquireSkillType type)
	{
		return null;
	}
	
	/**
	 * This function is called whenever a player uses a quest item that has a quest events list.<br>
	 * TODO: complete this documentation and unhardcode it to work with all item uses not with those listed.
	 * @param item the quest item that the player used
	 * @param player the player who used the item
	 * @return
	 */
	public String onItemUse(L2Item item, L2PcInstance player)
	{
		return null;
	}
	
	/**
	 * This function is called whenever a player casts a skill near a registered NPC (1000 distance).<br>
	 * <b>Note:</b><br>
	 * If a skill does damage, both onSkillSee(..) and onAttack(..) will be triggered for the damaged NPC!<br>
	 * However, only onSkillSee(..) will be triggered if the skill does no damage,<br>
	 * or if it damages an NPC who has no onAttack(..) registration while near another NPC who has an onSkillSee registration.<br>
	 * TODO: confirm if the distance is 1000 and unhardcode.
	 * @param npc the NPC that saw the skill
	 * @param caster the player who cast the skill
	 * @param skill the actual skill that was used
	 * @param targets an array of all objects (can be any type of object, including mobs and players) that were affected by the skill
	 * @param isSummon if {@code true}, the skill was actually cast by the player's summon, not the player himself
	 * @return
	 */
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		return null;
	}
	
	/**
	 * This function is called whenever an NPC finishes casting a skill.
	 * @param npc the NPC that casted the skill.
	 * @param player the player who is the target of the skill. Can be {@code null}.
	 * @param skill the actual skill that was used by the NPC.
	 * @return
	 */
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}
	
	/**
	 * This function is called whenever a trap action is performed.
	 * @param trap this parameter contains a reference to the exact instance of the trap that was activated.
	 * @param trigger this parameter contains a reference to the exact instance of the character that triggered the action.
	 * @param action this parameter contains a reference to the action that was triggered.
	 * @return
	 */
	public String onTrapAction(L2TrapInstance trap, L2Character trigger, TrapAction action)
	{
		return null;
	}
	
	/**
	 * This function is called whenever an NPC spawns or re-spawns and passes a reference to the newly (re)spawned NPC.<br>
	 * Currently the only function that has no reference to a player.<br>
	 * It is useful for initializations, starting quest timers, displaying chat (NpcSay), and more.
	 * @param npc this parameter contains a reference to the exact instance of the NPC who just (re)spawned.
	 * @return
	 */
	public String onSpawn(L2Npc npc)
	{
		return null;
	}
	
	/**
	 * This function is called whenever an NPC is called by another NPC in the same faction.
	 * @param npc this parameter contains a reference to the exact instance of the NPC who is being asked for help.
	 * @param caller this parameter contains a reference to the exact instance of the NPC who is asking for help.<br>
	 * @param attacker this parameter contains a reference to the exact instance of the player who attacked.
	 * @param isSummon this parameter if it's {@code false} it denotes that the attacker was indeed the player, else it specifies that the attacker was the player's summon.
	 * @return
	 */
	public String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isSummon)
	{
		return null;
	}
	
	/**
	 * This function is called whenever a player enters an NPC aggression range.
	 * @param npc this parameter contains a reference to the exact instance of the NPC whose aggression range is being transgressed.
	 * @param player this parameter contains a reference to the exact instance of the player who is entering the NPC's aggression range.
	 * @param isSummon this parameter if it's {@code false} it denotes that the character that entered the aggression range was indeed the player, else it specifies that the character was the player's summon.
	 * @return
	 */
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		return null;
	}
	
	/**
	 * This function is called whenever a NPC "sees" a creature.
	 * @param npc the NPC who sees the creature
	 * @param creature the creature seen by the NPC
	 * @param isSummon this parameter if it's {@code false} it denotes that the character seen by the NPC was indeed the player, else it specifies that the character was the player's summon
	 * @return
	 */
	public String onSeeCreature(L2Npc npc, L2Character creature, boolean isSummon)
	{
		return null;
	}
	
	/**
	 * This function is called whenever a player enters the game.
	 * @param player this parameter contains a reference to the exact instance of the player who is entering to the world.
	 * @return
	 */
	public String onEnterWorld(L2PcInstance player)
	{
		return null;
	}
	
	/**
	 * This function is called whenever a character enters a registered zone.
	 * @param character this parameter contains a reference to the exact instance of the character who is entering the zone.
	 * @param zone this parameter contains a reference to the zone.
	 * @return
	 */
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		return null;
	}
	
	/**
	 * This function is called whenever a character exits a registered zone.
	 * @param character this parameter contains a reference to the exact instance of the character who is exiting the zone.
	 * @param zone this parameter contains a reference to the zone.
	 * @return
	 */
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		return null;
	}
	
	public void onMoveFinished(L2Npc npc)
	{
		
	}
	
	/**
	 * @param eventName - name of event
	 * @param sender - NPC, who sent event
	 * @param receiver - NPC, who received event
	 * @param reference - L2Object to pass, if needed
	 * @return
	 */
	public String onEventReceived(String eventName, L2Npc sender, L2Npc receiver, L2Object reference)
	{
		return null;
	}
	
	/**
	 * This function is called whenever a player wins an Olympiad Game.
	 * @param winner this parameter contains a reference to the exact instance of the player who won the competition.
	 * @param type this parameter contains a reference to the competition type.
	 */
	public void onOlympiadWin(L2PcInstance winner, CompetitionType type)
	{
		
	}
	
	/**
	 * This function is called whenever a player looses an Olympiad Game.
	 * @param loser this parameter contains a reference to the exact instance of the player who lose the competition.
	 * @param type this parameter contains a reference to the competition type.
	 */
	public void onOlympiadLose(L2PcInstance loser, CompetitionType type)
	{
		
	}
	
	/**
	 * @param npc
	 * @param player
	 * @return {@code true} if player can see this npc, {@code false} otherwise.
	 */
	public boolean onCanSeeMe(L2Npc npc, L2PcInstance player)
	{
		return false;
	}
	
	/**
	 * This function is called whenever a walker NPC (controlled by WalkingManager) arrive a walking node
	 * @param npc registered NPC
	 */
	public void onNodeArrived(L2Npc npc)
	{
		
	}
	
	/**
	 * This function is called whenever a walker NPC (controlled by WalkingManager) arrive to last node
	 * @param npc registered NPC
	 */
	public void onRouteFinished(L2Npc npc)
	{
		
	}
	
	/**
	 * @param mob
	 * @param playable
	 * @return {@code true} if npc can hate the playable, {@code false} otherwise.
	 */
	public boolean onNpcHate(L2Attackable mob, L2Playable playable)
	{
		return true;
	}
	
	/**
	 * Show an error message to the specified player.
	 * @param player the player to whom to send the error (must be a GM)
	 * @param t the {@link Throwable} to get the message/stacktrace from
	 * @return {@code false}
	 */
	public boolean showError(L2PcInstance player, Throwable t)
	{
		_log.warn(getScriptFile().getAbsolutePath(), t);
		if (t.getMessage() == null)
		{
			_log.warn(getClass().getSimpleName() + ": " + t.getMessage());
		}
		if ((player != null) && player.getAccessLevel().isGm())
		{
			String res = "<html><body><title>Script error</title>" + Util.getStackTrace(t) + "</body></html>";
			return showResult(player, res);
		}
		return false;
	}
	
	/**
	 * Show a message to the specified player.<br>
	 * <u><i>Concept:</i></u><br>
	 * Three cases are managed according to the value of the {@code res} parameter:<br>
	 * <ul>
	 * <li><u>{@code res} ends with ".htm" or ".html":</u> the contents of the specified HTML file are shown in a dialog window</li>
	 * <li><u>{@code res} starts with "&lt;html&gt;":</u> the contents of the parameter are shown in a dialog window</li>
	 * <li><u>all other cases :</u> the text contained in the parameter is shown in chat</li>
	 * </ul>
	 * @param player the player to whom to show the result
	 * @param res the message to show to the player
	 * @return {@code false} if the message was sent, {@code true} otherwise
	 */
	public boolean showResult(L2PcInstance player, String res)
	{
		if ((res == null) || res.isEmpty() || (player == null))
		{
			return true;
		}
		
		if (res.endsWith(".htm") || res.endsWith(".html"))
		{
			showHtmlFile(player, res);
		}
		else if (res.startsWith("<html>"))
		{
			NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(res);
			npcReply.replace("%playername%", player.getName());
			player.sendPacket(npcReply);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.sendMessage(res);
		}
		return false;
	}
	
	/**
	 * Loads all quest states and variables for the specified player.
	 * @param player the player who is entering the world
	 */
	public static final void playerEnter(L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement invalidQuestData = con.prepareStatement("DELETE FROM character_quests WHERE charId = ? AND name = ?");
			PreparedStatement invalidQuestDataVar = con.prepareStatement("DELETE FROM character_quests WHERE charId = ? AND name = ? AND var = ?");
			PreparedStatement ps1 = con.prepareStatement("SELECT name, value FROM character_quests WHERE charId = ? AND var = ?"))
		{
			// Get list of quests owned by the player from database
			
			ps1.setInt(1, player.getObjectId());
			ps1.setString(2, "<state>");
			try (ResultSet rs = ps1.executeQuery())
			{
				while (rs.next())
				{
					// Get Id of the quest and Id of its state
					String questId = rs.getString("name");
					String statename = rs.getString("value");
					
					// Search quest associated with the Id
					Quest q = QuestManager.getInstance().getQuest(questId);
					if (q == null)
					{
						_log.info("Unknown quest " + questId + " for player " + player.getName());
						if (Config.AUTODELETE_INVALID_QUEST_DATA)
						{
							invalidQuestData.setInt(1, player.getObjectId());
							invalidQuestData.setString(2, questId);
							invalidQuestData.executeUpdate();
						}
						continue;
					}
					
					// Create a new QuestState for the player that will be added to the player's list of quests
					new QuestState(q, player, State.getStateId(statename));
				}
			}
			
			// Get list of quests owned by the player from the DB in order to add variables used in the quest.
			try (PreparedStatement ps2 = con.prepareStatement("SELECT name, var, value FROM character_quests WHERE charId = ? AND var <> ?"))
			{
				ps2.setInt(1, player.getObjectId());
				ps2.setString(2, "<state>");
				try (ResultSet rs = ps2.executeQuery())
				{
					while (rs.next())
					{
						String questId = rs.getString("name");
						String var = rs.getString("var");
						String value = rs.getString("value");
						// Get the QuestState saved in the loop before
						QuestState qs = player.getQuestState(questId);
						if (qs == null)
						{
							_log.info("Lost variable " + var + " in quest " + questId + " for player " + player.getName());
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
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("could not insert char quest:", e);
		}
		
		// events
		for (String name : QuestManager.getInstance().getScripts().keySet())
		{
			player.processQuestEvent(name, "enter");
		}
	}
	
	/**
	 * Insert (or update) in the database variables that need to stay persistent for this quest after a reboot.<br>
	 * This function is for storage of values that do not related to a specific player but are global for all characters.<br>
	 * For example, if we need to disable a quest-gatekeeper until a certain time (as is done with some grand-boss gatekeepers), we can save that time in the DB.
	 * @param var the name of the variable to save
	 * @param value the value of the variable
	 */
	public final void saveGlobalQuestVar(String var, String value)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("REPLACE INTO quest_global_data (quest_name,var,value) VALUES (?,?,?)"))
		{
			statement.setString(1, getName());
			statement.setString(2, var);
			statement.setString(3, value);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warn("could not insert global quest variable:", e);
		}
	}
	
	/**
	 * Read from the database a previously saved variable for this quest.<br>
	 * Due to performance considerations, this function should best be used only when the quest is first loaded.<br>
	 * Subclasses of this class can define structures into which these loaded values can be saved.<br>
	 * However, on-demand usage of this function throughout the script is not prohibited, only not recommended.<br>
	 * Values read from this function were entered by calls to "saveGlobalQuestVar".
	 * @param var the name of the variable to load
	 * @return the current value of the specified variable, or an empty string if the variable does not exist
	 */
	public final String loadGlobalQuestVar(String var)
	{
		String result = "";
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT value FROM quest_global_data WHERE quest_name = ? AND var = ?"))
		{
			statement.setString(1, getName());
			statement.setString(2, var);
			try (ResultSet rs = statement.executeQuery())
			{
				if (rs.first())
				{
					result = rs.getString(1);
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("could not load global quest variable:", e);
		}
		return result;
	}
	
	/**
	 * Permanently delete from the database a global quest variable that was previously saved for this quest.
	 * @param var the name of the variable to delete
	 */
	public final void deleteGlobalQuestVar(String var)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ? AND var = ?"))
		{
			statement.setString(1, getName());
			statement.setString(2, var);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warn("could not delete global quest variable:", e);
		}
	}
	
	/**
	 * Permanently delete from the database all global quest variables that were previously saved for this quest.
	 */
	public final void deleteAllGlobalQuestVars()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ?"))
		{
			statement.setString(1, getName());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warn("could not delete global quest variables:", e);
		}
	}
	
	/**
	 * Insert in the database the quest for the player.
	 * @param qs the {@link QuestState} object whose variable to insert
	 * @param var the name of the variable
	 * @param value the value of the variable
	 */
	public static void createQuestVarInDb(QuestState qs, String var, String value)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO character_quests (charId,name,var,value) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE value=?"))
		{
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.setString(4, value);
			statement.setString(5, value);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warn("could not insert char quest:", e);
		}
	}
	
	/**
	 * Update the value of the variable "var" for the specified quest in database
	 * @param qs the {@link QuestState} object whose variable to update
	 * @param var the name of the variable
	 * @param value the value of the variable
	 */
	public static void updateQuestVarInDb(QuestState qs, String var, String value)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE character_quests SET value=? WHERE charId=? AND name=? AND var = ?"))
		{
			statement.setString(1, value);
			statement.setInt(2, qs.getPlayer().getObjectId());
			statement.setString(3, qs.getQuestName());
			statement.setString(4, var);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warn("could not update char quest:", e);
		}
	}
	
	/**
	 * Delete a variable of player's quest from the database.
	 * @param qs the {@link QuestState} object whose variable to delete
	 * @param var the name of the variable to delete
	 */
	public static void deleteQuestVarInDb(QuestState qs, String var)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_quests WHERE charId=? AND name=? AND var=?"))
		{
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warn("could not delete char quest:", e);
		}
	}
	
	/**
	 * Delete from the database all variables and states of the specified quest state.
	 * @param qs the {@link QuestState} object whose variables to delete
	 * @param repeatable if {@code false}, the state variable will be preserved, otherwise it will be deleted as well
	 */
	public static void deleteQuestInDb(QuestState qs, boolean repeatable)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(repeatable ? QUEST_DELETE_FROM_CHAR_QUERY : QUEST_DELETE_FROM_CHAR_QUERY_NON_REPEATABLE_QUERY))
		{
			ps.setInt(1, qs.getPlayer().getObjectId());
			ps.setString(2, qs.getQuestName());
			if (!repeatable)
			{
				ps.setString(3, "<state>");
			}
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warn("could not delete char quest:", e);
		}
	}
	
	/**
	 * Create a database record for the specified quest state.
	 * @param qs the {@link QuestState} object whose data to write in the database
	 */
	public static void createQuestInDb(QuestState qs)
	{
		createQuestVarInDb(qs, "<state>", State.getStateName(qs.getState()));
	}
	
	/**
	 * Update informations regarding quest in database.<br>
	 * Actions:<br>
	 * <ul>
	 * <li>Get Id state of the quest recorded in object qs</li>
	 * <li>Test if quest is completed. If true, add a star (*) before the Id state</li>
	 * <li>Save in database the Id state (with or without the star) for the variable called "&lt;state&gt;" of the quest</li>
	 * </ul>
	 * @param qs the quest state
	 */
	public static void updateQuestInDb(QuestState qs)
	{
		updateQuestVarInDb(qs, "<state>", State.getStateName(qs.getState()));
	}
	
	/**
	 * @param player the player whose language settings to use in finding the html of the right language
	 * @return the default html for when no quest is available: "You are either not on a quest that involves this NPC.."
	 */
	public static String getNoQuestMsg(L2PcInstance player)
	{
		final String result = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/noquest.htm");
		if ((result != null) && (result.length() > 0))
		{
			return result;
		}
		return DEFAULT_NO_QUEST_MSG;
	}
	
	/**
	 * @param player the player whose language settings to use in finding the html of the right language
	 * @return the default html for when no quest is already completed: "This quest has already been completed."
	 */
	public static String getAlreadyCompletedMsg(L2PcInstance player)
	{
		final String result = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/alreadycompleted.htm");
		if ((result != null) && (result.length() > 0))
		{
			return result;
		}
		return DEFAULT_ALREADY_COMPLETED_MSG;
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for the specified Event type.
	 * @param eventType type of event being registered
	 * @param npcId NPC Id to register
	 */
	public void addEventId(QuestEventType eventType, int npcId)
	{
		try
		{
			final L2NpcTemplate t = NpcTable.getInstance().getTemplate(npcId);
			if (t != null)
			{
				t.addQuestEvent(eventType, this);
				_questInvolvedNpcs.add(npcId);
			}
		}
		catch (Exception e)
		{
			_log.warn("Exception on addEventId(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for the specified Event type.
	 * @param eventType type of event being registered
	 * @param npcIds NPC Ids to register
	 */
	public void addEventId(QuestEventType eventType, int... npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(eventType, npcId);
		}
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for the specified Event type.
	 * @param eventType type of event being registered
	 * @param npcIds NPC Ids to register
	 */
	public void addEventId(QuestEventType eventType, Collection<Integer> npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(eventType, npcId);
		}
	}
	
	// TODO: Remove after all Jython scripts are replaced with Java versions.
	public void addStartNpc(int npcId)
	{
		addEventId(QuestEventType.QUEST_START, npcId);
	}
	
	public void addFirstTalkId(int npcId)
	{
		addEventId(QuestEventType.ON_FIRST_TALK, npcId);
	}
	
	public void addTalkId(int npcId)
	{
		addEventId(QuestEventType.ON_TALK, npcId);
	}
	
	public void addKillId(int killId)
	{
		addEventId(QuestEventType.ON_KILL, killId);
	}
	
	public void addAttackId(int npcId)
	{
		addEventId(QuestEventType.ON_ATTACK, npcId);
	}
	
	/**
	 * Add the quest to the NPC's startQuest
	 * @param npcIds
	 */
	public void addStartNpc(int... npcIds)
	{
		addEventId(QuestEventType.QUEST_START, npcIds);
	}
	
	/**
	 * Add the quest to the NPC's startQuest
	 * @param npcIds
	 */
	public void addStartNpc(Collection<Integer> npcIds)
	{
		addEventId(QuestEventType.QUEST_START, npcIds);
	}
	
	/**
	 * Add the quest to the NPC's first-talk (default action dialog)
	 * @param npcIds
	 */
	public void addFirstTalkId(int... npcIds)
	{
		addEventId(QuestEventType.ON_FIRST_TALK, npcIds);
	}
	
	/**
	 * Add the quest to the NPC's first-talk (default action dialog)
	 * @param npcIds
	 */
	public void addFirstTalkId(Collection<Integer> npcIds)
	{
		addEventId(QuestEventType.ON_FIRST_TALK, npcIds);
	}
	
	/**
	 * Add the NPC to the AcquireSkill dialog
	 * @param npcIds
	 */
	public void addAcquireSkillId(int... npcIds)
	{
		addEventId(QuestEventType.ON_SKILL_LEARN, npcIds);
	}
	
	/**
	 * Add the NPC to the AcquireSkill dialog
	 * @param npcIds
	 */
	public void addAcquireSkillId(Collection<Integer> npcIds)
	{
		addEventId(QuestEventType.ON_SKILL_LEARN, npcIds);
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for Attack Events.
	 * @param npcIds
	 */
	public void addAttackId(int... npcIds)
	{
		addEventId(QuestEventType.ON_ATTACK, npcIds);
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for Attack Events.
	 * @param npcIds
	 */
	public void addAttackId(Collection<Integer> npcIds)
	{
		addEventId(QuestEventType.ON_ATTACK, npcIds);
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for Kill Events.
	 * @param killIds
	 */
	public void addKillId(int... killIds)
	{
		addEventId(QuestEventType.ON_KILL, killIds);
	}
	
	/**
	 * Add this quest event to the collection of NPC Ids that will respond to for on kill events.
	 * @param killIds the collection of NPC Ids
	 */
	public void addKillId(Collection<Integer> killIds)
	{
		addEventId(QuestEventType.ON_KILL, killIds);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Talk Events.
	 * @param npcIds Id of the NPC
	 */
	public void addTalkId(int... npcIds)
	{
		addEventId(QuestEventType.ON_TALK, npcIds);
	}
	
	public void addTalkId(Collection<Integer> npcIds)
	{
		addEventId(QuestEventType.ON_TALK, npcIds);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Spawn Events.
	 * @param npcIds Id of the NPC
	 */
	public void addSpawnId(int... npcIds)
	{
		addEventId(QuestEventType.ON_SPAWN, npcIds);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Spawn Events.
	 * @param npcIds Id of the NPC
	 */
	public void addSpawnId(Collection<Integer> npcIds)
	{
		addEventId(QuestEventType.ON_SPAWN, npcIds);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Skill-See Events.
	 * @param npcIds Id of the NPC
	 */
	public void addSkillSeeId(int... npcIds)
	{
		addEventId(QuestEventType.ON_SKILL_SEE, npcIds);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Skill-See Events.
	 * @param npcIds Id of the NPC
	 */
	public void addSkillSeeId(Collection<Integer> npcIds)
	{
		addEventId(QuestEventType.ON_SKILL_SEE, npcIds);
	}
	
	/**
	 * @param npcIds
	 */
	public void addSpellFinishedId(int... npcIds)
	{
		addEventId(QuestEventType.ON_SPELL_FINISHED, npcIds);
	}
	
	/**
	 * @param npcIds
	 */
	public void addSpellFinishedId(Collection<Integer> npcIds)
	{
		addEventId(QuestEventType.ON_SPELL_FINISHED, npcIds);
	}
	
	/**
	 * @param npcIds
	 */
	public void addTrapActionId(int... npcIds)
	{
		addEventId(QuestEventType.ON_TRAP_ACTION, npcIds);
	}
	
	/**
	 * @param npcIds
	 */
	public void addTrapActionId(Collection<Integer> npcIds)
	{
		addEventId(QuestEventType.ON_TRAP_ACTION, npcIds);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Faction Call Events.
	 * @param npcIds Id of the NPC
	 */
	public void addFactionCallId(int... npcIds)
	{
		addEventId(QuestEventType.ON_FACTION_CALL, npcIds);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Faction Call Events.
	 * @param npcIds Id of the NPC
	 */
	public void addFactionCallId(Collection<Integer> npcIds)
	{
		addEventId(QuestEventType.ON_FACTION_CALL, npcIds);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Character See Events.
	 * @param npcIds Id of the NPC
	 */
	public void addAggroRangeEnterId(int... npcIds)
	{
		addEventId(QuestEventType.ON_AGGRO_RANGE_ENTER, npcIds);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Character See Events.
	 * @param npcIds Id of the NPC
	 */
	public void addAggroRangeEnterId(Collection<Integer> npcIds)
	{
		addEventId(QuestEventType.ON_AGGRO_RANGE_ENTER, npcIds);
	}
	
	/**
	 * @param npcIds NPC Ids to register to on see creature event
	 */
	public void addSeeCreatureId(int... npcIds)
	{
		addEventId(QuestEventType.ON_SEE_CREATURE, npcIds);
	}
	
	/**
	 * @param npcIds NPC Ids to register to on see creature event
	 */
	public void addSeeCreatureId(Collection<Integer> npcIds)
	{
		addEventId(QuestEventType.ON_SEE_CREATURE, npcIds);
	}
	
	/**
	 * Register onEnterZone trigger for Zone
	 * @param zoneId
	 */
	public void addEnterZoneId(int zoneId)
	{
		final L2ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
		if (zone != null)
		{
			zone.addQuestEvent(QuestEventType.ON_ENTER_ZONE, this);
		}
	}
	
	/**
	 * Register onEnterZone trigger for Zones
	 * @param zoneIds
	 */
	public void addEnterZoneId(int... zoneIds)
	{
		for (int zoneId : zoneIds)
		{
			addEnterZoneId(zoneId);
		}
	}
	
	/**
	 * Register onEnterZone trigger for Zones
	 * @param zoneIds
	 */
	public void addEnterZoneId(Collection<Integer> zoneIds)
	{
		for (int zoneId : zoneIds)
		{
			addEnterZoneId(zoneId);
		}
	}
	
	/**
	 * Register onExitZone trigger for Zone
	 * @param zoneId
	 */
	public void addExitZoneId(int zoneId)
	{
		L2ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
		if (zone != null)
		{
			zone.addQuestEvent(QuestEventType.ON_EXIT_ZONE, this);
		}
	}
	
	/**
	 * Register onExitZone trigger for Zones
	 * @param zoneIds
	 */
	public void addExitZoneId(int... zoneIds)
	{
		for (int zoneId : zoneIds)
		{
			addExitZoneId(zoneId);
		}
	}
	
	/**
	 * Register onExitZone trigger for Zones
	 * @param zoneIds
	 */
	public void addExitZoneId(Collection<Integer> zoneIds)
	{
		for (int zoneId : zoneIds)
		{
			addExitZoneId(zoneId);
		}
	}
	
	/**
	 * Register onEventReceived trigger for NPC
	 * @param npcIds
	 */
	public void addEventReceivedId(int... npcIds)
	{
		addEventId(QuestEventType.ON_EVENT_RECEIVED, npcIds);
	}
	
	/**
	 * Register onEventReceived trigger for NPC
	 * @param npcIds
	 */
	public void addEventReceivedId(Collection<Integer> npcIds)
	{
		addEventId(QuestEventType.ON_EVENT_RECEIVED, npcIds);
	}
	
	/**
	 * Register onMoveFinished trigger for NPC
	 * @param npcIds
	 */
	public void addMoveFinishedId(int... npcIds)
	{
		addEventId(QuestEventType.ON_MOVE_FINISHED, npcIds);
	}
	
	/**
	 * Register onMoveFinished trigger for NPC
	 * @param npcIds
	 */
	public void addMoveFinishedId(Collection<Integer> npcIds)
	{
		addEventId(QuestEventType.ON_MOVE_FINISHED, npcIds);
	}
	
	/**
	 * Register onNodeArrived trigger for NPC
	 * @param npcIds id of NPC to register
	 */
	public void addNodeArrivedId(int... npcIds)
	{
		addEventId(QuestEventType.ON_NODE_ARRIVED, npcIds);
	}
	
	/**
	 * Register onNodeArrived trigger for NPC
	 * @param npcIds id of NPC to register
	 */
	public void addNodeArrivedId(Collection<Integer> npcIds)
	{
		addEventId(QuestEventType.ON_NODE_ARRIVED, npcIds);
	}
	
	/**
	 * Register onRouteFinished trigger for NPC
	 * @param npcIds id of NPC to register
	 */
	public void addRouteFinishedId(int... npcIds)
	{
		addEventId(QuestEventType.ON_ROUTE_FINISHED, npcIds);
	}
	
	/**
	 * Register onRouteFinished trigger for NPC
	 * @param npcIds id of NPC to register
	 */
	public void addRouteFinishedId(Collection<Integer> npcIds)
	{
		addEventId(QuestEventType.ON_ROUTE_FINISHED, npcIds);
	}
	
	/**
	 * Register onNpcHate trigger for NPC
	 * @param npcIds
	 */
	public void addNpcHateId(int... npcIds)
	{
		addEventId(QuestEventType.ON_NPC_HATE, npcIds);
	}
	
	/**
	 * Register onNpcHate trigger for NPC
	 * @param npcIds
	 */
	public void addNpcHateId(Collection<Integer> npcIds)
	{
		addEventId(QuestEventType.ON_NPC_HATE, npcIds);
	}
	
	/**
	 * Registers onCanSeeMe trigger whenever an npc info must be sent to player.
	 * @param npcIds
	 */
	public void addCanSeeMeId(int... npcIds)
	{
		addEventId(QuestEventType.ON_CAN_SEE_ME, npcIds);
	}
	
	/**
	 * Registers onCanSeeMe trigger whenever an npc info must be sent to player.
	 * @param npcIds
	 */
	public void addCanSeeMeId(Collection<Integer> npcIds)
	{
		addEventId(QuestEventType.ON_CAN_SEE_ME, npcIds);
	}
	
	/**
	 * Use this method to get a random party member from a player's party.<br>
	 * Useful when distributing rewards after killing an NPC.
	 * @param player this parameter represents the player whom the party will taken.
	 * @return {@code null} if {@code player} is {@code null}, {@code player} itself if the player does not have a party, and a random party member in all other cases
	 */
	public L2PcInstance getRandomPartyMember(L2PcInstance player)
	{
		if (player == null)
		{
			return null;
		}
		final L2Party party = player.getParty();
		if ((party == null) || (party.getMembers().isEmpty()))
		{
			return player;
		}
		return party.getMembers().get(Rnd.get(party.getMembers().size()));
	}
	
	/**
	 * Get a random party member with required cond value.
	 * @param player the instance of a player whose party is to be searched
	 * @param cond the value of the "cond" variable that must be matched
	 * @return a random party member that matches the specified condition, or {@code null} if no match was found
	 */
	public L2PcInstance getRandomPartyMember(L2PcInstance player, int cond)
	{
		return getRandomPartyMember(player, "cond", String.valueOf(cond));
	}
	
	/**
	 * Auxiliary function for party quests.<br>
	 * Note: This function is only here because of how commonly it may be used by quest developers.<br>
	 * For any variations on this function, the quest script can always handle things on its own.
	 * @param player the instance of a player whose party is to be searched
	 * @param var the quest variable to look for in party members. If {@code null}, it simply unconditionally returns a random party member
	 * @param value the value of the specified quest variable the random party member must have
	 * @return a random party member that matches the specified conditions or {@code null} if no match was found.<br>
	 *         If the {@code var} parameter is {@code null}, a random party member is selected without any conditions.<br>
	 *         The party member must be within a range of 1500 ingame units of the target of the reference player, or, if no target exists, within the same range of the player itself
	 */
	public L2PcInstance getRandomPartyMember(L2PcInstance player, String var, String value)
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
		if ((party == null) || (party.getMembers().isEmpty()))
		{
			temp = player.getQuestState(getName());
			if ((temp != null) && temp.isSet(var) && temp.get(var).equalsIgnoreCase(value))
			{
				return player; // match
			}
			return null; // no match
		}
		
		// if the player is in a party, gather a list of all matching party members (possibly including this player)
		List<L2PcInstance> candidates = new ArrayList<>();
		// get the target for enforcing distance limitations.
		L2Object target = player.getTarget();
		if (target == null)
		{
			target = player;
		}
		
		for (L2PcInstance partyMember : party.getMembers())
		{
			if (partyMember == null)
			{
				continue;
			}
			temp = partyMember.getQuestState(getName());
			if ((temp != null) && (temp.get(var) != null) && (temp.get(var)).equalsIgnoreCase(value) && partyMember.isInsideRadius(target, 1500, true, false))
			{
				candidates.add(partyMember);
			}
		}
		// if there was no match, return null...
		if (candidates.isEmpty())
		{
			return null;
		}
		// if a match was found from the party, return one of them at random.
		return candidates.get(Rnd.get(candidates.size()));
	}
	
	/**
	 * Get a random party member from the specified player's party.<br>
	 * If the player is not in a party, only the player himself is checked.<br>
	 * The lucky member is chosen by standard loot roll rules -<br>
	 * each member rolls a random number, the one with the highest roll wins.
	 * @param player the player whose party to check
	 * @param npc the NPC used for distance and other checks (if {@link #checkPartyMember(L2PcInstance, L2Npc)} is overriden)
	 * @return the random party member or {@code null}
	 */
	public L2PcInstance getRandomPartyMember(L2PcInstance player, L2Npc npc)
	{
		if ((player == null) || !checkDistanceToTarget(player, npc))
		{
			return null;
		}
		final L2Party party = player.getParty();
		L2PcInstance luckyPlayer = null;
		if (party == null)
		{
			if (checkPartyMember(player, npc))
			{
				luckyPlayer = player;
			}
		}
		else
		{
			int highestRoll = 0;
			
			for (L2PcInstance member : party.getMembers())
			{
				final int rnd = getRandom(1000);
				
				if ((rnd > highestRoll) && checkPartyMember(member, npc))
				{
					highestRoll = rnd;
					luckyPlayer = member;
				}
			}
		}
		if ((luckyPlayer != null) && checkDistanceToTarget(luckyPlayer, npc))
		{
			return luckyPlayer;
		}
		return null;
	}
	
	/**
	 * This method is called for every party member in {@link #getRandomPartyMember(L2PcInstance, L2Npc)}.<br>
	 * It is intended to be overriden by the specific quest implementations.
	 * @param player the player to check
	 * @param npc the NPC that was passed to {@link #getRandomPartyMember(L2PcInstance, L2Npc)}
	 * @return {@code true} if this party member passes the check, {@code false} otherwise
	 */
	public boolean checkPartyMember(L2PcInstance player, L2Npc npc)
	{
		return true;
	}
	
	/**
	 * Auxiliary function for party quests.<br>
	 * Note: This function is only here because of how commonly it may be used by quest developers.<br>
	 * For any variations on this function, the quest script can always handle things on its own.
	 * @param player the player whose random party member is to be selected
	 * @param state the quest state required of the random party member
	 * @return {@code null} if nothing was selected or a random party member that has the specified quest state
	 */
	public L2PcInstance getRandomPartyMemberState(L2PcInstance player, byte state)
	{
		// if no valid player instance is passed, there is nothing to check...
		if (player == null)
		{
			return null;
		}
		
		// normal cases...if the player is not in a party check the player's state
		QuestState temp = null;
		L2Party party = player.getParty();
		// if this player is not in a party, just check if this player instance matches the conditions itself
		if ((party == null) || (party.getMembers().isEmpty()))
		{
			temp = player.getQuestState(getName());
			if ((temp != null) && (temp.getState() == state))
			{
				return player; // match
			}
			
			return null; // no match
		}
		
		// if the player is in a party, gather a list of all matching party members (possibly
		// including this player)
		List<L2PcInstance> candidates = new ArrayList<>();
		
		// get the target for enforcing distance limitations.
		L2Object target = player.getTarget();
		if (target == null)
		{
			target = player;
		}
		
		for (L2PcInstance partyMember : party.getMembers())
		{
			if (partyMember == null)
			{
				continue;
			}
			temp = partyMember.getQuestState(getName());
			if ((temp != null) && (temp.getState() == state) && partyMember.isInsideRadius(target, 1500, true, false))
			{
				candidates.add(partyMember);
			}
		}
		// if there was no match, return null...
		if (candidates.isEmpty())
		{
			return null;
		}
		
		// if a match was found from the party, return one of them at random.
		return candidates.get(Rnd.get(candidates.size()));
	}
	
	/**
	 * Get a random party member from the player's party who has this quest at the specified quest progress.<br>
	 * If the player is not in a party, only the player himself is checked.
	 * @param player the player whose random party member state to get
	 * @param condition the quest progress step the random member should be at (-1 = check only if quest is started)
	 * @param playerChance how many times more chance does the player get compared to other party members (3 - 3x more chance).<br>
	 *            On retail servers, the killer usually gets 2-3x more chance than other party members
	 * @param target the NPC to use for the distance check (can be null)
	 * @return the {@link QuestState} object of the random party member or {@code null} if none matched the condition
	 */
	public QuestState getRandomPartyMemberState(L2PcInstance player, int condition, int playerChance, L2Npc target)
	{
		if ((player == null) || (playerChance < 1))
		{
			return null;
		}
		
		QuestState qs = player.getQuestState(getName());
		if (!player.isInParty())
		{
			if (!checkPartyMemberConditions(qs, condition, target))
			{
				return null;
			}
			if (!checkDistanceToTarget(player, target))
			{
				return null;
			}
			return qs;
		}
		
		final List<QuestState> candidates = new ArrayList<>();
		if (checkPartyMemberConditions(qs, condition, target) && (playerChance > 0))
		{
			for (int i = 0; i < playerChance; i++)
			{
				candidates.add(qs);
			}
		}
		
		for (L2PcInstance member : player.getParty().getMembers())
		{
			if (member == player)
			{
				continue;
			}
			
			qs = member.getQuestState(getName());
			if (checkPartyMemberConditions(qs, condition, target))
			{
				candidates.add(qs);
			}
		}
		
		if (candidates.isEmpty())
		{
			return null;
		}
		
		qs = candidates.get(getRandom(candidates.size()));
		if (!checkDistanceToTarget(qs.getPlayer(), target))
		{
			return null;
		}
		return qs;
	}
	
	private boolean checkPartyMemberConditions(QuestState qs, int condition, L2Npc npc)
	{
		return ((qs != null) && ((condition == -1) ? qs.isStarted() : qs.isCond(condition)) && checkPartyMember(qs, npc));
	}
	
	private static boolean checkDistanceToTarget(L2PcInstance player, L2Npc target)
	{
		return ((target == null) || l2r.gameserver.util.Util.checkIfInRange(1500, player, target, true));
	}
	
	/**
	 * This method is called for every party member in {@link #getRandomPartyMemberState(L2PcInstance, int, int, L2Npc)} if/after all the standard checks are passed.<br>
	 * It is intended to be overriden by the specific quest implementations.<br>
	 * It can be used in cases when there are more checks performed than simply a quest condition check,<br>
	 * for example, if an item is required in the player's inventory.
	 * @param qs the {@link QuestState} object of the party member
	 * @param npc the NPC that was passed as the last parameter to {@link #getRandomPartyMemberState(L2PcInstance, int, int, L2Npc)}
	 * @return {@code true} if this party member passes the check, {@code false} otherwise
	 */
	public boolean checkPartyMember(QuestState qs, L2Npc npc)
	{
		return true;
	}
	
	/**
	 * Show an on screen message to the player.
	 * @param player the player to display the message
	 * @param text the message
	 * @param time the display time
	 */
	public static void showOnScreenMsg(L2PcInstance player, String text, int time)
	{
		player.sendPacket(new ExShowScreenMessage(text, time));
	}
	
	/**
	 * Show an on screen message to the player.
	 * @param player the player to display the message
	 * @param npcString the NPC String to display
	 * @param position the position in the screen
	 * @param time the display time
	 */
	public static void showOnScreenMsg(L2PcInstance player, NpcStringId npcString, int position, int time)
	{
		player.sendPacket(new ExShowScreenMessage(npcString, position, time));
	}
	
	/**
	 * Show an on screen message to the player.
	 * @param player the player to display the message
	 * @param systemMsg the System Message to display
	 * @param position the position in the screen
	 * @param time the display time
	 * @param params parameters values to replace in the System Message
	 */
	public static void showOnScreenMsg(L2PcInstance player, SystemMessageId systemMsg, int position, int time, String... params)
	{
		player.sendPacket(new ExShowScreenMessage(systemMsg, position, time, params));
	}
	
	/**
	 * Show HTML file to client
	 * @param player
	 * @param fileName
	 * @return String message sent to client
	 */
	public String showHtmlFile(L2PcInstance player, String fileName)
	{
		boolean questwindow = !fileName.endsWith(".html");
		int questId = getId();
		
		// Create handler to file linked to the quest
		String content = getHtm(player.getHtmlPrefix(), fileName);
		
		// Send message to client if message not empty
		if (content != null)
		{
			if (player.getTarget() != null)
			{
				content = content.replaceAll("%objectId%", Integer.toString(player.getTargetId()));
			}
			
			if (questwindow && (questId > 0) && (questId < 20000) && (questId != 999))
			{
				NpcQuestHtmlMessage npcReply = new NpcQuestHtmlMessage(player.getTargetId(), questId);
				npcReply.setHtml(content);
				npcReply.replace("%playername%", player.getName());
				player.sendPacket(npcReply);
			}
			else
			{
				NpcHtmlMessage npcReply = new NpcHtmlMessage(player.getTargetId());
				npcReply.setHtml(content);
				npcReply.replace("%playername%", player.getName());
				player.sendPacket(npcReply);
			}
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		
		return content;
	}
	
	/**
	 * @param prefix player's language prefix.
	 * @param fileName the html file to be get.
	 * @return the HTML file contents
	 */
	public String getHtm(String prefix, String fileName)
	{
		final HtmCache hc = HtmCache.getInstance();
		String content = hc.getHtm(prefix, fileName.startsWith("data/") ? fileName : "data/scripts/" + getDescr().toLowerCase() + "/" + getName() + "/" + fileName);
		if (content == null)
		{
			content = hc.getHtm(prefix, "data/scripts/" + getDescr() + "/" + getName() + "/" + fileName);
			if (content == null)
			{
				content = hc.getHtmForce(prefix, "data/scripts/quests/" + getName() + "/" + fileName);
			}
		}
		return content;
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param pos the object containing the spawn location coordinates
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean, int)
	 */
	public static L2Npc addSpawn(int npcId, IPositionable pos)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), false, 0, false, 0);
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param pos the object containing the spawn location coordinates
	 * @param isSummonSpawn if {@code true}, displays a summon animation on NPC spawn
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean, int)
	 */
	public static L2Npc addSpawn(int npcId, IPositionable pos, boolean isSummonSpawn)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), false, 0, isSummonSpawn, 0);
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param pos the object containing the spawn location coordinates
	 * @param randomOffset if {@code true}, adds +/- 50~100 to X/Y coordinates of the spawn location
	 * @param despawnDelay time in milliseconds till the NPC is despawned (0 - only despawned on server shutdown)
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean, int)
	 */
	public static L2Npc addSpawn(int npcId, IPositionable pos, boolean randomOffset, long despawnDelay)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), randomOffset, despawnDelay, false, 0);
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param pos the object containing the spawn location coordinates
	 * @param randomOffset if {@code true}, adds +/- 50~100 to X/Y coordinates of the spawn location
	 * @param despawnDelay time in milliseconds till the NPC is despawned (0 - only despawned on server shutdown)
	 * @param isSummonSpawn if {@code true}, displays a summon animation on NPC spawn
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean, int)
	 */
	public static L2Npc addSpawn(int npcId, IPositionable pos, boolean randomOffset, long despawnDelay, boolean isSummonSpawn)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), randomOffset, despawnDelay, isSummonSpawn, 0);
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param pos the object containing the spawn location coordinates
	 * @param randomOffset if {@code true}, adds +/- 50~100 to X/Y coordinates of the spawn location
	 * @param despawnDelay time in milliseconds till the NPC is despawned (0 - only despawned on server shutdown)
	 * @param isSummonSpawn if {@code true}, displays a summon animation on NPC spawn
	 * @param instanceId the ID of the instance to spawn the NPC in (0 - the open world)
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable)
	 * @see #addSpawn(int, IPositionable, boolean)
	 * @see #addSpawn(int, IPositionable, boolean, long)
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean, int)
	 */
	public static L2Npc addSpawn(int npcId, IPositionable pos, boolean randomOffset, long despawnDelay, boolean isSummonSpawn, int instanceId)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), randomOffset, despawnDelay, isSummonSpawn, instanceId);
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param x the X coordinate of the spawn location
	 * @param y the Y coordinate of the spawn location
	 * @param z the Z coordinate (height) of the spawn location
	 * @param heading the heading of the NPC
	 * @param randomOffset if {@code true}, adds +/- 50~100 to X/Y coordinates of the spawn location
	 * @param despawnDelay time in milliseconds till the NPC is despawned (0 - only despawned on server shutdown)
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean, int)
	 */
	public static L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay)
	{
		return addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, false, 0);
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param x the X coordinate of the spawn location
	 * @param y the Y coordinate of the spawn location
	 * @param z the Z coordinate (height) of the spawn location
	 * @param heading the heading of the NPC
	 * @param randomOffset if {@code true}, adds +/- 50~100 to X/Y coordinates of the spawn location
	 * @param despawnDelay time in milliseconds till the NPC is despawned (0 - only despawned on server shutdown)
	 * @param isSummonSpawn if {@code true}, displays a summon animation on NPC spawn
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean, int)
	 */
	public static L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn)
	{
		return addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, isSummonSpawn, 0);
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param x the X coordinate of the spawn location
	 * @param y the Y coordinate of the spawn location
	 * @param z the Z coordinate (height) of the spawn location
	 * @param heading the heading of the NPC
	 * @param randomOffset if {@code true}, adds +/- 50~100 to X/Y coordinates of the spawn location
	 * @param despawnDelay time in milliseconds till the NPC is despawned (0 - only despawned on server shutdown)
	 * @param isSummonSpawn if {@code true}, displays a summon animation on NPC spawn
	 * @param instanceId the ID of the instance to spawn the NPC in (0 - the open world)
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, long)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean)
	 */
	public static L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn, int instanceId)
	{
		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			if (template == null)
			{
				_log.error("addSpawn(): no NPC template found for NPC #" + npcId + "!");
			}
			else
			{
				if ((x == 0) && (y == 0))
				{
					_log.error("addSpawn(): invalid spawn coordinates for NPC #" + npcId + "!");
					return null;
				}
				if (randomOffset)
				{
					int offset = Rnd.get(50, 100);
					if (Rnd.nextBoolean())
					{
						offset *= -1;
					}
					x += offset;
					
					offset = Rnd.get(50, 100);
					if (Rnd.nextBoolean())
					{
						offset *= -1;
					}
					y += offset;
				}
				L2Spawn spawn = new L2Spawn(template);
				spawn.setInstanceId(instanceId);
				spawn.setHeading(heading);
				spawn.setX(x);
				spawn.setY(y);
				spawn.setZ(z);
				spawn.stopRespawn();
				L2Npc result = spawn.spawnOne(isSummonSpawn);
				
				if (despawnDelay > 0)
				{
					result.scheduleDespawn(despawnDelay);
				}
				
				return result;
			}
		}
		catch (Exception e1)
		{
			_log.warn("Could not spawn NPC #" + npcId + "; error: " + e1.getMessage());
		}
		
		return null;
	}
	
	/**
	 * @param trapId
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @param skill
	 * @param instanceId
	 * @return
	 */
	public L2TrapInstance addTrap(int trapId, int x, int y, int z, int heading, L2Skill skill, int instanceId)
	{
		final L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(trapId);
		L2TrapInstance trap = new L2TrapInstance(IdFactory.getInstance().getNextId(), npcTemplate, instanceId, -1);
		trap.setCurrentHp(trap.getMaxHp());
		trap.setCurrentMp(trap.getMaxMp());
		trap.setIsInvul(true);
		trap.setHeading(heading);
		trap.spawnMe(x, y, z);
		return trap;
	}
	
	/**
	 * @param master
	 * @param minionId
	 * @return
	 */
	public L2Npc addMinion(L2MonsterInstance master, int minionId)
	{
		return MinionList.spawnMinion(master, minionId);
	}
	
	/**
	 * @return the registered quest items Ids.
	 */
	public int[] getRegisteredItemIds()
	{
		return questItemIds;
	}
	
	/**
	 * Registers all items that have to be destroyed in case player abort the quest or finish it.
	 * @param items
	 */
	public void registerQuestItems(int... items)
	{
		questItemIds = items;
	}
	
	@Override
	public String getScriptName()
	{
		return getName();
	}
	
	@Override
	public void setActive(boolean status)
	{
		// TODO implement me
	}
	
	@Override
	public boolean reload()
	{
		unload();
		return super.reload();
	}
	
	@Override
	public boolean unload()
	{
		return unload(true);
	}
	
	/**
	 * @param removeFromList
	 * @return
	 */
	public boolean unload(boolean removeFromList)
	{
		saveGlobalData();
		// cancel all pending timers before reloading.
		// if timers ought to be restarted, the quest can take care of it
		// with its code (example: save global data indicating what timer must be restarted).
		for (List<QuestTimer> timers : _allEventTimers.values())
		{
			_readLock.lock();
			try
			{
				for (QuestTimer timer : timers)
				{
					timer.cancel();
				}
			}
			finally
			{
				_readLock.unlock();
			}
			timers.clear();
		}
		_allEventTimers.clear();
		
		for (Integer npcId : _questInvolvedNpcs)
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId.intValue());
			if (template != null)
			{
				template.removeQuest(this);
			}
		}
		_questInvolvedNpcs.clear();
		
		if (removeFromList)
		{
			return QuestManager.getInstance().removeScript(this);
		}
		return true;
	}
	
	public Set<Integer> getQuestInvolvedNpcs()
	{
		return _questInvolvedNpcs;
	}
	
	@Override
	public ScriptManager<?> getScriptManager()
	{
		return QuestManager.getInstance();
	}
	
	/**
	 * @param val
	 */
	public void setOnEnterWorld(boolean val)
	{
		_onEnterWorld = val;
	}
	
	/**
	 * @return
	 */
	public boolean getOnEnterWorld()
	{
		return _onEnterWorld;
	}
	
	/**
	 * Checking if array contains object.
	 * @param array
	 * @param obj
	 * @return true, if array contains obj
	 */
	public static boolean contains(int[] array, int obj)
	{
		for (int element : array)
		{
			if (element == obj)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * If a quest is set as custom, it will display it's name in the NPC Quest List.<br>
	 * Retail quests are unhardcoded to display the name using a client string.
	 * @param val if {@code true} the quest script will be set as custom quest.
	 */
	public void setIsCustom(boolean val)
	{
		_isCustom = val;
	}
	
	/**
	 * @return {@code true} if the quest script is a custom quest, {@code false} otherwise.
	 */
	public boolean isCustomQuest()
	{
		return _isCustom;
	}
	
	/**
	 * @param val
	 */
	public void setOlympiadUse(boolean val)
	{
		_isOlympiadUse = val;
	}
	
	/**
	 * @return {@code true} if the quest script is used for Olympiad quests, {@code false} otherwise.
	 */
	public boolean isOlympiadUse()
	{
		return _isOlympiadUse;
	}
	
	/**
	 * Get the amount of an item in player's inventory.
	 * @param player the player whose inventory to check
	 * @param itemId the Id of the item whose amount to get
	 * @return the amount of the specified item in player's inventory
	 */
	public static long getQuestItemsCount(L2PcInstance player, int itemId)
	{
		return player.getInventory().getInventoryItemCount(itemId, -1);
	}
	
	/**
	 * Get the total amount of all specified items in player's inventory.
	 * @param player the player whose inventory to check
	 * @param itemIds a list of Ids of items whose amount to get
	 * @return the summary amount of all listed items in player's inventory
	 */
	public long getQuestItemsCount(L2PcInstance player, int... itemIds)
	{
		long count = 0;
		for (L2ItemInstance item : player.getInventory().getItems())
		{
			if (item == null)
			{
				continue;
			}
			
			for (int itemId : itemIds)
			{
				if (item.getId() == itemId)
				{
					if ((count + item.getCount()) > Long.MAX_VALUE)
					{
						return Long.MAX_VALUE;
					}
					count += item.getCount();
				}
			}
		}
		return count;
	}
	
	/**
	 * Check if the player has the specified item in his inventory.
	 * @param player the player whose inventory to check for the specified item
	 * @param item the {@link ItemHolder} object containing the ID and count of the item to check
	 * @return {@code true} if the player has the required count of the item
	 */
	protected static boolean hasItem(L2PcInstance player, ItemHolder item)
	{
		return hasItem(player, item, true);
	}
	
	/**
	 * Check if the player has the required count of the specified item in his inventory.
	 * @param player the player whose inventory to check for the specified item
	 * @param item the {@link ItemHolder} object containing the ID and count of the item to check
	 * @param checkCount if {@code true}, check if each item is at least of the count specified in the ItemHolder,<br>
	 *            otherwise check only if the player has the item at all
	 * @return {@code true} if the player has the item
	 */
	protected static boolean hasItem(L2PcInstance player, ItemHolder item, boolean checkCount)
	{
		if (item == null)
		{
			return false;
		}
		if (checkCount)
		{
			return (getQuestItemsCount(player, item.getId()) >= item.getCount());
		}
		return hasQuestItems(player, item.getId());
	}
	
	/**
	 * Check if the player has all the specified items in his inventory and, if necessary, if their count is also as required.
	 * @param player the player whose inventory to check for the specified item
	 * @param checkCount if {@code true}, check if each item is at least of the count specified in the ItemHolder,<br>
	 *            otherwise check only if the player has the item at all
	 * @param itemList a list of {@link ItemHolder} objects containing the IDs of the items to check
	 * @return {@code true} if the player has all the items from the list
	 */
	protected static boolean hasAllItems(L2PcInstance player, boolean checkCount, ItemHolder... itemList)
	{
		if ((itemList == null) || (itemList.length == 0))
		{
			return false;
		}
		for (ItemHolder item : itemList)
		{
			if (!hasItem(player, item, checkCount))
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Check for an item in player's inventory.
	 * @param player the player whose inventory to check for quest items
	 * @param itemId the Id of the item to check for
	 * @return {@code true} if the item exists in player's inventory, {@code false} otherwise
	 */
	public static boolean hasQuestItems(L2PcInstance player, int itemId)
	{
		return (player.getInventory().getItemByItemId(itemId) != null);
	}
	
	/**
	 * Check for multiple items in player's inventory.
	 * @param player the player whose inventory to check for quest items
	 * @param itemIds a list of item Ids to check for
	 * @return {@code true} if all items exist in player's inventory, {@code false} otherwise
	 */
	public static boolean hasQuestItems(L2PcInstance player, int... itemIds)
	{
		if ((itemIds == null) || (itemIds.length == 0))
		{
			return false;
		}
		final PcInventory inv = player.getInventory();
		for (int itemId : itemIds)
		{
			if (inv.getItemByItemId(itemId) == null)
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Check for multiple items in player's inventory.
	 * @param player the player whose inventory to check for quest items
	 * @param itemIds a list of item Ids to check for
	 * @return {@code true} if at least one items exist in player's inventory, {@code false} otherwise
	 */
	public boolean hasAtLeastOneQuestItem(L2PcInstance player, int... itemIds)
	{
		final PcInventory inv = player.getInventory();
		for (int itemId : itemIds)
		{
			if (inv.getItemByItemId(itemId) != null)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get the enchantment level of an item in player's inventory.
	 * @param player the player whose item to check
	 * @param itemId the Id of the item whose enchantment level to get
	 * @return the enchantment level of the item or 0 if the item was not found
	 */
	public static int getEnchantLevel(L2PcInstance player, int itemId)
	{
		final L2ItemInstance enchantedItem = player.getInventory().getItemByItemId(itemId);
		if (enchantedItem == null)
		{
			return 0;
		}
		return enchantedItem.getEnchantLevel();
	}
	
	/**
	 * Give Adena to the player.
	 * @param player the player to whom to give the Adena
	 * @param count the amount of Adena to give
	 * @param applyRates if {@code true} quest rates will be applied to the amount
	 */
	public void giveAdena(L2PcInstance player, long count, boolean applyRates)
	{
		if (applyRates)
		{
			rewardItems(player, Inventory.ADENA_ID, count);
		}
		else
		{
			giveItems(player, Inventory.ADENA_ID, count);
		}
	}
	
	/**
	 * Give a reward to player using multipliers.
	 * @param player the player to whom to give the item
	 * @param holder
	 */
	public static void rewardItems(L2PcInstance player, ItemHolder holder)
	{
		rewardItems(player, holder.getId(), holder.getCount());
	}
	
	/**
	 * Give a reward to player using multipliers.
	 * @param player the player to whom to give the item
	 * @param itemId the Id of the item to give
	 * @param count the amount of items to give
	 */
	public static void rewardItems(L2PcInstance player, int itemId, long count)
	{
		if (count <= 0)
		{
			return;
		}
		
		final L2ItemInstance _tmpItem = ItemData.getInstance().createDummyItem(itemId);
		if (_tmpItem == null)
		{
			return;
		}
		
		try
		{
			if (itemId == Inventory.ADENA_ID)
			{
				count *= Config.RATE_QUEST_REWARD_ADENA;
			}
			else if (Config.RATE_QUEST_REWARD_USE_MULTIPLIERS)
			{
				if (_tmpItem.isEtcItem())
				{
					switch (_tmpItem.getEtcItem().getItemType())
					{
						case POTION:
							count *= Config.RATE_QUEST_REWARD_POTION;
							break;
						case SCRL_ENCHANT_WP:
						case SCRL_ENCHANT_AM:
						case SCROLL:
							count *= Config.RATE_QUEST_REWARD_SCROLL;
							break;
						case RECIPE:
							count *= Config.RATE_QUEST_REWARD_RECIPE;
							break;
						case MATERIAL:
							count *= Config.RATE_QUEST_REWARD_MATERIAL;
							break;
						default:
							count *= Config.RATE_QUEST_REWARD;
					}
				}
			}
			else
			{
				count *= Config.RATE_QUEST_REWARD;
			}
		}
		catch (Exception e)
		{
			count = Long.MAX_VALUE;
		}
		
		// Add items to player's inventory
		L2ItemInstance item = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
		if (item == null)
		{
			return;
		}
		
		sendItemGetMessage(player, item, count);
	}
	
	/**
	 * Send the system message and the status update packets to the player.
	 * @param player the player that has got the item
	 * @param item the item obtain by the player
	 * @param count the item count
	 */
	private static void sendItemGetMessage(L2PcInstance player, L2ItemInstance item, long count)
	{
		// If item for reward is gold, send message of gold reward to client
		if (item.getId() == Inventory.ADENA_ID)
		{
			SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA);
			smsg.addLong(count);
			player.sendPacket(smsg);
		}
		// Otherwise, send message of object reward to client
		else
		{
			if (count > 1)
			{
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				smsg.addItemName(item);
				smsg.addLong(count);
				player.sendPacket(smsg);
			}
			else
			{
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
				smsg.addItemName(item);
				player.sendPacket(smsg);
			}
		}
		// send packets
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
	
	/**
	 * Give item/reward to the player
	 * @param player
	 * @param itemId
	 * @param count
	 */
	public static void giveItems(L2PcInstance player, int itemId, long count)
	{
		giveItems(player, itemId, count, 0);
	}
	
	/**
	 * Give item/reward to the player
	 * @param player
	 * @param holder
	 */
	protected static void giveItems(L2PcInstance player, ItemHolder holder)
	{
		giveItems(player, holder.getId(), holder.getCount());
	}
	
	/**
	 * @param player
	 * @param itemId
	 * @param count
	 * @param enchantlevel
	 */
	public static void giveItems(L2PcInstance player, int itemId, long count, int enchantlevel)
	{
		if (count <= 0)
		{
			return;
		}
		
		// Add items to player's inventory
		final L2ItemInstance item = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
		if (item == null)
		{
			return;
		}
		
		// set enchant level for item if that item is not adena
		if ((enchantlevel > 0) && (itemId != Inventory.ADENA_ID))
		{
			item.setEnchantLevel(enchantlevel);
		}
		
		sendItemGetMessage(player, item, count);
	}
	
	/**
	 * @param player
	 * @param itemId
	 * @param count
	 * @param attributeId
	 * @param attributeLevel
	 */
	public static void giveItems(L2PcInstance player, int itemId, long count, byte attributeId, int attributeLevel)
	{
		if (count <= 0)
		{
			return;
		}
		
		// Add items to player's inventory
		final L2ItemInstance item = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
		if (item == null)
		{
			return;
		}
		
		// set enchant level for item if that item is not adena
		if ((attributeId >= 0) && (attributeLevel > 0))
		{
			item.setElementAttr(attributeId, attributeLevel);
			if (item.isEquipped())
			{
				item.updateElementAttrBonus(player);
			}
			
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(item);
			player.sendPacket(iu);
		}
		
		sendItemGetMessage(player, item, count);
	}
	
	/**
	 * Give the specified player a set amount of items if he is lucky enough.<br>
	 * Not recommended to use this for non-stacking items.
	 * @param player : the player to give the item(s) to
	 * @param itemId : the ID of the item to give
	 * @param amountToGive : the amount of items to give
	 * @param limit : the maximum amount of items the player can have. Won't give more if this limit is reached. 0 - no limit.
	 * @param dropChance : the drop chance as a decimal digit from 0 to 1
	 * @param playSound : if true, plays ItemSound.quest_itemget when items are given and ItemSound.quest_middle when the limit is reached
	 * @return {@code true} if the player has collected {@code limit} of the specified item, {@code false} otherwise
	 */
	public static boolean giveItemRandomly(L2PcInstance player, int itemId, long amountToGive, long limit, double dropChance, boolean playSound)
	{
		return giveItemRandomly(player, null, itemId, amountToGive, amountToGive, limit, dropChance, playSound);
	}
	
	/**
	 * Give the specified player a set amount of items if he is lucky enough.<br>
	 * Not recommended to use this for non-stacking items.
	 * @param player : the player to give the item(s) to
	 * @param npc : the NPC that "dropped" the item (can be null)
	 * @param itemId : the ID of the item to give
	 * @param amountToGive : the amount of items to give
	 * @param limit : the maximum amount of items the player can have. Won't give more if this limit is reached. 0 - no limit.
	 * @param dropChance : the drop chance as a decimal digit from 0 to 1
	 * @param playSound : if true, plays ItemSound.quest_itemget when items are given and ItemSound.quest_middle when the limit is reached
	 * @return {@code true} if the player has collected {@code limit} of the specified item, {@code false} otherwise
	 */
	public static boolean giveItemRandomly(L2PcInstance player, L2Npc npc, int itemId, long amountToGive, long limit, double dropChance, boolean playSound)
	{
		return giveItemRandomly(player, npc, itemId, amountToGive, amountToGive, limit, dropChance, playSound);
	}
	
	/**
	 * Give the specified player a random amount of items if he is lucky enough.<br>
	 * Not recommended to use this for non-stacking items.
	 * @param player : the player to give the item(s) to
	 * @param npc : the NPC that "dropped" the item (can be null)
	 * @param itemId : the ID of the item to give
	 * @param minAmount : the minimum amount of items to give
	 * @param maxAmount : the maximum amount of items to give (will give a random amount between min/maxAmount multiplied by quest rates)
	 * @param limit : the maximum amount of items the player can have. Won't give more if this limit is reached. 0 - no limit.
	 * @param dropChance : the drop chance as a decimal digit from 0 to 1
	 * @param playSound : if true, plays ItemSound.quest_itemget when items are given and ItemSound.quest_middle when the limit is reached
	 * @return {@code true} if the player has collected {@code limit} of the specified item, {@code false} otherwise
	 */
	public static boolean giveItemRandomly(L2PcInstance player, L2Npc npc, int itemId, long minAmount, long maxAmount, long limit, double dropChance, boolean playSound)
	{
		final long currentCount = getQuestItemsCount(player, itemId);
		
		if ((limit > 0) && (currentCount >= limit))
		{
			return true;
		}
		
		minAmount *= Config.RATE_QUEST_DROP;
		maxAmount *= Config.RATE_QUEST_DROP;
		dropChance *= Config.RATE_QUEST_DROP; // TODO separate configs for rate and amount
		
		if ((npc != null) && Config.L2JMOD_CHAMPION_ENABLE && npc.isChampion())
		{
			dropChance *= Config.L2JMOD_CHAMPION_REWARDS;
			
			if ((itemId == Inventory.ADENA_ID) || (itemId == Inventory.ANCIENT_ADENA_ID))
			{
				minAmount *= Config.L2JMOD_CHAMPION_ADENAS_REWARDS;
				maxAmount *= Config.L2JMOD_CHAMPION_ADENAS_REWARDS;
			}
			else
			{
				minAmount *= Config.L2JMOD_CHAMPION_REWARDS;
				maxAmount *= Config.L2JMOD_CHAMPION_REWARDS;
			}
		}
		
		long amountToGive = ((minAmount == maxAmount) ? minAmount : Rnd.get(minAmount, maxAmount));
		final double random = Rnd.nextDouble();
		
		// Inventory slot check (almost useless for non-stacking items)
		if ((dropChance >= random) && (amountToGive > 0) && player.getInventory().validateCapacityByItemId(itemId))
		{
			if ((limit > 0) && ((currentCount + amountToGive) > limit))
			{
				amountToGive = limit - currentCount;
			}
			// Give the item to player
			L2ItemInstance item = player.addItem("Quest", itemId, amountToGive, npc, true);
			
			if (item != null)
			{
				// limit reached
				if ((currentCount + amountToGive) == limit)
				{
					if (playSound)
					{
						playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					return true;
				}
				
				if (playSound)
				{
					playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		return false;
	}
	
	/**
	 * Take an amount of a specified item from player's inventory.
	 * @param player the player whose item to take
	 * @param itemId the Id of the item to take
	 * @param amount the amount to take
	 * @return {@code true} if any items were taken, {@code false} otherwise
	 */
	public static boolean takeItems(L2PcInstance player, int itemId, long amount)
	{
		// Get object item from player's inventory list
		final L2ItemInstance item = player.getInventory().getItemByItemId(itemId);
		if (item == null)
		{
			return false;
		}
		
		// Tests on count value in order not to have negative value
		if ((amount < 0) || (amount > item.getCount()))
		{
			amount = item.getCount();
		}
		
		// Destroy the quantity of items wanted
		if (item.isEquipped())
		{
			final L2ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance itm : unequiped)
			{
				iu.addModifiedItem(itm);
			}
			player.sendPacket(iu);
			player.broadcastUserInfo();
		}
		return player.destroyItemByItemId("Quest", itemId, amount, player, true);
	}
	
	/**
	 * Take a set amount of a specified item from player's inventory.
	 * @param player the player whose item to take
	 * @param holder the {@link ItemHolder} object containing the ID and count of the item to take
	 * @return {@code true} if the item was taken, {@code false} otherwise
	 */
	protected static boolean takeItem(L2PcInstance player, ItemHolder holder)
	{
		if (holder == null)
		{
			return false;
		}
		return takeItems(player, holder.getId(), holder.getCount());
	}
	
	/**
	 * Take a set amount of all specified items from player's inventory.
	 * @param player the player whose items to take
	 * @param itemList the list of {@link ItemHolder} objects containing the IDs and counts of the items to take
	 * @return {@code true} if all items were taken, {@code false} otherwise
	 */
	protected static boolean takeAllItems(L2PcInstance player, ItemHolder... itemList)
	{
		if ((itemList == null) || (itemList.length == 0))
		{
			return false;
		}
		// first check if the player has all items to avoid taking half the items from the list
		if (!hasAllItems(player, true, itemList))
		{
			return false;
		}
		for (ItemHolder item : itemList)
		{
			// this should never be false, but just in case
			if (!takeItem(player, item))
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Take an amount of all specified items from player's inventory.
	 * @param player the player whose items to take
	 * @param amount the amount to take of each item
	 * @param itemIds a list or an array of Ids of the items to take
	 * @return {@code true} if all items were taken, {@code false} otherwise
	 */
	public static boolean takeItems(L2PcInstance player, int amount, int... itemIds)
	{
		boolean check = true;
		if (itemIds != null)
		{
			for (int item : itemIds)
			{
				check &= takeItems(player, item, amount);
			}
		}
		return check;
	}
	
	/**
	 * Remove all quest items associated with this quest from the specified player's inventory.
	 * @param player the player whose quest items to remove
	 */
	public void removeRegisteredQuestItems(L2PcInstance player)
	{
		takeItems(player, -1, questItemIds);
	}
	
	/**
	 * Send a packet in order to play a sound to the player.
	 * @param player the player whom to send the packet
	 * @param sound the name of the sound to play
	 */
	public static void playSound(L2PcInstance player, String sound)
	{
		player.sendPacket(QuestSound.getSound(sound));
	}
	
	/**
	 * Send a packet in order to play a sound to the player.
	 * @param player the player whom to send the packet
	 * @param sound the {@link QuestSound} object of the sound to play
	 */
	public static void playSound(L2PcInstance player, QuestSound sound)
	{
		player.sendPacket(sound.getPacket());
	}
	
	/**
	 * Add EXP and SP as quest reward.
	 * @param player the player whom to reward with the EXP/SP
	 * @param exp the amount of EXP to give to the player
	 * @param sp the amount of SP to give to the player
	 */
	public static void addExpAndSp(L2PcInstance player, long exp, int sp)
	{
		player.addExpAndSp((long) player.calcStat(Stats.EXPSP_RATE, exp * Config.RATE_QUEST_REWARD_XP, null, null), (int) player.calcStat(Stats.EXPSP_RATE, sp * Config.RATE_QUEST_REWARD_SP, null, null));
	}
	
	/**
	 * Get a random integer from 0 (inclusive) to {@code max} (exclusive).<br>
	 * Use this method instead of importing {@link l2r.util.Rnd} utility.
	 * @param max the maximum value for randomization
	 * @return a random integer number from 0 to {@code max - 1}
	 */
	public static int getRandom(int max)
	{
		return Rnd.get(max);
	}
	
	/**
	 * Get a random integer from {@code min} (inclusive) to {@code max} (inclusive).<br>
	 * Use this method instead of importing {@link l2r.util.Rnd} utility.
	 * @param min the minimum value for randomization
	 * @param max the maximum value for randomization
	 * @return a random integer number from {@code min} to {@code max}
	 */
	public static int getRandom(int min, int max)
	{
		return Rnd.get(min, max);
	}
	
	/**
	 * Get a random boolean.
	 * @return {@code true} or {@code false} randomly
	 */
	public static boolean getRandomBoolean()
	{
		return Rnd.nextBoolean();
	}
	
	/**
	 * Get the Id of the item equipped in the specified inventory slot of the player.
	 * @param player the player whose inventory to check
	 * @param slot the location in the player's inventory to check
	 * @return the Id of the item equipped in the specified inventory slot or 0 if the slot is empty or item is {@code null}.
	 */
	public static int getItemEquipped(L2PcInstance player, int slot)
	{
		return player.getInventory().getPaperdollItemId(slot);
	}
	
	/**
	 * @return the number of ticks from the {@link l2r.gameserver.GameTimeController}.
	 */
	public static int getGameTicks()
	{
		return GameTimeController.getInstance().getGameTicks();
	}
	
	/**
	 * Executes a procedure for each player, depending on the parameters.
	 * @param player the player were the procedure will be executed
	 * @param npc the related Npc
	 * @param isSummon {@code true} if the event that call this method was originated by the player's summon
	 * @param includeParty if {@code true} #actionForEachPlayer(L2PcInstance, L2Npc, boolean) will be called with the player's party members
	 * @param includeCommandChannel if {@code true} {@link #actionForEachPlayer(L2PcInstance, L2Npc, boolean)} will be called with the player's command channel members
	 * @see #actionForEachPlayer(L2PcInstance, L2Npc, boolean)
	 */
	public final void executeForEachPlayer(L2PcInstance player, final L2Npc npc, final boolean isSummon, boolean includeParty, boolean includeCommandChannel)
	{
		if ((includeParty || includeCommandChannel) && player.isInParty())
		{
			if (includeCommandChannel && player.getParty().isInCommandChannel())
			{
				player.getParty().getCommandChannel().forEachMember(member ->
				{
					actionForEachPlayer(member, npc, isSummon);
					return true;
				});
			}
			else if (includeParty)
			{
				player.getParty().forEachMember(member ->
				{
					actionForEachPlayer(member, npc, isSummon);
					return true;
				});
			}
		}
		else
		{
			actionForEachPlayer(player, npc, isSummon);
		}
	}
	
	/**
	 * Overridable method called from {@link #executeForEachPlayer(L2PcInstance, L2Npc, boolean, boolean, boolean)}
	 * @param player the player where the action will be run
	 * @param npc the Npc related to this action
	 * @param isSummon {@code true} if the event that call this method was originated by the player's summon
	 */
	public void actionForEachPlayer(L2PcInstance player, L2Npc npc, boolean isSummon)
	{
		// To be overridden in quest scripts.
	}
	
	/**
	 * Opens the door if presents on the instance and its not open.
	 * @param doorId
	 * @param instanceId
	 */
	public void openDoor(int doorId, int instanceId)
	{
		final L2DoorInstance door = getDoor(doorId, instanceId);
		if (door == null)
		{
			_log.warn(getClass().getSimpleName() + ": called openDoor(" + doorId + ", " + instanceId + "); but door wasnt found!", new NullPointerException());
		}
		else if (!door.getOpen())
		{
			door.openMe();
		}
	}
	
	/**
	 * Closes the door if presents on the instance and its open
	 * @param doorId
	 * @param instanceId
	 */
	public void closeDoor(int doorId, int instanceId)
	{
		final L2DoorInstance door = getDoor(doorId, instanceId);
		if (door == null)
		{
			_log.warn(getClass().getSimpleName() + ": called closeDoor(" + doorId + ", " + instanceId + "); but door wasnt found!", new NullPointerException());
		}
		else if (door.getOpen())
		{
			door.closeMe();
		}
	}
	
	/**
	 * Retriving Door from instances or from the real world.
	 * @param doorId
	 * @param instanceId
	 * @return {@link L2DoorInstance}
	 */
	public L2DoorInstance getDoor(int doorId, int instanceId)
	{
		L2DoorInstance door = null;
		if (instanceId <= 0)
		{
			door = DoorData.getInstance().getDoor(doorId);
		}
		else
		{
			final Instance inst = InstanceManager.getInstance().getInstance(instanceId);
			if (inst != null)
			{
				door = inst.getDoor(doorId);
			}
		}
		return door;
	}
	
	/**
	 * Teleport player to/from instance
	 * @param player
	 * @param loc
	 * @param instanceId
	 */
	public void teleportPlayer(L2PcInstance player, Location loc, int instanceId)
	{
		teleportPlayer(player, loc, instanceId, true);
	}
	
	/**
	 * Teleport player to/from instance
	 * @param player
	 * @param loc
	 * @param instanceId
	 * @param allowRandomOffset
	 */
	public void teleportPlayer(L2PcInstance player, Location loc, int instanceId, boolean allowRandomOffset)
	{
		loc.setInstanceId(instanceId);
		player.teleToLocation(loc, allowRandomOffset);
	}
	
	public static final void specialCamera(L2PcInstance player, L2Character creature, int force, int angle1, int angle2, int time, int range, int duration, int relYaw, int relPitch, int isWide, int relAngle)
	{
		player.sendPacket(new SpecialCamera(creature, force, angle1, angle2, time, range, duration, relYaw, relPitch, isWide, relAngle));
	}
	
	public static final void specialCameraEx(L2PcInstance player, L2Character creature, int force, int angle1, int angle2, int time, int duration, int relYaw, int relPitch, int isWide, int relAngle)
	{
		player.sendPacket(new SpecialCamera(creature, player, force, angle1, angle2, time, duration, relYaw, relPitch, isWide, relAngle));
	}
	
	public static final void specialCamera3(L2PcInstance player, L2Character creature, int force, int angle1, int angle2, int time, int range, int duration, int relYaw, int relPitch, int isWide, int relAngle, int unk)
	{
		player.sendPacket(new SpecialCamera(creature, force, angle1, angle2, time, range, duration, relYaw, relPitch, isWide, relAngle, unk));
	}
	
	public L2Npc spawnNpc(int npcId, int x, int y, int z, int heading, int instId)
	{
		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		Instance inst = InstanceManager.getInstance().getInstance(instId);
		try
		{
			L2Spawn npcSpawn = new L2Spawn(npcTemplate);
			npcSpawn.setX(x);
			npcSpawn.setY(y);
			npcSpawn.setZ(z);
			npcSpawn.setHeading(heading);
			npcSpawn.setAmount(1);
			npcSpawn.setInstanceId(instId);
			SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
			L2Npc npc = npcSpawn.spawnOne(false);
			inst.addNpc(npc);
			return npc;
		}
		catch (Exception ignored)
		{
		}
		return null;
	}
	
	public L2Npc spawnNpc(int npcId, Location loc, int heading, int instId)
	{
		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		Instance inst = InstanceManager.getInstance().getInstance(instId);
		try
		{
			L2Spawn npcSpawn = new L2Spawn(npcTemplate);
			npcSpawn.setX(loc.getX());
			npcSpawn.setY(loc.getY());
			npcSpawn.setZ(loc.getZ());
			npcSpawn.setHeading(loc.getHeading());
			npcSpawn.setAmount(1);
			npcSpawn.setInstanceId(instId);
			SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
			L2Npc npc = npcSpawn.spawnOne(false);
			inst.addNpc(npc);
			return npc;
		}
		catch (Exception ignored)
		{
		}
		return null;
	}
	
	public String onKillByMob(L2Npc npc, L2Npc killer)
	{
		return null;
	}
	
	public List<L2PcInstance> getPartyMembers(L2PcInstance player, L2Npc npc, String var, String value)
	{
		ArrayList<L2PcInstance> candidates = new ArrayList<>();
		
		if ((player != null) && (player.isInParty()))
		{
			for (L2PcInstance partyMember : player.getParty().getMembers())
			{
				if (partyMember != null)
				{
					if (checkPlayerCondition(partyMember, npc, var, value) != null)
					{
						candidates.add(partyMember);
					}
				}
			}
		}
		else if (checkPlayerCondition(player, npc, var, value) != null)
		{
			candidates.add(player);
		}
		return candidates;
	}
	
	public QuestState checkPlayerCondition(L2PcInstance player, L2Npc npc, String var, String value)
	{
		if (player == null)
		{
			return null;
		}
		
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		if ((st.get(var) == null) || (!value.equalsIgnoreCase(st.get(var))))
		{
			return null;
		}
		
		if (npc == null)
		{
			return null;
		}
		
		if (!player.isInsideRadius(npc, Config.ALT_PARTY_RANGE, true, false))
		{
			return null;
		}
		return st;
	}
	
	protected static boolean isIntInArray(int i, int[] ia)
	{
		for (int v : ia)
		{
			if (i == v)
			{
				return true;
			}
		}
		return false;
	}
}
