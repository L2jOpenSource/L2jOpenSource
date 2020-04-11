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
package gracia.instances.SeedOfDestruction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import l2r.Config;
import l2r.gameserver.GeoData;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.enums.TrapAction;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.instancemanager.SoDManager;
import l2r.gameserver.model.L2CommandChannel;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.L2Territory;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2DoorInstance;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2TrapInstance;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExStartScenePlayer;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;
import l2r.util.data.xml.IXmlReader.IXmlReader;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import ai.npc.AbstractNpcAI;

/**
 * Seed of Destruction instance zone.<br>
 * TODO:
 * <ul>
 * <li>No random mob spawns after mob kill.</li>
 * <li>Implement Seed of Destruction Defense state and one party instances.</li>
 * <li>Use proper zone spawn system.</li>
 * </ul>
 * Please maintain consistency between the Seed scripts.
 * @author Gigiikun
 */
public final class SeedOfDestruction extends AbstractNpcAI implements IXmlReader
{
	protected class SOD1World extends InstanceWorld
	{
		protected List<L2PcInstance> playersInside = new ArrayList<>();
		protected Map<L2Npc, Boolean> npcList = new HashMap<>();
		protected int deviceSpawnedMobCount = 0;
		protected Lock lock = new ReentrantLock();
		protected L2MonsterInstance tiat;
		
		public int _tiatCounter = 0;
		public long _lastTiatSpeech = -1;
		public int _thronePowerfullDevices = 0;
		protected boolean _throneDoorClosed = false;
	}
	
	protected static class SODSpawn
	{
		public boolean isZone = false;
		public boolean isNeededNextFlag = false;
		public int npcId;
		public int x = 0;
		public int y = 0;
		public int z = 0;
		public int h = 0;
		public int zone = 0;
		public int count = 0;
	}
	
	private static final int INSTANCEID = 110; // this is the client number
	private static final int MIN_PLAYERS = Config.MIN_PLAYER_TO_TIAT;
	private static final int MAX_PLAYERS = Config.MAX_PLAYER_TO_TIAT;
	private static final int MAX_DEVICESPAWNEDMOBCOUNT = 100; // prevent too much mob spawn
	
	private final Map<Integer, L2Territory> _spawnZoneList = new HashMap<>();
	private final Map<Integer, List<SODSpawn>> _spawnList = new HashMap<>();
	private final List<Integer> _mustKillMobsId = new ArrayList<>();
	private int _spawnsCount = 0;
	
	// teleports
	private static final Location ENTER_TELEPORT_1 = new Location(-242759, 219981, -9986);
	private static final Location ENTER_TELEPORT_2 = new Location(-245800, 220488, -12112);
	private static final Location CENTER_TELEPORT = new Location(-245802, 220528, -12104);
	
	// Traps/Skills
	private static final SkillHolder TRAP_HOLD = new SkillHolder(4186, 9); // 18720-18728
	private static final SkillHolder TRAP_STUN = new SkillHolder(4072, 10); // 18729-18736
	private static final SkillHolder TRAP_DAMAGE = new SkillHolder(5340, 4); // 18737-18770
	private static final int[] TRAP_18771_NPCS =
	{
		22541,
		22544,
		22541,
		22544
	};
	private static final int[] TRAP_OTHER_NPCS =
	{
		22546,
		22546,
		22538,
		22537
	};
	
	// NPCs
	private static final int ALENOS = 32526;
	private static final int TELEPORT = 32601;
	
	// mobs
	private static final int OBELISK = 18776;
	private static final int POWERFUL_DEVICE = 18777;
	private static final int THRONE_POWERFUL_DEVICE = 18778;
	private static final int SPAWN_DEVICE = 18696;
	private static final int TIAT = 29163;
	private static final int TIAT_GUARD = 29162;
	private static final int TIAT_GUARD_NUMBER = 5;
	private static final int TIAT_VIDEO_NPC = 29169;
	private static final Location MOVE_TO_TIAT = new Location(-250403, 207273, -11952, 16384);
	private static final Location MOVE_TO_DOOR = new Location(-251432, 214905, -12088, 16384);
	
