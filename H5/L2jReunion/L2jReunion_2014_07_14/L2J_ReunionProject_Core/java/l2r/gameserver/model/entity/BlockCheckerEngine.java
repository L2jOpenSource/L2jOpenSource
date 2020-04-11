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
package l2r.gameserver.model.entity;

import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.datatables.SpawnTable;
import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.enums.Team;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.HandysBlockCheckerManager;
import l2r.gameserver.model.ArenaParticipantsHolder;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2BlockInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.itemcontainer.PcInventory;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.ExBasicActionList;
import l2r.gameserver.network.serverpackets.ExCubeGameChangePoints;
import l2r.gameserver.network.serverpackets.ExCubeGameCloseUI;
import l2r.gameserver.network.serverpackets.ExCubeGameEnd;
import l2r.gameserver.network.serverpackets.ExCubeGameExtendedChangePoints;
import l2r.gameserver.network.serverpackets.RelationChanged;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.util.Rnd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author BiggBoss
 */
public final class BlockCheckerEngine
{
	protected static final Logger _log = LoggerFactory.getLogger(BlockCheckerEngine.class);
	// The object which holds all basic members info
	protected ArenaParticipantsHolder _holder;
	// Maps to hold player of each team and his points
	protected FastMap<L2PcInstance, Integer> _redTeamPoints = new FastMap<>();
	protected FastMap<L2PcInstance, Integer> _blueTeamPoints = new FastMap<>();
	// The initial points of the event
	protected int _redPoints = 15;
	protected int _bluePoints = 15;
	// Current used arena
	protected int _arena = -1;
	// All blocks
	protected FastList<L2Spawn> _spawns = new FastList<>();
	// Sets if the red team won the event at the end of this (used for packets)
	protected boolean _isRedWinner;
	// Time when the event starts. Used on packet sending
	protected long _startedTime;
	// The needed arena coordinates
	// Arena X: team1X, team1Y, team2X, team2Y, ArenaCenterX, ArenaCenterY
	protected static final int[][] _arenaCoordinates =
	{
		// Arena 0 - Team 1 XY, Team 2 XY - CENTER XY
		{
			-58368,
			-62745,
			-57751,
			-62131,
			-58053,
			-62417
		},
		// Arena 1 - Team 1 XY, Team 2 XY - CENTER XY
		{
			-58350,
			-63853,
			-57756,
			-63266,
			-58053,
			-63551
		},
		// Arena 2 - Team 1 XY, Team 2 XY - CENTER XY
		{
			-57194,
			-63861,
			-56580,
			-63249,
			-56886,
			-63551
		},
		// Arena 3 - Team 1 XY, Team 2 XY - CENTER XY
		{
			-57200,
			-62727,
			-56584,
			-62115,
			-56850,
			-62391
		}
	};
	// Common z coordinate
	private static final int _zCoord = -2405;
	// List of dropped items in event (for later deletion)
	protected FastList<L2ItemInstance> _drops = new FastList<>();
	// Default arena
	private static final byte DEFAULT_ARENA = -1;
	// Event is started
	protected boolean _isStarted = false;
	// Event end
	protected ScheduledFuture<?> _task;
	// Preserve from exploit reward by logging out
	protected boolean _abnormalEnd = false;
	
	public BlockCheckerEngine(ArenaParticipantsHolder holder, int arena)
	{
		_holder = holder;
		if ((arena > -1) && (arena < 4))
		{
			_arena = arena;
		}
		
		for (L2PcInstance player : holder.getRedPlayers())
		{
			_redTeamPoints.put(player, 0);
		}
		for (L2PcInstance player : holder.getBluePlayers())
		{
			_blueTeamPoints.put(player, 0);
		}
	}
	
	/**
	 * Updates the player holder before the event starts to synchronize all info
	 * @param holder
	 */
	public void updatePlayersOnStart(ArenaParticipantsHolder holder)
	{
		_holder = holder;
	}
	
	/**
	 * Returns the current holder object of this object engine
	 * @return HandysBlockCheckerManager.ArenaParticipantsHolder
	 */
	public ArenaParticipantsHolder getHolder()
	{
		return _holder;
	}
	
	/**
	 * Will return the id of the arena used by this event
	 * @return false;
	 */
	public int getArena()
	{
		return _arena;
	}
	
