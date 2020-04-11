/*
+ * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.scripts.instances;

import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_ATTACK;

import java.util.Calendar;
import java.util.List;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2r.Config;
import l2r.gameserver.GeoData;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.enums.MountType;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.instancemanager.GrandBossManager;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2DecoyInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.zone.L2ZoneForm;
import l2r.gameserver.model.zone.L2ZoneType;
import l2r.gameserver.model.zone.type.L2BossZone;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.AbstractNpcInfo;
import l2r.gameserver.network.serverpackets.PlaySound;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

/**
 * @author Nyaran (based on org.inc? work)
 */
public class Zaken extends Quest
{
	private class ZWorld extends InstanceWorld
	{
		List<L2PcInstance> _playersInInstance = new FastList<>();
		List<L2PcInstance> _playersInZakenZone = new FastList<>();
		int _zakenZone = 0;
		int _blueCandles = 0;
		L2Character _target;
		L2Attackable _zaken;
		L2Npc _barel1;
		L2Npc _barel2;
		L2Npc _barel3;
		L2Npc _barel4;
		L2Npc _barel5;
		L2Npc _barel6;
		L2Npc _barel7;
		L2Npc _barel8;
		L2Npc _barel9;
		L2Npc _barel10;
		L2Npc _barel11;
		L2Npc _barel12;
		L2Npc _barel13;
		L2Npc _barel14;
		L2Npc _barel15;
		L2Npc _barel16;
		L2Npc _barel17;
		L2Npc _barel18;
		L2Npc _barel19;
		L2Npc _barel20;
		L2Npc _barel21;
		L2Npc _barel22;
		L2Npc _barel23;
		L2Npc _barel24;
		L2Npc _barel25;
		L2Npc _barel26;
		L2Npc _barel27;
		L2Npc _barel28;
		L2Npc _barel29;
		L2Npc _barel30;
		L2Npc _barel31;
		L2Npc _barel32;
		L2Npc _barel33;
		L2Npc _barel34;
		L2Npc _barel35;
		L2Npc _barel36;
		L2Npc _zakenBarel1;
		L2Npc _zakenBarel2;
		L2Npc _zakenBarel3;
		L2Npc _zakenBarel4;
		public List<L2Npc> _barrels = new FastList<>();
		
		public ZWorld()
		{
		}
	}
	
	private static final int INSTANCEID_NIGHT = 114; // this is the client number
	private static final int INSTANCEID_DAY = 133; // this is the client number
	private static final int INSTANCEID_DAY83 = 135; // this is the client number
	private static final boolean DEBUG = false;
	
	// NPCs
	// Bosses
	private static final int ZAKEN_NIGHT = 29022;
	private static final int ZAKEN_DAY = 29176;
	private static final int ZAKEN_DAY83 = 29181;
	// Mobs
	private static final int DOLL_BLADER = 29023;
	private static final int VALE_MASTER = 29024;
	private static final int ZOMBIE_CAPTAIN = 29026;
	private static final int ZOMBIE = 29027;
	private static final int DOLL_BLADER_DAY83 = 29182;
	private static final int VALE_MASTER_DAY83 = 29183;
	private static final int ZOMBIE_CAPTAIN_DAY83 = 29184;
	private static final int ZOMBIE_DAY83 = 29185;
	// Telepoters
	private static final int PATHFINDER = 32713;
	// Barrel
	private static final int BARREL = 32705;
	
	// Teleports
	private static final Location ENTER_TELEPORT = new Location(52680, 219088, -3232);
	
	// Zones for rooms
	// floor 1
	private static int _room1_zone = 120111;
	private static int _room2_zone = 120112;
	private static int _room3_zone = 120113;
	private static int _room4_zone = 120114;
	private static int _room5_zone = 120115;
	// floor 2
	private static int _room6_zone = 120116;
	private static int _room7_zone = 120117;
	private static int _room8_zone = 120118;
	private static int _room9_zone = 120119;
	private static int _room10_zone = 120120;
	// floor 3
	private static int _room11_zone = 120121;
	private static int _room12_zone = 120122;
	private static int _room13_zone = 120123;
	private static int _room14_zone = 120124;
	private static int _room15_zone = 120125;
	
	private enum candleStates
	{
		NONE(0),
		SPARKS(15280),
		RED(15281),
		BLUE(15302);
		
		private int _id;
		
		candleStates(int id)
		{
			_id = id;
		}
		
		protected int getId()
		{
			return _id;
		}
	}
	
	private static final int[] _zones =
	{
		_room1_zone,
		_room2_zone,
		_room3_zone,
		_room4_zone,
		_room5_zone,
		_room6_zone,
		_room7_zone,
		_room8_zone,
		_room9_zone,
		_room10_zone,
		_room11_zone,
		_room12_zone,
		_room13_zone,
		_room14_zone,
		_room15_zone
	};
	private static final int[] _barrelSpawnZones =
	{
		_room4_zone,
		_room4_zone,
		_room3_zone,
		_room1_zone,
		_room3_zone,
		_room3_zone,
		_room5_zone,
		_room5_zone,
		_room3_zone,
		_room2_zone,
		_room2_zone,
		_room1_zone,
		_room9_zone,
		_room9_zone,
		_room8_zone,
		_room6_zone,
		_room8_zone,
		_room8_zone,
		_room10_zone,
		_room10_zone,
		_room8_zone,
		_room7_zone,
		_room7_zone,
		_room6_zone,
		_room14_zone,
		_room14_zone,
		_room13_zone,
		_room11_zone,
		_room13_zone,
		_room13_zone,
		_room15_zone,
		_room15_zone,
		_room13_zone,
		_room12_zone,
		_room12_zone,
		_room11_zone,
	};
	
