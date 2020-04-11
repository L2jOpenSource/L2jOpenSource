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

import java.util.Collection;
import java.util.List;

import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.zone.type.L2OlympiadStadiumZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author GodKratos, DS
 */
public class OlympiadGameManager implements Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(OlympiadGameManager.class);
	
	private volatile boolean _battleStarted = false;
	private final OlympiadGameTask[] _tasks;
	
	protected OlympiadGameManager()
	{
		final Collection<L2OlympiadStadiumZone> zones = ZoneManager.getInstance().getAllZones(L2OlympiadStadiumZone.class);
		if ((zones == null) || zones.isEmpty())
		{
			throw new Error("No olympiad stadium zones defined !");
		}
		
		_tasks = new OlympiadGameTask[zones.size()];
		int i = 0;
		for (L2OlympiadStadiumZone zone : zones)
		{
			_tasks[i++] = new OlympiadGameTask(zone);
		}
		
		_log.info("Olympiad System: Loaded " + _tasks.length + " stadiums.");
	}
	
	public static final OlympiadGameManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected final boolean isBattleStarted()
	{
		return _battleStarted;
	}
	
	protected final void startBattle()
	{
		_battleStarted = true;
	}
	
	@Override
	public final void run()
	{
		if (Olympiad.getInstance().isOlympiadEnd())
		{
			return;
		}
		
		if (Olympiad.getInstance().inCompPeriod())
		{
			OlympiadGameTask task;
			AbstractOlympiadGame newGame;
			
			List<List<Integer>> readyClassed = OlympiadManager.getInstance().hasEnoughRegisteredClassed();
			boolean readyNonClassed = OlympiadManager.getInstance().hasEnoughRegisteredNonClassed();
			boolean readyTeams = OlympiadManager.getInstance().hasEnoughRegisteredTeams();
			
			if ((readyClassed != null) || readyNonClassed || readyTeams)
			{
				// set up the games queue
				for (int i = 0; i < _tasks.length; i++)
				{
					task = _tasks[i];
					synchronized (task)
					{
						if (!task.isRunning())
						{
							// Fair arena distribution
							// 0,2,4,6,8.. arenas checked for classed or teams first
							if (((readyClassed != null) || readyTeams) && ((i % 2) == 0))
							{
								// 0,4,8.. arenas checked for teams first
								if (readyTeams && ((i % 4) == 0))
								{
									newGame = OlympiadGameTeams.createGame(i, OlympiadManager.getInstance().getRegisteredTeamsBased());
									if (newGame != null)
									{
										task.attachGame(newGame);
										continue;
									}
									readyTeams = false;
								}
								// if no ready teams found check for classed
								if (readyClassed != null)
								{
									newGame = OlympiadGameClassed.createGame(i, readyClassed);
									if (newGame != null)
									{
										task.attachGame(newGame);
										continue;
									}
									readyClassed = null;
								}
							}
							// 1,3,5,7,9.. arenas used for non-classed
							// also other arenas will be used for non-classed if no classed or teams available
							if (readyNonClassed)
							{
								newGame = OlympiadGameNonClassed.createGame(i, OlympiadManager.getInstance().getRegisteredNonClassBased());
								if (newGame != null)
								{
									task.attachGame(newGame);
									continue;
								}
								readyNonClassed = false;
							}
						}
					}
					
					// stop generating games if no more participants
					if ((readyClassed == null) && !readyNonClassed && !readyTeams)
					{
						break;
					}
				}
			}
		}
		else
		{
			// not in competition period
			if (isAllTasksFinished())
			{
				OlympiadManager.getInstance().clearRegistered();
				_battleStarted = false;
				_log.info("Olympiad System: All current games finished.");
			}
		}
	}
	
	public final boolean isAllTasksFinished()
	{
		for (OlympiadGameTask task : _tasks)
		{
			if (task.isRunning())
			{
				return false;
			}
		}
		return true;
	}
	
	public final OlympiadGameTask getOlympiadTask(int id)
	{
		if ((id < 0) || (id >= _tasks.length))
		{
			return null;
		}
		
		return _tasks[id];
	}
	
	public final int getNumberOfStadiums()
	{
		return _tasks.length;
	}
	
	public final void notifyCompetitorDamage(L2PcInstance player, int damage)
	{
		if (player == null)
		{
			return;
		}
		
		final int id = player.getOlympiadGameId();
		if ((id < 0) || (id >= _tasks.length))
		{
			return;
		}
		
		final AbstractOlympiadGame game = _tasks[id].getGame();
		if (game != null)
		{
			game.addDamage(player, damage);
		}
	}
	
	private static class SingletonHolder
	{
		protected static final OlympiadGameManager _instance = new OlympiadGameManager();
	}
}