	/**
	 * Returns the time when the event started
	 * @return long
	 */
	public long getStarterTime()
	{
		return _startedTime;
	}
	
	/**
	 * Returns the current red team points
	 * @return int
	 */
	public int getRedPoints()
	{
		synchronized (this)
		{
			return _redPoints;
		}
	}
	
	/**
	 * Returns the current blue team points
	 * @return int
	 */
	public int getBluePoints()
	{
		synchronized (this)
		{
			return _bluePoints;
		}
	}
	
	/**
	 * Returns the player points
	 * @param player
	 * @param isRed
	 * @return int
	 */
	public int getPlayerPoints(L2PcInstance player, boolean isRed)
	{
		if (!_redTeamPoints.containsKey(player) && !_blueTeamPoints.containsKey(player))
		{
			return 0;
		}
		
		if (isRed)
		{
			return _redTeamPoints.get(player);
		}
		return _blueTeamPoints.get(player);
	}
	
	/**
	 * Increases player points for his teams
	 * @param player
	 * @param team
	 */
	public synchronized void increasePlayerPoints(L2PcInstance player, int team)
	{
		if (player == null)
		{
			return;
		}
		
		if (team == 0)
		{
			int points = _redTeamPoints.get(player) + 1;
			_redTeamPoints.put(player, points);
			_redPoints++;
			_bluePoints--;
		}
		else
		{
			int points = _blueTeamPoints.get(player) + 1;
			_blueTeamPoints.put(player, points);
			_bluePoints++;
			_redPoints--;
		}
	}
	
	/**
	 * Will add a new drop into the list of dropped items
	 * @param item
	 */
	public void addNewDrop(L2ItemInstance item)
	{
		if (item != null)
		{
			_drops.add(item);
		}
	}
	
	/**
	 * Will return true if the event is already started
	 * @return boolean
	 */
	public boolean isStarted()
	{
		return _isStarted;
	}
	
	/**
	 * Will send all packets for the event members with the relation info
	 * @param plr
	 */
	protected void broadcastRelationChanged(L2PcInstance plr)
	{
		for (L2PcInstance p : _holder.getAllPlayers())
		{
			p.sendPacket(new RelationChanged(plr, plr.getRelation(p), plr.isAutoAttackable(p)));
		}
	}
	
	/**
	 * Called when a there is an empty team. The event will end.
	 */
	public void endEventAbnormally()
	{
		try
		{
			synchronized (this)
			{
				_isStarted = false;
				
				if (_task != null)
				{
					_task.cancel(true);
				}
				
				_abnormalEnd = true;
				
				ThreadPoolManager.getInstance().executeGeneral(new EndEvent());
				
				if (Config.DEBUG)
				{
					_log.info("Handys Block Checker Event at arena " + _arena + " ended due lack of players!");
				}
			}
		}
		catch (Exception e)
		{
			_log.error("Couldnt end Block Checker event at " + _arena, e);
		}
	}
	
	/**
	 * This inner class set ups all player and arena parameters to start the event
	 */
	public class StartEvent implements Runnable
	{
		// In event used skills
		private final L2Skill _freeze, _transformationRed, _transformationBlue;
		// Common and unparametizer packet
		private final ExCubeGameCloseUI _closeUserInterface = new ExCubeGameCloseUI();
		
		public StartEvent()
		{
			// Initialize all used skills
			_freeze = SkillData.getInstance().getInfo(6034, 1);
			_transformationRed = SkillData.getInstance().getInfo(6035, 1);
			_transformationBlue = SkillData.getInstance().getInfo(6036, 1);
		}
		
