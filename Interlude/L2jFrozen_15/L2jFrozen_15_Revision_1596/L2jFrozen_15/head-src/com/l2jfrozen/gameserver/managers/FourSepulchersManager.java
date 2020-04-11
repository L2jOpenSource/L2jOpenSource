package com.l2jfrozen.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.csv.DoorTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SepulcherMonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SepulcherNpcInstance;
import com.l2jfrozen.gameserver.model.quest.QuestState;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.model.zone.type.L2BossZone;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.Util;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.random.Rnd;

/**
 * This class ...
 * @version $Revision: $ $Date: $
 * @author  sandman TODO: Gatekeepers shouting some text when doors get opened..so far unknown in leaked C4 is this text: 1000502 [brushes hinders competitor's monster.] which is really ugly translation TODO: Victim should attack one npc, when u save this NPC debuff zones will not be activated and
 *          NPC will polymorph into some kind of Tammed Beast xD and shout: 1000503 [many thanks rescue.] which is again really ugly translation. When Victim kill this NPC, debuff zones will get activated with current core its impossible to make attack npc * npc i will try to search where is this
 *          prevented but still is unknown which npc u need to save to survive in next room without debuffs
 */
public class FourSepulchersManager extends GrandBossManager
{
	private static final String QUEST_ID = "620_FourGoblets";
	private static final String SELECT_FOUR_SEPULCHERS_SPAWNS = "SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist WHERE spawntype = 0 ORDER BY id";
	
	private static final int ENTRANCE_PASS = 7075;
	private static final int USED_PASS = 7261;
	private static final int CHAPEL_KEY = 7260;
	private static final int ANTIQUE_BROOCH = 7262;
	
	protected boolean firstTimeRun;
	protected boolean inEntryTime = false;
	protected boolean inWarmUpTime = false;
	protected boolean inAttackTime = false;
	protected boolean inCoolDownTime = false;
	
	protected ScheduledFuture<?> changeCoolDownTimeTask = null;
	protected ScheduledFuture<?> changeEntryTimeTask = null;
	protected ScheduledFuture<?> changeWarmUpTimeTask = null;
	protected ScheduledFuture<?> changeAttackTimeTask = null;
	protected ScheduledFuture<?> onPartyAnnihilatedTask = null;
	
	private final int[][] startHallSpawn =
	{
		{
			181632,
			-85587,
			-7218
		},
		{
			179963,
			-88978,
			-7218
		},
		{
			173217,
			-86132,
			-7218
		},
		{
			175608,
			-82296,
			-7218
		}
	};
	
	private final int[][][] shadowSpawnLoc =
	{
		{
			{
				25339,
				191231,
				-85574,
				-7216,
				33380
			},
			{
				25349,
				189534,
				-88969,
				-7216,
				32768
			},
			{
				25346,
				173195,
				-76560,
				-7215,
				49277
			},
			{
				25342,
				175591,
				-72744,
				-7215,
				49317
			}
		},
		{
			{
				25342,
				191231,
				-85574,
				-7216,
				33380
			},
			{
				25339,
				189534,
				-88969,
				-7216,
				32768
			},
			{
				25349,
				173195,
				-76560,
				-7215,
				49277
			},
			{
				25346,
				175591,
				-72744,
				-7215,
				49317
			}
		},
		{
			{
				25346,
				191231,
				-85574,
				-7216,
				33380
			},
			{
				25342,
				189534,
				-88969,
				-7216,
				32768
			},
			{
				25339,
				173195,
				-76560,
				-7215,
				49277
			},
			{
				25349,
				175591,
				-72744,
				-7215,
				49317
			}
		},
		{
			{
				25349,
				191231,
				-85574,
				-7216,
				33380
			},
			{
				25346,
				189534,
				-88969,
				-7216,
				32768
			},
			{
				25342,
				173195,
				-76560,
				-7215,
				49277
			},
			{
				25339,
				175591,
				-72744,
				-7215,
				49317
			}
		},
	};
	
	protected Map<Integer, Boolean> archonSpawned = new HashMap<>();
	protected Map<Integer, Boolean> hallInUse = new HashMap<>();
	protected Map<Integer, int[]> startHallSpawns = new HashMap<>();
	protected Map<Integer, Integer> hallGateKeepers = new HashMap<>();
	protected Map<Integer, Integer> keyBoxNpc = new HashMap<>();
	protected Map<Integer, Integer> victim = new HashMap<>();
	protected Map<Integer, L2PcInstance> challengers = new HashMap<>();
	protected Map<Integer, L2Spawn> executionerSpawns = new HashMap<>();
	protected Map<Integer, L2Spawn> keyBoxSpawns = new HashMap<>();
	protected Map<Integer, L2Spawn> mysteriousBoxSpawns = new HashMap<>();
	protected Map<Integer, L2Spawn> shadowSpawns = new HashMap<>();
	protected Map<Integer, List<L2Spawn>> dukeFinalMobs = new HashMap<>();
	protected Map<Integer, List<L2SepulcherMonsterInstance>> dukeMobs = new HashMap<>();
	protected Map<Integer, List<L2Spawn>> emperorsGraveNpcs = new HashMap<>();
	protected Map<Integer, List<L2Spawn>> magicalMonsters = new HashMap<>();
	protected Map<Integer, List<L2Spawn>> physicalMonsters = new HashMap<>();
	protected Map<Integer, List<L2SepulcherMonsterInstance>> viscountMobs = new HashMap<>();
	
	protected List<L2Spawn> physicalSpawns;
	protected List<L2Spawn> magicalSpawns;
	protected List<L2Spawn> managers;
	protected List<L2Spawn> dukeFinalSpawns;
	protected List<L2Spawn> emperorsGraveSpawns;
	protected List<L2NpcInstance> allMobs = new ArrayList<>();
	
	protected long attackTimeEnd = 0;
	protected long coolDownTimeEnd = 0;
	protected long entryTimeEnd = 0;
	protected long warmUpTimeEnd = 0;
	
	protected byte newCycleMin = 55;
	
	public static final FourSepulchersManager getInstance()
	{
		return SingletonHolder.instance;
	}
	
	public FourSepulchersManager()
	{
		init();
	}
	
	private void init()
	{
		if (changeCoolDownTimeTask != null)
		{
			changeCoolDownTimeTask.cancel(true);
		}
		if (changeEntryTimeTask != null)
		{
			changeEntryTimeTask.cancel(true);
		}
		if (changeWarmUpTimeTask != null)
		{
			changeWarmUpTimeTask.cancel(true);
		}
		if (changeAttackTimeTask != null)
		{
			changeAttackTimeTask.cancel(true);
		}
		
		changeCoolDownTimeTask = null;
		changeEntryTimeTask = null;
		changeWarmUpTimeTask = null;
		changeAttackTimeTask = null;
		
		inEntryTime = false;
		inWarmUpTime = false;
		inAttackTime = false;
		inCoolDownTime = false;
		
		firstTimeRun = true;
		initFixedInfo();
		loadMysteriousBox();
		initKeyBoxSpawns();
		loadPhysicalMonsters();
		loadMagicalMonsters();
		initLocationShadowSpawns();
		initExecutionerSpawns();
		loadDukeMonsters();
		loadEmperorsGraveMonsters();
		spawnManagers();
		timeSelector();
	}
	
