package l2r.gameserver.scripts.quests;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.util.Rnd;

public class Q10503_FrintezzaEmbroideredSoulCloak extends Quest
{
	// NPCs
	private static final int Olfadams = 32612;
	private static final int Frintezza = 29045;
	private static final int tezzaPet = 29047;
	
	// Item
	private static final int Frintezzasoulfragment = 21724;
	
	// Reward
	private static final int CloakofFrintezza = 21721;
	
	public Q10503_FrintezzaEmbroideredSoulCloak()
	{
		super(10503, Q10503_FrintezzaEmbroideredSoulCloak.class.getSimpleName(), "Frintezza Embroidered Soul Cloak");
		addStartNpc(Olfadams);
		addTalkId(Olfadams);
		addKillId(Frintezza);
		addKillId(tezzaPet);
		
		questItemIds = new int[]
		{
			Frintezzasoulfragment
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("32612-01.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			htmltext = "32612-01.htm";
		}
		else if (event.equalsIgnoreCase("32612-03.htm"))
		{
			if (st.getQuestItemsCount(Frintezzasoulfragment) < 20)
			{
				st.set("cond", "1");
				st.playSound("ItemSound.quest_middle");
				htmltext = "32612-error.htm";
			}
			else
			{
				st.giveItems(CloakofFrintezza, 1);
				st.takeItems(Frintezzasoulfragment, 20);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
				htmltext = "32612-reward.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (st.isCompleted())
		{
			htmltext = getAlreadyCompletedMsg(player);
		}
		else if (st.isCreated())
		{
			if (player.getLevel() < 80)
			{
				htmltext = "32612-level_error.htm";
			}
			else
			{
				htmltext = "32612-00.htm";
			}
		}
		else if (st.getInt("cond") == 2)
		{
			htmltext = "32612-02.htm";
		}
		else
		{
			htmltext = "32612-01.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, 1);
		
		if (partyMember == null)
		{
			return super.onKill(npc, player, isPet);
		}
		
		final QuestState st = player.getQuestState(getName());
		
		if (st != null)
		{
			if (st.getQuestItemsCount(Frintezzasoulfragment) <= 19)
			{
				if (st.getQuestItemsCount(Frintezzasoulfragment) == 18)
				{
					st.giveItems(Frintezzasoulfragment, Rnd.get(1, 2));
					st.playSound("ItemSound.quest_itemget");
				}
				else if (st.getQuestItemsCount(Frintezzasoulfragment) == 19)
				{
					st.giveItems(Frintezzasoulfragment, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else
				{
					st.giveItems(Frintezzasoulfragment, Rnd.get(1, 3));
					st.playSound("ItemSound.quest_itemget");
				}
				if (st.getQuestItemsCount(Frintezzasoulfragment) >= 20)
				{
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
			}
		}
		
		if (player.getParty() != null)
		{
			QuestState st2;
			for (L2PcInstance pmember : player.getParty().getMembers())
			{
				st2 = pmember.getQuestState(getName());
				
				if ((st2 != null) && (st2.getInt("cond") == 1) && (pmember.getObjectId() != partyMember.getObjectId()))
				{
					if (st2.getQuestItemsCount(Frintezzasoulfragment) <= 19)
					{
						if (st2.getQuestItemsCount(Frintezzasoulfragment) == 18)
						{
							st2.giveItems(Frintezzasoulfragment, Rnd.get(1, 2));
							st2.playSound("ItemSound.quest_itemget");
						}
						else if (st2.getQuestItemsCount(Frintezzasoulfragment) == 19)
						{
							st2.giveItems(Frintezzasoulfragment, 1);
							st2.playSound("ItemSound.quest_itemget");
						}
						else
						{
							st2.giveItems(Frintezzasoulfragment, Rnd.get(1, 3));
							st2.playSound("ItemSound.quest_itemget");
						}
						if (st2.getQuestItemsCount(Frintezzasoulfragment) >= 20)
						{
							st2.set("cond", "2");
							st2.playSound("ItemSound.quest_middle");
						}
					}
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}
}