	private static final FastMap<Integer, Integer[]> zoneBarrels = new FastMap<>();
	static
	{
		zoneBarrels.put(_room1_zone, new Integer[]
		{
			3,
			4,
			5,
			12
		});
		zoneBarrels.put(_room2_zone, new Integer[]
		{
			5,
			9,
			10,
			11
		});
		zoneBarrels.put(_room3_zone, new Integer[]
		{
			3,
			5,
			6,
			9
		});
		zoneBarrels.put(_room4_zone, new Integer[]
		{
			1,
			2,
			3,
			6
		});
		zoneBarrels.put(_room5_zone, new Integer[]
		{
			6,
			7,
			8,
			9
		});
		zoneBarrels.put(_room6_zone, new Integer[]
		{
			15,
			16,
			17,
			24
		});
		zoneBarrels.put(_room7_zone, new Integer[]
		{
			17,
			21,
			22,
			23
		});
		zoneBarrels.put(_room8_zone, new Integer[]
		{
			15,
			17,
			18,
			21
		});
		zoneBarrels.put(_room9_zone, new Integer[]
		{
			13,
			14,
			15,
			18
		});
		zoneBarrels.put(_room10_zone, new Integer[]
		{
			18,
			19,
			20,
			21
		});
		zoneBarrels.put(_room11_zone, new Integer[]
		{
			27,
			28,
			29,
			36
		});
		zoneBarrels.put(_room12_zone, new Integer[]
		{
			29,
			33,
			34,
			35
		});
		zoneBarrels.put(_room13_zone, new Integer[]
		{
			27,
			29,
			30,
			33
		});
		zoneBarrels.put(_room14_zone, new Integer[]
		{
			25,
			26,
			27,
			30
		});
		zoneBarrels.put(_room15_zone, new Integer[]
		{
			30,
			31,
			32,
			33
		});
	}
	
	private static final FastList<Location> _spawnPcLocationDaytime = new FastList<>();
	static
	{
		_spawnPcLocationDaytime.add(new Location(52684, 219989, -3496));
		_spawnPcLocationDaytime.add(new Location(52669, 219120, -3224));
		_spawnPcLocationDaytime.add(new Location(52672, 219439, -3312));
	}
	
	private static final FastList<Location> _spawnPcLocationNighttime = new FastList<>();
	static
	{
		_spawnPcLocationNighttime.add(new Location(54469, 219798, -3488));
		_spawnPcLocationNighttime.add(new Location(54469, 219870, -3488));
		_spawnPcLocationNighttime.add(new Location(54469, 219930, -3488));
		_spawnPcLocationNighttime.add(new Location(54469, 220043, -3488));
		_spawnPcLocationNighttime.add(new Location(54469, 220094, -3488));
		_spawnPcLocationNighttime.add(new Location(54469, 220157, -3488));
		_spawnPcLocationNighttime.add(new Location(54469, 220214, -3488));
		_spawnPcLocationNighttime.add(new Location(54469, 220274, -3488));
		_spawnPcLocationNighttime.add(new Location(54469, 220333, -3488));
		_spawnPcLocationNighttime.add(new Location(54469, 220397, -3488));
		_spawnPcLocationNighttime.add(new Location(54469, 220463, -3488));
	}
	
	private static final FastList<Location> _spawnsZaken = new FastList<>();
	static
	{
		_spawnsZaken.add(new Location(54237, 218135, -3496));
		_spawnsZaken.add(new Location(56288, 218087, -3496));
		_spawnsZaken.add(new Location(55273, 219140, -3496));
		_spawnsZaken.add(new Location(54232, 220184, -3496));
		_spawnsZaken.add(new Location(56259, 220168, -3496));
		_spawnsZaken.add(new Location(54250, 218122, -3224));
		_spawnsZaken.add(new Location(56308, 218125, -3224));
		_spawnsZaken.add(new Location(55243, 219064, -3224));
		_spawnsZaken.add(new Location(54255, 220156, -3224));
		_spawnsZaken.add(new Location(56255, 220161, -3224));
		_spawnsZaken.add(new Location(54261, 218095, -2952));
		_spawnsZaken.add(new Location(56258, 218086, -2952));
		_spawnsZaken.add(new Location(55258, 219080, -2952));
		_spawnsZaken.add(new Location(54292, 220096, -2952));
		_spawnsZaken.add(new Location(56258, 220135, -2952));
		
	}
	
	public Zaken()
	{
		super(-1, Zaken.class.getSimpleName(), "instances");
		addStartNpc(PATHFINDER);
		addTalkId(PATHFINDER);
		addFirstTalkId(PATHFINDER, BARREL);
		addKillId(ZAKEN_DAY, ZAKEN_DAY83, ZAKEN_NIGHT);
		addAttackId(ZAKEN_DAY, ZAKEN_DAY83, ZAKEN_NIGHT);
		addAggroRangeEnterId(ZAKEN_DAY, ZAKEN_DAY83, ZAKEN_NIGHT);
		
		for (int i = 120111; i <= 120125; i++)
		{
			addEnterZoneId(i);
			addExitZoneId(i);
		}
	}
	
