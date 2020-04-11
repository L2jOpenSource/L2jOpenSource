package com.l2jfrozen.gameserver.model;

/**
 * @author -Nemesiss-
 */
public class L2SummonItem
{
	private final int itemId;
	private final int npcId;
	private final byte type;
	
	public L2SummonItem(final int itemId, final int npcId, final byte type)
	{
		this.itemId = itemId;
		this.npcId = npcId;
		this.type = type;
	}
	
	public int getItemId()
	{
		return itemId;
	}
	
	public int getNpcId()
	{
		return npcId;
	}
	
	public byte getType()
	{
		return type;
	}
	
	public boolean isPetSummon()
	{
		return type == 1 || type == 2;
	}
}
