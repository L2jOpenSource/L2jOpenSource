package net.sf.l2j.gameserver.scripting.scripts.custom;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.commons.lang.StringUtil;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class EchoCrystals extends Quest
{
	private static final String qn = "EchoCrystals";
	
	private static final int ADENA = 57;
	private static final int COST = 200;
	
	private static final Map<Integer, ScoreData> SCORES = new HashMap<>();
	{
		SCORES.put(4410, new ScoreData(4411, "01", "02", "03"));
		SCORES.put(4409, new ScoreData(4412, "04", "05", "06"));
		SCORES.put(4408, new ScoreData(4413, "07", "08", "09"));
		SCORES.put(4420, new ScoreData(4414, "10", "11", "12"));
		SCORES.put(4421, new ScoreData(4415, "13", "14", "15"));
		SCORES.put(4419, new ScoreData(4417, "16", "05", "06"));
		SCORES.put(4418, new ScoreData(4416, "17", "05", "06"));
	}
	
	public EchoCrystals()
	{
		super(-1, "custom");
		
		addStartNpc(31042, 31043);
		addTalkId(31042, 31043);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(qn);
		
		if (st != null && StringUtil.isDigit(event))
		{
			final int score = Integer.parseInt(event);
			
			final ScoreData sd = SCORES.get(score);
			if (sd != null)
			{
				if (st.getQuestItemsCount(score) == 0)
					htmltext = npc.getNpcId() + "-" + sd.getNoScoreMsg() + ".htm";
				else if (st.getQuestItemsCount(ADENA) < COST)
					htmltext = npc.getNpcId() + "-" + sd.getNoAdenaMsg() + ".htm";
				else
				{
					st.takeItems(ADENA, COST);
					st.giveItems(sd.getCrystalId(), 1);
					htmltext = npc.getNpcId() + "-" + sd.getOkMsg() + ".htm";
				}
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		return "1.htm";
	}
	
	private class ScoreData
	{
		private final int _crystalId;
		private final String _okMsg;
		private final String _noAdenaMsg;
		private final String _noScoreMsg;
		
		public ScoreData(int crystalId, String okMsg, String noAdenaMsg, String noScoreMsg)
		{
			_crystalId = crystalId;
			_okMsg = okMsg;
			_noAdenaMsg = noAdenaMsg;
			_noScoreMsg = noScoreMsg;
		}
		
		public int getCrystalId()
		{
			return _crystalId;
		}
		
		public String getOkMsg()
		{
			return _okMsg;
		}
		
		public String getNoAdenaMsg()
		{
			return _noAdenaMsg;
		}
		
		public String getNoScoreMsg()
		{
			return _noScoreMsg;
		}
	}
}