package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * @author Luca Baldi
 */
public class ExShowQuestMark extends L2GameServerPacket
{
	private final int questId;
	
	public ExShowQuestMark(final int questId)
	{
		this.questId = questId;
	}
	
	@Override
	public String getType()
	{
		return null;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x1a);
		writeD(questId);
	}
}
