/*
 * Copyright (C) 2004-2015 L2J DataPack
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
package instances.IceQueensCastleUltimateBattle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import l2r.Config;
import l2r.gameserver.GeoData;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.MountType;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.L2CommandChannel;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2GrandBossInstance;
import l2r.gameserver.model.actor.instance.L2NpcInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2QuestGuardInstance;
import l2r.gameserver.model.actor.instance.L2RaidBossInstance;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.variables.NpcVariables;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.ExChangeClientEffectInfo;
import l2r.gameserver.network.serverpackets.ExSendUIEvent;
import l2r.gameserver.network.serverpackets.ExStartScenePlayer;
import l2r.gameserver.network.serverpackets.OnEventTrigger;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.taskmanager.DecayTaskManager;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

import instances.AbstractInstance;
import quests.Q10286_ReunionWithSirra.Q10286_ReunionWithSirra;

/**
 * Ice Queen's Castle (Ultimate Battle) instance zone.
 * @author vGodFather
 */
public final class IceQueensCastleUltimateBattle extends AbstractInstance
{
	protected class IQCUBWorld extends InstanceWorld
	{
		protected List<L2PcInstance> playersInside = new ArrayList<>();
		protected List<L2Npc> knightStatues = new ArrayList<>();
		protected List<L2Attackable> spawnedMobs = new CopyOnWriteArrayList<>();
		protected L2NpcInstance controller = null;
		protected L2GrandBossInstance freya = null;
		protected L2QuestGuardInstance supp_Jinia = null;
		protected L2QuestGuardInstance supp_Kegor = null;
		protected boolean isSupportActive = false;
		protected boolean canSpawnMobs = true;
	}
	
	// Npcs
	private static final int FREYA_THRONE = 29177; // First freya
	private static final int FREYA_SPELLING = 29178; // Second freya
	private static final int FREYA_STAND = 29180; // Last freya - HARD
	private static final int INVISIBLE_NPC = 18919;
	private static final int KNIGHT = 18856; // Archery Knight - HARD
	private static final int GLACIER = 18853; // Glacier
	private static final int BREATH = 18854; // Archer's Breath
	private static final int GLAKIAS = 25700; // Glakias (Archery Knight Captain) - HARD
	private static final int SIRRA = 32762; // Sirra
	private static final int JINIA = 32781; // Jinia
	private static final int SUPP_JINIA = 18850; // Jinia
	private static final int SUPP_KEGOR = 18851; // Kegor
	// Skills
	private static final SkillHolder BLIZZARD = new SkillHolder(6275, 1); // Eternal Blizzard
	private static final SkillHolder ETERNAL_BLIZZARD_FORCE = new SkillHolder(6697, 1);
	private static final SkillHolder BLIZZARD_BREATH = new SkillHolder(6299, 1); // Breath of Ice Palace - Ice Storm
	private static final SkillHolder SUICIDE_BREATH = new SkillHolder(6300, 1); // Self-Destruction
	private static final SkillHolder JINIA_SUPPORT = new SkillHolder(6288, 1); // Jinia's Prayer
	private static final SkillHolder KEGOR_SUPPORT = new SkillHolder(6289, 1); // Kegor's Courage
	private static final SkillHolder ICE_STONE = new SkillHolder(6301, 1); // Cold Mana's Fragment
	private static final SkillHolder CANCEL = new SkillHolder(4618, 1); // NPC Cancel PC Target
	private static final SkillHolder POWER_STRIKE = new SkillHolder(6293, 1); // Power Strike
	private static final SkillHolder POINT_TARGET = new SkillHolder(6295, 1); // Point Target
	private static final SkillHolder CYLINDER_THROW = new SkillHolder(6297, 1); // Cylinder Throw
	private static final SkillHolder SelfRangeBuff = new SkillHolder(6294, 1); // Leader's Roar
	private static final SkillHolder LEADER_RUSH = new SkillHolder(6296, 1); // Rush
	private static final SkillHolder ANTI_STRIDER = new SkillHolder(4258, 1); // Hinder Strider
	private static final SkillHolder ICE_BALL = new SkillHolder(6278, 1); // Ice Ball
	private static final SkillHolder SUMMON_ELEMENTAL = new SkillHolder(6277, 1); // Summon Spirits
	private static final SkillHolder SELF_NOVA = new SkillHolder(6279, 1); // Attack Nearby Range
	private static final SkillHolder REFLECT_MAGIC = new SkillHolder(6282, 1); // Reflect Magic
	private static final SkillHolder RAGE_OF_ICE = new SkillHolder(6285, 1); // Rage of Ice
	private static final SkillHolder FREYAS_BLESS = new SkillHolder(6284, 1); // Freya's Bless
	// Locations
	private static final Location FREYA_SPAWN = new Location(114720, -117085, -11088, 15956);
	private static final Location FREYA_SPELLING_SPAWN = new Location(114723, -117502, -10672, 15956);
	private static final Location FREYA_CORPSE = new Location(114767, -114795, -11200, 0);
	private static final Location MIDDLE_POINT = new Location(114730, -114805, -11200);
	private static final Location KEGOR_FINISH = new Location(114659, -114796, -11205);
	private static final Location GLAKIAS_SPAWN = new Location(114707, -114799, -11199, 15956);
	private static final Location SUPP_JINIA_SPAWN = new Location(114751, -114781, -11205);
	private static final Location SUPP_KEGOR_SPAWN = new Location(114659, -114796, -11205);
	private static final Location BATTLE_PORT = new Location(114694, -113700, -11200);
	private static final Location CONTROLLER_LOC = new Location(114394, -112383, -11200);
	private static final Location[] ENTER_LOC =
	{
		new Location(114185, -112435, -11210),
		new Location(114183, -112280, -11210),
		new Location(114024, -112435, -11210),
		new Location(114024, -112278, -11210),
		new Location(113865, -112435, -11210),
		new Location(113865, -112276, -11210),
		
	};
	private static final Location[] STATUES_LOC =
	{
		new Location(113845, -116091, -11168, 8264),
		new Location(113381, -115622, -11168, 8264),
		new Location(113380, -113978, -11168, -8224),
		new Location(113845, -113518, -11168, -8224),
		new Location(115591, -113516, -11168, -24504),
		new Location(116053, -113981, -11168, -24504),
		new Location(116061, -115611, -11168, 24804),
		new Location(115597, -116080, -11168, 24804),
		new Location(112942, -115480, -10960, 52),
		new Location(112940, -115146, -10960, 52),
		new Location(112945, -114453, -10960, 52),
		new Location(112945, -114123, -10960, 52),
		new Location(116497, -114117, -10960, 32724),
		new Location(116499, -114454, -10960, 32724),
		new Location(116501, -115145, -10960, 32724),
		new Location(116502, -115473, -10960, 32724),
	};
	private static Location[] KNIGHTS_LOC =
	{
		new Location(114502, -115315, -11205, 15451),
		new Location(114937, -115323, -11205, 18106),
		new Location(114722, -115185, -11205, 16437),
	};
	// Misc
	private static final int MAX_PLAYERS = Config.MAX_PLAYERS_TO_HARD;
	private static final int MIN_PLAYERS = Config.MIN_PLAYERS_TO_HARD;
	private static final int MIN_LEVEL = Config.MIN_PLAYER_LEVEL_TO_HARD;
	private static final int TEMPLATE_ID = 144; // Ice Queen's Castle
	private static final int DOOR_ID = 23140101;
	
