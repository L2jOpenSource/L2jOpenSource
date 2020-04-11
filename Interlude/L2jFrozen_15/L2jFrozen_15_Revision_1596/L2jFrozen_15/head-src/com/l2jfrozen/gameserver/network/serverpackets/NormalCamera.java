package com.l2jfrozen.gameserver.network.serverpackets;

public class NormalCamera extends L2GameServerPacket
{
	public NormalCamera()
	{
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xc8);
	}
	
	@Override
	public String getType()
	{
		return "[S] C8 NormalCamera";
	}
}
