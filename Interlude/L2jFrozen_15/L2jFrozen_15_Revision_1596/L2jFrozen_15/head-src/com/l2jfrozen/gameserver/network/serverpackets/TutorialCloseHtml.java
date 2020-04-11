package com.l2jfrozen.gameserver.network.serverpackets;

public class TutorialCloseHtml extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0xa3);
	}
	
	@Override
	public String getType()
	{
		return "[S] a3 TutorialCloseHtml";
	}
}
