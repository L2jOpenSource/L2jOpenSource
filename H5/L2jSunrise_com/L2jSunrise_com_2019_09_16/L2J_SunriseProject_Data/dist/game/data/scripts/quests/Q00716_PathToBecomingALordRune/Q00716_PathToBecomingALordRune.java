package quests.Q00716_PathToBecomingALordRune;

import java.util.ArrayList;
import java.util.List;

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

import quests.Q00021_HiddenTruth.Q00021_HiddenTruth;
import quests.Q00025_HidingBehindTheTruth.Q00025_HidingBehindTheTruth;

public class Q00716_PathToBecomingALordRune extends Quest
{
	private static final int Frederick = 35509;
	private static final int Agripel = 31348;
	private static final int Innocentin = 31328;
	
	private static final int RuneCastle = 8;
	private static List<Integer> Pagans = new ArrayList<>();
	
	static
	{
		for (int i = 22138; i <= 22176; i++)
		{
			Pagans.add(i);
		}
		for (int i = 22188; i <= 22195; i++)
		{
			Pagans.add(i);
		}
	}
	
	public Q00716_PathToBecomingALordRune()
	{
		super(716, Q00716_PathToBecomingALordRune.class.getSimpleName(), "Path to Becoming a Lord - Rune");
		addStartNpc(Frederick);
		addTalkId(Frederick);
		addTalkId(new int[]
		{
			Agripel,
			Innocentin
		});
		for (int i : Pagans)
		{
			addKillId(i);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		Castle castle = CastleManager.getInstance().getCastleById(RuneCastle);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		L2PcInstance castleOwner = castle.getOwner().getLeader().getPlayerInstance();
		if (event.equals("frederick_q716_03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
		}
		else if (event.equals("agripel_q716_03.htm"))
		{
			st.set("cond", "3");
		}
		else if (event.equals("frederick_q716_08.htm"))
		{
			castleOwner.getQuestState(Q00716_PathToBecomingALordRune.class.getSimpleName()).set("confidant", String.valueOf(st.getPlayer().getObjectId()));
			castleOwner.getQuestState(Q00716_PathToBecomingALordRune.class.getSimpleName()).set("cond", "5");
			st.setState(State.STARTED);
		}
		else if (event.equals("innocentin_q716_03.htm"))
		{
			if ((castleOwner != null) && (castleOwner != st.getPlayer()) && (castleOwner.getQuestState(Q00716_PathToBecomingALordRune.class.getSimpleName()) != null) && (castleOwner.getQuestState(Q00716_PathToBecomingALordRune.class.getSimpleName()).getInt("cond") == 5))
			{
				castleOwner.getQuestState(Q00716_PathToBecomingALordRune.class.getSimpleName()).set("cond", "6");
			}
		}
		else if (event.equals("agripel_q716_08.htm"))
		{
			st.set("cond", "8");
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
		Castle castle = CastleManager.getInstance().getCastleById(RuneCastle);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		L2PcInstance castleOwner = castle.getOwner().getLeader().getPlayerInstance();
		
		if (npcId == Frederick)
		{
			if (cond == 0)
			{
				if (castleOwner == st.getPlayer())
				{
					if (!hasFort())
					{
						htmltext = "frederick_q716_01.htm";
					}
					else
					{
						htmltext = "frederick_q716_00.htm";
						st.exitQuest(true);
					}
				}
				else if ((castleOwner != null) && (castleOwner != st.getPlayer()) && (castleOwner.getQuestState(Q00716_PathToBecomingALordRune.class.getSimpleName()) != null) && (castleOwner.getQuestState(Q00716_PathToBecomingALordRune.class.getSimpleName()).getInt("cond") == 4))
				{
					if (castleOwner.getDistanceSq(npc) <= 200)
					{
						htmltext = "frederick_q716_07.htm";
					}
					else
					{
						htmltext = "frederick_q716_07a.htm";
					}
				}
				else if (st.getState() == State.STARTED)
				{
					htmltext = "frederick_q716_00b.htm";
				}
				else
				{
					htmltext = "frederick_q716_00a.htm";
					st.exitQuest(true);
				}
			}
			else if (cond == 1)
			{
				QuestState hidingBehindTheTruth = st.getPlayer().getQuestState(Q00025_HidingBehindTheTruth.class.getSimpleName());
				QuestState hiddenTruth = st.getPlayer().getQuestState(Q00021_HiddenTruth.class.getSimpleName());
				if ((hidingBehindTheTruth != null) && hidingBehindTheTruth.isCompleted() && (hiddenTruth != null) && hiddenTruth.isCompleted())
				{
					st.set("cond", "2");
					htmltext = "frederick_q716_04.htm";
				}
				else
				{
					htmltext = "frederick_q716_03.htm";
				}
			}
			else if (cond == 2)
			{
				htmltext = "frederick_q716_04a.htm";
			}
			else if (cond == 3)
			{
				st.set("cond", "4");
				htmltext = "frederick_q716_05.htm";
			}
			else if (cond == 4)
			{
				htmltext = "frederick_q716_06.htm";
			}
			else if (cond == 5)
			{
				htmltext = "frederick_q716_09.htm";
			}
			else if (cond == 6)
			{
				st.set("cond", "7");
				htmltext = "frederick_q716_10.htm";
			}
			else if (cond == 7)
			{
				htmltext = "frederick_q716_11.htm";
			}
			else if (cond == 8)
			{
				if (castleOwner != null)
				{
					NpcSay packet = new NpcSay(npc.getObjectId(), Say2.NPC_SHOUT, npc.getId(), NpcStringId.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_RUNE_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_RUNE);
					packet.addStringParameter(player.getName());
					npc.broadcastPacket(packet);
					
					/**
					 * Territory terr = TerritoryWarManager.getInstance().getTerritory(castle.getId()); terr.setLordId(castleOwner.getObjectId()); terr.updateDataInDB(); terr.updateState();
					 */
					
					htmltext = "frederick_q716_12.htm";
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(true);
				}
			}
		}
		else if (npcId == Agripel)
		{
			if (cond == 2)
			{
				htmltext = "agripel_q716_01.htm";
			}
			else if (cond == 7)
			{
				if ((st.get("paganCount") != null) && (st.getInt("paganCount") >= 100))
				{
					htmltext = "agripel_q716_07.htm";
				}
				else
				{
					htmltext = "agripel_q716_04.htm";
				}
			}
			else if (cond == 8)
			{
				htmltext = "agripel_q716_09.htm";
			}
		}
		else if (npcId == Innocentin)
		{
			if ((st.getState() == State.STARTED) && (st.getInt("cond") == 0))
			{
				if ((castleOwner != null) && (castleOwner != st.getPlayer()) && (castleOwner.getQuestState(Q00716_PathToBecomingALordRune.class.getSimpleName()) != null) && (castleOwner.getQuestState(Q00716_PathToBecomingALordRune.class.getSimpleName()).getInt("cond") == 5))
				{
					if (castleOwner.getQuestState(Q00716_PathToBecomingALordRune.class.getSimpleName()).getInt("confidant") == st.getPlayer().getObjectId())
					{
						htmltext = "innocentin_q716_01.htm";
					}
					else
					{
						htmltext = "innocentin_q716_00.htm";
					}
				}
				else
				{
					htmltext = "innocentin_q716_00a.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		if (st.getState() != State.STARTED)
		{
			return null;
		}
		Castle castle = CastleManager.getInstance().getCastleById(RuneCastle);
		L2PcInstance castleOwner = castle.getOwner().getLeader().getPlayerInstance();
		if ((st.getState() == State.STARTED) && (st.getInt("cond") == 0))
		{
			if ((castleOwner != null) && (castleOwner != st.getPlayer()) && (castleOwner.getQuestState(Q00716_PathToBecomingALordRune.class.getSimpleName()) != null) && (castleOwner.getQuestState(Q00716_PathToBecomingALordRune.class.getSimpleName()).getInt("cond") == 7))
			{
				if (castleOwner.getQuestState(Q00716_PathToBecomingALordRune.class.getSimpleName()).get("paganCount") != null)
				{
					castleOwner.getQuestState(Q00716_PathToBecomingALordRune.class.getSimpleName()).set("paganCount", String.valueOf(castleOwner.getQuestState(Q00716_PathToBecomingALordRune.class.getSimpleName()).getInt("paganCount") + 1));
				}
				else
				{
					castleOwner.getQuestState(Q00716_PathToBecomingALordRune.class.getSimpleName()).set("paganCount", "1");
				}
			}
		}
		return null;
	}
	
	private boolean hasFort()
	{
		for (Fort fortress : FortManager.getInstance().getForts())
		{
			if (fortress.getCastleId() == RuneCastle)
			{
				return true;
			}
		}
		return false;
	}
}