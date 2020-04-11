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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.enums.QuestSound;
import l2r.gameserver.enums.QuestType;
import l2r.gameserver.instancemanager.PcCafePointsManager;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.ItemHolder;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.network.serverpackets.ExShowQuestMark;
import l2r.gameserver.network.serverpackets.PlaySound;
import l2r.gameserver.network.serverpackets.QuestList;
import l2r.gameserver.network.serverpackets.TutorialCloseHtml;
import l2r.gameserver.network.serverpackets.TutorialEnableClientEvent;
import l2r.gameserver.network.serverpackets.TutorialShowHtml;
import l2r.gameserver.network.serverpackets.TutorialShowQuestionMark;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quest state class.
 * @author Luis Arias
 */
public final class QuestState
{
	protected static final Logger _log = LoggerFactory.getLogger(QuestState.class);
	
	/** The name of the quest of this QuestState */
	private final String _questName;
	
	/** The "owner" of this QuestState object */
	private final L2PcInstance _player;
	
	/** The current state of the quest */
	private byte _state;
	
	/** A map of key->value pairs containing the quest state variables and their values */
	private Map<String, String> _vars;
	
	/**
	 * boolean flag letting QuestStateManager know to exit quest when cleaning up
	 */
	private boolean _isExitQuestOnCleanUp = false;
	
	/**
	 * Constructor of the QuestState. Creates the QuestState object and sets the player's progress of the quest to this QuestState.
	 * @param quest the {@link Quest} object associated with the QuestState
	 * @param player the owner of this {@link QuestState} object
	 * @param state the initial state of the quest
	 */
	public QuestState(Quest quest, L2PcInstance player, byte state)
	{
		_questName = quest.getName();
		_player = player;
		_state = state;
		
		player.setQuestState(this);
	}
	
	/**
	 * @return the name of the quest of this QuestState
	 */
	public String getQuestName()
	{
		return _questName;
	}
	
	/**
	 * @return the {@link Quest} object of this QuestState
	 */
	public Quest getQuest()
	{
		return QuestManager.getInstance().getQuest(_questName);
	}
	
	/**
	 * @return the {@link L2PcInstance} object of the owner of this QuestState
	 */
	public L2PcInstance getPlayer()
	{
		return _player;
	}
	
	/**
	 * @return the current State of this QuestState
	 * @see l2r.gameserver.model.quest.State
	 */
	public byte getState()
	{
		return _state;
	}
	
	/**
	 * @return {@code true} if the State of this QuestState is CREATED, {@code false} otherwise
	 * @see l2r.gameserver.model.quest.State
	 */
	public boolean isCreated()
	{
		return (_state == State.CREATED);
	}
	
	/**
	 * @return {@code true} if the State of this QuestState is STARTED, {@code false} otherwise
	 * @see l2r.gameserver.model.quest.State
	 */
	public boolean isStarted()
	{
		return (_state == State.STARTED);
	}
	
	/**
	 * @return {@code true} if the State of this QuestState is COMPLETED, {@code false} otherwise
	 * @see l2r.gameserver.model.quest.State
	 */
	public boolean isCompleted()
	{
		return (_state == State.COMPLETED);
	}
	
	/**
	 * @param state the new state of the quest to set
	 * @return {@code true} if state was changed, {@code false} otherwise
	 * @see #setState(byte state, boolean saveInDb)
	 * @see l2r.gameserver.model.quest.State
	 */
	public boolean setState(byte state)
	{
		return setState(state, true);
	}
	
	/**
	 * Change the state of this quest to the specified value.
	 * @param state the new state of the quest to set
	 * @param saveInDb if {@code true}, will save the state change in the database
	 * @return {@code true} if state was changed, {@code false} otherwise
	 * @see l2r.gameserver.model.quest.State
	 */
	public boolean setState(byte state, boolean saveInDb)
	{
		if (_state == state)
		{
			return false;
		}
		final boolean newQuest = isCreated();
		_state = state;
		if (saveInDb)
		{
			if (newQuest)
			{
				Quest.createQuestInDb(this);
			}
			else
			{
				Quest.updateQuestInDb(this);
			}
		}
		
		_player.sendPacket(new QuestList());
		return true;
	}
	
	/**
	 * Add parameter used in quests.
	 * @param var String pointing out the name of the variable for quest
	 * @param val String pointing out the value of the variable for quest
	 * @return String (equal to parameter "val")
	 */
	public String setInternal(String var, String val)
	{
		if (_vars == null)
		{
			_vars = new HashMap<>();
		}
		
		if (val == null)
		{
			val = "";
		}
		
		_vars.put(var, val);
		return val;
	}
	
