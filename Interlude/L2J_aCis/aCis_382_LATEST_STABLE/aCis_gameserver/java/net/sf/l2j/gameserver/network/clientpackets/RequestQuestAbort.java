package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.xml.ScriptData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public final class RequestQuestAbort extends L2GameClientPacket
{
	private int _questId;
	
	@Override
	protected void readImpl()
	{
		_questId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getPlayer();
		if (activeChar == null)
			return;
		
		final Quest qe = ScriptData.getInstance().getQuest(_questId);
		if (qe == null)
			return;
		
		final QuestState qs = activeChar.getQuestState(qe.getName());
		if (qs != null)
			qs.exitQuest(true);
	}
}