	// phase select on server launch
	protected void timeSelector()
	{
		timeCalculator();
		final long currentTime = Calendar.getInstance().getTimeInMillis();
		// if current time >= time of entry beginning and if current time < time
		// of entry beginning + time of entry end
		if (currentTime >= coolDownTimeEnd && currentTime < entryTimeEnd) // entry
		// time
		// check
		{
			clean();
			changeEntryTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChangeEntryTime(), 0);
			LOGGER.info("FourSepulchersManager: Beginning in Entry time");
		}
		else if (currentTime >= entryTimeEnd && currentTime < warmUpTimeEnd) // warmup
		// time
		// check
		{
			clean();
			changeWarmUpTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChangeWarmUpTime(), 0);
			LOGGER.info("FourSepulchersManager: Beginning in WarmUp time");
		}
		else if (currentTime >= warmUpTimeEnd && currentTime < attackTimeEnd) // attack
		// time
		// check
		{
			clean();
			changeAttackTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChangeAttackTime(), 0);
			LOGGER.info("FourSepulchersManager: Beginning in Attack time");
		}
		else
		// else cooldown time and without cleanup because it's already
		// implemented
		{
			changeCoolDownTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChangeCoolDownTime(), 0);
			LOGGER.info("FourSepulchersManager: Beginning in Cooldown time");
		}
	}
	
	// phase end times calculator
	protected void timeCalculator()
	{
		Calendar tmp = Calendar.getInstance();
		if (tmp.get(Calendar.MINUTE) < newCycleMin)
		{
			tmp.set(Calendar.HOUR, Calendar.getInstance().get(Calendar.HOUR) - 1);
		}
		tmp.set(Calendar.MINUTE, newCycleMin);
		coolDownTimeEnd = tmp.getTimeInMillis();
		entryTimeEnd = coolDownTimeEnd + Config.FS_TIME_ENTRY * 60000;
		warmUpTimeEnd = entryTimeEnd + Config.FS_TIME_WARMUP * 60000;
		attackTimeEnd = warmUpTimeEnd + Config.FS_TIME_ATTACK * 60000;
		tmp = null;
	}
	
	public void clean()
	{
		for (int i = 31921; i < 31925; i++)
		{
			final int[] Location = startHallSpawns.get(i);
			if (Location != null && Location.length == 3)
			{
				final L2BossZone zone = GrandBossManager.getInstance().getZone(Location[0], Location[1], Location[2]);
				if (zone != null)
				{
					zone.oustAllPlayers();
				}
			}
			
		}
		
		deleteAllMobs();
		closeAllDoors();
		
		hallInUse.clear();
		hallInUse.put(31921, false);
		hallInUse.put(31922, false);
		hallInUse.put(31923, false);
		hallInUse.put(31924, false);
		
		if (archonSpawned.size() != 0)
		{
			Set<Integer> npcIdSet = archonSpawned.keySet();
			for (final int npcId : npcIdSet)
			{
				archonSpawned.put(npcId, false);
			}
			npcIdSet = null;
		}
	}
	
	protected void spawnManagers()
	{
		managers = new ArrayList<>();
		// L2Spawn spawnDat;
		
		int i = 31921;
		for (L2Spawn spawnDat; i <= 31924; i++)
		{
			if (i < 31921 || i > 31924)
			{
				continue;
			}
			final L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(i);
			if (template1 == null)
			{
				continue;
			}
			try
			{
				spawnDat = new L2Spawn(template1);
				
				spawnDat.setAmount(1);
				spawnDat.setRespawnDelay(60);
				switch (i)
				{
					case 31921: // conquerors
						spawnDat.setLocx(181061);
						spawnDat.setLocy(-85595);
						spawnDat.setLocz(-7200);
						spawnDat.setHeading(-32584);
						break;
					case 31922: // emperors
						spawnDat.setLocx(179292);
						spawnDat.setLocy(-88981);
						spawnDat.setLocz(-7200);
						spawnDat.setHeading(-33272);
						break;
					case 31923: // sages
						spawnDat.setLocx(173202);
						spawnDat.setLocy(-87004);
						spawnDat.setLocz(-7200);
						spawnDat.setHeading(-16248);
						break;
					case 31924: // judges
						spawnDat.setLocx(175606);
						spawnDat.setLocy(-82853);
						spawnDat.setLocz(-7200);
						spawnDat.setHeading(-16248);
						break;
				}
				managers.add(spawnDat);
				SpawnTable.getInstance().addNewSpawn(spawnDat, false);
				spawnDat.doSpawn();
				spawnDat.startRespawn();
				if (Config.DEBUG)
				{
					LOGGER.info("FourSepulchersManager: spawned " + spawnDat.getTemplate().getName());
				}
			}
			catch (final SecurityException e)
			{
				e.printStackTrace();
			}
			catch (final ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (final NoSuchMethodException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	protected void initFixedInfo()
	{
		startHallSpawns.put(31921, startHallSpawn[0]);
		startHallSpawns.put(31922, startHallSpawn[1]);
		startHallSpawns.put(31923, startHallSpawn[2]);
		startHallSpawns.put(31924, startHallSpawn[3]);
		
		hallInUse.put(31921, false);
		hallInUse.put(31922, false);
		hallInUse.put(31923, false);
		hallInUse.put(31924, false);
		
		hallGateKeepers.put(31925, 25150012);
		hallGateKeepers.put(31926, 25150013);
		hallGateKeepers.put(31927, 25150014);
		hallGateKeepers.put(31928, 25150015);
		hallGateKeepers.put(31929, 25150016);
		hallGateKeepers.put(31930, 25150002);
		hallGateKeepers.put(31931, 25150003);
		hallGateKeepers.put(31932, 25150004);
		hallGateKeepers.put(31933, 25150005);
		hallGateKeepers.put(31934, 25150006);
		hallGateKeepers.put(31935, 25150032);
		hallGateKeepers.put(31936, 25150033);
		hallGateKeepers.put(31937, 25150034);
		hallGateKeepers.put(31938, 25150035);
		hallGateKeepers.put(31939, 25150036);
		hallGateKeepers.put(31940, 25150022);
		hallGateKeepers.put(31941, 25150023);
		hallGateKeepers.put(31942, 25150024);
		hallGateKeepers.put(31943, 25150025);
		hallGateKeepers.put(31944, 25150026);
		
		keyBoxNpc.put(18120, 31455);
		keyBoxNpc.put(18121, 31455);
		keyBoxNpc.put(18122, 31455);
		keyBoxNpc.put(18123, 31455);
		keyBoxNpc.put(18124, 31456);
		keyBoxNpc.put(18125, 31456);
		keyBoxNpc.put(18126, 31456);
		keyBoxNpc.put(18127, 31456);
		keyBoxNpc.put(18128, 31457);
		keyBoxNpc.put(18129, 31457);
		keyBoxNpc.put(18130, 31457);
		keyBoxNpc.put(18131, 31457);
		keyBoxNpc.put(18149, 31458);
		keyBoxNpc.put(18150, 31459);
		keyBoxNpc.put(18151, 31459);
		keyBoxNpc.put(18152, 31459);
		keyBoxNpc.put(18153, 31459);
		keyBoxNpc.put(18154, 31460);
		keyBoxNpc.put(18155, 31460);
		keyBoxNpc.put(18156, 31460);
		keyBoxNpc.put(18157, 31460);
		keyBoxNpc.put(18158, 31461);
		keyBoxNpc.put(18159, 31461);
		keyBoxNpc.put(18160, 31461);
		keyBoxNpc.put(18161, 31461);
		keyBoxNpc.put(18162, 31462);
		keyBoxNpc.put(18163, 31462);
		keyBoxNpc.put(18164, 31462);
		keyBoxNpc.put(18165, 31462);
		keyBoxNpc.put(18183, 31463);
		keyBoxNpc.put(18184, 31464);
		keyBoxNpc.put(18212, 31465);
		keyBoxNpc.put(18213, 31465);
		keyBoxNpc.put(18214, 31465);
		keyBoxNpc.put(18215, 31465);
		keyBoxNpc.put(18216, 31466);
		keyBoxNpc.put(18217, 31466);
		keyBoxNpc.put(18218, 31466);
		keyBoxNpc.put(18219, 31466);
		
		victim.put(18150, 18158);
		victim.put(18151, 18159);
		victim.put(18152, 18160);
		victim.put(18153, 18161);
		victim.put(18154, 18162);
		victim.put(18155, 18163);
		victim.put(18156, 18164);
		victim.put(18157, 18165);
	}
	
	private void loadMysteriousBox()
	{
		mysteriousBoxSpawns.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_FOUR_SEPULCHERS_SPAWNS);
			ResultSet rset = statement.executeQuery())
		{
			L2Spawn spawn;
			L2NpcTemplate template;
			
			while (rset.next())
			{
				template = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template != null)
				{
					spawn = new L2Spawn(template);
					spawn.setAmount(rset.getInt("count"));
					spawn.setLocx(rset.getInt("locx"));
					spawn.setLocy(rset.getInt("locy"));
					spawn.setLocz(rset.getInt("locz"));
					spawn.setHeading(rset.getInt("heading"));
					spawn.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawn, false);
					final int keyNpcId = rset.getInt("key_npc_id");
					mysteriousBoxSpawns.put(keyNpcId, spawn);
				}
				else
				{
					LOGGER.warn("FourSepulchersManager.LoadMysteriousBox: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			
			if (Config.DEBUG)
			{
				LOGGER.info("FourSepulchersManager: loaded " + mysteriousBoxSpawns.size() + " Mysterious-Box spawns.");
			}
		}
		
		catch (Exception e)
		{
			LOGGER.error("FourSepulchersManager.LoadMysteriousBox: Spawn could not be initialized", e);
		}
	}
	
	private void initKeyBoxSpawns()
	{
		L2Spawn spawnDat;
		L2NpcTemplate template;
		
		for (final int keyNpcId : keyBoxNpc.keySet())
		{
			try
			{
				template = NpcTable.getInstance().getTemplate(keyBoxNpc.get(keyNpcId));
				if (template != null)
				{
					spawnDat = new L2Spawn(template);
					spawnDat.setAmount(1);
					spawnDat.setLocx(0);
					spawnDat.setLocy(0);
					spawnDat.setLocz(0);
					spawnDat.setHeading(0);
					spawnDat.setRespawnDelay(3600);
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					keyBoxSpawns.put(keyNpcId, spawnDat);
				}
				else
				{
					LOGGER.warn("FourSepulchersManager.InitKeyBoxSpawns: Data missing in NPC table for ID: " + keyBoxNpc.get(keyNpcId) + ".");
				}
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				LOGGER.warn("FourSepulchersManager.InitKeyBoxSpawns: Spawn could not be initialized: " + e);
			}
		}
		spawnDat = null;
		template = null;
	}
	
	private void loadPhysicalMonsters()
	{
		physicalMonsters.clear();
		
		int loaded = 0;
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
			statement1.setInt(1, 1);
			ResultSet rset1 = statement1.executeQuery();
			while (rset1.next())
			{
				final int keyNpcId = rset1.getInt("key_npc_id");
				
				PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
				statement2.setInt(1, keyNpcId);
				statement2.setInt(2, 1);
				ResultSet rset2 = statement2.executeQuery();
				
				L2Spawn spawnDat;
				L2NpcTemplate template1;
				
				physicalSpawns = new ArrayList<>();
				
				while (rset2.next())
				{
					template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
					if (template1 != null)
					{
						spawnDat = new L2Spawn(template1);
						spawnDat.setAmount(rset2.getInt("count"));
						spawnDat.setLocx(rset2.getInt("locx"));
						spawnDat.setLocy(rset2.getInt("locy"));
						spawnDat.setLocz(rset2.getInt("locz"));
						spawnDat.setHeading(rset2.getInt("heading"));
						spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
						SpawnTable.getInstance().addNewSpawn(spawnDat, false);
						physicalSpawns.add(spawnDat);
						loaded++;
					}
					else
					{
						LOGGER.warn("FourSepulchersManager.LoadPhysicalMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
					}
				}
				
				rset2.close();
				statement2.close();
				rset2 = null;
				statement2 = null;
				physicalMonsters.put(keyNpcId, physicalSpawns);
			}
			
			rset1.close();
			statement1.close();
			rset1 = null;
			statement1 = null;
			if (Config.DEBUG)
			{
				LOGGER.info("FourSepulchersManager: loaded " + loaded + " Physical type monsters spawns.");
			}
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			// problem with initializing spawn, go to next one
			LOGGER.warn("FourSepulchersManager.LoadPhysicalMonsters: Spawn could not be initialized: " + e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	private void loadMagicalMonsters()
	{
		magicalMonsters.clear();
		
		int loaded = 0;
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
			statement1.setInt(1, 2);
			ResultSet rset1 = statement1.executeQuery();
			while (rset1.next())
			{
				final int keyNpcId = rset1.getInt("key_npc_id");
				
				PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
				statement2.setInt(1, keyNpcId);
				statement2.setInt(2, 2);
				ResultSet rset2 = statement2.executeQuery();
				
				L2Spawn spawnDat;
				L2NpcTemplate template1;
				
				magicalSpawns = new ArrayList<>();
				
				while (rset2.next())
				{
					template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
					if (template1 != null)
					{
						spawnDat = new L2Spawn(template1);
						spawnDat.setAmount(rset2.getInt("count"));
						spawnDat.setLocx(rset2.getInt("locx"));
						spawnDat.setLocy(rset2.getInt("locy"));
						spawnDat.setLocz(rset2.getInt("locz"));
						spawnDat.setHeading(rset2.getInt("heading"));
						spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
						SpawnTable.getInstance().addNewSpawn(spawnDat, false);
						magicalSpawns.add(spawnDat);
						loaded++;
					}
					else
					{
						LOGGER.warn("FourSepulchersManager.LoadMagicalMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
					}
				}
				
				rset2.close();
				statement2.close();
				rset2 = null;
				statement2 = null;
				magicalMonsters.put(keyNpcId, magicalSpawns);
			}
			
			rset1.close();
			statement1.close();
			rset1 = null;
			statement1 = null;
			if (Config.DEBUG)
			{
				LOGGER.info("FourSepulchersManager: loaded " + loaded + " Magical type monsters spawns.");
			}
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			// problem with initializing spawn, go to next one
			LOGGER.warn("FourSepulchersManager.LoadMagicalMonsters: Spawn could not be initialized: " + e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	private void loadDukeMonsters()
	{
		dukeFinalMobs.clear();
		archonSpawned.clear();
		
		int loaded = 0;
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
			statement1.setInt(1, 5);
			ResultSet rset1 = statement1.executeQuery();
			while (rset1.next())
			{
				final int keyNpcId = rset1.getInt("key_npc_id");
				
				PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
				statement2.setInt(1, keyNpcId);
				statement2.setInt(2, 5);
				ResultSet rset2 = statement2.executeQuery();
				
				L2Spawn spawnDat;
				L2NpcTemplate template1;
				
				dukeFinalSpawns = new ArrayList<>();
				
				while (rset2.next())
				{
					template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
					if (template1 != null)
					{
						spawnDat = new L2Spawn(template1);
						spawnDat.setAmount(rset2.getInt("count"));
						spawnDat.setLocx(rset2.getInt("locx"));
						spawnDat.setLocy(rset2.getInt("locy"));
						spawnDat.setLocz(rset2.getInt("locz"));
						spawnDat.setHeading(rset2.getInt("heading"));
						spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
						SpawnTable.getInstance().addNewSpawn(spawnDat, false);
						dukeFinalSpawns.add(spawnDat);
						loaded++;
					}
					else
					{
						LOGGER.warn("FourSepulchersManager.LoadDukeMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
					}
				}
				
				rset2.close();
				statement2.close();
				rset2 = null;
				statement2 = null;
				dukeFinalMobs.put(keyNpcId, dukeFinalSpawns);
				archonSpawned.put(keyNpcId, false);
			}
			
			rset1.close();
			statement1.close();
			rset1 = null;
			statement1 = null;
			if (Config.DEBUG)
			{
				LOGGER.info("FourSepulchersManager: loaded " + loaded + " Church of duke monsters spawns.");
			}
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			// problem with initializing spawn, go to next one
			LOGGER.warn("FourSepulchersManager.LoadDukeMonsters: Spawn could not be initialized: " + e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	private void loadEmperorsGraveMonsters()
	{
		emperorsGraveNpcs.clear();
		
		int loaded = 0;
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
			statement1.setInt(1, 6);
			ResultSet rset1 = statement1.executeQuery();
			while (rset1.next())
			{
				final int keyNpcId = rset1.getInt("key_npc_id");
				
				PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
				statement2.setInt(1, keyNpcId);
				statement2.setInt(2, 6);
				ResultSet rset2 = statement2.executeQuery();
				
				L2Spawn spawnDat;
				L2NpcTemplate template1;
				
				emperorsGraveSpawns = new ArrayList<>();
				
				while (rset2.next())
				{
					template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
					if (template1 != null)
					{
						spawnDat = new L2Spawn(template1);
						spawnDat.setAmount(rset2.getInt("count"));
						spawnDat.setLocx(rset2.getInt("locx"));
						spawnDat.setLocy(rset2.getInt("locy"));
						spawnDat.setLocz(rset2.getInt("locz"));
						spawnDat.setHeading(rset2.getInt("heading"));
						spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
						SpawnTable.getInstance().addNewSpawn(spawnDat, false);
						emperorsGraveSpawns.add(spawnDat);
						loaded++;
					}
					else
					{
						LOGGER.warn("FourSepulchersManager.LoadEmperorsGraveMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
					}
				}
				
				rset2.close();
				statement2.close();
				rset2 = null;
				statement2 = null;
				emperorsGraveNpcs.put(keyNpcId, emperorsGraveSpawns);
			}
			
			rset1.close();
			statement1.close();
			rset1 = null;
			statement1 = null;
			if (Config.DEBUG)
			{
				LOGGER.info("FourSepulchersManager: loaded " + loaded + " Emperor's grave NPC spawns.");
			}
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			// problem with initializing spawn, go to next one
			LOGGER.warn("FourSepulchersManager.LoadEmperorsGraveMonsters: Spawn could not be initialized: " + e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	protected void initLocationShadowSpawns()
	{
		final int locNo = Rnd.get(4);
		final int[] gateKeeper =
		{
			31929,
			31934,
			31939,
			31944
		};
		
		L2Spawn spawnDat;
		L2NpcTemplate template;
		
		shadowSpawns.clear();
		
		for (int i = 0; i <= 3; i++)
		{
			template = NpcTable.getInstance().getTemplate(shadowSpawnLoc[locNo][i][0]);
			if (template != null)
			{
				try
				{
					spawnDat = new L2Spawn(template);
					spawnDat.setAmount(1);
					spawnDat.setLocx(shadowSpawnLoc[locNo][i][1]);
					spawnDat.setLocy(shadowSpawnLoc[locNo][i][2]);
					spawnDat.setLocz(shadowSpawnLoc[locNo][i][3]);
					spawnDat.setHeading(shadowSpawnLoc[locNo][i][4]);
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					final int keyNpcId = gateKeeper[i];
					shadowSpawns.put(keyNpcId, spawnDat);
				}
				catch (final Exception e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
					LOGGER.warn("initLocationShadowSpawns:" + e.getMessage());
				}
			}
			else
			{
				LOGGER.warn("FourSepulchersManager.InitLocationShadowSpawns: Data missing in NPC table for ID: " + shadowSpawnLoc[locNo][i][0] + ".");
			}
		}
	}
	
	protected void initExecutionerSpawns()
	{
		L2Spawn spawnDat;
		L2NpcTemplate template;
		
		for (final int keyNpcId : victim.keySet())
		{
			try
			{
				template = NpcTable.getInstance().getTemplate(victim.get(keyNpcId));
				if (template != null)
				{
					spawnDat = new L2Spawn(template);
					spawnDat.setAmount(1);
					spawnDat.setLocx(0);
					spawnDat.setLocy(0);
					spawnDat.setLocz(0);
					spawnDat.setHeading(0);
					spawnDat.setRespawnDelay(3600);
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					executionerSpawns.put(keyNpcId, spawnDat);
				}
				else
				{
					LOGGER.warn("FourSepulchersManager.InitExecutionerSpawns: Data missing in NPC table for ID: " + victim.get(keyNpcId) + ".");
				}
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				LOGGER.warn("FourSepulchersManager.InitExecutionerSpawns: Spawn could not be initialized: " + e);
			}
		}
	}
	
	public boolean isEntryTime()
	{
		return inEntryTime;
	}
	
	public boolean isAttackTime()
	{
		return inAttackTime;
	}
	
	public synchronized void tryEntry(final L2NpcInstance npc, final L2PcInstance player)
	{
		final int npcId = npc.getNpcId();
		switch (npcId)
		{
			// ID ok
			case 31921:
			case 31922:
			case 31923:
			case 31924:
				break;
			// ID not ok
			default:
				if (!player.isGM())
				{
					LOGGER.warn("Player " + player.getName() + "(" + player.getObjectId() + ") tried to cheat in four sepulchers.");
					Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " tried to enter four sepulchers with invalid npc id.", Config.DEFAULT_PUNISH);
				}
				return;
		}
		
		if (hallInUse.get(npcId).booleanValue())
		{
			showHtmlFile(player, npcId + "-FULL.htm", npc, null);
			return;
		}
		
		if (Config.FS_PARTY_MEMBER_COUNT > 1)
		{
			if (!player.isInParty() || player.getParty().getMemberCount() < Config.FS_PARTY_MEMBER_COUNT)
			{
				showHtmlFile(player, npcId + "-SP.htm", npc, null);
				return;
			}
			
			if (!player.getParty().isLeader(player))
			{
				showHtmlFile(player, npcId + "-NL.htm", npc, null);
				return;
			}
			
			for (final L2PcInstance mem : player.getParty().getPartyMembers())
			{
				final QuestState qs = mem.getQuestState(QUEST_ID);
				if (qs == null || !qs.isStarted() && !qs.isCompleted())
				{
					showHtmlFile(player, npcId + "-NS.htm", npc, mem);
					return;
				}
				
				if (mem.getInventory().getItemByItemId(ENTRANCE_PASS) == null)
				{
					showHtmlFile(player, npcId + "-SE.htm", npc, mem);
					return;
				}
				
				if (mem.getWeightPenalty() >= 3)
				{
					mem.sendPacket(new SystemMessage(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT));
					return;
				}
			}
		}
		else if (Config.FS_PARTY_MEMBER_COUNT <= 1 && player.isInParty())
		{
			if (!player.getParty().isLeader(player))
			{
				showHtmlFile(player, npcId + "-NL.htm", npc, null);
				return;
			}
			for (final L2PcInstance mem : player.getParty().getPartyMembers())
			{
				final QuestState qs = mem.getQuestState(QUEST_ID);
				if (qs == null || !qs.isStarted() && !qs.isCompleted())
				{
					showHtmlFile(player, npcId + "-NS.htm", npc, mem);
					return;
				}
				
				if (mem.getInventory().getItemByItemId(ENTRANCE_PASS) == null)
				{
					showHtmlFile(player, npcId + "-SE.htm", npc, mem);
					return;
				}
				
				if (mem.getWeightPenalty() >= 3)
				{
					mem.sendPacket(new SystemMessage(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT));
					return;
				}
			}
		}
		else
		{
			final QuestState qs = player.getQuestState(QUEST_ID);
			if (qs == null || !qs.isStarted() && !qs.isCompleted())
			{
				showHtmlFile(player, npcId + "-NS.htm", npc, player);
				return;
			}
			
			if (player.getInventory().getItemByItemId(ENTRANCE_PASS) == null)
			{
				showHtmlFile(player, npcId + "-SE.htm", npc, player);
				return;
			}
			
			if (player.getWeightPenalty() >= 3)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT));
				return;
			}
		}
		
		if (!isEntryTime())
		{
			showHtmlFile(player, npcId + "-NE.htm", npc, null);
			return;
		}
		
		showHtmlFile(player, npcId + "-OK.htm", npc, null);
		
		entry(npcId, player);
	}
	
	private void entry(final int npcId, final L2PcInstance player)
	{
		final int[] Location = startHallSpawns.get(npcId);
		int driftx;
		int drifty;
		
		if (Config.FS_PARTY_MEMBER_COUNT > 1)
		{
			List<L2PcInstance> members = new ArrayList<>();
			for (final L2PcInstance mem : player.getParty().getPartyMembers())
			{
				if (!mem.isDead() && Util.checkIfInRange(700, player, mem, true))
				{
					members.add(mem);
				}
			}
			
			for (final L2PcInstance mem : members)
			{
				GrandBossManager.getInstance().getZone(Location[0], Location[1], Location[2]).allowPlayerEntry(mem, 30);
				driftx = Rnd.get(-80, 80);
				drifty = Rnd.get(-80, 80);
				mem.teleToLocation(Location[0] + driftx, Location[1] + drifty, Location[2]);
				mem.destroyItemByItemId("Quest", ENTRANCE_PASS, 1, mem, true);
				if (mem.getInventory().getItemByItemId(ANTIQUE_BROOCH) == null)
				{
					mem.addItem("Quest", USED_PASS, 1, mem, true);
				}
				
				final L2ItemInstance hallsKey = mem.getInventory().getItemByItemId(CHAPEL_KEY);
				if (hallsKey != null)
				{
					mem.destroyItemByItemId("Quest", CHAPEL_KEY, hallsKey.getCount(), mem, true);
				}
			}
			
			members = null;
			
			challengers.remove(npcId);
			challengers.put(npcId, player);
			
			hallInUse.remove(npcId);
			hallInUse.put(npcId, true);
		}
		else if (Config.FS_PARTY_MEMBER_COUNT <= 1 && player.isInParty())
		{
			List<L2PcInstance> members = new ArrayList<>();
			for (final L2PcInstance mem : player.getParty().getPartyMembers())
			{
				if (!mem.isDead() && Util.checkIfInRange(700, player, mem, true))
				{
					members.add(mem);
				}
			}
			
			for (final L2PcInstance mem : members)
			{
				GrandBossManager.getInstance().getZone(Location[0], Location[1], Location[2]).allowPlayerEntry(mem, 30);
				driftx = Rnd.get(-80, 80);
				drifty = Rnd.get(-80, 80);
				mem.teleToLocation(Location[0] + driftx, Location[1] + drifty, Location[2]);
				mem.destroyItemByItemId("Quest", ENTRANCE_PASS, 1, mem, true);
				if (mem.getInventory().getItemByItemId(ANTIQUE_BROOCH) == null)
				{
					mem.addItem("Quest", USED_PASS, 1, mem, true);
				}
				
				final L2ItemInstance hallsKey = mem.getInventory().getItemByItemId(CHAPEL_KEY);
				if (hallsKey != null)
				{
					mem.destroyItemByItemId("Quest", CHAPEL_KEY, hallsKey.getCount(), mem, true);
				}
			}
			
			members = null;
			
			challengers.remove(npcId);
			challengers.put(npcId, player);
			
			hallInUse.remove(npcId);
			hallInUse.put(npcId, true);
		}
		else
		{
			GrandBossManager.getInstance().getZone(Location[0], Location[1], Location[2]).allowPlayerEntry(player, 30);
			driftx = Rnd.get(-80, 80);
			drifty = Rnd.get(-80, 80);
			player.teleToLocation(Location[0] + driftx, Location[1] + drifty, Location[2]);
			player.destroyItemByItemId("Quest", ENTRANCE_PASS, 1, player, true);
			if (player.getInventory().getItemByItemId(ANTIQUE_BROOCH) == null)
			{
				player.addItem("Quest", USED_PASS, 1, player, true);
			}
			
			L2ItemInstance hallsKey = player.getInventory().getItemByItemId(CHAPEL_KEY);
			if (hallsKey != null)
			{
				player.destroyItemByItemId("Quest", CHAPEL_KEY, hallsKey.getCount(), player, true);
			}
			
			hallsKey = null;
			
			challengers.remove(npcId);
			challengers.put(npcId, player);
			
			hallInUse.remove(npcId);
			hallInUse.put(npcId, true);
		}
	}
	
	public void spawnMysteriousBox(final int npcId)
	{
		if (!isAttackTime())
		{
			return;
		}
		
		L2Spawn spawnDat = mysteriousBoxSpawns.get(npcId);
		if (spawnDat != null)
		{
			allMobs.add(spawnDat.doSpawn());
			spawnDat.stopRespawn();
		}
		spawnDat = null;
	}
	
	public void spawnMonster(final int npcId)
	{
		if (!isAttackTime())
		{
			return;
		}
		
		List<L2Spawn> monsterList;
		List<L2SepulcherMonsterInstance> mobs = new ArrayList<>();
		L2Spawn keyBoxMobSpawn;
		
		if (Rnd.get(2) == 0)
		{
			monsterList = physicalMonsters.get(npcId);
		}
		else
		{
			monsterList = magicalMonsters.get(npcId);
		}
		
		if (monsterList != null)
		{
			boolean spawnKeyBoxMob = false;
			boolean spawnedKeyBoxMob = false;
			
			for (final L2Spawn spawnDat : monsterList)
			{
				if (spawnedKeyBoxMob)
				{
					spawnKeyBoxMob = false;
				}
				else
				{
					switch (npcId)
					{
						case 31469:
						case 31474:
						case 31479:
						case 31484:
							if (Rnd.get(48) == 0)
							{
								spawnKeyBoxMob = true;
								// LOGGER.info("FourSepulchersManager.SpawnMonster:
								// Set to spawn Church of Viscount Key Mob.");
							}
							break;
						default:
							spawnKeyBoxMob = false;
					}
				}
				
				L2SepulcherMonsterInstance mob = null;
				
				if (spawnKeyBoxMob)
				{
					try
					{
						final L2NpcTemplate template = NpcTable.getInstance().getTemplate(18149);
						if (template != null)
						{
							keyBoxMobSpawn = new L2Spawn(template);
							keyBoxMobSpawn.setAmount(1);
							keyBoxMobSpawn.setLocx(spawnDat.getLocx());
							keyBoxMobSpawn.setLocy(spawnDat.getLocy());
							keyBoxMobSpawn.setLocz(spawnDat.getLocz());
							keyBoxMobSpawn.setHeading(spawnDat.getHeading());
							keyBoxMobSpawn.setRespawnDelay(3600);
							SpawnTable.getInstance().addNewSpawn(keyBoxMobSpawn, false);
							mob = (L2SepulcherMonsterInstance) keyBoxMobSpawn.doSpawn();
							keyBoxMobSpawn.stopRespawn();
						}
						else
						{
							LOGGER.warn("FourSepulchersManager.SpawnMonster: Data missing in NPC table for ID: 18149");
						}
					}
					catch (final Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e.printStackTrace();
						}
						
						LOGGER.warn("FourSepulchersManager.SpawnMonster: Spawn could not be initialized: " + e);
					}
					
					spawnedKeyBoxMob = true;
				}
				else
				{
					mob = (L2SepulcherMonsterInstance) spawnDat.doSpawn();
					spawnDat.stopRespawn();
				}
				
				if (mob != null)
				{
					mob.mysteriousBoxId = npcId;
					switch (npcId)
					{
						case 31469:
						case 31474:
						case 31479:
						case 31484:
						case 31472:
						case 31477:
						case 31482:
						case 31487:
							mobs.add(mob);
					}
					allMobs.add(mob);
				}
			}
			
			switch (npcId)
			{
				case 31469:
				case 31474:
				case 31479:
				case 31484:
					viscountMobs.put(npcId, mobs);
					break;
				
				case 31472:
				case 31477:
				case 31482:
				case 31487:
					dukeMobs.put(npcId, mobs);
					break;
			}
		}
		monsterList = null;
		mobs = null;
		keyBoxMobSpawn = null;
	}
	
	public synchronized boolean isViscountMobsAnnihilated(final int npcId)
	{
		List<L2SepulcherMonsterInstance> mobs = viscountMobs.get(npcId);
		
		if (mobs == null)
		{
			return true;
		}
		
		for (final L2SepulcherMonsterInstance mob : mobs)
		{
			if (!mob.isDead())
			{
				return false;
			}
		}
		mobs = null;
		
		return true;
	}
	
	public synchronized boolean isDukeMobsAnnihilated(final int npcId)
	{
		List<L2SepulcherMonsterInstance> mobs = dukeMobs.get(npcId);
		
		if (mobs == null)
		{
			return true;
		}
		
		for (final L2SepulcherMonsterInstance mob : mobs)
		{
			if (!mob.isDead())
			{
				return false;
			}
		}
		mobs = null;
		
		return true;
	}
	
	public void spawnKeyBox(final L2NpcInstance activeChar)
	{
		if (!isAttackTime())
		{
			return;
		}
		
		L2Spawn spawnDat = keyBoxSpawns.get(activeChar.getNpcId());
		
		if (spawnDat != null)
		{
			spawnDat.setAmount(1);
			spawnDat.setLocx(activeChar.getX());
			spawnDat.setLocy(activeChar.getY());
			spawnDat.setLocz(activeChar.getZ());
			spawnDat.setHeading(activeChar.getHeading());
			spawnDat.setRespawnDelay(3600);
			allMobs.add(spawnDat.doSpawn());
			spawnDat.stopRespawn();
			
			spawnDat = null;
		}
	}
	
	public void spawnExecutionerOfHalisha(final L2NpcInstance activeChar)
	{
		if (!isAttackTime())
		{
			return;
		}
		
		L2Spawn spawnDat = executionerSpawns.get(activeChar.getNpcId());
		
		if (spawnDat != null)
		{
			spawnDat.setAmount(1);
			spawnDat.setLocx(activeChar.getX());
			spawnDat.setLocy(activeChar.getY());
			spawnDat.setLocz(activeChar.getZ());
			spawnDat.setHeading(activeChar.getHeading());
			spawnDat.setRespawnDelay(3600);
			allMobs.add(spawnDat.doSpawn());
			spawnDat.stopRespawn();
			
			spawnDat = null;
		}
	}
	
	public void spawnArchonOfHalisha(final int npcId)
	{
		if (!isAttackTime())
		{
			return;
		}
		
		if (archonSpawned.get(npcId))
		{
			return;
		}
		
		List<L2Spawn> monsterList = dukeFinalMobs.get(npcId);
		
		if (monsterList != null)
		{
			for (final L2Spawn spawnDat : monsterList)
			{
				final L2SepulcherMonsterInstance mob = (L2SepulcherMonsterInstance) spawnDat.doSpawn();
				spawnDat.stopRespawn();
				
				if (mob != null)
				{
					mob.mysteriousBoxId = npcId;
					allMobs.add(mob);
				}
			}
			archonSpawned.put(npcId, true);
			monsterList = null;
		}
	}
	
	public void spawnEmperorsGraveNpc(final int npcId)
	{
		if (!isAttackTime())
		{
			return;
		}
		
		List<L2Spawn> monsterList = emperorsGraveNpcs.get(npcId);
		
		if (monsterList != null)
		{
			for (final L2Spawn spawnDat : monsterList)
			{
				allMobs.add(spawnDat.doSpawn());
				spawnDat.stopRespawn();
			}
			monsterList = null;
		}
	}
	
	protected void locationShadowSpawns()
	{
		final int locNo = Rnd.get(4);
		// LOGGER.info("FourSepulchersManager.LocationShadowSpawns: Location index
		// is " + locNo + ".");
		final int[] gateKeeper =
		{
			31929,
			31934,
			31939,
			31944
		};
		
		L2Spawn spawnDat;
		
		for (int i = 0; i <= 3; i++)
		{
			final int keyNpcId = gateKeeper[i];
			spawnDat = shadowSpawns.get(keyNpcId);
			spawnDat.setLocx(shadowSpawnLoc[locNo][i][1]);
			spawnDat.setLocy(shadowSpawnLoc[locNo][i][2]);
			spawnDat.setLocz(shadowSpawnLoc[locNo][i][3]);
			spawnDat.setHeading(shadowSpawnLoc[locNo][i][4]);
			shadowSpawns.put(keyNpcId, spawnDat);
		}
		spawnDat = null;
	}
	
	public void spawnShadow(final int npcId)
	{
		if (!isAttackTime())
		{
			return;
		}
		
		L2Spawn spawnDat = shadowSpawns.get(npcId);
		if (spawnDat != null)
		{
			final L2SepulcherMonsterInstance mob = (L2SepulcherMonsterInstance) spawnDat.doSpawn();
			spawnDat.stopRespawn();
			
			if (mob != null)
			{
				mob.mysteriousBoxId = npcId;
				allMobs.add(mob);
			}
			spawnDat = null;
		}
	}
	
	public void deleteAllMobs()
	{
		for (final L2NpcInstance mob : allMobs)
		{
			try
			{
				mob.getSpawn().stopRespawn();
				mob.deleteMe();
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				LOGGER.warn("deleteAllMobs: " + e.getMessage());
			}
		}
		allMobs.clear();
	}
	
	protected void closeAllDoors()
	{
		for (final int doorId : hallGateKeepers.values())
		{
			final L2DoorInstance door = DoorTable.getInstance().getDoor(doorId);
			try
			{
				if (door != null)
				{
					door.closeMe();
				}
				else
				{
					LOGGER.warn("Ahenbek ashelbek! Shaitanama!! " + doorId);
				}
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				LOGGER.warn("Failed closing door " + doorId + " - " + e.getMessage());
			}
		}
	}
	
	protected byte minuteSelect(byte min)
	{
		if ((double) min % 5 != 0)// if doesn't divides on 5 fully
		{
			// mad table for selecting proper minutes...
			// may be there is a better way to do this
			switch (min)
			{
				case 6:
				case 7:
					min = 5;
					break;
				case 8:
				case 9:
				case 11:
				case 12:
					min = 10;
					break;
				case 13:
				case 14:
				case 16:
				case 17:
					min = 15;
					break;
				case 18:
				case 19:
				case 21:
				case 22:
					min = 20;
					break;
				case 23:
				case 24:
				case 26:
				case 27:
					min = 25;
					break;
				case 28:
				case 29:
				case 31:
				case 32:
					min = 30;
					break;
				case 33:
				case 34:
				case 36:
				case 37:
					min = 35;
					break;
				case 38:
				case 39:
				case 41:
				case 42:
					min = 40;
					break;
				case 43:
				case 44:
				case 46:
				case 47:
					min = 45;
					break;
				case 48:
				case 49:
				case 51:
				case 52:
					min = 50;
					break;
				case 53:
				case 54:
				case 56:
				case 57:
					min = 55;
					break;
			}
		}
		return min;
	}
	
	public void managerSay(byte min)
	{
		// for attack phase, sending message every 5 minutes
		if (inAttackTime)
		{
			if (min < 5)
			{
				return; // do not shout when < 5 minutes
			}
			
			min = minuteSelect(min);
			String msg = min + " minute(s) have passed."; // now this is a proper message^^
			if (min == 90)
			{
				msg = "Game over. The teleport will appear momentarily";
			}
			
			for (final L2Spawn temp : managers)
			{
				if (temp == null)
				{
					LOGGER.warn("FourSepulchersManager: managerSay(): manager is null");
					continue;
				}
				if (!(temp.getLastSpawn() instanceof L2SepulcherNpcInstance))
				{
					LOGGER.warn("FourSepulchersManager: managerSay(): manager is not Sepulcher instance");
					continue;
				}
				// hall not used right now, so its manager will not tell you
				// anything :)
				// if you don't need this - delete next two lines.
				if (!hallInUse.get(temp.getNpcid()).booleanValue())
				{
					continue;
				}
				
				((L2SepulcherNpcInstance) temp.getLastSpawn()).sayInShout(msg);
			}
		}
		
		else if (inEntryTime)
		{
			final String msg1 = "You may now enter the Sepulcher";
			final String msg2 = "If you place your hand on the stone statue in front of each sepulcher," + " you will be able to enter";
			
			for (final L2Spawn temp : managers)
			{
				if (temp == null)
				{
					LOGGER.warn("FourSepulchersManager: Something goes wrong in managerSay()...");
					continue;
				}
				if (!(temp.getLastSpawn() instanceof L2SepulcherNpcInstance))
				{
					LOGGER.warn("FourSepulchersManager: Something goes wrong in managerSay()...");
					continue;
				}
				((L2SepulcherNpcInstance) temp.getLastSpawn()).sayInShout(msg1);
				((L2SepulcherNpcInstance) temp.getLastSpawn()).sayInShout(msg2);
			}
		}
	}
	
	protected class ManagerSay implements Runnable
	{
		@Override
		public void run()
		{
			if (inAttackTime)
			{
				Calendar tmp = Calendar.getInstance();
				tmp.setTimeInMillis(Calendar.getInstance().getTimeInMillis() - warmUpTimeEnd);
				if (tmp.get(Calendar.MINUTE) + 5 < Config.FS_TIME_ATTACK)
				{
					managerSay((byte) tmp.get(Calendar.MINUTE)); // byte
					// because
					// minute
					// cannot be
					// more than
					// 59
					ThreadPoolManager.getInstance().scheduleGeneral(new ManagerSay(), 5 * 60000);
				}
				// attack time ending chat
				else if (tmp.get(Calendar.MINUTE) + 5 >= Config.FS_TIME_ATTACK)
				{
					managerSay((byte) 90); // sending a unique id :D
				}
				tmp = null;
			}
			else if (inEntryTime)
			{
				managerSay((byte) 0);
			}
		}
	}
	
	protected class ChangeEntryTime implements Runnable
	{
		@Override
		public void run()
		{
			// LOGGER.info("FourSepulchersManager:In Entry Time");
			inEntryTime = true;
			inWarmUpTime = false;
			inAttackTime = false;
			inCoolDownTime = false;
			
			long interval = 0;
			// if this is first launch - search time when entry time will be
			// ended:
			// counting difference between time when entry time ends and current
			// time
			// and then launching change time task
			if (firstTimeRun)
			{
				interval = entryTimeEnd - Calendar.getInstance().getTimeInMillis();
			}
			else
			{
				interval = Config.FS_TIME_ENTRY * 60000; // else use stupid
				// method
			}
			
			// launching saying process...
			ThreadPoolManager.getInstance().scheduleGeneral(new ManagerSay(), 0);
			changeWarmUpTimeTask = ThreadPoolManager.getInstance().scheduleEffect(new ChangeWarmUpTime(), interval);
			if (changeEntryTimeTask != null)
			{
				changeEntryTimeTask.cancel(true);
				changeEntryTimeTask = null;
			}
			
		}
	}
	
	protected class ChangeWarmUpTime implements Runnable
	{
		@Override
		public void run()
		{
			// LOGGER.info("FourSepulchersManager:In Warm-Up Time");
			inEntryTime = true;
			inWarmUpTime = false;
			inAttackTime = false;
			inCoolDownTime = false;
			
			long interval = 0;
			// searching time when warmup time will be ended:
			// counting difference between time when warmup time ends and
			// current time
			// and then launching change time task
			if (firstTimeRun)
			{
				interval = warmUpTimeEnd - Calendar.getInstance().getTimeInMillis();
			}
			else
			{
				interval = Config.FS_TIME_WARMUP * 60000;
			}
			changeAttackTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChangeAttackTime(), interval);
			
			if (changeWarmUpTimeTask != null)
			{
				changeWarmUpTimeTask.cancel(true);
				changeWarmUpTimeTask = null;
			}
		}
	}
	
	protected class ChangeAttackTime implements Runnable
	{
		@Override
		public void run()
		{
			// LOGGER.info("FourSepulchersManager:In Attack Time");
			inEntryTime = false;
			inWarmUpTime = false;
			inAttackTime = true;
			inCoolDownTime = false;
			
			locationShadowSpawns();
			
			spawnMysteriousBox(31921);
			spawnMysteriousBox(31922);
			spawnMysteriousBox(31923);
			spawnMysteriousBox(31924);
			
			if (!firstTimeRun)
			{
				warmUpTimeEnd = Calendar.getInstance().getTimeInMillis();
			}
			
			long interval = 0;
			// say task
			if (firstTimeRun)
			{
				for (double min = Calendar.getInstance().get(Calendar.MINUTE); min < newCycleMin; min++)
				{
					// looking for next shout time....
					if (min % 5 == 0)// check if min can be divided by 5
					{
						// LOGGER.info(Calendar.getInstance().getTime()
						// + " Atk announce scheduled to " + min
						// + " minute of this hour.");
						final Calendar inter = Calendar.getInstance();
						inter.set(Calendar.MINUTE, (int) min);
						ThreadPoolManager.getInstance().scheduleGeneral(new ManagerSay(), inter.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
						break;
					}
				}
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new ManagerSay(), 5 * 60400);
			}
			// searching time when attack time will be ended:
			// counting difference between time when attack time ends and
			// current time
			// and then launching change time task
			if (firstTimeRun)
			{
				interval = attackTimeEnd - Calendar.getInstance().getTimeInMillis();
			}
			else
			{
				interval = Config.FS_TIME_ATTACK * 60000;
			}
			changeCoolDownTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChangeCoolDownTime(), interval);
			
			if (changeAttackTimeTask != null)
			{
				changeAttackTimeTask.cancel(true);
				changeAttackTimeTask = null;
			}
		}
	}
	
	protected class ChangeCoolDownTime implements Runnable
	{
		@Override
		public void run()
		{
			// LOGGER.info("FourSepulchersManager:In Cool-Down Time");
			inEntryTime = false;
			inWarmUpTime = false;
			inAttackTime = false;
			inCoolDownTime = true;
			
			clean();
			
			Calendar time = Calendar.getInstance();
			// one hour = 55th min to 55 min of next hour, so we check for this,
			// also check for first launch
			if (Calendar.getInstance().get(Calendar.MINUTE) > newCycleMin && !firstTimeRun)
			{
				time.set(Calendar.HOUR, Calendar.getInstance().get(Calendar.HOUR) + 1);
			}
			time.set(Calendar.MINUTE, newCycleMin);
			// LOGGER.info("FourSepulchersManager: Entry time: " + time.getTime());
			if (firstTimeRun)
			{
				firstTimeRun = false; // cooldown phase ends event hour, so it
				// will be not first run
			}
			
			final long interval = time.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
			changeEntryTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChangeEntryTime(), interval);
			
			if (changeCoolDownTimeTask != null)
			{
				changeCoolDownTimeTask.cancel(true);
				changeCoolDownTimeTask = null;
			}
			time = null;
		}
	}
	
	public Map<Integer, Integer> getHallGateKeepers()
	{
		return hallGateKeepers;
	}
	
	public void showHtmlFile(final L2PcInstance player, final String file, final L2NpcInstance npc, final L2PcInstance member)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile("data/html/SepulcherNpc/" + file);
		if (member != null)
		{
			html.replace("%member%", member.getName());
		}
		player.sendPacket(html);
		html = null;
	}
	
	private static class SingletonHolder
	{
		protected static final FourSepulchersManager instance = new FourSepulchersManager();
	}
}
