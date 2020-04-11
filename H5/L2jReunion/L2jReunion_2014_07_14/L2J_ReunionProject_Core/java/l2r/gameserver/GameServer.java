/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.logging.LogManager;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.Server;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.datatables.EventDroplist;
import l2r.gameserver.datatables.SpawnTable;
import l2r.gameserver.datatables.sql.BotReportTable;
import l2r.gameserver.datatables.sql.CharNameTable;
import l2r.gameserver.datatables.sql.CharSummonTable;
import l2r.gameserver.datatables.sql.ClanTable;
import l2r.gameserver.datatables.sql.CrestTable;
import l2r.gameserver.datatables.sql.NpcBufferTable;
import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.datatables.sql.OfflineTradersTable;
import l2r.gameserver.datatables.sql.SummonSkillsTable;
import l2r.gameserver.datatables.sql.TeleportLocationTable;
import l2r.gameserver.datatables.xml.AdminData;
import l2r.gameserver.datatables.xml.ArmorSetsData;
import l2r.gameserver.datatables.xml.AugmentationData;
import l2r.gameserver.datatables.xml.BuyListData;
import l2r.gameserver.datatables.xml.CategoryData;
import l2r.gameserver.datatables.xml.CharTemplateData;
import l2r.gameserver.datatables.xml.ClassListData;
import l2r.gameserver.datatables.xml.DoorData;
import l2r.gameserver.datatables.xml.EnchantItemData;
import l2r.gameserver.datatables.xml.EnchantItemGroupsData;
import l2r.gameserver.datatables.xml.EnchantItemHPBonusData;
import l2r.gameserver.datatables.xml.EnchantItemOptionsData;
import l2r.gameserver.datatables.xml.EnchantSkillGroupsData;
import l2r.gameserver.datatables.xml.ExperienceData;
import l2r.gameserver.datatables.xml.FishData;
import l2r.gameserver.datatables.xml.FishingMonstersData;
import l2r.gameserver.datatables.xml.FishingRodsData;
import l2r.gameserver.datatables.xml.HennaData;
import l2r.gameserver.datatables.xml.HerbDropData;
import l2r.gameserver.datatables.xml.HitConditionBonusData;
import l2r.gameserver.datatables.xml.InitialEquipmentData;
import l2r.gameserver.datatables.xml.ItemData;
import l2r.gameserver.datatables.xml.KarmaData;
import l2r.gameserver.datatables.xml.ManorData;
import l2r.gameserver.datatables.xml.MerchantPriceConfigData;
import l2r.gameserver.datatables.xml.MultiSell;
import l2r.gameserver.datatables.xml.OptionsData;
import l2r.gameserver.datatables.xml.PetData;
import l2r.gameserver.datatables.xml.ProductItemData;
import l2r.gameserver.datatables.xml.RecipeData;
import l2r.gameserver.datatables.xml.SecondaryAuthData;
import l2r.gameserver.datatables.xml.SiegeScheduleData;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.datatables.xml.SkillLearnData;
import l2r.gameserver.datatables.xml.SkillTreesData;
import l2r.gameserver.datatables.xml.StaticObjectsData;
import l2r.gameserver.datatables.xml.TransformData;
import l2r.gameserver.datatables.xml.UIData;
import l2r.gameserver.geoeditorcon.GeoEditorListener;
import l2r.gameserver.handler.EffectHandler;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.instancemanager.AirShipManager;
import l2r.gameserver.instancemanager.AntiFeedManager;
import l2r.gameserver.instancemanager.AuctionManager;
import l2r.gameserver.instancemanager.BoatManager;
import l2r.gameserver.instancemanager.CHSiegeManager;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.instancemanager.CastleManorManager;
import l2r.gameserver.instancemanager.ClanHallManager;
import l2r.gameserver.instancemanager.CoupleManager;
import l2r.gameserver.instancemanager.CursedWeaponsManager;
import l2r.gameserver.instancemanager.DayNightSpawnManager;
import l2r.gameserver.instancemanager.DimensionalRiftManager;
import l2r.gameserver.instancemanager.FortManager;
import l2r.gameserver.instancemanager.FortSiegeManager;
import l2r.gameserver.instancemanager.FourSepulchersManager;
import l2r.gameserver.instancemanager.GlobalVariablesManager;
import l2r.gameserver.instancemanager.GraciaSeedsManager;
import l2r.gameserver.instancemanager.GrandBossManager;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.instancemanager.ItemAuctionManager;
import l2r.gameserver.instancemanager.ItemsOnGroundManager;
import l2r.gameserver.instancemanager.MailManager;
import l2r.gameserver.instancemanager.MapRegionManager;
import l2r.gameserver.instancemanager.MercTicketManager;
import l2r.gameserver.instancemanager.PcCafePointsManager;
import l2r.gameserver.instancemanager.PunishmentManager;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.instancemanager.RaidBossPointsManager;
import l2r.gameserver.instancemanager.RaidBossSpawnManager;
import l2r.gameserver.instancemanager.SiegeManager;
import l2r.gameserver.instancemanager.TerritoryWarManager;
import l2r.gameserver.instancemanager.WalkingManager;
import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.instancemanager.petition.PetitionManager;
import l2r.gameserver.model.AutoSpawnHandler;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.PartyMatchRoomList;
import l2r.gameserver.model.PartyMatchWaitingList;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.olympiad.Olympiad;
import l2r.gameserver.network.L2GameClient;
import l2r.gameserver.network.L2GamePacketHandler;
import l2r.gameserver.pathfinding.PathFinding;
import l2r.gameserver.script.faenor.FaenorScriptEngine;
import l2r.gameserver.scripting.L2ScriptEngineManager;
import l2r.gameserver.taskmanager.AutoAnnounceTaskManager;
import l2r.gameserver.taskmanager.KnownListUpdateTaskManager;
import l2r.gameserver.taskmanager.TaskManager;
import l2r.status.Status;
import l2r.util.DeadLockDetector;
import l2r.util.IPv4Filter;

