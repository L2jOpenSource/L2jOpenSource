package main.data.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.skills.Stats;

import main.enums.WeekDayType;
import main.holders.IntIntHolder;
import main.holders.RewardHolder;
import main.util.UtilProperties;

/**
 * @author fissban
 */
public class ConfigData
{
	protected static final Logger LOG = Logger.getLogger(ConfigData.class.getName());
	
	// Files
	private static final String ANTI_BOOT_FILE = "./config/engine/AntiBot.properties";
	private static final String FAKES_PLAYERS_FILE = "./config/engine/FakePlayers.properties";
	private static final String COMMUNITY_FILE = "./config/engine/Community.properties";
	private static final String OFFLINE_SHOP_FILE = "./config/engine/OfflineShops.properties";
	private static final String VIP_FILE = "./config/engine/Vip.properties";
	private static final String AIO_FILE = "./config/engine/Aio.properties";
	private static final String NEW_CHARACTER_CREATED_FILE = "./config/engine/NewCharacterCreated.properties";
	private static final String VOTE_REWARD_FILE = "./config/engine/VoteReward.properties";
	private static final String ANNOUNCE_KILL_BOOS_FILE = "./config/engine/AnnounceKillBoss.properties";
	private static final String COLOR_AMOUNT_PVP_FILE = "./config/engine/ColorAccordingAmountPvP.properties";
	private static final String ENCHANT_ABNORMAL_EFFECT_FILE = "./config/engine/EnchantAbnormalEffectArmor.properties";
	private static final String PVP_REWARD_FILE = "./config/engine/PvpReward.properties";
	private static final String SPREE_KILLS_FILE = "./config/engine/SpreeKills.properties";
	private static final String SUBCLASS_ACUMULATIVES_FILE = "./config/engine/SubClassAcumulatives.properties";
	private static final String COOPERATIVE_FILE = "./config/engine/EventsCooperative.properties";
	private static final String EVENTS_RANDOMS_FILE = "./config/engine/EventsRandoms.properties";
	private static final String EVENTS_NORMAL_FILE = "./config/engine/EventsNormal.properties";
	
	// FAKE PLAYERS ----------------------------------------------------------------------------------------------- //
	public static boolean ENABLE_fakePlayers;
	public static int FAKE_LEVEL = 78;
	public static int FAKE_MAX_PVP = 500;
	public static int FAKE_MAX_PK = 100;
	public static int FAKE_CHANCE_SIT = 30;
	public static int FAKE_CHANCE_HAS_CLAN = 50;
	public static int SPAWN_RANGE = 200;
	// COMMUNITY -------------------------------------------------------------------------------------------------- //
	public static boolean ENABLE_BBS_REGION;
	public static boolean ENABLE_BBS_CLAN;
	public static boolean ENABLE_BBS_HOME;
	// AUCTION ---------------------------------------------------------------------------------------------------- //
	public static boolean ENABLE_BBS_MEMO;
	// comision en adena al comenzar la venta
	public static int COMMISION_FOR_START_SELL = 10000;
	public static int COMMISION_FOR_START_SELL_ID = 57;
	// comision en porcentaje al finalizar la venta.
	public static int COMMISION_FOR_END_SELL = 10;
	
	// REBIRTH ---------------------------------------------------------------------------------------------------- //
	public static boolean ENABLE_BBS_FAVORITE;
	// cantidad maxima de rebirths
	public static int MAX_REBIRTH = 10;
	public static int PRICE_FOR_REBIRTH;
	public static double MUL_PRICE_PER_REBIRTH;
	// puntos que se ganaran por cada rebirth para repartir en los stats -> CON,STR,DEX,WIT.....
	public static int STAT_POINT_PER_REBIRTH = 4;
	// puntos que se ganaran por cada rebirth para repartir en el arbol de habilidades.
	public static int MASTERY_POINT_PER_REBIRTH = 2;
	// nivel en que quedara el personaje luego de renacer
	public static int LVL_REBIRTH = 1;
	// Offline Shop ---------------------------------------------------------------------------------------------- //
	public static boolean OFFLINE_TRADE_ENABLE;
	public static boolean OFFLINE_MODE_IN_PEACE_ZONE;
	public static boolean OFFLINE_MODE_NO_DAMAGE;
	public static int OFFLINE_MAX_DAYS;
	public static boolean OFFLINE_DISCONNECT_FINISHED;
	// SELLBUFF SHOP ---------------------------------------------------------------------------------------------- //
	public static boolean SELLBUFF_ENABLE;
	public static boolean OFFLINE_SELLBUFF_ENABLE;
	public static boolean OFFLINE_SET_NAME_COLOR;
	public static int OFFLINE_NAME_COLOR;
	// COLOR ACORDING AMOUNT PVP OR PK ---------------------------------------------------------------------------- //
	public static boolean ENABLE_ColorAccordingAmountPvP;
	public static Map<Integer, String> PVP_COLOR_NAME = new LinkedHashMap<>();
	// ENCHANT ABNORMAL EFFECT ------------------------------------------------------------------------------------ //
	public static boolean ENABLE_EnchantAbnormalEffectArmor;
	public static int ENCHANT_EFFECT_LVL;
	// PVP REWARD ------------------------------------------------------------------------------------------------- //
	public static boolean ENABLE_PvpReward;
	public static List<RewardHolder> PVP_REWARDS = new ArrayList<>();
	public static int PVP_TIME;
	// SPREE KILLS ------------------------------------------------------------------------------------------------ //
	public static boolean ENABLE_SpreeKills;
	public static Map<Integer, String> ANNOUNCEMENTS_KILLS = new LinkedHashMap<>();
	public static Map<Integer, String> SOUNDS_KILLS = new LinkedHashMap<>();
	// SUBCLASS ACUMULATIVE --------------------------------------------------------------------------------------- //
	public static boolean ENABLE_SubClassAcumulatives;
	public static boolean ACUMULATIVE_PASIVE_SKILLS;
	public static Set<Integer> DONT_ACUMULATIVE_SKILLS_ID = new HashSet<>();
	// ANNOUNCE KILL BOSS ----------------------------------------------------------------------------------------- //
	public static boolean ENABLE_AnnounceKillBoss;
	public static List<WeekDayType> ANNOUNCE_KILL_BOSS_ENABLE_DAY = new ArrayList<>();
	public static String ANNOUNCE_KILL_BOSS;
	public static String ANNOUNCE_KILL_GRANDBOSS;
	
