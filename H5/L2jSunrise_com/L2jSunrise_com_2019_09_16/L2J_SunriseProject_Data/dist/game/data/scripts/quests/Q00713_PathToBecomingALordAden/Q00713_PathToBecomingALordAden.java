package quests.Q00713_PathToBecomingALordAden;

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

public class Q00713_PathToBecomingALordAden extends Quest
{
	private static final int Logan = 35274;
	private static final int Orven = 30857;
	private static final int[] Orcs =
	{
		20669,
		20665
	};
	
	private static final int AdenCastle = 5;
	
	public Q00713_PathToBecomingALordAden()
	{
		super(713, Q00713_PathToBecomingALordAden.class.getSimpleName(), "Path to Becoming a Lord - Aden");
		addStartNpc(Logan);
		addTalkId(Logan);
		addTalkId(Orven);
		addKillId(Orcs);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		Castle castle = CastleManager.getInstance().getCastleById(AdenCastle);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		
		if (event.equals("logan_q713_02.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
		}
		else if (event.equals("orven_q713_03.htm"))
		{
			st.set("cond", "2");
		}
		else if (event.equals("logan_q713_05.htm"))
		{
			if (castle.getOwner().getLeader().getPlayerInstance() != null)
			{
				NpcSay packet = new NpcSay(npc.getObjectId(), Say2.NPC_SHOUT, npc.getId(), NpcStringId.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_ADEN_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_ADEN);
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
		Castle castle = CastleManager.getInstance().getCastleById(AdenCastle);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		L2PcInstance castleOwner = castle.getOwner().getLeader().getPlayerInstance();
		
		if (npcId == Logan)
		{
			if (cond == 0)
			{
				if (castleOwner == st.getPlayer())
				{
					if (!hasFort())
					{
						htmltext = "logan_q713_01.htm";
					}
					else
					{
						htmltext = "logan_q713_00.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "logan_q713_00a.htm";
					st.exitQuest(true);
				}
			}
			else if (cond == 1)
			{
				htmltext = "logan_q713_03.htm";
			}
			else if (cond == 7)
			{
				htmltext = "logan_q713_04.htm";
			}
		}
		else if (npcId == Orven)
		{
			if (cond == 1)
			{
				htmltext = "orven_q713_01.htm";
			}
			else if (cond == 2)
			{
				htmltext = "orven_q713_04.htm";
			}
			else if (cond == 4)
			{
				htmltext = "orven_q713_05.htm";
			}
			else if (cond == 5)
			{
				st.set("cond", "7");
				htmltext = "orven_q713_06.htm";
			}
			else if (cond == 7)
			{
				htmltext = "orven_q713_06.htm";
			}
		}
		
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getName());
		if ((st != null) && (st.getInt("cond") == 4))
		{
			if (st.getInt("mobs") < 100)
			{
				st.set("mobs", String.valueOf(st.getInt("mobs") + 1));
			}
			else
			{
				st.set("cond", "5");
			}
		}
		return null;
	}
	
	private boolean hasFort()
	{
		for (Fort fortress : FortManager.getInstance().getForts())
		{
			if (fortress.getCastleId() == AdenCastle)
			{
				return true;
			}
		}
		return false;
	}
}