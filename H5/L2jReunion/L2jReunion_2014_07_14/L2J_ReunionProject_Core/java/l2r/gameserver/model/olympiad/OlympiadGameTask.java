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
package l2r.gameserver.model.olympiad;

import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.zone.type.L2OlympiadStadiumZone;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author DS
 */
public final class OlympiadGameTask implements Runnable
{
	protected static final Logger _log = LoggerFactory.getLogger(OlympiadGameTask.class);
	protected static final long BATTLE_PERIOD = Config.ALT_OLY_BATTLE; // 6 mins
	
	public static final int[] TELEPORT_TO_ARENA =
	{
		120,
		60,
		30,
		15,
		10,
		5,
		4,
		3,
		2,
		1,
		0
	};
	public static final int[] BATTLE_START_TIME_FIRST =
	{
		60,
		50,
		40,
		30,
		20,
		10,
		0
	};
	public static final int[] BATTLE_START_TIME_SECOND =
	{
		10,
		5,
		4,
		3,
		2,
		1,
		0
	};
	public static final int[] TELEPORT_TO_TOWN =
	{
		40,
		30,
		20,
		10,
		5,
		4,
		3,
		2,
		1,
		0
	};
	
	private final L2OlympiadStadiumZone _zone;
	private AbstractOlympiadGame _game;
	private GameState _state = GameState.IDLE;
	private boolean _needAnnounce = false;
	private int _countDown = 0;
	
	private static enum GameState
	{
		BEGIN,
		TELEPORT_TO_ARENA,
		GAME_STARTED,
		BATTLE_COUNTDOWN_FIRST,
		BATTLE_COUNTDOWN_SECOND,
		BATTLE_STARTED,
		BATTLE_IN_PROGRESS,
		GAME_STOPPED,
		TELEPORT_TO_TOWN,
		CLEANUP,
		IDLE
	}
	
	public OlympiadGameTask(L2OlympiadStadiumZone zone)
	{
		_zone = zone;
		zone.registerTask(this);
	}
	
	public final boolean isRunning()
	{
		return _state != GameState.IDLE;
	}
	
	public final boolean isGameStarted()
	{
		return (_state.ordinal() >= GameState.GAME_STARTED.ordinal()) && (_state.ordinal() <= GameState.CLEANUP.ordinal());
	}
	
	public final boolean isBattleStarted()
	{
		return _state == GameState.BATTLE_IN_PROGRESS;
	}
	
	public final boolean isBattleFinished()
	{
		return _state == GameState.TELEPORT_TO_TOWN;
	}
	
	public final boolean needAnnounce()
	{
		if (_needAnnounce)
		{
			_needAnnounce = false;
			return true;
		}
		return false;
	}
	
	public final L2OlympiadStadiumZone getZone()
	{
		return _zone;
	}
	
	public final AbstractOlympiadGame getGame()
	{
		return _game;
	}
	
	public final void attachGame(AbstractOlympiadGame game)
	{
		if ((game != null) && (_state != GameState.IDLE))
		{
			_log.warn("Attempt to overwrite non-finished game in state " + _state);
			return;
		}
		
		_game = game;
		_state = GameState.BEGIN;
		_needAnnounce = false;
		ThreadPoolManager.getInstance().executeGeneral(this);
	}
	
	@Override
	public final void run()
	{
		try
		{
			int delay = 1; // schedule next call after 1s
			switch (_state)
			{
			// Game created
				case BEGIN:
				{
					_state = GameState.TELEPORT_TO_ARENA;
					_countDown = Config.ALT_OLY_WAIT_TIME;
					break;
				}
				// Teleport to arena countdown
				case TELEPORT_TO_ARENA:
				{
					if (_countDown > 0)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S);
						sm.addInt(_countDown);
						_game.broadcastPacket(sm);
					}
					
					delay = getDelay(TELEPORT_TO_ARENA);
					if (_countDown <= 0)
					{
						_state = GameState.GAME_STARTED;
					}
					break;
				}
				// Game start, port players to arena
				case GAME_STARTED:
				{
					if (!startGame())
					{
						_state = GameState.GAME_STOPPED;
						break;
					}
					
					_state = GameState.BATTLE_COUNTDOWN_FIRST;
					_countDown = BATTLE_START_TIME_FIRST[0];
					delay = 5;
					break;
				}
				// Battle start countdown, first part (60-10)
				case BATTLE_COUNTDOWN_FIRST:
				{
					if (_countDown > 0)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_WILL_START_IN_S1_SECOND_S);
						sm.addInt(_countDown);
						_zone.broadcastPacket(sm);
					}
					
					delay = getDelay(BATTLE_START_TIME_FIRST);
					if (_countDown <= 0)
					{
						openDoors();
						
						_state = GameState.BATTLE_COUNTDOWN_SECOND;
						_countDown = BATTLE_START_TIME_SECOND[0];
						delay = getDelay(BATTLE_START_TIME_SECOND);
					}
					
