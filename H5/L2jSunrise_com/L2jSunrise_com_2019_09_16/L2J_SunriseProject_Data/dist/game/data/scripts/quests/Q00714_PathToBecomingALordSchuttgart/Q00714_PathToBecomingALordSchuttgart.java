package quests.Q00714_PathToBecomingALordSchuttgart;

import l2r.gameserver.enums.QuestSound;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.instancemanager.FortManager;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Castle;
import l2r.gameserver.model.entity.Fort;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.NpcSay;

import quests.Q00114_ResurrectionOfAnOldManager.Q00114_ResurrectionOfAnOldManager;
import quests.Q00120_PavelsLastResearch.Q00120_PavelsLastResearch;
import quests.Q00121_PavelTheGiant.Q00121_PavelTheGiant;

public class Q00714_PathToBecomingALordSchuttgart extends Quest
{
	private static final int August = 35555;
	private static final int Newyear = 31961;
	private static final int Yasheni = 31958;
	private static final int GolemShard = 17162;
	
	private static final int ShuttgartCastle = 9;
	
	public Q00714_PathToBecomingALordSchuttgart()
	{
		super(714, Q00714_PathToBecomingALordSchuttgart.class.getSimpleName(), "Path to Becoming a Lord - Schuttgart");
		addStartNpc(August);
		addTalkId(August);
		addTalkId(Newyear);
		addTalkId(Yasheni);
		for (int i = 22801; i < 22812; i++)
		{
			addKillId(i);
		}
		questItemIds = new int[]
		{
			GolemShard
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		Castle castle = CastleManager.getInstance().getCastleById(ShuttgartCastle);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		
		if (event.equals("august_q714_03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
		}
		else if (event.equals("august_q714_05.htm"))
		{
			st.set("cond", "2");
		}
		else if (event.equals("newyear_q714_03.htm"))
		{
			st.set("cond", "3");
		}
		else if (event.equals("yasheni_q714_02.htm"))
		{
			st.set("cond", "5");
		}
		else if (event.equals("august_q714_08.htm"))
		{
			if (castle.getOwner().getLeader().getPlayerInstance() != null)
			{
				NpcSay packet = new NpcSay(npc.getObjectId(), Say2.NPC_SHOUT, npc.getId(), NpcStringId.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_SCHUTTGART_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_SCHUTTGART);
				packet.addStringParameter(player.getName());
				npc.broadcastPacket(packet);
				
				/**
				 * Territory terr = TerritoryWarManager.getInstance().getTerritory(castle.getId()); terr.setLordId(castleOwner.getObjectId()); terr.updateDataInDB(); terr.updateState();
				 */
				
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(true);
			}
		}
		return event;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = getQuestState(player, true);
		if (st == null)
		{
			return htmltext;
		}
		
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		Castle castle = CastleManager.getInstance().getCastleById(ShuttgartCastle);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		L2PcInstance castleOwner = castle.getOwner().getLeader().getPlayerInstance();
		
		if (npcId == August)
		{
			if (cond == 0)
			{
				if (castleOwner == st.getPlayer())
				{
					if (!hasFort())
					{
						htmltext = "august_q714_01.htm";
					}
					else
					{
						htmltext = "august_q714_00.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "august_q714_00a.htm";
					st.exitQuest(true);
				}
			}
			else if (cond == 1)
			{
				htmltext = "august_q714_04.htm";
			}
			else if (cond == 2)
			{
				htmltext = "august_q714_06.htm";
			}
			else if (cond == 7)
			{
				htmltext = "august_q714_07.htm";
			}
			
		}
		else if (npcId == Newyear)
		{
			if (cond == 2)
			{
				htmltext = "newyear_q714_01.htm";
			}
			else if (cond == 3)
			{
				QuestState q1 = st.getPlayer().getQuestState(Q00114_ResurrectionOfAnOldManager.class.getSimpleName());
				QuestState q2 = st.getPlayer().getQuestState(Q00120_PavelsLastResearch.class.getSimpleName());
				QuestState q3 = st.getPlayer().getQuestState(Q00121_PavelTheGiant.class.getSimpleName());
				if ((q3 != null) && q3.isCompleted())
				{
					if ((q1 != null) && q1.isCompleted())
					{
						if ((q2 != null) && q2.isCompleted())
						{
							st.set("cond", "4");
							htmltext = "newyear_q714_04.htm";
						}
						else
						{
							htmltext = "newyear_q714_04a.htm";
						}
					}
					else
					{
						htmltext = "newyear_q714_04b.htm";
					}
				}
				else
				{
					htmltext = "newyear_q714_04c.htm";
				}
			}
		}
		else if (npcId == Yasheni)
		{
			if (cond == 4)
			{
				htmltext = "yasheni_q714_01.htm";
			}
			else if (cond == 5)
			{
				htmltext = "yasheni_q714_03.htm";
			}
			else if (cond == 6)
			{
				takeItems(player, GolemShard, -1);
				st.set("cond", "7");
				htmltext = "yasheni_q714_04.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getName());
		if ((st != null) && (st.getInt("cond") == 5))
		{
			if (st.getQuestItemsCount(GolemShard) < 300)
			{
				st.giveItems(GolemShard, 1);
			}
			if (st.getQuestItemsCount(GolemShard) >= 300)
			{
				st.set("cond", "6");
			}
		}
		return null;
	}
	
	private boolean hasFort()
	{
		for (Fort fortress : FortManager.getInstance().getForts())
		{
			if (fortress.getCastleId() == ShuttgartCastle)
			{
				return true;
			}
		}
		return false;
	}
}