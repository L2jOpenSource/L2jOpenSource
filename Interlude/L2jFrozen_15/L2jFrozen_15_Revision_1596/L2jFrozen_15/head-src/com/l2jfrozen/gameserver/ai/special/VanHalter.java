package com.l2jfrozen.gameserver.ai.special;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.csv.DoorTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.SpecialCamera;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.random.Rnd;

/**
 * This class ... control for sequence of fight against "High Priestess van Halter".
 * @version $Revision: $ $Date: $
 * @author  L2J_JP SANDMAN
 **/

public class VanHalter extends Quest implements Runnable
{
	private static final Logger LOGGER = Logger.getLogger(VanHalter.class);
	
	// List of intruders.
	protected Map<Integer, List<L2PcInstance>> bleedingPlayers = new HashMap<>();
	
	// Spawn data of monsters.
	protected Map<Integer, L2Spawn> monsterSpawn = new HashMap<>();
	protected List<L2Spawn> royalGuardSpawn = new ArrayList<>();
	protected List<L2Spawn> royalGuardCaptainSpawn = new ArrayList<>();
	protected List<L2Spawn> royalGuardHelperSpawn = new ArrayList<>();
	protected List<L2Spawn> triolRevelationSpawn = new ArrayList<>();
	protected List<L2Spawn> triolRevelationAlive = new ArrayList<>();
	protected List<L2Spawn> guardOfAltarSpawn = new ArrayList<>();
	protected Map<Integer, L2Spawn> cameraMarkerSpawn = new HashMap<>();
	protected L2Spawn ritualOfferingSpawn = null;
	protected L2Spawn ritualSacrificeSpawn = null;
	protected L2Spawn vanHalterSpawn = null;
	
	// Instance of monsters.
	protected List<L2NpcInstance> monsters = new ArrayList<>();
	protected List<L2NpcInstance> royalGuard = new ArrayList<>();
	protected List<L2NpcInstance> royalGuardCaptain = new ArrayList<>();
	protected List<L2NpcInstance> royalGuardHepler = new ArrayList<>();
	protected List<L2NpcInstance> triolRevelation = new ArrayList<>();
	protected List<L2NpcInstance> guardOfAltar = new ArrayList<>();
	protected Map<Integer, L2NpcInstance> cameraMarker = new HashMap<>();
	protected List<L2DoorInstance> doorOfAltar = new ArrayList<>();
	protected List<L2DoorInstance> doorOfSacrifice = new ArrayList<>();
	protected L2NpcInstance ritualOffering = null;
	protected L2NpcInstance ritualSacrifice = null;
	protected L2RaidBossInstance vanHalter = null;
	
	// Task
	protected ScheduledFuture<?> movieTask = null;
	protected ScheduledFuture<?> closeDoorOfAltarTask = null;
	protected ScheduledFuture<?> openDoorOfAltarTask = null;
	protected ScheduledFuture<?> lockUpDoorOfAltarTask = null;
	protected ScheduledFuture<?> callRoyalGuardHelperTask = null;
	protected ScheduledFuture<?> timeUpTask = null;
	protected ScheduledFuture<?> intervalTask = null;
	protected ScheduledFuture<?> halterEscapeTask = null;
	protected ScheduledFuture<?> setBleedTask = null;
	
	// State of High Priestess van Halter
	boolean isLocked = false;
	boolean isHalterSpawned = false;
	boolean isSacrificeSpawned = false;
	boolean isCaptainSpawned = false;
	boolean isHelperCalled = false;
	
	// VanHalter Status Tracking :
	private static final byte INTERVAL = 0;
	private static final byte NOTSPAWN = 1;
	private static final byte ALIVE = 2;
	
