package net.sf.l2j.gameserver.model;

import net.sf.l2j.commons.util.StatsSet;

import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;

/**
 * A datatype used to retain a fish information.
 */
public class Fish
{
	private final int _id;
	private final int _level;
	private final int _hp;
	private final int _hpRegen;
	private final int _type;
	private final int _group;
	private final int _guts;
	private final int _gutsCheckTime;
	private final int _waitTime;
	private final int _combatTime;
	
	public Fish(StatsSet set)
	{
		_id = set.getInteger("id");
		_level = set.getInteger("level");
		_hp = set.getInteger("hp");
		_hpRegen = set.getInteger("hpRegen");
		_type = set.getInteger("type");
		_group = set.getInteger("group");
		_guts = set.getInteger("guts");
		_gutsCheckTime = set.getInteger("gutsCheckTime");
		_waitTime = set.getInteger("waitTime");
		_combatTime = set.getInteger("combatTime");
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public int getHp()
	{
		return _hp;
	}
	
	public int getHpRegen()
	{
		return _hpRegen;
	}
	
	public int getType()
	{
		return _type;
	}
	
	public int getType(boolean isLureNight)
	{
		if (!GameTimeTaskManager.getInstance().isNight() && isLureNight)
			return -1;
		
		return _type;
	}
	
	public int getGroup()
	{
		return _group;
	}
	
	public int getGuts()
	{
		return _guts;
	}
	
	public int getGutsCheckTime()
	{
		return _gutsCheckTime;
	}
	
	public int getWaitTime()
	{
		return _waitTime;
	}
	
	public int getCombatTime()
	{
		return _combatTime;
	}
}