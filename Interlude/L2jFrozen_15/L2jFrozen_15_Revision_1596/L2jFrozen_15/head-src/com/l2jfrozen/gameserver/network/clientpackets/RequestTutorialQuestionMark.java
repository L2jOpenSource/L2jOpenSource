package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.quest.QuestState;

/**
 * @author ProGramMoS
 */
public class RequestTutorialQuestionMark extends L2GameClientPacket
{
	int number = 0;
	
	@Override
	protected void readImpl()
	{
		number = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		final QuestState qs = player.getQuestState("255_Tutorial");
		if (qs != null)
		{
			qs.getQuest().notifyEvent("QM" + number + "", null, player);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 7d RequestTutorialQuestionMark";
	}
}