	// VOTE REWARD ------------------------------------------------------------------------------------------------ //
	public static boolean ENABLE_VoteReward;
	public static int TIME_CHECK_VOTES;
	public static Map<Integer, RewardHolder> VOTE_REWARDS = new LinkedHashMap<>();
	public static boolean ENABLE_HOPZONE;
	public static boolean ENABLE_TOPZONE;
	public static boolean ENABLE_NETWORK;
	public static String HOPZONE_URL;
	public static String TOPZONE_URL;
	public static String NETWORK_URL;
	
	public static boolean ENABLE_VoteRewardIndivualHopzone;
	public static boolean ENABLE_VoteRewardIndivualTopzone;
	public static boolean ENABLE_VoteRewardIndivualNetwork;
	public static int INDIVIDUAL_VOTE_TIME_VOTE;
	public static RewardHolder INDIVIDUAL_VOTE_REWARD = new RewardHolder(57, 1000);
	public static int INDIVIDUAL_VOTE_TIME_CAN_VOTE;// hs
	// ANTI BOT --------------------------------------------------------------------------------------------------- //
	public static boolean ENABLE_AntiBot;
	public static int TIME_CHECK_ANTIBOT;// segundos
	public static int KILLER_MONSTERS_ANTIBOT;
	public static boolean KILLER_MONSTERS_ANTIBOT_INCREASE_LEVEL;
	// RANDOM BOSS SPAWN ------------------------------------------------------------------------------------------ //
	public static boolean ENABLE_RandomBossSpawn;
	public static List<WeekDayType> RANDOM_BOSS_SPAWN_ENABLE_DAY = new ArrayList<>();
	public static int RANDOM_BOSS_SPWNNED_TIME;
	public static List<Integer> RANDOM_BOSS_NPC_ID = new ArrayList<>();
	public static List<RewardHolder> RANDOM_BOSS_REWARDS = new ArrayList<>();
	// NEW CHARACTER CREATED -------------------------------------------------------------------------------------- //
	public static String NEW_CHARACTER_CREATED_TITLE;
	public static boolean NEW_CHARACTER_CREATED_GIVE_BUFF;
	public static List<IntIntHolder> NEW_CHARACTER_CREATED_BUFFS_WARRIORS = new ArrayList<>();
	public static List<IntIntHolder> NEW_CHARACTER_CREATED_BUFFS_MAGE = new ArrayList<>();
	public static String NEW_CHARACTER_CREATED_SEND_SCREEN_MSG;
	// AIO -------------------------------------------------------------------------------------------------------- //
	public static Map<Stats, Double> AIO_STATS = new HashMap<>();
	
	public static boolean AIO_CAN_EXIT_PEACE_ZONE;
	public static String AIO_TITLE;
	public static List<IntIntHolder> AIO_LIST_SKILLS = new ArrayList<>();
	public static boolean AIO_SET_MAX_LVL;
	public static int AIO_ITEM_ID;
	// VIP -------------------------------------------------------------------------------------------------------- //
	public static Map<Stats, Double> VIP_STATS = new HashMap<>();
	public static double VIP_BONUS_XP;
	public static double VIP_BONUS_SP;
	
	public static double VIP_BONUS_DROP_NORMAL_AMOUNT;
	public static double VIP_BONUS_DROP_SPOIL_AMOUNT;
	public static double VIP_BONUS_DROP_SEED_AMOUNT;
	
	public static double VIP_BONUS_DROP_NORMAL_CHANCE;
	public static double VIP_BONUS_DROP_SPOIL_CHANCE;
	public static double VIP_BONUS_DROP_SEED_CHANCE;
	
	public static boolean ALLOW_VIP_NCOLOR;
	public static int VIP_NCOLOR;
	// EVENTS ----------------------------------------------------------------------------------------------------- //
	// General
	public static int TIME_PER_EVENT;
	// Time between events
	public static int RANDOM_TIME_BETWEEN_EVENTS;
	