		/**
		 * Will set up all player parameters and port them to their respective location based on their teams
		 */
		private void setUpPlayers()
		{
			// Set current arena as being used
			HandysBlockCheckerManager.getInstance().setArenaBeingUsed(_arena);
			
			// Initialize packets avoiding create a new one per player
			_redPoints = _spawns.size() / 2;
			_bluePoints = _spawns.size() / 2;
			final ExCubeGameChangePoints initialPoints = new ExCubeGameChangePoints(300, _bluePoints, _redPoints);
			ExCubeGameExtendedChangePoints clientSetUp;
			
			for (L2PcInstance player : _holder.getAllPlayers())
			{
				if (player == null)
				{
					continue;
				}
				
				// Send the secret client packet set up
				boolean isRed = _holder.getRedPlayers().contains(player);
				
				clientSetUp = new ExCubeGameExtendedChangePoints(300, _bluePoints, _redPoints, isRed, player, 0);
				player.sendPacket(clientSetUp);
				
				player.sendPacket(ActionFailed.STATIC_PACKET);
				
				// Teleport Player - Array access
				// Team 0 * 2 = 0; 0 = 0, 0 + 1 = 1.
				// Team 1 * 2 = 2; 2 = 2, 2 + 1 = 3
				int tc = _holder.getPlayerTeam(player) * 2;
				// Get x and y coordinates
				int x = _arenaCoordinates[_arena][tc];
				int y = _arenaCoordinates[_arena][tc + 1];
				player.teleToLocation(x, y, _zCoord);
				// Set the player team
				if (isRed)
				{
					_redTeamPoints.put(player, 0);
					player.setTeam(Team.RED);
				}
				else
				{
					_blueTeamPoints.put(player, 0);
					player.setTeam(Team.BLUE);
				}
				player.stopAllEffects();
				
				if (player.hasSummon())
				{
					player.getSummon().unSummon(player);
				}
				
				// Give the player start up effects
				// Freeze
				_freeze.getEffects(player, player);
				// Transformation
				if (_holder.getPlayerTeam(player) == 0)
				{
					_transformationRed.getEffects(player, player);
				}
				else
				{
					_transformationBlue.getEffects(player, player);
				}
				// Set the current player arena
				player.setBlockCheckerArena((byte) _arena);
				player.setInsideZone(ZoneIdType.PVP, true);
				// Send needed packets
				player.sendPacket(initialPoints);
				player.sendPacket(_closeUserInterface);
				// ExBasicActionList
				player.sendPacket(ExBasicActionList.STATIC_PACKET);
				broadcastRelationChanged(player);
			}
		}
		
		@Override
		public void run()
		{
			// Wrong arena passed, stop event
			if (_arena == -1)
			{
				_log.error("Couldnt set up the arena Id for the Block Checker event, cancelling event...");
				return;
			}
			_isStarted = true;
			// Spawn the blocks
			ThreadPoolManager.getInstance().executeGeneral(new SpawnRound(16, 1));
			// Start up player parameters
			setUpPlayers();
			// Set the started time
			_startedTime = System.currentTimeMillis() + 300000;
		}
	}
	
	/**
	 * This class spawns the second round of boxes and schedules the event end
	 */
	private class SpawnRound implements Runnable
	{
		int _numOfBoxes;
		int _round;
		
		SpawnRound(int numberOfBoxes, int round)
		{
			_numOfBoxes = numberOfBoxes;
			_round = round;
		}
		
		@Override
		public void run()
		{
			if (!_isStarted)
			{
				return;
			}
			
			switch (_round)
			{
				case 1:
					// Schedule second spawn round
					_task = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRound(20, 2), 60000);
					break;
				case 2:
					// Schedule third spawn round
					_task = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRound(14, 3), 60000);
					break;
				case 3:
					// Schedule Event End Count Down
					_task = ThreadPoolManager.getInstance().scheduleGeneral(new EndEvent(), 180000);
					break;
			}
			// random % 2, if == 0 will spawn a red block
			// if != 0, will spawn a blue block
			byte random = 2;
			// common template
			final L2NpcTemplate template = NpcTable.getInstance().getTemplate(18672);
			// Spawn blocks
			try
			{
				// Creates 50 new blocks
				for (int i = 0; i < _numOfBoxes; i++)
				{
					L2Spawn spawn = new L2Spawn(template);
					spawn.setX(_arenaCoordinates[_arena][4] + Rnd.get(-400, 400));
					spawn.setY(_arenaCoordinates[_arena][5] + Rnd.get(-400, 400));
					spawn.setZ(_zCoord);
					spawn.setAmount(1);
					spawn.setHeading(1);
					spawn.setRespawnDelay(1);
					SpawnTable.getInstance().addNewSpawn(spawn, false);
					spawn.init();
					L2BlockInstance block = (L2BlockInstance) spawn.getLastSpawn();
					// switch color
					if ((random % 2) == 0)
					{
						block.setRed(true);
					}
					else
					{
						block.setRed(false);
					}
					
					block.disableCoreAI(true);
					_spawns.add(spawn);
					random++;
				}
			}
			catch (Exception e)
			{
				_log.warn(getClass().getSimpleName() + ": " + e.getMessage());
			}
			