	//@formatter:off
	private static final int[] SPAWN_MOB_IDS = { 22536, 22537, 22538, 22539, 22540, 22541, 22542, 22543, 22544, 22547, 22550, 22551, 22552, 22596 };
	
	// Doors/Walls/Zones
	private static final int[] ATTACKABLE_DOORS =
	{
		12240005, 12240006, 12240007, 12240008, 12240009,
		12240010, 12240013, 12240014, 12240015, 12240016,
		12240017, 12240018, 12240021, 12240022, 12240023,
		12240024, 12240025, 12240026, 12240028, 12240029,
		12240030
	};
	private static final int[] ENTRANCE_ROOM_DOORS = { 12240001, 12240002 };
	private static final int[] SQUARE_DOORS = { 12240003, 12240004, 12240011, 12240012, 12240019, 12240020 };
	//@formatter:on
	private static final int SCOUTPASS_DOOR = 12240027;
	private static final int FORTRESS_DOOR = 12240030;
	private static final int THRONE_DOOR = 12240031;
	
	// Initialization at 6:30 am on Wednesday and Saturday
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;
	private static final int RESET_DAY_1 = 4;
	private static final int RESET_DAY_2 = 7;
	
	public SeedOfDestruction()
	{
		super(SeedOfDestruction.class.getSimpleName(), "gracia/instances");
		load();
		addStartNpc(ALENOS);
		addTalkId(ALENOS);
		addStartNpc(TELEPORT);
		addTalkId(TELEPORT);
		addFirstTalkId(TELEPORT);
		addAttackId(OBELISK);
		addSpawnId(OBELISK);
		addKillId(OBELISK);
		addSpawnId(POWERFUL_DEVICE);
		addKillId(POWERFUL_DEVICE);
		addSpawnId(THRONE_POWERFUL_DEVICE);
		addKillId(THRONE_POWERFUL_DEVICE);
		addAttackId(TIAT);
		addKillId(TIAT);
		addKillId(SPAWN_DEVICE);
		addSpawnId(TIAT_GUARD);
		addKillId(TIAT_GUARD);
		addAggroRangeEnterId(TIAT_VIDEO_NPC);
		// registering spawn traps which handled in this script
		for (int i = 18771; i <= 18774; i++)
		{
			addTrapActionId(i);
		}
		addKillId(_mustKillMobsId);
	}
	
