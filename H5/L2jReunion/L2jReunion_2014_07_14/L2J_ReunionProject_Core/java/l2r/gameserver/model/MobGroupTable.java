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
package l2r.gameserver.model;

import java.util.Map;

import javolution.util.FastMap;
import l2r.gameserver.model.actor.instance.L2ControllableMobInstance;

/**
 * @author littlecrow
 */
public class MobGroupTable
{
	private final Map<Integer, MobGroup> _groupMap;
	
	public static final int FOLLOW_RANGE = 300;
	public static final int RANDOM_RANGE = 300;
	
	protected MobGroupTable()
	{
		_groupMap = new FastMap<>();
	}
	
	public static MobGroupTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public void addGroup(int groupKey, MobGroup group)
	{
		_groupMap.put(groupKey, group);
	}
	
	public MobGroup getGroup(int groupKey)
	{
		return _groupMap.get(groupKey);
	}
	
	public int getGroupCount()
	{
		return _groupMap.size();
	}
	
	public MobGroup getGroupForMob(L2ControllableMobInstance mobInst)
	{
		for (MobGroup mobGroup : _groupMap.values())
		{
			if (mobGroup.isGroupMember(mobInst))
			{
				return mobGroup;
			}
		}
		
		return null;
	}
	
	public MobGroup[] getGroups()
	{
		return _groupMap.values().toArray(new MobGroup[getGroupCount()]);
	}
	
	public boolean removeGroup(int groupKey)
	{
		return (_groupMap.remove(groupKey) != null);
	}
	
	private static class SingletonHolder
	{
		protected static final MobGroupTable _instance = new MobGroupTable();
	}
}