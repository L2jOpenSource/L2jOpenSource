package quests.Q00710_PathToBecomingALordGiran;

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

public class Q00710_PathToBecomingALordGiran extends Quest
{
	private static final int Saul = 35184;
	private static final int Gesto = 30511;
	private static final int Felton = 30879;
	private static final int CargoBox = 32243;
	
	private static final int FreightChest = 13014;
	private static final int GestoBox = 13013;
	
	private static final int[] Mobs =
	{
		20832,
		20833,
		20835,
		21602,
		21603,
		21604,
		21605,
		21606,
		21607,
		21608,
		21609
	};
	
	private static final int GiranCastle = 3;
	
	public Q00710_PathToBecomingALordGiran()
	{
		super(710, Q00710_PathToBecomingALordGiran.class.getSimpleName(), "Path to Becoming a Lord - Giran");
		addStartNpc(Saul);
		addTalkId(Saul);
		addTalkId(Gesto);
		addTalkId(Felton);
		addTalkId(CargoBox);
		questItemIds = new int[]
		{
			FreightChest,
			GestoBox
		};
		addKillId(Mobs);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		Castle castle = CastleManager.getInstance().getCastleById(GiranCastle);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		if (event.equals("saul_q710_03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
		}
		else if (event.equals("gesto_q710_03.htm"))
		{
			st.set("cond", "3");
		}
		else if (event.equals("felton_q710_02.htm"))
		{
			st.set("cond", "4");
		}
		else if (event.equals("saul_q710_07.htm"))
		{
			if (castle.getOwner().getLeader().getPlayerInstance() != null)
			{
				NpcSay packet = new NpcSay(npc.getObjectId(), Say2.NPC_SHOUT, npc.getId(), NpcStringId.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_GIRAN_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_GIRAN);
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
		Castle castle = CastleManager.getInstance().getCastleById(GiranCastle);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		L2PcInstance castleOwner = castle.getOwner().getLeader().getPlayerInstance();
		if (npcId == Saul)
		{
			if (cond == 0)
			{
				if (castleOwner == st.getPlayer())
				{
					if (!hasFort())
					{
						htmltext = "saul_q710_01.htm";
					}
					else
					{
						htmltext = "saul_q710_00.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "saul_q710_00a.htm";
					st.exitQuest(true);
				}
			}
			else if (cond == 1)
			{
				st.set("cond", "2");
				htmltext = "saul_q710_04.htm";
			}
			else if (cond == 2)
			{
				htmltext = "saul_q710_05.htm";
			}
			else if (cond == 9)
			{
				htmltext = "saul_q710_06.htm";
			}
		}
		else if (npcId == Gesto)
		{
			if (cond == 2)
			{
				htmltext = "gesto_q710_01.htm";
			}
			else if ((cond == 3) || (cond == 4))
			{
				htmltext = "gesto_q710_04.htm";
			}
			else if (cond == 5)
			{
				takeItems(player, FreightChest, -1);
				st.set("cond", "7");
				htmltext = "gesto_q710_05.htm";
			}
			else if (cond == 7)
			{
				htmltext = "gesto_q710_06.htm";
			}
			else if (cond == 8)
			{
				takeItems(player, GestoBox, -1);
				st.set("cond", "9");
				htmltext = "gesto_q710_07.htm";
			}
			else if (cond == 9)
			{
				htmltext = "gesto_q710_07.htm";
			}
			
		}
		else if (npcId == Felton)
		{
			if (cond == 3)
			{
				htmltext = "felton_q710_01.htm";
			}
			else if (cond == 4)
			{
				htmltext = "felton_q710_03.htm";
			}
		}
		else if (npcId == CargoBox)
		{
			if (cond == 4)
			{
				st.set("cond", "5");
				st.giveItems(FreightChest, 1);
				htmltext = "box_q710_01.htm";
			}
			else if (cond == 5)
			{
				htmltext = "box_q710_02.htm";
			}
		}
		
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getName());
		
		if ((st != null) && (st.getInt("cond") == 7))
		{
			if (st.getQuestItemsCount(GestoBox) < 300)
			{
				st.giveItems(GestoBox, 1);
			}
			if (st.getQuestItemsCount(GestoBox) >= 300)
			{
				st.set("cond", "8");
			}
		}
		return null;
	}
	
	private boolean hasFort()
	{
		for (Fort fortress : FortManager.getInstance().getForts())
		{
			if (fortress.getCastleId() == GiranCastle)
			{
				return true;
			}
		}
		return false;
	}
}