package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: ch Trigger packet.
 * @author KenM
 */
public class ExShowVariationCancelWindow extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x51);
	}
	
	/**
	 * Gets the type.
	 * @return the type
	 */
	@Override
	public String getType()
	{
		return "[S] FE:51 ExShowVariationCancelWindow";
	}
}
