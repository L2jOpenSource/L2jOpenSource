package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * @author chris_00 opens the CommandChannel Information window
 */
public class ExOpenMPCC extends L2GameServerPacket
{
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x25);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:25 ExOpenMPCC";
	}
}