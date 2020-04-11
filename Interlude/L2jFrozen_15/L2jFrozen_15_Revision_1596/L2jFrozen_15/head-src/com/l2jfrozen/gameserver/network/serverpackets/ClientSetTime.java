package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.controllers.GameTimeController;

public class ClientSetTime extends L2GameServerPacket
{
	// public static final ClientSetTime STATIC_PACKET = new ClientSetTime(); private ClientSetTime() { }
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xEC);
		writeD(GameTimeController.getInstance().getGameTime()); // time in client minutes
		writeD(6); // constant to match the server time( this determines the speed of the client clock)
	}
	
	@Override
	public String getType()
	{
		return "[S] f2 ClientSetTime [dd]";
	}
}
