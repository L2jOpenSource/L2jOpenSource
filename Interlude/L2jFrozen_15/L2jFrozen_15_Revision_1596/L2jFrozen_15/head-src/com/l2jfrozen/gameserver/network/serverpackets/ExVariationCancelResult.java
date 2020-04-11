package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: (ch)ddd.
 */
public class ExVariationCancelResult extends L2GameServerPacket
{
	private final int closeWindow;
	private final int unk1;
	
	/**
	 * Instantiates a new ex variation cancel result.
	 * @param result the result
	 */
	public ExVariationCancelResult(final int result)
	{
		closeWindow = 1;
		unk1 = result;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x57);
		writeD(closeWindow);
		writeD(unk1);
	}
	
	/**
	 * Gets the type.
	 * @return the type
	 */
	@Override
	public String getType()
	{
		return "[S] FE:57 ExVariationCancelResult";
	}
}