	// Elpys
	public static boolean ELPY_Enabled;
	public static int ELPY;
	public static int ELPY_COUNT;
	public static int ELPY_RANGE_SPAWN;
	public static List<Location> ELPY_LOC = new ArrayList<>();
	public static List<RewardHolder> ELPY_REWARDS = new ArrayList<>();
	// Chest
	public static boolean CHEST_Enabled;
	public static int CHEST;
	public static List<RewardHolder> CHEST_REWARDS = new ArrayList<>();
	// All Flags
	public static boolean ALL_FLAGS_Enabled;
	
	// EVENTS NORMALS ----------------------------------------------------------------------------------------------- //
	
	// CHAMPIONS -------------------------------------------------------------------------------------------------- //
	public static boolean ENABLE_Champions;
	public static List<WeekDayType> CHAMPION_ENABLE_DAY = new ArrayList<>();
	public static int CHANCE_SPAWN_WEAK;
	public static int CHANCE_SPAWN_SUPER;
	public static int CHANCE_SPAWN_HARD;
	public static Map<Stats, Double> CHAMPION_STAT_WEAK = new HashMap<>();
	public static Map<Stats, Double> CHAMPION_STAT_SUPER = new HashMap<>();
	public static Map<Stats, Double> CHAMPION_STAT_HARD = new HashMap<>();
	public static List<RewardHolder> CHAMPION_REWARD_WEAK = new ArrayList<>();
	public static List<RewardHolder> CHAMPION_REWARD_SUPER = new ArrayList<>();
	public static List<RewardHolder> CHAMPION_REWARD_HARD = new ArrayList<>();
	public static double CHAMPION_BONUS_RATE_EXP;
	public static double CHAMPION_BONUS_RATE_SP;
	public static double CHAMPION_BONUS_DROP;
	public static double CHAMPION_BONUS_SPOIL;
	public static double CHAMPION_BONUS_SEED;
	
	// BONUS WEEKEND ---------------------------------------------------------------------------------------------------- //
	public static boolean ENABLE_BonusWeekend;
	public static List<WeekDayType> BONUS_WEEKEND_ENABLE_DAY = new ArrayList<>();
	public static double BONUS_WEEKEND_RATE_EXP;
	public static double BONUS_WEEKEND_RATE_SP;
	public static double BONUS_WEEKEND_DROP;
	public static double BONUS_WEEKEND_SPOIL;
	public static double BONUS_WEEKEND_SEED;
	
	public static boolean ENABLE_ClassMaster;
	public static String CLASSMASTER_DATE_START;
	public static String CLASSMASTER_DATE_END;
	public static String CLASSMASTER_MESSAGE_START;
	
	public static RewardHolder CLASSMASTER_PRICE_JOB1;
	public static RewardHolder CLASSMASTER_REWARD_JOB1;
	
	public static RewardHolder CLASSMASTER_PRICE_JOB2;
	public static RewardHolder CLASSMASTER_REWARD_JOB2;
	
	public static RewardHolder CLASSMASTER_PRICE_JOB3;
	public static RewardHolder CLASSMASTER_REWARD_JOB3;
	
	public static boolean ENABLE_FireCat;
	public static String FIRE_CAT_DATE_START;
	public static String FIRE_CAT_DATE_END;
	public static String FIRE_CAT_MESSAGE_START;
	
	public static boolean ENABLE_HeavyMedals;
	public static String HEAVY_MEDALS_DATE_START;
	public static String HEAVY_MEDALS_DATE_END;
	public static String HEAVY_MEDALS_MESSAGE_START;
	// COOPERATIVES ------------------------------------------------------------------------------------------------ //
	// General
	public static int COOPERATIVE_EVENT_DURATION;
	public static int COOPERATIVE_BETWEEN_EACH_EVENT;
	public static int COOPERATIVE_MIN_PLAYERS = 2;
	public static int COOPERATIVE_MAX_PLAYERS = 20;
	public static List<IntIntHolder> COOPERATIVE_BUFF_WARRIOR = new ArrayList<>();
	public static List<IntIntHolder> COOPERATIVE_BUFF_MAGE = new ArrayList<>();
	public static boolean COOPERATIVE_AFK_CHECK;
	public static int COOPERATIVE_AFK_SECONDS;
	public static boolean COOPERATIVE_CHECK_PLAYER_IP;
	// Team Vs Team
	public static Location TVT_SPAWN_TEAM_BLUE;
	public static Location TVT_SPAWN_TEAM_RED;
	public static List<RewardHolder> TVT_REWARDS = new ArrayList<>();
	public static int TVT_RADIUS_SPAWN;
	// Survive
	public static List<Integer> SURVIVE_MOBS_ID = new ArrayList<>();
	public static int SURVIVE_MOBS_PER_ROUND = 5;
	public static List<RewardHolder> SURVIVE_REWARDS = new ArrayList<>();
	public static int SURVIVE_RANGE_SPAWN;
	// All vs All
	public static List<RewardHolder> AVA_REWARDS = new ArrayList<>();
	public static Location AVA_SPAWN_POINT;
	public static int AVA_RANGE_SPAWN;
	// Capture the flag
	public static int CTF_FLAG_ID;
	public static int CTF_HOLDER_ID;
	
	public static int CTF_POINT_DELIVER_FLAG;
	public static int CTF_POINT_KILL_PLAYER;
	
