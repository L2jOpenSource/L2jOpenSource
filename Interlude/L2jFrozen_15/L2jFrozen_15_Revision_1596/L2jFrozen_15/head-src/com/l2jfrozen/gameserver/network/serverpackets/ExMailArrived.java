package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Fromat: (ch) (just a trigger)
 * @author -Wooden-
 */
public class ExMailArrived extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x2d);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:2D ExMailArrived";
	}
	
}