	// Initialize
	public VanHalter(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		
		final int[] mobs =
		{
			29062,
			22188,
			32058,
			32059,
			32060,
			32061,
			32062,
			32063,
			32064,
			32065,
			32066
		};
		
		addEventId(29062, Quest.QuestEventType.ON_ATTACK);
		for (final int mob : mobs)
		{
			addEventId(mob, Quest.QuestEventType.ON_KILL);
		}
		
		// GrandBossManager.getInstance().addBoss(29062);
		// Clear flag.
		isLocked = false;
		isCaptainSpawned = false;
		isHelperCalled = false;
		isHalterSpawned = false;
		
		// Setting door state.
		doorOfAltar.add(DoorTable.getInstance().getDoor(19160014));
		doorOfAltar.add(DoorTable.getInstance().getDoor(19160015));
		openDoorOfAltar(true);
		doorOfSacrifice.add(DoorTable.getInstance().getDoor(19160016));
		doorOfSacrifice.add(DoorTable.getInstance().getDoor(19160017));
		closeDoorOfSacrifice();
		
		// Load spawn data of monsters.
		loadRoyalGuard();
		loadTriolRevelation();
		loadRoyalGuardCaptain();
		loadRoyalGuardHelper();
		loadGuardOfAltar();
		loadVanHalter();
		loadRitualOffering();
		loadRitualSacrifice();
		
		// Spawn monsters.
		spawnRoyalGuard();
		spawnTriolRevelation();
		spawnVanHalter();
		spawnRitualOffering();
		
		// Setting spawn data of Dummy camera marker.
		cameraMarkerSpawn.clear();
		try
		{
			final L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(13014); // Dummy npc
			L2Spawn tempSpawn;
			
			// Dummy camera marker.
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55200);
			tempSpawn.setLocz(-10449);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			cameraMarkerSpawn.put(1, tempSpawn);
			
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55200);
			tempSpawn.setLocz(-10051);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			cameraMarkerSpawn.put(2, tempSpawn);
			
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55200);
			tempSpawn.setLocz(-9741);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			cameraMarkerSpawn.put(3, tempSpawn);
			
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55200);
			tempSpawn.setLocz(-9394);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			cameraMarkerSpawn.put(4, tempSpawn);
			
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55197);
			tempSpawn.setLocz(-8739);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			cameraMarkerSpawn.put(5, tempSpawn);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("VanHalterManager : " + e.getMessage() + " :" + e);
		}
		
		// Set time up.
		if (timeUpTask != null)
		{
			timeUpTask.cancel(false);
		}
		timeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), Config.HPH_ACTIVITYTIMEOFHALTER);
		
		// Set bleeding to palyers.
		if (setBleedTask != null)
		{
			setBleedTask.cancel(false);
		}
		setBleedTask = ThreadPoolManager.getInstance().scheduleGeneral(new Bleeding(), 2000);
		
		final Integer status = GrandBossManager.getInstance().getBossStatus(29062);
		if (status == INTERVAL)
		{
			enterInterval();
		}
		else
		{
			GrandBossManager.getInstance().setBossStatus(29062, NOTSPAWN);
		}
	}
	
	@Override
	public String onAttack(final L2NpcInstance npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		if (npc.getNpcId() == 29062)
		{
			if ((int) (npc.getStatus().getCurrentHp() / npc.getMaxHp()) * 100 <= 20)
			{
				callRoyalGuardHelper();
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(final L2NpcInstance npc, final L2PcInstance killer, final boolean isPet)
	{
		final int npcId = npc.getNpcId();
		if (npcId == 32058 || npcId == 32059 || npcId == 32060 || npcId == 32061 || npcId == 32062 || npcId == 32063 || npcId == 32064 || npcId == 32065 || npcId == 32066)
		{
			removeBleeding(npcId);
		}
		checkTriolRevelationDestroy();
		if (npcId == 22188)
		{
			checkRoyalGuardCaptainDestroy();
		}
		if (npcId == 29062)
		{
			enterInterval();
		}
		return super.onKill(npc, killer, isPet);
	}
	
	// Load Royal Guard.
	protected void loadRoyalGuard()
	{
		royalGuardSpawn.clear();
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid between ? and ? ORDER BY id");
			statement.setInt(1, 22175);
			statement.setInt(2, 22176);
			final ResultSet rset = statement.executeQuery();
			
			L2Spawn spawnDat;
			L2NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					royalGuardSpawn.add(spawnDat);
				}
				else
				{
					LOGGER.warn("VanHalterManager.loadRoyalGuard: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			if (Config.DEBUG)
			{
				LOGGER.info("VanHalterManager.loadRoyalGuard: Loaded " + royalGuardSpawn.size() + " Royal Guard spawn locations.");
			}
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			// Problem with initializing spawn, go to next one
			LOGGER.warn("VanHalterManager.loadRoyalGuard: Spawn could not be initialized: " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	protected void spawnRoyalGuard()
	{
		if (!royalGuard.isEmpty())
		{
			deleteRoyalGuard();
		}
		
		for (final L2Spawn rgs : royalGuardSpawn)
		{
			rgs.startRespawn();
			royalGuard.add(rgs.doSpawn());
		}
	}
	
	protected void deleteRoyalGuard()
	{
		for (final L2NpcInstance rg : royalGuard)
		{
			rg.getSpawn().stopRespawn();
			rg.deleteMe();
		}
		
		royalGuard.clear();
	}
	
	// Load Triol's Revelation.
	protected void loadTriolRevelation()
	{
		triolRevelationSpawn.clear();
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid between ? and ? ORDER BY id");
			statement.setInt(1, 32058);
			statement.setInt(2, 32068);
			final ResultSet rset = statement.executeQuery();
			
			L2Spawn spawnDat;
			L2NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					triolRevelationSpawn.add(spawnDat);
				}
				else
				{
					LOGGER.warn("VanHalterManager.loadTriolRevelation: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			if (Config.DEBUG)
			{
				LOGGER.info("VanHalterManager.loadTriolRevelation: Loaded " + triolRevelationSpawn.size() + " Triol's Revelation spawn locations.");
			}
		}
		catch (final Exception e)
		{
			// Problem with initializing spawn, go to next one
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("VanHalterManager.loadTriolRevelation: Spawn could not be initialized: " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	protected void spawnTriolRevelation()
	{
		if (!triolRevelation.isEmpty())
		{
			deleteTriolRevelation();
		}
		
		for (final L2Spawn trs : triolRevelationSpawn)
		{
			trs.startRespawn();
			triolRevelation.add(trs.doSpawn());
			if (trs.getNpcid() != 32067 && trs.getNpcid() != 32068)
			{
				triolRevelationAlive.add(trs);
			}
		}
	}
	
	protected void deleteTriolRevelation()
	{
		for (final L2NpcInstance tr : triolRevelation)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}
		triolRevelation.clear();
		bleedingPlayers.clear();
	}
	
	// Load Royal Guard Captain.
	protected void loadRoyalGuardCaptain()
	{
		royalGuardCaptainSpawn.clear();
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 22188);
			final ResultSet rset = statement.executeQuery();
			
			L2Spawn spawnDat;
			L2NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					royalGuardCaptainSpawn.add(spawnDat);
				}
				else
				{
					LOGGER.warn("VanHalterManager.loadRoyalGuardCaptain: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			if (Config.DEBUG)
			{
				LOGGER.info("VanHalterManager.loadRoyalGuardCaptain: Loaded " + royalGuardCaptainSpawn.size() + " Royal Guard Captain spawn locations.");
			}
		}
		catch (final Exception e)
		{
			// Problem with initializing spawn, go to next one
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("VanHalterManager.loadRoyalGuardCaptain: Spawn could not be initialized: " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	protected void spawnRoyalGuardCaptain()
	{
		if (!royalGuardCaptain.isEmpty())
		{
			deleteRoyalGuardCaptain();
		}
		
		for (final L2Spawn trs : royalGuardCaptainSpawn)
		{
			trs.startRespawn();
			royalGuardCaptain.add(trs.doSpawn());
		}
		isCaptainSpawned = true;
	}
	
	protected void deleteRoyalGuardCaptain()
	{
		for (final L2NpcInstance tr : royalGuardCaptain)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}
		
		royalGuardCaptain.clear();
	}
	
	// Load Royal Guard Helper.
	protected void loadRoyalGuardHelper()
	{
		royalGuardHelperSpawn.clear();
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 22191);
			final ResultSet rset = statement.executeQuery();
			
			L2Spawn spawnDat;
			L2NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					royalGuardHelperSpawn.add(spawnDat);
				}
				else
				{
					LOGGER.warn("VanHalterManager.loadRoyalGuardHelper: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			if (Config.DEBUG)
			{
				LOGGER.info("VanHalterManager.loadRoyalGuardHelper: Loaded " + royalGuardHelperSpawn.size() + " Royal Guard Helper spawn locations.");
			}
		}
		catch (final Exception e)
		{
			// Problem with initializing spawn, go to next one
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("VanHalterManager.loadRoyalGuardHelper: Spawn could not be initialized: " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	protected void spawnRoyalGuardHepler()
	{
		for (final L2Spawn trs : royalGuardHelperSpawn)
		{
			trs.startRespawn();
			royalGuardHepler.add(trs.doSpawn());
		}
	}
	
	protected void deleteRoyalGuardHepler()
	{
		for (final L2NpcInstance tr : royalGuardHepler)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}
		royalGuardHepler.clear();
	}
	
	// Load Guard Of Altar
	protected void loadGuardOfAltar()
	{
		guardOfAltarSpawn.clear();
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 32051);
			final ResultSet rset = statement.executeQuery();
			
			L2Spawn spawnDat;
			L2NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					guardOfAltarSpawn.add(spawnDat);
				}
				else
				{
					LOGGER.warn("VanHalterManager.loadGuardOfAltar: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			if (Config.DEBUG)
			{
				LOGGER.info("VanHalterManager.loadGuardOfAltar: Loaded " + guardOfAltarSpawn.size() + " Guard Of Altar spawn locations.");
			}
		}
		catch (final Exception e)
		{
			// Problem with initializing spawn, go to next one
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("VanHalterManager.loadGuardOfAltar: Spawn could not be initialized: " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	protected void spawnGuardOfAltar()
	{
		if (!guardOfAltar.isEmpty())
		{
			deleteGuardOfAltar();
		}
		
		for (final L2Spawn trs : guardOfAltarSpawn)
		{
			trs.startRespawn();
			guardOfAltar.add(trs.doSpawn());
		}
	}
	
	protected void deleteGuardOfAltar()
	{
		for (final L2NpcInstance tr : guardOfAltar)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}
		
		guardOfAltar.clear();
	}
	
	// Load High Priestess van Halter.
	protected void loadVanHalter()
	{
		vanHalterSpawn = null;
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 29062);
			final ResultSet rset = statement.executeQuery();
			
			L2Spawn spawnDat;
			L2NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					vanHalterSpawn = spawnDat;
				}
				else
				{
					LOGGER.warn("VanHalterManager.loadVanHalter: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			if (Config.DEBUG)
			{
				LOGGER.info("VanHalterManager.loadVanHalter: Loaded High Priestess van Halter spawn locations.");
			}
		}
		catch (final Exception e)
		{
			// Problem with initializing spawn, go to next one
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("VanHalterManager.loadVanHalter: Spawn could not be initialized: " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	protected void spawnVanHalter()
	{
		vanHalter = (L2RaidBossInstance) vanHalterSpawn.doSpawn();
		// vanHalter.setIsImmobilized(true);
		vanHalter.setIsInvul(true);
		isHalterSpawned = true;
	}
	
	protected void deleteVanHalter()
	{
		// vanHalter.setIsImmobilized(false);
		vanHalter.setIsInvul(false);
		vanHalter.getSpawn().stopRespawn();
		vanHalter.deleteMe();
	}
	
	// Load Ritual Offering.
	protected void loadRitualOffering()
	{
		ritualOfferingSpawn = null;
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 32038);
			final ResultSet rset = statement.executeQuery();
			
			L2Spawn spawnDat;
			L2NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					ritualOfferingSpawn = spawnDat;
				}
				else
				{
					LOGGER.warn("VanHalterManager.loadRitualOffering: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			if (Config.DEBUG)
			{
				LOGGER.info("VanHalterManager.loadRitualOffering: Loaded Ritual Offering spawn locations.");
			}
		}
		catch (final Exception e)
		{
			// Problem with initializing spawn, go to next one
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("VanHalterManager.loadRitualOffering: Spawn could not be initialized: " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	protected void spawnRitualOffering()
	{
		ritualOffering = ritualOfferingSpawn.doSpawn();
		// ritualOffering.setIsImmobilized(true);
		ritualOffering.setIsInvul(true);
		ritualOffering.setIsParalyzed(true);
	}
	
	protected void deleteRitualOffering()
	{
		// ritualOffering.setIsImmobilized(false);
		ritualOffering.setIsInvul(false);
		ritualOffering.setIsParalyzed(false);
		ritualOffering.getSpawn().stopRespawn();
		ritualOffering.deleteMe();
	}
	
	// Load Ritual Sacrifice.
	protected void loadRitualSacrifice()
	{
		ritualSacrificeSpawn = null;
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 22195);
			final ResultSet rset = statement.executeQuery();
			
			L2Spawn spawnDat;
			L2NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					ritualSacrificeSpawn = spawnDat;
				}
				else
				{
					LOGGER.warn("VanHalterManager.loadRitualSacrifice: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			if (Config.DEBUG)
			{
				LOGGER.info("VanHalterManager.loadRitualSacrifice: Loaded Ritual Sacrifice spawn locations.");
			}
		}
		catch (final Exception e)
		{
			// Problem with initializing spawn, go to next one
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("VanHalterManager.loadRitualSacrifice: Spawn could not be initialized: " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	protected void spawnRitualSacrifice()
	{
		ritualSacrifice = ritualSacrificeSpawn.doSpawn();
		// ritualSacrifice.setIsImmobilized(true);
		ritualSacrifice.setIsInvul(true);
		isSacrificeSpawned = true;
	}
	
	protected void deleteRitualSacrifice()
	{
		if (!isSacrificeSpawned)
		{
			return;
		}
		
		ritualSacrifice.getSpawn().stopRespawn();
		ritualSacrifice.deleteMe();
		isSacrificeSpawned = false;
	}
	
	protected void spawnCameraMarker()
	{
		cameraMarker.clear();
		for (int i = 1; i <= cameraMarkerSpawn.size(); i++)
		{
			cameraMarker.put(i, cameraMarkerSpawn.get(i).doSpawn());
			cameraMarker.get(i).getSpawn().stopRespawn();
			cameraMarker.get(i).setIsImobilised(true);
		}
	}
	
	protected void deleteCameraMarker()
	{
		if (cameraMarker.isEmpty())
		{
			return;
		}
		
		for (int i = 1; i <= cameraMarker.size(); i++)
		{
			cameraMarker.get(i).deleteMe();
		}
		cameraMarker.clear();
	}
	
	// Door control.
	/**
	 * @param intruder
	 */
	public void intruderDetection(final L2PcInstance intruder)
	{
		if (lockUpDoorOfAltarTask == null && !isLocked && isCaptainSpawned)
		{
			lockUpDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new LockUpDoorOfAltar(), Config.HPH_TIMEOFLOCKUPDOOROFALTAR);
		}
	}
	
	protected class LockUpDoorOfAltar implements Runnable
	{
		@Override
		public void run()
		{
			closeDoorOfAltar(false);
			isLocked = true;
			lockUpDoorOfAltarTask = null;
		}
	}
	
	protected void openDoorOfAltar(final boolean loop)
	{
		for (final L2DoorInstance door : doorOfAltar)
		{
			try
			{
				door.openMe();
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				LOGGER.warn(e.getMessage() + " :" + e);
			}
		}
		
		if (loop)
		{
			isLocked = false;
			
			if (closeDoorOfAltarTask != null)
			{
				closeDoorOfAltarTask.cancel(false);
			}
			closeDoorOfAltarTask = null;
			closeDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new CloseDoorOfAltar(), Config.HPH_INTERVALOFDOOROFALTER);
		}
		else
		{
			if (closeDoorOfAltarTask != null)
			{
				closeDoorOfAltarTask.cancel(false);
			}
			closeDoorOfAltarTask = null;
		}
	}
	
	protected class OpenDoorOfAltar implements Runnable
	{
		@Override
		public void run()
		{
			openDoorOfAltar(true);
		}
	}
	
	protected void closeDoorOfAltar(final boolean loop)
	{
		for (final L2DoorInstance door : doorOfAltar)
		{
			door.closeMe();
		}
		
		if (loop)
		{
			if (openDoorOfAltarTask != null)
			{
				openDoorOfAltarTask.cancel(false);
			}
			openDoorOfAltarTask = null;
			openDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new OpenDoorOfAltar(), Config.HPH_INTERVALOFDOOROFALTER);
		}
		else
		{
			if (openDoorOfAltarTask != null)
			{
				openDoorOfAltarTask.cancel(false);
			}
			openDoorOfAltarTask = null;
		}
	}
	
	protected class CloseDoorOfAltar implements Runnable
	{
		@Override
		public void run()
		{
			closeDoorOfAltar(true);
		}
	}
	
	protected void openDoorOfSacrifice()
	{
		for (final L2DoorInstance door : doorOfSacrifice)
		{
			try
			{
				door.openMe();
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				LOGGER.warn(e.getMessage() + " :" + e);
			}
		}
	}
	
	protected void closeDoorOfSacrifice()
	{
		for (final L2DoorInstance door : doorOfSacrifice)
		{
			try
			{
				door.closeMe();
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				LOGGER.warn(e.getMessage() + " :" + e);
			}
		}
	}
	
	// event
	public void checkTriolRevelationDestroy()
	{
		if (isCaptainSpawned)
		{
			return;
		}
		
		boolean isTriolRevelationDestroyed = true;
		for (final L2Spawn tra : triolRevelationAlive)
		{
			if (!tra.getLastSpawn().isDead())
			{
				isTriolRevelationDestroyed = false;
			}
		}
		
		if (isTriolRevelationDestroyed)
		{
			spawnRoyalGuardCaptain();
		}
	}
	
	public void checkRoyalGuardCaptainDestroy()
	{
		if (!isHalterSpawned)
		{
			return;
		}
		
		deleteRoyalGuard();
		deleteRoyalGuardCaptain();
		spawnGuardOfAltar();
		openDoorOfSacrifice();
		
		// vanHalter.setIsImmobilized(true);
		vanHalter.setIsInvul(true);
		spawnCameraMarker();
		
		if (timeUpTask != null)
		{
			timeUpTask.cancel(false);
		}
		timeUpTask = null;
		
		movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(1), Config.HPH_APPTIMEOFHALTER);
	}
	
	// Start fight against High Priestess van Halter.
	protected void combatBeginning()
	{
		if (timeUpTask != null)
		{
			timeUpTask.cancel(false);
		}
		timeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), Config.HPH_FIGHTTIMEOFHALTER);
		
		final Map<Integer, L2PcInstance> targets = new HashMap<>();
		int i = 0;
		
		for (final L2PcInstance pc : vanHalter.getKnownList().getKnownPlayers().values())
		{
			i++;
			targets.put(i, pc);
		}
		
		vanHalter.reduceCurrentHp(1, targets.get(Rnd.get(1, i)));
	}
	
	// Call Royal Guard Helper and escape from player.
	public void callRoyalGuardHelper()
	{
		if (!isHelperCalled)
		{
			isHelperCalled = true;
			halterEscapeTask = ThreadPoolManager.getInstance().scheduleGeneral(new HalterEscape(), 500);
			callRoyalGuardHelperTask = ThreadPoolManager.getInstance().scheduleGeneral(new CallRoyalGuardHelper(), 1000);
		}
	}
	
	protected class CallRoyalGuardHelper implements Runnable
	{
		@Override
		public void run()
		{
			spawnRoyalGuardHepler();
			
			if (royalGuardHepler.size() <= Config.HPH_CALLROYALGUARDHELPERCOUNT && !vanHalter.isDead())
			{
				if (callRoyalGuardHelperTask != null)
				{
					callRoyalGuardHelperTask.cancel(false);
				}
				callRoyalGuardHelperTask = ThreadPoolManager.getInstance().scheduleGeneral(new CallRoyalGuardHelper(), Config.HPH_CALLROYALGUARDHELPERINTERVAL);
			}
			else
			{
				if (callRoyalGuardHelperTask != null)
				{
					callRoyalGuardHelperTask.cancel(false);
				}
				callRoyalGuardHelperTask = null;
			}
		}
	}
	
	protected class HalterEscape implements Runnable
	{
		@Override
		public void run()
		{
			if (royalGuardHepler.size() <= Config.HPH_CALLROYALGUARDHELPERCOUNT && !vanHalter.isDead())
			{
				if (vanHalter.isAfraid())
				{
					vanHalter.stopEffects(L2Effect.EffectType.FEAR);
					vanHalter.setIsAfraid(false);
					vanHalter.updateAbnormalEffect();
				}
				else
				{
					vanHalter.startFear();
					if (vanHalter.getZ() >= -10476)
					{
						final L2CharPosition pos = new L2CharPosition(-16397, -53308, -10448, 0);
						if (vanHalter.getX() == pos.x && vanHalter.getY() == pos.y)
						{
							vanHalter.stopEffects(L2Effect.EffectType.FEAR);
							vanHalter.setIsAfraid(false);
							vanHalter.updateAbnormalEffect();
						}
						else
						{
							vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
						}
					}
					else if (vanHalter.getX() >= -16397)
					{
						final L2CharPosition pos = new L2CharPosition(-15548, -54830, -10475, 0);
						vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
					}
					else
					{
						final L2CharPosition pos = new L2CharPosition(-17248, -54830, -10475, 0);
						vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
					}
				}
				if (halterEscapeTask != null)
				{
					halterEscapeTask.cancel(false);
				}
				halterEscapeTask = ThreadPoolManager.getInstance().scheduleGeneral(new HalterEscape(), 5000);
			}
			else
			{
				vanHalter.stopEffects(L2Effect.EffectType.FEAR);
				vanHalter.setIsAfraid(false);
				vanHalter.updateAbnormalEffect();
				if (halterEscapeTask != null)
				{
					halterEscapeTask.cancel(false);
				}
				halterEscapeTask = null;
			}
		}
	}
	
	// Check bleeding player.
	protected void addBleeding()
	{
		final L2Skill bleed = SkillTable.getInstance().getInfo(4615, 12);
		
		for (final L2NpcInstance tr : triolRevelation)
		{
			if (!tr.getKnownList().getKnownPlayersInRadius(tr.getAggroRange()).iterator().hasNext() || tr.isDead())
			{
				continue;
			}
			
			final List<L2PcInstance> bpc = new ArrayList<>();
			
			for (final L2PcInstance pc : tr.getKnownList().getKnownPlayersInRadius(tr.getAggroRange()))
			{
				if (pc.getFirstEffect(bleed) == null)
				{
					bleed.getEffects(tr, pc, false, false, false);
					tr.broadcastPacket(new MagicSkillUser(tr, pc, bleed.getId(), 12, 1, 1));
				}
				
				bpc.add(pc);
			}
			bleedingPlayers.remove(tr.getNpcId());
			bleedingPlayers.put(tr.getNpcId(), bpc);
		}
	}
	
	public void removeBleeding(final int npcId)
	{
		if (bleedingPlayers.get(npcId) == null)
		{
			return;
		}
		
		for (L2PcInstance pc : bleedingPlayers.get(npcId))
		{
			if (pc.getFirstEffect(L2Effect.EffectType.DMG_OVER_TIME) != null)
			{
				pc.stopEffects(L2Effect.EffectType.DMG_OVER_TIME);
			}
		}
		
		bleedingPlayers.remove(npcId);
	}
	
	protected class Bleeding implements Runnable
	{
		@Override
		public void run()
		{
			addBleeding();
			
			if (setBleedTask != null)
			{
				setBleedTask.cancel(false);
			}
			setBleedTask = ThreadPoolManager.getInstance().scheduleGeneral(new Bleeding(), 2000);
		}
	}
	
	// High Priestess van Halter dead or time up.
	public void enterInterval()
	{
		// Cancel all task
		if (callRoyalGuardHelperTask != null)
		{
			callRoyalGuardHelperTask.cancel(false);
		}
		callRoyalGuardHelperTask = null;
		
		if (closeDoorOfAltarTask != null)
		{
			closeDoorOfAltarTask.cancel(false);
		}
		closeDoorOfAltarTask = null;
		
		if (halterEscapeTask != null)
		{
			halterEscapeTask.cancel(false);
		}
		halterEscapeTask = null;
		
		if (intervalTask != null)
		{
			intervalTask.cancel(false);
		}
		intervalTask = null;
		
		if (lockUpDoorOfAltarTask != null)
		{
			lockUpDoorOfAltarTask.cancel(false);
		}
		lockUpDoorOfAltarTask = null;
		
		if (movieTask != null)
		{
			movieTask.cancel(false);
		}
		movieTask = null;
		
		if (openDoorOfAltarTask != null)
		{
			openDoorOfAltarTask.cancel(false);
		}
		openDoorOfAltarTask = null;
		
		if (timeUpTask != null)
		{
			timeUpTask.cancel(false);
		}
		timeUpTask = null;
		
		// Delete monsters
		if (vanHalter.isDead())
		{
			vanHalter.getSpawn().stopRespawn();
		}
		else
		{
			deleteVanHalter();
		}
		deleteRoyalGuardHepler();
		deleteRoyalGuardCaptain();
		deleteRoyalGuard();
		deleteRitualOffering();
		deleteRitualSacrifice();
		deleteGuardOfAltar();
		
		// Set interval end.
		if (intervalTask != null)
		{
			intervalTask.cancel(false);
		}
		
		final Integer status = GrandBossManager.getInstance().getBossStatus(29062);
		
		if (status != INTERVAL)
		{
			final long interval = Rnd.get(Config.HPH_FIXINTERVALOFHALTER, Config.HPH_FIXINTERVALOFHALTER + Config.HPH_RANDOMINTERVALOFHALTER)/* * 3600000 */;
			final StatsSet info = GrandBossManager.getInstance().getStatsSet(29062);
			info.set("respawn_time", (System.currentTimeMillis() + interval));
			GrandBossManager.getInstance().setStatsSet(29062, info);
			GrandBossManager.getInstance().setBossStatus(29062, INTERVAL);
		}
		
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(29062);
		final long temp = info.getLong("respawn_time") - System.currentTimeMillis();
		intervalTask = ThreadPoolManager.getInstance().scheduleGeneral(new Interval(), temp);
	}
	
	// Interval.
	protected class Interval implements Runnable
	{
		@Override
		public void run()
		{
			setupAltar();
		}
	}
	
	// Interval end.
	public void setupAltar()
	{
		// Cancel all task
		if (callRoyalGuardHelperTask != null)
		{
			callRoyalGuardHelperTask.cancel(false);
		}
		callRoyalGuardHelperTask = null;
		
		if (closeDoorOfAltarTask != null)
		{
			closeDoorOfAltarTask.cancel(false);
		}
		closeDoorOfAltarTask = null;
		
		if (halterEscapeTask != null)
		{
			halterEscapeTask.cancel(false);
		}
		halterEscapeTask = null;
		
		if (intervalTask != null)
		{
			intervalTask.cancel(false);
		}
		intervalTask = null;
		
		if (lockUpDoorOfAltarTask != null)
		{
			lockUpDoorOfAltarTask.cancel(false);
		}
		lockUpDoorOfAltarTask = null;
		
		if (movieTask != null)
		{
			movieTask.cancel(false);
		}
		movieTask = null;
		
		if (openDoorOfAltarTask != null)
		{
			openDoorOfAltarTask.cancel(false);
		}
		openDoorOfAltarTask = null;
		
		if (timeUpTask != null)
		{
			timeUpTask.cancel(false);
		}
		timeUpTask = null;
		
		// Delete all monsters
		deleteVanHalter();
		deleteTriolRevelation();
		deleteRoyalGuardHepler();
		deleteRoyalGuardCaptain();
		deleteRoyalGuard();
		deleteRitualSacrifice();
		deleteRitualOffering();
		deleteGuardOfAltar();
		deleteCameraMarker();
		
		// Clear flag.
		isLocked = false;
		isCaptainSpawned = false;
		isHelperCalled = false;
		isHalterSpawned = false;
		
		// Set door state
		closeDoorOfSacrifice();
		openDoorOfAltar(true);
		
		// Respawn monsters.
		spawnTriolRevelation();
		spawnRoyalGuard();
		spawnRitualOffering();
		spawnVanHalter();
		
		GrandBossManager.getInstance().setBossStatus(29062, NOTSPAWN);
		
		// Set time up.
		if (timeUpTask != null)
		{
			timeUpTask.cancel(false);
		}
		timeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), Config.HPH_ACTIVITYTIMEOFHALTER);
	}
	
	// Time up.
	protected class TimeUp implements Runnable
	{
		@Override
		public void run()
		{
			enterInterval();
		}
	}
	
	// Appearance movie.
	private class Movie implements Runnable
	{
		private final int distance = 6502500;
		private final int taskId;
		
		public Movie(final int taskId)
		{
			this.taskId = taskId;
		}
		
		@Override
		public void run()
		{
			vanHalter.setHeading(16384);
			vanHalter.setTarget(ritualOffering);
			
			switch (taskId)
			{
				case 1:
					GrandBossManager.getInstance().setBossStatus(29062, ALIVE);
					
					// Set camera.
					for (final L2PcInstance pc : vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(vanHalter) <= distance)
						{
							vanHalter.broadcastPacket(new SpecialCamera(vanHalter.getObjectId(), 50, 90, 0, 0, 15000));
						}
					}
					
					// Set next task.
					if (movieTask != null)
					{
						movieTask.cancel(false);
					}
					movieTask = null;
					movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(2), 16);
					
					break;
				
				case 2:
					// Set camera.
					for (final L2PcInstance pc : vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(cameraMarker.get(5)) <= distance)
						{
							cameraMarker.get(5).broadcastPacket(new SpecialCamera(cameraMarker.get(5).getObjectId(), 1842, 100, -3, 0, 15000));
						}
					}
					
					// Set next task.
					if (movieTask != null)
					{
						movieTask.cancel(false);
					}
					movieTask = null;
					movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(3), 1);
					
					break;
				
				case 3:
					// Set camera.
					for (final L2PcInstance pc : vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(cameraMarker.get(5)) <= distance)
						{
							cameraMarker.get(5).broadcastPacket(new SpecialCamera(cameraMarker.get(5).getObjectId(), 1861, 97, -10, 1500, 15000));
						}
					}
					
					// Set next task.
					if (movieTask != null)
					{
						movieTask.cancel(false);
					}
					movieTask = null;
					movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(4), 1500);
					
					break;
				
				case 4:
					// Set camera.
					for (final L2PcInstance pc : vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(cameraMarker.get(4)) <= distance)
						{
							cameraMarker.get(4).broadcastPacket(new SpecialCamera(cameraMarker.get(4).getObjectId(), 1876, 97, 12, 0, 15000));
						}
					}
					
					// Set next task.
					if (movieTask != null)
					{
						movieTask.cancel(false);
					}
					movieTask = null;
					movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(5), 1);
					
					break;
				
				case 5:
					// Set camera.
					for (final L2PcInstance pc : vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(cameraMarker.get(4)) <= distance)
						{
							cameraMarker.get(4).broadcastPacket(new SpecialCamera(cameraMarker.get(4).getObjectId(), 1839, 94, 0, 1500, 15000));
						}
					}
					
					// Set next task.
					if (movieTask != null)
					{
						movieTask.cancel(false);
					}
					movieTask = null;
					movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(6), 1500);
					
					break;
				
				case 6:
					// Set camera.
					for (final L2PcInstance pc : vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(cameraMarker.get(3)) <= distance)
						{
							cameraMarker.get(3).broadcastPacket(new SpecialCamera(cameraMarker.get(3).getObjectId(), 1872, 94, 15, 0, 15000));
						}
					}
					
					// Set next task.
					if (movieTask != null)
					{
						movieTask.cancel(false);
					}
					movieTask = null;
					movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(7), 1);
					
					break;
				
				case 7:
					// Set camera.
					for (final L2PcInstance pc : vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(cameraMarker.get(3)) <= distance)
						{
							cameraMarker.get(3).broadcastPacket(new SpecialCamera(cameraMarker.get(3).getObjectId(), 1839, 92, 0, 1500, 15000));
						}
					}
					
					// Set next task.
					if (movieTask != null)
					{
						movieTask.cancel(false);
					}
					movieTask = null;
					movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(8), 1500);
					
					break;
				
				case 8:
					// Set camera.
					for (final L2PcInstance pc : vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(cameraMarker.get(2)) <= distance)
						{
							cameraMarker.get(2).broadcastPacket(new SpecialCamera(cameraMarker.get(2).getObjectId(), 1872, 92, 15, 0, 15000));
						}
					}
					
					// Set next task.
					if (movieTask != null)
					{
						movieTask.cancel(false);
					}
					movieTask = null;
					movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(9), 1);
					
					break;
				
				case 9:
					// Set camera.
					for (final L2PcInstance pc : vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(cameraMarker.get(2)) <= distance)
						{
							cameraMarker.get(2).broadcastPacket(new SpecialCamera(cameraMarker.get(2).getObjectId(), 1839, 90, 5, 1500, 15000));
						}
					}
					
					// Set next task.
					if (movieTask != null)
					{
						movieTask.cancel(false);
					}
					movieTask = null;
					movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(10), 1500);
					
					break;
				
				case 10:
					// Set camera.
					for (final L2PcInstance pc : vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(cameraMarker.get(1)) <= distance)
						{
							cameraMarker.get(1).broadcastPacket(new SpecialCamera(cameraMarker.get(1).getObjectId(), 1872, 90, 5, 0, 15000));
						}
					}
					
					// Set next task.
					if (movieTask != null)
					{
						movieTask.cancel(false);
					}
					movieTask = null;
					movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(11), 1);
					
					break;
				
				case 11:
					// Set camera.
					for (final L2PcInstance pc : vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(cameraMarker.get(1)) <= distance)
						{
							cameraMarker.get(1).broadcastPacket(new SpecialCamera(cameraMarker.get(1).getObjectId(), 2002, 90, 2, 1500, 15000));
						}
					}
					
					// Set next task.
					if (movieTask != null)
					{
						movieTask.cancel(false);
					}
					movieTask = null;
					movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(12), 2000);
					
					break;
				
				case 12:
					// Set camera.
					for (final L2PcInstance pc : vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(vanHalter) <= distance)
						{
							vanHalter.broadcastPacket(new SpecialCamera(vanHalter.getObjectId(), 50, 90, 10, 0, 15000));
						}
					}
					
					// Set next task.
					if (movieTask != null)
					{
						movieTask.cancel(false);
					}
					movieTask = null;
					movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(13), 1000);
					
					break;
				
				case 13:
					// High Priestess van Halter uses the skill to kill Ritual Offering.
					final L2Skill skill = SkillTable.getInstance().getInfo(1168, 7);
					ritualOffering.setIsInvul(false);
					vanHalter.setTarget(ritualOffering);
					// vanHalter.setIsImmobilized(false);
					vanHalter.doCast(skill);
					// vanHalter.setIsImmobilized(true);
					
					// Set next task.
					if (movieTask != null)
					{
						movieTask.cancel(false);
					}
					movieTask = null;
					movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(14), 4700);
					
					break;
				
				case 14:
					ritualOffering.setIsInvul(false);
					ritualOffering.reduceCurrentHp(ritualOffering.getMaxHp() + 1, vanHalter);
					
					// Set next task.
					if (movieTask != null)
					{
						movieTask.cancel(false);
					}
					movieTask = null;
					movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(15), 4300);
					
					break;
				
				case 15:
					spawnRitualSacrifice();
					deleteRitualOffering();
					
					// Set camera.
					for (final L2PcInstance pc : vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(vanHalter) <= distance)
						{
							vanHalter.broadcastPacket(new SpecialCamera(vanHalter.getObjectId(), 100, 90, 15, 1500, 15000));
						}
					}
					
					// Set next task.
					if (movieTask != null)
					{
						movieTask.cancel(false);
					}
					movieTask = null;
					movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(16), 2000);
					
					break;
				
				case 16:
					// Set camera.
					for (final L2PcInstance pc : vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(vanHalter) <= distance)
						{
							vanHalter.broadcastPacket(new SpecialCamera(vanHalter.getObjectId(), 5200, 90, -10, 9500, 6000));
						}
					}
					
					// Set next task.
					if (movieTask != null)
					{
						movieTask.cancel(false);
					}
					movieTask = null;
					movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(17), 6000);
					
					break;
				
				case 17:
					deleteRitualSacrifice();
					deleteCameraMarker();
					// vanHalter.setIsImmobilized(false);
					vanHalter.setIsInvul(false);
					
					if (movieTask != null)
					{
						movieTask.cancel(false);
					}
					movieTask = null;
					movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(18), 1000);
					
					break;
				
				case 18:
					combatBeginning();
					if (movieTask != null)
					{
						movieTask.cancel(false);
					}
					movieTask = null;
			}
		}
	}
	
	@Override
	public void run()
	{
	}
}
