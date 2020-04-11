package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * @author chris_00 close the CommandChannel Information window
 */
public class ExCloseMPCC extends L2GameServerPacket
{
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x26);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:26 ExCloseMPCC";
	}
	
}
