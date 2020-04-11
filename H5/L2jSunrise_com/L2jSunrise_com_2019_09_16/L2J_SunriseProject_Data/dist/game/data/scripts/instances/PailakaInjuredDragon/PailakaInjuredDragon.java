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
package instances.PailakaInjuredDragon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.audio.Sound;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.model.zone.L2ZoneType;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SpecialCamera;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

/**
 * @author vGodFather TODO: confirm stage1 and stage2 drop chance
 */
public class PailakaInjuredDragon extends Quest
{
	private static final String qn = "Q00144_PailakaInjuredDragon";
	
	private static final int MIN_LEVEL = 73;
	private static final int MAX_LEVEL = 77;
	private static final int MAX_SUMMON_LEVEL = 80;
	private static final int EXIT_TIME = 5;
	private static final int TEMPLATE_ID = 45;
	
	//@formatter:off
	protected static final int[] TELEPORT =
	{
		125757, -40928, -3736
	};
	
	private static final Map<Integer, int[]> NOEXIT_ZONES = new ConcurrentHashMap<>();
	static
	{
		NOEXIT_ZONES.put(200001, new int[] { 123167, -45743, -3023 }); 
		NOEXIT_ZONES.put(200002, new int[] { 117783, -46398, -2560 });
		NOEXIT_ZONES.put(200003, new int[] { 116791, -51556, -2584 });
		NOEXIT_ZONES.put(200004, new int[] { 117993, -52505, -2480 });
		NOEXIT_ZONES.put(200005, new int[] { 113226, -44080, -2776 });
		NOEXIT_ZONES.put(200006, new int[] { 107916, -46716, -2008 });
		NOEXIT_ZONES.put(200007, new int[] { 118341, -55951, -2280 });
		NOEXIT_ZONES.put(200008, new int[] { 110127, -41562, -2332 });
	}
	
	// NPCS
	private static final int KETRA_ORC_SHAMAN = 32499;
	private static final int KETRA_ORC_SUPPORTER = 32502;
	private static final int KETRA_ORC_SUPPORTER2 = 32512;
	private static final int KETRA_ORC_INTELIGENCE_OFFICER = 32509;
	
	// WALL MOBS
	private static final int VARKA_SILENOS_RECRUIT = 18635;
	private static final int VARKA_SILENOS_FOOTMAN = 18636;
	private static final int VARKA_SILENOS_WARRIOR = 18642;
	private static final int VARKA_SILENOS_OFFICER = 18646;
	private static final int VARKAS_COMMANDER = 18654;
	private static final int VARKA_ELITE_GUARD = 18653;
	private static final int VARKA_SILENOS_GREAT_MAGUS = 18649;
	private static final int VARKA_SILENOS_GENERAL = 18650;
	private static final int VARKA_SILENOS_HEAD_GUARD = 18655;
	private static final int PROPHET_GUARD = 18657;
	private static final int VARKAS_PROPHET = 18659;
	
	// EXTRA WALL SILENOS
	private static final int VARKA_SILENOS_MEDIUM = 18644;
	private static final int VARKA_SILENOS_PRIEST = 18641;
	private static final int VARKA_SILENOS_SHAMAN = 18640;
	private static final int VARKA_SILENOS_SEER = 18648;
	private static final int VARKA_SILENOS_MAGNUS = 18645;
	private static final int DISCIPLE_OF_PROPHET = 18658;
	private static final int VARKA_HEAD_MAGUS = 18656;
	private static final int VARKA_SILENOS_GREAT_SEER = 18652;
	
	// NORMAL MOBS
	private static final int ANTYLOPE_1 = 18637;
	private static final int ANTYLOPE_2 = 18643;
	private static final int ANTYLOPE_3 = 18651;
	private static final int FLAVA = 18647;
	
	// BOSS
	private static final int LATANA = 18660;
	
	// ITEMS
	private static final int SPEAR = 13052;
	private static final int ENCHSPEAR = 13053;
	private static final int LASTSPEAR = 13054;
	private static final int STAGE1 = 13056;
	private static final int STAGE2 = 13057;
	
