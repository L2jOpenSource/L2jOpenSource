package quests.Q10270_BirthOfTheSeed;

import l2r.gameserver.enums.audio.Sound;
import l2r.gameserver.model.L2CommandChannel;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;

public class Q10270_BirthOfTheSeed extends Quest
{
	private static final int _flenos = 32563;
	private static final int _klodekus = 25665;
	private static final int _klanikus = 25666;
	private static final int _cohemenes = 25634;
	private static final int _jinbi = 32566;
	private static final int _relrikia = 32567;
	private static final int _artius = 32559;
	
	public Q10270_BirthOfTheSeed()
	{
		super(10270, Q10270_BirthOfTheSeed.class.getSimpleName(), "BirthOfTheSeed");
		questItemIds = new int[]
		{
			13868,
			13869,
			13870
		};
		addStartNpc(_flenos);
		addTalkId(_flenos);
		addTalkId(_relrikia);
		addTalkId(_jinbi);
		addTalkId(_artius);
		addKillId(_klanikus);
		addKillId(_klodekus);
		addKillId(_cohemenes);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState qs = player.getQuestState(getName());
		if (event.equalsIgnoreCase("32563-05.htm"))
		{
			qs.setState(State.STARTED);
			qs.set("cond", "1");
			playSound(player, Sound.ITEMSOUND_QUEST_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32559-03.htm"))
		{
			qs.set("cond", "2");
			playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32559-09.htm"))
		{
			qs.set("cond", "4");
			playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32559-13.htm"))
		{
			qs.setState(State.COMPLETED);
			playSound(player, Sound.ITEMSOUND_QUEST_FINISH);
			qs.exitQuest(false);
			qs.addExpAndSp(251602, 25244);
			qs.giveAdena(41677, true);
		}
		else if (event.equalsIgnoreCase("32566-05.htm"))
		{
			if (qs.getQuestItemsCount(57) < 10000)
			{
				htmltext = "32566-04a.htm";
			}
			else
			{
				qs.takeItems(57, 10000);
				qs.set("pay", "1");
			}
		}
		else if (event.equalsIgnoreCase("32567-05.htm"))
		{
			qs.set("cond", "5");
			playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getId();
		QuestState qs = getQuestState(player, true);
		int cond = qs.getInt("cond");
		String htmltext = getNoQuestMsg(player);
		
		switch (npcId)
		{
			case _flenos:
				switch (qs.getState())
				{
					case State.CREATED:
						if (player.getLevel() < 75)
						{
							htmltext = "32563-02.htm";
						}
						else
						{
							htmltext = "32563-01.htm";
						}
						break;
						
					case State.STARTED:
						if (cond == 1)
						{
							htmltext = "32563-06.htm";
						}
						break;
						
					case State.COMPLETED:
						htmltext = "32563-03.htm";
						break;
				}
				break;
			case _artius:
				if (cond == 1)
				{
					htmltext = "32559-01.htm";
				}
				else if (cond == 2)
				{
					if ((qs.getQuestItemsCount(13868) < 1) && (qs.getQuestItemsCount(13869) < 1) && (qs.getQuestItemsCount(13870) < 1))
					{
						htmltext = "32559-04.htm";
					}
					else if ((qs.getQuestItemsCount(13868) + qs.getQuestItemsCount(13869) + qs.getQuestItemsCount(13870)) < 3)
					{
						htmltext = "32559-05.htm";
					}
					else if ((qs.getQuestItemsCount(13868) == 1) && (qs.getQuestItemsCount(13869) == 1) && (qs.getQuestItemsCount(13870) == 1))
					{
						htmltext = "32559-06.htm";
						qs.takeItems(13868, 1);
						qs.takeItems(13869, 1);
						qs.takeItems(13870, 1);
						qs.set("cond", "3");
						playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
					}
				}
				else if ((cond == 3) || (cond == 4))
				{
					htmltext = "32559-07.htm";
				}
				else if (cond == 5)
				{
					htmltext = "32559-12.htm";
				}
				if (qs.getState() == State.COMPLETED)
				{
					htmltext = "32559-02.htm";
				}
				break;
			case _jinbi:
				if (cond < 4)
				{
					htmltext = "32566-02.htm";
				}
				else if (cond == 4)
				{
					if (qs.getInt("pay") == 1)
					{
						htmltext = "32566-10.htm";
					}
					else
					{
						htmltext = "32566-04.htm";
					}
				}
				else if (cond > 4)
				{
					htmltext = "32566-12.htm";
				}
				
				break;
			case _relrikia:
				if (cond == 4)
				{
					htmltext = "32567-01.htm";
				}
				else if (cond == 5)
				{
					htmltext = "32567-07.htm";
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (npc.getId() == _klanikus)
		{
			if (player.getParty() != null)
			{
				L2Party party = player.getParty();
				if (party.getCommandChannel() != null)
				{
					L2CommandChannel cc = party.getCommandChannel();
					for (L2PcInstance partyMember : cc.getMembers())
					{
						QuestState qs = partyMember.getQuestState(getName());
						if ((qs != null) && (qs.getInt("cond") == 2))
						{
							qs.giveItems(13869, 1);
						}
					}
				}
				else
				{
					for (L2PcInstance partyMember : party.getMembers())
					{
						QuestState qs = partyMember.getQuestState(getName());
						if ((qs != null) && (qs.getInt("cond") == 2))
						{
							qs.giveItems(13869, 1);
						}
					}
				}
			}
			else
			{
				QuestState qs = player.getQuestState(getName());
				if ((qs != null) && (qs.getInt("cond") == 2))
				{
					qs.giveItems(13869, 1);
				}
			}
		}
		else if (npc.getId() == _klodekus)
		{
			if (player.getParty() != null)
			{
				L2Party party = player.getParty();
				if (party.getCommandChannel() != null)
				{
					L2CommandChannel cc = party.getCommandChannel();
					for (L2PcInstance partyMember : cc.getMembers())
					{
						QuestState qs = partyMember.getQuestState(getName());
						if ((qs != null) && (qs.getInt("cond") == 2))
						{
							qs.giveItems(13868, 1);
						}
					}
				}
				else
				{
					for (L2PcInstance partyMember : party.getMembers())
					{
						QuestState qs = partyMember.getQuestState(getName());
						if ((qs != null) && (qs.getInt("cond") == 2))
						{
							qs.giveItems(13868, 1);
						}
					}
				}
			}
			else
			{
				QuestState qs = player.getQuestState(getName());
				if ((qs != null) && (qs.getInt("cond") == 2))
				{
					qs.giveItems(13868, 1);
				}
			}
		}
		else if (npc.getId() == _cohemenes)
		{
			if (player.getParty() != null)
			{
				L2Party party = player.getParty();
				if (party.getCommandChannel() != null)
				{
					L2CommandChannel cc = party.getCommandChannel();
					for (L2PcInstance partyMember : cc.getMembers())
					{
						QuestState qs = partyMember.getQuestState(getName());
						if ((qs != null) && (qs.getInt("cond") == 2))
						{
							qs.giveItems(13870, 1);
						}
					}
				}
				else
				{
					for (L2PcInstance partyMember : party.getMembers())
					{
						QuestState qs = partyMember.getQuestState(getName());
						if ((qs != null) && (qs.getInt("cond") == 2))
						{
							qs.giveItems(13870, 1);
						}
					}
				}
			}
			else
			{
				QuestState qs = player.getQuestState(getName());
				if ((qs != null) && (qs.getInt("cond") == 2))
				{
					qs.giveItems(13870, 1);
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}
}