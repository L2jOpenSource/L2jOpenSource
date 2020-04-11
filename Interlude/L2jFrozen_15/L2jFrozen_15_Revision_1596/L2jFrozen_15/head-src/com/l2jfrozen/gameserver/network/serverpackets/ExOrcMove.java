package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: (ch).
 * @author -Wooden-
 */
public class ExOrcMove extends L2GameServerPacket
{
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x44);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:44 ExOrcMove";
	}
}
