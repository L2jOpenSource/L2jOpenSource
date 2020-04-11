package quests.Q00708_PathToBecomingALordGludio;

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
import l2r.util.Rnd;

public class Q00708_PathToBecomingALordGludio extends Quest
{
	private static final int Sayres = 35100;
	private static final int Pinter = 30298;
	private static final int Bathis = 30332;
	private static final int HeadlessKnight = 20280;
	
	private static final int HeadlessKnightsArmor = 13848;
	
	private static final int[] Mobs =
	{
		20045,
		20051,
		20099,
		HeadlessKnight
	};
	
	private static final int GludioCastle = 1;
	
	public Q00708_PathToBecomingALordGludio()
	{
		super(708, Q00708_PathToBecomingALordGludio.class.getSimpleName(), "Path to Becoming a Lord - Gludio");
		addStartNpc(Sayres);
		addTalkId(Sayres);
		addTalkId(Pinter);
		addTalkId(Bathis);
		addKillId(Mobs);
		questItemIds = new int[]
		{
			HeadlessKnightsArmor
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		
		Castle castle = CastleManager.getInstance().getCastleById(GludioCastle);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		L2PcInstance castleOwner = castle.getOwner().getLeader().getPlayerInstance();
		if (event.equals("sayres_q708_03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
		}
		else if (event.equals("sayres_q708_05.htm"))
		{
			st.set("cond", "2");
		}
		else if (event.equals("sayres_q708_08.htm"))
		{
			if (isLordAvailable(2, st))
			{
				castleOwner.getQuestState(getName()).set("confidant", String.valueOf(st.getPlayer().getObjectId()));
				castleOwner.getQuestState(getName()).set("cond", "3");
				st.setState(State.STARTED);
			}
			else
			{
				htmltext = "sayres_q708_05a.htm";
			}
		}
		else if (event.equals("pinter_q708_03.htm"))
		{
			if (isLordAvailable(3, st))
			{
				castleOwner.getQuestState(getName()).set("cond", "4");
			}
			else
			{
				htmltext = "pinter_q708_03a.htm";
			}
		}
		else if (event.equals("bathis_q708_02.htm"))
		{
			st.set("cond", "6");
		}
		else if (event.equals("bathis_q708_05.htm"))
		{
			st.takeItems(HeadlessKnightsArmor, 1);
			st.set("cond", "8");
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.NPC_SHOUT, npc.getId(), NpcStringId.LISTEN_YOU_VILLAGERS_OUR_LIEGE_WHO_WILL_SOON_BECOME_A_LORD_HAS_DEFEATED_THE_HEADLESS_KNIGHT_YOU_CAN_NOW_REST_EASY));
		}
		else if (event.equals("pinter_q708_05.htm"))
		{
			if (isLordAvailable(8, st))
			{
				st.takeItems(1867, 100);
				st.takeItems(1865, 100);
				st.takeItems(1869, 100);
				st.takeItems(1879, 50);
				castleOwner.getQuestState(getName()).set("cond", "9");
			}
			else
			{
				htmltext = "pinter_q708_03a.htm";
			}
		}
		else if (event.equals("sayres_q708_12.htm"))
		{
			if (castleOwner != null)
			{
				NpcSay packet = new NpcSay(npc.getObjectId(), Say2.NPC_SHOUT, npc.getId(), NpcStringId.S1_HAS_BECOME_LORD_OF_THE_TOWN_OF_GLUDIO_LONG_MAY_HE_REIGN);
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
		int cond = st.getInt("cond");
		Castle castle = CastleManager.getInstance().getCastleById(GludioCastle);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		L2PcInstance castleOwner = castle.getOwner().getLeader().getPlayerInstance();
		
		if (npcId == Sayres)
		{
			if (cond == 0)
			{
				if (castleOwner == st.getPlayer())
				{
					if (!hasFort())
					{
						htmltext = "sayres_q708_01.htm";
					}
					else
					{
						htmltext = "sayres_q708_00.htm";
						st.exitQuest(true);
					}
				}
				else if (isLordAvailable(2, st))
				{
					if (castleOwner.getDistanceSq(npc) <= 200)
					{
						htmltext = "sayres_q708_07.htm";
					}
					else
					{
						htmltext = "sayres_q708_05a.htm";
					}
				}
				else if (st.getState() == State.STARTED)
				{
					htmltext = "sayres_q708_08a.htm";
				}
				else
				{
					htmltext = "sayres_q708_00a.htm";
					st.exitQuest(true);
				}
			}
			else if (cond == 1)
			{
				htmltext = "sayres_q708_04.htm";
			}
			else if (cond == 2)
			{
				htmltext = "sayres_q708_06.htm";
			}
			else if (cond == 4)
			{
				st.set("cond", "5");
				htmltext = "sayres_q708_09.htm";
			}
			else if (cond == 5)
			{
				htmltext = "sayres_q708_10.htm";
			}
			else if ((cond > 5) && (cond < 9))
			{
				htmltext = "sayres_q708_08.htm";
			}
			else if (cond == 9)
			{
				htmltext = "sayres_q708_11.htm";
			}
			
		}
		else if (npcId == Pinter)
		{
			if ((st.getState() == State.STARTED) && (cond == 0) && isLordAvailable(3, st))
			{
				if (castleOwner.getQuestState(getName()).getInt("confidant") == st.getPlayer().getObjectId())
				{
					htmltext = "pinter_q708_01.htm";
				}
			}
			else if ((st.getState() == State.STARTED) && (cond == 0) && isLordAvailable(8, st))
			{
				if ((st.getQuestItemsCount(1867) >= 100) && (st.getQuestItemsCount(1865) >= 100) && (st.getQuestItemsCount(1869) >= 100) && (st.getQuestItemsCount(1879) >= 50))
				{
					htmltext = "pinter_q708_04.htm";
				}
				else
				{
					htmltext = "pinter_q708_04a.htm";
				}
			}
			else if ((st.getState() == State.STARTED) && (cond == 0) && isLordAvailable(9, st))
			{
				htmltext = "pinter_q708_06.htm";
			}
			
		}
		else if (npcId == Bathis)
		{
			if (cond == 5)
			{
				htmltext = "bathis_q708_01.htm";
			}
			else if (cond == 6)
			{
				htmltext = "bathis_q708_03.htm";
			}
			else if (cond == 7)
			{
				htmltext = "bathis_q708_04.htm";
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
		
		if ((st != null) && (st.getInt("cond") == 6))
		{
			if ((npc.getId() != HeadlessKnight) && Rnd.chance(10))
			{
				st.addSpawn(HeadlessKnight, npc, 300000);
			}
			else if (npc.getId() == HeadlessKnight)
			{
				st.giveItems(HeadlessKnightsArmor, 1);
				st.set("cond", "7");
			}
		}
		return null;
	}
	
	private boolean isLordAvailable(int cond, QuestState st)
	{
		Castle castle = CastleManager.getInstance().getCastleById(GludioCastle);
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
			if (fortress.getCastleId() == GludioCastle)
			{
				return true;
			}
		}
		return false;
	}
}