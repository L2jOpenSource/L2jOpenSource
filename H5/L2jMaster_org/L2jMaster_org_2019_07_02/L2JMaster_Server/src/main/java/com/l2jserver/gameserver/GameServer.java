/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.Config;
import com.l2jserver.Server;
import com.l2jserver.UPnPService;
import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.cache.HtmCache;
import com.l2jserver.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jserver.gameserver.data.sql.impl.AnnouncementsTable;
import com.l2jserver.gameserver.data.sql.impl.CharNameTable;
import com.l2jserver.gameserver.data.sql.impl.CharSummonTable;
import com.l2jserver.gameserver.data.sql.impl.ClanTable;
import com.l2jserver.gameserver.data.sql.impl.CrestTable;
import com.l2jserver.gameserver.data.sql.impl.NpcBufferTable;
import com.l2jserver.gameserver.data.sql.impl.OfflineTradersTable;
import com.l2jserver.gameserver.data.sql.impl.SummonSkillsTable;
import com.l2jserver.gameserver.data.sql.impl.TeleportLocationTable;
import com.l2jserver.gameserver.data.xml.impl.AdminData;
import com.l2jserver.gameserver.data.xml.impl.ArmorSetsData;
import com.l2jserver.gameserver.data.xml.impl.BuyListData;
import com.l2jserver.gameserver.data.xml.impl.CategoryData;
import com.l2jserver.gameserver.data.xml.impl.ClassListData;
import com.l2jserver.gameserver.data.xml.impl.DoorData;
import com.l2jserver.gameserver.data.xml.impl.EnchantItemData;
import com.l2jserver.gameserver.data.xml.impl.EnchantItemGroupsData;
import com.l2jserver.gameserver.data.xml.impl.EnchantItemHPBonusData;
import com.l2jserver.gameserver.data.xml.impl.EnchantItemOptionsData;
import com.l2jserver.gameserver.data.xml.impl.EnchantSkillGroupsData;
import com.l2jserver.gameserver.data.xml.impl.ExperienceData;
import com.l2jserver.gameserver.data.xml.impl.FishData;
import com.l2jserver.gameserver.data.xml.impl.FishingMonstersData;
import com.l2jserver.gameserver.data.xml.impl.FishingRodsData;
import com.l2jserver.gameserver.data.xml.impl.HennaData;
import com.l2jserver.gameserver.data.xml.impl.HitConditionBonusData;
import com.l2jserver.gameserver.data.xml.impl.InitialEquipmentData;
import com.l2jserver.gameserver.data.xml.impl.InitialShortcutData;
import com.l2jserver.gameserver.data.xml.impl.KarmaData;
import com.l2jserver.gameserver.data.xml.impl.MultisellData;
import com.l2jserver.gameserver.data.xml.impl.NpcData;
import com.l2jserver.gameserver.data.xml.impl.OptionData;
import com.l2jserver.gameserver.data.xml.impl.PetDataTable;
import com.l2jserver.gameserver.data.xml.impl.PlayerTemplateData;
import com.l2jserver.gameserver.data.xml.impl.PlayerXpPercentLostData;
import com.l2jserver.gameserver.data.xml.impl.PrimeShopData;
import com.l2jserver.gameserver.data.xml.impl.RecipeData;
import com.l2jserver.gameserver.data.xml.impl.SecondaryAuthData;
import com.l2jserver.gameserver.data.xml.impl.SiegeScheduleData;
import com.l2jserver.gameserver.data.xml.impl.SkillLearnData;
import com.l2jserver.gameserver.data.xml.impl.SkillTreesData;
import com.l2jserver.gameserver.data.xml.impl.StaticObjectData;
import com.l2jserver.gameserver.data.xml.impl.TransformData;
import com.l2jserver.gameserver.data.xml.impl.UIData;
import com.l2jserver.gameserver.datatables.AntiBotTable;
import com.l2jserver.gameserver.datatables.AugmentationData;
import com.l2jserver.gameserver.datatables.BotReportTable;
import com.l2jserver.gameserver.datatables.EventDroplist;
import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.datatables.MerchantPriceConfigTable;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.datatables.SpawnTable;
import com.l2jserver.gameserver.datatables.customs.FakePcsTable;
import com.l2jserver.gameserver.handler.EffectHandler;
import com.l2jserver.gameserver.idfactory.IdFactory;
import com.l2jserver.gameserver.instancemanager.AirShipManager;
import com.l2jserver.gameserver.instancemanager.AntiFeedManager;
import com.l2jserver.gameserver.instancemanager.AuctionManager;
import com.l2jserver.gameserver.instancemanager.BoatManager;
import com.l2jserver.gameserver.instancemanager.CHSiegeManager;
import com.l2jserver.gameserver.instancemanager.CastleManager;
import com.l2jserver.gameserver.instancemanager.CastleManorManager;
import com.l2jserver.gameserver.instancemanager.ClanHallManager;
import com.l2jserver.gameserver.instancemanager.CoupleManager;
import com.l2jserver.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jserver.gameserver.instancemanager.DayNightSpawnManager;
import com.l2jserver.gameserver.instancemanager.DimensionalRiftManager;
import com.l2jserver.gameserver.instancemanager.FishingChampionshipManager;
import com.l2jserver.gameserver.instancemanager.FortManager;
import com.l2jserver.gameserver.instancemanager.FortSiegeManager;
import com.l2jserver.gameserver.instancemanager.FourSepulchersManager;
import com.l2jserver.gameserver.instancemanager.GlobalVariablesManager;
import com.l2jserver.gameserver.instancemanager.GraciaSeedsManager;
import com.l2jserver.gameserver.instancemanager.GrandBossManager;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.instancemanager.ItemAuctionManager;
import com.l2jserver.gameserver.instancemanager.ItemsOnGroundManager;
import com.l2jserver.gameserver.instancemanager.KrateisCubeManager;
import com.l2jserver.gameserver.instancemanager.MailManager;
import com.l2jserver.gameserver.instancemanager.MapRegionManager;
import com.l2jserver.gameserver.instancemanager.MercTicketManager;
import com.l2jserver.gameserver.instancemanager.PcCafePointsManager;
import com.l2jserver.gameserver.instancemanager.PetitionManager;
import com.l2jserver.gameserver.instancemanager.PunishmentManager;
import com.l2jserver.gameserver.instancemanager.QuestManager;
import com.l2jserver.gameserver.instancemanager.RaidBossPointsManager;
import com.l2jserver.gameserver.instancemanager.RaidBossSpawnManager;
import com.l2jserver.gameserver.instancemanager.SiegeManager;
import com.l2jserver.gameserver.instancemanager.TerritoryWarManager;
import com.l2jserver.gameserver.instancemanager.WalkingManager;
import com.l2jserver.gameserver.instancemanager.ZoneManager;
import com.l2jserver.gameserver.instancemanager.Leaderboards.ArenaLeaderboard;
import com.l2jserver.gameserver.instancemanager.Leaderboards.CraftLeaderboard;
import com.l2jserver.gameserver.instancemanager.Leaderboards.FishermanLeaderboard;
import com.l2jserver.gameserver.instancemanager.Leaderboards.TvTLeaderboard;
import com.l2jserver.gameserver.instancemanager.achievements.AchievementsManager;
import com.l2jserver.gameserver.model.AutoSpawnHandler;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.PartyMatchRoomList;
import com.l2jserver.gameserver.model.PartyMatchWaitingList;
import com.l2jserver.gameserver.model.entity.Hero;
import com.l2jserver.gameserver.model.entity.TvTManager;
import com.l2jserver.gameserver.model.events.EventDispatcher;
import com.l2jserver.gameserver.model.olympiad.Olympiad;
import com.l2jserver.gameserver.network.L2GameClient;
import com.l2jserver.gameserver.network.L2GamePacketHandler;
import com.l2jserver.gameserver.pathfinding.PathFinding;
import com.l2jserver.gameserver.script.faenor.FaenorScriptEngine;
import com.l2jserver.gameserver.scripting.L2ScriptEngineManager;
import com.l2jserver.gameserver.taskmanager.KnownListUpdateTaskManager;
import com.l2jserver.gameserver.taskmanager.TaskManager;
import com.l2jserver.mmocore.SelectorConfig;
import com.l2jserver.mmocore.SelectorThread;
import com.l2jserver.status.Status;
import com.l2jserver.util.DeadLockDetector;
import com.l2jserver.util.IPv4Filter;

