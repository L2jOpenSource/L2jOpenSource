package com.l2jfrozen.gameserver.model;

/**
 * This class ...
 * @version $Revision: 1.2.4.2 $ $Date: 2005/03/27 15:29:33 $
 */
public final class L2PledgeSkillLearn
{
	// these two build the primary key
	private final int id;
	private final int level;
	
	// not needed, just for easier debug
	private final String name;
	
	private final int repCost;
	private final int baseLvl;
	private final int itemId;
	
	public L2PledgeSkillLearn(final int id, final int lvl, final int baseLvl, final String name, final int cost, final int itemId)
	{
		this.id = id;
		level = lvl;
		this.baseLvl = baseLvl;
		this.name = name.intern();
		repCost = cost;
		this.itemId = itemId;
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
	 * @return Returns the minLevel.
	 */
	public int getBaseLevel()
	{
		return baseLvl;
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * @return Returns the spCost.
	 */
	public int getRepCost()
	{
		return repCost;
	}
	
	public int getItemId()
	{
		return itemId;
	}
}