import org.mmocore.network.SelectorConfig;
import org.mmocore.network.SelectorThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.reunion.configsEngine.CustomConfigController;
import gr.reunion.interf.ReunionEvents;
import gr.reunion.main.CustomServerMods;
import gr.reunion.main.PlayerValues;
import gr.reunion.main.ReunionInfo;
import gr.reunion.protection.Protection;

public class GameServer
{
	private static final Logger _log = LoggerFactory.getLogger(GameServer.class);
	
	private final SelectorThread<L2GameClient> _selectorThread;
	private final L2GamePacketHandler _gamePacketHandler;
	private final DeadLockDetector _deadDetectThread;
	private final IdFactory _idFactory;
	public static GameServer gameServer;
	private final LoginServerThread _loginThread;
	private static Status _statusServer;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
	
	public SelectorThread<L2GameClient> getSelectorThread()
	{
		return _selectorThread;
	}
	
	public L2GamePacketHandler getL2GamePacketHandler()
	{
		return _gamePacketHandler;
	}
	
	public DeadLockDetector getDeadLockDetectorThread()
	{
		return _deadDetectThread;
	}
	
	public GameServer() throws Exception
	{
		long serverLoadStart = System.currentTimeMillis();
		
		gameServer = this;
		
		_idFactory = IdFactory.getInstance();
		
		if (!_idFactory.isInitialized())
		{
			_log.error(getClass().getSimpleName() + ": Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}
		
		ThreadPoolManager.getInstance();
		
		new File("log/game").mkdirs();
		
		// load script engines
		printSection("Engines");
		L2ScriptEngineManager.getInstance();
		
		printSection("Geodata");
		GeoData.getInstance();
		if (Config.GEODATA == 2)
		{
			PathFinding.getInstance();
		}
		
		printSection("World");
		// start game time control early
		GameTimeController.init();
		InstanceManager.getInstance();
		L2World.getInstance();
		MapRegionManager.getInstance();
		Announcements.getInstance();
		GlobalVariablesManager.getInstance();
		
		printSection("Data");
		CategoryData.getInstance();
		SecondaryAuthData.getInstance();
		
		printSection("Skills");
		EffectHandler.getInstance().executeScript();
		EnchantSkillGroupsData.getInstance();
		SkillTreesData.getInstance();
		SkillData.getInstance();
		SummonSkillsTable.getInstance();
		
		printSection("Items");
		ItemData.getInstance();
		ProductItemData.getInstance();
		EnchantItemGroupsData.getInstance();
		EnchantItemData.getInstance();
		EnchantItemOptionsData.getInstance();
		OptionsData.getInstance();
		EnchantItemHPBonusData.getInstance();
		MerchantPriceConfigData.getInstance().loadInstances();
		BuyListData.getInstance();
		MultiSell.getInstance();
		RecipeData.getInstance();
		ArmorSetsData.getInstance();
		FishData.getInstance();
		FishingMonstersData.getInstance();
		FishingRodsData.getInstance();
		HennaData.getInstance();
		
		printSection("Characters");
		ClassListData.getInstance();
		InitialEquipmentData.getInstance();
		ExperienceData.getInstance();
		KarmaData.getInstance();
		HitConditionBonusData.getInstance();
		CharTemplateData.getInstance();
		CharNameTable.getInstance();
		AdminData.getInstance();
		RaidBossPointsManager.getInstance();
		PetData.getInstance();
		CharSummonTable.getInstance().init();
		
		printSection("Clans");
		ClanTable.getInstance();
		CHSiegeManager.getInstance();
		ClanHallManager.getInstance();
		AuctionManager.getInstance();
		
		printSection("NPCs");
		HerbDropData.getInstance();
		SkillLearnData.getInstance();
		NpcTable.getInstance();
		WalkingManager.getInstance();
		StaticObjectsData.getInstance();
		ZoneManager.getInstance();
		DoorData.getInstance();
		ItemAuctionManager.getInstance();
		CastleManager.getInstance().loadInstances();
		FortManager.getInstance().loadInstances();
		NpcBufferTable.getInstance();
		SpawnTable.getInstance();
		RaidBossSpawnManager.getInstance();
		DayNightSpawnManager.getInstance().trim().notifyChangeMode();
		GrandBossManager.getInstance().initZones();
		FourSepulchersManager.getInstance().init();
		DimensionalRiftManager.getInstance();
		EventDroplist.getInstance();
		
		printSection("Siege");
		SiegeScheduleData.getInstance();
		SiegeManager.getInstance().getSieges();
		FortSiegeManager.getInstance();
		TerritoryWarManager.getInstance();
		CastleManorManager.getInstance();
		MercTicketManager.getInstance();
		PcCafePointsManager.getInstance();
		ManorData.getInstance();
		
		printSection("Olympiad");
		if (Config.ENABLE_OLYMPIAD)
		{
			Olympiad.getInstance();
			Hero.getInstance();
		}
		else
		{
			_log.info("Olympiad is disable by config.");
		}
		
		printSection("Seven Signs");
		SevenSigns.getInstance();
		
		// Call to load caches
		printSection("Cache");
		HtmCache.getInstance();
		CrestTable.getInstance();
		TeleportLocationTable.getInstance();
		PlayerValues.checkPlayers();
		UIData.getInstance();
		PartyMatchWaitingList.getInstance();
		PartyMatchRoomList.getInstance();
		PetitionManager.getInstance();
		AugmentationData.getInstance();
		CursedWeaponsManager.getInstance();
		TransformData.getInstance();
		BotReportTable.getInstance();
		
		printSection("Reunion Events");
		ReunionEvents.start();
		
		printSection("Scripts");
		QuestManager.getInstance();
		BoatManager.getInstance();
		AirShipManager.getInstance();
		GraciaSeedsManager.getInstance();
		
		CastleManager.getInstance().activateInstances();
		FortManager.getInstance().activateInstances();
		MerchantPriceConfigData.getInstance().updateReferences();
		
		_log.info("Loading Python Scripts");
		L2ScriptEngineManager.getInstance().executeScriptList();
		
		QuestManager.getInstance().report();
		
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance();
		}
		
		if ((Config.AUTODESTROY_ITEM_AFTER > 0) || (Config.HERB_AUTO_DESTROY_TIME > 0))
		{
			ItemsAutoDestroy.getInstance();
		}
		
		MonsterRace.getInstance();
		
		SevenSigns.getInstance().spawnSevenSignsNPC();
		SevenSignsFestival.getInstance();
		AutoSpawnHandler.getInstance();
		
		FaenorScriptEngine.getInstance();
		// Init of a cursed weapon manager
		
		_log.info("AutoSpawnHandler: Loaded " + AutoSpawnHandler.getInstance().size() + " handlers in total.");
		
		if (Config.L2JMOD_ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
		}
		
		TaskManager.getInstance();
		
		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.GAME_ID);
		
		if (Config.ALLOW_MAIL)
		{
			MailManager.getInstance();
		}
		
		if (Config.ACCEPT_GEOEDITOR_CONN)
		{
			GeoEditorListener.getInstance();
		}
		
		PunishmentManager.getInstance();
		
		// Custom settings section
		printSection("Custom");
		CustomServerMods.getInstance().checkCustomMods();
		// System.out.println("Loading static images....");
		// CustomServerMods.getInstance().loadStaticImages();
		
		// Antibot systems
		printSection("Antibot Engine");
		CustomServerMods.getInstance().checkAntibotMod();
		
		// Leader board load data
		printSection("Leaderboards");
		CustomServerMods.getInstance().checkLeaderboardsMod();
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		
		printSection("Protection System");
		Protection.Init();
		if (Protection.isProtectionOn())
		{
			_log.info("[Protection]: System is loading.");
		}
		else
		{
			_log.info("[Protection]: System is disabled.");
		}
		
		KnownListUpdateTaskManager.getInstance();
		
		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
		{
			OfflineTradersTable.getInstance().restoreOfflineTraders();
		}
		
		if (Config.DEADLOCK_DETECTOR)
		{
			_deadDetectThread = new DeadLockDetector();
			_deadDetectThread.setDaemon(true);
			_deadDetectThread.start();
		}
		else
		{
			_deadDetectThread = null;
		}
		System.gc();
		// maxMemory is the upper limit the jvm can use, totalMemory the size of
		// the current allocation pool, freeMemory the unused memory in the allocation pool
		long freeMem = ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) + Runtime.getRuntime().freeMemory()) / 1048576;
		long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		_log.info(getClass().getSimpleName() + ": Started, free memory " + freeMem + " Mb of " + totalMem + " Mb");
		Toolkit.getDefaultToolkit().beep();
		
		_loginThread = LoginServerThread.getInstance();
		_loginThread.start();
		
		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		sc.TCP_NODELAY = Config.MMO_TCP_NODELAY;
		
		_gamePacketHandler = new L2GamePacketHandler();
		_selectorThread = new SelectorThread<>(sc, _gamePacketHandler, _gamePacketHandler, _gamePacketHandler, new IPv4Filter());
		
		InetAddress bindAddress = null;
		if (!Config.GAMESERVER_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			}
			catch (UnknownHostException e1)
			{
				_log.error(getClass().getSimpleName() + ": WARNING: The GameServer bind address is invalid, using all avaliable IPs. Reason: " + e1.getMessage(), e1);
			}
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
		}
		catch (IOException e)
		{
			_log.error(getClass().getSimpleName() + ": FATAL: Failed to open server socket. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		_selectorThread.start();
		_log.info("Maximum Numbers of Connected players: " + Config.MAXIMUM_ONLINE_USERS);
		long serverLoadEnd = System.currentTimeMillis();
		_log.info("Server loaded in " + ((serverLoadEnd - serverLoadStart) / 1000) + " seconds.");
		
		AutoAnnounceTaskManager.getInstance();
		
		ReunionInfo.load();
	}
	
	public static void main(String[] args) throws Exception
	{
		Server.serverMode = Server.MODE_GAMESERVER;
		// Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME = "./log.cfg"; // Name of log file
		
		/*** Main ***/
		// Create log folder
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		try (InputStream is = new FileInputStream(new File(LOG_NAME)))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		
		// Initialize config
		Config.load();
		// Custom configs load section
		CustomConfigController.getInstance().reloadCustomConfigs();
		// Check binding address
		checkFreePorts();
		_log.info("Custom Configs Loaded...");
		// Initialize database
		Class.forName(Config.DATABASE_DRIVER).newInstance();
		L2DatabaseFactory.getInstance().getConnection().close();
		gameServer = new GameServer();
		
		if (Config.IS_TELNET_ENABLED)
		{
			_statusServer = new Status(Server.serverMode);
			_statusServer.start();
		}
		else
		{
			_log.info(GameServer.class.getSimpleName() + ": Telnet server is currently disabled.");
		}
	}
	
	public static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 61)
		{
			s = "-" + s;
		}
		_log.info(s);
	}
	
	public static void checkFreePorts()
	{
		boolean binded = false;
		while (!binded)
		{
			try
			{
				ServerSocket ss;
				if (Config.GAMESERVER_HOSTNAME.equalsIgnoreCase("*"))
				{
					ss = new ServerSocket(Config.PORT_GAME);
				}
				else
				{
					ss = new ServerSocket(Config.PORT_GAME, 50, InetAddress.getByName(Config.GAMESERVER_HOSTNAME));
				}
				ss.close();
				binded = true;
			}
			catch (Exception e)
			{
				_log.warn("Port " + Config.PORT_GAME + " is allready binded. Please free it and restart server.");
				binded = false;
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e2)
				{
				}
			}
		}
	}
}
