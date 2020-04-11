/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q00503_PursuitClanAmbition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2r.L2DatabaseFactory;
import l2r.gameserver.enums.audio.Sound;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.serverpackets.NpcSay;

public class Q00503_PursuitClanAmbition extends Quest
{
	private static int ImpGraveKepperStat = 1;
	
	// First part
	private static final int G_Let_Martien = 3866;
	private static final int Th_Wyrm_Eggs = 3842;
	private static final int Drake_Eggs = 3841;
	private static final int Bl_Wyrm_Eggs = 3840;
	private static final int Mi_Drake_Eggs = 3839;
	private static final int Brooch = 3843;
	private static final int Bl_Anvil_Coin = 3871;
	
	// Second Part
	private static final int G_Let_Balthazar = 3867;
	private static final int Spiteful_Soul_Energy = 14855;
	
	// Third part
	private static final int G_Let_Rodemai = 3868;
	private static final int Imp_Keys = 3847;
	private static final int Scepter_Judgement = 3869;
	
	// The final item
	private static final int Proof_Aspiration = 3870;
	private static final int[] EggList =
	{
		Mi_Drake_Eggs,
		Bl_Wyrm_Eggs,
		Drake_Eggs,
		Th_Wyrm_Eggs
	};
	
	// NPC's
	private static final int[] NPC =
	{
		30645,
		30758,
		30759,
		30760,
		30761,
		30762,
		30763,
		30512,
		30764,
		30868,
		30765,
		30766
	};
	private static final String[] STATS =
	{
		"cond",
		"Fritz",
		"Lutz",
		"Kurtz",
		"ImpGraveKeeper"
	};
	
	private class dropList
	{
		public int cond, maxcount, chance;
		public int[] items;
		
		protected dropList(int _cond, int _maxcount, int _chance, int[] _items)
		{
			cond = _cond;
			maxcount = _maxcount;
			chance = _chance;
			items = _items;
		}
	}
	
	private static final Map<Integer, dropList> drop = new HashMap<>();
	
