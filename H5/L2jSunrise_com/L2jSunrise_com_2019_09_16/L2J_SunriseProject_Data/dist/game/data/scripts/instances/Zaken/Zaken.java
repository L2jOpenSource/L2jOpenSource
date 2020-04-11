package instances.Zaken;

import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_ATTACK;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import l2r.Config;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.enums.MountType;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.enums.audio.Music;
import l2r.gameserver.instancemanager.GrandBossManager;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.zone.L2ZoneForm;
import l2r.gameserver.model.zone.L2ZoneType;
import l2r.gameserver.model.zone.type.L2BossZone;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

import ai.npc.AbstractNpcAI;

public class Zaken extends AbstractNpcAI
{
	private class ZWorld extends InstanceWorld
	{
		L2Attackable _zaken;
		
		public ZWorld()
		{
		}
	}
	
	private static final int INSTANCEID_NIGHT = 114; // this is the client number
	
	// NPCs
	// Bosses
	private static final int ZAKEN_NIGHT = 29022;
	// Mobs
	private static final int DOLL_BLADER = 29023;
	private static final int VALE_MASTER = 29024;
	private static final int ZOMBIE_CAPTAIN = 29026;
	private static final int ZOMBIE = 29027;
	// Skills
	private static final SkillHolder ANTI_STRIDER = new SkillHolder(4258, 1); // Hinder Strider
	// Locations
	private static final int PATHFINDER = 32713;
	private static final Location ENTER_TELEPORT = new Location(52680, 219088, -3232);
	
	private static final List<Location> _spawnPcLocationNighttime = new CopyOnWriteArrayList<>();
	
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
	
	private static final List<Location> _spawnsZaken = new CopyOnWriteArrayList<>();
	
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
		super(Zaken.class.getSimpleName(), "instances");
		addStartNpc(PATHFINDER);
		addTalkId(PATHFINDER);
		
