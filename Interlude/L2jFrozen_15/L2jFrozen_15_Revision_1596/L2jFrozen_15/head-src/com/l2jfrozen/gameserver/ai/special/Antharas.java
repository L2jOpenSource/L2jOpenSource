package com.l2jfrozen.gameserver.ai.special;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.model.zone.type.L2BossZone;
import com.l2jfrozen.gameserver.network.serverpackets.Earthquake;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.SpecialCamera;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.random.Rnd;

/**
 * This class ... control for sequence of fight against Antharas.
 * @version $Revision: $ $Date: $
 * @author  L2J_JP SANDMAN
 */
public class Antharas extends Quest implements Runnable
{
	protected static final Logger LOGGER = Logger.getLogger(Antharas.class);
	
	// config
	private static final int FWA_ACTIVITYTIMEOFANTHARAS = 120;
	// private static final int FWA_APPTIMEOFANTHARAS = 1800000;
	// private static final int FWA_INACTIVITYTIME = 900000;
	// private static final boolean FWA_OLDANTHARAS = true; //use antharas interlude with minions
	protected static final boolean FWA_OLDANTHARAS = Config.ANTHARAS_OLD; // use antharas interlude with minions
	private static final boolean FWA_MOVEATRANDOM = true;
	private static final boolean FWA_DOSERVEREARTHQUAKE = true;
	private static final int FWA_LIMITOFWEAK = 45;
	private static final int FWA_LIMITOFNORMAL = 63;
	
	private static final int FWA_MAXMOBS = 10; // this includes Antharas itself
	private static final int FWA_INTERVALOFMOBSWEAK = 180000;
	private static final int FWA_INTERVALOFMOBSNORMAL = 150000;
	private static final int FWA_INTERVALOFMOBSSTRONG = 120000;
	private static final int FWA_PERCENTOFBEHEMOTH = 60;
	private static final int FWA_SELFDESTRUCTTIME = 15000;
	// Location of teleport cube.
	private final int teleportCubeId = 31859;
	private final int teleportCubeLocation[][] =
	{
		{
			177615,
			114941,
			-7709,
			0
		}
	};
	
	protected List<L2Spawn> teleportCubeSpawn = new ArrayList<>();
	protected List<L2NpcInstance> teleportCube = new ArrayList<>();
	
	// Spawn data of monsters.
	protected HashMap<Integer, L2Spawn> monsterSpawn = new HashMap<>();
	
	// Instance of monsters.
	protected List<L2NpcInstance> monsters = new ArrayList<>();
	protected L2GrandBossInstance antharas = null;
	
	// monstersId
	private static final int ANTHARASOLDID = 29019;
	private static final int ANTHARASWEAKID = 29066;
	private static final int ANTHARASNORMALID = 29067;
	private static final int ANTHARASSTRONGID = 29068;
	
	// Tasks.
	protected ScheduledFuture<?> cubeSpawnTask = null;
	protected volatile ScheduledFuture<?> monsterSpawnTask = null;
	protected ScheduledFuture<?> activityCheckTask = null;
	protected ScheduledFuture<?> socialTask = null;
	protected ScheduledFuture<?> mobiliseTask = null;
	protected ScheduledFuture<?> mobsSpawnTask = null;
	protected ScheduledFuture<?> selfDestructionTask = null;
	protected ScheduledFuture<?> moveAtRandomTask = null;
	protected ScheduledFuture<?> movieTask = null;
	
	// Antharas Status Tracking :
	private static final int DORMANT = 0; // Antharas is spawned and no one has entered yet. Entry is unlocked
	private static final int WAITING = 1; // Antharas is spawend and someone has entered, triggering a 30 minute window for additional people to enter
	// before he unleashes his attack. Entry is unlocked
	private static final int FIGHTING = 2; // Antharas is engaged in battle, annihilating his foes. Entry is locked
	private static final int DEAD = 3; // Antharas has been killed. Entry is locked
	
	protected static long LastAction = 0;
	
	protected static L2BossZone zone;
	
	// Boss: Antharas
	public Antharas(final int id, final String name, final String descr)
	{
		super(id, name, descr);
		final int[] mob =
		{
			ANTHARASOLDID,
			ANTHARASWEAKID,
			ANTHARASNORMALID,
			ANTHARASSTRONGID,
			29069,
			29070,
			29071,
			29072,
			29073,
			29074,
			29075,
			29076
		};
		this.registerMobs(mob);
		init();
	}
	
