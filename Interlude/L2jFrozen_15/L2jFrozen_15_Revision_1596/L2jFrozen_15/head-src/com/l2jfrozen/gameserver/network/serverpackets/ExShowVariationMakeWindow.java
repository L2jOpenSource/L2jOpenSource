package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: ch Trigger packet.
 * @author KenM
 */
public class ExShowVariationMakeWindow extends L2GameServerPacket
{
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x50);
	}
	
	/**
	 * Gets the type.
	 * @return the type
	 */
	@Override
	public String getType()
	{
		return "[S] FE:50 ExShowVariationMakeWindow";
	}
}
