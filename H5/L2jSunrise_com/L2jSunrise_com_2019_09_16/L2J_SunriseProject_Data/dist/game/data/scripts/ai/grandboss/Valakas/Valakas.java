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
package ai.grandboss.Valakas;

import java.util.Collections;
import java.util.List;

import l2r.Config;
import l2r.gameserver.GeoData;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.MountType;
import l2r.gameserver.enums.audio.Music;
import l2r.gameserver.instancemanager.GrandBossManager;
import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.instance.L2GrandBossInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.quest.QuestTimer;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.zone.type.L2NoRestartZone;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.SocialAction;
import l2r.gameserver.network.serverpackets.SpecialCamera;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;

import ai.npc.AbstractNpcAI;

/**
 * Valakas AI.
 * @author vGodFather
 */
public final class Valakas extends AbstractNpcAI
{
	// NPC
	private static final int VALAKAS = 29028;
	private static final int VOLCANO = 31385;
	private static final int CUBE = 31759;
	// Locations
	private static final Location REST_ROOM = new Location(-105200, -253104, -15264);
	private static final Location ENTRANCE = new Location(204187, -111864, 34);
	private static final Location VALAKAS_SPAWN = new Location(212852, -114842, -1632);
	private static final Location OUT_LOC = new Location(150037, -57720, -2976);
	private static final Location[] CUBE_LOC =
	{
		new Location(214880, -116144, -1644),
		new Location(213696, -116592, -1644),
		new Location(212112, -116688, -1644),
		new Location(211184, -115472, -1664),
		new Location(210336, -114592, -1644),
		new Location(211360, -113904, -1644),
		new Location(213152, -112352, -1644),
		new Location(214032, -113232, -1644),
		new Location(214752, -114592, -1644),
		new Location(209824, -115568, -1421),
		new Location(210528, -112192, -1403),
		new Location(213120, -111136, -1408),
		new Location(215184, -111504, -1392),
		new Location(215456, -117328, -1392),
		new Location(213200, -118160, -1424)
	};
	// Skills
	private static final SkillHolder VALAKAS_REGEN1 = new SkillHolder(4691, 1);
	private static final SkillHolder VALAKAS_REGEN2 = new SkillHolder(4691, 2);
	private static final SkillHolder VALAKAS_REGEN3 = new SkillHolder(4691, 3);
	private static final SkillHolder VALAKAS_REGEN4 = new SkillHolder(4691, 4);
	private static final SkillHolder VALAKAS_REGEN5 = new SkillHolder(4691, 5);
	private static final SkillHolder VALAKAS_LAVA_SKIN = new SkillHolder(4680, 1);
	private static final SkillHolder ANTI_STRIDER = new SkillHolder(4258, 1); // Hinder Strider
	private static final SkillHolder[] VALAKAS_REGULAR_SKILLS =
	{
		new SkillHolder(4681, 1), // Valakas Trample
		new SkillHolder(4682, 1), // Valakas Trample
		new SkillHolder(4683, 1), // Valakas Dragon Breath
		new SkillHolder(4689, 1), // Valakas Fear
	};
	private static final SkillHolder[] VALAKAS_LOWHP_SKILLS =
	{
		new SkillHolder(4681, 1), // Valakas Trample
		new SkillHolder(4682, 1), // Valakas Trample
		new SkillHolder(4683, 1), // Valakas Dragon Breath
		new SkillHolder(4689, 1), // Valakas Fear
		new SkillHolder(4690, 1), // Valakas Meteor Storm
	};
	private static final SkillHolder[] VALAKAS_AOE_SKILLS =
	{
		new SkillHolder(4683, 1), // Valakas Dragon Breath
		new SkillHolder(4684, 1), // Valakas Dragon Breath
		new SkillHolder(4685, 1), // Valakas Tail Stomp
		new SkillHolder(4686, 1), // Valakas Tail Stomp
		new SkillHolder(4688, 1), // Valakas Stun
		new SkillHolder(4689, 1), // Valakas Fear
		new SkillHolder(4690, 1), // Valakas Meteor Storm
	};
	// Status
	private static final int ALIVE = 0;
	private static final int WAITING = 1;
	private static final int IN_FIGHT = 2;
	private static final int DEAD = 3;
	// Zone
	private static final L2NoRestartZone zone = ZoneManager.getInstance().getZoneById(70052, L2NoRestartZone.class); // Valakas Nest zone
	// Misc
	private static final int MAX_PEOPLE = Config.VALAKAS_MAX_PLAYERS; // Max allowed players
	private L2GrandBossInstance _valakas = null;
	private static long _lastAttack = 0;
	private L2Playable _actualVictim; // Actual target of Valakas.
	
