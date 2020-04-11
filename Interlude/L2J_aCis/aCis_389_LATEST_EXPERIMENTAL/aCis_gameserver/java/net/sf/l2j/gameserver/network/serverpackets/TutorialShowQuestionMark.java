package net.sf.l2j.gameserver.network.serverpackets;

public final class TutorialShowQuestionMark extends L2GameServerPacket
{
	private final int _markId;
	
	public TutorialShowQuestionMark(int blink)
	{
		_markId = blink;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xa1);
		writeD(_markId);
	}
}