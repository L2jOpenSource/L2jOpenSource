package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: (ch)ddddd
 */
public class ExConfirmVariationGemstone extends L2GameServerPacket
{
	private final int gemstoneObjId;
	private final int unk1;
	private final int gemstoneCount;
	private final int unk2;
	private final int unk3;
	
	public ExConfirmVariationGemstone(final int gemstoneObjId, final int count)
	{
		this.gemstoneObjId = gemstoneObjId;
		unk1 = 1;
		gemstoneCount = count;
		unk2 = 1;
		unk3 = 1;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x54);
		writeD(gemstoneObjId);
		writeD(unk1);
		writeD(gemstoneCount);
		writeD(unk2);
		writeD(unk3);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:54 ExConfirmVariationGemstone";
	}
}
