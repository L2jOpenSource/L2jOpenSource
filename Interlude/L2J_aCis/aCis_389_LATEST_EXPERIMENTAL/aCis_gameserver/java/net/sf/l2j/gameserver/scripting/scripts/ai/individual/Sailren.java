package net.sf.l2j.gameserver.scripting.scripts.ai.individual;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.StatsSet;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.GrandBossManager;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.model.zone.type.BossZone;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SpecialCamera;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.skills.L2Skill;

public class Sailren extends L2AttackableAIScript
{
	private static final BossZone SAILREN_LAIR = ZoneManager.getInstance().getZoneById(110011, BossZone.class);
	
	public static final int SAILREN = 29065;
	
	public static final byte DORMANT = 0; // No one has entered yet. Entry is unlocked.
	public static final byte FIGHTING = 1; // A group entered in the nest. Entry is locked.
	public static final byte DEAD = 2; // Sailren has been killed. Entry is locked.
	
	private static final int VELOCIRAPTOR = 22223;
	private static final int PTEROSAUR = 22199;
	private static final int TREX = 22217;
	
	private static final int DUMMY = 32110;
	private static final int CUBE = 32107;
	
	private static final long INTERVAL_CHECK = 600000L; // 10 minutes
	
	private static final SpawnLocation SAILREN_LOC = new SpawnLocation(27549, -6638, -2008, 0);
	
	private final List<Npc> _mobs = new CopyOnWriteArrayList<>();
	private static long _timeTracker = 0;
	
	public Sailren()
	{
		super("ai/individual");
		
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(SAILREN);
		
		switch (GrandBossManager.getInstance().getBossStatus(SAILREN))
		{
			case DEAD: // Launch the timer to set DORMANT, or set DORMANT directly if timer expired while offline.
				final long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
				if (temp > 0)
					startQuestTimer("unlock", null, null, temp);
				else
					GrandBossManager.getInstance().setBossStatus(SAILREN, DORMANT);
				break;
			
			case FIGHTING:
				final int loc_x = info.getInteger("loc_x");
				final int loc_y = info.getInteger("loc_y");
				final int loc_z = info.getInteger("loc_z");
				final int heading = info.getInteger("heading");
				final int hp = info.getInteger("currentHP");
				final int mp = info.getInteger("currentMP");
				
				final Npc sailren = addSpawn(SAILREN, loc_x, loc_y, loc_z, heading, false, 0, false);
				GrandBossManager.getInstance().addBoss((GrandBoss) sailren);
				_mobs.add(sailren);
				
				sailren.setCurrentHpMp(hp, mp);
				sailren.forceRunStance();
				
				// Don't need to edit _timeTracker, as it's initialized to 0.
				startQuestTimerAtFixedRate("inactivity", null, null, INTERVAL_CHECK);
				break;
		}
	}
	
	@Override
	protected void registerNpcs()
	{
		addAttackId(VELOCIRAPTOR, PTEROSAUR, TREX, SAILREN);
		addKillId(VELOCIRAPTOR, PTEROSAUR, TREX, SAILREN);
	}
	