	//@formatter:off
	private static final int[] _eventTriggers =
	{
		23140202, 23140204, 23140206, 23140208, 23140212, 23140214, 23140216 };
	
	private static final int[] _zoneSkill =
	{
		// ID, LVL
		6437, 7
	};
	//@formatter:on
	
	public IceQueensCastleUltimateBattle()
	{
		super(IceQueensCastleUltimateBattle.class.getSimpleName());
		addStartNpc(SIRRA, SUPP_KEGOR, SUPP_JINIA);
		addFirstTalkId(SUPP_KEGOR, SUPP_JINIA);
		addTalkId(SIRRA, JINIA, SUPP_KEGOR);
		addAttackId(FREYA_THRONE, FREYA_STAND, GLAKIAS, GLACIER, BREATH, KNIGHT);
		addKillId(GLAKIAS, FREYA_STAND, KNIGHT, GLACIER, BREATH);
		addSpawnId(GLAKIAS, FREYA_STAND, KNIGHT, GLACIER, BREATH);
		addSpellFinishedId(GLACIER, BREATH);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("enter"))
		{
			enterInstance(player, new IQCUBWorld(), "IceQueensCastleUltimateBattle.xml", TEMPLATE_ID);
		}
		else
		{
			final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			
			if ((tmpworld != null) && (tmpworld instanceof IQCUBWorld))
			{
				final IQCUBWorld world = (IQCUBWorld) tmpworld;
				switch (event)
				{
					case "openDoor":
					{
						if (npc.isScriptValue(0))
						{
							npc.setScriptValue(1);
							openDoor(DOOR_ID, world.getInstanceId());
							world.controller = (L2NpcInstance) addSpawn(INVISIBLE_NPC, CONTROLLER_LOC, false, 0, true, world.getInstanceId());
							for (Location loc : STATUES_LOC)
							{
								if (loc.getZ() == -11168)
								{
									final L2Npc statue = addSpawn(INVISIBLE_NPC, loc, false, 0, false, world.getInstanceId());
									world.knightStatues.add(statue);
								}
							}
							
							startQuestTimer("STAGE_1_MOVIE", 60000, world.controller, null);
						}
						break;
					}
					case "portInside":
					{
						teleportPlayer(player, BATTLE_PORT, world.getInstanceId());
						break;
					}
					case "killFreya":
					{
						world.supp_Kegor.deleteMe();
						world.freya.decayMe();
						manageMovie(world, ExStartScenePlayer.SCENE_BOSS_FREYA_ENDING_B);
						cancelQuestTimer("FINISH_WORLD", world.controller, null);
						startQuestTimer("FINISH_WORLD", 58500, world.controller, null);
						break;
					}
					case "18851-01.html":
					{
						return event;
					}
					case "STAGE_1_MOVIE":
					{
						closeDoor(DOOR_ID, world.getInstanceId());
						world.setStatus(1);
						manageMovie(world, ExStartScenePlayer.SCENE_BOSS_FREYA_OPENING);
						startQuestTimer("STAGE_1_START", 53500, world.controller, null);
						break;
					}
					case "STAGE_1_START":
					{
						world.freya = (L2GrandBossInstance) addSpawn(FREYA_THRONE, FREYA_SPAWN, false, 0, true, world.getInstanceId());
						world.freya.setIsMortal(false);
						manageScreenMsg(world, NpcStringId.BEGIN_STAGE_1_FREYA);
						startQuestTimer("CAST_BLIZZARD", 50000, world.controller, null);
						startQuestTimer("STAGE_1_SPAWN", 2000, world.freya, null);
						startQuestTimer("MANAGE_ZONE_SKILL", 9000, world.controller, null, true);
						break;
					}
					case "STAGE_1_SPAWN":
					{
						notifyEvent("START_SPAWN", world.controller, null);
						break;
					}
					case "STAGE_1_FINISH":
					{
						if (world.freya != null)
						{
							world.freya.deleteMe();
							world.freya = null;
						}
						manageDespawnMinions(world);
						manageMovie(world, ExStartScenePlayer.SCENE_BOSS_FREYA_PHASE_A);
						startQuestTimer("STAGE_1_PAUSE", 24100 - 1000, world.controller, null);
						cancelQuestTimer("MANAGE_ZONE_SKILL", world.controller, null);
						break;
					}
					case "STAGE_1_PAUSE":
					{
						world.freya = (L2GrandBossInstance) addSpawn(FREYA_SPELLING, FREYA_SPELLING_SPAWN, false, 0, true, world.getInstanceId());
						world.freya.setIsInvul(true);
						world.freya.disableCoreAI(true);
						manageTimer(world, 60, NpcStringId.TIME_REMAINING_UNTIL_NEXT_BATTLE);
						world.setStatus(2);
						startQuestTimer("STAGE_2_START", 60000, world.controller, null);
						break;
					}
					case "STAGE_2_START":
					{
						world.canSpawnMobs = true;
						notifyEvent("START_SPAWN", world.controller, null);
						manageScreenMsg(world, NpcStringId.BEGIN_STAGE_2_FREYA);
						
						startQuestTimer("STAGE_2_FAILED", 360000, world.controller, null);
						manageTimer(world, 360, NpcStringId.BATTLE_END_LIMIT_TIME);
						world.controller.getVariables().set("TIMER_END", System.currentTimeMillis() + 360000);
						break;
					}
					case "STAGE_2_FAILED":
					{
						manageMovie(world, ExStartScenePlayer.SCENE_BOSS_FREYA_DEFEAT);
						startQuestTimer("STAGE_2_FAILED2", 22000, npc, null);
						break;
					}
					case "STAGE_2_FAILED2":
					{
						InstanceManager.getInstance().destroyInstance(world.getInstanceId());
						break;
					}
					case "STAGE_2_MOVIE":
					{
						manageMovie(world, ExStartScenePlayer.SCENE_ICE_HEAVYKNIGHT_SPAWN);
						startQuestTimer("STAGE_2_GLAKIAS", 7000, world.controller, null);
						break;
					}
					case "STAGE_2_GLAKIAS":
					{
						for (Location loc : STATUES_LOC)
						{
							if (loc.getZ() == -10960)
							{
								final L2Npc statue = addSpawn(INVISIBLE_NPC, loc, false, 0, false, world.getInstanceId());
								world.knightStatues.add(statue);
								startQuestTimer("SPAWN_KNIGHT", 5000, statue, null);
							}
						}
						final L2RaidBossInstance glakias = (L2RaidBossInstance) addSpawn(GLAKIAS, GLAKIAS_SPAWN, false, 0, true, world.getInstanceId());
						startQuestTimer("LEADER_DELAY", 5000, glakias, null);
						startQuestTimer("SHOW_GLAKIAS_TIMER", 3000, world.controller, null);
						startQuestTimer("MANAGE_ZONE_SKILL", 9000, world.controller, null, true);
						break;
					}
					case "STAGE_3_MOVIE":
					{
						manageStorm(world, true);
						manageMovie(world, ExStartScenePlayer.SCENE_BOSS_FREYA_PHASE_B);
						startQuestTimer("STAGE_3_START", 21500, world.controller, null);
						break;
					}
					case "STAGE_3_START":
					{
						world.setStatus(4);
						world.freya.deleteMe();
						world.canSpawnMobs = true;
						world.freya = (L2GrandBossInstance) addSpawn(FREYA_STAND, FREYA_SPAWN, false, 0, true, world.getInstanceId());
						world.controller.getVariables().set("FREYA_MOVE", 0);
						world.playersInside.stream().filter(p -> (p != null) && (p.getInstanceId() == world.getInstanceId())).forEach(p -> p.broadcastPacket(ExChangeClientEffectInfo.STATIC_FREYA_DESTROYED));
						notifyEvent("START_SPAWN", world.controller, null);
						startQuestTimer("START_MOVE", 10000, world.controller, null);
						startQuestTimer("CAST_BLIZZARD", 50000, world.controller, null);
						manageScreenMsg(world, NpcStringId.BEGIN_STAGE_3_FREYA);
						
						world.freya.doCast(RAGE_OF_ICE.getSkill());
						startQuestTimer("FREYA_BUFF", 15000, world.controller, null);
						
						startQuestTimer("MANAGE_ZONE_SKILL", 9000, world.controller, null, true);
						break;
					}
					case "FREYA_BUFF":
					{
						world.freya.doCast(FREYAS_BLESS.getSkill());
						startQuestTimer("FREYA_BUFF", 15000, world.controller, null);
						break;
					}
					case "START_MOVE":
					{
						if (npc.getVariables().getInt("FREYA_MOVE") == 0)
						{
							world.controller.getVariables().set("FREYA_MOVE", 1);
							world.freya.setIsRunning(true);
							world.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MIDDLE_POINT);
						}
						break;
					}
					case "CAST_BLIZZARD":
					{
						if (!world.freya.isInvul())
						{
							final int blizzardForceCount = world.controller.getVariables().getInt("BLIZZARD_FORCE_COUNT", 0);
							if ((blizzardForceCount < 4) && (world.freya.getCurrentHp() < (world.freya.getMaxHp() * (0.8 - (0.2 * blizzardForceCount)))))
							{
								world.controller.getVariables().set("BLIZZARD_FORCE_COUNT", blizzardForceCount + 1);
								world.freya.doCast(ETERNAL_BLIZZARD_FORCE.getSkill());
								manageScreenMsg(world, NpcStringId.MAGIC_POWER_SO_STRONG_THAT_IT_COULD_MAKE_YOU_LOSE_YOUR_MIND_CAN_BE_FELT_FROM_SOMEWHERE);
							}
							else
							{
								world.freya.doCast(BLIZZARD.getSkill());
								manageScreenMsg(world, NpcStringId.STRONG_MAGIC_POWER_CAN_BE_FELT_FROM_SOMEWHERE);
							}
						}
						
						for (L2Attackable minion : world.spawnedMobs)
						{
							if ((minion != null) && !minion.isDead() && !minion.isInCombat())
							{
								manageRandomAttack(world, minion);
							}
						}
						startQuestTimer("CAST_BLIZZARD", getRandom(55, 60) * 1000, world.controller, null);
						break;
					}
					case "SPAWN_SUPPORT":
					{
						for (L2PcInstance players : world.playersInside)
						{
							if (players != null)
							{
								players.setIsInvul(false);
							}
						}
						world.freya.setIsInvul(false);
						world.freya.disableCoreAI(false);
						manageScreenMsg(world, NpcStringId.BEGIN_STAGE_4_FREYA);
						world.supp_Jinia = (L2QuestGuardInstance) addSpawn(SUPP_JINIA, SUPP_JINIA_SPAWN, false, 0, true, world.getInstanceId());
						world.supp_Jinia.setIsRunning(true);
						world.supp_Jinia.setIsInvul(true);
						world.supp_Jinia.setCanReturnToSpawnPoint(false);
						world.supp_Kegor = (L2QuestGuardInstance) addSpawn(SUPP_KEGOR, SUPP_KEGOR_SPAWN, false, 0, true, world.getInstanceId());
						world.supp_Kegor.setIsRunning(true);
						world.supp_Kegor.setIsInvul(true);
						world.supp_Kegor.setCanReturnToSpawnPoint(false);
						startQuestTimer("ATTACK_FREYA", 5000, world.supp_Jinia, null);
						startQuestTimer("ATTACK_FREYA", 5000, world.supp_Kegor, null);
						startQuestTimer("GIVE_SUPPORT", 1000, world.controller, null);
						break;
					}
					case "GIVE_SUPPORT":
					{
						if (world.isSupportActive)
						{
							world.supp_Jinia.doCast(JINIA_SUPPORT.getSkill());
							world.supp_Kegor.doCast(KEGOR_SUPPORT.getSkill());
							startQuestTimer("GIVE_SUPPORT", 25000, world.controller, null);
						}
						break;
					}
					case "FINISH_STAGE":
					{
						if (world.supp_Jinia != null)
						{
							world.supp_Jinia.deleteMe();
							world.supp_Jinia = null;
						}
						
						if (world.freya != null)
						{
							world.freya.teleToLocation(FREYA_CORPSE);
						}
						
						if (world.supp_Kegor != null)
						{
							world.supp_Kegor.teleToLocation(KEGOR_FINISH);
						}
						break;
					}
					case "START_SPAWN":
					{
						for (L2Npc statues : world.knightStatues)
						{
							notifyEvent("SPAWN_KNIGHT", statues, null);
						}
						
						for (Location loc : KNIGHTS_LOC)
						{
							final L2Attackable knight = (L2Attackable) addSpawn(KNIGHT, loc, false, 0, false, world.getInstanceId());
							knight.disableCoreAI(true);
							knight.setDisplayEffect(1);
							knight.getSpawn().setLocation(loc);
							manageTargetToAttack(world, knight);
							world.spawnedMobs.add(knight);
							startQuestTimer("ICE_RUPTURE", getRandom(2, 5) * 1000, knight, null);
						}
						
						int maxGlaciers = world.getStatus() + 2;
						for (int i = 0; i < maxGlaciers; i++)
						{
							notifyEvent("SPAWN_GLACIER", world.controller, null);
						}
						break;
					}
					case "SPAWN_KNIGHT":
					{
						if (world.canSpawnMobs)
						{
							final Location loc = new Location(MIDDLE_POINT.getX() + getRandom(-1000, 1000), MIDDLE_POINT.getY() + getRandom(-1000, 1000), MIDDLE_POINT.getZ());
							final L2Attackable knight = (L2Attackable) addSpawn(KNIGHT, npc.getLocation(), false, 0, false, world.getInstanceId());
							knight.getVariables().set("SPAWNED_NPC", npc);
							knight.disableCoreAI(true);
							knight.setIsImmobilized(true);
							knight.setDisplayEffect(1);
							knight.getSpawn().setLocation(loc);
							manageTargetToAttack(world, knight);
							world.spawnedMobs.add(knight);
							startQuestTimer("ICE_RUPTURE", getRandom(5, 10) * 1000, knight, null);
						}
						break;
					}
					case "SPAWN_GLACIER":
					{
						if (world.canSpawnMobs)
						{
							final Location loc = new Location(MIDDLE_POINT.getX() + getRandom(-1000, 1000), MIDDLE_POINT.getY() + getRandom(-1000, 1000), MIDDLE_POINT.getZ());
							final L2Attackable glacier = (L2Attackable) addSpawn(GLACIER, loc, false, 0, false, world.getInstanceId());
							glacier.setDisplayEffect(1);
							glacier.disableCoreAI(true);
							glacier.setIsImmobilized(true);
							world.spawnedMobs.add(glacier);
							startQuestTimer("CHANGE_STATE", 1400, glacier, null);
						}
						break;
					}
					case "ICE_RUPTURE":
					{
						if (npc.isCoreAIDisabled())
						{
							npc.disableCoreAI(false);
							npc.setIsImmobilized(false);
							npc.setDisplayEffect(2);
							manageRandomAttack(world, (L2Attackable) npc);
						}
						break;
					}
					case "FIND_TARGET":
					{
						manageRandomAttack(world, (L2Attackable) npc);
						break;
					}
					case "CHANGE_STATE":
					{
						npc.setDisplayEffect(2);
						startQuestTimer("CAST_SKILL", 20000, npc, null);
						break;
					}
					case "CAST_SKILL":
					{
						if (npc.isScriptValue(0) && !npc.isDead())
						{
							npc.setTarget(npc);
							npc.doCast(ICE_STONE.getSkill());
							npc.setScriptValue(1);
						}
						break;
					}
					case "SUICIDE":
					{
						npc.setDisplayEffect(3);
						npc.setIsMortal(true);
						npc.doDie(null);
						break;
					}
					case "BLIZZARD":
					{
						npc.getVariables().set("SUICIDE_COUNT", npc.getVariables().getInt("SUICIDE_COUNT") + 1);
						
						if (npc.getVariables().getInt("SUICIDE_ON") == 0)
						{
							if (npc.getVariables().getInt("SUICIDE_COUNT") == 2)
							{
								startQuestTimer("ELEMENTAL_SUICIDE", 20000, npc, null);
							}
							else
							{
								if (npc.checkDoCastConditions(BLIZZARD_BREATH.getSkill()) && !npc.isCastingNow())
								{
									npc.setTarget(npc);
									npc.doCast(BLIZZARD_BREATH.getSkill());
								}
								startQuestTimer("BLIZZARD", 20000, npc, null);
							}
						}
						break;
					}
					case "ELEMENTAL_SUICIDE":
					{
						npc.setTarget(npc);
						npc.doCast(SUICIDE_BREATH.getSkill());
						break;
					}
					case "ELEMENTAL_KILLED":
					{
						if (npc.getVariables().getInt("SUICIDE_ON") == 1)
						{
							npc.setTarget(npc);
							npc.doCast(SUICIDE_BREATH.getSkill());
						}
						break;
					}
					case "ATTACK_FREYA":
					{
						try
						{
							final SkillHolder skill = npc.getTemplate().getParameters().getObject("Skill01_ID", SkillHolder.class);
							if (npc.isInsideRadius(world.freya, 100, true, false))
							{
								if (npc.checkDoCastConditions(skill.getSkill()) && !npc.isCastingNow())
								{
									npc.setTarget(world.freya);
									npc.doCast(skill.getSkill());
									startQuestTimer("ATTACK_FREYA", 20000, npc, null);
								}
								else
								{
									startQuestTimer("ATTACK_FREYA", 5000, npc, null);
								}
							}
							else
							{
								npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, world.freya);
								startQuestTimer("ATTACK_FREYA", 5000, npc, null);
							}
						}
						catch (Exception e)
						{
						
						}
						break;
					}
					case "FINISH_WORLD":
					{
						if (world.freya != null)
						{
							world.freya.decayMe();
						}
						
						for (L2PcInstance players : world.playersInside)
						{
							if ((players != null))
							{
								players.broadcastPacket(ExChangeClientEffectInfo.STATIC_FREYA_DEFAULT);
							}
						}
						
						// vGodFather this is already handled
						// InstanceManager.getInstance().destroyInstance(world.getInstanceId());
						break;
					}
					case "LEADER_RANGEBUFF":
					{
						if (npc.checkDoCastConditions(SelfRangeBuff.getSkill()) && !npc.isCastingNow())
						{
							npc.setTarget(npc);
							npc.doCast(SelfRangeBuff.getSkill());
						}
						else
						{
							startQuestTimer("LEADER_RANGEBUFF", 30000, npc, null);
						}
						break;
					}
					case "LEADER_RANDOMIZE":
					{
						final L2Attackable mob = (L2Attackable) npc;
						mob.clearAggroList();
						
						for (L2Character characters : npc.getKnownList().getKnownPlayersInRadius(1000))
						{
							if ((characters != null))
							{
								mob.addDamageHate(characters, 0, getRandom(10000, 20000));
							}
						}
						startQuestTimer("LEADER_RANDOMIZE", 25000, npc, null);
						break;
					}
					case "LEADER_DASH":
					{
						final L2Character mostHated = ((L2Attackable) npc).getMostHated();
						if (getRandomBoolean() && !npc.isCastingNow() && (mostHated != null) && !mostHated.isDead() && (npc.calculateDistance(mostHated, true, false) < 1000))
						{
							npc.setTarget(mostHated);
							npc.doCast(LEADER_RUSH.getSkill());
						}
						startQuestTimer("LEADER_DASH", 10000, npc, null);
						break;
					}
					case "LEADER_DESTROY":
					{
						final L2Attackable mob = (L2Attackable) npc;
						if (npc.getVariables().getInt("OFF_SHOUT") == 0)
						{
							manageScreenMsg(world, NpcStringId.THE_SPACE_FEELS_LIKE_ITS_GRADUALLY_STARTING_TO_SHAKE);
							
							switch (getRandom(4))
							{
								case 0:
								{
									broadcastNpcSay(npc, Say2.SHOUT, NpcStringId.ARCHER_GIVE_YOUR_BREATH_FOR_THE_INTRUDER);
									break;
								}
								case 1:
								{
									broadcastNpcSay(npc, Say2.SHOUT, NpcStringId.MY_KNIGHTS_SHOW_YOUR_LOYALTY);
									break;
								}
								case 2:
								{
									broadcastNpcSay(npc, Say2.SHOUT, NpcStringId.I_CAN_TAKE_IT_NO_LONGER);
									break;
								}
								case 3:
								{
									broadcastNpcSay(npc, Say2.SHOUT, NpcStringId.ARCHER_HEED_MY_CALL);
									for (int i = 0; i < 3; i++)
									{
										manageBreathSpawn(mob.getMostHated(), npc, world);
									}
									break;
								}
							}
						}
						break;
					}
					case "LEADER_DELAY":
					{
						if (npc.getVariables().getInt("DELAY_VAL") == 0)
						{
							npc.getVariables().set("DELAY_VAL", 1);
						}
						break;
					}
					case "SHOW_GLAKIAS_TIMER":
					{
						final int time = (int) ((world.controller.getVariables().getLong("TIMER_END", 0) - System.currentTimeMillis()) / 1000);
						manageTimer(world, time, NpcStringId.BATTLE_END_LIMIT_TIME);
						break;
					}
					case "MANAGE_ZONE_SKILL":
					{
						manageZoneSkill(world);
						break;
					}
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		
		if (tmpworld instanceof IQCUBWorld)
		{
			final IQCUBWorld world = (IQCUBWorld) tmpworld;
			
			if (npc.getId() == SUPP_JINIA)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return null;
			}
			else if (npc.getId() == SUPP_KEGOR)
			{
				if (world.isSupportActive)
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return null;
				}
				return "18851.html";
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, L2Skill skill)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		
		if (tmpworld instanceof IQCUBWorld)
		{
			final IQCUBWorld world = (IQCUBWorld) tmpworld;
			switch (npc.getId())
			{
				case FREYA_THRONE:
				{
					if ((world.controller.getVariables().getInt("FREYA_MOVE") == 0) && world.isStatus(1))
					{
						world.controller.getVariables().set("FREYA_MOVE", 1);
						manageScreenMsg(world, NpcStringId.FREYA_HAS_STARTED_TO_MOVE);
						world.freya.setIsRunning(true);
						world.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MIDDLE_POINT);
					}
					
					if (npc.getCurrentHp() < (npc.getMaxHp() * 0.02))
					{
						notifyEvent("STAGE_1_FINISH", world.controller, null);
						cancelQuestTimer("CAST_BLIZZARD", world.controller, null);
					}
					else
					{
						if ((attacker.getMountType() == MountType.STRIDER) && !attacker.isAffectedBySkill(ANTI_STRIDER.getSkillId()) && !npc.isCastingNow())
						{
							if (!npc.isSkillDisabled(ANTI_STRIDER.getSkill()))
							{
								npc.setTarget(attacker);
								npc.doCast(ANTI_STRIDER.getSkill());
							}
						}
						
						final L2Character mostHated = ((L2Attackable) npc).getMostHated();
						final boolean canReachMostHated = (mostHated != null) && !mostHated.isDead() && (npc.calculateDistance(mostHated, true, false) <= 800);
						
						if (getRandom(10000) < 3333)
						{
							if (getRandomBoolean())
							{
								if ((npc.calculateDistance(attacker, true, false) <= 800) && npc.checkDoCastConditions(ICE_BALL.getSkill()) && !npc.isCastingNow())
								{
									npc.setTarget(attacker);
									npc.doCast(ICE_BALL.getSkill());
								}
							}
							else
							{
								if (canReachMostHated && npc.checkDoCastConditions(ICE_BALL.getSkill()) && !npc.isCastingNow())
								{
									npc.setTarget(mostHated);
									npc.doCast(ICE_BALL.getSkill());
								}
							}
						}
						else if (getRandom(10000) < 800)
						{
							if (getRandomBoolean())
							{
								if ((npc.calculateDistance(attacker, true, false) <= 800) && npc.checkDoCastConditions(SUMMON_ELEMENTAL.getSkill()) && !npc.isCastingNow())
								{
									npc.setTarget(attacker);
									npc.doCast(SUMMON_ELEMENTAL.getSkill());
								}
							}
							else
							{
								if (canReachMostHated && npc.checkDoCastConditions(SUMMON_ELEMENTAL.getSkill()) && !npc.isCastingNow())
								{
									npc.setTarget(mostHated);
									npc.doCast(SUMMON_ELEMENTAL.getSkill());
								}
							}
						}
						else if (getRandom(10000) < 1500)
						{
							if (!npc.isAffectedBySkill(SELF_NOVA.getSkillId()) && npc.checkDoCastConditions(SELF_NOVA.getSkill()) && !npc.isCastingNow())
							{
								npc.setTarget(npc);
								npc.doCast(SELF_NOVA.getSkill());
							}
						}
					}
					break;
				}
				case FREYA_STAND:
				{
					if (world.controller.getVariables().getInt("FREYA_MOVE") == 0)
					{
						world.controller.getVariables().set("FREYA_MOVE", 1);
						world.freya.setIsRunning(true);
						world.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MIDDLE_POINT);
					}
					
					if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.2)) && !world.isSupportActive)
					{
						world.isSupportActive = true;
						world.freya.setIsInvul(true);
						world.freya.disableCoreAI(true);
						for (L2PcInstance players : world.playersInside)
						{
							if (!players.isDead())
							{
								players.setIsInvul(true);
								players.abortAttack();
							}
						}
						manageMovie(world, ExStartScenePlayer.SCENE_BOSS_KEGOR_INTRUSION);
						startQuestTimer("SPAWN_SUPPORT", 27000, world.controller, null);
					}
					
					if ((attacker.getMountType() == MountType.STRIDER) && !attacker.isAffectedBySkill(ANTI_STRIDER.getSkillId()) && !npc.isCastingNow())
					{
						if (!npc.isSkillDisabled(ANTI_STRIDER.getSkill()))
						{
							npc.setTarget(attacker);
							npc.doCast(ANTI_STRIDER.getSkill());
						}
					}
					
					final L2Character mostHated = ((L2Attackable) npc).getMostHated();
					final boolean canReachMostHated = (mostHated != null) && !mostHated.isDead() && (npc.calculateDistance(mostHated, true, false) <= 800);
					
					if (getRandom(10000) < 3333)
					{
						if (getRandomBoolean())
						{
							if ((npc.calculateDistance(attacker, true, false) <= 800) && npc.checkDoCastConditions(ICE_BALL.getSkill()) && !npc.isCastingNow())
							{
								npc.setTarget(attacker);
								npc.doCast(ICE_BALL.getSkill());
							}
						}
						else
						{
							if (canReachMostHated && npc.checkDoCastConditions(ICE_BALL.getSkill()) && !npc.isCastingNow())
							{
								npc.setTarget(mostHated);
								npc.doCast(ICE_BALL.getSkill());
							}
						}
					}
					else if (getRandom(10000) < 1333)
					{
						if (getRandomBoolean())
						{
							if ((npc.calculateDistance(attacker, true, false) <= 800) && npc.checkDoCastConditions(SUMMON_ELEMENTAL.getSkill()) && !npc.isCastingNow())
							{
								npc.setTarget(attacker);
								npc.doCast(SUMMON_ELEMENTAL.getSkill());
							}
						}
						else
						{
							if (canReachMostHated && npc.checkDoCastConditions(SUMMON_ELEMENTAL.getSkill()) && !npc.isCastingNow())
							{
								npc.setTarget(mostHated);
								npc.doCast(SUMMON_ELEMENTAL.getSkill());
							}
						}
					}
					else if (getRandom(10000) < 1500)
					{
						if (!npc.isAffectedBySkill(SELF_NOVA.getSkillId()) && npc.checkDoCastConditions(SELF_NOVA.getSkill()) && !npc.isCastingNow())
						{
							npc.setTarget(npc);
							npc.doCast(SELF_NOVA.getSkill());
						}
					}
					else if (getRandom(10000) < 1333)
					{
						if (!npc.isAffectedBySkill(REFLECT_MAGIC.getSkillId()) && npc.checkDoCastConditions(REFLECT_MAGIC.getSkill()) && !npc.isCastingNow())
						{
							npc.setTarget(npc);
							npc.doCast(REFLECT_MAGIC.getSkill());
						}
					}
					break;
				}
				case GLACIER:
				{
					if (npc.isScriptValue(0) && (npc.getCurrentHp() < (npc.getMaxHp() * 0.5)))
					{
						npc.setTarget(attacker);
						npc.doCast(ICE_STONE.getSkill());
						npc.setScriptValue(1);
					}
					break;
				}
				case BREATH:
				{
					if ((npc.getCurrentHp() < (npc.getMaxHp() / 20)) && (npc.getVariables().getInt("SUICIDE_ON", 0) == 0))
					{
						npc.getVariables().set("SUICIDE_ON", 1);
						startQuestTimer("ELEMENTAL_KILLED", 1000, npc, null);
					}
					break;
				}
				case KNIGHT:
				{
					if (npc.isCoreAIDisabled())
					{
						manageRandomAttack(world, (L2Attackable) npc);
						npc.disableCoreAI(false);
						npc.setIsImmobilized(false);
						npc.setDisplayEffect(2);
						cancelQuestTimer("ICE_RUPTURE", npc, null);
					}
					break;
				}
				case GLAKIAS:
				{
					if (npc.getCurrentHp() < (npc.getMaxHp() * 0.02))
					{
						if (npc.getVariables().getInt("OFF_SHOUT") == 0)
						{
							npc.getVariables().set("OFF_SHOUT", 1);
							npc.getVariables().set("DELAY_VAL", 2);
							npc.setTarget(attacker);
							npc.doCast(CANCEL.getSkill());
						}
						else if (npc.getVariables().getInt("OFF_SHOUT") == 1)
						{
							npc.setTarget(attacker);
							npc.doCast(CANCEL.getSkill());
						}
					}
					else if ((npc.getVariables().getInt("OFF_SHOUT") == 0) && (npc.getVariables().getInt("DELAY_VAL") == 1))
					{
						final L2Character mostHated = ((L2Attackable) npc).getMostHated();
						final boolean canReachMostHated = (mostHated != null) && !mostHated.isDead() && (npc.calculateDistance(mostHated, true, false) < 1000);
						
						if (npc.getVariables().getInt("TIMER_ON") == 0)
						{
							npc.getVariables().set("TIMER_ON", 1);
							startQuestTimer("LEADER_RANGEBUFF", getRandom(5, 30) * 1000, npc, null);
							startQuestTimer("LEADER_RANDOMIZE", 25000, npc, null);
							startQuestTimer("LEADER_DASH", 5000, npc, null);
							startQuestTimer("LEADER_DESTROY", 60000, npc, null);
						}
						
						if (getRandom(10000) < 2500)
						{
							if (getRandom(10000) < 2500)
							{
								if (npc.checkDoCastConditions(POWER_STRIKE.getSkill()) && !npc.isCastingNow())
								{
									npc.setTarget(attacker);
									npc.doCast(POWER_STRIKE.getSkill());
								}
							}
							else if (npc.checkDoCastConditions(POWER_STRIKE.getSkill()) && !npc.isCastingNow() && canReachMostHated)
							{
								npc.setTarget(((L2Attackable) npc).getMostHated());
								npc.doCast(POWER_STRIKE.getSkill());
							}
						}
						else if (getRandom(10000) < 1500)
						{
							if (getRandomBoolean())
							{
								if (npc.checkDoCastConditions(POINT_TARGET.getSkill()) && !npc.isCastingNow())
								{
									npc.setTarget(attacker);
									npc.doCast(POINT_TARGET.getSkill());
								}
							}
							else if (npc.checkDoCastConditions(POINT_TARGET.getSkill()) && !npc.isCastingNow() && canReachMostHated)
							{
								npc.setTarget(((L2Attackable) npc).getMostHated());
								npc.doCast(POINT_TARGET.getSkill());
							}
						}
						else if (getRandom(10000) < 1500)
						{
							if (getRandomBoolean())
							{
								if (npc.checkDoCastConditions(CYLINDER_THROW.getSkill()) && !npc.isCastingNow())
								{
									npc.setTarget(attacker);
									npc.doCast(CYLINDER_THROW.getSkill());
								}
							}
							else if (npc.checkDoCastConditions(CYLINDER_THROW.getSkill()) && !npc.isCastingNow() && canReachMostHated)
							{
								npc.setTarget(((L2Attackable) npc).getMostHated());
								npc.doCast(CYLINDER_THROW.getSkill());
							}
						}
					}
					break;
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		
		if (tmpworld instanceof IQCUBWorld)
		{
			final IQCUBWorld world = (IQCUBWorld) tmpworld;
			
			switch (npc.getId())
			{
				case GLACIER:
				{
					if (skill == ICE_STONE.getSkill())
					{
						if (getRandom(100) < 75)
						{
							manageBreathSpawn(player, npc, world);
						}
						notifyEvent("SUICIDE", npc, null);
					}
					break;
				}
				case BREATH:
				{
					if (skill == SUICIDE_BREATH.getSkill())
					{
						npc.doDie(null);
					}
					break;
				}
			}
		}
		return super.onSpellFinished(npc, player, skill);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		
		if (tmpworld instanceof IQCUBWorld)
		{
			final IQCUBWorld world = (IQCUBWorld) tmpworld;
			switch (npc.getId())
			{
				case GLAKIAS:
				{
					manageDespawnMinions(world);
					manageTimer(world, 60, NpcStringId.TIME_REMAINING_UNTIL_NEXT_BATTLE);
					cancelQuestTimer("STAGE_2_FAILED", world.controller, null);
					startQuestTimer("STAGE_3_MOVIE", 60000, world.controller, null);
					cancelQuestTimer("MANAGE_ZONE_SKILL", world.controller, null);
					break;
				}
				case FREYA_STAND:
				{
					world.isSupportActive = false;
					manageMovie(world, ExStartScenePlayer.SCENE_BOSS_FREYA_ENDING_A);
					manageDespawnMinions(world);
					finishInstance(world);
					DecayTaskManager.getInstance().cancel(world.freya);
					cancelQuestTimer("ATTACK_FREYA", world.supp_Jinia, null);
					cancelQuestTimer("ATTACK_FREYA", world.supp_Kegor, null);
					cancelQuestTimer("GIVE_SUPPORT", world.controller, null);
					cancelQuestTimer("CAST_BLIZZARD", world.controller, null);
					cancelQuestTimer("FREYA_BUFF", world.controller, null);
					cancelQuestTimer("MANAGE_ZONE_SKILL", world.controller, null);
					startQuestTimer("FINISH_STAGE", 16000, world.controller, null);
					startQuestTimer("FINISH_WORLD", 300000, world.controller, null);
					break;
				}
				case KNIGHT:
				{
					final L2Npc spawnedBy = npc.getVariables().getObject("SPAWNED_NPC", L2Npc.class);
					final NpcVariables var = world.controller.getVariables();
					int knightCount = var.getInt("KNIGHT_COUNT");
					
					if ((var.getInt("FREYA_MOVE") == 0) && world.isStatus(1))
					{
						var.set("FREYA_MOVE", 1);
						manageScreenMsg(world, NpcStringId.FREYA_HAS_STARTED_TO_MOVE);
						world.freya.setIsRunning(true);
						world.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MIDDLE_POINT);
					}
					
					if ((knightCount < 10) && (world.isStatus(2)))
					{
						knightCount++;
						var.set("KNIGHT_COUNT", knightCount);
						
						if (knightCount == 10)
						{
							notifyEvent("STAGE_2_MOVIE", world.controller, null);
							world.setStatus(3);
						}
					}
					
					if (spawnedBy != null)
					{
						startQuestTimer("SPAWN_KNIGHT", getRandom(30, 60) * 1000, spawnedBy, null);
					}
					world.spawnedMobs.remove(npc);
					break;
				}
				case GLACIER:
				{
					startQuestTimer("SPAWN_GLACIER", getRandom(30, 60) * 1000, world.controller, null);
					npc.setDisplayEffect(3);
					world.spawnedMobs.remove(npc);
					
					manageBreathSpawn(killer, npc, world);
					break;
				}
				case BREATH:
				{
					world.spawnedMobs.remove(npc);
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public void onEnterInstance(L2PcInstance player, InstanceWorld world, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			if (!player.isInParty())
			{
				managePlayerEnter(player, (IQCUBWorld) world);
			}
			else if (player.getParty().isInCommandChannel())
			{
				for (L2PcInstance players : player.getParty().getCommandChannel().getMembers())
				{
					managePlayerEnter(players, (IQCUBWorld) world);
				}
			}
			else
			{
				for (L2PcInstance players : player.getParty().getMembers())
				{
					managePlayerEnter(players, (IQCUBWorld) world);
				}
			}
		}
		else
		{
			teleportPlayer(player, world.isStatus(4) ? BATTLE_PORT : ENTER_LOC[getRandom(ENTER_LOC.length)], world.getInstanceId());
		}
	}
	
	private void managePlayerEnter(L2PcInstance player, IQCUBWorld world)
	{
		world.playersInside.add(player);
		world.addAllowed(player.getObjectId());
		teleportPlayer(player, ENTER_LOC[getRandom(ENTER_LOC.length)], world.getInstanceId(), false);
	}
	
	@Override
	protected boolean checkConditions(L2PcInstance player)
	{
		final L2Party party = player.getParty();
		final L2CommandChannel channel = party != null ? party.getCommandChannel() : null;
		
		if (player.isGM() && player.canOverrideCond(PcCondOverride.INSTANCE_CONDITIONS))
		{
			return true;
		}
		
		if (party == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		else if (channel == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
			return false;
		}
		else if (player != channel.getLeader())
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		else if ((channel.getMemberCount() < MIN_PLAYERS) || (channel.getMemberCount() > MAX_PLAYERS))
		{
			player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}
		for (L2PcInstance channelMember : channel.getMembers())
		{
			QuestState st = channelMember.getQuestState(Q10286_ReunionWithSirra.class.getSimpleName());
			if ((st == null) || !st.isCompleted())
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED);
				sm.addPcName(channelMember);
				player.getParty().getCommandChannel().broadcastPacket(sm);
				return false;
			}
			
			if (channelMember.getLevel() < MIN_LEVEL)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED);
				sm.addPcName(channelMember);
				party.broadcastPacket(sm);
				return false;
			}
			else if (!Util.checkIfInRange(1000, player, channelMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_LOCATION_WHICH_CANNOT_BE_ENTERED_THEREFORE_IT_CANNOT_BE_PROCESSED);
				sm.addPcName(channelMember);
				party.broadcastPacket(sm);
				return false;
			}
			else if (System.currentTimeMillis() < InstanceManager.getInstance().getInstanceTime(channelMember.getObjectId(), TEMPLATE_ID))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_RE_ENTER_YET);
				sm.addPcName(channelMember);
				party.broadcastPacket(sm);
				return false;
			}
		}
		return true;
	}
	
	private void manageRandomAttack(IQCUBWorld world, L2Attackable mob)
	{
		final List<L2PcInstance> players = new ArrayList<>();
		for (L2PcInstance player : world.playersInside)
		{
			if ((player != null) && !player.isDead() && GeoData.getInstance().canMove(mob, player) && (player.getInstanceId() == world.getInstanceId()) && !player.isInvisible())
			{
				players.add(player);
			}
		}
		
		Collections.shuffle(players);
		final L2PcInstance target = (!players.isEmpty()) ? players.get(0) : null;
		if (target != null)
		{
			mob.addDamageHate(target, 0, 999);
			mob.setIsRunning(true);
			mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
		else
		{
			startQuestTimer("FIND_TARGET", 10000, mob, null);
		}
	}
	
	private void manageDespawnMinions(IQCUBWorld world)
	{
		world.canSpawnMobs = false;
		for (L2Attackable mobs : world.spawnedMobs)
		{
			if ((mobs != null) && !mobs.isDead())
			{
				mobs.doDie(null);
			}
		}
	}
	
	private void manageTimer(IQCUBWorld world, int time, NpcStringId npcStringId)
	{
		for (L2PcInstance players : world.playersInside)
		{
			if ((players != null) && (players.getInstanceId() == world.getInstanceId()))
			{
				players.sendPacket(new ExSendUIEvent(players, false, false, time, 0, npcStringId));
			}
		}
	}
	
	private void manageScreenMsg(IQCUBWorld world, NpcStringId stringId)
	{
		for (L2PcInstance players : world.playersInside)
		{
			if ((players != null) && (players.getInstanceId() == world.getInstanceId()))
			{
				showOnScreenMsg(players, stringId, 2, 6000);
			}
		}
	}
	
	private void manageMovie(IQCUBWorld world, int movie)
	{
		for (L2PcInstance players : world.playersInside)
		{
			if ((players != null) && (players.getInstanceId() == world.getInstanceId()))
			{
				players.showQuestMovie(movie);
			}
		}
	}
	
	private void manageStorm(IQCUBWorld world, boolean active)
	{
		for (L2PcInstance players : world.playersInside)
		{
			if ((players != null) && (players.getInstanceId() == world.getInstanceId()))
			{
				for (int _eventTrigger : _eventTriggers)
				{
					players.sendPacket(new OnEventTrigger(_eventTrigger, active));
				}
			}
		}
	}
	
	private void manageBreathSpawn(L2Character killer, L2Npc npc, IQCUBWorld world)
	{
		final L2Attackable breath = (L2Attackable) addSpawn(BREATH, npc.getLocation(), false, 0, true, world.getInstanceId());
		if (killer != null)
		{
			breath.setIsRunning(true);
			breath.addDamageHate(killer, 0, 999);
			breath.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
		}
		else
		{
			manageRandomAttack(world, breath);
		}
		
		startQuestTimer("BLIZZARD", 20000, breath, null);
		world.spawnedMobs.add(breath);
	}
	
	private void manageZoneSkill(IQCUBWorld world)
	{
		if (world != null)
		{
			// if (world.spawnedMobs.stream().anyMatch(mob -> mob.getId() == GLACIER))
			{
				int skillLvl = Rnd.get(1, _zoneSkill[1]);
				L2Skill skill = SkillData.getInstance().getInfo(_zoneSkill[0], skillLvl);
				for (L2PcInstance player : world.playersInside)
				{
					if ((player != null) && !player.isDead() && !player.isAffectedBySkill(_zoneSkill[0]) && (player.getInstanceId() == world.getInstanceId()))
					{
						skill.getEffects(player, player);
					}
				}
			}
		}
	}
	
	private void manageTargetToAttack(IQCUBWorld world, L2Attackable knight)
	{
		for (L2PcInstance player : world.playersInside)
		{
			if ((player != null) && !player.isDead() && GeoData.getInstance().canSeeTarget(knight, player) && (player.getInstanceId() == world.getInstanceId()))
			{
				knight.addDamageHate(player, 0, 500);
				knight.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
				return;
			}
		}
	}
}