	private static final int SHIELD_POTION = 13032;
	private static final int HEAL_POTION = 13033;
	
	// Rewards
	private static final int PSHIRT = 13296;
	private static final int SCROLL_OF_ESCAPE = 736;
	
	private static int buff_counter = 5;
	private static boolean _hasDoneAnimation = false;
	
	// Arrays
	private static final int[] NPCS =
	{
		KETRA_ORC_SHAMAN,
		KETRA_ORC_SUPPORTER,
		KETRA_ORC_INTELIGENCE_OFFICER,
		KETRA_ORC_SUPPORTER2
	};
	
	private static final List<Integer> WALL_MONSTERS = new ArrayList<>();
	static
	{
		// 1st Row Mobs
		WALL_MONSTERS.add(VARKA_SILENOS_FOOTMAN);
		WALL_MONSTERS.add(VARKA_SILENOS_WARRIOR);
		WALL_MONSTERS.add(VARKA_SILENOS_OFFICER);
		WALL_MONSTERS.add(VARKAS_COMMANDER);
		WALL_MONSTERS.add(VARKA_SILENOS_RECRUIT);
		WALL_MONSTERS.add(PROPHET_GUARD);
		WALL_MONSTERS.add(VARKA_ELITE_GUARD);
		WALL_MONSTERS.add(VARKA_SILENOS_GREAT_MAGUS);
		WALL_MONSTERS.add(VARKA_SILENOS_GENERAL);
		WALL_MONSTERS.add(VARKA_SILENOS_HEAD_GUARD);
		WALL_MONSTERS.add(PROPHET_GUARD);
		WALL_MONSTERS.add(VARKAS_PROPHET);
		
		// 2nd Row Mobs
		WALL_MONSTERS.add(DISCIPLE_OF_PROPHET);
		WALL_MONSTERS.add(VARKA_HEAD_MAGUS);
		WALL_MONSTERS.add(VARKA_SILENOS_GREAT_SEER);
		WALL_MONSTERS.add(VARKA_SILENOS_SHAMAN);
		WALL_MONSTERS.add(VARKA_SILENOS_MAGNUS);
		WALL_MONSTERS.add(VARKA_SILENOS_SEER);
		WALL_MONSTERS.add(VARKA_SILENOS_MEDIUM);
		WALL_MONSTERS.add(VARKA_SILENOS_PRIEST);
	}
	
	private static final List<PailakaDrop> DROPLIST = new ArrayList<>();
	static
	{
		DROPLIST.add(new PailakaDrop(HEAL_POTION, 80));
		DROPLIST.add(new PailakaDrop(SHIELD_POTION, 30));
	}
	
	private static final int[] OTHER_MONSTERS =
	{
		ANTYLOPE_1,
		ANTYLOPE_2,
		ANTYLOPE_3,
		FLAVA
	};
	
	private static final int[] ITEMS =
	{
		SPEAR,
		ENCHSPEAR,
		LASTSPEAR,
		STAGE1,
		STAGE2,
		SHIELD_POTION,
		HEAL_POTION
	};
	
	private static final int[][] BUFFS =
	{
		{ 4357, 2 }, // Haste Lv2
		{ 4342, 2 }, // Wind Walk Lv2
		{ 4356, 3 }, // Empower Lv3
		{ 4355, 3 }, // Acumen Lv3
		{ 4351, 6 }, // Concentration Lv6
		{ 4345, 3 }, // Might Lv3
		{ 4358, 3 }, // Guidance Lv3
		{ 4359, 3 }, // Focus Lv3
		{ 4360, 3 }, // Death Wisper Lv3
		{ 4352, 2 }, // Berserker Spirit Lv2
		{ 4354, 4 }, // Vampiric Rage Lv4
		{ 4347, 6 } // Blessed Body Lv6
	};
	
	private static final int[][] HP_HERBS_DROPLIST =
	{
		// itemId, count, chance
		{ 8601, 1, 40 },
		{ 8600, 1, 70 }
	};
	
	private static final int[][] MP_HERBS_DROPLIST =
	{
		// itemId, count, chance
		{ 8604, 1, 40 },
		{ 8603, 1, 70 }
	};
	//@formatter:on
	
