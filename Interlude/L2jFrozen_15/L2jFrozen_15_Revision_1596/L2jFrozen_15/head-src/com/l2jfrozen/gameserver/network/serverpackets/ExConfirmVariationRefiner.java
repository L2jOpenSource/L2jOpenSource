package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: (ch)ddddd
 */
public class ExConfirmVariationRefiner extends L2GameServerPacket
{
	private final int refinerItemObjId;
	private final int lifestoneItemId;
	private final int gemstoneItemId;
	private final int gemstoneCount;
	private final int unk2;
	
	public ExConfirmVariationRefiner(final int refinerItemObjId, final int lifeStoneId, final int gemstoneItemId, final int gemstoneCount)
	{
		this.refinerItemObjId = refinerItemObjId;
		lifestoneItemId = lifeStoneId;
		this.gemstoneItemId = gemstoneItemId;
		this.gemstoneCount = gemstoneCount;
		unk2 = 1;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x53);
		writeD(refinerItemObjId);
		writeD(lifestoneItemId);
		writeD(gemstoneItemId);
		writeD(gemstoneCount);
		writeD(unk2);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:53 ExConfirmVariationRefiner";
	}
}