			// Spawn the block carrying girl
			if ((_round == 1) || (_round == 2))
			{
				L2NpcTemplate girl = NpcTable.getInstance().getTemplate(18676);
				try
				{
					final L2Spawn girlSpawn = new L2Spawn(girl);
					girlSpawn.setX(_arenaCoordinates[_arena][4] + Rnd.get(-400, 400));
					girlSpawn.setY(_arenaCoordinates[_arena][5] + Rnd.get(-400, 400));
					girlSpawn.setZ(_zCoord);
					girlSpawn.setAmount(1);
					girlSpawn.setHeading(1);
					girlSpawn.setRespawnDelay(1);
					SpawnTable.getInstance().addNewSpawn(girlSpawn, false);
					girlSpawn.init();
					// Schedule his deletion after 9 secs of spawn
					ThreadPoolManager.getInstance().scheduleGeneral(new CarryingGirlUnspawn(girlSpawn), 9000);
				}
				catch (Exception e)
				{
					_log.warn("Couldnt Spawn Block Checker NPCs! Wrong instance type at npc table?");
					_log.warn(getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
			
			_redPoints += _numOfBoxes / 2;
			_bluePoints += _numOfBoxes / 2;
			
			int timeLeft = (int) ((getStarterTime() - System.currentTimeMillis()) / 1000);
			ExCubeGameChangePoints changePoints = new ExCubeGameChangePoints(timeLeft, getBluePoints(), getRedPoints());
			getHolder().broadCastPacketToTeam(changePoints);
		}
	}
	
	private class CarryingGirlUnspawn implements Runnable
	{
		private final L2Spawn _spawn;
		
		protected CarryingGirlUnspawn(L2Spawn spawn)
		{
			_spawn = spawn;
		}
		
		@Override
		public void run()
		{
			if (_spawn == null)
			{
				_log.warn("HBCE: Block Carrying Girl is null");
				return;
			}
			SpawnTable.getInstance().deleteSpawn(_spawn, false);
			_spawn.stopRespawn();
			_spawn.getLastSpawn().deleteMe();
		}
	}
	
	/*
	 * private class CountDown implements Runnable {
	 * @Override public void run() { _holder.broadCastPacketToTeam(SystemMessage.getSystemMessage(SystemMessageId.BLOCK_CHECKER_ENDS_5)); ThreadPoolManager.getInstance().scheduleGeneral(new EndEvent(), 5000); } }
	 */
	
	/**
	 * This class erase all event parameters on player and port them back near Handy. Also, unspawn blocks, runs a garbage collector and set as free the used arena
	 */
	protected class EndEvent implements Runnable
	{
		// Garbage collector and arena free setter
		private void clearMe()
		{
			HandysBlockCheckerManager.getInstance().clearPaticipantQueueByArenaId(_arena);
			_holder.clearPlayers();
			_blueTeamPoints.clear();
			_redTeamPoints.clear();
			HandysBlockCheckerManager.getInstance().setArenaFree(_arena);
			
			for (L2Spawn spawn : _spawns)
			{
				spawn.stopRespawn();
				spawn.getLastSpawn().deleteMe();
				SpawnTable.getInstance().deleteSpawn(spawn, false);
				spawn = null;
			}
			_spawns.clear();
			
			for (L2ItemInstance item : _drops)
			{
				// npe
				if (item == null)
				{
					continue;
				}
				
				// a player has it, it will be deleted later
				if (!item.isVisible() || (item.getOwnerId() != 0))
				{
					continue;
				}
				
				item.decayMe();
				L2World.getInstance().removeObject(item);
			}
			_drops.clear();
		}
		
		/**
		 * Reward players after event. Tie - No Reward
		 */
		private void rewardPlayers()
		{
			if (_redPoints == _bluePoints)
			{
				return;
			}
			
			_isRedWinner = _redPoints > _bluePoints ? true : false;
			
			if (_isRedWinner)
			{
				rewardAsWinner(true);
				rewardAsLooser(false);
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.TEAM_C1_WON);
				msg.addString("Red Team");
				_holder.broadCastPacketToTeam(msg);
			}
			else if (_bluePoints > _redPoints)
			{
				rewardAsWinner(false);
				rewardAsLooser(true);
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.TEAM_C1_WON);
				msg.addString("Blue Team");
				_holder.broadCastPacketToTeam(msg);
			}
			else
			{
				rewardAsLooser(true);
				rewardAsLooser(false);
			}
		}
		