	public String set(String var, int val)
	{
		return set(var, Integer.toString(val));
	}
	
	/**
	 * Return value of parameter "val" after adding the couple (var,val) in class variable "vars".<br>
	 * Actions:<br>
	 * <ul>
	 * <li>Initialize class variable "vars" if is null.</li>
	 * <li>Initialize parameter "val" if is null</li>
	 * <li>Add/Update couple (var,val) in class variable FastMap "vars"</li>
	 * <li>If the key represented by "var" exists in FastMap "vars", the couple (var,val) is updated in the database.<br>
	 * The key is known as existing if the preceding value of the key (given as result of function put()) is not null.<br>
	 * If the key doesn't exist, the couple is added/created in the database</li>
	 * <ul>
	 * @param var String indicating the name of the variable for quest
	 * @param val String indicating the value of the variable for quest
	 * @return String (equal to parameter "val")
	 */
	public String set(String var, String val)
	{
		if (_vars == null)
		{
			_vars = new HashMap<>();
		}
		
		if (val == null)
		{
			val = "";
		}
		
		String old = _vars.put(var, val);
		if (old != null)
		{
			Quest.updateQuestVarInDb(this, var, val);
		}
		else
		{
			Quest.createQuestVarInDb(this, var, val);
		}
		
		if ("cond".equals(var))
		{
			try
			{
				int previousVal = 0;
				try
				{
					previousVal = Integer.parseInt(old);
				}
				catch (Exception ex)
				{
					previousVal = 0;
				}
				setCond(Integer.parseInt(val), previousVal);
			}
			catch (Exception e)
			{
				_log.warn(_player.getName() + ", " + getQuestName() + " cond [" + val + "] is not an integer.  Value stored, but no packet was sent: " + e.getMessage(), e);
			}
		}
		
		return val;
	}
	