import WDConfig.ConfigWD;
import ZeuS.ZeuS;
import main.EngineModsManager;

public final class GameServer
{
	private static final Logger _log = LoggerFactory.getLogger(GameServer.class);
	private static final String LOG_FOLDER = "log"; // Name of folder for log file
	private static final String LOG_NAME = "./log.cfg"; // Name of log file
	
	private final SelectorThread<L2GameClient> _selectorThread;
	private final L2GamePacketHandler _gamePacketHandler;
	private final DeadLockDetector _deadDetectThread;
	public static GameServer gameServer;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
	
	public GameServer() throws Exception
	{
		long serverLoadStart = System.currentTimeMillis();
		
		_log.info("{}: Used memory: {}MB.", getClass().getSimpleName(), getUsedMemoryMB());
		
		if (!IdFactory.getInstance().isInitialized())
		{
			_log.error("{}: Could not read object IDs from database. Please check your configuration.", getClass().getSimpleName());
			throw new Exception("Could not initialize the ID factory!");
		}
		
		ThreadPoolManager.getInstance();
		EventDispatcher.getInstance();
		
		new File("log/game").mkdirs();
		
		// load script engines
		printSection("Engines");
		L2ScriptEngineManager.getInstance();
		
		printSection("World");
		// start game time control early
		GameTimeController.init();
		InstanceManager.getInstance();
		L2World.getInstance();
		MapRegionManager.getInstance();
		AnnouncementsTable.getInstance();
		GlobalVariablesManager.getInstance();
		
		printSection("Data");
		CategoryData.getInstance();
		SecondaryAuthData.getInstance();
		
		printSection("Effects");
		EffectHandler.getInstance().executeScript();
		printSection("Enchant Skill Groups");
		EnchantSkillGroupsData.getInstance();
		printSection("Skill Trees");
		SkillTreesData.getInstance();
		printSection("Skills");
		SkillData.getInstance();
		SummonSkillsTable.getInstance();
		
		printSection("Items");
		ItemTable.getInstance();
		EnchantItemGroupsData.getInstance();
		EnchantItemData.getInstance();
		EnchantItemOptionsData.getInstance();
		OptionData.getInstance();
		EnchantItemHPBonusData.getInstance();
		MerchantPriceConfigTable.getInstance().loadInstances();
		BuyListData.getInstance();
		MultisellData.getInstance();
		RecipeData.getInstance();
		ArmorSetsData.getInstance();
		FishData.getInstance();
		FishingMonstersData.getInstance();
		FishingRodsData.getInstance();
		HennaData.getInstance();
		if (Config.PRIME_SHOP_POINTS_ENABLED)
		{
			PrimeShopData.getInstance();
		}
		
		printSection("Characters");
		ClassListData.getInstance();
		InitialEquipmentData.getInstance();
		InitialShortcutData.getInstance();
		ExperienceData.getInstance();
		PlayerXpPercentLostData.getInstance();
		KarmaData.getInstance();
		HitConditionBonusData.getInstance();
		PlayerTemplateData.getInstance();
		CharNameTable.getInstance();
		AdminData.getInstance();
		RaidBossPointsManager.getInstance();
		PetDataTable.getInstance();
		CharSummonTable.getInstance().init();
		
		printSection("Clans");
		ClanTable.getInstance();
		CHSiegeManager.getInstance();
		ClanHallManager.getInstance();
		AuctionManager.getInstance();
		
		printSection("Geodata");
		GeoData.getInstance();
		
		if (Config.PATHFINDING > 0)
		{
			PathFinding.getInstance();
		}
		
		printSection("NPCs");
		SkillLearnData.getInstance();
		NpcData.getInstance();
		WalkingManager.getInstance();
		StaticObjectData.getInstance();
		ZoneManager.getInstance();
		DoorData.getInstance();
		CastleManager.getInstance().loadInstances();
		NpcBufferTable.getInstance();
		GrandBossManager.getInstance().initZones();
		EventDroplist.getInstance();
		printSection("Auction Manager");
		ItemAuctionManager.getInstance();
		
		printSection("Olympiad");
		Olympiad.getInstance();
		Hero.getInstance();
		
		printSection("Seven Signs");
		SevenSigns.getInstance();
		
		// Call to load caches
		printSection("Cache");
		HtmCache.getInstance();
		CrestTable.getInstance();
		TeleportLocationTable.getInstance();
		UIData.getInstance();
		PartyMatchWaitingList.getInstance();
		PartyMatchRoomList.getInstance();
		PetitionManager.getInstance();
		AugmentationData.getInstance();
		CursedWeaponsManager.getInstance();
		TransformData.getInstance();
		BotReportTable.getInstance();
		
		printSection("Scripts");
		QuestManager.getInstance();
		BoatManager.getInstance();
		AirShipManager.getInstance();
		GraciaSeedsManager.getInstance();
		
		try
		{
			_log.info("{}: Loading server scripts:", getClass().getSimpleName());
			if (!Config.ALT_DEV_NO_HANDLERS || !Config.ALT_DEV_NO_QUESTS)
			{
				L2ScriptEngineManager.getInstance().executeScriptList(new File(Config.DATAPACK_ROOT, "data/scripts.cfg"));
			}
		}
		catch (IOException ioe)
		{
			_log.error("{}: Failed loading scripts.cfg, scripts are not going to be loaded!", getClass().getSimpleName());
		}
		
		printSection("MasterMods");
		FakePcsTable.getInstance();
		_log.info("Fake Pcs: Loaded.");
		
		if (Config.COMBINE_TALISMAN_ENABLED)
		{
			_log.info("Combine Talismans Command: Enabled.");
		}
		
		if (Config.TELEPORT_TO_MOB_COMMAND)
		{
			_log.info("To Mob Command: Enabled.");
		}
		
		if (Config.TELEPORT_TO_TOWN_COMMNAND)
		{
			_log.info("To Town Command: Enaabled.");
		}
		
		if (Config.ALLOW_REPAIR_COMMAND)
		{
			_log.info("Repair Command: Enabled.");
		}
		if (Config.COMMAND_SELL_BUFF)
		{
			_log.info("Sell Buff Command: Enabled.");
		}
		
		if (Config.ALLOW_EPIC_COMMAND)
		{
			_log.info("Epic Command: Enabled.");
		}
		
		if (Config.ALLOW_CL_COMMAND)
		{
			_log.info("Go To Clan Leader Command: Enabled.");
		}
		
		PcCafePointsManager.getInstance();
		if (!Config.PC_BANG_ENABLED)
		{
			_log.info("PC Cafe Points: Disabled.");
		}
		
		if (Config.GAME_SERVER_AUTO_RESTART)
		{
			GameServerRestart.getInstance().StartCalculationOfNextRestartTime();
		}
		
		if (Config.ANNOUNCE_HERO_ON_ENTER)
		{
			_log.info("Announce Hero: Enabled.");
		}
		
		if (Config.ANNOUNCE_CASTLE_LORDS_ON_ENTER)
		{
			_log.info("Announce Castle Lord: Enabled.");
		}
		
		if (Config.RESTORE_CANCELED_BUFFS_ENABLED)
		{
			_log.info("Cancel Return: Enabled.");
		}
		
		if (Config.ANNOUNCE_RAIDBOSS_ALIVE_KILL)
		{
			_log.info("Announce Raid Boss: Enabled.");
		}
		
		if (Config.ANNOUNCE_GRANDBOSS_ALIVE_KILL)
		{
			_log.info("Announce Grand Boss Kill: Enabled.");
		}
		
		if (Config.ANNOUNCE_BOSS_SPAWN)
		{
			_log.info("Announce Raid Boss Spawn: Enabled.");
		}
		
		if (Config.CLAN_LEADER_COLOR_ENABLED)
		{
			_log.info("Clan Leader Color: Enabled.");
		}
		
		if (Config.USE_CUSTOM_CLANHALLS)
		{
			_log.info("Custom Clan Halls: Enabled.");
		}
		
		if (Config.PREMIUM_SYSTEM_ENABLED)
		{
			_log.info("Premium System: Enabled.");
		}
		
		if (Config.RANK_ARENA_ENABLED)
		{
			ArenaLeaderboard.getInstance();
		}
		
		if (Config.RANK_FISHERMAN_ENABLED)
		{
			FishermanLeaderboard.getInstance();
		}
		
		if (Config.RANK_CRAFT_ENABLED)
		{
			CraftLeaderboard.getInstance();
		}
		
		if (Config.RANK_TVT_ENABLED)
		{
			TvTLeaderboard.getInstance();
		}
		if (Config.ENABLE_ARCHIEVEMENTS)
		{
			AchievementsManager.getInstance();
		}
		
		printSection("Engine");
		EngineModsManager.init();
		if (Config.ALLOW_COMMUNITY_MULTISELL)
		{
			_log.info("Shop: Enabled.");
		}
		if (Config.ALLOW_COMMUNITY_TELEPORT)
		{
			_log.info("Teleport: Enabled.");
		}
		if (Config.ALLOW_COMMUNITY_SERVICES)
		{
			_log.info("Services: Enabled.");
		}
		RegionBBSManager.getInstance().startBoard();
		
		printSection("Protection System");
		_log.info("AntiBot: Enabled.");
		AntiBotTable.getInstance().loadImage();
		
		printSection("Spawn");
		SpawnTable.getInstance().load();
		DayNightSpawnManager.getInstance().trim().notifyChangeMode();
		FourSepulchersManager.getInstance().init();
		DimensionalRiftManager.getInstance();
		RaidBossSpawnManager.getInstance();
		
		printSection("Siege");
		SiegeManager.getInstance().getSieges();
		CastleManager.getInstance().activateInstances();
		FortManager.getInstance().loadInstances();
		FortManager.getInstance().activateInstances();
		FortSiegeManager.getInstance();
		SiegeScheduleData.getInstance();
		
		MerchantPriceConfigTable.getInstance().updateReferences();
		TerritoryWarManager.getInstance();
		CastleManorManager.getInstance();
		MercTicketManager.getInstance();
		
		QuestManager.getInstance().report();
		
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance();
		}
		