		/**
		 * Reward the speicifed team as a winner team 1) Higher score - 8 extra 2) Higher score - 5 extra
		 * @param isRed
		 */
		private void rewardAsWinner(boolean isRed)
		{
			FastMap<L2PcInstance, Integer> tempPoints = isRed ? _redTeamPoints : _blueTeamPoints;
			
			// Main give
			for (Entry<L2PcInstance, Integer> points : tempPoints.entrySet())
			{
				if (points.getKey() == null)
				{
					continue;
				}
				
				if (points.getValue() >= 10)
				{
					points.getKey().addItem("Block Checker", 13067, 2, points.getKey(), true);
				}
				else
				{
					tempPoints.remove(points.getKey());
				}
			}
			
			int first = 0, second = 0;
			L2PcInstance winner1 = null, winner2 = null;
			for (Entry<L2PcInstance, Integer> entry : tempPoints.entrySet())
			{
				L2PcInstance pc = entry.getKey();
				int pcPoints = entry.getValue();
				if (pcPoints > first)
				{
					// Move old data
					second = first;
					winner2 = winner1;
					// Set new data
					first = pcPoints;
					winner1 = pc;
				}
				else if (pcPoints > second)
				{
					second = pcPoints;
					winner2 = pc;
				}
			}
			if (winner1 != null)
			{
				winner1.addItem("Block Checker", 13067, 8, winner1, true);
			}
			if (winner2 != null)
			{
				winner2.addItem("Block Checker", 13067, 5, winner2, true);
			}
		}
		
		/**
		 * Will reward the looser team with the predefined rewards Player got >= 10 points: 2 coins Player got < 10 points: 0 coins
		 * @param isRed
		 */
		private void rewardAsLooser(boolean isRed)
		{
			FastMap<L2PcInstance, Integer> tempPoints = isRed ? _redTeamPoints : _blueTeamPoints;
			
			for (Entry<L2PcInstance, Integer> entry : tempPoints.entrySet())
			{
				L2PcInstance player = entry.getKey();
				if ((player != null) && (entry.getValue() >= 10))
				{
					player.addItem("Block Checker", 13067, 2, player, true);
				}
			}
		}
		
		/**
		 * Telport players back, give status back and send final packet
		 */
		private void setPlayersBack()
		{
			final ExCubeGameEnd end = new ExCubeGameEnd(_isRedWinner);
			
			for (L2PcInstance player : _holder.getAllPlayers())
			{
				if (player == null)
				{
					continue;
				}
				
				player.stopAllEffects();
				// Remove team aura
				player.setTeam(Team.NONE);
				// Set default arena
				player.setBlockCheckerArena(DEFAULT_ARENA);
				// Remove the event items
				PcInventory inv = player.getInventory();
				if (inv.getItemByItemId(13787) != null)
				{
					long count = inv.getInventoryItemCount(13787, 0);
					inv.destroyItemByItemId("Handys Block Checker", 13787, count, player, player);
				}
				if (inv.getItemByItemId(13788) != null)
				{
					long count = inv.getInventoryItemCount(13788, 0);
					inv.destroyItemByItemId("Handys Block Checker", 13788, count, player, player);
				}
				broadcastRelationChanged(player);
				// Teleport Back
				player.teleToLocation(-57478, -60367, -2370);
				player.setInsideZone(ZoneIdType.PVP, false);
				// Send end packet
				player.sendPacket(end);
				player.broadcastUserInfo();
			}
		}
		
		@Override
		public void run()
		{
			if (!_abnormalEnd)
			{
				rewardPlayers();
			}
			setPlayersBack();
			clearMe();
			_isStarted = false;
			_abnormalEnd = false;
		}
	}
}