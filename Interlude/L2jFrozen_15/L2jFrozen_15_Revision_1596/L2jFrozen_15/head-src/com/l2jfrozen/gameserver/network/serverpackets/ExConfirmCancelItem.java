package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: (ch)ddd
 */
public class ExConfirmCancelItem extends L2GameServerPacket
{
	private final int itemObjId;
	private final int price;
	
	public ExConfirmCancelItem(final int itemObjId, final int price)
	{
		this.itemObjId = itemObjId;
		this.price = price;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x56);
		writeD(0x40A97712);
		writeD(itemObjId);
		writeD(0x27);
		writeD(0x2006);
		writeQ(price);
		writeD(0x01);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:56 ExConfirmCancelItem";
	}
}