		addKillId(ZAKEN_NIGHT);
		addAttackId(ZAKEN_NIGHT);
		addAggroRangeEnterId(ZAKEN_NIGHT);
	}
	
	private boolean checkConditions(L2PcInstance player)
	{
		if (player.isGM() || player.canOverrideCond(PcCondOverride.INSTANCE_CONDITIONS))
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
		int minMembers = Config.ZAKEN_MINMEMBERS_NIGHTTIME;
		int maxMembers = Config.ZAKEN_MAXMEMBERS_NIGHTTIME;
		
		if (party.isInCommandChannel())
		{
			members = party.getCommandChannel().getMembers();
		}
		else
		{
			members = party.getMembers();
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
			if (!Util.checkIfInRange(1000, player, member, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_LOCATION_WHICH_CANNOT_BE_ENTERED_THEREFORE_IT_CANNOT_BE_PROCESSED);
				sm.addPcName(member);
				party.broadcastPacket(sm);
				return false;
			}
			
			Long reenterTime = 0L;
			reenterTime = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), INSTANCEID_NIGHT);
			
			if (System.currentTimeMillis() < reenterTime)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_RE_ENTER_YET);
				sm.addPcName(member);
				party.broadcastPacket(sm);
				return false;
			}
		}
		return true;
	}
	
	protected int enterInstance(L2PcInstance player, String template, Location loc)
	{
		int instanceId = 0;
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (!(world instanceof ZWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ENTERED_ANOTHER_INSTANT_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON));
				return 0;
			}
			teleportPlayer(player, loc, world.getInstanceId(), false);
			return world.getInstanceId();
		}
		
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new ZWorld();
		world.setTemplateId(INSTANCEID_NIGHT);
		world.setInstanceId(instanceId);
		world.setStatus(0);
		InstanceManager.getInstance().addWorld(world);
		_log.info("Zaken Night started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		
		startQuestTimer("ZakenSpawn", 1000, null, player);
		for (int i = 120111; i <= 120125; i++)
		{
			spawnRoom(world.getInstanceId(), i);
		}
		
		// teleport players
		List<L2PcInstance> players = new LinkedList<>();
		L2Party party = player.getParty();
		if (party == null)
		{
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
			if (!checkConditions(member))
			{
				return 0;
			}
		}
		
		int count = 1;
		for (L2PcInstance member : players)
		{
			_log.info("Zaken Party Member " + count + ", Name is: " + member.getName());
			count++;
			teleportPlayer(member, loc, world.getInstanceId(), false);
			world.addAllowed(member.getObjectId());
			playSound(member, Music.BS01_A_10000);
			savePlayerReenter(member);
		}
		
		return instanceId;
	}
	
	private void spawnRoom(int instanceId, int zoneId)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(instanceId);
		if (tmpworld instanceof ZWorld)
		{
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
				((L2Attackable) addSpawn(ZOMBIE, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId)).setIsRaidMinion(true);
			}
			for (int i = 1; i <= 3; i++)
			{
				position = zoneform.getRandomPoint();
				((L2Attackable) addSpawn(DOLL_BLADER, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId)).setIsRaidMinion(true);
			}
			for (int i = 1; i <= 2; i++)
			{
				((L2Attackable) addSpawn(VALE_MASTER, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId)).setIsRaidMinion(true);
			}
			for (int i = 1; i <= 2; i++)
			{
				position = zoneform.getRandomPoint();
				((L2Attackable) addSpawn(ZOMBIE_CAPTAIN, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId)).setIsRaidMinion(true);
			}
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("nighttime"))
		{
			if (!player.isGM() || !player.canOverrideCond(PcCondOverride.INSTANCE_CONDITIONS))
			{
				L2Party party = player.getParty();
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
				else if (event.equalsIgnoreCase("nighttime") && party.isInCommandChannel() && (party.getMemberCount() < 7))
				{
					return "no-minimum-party.html";
				}
			}
			
			enterInstance(player, "Zakennighttime.xml", ENTER_TELEPORT);
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
			if (event.equalsIgnoreCase("ZakenSpawn"))
			{
				if (world.getTemplateId() == INSTANCEID_NIGHT)
				{
					Location loc = _spawnsZaken.get(Rnd.get(_spawnsZaken.size()));
					world._zaken = (L2Attackable) addSpawn(ZAKEN_NIGHT, loc.getX(), loc.getY(), loc.getZ(), 32768, false, 0, false, world.getInstanceId());
					_log.info("Zaken Night: spawned at: X: " + loc.getX() + " Y: " + loc.getY() + " Z: " + loc.getZ());
					world._zaken.addSkill(SkillData.getInstance().getInfo(4216, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(4217, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(4218, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(4219, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(4220, 1));
					world._zaken.addSkill(SkillData.getInstance().getInfo(4221, 1));
					world._zaken.setRunning();
					world._zaken.addDamageHate(player, 0, 999);
					world._zaken.getAI().setIntention(AI_INTENTION_ATTACK, player);
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getId();
		
		if (npcId == ZAKEN_NIGHT)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof ZWorld)
			{
				ZWorld world = (ZWorld) tmpworld;
				InstanceManager.getInstance().getInstance(world.getInstanceId()).setDuration(300000);
				
				for (Integer objectId : world.getAllowed())
				{
					final L2PcInstance player = L2World.getInstance().getPlayer(objectId);
					if (player != null)
					{
						playSound(player, Music.BS01_D_10000);
					}
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getId();
		if (npcId == ZAKEN_NIGHT)
		{
			L2BossZone zone = GrandBossManager.getInstance().getZone(55312, 219168, -3223);
			if ((zone != null) && zone.isInsideZone(npc))
			{
				L2Character target = isPet ? player.getSummon() : player;
				((L2Attackable) npc).addDamageHate(target, 1, 200);
			}
			
			if ((player.getZ() > (npc.getZ() - 100)) && (player.getZ() < (npc.getZ() + 100)))
			{
				castSkill(player, npc);
			}
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getId();
		if (npcId == ZAKEN_NIGHT)
		{
			if ((attacker.getMountType() == MountType.STRIDER) && !attacker.isAffectedBySkill(ANTI_STRIDER.getSkillId()))
			{
				if (!npc.isSkillDisabled(ANTI_STRIDER.getSkill()))
				{
					npc.setTarget(attacker);
					npc.doCast(ANTI_STRIDER.getSkill());
				}
			}
			
			L2Character originalAttacker = isPet ? attacker.getSummon() : attacker;
			int hate = (int) (((damage / npc.getMaxHp()) / 0.05) * 20000);
			((L2Attackable) npc).addDamageHate(originalAttacker, 0, hate);
			castSkill(attacker, npc);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		int npcId = npc.getId();
		if (npcId == ZAKEN_NIGHT)
		{
			if (skill.getAggroPoints() > 0)
			{
				((L2Attackable) npc).addDamageHate(caster, 0, (((skill.getAggroPoints() / npc.getMaxHp()) * 10) * 150));
			}
			
			castSkill(caster, npc);
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	private void castSkill(L2PcInstance player, L2Npc npc)
	{
		if (Rnd.get(15) < 1)
		{
			int skillId = 0;
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
				skillId = 4219;
			}
			else if (rnd < 8) // Absorb HP MP
			{
				skillId = 4218;
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
						skillId = 4221;
					}
				}
			}
			if (Rnd.get(2) < 1) // Deadly Dual-Sword Weapon
			{
				if (player == ((L2Attackable) npc).getMostHated())
				{
					skillId = 4220;
				}
			}
			if (skillId != 0)
			{
				npc.setTarget(player);
				npc.doCast(SkillData.getInstance().getInfo(skillId, 1));
			}
		}
	}
	
	private void savePlayerReenter(L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
		Calendar reuseTime = Calendar.getInstance();
		
		if (tmpworld.getTemplateId() == INSTANCEID_NIGHT)
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
		
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_FROM_HERE_S1_S_ENTRY_HAS_BEEN_RESTRICTED);
		sm.addInstanceName(tmpworld.getTemplateId());
		InstanceManager.getInstance().setInstanceTime(player.getObjectId(), tmpworld.getTemplateId(), reuseTime.getTimeInMillis());
		player.sendPacket(sm);
	}
}