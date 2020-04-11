package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: (ch)ddd.
 */
public class ExVariationResult extends L2GameServerPacket
{
	private final int stat12;
	private final int stat34;
	private final int unk3;
	
	/**
	 * Instantiates a new ex variation result.
	 * @param unk1 the unk1
	 * @param unk2 the unk2
	 * @param unk3 the unk3
	 */
	public ExVariationResult(final int unk1, final int unk2, final int unk3)
	{
		stat12 = unk1;
		stat34 = unk2;
		this.unk3 = unk3;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x55);
		writeD(stat12);
		writeD(stat34);
		writeD(unk3);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:55 ExVariationResult";
	}
}
