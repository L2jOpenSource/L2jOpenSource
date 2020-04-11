package quests.Q00712_PathToBecomingALordOren;

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

public class Q00712_PathToBecomingALordOren extends Quest
{
	private static final int Brasseur = 35226;
	private static final int Croop = 30676;
	private static final int Marty = 30169;
	private static final int Valleria = 30176;
	
	private static final int NebuliteOrb = 13851;
	
	private static final int[] OelMahims =
	{
		20575,
		20576
	};
	
	private static final int OrenCastle = 4;
	
	public Q00712_PathToBecomingALordOren()
	{
		super(712, Q00712_PathToBecomingALordOren.class.getSimpleName(), "Path to Becoming a Lord - Oren");
		addStartNpc(new int[]
		{
			Brasseur,
			Marty
		});
		addTalkId(Brasseur);
		addTalkId(Croop);
		addTalkId(Marty);
		addTalkId(Valleria);
		questItemIds = new int[]
		{
			NebuliteOrb
		};
		addKillId(OelMahims);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		Castle castle = CastleManager.getInstance().getCastleById(OrenCastle);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		L2PcInstance castleOwner = castle.getOwner().getLeader().getPlayerInstance();
		if (event.equals("brasseur_q712_03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
		}
		else if (event.equals("croop_q712_03.htm"))
		{
			st.set("cond", "3");
		}
		else if (event.equals("marty_q712_02.htm"))
		{
			if (isLordAvailable(3, st))
			{
				castleOwner.getQuestState(getName()).set("cond", "4");
				st.setState(State.STARTED);
			}
		}
		else if (event.equals("valleria_q712_02.htm"))
		{
			if (isLordAvailable(4, st))
			{
				castleOwner.getQuestState(getName()).set("cond", "5");
				st.exitQuest(true);
			}
		}
		else if (event.equals("croop_q712_05.htm"))
		{
			st.set("cond", "6");
		}
		else if (event.equals("croop_q712_07.htm"))
		{
			takeItems(player, NebuliteOrb, -1);
			st.set("cond", "8");
		}
		else if (event.equals("brasseur_q712_06.htm"))
		{
			if (castleOwner != null)
			{
				NpcSay packet = new NpcSay(npc.getObjectId(), Say2.NPC_SHOUT, npc.getId(), NpcStringId.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_OREN_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_OREN);
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
		Castle castle = CastleManager.getInstance().getCastleById(OrenCastle);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		L2PcInstance castleOwner = castle.getOwner().getLeader().getPlayerInstance();
		
		if (npcId == Brasseur)
		{
			if (cond == 0)
			{
				if (castleOwner == st.getPlayer())
				{
					if (!hasFort())
					{
						htmltext = "brasseur_q712_01.htm";
					}
					else
					{
						htmltext = "brasseur_q712_00.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "brasseur_q712_00a.htm";
					st.exitQuest(true);
				}
			}
			else if (cond == 1)
			{
				st.set("cond", "2");
				htmltext = "brasseur_q712_04.htm";
			}
			else if (cond == 2)
			{
				htmltext = "brasseur_q712_04.htm";
			}
			else if (cond == 8)
			{
				htmltext = "brasseur_q712_05.htm";
			}
		}
		else if (npcId == Croop)
		{
			if (cond == 2)
			{
				htmltext = "croop_q712_01.htm";
			}
			else if ((cond == 3) || (cond == 4))
			{
				htmltext = "croop_q712_03.htm";
			}
			else if (cond == 5)
			{
				htmltext = "croop_q712_04.htm";
			}
			else if (cond == 6)
			{
				htmltext = "croop_q712_05.htm";
			}
			else if (cond == 7)
			{
				htmltext = "croop_q712_06.htm";
			}
			else if (cond == 8)
			{
				htmltext = "croop_q712_08.htm";
			}
		}
		else if (npcId == Marty)
		{
			if (cond == 0)
			{
				if (isLordAvailable(3, st))
				{
					htmltext = "marty_q712_01.htm";
				}
				else
				{
					htmltext = "marty_q712_00.htm";
				}
			}
		}
		else if (npcId == Valleria)
		{
			if ((st.getState() == State.STARTED) && isLordAvailable(4, st))
			{
				htmltext = "valleria_q712_01.htm";
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
			if (st.getQuestItemsCount(NebuliteOrb) < 300)
			{
				st.giveItems(NebuliteOrb, 1);
			}
			if (st.getQuestItemsCount(NebuliteOrb) >= 300)
			{
				st.set("cond", "7");
			}
		}
		return null;
	}
	
	private boolean isLordAvailable(int cond, QuestState st)
	{
		Castle castle = CastleManager.getInstance().getCastleById(OrenCastle);
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
			if (fortress.getCastleId() == OrenCastle)
			{
				return true;
			}
		}
		return false;
	}
}