	private boolean checkConditions(L2PcInstance player, String choice)
	{
		if (DEBUG || player.canOverrideCond(PcCondOverride.INSTANCE_CONDITIONS))
		{
			return true;
		}
		
		L2Party party = player.getParty();
		if (party == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		
		List<L2PcInstance> members;
		int minLevel = 0; // nighttime haven't level limit
		int minMembers = Config.ZAKEN_MINMEMBERS_DAYTIME;
		int maxMembers = Config.ZAKEN_MAXMEMBERS_DAYTIME;
		
		if (party.isInCommandChannel())
		{
			members = party.getCommandChannel().getMembers();
		}
		else
		{
			members = party.getMembers();
		}
		
		if (choice.equalsIgnoreCase("daytime"))
		{
			minLevel = Config.ZAKEN_MINLEVEL_DAYTIME;
		}
		else if (choice.equalsIgnoreCase("daytime83"))
		{
			minLevel = Config.ZAKEN_MINLEVEL_DAYTIME83;
		}
		
		if (choice.equalsIgnoreCase("nighttime"))
		{
			minMembers = Config.ZAKEN_MINMEMBERS_NIGHTTIME;
			maxMembers = Config.ZAKEN_MAXMEMBERS_NIGHTTIME;
		}
		
		if (members.size() < minMembers)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_MUST_HAVE_MINIMUM_OF_S1_PEOPLE_TO_ENTER);
			sm.addInt(minMembers);
			if (party.isInCommandChannel())
			{
				party.getCommandChannel().broadcastPacket(sm);
			}
			else
			{
				party.broadcastPacket(sm);
			}
			return false;
		}
		else if (members.size() > maxMembers)
		{
			player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}
		
		for (L2PcInstance member : members)
		{
			if (choice.equalsIgnoreCase("daytime") || (choice.equalsIgnoreCase("daytime83")))
			{
				if ((member == null) || (member.getLevel() < minLevel))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
					sm.addPcName(member);
					party.broadcastPacket(sm);
					return false;
				}
			}
			