	public static Location CTF_SPAWN_TEAM_BLUE;
	public static Location CTF_SPAWN_TEAM_RED;
	public static Location CTF_FLAG_SPAWN_TEAM_BLUE;
	public static Location CTF_FLAG_SPAWN_TEAM_RED;
	public static List<RewardHolder> CTF_REWARDS;
	public static int CTF_RADIUS_SPAWN;
	// One vs One
	public static List<RewardHolder> ONEVSONE_DUEL_WINNER_REWARDS;
	public static List<RewardHolder> ONEVSONE_DUEL_LOSER_REWARDS;
	public static Location ONEVSONE_SPAWN_TEAM_BLUE;
	public static Location ONEVSONE_SPAWN_TEAM_RED;
	public static Location ONEVSONE_WAITING_LOCATION;
	public static List<RewardHolder> ONEVSONE_FINAL_DUEL_LOSER_REWARDS;
	public static List<RewardHolder> ONEVSONE_FINAL_DUEL_WINNER_REWARDS;
	
	// XXX load
	public static void load()
	{
		loadAntibot();
		loadCommunity();
		loadOfflineShop();
		loadVip();
		loadAio();
		loadNewCharacter();
		loadVoteReward();
		loadAnnounceKillBoss();
		loadColorAmountPvp();
		loadEnchantAbnormalEffect();
		loadPvpReward();
		loadSpreeKills();
		loadSubclassAcumulatives();
		loadCooperative();
		loadEventsRandoms();
		loadEventsNormal();
	}
	
	private static void loadEventsNormal()
	{
		UtilProperties config = load(EVENTS_NORMAL_FILE);
		
		ENABLE_ClassMaster = config.getProperty("Enable_ClassMaster", true);
		CLASSMASTER_DATE_START = config.getProperty("ClassMasterDateStart", "24-2-2015");
		CLASSMASTER_DATE_END = config.getProperty("ClassMasterDateEnd", "24-2-2020");
		CLASSMASTER_MESSAGE_START = config.getProperty("ClassMasterMessageStart", "");
		
		CLASSMASTER_PRICE_JOB1 = parseReward(config, "ClassMasterPriceJob1").get(0);
		CLASSMASTER_REWARD_JOB1 = parseReward(config, "ClassMasterRewardJob1").get(0);
		
		CLASSMASTER_PRICE_JOB2 = parseReward(config, "ClassMasterPriceJob2").get(0);
		CLASSMASTER_REWARD_JOB2 = parseReward(config, "ClassMasterRewardJob2").get(0);
		
		CLASSMASTER_PRICE_JOB3 = parseReward(config, "ClassMasterPriceJob3").get(0);
		CLASSMASTER_REWARD_JOB3 = parseReward(config, "ClassMasterRewardJob3").get(0);
		
		ENABLE_FireCat = config.getProperty("Enable_FireCat", true);
		FIRE_CAT_DATE_START = config.getProperty("FireCatDateStart", "24-2-2015");
		FIRE_CAT_DATE_END = config.getProperty("FireCatDateEnd", "24-2-2020");
		FIRE_CAT_MESSAGE_START = config.getProperty("FireCatMessageStart", "");
		
		//
		ENABLE_HeavyMedals = config.getProperty("Enable_HeavyMedals", true);
		HEAVY_MEDALS_DATE_START = config.getProperty("HeavyMedalsDateStart", "24-2-2015");
		HEAVY_MEDALS_DATE_END = config.getProperty("HeavyMedalsDateEnd", "24-2-2020");
		HEAVY_MEDALS_MESSAGE_START = config.getProperty("HeavyMedalsMessageStart", "");
		
		ENABLE_BonusWeekend = config.getProperty("Enable_BonusExpSp", false);
		BONUS_WEEKEND_ENABLE_DAY = parseWeekDay(config, "BonusExpSpEnableDay");
		BONUS_WEEKEND_RATE_EXP = config.getProperty("BonusExp", 1.2);
		BONUS_WEEKEND_RATE_SP = config.getProperty("BonusSp", 1.2);
		BONUS_WEEKEND_DROP = config.getProperty("BonusDrop", 1.2);
		BONUS_WEEKEND_SPOIL = config.getProperty("BonusSpoil", 1.2);
		BONUS_WEEKEND_SEED = config.getProperty("BonusSeed", 1.2);
		
		ENABLE_Champions = config.getProperty("Enable_Champions", false);
		CHAMPION_ENABLE_DAY = parseWeekDay(config, "ChampionEnableDay");
		
		CHANCE_SPAWN_WEAK = config.getProperty("ChanceSpawnWeak", 10);
		CHANCE_SPAWN_SUPER = config.getProperty("ChanceSpawnSuper", 10);
		CHANCE_SPAWN_HARD = config.getProperty("ChanceSpawnHard", 10);
		
		CHAMPION_STAT_WEAK = parseStats(config, "_Weak");
		CHAMPION_STAT_SUPER = parseStats(config, "_Super");
		CHAMPION_STAT_HARD = parseStats(config, "_Hard");
		CHAMPION_REWARD_WEAK = parseReward(config, "RewardsToKillWeak");
		CHAMPION_REWARD_SUPER = parseReward(config, "RewardsToKillSuper");
		CHAMPION_REWARD_HARD = parseReward(config, "RewardsToKillHard");
		
		CHAMPION_BONUS_RATE_EXP = config.getProperty("BonusExp", 1.2);
		CHAMPION_BONUS_RATE_SP = config.getProperty("BonusSp", 1.2);
		
		CHAMPION_BONUS_DROP = config.getProperty("BonusDrop", 1.2);
		CHAMPION_BONUS_SPOIL = config.getProperty("BonusSpoil", 1.2);
		CHAMPION_BONUS_SEED = config.getProperty("BonusSeed", 1.2);
		
		ENABLE_RandomBossSpawn = config.getProperty("Enable_RandomBossSpawn", false);
		RANDOM_BOSS_SPAWN_ENABLE_DAY = parseWeekDay(config, "RandomBossSpawnEnableDay");
		String aux = config.getProperty("RandomBossSpawnId", "10447,10450,10444,10490").trim();
		for (String info : aux.split(","))
		{
			RANDOM_BOSS_NPC_ID.add(Integer.parseInt(info));
		}
		RANDOM_BOSS_REWARDS = parseReward(config, "RandomBossRewards");
		RANDOM_BOSS_SPWNNED_TIME = config.getProperty("RandomBossSpawnTime", 10);
	}
	