	public PailakaInjuredDragon()
	{
		super(144, qn, "");
		addStartNpc(KETRA_ORC_SHAMAN);
		addFirstTalkId(NPCS);
		addTalkId(NPCS);
		addKillId(LATANA);
		addKillId(OTHER_MONSTERS);
		addAggroRangeEnterId(LATANA);
		addSpawnId(WALL_MONSTERS);
		addKillId(WALL_MONSTERS);
		addAttackId(LATANA);
		addEnterZoneId(NOEXIT_ZONES.keySet());
		
		questItemIds = ITEMS;
	}
	
	private static final void dropItem(L2Npc mob, L2PcInstance player)
	{
		Collections.shuffle(DROPLIST);
		for (PailakaDrop pd : DROPLIST)
		{
			if (getRandom(100) < pd.getChance())
			{
				((L2MonsterInstance) mob).dropItem(player, pd.getItemID(), getRandom(1, 6));
				return;
			}
		}
	}
	
	private static void giveBuff(L2Npc npc, L2PcInstance player, int skillId, int level)
	{
		npc.setTarget(player);
		npc.doCast(SkillData.getInstance().getInfo(skillId, level));
		buff_counter--;
		return;
	}
	
	protected static final void teleportPlayer(L2Playable player, int[] coords, int instanceId)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], true);
	}
	
	private final synchronized void enterInstance(L2PcInstance player, boolean isNewQuest)
	{
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (world.getTemplateId() != TEMPLATE_ID)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ENTERED_ANOTHER_INSTANT_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON));
				return;
			}
			
			final Instance inst = InstanceManager.getInstance().getInstance(world.getInstanceId());
			if (inst != null)
			{
				checkMaxSummonLevel(player);
				teleportPlayer(player, TELEPORT, world.getInstanceId());
			}
		}
		// New instance
		else
		{
			if (!isNewQuest)
			{
				final QuestState st = player.getQuestState(qn);
				st.unset("cond");
				st.exitQuest(true);
				player.sendMessage("Your instance has ended so your quest has been canceled. Talk to me again");
				return;
			}
			
			final int instanceId = InstanceManager.getInstance().createDynamicInstance("PailakaInjuredDragon.xml");
			world = new InstanceWorld();
			world.setInstanceId(instanceId);
			world.setTemplateId(TEMPLATE_ID);
			InstanceManager.getInstance().addWorld(world);
			
			checkMaxSummonLevel(player);
			
			world.addAllowed(player.getObjectId());
			teleportPlayer(player, TELEPORT, instanceId);
		}
	}
	
	private final void checkMaxSummonLevel(L2PcInstance player)
	{
		if (player.hasSummon() && player.getSummon().isPet() && (player.getSummon().getLevel() > MAX_SUMMON_LEVEL))
		{
			player.getSummon().unSummon(player);
		}
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		final int cond = st.getInt("cond");
		if (event.equalsIgnoreCase("enter"))
		{
			if (player.getLevel() < MIN_LEVEL)
			{
				return "32499-no.htm";
			}
			if (player.getLevel() > MAX_LEVEL)
			{
				return "32499-no.htm";
			}
			if (cond < 2)
			{
				return "32499-no.htm";
			}
			enterInstance(player, cond == 2);
			return null;
		}
		else if (event.equalsIgnoreCase("32499-02.htm")) // Shouldn't be 32499-04.htm ???
		{
			if (cond == 0)
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				playSound(player, Sound.ITEMSOUND_QUEST_ACCEPT);
			}
		}
		else if (event.equalsIgnoreCase("32499-05.htm"))
		{
			if (cond == 1)
			{
				st.set("cond", "2");
				st.setMemoState(2);
				playSound(player, Sound.ITEMSOUND_QUEST_ACCEPT); // double quest accept ???
			}
		}
		else if (event.equalsIgnoreCase("32502-05.htm"))
		{
			if (cond == 2)
			{
				st.set("cond", "3");
				if (!st.hasQuestItems(SPEAR))
				{
					st.giveItems(SPEAR, 1);
					playSound(player, Sound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		else if (event.equalsIgnoreCase("32509-02.htm"))
		{
			switch (cond)
			{
				case 2:
				case 3:
					return "32509-07.htm";
				case 4:
					st.set("cond", "5");
					st.takeItems(SPEAR, 1);
					st.takeItems(STAGE1, 1);
					st.giveItems(ENCHSPEAR, 1);
					playSound(player, Sound.ITEMSOUND_QUEST_ITEMGET);
					return "32509-02.htm";
				case 5:
					return "32509-01.htm";
				case 6:
					st.set("cond", "7");
					st.takeItems(ENCHSPEAR, 1);
					st.takeItems(STAGE2, 1);
					st.giveItems(LASTSPEAR, 1);
					playSound(player, Sound.ITEMSOUND_QUEST_ITEMGET);
					
					addSpawn(LATANA, 105732, -41787, -1782, 35742, false, 0, false, npc.getInstanceId());
					return "32509-03.htm";
				case 7:
					return "32509-03.htm";
				default:
					break;
			}
		}
		else if (event.equalsIgnoreCase("32509-06.htm"))
		{
			if (buff_counter < 1)
			{
				return "32509-05.htm";
			}
		}
		else if (event.equalsIgnoreCase("32512-02.htm"))
		{
			st.unset("cond");
			playSound(player, Sound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(false);
			
			Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
			inst.setDuration(EXIT_TIME * 60000);
			inst.setEmptyDestroyTime(0);
			
			if (inst.containsPlayer(player.getObjectId()))
			{
				player.setVitalityPoints(20000, true);
				st.addExpAndSp(28000000, 2850000);
				st.giveItems(SCROLL_OF_ESCAPE, 1);
				st.giveItems(PSHIRT, 1);
			}
		}
		else if (event.startsWith("buff"))
		{
			if (buff_counter > 0)
			{
				final int nr = Integer.parseInt(event.split("buff")[1]);
				giveBuff(npc, player, BUFFS[nr - 1][0], BUFFS[nr - 1][1]);
				return "32509-06.htm";
			}
			return "32509-05.htm";
		}
		else if (event.equalsIgnoreCase("start_anime"))
		{
			_hasDoneAnimation = true;
			
			npc.abortAttack();
			npc.abortCast();
			npc.setIsInvul(true);
			npc.setIsImmobilized(true);
			npc.disableAllSkills();
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.abortAttack();
			player.abortCast();
			player.stopMove(null);
			player.setTarget(null);
			if (player.hasSummon())
			{
				player.getSummon().abortAttack();
				player.getSummon().abortCast();
				player.getSummon().stopMove(null);
				player.getSummon().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
			
			player.sendPacket(new SpecialCamera(npc, 600, 200, 5, 0, 15000, 10000, (-10), 8, 1, 1, 1));
			startQuestTimer("start_anime2", 2000, npc, player);
			return null;
		}
		else if (event.equalsIgnoreCase("start_anime2"))
		{
			player.sendPacket(new SpecialCamera(npc, 400, 200, 5, 4000, 15000, 10000, (-10), 8, 1, 1, 0));
			startQuestTimer("start_anime3", 4000, npc, player);
			return null;
		}
		else if (event.equalsIgnoreCase("start_anime3"))
		{
			player.sendPacket(new SpecialCamera(npc, 300, 195, 4, 1500, 15000, 10000, (-5), 10, 1, 1, 0));
			startQuestTimer("start_anime4", 1700, npc, player);
			return null;
		}
		else if (event.equalsIgnoreCase("start_anime4"))
		{
			player.sendPacket(new SpecialCamera(npc, 130, 2, 5, 0, 15000, 10000, 0, 0, 1, 0, 1));
			startQuestTimer("start_anime5", 2000, npc, player);
			return null;
		}
		else if (event.equalsIgnoreCase("start_anime5"))
		{
			player.sendPacket(new SpecialCamera(npc, 220, 0, 4, 800, 15000, 10000, 5, 10, 1, 0, 0));
			startQuestTimer("start_anime6", 2000, npc, player);
			return null;
		}
		else if (event.equalsIgnoreCase("start_anime6"))
		{
			player.sendPacket(new SpecialCamera(npc, 250, 185, 5, 4000, 15000, 10000, (-5), 10, 1, 1, 0));
			startQuestTimer("start_anime7", 4000, npc, player);
			return null;
		}
		else if (event.equalsIgnoreCase("start_anime7"))
		{
			player.sendPacket(new SpecialCamera(npc, 200, 0, 5, 2000, 15000, 10000, 0, 25, 1, 0, 0));
			startQuestTimer("start_anime8", 4530, npc, player);
			return null;
		}
		else if (event.equalsIgnoreCase("start_anime8"))
		{
			npc.doCast(SkillData.getInstance().getInfo(5759, 1));
			player.sendPacket(new SpecialCamera(npc, 300, (-3), 5, 3500, 15000, 6000, 0, 6, 1, 0, 0));
			startQuestTimer("start_anime9", 10000, npc, player);
			return null;
		}
		else if (event.equalsIgnoreCase("start_anime9"))
		{
			npc.setIsInvul(false);
			npc.setIsImmobilized(false);
			npc.enableAllSkills();
			npc.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player);
			return null;
		}
		else if (event.equalsIgnoreCase("end_anime"))
		{
			player.sendPacket(new SpecialCamera(npc, 450, 200, 3, 0, 15000, 10000, (-15), 20, 1, 1, 1));
			startQuestTimer("end_anime1", 100, npc, player);
			return null;
		}
		else if (event.equalsIgnoreCase("end_anime1"))
		{
			player.sendPacket(new SpecialCamera(npc, 350, 200, 5, 5600, 15000, 10000, (-15), 10, 1, 1, 0));
			startQuestTimer("end_anime2", 5600, npc, player);
			return null;
		}
		else if (event.equalsIgnoreCase("end_anime2"))
		{
			player.sendPacket(new SpecialCamera(npc, 360, 200, 5, 1000, 15000, 2000, (-15), 10, 1, 1, 0));
			return null;
		}
		return event;
	}
	
	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.getId() + ".htm";
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		final int cond = st.getInt("cond");
		switch (npc.getId())
		{
			case KETRA_ORC_SHAMAN:
				switch (st.getState())
				{
					case State.CREATED:
						if (player.getLevel() < MIN_LEVEL)
						{
							return "32499-no.htm";
						}
						if (player.getLevel() > MAX_LEVEL)
						{
							return "32499-over.htm";
						}
						return "32499-01.htm";
					case State.STARTED:
						if (player.getLevel() < MIN_LEVEL)
						{
							return "32499-no.htm";
						}
						if (player.getLevel() > MAX_LEVEL)
						{
							return "32499-over.htm";
						}
						
						if (st.getMemoState() < 2)
						{
							return "32499-02.htm";
						}
						
						if (cond == 1)
						{
							return "32499-04.htm";
						}
						else if (cond >= 2)
						{
							return "32499-05.htm";
						}
					case State.COMPLETED:
						return "32499-completed.htm";
					default:
						return "32499-no.htm";
				}
			case KETRA_ORC_SUPPORTER:
				if (cond > 2)
				{
					return "32502-05.htm";
				}
				return "32502-01.htm";
			case KETRA_ORC_INTELIGENCE_OFFICER:
				return "32509-00.htm";
			case KETRA_ORC_SUPPORTER2:
				if (st.getState() == State.COMPLETED)
				{
					return "32512-03.htm";
				}
				else if (cond == 8)
				{
					return "32512-01.htm";
				}
		}
		return getNoQuestMsg(player);
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if ((st == null) || (st.getState() != State.STARTED))
		{
			return null;
		}
		
		final int cond = st.getInt("cond");
		switch (npc.getId())
		{
			case VARKA_SILENOS_FOOTMAN:
			case VARKA_SILENOS_RECRUIT:
				dropHerb(npc, player, HP_HERBS_DROPLIST);
				if ((cond == 3) && st.hasQuestItems(SPEAR) && !st.hasQuestItems(STAGE1) && (getRandom(100) < 70))
				{
					st.set("cond", "4");
					st.giveItems(STAGE1, 1);
					playSound(player, Sound.ITEMSOUND_QUEST_ITEMGET);
				}
				spawnMageBehind(npc, player, VARKA_SILENOS_MEDIUM);
				checkIfLastInWall(npc, player);
				break;
			case VARKA_SILENOS_WARRIOR:
				dropHerb(npc, player, HP_HERBS_DROPLIST);
				if ((cond == 3) && st.hasQuestItems(SPEAR) && !st.hasQuestItems(STAGE1) && (getRandom(100) < 70))
				{
					st.set("cond", "4");
					st.giveItems(STAGE1, 1);
					playSound(player, Sound.ITEMSOUND_QUEST_ITEMGET);
				}
				spawnMageBehind(npc, player, VARKA_SILENOS_PRIEST);
				checkIfLastInWall(npc, player);
				break;
			case VARKA_ELITE_GUARD:
				dropHerb(npc, player, HP_HERBS_DROPLIST);
				if ((cond == 3) && st.hasQuestItems(SPEAR) && !st.hasQuestItems(STAGE1) && (getRandom(100) < 70))
				{
					st.set("cond", "4");
					st.giveItems(STAGE1, 1);
					playSound(player, Sound.ITEMSOUND_QUEST_ITEMGET);
				}
				spawnMageBehind(npc, player, VARKA_SILENOS_SHAMAN);
				checkIfLastInWall(npc, player);
				break;
			case VARKAS_COMMANDER:
			case VARKA_SILENOS_OFFICER:
				dropHerb(npc, player, HP_HERBS_DROPLIST);
				if ((cond == 3) && st.hasQuestItems(SPEAR) && !st.hasQuestItems(STAGE1) && (getRandom(100) < 30))
				{
					st.set("cond", "4");
					st.giveItems(STAGE1, 1);
					playSound(player, Sound.ITEMSOUND_QUEST_ITEMGET);
				}
				spawnMageBehind(npc, player, VARKA_SILENOS_SEER);
				checkIfLastInWall(npc, player);
				break;
			case VARKA_SILENOS_GREAT_MAGUS:
			case VARKA_SILENOS_GENERAL:
				dropHerb(npc, player, HP_HERBS_DROPLIST);
				if ((cond == 5) && st.hasQuestItems(ENCHSPEAR) && !st.hasQuestItems(STAGE2) && (getRandom(100) < 70))
				{
					st.set("cond", "6");
					st.giveItems(STAGE2, 1);
					playSound(player, Sound.ITEMSOUND_QUEST_ITEMGET);
				}
				spawnMageBehind(npc, player, VARKA_SILENOS_MAGNUS);
				checkIfLastInWall(npc, player);
				break;
			case VARKAS_PROPHET:
				dropHerb(npc, player, HP_HERBS_DROPLIST);
				if ((cond == 5) && st.hasQuestItems(ENCHSPEAR) && !st.hasQuestItems(STAGE2) && (getRandom(100) < 70))
				{
					st.set("cond", "6");
					st.giveItems(STAGE2, 1);
					playSound(player, Sound.ITEMSOUND_QUEST_ITEMGET);
				}
				spawnMageBehind(npc, player, DISCIPLE_OF_PROPHET);
				checkIfLastInWall(npc, player);
				break;
			case VARKA_SILENOS_HEAD_GUARD:
				dropHerb(npc, player, HP_HERBS_DROPLIST);
				if ((cond == 5) && st.hasQuestItems(ENCHSPEAR) && !st.hasQuestItems(STAGE2) && (getRandom(100) < 70))
				{
					st.set("cond", "6");
					st.giveItems(STAGE2, 1);
					playSound(player, Sound.ITEMSOUND_QUEST_ITEMGET);
				}
				spawnMageBehind(npc, player, VARKA_HEAD_MAGUS);
				checkIfLastInWall(npc, player);
				break;
			case PROPHET_GUARD:
				dropHerb(npc, player, HP_HERBS_DROPLIST);
				if ((cond == 5) && st.hasQuestItems(ENCHSPEAR) && !st.hasQuestItems(STAGE2) && (getRandom(100) < 70))
				{
					st.set("cond", "6");
					st.giveItems(STAGE2, 1);
					playSound(player, Sound.ITEMSOUND_QUEST_ITEMGET);
				}
				spawnMageBehind(npc, player, VARKA_SILENOS_GREAT_SEER);
				checkIfLastInWall(npc, player);
				break;
			case LATANA:
				st.set("cond", "8");
				playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
				startQuestTimer("end_anime", 1000, npc, player);
				addSpawn(KETRA_ORC_SUPPORTER2, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false, npc.getInstanceId());
				break;
			case ANTYLOPE_1:
			case ANTYLOPE_2:
			case ANTYLOPE_3:
			case FLAVA:
				dropItem(npc, player);
				break;
			default:
				dropHerb(npc, player, HP_HERBS_DROPLIST);
				dropHerb(npc, player, MP_HERBS_DROPLIST);
				break;
		}
		return super.onKill(npc, player, isSummon);
	}
	
	private final void spawnMageBehind(L2Npc npc, L2PcInstance player, int mageId)
	{
		if (getRandom(100) < 40)
		{
			List<L2Character> mobs = player.getKnownList().getKnownCharactersInRadius(700);
			for (L2Character npcs : mobs)
			{
				switch (npcs.getId())
				{
					case VARKA_SILENOS_GREAT_SEER:
					case VARKA_HEAD_MAGUS:
					case DISCIPLE_OF_PROPHET:
					case VARKA_SILENOS_SEER:
					case VARKA_SILENOS_SHAMAN:
					case VARKA_SILENOS_MEDIUM:
						return;
				}
			}
			
			final double rads = Math.toRadians(Util.convertHeadingToDegree(npc.getSpawn().getHeading()) + 180);
			final int mageX = (int) (npc.getX() + (150 * Math.cos(rads)));
			final int mageY = (int) (npc.getY() + (150 * Math.sin(rads)));
			final L2Npc mageBack = addSpawn(mageId, mageX, mageY, npc.getZ(), npc.getSpawn().getHeading(), false, 0, true, npc.getInstanceId());
			mageBack.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, player, 1000);
		}
	}
	
	private final void checkIfLastInWall(L2Npc npc, L2PcInstance player)
	{
		final Collection<L2Character> knowns = npc.getKnownList().getKnownCharactersInRadius(700);
		for (L2Character npcs : knowns)
		{
			if (!npcs.isNpc() || npcs.isDead())
			{
				continue;
			}
			
			switch (npc.getId())
			{
				case VARKA_SILENOS_FOOTMAN:
				case VARKA_SILENOS_RECRUIT:
				case VARKA_SILENOS_WARRIOR:
					switch (npcs.getId())
					{
						case VARKA_SILENOS_FOOTMAN:
						case VARKA_SILENOS_RECRUIT:
						case VARKA_SILENOS_WARRIOR:
							return;
					}
					break;
				case VARKA_ELITE_GUARD:
				case VARKAS_COMMANDER:
				case VARKA_SILENOS_OFFICER:
					switch (npcs.getId())
					{
						case VARKA_ELITE_GUARD:
						case VARKAS_COMMANDER:
						case VARKA_SILENOS_OFFICER:
							return;
					}
					break;
				case VARKA_SILENOS_GREAT_MAGUS:
				case VARKA_SILENOS_GENERAL:
				case VARKAS_PROPHET:
					switch (npcs.getId())
					{
						case VARKA_SILENOS_GREAT_MAGUS:
						case VARKA_SILENOS_GENERAL:
						case VARKAS_PROPHET:
							return;
					}
					break;
				case VARKA_SILENOS_HEAD_GUARD:
				case PROPHET_GUARD:
					switch (npcs.getId())
					{
						case VARKA_SILENOS_HEAD_GUARD:
						case PROPHET_GUARD:
							return;
					}
					break;
			}
		}
		
		for (L2Character npcs : knowns)
		{
			if (!npcs.isNpc() || npcs.isDead())
			{
				continue;
			}
			
			switch (npc.getId())
			{
				case VARKA_SILENOS_FOOTMAN:
				case VARKA_SILENOS_RECRUIT:
				case VARKA_SILENOS_WARRIOR:
					switch (npcs.getId())
					{
						case VARKA_SILENOS_MEDIUM:
						case VARKA_SILENOS_PRIEST:
							npcs.abortCast();
							npcs.deleteMe();
							break;
					}
					break;
				case VARKA_ELITE_GUARD:
				case VARKAS_COMMANDER:
				case VARKA_SILENOS_OFFICER:
					switch (npcs.getId())
					{
						case VARKA_SILENOS_SHAMAN:
						case VARKA_SILENOS_SEER:
							npcs.abortCast();
							npcs.deleteMe();
							break;
					}
					break;
				case VARKA_SILENOS_GREAT_MAGUS:
				case VARKA_SILENOS_GENERAL:
				case VARKAS_PROPHET:
					switch (npcs.getId())
					{
						case VARKA_SILENOS_MAGNUS:
						case DISCIPLE_OF_PROPHET:
							npcs.abortCast();
							npcs.deleteMe();
							break;
					}
					break;
				case VARKA_SILENOS_HEAD_GUARD:
				case PROPHET_GUARD:
					switch (npcs.getId())
					{
						case VARKA_HEAD_MAGUS:
						case VARKA_SILENOS_GREAT_SEER:
							npcs.abortCast();
							npcs.deleteMe();
							break;
					}
					break;
			}
			
			dropItem(npc, player);
		}
	}
	
	@Override
	public final String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final QuestState st = player.getQuestState(qn);
		if ((st == null) || (st.getState() != State.STARTED) || isSummon)
		{
			return null;
		}
		
		switch (npc.getId())
		{
			case LATANA:
				if (!_hasDoneAnimation)
				{
					startQuestTimer("start_anime", 600, npc, player);
					return null;
				}
				break;
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	@Override
	public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		switch (npc.getId())
		{
			case LATANA:
				if (!_hasDoneAnimation)
				{
					final QuestState st = attacker.getQuestState(qn);
					if ((st == null) || (st.getState() != State.STARTED))
					{
						return super.onAttack(npc, attacker, damage, isSummon);
					}
					
					startQuestTimer("start_anime", 600, npc, attacker);
					return null;
				}
				break;
		}
		
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (WALL_MONSTERS.contains(npc.getId()))
		{
			final L2MonsterInstance monster = (L2MonsterInstance) npc;
			monster.setIsImmobilized(true);
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if ((character instanceof L2Playable) && !character.isDead() && !character.isTeleporting() && character.getActingPlayer().isOnline())
		{
			InstanceWorld world = InstanceManager.getInstance().getWorld(character.getInstanceId());
			if ((world != null) && (world.getTemplateId() == TEMPLATE_ID))
			{
				final int[] zoneTeleport = NOEXIT_ZONES.get(zone.getId());
				if (zoneTeleport != null)
				{
					final Collection<L2Character> knowns = character.getKnownList().getKnownCharactersInRadius(1200);
					for (L2Character npcs : knowns)
					{
						if (!(npcs instanceof L2Npc))
						{
							continue;
						}
						
						if (npcs.isDead())
						{
							continue;
						}
						
						teleportPlayer(character.getActingPlayer(), zoneTeleport, world.getInstanceId());
						break;
					}
				}
			}
		}
		return super.onEnterZone(character, zone);
	}
	
	private static class PailakaDrop
	{
		private final int _itemId;
		private final int _chance;
		
		public PailakaDrop(int itemId, int chance)
		{
			_itemId = itemId;
			_chance = chance;
		}
		
		public int getItemID()
		{
			return _itemId;
		}
		
		public int getChance()
		{
			return _chance;
		}
	}
	
	private static final void dropHerb(L2Npc mob, L2PcInstance player, int[][] drop)
	{
		final int chance = Rnd.get(100);
		for (int[] element : drop)
		{
			if (chance < element[2])
			{
				((L2MonsterInstance) mob).dropItem(player, element[0], element[1]);
				return;
			}
		}
	}
}
