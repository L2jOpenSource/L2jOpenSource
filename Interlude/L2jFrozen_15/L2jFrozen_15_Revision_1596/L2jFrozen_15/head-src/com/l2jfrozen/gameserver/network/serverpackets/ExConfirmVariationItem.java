package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: (ch)ddd
 */
public class ExConfirmVariationItem extends L2GameServerPacket
{
	private final int itemObjId;
	private final int unk1;
	private final int unk2;
	
	public ExConfirmVariationItem(final int itemObjId)
	{
		this.itemObjId = itemObjId;
		unk1 = 1;
		unk2 = 1;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x52);
		writeD(itemObjId);
		writeD(unk1);
		writeD(unk2);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:52 ExConfirmVariationItem";
	}
}
