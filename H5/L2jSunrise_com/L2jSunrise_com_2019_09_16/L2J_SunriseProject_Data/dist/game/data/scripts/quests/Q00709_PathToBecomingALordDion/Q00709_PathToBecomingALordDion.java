package quests.Q00709_PathToBecomingALordDion;

import l2r.gameserver.enums.QuestSound;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.instancemanager.FortManager;
import l2r.gameserver.model.L2Clan;
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
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

public class Q00709_PathToBecomingALordDion extends Quest
{
	private static final int Crosby = 35142;
	private static final int Rouke = 31418;
	private static final int Sophia = 30735;
	private static final int MandragoraRoot = 13849;
	private static final int BloodyAxeAide = 27392;
	private static final int Epaulette = 13850;
	private static final int[] OlMahums =
	{
		20208,
		20209,
		20210,
		20211,
		BloodyAxeAide
	};
	private static final int[] Manragoras =
	{
		20154,
		20155,
		20156
	};
	private static final int DionCastle = 2;
	
	public Q00709_PathToBecomingALordDion()
	{
		super(709, Q00709_PathToBecomingALordDion.class.getSimpleName(), "Path to Becoming a Lord - Dion");
		addStartNpc(Crosby);
		addTalkId(Crosby);
		addTalkId(Sophia);
		addTalkId(Rouke);
		questItemIds = new int[]
		{
			Epaulette,
			MandragoraRoot
		};
		addKillId(OlMahums);
		addKillId(Manragoras);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		Castle castle = CastleManager.getInstance().getCastleById(DionCastle);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		
		L2PcInstance castleOwner = castle.getOwner().getLeader().getPlayerInstance();
		if (event.equals("crosby_q709_03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
		}
		else if (event.equals("crosby_q709_06.htm"))
		{
			if (isLordAvailable(2, st))
			{
				castleOwner.getQuestState(getName()).set("confidant", String.valueOf(st.getPlayer().getObjectId()));
				castleOwner.getQuestState(getName()).set("cond", "3");
				st.setState(State.STARTED);
			}
			else
			{
				htmltext = "crosby_q709_05a.htm";
			}
		}
		else if (event.equals("rouke_q709_03.htm"))
		{
			if (isLordAvailable(3, st))
			{
				castleOwner.getQuestState(getName()).set("cond", "4");
			}
			else
			{
				htmltext = "crosby_q709_05a.htm";
			}
		}
		else if (event.equals("sophia_q709_02.htm"))
		{
			st.set("cond", "6");
		}
		else if (event.equals("sophia_q709_05.htm"))
		{
			st.takeItems(Epaulette, 1);
			st.set("cond", "8");
		}
		else if (event.equals("rouke_q709_05.htm"))
		{
			if (isLordAvailable(8, st))
			{
				takeItems(player, MandragoraRoot, -1);
				castleOwner.getQuestState(getName()).set("cond", "9");
			}
		}
		else if (event.equals("crosby_q709_10.htm"))
		{
			if (castleOwner != null)
			{
				NpcSay packet = new NpcSay(npc.getObjectId(), Say2.NPC_SHOUT, npc.getId(), NpcStringId.S1_HAS_BECOME_LORD_OF_THE_TOWN_OF_DION_LONG_MAY_HE_REIGN);
				packet.addStringParameter(player.getName());
				npc.broadcastPacket(packet);
				
				/**
				 * Territory terr = TerritoryWarManager.getInstance().getTerritory(castle.getId()); terr.setLordId(castleOwner.getObjectId()); terr.updateDataInDB(); terr.updateState();
				 */
				
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(true);
			}
		}
		return htmltext;
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
		// int id = st.getState();
		int cond = st.getInt("cond");
		Castle castle = CastleManager.getInstance().getCastleById(DionCastle);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		L2PcInstance castleOwner = castle.getOwner().getLeader().getPlayerInstance();
		
		if (npcId == Crosby)
		{
			if (cond == 0)
			{
				if (castleOwner == st.getPlayer())
				{
					if (!hasFort())
					{
						htmltext = "crosby_q709_01.htm";
					}
					else
					{
						htmltext = "crosby_q709_00.htm";
						st.exitQuest(true);
					}
				}
				else if (isLordAvailable(2, st))
				{
					if (castleOwner.getDistanceSq(npc) <= 200)
					{
						htmltext = "crosby_q709_05.htm";
					}
					else
					{
						htmltext = "crosby_q709_05a.htm";
					}
				}
				else
				{
					htmltext = "crosby_q709_00a.htm";
					st.exitQuest(true);
				}
			}
			else if (cond == 1)
			{
				st.set("cond", "2");
				htmltext = "crosby_q709_04.htm";
			}
			else if ((cond == 2) || (cond == 3))
			{
				htmltext = "crosby_q709_04a.htm";
			}
			else if (cond == 4)
			{
				st.set("cond", "5");
				htmltext = "crosby_q709_07.htm";
			}
			else if (cond == 5)
			{
				htmltext = "crosby_q709_07.htm";
			}
			else if ((cond > 5) && (cond < 9))
			{
				htmltext = "crosby_q709_08.htm";
			}
			else if (cond == 9)
			{
				htmltext = "crosby_q709_09.htm";
			}
			
		}
		else if (npcId == Rouke)
		{
			if ((st.getState() == State.STARTED) && (cond == 0) && isLordAvailable(3, st))
			{
				if (castleOwner.getQuestState(getName()).getInt("confidant") == st.getPlayer().getObjectId())
				{
					htmltext = "rouke_q709_01.htm";
				}
			}
			else if ((st.getState() == State.STARTED) && (cond == 0) && isLordAvailable(8, st))
			{
				if (st.getQuestItemsCount(MandragoraRoot) >= 100)
				{
					htmltext = "rouke_q709_04.htm";
				}
				else
				{
					htmltext = "rouke_q709_04a.htm";
				}
			}
			else if ((st.getState() == State.STARTED) && (cond == 0) && isLordAvailable(9, st))
			{
				htmltext = "rouke_q709_06.htm";
			}
			
		}
		else if (npcId == Sophia)
		{
			if (cond == 5)
			{
				htmltext = "sophia_q709_01.htm";
			}
			else if (cond == 6)
			{
				htmltext = "sophia_q709_03.htm";
			}
			else if (cond == 7)
			{
				htmltext = "sophia_q709_04.htm";
			}
			else if (cond == 8)
			{
				htmltext = "sophia_q709_06.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getName());
		
		if ((st != null) && (st.getInt("cond") == 6) && Util.contains(OlMahums, npc.getId()))
		{
			if ((npc.getId() != BloodyAxeAide) && Rnd.chance(10))
			{
				st.addSpawn(BloodyAxeAide, npc, 300000);
			}
			else if (npc.getId() == BloodyAxeAide)
			{
				st.giveItems(Epaulette, 1);
				st.set("cond", "7");
			}
		}
		if ((st != null) && (st.getState() == State.STARTED) && (st.getInt("cond") == 0) && isLordAvailable(8, st) && Util.contains(Manragoras, npc.getId()))
		{
			if (st.getQuestItemsCount(MandragoraRoot) < 100)
			{
				st.giveItems(MandragoraRoot, 1);
			}
		}
		return null;
	}
	
	private boolean isLordAvailable(int cond, QuestState st)
	{
		Castle castle = CastleManager.getInstance().getCastleById(DionCastle);
		L2Clan owner = castle.getOwner();
		L2PcInstance castleOwner = castle.getOwner().getLeader().getPlayerInstance();
		if (owner != null)
		{
			if ((castleOwner != null) && (castleOwner != st.getPlayer()) && (owner == st.getPlayer().getClan()) && (castleOwner.getQuestState(getName()) != null) && (castleOwner.getQuestState(getName()).getInt("cond") == cond))
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean hasFort()
	{
		for (Fort fortress : FortManager.getInstance().getForts())
		{
			if (fortress.getCastleId() == DionCastle)
			{
				return true;
			}
		}
		return false;
	}
}