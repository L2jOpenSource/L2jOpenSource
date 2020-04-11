package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class QuestList extends L2GameServerPacket
{
	private final List<Quest> _quests;
	private final Player _player;
	
	public QuestList(Player player)
	{
		_quests = player.getAllQuests(true);
		_player = player;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x80);
		
		writeH(_quests.size());
		
		for (Quest quest : _quests)
		{
			writeD(quest.getQuestId());
			
			final QuestState qs = _player.getQuestState(quest.getName());
			if (qs == null)
			{
				writeD(0);
				continue;
			}
			
			final int flag = qs.getInt("__compltdStateFlags");
			if (flag != 0)
				writeD(flag);
			else
				writeD(qs.getInt("cond"));
		}
	}
}