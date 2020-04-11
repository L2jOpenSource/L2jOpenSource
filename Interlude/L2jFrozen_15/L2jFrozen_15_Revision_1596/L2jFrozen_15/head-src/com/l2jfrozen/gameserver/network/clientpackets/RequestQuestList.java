package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.network.serverpackets.QuestList;

public final class RequestQuestList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final QuestList ql = new QuestList();
		sendPacket(ql);
	}
	
	@Override
	public String getType()
	{
		return "[C] 63 RequestQuestList";
	}
}
