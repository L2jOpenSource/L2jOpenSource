package com.l2jfrozen.gameserver.model;

public class FishData
{
	private final int id;
	private final int level;
	private final String name;
	private final int hp;
	private final int hpRegen;
	private int type;
	private final int group;
	private final int fishGuts;
	private final int gutsCheckTime;
	private final int waitTime;
	private final int combatTime;
	
	public FishData(final int id, final int lvl, final String name, final int HP, final int HpRegen, final int type, final int group, final int fish_guts, final int guts_check_time, final int wait_time, final int combat_time)
	{
		this.id = id;
		level = lvl;
		this.name = name.intern();
		hp = HP;
		hpRegen = HpRegen;
		this.type = type;
		this.group = group;
		fishGuts = fish_guts;
		gutsCheckTime = guts_check_time;
		waitTime = wait_time;
		combatTime = combat_time;
	}
	
	public FishData(final FishData copyOf)
	{
		id = copyOf.getId();
		level = copyOf.getLevel();
		name = copyOf.getName();
		hp = copyOf.getHP();
		hpRegen = copyOf.getHpRegen();
		type = copyOf.getType();
		group = copyOf.getGroup();
		fishGuts = copyOf.getFishGuts();
		gutsCheckTime = copyOf.getGutsCheckTime();
		waitTime = copyOf.getWaitTime();
		combatTime = copyOf.getCombatTime();
	}
	
	/**
	 * @return Returns the id.
	 */
	public int getId()
	{
		return id;
	}
	
	/**
	 * @return Returns the level.
	 */
	public int getLevel()
	{
		return level;
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}
	
	public int getHP()
	{
		return hp;
	}
	
	public int getHpRegen()
	{
		return hpRegen;
	}
	
	public int getType()
	{
		return type;
	}
	
	public int getGroup()
	{
		return group;
	}
	
	public int getFishGuts()
	{
		return fishGuts;
	}
	
	public int getGutsCheckTime()
	{
		return gutsCheckTime;
	}
	
	public int getWaitTime()
	{
		return waitTime;
	}
	
	public int getCombatTime()
	{
		return combatTime;
	}
	
	public void setType(final int type)
	{
		this.type = type;
	}
}