	/**
	 * Internally handles the progression of the quest so that it is ready for sending appropriate packets to the client.<br>
	 * <u><i>Actions :</i></u><br>
	 * <ul>
	 * <li>Check if the new progress number resets the quest to a previous (smaller) step.</li>
	 * <li>If not, check if quest progress steps have been skipped.</li>
	 * <li>If skipped, prepare the variable completedStateFlags appropriately to be ready for sending to clients.</li>
	 * <li>If no steps were skipped, flags do not need to be prepared...</li>
	 * <li>If the passed step resets the quest to a previous step, reset such that steps after the parameter are not considered, while skipped steps before the parameter, if any, maintain their info.</li>
	 * </ul>
	 * @param cond the current quest progress condition (0 - 31 including)
	 * @param old the previous quest progress condition to check against
	 */
	private void setCond(int cond, int old)
	{
		if (cond == old)
		{
			return;
		}
		
		int completedStateFlags = 0;
		// cond 0 and 1 do not need completedStateFlags. Also, if cond > 1, the 1st step must
		// always exist (i.e. it can never be skipped). So if cond is 2, we can still safely
		// assume no steps have been skipped.
		// Finally, more than 31 steps CANNOT be supported in any way with skipping.
		if ((cond < 3) || (cond > 31))
		{
			unset("__compltdStateFlags");
		}
		else
		{
			completedStateFlags = getInt("__compltdStateFlags");
		}
		
		// case 1: No steps have been skipped so far...
		if (completedStateFlags == 0)
		{
			// check if this step also doesn't skip anything. If so, no further work is needed
			// also, in this case, no work is needed if the state is being reset to a smaller value
			// in those cases, skip forward to informing the client about the change...
			
			// ELSE, if we just now skipped for the first time...prepare the flags!!!
			if (cond > (old + 1))
			{
				// set the most significant bit to 1 (indicates that there exist skipped states)
				// also, ensure that the least significant bit is an 1 (the first step is never skipped, no matter
				// what the cond says)
				completedStateFlags = 0x80000001;
				
				// since no flag had been skipped until now, the least significant bits must all
				// be set to 1, up until "old" number of bits.
				completedStateFlags |= ((1 << old) - 1);
				
				// now, just set the bit corresponding to the passed cond to 1 (current step)
				completedStateFlags |= (1 << (cond - 1));
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}
		// case 2: There were exist previously skipped steps
		// if this is a push back to a previous step, clear all completion flags ahead
		else if (cond < old)
		{
			// note, this also unsets the flag indicating that there exist skips
			completedStateFlags &= ((1 << cond) - 1);
			
			// now, check if this resulted in no steps being skipped any more
			if (completedStateFlags == ((1 << cond) - 1))
			{
				unset("__compltdStateFlags");
			}
			else
			{
				// set the most significant bit back to 1 again, to correctly indicate that this skips states.
				// also, ensure that the least significant bit is an 1 (the first step is never skipped, no matter
				// what the cond says)
				completedStateFlags |= 0x80000001;
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}
		// If this moves forward, it changes nothing on previously skipped steps.
		// Just mark this state and we are done.
		else
		{
			completedStateFlags |= (1 << (cond - 1));
			set("__compltdStateFlags", String.valueOf(completedStateFlags));
		}
		
		// send a packet to the client to inform it of the quest progress (step change)
		_player.sendPacket(new QuestList());
		
		final Quest q = getQuest();
		if (!q.isCustomQuest() && (cond > 0))
		{
			_player.sendPacket(new ExShowQuestMark(q.getId()));
		}
	}
	
	/**
	 * Removes a quest variable from the list of existing quest variables.
	 * @param var the name of the variable to remove
	 * @return the previous value of the variable or {@code null} if none were found
	 */
	public String unset(String var)
	{
		if (_vars == null)
		{
			return null;
		}
		
		String old = _vars.remove(var);
		if (old != null)
		{
			Quest.deleteQuestVarInDb(this, var);
		}
		return old;
	}
	
	/**
	 * Insert (or update) in the database variables that need to stay persistent for this player after a reboot. This function is for storage of values that are not related to a specific quest but are global instead, i.e. can be used by any script.
	 * @param var the name of the variable to save
	 * @param value the value of the variable
	 */
	// TODO: these methods should not be here, they could be used by other classes to save some variables, but they can't because they require to create a QuestState first.
	public final void saveGlobalQuestVar(String var, String value)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("REPLACE INTO character_quest_global_data (charId, var, value) VALUES (?, ?, ?)"))
		{
			statement.setInt(1, _player.getObjectId());
			statement.setString(2, var);
			statement.setString(3, value);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warn("Could not insert player's global quest variable: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Read from the database a previously saved variable for this quest.<br>
	 * Due to performance considerations, this function should best be used only when the quest is first loaded.<br>
	 * Subclasses of this class can define structures into which these loaded values can be saved.<br>
	 * However, on-demand usage of this function throughout the script is not prohibited, only not recommended.<br>
	 * Values read from this function were entered by calls to "saveGlobalQuestVar".
	 * @param var the name of the variable whose value to get
	 * @return the value of the variable or an empty string if the variable does not exist in the database
	 */
	// TODO: these methods should not be here, they could be used by other classes to save some variables, but they can't because they require to create a QuestState first.
	public final String getGlobalQuestVar(String var)
	{
		String result = "";
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT value FROM character_quest_global_data WHERE charId = ? AND var = ?"))
		{
			ps.setInt(1, _player.getObjectId());
			ps.setString(2, var);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.first())
				{
					result = rs.getString(1);
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Could not load player's global quest variable: " + e.getMessage(), e);
		}
		return result;
	}
	
	/**
	 * Permanently delete a global quest variable from the database.
	 * @param var the name of the variable to delete
	 */
	public final void deleteGlobalQuestVar(String var)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_quest_global_data WHERE charId = ? AND var = ?"))
		{
			statement.setInt(1, _player.getObjectId());
			statement.setString(2, var);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warn("could not delete player's global quest variable; charId = " + _player.getObjectId() + ", variable name = " + var + ". Exception: " + e.getMessage(), e);
		}
	}
	
	/**
	 * @param var the name of the variable to get
	 * @return the value of the variable from the list of quest variables
	 */
	public String get(String var)
	{
		if (_vars == null)
		{
			return null;
		}
		
		return _vars.get(var);
	}
	
	/**
	 * @param var the name of the variable to get
	 * @return the integer value of the variable or 0 if the variable does not exist or its value is not an integer
	 */
	public int getInt(String var)
	{
		if (_vars == null)
		{
			return 0;
		}
		
		final String variable = _vars.get(var);
		if ((variable == null) || variable.isEmpty())
		{
			return 0;
		}
		
		int varint = 0;
		try
		{
			varint = Integer.parseInt(variable);
		}
		catch (NumberFormatException nfe)
		{
			_log.info("Quest " + getQuestName() + ", method getInt(" + var + "), tried to parse a non-integer value (" + variable + "). Char Id: " + _player.getObjectId(), nfe);
		}
		
		return varint;
	}
	
	/**
	 * Checks if the quest state progress ({@code cond}) is at the specified step.
	 * @param condition the condition to check against
	 * @return {@code true} if the quest condition is equal to {@code condition}, {@code false} otherwise
	 * @see #getInt(String var)
	 */
	public boolean isCond(int condition)
	{
		return (getInt("cond") == condition);
	}
	
	/**
	 * Sets the quest state progress ({@code cond}) to the specified step.
	 * @param value the new value of the quest state progress
	 * @return this {@link QuestState} object
	 * @see #set(String var, String val)
	 * @see #setCond(int, boolean)
	 */
	public QuestState setCond(int value)
	{
		if (isStarted())
		{
			set("cond", Integer.toString(value));
		}
		return this;
	}
	
	/**
	 * @return the current quest progress ({@code cond})
	 */
	public int getCond()
	{
		if (isStarted())
		{
			return getInt("cond");
		}
		return 0;
	}
	
	/**
	 * Check if a given variable is set for this quest.
	 * @param variable the variable to check
	 * @return {@code true} if the variable is set, {@code false} otherwise
	 * @see #get(String)
	 * @see #getInt(String)
	 * @see #getCond()
	 */
	public boolean isSet(String variable)
	{
		return (get(variable) != null);
	}
	
	/**
	 * Sets the quest state progress ({@code cond}) to the specified step.
	 * @param value the new value of the quest state progress
	 * @param playQuestMiddle if {@code true}, plays "ItemSound.quest_middle"
	 * @return this {@link QuestState} object
	 * @see #setCond(int value)
	 * @see #set(String var, String val)
	 */
	public QuestState setCond(int value, boolean playQuestMiddle)
	{
		if (!isStarted())
		{
			return this;
		}
		set("cond", String.valueOf(value));
		
		if (playQuestMiddle)
		{
			Quest.playSound(_player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		return this;
	}
	
	public QuestState setMemoState(int value)
	{
		set("memoState", String.valueOf(value));
		return this;
	}
	
	/**
	 * @return the current Memo State
	 */
	public int getMemoState()
	{
		if (isStarted())
		{
			return getInt("memoState");
		}
		return 0;
	}
	
	public boolean isMemoState(int memoState)
	{
		return (getInt("memoState") == memoState);
	}
	
	/**
	 * Add player to get notification of characters death
	 * @param character the {@link L2Character} object of the character to get notification of death
	 */
	public void addNotifyOfDeath(L2Character character)
	{
		if (!(character instanceof L2PcInstance))
		{
			return;
		}
		
		((L2PcInstance) character).addNotifyQuestOfDeath(this);
	}
	
	// TODO: This all remains because of backward compatibility, should be cleared when all scripts are rewritten in java
	
	/**
	 * Return the quantity of one sort of item hold by the player
	 * @param itemId the Id of the item wanted to be count
	 * @return long
	 */
	public long getQuestItemsCount(int itemId)
	{
		return Quest.getQuestItemsCount(_player, itemId);
	}
	
	/**
	 * @param itemId the Id of the item required
	 * @return true if item exists in player's inventory, false - if not
	 */
	public boolean hasQuestItems(int itemId)
	{
		return Quest.hasQuestItems(_player, itemId);
	}
	
	/**
	 * @param itemIds list of items that are required
	 * @return true if all items exists in player's inventory, false - if not
	 */
	public boolean hasQuestItems(int... itemIds)
	{
		return Quest.hasQuestItems(_player, itemIds);
	}
	
	/**
	 * Return the level of enchantment on the weapon of the player(Done specifically for weapon SA's)
	 * @param itemId Id of the item to check enchantment
	 * @return int
	 */
	public int getEnchantLevel(int itemId)
	{
		return Quest.getEnchantLevel(_player, itemId);
	}
	
	/**
	 * Give adena to the player
	 * @param count
	 * @param applyRates
	 */
	public void giveAdena(long count, boolean applyRates)
	{
		giveItems(Inventory.ADENA_ID, count, applyRates ? 0 : 1);
	}
	
	/**
	 * Give reward to player using multiplier's
	 * @param item
	 */
	public void rewardItems(ItemHolder item)
	{
		Quest.rewardItems(_player, item);
	}
	
	/**
	 * Give reward to player using multiplier's
	 * @param itemId
	 * @param count
	 */
	public void rewardItems(int itemId, long count)
	{
		Quest.rewardItems(_player, itemId, count);
	}
	
	/**
	 * Give item/reward to the player
	 * @param itemId
	 * @param count
	 */
	public void giveItems(int itemId, long count)
	{
		Quest.giveItems(_player, itemId, count, 0);
	}
	
	public void giveItems(ItemHolder holder)
	{
		Quest.giveItems(_player, holder.getId(), holder.getCount(), 0);
	}
	
	public void giveItems(int itemId, long count, int enchantlevel)
	{
		Quest.giveItems(_player, itemId, count, enchantlevel);
	}
	
	public void giveItems(int itemId, long count, byte attributeId, int attributeLevel)
	{
		Quest.giveItems(_player, itemId, count, attributeId, attributeLevel);
	}
	
	public boolean giveItemRandomly(int itemId, long amount, long limit, double dropChance, boolean playSound)
	{
		return Quest.giveItemRandomly(_player, null, itemId, amount, amount, limit, dropChance, playSound);
	}
	
	public boolean giveItemRandomly(L2Npc npc, int itemId, long amount, long limit, double dropChance, boolean playSound)
	{
		return Quest.giveItemRandomly(_player, npc, itemId, amount, amount, limit, dropChance, playSound);
	}
	
	public boolean giveItemRandomly(L2Npc npc, int itemId, long minAmount, long maxAmount, long limit, double dropChance, boolean playSound)
	{
		return Quest.giveItemRandomly(_player, npc, itemId, minAmount, maxAmount, limit, dropChance, playSound);
	}
	
	// TODO: More radar functions need to be added when the radar class is complete.
	// BEGIN STUFF THAT WILL PROBABLY BE CHANGED
	public void addRadar(int x, int y, int z)
	{
		_player.getRadar().addMarker(x, y, z);
	}
	
	public void removeRadar(int x, int y, int z)
	{
		_player.getRadar().removeMarker(x, y, z);
	}
	
	public void clearRadar()
	{
		_player.getRadar().removeAllMarkers();
	}
	
	// END STUFF THAT WILL PROBABLY BE CHANGED
	
	/**
	 * Remove items from player's inventory when talking to NPC in order to have rewards.<br>
	 * Actions:<br>
	 * <ul>
	 * <li>Destroy quantity of items wanted</li>
	 * <li>Send new inventory list to player</li>
	 * </ul>
	 * @param itemId Identifier of the item
	 * @param count Quantity of items to destroy
	 */
	public void takeItems(int itemId, long count)
	{
		Quest.takeItems(_player, itemId, count);
	}
	
	/**
	 * Send a packet in order to play a sound to the player.
	 * @param sound the name of the sound to play
	 */
	public void playSound(String sound)
	{
		Quest.playSound(_player, sound);
	}
	
	/**
	 * Send a packet in order to play a sound to the player.
	 * @param sound the {@link QuestSound} object of the sound to play
	 */
	public void playSound(QuestSound sound)
	{
		Quest.playSound(_player, sound);
	}
	
	/**
	 * Add XP and SP as quest reward
	 * @param exp
	 * @param sp
	 */
	public void addExpAndSp(int exp, int sp)
	{
		Quest.addExpAndSp(_player, exp, sp);
		PcCafePointsManager.getInstance().givePcCafePoint(_player, (long) (exp * Config.RATE_QUEST_REWARD_XP));
	}
	
	public int getRandom(int max)
	{
		return Rnd.get(max);
	}
	
	/**
	 * @param loc
	 * @return number of ticks from GameTimeController
	 */
	public int getItemEquipped(int loc)
	{
		return Quest.getItemEquipped(_player, loc);
	}
	
	/**
	 * @return {@code true} if quest is to be exited on clean up by QuestStateManager, {@code false} otherwise
	 */
	public final boolean isExitQuestOnCleanUp()
	{
		return _isExitQuestOnCleanUp;
	}
	
	/**
	 * @param isExitQuestOnCleanUp {@code true} if quest is to be exited on clean up by QuestStateManager, {@code false} otherwise
	 */
	public void setIsExitQuestOnCleanUp(boolean isExitQuestOnCleanUp)
	{
		_isExitQuestOnCleanUp = isExitQuestOnCleanUp;
	}
	
	/**
	 * Start a timed event for a quest.<br>
	 * Will call an event in onEvent/onAdvEvent.
	 * @param name the name of the timer/event
	 * @param time time in milliseconds till the event is executed
	 */
	public void startQuestTimer(String name, long time)
	{
		getQuest().startQuestTimer(name, time, null, _player, false);
	}
	
	/**
	 * Start a timed event for a quest.<br>
	 * Will call an event in onEvent/onAdvEvent.
	 * @param name the name of the timer/event
	 * @param time time in milliseconds till the event is executed
	 * @param npc the L2Npc associated with this event
	 */
	public void startQuestTimer(String name, long time, L2Npc npc)
	{
		getQuest().startQuestTimer(name, time, npc, _player, false);
	}
	
	/**
	 * Start a repeating timed event for a quest.<br>
	 * Will call an event in onEvent/onAdvEvent.
	 * @param name the name of the timer/event
	 * @param time time in milliseconds till the event is executed/repeated
	 */
	public void startRepeatingQuestTimer(String name, long time)
	{
		getQuest().startQuestTimer(name, time, null, _player, true);
	}
	
	/**
	 * Start a repeating timed event for a quest.<br>
	 * Will call an event in onEvent/onAdvEvent.
	 * @param name the name of the timer/event
	 * @param time time in milliseconds till the event is executed/repeated
	 * @param npc the L2Npc associated with this event
	 */
	public void startRepeatingQuestTimer(String name, long time, L2Npc npc)
	{
		getQuest().startQuestTimer(name, time, npc, _player, true);
	}
	
	/**
	 * @param name the name of the QuestTimer required
	 * @return the {@link QuestTimer} object with the specified name or {@code null} if it doesn't exist
	 */
	public final QuestTimer getQuestTimer(String name)
	{
		return getQuest().getQuestTimer(name, null, _player);
	}
	
	// --- Spawn methods ---
	/**
	 * Add a temporary spawn of the specified npc.<br>
	 * Player's coordinates will be used for the spawn.
	 * @param npcId the Id of the npc to spawn
	 * @return the {@link L2Npc} object of the newly spawned npc or {@code null} if the npc doesn't exist
	 * @see #addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay, boolean isSummonSpawn)
	 */
	public L2Npc addSpawn(int npcId)
	{
		return addSpawn(npcId, _player.getX(), _player.getY(), _player.getZ(), 0, false, 0, false);
	}
	
	/**
	 * Add a temporary spawn of the specified npc.<br>
	 * Player's coordinates will be used for the spawn.
	 * @param npcId the Id of the npc to spawn
	 * @param despawnDelay time in milliseconds till the npc is despawned (default: 0)
	 * @return the {@link L2Npc} object of the newly spawned npc or {@code null} if the npc doesn't exist
	 * @see #addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay, boolean isSummonSpawn)
	 */
	public L2Npc addSpawn(int npcId, int despawnDelay)
	{
		return addSpawn(npcId, _player.getX(), _player.getY(), _player.getZ(), 0, false, despawnDelay, false);
	}
	
	/**
	 * Add a temporary spawn of the specified npc.
	 * @param npcId the Id of the npc to spawn
	 * @param x the X coordinate of the npc spawn location
	 * @param y the Y coordinate of the npc spawn location
	 * @param z the Z coordinate (height) of the npc spawn location
	 * @return the {@link L2Npc} object of the newly spawned npc or {@code null} if the npc doesn't exist
	 * @see #addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay, boolean isSummonSpawn)
	 */
	public L2Npc addSpawn(int npcId, int x, int y, int z)
	{
		return addSpawn(npcId, x, y, z, 0, false, 0, false);
	}
	
	/**
	 * Add a temporary spawn of the specified npc.
	 * @param npcId the Id of the npc to spawn
	 * @param x the X coordinate of the npc spawn location
	 * @param y the Y coordinate of the npc spawn location
	 * @param z the Z coordinate (height) of the npc spawn location
	 * @param despawnDelay time in milliseconds till the npc is despawned (default: 0)
	 * @return the {@link L2Npc} object of the newly spawned npc or {@code null} if the npc doesn't exist
	 * @see #addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay, boolean isSummonSpawn)
	 */
	public L2Npc addSpawn(int npcId, int x, int y, int z, int despawnDelay)
	{
		return addSpawn(npcId, x, y, z, 0, false, despawnDelay, false);
	}
	
	/**
	 * Add a temporary spawn of the specified npc.
	 * @param npcId the Id of the npc to spawn
	 * @param cha the character whose coordinates will be used for the npc spawn
	 * @return the {@link L2Npc} object of the newly spawned npc or {@code null} if the npc doesn't exist
	 * @see #addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay, boolean isSummonSpawn)
	 */
	public L2Npc addSpawn(int npcId, L2Character cha)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), true, 0, false);
	}
	
	/**
	 * Add a temporary spawn of the specified npc.
	 * @param npcId the Id of the npc to spawn
	 * @param cha the character whose coordinates will be used for the npc spawn
	 * @param despawnDelay time in milliseconds till the npc is despawned (default: 0)
	 * @return the {@link L2Npc} object of the newly spawned npc or {@code null} if the npc doesn't exist
	 * @see #addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay, boolean isSummonSpawn)
	 */
	public L2Npc addSpawn(int npcId, L2Character cha, int despawnDelay)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), true, despawnDelay, false);
	}
	
	/**
	 * Add a temporary spawn of the specified npc.
	 * @param npcId the Id of the npc to spawn
	 * @param cha the character whose coordinates will be used for the npc spawn
	 * @param randomOffset if {@code true}, adds +/- 50~100 to X/Y coordinates of the spawn location
	 * @param despawnDelay time in milliseconds till the npc is despawned (default: 0)
	 * @return the {@link L2Npc} object of the newly spawned npc or {@code null} if the npc doesn't exist
	 * @see #addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay, boolean isSummonSpawn)
	 */
	public L2Npc addSpawn(int npcId, L2Character cha, boolean randomOffset, int despawnDelay)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), randomOffset, despawnDelay, false);
	}
	
	/**
	 * Add a temporary spawn of the specified npc.
	 * @param npcId the Id of the npc to spawn
	 * @param x the X coordinate of the npc spawn location
	 * @param y the Y coordinate of the npc spawn location
	 * @param z the Z coordinate (height) of the npc spawn location
	 * @param heading the heading of the npc
	 * @param randomOffset if {@code true}, adds +/- 50~100 to X/Y coordinates of the spawn location
	 * @param despawnDelay time in milliseconds till the npc is despawned (default: 0)
	 * @return the {@link L2Npc} object of the newly spawned npc or {@code null} if the npc doesn't exist
	 * @see #addSpawn(int, int, int, int, int, boolean, int, boolean)
	 */
	public L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay)
	{
		return addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, false);
	}
	
	/**
	 * Add a temporary spawn of the specified npc.
	 * @param npcId the Id of the npc to spawn
	 * @param x the X coordinate of the npc spawn location
	 * @param y the Y coordinate of the npc spawn location
	 * @param z the Z coordinate (height) of the npc spawn location
	 * @param heading the heading of the npc
	 * @param randomOffset if {@code true}, adds +/- 50~100 to X/Y coordinates of the spawn location
	 * @param despawnDelay time in milliseconds till the npc is despawned (default: 0)
	 * @param isSummonSpawn if {@code true}, displays a summon animation on npc spawn (default: {@code false})
	 * @return the {@link L2Npc} object of the newly spawned npc or {@code null} if the npc doesn't exist
	 * @see #addSpawn(int)
	 * @see #addSpawn(int, int)
	 * @see #addSpawn(int, L2Character)
	 * @see #addSpawn(int, L2Character, int)
	 * @see #addSpawn(int, int, int, int)
	 * @see #addSpawn(int, L2Character, boolean, int)
	 * @see #addSpawn(int, int, int, int, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, int, boolean)
	 */
	public L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay, boolean isSummonSpawn)
	{
		return Quest.addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, isSummonSpawn);
	}
	
	/**
	 * @param fileName the name of the file you want to show. Must be in the same folder (or subfolder) as script
	 * @return a String containing the contents of the specified HTML file
	 */
	public String showHtmlFile(String fileName)
	{
		return getQuest().showHtmlFile(_player, fileName);
	}
	
	/**
	 * Set condition to 1, state to STARTED and play the "ItemSound.quest_accept".<br>
	 * Works only if state is CREATED and the quest is not a custom quest.
	 * @return the newly created {@code QuestState} object
	 */
	public QuestState startQuest()
	{
		if (isCreated() && !getQuest().isCustomQuest())
		{
			set("cond", "1");
			setState(State.STARTED);
			playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
		}
		return this;
	}
	
	/**
	 * Finishes the quest and removes all quest items associated with this quest from the player's inventory.<br>
	 * If {@code type} is {@code QuestType.ONE_TIME}, also removes all other quest data associated with this quest.
	 * @param type the {@link QuestType} of the quest
	 * @return this {@link QuestState} object
	 * @see #exitQuest(QuestType type, boolean playExitQuest)
	 * @see #exitQuest(boolean repeatable)
	 * @see #exitQuest(boolean repeatable, boolean playExitQuest)
	 */
	public QuestState exitQuest(QuestType type)
	{
		switch (type)
		{
			case DAILY:
			{
				exitQuest(false);
				setRestartTime();
				break;
			}
			// case ONE_TIME:
			// case REPEATABLE:
			default:
			{
				exitQuest(type == QuestType.REPEATABLE);
				break;
			}
		}
		return this;
	}
	
	/**
	 * Finishes the quest and removes all quest items associated with this quest from the player's inventory.<br>
	 * If {@code type} is {@code QuestType.ONE_TIME}, also removes all other quest data associated with this quest.
	 * @param type the {@link QuestType} of the quest
	 * @param playExitQuest if {@code true}, plays "ItemSound.quest_finish"
	 * @return this {@link QuestState} object
	 * @see #exitQuest(QuestType type)
	 * @see #exitQuest(boolean repeatable)
	 * @see #exitQuest(boolean repeatable, boolean playExitQuest)
	 */
	public QuestState exitQuest(QuestType type, boolean playExitQuest)
	{
		exitQuest(type);
		if (playExitQuest)
		{
			playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
		}
		return this;
	}
	
	/**
	 * Finishes the quest and removes all quest items associated with this quest from the player's inventory.<br>
	 * If {@code repeatable} is set to {@code false}, also removes all other quest data associated with this quest.
	 * @param repeatable if {@code true}, deletes all data and variables of this quest, otherwise keeps them
	 * @return this {@link QuestState} object
	 * @see #exitQuest(QuestType type)
	 * @see #exitQuest(QuestType type, boolean playExitQuest)
	 * @see #exitQuest(boolean repeatable, boolean playExitQuest)
	 */
	public QuestState exitQuest(boolean repeatable)
	{
		_player.removeNotifyQuestOfDeath(this);
		
		if (!isStarted())
		{
			return this;
		}
		
		// Clean registered quest items
		getQuest().removeRegisteredQuestItems(_player);
		
		Quest.deleteQuestInDb(this, repeatable);
		if (repeatable)
		{
			_player.delQuestState(getQuestName());
			_player.sendPacket(new QuestList());
		}
		else
		{
			setState(State.COMPLETED);
		}
		_vars = null;
		return this;
	}
	
	/**
	 * Finishes the quest and removes all quest items associated with this quest from the player's inventory.<br>
	 * If {@code repeatable} is set to {@code false}, also removes all other quest data associated with this quest.
	 * @param repeatable if {@code true}, deletes all data and variables of this quest, otherwise keeps them
	 * @param playExitQuest if {@code true}, plays "ItemSound.quest_finish"
	 * @return this {@link QuestState} object
	 * @see #exitQuest(QuestType type)
	 * @see #exitQuest(QuestType type, boolean playExitQuest)
	 * @see #exitQuest(boolean repeatable)
	 */
	public QuestState exitQuest(boolean repeatable, boolean playExitQuest)
	{
		exitQuest(repeatable);
		if (playExitQuest)
		{
			playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
		}
		return this;
	}
	
	public void showQuestionMark(int number)
	{
		_player.sendPacket(new TutorialShowQuestionMark(number));
	}
	
	// TODO make tutorial voices the same as quest sounds
	public void playTutorialVoice(String voice)
	{
		_player.sendPacket(new PlaySound(2, voice, 0, 0, _player.getX(), _player.getY(), _player.getZ()));
	}
	
	/**
	 * Used only in 255_Tutorial
	 * @param html
	 */
	public void showTutorialHTML(String html)
	{
		String text = HtmCache.getInstance().getHtm(_player.getHtmlPrefix(), "data/scripts/quests/255_Tutorial/" + html);
		if (text == null)
		{
			_log.warn("missing html page data/scripts/quests/255_Tutorial/" + html);
			text = "<html><body>File data/scripts/quests/255_Tutorial/" + html + " not found or file is empty.</body></html>";
		}
		_player.sendPacket(new TutorialShowHtml(text));
	}
	
	/**
	 * Used only in 255_Tutorial
	 */
	public void closeTutorialHtml()
	{
		_player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
	}
	
	/**
	 * Used only in 255_Tutorial
	 * @param number
	 */
	public void onTutorialClientEvent(int number)
	{
		_player.sendPacket(new TutorialEnableClientEvent(number));
	}
	
	/**
	 * Set the restart time for the daily quests.<br>
	 * The time is hardcoded at {@link Quest#getResetHour()} hours, {@link Quest#getResetMinutes()} minutes of the following day.<br>
	 * It can be overridden in scripts (quests).
	 */
	public void setRestartTime()
	{
		final Calendar reDo = Calendar.getInstance();
		if (reDo.get(Calendar.HOUR_OF_DAY) >= getQuest().getResetHour())
		{
			reDo.add(Calendar.DATE, 1);
		}
		reDo.set(Calendar.HOUR_OF_DAY, getQuest().getResetHour());
		reDo.set(Calendar.MINUTE, getQuest().getResetMinutes());
		set("restartTime", String.valueOf(reDo.getTimeInMillis()));
	}
	
	/**
	 * Check if a daily quest is available to be started over.
	 * @return {@code true} if the quest is available, {@code false} otherwise.
	 */
	public boolean isNowAvailable()
	{
		final String val = get("restartTime");
		return ((val == null) || !Util.isDigit(val)) || (Long.parseLong(val) <= System.currentTimeMillis());
	}
}
