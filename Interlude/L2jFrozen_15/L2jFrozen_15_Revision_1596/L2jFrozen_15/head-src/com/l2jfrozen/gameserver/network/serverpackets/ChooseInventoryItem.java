package com.l2jfrozen.gameserver.network.serverpackets;

public class ChooseInventoryItem extends L2GameServerPacket
{
	private final int itemId;
	
	public ChooseInventoryItem(final int itemId)
	{
		this.itemId = itemId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x6f);
		writeD(itemId);
	}
	
	@Override
	public String getType()
	{
		return "[S] 6f ChooseInventoryItem";
	}
}
