package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * @author Gnacik
 */
public class ExClosePartyRoom extends L2GameServerPacket
{
	public ExClosePartyRoom()
	{
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:0f ExClosePartyRoom";
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x0f);
	}
}