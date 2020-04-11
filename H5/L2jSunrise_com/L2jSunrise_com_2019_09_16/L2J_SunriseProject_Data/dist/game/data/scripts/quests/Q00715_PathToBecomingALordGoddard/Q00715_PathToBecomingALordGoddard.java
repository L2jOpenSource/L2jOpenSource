package quests.Q00715_PathToBecomingALordGoddard;

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

public class Q00715_PathToBecomingALordGoddard extends Quest
{
	private static final int Alfred = 35363;
	
	private static final int WaterSpiritAshutar = 25316;
	private static final int FireSpiritNastron = 25306;
	
	private static final int GoddardCastle = 7;
	
	public Q00715_PathToBecomingALordGoddard()
	{
		super(715, Q00715_PathToBecomingALordGoddard.class.getSimpleName(), "Path to Becoming a Lord - Goddard");
		addStartNpc(Alfred);
		addTalkId(Alfred);
		addKillId(WaterSpiritAshutar);
		addKillId(FireSpiritNastron);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		Castle castle = CastleManager.getInstance().getCastleById(GoddardCastle);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		
		if (event.equals("alfred_q715_03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
		}
		else if (event.equals("alfred_q715_04a.htm"))
		{
			st.set("cond", "3");
		}
		else if (event.equals("alfred_q715_04b.htm"))
		{
			st.set("cond", "2");
		}
		else if (event.equals("alfred_q715_08.htm"))
		{
			if (castle.getOwner().getLeader().getPlayerInstance() != null)
			{
				NpcSay packet = new NpcSay(npc.getObjectId(), Say2.NPC_SHOUT, npc.getId(), NpcStringId.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_GODDARD_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_GODDARD);
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
		
		int cond = st.getInt("cond");
		Castle castle = CastleManager.getInstance().getCastleById(GoddardCastle);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		L2PcInstance castleOwner = castle.getOwner().getLeader().getPlayerInstance();
		
		if (cond == 0)
		{
			if (castleOwner == st.getPlayer())
			{
				if (!hasFort())
				{
					htmltext = "alfred_q715_01.htm";
				}
				else
				{
					htmltext = "alfred_q715_00.htm";
					st.exitQuest(true);
				}
			}
			else
			{
				htmltext = "alfred_q715_00a.htm";
				st.exitQuest(true);
			}
		}
		else if (cond == 1)
		{
			htmltext = "alfred_q715_03.htm";
		}
		else if (cond == 2)
		{
			htmltext = "alfred_q715_05b.htm";
		}
		else if (cond == 3)
		{
			htmltext = "alfred_q715_05a.htm";
		}
		else if (cond == 4)
		{
			st.set("cond", "6");
			htmltext = "alfred_q715_06b.htm";
		}
		else if (cond == 5)
		{
			st.set("cond", "7");
			htmltext = "alfred_q715_06a.htm";
		}
		else if (cond == 6)
		{
			htmltext = "alfred_q715_06b.htm";
		}
		else if (cond == 7)
		{
			htmltext = "alfred_q715_06a.htm";
		}
		else if ((cond == 8) || (cond == 9))
		{
			htmltext = "alfred_q715_07.htm";
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		if ((st.getInt("cond") == 2) && (npc.getId() == FireSpiritNastron))
		{
			st.set("cond", "4");
		}
		else if ((st.getInt("cond") == 3) && (npc.getId() == WaterSpiritAshutar))
		{
			st.set("cond", "5");
		}
		
		if ((st.getInt("cond") == 6) && (npc.getId() == WaterSpiritAshutar))
		{
			st.set("cond", "9");
		}
		else if ((st.getInt("cond") == 7) && (npc.getId() == FireSpiritNastron))
		{
			st.set("cond", "8");
		}
		return null;
	}
	
	private boolean hasFort()
	{
		for (Fort fortress : FortManager.getInstance().getForts())
		{
			if (fortress.getCastleId() == GoddardCastle)
			{
				return true;
			}
		}
		return false;
	}
}