	@Override
	public String onTimer(String name, Npc npc, Player player)
	{
		if (name.equalsIgnoreCase("beginning"))
		{
			_timeTracker = 0;
			
			for (int i = 0; i < 3; i++)
			{
				final Npc temp = addSpawn(VELOCIRAPTOR, SAILREN_LOC, true, 0, false);
				temp.getAI().tryTo(IntentionType.ACTIVE, null, null);
				temp.forceRunStance();
				_mobs.add(temp);
			}
			startQuestTimerAtFixedRate("inactivity", null, null, INTERVAL_CHECK);
		}
		else if (name.equalsIgnoreCase("spawn"))
		{
			// Dummy spawn used to cast the skill. Despawned after 26sec.
			final Npc temp = addSpawn(DUMMY, SAILREN_LOC, false, 26000, false);
			
			// Cast skill every 2,5sec.
			SAILREN_LAIR.broadcastPacket(new MagicSkillUse(npc, npc, 5090, 1, 2500, 0));
			startQuestTimerAtFixedRate("skill", temp, null, 2500);
			
			// Cinematic, meanwhile.
			SAILREN_LAIR.broadcastPacket(new SpecialCamera(temp.getObjectId(), 60, 110, 30, 4000, 4000, 0, 65, 1, 0)); // 4sec
			
			startQuestTimer("camera_0", temp, null, 3900); // 3sec
			startQuestTimer("camera_1", temp, null, 6800); // 3sec
			startQuestTimer("camera_2", temp, null, 9700); // 3sec
			startQuestTimer("camera_3", temp, null, 12600); // 3sec
			startQuestTimer("camera_4", temp, null, 15500); // 3sec
			startQuestTimer("camera_5", temp, null, 18400); // 7sec
		}
		else if (name.equalsIgnoreCase("skill"))
			SAILREN_LAIR.broadcastPacket(new MagicSkillUse(npc, npc, 5090, 1, 2500, 0));
		else if (name.equalsIgnoreCase("camera_0"))
			SAILREN_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 100, 180, 30, 3000, 3000, 0, 50, 1, 0));
		else if (name.equalsIgnoreCase("camera_1"))
			SAILREN_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 150, 270, 25, 3000, 3000, 0, 30, 1, 0));
		else if (name.equalsIgnoreCase("camera_2"))
			SAILREN_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 160, 360, 20, 3000, 3000, 10, 15, 1, 0));
		else if (name.equalsIgnoreCase("camera_3"))
			SAILREN_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 160, 450, 10, 3000, 3000, 0, 10, 1, 0));
		else if (name.equalsIgnoreCase("camera_4"))
		{
			SAILREN_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 160, 560, 0, 3000, 3000, 0, 10, 1, 0));
			
			final Npc temp = addSpawn(SAILREN, SAILREN_LOC, false, 0, false);
			GrandBossManager.getInstance().addBoss((GrandBoss) temp);
			_mobs.add(temp);
			
			// Stop skill task.
			cancelQuestTimers("skill");
			SAILREN_LAIR.broadcastPacket(new MagicSkillUse(npc, npc, 5091, 1, 2500, 0));
			
			temp.broadcastPacket(new SocialAction(temp, 2));
		}
		else if (name.equalsIgnoreCase("camera_5"))
			SAILREN_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 70, 560, 0, 500, 7000, -15, 10, 1, 0));
		else if (name.equalsIgnoreCase("unlock"))
			GrandBossManager.getInstance().setBossStatus(SAILREN, DORMANT);
		else if (name.equalsIgnoreCase("inactivity"))
		{
			// 10 minutes without any attack activity leads to a reset.
			if ((System.currentTimeMillis() - _timeTracker) >= INTERVAL_CHECK)
			{
				// Set it dormant.
				GrandBossManager.getInstance().setBossStatus(SAILREN, DORMANT);
				
				// Delete all monsters and clean the list.
				if (!_mobs.isEmpty())
				{
					_mobs.forEach(Npc::deleteMe);
					_mobs.clear();
				}
				
				// Oust all players from area.
				SAILREN_LAIR.oustAllPlayers();
				
				// Cancel inactivity task.
				cancelQuestTimers("inactivity");
			}
		}
		else if (name.equalsIgnoreCase("oust"))
		{
			// Oust all players from area.
			SAILREN_LAIR.oustAllPlayers();
		}
		return super.onTimer(name, npc, player);
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		if (killer instanceof Playable)
		{
			final Player player = killer.getActingPlayer();
			if (player == null || !_mobs.contains(npc) || !SAILREN_LAIR.getAllowedPlayers().contains(player.getObjectId()))
				return null;
		}
		
		switch (npc.getNpcId())
		{
			case VELOCIRAPTOR:
				// Once the 3 Velociraptors are dead, spawn a Pterosaur.
				if (_mobs.remove(npc) && _mobs.isEmpty())
				{
					final Npc temp = addSpawn(PTEROSAUR, SAILREN_LOC, false, 0, false);
					temp.forceRunStance();
					temp.getAI().tryTo(IntentionType.ATTACK, killer, null);
					_mobs.add(temp);
				}
				break;
			
			case PTEROSAUR:
				// Pterosaur is dead, spawn a Trex.
				if (_mobs.remove(npc))
				{
					final Npc temp = addSpawn(TREX, SAILREN_LOC, false, 0, false);
					temp.forceRunStance();
					temp.getAI().tryTo(IntentionType.ATTACK, killer, null);
					temp.broadcastNpcSay("?");
					_mobs.add(temp);
				}
				break;
			
			case TREX:
				// Trex is dead, wait 5min and spawn Sailren.
				if (_mobs.remove(npc))
					startQuestTimer("spawn", npc, null, Config.WAIT_TIME_SAILREN);
				break;
			
			case SAILREN:
				if (_mobs.remove(npc))
				{
					// Set Sailren as dead.
					GrandBossManager.getInstance().setBossStatus(SAILREN, DEAD);
					
					// Spawn the Teleport Cube for 10min.
					addSpawn(CUBE, npc, false, INTERVAL_CHECK, false);
					
					// Cancel inactivity task.
					cancelQuestTimers("inactivity");
					
					long respawnTime = (long) Config.SPAWN_INTERVAL_SAILREN + Rnd.get(-Config.RANDOM_SPAWN_TIME_SAILREN, Config.RANDOM_SPAWN_TIME_SAILREN);
					respawnTime *= 3600000;
					
					startQuestTimer("oust", null, null, INTERVAL_CHECK);
					startQuestTimer("unlock", null, null, respawnTime);
					
					// Save the respawn time so that the info is maintained past reboots.
					final StatsSet info = GrandBossManager.getInstance().getStatsSet(SAILREN);
					info.set("respawn_time", System.currentTimeMillis() + respawnTime);
					GrandBossManager.getInstance().setStatsSet(SAILREN, info);
				}
				break;
		}
		
		return super.onKill(npc, killer);
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Playable)
		{
			final Player player = attacker.getActingPlayer();
			if (player == null || !_mobs.contains(npc) || !SAILREN_LAIR.getAllowedPlayers().contains(player.getObjectId()))
				return null;
			
			// Curses
			if (testCursesOnAttack(npc, attacker, SAILREN))
				return null;
			
			// Refresh timer on every hit.
			_timeTracker = System.currentTimeMillis();
		}
		return super.onAttack(npc, attacker, damage, skill);
	}
}