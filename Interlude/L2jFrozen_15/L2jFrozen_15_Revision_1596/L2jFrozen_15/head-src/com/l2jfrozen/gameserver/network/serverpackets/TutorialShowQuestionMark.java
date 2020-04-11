package com.l2jfrozen.gameserver.network.serverpackets;

public class TutorialShowQuestionMark extends L2GameServerPacket
{
	private final int blink;
	
	public TutorialShowQuestionMark(final int blink)
	{
		this.blink = blink; // this influences the blinking frequancy :S
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xa1);
		writeD(blink);
		
	}
	
	@Override
	public String getType()
	{
		return "[S] a1 TutorialShowQuestionMark";
	}
	
}