	// Initialize
	private void init()
	{
		// Setting spawn data of monsters.
		try
		{
			zone = GrandBossManager.getInstance().getZone(179700, 113800, -7709);
			L2NpcTemplate template1;
			L2Spawn tempSpawn;
			
			// Old Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASOLDID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			monsterSpawn.put(29019, tempSpawn);
			
			// Weak Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASWEAKID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			monsterSpawn.put(29066, tempSpawn);
			
			// Normal Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASNORMALID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			monsterSpawn.put(29067, tempSpawn);
			
			// Strong Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASSTRONGID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			monsterSpawn.put(29068, tempSpawn);
		}
		catch (final Exception e)
		{
			LOGGER.warn(e.getMessage());
		}
		
		// Setting spawn data of teleport cube.
		try
		{
			final L2NpcTemplate Cube = NpcTable.getInstance().getTemplate(teleportCubeId);
			L2Spawn spawnDat;
			for (final int[] element : teleportCubeLocation)
			{
				spawnDat = new L2Spawn(Cube);
				spawnDat.setAmount(1);
				spawnDat.setLocx(element[0]);
				spawnDat.setLocy(element[1]);
				spawnDat.setLocz(element[2]);
				spawnDat.setHeading(element[3]);
				spawnDat.setRespawnDelay(60);
				spawnDat.setLocation(0);
				SpawnTable.getInstance().addNewSpawn(spawnDat, false);
				teleportCubeSpawn.add(spawnDat);
			}
		}
		catch (final Exception e)
		{
			LOGGER.warn(e.getMessage());
		}
		
		Integer status = GrandBossManager.getInstance().getBossStatus(ANTHARASOLDID);
		
		if (FWA_OLDANTHARAS || status == WAITING)
		{
			final StatsSet info = GrandBossManager.getInstance().getStatsSet(ANTHARASOLDID);
			final Long respawnTime = info.getLong("respawn_time");
			if (status == DEAD && respawnTime <= System.currentTimeMillis())
			{
				// the time has already expired while the server was offline. Immediately spawn antharas in his cave.
				// also, the status needs to be changed to DORMANT
				GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID, DORMANT);
				status = DORMANT;
			}
			else if (status == FIGHTING)
			{
				final int loc_x = info.getInteger("loc_x");
				final int loc_y = info.getInteger("loc_y");
				final int loc_z = info.getInteger("loc_z");
				final int heading = info.getInteger("heading");
				final int hp = info.getInteger("currentHP");
				final int mp = info.getInteger("currentMP");
				antharas = (L2GrandBossInstance) addSpawn(ANTHARASOLDID, loc_x, loc_y, loc_z, heading, false, 0);
				GrandBossManager.getInstance().addBoss(antharas);
				antharas.setCurrentHpMp(hp, mp);
				LastAction = System.currentTimeMillis();
				// Start repeating timer to check for inactivity
				activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);
			}
			else if (status == DEAD)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new UnlockAntharas(ANTHARASOLDID), respawnTime - System.currentTimeMillis());
			}
			else if (status == DORMANT)
			{
				// Here status is 0 on Database, dont do nothing
			}
			else
			{
				setAntharasSpawnTask();
			}
		}
		else
		{
			final Integer statusWeak = GrandBossManager.getInstance().getBossStatus(ANTHARASWEAKID);
			
			final Integer statusNormal = GrandBossManager.getInstance().getBossStatus(ANTHARASNORMALID);
			
			final Integer statusStrong = GrandBossManager.getInstance().getBossStatus(ANTHARASSTRONGID);
			
			int antharasId = 0;
			if (statusWeak == FIGHTING || statusWeak == DEAD)
			{
				antharasId = ANTHARASWEAKID;
				status = statusWeak;
			}
			else if (statusNormal == FIGHTING || statusNormal == DEAD)
			{
				antharasId = ANTHARASNORMALID;
				status = statusNormal;
			}
			else if (statusStrong == FIGHTING || statusStrong == DEAD)
			{
				antharasId = ANTHARASSTRONGID;
				status = statusStrong;
			}
			if (antharasId != 0 && status == FIGHTING)
			{
				final StatsSet info = GrandBossManager.getInstance().getStatsSet(antharasId);
				final int loc_x = info.getInteger("loc_x");
				final int loc_y = info.getInteger("loc_y");
				final int loc_z = info.getInteger("loc_z");
				final int heading = info.getInteger("heading");
				final int hp = info.getInteger("currentHP");
				final int mp = info.getInteger("currentMP");
				antharas = (L2GrandBossInstance) addSpawn(antharasId, loc_x, loc_y, loc_z, heading, false, 0);
				GrandBossManager.getInstance().addBoss(antharas);
				antharas.setCurrentHpMp(hp, mp);
				LastAction = System.currentTimeMillis();
				// Start repeating timer to check for inactivity
				activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);
			}
			else if (antharasId != 0 && status == DEAD)
			{
				final StatsSet info = GrandBossManager.getInstance().getStatsSet(antharasId);
				final Long respawnTime = info.getLong("respawn_time");
				if (respawnTime <= System.currentTimeMillis())
				{
					// the time has already expired while the server was offline. Immediately spawn antharas in his cave.
					// also, the status needs to be changed to DORMANT
					GrandBossManager.getInstance().setBossStatus(antharasId, DORMANT);
					status = DORMANT;
				}
				else
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new UnlockAntharas(antharasId), respawnTime - System.currentTimeMillis());
				}
			}
		}
	}
	
	// Do spawn teleport cube.
	public void spawnCube()
	{
		if (mobsSpawnTask != null)
		{
			mobsSpawnTask.cancel(true);
			mobsSpawnTask = null;
		}
		if (selfDestructionTask != null)
		{
			selfDestructionTask.cancel(true);
			selfDestructionTask = null;
		}
		if (activityCheckTask != null)
		{
			activityCheckTask.cancel(false);
			activityCheckTask = null;
		}
		
		for (final L2Spawn spawnDat : teleportCubeSpawn)
		{
			teleportCube.add(spawnDat.doSpawn());
		}
	}
	
	// Setting Antharas spawn task.
	public void setAntharasSpawnTask()
	{
		if (monsterSpawnTask == null)
		{
			synchronized (this)
			{
				if (monsterSpawnTask == null)
				{
					GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID, WAITING);
					monsterSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(1), 60000 * Config.ANTHARAS_WAIT_TIME);
				}
			}
		}
	}
	
	protected void startMinionSpawns(final int antharasId)
	{
		int intervalOfMobs;
		
		// Interval of minions is decided by the type of Antharas
		// that invaded the lair.
		switch (antharasId)
		{
			case ANTHARASWEAKID:
				intervalOfMobs = FWA_INTERVALOFMOBSWEAK;
				break;
			case ANTHARASNORMALID:
				intervalOfMobs = FWA_INTERVALOFMOBSNORMAL;
				break;
			default:
				intervalOfMobs = FWA_INTERVALOFMOBSSTRONG;
				break;
		}
		
		// Spawn mobs.
		mobsSpawnTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new MobsSpawn(), intervalOfMobs, intervalOfMobs);
	}
	
	// Do spawn Antharas.
	private class AntharasSpawn implements Runnable
	{
		private int taskId = 0;
		private Collection<L2Character> players;
		
		AntharasSpawn(final int taskId)
		{
			this.taskId = taskId;
			if (zone.getCharactersInside() != null)
			{
				players = zone.getCharactersInside().values();
			}
		}
		
		@Override
		public void run()
		{
			int npcId;
			L2Spawn antharasSpawn = null;
			
			switch (taskId)
			{
				case 1: // Spawn.
					// Strength of Antharas is decided by the number of players that
					// invaded the lair.
					monsterSpawnTask.cancel(false);
					monsterSpawnTask = null;
					if (FWA_OLDANTHARAS)
					{
						npcId = 29019; // old
					}
					else if (players == null || players != null && players.size() <= FWA_LIMITOFWEAK)
					{
						npcId = 29066; // weak
					}
					else if (players != null && players.size() > FWA_LIMITOFNORMAL)
					{
						npcId = 29068; // strong
					}
					else
					{
						npcId = 29067; // normal
					}
					
					// Do spawn.
					antharasSpawn = monsterSpawn.get(npcId);
					antharas = (L2GrandBossInstance) antharasSpawn.doSpawn();
					GrandBossManager.getInstance().addBoss(antharas);
					
					monsters.add(antharas);
					antharas.setIsImobilised(true);
					
					GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID, DORMANT);
					GrandBossManager.getInstance().setBossStatus(npcId, FIGHTING);
					LastAction = System.currentTimeMillis();
					// Start repeating timer to check for inactivity
					activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);
					
					// Setting 1st time of minions spawn task.
					if (!FWA_OLDANTHARAS)
					{
						startMinionSpawns(npcId);
					}
					
					// Set next task.
					if (socialTask != null)
					{
						socialTask.cancel(true);
						socialTask = null;
					}
					socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(2), 16);
					break;
				case 2:
					// Set camera.
					broadcastPacket(new SpecialCamera(antharas.getObjectId(), 700, 13, -19, 0, 20000));
					
					// Set next task.
					if (socialTask != null)
					{
						socialTask.cancel(true);
						socialTask = null;
					}
					socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(3), 3000);
					break;
				
				case 3:
					// Do social.
					broadcastPacket(new SpecialCamera(antharas.getObjectId(), 700, 13, 0, 6000, 20000));
					// Set next task.
					if (socialTask != null)
					{
						socialTask.cancel(true);
						socialTask = null;
					}
					socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(4), 10000);
					break;
				case 4:
					broadcastPacket(new SpecialCamera(antharas.getObjectId(), 3700, 0, -3, 0, 10000));
					// Set next task.
					if (socialTask != null)
					{
						socialTask.cancel(true);
						socialTask = null;
					}
					socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(5), 200);
					break;
				
				case 5:
					// Do social.
					broadcastPacket(new SpecialCamera(antharas.getObjectId(), 1100, 0, -3, 22000, 30000));
					// Set next task.
					if (socialTask != null)
					{
						socialTask.cancel(true);
						socialTask = null;
					}
					socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(6), 10800);
					break;
				
				case 6:
					// Set camera.
					broadcastPacket(new SpecialCamera(antharas.getObjectId(), 1100, 0, -3, 300, 7000));
					// Set next task.
					if (socialTask != null)
					{
						socialTask.cancel(true);
						socialTask = null;
					}
					socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(7), 1900);
					break;
				
				case 7:
					antharas.abortCast();
					
					mobiliseTask = ThreadPoolManager.getInstance().scheduleGeneral(new SetMobilised(antharas), 16);
					
					// Move at random.
					if (FWA_MOVEATRANDOM)
					{
						final L2CharPosition pos = new L2CharPosition(Rnd.get(175000, 178500), Rnd.get(112400, 116000), -7707, 0);
						moveAtRandomTask = ThreadPoolManager.getInstance().scheduleGeneral(new MoveAtRandom(antharas, pos), 500);
					}
					
					if (socialTask != null)
					{
						socialTask.cancel(true);
						socialTask = null;
					}
					break;
			}
		}
	}
	
	protected void broadcastPacket(final L2GameServerPacket mov)
	{
		if (zone != null)
		{
			for (final L2Character characters : zone.getCharactersInside().values())
			{
				if (characters instanceof L2PcInstance)
				{
					characters.sendPacket(mov);
				}
			}
		}
	}
	
	// Do spawn Behemoth or Bomber.
	private class MobsSpawn implements Runnable
	{
		public MobsSpawn()
		{
		}
		
		@Override
		public void run()
		{
			L2NpcTemplate template1;
			L2Spawn tempSpawn;
			final boolean isBehemoth = Rnd.get(100) < FWA_PERCENTOFBEHEMOTH;
			try
			{
				final int mobNumber = (isBehemoth ? 2 : 3);
				// Set spawn.
				for (int i = 0; i < mobNumber; i++)
				{
					if (monsters.size() >= FWA_MAXMOBS)
					{
						break;
					}
					int npcId;
					if (isBehemoth)
					{
						npcId = 29069;
					}
					else
					{
						npcId = Rnd.get(29070, 29076);
					}
					template1 = NpcTable.getInstance().getTemplate(npcId);
					tempSpawn = new L2Spawn(template1);
					// allocates it at random in the lair of Antharas.
					int tried = 0;
					boolean notFound = true;
					int x = 175000;
					int y = 112400;
					int dt = (antharas.getX() - x) * (antharas.getX() - x) + (antharas.getY() - y) * (antharas.getY() - y);
					while (tried++ < 25 && notFound)
					{
						final int rx = Rnd.get(175000, 179900);
						final int ry = Rnd.get(112400, 116000);
						final int rdt = (antharas.getX() - rx) * (antharas.getX() - rx) + (antharas.getY() - ry) * (antharas.getY() - ry);
						if (GeoData.getInstance().canSeeTarget(antharas.getX(), antharas.getY(), -7704, rx, ry, -7704))
						{
							if (rdt < dt)
							{
								x = rx;
								y = ry;
								dt = rdt;
								if (rdt <= 900000)
								{
									notFound = false;
								}
							}
						}
					}
					tempSpawn.setLocx(x);
					tempSpawn.setLocy(y);
					tempSpawn.setLocz(-7704);
					tempSpawn.setHeading(0);
					tempSpawn.setAmount(1);
					tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
					SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
					// Do spawn.
					monsters.add(tempSpawn.doSpawn());
				}
			}
			catch (final Exception e)
			{
				LOGGER.warn(e.getMessage());
			}
		}
	}
	
	@Override
	public String onAggroRangeEnter(final L2NpcInstance npc, final L2PcInstance player, final boolean isPet)
	{
		switch (npc.getNpcId())
		{
			case 29070:
			case 29071:
			case 29072:
			case 29073:
			case 29074:
			case 29075:
			case 29076:
				if (selfDestructionTask == null && !npc.isDead())
				{
					selfDestructionTask = ThreadPoolManager.getInstance().scheduleGeneral(new SelfDestructionOfBomber(npc), FWA_SELFDESTRUCTTIME);
				}
				break;
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	// Do self destruction.
	private class SelfDestructionOfBomber implements Runnable
	{
		private final L2NpcInstance bomber;
		
		public SelfDestructionOfBomber(final L2NpcInstance bomber)
		{
			this.bomber = bomber;
		}
		
		@Override
		public void run()
		{
			L2Skill skill = null;
			switch (bomber.getNpcId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
					skill = SkillTable.getInstance().getInfo(5097, 1);
					break;
				case 29076:
					skill = SkillTable.getInstance().getInfo(5094, 1);
					break;
			}
			
			bomber.doCast(skill);
			
			if (selfDestructionTask != null)
			{
				selfDestructionTask.cancel(false);
				selfDestructionTask = null;
			}
			
		}
	}
	
	@Override
	public String onSpellFinished(final L2NpcInstance npc, final L2PcInstance player, final L2Skill skill)
	{
		if (npc.isInvul())
		{
			return null;
		}
		else if (skill != null && (skill.getId() == 5097 || skill.getId() == 5094))
		{
			switch (npc.getNpcId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
				case 29076:
					npc.doDie(npc);
					break;
			}
		}
		return super.onSpellFinished(npc, player, skill);
	}
	
	// At end of activity time.
	protected class CheckActivity implements Runnable
	{
		@Override
		public void run()
		{
			final Long temp = (System.currentTimeMillis() - LastAction);
			if (temp > (Config.ANTHARAS_DESPAWN_TIME * 60000))
			{
				GrandBossManager.getInstance().setBossStatus(antharas.getNpcId(), DORMANT);
				setUnspawn();
			}
		}
	}
	
	// Clean Antharas's lair.
	public void setUnspawn()
	{
		// Eliminate players.
		zone.oustAllPlayers();
		
		// Not executed tasks is canceled.
		if (cubeSpawnTask != null)
		{
			cubeSpawnTask.cancel(true);
			cubeSpawnTask = null;
		}
		if (monsterSpawnTask != null)
		{
			monsterSpawnTask.cancel(true);
			monsterSpawnTask = null;
		}
		if (activityCheckTask != null)
		{
			activityCheckTask.cancel(false);
			activityCheckTask = null;
		}
		if (socialTask != null)
		{
			socialTask.cancel(true);
			socialTask = null;
		}
		if (mobiliseTask != null)
		{
			mobiliseTask.cancel(true);
			mobiliseTask = null;
		}
		if (mobsSpawnTask != null)
		{
			mobsSpawnTask.cancel(true);
			mobsSpawnTask = null;
		}
		if (selfDestructionTask != null)
		{
			selfDestructionTask.cancel(true);
			selfDestructionTask = null;
		}
		if (moveAtRandomTask != null)
		{
			moveAtRandomTask.cancel(true);
			moveAtRandomTask = null;
		}
		
		// Delete monsters.
		for (final L2NpcInstance mob : monsters)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}
		monsters.clear();
		
		// Delete teleport cube.
		for (final L2NpcInstance cube : teleportCube)
		{
			cube.getSpawn().stopRespawn();
			cube.deleteMe();
		}
		teleportCube.clear();
	}
	
	// Do spawn teleport cube.
	private class CubeSpawn implements Runnable
	{
		private final int type;
		
		CubeSpawn(final int type)
		{
			this.type = type;
		}
		
		@Override
		public void run()
		{
			if (type == 0)
			{
				spawnCube();
				cubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(1), 1800000);
			}
			else
			{
				setUnspawn();
			}
		}
	}
	
	// UnLock Antharas.
	private static class UnlockAntharas implements Runnable
	{
		private final int bossId;
		
		public UnlockAntharas(final int bossId)
		{
			this.bossId = bossId;
		}
		
		@Override
		public void run()
		{
			GrandBossManager.getInstance().setBossStatus(bossId, DORMANT);
			if (FWA_DOSERVEREARTHQUAKE)
			{
				for (final L2PcInstance p : L2World.getInstance().getAllPlayers())
				{
					p.broadcastPacket(new Earthquake(185708, 114298, -8221, 20, 10));
				}
			}
		}
	}
	
	// Action is enabled the boss.
	private class SetMobilised implements Runnable
	{
		private final L2GrandBossInstance boss;
		
		public SetMobilised(final L2GrandBossInstance boss)
		{
			this.boss = boss;
		}
		
		@Override
		public void run()
		{
			boss.setIsImobilised(false);
			
			// When it is possible to act, a social action is canceled.
			if (socialTask != null)
			{
				socialTask.cancel(true);
				socialTask = null;
			}
		}
	}
	
	// Move at random on after Antharas appears.
	private static class MoveAtRandom implements Runnable
	{
		private final L2NpcInstance npc;
		private final L2CharPosition pos;
		
		public MoveAtRandom(final L2NpcInstance npc, final L2CharPosition pos)
		{
			this.npc = npc;
			this.pos = pos;
		}
		
		@Override
		public void run()
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
		}
	}
	
	@Override
	public String onAttack(final L2NpcInstance npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		
		if (npc.getSpawn() != null && !npc.getSpawn().isCustomRaidBoss() && npc.getNpcId() == 29019 || npc.getNpcId() == 29066 || npc.getNpcId() == 29067 || npc.getNpcId() == 29068)
		{
			LastAction = System.currentTimeMillis();
			if (!FWA_OLDANTHARAS && mobsSpawnTask == null)
			{
				startMinionSpawns(npc.getNpcId());
			}
		}
		else if (npc.getNpcId() > 29069 && npc.getNpcId() < 29077 && npc.getCurrentHp() <= damage)
		{
			L2Skill skill = null;
			switch (npc.getNpcId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
					skill = SkillTable.getInstance().getInfo(5097, 1);
					break;
				case 29076:
					skill = SkillTable.getInstance().getInfo(5094, 1);
					break;
			}
			
			npc.doCast(skill);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(final L2NpcInstance npc, final L2PcInstance killer, final boolean isPet)
	{
		if (npc.getNpcId() == 29019 || npc.getNpcId() == 29066 || npc.getNpcId() == 29067 || npc.getNpcId() == 29068)
		{
			npc.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
			
			if (!npc.getSpawn().isCustomRaidBoss())
			{
				cubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(0), 10000);
				GrandBossManager.getInstance().setBossStatus(npc.getNpcId(), DEAD);
				final long respawnTime = (Config.ANTHARAS_RESP_FIRST + Rnd.get(Config.ANTHARAS_RESP_SECOND)) * 3600000;
				ThreadPoolManager.getInstance().scheduleGeneral(new UnlockAntharas(npc.getNpcId()), respawnTime);
				// also save the respawn time so that the info is maintained past reboots
				final StatsSet info = GrandBossManager.getInstance().getStatsSet(npc.getNpcId());
				info.set("respawn_time", (System.currentTimeMillis() + respawnTime));
				GrandBossManager.getInstance().setStatsSet(npc.getNpcId(), info);
			}
			
		}
		else if (npc.getNpcId() == 29069)
		{
			final int countHPHerb = Rnd.get(6, 18);
			final int countMPHerb = Rnd.get(6, 18);
			for (int i = 0; i < countHPHerb; i++)
			{
				((L2MonsterInstance) npc).DropItem(killer, 8602, 1);
			}
			for (int i = 0; i < countMPHerb; i++)
			{
				((L2MonsterInstance) npc).DropItem(killer, 8605, 1);
			}
		}
		if (monsters.contains(npc))
		{
			monsters.remove(npc);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public void run()
	{
	}
}