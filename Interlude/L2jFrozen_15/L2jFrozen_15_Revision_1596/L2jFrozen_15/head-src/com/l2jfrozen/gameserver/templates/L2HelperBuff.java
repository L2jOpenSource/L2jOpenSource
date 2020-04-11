package com.l2jfrozen.gameserver.templates;

/**
 * This class represents a Newbie Helper Buff Author: Ayor
 */

public class L2HelperBuff
{
	/** Min level that the player must achieve to obtain this buff from Newbie Helper */
	private int lowerLevel;
	
	/** Max level that the player mustn't exceed if it want to obtain this buff from Newbie Helper */
	private int upperLevel;
	
	/** Identifier of the skill (buff) that the Newbie Helper must cast */
	private int skillID;
	
	/** Level of the skill (buff) that the Newbie Helper must cast */
	private int skillLevel;
	
	/**
	 * If True only Magus class will obtain this Buff <BR>
	 * If False only Fighter class will obtain this Buff
	 */
	private boolean isMagicClass;
	
	/**
	 * Constructor of L2HelperBuff.<BR>
	 * <BR>
	 * @param set
	 */
	public L2HelperBuff(final StatsSet set)
	{
		
		lowerLevel = set.getInteger("lowerLevel");
		upperLevel = set.getInteger("upperLevel");
		skillID = set.getInteger("skillID");
		skillLevel = set.getInteger("skillLevel");
		
		if ("false".equals(set.getString("isMagicClass")))
		{
			isMagicClass = false;
		}
		else
		{
			isMagicClass = true;
		}
		
	}
	
	/**
	 * Returns the lower level that the L2PcInstance must achieve in order to obtain this buff
	 * @return int
	 */
	public int getLowerLevel()
	{
		return lowerLevel;
	}
	
	/**
	 * Sets the lower level that the L2PcInstance must achieve in order to obtain this buff
	 * @param lowerLevel
	 */
	public void setLowerLevel(final int lowerLevel)
	{
		this.lowerLevel = lowerLevel;
	}
	
	/**
	 * Returns the upper level that the L2PcInstance mustn't exceed in order to obtain this buff
	 * @return int
	 */
	public int getUpperLevel()
	{
		return upperLevel;
	}
	
	/**
	 * Sets the upper level that the L2PcInstance mustn't exceed in order to obtain this buff
	 * @param upperLevel
	 */
	public void setUpperLevel(final int upperLevel)
	{
		this.upperLevel = upperLevel;
	}
	
	/**
	 * Returns the ID of the buff that the L2PcInstance will receive
	 * @return int
	 */
	public int getSkillID()
	{
		return skillID;
	}
	
	/**
	 * Sets the ID of the buff that the L2PcInstance will receive
	 * @param skillID
	 */
	public void setSkillID(final int skillID)
	{
		this.skillID = skillID;
	}
	
	/**
	 * Returns the Level of the buff that the L2PcInstance will receive
	 * @return int
	 */
	public int getSkillLevel()
	{
		return skillLevel;
	}
	
	/**
	 * Sets the Level of the buff that the L2PcInstance will receive
	 * @param skillLevel
	 */
	public void setSkillLevel(final int skillLevel)
	{
		this.skillLevel = skillLevel;
	}
	
	/**
	 * Returns if this Buff can be cast on a fighter or a mystic
	 * @return boolean : False if it's a fighter class Buff
	 */
	public boolean isMagicClassBuff()
	{
		return isMagicClass;
	}
	
	/**
	 * Sets if this Buff can be cast on a fighter or a mystic
	 * @param isMagicClass
	 */
	public void setIsMagicClass(final boolean isMagicClass)
	{
		this.isMagicClass = isMagicClass;
	}
	
}