	private static void loadEventsRandoms()
	{
		UtilProperties config = load(EVENTS_RANDOMS_FILE);
		
		RANDOM_TIME_BETWEEN_EVENTS = config.getProperty("TimeBetweenEvents", 60);
		TIME_PER_EVENT = config.getProperty("TimerEvent", 60);
		// Elpys
		ELPY_Enabled = config.getProperty("Elpy_Enabled", true);
		ELPY = config.getProperty("Elpy_MobId", 20432);
		ELPY_COUNT = config.getProperty("Elpy_MobCount", 50);
		ELPY_RANGE_SPAWN = config.getProperty("Elpy_EventRangeSpawn", 1000);
		ELPY_LOC = parseLocation(config, "Elpy_EventLoc");
		ELPY_REWARDS = parseReward(config, "Elpy_EventRewards");
		// Chests
		CHEST_Enabled = config.getProperty("Chest_Enabled", true);
		CHEST = config.getProperty("Chest_MobId", 60016);
		CHEST_REWARDS = parseReward(config, "Chest_EventRewards");
		// All Flags
		ALL_FLAGS_Enabled = config.getProperty("Flags_Enabled", true);
	}
	
	private static void loadCooperative()
	{
		UtilProperties config = load(COOPERATIVE_FILE);
		
		COOPERATIVE_EVENT_DURATION = config.getProperty("EventDuration", 5);
		COOPERATIVE_BETWEEN_EACH_EVENT = config.getProperty("BetweenEachEvent", 2);
		COOPERATIVE_MIN_PLAYERS = config.getProperty("MinPlayers", 2);
		COOPERATIVE_MAX_PLAYERS = config.getProperty("MaxPlayers", 20);
		COOPERATIVE_BUFF_WARRIOR = parseBuff(config, "BuffWarrior");
		COOPERATIVE_BUFF_MAGE = parseBuff(config, "BuffMage");
		COOPERATIVE_AFK_CHECK = config.getProperty("CheckAfk", true);
		COOPERATIVE_AFK_SECONDS = config.getProperty("CheckAfkSeconds", 240);
		COOPERATIVE_CHECK_PLAYER_IP = config.getProperty("CheckIp", true);
		
		// team vs team
		TVT_SPAWN_TEAM_BLUE = parseLocation(config, "TvT_SpawnTeamBlue").get(0);
		TVT_SPAWN_TEAM_RED = parseLocation(config, "TvT_SpawnTeamRed").get(0);
		TVT_REWARDS = parseReward(config, "TvT_Reward");
		TVT_RADIUS_SPAWN = config.getProperty("TvT_RadiusSpawn", 100);
		
		// survive
		String aux = config.getProperty("Survive_Mobs", "10447").trim();
		for (String mobId : aux.split(","))
		{
			SURVIVE_MOBS_ID.add(Integer.valueOf(mobId));
		}
		SURVIVE_MOBS_PER_ROUND = config.getProperty("Survive_MobsPerRound", 5);
		SURVIVE_REWARDS = parseReward(config, "Survive_Reward");
		SURVIVE_RANGE_SPAWN = config.getProperty("Survive_RangeSpawn", 500);
		
		// all vs all
		AVA_REWARDS = parseReward(config, "AvA_Reward");
		AVA_SPAWN_POINT = parseLocation(config, "AvA_SpawnPoint").get(0);
		AVA_RANGE_SPAWN = config.getProperty("AvA_RangeSpawn", 500);
		
		// capture the flag
		CTF_FLAG_ID = config.getProperty("CtF_FlagId", 60018);
		CTF_HOLDER_ID = config.getProperty("Ctf_HolderId", 60019);
		CTF_POINT_DELIVER_FLAG = config.getProperty("Ctf_DeliverFlag", 30);
		CTF_POINT_KILL_PLAYER = config.getProperty("Ctf_KillPlayer", 1);
		CTF_SPAWN_TEAM_BLUE = parseLocation(config, "CtF_SpawnTeamBlue").get(0);
		CTF_SPAWN_TEAM_RED = parseLocation(config, "CtF_SpawnTeamRed").get(0);
		CTF_FLAG_SPAWN_TEAM_BLUE = parseLocation(config, "CtF_FlagSpawnTeamBlue").get(0);
		CTF_FLAG_SPAWN_TEAM_RED = parseLocation(config, "CtF_FlagSpawnTeamRed").get(0);
		CTF_REWARDS = parseReward(config, "CtF_Reward");
		CTF_RADIUS_SPAWN = config.getProperty("CtF_RadiusSpawn", 100);
		
		// One vs One
		ONEVSONE_DUEL_WINNER_REWARDS = parseReward(config, "OvO_WinnerDuelReward");
		ONEVSONE_DUEL_LOSER_REWARDS = parseReward(config, "OvO_LoserDuelReward");
		ONEVSONE_FINAL_DUEL_WINNER_REWARDS = parseReward(config, "OvO_FinalDuelWinnerReward");
		ONEVSONE_FINAL_DUEL_LOSER_REWARDS = parseReward(config, "OvO_FinalDuelLoserReward");
		ONEVSONE_SPAWN_TEAM_BLUE = parseLocation(config, "OvO_SpawnTeamBlue").get(0);
		ONEVSONE_SPAWN_TEAM_RED = parseLocation(config, "OvO_SpawnTeamRed").get(0);
		ONEVSONE_WAITING_LOCATION = parseLocation(config, "OvO_WaitingLocation").get(0);
	}
	
