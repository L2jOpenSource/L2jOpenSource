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
package com.l2jserver.gameserver.model.actor.knownlist;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2WorldRegion;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.util.Util;

public class ObjectKnownList
{
	private final L2Object _activeObject;
	private volatile Map<Integer, L2Object> _knownObjects;
	
	public ObjectKnownList(L2Object activeObject)
	{
		_activeObject = activeObject;
	}
	
	public boolean addKnownObject(L2Object object)
	{
		if (object == null)
		{
			return false;
		}
		
		// Instance -1 is for GMs that can see everything on all instances
		if ((getActiveObject().getInstanceId() != -1) && (object.getInstanceId() != getActiveObject().getInstanceId()))
		{
			return false;
		}
		
		// Check if the object is an L2PcInstance in ghost mode
		if (object.isPlayer() && object.getActingPlayer().getAppearance().isGhost())
		{
			return false;
		}
		
		// Check if already know object
		if (knowsObject(object))
		{
			return false;
		}
		
		// Check if object is not inside distance to watch object
		if (!Util.checkIfInShortRadius(getDistanceToWatchObject(object), getActiveObject(), object, true))
		{
			return false;
		}
		
		return (getKnownObjects().put(object.getObjectId(), object) == null);
	}
	
	public final boolean knowsObject(L2Object object)
	{
		if (object == null)
		{
			return false;
		}
		
		return (getActiveObject() == object) || getKnownObjects().containsKey(object.getObjectId());
	}
	
	/**
	 * Remove all L2Object from _knownObjects
	 */
	public void removeAllKnownObjects()
	{
		getKnownObjects().clear();
	}
	
	public final boolean removeKnownObject(L2Object object)
	{
		return removeKnownObject(object, false);
	}
	
	protected boolean removeKnownObject(L2Object object, boolean forget)
	{
		if (object == null)
		{
			return false;
		}
		
		if (forget)
		{
			return true;
		}
		
		return getKnownObjects().remove(object.getObjectId()) != null;
	}
	
	/**
	 * Used only in Config.MOVE_BASED_KNOWNLIST and does not support guards seeing moving monsters
	 */
	public final void findObjects()
	{
		final L2WorldRegion worldRegion = getActiveObject().getWorldRegion();
		if (worldRegion == null)
		{
			return;
		}
		
		if (getActiveObject().isPlayable())
		{
			for (L2WorldRegion surroundingRegion : worldRegion.getSurroundingRegions()) // offer members of this and surrounding regions
			{
				for (L2Object object : surroundingRegion.getVisibleObjects().values())
				{
					if (object != getActiveObject())
					{
						addKnownObject(object);
						if (object instanceof L2Character)
						{
							object.getKnownList().addKnownObject(getActiveObject());
						}
					}
				}
			}
		}
		else if (getActiveObject() instanceof L2Character)
		{
			for (L2WorldRegion surroundingRegion : worldRegion.getSurroundingRegions()) // offer members of this and surrounding regions
			{
				if (surroundingRegion.isActive())
				{
					for (L2Object object : surroundingRegion.getVisiblePlayable().values())
					{
						if (object != getActiveObject())
						{
							addKnownObject(object);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Remove invisible and too far L2Object from _knowObject and if necessary from _knownPlayers of the L2Character
	 * @param fullCheck
	 */
	public void forgetObjects(boolean fullCheck)
	{
		final Iterator<L2Object> oIter = getKnownObjects().values().iterator();
		while (oIter.hasNext())
		{
			final L2Object object = oIter.next();
			if (!fullCheck && !object.isPlayable())
			{
				continue;
			}
			
			// Remove all objects invisible or too far
			if (!object.isVisible() || !Util.checkIfInShortRadius(getDistanceToForgetObject(object), getActiveObject(), object, true))
			{
				oIter.remove();
				removeKnownObject(object, true);
			}
		}
	}
	
	public L2Object getActiveObject()
	{
		return _activeObject;
	}
	
	public int getDistanceToForgetObject(L2Object object)
	{
		return 0;
	}
	
	public int getDistanceToWatchObject(L2Object object)
	{
		return 0;
	}
	
	/**
	 * @return the _knownObjects containing all L2Object known by the L2Character.
	 */
	public final Map<Integer, L2Object> getKnownObjects()
	{
		if (_knownObjects == null)
		{
			synchronized (this)
			{
				if (_knownObjects == null)
				{
					_knownObjects = new ConcurrentHashMap<>();
				}
			}
		}
		return _knownObjects;
	}
}
