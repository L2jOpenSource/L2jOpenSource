package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q122_OminousNews extends Quest
{
	private static final String qn = "Q122_OminousNews";
	
	// NPCs
	private static final int MOIRA = 31979;
	private static final int KARUDA = 32017;
	
	public Q122_OminousNews()
	{
		super(122, "Ominous News");
		
		addStartNpc(MOIRA);
		addTalkId(MOIRA, KARUDA);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31979-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32017-02.htm"))
		{
			st.rewardItems(57, 1695);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				htmltext = (player.getLevel() < 20) ? "31979-01.htm" : "31979-02.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case MOIRA:
						htmltext = "31979-03.htm";
						break;
					
					case KARUDA:
						htmltext = "32017-01.htm";
						break;
				}
				break;
			
			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
}