	private static void loadAntibot()
	{
		UtilProperties config = load(ANTI_BOOT_FILE);
		
		ENABLE_AntiBot = config.getProperty("Enable_AntiBot", false);
		TIME_CHECK_ANTIBOT = config.getProperty("TimeCheckAntiBot", 30);
		KILLER_MONSTERS_ANTIBOT = config.getProperty("KillerMonstersCount", 30);
		KILLER_MONSTERS_ANTIBOT_INCREASE_LEVEL = config.getProperty("KillerMonstersCountIncreaseLevel", true);
	}
	
	private static void loadFakes()
	{
		UtilProperties config = load(FAKES_PLAYERS_FILE);
		
		ENABLE_fakePlayers = config.getProperty("EnableFakePlayers", true);
		FAKE_LEVEL = config.getProperty("FakeLevel", 81);
		FAKE_MAX_PVP = config.getProperty("FakeMaxPvP", 10);
		FAKE_MAX_PK = config.getProperty("FakeMaxPk", 10);
		FAKE_CHANCE_SIT = config.getProperty("FakeChanceSit", 30);
		FAKE_CHANCE_HAS_CLAN = config.getProperty("FakeChanceHasClan", 50);
		SPAWN_RANGE = config.getProperty("FakeSpawnRange", 200);
	}
	
	private static void loadCommunity()
	{
		UtilProperties config = load(COMMUNITY_FILE);
		
		ENABLE_BBS_REGION = config.getProperty("EnableBbsRegion", false);
		ENABLE_BBS_CLAN = config.getProperty("EnableBbsClan", false);
		ENABLE_BBS_HOME = config.getProperty("EnableBbsHome", false);
		// Auction
		ENABLE_BBS_MEMO = config.getProperty("EnableBbsMemo", false);
		COMMISION_FOR_START_SELL = config.getProperty("CommisionForStartSell", 10000);
		COMMISION_FOR_START_SELL_ID = config.getProperty("CommisionForStartSellId", 57);
		COMMISION_FOR_END_SELL = config.getProperty("CommisionForEndSell", 10);
		// rebirth
		ENABLE_BBS_FAVORITE = config.getProperty("EnableBbsFavorite", false);
		MAX_REBIRTH = config.getProperty("MaxRebirth", 10);
		PRICE_FOR_REBIRTH = config.getProperty("PriceForRebirth", 100);
		MUL_PRICE_PER_REBIRTH = config.getProperty("MulPricePerRebirth", 0.3);
		STAT_POINT_PER_REBIRTH = config.getProperty("StatPointPerRebirth", 10);
		MASTERY_POINT_PER_REBIRTH = config.getProperty("MasteryPointPerRebirth", 2);
		LVL_REBIRTH = config.getProperty("LvlRebirth", 1);
	}
	
	private static void loadOfflineShop()
	{
		UtilProperties config = load(OFFLINE_SHOP_FILE);
		
		OFFLINE_TRADE_ENABLE = config.getProperty("OfflineTradeEnable", false);
		OFFLINE_MODE_IN_PEACE_ZONE = config.getProperty("OfflineModeInPeaceZone", false);
		OFFLINE_MODE_NO_DAMAGE = config.getProperty("OfflineModeNoDamage", false);
		OFFLINE_SET_NAME_COLOR = config.getProperty("OfflineSetNameColor", false);
		OFFLINE_NAME_COLOR = Integer.decode("0x" + config.getProperty("OfflineNameColor", 808080));
		OFFLINE_MAX_DAYS = config.getProperty("OfflineMaxDays", 10);
		OFFLINE_DISCONNECT_FINISHED = config.getProperty("OfflineDisconnectFinished", true);
		
		SELLBUFF_ENABLE = config.getProperty("SellBuffEnable", false);
		OFFLINE_SELLBUFF_ENABLE = config.getProperty("OfflineSellBuffEnable", false);
	}
	