			if (!Util.checkIfInRange(1000, player, member, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(member);
				party.broadcastPacket(sm);
				return false;
			}
			
			Long reenterTime = 0L;
			if (choice.equalsIgnoreCase("daytime"))
			{
				reenterTime = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), INSTANCEID_DAY);
			}
			else if (choice.equalsIgnoreCase("daytime83"))
			{
				reenterTime = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), INSTANCEID_DAY83);
			}
			else if (choice.equalsIgnoreCase("nighttime"))
			{
				reenterTime = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), INSTANCEID_NIGHT);
			}
			
			if (System.currentTimeMillis() < reenterTime)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
				sm.addPcName(member);
				party.broadcastPacket(sm);
				return false;
			}
		}
		return true;
	}
	
	protected int enterInstance(L2PcInstance player, String template, String choice, Location loc)
	{
		int instanceId = 0;
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (!(world instanceof ZWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleportPlayer(player, loc, world.getInstanceId(), false);
			return world.getInstanceId();
		}
		
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new ZWorld();
		if (choice.equalsIgnoreCase("daytime"))
		{
			world.setTemplateId(INSTANCEID_DAY);
		}
		else if (choice.equalsIgnoreCase("daytime83"))
		{
			world.setTemplateId(INSTANCEID_DAY83);
		}
		else if (choice.equalsIgnoreCase("nighttime"))
		{
			world.setTemplateId(INSTANCEID_NIGHT);
		}
		
		world.setInstanceId(instanceId);
		world.setStatus(0);
		InstanceManager.getInstance().addWorld(world);
		_log.info("Zaken (" + choice + ") started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		
		if (choice.equalsIgnoreCase("nighttime"))
		{
			startQuestTimer("ZakenSpawn", 1000, null, player);
			for (int i = _room1_zone; i <= _room15_zone; i++)
			{
				spawnRoom(world.getInstanceId(), i);
			}
		}
		else
		{
			startQuestTimer("ChooseZakenRoom", 1000, null, player);
		}
		
		// teleport players
		List<L2PcInstance> players;
		L2Party party = player.getParty();
		if (party == null) // this can happen only if debug is true
		{
			players = new FastList<>();
			players.add(player);
		}
		else if (party.isInCommandChannel())
		{
			players = party.getCommandChannel().getMembers();
		}
		else
		{
			players = party.getMembers();
		}
		
		for (L2PcInstance member : players)
		{
			// new instance
			if (!checkConditions(member, choice))
			{
				return 0;
			}
		}
		
		int count = 1;
		for (L2PcInstance member : players)
		{
			_log.info("Zaken Party Member " + count + ", Member name is: " + member.getName());
			count++;
			((ZWorld) world)._playersInInstance.add(member);
			teleportPlayer(member, loc, world.getInstanceId(), false);
			world.addAllowed(member.getObjectId());
			member.sendPacket(new PlaySound("BS01_A"));
		}
		
		for (L2PcInstance pc : ((ZWorld) world)._playersInInstance)
		{
			if (pc != null)
			{
				savePlayerReenter(pc);
			}
		}
		
		return instanceId;
	}
	
	private void spawnRoom(int instanceId, int zoneId)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(instanceId);
		
		if (tmpworld instanceof ZWorld)
		{
			ZWorld world = (ZWorld) tmpworld;
			int[] position =
			{
				0,
				0,
				0
			};
			
			L2ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
			L2ZoneForm zoneform = zone.getZone();
			
			for (int i = 1; i <= 5; i++)
			{
				position = zoneform.getRandomPoint();
				if ((world.getTemplateId() == INSTANCEID_DAY) || (world.getTemplateId() == INSTANCEID_NIGHT))
				{
					((L2Attackable) addSpawn(ZOMBIE, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId)).setIsRaidMinion(true);
				}
				else if (world.getTemplateId() == INSTANCEID_DAY83)
				{
					((L2Attackable) addSpawn(ZOMBIE_DAY83, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId)).setIsRaidMinion(true);
				}
			}
			for (int i = 1; i <= 3; i++)
			{
				position = zoneform.getRandomPoint();
				if ((world.getTemplateId() == INSTANCEID_DAY) || (world.getTemplateId() == INSTANCEID_NIGHT))
				{
					((L2Attackable) addSpawn(DOLL_BLADER, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId)).setIsRaidMinion(true);
				}
				else if (world.getTemplateId() == INSTANCEID_DAY83)
				{
					((L2Attackable) addSpawn(DOLL_BLADER_DAY83, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId)).setIsRaidMinion(true);
				}
			}
			for (int i = 1; i <= 2; i++)
			{
				if ((world.getTemplateId() == INSTANCEID_DAY) || (world.getTemplateId() == INSTANCEID_NIGHT))
				{
					if (world.getTemplateId() == INSTANCEID_DAY)
					{
						((L2Attackable) addSpawn(VALE_MASTER, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId)).setIsRaidMinion(true);
					}
					else if (world.getTemplateId() == INSTANCEID_DAY83)
					{
						((L2Attackable) addSpawn(VALE_MASTER_DAY83, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId)).setIsRaidMinion(true);
					}
				}
			}
			for (int i = 1; i <= 2; i++)
			{
				position = zoneform.getRandomPoint();
				if ((world.getTemplateId() == INSTANCEID_DAY) || (world.getTemplateId() == INSTANCEID_NIGHT))
				{
					((L2Attackable) addSpawn(ZOMBIE_CAPTAIN, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId)).setIsRaidMinion(true);
				}
				else if (world.getTemplateId() == INSTANCEID_DAY83)
				{
					((L2Attackable) addSpawn(ZOMBIE_CAPTAIN_DAY83, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId)).setIsRaidMinion(true);
				}
			}
		}
		
		if (DEBUG)
		{
			_log.info("Zaken minions spawned");
		}
	}
	
	private L2Character getRandomTarget(L2Npc npc)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		L2Character character = null;
		if (tmpworld instanceof ZWorld)
		{
			ZWorld world = (ZWorld) tmpworld;
			FastList<L2Character> result = new FastList<>();
			{
				for (L2Character obj : world._playersInZakenZone)
				{
					if (!(GeoData.getInstance().canSeeTarget(obj, npc)))
					{
						continue;
					}
					
					if ((obj instanceof L2PcInstance) || (obj instanceof L2Summon) || (obj instanceof L2DecoyInstance))
					{
						if (Util.checkIfInRange(2000, npc, obj, true) && !obj.isDead())
						{
							if ((obj instanceof L2PcInstance) && ((L2PcInstance) obj).isInvisible())
							{
								continue;
							}
							
							result.add(obj);
						}
					}
				}
			}
			if (!result.isEmpty() && (result.size() != 0))
			{
				Object[] characters = result.toArray();
				character = (L2Character) characters[Rnd.get(characters.length)];
				_log.info("Zaken target to " + character);
			}
		}
		return character;
	}
	
	private void savePlayerReenter(L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
		Calendar reuseTime = Calendar.getInstance();
		int dayWeek = reuseTime.get(Calendar.DAY_OF_WEEK);
		
		if ((tmpworld.getTemplateId() == INSTANCEID_DAY) || (tmpworld.getTemplateId() == INSTANCEID_DAY83)) // Monday, Wednesday, Friday (confirmed at leaked Freya)
		{
			if ((dayWeek == Calendar.MONDAY) || (dayWeek == Calendar.TUESDAY))
			{
				reuseTime.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
			}
			else if ((dayWeek == Calendar.WEDNESDAY) || (dayWeek == Calendar.THURSDAY))
			{
				reuseTime.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
			}
			else
			{
				reuseTime.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			}
		}
		else if (tmpworld.getTemplateId() == INSTANCEID_NIGHT)
		{
			reuseTime.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
		}
		
		if (reuseTime.getTimeInMillis() <= System.currentTimeMillis())
		{
			reuseTime.add(Calendar.DAY_OF_WEEK, 7);
		}
		
		reuseTime.set(Calendar.HOUR_OF_DAY, 6);
		reuseTime.set(Calendar.MINUTE, 30);
		reuseTime.set(Calendar.SECOND, 0);
		
		InstanceManager.getInstance().setInstanceTime(player.getObjectId(), tmpworld.getTemplateId(), reuseTime.getTimeInMillis());
		
		if (DEBUG)
		{
			_log.info("Player " + player + " Zaken reuse set to: " + reuseTime.getTime());
		}
	}
	
	protected void npcUpdate(L2Npc npc)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof ZWorld)
		{
			ZWorld world = (ZWorld) tmpworld;
			for (L2Character pc : world._playersInInstance)
			{
				if (pc.isPlayer())
				{
					((L2PcInstance) pc).sendPacket(new AbstractNpcInfo.NpcInfo(npc, pc));
				}
			}
		}
	}
	
	private void spawnBarrels(int instanceId)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(instanceId);
		if (tmpworld instanceof ZWorld)
		{
			ZWorld world = (ZWorld) tmpworld;
			world._barel1 = addSpawn(BARREL, 53312, 220128, -3504, 0, false, 0, false, world.getInstanceId()); // 4a
			world._barrels.add(0, world._barel1);
			world._barel2 = addSpawn(BARREL, 54241, 221062, -3499, 49151, false, 0, false, world.getInstanceId()); // 4a
			world._barrels.add(1, world._barel2);
			world._barel3 = addSpawn(BARREL, 54333, 219104, -3504, 35048, false, 0, false, world.getInstanceId()); // 1,3a,4
			world._barrels.add(2, world._barel3);
			world._barel4 = addSpawn(BARREL, 53312, 218079, -3504, 0, false, 0, false, world.getInstanceId()); // 1a
			world._barrels.add(3, world._barel4);
			world._barel5 = addSpawn(BARREL, 55260, 218171, -3504, 16384, false, 0, false, world.getInstanceId()); // 1,2,3a
			world._barrels.add(4, world._barel5);
			world._barel6 = addSpawn(BARREL, 55266, 220042, -3504, 49151, false, 0, false, world.getInstanceId()); // 3a,4,5
			world._barrels.add(5, world._barel6);
			world._barel7 = addSpawn(BARREL, 56288, 221056, -3504, 49152, false, 0, false, world.getInstanceId()); // 5a
			world._barrels.add(6, world._barel7);
			world._barel8 = addSpawn(BARREL, 57200, 220128, -3504, 32768, false, 0, false, world.getInstanceId()); // 5a
			world._barrels.add(7, world._barel8);
			world._barel9 = addSpawn(BARREL, 56192, 219104, -3504, 32768, false, 0, false, world.getInstanceId()); // 2,3a,5
			world._barrels.add(8, world._barel9);
			world._barel10 = addSpawn(BARREL, 57216, 218080, -3504, 32768, false, 0, false, world.getInstanceId()); // 2a
			world._barrels.add(9, world._barel10);
			world._barel11 = addSpawn(BARREL, 56286, 217156, -3504, 16384, false, 0, false, world.getInstanceId()); // 2a
			world._barrels.add(10, world._barel11);
			world._barel12 = addSpawn(BARREL, 54240, 217168, -3504, 16384, false, 0, false, world.getInstanceId()); // 1a
			world._barrels.add(11, world._barel12);
			
			world._barel13 = addSpawn(BARREL, 53332, 220128, -3227, 0, false, 0, false, world.getInstanceId()); // 9a
			world._barrels.add(12, world._barel13);
			world._barel14 = addSpawn(BARREL, 54240, 221040, -3232, 49152, false, 0, false, world.getInstanceId()); // 9a
			world._barrels.add(13, world._barel14);
			world._barel15 = addSpawn(BARREL, 54336, 219104, -3232, 0, false, 0, false, world.getInstanceId()); // 6,8a,9
			world._barrels.add(14, world._barel15);
			world._barel16 = addSpawn(BARREL, 53312, 218080, -3232, 0, false, 0, false, world.getInstanceId()); // 6a
			world._barrels.add(15, world._barel16);
			world._barel17 = addSpawn(BARREL, 55270, 218176, -3232, 16384, false, 0, false, world.getInstanceId()); // 6,7,8a
			world._barrels.add(16, world._barel17);
			world._barel18 = addSpawn(BARREL, 55264, 220032, -3232, 49152, false, 0, false, world.getInstanceId()); // 8a,9,10
			world._barrels.add(17, world._barel18);
			world._barel19 = addSpawn(BARREL, 56288, 221040, -3232, 49152, false, 0, false, world.getInstanceId()); // 10a
			world._barrels.add(18, world._barel19);
			world._barel20 = addSpawn(BARREL, 57200, 220128, -3232, 32768, false, 0, false, world.getInstanceId()); // 10a
			world._barrels.add(19, world._barel20);
			world._barel21 = addSpawn(BARREL, 56192, 219104, -3232, 32768, false, 0, false, world.getInstanceId()); // 7,8a,10
			world._barrels.add(20, world._barel21);
			world._barel22 = addSpawn(BARREL, 57213, 218080, -3229, 32768, false, 0, false, world.getInstanceId()); // 7a
			world._barrels.add(21, world._barel22);
			world._barel23 = addSpawn(BARREL, 56293, 217149, -3231, 16384, false, 0, false, world.getInstanceId()); // 7a
			world._barrels.add(22, world._barel23);
			world._barel24 = addSpawn(BARREL, 54240, 217152, -3232, 16384, false, 0, false, world.getInstanceId()); // 6a
			world._barrels.add(23, world._barel24);
			
			world._barel25 = addSpawn(BARREL, 53328, 220128, -2960, 0, false, 0, false, world.getInstanceId()); // 14a
			world._barrels.add(24, world._barel25);
			world._barel26 = addSpawn(BARREL, 54240, 221040, -2960, 49152, false, 0, false, world.getInstanceId()); // 14a
			world._barrels.add(25, world._barel26);
			world._barel27 = addSpawn(BARREL, 54331, 219104, -2960, 0, false, 0, false, world.getInstanceId()); // 11,13a,14
			world._barrels.add(26, world._barel27);
			world._barel28 = addSpawn(BARREL, 53328, 218080, -2956, 0, false, 0, false, world.getInstanceId()); // 11a
			world._barrels.add(27, world._barel28);
			world._barel29 = addSpawn(BARREL, 55264, 218165, -2960, 16384, false, 0, false, world.getInstanceId()); // 11,12,13a
			world._barrels.add(28, world._barel29);
			world._barel30 = addSpawn(BARREL, 55264, 220016, -2960, 49152, false, 0, false, world.getInstanceId()); // 13a,14,15
			world._barrels.add(29, world._barel30);
			world._barel31 = addSpawn(BARREL, 56288, 221024, -2960, 49152, false, 0, false, world.getInstanceId()); // 15a
			world._barrels.add(30, world._barel31);
			world._barel32 = addSpawn(BARREL, 57200, 220128, -2960, 32768, false, 0, false, world.getInstanceId()); // 15a
			world._barrels.add(31, world._barel32);
			world._barel33 = addSpawn(BARREL, 56192, 219104, -2960, 32768, false, 0, false, world.getInstanceId()); // 12,13a,15
			world._barrels.add(32, world._barel33);
			world._barel34 = addSpawn(BARREL, 57200, 218080, -2960, 32768, false, 0, false, world.getInstanceId()); // 12a
			world._barrels.add(33, world._barel34);
			world._barel35 = addSpawn(BARREL, 56288, 217152, -2960, 16384, false, 0, false, world.getInstanceId()); // 12a
			world._barrels.add(34, world._barel35);
			world._barel36 = addSpawn(BARREL, 54240, 217152, -2960, 16384, false, 0, false, world.getInstanceId()); // 11a
			world._barrels.add(35, world._barel36);
		}
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getId();
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		if (npcId == PATHFINDER)
		{
			return "32713.html";
		}
		else if ((npcId == BARREL) && (npc.getRightHandItem() == 0))
		{
			startQuestTimer("lightBarrel", 100, npc, player);
		}
		return "";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("nighttime.html") || event.equalsIgnoreCase("daytime.html") || event.equalsIgnoreCase("daytime83.html"))
		{
			return event;
		}
		else if (event.equalsIgnoreCase("nighttime") || event.equalsIgnoreCase("daytime") || event.equalsIgnoreCase("daytime83"))
		{
			L2Party party = player.getParty();
			if (!DEBUG && !player.isGM())
			{
				if (party == null)
				{
					return "alone.html";
				}
				else if (!party.isInCommandChannel() && (party.getLeaderObjectId() != player.getObjectId()))
				{
					return "no-party-leader.html";
				}
				else if (party.isInCommandChannel() && (party.getCommandChannel().getLeader().getObjectId() != player.getObjectId()))
				{
					return "no-command-leader.html";
				}
				else if (event.equalsIgnoreCase("nighttime") && party.isInCommandChannel() && (party.getCommandChannel().getPartys().size() < 7))
				{
					return "no-minimum-party.html";
				}
			}
			
			enterInstance(player, "Zaken" + event + ".xml", event, ENTER_TELEPORT);
		}
		InstanceWorld tmpworld;
		
		if (npc != null)
		{
			tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		}
		else
		{
			tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
		}
		
		if (tmpworld instanceof ZWorld)
		{
			ZWorld world = (ZWorld) tmpworld;
			if ((npc != null) && event.equalsIgnoreCase("lightBarrel"))
			{
				npc.setRHandId(candleStates.SPARKS.getId());
				npcUpdate(npc);
				
				if ((npc == world._zakenBarel1) || (npc == world._zakenBarel2) || (npc == world._zakenBarel3) || (npc == world._zakenBarel4))
				{
					startQuestTimer("BlueBarrel", 5000, npc, player);
				}
				else
				{
					startQuestTimer("RedBarrel", 5000, npc, player);
				}
			}
			else if ((npc != null) && event.equalsIgnoreCase("RedBarrel"))
			{
				npc.setRHandId(candleStates.RED.getId());
				npcUpdate(npc);
				for (L2Npc barrel : world._barrels)
				{
					if (barrel.equals(npc))
					{
						spawnRoom(npc.getInstanceId(), _barrelSpawnZones[world._barrels.indexOf(barrel)]);
					}
				}
			}
			else if ((npc != null) && event.equalsIgnoreCase("BlueBarrel"))
			{
				npc.setRHandId(candleStates.BLUE.getId());
				npcUpdate(npc);
				world._blueCandles++;
				if (player != null)
				{
					_log.warn("Player: " + player.getName() + " fires a blue candle. Candle count is: " + world._blueCandles);
				}
				if (world._blueCandles == 4)
				{
					startQuestTimer("ZakenSpawn", 1000, npc, player);
				}
			}
			else if (event.equalsIgnoreCase("ZakenSpawn"))
			{
				if (world.getTemplateId() == INSTANCEID_DAY)
				{
					int[] position = ZoneManager.getInstance().getZoneById(world._zakenZone).getZone().getRandomPoint();
					world._zaken = (L2Attackable) addSpawn(ZAKEN_DAY, position[0], position[1], position[2], 32768, false, 0, false, world.getInstanceId());
					_log.warn("Zaken Day:  spawned at: X: " + String.valueOf(position[0]) + " Y: " + String.valueOf(position[1]) + " Z: " + String.valueOf(position[2]));
					world._zaken.addSkill(SkillData.getInstance().getInfo(4216, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(4217, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(4218, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(4219, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(4220, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(4221, 1));
					world._zaken.setRunning();
					world._zaken.addDamageHate(player, 0, 999);
					world._zaken.getAI().setIntention(AI_INTENTION_ATTACK, player);
					world._target = player;
				}
				else if (world.getTemplateId() == INSTANCEID_DAY83)
				{
					int[] position = ZoneManager.getInstance().getZoneById(world._zakenZone).getZone().getRandomPoint();
					world._zaken = (L2Attackable) addSpawn(ZAKEN_DAY83, position[0], position[1], position[2], 32768, false, 0, false, world.getInstanceId());
					_log.warn("Zaken Day 83:  spawned at: X: " + String.valueOf(position[0]) + " Y: " + String.valueOf(position[1]) + " Z: " + String.valueOf(position[2]));
					world._zaken.addSkill(SkillData.getInstance().getInfo(4216, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(4217, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(6689, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(6690, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(6691, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(6692, 1));
					world._zaken.setRunning();
					world._zaken.addDamageHate(player, 0, 999);
					world._zaken.getAI().setIntention(AI_INTENTION_ATTACK, player);
					world._target = player;
				}
				else if (world.getTemplateId() == INSTANCEID_NIGHT)
				{
					Location loc = _spawnsZaken.get(Rnd.get(_spawnsZaken.size()));
					world._zaken = (L2Attackable) addSpawn(ZAKEN_NIGHT, loc.getX(), loc.getY(), loc.getZ(), 32768, false, 0, false, world.getInstanceId());
					_log.warn("Zaken Night:  spawned at: X: " + loc.getX() + " Y: " + loc.getY() + " Z: " + loc.getZ());
					world._zaken.addSkill(SkillData.getInstance().getInfo(4216, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(4217, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(4218, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(4219, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(4220, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(4221, 1));
					world._zaken.setRunning();
					world._zaken.addDamageHate(player, 0, 999);
					world._zaken.getAI().setIntention(AI_INTENTION_ATTACK, player);
					world._target = player;
				}
				
				List<L2PcInstance> players;
				L2Party party = player.getParty();
				if (party == null) // this can happen only if debug is true
				{
					players = new FastList<>();
					players.add(player);
				}
				else if (party.isInCommandChannel())
				{
					players = party.getCommandChannel().getMembers();
				}
				else
				{
					players = party.getMembers();
				}
				
				for (L2PcInstance member : players)
				{
					member.sendPacket(new PlaySound("BS01_A"));
				}
			}
			else if (event.equalsIgnoreCase("ChooseZakenRoom"))
			{
				world._zakenZone = _zones[Rnd.get(_zones.length)];
				spawnBarrels(world.getInstanceId());
				Integer[] barrels = zoneBarrels.get(world._zakenZone);
				world._zakenBarel1 = world._barrels.get(barrels[0] - 1);
				world._zakenBarel2 = world._barrels.get(barrels[1] - 1);
				world._zakenBarel3 = world._barrels.get(barrels[2] - 1);
				world._zakenBarel4 = world._barrels.get(barrels[3] - 1);
			}
			else if (event.equalsIgnoreCase("RandomTarget"))
			{
				L2Character target = getRandomTarget(world._zaken);
				if (target != null)
				{
					world._zaken.reduceHate(world._target, 1000000);
					world._target = target;
					world._zaken.setTarget(world._target);
					world._zaken.addDamageHate(world._target, 0, 1000000);
					world._zaken.getAI().setIntention(AI_INTENTION_ATTACK, world._target);
				}
				startQuestTimer("RandomTarget", 6000, world._zaken, (L2PcInstance) world._target);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getId();
		
		if ((npcId == ZAKEN_DAY) || (npcId == ZAKEN_DAY83) || (npcId == ZAKEN_NIGHT))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof ZWorld)
			{
				ZWorld world = (ZWorld) tmpworld;
				InstanceManager.getInstance().getInstance(world.getInstanceId()).setDuration(300000);
				
				for (L2PcInstance player : world._playersInInstance)
				{
					if (player != null)
					{
						player.sendPacket(new PlaySound("BS02_D"));
					}
				}
				world._playersInInstance.clear();
			}
		}
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getId();
		if ((npcId == ZAKEN_DAY) || (npcId == ZAKEN_DAY83) || (npcId == ZAKEN_NIGHT))
		{
			int skillId = 0;
			
			L2BossZone zone = GrandBossManager.getInstance().getZone(55312, 219168, -3223);
			
			if (zone.isInsideZone(npc))
			{
				L2Character target = isPet ? player.getSummon() : player;
				((L2Attackable) npc).addDamageHate(target, 1, 200);
			}
			if ((player.getZ() > (npc.getZ() - 100)) && (player.getZ() < (npc.getZ() + 100)))
			{
				if (Rnd.get(15) < 1)
				{
					int rnd = Rnd.get((15 * 15));
					if (rnd < 1)
					{
						skillId = 4216;
					}
					else if (rnd < 2)
					{
						skillId = 4217;
					}
					else if (rnd < 4) // Hold
					{
						if ((npcId == ZAKEN_NIGHT) || (npcId == ZAKEN_DAY))
						{
							skillId = 4219;
						}
						else if (npcId == ZAKEN_DAY83)
						{
							skillId = 6690;
						}
					}
					else if (rnd < 8) // Absorb HP MP
					{
						if ((npcId == ZAKEN_NIGHT) || (npcId == ZAKEN_DAY))
						{
							skillId = 4218;
						}
						else if (npcId == ZAKEN_DAY83)
						{
							skillId = 6689;
						}
					}
					else if (rnd < 15) // Deadly Dual-Sword Weapon: Range Attack
					{
						for (L2Character character : npc.getKnownList().getKnownCharactersInRadius(100))
						{
							if (character != player)
							{
								continue;
							}
							if (player != ((L2Attackable) npc).getMostHated())
							{
								if ((npcId == ZAKEN_NIGHT) || (npcId == ZAKEN_DAY))
								{
									skillId = 4221;
								}
								else if (npcId == ZAKEN_DAY83)
								{
									skillId = 6692;
								}
							}
						}
					}
					if (Rnd.get(2) < 1) // Deadly Dual-Sword Weapon
					{
						if (player == ((L2Attackable) npc).getMostHated())
						{
							if ((npcId == ZAKEN_NIGHT) || (npcId == ZAKEN_DAY))
							{
								skillId = 4220;
							}
							else if (npcId == ZAKEN_DAY83)
							{
								skillId = 6691;
							}
						}
					}
					if (skillId != 0)
					{
						npc.setTarget(player);
						npc.doCast(SkillData.getInstance().getInfo(skillId, 1));
					}
				}
			}
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getId();
		if ((npcId == ZAKEN_DAY) || (npcId == ZAKEN_DAY83) || (npcId == ZAKEN_NIGHT))
		{
			if (attacker.getMountType() == MountType.STRIDER)
			{
				int sk_4258 = 0;
				L2Effect[] effects = attacker.getAllEffects();
				if ((effects != null) && (effects.length != 0))
				{
					for (L2Effect e : effects)
					{
						if (e.getSkill().getId() == 4258)
						{
							sk_4258 = 1;
						}
					}
				}
				if (sk_4258 == 0)
				{
					npc.setTarget(attacker);
					npc.doCast(SkillData.getInstance().getInfo(4258, 1));
				}
			}
			L2Character originalAttacker = isPet ? attacker.getSummon() : attacker;
			int hate = (int) (((damage / npc.getMaxHp()) / 0.05) * 20000);
			((L2Attackable) npc).addDamageHate(originalAttacker, 0, hate);
			if (Rnd.get(10) < 1)
			{
				int i0 = Rnd.get((15 * 15));
				int skillId = 0;
				if (i0 < 1)
				{
					skillId = 4216;
				}
				else if (i0 < 2)
				{
					skillId = 4217;
				}
				else if (i0 < 4) // Hold
				{
					if ((npcId == ZAKEN_NIGHT) || (npcId == ZAKEN_DAY))
					{
						skillId = 4219;
					}
					else if (npcId == ZAKEN_DAY83)
					{
						skillId = 6690;
					}
				}
				else if (i0 < 8)// Absorb HP MP
				{
					if ((npcId == ZAKEN_NIGHT) || (npcId == ZAKEN_DAY))
					{
						skillId = 4218;
					}
					else if (npcId == ZAKEN_DAY83)
					{
						skillId = 6689;
					}
				}
				else if (i0 < 15) // Deadly Dual-Sword Weapon: Range Attack
				{
					for (L2Character character : npc.getKnownList().getKnownCharactersInRadius(100))
					{
						if (character != attacker)
						{
							continue;
						}
						if (attacker != ((L2Attackable) npc).getMostHated())
						{
							if ((npcId == ZAKEN_NIGHT) || (npcId == ZAKEN_DAY))
							{
								skillId = 4221;
							}
							else if (npcId == ZAKEN_DAY83)
							{
								skillId = 6692;
							}
						}
					}
				}
				if (Rnd.get(2) < 1) // Deadly Dual-Sword Weapon
				{
					if (attacker == ((L2Attackable) npc).getMostHated())
					{
						if ((npcId == ZAKEN_NIGHT) || (npcId == ZAKEN_DAY))
						{
							skillId = 4220;
						}
						else if (npcId == ZAKEN_DAY83)
						{
							skillId = 6691;
						}
					}
				}
				if (skillId != 0)
				{
					npc.setTarget(attacker);
					npc.doCast(SkillData.getInstance().getInfo(skillId, 1));
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
		
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		int npcId = npc.getId();
		if ((npcId == ZAKEN_DAY) || (npcId == ZAKEN_DAY83) || (npcId == ZAKEN_NIGHT))
		{
			if (skill.getAggroPoints() > 0)
			{
				((L2Attackable) npc).addDamageHate(caster, 0, (((skill.getAggroPoints() / npc.getMaxHp()) * 10) * 150));
			}
			
			if (Rnd.get(12) < 1)
			{
				int i0 = Rnd.get((15 * 15));
				int skillId = 0;
				if (i0 < 1)
				{
					skillId = 4216;
				}
				else if (i0 < 2)
				{
					skillId = 4217;
				}
				else if (i0 < 4) // Hold
				{
					if ((npcId == ZAKEN_NIGHT) || (npcId == ZAKEN_DAY))
					{
						skillId = 4219;
					}
					else if (npcId == ZAKEN_DAY83)
					{
						skillId = 6690;
					}
				}
				else if (i0 < 8)// Absorb HP MP
				{
					if ((npcId == ZAKEN_NIGHT) || (npcId == ZAKEN_DAY))
					{
						skillId = 4218;
					}
					else if (npcId == ZAKEN_DAY83)
					{
						skillId = 6689;
					}
				}
				else if (i0 < 15) // Deadly Dual-Sword Weapon: Range Attack
				{
					for (L2Character character : npc.getKnownList().getKnownCharactersInRadius(100))
					{
						if (character != caster)
						{
							continue;
						}
						if (caster != ((L2Attackable) npc).getMostHated())
						{
							if ((npcId == ZAKEN_NIGHT) || (npcId == ZAKEN_DAY))
							{
								skillId = 4221;
							}
							else if (npcId == ZAKEN_DAY83)
							{
								skillId = 6692;
							}
						}
					}
				}
				if (Rnd.get(2) < 1) // Deadly Dual-Sword Weapon
				{
					if (caster == ((L2Attackable) npc).getMostHated())
					{
						if ((npcId == ZAKEN_NIGHT) || (npcId == ZAKEN_DAY))
						{
							skillId = 4220;
						}
						else if (npcId == ZAKEN_DAY83)
						{
							skillId = 6691;
						}
					}
				}
				if (skillId != 0)
				{
					npc.setTarget(caster);
					npc.doCast(SkillData.getInstance().getInfo(skillId, 1));
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character instanceof L2PcInstance)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(character.getInstanceId());
			if (tmpworld instanceof ZWorld)
			{
				ZWorld world = (ZWorld) tmpworld;
				if (zone.getId() == world._zakenZone)
				{
					world._playersInZakenZone.add((L2PcInstance) character);
				}
			}
		}
		return super.onEnterZone(character, zone);
	}
	
	@Override
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		if (character instanceof L2PcInstance)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(character.getInstanceId());
			if (tmpworld instanceof ZWorld)
			{
				ZWorld world = (ZWorld) tmpworld;
				if (zone.getId() == world._zakenZone)
				{
					world._playersInZakenZone.remove(character);
				}
			}
		}
		return super.onExitZone(character, zone);
	}
	
	public static void main(String[] args)
	{
		new Zaken();
	}
}