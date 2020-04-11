package com.l2jfrozen.gameserver.network.serverpackets;

public class TutorialEnableClientEvent extends L2GameServerPacket
{
	private int eventId = 0;
	
	public TutorialEnableClientEvent(final int event)
	{
		eventId = event;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xa2);
		writeD(eventId);
	}
	
	@Override
	public String getType()
	{
		return "[S] a2 TutorialEnableClientEvent";
	}
}