	@Override
	public void load()
	{
		_spawnsCount = 0;
		_spawnList.clear();
		_spawnZoneList.clear();
		parseDatapackFile("data/xml/spawnZones/seed_of_destruction.xml");
		LOGGER.info(SeedOfDestruction.class.getSimpleName() + ": Loaded " + _spawnZoneList.size() + " spawn zones data.");
		LOGGER.info(SeedOfDestruction.class.getSimpleName() + ": Loaded " + _spawnsCount + " spawns data.");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		Node first = doc.getFirstChild();
		if ((first != null) && "list".equalsIgnoreCase(first.getNodeName()))
		{
			for (Node n = first.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("npc".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("spawn".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							Node att = attrs.getNamedItem("npcId");
							if (att == null)
							{
								_log.error(SeedOfDestruction.class.getSimpleName() + ": Missing npcId in npc List, skipping");
								continue;
							}
							int npcId = Integer.parseInt(attrs.getNamedItem("npcId").getNodeValue());
							
							att = attrs.getNamedItem("flag");
							if (att == null)
							{
								_log.error(SeedOfDestruction.class.getSimpleName() + ": Missing flag in npc List npcId: " + npcId + ", skipping");
								continue;
							}
							int flag = Integer.parseInt(attrs.getNamedItem("flag").getNodeValue());
							if (!_spawnList.containsKey(flag))
							{
								_spawnList.put(flag, new ArrayList<SODSpawn>());
							}
							
							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if ("loc".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									SODSpawn spw = new SODSpawn();
									spw.npcId = npcId;
									
									att = attrs.getNamedItem("x");
									if (att != null)
									{
										spw.x = Integer.parseInt(att.getNodeValue());
									}
									else
									{
										continue;
									}
									att = attrs.getNamedItem("y");
									if (att != null)
									{
										spw.y = Integer.parseInt(att.getNodeValue());
									}
									else
									{
										continue;
									}
									att = attrs.getNamedItem("z");
									if (att != null)
									{
										spw.z = Integer.parseInt(att.getNodeValue());
									}
									else
									{
										continue;
									}
									att = attrs.getNamedItem("heading");
									if (att != null)
									{
										spw.h = Integer.parseInt(att.getNodeValue());
									}
									else
									{
										continue;
									}
									att = attrs.getNamedItem("mustKill");
									if (att != null)
									{
										spw.isNeededNextFlag = Boolean.parseBoolean(att.getNodeValue());
									}
									if (spw.isNeededNextFlag)
									{
										_mustKillMobsId.add(npcId);
									}
									_spawnList.get(flag).add(spw);
									_spawnsCount++;
								}
								else if ("zone".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									SODSpawn spw = new SODSpawn();
									spw.npcId = npcId;
									spw.isZone = true;
									
									att = attrs.getNamedItem("id");
									if (att != null)
									{
										spw.zone = Integer.parseInt(att.getNodeValue());
									}
									else
									{
										continue;
									}
									att = attrs.getNamedItem("count");
									if (att != null)
									{
										spw.count = Integer.parseInt(att.getNodeValue());
									}
									else
									{
										continue;
									}
									att = attrs.getNamedItem("mustKill");
									if (att != null)
									{
										spw.isNeededNextFlag = Boolean.parseBoolean(att.getNodeValue());
									}
									if (spw.isNeededNextFlag)
									{
										_mustKillMobsId.add(npcId);
									}
									_spawnList.get(flag).add(spw);
									_spawnsCount++;
								}
							}
						}
					}
				}
				else if ("spawnZones".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("zone".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							Node att = attrs.getNamedItem("id");
							if (att == null)
							{
								_log.error(SeedOfDestruction.class.getSimpleName() + ": Missing id in spawnZones List, skipping");
								continue;
							}
							int id = Integer.parseInt(att.getNodeValue());
							att = attrs.getNamedItem("minZ");
							if (att == null)
							{
								_log.error(SeedOfDestruction.class.getSimpleName() + ": Missing minZ in spawnZones List id: " + id + ", skipping");
								continue;
							}
							int minz = Integer.parseInt(att.getNodeValue());
							att = attrs.getNamedItem("maxZ");
							if (att == null)
							{
								_log.error(SeedOfDestruction.class.getSimpleName() + ": Missing maxZ in spawnZones List id: " + id + ", skipping");
								continue;
							}
							int maxz = Integer.parseInt(att.getNodeValue());
							L2Territory ter = new L2Territory(id);
							
							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if ("point".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									int x, y;
									att = attrs.getNamedItem("x");
									if (att != null)
									{
										x = Integer.parseInt(att.getNodeValue());
									}
									else
									{
										continue;
									}
									att = attrs.getNamedItem("y");
									if (att != null)
									{
										y = Integer.parseInt(att.getNodeValue());
									}
									else
									{
										continue;
									}
									
									ter.add(x, y, minz, maxz, 0);
								}
							}
							
							_spawnZoneList.put(id, ter);
						}
					}
				}
			}
		}
	}
	
	private boolean checkConditions(L2PcInstance player)
	{
		if (player.isGM())
		{
			return true;
		}
		
		final L2Party party = player.getParty();
		if (party == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		final L2CommandChannel channel = player.getParty().getCommandChannel();
		if (channel == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
			return false;
		}
		else if (channel.getLeader() != player)
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		else if ((channel.getMemberCount() < MIN_PLAYERS) || (channel.getMemberCount() > MAX_PLAYERS))
		{
			player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}
		for (L2PcInstance partyMember : channel.getMembers())
		{
			if (partyMember.getLevel() < Config.MIN_LEVEL_TO_TIAT)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_LOCATION_WHICH_CANNOT_BE_ENTERED_THEREFORE_IT_CANNOT_BE_PROCESSED);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCEID);
			if (System.currentTimeMillis() < reentertime)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_RE_ENTER_YET);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
		}
		return true;
	}
	
	protected int enterInstance(L2PcInstance player, String template)
	{
		int instanceId = 0;
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (!(world instanceof SOD1World))
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_ENTERED_ANOTHER_INSTANT_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON);
				return 0;
			}
			teleportPlayer(player, ENTER_TELEPORT_1, world.getInstanceId());
			return world.getInstanceId();
		}
		// New instance
		if (!checkConditions(player))
		{
			return 0;
		}
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new SOD1World();
		world.setTemplateId(INSTANCEID);
		world.setInstanceId(instanceId);
		world.setStatus(0);
		InstanceManager.getInstance().addWorld(world);
		spawnState((SOD1World) world);
		for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
		{
			if (Util.contains(ATTACKABLE_DOORS, door.getId()) || (door.getId() == FORTRESS_DOOR))
			{
				door.setIsAttackableDoor(true);
				door.setTargetable(true);
				closeDoor(door.getId(), instanceId);
			}
			else if (door.getId() == THRONE_DOOR)
			{
				closeDoor(door.getId(), instanceId);
			}
		}
		_log.info("Seed of Destruction started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		// teleport players
		if ((player.getParty() == null) || (player.getParty().getCommandChannel() == null))
		{
			managePlayerEnter(player, (SOD1World) world);
		}
		else
		{
			for (L2PcInstance channelMember : player.getParty().getCommandChannel().getMembers())
			{
				managePlayerEnter(channelMember, (SOD1World) world);
			}
		}
		return instanceId;
	}
	
	private void managePlayerEnter(L2PcInstance player, SOD1World world)
	{
		world.playersInside.add(player);
		world.addAllowed(player.getObjectId());
		teleportPlayer(player, ENTER_TELEPORT_1, world.getInstanceId(), false);
	}
	
	protected boolean checkKillProgress(L2Npc mob, SOD1World world)
	{
		if (world.npcList.containsKey(mob))
		{
			world.npcList.put(mob, true);
		}
		for (boolean isDead : world.npcList.values())
		{
			if (!isDead)
			{
				return false;
			}
		}
		return true;
	}
	
	private void spawnFlaggedNPCs(SOD1World world, int flag)
	{
		if (world.lock.tryLock())
		{
			try
			{
				for (SODSpawn spw : _spawnList.get(flag))
				{
					if (spw.isZone)
					{
						for (int i = 0; i < spw.count; i++)
						{
							if (_spawnZoneList.containsKey(spw.zone))
							{
								final Location location = _spawnZoneList.get(spw.zone).getRandomPoint();
								if (location != null)
								{
									spawn(world, spw.npcId, location.getX(), location.getY(), GeoData.getInstance().getSpawnHeight(location), getRandom(65535), spw.isNeededNextFlag);
								}
							}
							else
							{
								_log.info(SeedOfDestruction.class.getSimpleName() + ": Missing zone: " + spw.zone);
							}
						}
					}
					else
					{
						spawn(world, spw.npcId, spw.x, spw.y, spw.z, spw.h, spw.isNeededNextFlag);
					}
				}
			}
			finally
			{
				world.lock.unlock();
			}
		}
	}
	
	protected boolean spawnState(SOD1World world)
	{
		if (world.lock.tryLock())
		{
			try
			{
				world.npcList.clear();
				switch (world.getStatus())
				{
					case 0:
						spawnFlaggedNPCs(world, 0);
						break;
					case 1:
						manageScreenMsg(world, NpcStringId.THE_ENEMIES_HAVE_ATTACKED_EVERYONE_COME_OUT_AND_FIGHT_URGH);
						for (int i : ENTRANCE_ROOM_DOORS)
						{
							openDoor(i, world.getInstanceId());
						}
						spawnFlaggedNPCs(world, 1);
						break;
					case 2:
					case 3:
						// handled elsewhere
						return true;
					case 4:
						manageScreenMsg(world, NpcStringId.OBELISK_HAS_COLLAPSED_DONT_LET_THE_ENEMIES_JUMP_AROUND_WILDLY_ANYMORE);
						for (int i : SQUARE_DOORS)
						{
							openDoor(i, world.getInstanceId());
						}
						spawnFlaggedNPCs(world, 4);
						break;
					case 5:
						openDoor(SCOUTPASS_DOOR, world.getInstanceId());
						spawnFlaggedNPCs(world, 3);
						spawnFlaggedNPCs(world, 5);
						break;
					case 6:
						openDoor(THRONE_DOOR, world.getInstanceId());
						break;
					case 7:
						spawnFlaggedNPCs(world, 7);
						break;
					case 8:
						manageScreenMsg(world, NpcStringId.COME_OUT_WARRIORS_PROTECT_SEED_OF_DESTRUCTION);
						world.deviceSpawnedMobCount = 0;
						spawnFlaggedNPCs(world, 8);
						break;
					case 9:
						// instance end
						break;
				}
				world.incStatus();
				return true;
			}
			finally
			{
				world.lock.unlock();
			}
		}
		return false;
	}
	
	protected void spawn(SOD1World world, int npcId, int x, int y, int z, int h, boolean addToKillTable)
	{
		// traps
		if ((npcId >= 18720) && (npcId <= 18774))
		{
			L2Skill skill = null;
			if (npcId <= 18728)
			{
				skill = TRAP_HOLD.getSkill();
			}
			else if (npcId <= 18736)
			{
				skill = TRAP_STUN.getSkill();
			}
			else if (npcId <= 18770)
			{
				skill = TRAP_DAMAGE.getSkill();
			}
			
			addTrap(npcId, x, y, z, h, skill, world.getInstanceId());
			return;
		}
		L2Npc npc = addSpawn(npcId, x, y, z, h, false, 0, false, world.getInstanceId());
		if (addToKillTable)
		{
			world.npcList.put(npc, false);
		}
		npc.setIsNoRndWalk(true);
		if (npc.isInstanceTypes(InstanceType.L2Attackable))
		{
			((L2Attackable) npc).setSeeThroughSilentMove(true);
		}
		if (npcId == TIAT_VIDEO_NPC)
		{
			startQuestTimer("DoorCheck", 10000, npc, null);
		}
		else if (npcId == SPAWN_DEVICE)
		{
			npc.disableCoreAI(true);
			startQuestTimer("Spawn", 10000, npc, null, true);
		}
		else if (npcId == TIAT)
		{
			npc.setIsImmobilized(true);
			world.tiat = (L2MonsterInstance) npc;
			for (int i = 0; i < TIAT_GUARD_NUMBER; i++)
			{
				addMinion(world.tiat, TIAT_GUARD);
			}
		}
	}
	
	protected void setInstanceTimeRestrictions(SOD1World world)
	{
		Calendar reenter = Calendar.getInstance();
		reenter.set(Calendar.MINUTE, RESET_MIN);
		reenter.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
		// if time is >= RESET_HOUR - roll to the next day
		if (reenter.getTimeInMillis() <= System.currentTimeMillis())
		{
			reenter.add(Calendar.DAY_OF_MONTH, 1);
		}
		if (reenter.get(Calendar.DAY_OF_WEEK) <= RESET_DAY_1)
		{
			while (reenter.get(Calendar.DAY_OF_WEEK) != RESET_DAY_1)
			{
				reenter.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		else
		{
			while (reenter.get(Calendar.DAY_OF_WEEK) != RESET_DAY_2)
			{
				reenter.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_FROM_HERE_S1_S_ENTRY_HAS_BEEN_RESTRICTED);
		sm.addInstanceName(INSTANCEID);
		
		// set instance reenter time for all allowed players
		for (int objectId : world.getAllowed())
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objectId);
			InstanceManager.getInstance().setInstanceTime(objectId, INSTANCEID, reenter.getTimeInMillis());
			if ((player != null) && player.isOnline())
			{
				player.sendPacket(sm);
			}
		}
	}
	
	private void manageScreenMsg(SOD1World world, NpcStringId stringId)
	{
		for (L2PcInstance players : world.playersInside)
		{
			if ((players != null) && (players.getInstanceId() == world.getInstanceId()))
			{
				showOnScreenMsg(players, stringId, 2, 5000);
			}
		}
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getId() == TIAT_GUARD)
		{
			startQuestTimer("GuardThink", 2500 + getRandom(-200, 200), npc, null, true);
		}
		else
		{
			npc.disableCoreAI(true);
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if ((isSummon == false) && (player != null))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
			if (tmpworld instanceof SOD1World)
			{
				SOD1World world = (SOD1World) tmpworld;
				if (world.getStatus() == 7)
				{
					if (spawnState(world))
					{
						for (int objId : world.getAllowed())
						{
							L2PcInstance pl = L2World.getInstance().getPlayer(objId);
							if (pl != null)
							{
								pl.showQuestMovie(ExStartScenePlayer.SCENE_TIAT_OPENING);
							}
						}
						npc.deleteMe();
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, L2Skill skill)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof SOD1World)
		{
			SOD1World world = (SOD1World) tmpworld;
			if ((world.getStatus() == 2) && (npc.getId() == OBELISK))
			{
				world.setStatus(4);
				spawnFlaggedNPCs(world, 3);
			}
			else if ((world.getStatus() == 3) && (npc.getId() == OBELISK))
			{
				world.setStatus(4);
				spawnFlaggedNPCs(world, 2);
			}
			else if ((world.getStatus() <= 8) && (npc.getId() == TIAT))
			{
				if (npc.getCurrentHp() < (npc.getMaxHp() / 2))
				{
					if (spawnState(world))
					{
						startQuestTimer("TiatFullHp", 3000, npc, null);
						setInstanceTimeRestrictions(world);
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof SOD1World)
		{
			SOD1World world = (SOD1World) tmpworld;
			if (event.equalsIgnoreCase("Spawn"))
			{
				L2PcInstance target = L2World.getInstance().getPlayer(world.getAllowed().get(getRandom(world.getAllowed().size())));
				if ((world.deviceSpawnedMobCount < MAX_DEVICESPAWNEDMOBCOUNT) && (target != null) && (target.getInstanceId() == npc.getInstanceId()) && !target.isDead())
				{
					L2Attackable mob = (L2Attackable) addSpawn(SPAWN_MOB_IDS[getRandom(SPAWN_MOB_IDS.length)], npc.getSpawn().getLocation(), false, 0, false, world.getInstanceId());
					world.deviceSpawnedMobCount++;
					mob.setSeeThroughSilentMove(true);
					mob.setRunning();
					if (world.getStatus() >= 7)
					{
						mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_TIAT);
					}
					else
					{
						mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_DOOR);
					}
				}
			}
			else if (event.equalsIgnoreCase("DoorCheck"))
			{
				L2DoorInstance tmp = getDoor(FORTRESS_DOOR, npc.getInstanceId());
				if (tmp.getCurrentHp() < tmp.getMaxHp())
				{
					world.deviceSpawnedMobCount = 0;
					spawnFlaggedNPCs(world, 6);
					manageScreenMsg(world, NpcStringId.ENEMIES_ARE_TRYING_TO_DESTROY_THE_FORTRESS_EVERYONE_DEFEND_THE_FORTRESS);
				}
				else
				{
					startQuestTimer("DoorCheck", 10000, npc, null);
				}
			}
			else if (event.equalsIgnoreCase("TiatFullHp"))
			{
				if (!npc.isStunned() && !npc.isInvul())
				{
					npc.setCurrentHp(npc.getMaxHp());
				}
			}
			else if (event.equalsIgnoreCase("BodyGuardThink"))
			{
				L2Character mostHate = ((L2Attackable) npc).getMostHated();
				if (mostHate != null)
				{
					double dist = Util.calculateDistance(mostHate.getXdestination(), mostHate.getYdestination(), 0, npc.getSpawn().getX(), npc.getSpawn().getY(), 0, false, false);
					if (dist > 900)
					{
						((L2Attackable) npc).reduceHate(mostHate, ((L2Attackable) npc).getHating(mostHate));
					}
					mostHate = ((L2Attackable) npc).getMostHated();
					if ((mostHate != null) || (((L2Attackable) npc).getHating(mostHate) < 5))
					{
						((L2Attackable) npc).returnHome();
					}
				}
			}
		}
		return "";
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if (npc.getId() == SPAWN_DEVICE)
		{
			cancelQuestTimer("Spawn", npc, null);
			return "";
		}
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof SOD1World)
		{
			SOD1World world = (SOD1World) tmpworld;
			if (world.getStatus() == 1)
			{
				if (checkKillProgress(npc, world))
				{
					spawnState(world);
				}
			}
			else if (world.getStatus() == 2)
			{
				if (checkKillProgress(npc, world))
				{
					world.incStatus();
				}
			}
			else if ((world.getStatus() == 4) && (npc.getId() == OBELISK))
			{
				spawnState(world);
			}
			else if ((world.getStatus() == 5) && (npc.getId() == POWERFUL_DEVICE))
			{
				if (checkKillProgress(npc, world))
				{
					spawnState(world);
				}
			}
			else if ((world.getStatus() == 6) && (npc.getId() == THRONE_POWERFUL_DEVICE))
			{
				world._thronePowerfullDevices++;
				if (world._thronePowerfullDevices >= 2)
				{
					if (checkKillProgress(npc, world))
					{
						spawnState(world);
					}
				}
			}
			else if (world.getStatus() >= 7)
			{
				if (npc.getId() == TIAT)
				{
					world.incStatus();
					for (int objId : world.getAllowed())
					{
						L2PcInstance pl = L2World.getInstance().getPlayer(objId);
						if (pl != null)
						{
							pl.showQuestMovie(ExStartScenePlayer.SCENE_TIAT_SUCCESS);
						}
					}
					for (L2Npc mob : InstanceManager.getInstance().getInstance(world.getInstanceId()).getNpcs())
					{
						mob.deleteMe();
					}
					
					SoDManager.getInstance().increaseSoDTiatKilled();
					
					// Destroy instance after 5 minutes
					Instance inst = InstanceManager.getInstance().getInstance(tmpworld.getInstanceId());
					inst.setDuration(15 * 60000);
					inst.setEmptyDestroyTime(0);
				}
				else if (npc.getId() == TIAT_GUARD)
				{
					addMinion(world.tiat, TIAT_GUARD);
				}
			}
		}
		return "";
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		switch (npc.getId())
		{
			case ALENOS:
				InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
				if ((SoDManager.getInstance().getSoDState() == 1) || ((world != null) && (world instanceof SOD1World)))
				{
					enterInstance(player, "SeedOfDestruction.xml");
				}
				else if (SoDManager.getInstance().getSoDState() == 2)
				{
					teleportPlayer(player, ENTER_TELEPORT_2, 0, false);
				}
				break;
			case TELEPORT:
				teleportPlayer(player, CENTER_TELEPORT, player.getInstanceId(), false);
				break;
		}
		return "";
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "32601.htm";
	}
	
	@Override
	public String onTrapAction(L2TrapInstance trap, L2Character trigger, TrapAction action)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(trap.getInstanceId());
		if (tmpworld instanceof SOD1World)
		{
			SOD1World world = (SOD1World) tmpworld;
			switch (action)
			{
				case TRAP_TRIGGERED:
					if (trap.getId() == 18771)
					{
						for (int npcId : TRAP_18771_NPCS)
						{
							addSpawn(npcId, trap.getX(), trap.getY(), trap.getZ(), trap.getHeading(), true, 0, true, world.getInstanceId());
						}
					}
					else
					{
						for (int npcId : TRAP_OTHER_NPCS)
						{
							addSpawn(npcId, trap.getX(), trap.getY(), trap.getZ(), trap.getHeading(), true, 0, true, world.getInstanceId());
						}
					}
					break;
			}
		}
		return null;
	}
}