	private static void loadVip()
	{
		UtilProperties config = load(VIP_FILE);
		
		VIP_STATS = parseStats(config, "_vip");
		VIP_BONUS_XP = config.getProperty("VipBonusExp", 1.3);
		VIP_BONUS_SP = config.getProperty("VipBonusSp", 1.3);
		
		VIP_BONUS_DROP_NORMAL_AMOUNT = config.getProperty("VipBonusDropAmountNormal", 1.0);
		VIP_BONUS_DROP_SPOIL_AMOUNT = config.getProperty("VipBonusDropAmountSpoil", 1.0);
		VIP_BONUS_DROP_SEED_AMOUNT = config.getProperty("VipBonusDropAmountSeed", 1.0);
		
		VIP_BONUS_DROP_NORMAL_CHANCE = config.getProperty("VipBonusDropChanceNormal", 1.0);
		VIP_BONUS_DROP_SPOIL_CHANCE = config.getProperty("VipBonusDropChanceSpoil", 1.0);
		VIP_BONUS_DROP_SEED_CHANCE = config.getProperty("VipBonusDropChanceSeed", 1.0);
		
		ALLOW_VIP_NCOLOR = config.getProperty("AllowVipNameColor", false);
		VIP_NCOLOR = Integer.decode("0x" + config.getProperty("VipNameColor", "88AA88"));
	}
	
	private static void loadAio()
	{
		UtilProperties config = load(AIO_FILE);
		
		AIO_STATS = parseStats(config, "_aio");
		AIO_CAN_EXIT_PEACE_ZONE = config.getProperty("AioCanExitPeaceZone", false);
		AIO_TITLE = config.getProperty("AioTitle", "-=[ AIO ]=-");
		AIO_LIST_SKILLS = parseBuff(config, "AioSkills");
		AIO_SET_MAX_LVL = config.getProperty("AioSetMaxLevel", false);
		AIO_ITEM_ID = config.getProperty("AioItemId", 2516);
	}
	
	private static void loadNewCharacter()
	{
		UtilProperties config = load(NEW_CHARACTER_CREATED_FILE);
		
		NEW_CHARACTER_CREATED_TITLE = config.getProperty("Title", "L2DevsCustom");
		NEW_CHARACTER_CREATED_GIVE_BUFF = config.getProperty("GiveBuff", false);
		NEW_CHARACTER_CREATED_BUFFS_WARRIORS = parseBuff(config, "BuffsWarriors");
		NEW_CHARACTER_CREATED_BUFFS_MAGE = parseBuff(config, "BuffsMage");
		NEW_CHARACTER_CREATED_SEND_SCREEN_MSG = config.getProperty("SendScreenMsg", "http://l2devsadmins.com/");
	}
	
	private static void loadVoteReward()
	{
		UtilProperties config = load(VOTE_REWARD_FILE);
		
		ENABLE_VoteReward = config.getProperty("Enable_VoteReward", false);
		TIME_CHECK_VOTES = config.getProperty("TimeCheckVotes", 60);
		String aux = config.getProperty("VoteRewards").trim();
		for (String pvpReward : aux.split(";"))
		{
			final String[] infos = pvpReward.split(",");
			VOTE_REWARDS.put(Integer.valueOf(infos[0]), new RewardHolder(Integer.valueOf(infos[1]), Integer.valueOf(infos[2])));
		}
		ENABLE_HOPZONE = config.getProperty("EnableHopzone", false);
		ENABLE_TOPZONE = config.getProperty("EnableTopzone", false);
		ENABLE_NETWORK = config.getProperty("EnableNetwork", false);
		HOPZONE_URL = config.getProperty("HopzoneURL", "");
		TOPZONE_URL = config.getProperty("TopzoneURL", "");
		NETWORK_URL = config.getProperty("NetworkURL", "");
		
		ENABLE_VoteRewardIndivualHopzone = config.getProperty("IndividualHopzone", false);
		ENABLE_VoteRewardIndivualTopzone = config.getProperty("IndividualTopzone", false);
		ENABLE_VoteRewardIndivualNetwork = config.getProperty("IndividualNetwork", false);
		
		INDIVIDUAL_VOTE_TIME_VOTE = config.getProperty("TimeToCheckVote", 30);
		String[] rewards = config.getProperty("IndividualReward").split(",");
		INDIVIDUAL_VOTE_REWARD = new RewardHolder(Integer.valueOf(rewards[0]), Integer.valueOf(rewards[1]));
		INDIVIDUAL_VOTE_TIME_CAN_VOTE = config.getProperty("TimeCanVote", 12);
	}
	
	private static void loadAnnounceKillBoss()
	{
		UtilProperties config = load(ANNOUNCE_KILL_BOOS_FILE);
		
		ENABLE_AnnounceKillBoss = config.getProperty("Enable_AnnounceKillBoss", false);
		ANNOUNCE_KILL_BOSS_ENABLE_DAY = parseWeekDay(config, "AnnounceKillBossEnableDay");
		ANNOUNCE_KILL_BOSS = config.getProperty("AnnounceKillBoss", "%s1 ah matado al RaidBoss %s2");
		ANNOUNCE_KILL_GRANDBOSS = config.getProperty("AnnounceKillGrandBoss", "%s1 ah matado al GrandBoss %s2");
	}
	