	public Valakas()
	{
		super(Valakas.class.getSimpleName(), "ai/grandboss");
		addStartNpc(VOLCANO, CUBE);
		addFirstTalkId(VOLCANO);
		addTalkId(VOLCANO, CUBE);
		addKillId(VALAKAS);
		addAttackId(VALAKAS);
		
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(VALAKAS);
		final int curr_hp = info.getInt("currentHP");
		final int curr_mp = info.getInt("currentMP");
		final int loc_x = info.getInt("loc_x");
		final int loc_y = info.getInt("loc_y");
		final int loc_z = info.getInt("loc_z");
		final int heading = info.getInt("heading");
		final long respawnTime = info.getLong("respawn_time");
		
		switch (getStatus())
		{
			case ALIVE:
			{
				_valakas = (L2GrandBossInstance) addSpawn(VALAKAS, REST_ROOM, false, 0);
				_valakas.setCurrentHpMp(curr_hp, curr_mp);
				addBoss(_valakas);
				break;
			}
			case WAITING:
			{
				_valakas = (L2GrandBossInstance) addSpawn(VALAKAS, REST_ROOM, false, 0);
				_valakas.setCurrentHpMp(curr_hp, curr_mp);
				addBoss(_valakas);
				startQuestTimer("SPAWN_VALAKAS", Config.VALAKAS_WAIT_TIME * 60000, null, null);
				break;
			}
			case IN_FIGHT:
			{
				_valakas = (L2GrandBossInstance) addSpawn(VALAKAS, loc_x, loc_y, loc_z, heading, false, 0);
				_valakas.setCurrentHpMp(curr_hp, curr_mp);
				addBoss(_valakas);
				_lastAttack = System.currentTimeMillis();
				startQuestTimer("CHECK_ATTACK", 60000, _valakas, null);
				startQuestTimer("SPAWN_MINION", 300000, _valakas, null);
				
				QuestTimer manageSkill = getQuestTimer("MANAGE_SKILL", _valakas, null);
				if (manageSkill != null)
				{
					manageSkill.cancel();
				}
				startQuestTimer("MANAGE_SKILL", 2000, _valakas, null, true);
				break;
			}
			case DEAD:
			{
				final long remain = respawnTime - System.currentTimeMillis();
				if (remain > 0)
				{
					startQuestTimer("CLEAR_STATUS", remain, null, null);
				}
				else
				{
					setStatus(ALIVE);
					_valakas = (L2GrandBossInstance) addSpawn(VALAKAS, REST_ROOM, false, 0);
					addBoss(_valakas);
				}
				break;
			}
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "enter":
			{
				String htmltext = null;
				if (getStatus() == DEAD)
				{
					htmltext = "31385-01.html";
				}
				
				else if (getStatus() == IN_FIGHT)
				{
					htmltext = "31385-03.html";
				}
				else if (zone.getPlayersInside().size() >= MAX_PEOPLE)
				{
					htmltext = "31385-04.html";
				}
				else if ((Long.parseLong(player.getVar("valakas_last_enter", "0")) + 3600000) < System.currentTimeMillis())
				{
					htmltext = "31385-05.html";
				}
				else
				{
					if (player.isInParty())
					{
						final L2Party party = player.getParty();
						final boolean isInCC = party.isInCommandChannel();
						final boolean isPartyLeader = (isInCC) ? party.getCommandChannel().isLeader(player) : party.isLeader(player);
						final List<L2PcInstance> members = (isInCC) ? party.getCommandChannel().getMembers() : party.getMembers();
						
						if (isPartyLeader)
						{
							if (members.size() > (MAX_PEOPLE - zone.getPlayersInside().size()))
							{
								htmltext = "31385-04.html";
							}
							else if (members.stream().anyMatch(member -> (member.isInsideRadius(npc, 1000, true, false) && ((Long.parseLong(member.getVar("valakas_last_enter", "0")) + 3600000) < System.currentTimeMillis()))))
							{
								for (L2PcInstance member : members)
								{
									if (member.isInsideRadius(npc, 1000, true, false) && ((Long.parseLong(member.getVar("valakas_last_enter", "0")) + 7200000) < System.currentTimeMillis()))
									{
										party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addPcName(member));
										break;
									}
								}
								
								htmltext = "31385-05.html";
							}
							else
							{
								members.stream().filter(member -> member.isInsideRadius(npc, 1000, true, false)).forEach(member -> member.teleToLocation(ENTRANCE));
								
								if (getStatus() != WAITING)
								{
									setStatus(WAITING);
									startQuestTimer("SPAWN_VALAKAS", Config.VALAKAS_WAIT_TIME * 60000, null, null);
								}
							}
						}
						else
						{
							if (getStatus() != WAITING)
							{
								setStatus(WAITING);
								startQuestTimer("SPAWN_VALAKAS", Config.VALAKAS_WAIT_TIME * 60000, null, null);
							}
							player.teleToLocation(ENTRANCE);
						}
					}
					else
					{
						if (getStatus() != WAITING)
						{
							setStatus(WAITING);
							startQuestTimer("SPAWN_VALAKAS", Config.VALAKAS_WAIT_TIME * 60000, null, null);
						}
						player.teleToLocation(ENTRANCE);
					}
				}
				
				return htmltext;
			}
			case "teleportOut":
			{
				final Location loc = new Location(OUT_LOC.getX() + getRandom(500), OUT_LOC.getY() + getRandom(500), OUT_LOC.getZ());
				player.teleToLocation(loc);
				break;
			}
			case "SPAWN_VALAKAS":
			{
				_valakas.teleToLocation(VALAKAS_SPAWN);
				setStatus(IN_FIGHT);
				_lastAttack = System.currentTimeMillis();
				zone.broadcastPacket(Music.BS03_A_10000.getPacket());
				zone.broadcastPacket(new SocialAction(_valakas.getObjectId(), 3));
				startQuestTimer("CAMERA_1", 1700, _valakas, null);
				break;
			}
			case "CAMERA_1":
			{
				zone.broadcastPacket(new SpecialCamera(_valakas, 1800, 180, -1, 1500, 15000, 10000, 0, 0, 1, 0, 0));
				startQuestTimer("CAMERA_2", 1500, _valakas, null);
				break;
			}
			case "CAMERA_2":
			{
				zone.broadcastPacket(new SpecialCamera(_valakas, 1300, 180, -5, 3000, 15000, 10000, 0, -5, 1, 0, 0));
				startQuestTimer("CAMERA_3", 3300, _valakas, null);
				break;
			}
			case "CAMERA_3":
			{
				zone.broadcastPacket(new SpecialCamera(_valakas, 500, 180, -8, 600, 15000, 10000, 0, 60, 1, 0, 0));
				startQuestTimer("CAMERA_4", 2900, _valakas, null);
				break;
			}
			case "CAMERA_4":
			{
				zone.broadcastPacket(new SpecialCamera(_valakas, 800, 180, -8, 2700, 15000, 10000, 0, 30, 1, 0, 0));
				startQuestTimer("CAMERA_5", 2700, _valakas, null);
				break;
			}
			case "CAMERA_5":
			{
				zone.broadcastPacket(new SpecialCamera(_valakas, 200, 250, 70, 0, 15000, 10000, 30, 80, 1, 0, 0));
				startQuestTimer("CAMERA_6", 1, _valakas, null);
				break;
			}
			case "CAMERA_6":
			{
				zone.broadcastPacket(new SpecialCamera(_valakas, 1100, 250, 70, 2500, 15000, 10000, 30, 80, 1, 0, 0));
				startQuestTimer("CAMERA_7", 3200, _valakas, null);
				break;
			}
			case "CAMERA_7":
			{
				zone.broadcastPacket(new SpecialCamera(_valakas, 700, 150, 30, 0, 15000, 10000, -10, 60, 1, 0, 0));
				startQuestTimer("CAMERA_8", 1400, _valakas, null);
				break;
			}
			case "CAMERA_8":
			{
				zone.broadcastPacket(new SpecialCamera(_valakas, 1200, 150, 20, 2900, 15000, 10000, -10, 30, 1, 0, 0));
				startQuestTimer("CAMERA_9", 6700, _valakas, null);
				break;
			}
			case "CAMERA_9":
			{
				zone.broadcastPacket(new SpecialCamera(_valakas, 750, 170, -10, 3400, 15000, 4000, 10, -15, 1, 0, 0));
				startQuestTimer("START_MOVE", 5700, _valakas, null);
				break;
			}
			case "CAMERA_10":
			{
				zone.broadcastPacket(new SpecialCamera(_valakas, 1100, 210, -5, 3000, 15000, 10000, -13, 0, 1, 1, 0));
				startQuestTimer("CAMERA_11", 3500, _valakas, null);
				break;
			}
			case "CAMERA_11":
			{
				zone.broadcastPacket(new SpecialCamera(_valakas, 1300, 200, -8, 3000, 15000, 10000, 0, 15, 1, 1, 0));
				startQuestTimer("CAMERA_12", 4500, _valakas, null);
				break;
			}
			case "CAMERA_12":
			{
				zone.broadcastPacket(new SpecialCamera(_valakas, 1000, 190, 0, 500, 15000, 10000, 0, 10, 1, 1, 0));
				startQuestTimer("CAMERA_13", 500, _valakas, null);
				break;
			}
			case "CAMERA_13":
			{
				zone.broadcastPacket(new SpecialCamera(_valakas, 1700, 120, 0, 2500, 15000, 10000, 12, 40, 1, 1, 0));
				startQuestTimer("CAMERA_14", 4600, _valakas, null);
				break;
			}
			case "CAMERA_14":
			{
				zone.broadcastPacket(new SpecialCamera(_valakas, 1700, 20, 0, 700, 15000, 10000, 10, 10, 1, 1, 0));
				startQuestTimer("CAMERA_15", 750, _valakas, null);
				break;
			}
			case "CAMERA_15":
			{
				zone.broadcastPacket(new SpecialCamera(_valakas, 1700, 10, 0, 1000, 15000, 10000, 20, 70, 1, 1, 0));
				startQuestTimer("CAMERA_16", 2500, _valakas, null);
				break;
			}
			case "CAMERA_16":
			{
				zone.broadcastPacket(new SpecialCamera(_valakas, 1700, 10, 0, 300, 15000, 250, 20, -20, 1, 1, 0));
				break;
			}
			case "START_MOVE":
			{
				npc.doCast(VALAKAS_REGEN1.getSkill());
				startQuestTimer("CHECK_ATTACK", 60000, npc, null);
				startQuestTimer("SPAWN_MINION", 60000, npc, null);
				
				QuestTimer manageSkill = getQuestTimer("MANAGE_SKILL", _valakas, null);
				if (manageSkill != null)
				{
					manageSkill.cancel();
				}
				startQuestTimer("MANAGE_SKILL", 2000, _valakas, null, true);
				
				for (L2PcInstance players : npc.getKnownList().getKnownPlayersInRadius(4000))
				{
					if (players.isHero())
					{
						zone.broadcastPacket(new ExShowScreenMessage(NpcStringId.S1_YOU_CANNOT_HOPE_TO_DEFEAT_ME_WITH_YOUR_MEAGER_STRENGTH, 2, 4000, players.getName()));
						break;
					}
				}
				
				break;
			}
			case "SET_REGEN":
			{
				if ((((npc.getCurrentHp() / npc.getMaxHp()) * 100) < 75) && (getRandom(150) == 0) && (!npc.isAffectedBySkill(VALAKAS_LAVA_SKIN.getSkillId())))
				{
					npc.doCast(VALAKAS_REGEN5.getSkill());
				}
				
				if (npc.getCurrentHp() < (npc.getMaxHp() * 0.2))
				{
					npc.doCast(VALAKAS_REGEN5.getSkill());
				}
				else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.4))
				{
					npc.doCast(VALAKAS_REGEN4.getSkill());
				}
				else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.6))
				{
					npc.doCast(VALAKAS_REGEN3.getSkill());
				}
				else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.8))
				{
					npc.doCast(VALAKAS_REGEN2.getSkill());
				}
				else
				{
					npc.doCast(VALAKAS_REGEN1.getSkill());
				}
				startQuestTimer("SET_REGEN", 60000, npc, null);
				break;
			}
			case "CHECK_ATTACK":
			{
				if (npc != null)
				{
					if ((_lastAttack + 900000) < System.currentTimeMillis())
					{
						_log.info(getClass().getSimpleName() + ": kicked players using CHECK_ATTACK");
						setStatus(ALIVE);
						for (L2Character charInside : zone.getCharactersInside())
						{
							if (charInside != null)
							{
								if (charInside.isNpc())
								{
									if (charInside.getId() == VALAKAS)
									{
										charInside.teleToLocation(REST_ROOM);
									}
									else
									{
										charInside.deleteMe();
									}
								}
								else if (charInside.isPlayer())
								{
									final Location loc = new Location(OUT_LOC.getX() + getRandom(500), OUT_LOC.getY() + getRandom(500), OUT_LOC.getZ());
									charInside.teleToLocation(loc);
								}
							}
						}
						cancelQuestTimer("CHECK_ATTACK", npc, null);
						cancelQuestTimer("MANAGE_SKILL", npc, null);
					}
					else
					{
						startQuestTimer("CHECK_ATTACK", 60000, npc, null);
					}
				}
				break;
			}
			case "SPAWN_MINION":
			{
				final L2Attackable mob = (L2Attackable) npc;
				if (mob.getAggroList().size() >= 5)
				{
					// TODO: mob.getAggroList().sort(Comparator.comparingInt(AggroInfo::getHate));
				}
				startQuestTimer("SPAWN_MINION", 60000, npc, null);
				break;
			}
			case "CLEAR_STATUS":
			{
				_valakas = (L2GrandBossInstance) addSpawn(VALAKAS, REST_ROOM, false, 0);
				addBoss(_valakas);
				setStatus(ALIVE);
				break;
			}
			case "CLEAR_ZONE":
			{
				_log.info(getClass().getSimpleName() + ": kicked players using CLEAR_ZONE");
				for (L2Character charInside : zone.getCharactersInside())
				{
					if (charInside != null)
					{
						if (charInside.isNpc())
						{
							charInside.deleteMe();
						}
						else if (charInside.isPlayer())
						{
							final Location loc = new Location(OUT_LOC.getX() + getRandom(500), OUT_LOC.getY() + getRandom(500), OUT_LOC.getZ());
							charInside.teleToLocation(loc);
						}
					}
				}
				break;
			}
			case "SKIP_WAITING":
			{
				if (getStatus() == WAITING)
				{
					cancelQuestTimer("SPAWN_VALAKAS", null, null);
					notifyEvent("SPAWN_VALAKAS", null, null);
					player.sendMessage(getClass().getSimpleName() + ": Skipping waiting time ...");
				}
				else
				{
					player.sendMessage(getClass().getSimpleName() + ": You cant skip waiting time right now!");
				}
				break;
			}
			case "RESPAWN_VALAKAS":
			{
				if (getStatus() == DEAD)
				{
					setRespawn(0);
					cancelQuestTimer("CLEAR_STATUS", null, null);
					notifyEvent("CLEAR_STATUS", null, null);
					player.sendMessage(getClass().getSimpleName() + ": Valakas has been respawned.");
				}
				else
				{
					player.sendMessage(getClass().getSimpleName() + ": You cant respawn valakas while valakas is alive!");
				}
				break;
			}
			case "ABORT_FIGHT":
			{
				if (getStatus() == IN_FIGHT)
				{
					_log.info(getClass().getSimpleName() + ": kicked players using ABORT_FIGHT");
					setStatus(ALIVE);
					cancelQuestTimer("CHECK_ATTACK", _valakas, null);
					cancelQuestTimer("SPAWN_MINION", _valakas, null);
					cancelQuestTimer("MANAGE_SKILL", _valakas, null);
					for (L2Character charInside : zone.getCharactersInside())
					{
						if (charInside != null)
						{
							if (charInside.isNpc())
							{
								if (charInside.getId() == VALAKAS)
								{
									charInside.teleToLocation(REST_ROOM);
								}
								else
								{
									charInside.deleteMe();
								}
							}
							else if (charInside.isPlayer() && !charInside.isGM())
							{
								final Location loc = new Location(OUT_LOC.getX() + getRandom(500), OUT_LOC.getY() + getRandom(500), OUT_LOC.getZ());
								charInside.teleToLocation(loc);
							}
						}
					}
					player.sendMessage(getClass().getSimpleName() + ": Fight has been aborted!");
				}
				else
				{
					player.sendMessage(getClass().getSimpleName() + ": You cant abort fight right now!");
				}
				break;
			}
			case "MANAGE_SKILL":
			{
				manageSkills(_valakas);
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		_lastAttack = System.currentTimeMillis();
		
		if ((attacker.getMountType() == MountType.STRIDER) && !attacker.isAffectedBySkill(ANTI_STRIDER.getSkillId()))
		{
			if (!npc.isSkillDisabled(ANTI_STRIDER.getSkill()))
			{
				npc.setTarget(attacker);
				npc.doCast(ANTI_STRIDER.getSkill());
			}
		}
		
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (zone.isCharacterInZone(killer))
		{
			zone.broadcastPacket(new SpecialCamera(_valakas, 2000, 130, -1, 0, 15000, 10000, 0, 0, 1, 1, 0));
			zone.broadcastPacket(Music.B03_D_10000.getPacket());
			startQuestTimer("CAMERA_10", 500, _valakas, null);
			// Calculate Min and Max respawn times randomly.
			long respawnTime = Config.VALAKAS_SPAWN_INTERVAL + getRandom(-Config.VALAKAS_SPAWN_RANDOM, Config.VALAKAS_SPAWN_RANDOM);
			respawnTime *= 3600000;
			setRespawn(respawnTime);
			for (Location loc : CUBE_LOC)
			{
				addSpawn(CUBE, loc, false, 900000);
			}
			startQuestTimer("CLEAR_STATUS", respawnTime, null, null);
			cancelQuestTimer("SET_REGEN", npc, null);
			cancelQuestTimer("CHECK_ATTACK", npc, null);
			cancelQuestTimer("SPAWN_MINION", npc, null);
			cancelQuestTimer("MANAGE_SKILL", npc, null);
			startQuestTimer("CLEAR_ZONE", 900000, null, null);
			setStatus(DEAD);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		cancelQuestTimer("SET_REGEN", npc, null);
		startQuestTimer("SET_REGEN", 60000, npc, null);
		return super.onSpawn(npc);
	}
	
	private SkillHolder getRandomSkill(L2Npc npc)
	{
		final int hpRatio = (int) ((npc.getCurrentHp() / npc.getMaxHp()) * 100);
		
		// Valakas Lava Skin has priority.
		if ((hpRatio < 75) && (getRandom(150) == 0) && !npc.isAffectedBySkill(VALAKAS_LAVA_SKIN.getSkillId()))
		{
			return VALAKAS_LAVA_SKIN;
		}
		
		// Valakas will use mass spells if he feels surrounded.
		if (Util.getPlayersCountInRadius(1200, npc, false, false) >= 20)
		{
			return VALAKAS_AOE_SKILLS[getRandom(VALAKAS_AOE_SKILLS.length)];
		}
		
		if (hpRatio > 50)
		{
			return VALAKAS_REGULAR_SKILLS[getRandom(VALAKAS_REGULAR_SKILLS.length)];
		}
		
		return VALAKAS_LOWHP_SKILLS[getRandom(VALAKAS_LOWHP_SKILLS.length)];
	}
	
	private void manageSkills(L2Npc npc)
	{
		if ((npc == null) || npc.isDead() || npc.isInvul() || npc.isCastingNow())
		{
			return;
		}
		
		if (GrandBossManager.getInstance().getBossStatus(VALAKAS) != IN_FIGHT)
		{
			return;
		}
		
		if ((_actualVictim == null) || _actualVictim.isDead() || !(npc.getKnownList().knowsObject(_actualVictim)) || (getRandom(10) == 0))
		{
			_actualVictim = getRandomTarget(npc);
		}
		
		// If result is still null, Valakas will roam. Don't go deeper in skill AI.
		if (_actualVictim == null)
		{
			if (getRandom(10) == 0)
			{
				int x = npc.getX();
				int y = npc.getY();
				int z = npc.getZ();
				
				int posX = x + getRandom(-1400, 1400);
				int posY = y + getRandom(-1400, 1400);
				
				Location loc = GeoData.getInstance().moveCheck(x, y, z, posX, posY, z, npc.getInstanceId());
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, loc);
			}
			return;
		}
		
		L2Skill skill = getRandomSkill(npc).getSkill();
		int castRange = skill.getCastRange() < 600 ? 600 : skill.getCastRange();
		
		// Cast the skill or follow the target.
		if (Util.checkIfInRange(castRange, npc, _actualVictim, true))
		{
			npc.getAI().stopFollow();
			npc.setTarget(_actualVictim);
			npc.doCast(skill);
		}
		else
		{
			if (!npc.isRunning())
			{
				npc.setIsRunning(true);
			}
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _actualVictim);
		}
	}
	
	private L2Playable getRandomTarget(L2Npc npc)
	{
		List<L2Character> characters = npc.getKnownList().getKnownCharacters();
		Collections.shuffle(characters);
		for (L2Character creature : characters)
		{
			if ((creature != null) && creature.isPlayable() && !creature.isDead() && creature.isVisible())
			{
				return (L2Playable) creature;
			}
		}
		return null;
	}
	
	private int getStatus()
	{
		return GrandBossManager.getInstance().getBossStatus(VALAKAS);
	}
	
	private void addBoss(L2GrandBossInstance grandboss)
	{
		GrandBossManager.getInstance().addBoss(grandboss);
	}
	
	private void setStatus(int status)
	{
		GrandBossManager.getInstance().setBossStatus(VALAKAS, status);
	}
	
	private void setRespawn(long respawnTime)
	{
		GrandBossManager.getInstance().getStatsSet(VALAKAS).set("respawn_time", (System.currentTimeMillis() + respawnTime));
	}
}