		if ((Config.AUTODESTROY_ITEM_AFTER > 0) || (Config.HERB_AUTO_DESTROY_TIME > 0))
		{
			ItemsAutoDestroy.getInstance();
		}
		
		if (Config.KRATEIS_CUBE_EVENT_ENABLED)
		{
			KrateisCubeManager.getInstance();
		}
		
		MonsterRace.getInstance();
		
		SevenSigns.getInstance().spawnSevenSignsNPC();
		SevenSignsFestival.getInstance();
		AutoSpawnHandler.getInstance();
		
		FaenorScriptEngine.getInstance();
		// Init of a cursed weapon manager
		
		_log.info("AutoSpawnHandler: Loaded {} handlers in total.", AutoSpawnHandler.getInstance().size());
		
		if (Config.L2JMOD_ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
		}
		
		if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
		{
			FishingChampionshipManager.getInstance();
		}
		
		TaskManager.getInstance();
		
		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.GAME_ID);
		
		if (Config.ALLOW_MAIL)
		{
			MailManager.getInstance();
		}
		
		PunishmentManager.getInstance();
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		_log.info("IdFactory: Free ObjectID's remaining: {}", IdFactory.getInstance().size());
		
		TvTManager.getInstance();
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
		
		if (Config.ZEUS_ACTIVE && Config.ALLOW_CUSTOM_CB)
		{
			ZeuS.LoadZeuS();
		}
		
		long freeMem = ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) + Runtime.getRuntime().freeMemory()) / 1048576;
		long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		_log.info("{}: Started, free memory {} Mb of {} Mb", getClass().getSimpleName(), freeMem, totalMem);
		Toolkit.getDefaultToolkit().beep();
		LoginServerThread.getInstance().start();
		
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
				_log.error("{}: The GameServer bind address is invalid, using all avaliable IPs!", getClass().getSimpleName(), e1);
			}
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
			_selectorThread.start();
			_log.info("{}: is now listening on: {}:{}", getClass().getSimpleName(), Config.GAMESERVER_HOSTNAME, Config.PORT_GAME);
		}
		catch (IOException e)
		{
			_log.error("{}: Failed to open server socket!", getClass().getSimpleName(), e);
			System.exit(1);
		}
		
		_log.info("{}: Maximum numbers of connected players: {}", getClass().getSimpleName(), Config.MAXIMUM_ONLINE_USERS);
		_log.info("{}: Server loaded in {} seconds.", getClass().getSimpleName(), TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - serverLoadStart));
		
		printSection("UPnP");
		UPnPService.getInstance();
	}
	
	public static void main(String[] args) throws Exception
	{
		Server.serverMode = Server.MODE_GAMESERVER;
		
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
		ConfigWD.load();
		printSection("Database");
		ConnectionFactory.getInstance();
		
		gameServer = new GameServer();
		
		if (Config.IS_TELNET_ENABLED)
		{
			new Status(Server.serverMode).start();
		}
		else
		{
			_log.info("{}: Telnet server is currently disabled.", GameServer.class.getSimpleName());
		}
	}
	
	public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
	}
	
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
	
	public static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 61)
		{
			s = "-" + s;
		}
		_log.info(s);
	}
}
