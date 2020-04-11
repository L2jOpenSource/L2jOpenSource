package com.l2jfrozen.gameserver.model.base;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.xml.ExperienceData;

/**
 * Character Sub-Class Definition.<br>
 * Used to store key information about a character's sub-class.
 * @author programmos, l2jfrozen dev
 */
public final class SubClass
{
	private PlayerClass playerClass;
	private long exp = ExperienceData.getInstance().getExpForLevel(Config.BASE_SUBCLASS_LEVEL);
	private int sp = 0;
	private int level = Config.BASE_SUBCLASS_LEVEL;
	private int classIndex = 1;
	
	public SubClass(final int classId, final long exp, final int sp, final byte level, final int classIndex)
	{
		playerClass = PlayerClass.values()[classId];
		this.exp = exp;
		this.sp = sp;
		this.level = level;
		this.classIndex = classIndex;
	}
	
	public SubClass(final int classId, final int classIndex)
	{
		// Used for defining a sub class using default values for XP, SP and player level.
		playerClass = PlayerClass.values()[classId];
		this.classIndex = classIndex;
	}
	
	public SubClass()
	{
		// Used for specifying ALL attributes of a sub class directly,
		// using the preset default values.
	}
	
	public PlayerClass getClassDefinition()
	{
		return playerClass;
	}
	
	public int getClassId()
	{
		return playerClass.ordinal();
	}
	
	public long getExp()
	{
		return exp;
	}
	
	public int getSp()
	{
		return sp;
	}
	
	public int getLevel()
	{
		return level;
	}
	
	public int getClassIndex()
	{
		return classIndex;
	}
	
	public void setClassId(final int classId)
	{
		playerClass = PlayerClass.values()[classId];
	}
	
	public void setExp(long expValue)
	{
		if (expValue > ExperienceData.getInstance().getExpForLevel(Config.MAX_SUBCLASS_LEVEL))
		{
			expValue = ExperienceData.getInstance().getExpForLevel(Config.MAX_SUBCLASS_LEVEL);
		}
		
		exp = expValue;
	}
	
	public void setSp(final int spValue)
	{
		sp = spValue;
	}
	
	public void setClassIndex(final int classIndex)
	{
		this.classIndex = classIndex;
	}
	
	public void setLevel(int levelValue)
	{
		if (levelValue > Config.MAX_SUBCLASS_LEVEL - 1)
		{
			levelValue = Config.MAX_SUBCLASS_LEVEL - 1;
		}
		else if (levelValue < Config.BASE_SUBCLASS_LEVEL)
		{
			levelValue = Config.BASE_SUBCLASS_LEVEL;
		}
		
		level = levelValue;
	}
	
	public void incLevel()
	{
		if (getLevel() == Config.MAX_SUBCLASS_LEVEL - 1)
		{
			return;
		}
		level++;
		setExp(ExperienceData.getInstance().getExpForLevel(getLevel()));
	}
	
	public void decLevel()
	{
		if (getLevel() == Config.BASE_SUBCLASS_LEVEL)
		{
			return;
		}
		level--;
		setExp(ExperienceData.getInstance().getExpForLevel(getLevel()));
	}
}