					break;
				}
				// Battle start countdown, second part (10-0)
				case BATTLE_COUNTDOWN_SECOND:
				{
					if (_countDown > 0)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_WILL_START_IN_S1_SECOND_S);
						sm.addInt(_countDown);
						_zone.broadcastPacket(sm);
					}
					
					delay = getDelay(BATTLE_START_TIME_SECOND);
					if (_countDown <= 0)
					{
						_state = GameState.BATTLE_STARTED;
					}
					
					break;
				}
				// Beginning of the battle
				case BATTLE_STARTED:
				{
					_countDown = 0;
					_state = GameState.BATTLE_IN_PROGRESS; // set state first, used in zone update
					if (!startBattle())
					{
						_state = GameState.GAME_STOPPED;
					}
					
					break;
				}
				// Checks during battle
				case BATTLE_IN_PROGRESS:
				{
					_countDown += 1000;
					if (checkBattle() || (_countDown > Config.ALT_OLY_BATTLE))
					{
						_state = GameState.GAME_STOPPED;
					}
					
					break;
				}
				// End of the battle
				case GAME_STOPPED:
				{
					_state = GameState.TELEPORT_TO_TOWN;
					_countDown = TELEPORT_TO_TOWN[0];
					stopGame();
					delay = getDelay(TELEPORT_TO_TOWN);
					break;
				}
				// Teleport to town countdown
				case TELEPORT_TO_TOWN:
				{
					if (_countDown > 0)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_WILL_BE_MOVED_TO_TOWN_IN_S1_SECONDS);
						sm.addInt(_countDown);
						_game.broadcastPacket(sm);
					}
					
					delay = getDelay(TELEPORT_TO_TOWN);
					if (_countDown <= 0)
					{
						_state = GameState.CLEANUP;
					}
					
					break;
				}
				// Removals
				case CLEANUP:
				{
					cleanupGame();
					_state = GameState.IDLE;
					_game = null;
					return;
				}
			}
			ThreadPoolManager.getInstance().scheduleGeneral(this, delay * 1000);
		}
		catch (Exception e)
		{
			switch (_state)
			{
				case GAME_STOPPED:
				case TELEPORT_TO_TOWN:
				case CLEANUP:
				case IDLE:
				{
					_log.warn("Unable to return players back in town, exception: " + e.getMessage());
					_state = GameState.IDLE;
					_game = null;
					return;
				}
			}
			
			_log.warn("Exception in " + _state + ", trying to port players back: " + e.getMessage(), e);
			_state = GameState.GAME_STOPPED;
			ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
		}
	}
	
	private final int getDelay(int[] times)
	{
		int time;
		for (int i = 0; i < (times.length - 1); i++)
		{
			time = times[i];
			if (time >= _countDown)
			{
				continue;
			}
			
			final int delay = _countDown - time;
			_countDown = time;
			return delay;
		}
		// should not happens
		_countDown = -1;
		return 1;
	}
	
	/**
	 * Second stage: check for defaulted, port players to arena, announce game.
	 * @return true if no participants defaulted.
	 */
	private final boolean startGame()
	{
		try
		{
			// Checking for opponents and teleporting to arena
			if (_game.checkDefaulted())
			{
				return false;
			}
			
			_zone.closeDoors();
			if (_game.needBuffers())
			{
				_zone.spawnBuffers();
			}
			
			if (!_game.portPlayersToArena(_zone.getSpawns()))
			{
				return false;
			}
			
			_game.removals();
			_needAnnounce = true;
			OlympiadGameManager.getInstance().startBattle(); // inform manager
			return true;
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage(), e);
		}
		return false;
	}
	
	/**
	 * Third stage: open doors.
	 */
	private final void openDoors()
	{
		try
		{
			_game.resetDamage();
			_zone.openDoors();
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage(), e);
		}
	}
	
	/**
	 * Fourth stage: last checks, remove buffers, start competition itself.
	 * @return true if all participants online and ready on the stadium.
	 */
	private final boolean startBattle()
	{
		try
		{
			if (_game.needBuffers())
			{
				_zone.deleteBuffers();
			}
			
			if (_game.checkBattleStatus() && _game.makeCompetitionStart())
			{
				// game successfully started
				_game.broadcastOlympiadInfo(_zone);
				_zone.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.STARTS_THE_GAME));
				_zone.updateZoneStatusForCharactersInside();
				return true;
			}
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage(), e);
		}
		return false;
	}
	
	/**
	 * Fifth stage: battle is running, returns true if winner found.
	 * @return
	 */
	private final boolean checkBattle()
	{
		try
		{
			return _game.haveWinner();
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage(), e);
		}
		
		return true;
	}
	
	/**
	 * Sixth stage: winner's validations
	 */
	private final void stopGame()
	{
		try
		{
			_game.validateWinner(_zone);
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage(), e);
		}
		
		try
		{
			_zone.updateZoneStatusForCharactersInside();
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage(), e);
		}
		
		try
		{
			_game.cleanEffects();
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage(), e);
		}
	}
	
	/**
	 * Seventh stage: game cleanup (port players back, closing doors, etc)
	 */
	private final void cleanupGame()
	{
		try
		{
			_game.playersStatusBack();
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage(), e);
		}
		
		try
		{
			_game.portPlayersBack();
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage(), e);
		}
		
		try
		{
			_game.clearPlayers();
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage(), e);
		}
		
		try
		{
			_zone.closeDoors();
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage(), e);
		}
	}
}