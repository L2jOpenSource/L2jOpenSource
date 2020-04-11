package net.sf.l2j.gameserver.scripting.scripts.ai.individual;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.StatsSet;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.GrandBossManager;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.network.NpcStringId;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.skills.L2Skill;

public class Orfen extends L2AttackableAIScript
{
	private static final SpawnLocation[] ORFEN_LOCATION =
	{
		new SpawnLocation(43728, 17220, -4342, 0),
		new SpawnLocation(55024, 17368, -5412, 0),
		new SpawnLocation(53504, 21248, -5486, 0),
		new SpawnLocation(53248, 24576, -5262, 0)
	};
	
	private static final NpcStringId[] ORFEN_CHAT =
	{
		NpcStringId.ID_1000028,
		NpcStringId.ID_1000029,
		NpcStringId.ID_1000030,
		NpcStringId.ID_1000031
	};
	
	private static final int ORFEN = 29014;
	private static final int RAIKEL_LEOS = 29016;
	private static final int RIBA_IREN = 29018;
	
	private static final byte ALIVE = 0;
	private static final byte DEAD = 1;
	
	private boolean _isTeleported;
	
	public Orfen()
	{
		super("ai/individual");
		
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(ORFEN);
		final int status = GrandBossManager.getInstance().getBossStatus(ORFEN);
		
		if (status == DEAD)
		{
			// load the unlock date and time for Orfen from DB
			long temp = info.getLong("respawn_time") - System.currentTimeMillis();
			if (temp > 0)
			{
				// The time has not yet expired. Mark Orfen as currently locked (dead).
				startQuestTimer("orfen_unlock", null, null, temp);
			}
			else
			{
				// The time has already expired while the server was offline. Spawn Orfen in a random place.
				final GrandBoss orfen = (GrandBoss) addSpawn(ORFEN, ORFEN_LOCATION[Rnd.get(1, 3)], false, 0, false);
				GrandBossManager.getInstance().setBossStatus(ORFEN, ALIVE);
				spawnBoss(orfen);
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
			
			final GrandBoss orfen = (GrandBoss) addSpawn(ORFEN, loc_x, loc_y, loc_z, heading, false, 0, false);
			orfen.setCurrentHpMp(hp, mp);
			spawnBoss(orfen);
		}
	}
	
	@Override
	protected void registerNpcs()
	{
		addAttackId(ORFEN, RIBA_IREN);
		addFactionCallId(RAIKEL_LEOS, RIBA_IREN);
		addKillId(ORFEN);
		addSkillSeeId(ORFEN);
	}
	
	@Override
	public String onTimer(String name, Npc npc, Player player)
	{
		if (name.equalsIgnoreCase("orfen_unlock"))
		{
			final GrandBoss orfen = (GrandBoss) addSpawn(ORFEN, ORFEN_LOCATION[Rnd.get(1, 3)], false, 0, false);
			GrandBossManager.getInstance().setBossStatus(ORFEN, ALIVE);
			spawnBoss(orfen);
		}
		else if (name.equalsIgnoreCase("3001"))
		{
			if (_isTeleported)
			{
				// HPs raised over 95%, instantly random teleport elsewhere. Teleport flag is set back to false.
				if (npc.getCurrentHp() / npc.getMaxHp() > 0.95)
				{
					teleportOrfen(npc, Rnd.get(1, 3));
					
					_isTeleported = false;
				}
				// Orfen already ported once and is lured out of her lair ; teleport her back.
				else if (!npc.isInsideZone(ZoneId.SWAMP))
					teleportOrfen(npc, 0);
			}
		}
		return super.onTimer(name, npc, player);
	}
	
	@Override
	public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet)
	{
		Creature originalCaster = isPet ? caster.getSummon() : caster;
		if (skill.getAggroPoints() > 0 && Rnd.get(100) < 20 && npc.isIn3DRadius(originalCaster, 1000))
		{
			// Random chat.
			npc.broadcastNpcSay(Rnd.get(ORFEN_CHAT), caster.getName());
			
			// Teleport caster near Orfen.
			originalCaster.teleportTo(npc.getPosition(), 0);
			
			// Cast a skill.
			npc.setTarget(originalCaster);
			npc.getCast().doCast(4064, 1);
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	@Override
	public String onFactionCall(Npc npc, Npc caller, Player attacker, boolean isPet)
	{
		if (caller == null || npc == null || npc.getCast().isCastingNow())
			return super.onFactionCall(npc, caller, attacker, isPet);
		
		if (npc.getNpcId() == RAIKEL_LEOS && Rnd.get(100) < 5)
		{
			npc.setTarget(attacker);
			npc.getCast().doCast(4067, 4);
		}
		else if (npc.getNpcId() == RIBA_IREN && caller.getNpcId() != RIBA_IREN && (caller.getCurrentHp() / caller.getMaxHp() < 0.5) && Rnd.get(100) < ((caller.getNpcId() == ORFEN) ? 90 : 10))
		{
			npc.setTarget(caller);
			npc.getCast().doCast(4516, 1);
		}
		return super.onFactionCall(npc, caller, attacker, isPet);
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Playable)
		{
			// Curses
			if (testCursesOnAttack(npc, attacker))
				return null;
			
			if (npc.getNpcId() == ORFEN)
			{
				// Orfen didn't yet teleport, and reached 50% HP.
				if (!_isTeleported && npc.getCurrentHp() / npc.getMaxHp() < 0.5)
				{
					// Set teleport flag to true.
					_isTeleported = true;
					
					// Teleport Orfen to her lair.
					teleportOrfen(npc, 0);
				}
				else if (attacker instanceof Player)
				{
					final double dist = npc.distance3D(attacker);
					if (dist > 300 && dist < 1000 && Rnd.get(100) < 10)
					{
						// Random chat.
						npc.broadcastNpcSay(Rnd.get(ORFEN_CHAT), attacker.getName());
						
						// Teleport caster near Orfen.
						attacker.teleportTo(npc.getPosition(), 0);
						
						// Cast a skill.
						npc.setTarget(attacker);
						npc.getCast().doCast(4063, 1);
					}
					else if (Rnd.get(100) < 20)
					{
						// Cast a skill.
						npc.setTarget(attacker);
						npc.getCast().doCast(4064, 1);
					}
				}
			}
			// RIBA_IREN case, as it's the only other registered.
			else if (npc.getCurrentHp() / npc.getMaxHp() < 0.5)
			{
				// Cast a skill.
				npc.setTarget(attacker);
				npc.getCast().doCast(4516, 1);
			}
		}
		return super.onAttack(npc, attacker, damage, skill);
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		cancelQuestTimers("3001", npc);
		
		npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
		GrandBossManager.getInstance().setBossStatus(ORFEN, DEAD);
		
		long respawnTime = (long) Config.SPAWN_INTERVAL_ORFEN + Rnd.get(-Config.RANDOM_SPAWN_TIME_ORFEN, Config.RANDOM_SPAWN_TIME_ORFEN);
		respawnTime *= 3600000;
		
		startQuestTimer("orfen_unlock", null, null, respawnTime);
		
		// also save the respawn time so that the info is maintained past reboots
		StatsSet info = GrandBossManager.getInstance().getStatsSet(ORFEN);
		info.set("respawn_time", System.currentTimeMillis() + respawnTime);
		GrandBossManager.getInstance().setStatsSet(ORFEN, info);
		
		return super.onKill(npc, killer);
	}
	
	/**
	 * This method is used by Orfen to teleport from one location to another. In all cases, she loses aggro.
	 * @param npc : Orfen in any case.
	 * @param index : The SpawnLocation array index, which is 0 for her lair or 1-3 for desert.
	 */
	private static void teleportOrfen(Npc npc, int index)
	{
		// Clear all aggro.
		((Attackable) npc).getAggroList().clear();
		
		// Retrieve the SpawnLocation.
		final SpawnLocation loc = ORFEN_LOCATION[index];
		
		// Edit the spawn location and teleport the Npc.
		npc.getSpawn().setLoc(loc);
		npc.teleportTo(loc, 0);
	}
	
	private void spawnBoss(GrandBoss npc)
	{
		// Reset variable.
		_isTeleported = false;
		
		// Add boss to GrandBossManager.
		GrandBossManager.getInstance().addBoss(npc);
		
		// Broadcast spawn sound.
		npc.broadcastPacket(new PlaySound(1, "BS01_A", npc));
		
		// Fire a 10s task to check Orfen status.
		startQuestTimerAtFixedRate("3001", npc, null, 10000);
	}
}