	private static void loadColorAmountPvp()
	{
		UtilProperties config = load(COLOR_AMOUNT_PVP_FILE);
		String aux = "";
		
		ENABLE_ColorAccordingAmountPvP = config.getProperty("Enable_ColorAccordingAmountPvP", false);
		aux = config.getProperty("PvpColorName").trim();
		for (String colorInfo : aux.split(";"))
		{
			final String[] infos = colorInfo.split(",");
			PVP_COLOR_NAME.put(Integer.valueOf(infos[0]), infos[1]);
		}
	}
	
	private static void loadEnchantAbnormalEffect()
	{
		UtilProperties config = load(ENCHANT_ABNORMAL_EFFECT_FILE);
		
		ENABLE_EnchantAbnormalEffectArmor = config.getProperty("Enable_EnchantAbnormalEffectArmor", false);
		ENCHANT_EFFECT_LVL = config.getProperty("EnchantEffectLevel", 6);
	}
	
	private static void loadPvpReward()
	{
		UtilProperties config = load(PVP_REWARD_FILE);
		
		ENABLE_PvpReward = config.getProperty("Enable_PvpReward", false);
		PVP_REWARDS = parseReward(config, "PvpReward");
		PVP_TIME = config.getProperty("PvpTime", 5000);
	}
	
	private static void loadSpreeKills()
	{
		UtilProperties config = load(SPREE_KILLS_FILE);
		
		ENABLE_SpreeKills = config.getProperty("Enable_SpreeKills", false);
		String aux = config.getProperty("AnnouncementsKills").trim();
		for (String announcements : aux.split(";"))
		{
			final String[] infos = announcements.split(",");
			ANNOUNCEMENTS_KILLS.put(Integer.valueOf(infos[0]), infos[1]);
		}
		
		aux = config.getProperty("SoundsKills").trim();
		for (String announcements : aux.split(";"))
		{
			final String[] infos = announcements.split(",");
			SOUNDS_KILLS.put(Integer.valueOf(infos[0]), infos[1]);
		}
	}
	
	private static void loadSubclassAcumulatives()
	{
		UtilProperties config = load(SUBCLASS_ACUMULATIVES_FILE);
		
		ENABLE_SubClassAcumulatives = config.getProperty("Enable_SubClassAcumulatives", false);
		ACUMULATIVE_PASIVE_SKILLS = config.getProperty("AcumulativePasiveSkill", false);
		String aux = config.getProperty("DontAcumulativeSkills").trim();
		for (String subAcuInfo : aux.split(","))
		{
			DONT_ACUMULATIVE_SKILLS_ID.add(Integer.valueOf(subAcuInfo));
		}
	}
	
	// MISC ------------------------------------------------------------------------------------------------------- //
	private static UtilProperties load(String filename)
	{
		UtilProperties result = new UtilProperties();
		
		try
		{
			result.load(new File(filename));
		}
		catch (Exception e)
		{
			LOG.warning("Error loading config : " + filename + "!");
		}
		
		return result;
	}
	
	// PARSERS ---------------------------------------------------------------------------------------------------- //
	private static Map<Stats, Double> parseStats(UtilProperties propertie, String endConfigName)
	{
		Map<Stats, Double> map = new HashMap<>();
		
		for (Stats st : Stats.values())
		{
			map.put(st, propertie.getProperty(st.name() + endConfigName, 1.0));
		}
		
		return map;
	}
	
	private static List<Location> parseLocation(UtilProperties propertie, String configName)
	{
		List<Location> auxReturn = new ArrayList<>();
		
		String aux = propertie.getProperty(configName).trim();
		for (String loc : aux.split(";"))
		{
			final String[] infos = loc.split(",");
			auxReturn.add(new Location(Integer.parseInt(infos[0]), Integer.parseInt(infos[1]), Integer.parseInt(infos[2])));
		}
		
		return auxReturn;
	}
	
	private static List<RewardHolder> parseReward(UtilProperties propertie, String configName)
	{
		List<RewardHolder> auxReturn = new ArrayList<>();
		
		String aux = propertie.getProperty(configName).trim();
		for (String pvpReward : aux.split(";"))
		{
			final String[] infos = pvpReward.split(",");
			if (infos.length > 2)
			{
				auxReturn.add(new RewardHolder(Integer.valueOf(infos[0]), Integer.valueOf(infos[1]), Integer.valueOf(infos[2])));
				
			}
			else
			{
				auxReturn.add(new RewardHolder(Integer.valueOf(infos[0]), Integer.valueOf(infos[1])));
			}
		}
		
		return auxReturn;
	}
	
	private static List<WeekDayType> parseWeekDay(UtilProperties propertie, String configName)
	{
		List<WeekDayType> auxReturn = new ArrayList<>();
		
		String aux = propertie.getProperty(configName, "SUNDAY").trim();
		for (String info : aux.split(","))
		{
			auxReturn.add(WeekDayType.valueOf(info));
		}
		
		return auxReturn;
	}
	
	private static List<IntIntHolder> parseBuff(UtilProperties propertie, String configName)
	{
		List<IntIntHolder> auxReturn = new ArrayList<>();
		
		String aux = propertie.getProperty(configName).trim();
		for (String buff : aux.split(";"))
		{
			final String[] infos = buff.split(",");
			auxReturn.add(new IntIntHolder(Integer.valueOf(infos[0]), Integer.valueOf(infos[1])));
		}
		
		return auxReturn;
	}
}
