package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: (ch).
 * @author -Wooden-
 */
public class ExRestartClient extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x47);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:47 ExRestartClient";
	}
}
