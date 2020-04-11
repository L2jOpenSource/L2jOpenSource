/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.taskmanager;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.Config;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.L2WorldRegion;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.instance.L2GuardInstance;

public class KnownListUpdateTaskManager
{
	protected static final Logger _log = Logger.getLogger(KnownListUpdateTaskManager.class.getName());
	
	private static final int FULL_UPDATE_TIMER = 100;
	public static boolean updatePass = true;
	
	// Do full update every FULL_UPDATE_TIMER * KNOWNLIST_UPDATE_INTERVAL
	public static int _fullUpdateTimer = FULL_UPDATE_TIMER;
	
	protected static final Set<L2WorldRegion> FAILED_REGIONS = ConcurrentHashMap.newKeySet(1);
	
	protected KnownListUpdateTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new KnownListUpdate(), 1000, Config.KNOWNLIST_UPDATE_INTERVAL);
	}
	
	private class KnownListUpdate implements Runnable
	{
		public KnownListUpdate()
		{
		}
		
		@Override
		public void run()
		{
			try
			{
				boolean failed;
				for (L2WorldRegion regions[] : L2World.getInstance().getWorldRegions())
				{
					for (L2WorldRegion r : regions) // go through all world regions
					{
						// avoid stopping update if something went wrong in updateRegion()
						try
						{
							failed = FAILED_REGIONS.contains(r); // failed on last pass
							if (r.isActive()) // and check only if the region is active
							{
								updateRegion(r, ((_fullUpdateTimer == FULL_UPDATE_TIMER) || failed), updatePass);
							}
							if (failed)
							{
								FAILED_REGIONS.remove(r); // if all ok, remove
							}
						}
						catch (Exception e)
						{
							_log.log(Level.WARNING, "KnownListUpdateTaskManager: updateRegion(" + _fullUpdateTimer + "," + updatePass + ") failed for region " + r.getName() + ". Full update scheduled. " + e.getMessage(), e);
							FAILED_REGIONS.add(r);
						}
					}
				}
				updatePass = !updatePass;
				
				if (_fullUpdateTimer > 0)
				{
					_fullUpdateTimer--;
				}
				else
				{
					_fullUpdateTimer = FULL_UPDATE_TIMER;
				}
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "", e);
			}
		}
	}
	
	public void updateRegion(L2WorldRegion region, boolean fullUpdate, boolean forgetObjects)
	{
		Collection<L2Object> vObj = region.getVisibleObjects().values();
		for (L2Object object : vObj) // and for all members in region
		{
			if ((object == null) || !object.isVisible())
			{
				continue; // skip dying objects
			}
			
			// Some mobs need faster knownlist update
			final boolean aggro = (Config.GUARD_ATTACK_AGGRO_MOB && (object instanceof L2GuardInstance));
			
			if (forgetObjects)
			{
				object.getKnownList().forgetObjects(aggro || fullUpdate);
				continue;
			}
			for (L2WorldRegion regi : region.getSurroundingRegions())
			{
				if ((object instanceof L2Playable) || (aggro && regi.isActive()) || fullUpdate)
				{
					Collection<L2Object> inrObj = regi.getVisibleObjects().values();
					for (L2Object obj : inrObj)
					{
						if (obj != object)
						{
							object.getKnownList().addKnownObject(obj);
						}
					}
				}
				else if (object instanceof L2Character)
				{
					if (regi.isActive())
					{
						Collection<L2Playable> inrPls = regi.getVisiblePlayable().values();
						
						for (L2Object obj : inrPls)
						{
							if (obj != object)
							{
								object.getKnownList().addKnownObject(obj);
							}
						}
					}
				}
			}
		}
	}
	
	public static KnownListUpdateTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final KnownListUpdateTaskManager _instance = new KnownListUpdateTaskManager();
	}
}