	public Q00503_PursuitClanAmbition()
	{
		super(503, Q00503_PursuitClanAmbition.class.getSimpleName(), "");
		addStartNpc(30760);
		for (int npcId : NPC)
		{
			addTalkId(npcId);
		}
		
		drop.put(20282, new dropList(2, 10, 20, new int[]
		{
			Th_Wyrm_Eggs
		}));
		drop.put(20243, new dropList(2, 10, 15, new int[]
		{
			Th_Wyrm_Eggs
		}));
		drop.put(20137, new dropList(2, 10, 20, new int[]
		{
			Drake_Eggs
		}));
		drop.put(20285, new dropList(2, 10, 25, new int[]
		{
			Drake_Eggs
		}));
		drop.put(27178, new dropList(2, 10, 100, new int[]
		{
			Bl_Wyrm_Eggs
		}));
		drop.put(20974, new dropList(5, 10, 20, new int[]
		{
			Spiteful_Soul_Energy
		}));
		drop.put(20668, new dropList(10, 0, 15, new int[] {})); // Grave Guard
		drop.put(27179, new dropList(10, 6, 80, new int[]
		{
			Imp_Keys
		})); // GraveKeyKeeper
		drop.put(27181, new dropList(10, 0, 100, new int[] {})); // Imperial Gravekeeper
		
		addAttackId(27181);
		
		addKillId(20974);
		for (int mobId : drop.keySet())
		{
			addKillId(mobId);
		}
		
		questItemIds = new int[]
		{
			Spiteful_Soul_Energy
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("30760-08.htm"))
		{
			st.giveItems(G_Let_Martien, 1);
			st.set("cond", "1");
			for (String var : STATS)
			{
				st.set(var, "1");
			}
			st.setState(State.STARTED);
		}
		else if (event.equalsIgnoreCase("30760-12.htm"))
		{
			st.giveItems(G_Let_Balthazar, 1);
			st.set("cond", "4");
		}
		else if (event.equalsIgnoreCase("30760-16.htm"))
		{
			st.giveItems(G_Let_Rodemai, 1);
			st.set("cond", "7");
		}
		else if (event.equalsIgnoreCase("30760-20.htm"))
		{
			exit(true, st);
		}
		else if (event.equalsIgnoreCase("30760-22.htm"))
		{
			st.set("cond", "13");
		}
		else if (event.equalsIgnoreCase("30760-23.htm"))
		{
			exit(true, st);
		}
		else if (event.equalsIgnoreCase("30645-03.htm"))
		{
			st.takeItems(G_Let_Martien, -1);
			st.set("cond", "2");
			suscribeMembers(st);
			
			List<L2PcInstance> members = player.getClan().getOnlineMembers(player.getObjectId());
			
			for (L2PcInstance i : members)
			{
				st = newQuestState(i);
				st.setState(State.STARTED);
			}
		}
		else if (event.equalsIgnoreCase("30763-03.htm"))
		{
			if (st.getInt("Kurtz") == 1)
			{
				st.giveItems(Mi_Drake_Eggs, 6);
				st.giveItems(Brooch, 1);
				st.set("Kurtz", "2");
				return "30763-02.htm";
			}
		}
		else if (event.equalsIgnoreCase("30762-03.htm"))
		{
			addSpawn(27178, npc.getX() + 50, npc.getY() + 50, npc.getZ(), 0, false, 120000);
			addSpawn(27178, npc.getX() - 50, npc.getY() - 50, npc.getZ(), 0, false, 120000);
			if (st.getInt("Lutz") == 1)
			{
				st.giveItems(Mi_Drake_Eggs, 4);
				st.giveItems(Bl_Wyrm_Eggs, 3);
				st.set("Lutz", "2");
				return "30762-02.htm";
			}
		}
		else if (event.equalsIgnoreCase("30761-03.htm"))
		{
			addSpawn(27178, npc.getX() + 50, npc.getY() + 50, npc.getZ(), 0, false, 120000);
			addSpawn(27178, npc.getX() - 50, npc.getY() - 50, npc.getZ(), 0, false, 120000);
			if (st.getInt("Fritz") == 1)
			{
				st.giveItems(Bl_Wyrm_Eggs, 3);
				st.set("Fritz", "2");
				return "30761-02.htm";
			}
		}
		else if (event.equalsIgnoreCase("30512-03.htm"))
		{
			st.takeItems(Brooch, -1);
			st.giveItems(Bl_Anvil_Coin, 1);
			st.set("Kurtz", "3");
		}
		else if (event.equalsIgnoreCase("30764-03.htm"))
		{
			st.takeItems(G_Let_Balthazar, -1);
			st.set("cond", "5");
			st.set("Kurtz", "3");
		}
		else if (event.equalsIgnoreCase("30764-05.htm"))
		{
			st.takeItems(G_Let_Balthazar, -1);
			st.set("cond", "5");
		}
		else if (event.equalsIgnoreCase("30764-06.htm"))
		{
			st.takeItems(Bl_Anvil_Coin, -1);
			st.set("Kurtz", "4");
		}
		else if (event.equalsIgnoreCase("30868-04.htm"))
		{
			st.takeItems(G_Let_Rodemai, -1);
			st.set("cond", "8");
		}
		else if (event.equalsIgnoreCase("30868-06a.htm"))
		{
			st.set("cond", "10");
		}
		else if (event.equalsIgnoreCase("30868-10.htm"))
		{
			st.set("cond", "12");
		}
		else if (event.equalsIgnoreCase("30766-04.htm"))
		{
			st.set("cond", "9");
			L2Npc spawnedNpc = addSpawn(30766, 160622, 21230, -3710, 0, false, 90000);
			spawnedNpc.broadcastPacket(new NpcSay(spawnedNpc.getObjectId(), 0, spawnedNpc.getId(), NpcStringId.BLOOD_AND_HONOR));
			spawnedNpc = st.addSpawn(30759, 160665, 21209, -3710, 0, false, 90000);
			spawnedNpc.broadcastPacket(new NpcSay(spawnedNpc.getObjectId(), 0, spawnedNpc.getId(), NpcStringId.AMBITION_AND_POWER));
			spawnedNpc = st.addSpawn(30758, 160665, 21291, -3710, 0, false, 90000);
			spawnedNpc.broadcastPacket(new NpcSay(spawnedNpc.getObjectId(), 0, spawnedNpc.getId(), NpcStringId.WAR_AND_DEATH));
		}
		else if (event.equalsIgnoreCase("30766-08.htm"))
		{
			st.takeItems(Scepter_Judgement, -1);
			exit(false, st);
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
		
		boolean isLeader = player.isClanLeader();
		int npcId = npc.getId();
		
		int kurtz = st.getInt("Kurtz");
		int lutz = st.getInt("Lutz");
		int fritz = st.getInt("Fritz");
		
		switch (st.getState())
		{
			case State.CREATED:
				if (npcId == 30760)
				{
					for (String var : STATS)
					{
						st.set(var, "0");
					}
					
					if ((player.getClan() != null) && (player.getClan().getLevel() >= 5))
					{
						return htmltext;
					}
					else if (player.getClan() != null)
					{
						if (isLeader)
						{
							int clanLevel = player.getClan().getLevel();
							if (st.getQuestItemsCount(Proof_Aspiration) > 0)
							{
								htmltext = "30760-03.htm";
								st.exitQuest(true);
							}
							else if (clanLevel == 4)
							{
								htmltext = "30760-04.htm";
							}
							else
							{
								htmltext = "30760-02.htm";
								st.exitQuest(true);
							}
						}
						else
						{
							htmltext = "30760-04t.htm";
							st.exitQuest(true);
						}
					}
					else
					{
						htmltext = "30760-01.htm";
						st.exitQuest(true);
					}
				}
				break;
			case State.STARTED:
				if (isLeader)
				{
					int cond = st.getInt("cond");
					
					if (cond == 0)
					{
						st.set("cond", "1");
					}
					if (st.get("Kurtz") == null)
					{
						st.set("Kurtz", "1");
					}
					if (st.get("Lutz") == null)
					{
						st.set("Lutz", "1");
					}
					if (st.get("Fritz") == null)
					{
						st.set("Fritz", "1");
					}
					
					if (npcId == 30760)
					{
						if (cond == 1)
						{
							htmltext = "30760-09.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30760-10.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30760-11.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30760-13.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30760-14.htm";
						}
						else if (cond == 6)
						{
							htmltext = "30760-15.htm";
						}
						else if (cond == 7)
						{
							htmltext = "30760-17.htm";
						}
						else if (cond == 12)
						{
							htmltext = "30760-19.htm";
						}
						else if (cond == 13)
						{
							htmltext = "30760-24.htm";
						}
						else
						{
							htmltext = "30760-18.htm";
						}
					}
					else if (npcId == 30645)
					{
						if (cond == 1)
						{
							htmltext = "30645-02.htm";
						}
						else if (cond == 2)
						{
							if (checkEggs(st) && (kurtz > 1) && (lutz > 1) && (fritz > 1))
							{
								htmltext = "30645-05.htm";
								st.set("cond", "3");
								for (int item : EggList)
								{
									st.takeItems(item, -1);
								}
							}
							else
							{
								htmltext = "30645-04.htm";
							}
						}
						else if (cond == 3)
						{
							htmltext = "30645-07.htm";
						}
						else
						{
							htmltext = "30645-08.htm";
						}
					}
					else if ((npcId == 30762) && (cond == 2))
					{
						htmltext = "30762-01.htm";
					}
					else if ((npcId == 30763) && (cond == 2))
					{
						htmltext = "30763-01.htm";
					}
					else if ((npcId == 30761) && (cond == 2))
					{
						htmltext = "30761-01.htm";
					}
					else if (npcId == 30512)
					{
						if (kurtz == 1)
						{
							htmltext = "30512-01.htm";
						}
						else if (kurtz == 2)
						{
							htmltext = "30512-02.htm";
						}
						else
						{
							htmltext = "30512-04.htm";
						}
					}
					else if (npcId == 30764)
					{
						if (cond == 4)
						{
							if (kurtz > 2)
							{
								htmltext = "30764-04.htm";
							}
							else
							{
								htmltext = "30764-02.htm";
							}
						}
						else if (cond == 5)
						{
							if (st.getQuestItemsCount(Spiteful_Soul_Energy) > 9)
							{
								htmltext = "30764-08.htm";
								st.takeItems(Spiteful_Soul_Energy, -1);
								st.takeItems(Brooch, -1);
								st.set("cond", "6");
							}
							else
							{
								htmltext = "30764-07.htm";
							}
						}
						else if (cond == 6)
						{
							htmltext = "30764-09.htm";
						}
					}
					else if (npcId == 30868)
					{
						if (cond == 7)
						{
							htmltext = "30868-02.htm";
						}
						else if (cond == 8)
						{
							htmltext = "30868-05.htm";
						}
						else if (cond == 9)
						{
							htmltext = "30868-06.htm";
						}
						else if (cond == 10)
						{
							htmltext = "30868-08.htm";
						}
						else if (cond == 11)
						{
							htmltext = "30868-09.htm";
						}
						else if (cond == 12)
						{
							htmltext = "30868-11.htm";
						}
					}
					else if (npcId == 30766)
					{
						if (cond == 8)
						{
							htmltext = "30766-02.htm";
						}
						else if (cond == 9)
						{
							htmltext = "30766-05.htm";
						}
						else if (cond == 10)
						{
							htmltext = "30766-06.htm";
						}
						else if ((cond == 11) || (cond == 12) || (cond == 13))
						{
							htmltext = "30766-07.htm";
						}
					}
					else if (npcId == 30765)
					{
						if (cond == 10)
						{
							if (st.getQuestItemsCount(Imp_Keys) < 6)
							{
								htmltext = "30765-03a.htm";
							}
							else if (st.getInt("ImpGraveKeeper") == 3)
							{
								htmltext = "30765-02.htm";
								st.set("cond", "11");
								st.takeItems(Imp_Keys, 6);
								st.giveItems(Scepter_Judgement, 1);
							}
							else
							{
								htmltext = "30765-02a.htm";
							}
						}
						else
						{
							htmltext = "30765-03b.htm";
						}
					}
					else if (npcId == 30759)
					{
						htmltext = "30759-01.htm";
					}
					else if (npcId == 30758)
					{
						htmltext = "30758-01.htm";
					}
				}
				else
				{
					int cond = getLeaderVar(st, "cond");
					
					if ((npcId == 30645) && ((cond == 1) || (cond == 2) || (cond == 3)))
					{
						htmltext = "30645-01.htm";
					}
					else if (npcId == 30868)
					{
						if ((cond == 9) || (cond == 10))
						{
							htmltext = "30868-07.htm";
						}
						else if (cond == 7)
						{
							htmltext = "30868-01.htm";
						}
					}
					else if ((npcId == 30764) && (cond == 4))
					{
						htmltext = "30764-01.htm";
					}
					else if ((npcId == 30766) && (cond == 8))
					{
						htmltext = "30766-01.htm";
					}
					else if ((npcId == 30512) && (cond < 6) && (cond > 2))
					{
						htmltext = "30512-01a.htm";
					}
					else if ((npcId == 30765) && (cond == 10))
					{
						htmltext = "30765-01.htm";
					}
					else if (npcId == 30760)
					{
						if (cond == 3)
						{
							htmltext = "30760-11t.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30760-15t.htm";
						}
						else if (cond == 12)
						{
							htmltext = "30760-19t.htm";
						}
						else if (cond == 13)
						{
							htmltext = "30766-24t.htm";
						}
					}
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public final String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon, L2Skill skill)
	{
		if ((npc.getMaxHp() / 2) > npc.getCurrentHp())
		{
			if (getRandom(100) < 4)
			{
				if (ImpGraveKepperStat == 1)
				{
					for (int i = 0; i < 19; i++)
					{
						int x = (int) (100 * Math.cos(i * .785));
						int y = (int) (100 * Math.sin(i * .785));
						addSpawn(27180, npc.getX() + x, npc.getY() + y, npc.getZ(), 0, false, 0);
					}
					ImpGraveKepperStat = 2;
				}
				else
				{
					Collection<L2PcInstance> plrs = npc.getKnownList().getKnownPlayers().values();
					{
						if (!plrs.isEmpty())
						{
							L2PcInstance playerToTP = (L2PcInstance) plrs.toArray()[getRandom(plrs.size())];
							playerToTP.teleToLocation(185462, 20342, -3250);
						}
					}
				}
			}
		}
		return super.onAttack(npc, player, damage, isSummon, skill);
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		int npcId = npc.getId();
		
		QuestState leader_st = null;
		if (player.isClanLeader())
		{
			leader_st = player.getQuestState(getName());
		}
		else
		{
			L2Clan clan = player.getClan();
			if (clan != null)
			{
				if (clan.getLeader() != null)
				{
					L2PcInstance leader = clan.getLeader().getPlayerInstance();
					if (leader != null)
					{
						if (player.isInsideRadius(leader, 2000, false, false))
						{
							leader_st = leader.getQuestState(getName());
						}
					}
				}
			}
		}
		
		if (leader_st != null)
		{
			if (leader_st.getState() != State.STARTED)
			{
				return super.onKill(npc, player, isSummon);
			}
			
			dropList droplist;
			synchronized (drop)
			{
				droplist = drop.get(npcId);
			}
			
			int cond = leader_st.getInt("cond");
			
			if ((cond == droplist.cond) && (getRandom(100) < droplist.chance))
			{
				if (droplist.items.length > 0)
				{
					giveItem(droplist.items[0], droplist.maxcount, leader_st);
				}
				else
				{
					if (npcId == 27181)
					{
						L2Npc spawnedNpc = leader_st.addSpawn(30765, 120000);
						npc.broadcastPacket(new NpcSay(spawnedNpc.getObjectId(), 0, spawnedNpc.getId(), NpcStringId.CURSE_OF_THE_GODS_ON_THE_ONE_THAT_DEFILES_THE_PROPERTY_OF_THE_EMPIRE));
						leader_st.set("ImpGraveKeeper", "3");
						ImpGraveKepperStat = 1;
					}
					else
					{
						leader_st.addSpawn(27179);
					}
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	private void exit(boolean complete, QuestState st)
	{
		if (complete)
		{
			st.giveItems(Proof_Aspiration, 1);
			st.addExpAndSp(0, 250000);
			for (String var : STATS)
			{
				st.unset(var);
			}
			st.exitQuest(false);
		}
		else
		{
			st.exitQuest(true);
		}
		st.takeItems(Scepter_Judgement, -1);
		try
		{
			List<L2PcInstance> members = st.getPlayer().getClan().getOnlineMembers(0);
			for (L2PcInstance i : members)
			{
				if (i == null)
				{
					continue;
				}
				QuestState qs = i.getQuestState(getName());
				if (qs != null)
				{
					qs.exitQuest(true);
				}
			}
			offlineMemberExit(st);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void offlineMemberExit(QuestState st)
	{
		int clan = st.getPlayer().getClan().getId();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement offline = con.prepareStatement("DELETE FROM character_quests WHERE name = ? and charId IN (SELECT charId FROM characters WHERE clanId =? AND online=0)"))
		{
			offline.setString(1, getName());
			offline.setInt(2, clan);
			offline.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void suscribeMembers(QuestState st)
	{
		int clan = st.getPlayer().getClan().getId();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement offline = con.prepareStatement("SELECT charId FROM characters WHERE clanid=? AND online=0"))
		{
			PreparedStatement insertion = con.prepareStatement("REPLACE INTO character_quests (charId,name,var,value) VALUES (?,?,?,?)");
			offline.setInt(1, clan);
			ResultSet rs = offline.executeQuery();
			while (rs.next())
			{
				int charId = rs.getInt("charId");
				try
				{
					insertion.setInt(1, charId);
					insertion.setString(2, getName());
					insertion.setString(3, "<state>");
					insertion.setString(4, "Started");
					insertion.executeUpdate();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private int getLeaderVar(QuestState st, String var)
	{
		int val = -1;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			L2Clan clan = st.getPlayer().getClan();
			if (clan == null)
			{
				return -1;
			}
			L2PcInstance leader = clan.getLeader().getPlayerInstance();
			if (leader != null)
			{
				return leader.getQuestState(getName()).getInt(var);
			}
			
			PreparedStatement offline = con.prepareStatement("SELECT value FROM character_quests WHERE charId=? AND var=? AND name=?");
			offline.setInt(1, st.getPlayer().getClan().getLeaderId());
			offline.setString(2, var);
			offline.setString(3, getName());
			ResultSet rs = offline.executeQuery();
			while (rs.next())
			{
				val = rs.getInt("value");
			}
		}
		catch (Exception e)
		{
			_log.warn("Pursuit of Clan Ambition: cannot read quest states offline clan leader");
		}
		return val;
	}
	
	private boolean checkEggs(QuestState st)
	{
		int count = 0;
		for (int item : EggList)
		{
			if (st.getQuestItemsCount(item) > 9)
			{
				count += 1;
			}
		}
		return count > 3;
	}
	
	private void giveItem(int item, int maxcount, QuestState st)
	{
		long count = st.getQuestItemsCount(item);
		if (count < maxcount)
		{
			st.giveItems(item, 1);
			if (count == (maxcount - 1))
			{
				st.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
			}
			else
			{
				st.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
	}
}