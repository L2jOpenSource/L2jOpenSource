package quests.Q00711_PathToBecomingALordInnadril;

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

public class Q00711_PathToBecomingALordInnadril extends Quest
{
	private static final int Neurath = 35316;
	private static final int IasonHeine = 30969;
	
	private static final int InnadrilCastle = 6;
	private static final int[] mobs =
	{
		20789,
		20790,
		20791,
		20792,
		20793,
		20804,
		20805,
		20806,
		20807,
		20808
	};
	
	public Q00711_PathToBecomingALordInnadril()
	{
		super(711, Q00711_PathToBecomingALordInnadril.class.getSimpleName(), "Path to Becoming a Lord - Innadril");
		addStartNpc(Neurath);
		addTalkId(Neurath);
		addTalkId(IasonHeine);
		addKillId(mobs);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		Castle castle = CastleManager.getInstance().getCastleById(InnadrilCastle);
		L2PcInstance castleOwner = castle.getOwner().getLeader().getPlayerInstance();
		if (event.equals("neurath_q711_03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
		}
		else if (event.equals("neurath_q711_05.htm"))
		{
			st.set("cond", "2");
		}
		else if (event.equals("neurath_q711_08.htm"))
		{
			if (isLordAvailable(2, st))
			{
				castleOwner.getQuestState(getName()).set("confidant", String.valueOf(st.getPlayer().getObjectId()));
				castleOwner.getQuestState(getName()).set("cond", "3");
				st.setState(State.STARTED);
			}
			else
			{
				htmltext = "neurath_q711_07a.htm";
			}
			
		}
		else if (event.equals("heine_q711_03.htm"))
		{
			if (isLordAvailable(3, st))
			{
				castleOwner.getQuestState(getName()).set("cond", "4");
			}
			else
			{
				htmltext = "heine_q711_00a.htm";
			}
		}
		else if (event.equals("neurath_q711_12.htm"))
		{
			if (castleOwner != null)
			{
				NpcSay packet = new NpcSay(npc.getObjectId(), Say2.NPC_SHOUT, npc.getId(), NpcStringId.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_INNADRIL_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_INNADRIL);
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
		Castle castle = CastleManager.getInstance().getCastleById(InnadrilCastle);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		L2PcInstance castleOwner = castle.getOwner().getLeader().getPlayerInstance();
		
		if (npcId == Neurath)
		{
			if (cond == 0)
			{
				if (castleOwner == st.getPlayer())
				{
					if (!hasFort())
					{
						htmltext = "neurath_q711_01.htm";
					}
					else
					{
						htmltext = "neurath_q711_00.htm";
						st.exitQuest(true);
					}
				}
				else if (isLordAvailable(2, st))
				{
					if (castleOwner.getDistanceSq(npc) <= 200)
					{
						htmltext = "neurath_q711_07.htm";
					}
					else
					{
						htmltext = "neurath_q711_07a.htm";
					}
				}
				else if (st.getState() == State.STARTED)
				{
					htmltext = "neurath_q711_00b.htm";
				}
				else
				{
					htmltext = "neurath_q711_00a.htm";
					st.exitQuest(true);
				}
			}
			else if (cond == 1)
			{
				htmltext = "neurath_q711_04.htm";
			}
			else if (cond == 2)
			{
				htmltext = "neurath_q711_06.htm";
			}
			else if (cond == 3)
			{
				htmltext = "neurath_q711_09.htm";
			}
			else if (cond == 4)
			{
				st.set("cond", "5");
				htmltext = "neurath_q711_10.htm";
			}
			else if (cond == 5)
			{
				htmltext = "neurath_q711_10.htm";
			}
			else if (cond == 6)
			{
				htmltext = "neurath_q711_11.htm";
			}
		}
		else if (npcId == IasonHeine)
		{
			if ((st.getState() == State.STARTED) && (cond == 0))
			{
				if (isLordAvailable(3, st))
				{
					if (castleOwner.getQuestState(getName()).getInt("confidant") == st.getPlayer().getObjectId())
					{
						htmltext = "heine_q711_01.htm";
					}
					else
					{
						htmltext = "heine_q711_00.htm";
					}
				}
				else if (isLordAvailable(4, st))
				{
					if (castleOwner.getQuestState(getName()).getInt("confidant") == st.getPlayer().getObjectId())
					{
						htmltext = "heine_q711_03.htm";
					}
					else
					{
						htmltext = "heine_q711_00.htm";
					}
				}
				else
				{
					htmltext = "heine_q711_00a.htm";
				}
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
			if (st.getInt("mobs") < 99)
			{
				st.set("mobs", String.valueOf(st.getInt("mobs") + 1));
			}
			else
			{
				st.set("cond", "6");
			}
		}
		return null;
	}
	
	private boolean isLordAvailable(int cond, QuestState st)
	{
		Castle castle = CastleManager.getInstance().getCastleById(InnadrilCastle);
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
			if (fortress.getCastleId() == InnadrilCastle)
			{
				return true;
			}
		}
		return false;
	}
}