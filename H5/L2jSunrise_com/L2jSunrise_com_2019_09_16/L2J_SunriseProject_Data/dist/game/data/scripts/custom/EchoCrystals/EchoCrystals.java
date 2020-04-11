package custom.EchoCrystals;

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.util.Util;

/**
 * Echo Crystals AI.
 * @author vGodFather
 */
public final class EchoCrystals extends Quest
{
	private final static int[] NPCs =
	{
		31042,
		31043
	};
	
	private static final int ADENA = 57;
	private static final int COST = 200;
	
	private static final Map<Integer, ScoreData> SCORES = new HashMap<>();
	
	private class ScoreData
	{
		private final int crystalId;
		private final String okMsg;
		private final String noAdenaMsg;
		private final String noScoreMsg;
		
		public ScoreData(int crystalId, String okMsg, String noAdenaMsg, String noScoreMsg)
		{
			super();
			this.crystalId = crystalId;
			this.okMsg = okMsg;
			this.noAdenaMsg = noAdenaMsg;
			this.noScoreMsg = noScoreMsg;
		}
		
		public int getCrystalId()
		{
			return crystalId;
		}
		
		public String getOkMsg()
		{
			return okMsg;
		}
		
		public String getNoAdenaMsg()
		{
			return noAdenaMsg;
		}
		
		public String getNoScoreMsg()
		{
			return noScoreMsg;
		}
	}
	
	public EchoCrystals()
	{
		super(-1, EchoCrystals.class.getSimpleName(), "custom");
		// Initialize Map
		SCORES.put(4410, new ScoreData(4411, "01", "02", "03"));
		SCORES.put(4409, new ScoreData(4412, "04", "05", "06"));
		SCORES.put(4408, new ScoreData(4413, "07", "08", "09"));
		SCORES.put(4420, new ScoreData(4414, "10", "11", "12"));
		SCORES.put(4421, new ScoreData(4415, "13", "14", "15"));
		SCORES.put(4419, new ScoreData(4417, "16", "05", "06"));
		SCORES.put(4418, new ScoreData(4416, "17", "05", "06"));
		
		addStartNpc(NPCs);
		addTalkId(NPCs);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		final QuestState st = getQuestState(player, true);
		if ((st != null) && Util.isDigit(event))
		{
			int score = Integer.parseInt(event);
			if (SCORES.containsKey(score))
			{
				int crystal = SCORES.get(score).getCrystalId();
				String ok = SCORES.get(score).getOkMsg();
				String noadena = SCORES.get(score).getNoAdenaMsg();
				String noscore = SCORES.get(score).getNoScoreMsg();
				
				if (!hasQuestItems(player, score))
				{
					htmltext = npc.getId() + "-" + noscore + ".htm";
				}
				else if (getQuestItemsCount(player, ADENA) < COST)
				{
					htmltext = npc.getId() + "-" + noadena + ".htm";
				}
				else
				{
					takeItems(player, ADENA, COST);
					giveItems(player, crystal, 1);
					htmltext = npc.getId() + "-" + ok + ".htm";
				}
			}
		}
		else
		{
			return htmltext;
		}
		
		return htmltext;
	}
}
