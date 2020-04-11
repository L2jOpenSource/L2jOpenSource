package com.l2jfrozen.gameserver.model.holder;

import com.l2jfrozen.gameserver.model.L2Skill;

public class TimeStamp
{
	private L2Skill skill;
	private long reuse;
	private long stamp;
	
	public TimeStamp(L2Skill skill, long reuse)
	{
		this.skill = skill;
		this.reuse = reuse;
		stamp = System.currentTimeMillis() + reuse;
	}
	
	public TimeStamp(L2Skill skill, long reuse, long systime)
	{
		this.skill = skill;
		this.reuse = reuse;
		stamp = systime;
	}
	
	public long getStamp()
	{
		return stamp;
	}
	
	public long getReuse()
	{
		return reuse;
	}
	
	public long getRemaining()
	{
		return Math.max(stamp - System.currentTimeMillis(), 0L);
	}
	
	public boolean hasNotPassed()
	{
		return System.currentTimeMillis() < stamp;
	}
	
	public L2Skill getSkill()
	{
		return skill;
	}
}