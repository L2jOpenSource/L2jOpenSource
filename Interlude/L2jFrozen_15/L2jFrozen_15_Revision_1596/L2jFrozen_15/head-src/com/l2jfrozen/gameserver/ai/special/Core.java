package com.l2jfrozen.gameserver.ai.special;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.util.random.Rnd;

/**
 * Core AI
 * @author qwerty
 */
public class Core extends Quest implements Runnable
{
	private static final int CORE = 29006;
	private static final int DEATH_KNIGHT = 29007;
	private static final int DOOM_WRAITH = 29008;
	// private static final int DICOR = 29009;
	// private static final int VALIDUS = 29010;
	private static final int SUSCEPTOR = 29011;
	// private static final int PERUM = 29012;
	// private static final int PREMO = 29013;
	
	// CORE Status Tracking :
	private static final byte ALIVE = 0; // Core is spawned.
	private static final byte DEAD = 1; // Core has been killed.
	
	private static boolean firstAttacked;
	
	List<L2Attackable> minions = new ArrayList<>();
	
	// private static final Logger LOGGER = Logger.getLogger(Core.class);
	
	public Core(final int id, final String name, final String descr)
	{
		super(id, name, descr);
		
		final int[] mobs =
		{
			CORE,
			DEATH_KNIGHT,
			DOOM_WRAITH,
			SUSCEPTOR
		};
		
		for (final int mob : mobs)
		{
			addEventId(mob, Quest.QuestEventType.ON_KILL);
			addEventId(mob, Quest.QuestEventType.ON_ATTACK);
		}
		
		firstAttacked = false;
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(CORE);
		
		final Integer status = GrandBossManager.getInstance().getBossStatus(CORE);
		
		if (status == DEAD)
		{
			// load the unlock date and time for Core from DB
			final long temp = info.getLong("respawn_time") - System.currentTimeMillis();
			if (temp > 0)
			{
				startQuestTimer("core_unlock", temp, null, null);
			}
			else
			{
				// the time has already expired while the server was offline. Immediately spawn Core.
				final L2GrandBossInstance core = (L2GrandBossInstance) addSpawn(CORE, 17726, 108915, -6480, 0, false, 0);
				if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					Announcements.getInstance().announceToAll("Raid boss " + core.getName() + " spawned in world.");
				}
				GrandBossManager.getInstance().setBossStatus(CORE, ALIVE);
				spawnBoss(core);
			}
		}
		else
		{
			final String test = loadGlobalQuestVar("Core_Attacked");
			if (test.equalsIgnoreCase("true"))
			{
				firstAttacked = true;
			}
			/*
			 * int loc_x = info.getInteger("loc_x"); int loc_y = info.getInteger("loc_y"); int loc_z = info.getInteger("loc_z"); int heading = info.getInteger("heading"); int hp = info.getInteger("currentHP"); int mp = info.getInteger("currentMP"); L2GrandBossInstance core = (L2GrandBossInstance)
			 * addSpawn(CORE,loc_x,loc_y,loc_z,heading,false,0); core.setCurrentHpMp(hp,mp);
			 */
			final L2GrandBossInstance core = (L2GrandBossInstance) addSpawn(CORE, 17726, 108915, -6480, 0, false, 0);
			if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
			{
				Announcements.getInstance().announceToAll("Raid boss " + core.getName() + " spawned in world.");
			}
			spawnBoss(core);
		}
	}
	
	@Override
	public void saveGlobalData()
	{
		final String val = "" + firstAttacked;
		saveGlobalQuestVar("Core_Attacked", val);
	}
	
	@Override
	public String onAdvEvent(final String event, final L2NpcInstance npc, final L2PcInstance player)
	{
		final Integer status = GrandBossManager.getInstance().getBossStatus(CORE);
		
		if (event.equalsIgnoreCase("core_unlock"))
		{
			final L2GrandBossInstance core = (L2GrandBossInstance) addSpawn(CORE, 17726, 108915, -6480, 0, false, 0);
			if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
			{
				Announcements.getInstance().announceToAll("Raid boss " + core.getName() + " spawned in world.");
			}
			GrandBossManager.getInstance().setBossStatus(CORE, ALIVE);
			spawnBoss(core);
		}
		else if (status == null)
		{
			
			LOGGER.warn("GrandBoss with Id " + CORE + " has not valid status into GrandBossManager");
			
		}
		else if (event.equalsIgnoreCase("spawn_minion") && status == ALIVE)
		{
			minions.add((L2Attackable) addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0));
		}
		else if (event.equalsIgnoreCase("despawn_minions"))
		{
			for (int i = 0; i < minions.size(); i++)
			{
				final L2Attackable mob = minions.get(i);
				if (mob != null)
				{
					mob.decayMe();
				}
			}
			minions.clear();
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(final L2NpcInstance npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		if (npc.getNpcId() == CORE)
		{
			if (firstAttacked)
			{
				if (Rnd.get(100) == 0)
				{
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "Removing intruders."));
				}
			}
			else
			{
				firstAttacked = true;
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "A non-permitted target has been discovered."));
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "Starting intruder removal system."));
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(final L2NpcInstance npc, final L2PcInstance killer, final boolean isPet)
	{
		final int npcId = npc.getNpcId();
		final String name = npc.getName();
		if (npcId == CORE)
		{
			final int objId = npc.getObjectId();
			npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, objId, npc.getX(), npc.getY(), npc.getZ()));
			npc.broadcastPacket(new CreatureSay(objId, 0, name, "A fatal error has occurred."));
			npc.broadcastPacket(new CreatureSay(objId, 0, name, "System is being shut down..."));
			npc.broadcastPacket(new CreatureSay(objId, 0, name, "......"));
			firstAttacked = false;
			
			if (!npc.getSpawn().isCustomRaidBoss())
			{
				
				addSpawn(31842, 16502, 110165, -6394, 0, false, 900000);
				addSpawn(31842, 18948, 110166, -6397, 0, false, 900000);
				GrandBossManager.getInstance().setBossStatus(CORE, DEAD);
				// time is 60hour +/- 23hour
				final long respawnTime = (Config.CORE_RESP_FIRST + Rnd.get(Config.CORE_RESP_SECOND)) * 3600000;
				startQuestTimer("core_unlock", respawnTime, null, null);
				// also save the respawn time so that the info is maintained past reboots
				final StatsSet info = GrandBossManager.getInstance().getStatsSet(CORE);
				info.set("respawn_time", (System.currentTimeMillis() + respawnTime));
				GrandBossManager.getInstance().setStatsSet(CORE, info);
				startQuestTimer("despawn_minions", 20000, null, null);
				
			}
			
		}
		else
		{
			
			final Integer status = GrandBossManager.getInstance().getBossStatus(CORE);
			
			if (status == ALIVE && minions.contains(npc))
			{
				minions.remove(npc);
				startQuestTimer("spawn_minion", Config.CORE_RESP_MINION * 1000, npc, null);
			}
		}
		
		return super.onKill(npc, killer, isPet);
	}
	
	public void spawnBoss(final L2GrandBossInstance npc)
	{
		GrandBossManager.getInstance().addBoss(npc);
		npc.broadcastPacket(new PlaySound(1, "BS01_A", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
		// Spawn minions
		for (int i = 0; i < 5; i++)
		{
			final int x = 16800 + i * 360;
			minions.add((L2Attackable) addSpawn(DEATH_KNIGHT, x, 110000, npc.getZ(), 280 + Rnd.get(40), false, 0));
			minions.add((L2Attackable) addSpawn(DEATH_KNIGHT, x, 109000, npc.getZ(), 280 + Rnd.get(40), false, 0));
			final int x2 = 16800 + i * 600;
			minions.add((L2Attackable) addSpawn(DOOM_WRAITH, x2, 109300, npc.getZ(), 280 + Rnd.get(40), false, 0));
		}
		for (int i = 0; i < 4; i++)
		{
			final int x = 16800 + i * 450;
			minions.add((L2Attackable) addSpawn(SUSCEPTOR, x, 110300, npc.getZ(), 280 + Rnd.get(40), false, 0));
		}
	}
	
	@Override
	public void run()
	{
	}
}
