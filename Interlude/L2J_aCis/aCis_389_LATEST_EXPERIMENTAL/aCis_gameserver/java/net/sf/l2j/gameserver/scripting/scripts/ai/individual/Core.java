package net.sf.l2j.gameserver.scripting.scripts.ai.individual;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.StatsSet;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.GrandBossManager;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.skills.L2Skill;

public class Core extends L2AttackableAIScript
{
	private static final int CORE = 29006;
	private static final int DEATH_KNIGHT = 29007;
	private static final int DOOM_WRAITH = 29008;
	private static final int SUSCEPTOR = 29011;
	
	private static final byte ALIVE = 0; // Core is spawned.
	private static final byte DEAD = 1; // Core has been killed.
	
	private final List<Monster> _minions = new ArrayList<>();
	
	public Core()
	{
		super("ai/individual");
		
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(CORE);
		final int status = GrandBossManager.getInstance().getBossStatus(CORE);
		if (status == DEAD)
		{
			// load the unlock date and time for Core from DB
			final long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
			if (temp > 0)
			{
				// The time has not yet expired. Mark Core as currently locked (dead).
				startQuestTimer("core_unlock", null, null, temp);
			}
			else
			{
				// The time has expired while the server was offline. Spawn Core.
				final GrandBoss core = (GrandBoss) addSpawn(CORE, 17726, 108915, -6480, 0, false, 0, false);
				GrandBossManager.getInstance().setBossStatus(CORE, ALIVE);
				spawnBoss(core);
			}
		}
		else
		{
			final int loc_x = info.getInteger("loc_x");
			final int loc_y = info.getInteger("loc_y");
			final int loc_z = info.getInteger("loc_z");
			final int heading = info.getInteger("heading");
			final int hp = info.getInteger("currentHP");
			final int mp = info.getInteger("currentMP");
			
			final GrandBoss core = (GrandBoss) addSpawn(CORE, loc_x, loc_y, loc_z, heading, false, 0, false);
			core.setCurrentHpMp(hp, mp);
			spawnBoss(core);
		}
	}
	
	@Override
	protected void registerNpcs()
	{
		addAttackId(CORE);
		addKillId(CORE, DEATH_KNIGHT, DOOM_WRAITH, SUSCEPTOR);
	}
	
	public void spawnBoss(GrandBoss npc)
	{
		GrandBossManager.getInstance().addBoss(npc);
		npc.broadcastPacket(new PlaySound(1, "BS01_A", npc));
		
		// Spawn minions
		Monster monster;
		for (int i = 0; i < 5; i++)
		{
			int x = 16800 + i * 360;
			monster = (Monster) addSpawn(DEATH_KNIGHT, x, 110000, npc.getZ(), 280 + Rnd.get(40), false, 0, false);
			monster.setMinion(true);
			_minions.add(monster);
			monster = (Monster) addSpawn(DEATH_KNIGHT, x, 109000, npc.getZ(), 280 + Rnd.get(40), false, 0, false);
			monster.setMinion(true);
			_minions.add(monster);
			int x2 = 16800 + i * 600;
			monster = (Monster) addSpawn(DOOM_WRAITH, x2, 109300, npc.getZ(), 280 + Rnd.get(40), false, 0, false);
			monster.setMinion(true);
			_minions.add(monster);
		}
		
		for (int i = 0; i < 4; i++)
		{
			int x = 16800 + i * 450;
			monster = (Monster) addSpawn(SUSCEPTOR, x, 110300, npc.getZ(), 280 + Rnd.get(40), false, 0, false);
			monster.setMinion(true);
			_minions.add(monster);
		}
	}
	
	@Override
	public String onTimer(String name, Npc npc, Player player)
	{
		if (name.equalsIgnoreCase("core_unlock"))
		{
			final GrandBoss core = (GrandBoss) addSpawn(CORE, 17726, 108915, -6480, 0, false, 0, false);
			GrandBossManager.getInstance().setBossStatus(CORE, ALIVE);
			spawnBoss(core);
		}
		else if (name.equalsIgnoreCase("spawn_minion"))
		{
			final Monster monster = (Monster) addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false);
			monster.setMinion(true);
			_minions.add(monster);
		}
		else if (name.equalsIgnoreCase("despawn_minions"))
		{
			_minions.forEach(Npc::deleteMe);
			_minions.clear();
		}
		return super.onTimer(name, npc, player);
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Playable)
		{
			if (npc.isScriptValue(1))
			{
				if (Rnd.get(100) == 0)
					npc.broadcastNpcSay("Removing intruders.");
			}
			else
			{
				npc.setScriptValue(1);
				npc.broadcastNpcSay("A non-permitted target has been discovered.");
				npc.broadcastNpcSay("Starting intruder removal system.");
			}
		}
		return super.onAttack(npc, attacker, damage, skill);
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		if (npc.getNpcId() == CORE)
		{
			npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
			npc.broadcastNpcSay("A fatal error has occurred.");
			npc.broadcastNpcSay("System is being shut down...");
			npc.broadcastNpcSay("......");
			
			addSpawn(31842, 16502, 110165, -6394, 0, false, 900000, false);
			addSpawn(31842, 18948, 110166, -6397, 0, false, 900000, false);
			GrandBossManager.getInstance().setBossStatus(CORE, DEAD);
			
			long respawnTime = (long) Config.SPAWN_INTERVAL_CORE + Rnd.get(-Config.RANDOM_SPAWN_TIME_CORE, Config.RANDOM_SPAWN_TIME_CORE);
			respawnTime *= 3600000;
			
			startQuestTimer("core_unlock", null, null, respawnTime);
			
			final StatsSet info = GrandBossManager.getInstance().getStatsSet(CORE);
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatsSet(CORE, info);
			startQuestTimer("despawn_minions", null, null, 20000);
			cancelQuestTimers("spawn_minion");
		}
		else if (GrandBossManager.getInstance().getBossStatus(CORE) == ALIVE && _minions != null && _minions.contains(npc))
		{
			_minions.remove(npc);
			startQuestTimer("spawn_minion", npc, null, 60000);
		}
		return super.onKill(npc, killer);
	}
}