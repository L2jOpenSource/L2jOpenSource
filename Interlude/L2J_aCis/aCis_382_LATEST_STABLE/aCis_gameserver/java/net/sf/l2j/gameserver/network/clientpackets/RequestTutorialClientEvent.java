package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.QuestState;

public class RequestTutorialClientEvent extends L2GameClientPacket
{
	int eventId;
	
	@Override
	protected void readImpl()
	{
		eventId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		QuestState qs = player.getQuestState("Tutorial");
		if (qs != null)
			qs.getQuest().notifyEvent("CE" + eventId + "", null, player);
	}
}