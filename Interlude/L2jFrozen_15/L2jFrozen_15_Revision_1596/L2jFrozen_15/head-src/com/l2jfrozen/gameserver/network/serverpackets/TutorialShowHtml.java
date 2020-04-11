package com.l2jfrozen.gameserver.network.serverpackets;

public class TutorialShowHtml extends L2GameServerPacket
{
	private final String html;
	
	public TutorialShowHtml(final String html)
	{
		this.html = html;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xa0);
		writeS(html);
	}
	
	@Override
	public String getType()
	{
		return "[S] a0 TutorialShowHtml";
	}
	
}
