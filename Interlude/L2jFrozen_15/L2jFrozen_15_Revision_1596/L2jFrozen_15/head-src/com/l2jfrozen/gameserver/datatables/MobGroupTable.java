package com.l2jfrozen.gameserver.datatables;

import java.util.HashMap;
import java.util.Map;

import com.l2jfrozen.gameserver.model.MobGroup;
import com.l2jfrozen.gameserver.model.actor.instance.L2ControllableMobInstance;

/**
 * @author littlecrow
 */
public class MobGroupTable
{
	private static MobGroupTable instance;
	private final Map<Integer, MobGroup> groupMap;
	
	public static final int FOLLOW_RANGE = 300;
	public static final int RANDOM_RANGE = 300;
	
	public MobGroupTable()
	{
		groupMap = new HashMap<>();
	}
	
	public static MobGroupTable getInstance()
	{
		if (instance == null)
		{
			instance = new MobGroupTable();
		}
		
		return instance;
	}
	
	public void addGroup(final int groupKey, final MobGroup group)
	{
		groupMap.put(groupKey, group);
	}
	
	public MobGroup getGroup(final int groupKey)
	{
		return groupMap.get(groupKey);
	}
	
	public int getGroupCount()
	{
		return groupMap.size();
	}
	
	public MobGroup getGroupForMob(final L2ControllableMobInstance mobInst)
	{
		for (final MobGroup mobGroup : groupMap.values())
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
		return groupMap.values().toArray(new MobGroup[getGroupCount()]);
	}
	
	public boolean removeGroup(final int groupKey)
	{
		return groupMap.remove(groupKey) != null;
	}
}
