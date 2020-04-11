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
package l2r;

import info.tak11.subnet.Subnet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import l2r.gameserver.engines.DocumentParser;
import l2r.gameserver.enums.IllegalActionPunishmentType;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.util.FloodProtectorConfig;
import l2r.gameserver.util.Util;
import l2r.util.PropertiesParser;
import l2r.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class loads all the game server related configurations from files.<br>
 * The files are usually located in config folder in server root folder.<br>
 * Each configuration has a default value (that should reflect retail behavior).
 */
public final class Config
{
	private static final Logger _log = LoggerFactory.getLogger(Config.class);
	// --------------------------------------------------
	// Constants
	// --------------------------------------------------
	public static final String EOL = System.getProperty("line.separator");
	// --------------------------------------------------
	// L2J Property File Definitions
	// --------------------------------------------------
	public static final String CHARACTER_CONFIG_FILE = "./config/main/Character.ini";
	public static final String FEATURE_CONFIG_FILE = "./config/main/Feature.ini";
	public static final String FORTSIEGE_CONFIGURATION_FILE = "./config/main/FortSiege.ini";
	public static final String GENERAL_CONFIG_FILE = "./config/main/General.ini";
	public static final String HEXID_FILE = "./config/hexid.txt";
	public static final String ID_CONFIG_FILE = "./config/main/IdFactory.ini";
	public static final String SERVER_VERSION_FILE = "./config/version/l2rcore-version.ini";
	public static final String DATAPACK_VERSION_FILE = "./config/version/l2rdp-version.ini";
	public static final String L2JMOD_CONFIG_FILE = "./config/main/L2JMods.ini";
	public static final String LOGIN_CONFIGURATION_FILE = "./config/LoginServer.ini";
	public static final String NPC_CONFIG_FILE = "./config/main/NPC.ini";
	public static final String PVP_CONFIG_FILE = "./config/main/PVP.ini";
	public static final String RATES_CONFIG_FILE = "./config/main/Rates.ini";
	public static final String CONFIGURATION_FILE = "./config/network/Server.ini";
	public static final String IP_CONFIG_FILE = "./config/network/ipconfig.xml";
	public static final String SIEGE_CONFIGURATION_FILE = "./config/main/Siege.ini";
	public static final String TW_CONFIGURATION_FILE = "./config/main/TerritoryWar.ini";
	public static final String TELNET_FILE = "./config/network/Telnet.ini";
	public static final String FLOOD_PROTECTOR_FILE = "./config/main/FloodProtector.ini";
	public static final String MMO_CONFIG_FILE = "./config/main/MMO.ini";
	public static final String OLYMPIAD_CONFIG_FILE = "./config/main/Olympiad.ini";
	public static final String GRACIASEEDS_CONFIG_FILE = "./config/main/GraciaSeeds.ini";
	public static final String CHAT_FILTER_FILE = "./config/main/chatfilter.txt";
	public static final String EMAIL_CONFIG_FILE = "./config/main/Email.ini";
	public static final String CH_SIEGE_FILE = "./config/main/ConquerableHallSiege.ini";
	public static final String ITEM_MALL_CONFIG_FILE = "./config/main/ItemMall.ini";
	public static final String DEBUG_CONFIG_FILE = "./config/main/Debug.ini";
	public static final String GEODATA_CONFIG_FILE = "./config/main/Geodata.ini";
	// Bosses
	public static final String ANTHARAS_CONFIG = "./config/bosses/Antharas.ini";
	public static final String VALAKAS_CONFIG = "./config/bosses/Valakas.ini";
	public static final String BAIUM_CONFIG = "./config/bosses/Baium.ini";
	public static final String BELETH_CONFIG = "./config/bosses/Beleth.ini";
	public static final String CORE_CONFIG = "./config/bosses/Core.ini";
	public static final String EKIMUS_CONFIG = "./config/bosses/Ekimus.ini";
	public static final String FRINTEZZA_CONFIG = "./config/bosses/Frintezza.ini";
	public static final String FREYA_EASY_CONFIG = "./config/bosses/FreyaEasy.ini";
	public static final String FREYA_HARDCORE_CONFIG = "./config/bosses/FreyaHardCore.ini";
	public static final String ORFEN_CONFIG = "./config/bosses/Orfen.ini";
	public static final String QUEEN_ANT_CONFIG = "./config/bosses/QueenAnt.ini";
	public static final String SAILREN_CONFIG = "./config/bosses/Sailren.ini";
	public static final String VAN_HALTER_CONFIG = "./config/bosses/VanHalter.ini";
	public static final String ZAKEN_CONFIG = "./config/bosses/Zaken.ini";
	public static final String DESTRUCTION_BOSSES_CONFIG = "./config/bosses/DestructionBosses.ini";
	
	// --------------------------------------------------
	// L2J Variable Definitions
	// --------------------------------------------------
	public static boolean ALT_GAME_DELEVEL;
	public static boolean DECREASE_SKILL_LEVEL;
	public static double ALT_WEIGHT_LIMIT;
	public static int RUN_SPD_BOOST;
	public static int DEATH_PENALTY_CHANCE;
	public static double RESPAWN_RESTORE_CP;
	public static double RESPAWN_RESTORE_HP;
	public static double RESPAWN_RESTORE_MP;
	public static boolean ALT_GAME_TIREDNESS;
	public static boolean ENABLE_MODIFY_SKILL_DURATION;
	public static Map<Integer, Integer> SKILL_DURATION_LIST;
	public static boolean ENABLE_MODIFY_SKILL_REUSE;
	public static Map<Integer, Integer> SKILL_REUSE_LIST;
	public static boolean AUTO_LEARN_SKILLS;
	public static boolean AUTO_LEARN_FS_SKILLS;
	public static boolean AUTO_LOOT_HERBS;
	public static byte BUFFS_MAX_AMOUNT;
	public static byte TRIGGERED_BUFFS_MAX_AMOUNT;
	public static byte DANCES_MAX_AMOUNT;
	public static boolean DANCE_CANCEL_BUFF;
	public static boolean DANCE_CONSUME_ADDITIONAL_MP;
	public static boolean ALT_STORE_DANCES;
	public static boolean AUTO_LEARN_DIVINE_INSPIRATION;
	public static boolean ALT_GAME_CANCEL_BOW;
	public static boolean ALT_GAME_CANCEL_CAST;
	public static boolean EFFECT_CANCELING;
	public static boolean ALT_GAME_MAGICFAILURES;
	public static int PLAYER_FAKEDEATH_UP_PROTECTION;
	public static boolean STORE_SKILL_COOLTIME;
	public static boolean SUBCLASS_STORE_SKILL_COOLTIME;
	public static boolean SUMMON_STORE_SKILL_COOLTIME;
	public static boolean ALT_GAME_SHIELD_BLOCKS;
	public static int ALT_PERFECT_SHLD_BLOCK;
	public static boolean ALLOW_CLASS_MASTERS;
	public static ClassMasterSettings CLASS_MASTER_SETTINGS;
	public static boolean ALLOW_ENTIRE_TREE;
	public static boolean ALTERNATE_CLASS_MASTER;
	public static boolean LIFE_CRYSTAL_NEEDED;
	public static boolean ES_SP_BOOK_NEEDED;
	public static boolean DIVINE_SP_BOOK_NEEDED;
	public static boolean ALT_GAME_SKILL_LEARN;
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static boolean ALT_GAME_SUBCLASS_EVERYWHERE;
	public static boolean ALLOW_TRANSFORM_WITHOUT_QUEST;
	public static int FEE_DELETE_TRANSFER_SKILLS;
	public static int FEE_DELETE_SUBCLASS_SKILLS;
	public static boolean RESTORE_SERVITOR_ON_RECONNECT;
	public static boolean RESTORE_PET_ON_RECONNECT;
	public static double MAX_BONUS_EXP;
	public static double MAX_BONUS_SP;
	public static byte MAX_SUBCLASS;
	public static byte BASE_SUBCLASS_LEVEL;
	public static byte MAX_SUBCLASS_LEVEL;
	public static int MAX_PVTSTORESELL_SLOTS_DWARF;
	public static int MAX_PVTSTORESELL_SLOTS_OTHER;
	public static int MAX_PVTSTOREBUY_SLOTS_DWARF;
	public static int MAX_PVTSTOREBUY_SLOTS_OTHER;
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_GM;
	public static int INVENTORY_MAXIMUM_QUEST_ITEMS;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;
	public static int ALT_FREIGHT_SLOTS;
	public static int ALT_FREIGHT_PRICE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;
	public static int MAX_PERSONAL_FAME_POINTS;
	public static int FORTRESS_ZONE_FAME_TASK_FREQUENCY;
	public static int FORTRESS_ZONE_FAME_AQUIRE_POINTS;
	public static int CASTLE_ZONE_FAME_TASK_FREQUENCY;
	public static int CASTLE_ZONE_FAME_AQUIRE_POINTS;
	public static boolean FAME_FOR_DEAD_PLAYERS;
	public static boolean IS_CRAFTING_ENABLED;
	public static boolean CRAFT_MASTERWORK;
	public static int DWARF_RECIPE_LIMIT;
	public static int COMMON_RECIPE_LIMIT;
	public static boolean ALT_GAME_CREATION;
	public static double ALT_GAME_CREATION_SPEED;
	public static double ALT_GAME_CREATION_XP_RATE;
	public static double ALT_GAME_CREATION_RARE_XPSP_RATE;
	public static double ALT_GAME_CREATION_SP_RATE;
	public static boolean ALT_BLACKSMITH_USE_RECIPES;
	public static int ALT_CLAN_LEADER_DATE_CHANGE;
	public static String ALT_CLAN_LEADER_HOUR_CHANGE;
	public static boolean ALT_CLAN_LEADER_INSTANT_ACTIVATION;
	public static int ALT_CLAN_JOIN_DAYS;
	public static int ALT_CLAN_CREATE_DAYS;
	public static int ALT_CLAN_DISSOLVE_DAYS;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_LEAVED;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED;
	public static int ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED;
	public static int ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED;
	public static int ALT_MAX_NUM_OF_CLANS_IN_ALLY;
	public static int ALT_CLAN_MEMBERS_FOR_WAR;
	public static boolean ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH;
	public static boolean REMOVE_CASTLE_CIRCLETS;
	public static int ALT_PARTY_RANGE;
	public static int ALT_PARTY_RANGE2;
	public static boolean ALT_LEAVE_PARTY_LEADER;
	public static boolean INITIAL_EQUIPMENT_EVENT;
	public static long STARTING_ADENA;
	public static byte STARTING_LEVEL;
	public static int STARTING_SP;
	public static long MAX_ADENA;
	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_RAIDS;
	public static int LOOT_RAIDS_PRIVILEGE_INTERVAL;
	public static int LOOT_RAIDS_PRIVILEGE_CC_SIZE;
	public static int UNSTUCK_INTERVAL;
	public static int TELEPORT_WATCHDOG_TIMEOUT;
	public static int PLAYER_SPAWN_PROTECTION;
	public static List<Integer> SPAWN_PROTECTION_ALLOWED_ITEMS;
	public static int PLAYER_TELEPORT_PROTECTION;
	public static boolean RANDOM_RESPAWN_IN_TOWN_ENABLED;
	public static boolean OFFSET_ON_TELEPORT_ENABLED;
	public static int MAX_OFFSET_ON_TELEPORT;
	public static boolean RESTORE_PLAYER_INSTANCE;
	public static boolean ALLOW_SUMMON_TO_INSTANCE;
	public static int EJECT_DEAD_PLAYER_TIME;
	public static boolean PETITIONING_ALLOWED;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;
	public static boolean ALT_GAME_FREE_TELEPORT;
	public static int DELETE_DAYS;
	public static float ALT_GAME_EXPONENT_XP;
	public static float ALT_GAME_EXPONENT_SP;
	public static String PARTY_XP_CUTOFF_METHOD;
	public static double PARTY_XP_CUTOFF_PERCENT;
	public static int PARTY_XP_CUTOFF_LEVEL;
	public static int[][] PARTY_XP_CUTOFF_GAPS;
	public static int[] PARTY_XP_CUTOFF_GAP_PERCENTS;
	public static boolean DISABLE_TUTORIAL;
	public static boolean EXPERTISE_PENALTY;
	public static boolean STORE_RECIPE_SHOPLIST;
	public static boolean STORE_UI_SETTINGS;
	public static String[] FORBIDDEN_NAMES;
	public static boolean SILENCE_MODE_EXCLUDE;
	public static boolean ALT_VALIDATE_TRIGGER_SKILLS;
	
	// --------------------------------------------------
	// ClanHall Settings
	// --------------------------------------------------
	public static long CH_TELE_FEE_RATIO;
	public static int CH_TELE1_FEE;
	public static int CH_TELE2_FEE;
	public static long CH_ITEM_FEE_RATIO;
	public static int CH_ITEM1_FEE;
	public static int CH_ITEM2_FEE;
	public static int CH_ITEM3_FEE;
	public static long CH_MPREG_FEE_RATIO;
	public static int CH_MPREG1_FEE;
	public static int CH_MPREG2_FEE;
	public static int CH_MPREG3_FEE;
	public static int CH_MPREG4_FEE;
	public static int CH_MPREG5_FEE;
	public static long CH_HPREG_FEE_RATIO;
	public static int CH_HPREG1_FEE;
	public static int CH_HPREG2_FEE;
	public static int CH_HPREG3_FEE;
	public static int CH_HPREG4_FEE;
	public static int CH_HPREG5_FEE;
	public static int CH_HPREG6_FEE;
	public static int CH_HPREG7_FEE;
	public static int CH_HPREG8_FEE;
	public static int CH_HPREG9_FEE;
	public static int CH_HPREG10_FEE;
	public static int CH_HPREG11_FEE;
	public static int CH_HPREG12_FEE;
	public static int CH_HPREG13_FEE;
	public static long CH_EXPREG_FEE_RATIO;
	public static int CH_EXPREG1_FEE;
	public static int CH_EXPREG2_FEE;
	public static int CH_EXPREG3_FEE;
	public static int CH_EXPREG4_FEE;
	public static int CH_EXPREG5_FEE;
	public static int CH_EXPREG6_FEE;
	public static int CH_EXPREG7_FEE;
	public static long CH_SUPPORT_FEE_RATIO;
	public static int CH_SUPPORT1_FEE;
	public static int CH_SUPPORT2_FEE;
	public static int CH_SUPPORT3_FEE;
	public static int CH_SUPPORT4_FEE;
	public static int CH_SUPPORT5_FEE;
	public static int CH_SUPPORT6_FEE;
	public static int CH_SUPPORT7_FEE;
	public static int CH_SUPPORT8_FEE;
	public static long CH_CURTAIN_FEE_RATIO;
	public static int CH_CURTAIN1_FEE;
	public static int CH_CURTAIN2_FEE;
	public static long CH_FRONT_FEE_RATIO;
	public static int CH_FRONT1_FEE;
	public static int CH_FRONT2_FEE;
	public static boolean CH_BUFF_FREE;
	
	// --------------------------------------------------
	// Castle Settings
	// --------------------------------------------------
	public static long CS_TELE_FEE_RATIO;
	public static int CS_TELE1_FEE;
	public static int CS_TELE2_FEE;
	public static long CS_MPREG_FEE_RATIO;
	public static int CS_MPREG1_FEE;
	public static int CS_MPREG2_FEE;
	public static long CS_HPREG_FEE_RATIO;
	public static int CS_HPREG1_FEE;
	public static int CS_HPREG2_FEE;
	public static long CS_EXPREG_FEE_RATIO;
	public static int CS_EXPREG1_FEE;
	public static int CS_EXPREG2_FEE;
	public static long CS_SUPPORT_FEE_RATIO;
	public static int CS_SUPPORT1_FEE;
	public static int CS_SUPPORT2_FEE;
	public static List<Integer> SIEGE_HOUR_LIST;
	public static int OUTER_DOOR_UPGRADE_PRICE2;
	public static int OUTER_DOOR_UPGRADE_PRICE3;
	public static int OUTER_DOOR_UPGRADE_PRICE5;
	public static int INNER_DOOR_UPGRADE_PRICE2;
	public static int INNER_DOOR_UPGRADE_PRICE3;
	public static int INNER_DOOR_UPGRADE_PRICE5;
	public static int WALL_UPGRADE_PRICE2;
	public static int WALL_UPGRADE_PRICE3;
	public static int WALL_UPGRADE_PRICE5;
	public static int TRAP_UPGRADE_PRICE1;
	public static int TRAP_UPGRADE_PRICE2;
	public static int TRAP_UPGRADE_PRICE3;
	public static int TRAP_UPGRADE_PRICE4;
	
	// --------------------------------------------------
	// Fortress Settings
	// --------------------------------------------------
	public static long FS_TELE_FEE_RATIO;
	public static int FS_TELE1_FEE;
	public static int FS_TELE2_FEE;
	public static long FS_MPREG_FEE_RATIO;
	public static int FS_MPREG1_FEE;
	public static int FS_MPREG2_FEE;
	public static long FS_HPREG_FEE_RATIO;
	public static int FS_HPREG1_FEE;
	public static int FS_HPREG2_FEE;
	public static long FS_EXPREG_FEE_RATIO;
	public static int FS_EXPREG1_FEE;
	public static int FS_EXPREG2_FEE;
	public static long FS_SUPPORT_FEE_RATIO;
	public static int FS_SUPPORT1_FEE;
	public static int FS_SUPPORT2_FEE;
	public static int FS_BLOOD_OATH_COUNT;
	public static int FS_UPDATE_FRQ;
	public static int FS_MAX_SUPPLY_LEVEL;
	public static int FS_FEE_FOR_CASTLE;
	public static int FS_MAX_OWN_TIME;
	
	// --------------------------------------------------
	// Feature Settings
	// --------------------------------------------------
	public static int TAKE_FORT_POINTS;
	public static int LOOSE_FORT_POINTS;
	public static int TAKE_CASTLE_POINTS;
	public static int LOOSE_CASTLE_POINTS;
	public static int CASTLE_DEFENDED_POINTS;
	public static int FESTIVAL_WIN_POINTS;
	public static int HERO_POINTS;
	public static int ROYAL_GUARD_COST;
	public static int KNIGHT_UNIT_COST;
	public static int KNIGHT_REINFORCE_COST;
	public static int BALLISTA_POINTS;
	public static int BLOODALLIANCE_POINTS;
	public static int BLOODOATH_POINTS;
	public static int KNIGHTSEPAULETTE_POINTS;
	public static int REPUTATION_SCORE_PER_KILL;
	public static int JOIN_ACADEMY_MIN_REP_SCORE;
	public static int JOIN_ACADEMY_MAX_REP_SCORE;
	public static int RAID_RANKING_1ST;
	public static int RAID_RANKING_2ND;
	public static int RAID_RANKING_3RD;
	public static int RAID_RANKING_4TH;
	public static int RAID_RANKING_5TH;
	public static int RAID_RANKING_6TH;
	public static int RAID_RANKING_7TH;
	public static int RAID_RANKING_8TH;
	public static int RAID_RANKING_9TH;
	public static int RAID_RANKING_10TH;
	public static int RAID_RANKING_UP_TO_50TH;
	public static int RAID_RANKING_UP_TO_100TH;
	public static int CLAN_LEVEL_6_COST;
	public static int CLAN_LEVEL_7_COST;
	public static int CLAN_LEVEL_8_COST;
	public static int CLAN_LEVEL_9_COST;
	public static int CLAN_LEVEL_10_COST;
	public static int CLAN_LEVEL_11_COST;
	public static int CLAN_LEVEL_6_REQUIREMENT;
	public static int CLAN_LEVEL_7_REQUIREMENT;
	public static int CLAN_LEVEL_8_REQUIREMENT;
	public static int CLAN_LEVEL_9_REQUIREMENT;
	public static int CLAN_LEVEL_10_REQUIREMENT;
	public static int CLAN_LEVEL_11_REQUIREMENT;
	public static boolean ALLOW_WYVERN_ALWAYS;
	public static boolean ALLOW_WYVERN_DURING_SIEGE;
	
	// --------------------------------------------------
	// General Settings
	// --------------------------------------------------
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	public static boolean DISPLAY_SERVER_VERSION;
	public static boolean SERVER_LIST_BRACKET;
	public static int SERVER_LIST_TYPE;
	public static int SERVER_LIST_AGE;
	public static boolean SERVER_GMONLY;
	public static boolean GM_HERO_AURA;
	public static boolean GM_STARTUP_INVULNERABLE;
	public static boolean GM_STARTUP_INVISIBLE;
	public static boolean GM_STARTUP_SILENCE;
	public static boolean GM_STARTUP_AUTO_LIST;
	public static boolean GM_STARTUP_DIET_MODE;
	public static boolean GM_ITEM_RESTRICTION;
	public static boolean GM_SKILL_RESTRICTION;
	public static boolean GM_TRADE_RESTRICTED_ITEMS;
	public static boolean GM_RESTART_FIGHTING;
	public static boolean GM_ANNOUNCER_NAME;
	public static boolean GM_CRITANNOUNCER_NAME;
	public static boolean GM_GIVE_SPECIAL_SKILLS;
	public static boolean GM_GIVE_SPECIAL_AURA_SKILLS;
	public static boolean BYPASS_VALIDATION;
	public static boolean GAMEGUARD_ENFORCE;
	public static boolean GAMEGUARD_PROHIBITACTION;
	public static boolean LOG_CHAT;
	public static boolean LOG_AUTO_ANNOUNCEMENTS;
	public static boolean LOG_ITEMS;
	public static boolean LOG_ITEMS_SMALL_LOG;
	public static boolean LOG_ITEM_ENCHANTS;
	public static boolean LOG_SKILL_ENCHANTS;
	public static boolean GMAUDIT;
	public static boolean LOG_GAME_DAMAGE;
	public static int LOG_GAME_DAMAGE_THRESHOLD;
	public static boolean SKILL_CHECK_ENABLE;
	public static boolean SKILL_CHECK_REMOVE;
	public static boolean SKILL_CHECK_GM;
	public static int THREAD_P_EFFECTS;
	public static int THREAD_P_GENERAL;
	public static int GENERAL_PACKET_THREAD_CORE_SIZE;
	public static int IO_PACKET_THREAD_CORE_SIZE;
	public static int GENERAL_THREAD_CORE_SIZE;
	public static int AI_MAX_THREAD;
	public static int CLIENT_PACKET_QUEUE_SIZE;
	public static int CLIENT_PACKET_QUEUE_MAX_BURST_SIZE;
	public static int CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND;
	public static int CLIENT_PACKET_QUEUE_MEASURE_INTERVAL;
	public static int CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND;
	public static int CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN;
	public static int CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN;
	public static int CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN;
	public static int CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN;
	public static boolean DEADLOCK_DETECTOR;
	public static int DEADLOCK_CHECK_INTERVAL;
	public static boolean RESTART_ON_DEADLOCK;
	public static boolean ALLOW_DISCARDITEM;
	public static int AUTODESTROY_ITEM_AFTER;
	public static int HERB_AUTO_DESTROY_TIME;
	public static List<Integer> LIST_PROTECTED_ITEMS;
	public static boolean DATABASE_CLEAN_UP;
	public static long CONNECTION_CLOSE_TIME;
	public static int CHAR_STORE_INTERVAL;
	public static boolean LAZY_ITEMS_UPDATE;
	public static boolean UPDATE_ITEMS_ON_CHAR_STORE;
	public static boolean DESTROY_DROPPED_PLAYER_ITEM;
	public static boolean DESTROY_EQUIPABLE_PLAYER_ITEM;
	public static boolean SAVE_DROPPED_ITEM;
	public static boolean EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD;
	public static int SAVE_DROPPED_ITEM_INTERVAL;
	public static boolean CLEAR_DROPPED_ITEM_TABLE;
	public static boolean AUTODELETE_INVALID_QUEST_DATA;
	public static boolean PRECISE_DROP_CALCULATION;
	public static boolean MULTIPLE_ITEM_DROP;
	public static boolean FORCE_INVENTORY_UPDATE;
	public static boolean LAZY_CACHE;
	public static boolean CACHE_CHAR_NAMES;
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;
	public static int MIN_MONSTER_ANIMATION;
	public static int MAX_MONSTER_ANIMATION;
	public static boolean GRIDS_ALWAYS_ON;
	public static int GRID_NEIGHBOR_TURNON_TIME;
	public static int GRID_NEIGHBOR_TURNOFF_TIME;
	
	public static boolean MOVE_BASED_KNOWNLIST;
	public static long KNOWNLIST_UPDATE_INTERVAL;
	public static int PEACE_ZONE_MODE;
	public static String DEFAULT_GLOBAL_CHAT;
	public static String DEFAULT_TRADE_CHAT;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean WAREHOUSE_CACHE;
	public static int WAREHOUSE_CACHE_TIME;
	public static boolean ALLOW_REFUND;
	public static boolean ALLOW_MAIL;
	public static boolean ALLOW_ATTACHMENTS;
	public static boolean ALLOW_WEAR;
	public static int WEAR_DELAY;
	public static int WEAR_PRICE;
	public static boolean ALLOW_LOTTERY;
	public static boolean ALLOW_RACE;
	public static boolean ALLOW_WATER;
	public static boolean ALLOW_RENTPET;
	public static boolean ALLOWFISHING;
	public static boolean ALLOW_BOAT;
	public static int BOAT_BROADCAST_RADIUS;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean ALLOW_MANOR;
	public static boolean ALLOW_PET_WALKERS;
	public static boolean SERVER_NEWS;
	public static boolean ENABLE_COMMUNITY;
	public static String BBS_DEFAULT;
	public static boolean USE_SAY_FILTER;
	public static String CHAT_FILTER_CHARS;
	public static int[] BAN_CHAT_CHANNELS;
	public static int ALT_OLY_START_TIME;
	public static int ALT_OLY_MIN;
	public static long ALT_OLY_CPERIOD;
	public static long ALT_OLY_BATTLE;
	public static long ALT_OLY_WPERIOD;
	public static long ALT_OLY_VPERIOD;
	public static int ALT_OLY_START_POINTS;
	public static int ALT_OLY_WEEKLY_POINTS;
	public static int ALT_OLY_CLASSED;
	public static int ALT_OLY_NONCLASSED;
	public static int ALT_OLY_TEAMS;
	public static int ALT_OLY_REG_DISPLAY;
	public static int[][] ALT_OLY_CLASSED_REWARD;
	public static int[][] ALT_OLY_NONCLASSED_REWARD;
	public static int[][] ALT_OLY_TEAM_REWARD;
	public static int ALT_OLY_COMP_RITEM;
	public static int ALT_OLY_MIN_MATCHES;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_HERO_POINTS;
	public static int ALT_OLY_RANK1_POINTS;
	public static int ALT_OLY_RANK2_POINTS;
	public static int ALT_OLY_RANK3_POINTS;
	public static int ALT_OLY_RANK4_POINTS;
	public static int ALT_OLY_RANK5_POINTS;
	public static int ALT_OLY_MAX_POINTS;
	public static int ALT_OLY_DIVIDER_CLASSED;
	public static int ALT_OLY_DIVIDER_NON_CLASSED;
	public static int ALT_OLY_MAX_WEEKLY_MATCHES;
	public static int ALT_OLY_MAX_WEEKLY_MATCHES_NON_CLASSED;
	public static int ALT_OLY_MAX_WEEKLY_MATCHES_CLASSED;
	public static int ALT_OLY_MAX_WEEKLY_MATCHES_TEAM;
	public static boolean ALT_OLY_LOG_FIGHTS;
	public static boolean ALT_OLY_SHOW_MONTHLY_WINNERS;
	public static boolean ALT_OLY_ANNOUNCE_GAMES;
	public static List<Integer> LIST_OLY_RESTRICTED_ITEMS;
	public static int ALT_OLY_ENCHANT_LIMIT;
	public static int ALT_OLY_WAIT_TIME;
	public static boolean ENABLE_OLY_ANTIFEED;
	public static boolean ENABLE_REUNION_PERIOD;
	public static int[] ALT_OLY_END_DATE;
	public static String OLYMPIAD_PERIOD;
	public static boolean ENABLE_OLYMPIAD;
	public static int[] ALT_OLY_END_HOUR = new int[3];
	public static int ALT_MANOR_REFRESH_TIME;
	public static int ALT_MANOR_REFRESH_MIN;
	public static int ALT_MANOR_APPROVE_TIME;
	public static int ALT_MANOR_APPROVE_MIN;
	public static int ALT_MANOR_MAINTENANCE_PERIOD;
	public static boolean ALT_MANOR_SAVE_ALL_ACTIONS;
	public static int ALT_MANOR_SAVE_PERIOD_RATE;
	public static long ALT_LOTTERY_PRIZE;
	public static long ALT_LOTTERY_TICKET_PRICE;
	public static float ALT_LOTTERY_5_NUMBER_RATE;
	public static float ALT_LOTTERY_4_NUMBER_RATE;
	public static float ALT_LOTTERY_3_NUMBER_RATE;
	public static long ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
	public static boolean ALT_ITEM_AUCTION_ENABLED;
	public static int ALT_ITEM_AUCTION_EXPIRED_AFTER;
	public static long ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
	public static int FS_TIME_ATTACK;
	public static int FS_TIME_COOLDOWN;
	public static int FS_TIME_ENTRY;
	public static int FS_TIME_WARMUP;
	public static int FS_PARTY_MEMBER_COUNT;
	public static int RIFT_MIN_PARTY_SIZE;
	public static int RIFT_SPAWN_DELAY;
	public static int RIFT_MAX_JUMPS;
	public static int RIFT_AUTO_JUMPS_TIME_MIN;
	public static int RIFT_AUTO_JUMPS_TIME_MAX;
	public static float RIFT_BOSS_ROOM_TIME_MUTIPLY;
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;
	public static IllegalActionPunishmentType DEFAULT_PUNISH;
	public static int DEFAULT_PUNISH_PARAM;
	public static boolean ONLY_GM_ITEMS_FREE;
	public static boolean JAIL_IS_PVP;
	public static boolean JAIL_DISABLE_CHAT;
	public static boolean JAIL_DISABLE_TRANSACTION;
	public static boolean CUSTOM_SPAWNLIST_TABLE;
	public static boolean SAVE_GMSPAWN_ON_CUSTOM;
	public static boolean CUSTOM_NPC_TABLE;
	public static boolean CUSTOM_NPC_SKILLS_TABLE;
	public static boolean CUSTOM_TELEPORT_TABLE;
	public static boolean CUSTOM_DROPLIST_TABLE;
	public static boolean CUSTOM_SKILLS_LOAD;
	public static boolean CUSTOM_ITEMS_LOAD;
	public static boolean CUSTOM_MULTISELL_LOAD;
	public static boolean CUSTOM_BUYLIST_LOAD;
	public static int ALT_BIRTHDAY_GIFT;
	public static String ALT_BIRTHDAY_MAIL_SUBJECT;
	public static String ALT_BIRTHDAY_MAIL_TEXT;
	public static boolean ENABLE_BLOCK_CHECKER_EVENT;
	public static int MIN_BLOCK_CHECKER_TEAM_MEMBERS;
	public static boolean HBCE_FAIR_PLAY;
	public static boolean HELLBOUND_WITHOUT_QUEST;
	public static int PLAYER_MOVEMENT_BLOCK_TIME;
	public static int NORMAL_ENCHANT_COST_MULTIPLIER;
	public static int SAFE_ENCHANT_COST_MULTIPLIER;
	public static boolean BOTREPORT_ENABLE;
	public static String[] BOTREPORT_RESETPOINT_HOUR;
	public static long BOTREPORT_REPORT_DELAY;
	public static boolean BOTREPORT_ALLOW_REPORTS_FROM_SAME_CLAN_MEMBERS;
	
	// --------------------------------------------------
	// FloodProtector Settings
	// --------------------------------------------------
	public static FloodProtectorConfig FLOOD_PROTECTOR_USE_ITEM;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ROLL_DICE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_FIREWORK;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ITEM_PET_SUMMON;
	public static FloodProtectorConfig FLOOD_PROTECTOR_HERO_VOICE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_GLOBAL_CHAT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SUBCLASS;
	public static FloodProtectorConfig FLOOD_PROTECTOR_DROP_ITEM;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SERVER_BYPASS;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MULTISELL;
	public static FloodProtectorConfig FLOOD_PROTECTOR_TRANSACTION;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MANUFACTURE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MANOR;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SENDMAIL;
	public static FloodProtectorConfig FLOOD_PROTECTOR_CHARACTER_SELECT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ITEM_AUCTION;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SHOUT_CHAT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_TRADE_CHAT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_CLAN_CHAT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ALLY_CHAT;
	
	// --------------------------------------------------
	// L2JMods Settings
	// --------------------------------------------------
	public static boolean L2JMOD_CHAMPION_ENABLE;
	public static boolean L2JMOD_CHAMPION_PASSIVE;
	public static int L2JMOD_CHAMPION_FREQUENCY;
	public static String L2JMOD_CHAMP_TITLE;
	public static int L2JMOD_CHAMP_MIN_LVL;
	public static int L2JMOD_CHAMP_MAX_LVL;
	public static int L2JMOD_CHAMPION_HP;
	public static int L2JMOD_CHAMPION_REWARDS;
	public static float L2JMOD_CHAMPION_ADENAS_REWARDS;
	public static float L2JMOD_CHAMPION_HP_REGEN;
	public static float L2JMOD_CHAMPION_ATK;
	public static float L2JMOD_CHAMPION_SPD_ATK;
	public static int L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE;
	public static int L2JMOD_CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE;
	public static int L2JMOD_CHAMPION_REWARD_ID;
	public static int L2JMOD_CHAMPION_REWARD_QTY;
	public static boolean L2JMOD_CHAMPION_ENABLE_VITALITY;
	public static boolean L2JMOD_CHAMPION_ENABLE_IN_INSTANCES;
	public static boolean L2JMOD_ALLOW_WEDDING;
	public static int L2JMOD_WEDDING_PRICE;
	public static boolean L2JMOD_WEDDING_PUNISH_INFIDELITY;
	public static boolean L2JMOD_WEDDING_TELEPORT;
	public static int L2JMOD_WEDDING_TELEPORT_PRICE;
	public static int L2JMOD_WEDDING_TELEPORT_DURATION;
	public static boolean L2JMOD_WEDDING_SAMESEX;
	public static boolean L2JMOD_WEDDING_FORMALWEAR;
	public static int L2JMOD_WEDDING_DIVORCE_COSTS;
	public static boolean L2JMOD_HELLBOUND_STATUS;
	public static boolean BANKING_SYSTEM_ENABLED;
	public static int BANKING_SYSTEM_GOLDBARS;
	public static int BANKING_SYSTEM_ADENA;
	public static boolean L2JMOD_ENABLE_WAREHOUSESORTING_CLAN;
	public static boolean L2JMOD_ENABLE_WAREHOUSESORTING_PRIVATE;
	public static boolean OFFLINE_TRADE_ENABLE;
	public static boolean OFFLINE_CRAFT_ENABLE;
	public static boolean OFFLINE_MODE_IN_PEACE_ZONE;
	public static boolean OFFLINE_MODE_NO_DAMAGE;
	public static boolean RESTORE_OFFLINERS;
	public static int OFFLINE_MAX_DAYS;
	public static boolean OFFLINE_DISCONNECT_FINISHED;
	public static boolean OFFLINE_SET_NAME_COLOR;
	public static int OFFLINE_NAME_COLOR;
	public static boolean OFFLINE_FAME;
	public static boolean L2JMOD_ENABLE_MANA_POTIONS_SUPPORT;
	public static boolean L2JMOD_DISPLAY_SERVER_TIME;
	public static boolean WELCOME_MESSAGE_ENABLED;
	public static String WELCOME_MESSAGE_TEXT;
	public static int WELCOME_MESSAGE_TIME;
	public static boolean L2JMOD_ANTIFEED_ENABLE;
	public static boolean L2JMOD_ANTIFEED_DUALBOX;
	public static boolean L2JMOD_ANTIFEED_DISCONNECTED_AS_DUALBOX;
	public static int L2JMOD_ANTIFEED_INTERVAL;
	public static boolean ANNOUNCE_PK_PVP;
	public static boolean ANNOUNCE_PK_PVP_NORMAL_MESSAGE;
	public static String ANNOUNCE_PK_MSG;
	public static String ANNOUNCE_PVP_MSG;
	public static boolean L2JMOD_CHAT_ADMIN;
	public static boolean L2JMOD_MULTILANG_ENABLE;
	public static List<String> L2JMOD_MULTILANG_ALLOWED = new ArrayList<>();
	public static String L2JMOD_MULTILANG_DEFAULT;
	public static boolean L2JMOD_MULTILANG_VOICED_ALLOW;
	public static boolean L2JMOD_MULTILANG_SM_ENABLE;
	public static List<String> L2JMOD_MULTILANG_SM_ALLOWED = new ArrayList<>();
	public static boolean L2JMOD_MULTILANG_NS_ENABLE;
	public static List<String> L2JMOD_MULTILANG_NS_ALLOWED = new ArrayList<>();
	public static boolean L2WALKER_PROTECTION;
	public static boolean L2JMOD_DEBUG_VOICE_COMMAND;
	public static int L2JMOD_DUALBOX_CHECK_MAX_PLAYERS_PER_IP;
	public static int L2JMOD_DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP;
	public static int L2JMOD_DUALBOX_CHECK_MAX_L2EVENT_PARTICIPANTS_PER_IP;
	public static Map<Integer, Integer> L2JMOD_DUALBOX_CHECK_WHITELIST;
	public static boolean L2JMOD_ALLOW_CHANGE_PASSWORD;
	
	// --------------------------------------------------
	// NPC Settings
	// --------------------------------------------------
	public static boolean ANNOUNCE_MAMMON_SPAWN;
	public static boolean ALT_MOB_AGRO_IN_PEACEZONE;
	public static boolean ALT_ATTACKABLE_NPCS;
	public static boolean ALT_GAME_VIEWNPC;
	public static int MAX_DRIFT_RANGE;
	public static boolean DEEPBLUE_DROP_RULES;
	public static boolean DEEPBLUE_DROP_RULES_RAID;
	public static boolean SHOW_NPC_LVL;
	public static boolean SHOW_CREST_WITHOUT_QUEST;
	public static boolean ENABLE_RANDOM_ENCHANT_EFFECT;
	public static int MIN_NPC_LVL_DMG_PENALTY;
	public static Map<Integer, Float> NPC_DMG_PENALTY;
	public static Map<Integer, Float> NPC_CRIT_DMG_PENALTY;
	public static Map<Integer, Float> NPC_SKILL_DMG_PENALTY;
	public static int MIN_NPC_LVL_MAGIC_PENALTY;
	public static Map<Integer, Float> NPC_SKILL_CHANCE_PENALTY;
	public static int DECAY_TIME_TASK;
	public static int NPC_DECAY_TIME;
	public static int RAID_BOSS_DECAY_TIME;
	public static int SPOILED_DECAY_TIME;
	public static int MAX_SWEEPER_TIME;
	public static boolean GUARD_ATTACK_AGGRO_MOB;
	public static boolean ALLOW_WYVERN_UPGRADER;
	public static List<Integer> LIST_PET_RENT_NPC;
	public static double RAID_HP_REGEN_MULTIPLIER;
	public static double RAID_MP_REGEN_MULTIPLIER;
	public static double RAID_PDEFENCE_MULTIPLIER;
	public static double RAID_MDEFENCE_MULTIPLIER;
	public static double RAID_PATTACK_MULTIPLIER;
	public static double RAID_MATTACK_MULTIPLIER;
	public static double RAID_MINION_RESPAWN_TIMER;
	public static Map<Integer, Integer> MINIONS_RESPAWN_TIME;
	public static float RAID_MIN_RESPAWN_MULTIPLIER;
	public static float RAID_MAX_RESPAWN_MULTIPLIER;
	public static boolean RAID_DISABLE_CURSE;
	public static int RAID_CHAOS_TIME;
	public static int GRAND_CHAOS_TIME;
	public static int MINION_CHAOS_TIME;
	public static int INVENTORY_MAXIMUM_PET;
	public static double PET_HP_REGEN_MULTIPLIER;
	public static double PET_MP_REGEN_MULTIPLIER;
	
	// --------------------------------------------------
	// PvP Settings
	// --------------------------------------------------
	public static boolean KARMA_DROP_GM;
	public static boolean KARMA_AWARD_PK_KILL;
	public static int KARMA_PK_LIMIT;
	public static String KARMA_NONDROPPABLE_PET_ITEMS;
	public static String KARMA_NONDROPPABLE_ITEMS;
	public static int[] KARMA_LIST_NONDROPPABLE_PET_ITEMS;
	public static int[] KARMA_LIST_NONDROPPABLE_ITEMS;
	
	// --------------------------------------------------
	// Rate Settings
	// --------------------------------------------------
	public static float RATE_XP;
	public static float RATE_SP;
	public static float RATE_PARTY_XP;
	public static float RATE_PARTY_SP;
	public static float RATE_CONSUMABLE_COST;
	public static boolean HELLBOUND_ALWAYS_MAX_LEVEL;
	public static boolean HELLBOUND_ALWAYS_MAX_TRUST;
	public static float RATE_HB_TRUST_INCREASE;
	public static float RATE_HB_TRUST_DECREASE;
	public static float RATE_EXTRACTABLE;
	public static float RATE_DROP_ITEMS;
	public static float RATE_DROP_ITEMS_BY_RAID;
	public static float RATE_DROP_SPOIL;
	public static int RATE_DROP_MANOR;
	public static float RATE_QUEST_DROP;
	public static float RATE_QUEST_REWARD;
	public static float RATE_QUEST_REWARD_XP;
	public static float RATE_QUEST_REWARD_SP;
	public static float RATE_QUEST_REWARD_ADENA;
	public static boolean RATE_QUEST_REWARD_USE_MULTIPLIERS;
	public static float RATE_QUEST_REWARD_POTION;
	public static float RATE_QUEST_REWARD_SCROLL;
	public static float RATE_QUEST_REWARD_RECIPE;
	public static float RATE_QUEST_REWARD_MATERIAL;
	public static Map<Integer, Float> RATE_DROP_ITEMS_ID;
	public static float RATE_KARMA_LOST;
	public static float RATE_KARMA_EXP_LOST;
	public static float RATE_SIEGE_GUARDS_PRICE;
	public static float RATE_DROP_COMMON_HERBS;
	public static float RATE_DROP_HP_HERBS;
	public static float RATE_DROP_MP_HERBS;
	public static float RATE_DROP_SPECIAL_HERBS;
	public static int PLAYER_DROP_LIMIT;
	public static int PLAYER_RATE_DROP;
	public static int PLAYER_RATE_DROP_ITEM;
	public static int PLAYER_RATE_DROP_EQUIP;
	public static int PLAYER_RATE_DROP_EQUIP_WEAPON;
	public static float PET_XP_RATE;
	public static int PET_FOOD_RATE;
	public static float SINEATER_XP_RATE;
	public static int KARMA_DROP_LIMIT;
	public static int KARMA_RATE_DROP;
	public static int KARMA_RATE_DROP_ITEM;
	public static int KARMA_RATE_DROP_EQUIP;
	public static int KARMA_RATE_DROP_EQUIP_WEAPON;
	public static double[] PLAYER_XP_PERCENT_LOST;
	
	// --------------------------------------------------
	// Seven Signs Settings
	// --------------------------------------------------
	public static boolean ALT_GAME_CASTLE_DAWN;
	public static boolean ALT_GAME_CASTLE_DUSK;
	public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
	public static int ALT_FESTIVAL_MIN_PLAYER;
	public static int ALT_MAXIMUM_PLAYER_CONTRIB;
	public static long ALT_FESTIVAL_MANAGER_START;
	public static long ALT_FESTIVAL_LENGTH;
	public static long ALT_FESTIVAL_CYCLE_LENGTH;
	public static long ALT_FESTIVAL_FIRST_SPAWN;
	public static long ALT_FESTIVAL_FIRST_SWARM;
	public static long ALT_FESTIVAL_SECOND_SPAWN;
	public static long ALT_FESTIVAL_SECOND_SWARM;
	public static long ALT_FESTIVAL_CHEST_SPAWN;
	public static double ALT_SIEGE_DAWN_GATES_PDEF_MULT;
	public static double ALT_SIEGE_DUSK_GATES_PDEF_MULT;
	public static double ALT_SIEGE_DAWN_GATES_MDEF_MULT;
	public static double ALT_SIEGE_DUSK_GATES_MDEF_MULT;
	public static boolean ALT_STRICT_SEVENSIGNS;
	public static boolean ALT_SEVENSIGNS_LAZY_UPDATE;
	public static int SSQ_DAWN_TICKET_QUANTITY;
	public static int SSQ_DAWN_TICKET_PRICE;
	public static int SSQ_DAWN_TICKET_BUNDLE;
	public static int SSQ_MANORS_AGREEMENT_ID;
	public static int SSQ_JOIN_DAWN_ADENA_FEE;
	
	// --------------------------------------------------
	// Server Settings
	// --------------------------------------------------
	public static int PORT_GAME;
	public static int PORT_LOGIN;
	public static String LOGIN_BIND_ADDRESS;
	public static int LOGIN_TRY_BEFORE_BAN;
	public static int LOGIN_BLOCK_AFTER_BAN;
	public static String GAMESERVER_HOSTNAME;
	public static String DATABASE_DRIVER;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	public static int DATABASE_MAX_CONNECTIONS;
	public static int DATABASE_MAX_IDLE_TIME;
	public static int DATABASE_IDLE_TEST_PERIOD;
	public static int MAXIMUM_ONLINE_USERS;
	public static String CNAME_TEMPLATE;
	public static String PET_NAME_TEMPLATE;
	public static String CLAN_NAME_TEMPLATE;
	public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;
	public static File DATAPACK_ROOT;
	public static boolean ACCEPT_ALTERNATE_ID;
	public static int REQUEST_ID;
	public static boolean RESERVE_HOST_ON_LOGIN = false;
	public static List<Integer> PROTOCOL_LIST;
	public static boolean LOGIN_SERVER_SCHEDULE_RESTART;
	public static long LOGIN_SERVER_SCHEDULE_RESTART_TIME;
	
	// --------------------------------------------------
	// MMO Settings
	// --------------------------------------------------
	public static int MMO_SELECTOR_SLEEP_TIME;
	public static int MMO_MAX_SEND_PER_PASS;
	public static int MMO_MAX_READ_PER_PASS;
	public static int MMO_HELPER_BUFFER_COUNT;
	public static boolean MMO_TCP_NODELAY;
	
	// --------------------------------------------------
	// Vitality Settings
	// --------------------------------------------------
	public static boolean ENABLE_VITALITY;
	public static boolean RECOVER_VITALITY_ON_RECONNECT;
	public static boolean ENABLE_DROP_VITALITY_HERBS;
	public static float RATE_VITALITY_LEVEL_1;
	public static float RATE_VITALITY_LEVEL_2;
	public static float RATE_VITALITY_LEVEL_3;
	public static float RATE_VITALITY_LEVEL_4;
	public static float RATE_DROP_VITALITY_HERBS;
	public static float RATE_RECOVERY_VITALITY_PEACE_ZONE;
	public static float RATE_VITALITY_LOST;
	public static float RATE_VITALITY_GAIN;
	public static float RATE_RECOVERY_ON_RECONNECT;
	public static int STARTING_VITALITY_POINTS;
	
	// --------------------------------------------------
	// No classification assigned to the following yet
	// --------------------------------------------------
	public static int MAX_ITEM_IN_PACKET;
	public static boolean CHECK_KNOWN;
	public static int GAME_SERVER_LOGIN_PORT;
	public static String GAME_SERVER_LOGIN_HOST;
	public static List<String> GAME_SERVER_SUBNETS;
	public static List<String> GAME_SERVER_HOSTS;
	public static int PVP_NORMAL_TIME;
	public static int PVP_PVP_TIME;
	
	// --------------------------------------------------
	// ItemMall Settings
	// --------------------------------------------------
	public static int GAME_POINT_ITEM_ID;
	
	// --------------------------------------------------
	// Debug Settings
	// --------------------------------------------------
	public static boolean DEBUG;
	public static boolean DEBUG_SKILL_PARSING;
	public static boolean DEBUG_SKILL_DURATION;
	public static boolean DEBUG_MULTIPLE_REGISTRATIONS_OF_NPCS;
	public static boolean DEBUG_SCRIPT_NOTIFIES;
	public static boolean PACKET_HANDLER_DEBUG;
	public static boolean DEVELOPER;
	public static boolean ACCEPT_GEOEDITOR_CONN;
	public static boolean ALT_DEV_NO_HANDLERS;
	public static boolean ALT_DEV_NO_QUESTS;
	public static boolean ALT_DEV_NO_SPAWNS;
	public static boolean ALT_DEV_SHOW_QUESTS_LOAD_IN_LOGS;
	public static boolean ALT_DEV_SHOW_SCRIPTS_LOAD_IN_LOGS;
	
	// --------------------------------------------------
	// Geodata Settings
	// --------------------------------------------------
	public static int GEODATA;
	public static String GEODATA_DRIVER;
	public static String PATHFIND_BUFFERS;
	public static float LOW_WEIGHT;
	public static float MEDIUM_WEIGHT;
	public static float HIGH_WEIGHT;
	public static boolean ADVANCED_DIAGONAL_STRATEGY;
	public static float DIAGONAL_WEIGHT;
	public static int MAX_POSTFILTER_PASSES;
	public static boolean DEBUG_PATH;
	public static boolean FORCE_GEODATA;
	public static int COORD_SYNCHRONIZE;
	public static boolean ENABLE_FALLING_DAMAGE;
	
	public static enum IdFactoryType
	{
		Compaction,
		BitSet,
		Stack
	}
	
	public static IdFactoryType IDFACTORY_TYPE;
	public static boolean BAD_ID_CHECKING;
	
	public static int MAX_ENCHANT_LEVEL;
	public static double ENCHANT_CHANCE_ELEMENT_STONE;
	public static double ENCHANT_CHANCE_ELEMENT_CRYSTAL;
	public static double ENCHANT_CHANCE_ELEMENT_JEWEL;
	public static double ENCHANT_CHANCE_ELEMENT_ENERGY;
	public static int ENCHANT_SAFE_MAX;
	public static int ENCHANT_SAFE_MAX_FULL;
	public static int[] ENCHANT_BLACKLIST;
	public static int AUGMENTATION_NG_SKILL_CHANCE;
	public static int AUGMENTATION_NG_GLOW_CHANCE;
	public static int AUGMENTATION_MID_SKILL_CHANCE;
	public static int AUGMENTATION_MID_GLOW_CHANCE;
	public static int AUGMENTATION_HIGH_SKILL_CHANCE;
	public static int AUGMENTATION_HIGH_GLOW_CHANCE;
	public static int AUGMENTATION_TOP_SKILL_CHANCE;
	public static int AUGMENTATION_TOP_GLOW_CHANCE;
	public static int AUGMENTATION_BASESTAT_CHANCE;
	public static int AUGMENTATION_ACC_SKILL_CHANCE;
	public static boolean RETAIL_LIKE_AUGMENTATION;
	public static int[] RETAIL_LIKE_AUGMENTATION_NG_CHANCE;
	public static int[] RETAIL_LIKE_AUGMENTATION_MID_CHANCE;
	public static int[] RETAIL_LIKE_AUGMENTATION_HIGH_CHANCE;
	public static int[] RETAIL_LIKE_AUGMENTATION_TOP_CHANCE;
	public static boolean RETAIL_LIKE_AUGMENTATION_ACCESSORY;
	public static int[] AUGMENTATION_BLACKLIST;
	public static double HP_REGEN_MULTIPLIER;
	public static double MP_REGEN_MULTIPLIER;
	public static double CP_REGEN_MULTIPLIER;
	public static boolean IS_TELNET_ENABLED;
	public static boolean SHOW_LICENCE;
	public static boolean ACCEPT_NEW_GAMESERVER;
	public static int SERVER_ID;
	public static byte[] HEX_ID;
	public static boolean AUTO_CREATE_ACCOUNTS;
	public static boolean FLOOD_PROTECTION;
	public static int FAST_CONNECTION_LIMIT;
	public static int NORMAL_CONNECTION_TIME;
	public static int FAST_CONNECTION_TIME;
	public static int MAX_CONNECTION_PER_IP;
	
	// --------------------------------------------------
	// GrandBoss Settings
	// --------------------------------------------------
	// Antharas
	public static int ANTHARAS_WAIT_TIME;
	public static int ANTHARAS_SPAWN_INTERVAL;
	public static int ANTHARAS_SPAWN_RANDOM;
	
	// --------------------------------------------------
	// Valakas
	// --------------------------------------------------
	public static int VALAKAS_WAIT_TIME;
	public static int VALAKAS_SPAWN_INTERVAL;
	public static int VALAKAS_SPAWN_RANDOM;
	
	// --------------------------------------------------
	// Baium
	// --------------------------------------------------
	public static int BAIUM_SPAWN_INTERVAL;
	public static int BAIUM_SPAWN_RANDOM;
	
	// --------------------------------------------------
	// Core
	// --------------------------------------------------
	public static int CORE_SPAWN_INTERVAL;
	public static int CORE_SPAWN_RANDOM;
	
	// --------------------------------------------------
	// Offen
	// --------------------------------------------------
	public static int ORFEN_SPAWN_INTERVAL;
	public static int ORFEN_SPAWN_RANDOM;
	
	// --------------------------------------------------
	// Queen Ant
	// --------------------------------------------------
	public static int QUEEN_ANT_SPAWN_INTERVAL;
	public static int QUEEN_ANT_SPAWN_RANDOM;
	public static int QUEEN_ANT_CHAR_ENTER_LEVEL_RESTRICTION;
	
	// --------------------------------------------------
	// Zaken
	// --------------------------------------------------
	public static int ZAKEN_MINLEVEL_DAYTIME;
	public static int ZAKEN_MINLEVEL_DAYTIME83;
	public static int ZAKEN_MINMEMBERS_DAYTIME;
	public static int ZAKEN_MINMEMBERS_NIGHTTIME;
	public static int ZAKEN_MAXMEMBERS_DAYTIME;
	public static int ZAKEN_MAXMEMBERS_NIGHTTIME;
	
	// --------------------------------------------------
	// Beleth
	// --------------------------------------------------
	public static int BELETH_MIN_PLAYERS;
	public static int BELETH_SPAWN_INTERVAL;
	public static int BELETH_SPAWN_RANDOM;
	
	// --------------------------------------------------
	// Sailren
	// --------------------------------------------------
	public static int INTERVAL_OF_SAILREN_SPAWN;
	public static int RANDOM_OF_SAILREN_SPAWN;
	
	// --------------------------------------------------
	// Van Halter
	// --------------------------------------------------
	public static int HPH_FIXINTERVALOFHALTER;
	public static int HPH_RANDOMINTERVALOFHALTER;
	public static int HPH_APPTIMEOFHALTER;
	public static int HPH_ACTIVITYTIMEOFHALTER;
	public static int HPH_FIGHTTIMEOFHALTER;
	public static int HPH_CALLROYALGUARDHELPERCOUNT;
	public static int HPH_CALLROYALGUARDHELPERINTERVAL;
	public static int HPH_INTERVALOFDOOROFALTER;
	public static int HPH_TIMEOFLOCKUPDOOROFALTAR;
	
	// --------------------------------------------------
	// Freya
	// --------------------------------------------------
	public static int MIN_PLAYERS_TO_HARD;
	public static int MAX_PLAYERS_TO_HARD;
	public static int MIN_PLAYERS_TO_EASY;
	public static int MAX_PLAYERS_TO_EASY;
	public static int MIN_PLAYER_LEVEL_TO_HARD;
	public static int MIN_PLAYER_LEVEL_TO_EASY;
	
	// --------------------------------------------------
	// Frintezza
	// --------------------------------------------------
	public static int MIN_PLAYER_TO_FE;
	public static int MAX_PLAYER_TO_FE;
	public static int MIN_LEVEL_TO_FE;
	
	// --------------------------------------------------
	// Ekimus
	// --------------------------------------------------
	public static int SOI_EKIMUS_KILL_COUNT;
	public static int EROSION_ATTACK_MIN_PLAYERS;
	public static int EROSION_ATTACK_MAX_PLAYERS;
	public static int EROSION_DEFENCE_MIN_PLAYERS;
	public static int EROSION_DEFENCE_MAX_PLAYERS;
	public static int HEART_ATTACK_MIN_PLAYERS;
	public static int HEART_ATTACK_MAX_PLAYERS;
	public static int HEART_DEFENCE_MIN_PLAYERS;
	public static int HEART_DEFENCE_MAX_PLAYERS;
	
	// --------------------------------------------------
	// Blood Altars
	// --------------------------------------------------
	public static int CHANGE_STATUS;
	public static int CHANCE_SPAWN;
	public static int RESPAWN_TIME;
	
	// --------------------------------------------------
	// Gracia Seeds Settings
	// --------------------------------------------------
	public static int SOD_TIAT_KILL_COUNT;
	public static long SOD_STAGE_2_LENGTH;
	
	// --------------------------------------------------
	// Chatfilter Settings
	// --------------------------------------------------
	public static ArrayList<String> FILTER_LIST;
	
	// --------------------------------------------------
	// Email Settings
	// --------------------------------------------------
	public static String EMAIL_SERVERINFO_NAME;
	public static String EMAIL_SERVERINFO_ADDRESS;
	public static boolean EMAIL_SYS_ENABLED;
	public static String EMAIL_SYS_HOST;
	public static int EMAIL_SYS_PORT;
	public static boolean EMAIL_SYS_SMTP_AUTH;
	public static String EMAIL_SYS_FACTORY;
	public static boolean EMAIL_SYS_FACTORY_CALLBACK;
	public static String EMAIL_SYS_USERNAME;
	public static String EMAIL_SYS_PASSWORD;
	public static String EMAIL_SYS_ADDRESS;
	public static String EMAIL_SYS_SELECTQUERY;
	public static String EMAIL_SYS_DBFIELD;
	
	// --------------------------------------------------
	// Conquerable Halls Settings
	// --------------------------------------------------
	public static int CHS_CLAN_MINLEVEL;
	public static int CHS_MAX_ATTACKERS;
	public static int CHS_MAX_FLAGS_PER_CLAN;
	public static boolean CHS_ENABLE_FAME;
	public static int CHS_FAME_AMOUNT;
	public static int CHS_FAME_FREQUENCY;
	
	/**
	 * This class initializes all global variables for configuration.<br>
	 * If the key doesn't appear in properties file, a default value is set by this class. {@link #CONFIGURATION_FILE} (properties file) for configuring your server.
	 */
	public static void load()
	{
		if (Server.serverMode == Server.MODE_GAMESERVER)
		{
			FLOOD_PROTECTOR_USE_ITEM = new FloodProtectorConfig("UseItemFloodProtector");
			FLOOD_PROTECTOR_ROLL_DICE = new FloodProtectorConfig("RollDiceFloodProtector");
			FLOOD_PROTECTOR_FIREWORK = new FloodProtectorConfig("FireworkFloodProtector");
			FLOOD_PROTECTOR_ITEM_PET_SUMMON = new FloodProtectorConfig("ItemPetSummonFloodProtector");
			FLOOD_PROTECTOR_HERO_VOICE = new FloodProtectorConfig("HeroVoiceFloodProtector");
			FLOOD_PROTECTOR_GLOBAL_CHAT = new FloodProtectorConfig("GlobalChatFloodProtector");
			FLOOD_PROTECTOR_SUBCLASS = new FloodProtectorConfig("SubclassFloodProtector");
			FLOOD_PROTECTOR_DROP_ITEM = new FloodProtectorConfig("DropItemFloodProtector");
			FLOOD_PROTECTOR_SERVER_BYPASS = new FloodProtectorConfig("ServerBypassFloodProtector");
			FLOOD_PROTECTOR_MULTISELL = new FloodProtectorConfig("MultiSellFloodProtector");
			FLOOD_PROTECTOR_TRANSACTION = new FloodProtectorConfig("TransactionFloodProtector");
			FLOOD_PROTECTOR_MANUFACTURE = new FloodProtectorConfig("ManufactureFloodProtector");
			FLOOD_PROTECTOR_MANOR = new FloodProtectorConfig("ManorFloodProtector");
			FLOOD_PROTECTOR_SENDMAIL = new FloodProtectorConfig("SendMailFloodProtector");
			FLOOD_PROTECTOR_CHARACTER_SELECT = new FloodProtectorConfig("CharacterSelectFloodProtector");
			FLOOD_PROTECTOR_ITEM_AUCTION = new FloodProtectorConfig("ItemAuctionFloodProtector");
			FLOOD_PROTECTOR_SHOUT_CHAT = new FloodProtectorConfig("ShoutChatFloodProtector");
			FLOOD_PROTECTOR_TRADE_CHAT = new FloodProtectorConfig("TradeChatFloodProtector");
			FLOOD_PROTECTOR_CLAN_CHAT = new FloodProtectorConfig("ClanChatFloodProtector");
			FLOOD_PROTECTOR_ALLY_CHAT = new FloodProtectorConfig("AllyChatFloodProtector");
			
			final PropertiesParser serverSettings = new PropertiesParser(CONFIGURATION_FILE);
			
			GAMESERVER_HOSTNAME = serverSettings.getString("GameserverHostname", "*");
			PORT_GAME = serverSettings.getInt("GameserverPort", 7777);
			
			GAME_SERVER_LOGIN_PORT = serverSettings.getInt("LoginPort", 9014);
			GAME_SERVER_LOGIN_HOST = serverSettings.getString("LoginHost", "127.0.0.1");
			
			REQUEST_ID = serverSettings.getInt("RequestServerID", 0);
			ACCEPT_ALTERNATE_ID = serverSettings.getBoolean("AcceptAlternateID", true);
			
			DATABASE_DRIVER = serverSettings.getString("Driver", "com.mysql.jdbc.Driver");
			DATABASE_URL = serverSettings.getString("URL", "jdbc:mysql://localhost/l2jgs");
			DATABASE_LOGIN = serverSettings.getString("Login", "root");
			DATABASE_PASSWORD = serverSettings.getString("Password", "");
			DATABASE_MAX_CONNECTIONS = serverSettings.getInt("MaximumDbConnections", 10);
			DATABASE_MAX_IDLE_TIME = serverSettings.getInt("MaximumDbIdleTime", 0);
			DATABASE_IDLE_TEST_PERIOD = serverSettings.getInt("IdleConnectionTestPeriod", 60);
			
			try
			{
				DATAPACK_ROOT = new File(serverSettings.getString("DatapackRoot", ".").replaceAll("\\\\", "/")).getCanonicalFile();
			}
			catch (IOException e)
			{
				_log.warn("Error setting datapack root!", e);
				DATAPACK_ROOT = new File(".");
			}
			
			CNAME_TEMPLATE = serverSettings.getString("CnameTemplate", ".*");
			PET_NAME_TEMPLATE = serverSettings.getString("PetNameTemplate", ".*");
			CLAN_NAME_TEMPLATE = serverSettings.getString("ClanNameTemplate", ".*");
			
			MAX_CHARACTERS_NUMBER_PER_ACCOUNT = serverSettings.getInt("CharMaxNumber", 7);
			MAXIMUM_ONLINE_USERS = serverSettings.getInt("MaximumOnlineUsers", 100);
			
			String[] protocols = serverSettings.getString("AllowedProtocolRevisions", "267;268;271;273").split(";");
			PROTOCOL_LIST = new ArrayList<>(protocols.length);
			for (String protocol : protocols)
			{
				try
				{
					PROTOCOL_LIST.add(Integer.parseInt(protocol.trim()));
				}
				catch (NumberFormatException e)
				{
					_log.warn("Wrong config protocol version: " + protocol + ". Skipped.");
				}
			}
			
			// Hosts and Subnets
			IPConfigData ipcd = new IPConfigData();
			GAME_SERVER_SUBNETS = ipcd.getSubnets();
			GAME_SERVER_HOSTS = ipcd.getHosts();
			
			// Load Feature L2Properties file (if exists)
			final PropertiesParser Feature = new PropertiesParser(FEATURE_CONFIG_FILE);
			
			CH_TELE_FEE_RATIO = Feature.getLong("ClanHallTeleportFunctionFeeRatio", 604800000);
			CH_TELE1_FEE = Feature.getInt("ClanHallTeleportFunctionFeeLvl1", 7000);
			CH_TELE2_FEE = Feature.getInt("ClanHallTeleportFunctionFeeLvl2", 14000);
			CH_SUPPORT_FEE_RATIO = Feature.getLong("ClanHallSupportFunctionFeeRatio", 86400000);
			CH_SUPPORT1_FEE = Feature.getInt("ClanHallSupportFeeLvl1", 2500);
			CH_SUPPORT2_FEE = Feature.getInt("ClanHallSupportFeeLvl2", 5000);
			CH_SUPPORT3_FEE = Feature.getInt("ClanHallSupportFeeLvl3", 7000);
			CH_SUPPORT4_FEE = Feature.getInt("ClanHallSupportFeeLvl4", 11000);
			CH_SUPPORT5_FEE = Feature.getInt("ClanHallSupportFeeLvl5", 21000);
			CH_SUPPORT6_FEE = Feature.getInt("ClanHallSupportFeeLvl6", 36000);
			CH_SUPPORT7_FEE = Feature.getInt("ClanHallSupportFeeLvl7", 37000);
			CH_SUPPORT8_FEE = Feature.getInt("ClanHallSupportFeeLvl8", 52000);
			CH_MPREG_FEE_RATIO = Feature.getLong("ClanHallMpRegenerationFunctionFeeRatio", 86400000);
			CH_MPREG1_FEE = Feature.getInt("ClanHallMpRegenerationFeeLvl1", 2000);
			CH_MPREG2_FEE = Feature.getInt("ClanHallMpRegenerationFeeLvl2", 3750);
			CH_MPREG3_FEE = Feature.getInt("ClanHallMpRegenerationFeeLvl3", 6500);
			CH_MPREG4_FEE = Feature.getInt("ClanHallMpRegenerationFeeLvl4", 13750);
			CH_MPREG5_FEE = Feature.getInt("ClanHallMpRegenerationFeeLvl5", 20000);
			CH_HPREG_FEE_RATIO = Feature.getLong("ClanHallHpRegenerationFunctionFeeRatio", 86400000);
			CH_HPREG1_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl1", 700);
			CH_HPREG2_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl2", 800);
			CH_HPREG3_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl3", 1000);
			CH_HPREG4_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl4", 1166);
			CH_HPREG5_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl5", 1500);
			CH_HPREG6_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl6", 1750);
			CH_HPREG7_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl7", 2000);
			CH_HPREG8_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl8", 2250);
			CH_HPREG9_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl9", 2500);
			CH_HPREG10_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl10", 3250);
			CH_HPREG11_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl11", 3270);
			CH_HPREG12_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl12", 4250);
			CH_HPREG13_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl13", 5166);
			CH_EXPREG_FEE_RATIO = Feature.getLong("ClanHallExpRegenerationFunctionFeeRatio", 86400000);
			CH_EXPREG1_FEE = Feature.getInt("ClanHallExpRegenerationFeeLvl1", 3000);
			CH_EXPREG2_FEE = Feature.getInt("ClanHallExpRegenerationFeeLvl2", 6000);
			CH_EXPREG3_FEE = Feature.getInt("ClanHallExpRegenerationFeeLvl3", 9000);
			CH_EXPREG4_FEE = Feature.getInt("ClanHallExpRegenerationFeeLvl4", 15000);
			CH_EXPREG5_FEE = Feature.getInt("ClanHallExpRegenerationFeeLvl5", 21000);
			CH_EXPREG6_FEE = Feature.getInt("ClanHallExpRegenerationFeeLvl6", 23330);
			CH_EXPREG7_FEE = Feature.getInt("ClanHallExpRegenerationFeeLvl7", 30000);
			CH_ITEM_FEE_RATIO = Feature.getLong("ClanHallItemCreationFunctionFeeRatio", 86400000);
			CH_ITEM1_FEE = Feature.getInt("ClanHallItemCreationFunctionFeeLvl1", 30000);
			CH_ITEM2_FEE = Feature.getInt("ClanHallItemCreationFunctionFeeLvl2", 70000);
			CH_ITEM3_FEE = Feature.getInt("ClanHallItemCreationFunctionFeeLvl3", 140000);
			CH_CURTAIN_FEE_RATIO = Feature.getLong("ClanHallCurtainFunctionFeeRatio", 604800000);
			CH_CURTAIN1_FEE = Feature.getInt("ClanHallCurtainFunctionFeeLvl1", 2000);
			CH_CURTAIN2_FEE = Feature.getInt("ClanHallCurtainFunctionFeeLvl2", 2500);
			CH_FRONT_FEE_RATIO = Feature.getLong("ClanHallFrontPlatformFunctionFeeRatio", 259200000);
			CH_FRONT1_FEE = Feature.getInt("ClanHallFrontPlatformFunctionFeeLvl1", 1300);
			CH_FRONT2_FEE = Feature.getInt("ClanHallFrontPlatformFunctionFeeLvl2", 4000);
			CH_BUFF_FREE = Feature.getBoolean("AltClanHallMpBuffFree", false);
			SIEGE_HOUR_LIST = new ArrayList<>();
			for (String hour : Feature.getString("SiegeHourList", "").split(","))
			{
				if (Util.isDigit(hour))
				{
					SIEGE_HOUR_LIST.add(Integer.parseInt(hour));
				}
			}
			CS_TELE_FEE_RATIO = Feature.getLong("CastleTeleportFunctionFeeRatio", 604800000);
			CS_TELE1_FEE = Feature.getInt("CastleTeleportFunctionFeeLvl1", 1000);
			CS_TELE2_FEE = Feature.getInt("CastleTeleportFunctionFeeLvl2", 10000);
			CS_SUPPORT_FEE_RATIO = Feature.getLong("CastleSupportFunctionFeeRatio", 604800000);
			CS_SUPPORT1_FEE = Feature.getInt("CastleSupportFeeLvl1", 49000);
			CS_SUPPORT2_FEE = Feature.getInt("CastleSupportFeeLvl2", 120000);
			CS_MPREG_FEE_RATIO = Feature.getLong("CastleMpRegenerationFunctionFeeRatio", 604800000);
			CS_MPREG1_FEE = Feature.getInt("CastleMpRegenerationFeeLvl1", 45000);
			CS_MPREG2_FEE = Feature.getInt("CastleMpRegenerationFeeLvl2", 65000);
			CS_HPREG_FEE_RATIO = Feature.getLong("CastleHpRegenerationFunctionFeeRatio", 604800000);
			CS_HPREG1_FEE = Feature.getInt("CastleHpRegenerationFeeLvl1", 12000);
			CS_HPREG2_FEE = Feature.getInt("CastleHpRegenerationFeeLvl2", 20000);
			CS_EXPREG_FEE_RATIO = Feature.getLong("CastleExpRegenerationFunctionFeeRatio", 604800000);
			CS_EXPREG1_FEE = Feature.getInt("CastleExpRegenerationFeeLvl1", 63000);
			CS_EXPREG2_FEE = Feature.getInt("CastleExpRegenerationFeeLvl2", 70000);
			
			OUTER_DOOR_UPGRADE_PRICE2 = Feature.getInt("OuterDoorUpgradePriceLvl2", 3000000);
			OUTER_DOOR_UPGRADE_PRICE3 = Feature.getInt("OuterDoorUpgradePriceLvl3", 4000000);
			OUTER_DOOR_UPGRADE_PRICE5 = Feature.getInt("OuterDoorUpgradePriceLvl5", 5000000);
			INNER_DOOR_UPGRADE_PRICE2 = Feature.getInt("InnerDoorUpgradePriceLvl2", 750000);
			INNER_DOOR_UPGRADE_PRICE3 = Feature.getInt("InnerDoorUpgradePriceLvl3", 900000);
			INNER_DOOR_UPGRADE_PRICE5 = Feature.getInt("InnerDoorUpgradePriceLvl5", 1000000);
			WALL_UPGRADE_PRICE2 = Feature.getInt("WallUpgradePriceLvl2", 1600000);
			WALL_UPGRADE_PRICE3 = Feature.getInt("WallUpgradePriceLvl3", 1800000);
			WALL_UPGRADE_PRICE5 = Feature.getInt("WallUpgradePriceLvl5", 2000000);
			TRAP_UPGRADE_PRICE1 = Feature.getInt("TrapUpgradePriceLvl1", 3000000);
			TRAP_UPGRADE_PRICE2 = Feature.getInt("TrapUpgradePriceLvl2", 4000000);
			TRAP_UPGRADE_PRICE3 = Feature.getInt("TrapUpgradePriceLvl3", 5000000);
			TRAP_UPGRADE_PRICE4 = Feature.getInt("TrapUpgradePriceLvl4", 6000000);
			
			FS_TELE_FEE_RATIO = Feature.getLong("FortressTeleportFunctionFeeRatio", 604800000);
			FS_TELE1_FEE = Feature.getInt("FortressTeleportFunctionFeeLvl1", 1000);
			FS_TELE2_FEE = Feature.getInt("FortressTeleportFunctionFeeLvl2", 10000);
			FS_SUPPORT_FEE_RATIO = Feature.getLong("FortressSupportFunctionFeeRatio", 86400000);
			FS_SUPPORT1_FEE = Feature.getInt("FortressSupportFeeLvl1", 7000);
			FS_SUPPORT2_FEE = Feature.getInt("FortressSupportFeeLvl2", 17000);
			FS_MPREG_FEE_RATIO = Feature.getLong("FortressMpRegenerationFunctionFeeRatio", 86400000);
			FS_MPREG1_FEE = Feature.getInt("FortressMpRegenerationFeeLvl1", 6500);
			FS_MPREG2_FEE = Feature.getInt("FortressMpRegenerationFeeLvl2", 9300);
			FS_HPREG_FEE_RATIO = Feature.getLong("FortressHpRegenerationFunctionFeeRatio", 86400000);
			FS_HPREG1_FEE = Feature.getInt("FortressHpRegenerationFeeLvl1", 2000);
			FS_HPREG2_FEE = Feature.getInt("FortressHpRegenerationFeeLvl2", 3500);
			FS_EXPREG_FEE_RATIO = Feature.getLong("FortressExpRegenerationFunctionFeeRatio", 86400000);
			FS_EXPREG1_FEE = Feature.getInt("FortressExpRegenerationFeeLvl1", 9000);
			FS_EXPREG2_FEE = Feature.getInt("FortressExpRegenerationFeeLvl2", 10000);
			FS_UPDATE_FRQ = Feature.getInt("FortressPeriodicUpdateFrequency", 360);
			FS_BLOOD_OATH_COUNT = Feature.getInt("FortressBloodOathCount", 1);
			FS_MAX_SUPPLY_LEVEL = Feature.getInt("FortressMaxSupplyLevel", 6);
			FS_FEE_FOR_CASTLE = Feature.getInt("FortressFeeForCastle", 25000);
			FS_MAX_OWN_TIME = Feature.getInt("FortressMaximumOwnTime", 168);
			
			ALT_GAME_CASTLE_DAWN = Feature.getBoolean("AltCastleForDawn", true);
			ALT_GAME_CASTLE_DUSK = Feature.getBoolean("AltCastleForDusk", true);
			ALT_GAME_REQUIRE_CLAN_CASTLE = Feature.getBoolean("AltRequireClanCastle", false);
			ALT_FESTIVAL_MIN_PLAYER = Feature.getInt("AltFestivalMinPlayer", 5);
			ALT_MAXIMUM_PLAYER_CONTRIB = Feature.getInt("AltMaxPlayerContrib", 1000000);
			ALT_FESTIVAL_MANAGER_START = Feature.getLong("AltFestivalManagerStart", 120000);
			ALT_FESTIVAL_LENGTH = Feature.getLong("AltFestivalLength", 1080000);
			ALT_FESTIVAL_CYCLE_LENGTH = Feature.getLong("AltFestivalCycleLength", 2280000);
			ALT_FESTIVAL_FIRST_SPAWN = Feature.getLong("AltFestivalFirstSpawn", 120000);
			ALT_FESTIVAL_FIRST_SWARM = Feature.getLong("AltFestivalFirstSwarm", 300000);
			ALT_FESTIVAL_SECOND_SPAWN = Feature.getLong("AltFestivalSecondSpawn", 540000);
			ALT_FESTIVAL_SECOND_SWARM = Feature.getLong("AltFestivalSecondSwarm", 720000);
			ALT_FESTIVAL_CHEST_SPAWN = Feature.getLong("AltFestivalChestSpawn", 900000);
			ALT_SIEGE_DAWN_GATES_PDEF_MULT = Feature.getDouble("AltDawnGatesPdefMult", 1.1);
			ALT_SIEGE_DUSK_GATES_PDEF_MULT = Feature.getDouble("AltDuskGatesPdefMult", 0.8);
			ALT_SIEGE_DAWN_GATES_MDEF_MULT = Feature.getDouble("AltDawnGatesMdefMult", 1.1);
			ALT_SIEGE_DUSK_GATES_MDEF_MULT = Feature.getDouble("AltDuskGatesMdefMult", 0.8);
			ALT_STRICT_SEVENSIGNS = Feature.getBoolean("StrictSevenSigns", true);
			ALT_SEVENSIGNS_LAZY_UPDATE = Feature.getBoolean("AltSevenSignsLazyUpdate", true);
			
			SSQ_DAWN_TICKET_QUANTITY = Feature.getInt("SevenSignsDawnTicketQuantity", 300);
			SSQ_DAWN_TICKET_PRICE = Feature.getInt("SevenSignsDawnTicketPrice", 1000);
			SSQ_DAWN_TICKET_BUNDLE = Feature.getInt("SevenSignsDawnTicketBundle", 10);
			SSQ_MANORS_AGREEMENT_ID = Feature.getInt("SevenSignsManorsAgreementId", 6388);
			SSQ_JOIN_DAWN_ADENA_FEE = Feature.getInt("SevenSignsJoinDawnFee", 50000);
			
			TAKE_FORT_POINTS = Feature.getInt("TakeFortPoints", 200);
			LOOSE_FORT_POINTS = Feature.getInt("LooseFortPoints", 0);
			TAKE_CASTLE_POINTS = Feature.getInt("TakeCastlePoints", 1500);
			LOOSE_CASTLE_POINTS = Feature.getInt("LooseCastlePoints", 3000);
			CASTLE_DEFENDED_POINTS = Feature.getInt("CastleDefendedPoints", 750);
			FESTIVAL_WIN_POINTS = Feature.getInt("FestivalOfDarknessWin", 200);
			HERO_POINTS = Feature.getInt("HeroPoints", 1000);
			ROYAL_GUARD_COST = Feature.getInt("CreateRoyalGuardCost", 5000);
			KNIGHT_UNIT_COST = Feature.getInt("CreateKnightUnitCost", 10000);
			KNIGHT_REINFORCE_COST = Feature.getInt("ReinforceKnightUnitCost", 5000);
			BALLISTA_POINTS = Feature.getInt("KillBallistaPoints", 30);
			BLOODALLIANCE_POINTS = Feature.getInt("BloodAlliancePoints", 500);
			BLOODOATH_POINTS = Feature.getInt("BloodOathPoints", 200);
			KNIGHTSEPAULETTE_POINTS = Feature.getInt("KnightsEpaulettePoints", 20);
			REPUTATION_SCORE_PER_KILL = Feature.getInt("ReputationScorePerKill", 1);
			JOIN_ACADEMY_MIN_REP_SCORE = Feature.getInt("CompleteAcademyMinPoints", 190);
			JOIN_ACADEMY_MAX_REP_SCORE = Feature.getInt("CompleteAcademyMaxPoints", 650);
			RAID_RANKING_1ST = Feature.getInt("1stRaidRankingPoints", 1250);
			RAID_RANKING_2ND = Feature.getInt("2ndRaidRankingPoints", 900);
			RAID_RANKING_3RD = Feature.getInt("3rdRaidRankingPoints", 700);
			RAID_RANKING_4TH = Feature.getInt("4thRaidRankingPoints", 600);
			RAID_RANKING_5TH = Feature.getInt("5thRaidRankingPoints", 450);
			RAID_RANKING_6TH = Feature.getInt("6thRaidRankingPoints", 350);
			RAID_RANKING_7TH = Feature.getInt("7thRaidRankingPoints", 300);
			RAID_RANKING_8TH = Feature.getInt("8thRaidRankingPoints", 200);
			RAID_RANKING_9TH = Feature.getInt("9thRaidRankingPoints", 150);
			RAID_RANKING_10TH = Feature.getInt("10thRaidRankingPoints", 100);
			RAID_RANKING_UP_TO_50TH = Feature.getInt("UpTo50thRaidRankingPoints", 25);
			RAID_RANKING_UP_TO_100TH = Feature.getInt("UpTo100thRaidRankingPoints", 12);
			CLAN_LEVEL_6_COST = Feature.getInt("ClanLevel6Cost", 5000);
			CLAN_LEVEL_7_COST = Feature.getInt("ClanLevel7Cost", 10000);
			CLAN_LEVEL_8_COST = Feature.getInt("ClanLevel8Cost", 20000);
			CLAN_LEVEL_9_COST = Feature.getInt("ClanLevel9Cost", 40000);
			CLAN_LEVEL_10_COST = Feature.getInt("ClanLevel10Cost", 40000);
			CLAN_LEVEL_11_COST = Feature.getInt("ClanLevel11Cost", 75000);
			CLAN_LEVEL_6_REQUIREMENT = Feature.getInt("ClanLevel6Requirement", 30);
			CLAN_LEVEL_7_REQUIREMENT = Feature.getInt("ClanLevel7Requirement", 50);
			CLAN_LEVEL_8_REQUIREMENT = Feature.getInt("ClanLevel8Requirement", 80);
			CLAN_LEVEL_9_REQUIREMENT = Feature.getInt("ClanLevel9Requirement", 120);
			CLAN_LEVEL_10_REQUIREMENT = Feature.getInt("ClanLevel10Requirement", 140);
			CLAN_LEVEL_11_REQUIREMENT = Feature.getInt("ClanLevel11Requirement", 170);
			ALLOW_WYVERN_ALWAYS = Feature.getBoolean("AllowRideWyvernAlways", false);
			ALLOW_WYVERN_DURING_SIEGE = Feature.getBoolean("AllowRideWyvernDuringSiege", true);
			
			// Load Character L2Properties file (if exists)
			final PropertiesParser Character = new PropertiesParser(CHARACTER_CONFIG_FILE);
			
			ALT_GAME_DELEVEL = Character.getBoolean("Delevel", true);
			DECREASE_SKILL_LEVEL = Character.getBoolean("DecreaseSkillOnDelevel", true);
			ALT_WEIGHT_LIMIT = Character.getDouble("AltWeightLimit", 1);
			RUN_SPD_BOOST = Character.getInt("RunSpeedBoost", 0);
			DEATH_PENALTY_CHANCE = Character.getInt("DeathPenaltyChance", 20);
			RESPAWN_RESTORE_CP = Character.getDouble("RespawnRestoreCP", 0) / 100;
			RESPAWN_RESTORE_HP = Character.getDouble("RespawnRestoreHP", 65) / 100;
			RESPAWN_RESTORE_MP = Character.getDouble("RespawnRestoreMP", 0) / 100;
			HP_REGEN_MULTIPLIER = Character.getDouble("HpRegenMultiplier", 100) / 100;
			MP_REGEN_MULTIPLIER = Character.getDouble("MpRegenMultiplier", 100) / 100;
			CP_REGEN_MULTIPLIER = Character.getDouble("CpRegenMultiplier", 100) / 100;
			ALT_GAME_TIREDNESS = Character.getBoolean("AltGameTiredness", false);
			ENABLE_MODIFY_SKILL_DURATION = Character.getBoolean("EnableModifySkillDuration", false);
			
			// Create Map only if enabled
			if (ENABLE_MODIFY_SKILL_DURATION)
			{
				String[] propertySplit = Character.getString("SkillDurationList", "").split(";");
				SKILL_DURATION_LIST = new HashMap<>(propertySplit.length);
				for (String skill : propertySplit)
				{
					String[] skillSplit = skill.split(",");
					if (skillSplit.length != 2)
					{
						_log.warn(StringUtil.concat("[SkillDurationList]: invalid config property -> SkillDurationList \"", skill, "\""));
					}
					else
					{
						try
						{
							SKILL_DURATION_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!skill.isEmpty())
							{
								_log.warn(StringUtil.concat("[SkillDurationList]: invalid config property -> SkillList \"", skillSplit[0], "\"", skillSplit[1]));
							}
						}
					}
				}
			}
			ENABLE_MODIFY_SKILL_REUSE = Character.getBoolean("EnableModifySkillReuse", false);
			// Create Map only if enabled
			if (ENABLE_MODIFY_SKILL_REUSE)
			{
				String[] propertySplit = Character.getString("SkillReuseList", "").split(";");
				SKILL_REUSE_LIST = new HashMap<>(propertySplit.length);
				for (String skill : propertySplit)
				{
					String[] skillSplit = skill.split(",");
					if (skillSplit.length != 2)
					{
						_log.warn(StringUtil.concat("[SkillReuseList]: invalid config property -> SkillReuseList \"", skill, "\""));
					}
					else
					{
						try
						{
							SKILL_REUSE_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!skill.isEmpty())
							{
								_log.warn(StringUtil.concat("[SkillReuseList]: invalid config property -> SkillList \"", skillSplit[0], "\"", skillSplit[1]));
							}
						}
					}
				}
			}
			
			AUTO_LEARN_SKILLS = Character.getBoolean("AutoLearnSkills", false);
			AUTO_LEARN_FS_SKILLS = Character.getBoolean("AutoLearnForgottenScrollSkills", false);
			AUTO_LOOT_HERBS = Character.getBoolean("AutoLootHerbs", false);
			BUFFS_MAX_AMOUNT = Character.getByte("MaxBuffAmount", (byte) 20);
			TRIGGERED_BUFFS_MAX_AMOUNT = Character.getByte("MaxTriggeredBuffAmount", (byte) 12);
			DANCES_MAX_AMOUNT = Character.getByte("MaxDanceAmount", (byte) 12);
			DANCE_CANCEL_BUFF = Character.getBoolean("DanceCancelBuff", false);
			DANCE_CONSUME_ADDITIONAL_MP = Character.getBoolean("DanceConsumeAdditionalMP", true);
			ALT_STORE_DANCES = Character.getBoolean("AltStoreDances", false);
			AUTO_LEARN_DIVINE_INSPIRATION = Character.getBoolean("AutoLearnDivineInspiration", false);
			ALT_GAME_CANCEL_BOW = Character.getString("AltGameCancelByHit", "Cast").equalsIgnoreCase("bow") || Character.getString("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
			ALT_GAME_CANCEL_CAST = Character.getString("AltGameCancelByHit", "Cast").equalsIgnoreCase("cast") || Character.getString("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
			EFFECT_CANCELING = Character.getBoolean("CancelLesserEffect", true);
			ALT_GAME_MAGICFAILURES = Character.getBoolean("MagicFailures", true);
			PLAYER_FAKEDEATH_UP_PROTECTION = Character.getInt("PlayerFakeDeathUpProtection", 0);
			STORE_SKILL_COOLTIME = Character.getBoolean("StoreSkillCooltime", true);
			SUBCLASS_STORE_SKILL_COOLTIME = Character.getBoolean("SubclassStoreSkillCooltime", false);
			SUMMON_STORE_SKILL_COOLTIME = Character.getBoolean("SummonStoreSkillCooltime", true);
			ALT_GAME_SHIELD_BLOCKS = Character.getBoolean("AltShieldBlocks", false);
			ALT_PERFECT_SHLD_BLOCK = Character.getInt("AltPerfectShieldBlockRate", 10);
			ALLOW_CLASS_MASTERS = Character.getBoolean("AllowClassMasters", false);
			ALLOW_ENTIRE_TREE = Character.getBoolean("AllowEntireTree", false);
			ALTERNATE_CLASS_MASTER = Character.getBoolean("AlternateClassMaster", false);
			if (ALLOW_CLASS_MASTERS || ALTERNATE_CLASS_MASTER)
			{
				CLASS_MASTER_SETTINGS = new ClassMasterSettings(Character.getString("ConfigClassMaster", ""));
			}
			LIFE_CRYSTAL_NEEDED = Character.getBoolean("LifeCrystalNeeded", true);
			ES_SP_BOOK_NEEDED = Character.getBoolean("EnchantSkillSpBookNeeded", true);
			DIVINE_SP_BOOK_NEEDED = Character.getBoolean("DivineInspirationSpBookNeeded", true);
			ALT_GAME_SKILL_LEARN = Character.getBoolean("AltGameSkillLearn", false);
			ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Character.getBoolean("AltSubClassWithoutQuests", false);
			ALT_GAME_SUBCLASS_EVERYWHERE = Character.getBoolean("AltSubclassEverywhere", false);
			RESTORE_SERVITOR_ON_RECONNECT = Character.getBoolean("RestoreServitorOnReconnect", true);
			RESTORE_PET_ON_RECONNECT = Character.getBoolean("RestorePetOnReconnect", true);
			ALLOW_TRANSFORM_WITHOUT_QUEST = Character.getBoolean("AltTransformationWithoutQuest", false);
			FEE_DELETE_TRANSFER_SKILLS = Character.getInt("FeeDeleteTransferSkills", 10000000);
			FEE_DELETE_SUBCLASS_SKILLS = Character.getInt("FeeDeleteSubClassSkills", 10000000);
			ENABLE_VITALITY = Character.getBoolean("EnableVitality", true);
			RECOVER_VITALITY_ON_RECONNECT = Character.getBoolean("RecoverVitalityOnReconnect", true);
			STARTING_VITALITY_POINTS = Character.getInt("StartingVitalityPoints", 20000);
			MAX_BONUS_EXP = Character.getDouble("MaxExpBonus", 3.5);
			MAX_BONUS_SP = Character.getDouble("MaxSpBonus", 3.5);
			MAX_SUBCLASS = Character.getByte("MaxSubclass", (byte) 3);
			BASE_SUBCLASS_LEVEL = Character.getByte("BaseSubclassLevel", (byte) 40);
			MAX_SUBCLASS_LEVEL = Character.getByte("MaxSubclassLevel", (byte) 80);
			MAX_PVTSTORESELL_SLOTS_DWARF = Character.getInt("MaxPvtStoreSellSlotsDwarf", 4);
			MAX_PVTSTORESELL_SLOTS_OTHER = Character.getInt("MaxPvtStoreSellSlotsOther", 3);
			MAX_PVTSTOREBUY_SLOTS_DWARF = Character.getInt("MaxPvtStoreBuySlotsDwarf", 5);
			MAX_PVTSTOREBUY_SLOTS_OTHER = Character.getInt("MaxPvtStoreBuySlotsOther", 4);
			INVENTORY_MAXIMUM_NO_DWARF = Character.getInt("MaximumSlotsForNoDwarf", 80);
			INVENTORY_MAXIMUM_DWARF = Character.getInt("MaximumSlotsForDwarf", 100);
			INVENTORY_MAXIMUM_GM = Character.getInt("MaximumSlotsForGMPlayer", 250);
			INVENTORY_MAXIMUM_QUEST_ITEMS = Character.getInt("MaximumSlotsForQuestItems", 100);
			MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));
			WAREHOUSE_SLOTS_DWARF = Character.getInt("MaximumWarehouseSlotsForDwarf", 120);
			WAREHOUSE_SLOTS_NO_DWARF = Character.getInt("MaximumWarehouseSlotsForNoDwarf", 100);
			WAREHOUSE_SLOTS_CLAN = Character.getInt("MaximumWarehouseSlotsForClan", 150);
			ALT_FREIGHT_SLOTS = Character.getInt("MaximumFreightSlots", 200);
			ALT_FREIGHT_PRICE = Character.getInt("FreightPrice", 1000);
			MAX_ENCHANT_LEVEL = Character.getInt("MaxEnchantLevel", 0);
			ENCHANT_CHANCE_ELEMENT_STONE = Character.getDouble("EnchantChanceElementStone", 50);
			ENCHANT_CHANCE_ELEMENT_CRYSTAL = Character.getDouble("EnchantChanceElementCrystal", 30);
			ENCHANT_CHANCE_ELEMENT_JEWEL = Character.getDouble("EnchantChanceElementJewel", 20);
			ENCHANT_CHANCE_ELEMENT_ENERGY = Character.getDouble("EnchantChanceElementEnergy", 10);
			ENCHANT_SAFE_MAX = Character.getInt("EnchantSafeMax", 3);
			ENCHANT_SAFE_MAX_FULL = Character.getInt("EnchantSafeMaxFull", 4);
			String[] notenchantable = Character.getString("EnchantBlackList", "7816,7817,7818,7819,7820,7821,7822,7823,7824,7825,7826,7827,7828,7829,7830,7831,13293,13294,13296").split(",");
			ENCHANT_BLACKLIST = new int[notenchantable.length];
			for (int i = 0; i < notenchantable.length; i++)
			{
				ENCHANT_BLACKLIST[i] = Integer.parseInt(notenchantable[i]);
			}
			Arrays.sort(ENCHANT_BLACKLIST);
			
			AUGMENTATION_NG_SKILL_CHANCE = Character.getInt("AugmentationNGSkillChance", 15);
			AUGMENTATION_NG_GLOW_CHANCE = Character.getInt("AugmentationNGGlowChance", 0);
			AUGMENTATION_MID_SKILL_CHANCE = Character.getInt("AugmentationMidSkillChance", 30);
			AUGMENTATION_MID_GLOW_CHANCE = Character.getInt("AugmentationMidGlowChance", 40);
			AUGMENTATION_HIGH_SKILL_CHANCE = Character.getInt("AugmentationHighSkillChance", 45);
			AUGMENTATION_HIGH_GLOW_CHANCE = Character.getInt("AugmentationHighGlowChance", 70);
			AUGMENTATION_TOP_SKILL_CHANCE = Character.getInt("AugmentationTopSkillChance", 60);
			AUGMENTATION_TOP_GLOW_CHANCE = Character.getInt("AugmentationTopGlowChance", 100);
			AUGMENTATION_BASESTAT_CHANCE = Character.getInt("AugmentationBaseStatChance", 1);
			AUGMENTATION_ACC_SKILL_CHANCE = Character.getInt("AugmentationAccSkillChance", 0);
			
			RETAIL_LIKE_AUGMENTATION = Character.getBoolean("RetailLikeAugmentation", true);
			String[] array = Character.getString("RetailLikeAugmentationNoGradeChance", "55,35,7,3").split(",");
			RETAIL_LIKE_AUGMENTATION_NG_CHANCE = new int[array.length];
			for (int i = 0; i < 4; i++)
			{
				RETAIL_LIKE_AUGMENTATION_NG_CHANCE[i] = Integer.parseInt(array[i]);
			}
			array = Character.getString("RetailLikeAugmentationMidGradeChance", "55,35,7,3").split(",");
			RETAIL_LIKE_AUGMENTATION_MID_CHANCE = new int[array.length];
			for (int i = 0; i < 4; i++)
			{
				RETAIL_LIKE_AUGMENTATION_MID_CHANCE[i] = Integer.parseInt(array[i]);
			}
			array = Character.getString("RetailLikeAugmentationHighGradeChance", "55,35,7,3").split(",");
			RETAIL_LIKE_AUGMENTATION_HIGH_CHANCE = new int[array.length];
			for (int i = 0; i < 4; i++)
			{
				RETAIL_LIKE_AUGMENTATION_HIGH_CHANCE[i] = Integer.parseInt(array[i]);
			}
			array = Character.getString("RetailLikeAugmentationTopGradeChance", "55,35,7,3").split(",");
			RETAIL_LIKE_AUGMENTATION_TOP_CHANCE = new int[array.length];
			for (int i = 0; i < 4; i++)
			{
				RETAIL_LIKE_AUGMENTATION_TOP_CHANCE[i] = Integer.parseInt(array[i]);
			}
			RETAIL_LIKE_AUGMENTATION_ACCESSORY = Character.getBoolean("RetailLikeAugmentationAccessory", true);
			
			array = Character.getString("AugmentationBlackList", "6656,6657,6658,6659,6660,6661,6662,8191,10170,10314,13740,13741,13742,13743,13744,13745,13746,13747,13748,14592,14593,14594,14595,14596,14597,14598,14599,14600,14664,14665,14666,14667,14668,14669,14670,14671,14672,14801,14802,14803,14804,14805,14806,14807,14808,14809,15282,15283,15284,15285,15286,15287,15288,15289,15290,15291,15292,15293,15294,15295,15296,15297,15298,15299,16025,16026,21712,22173,22174,22175").split(",");
			AUGMENTATION_BLACKLIST = new int[array.length];
			
			for (int i = 0; i < array.length; i++)
			{
				AUGMENTATION_BLACKLIST[i] = Integer.parseInt(array[i]);
			}
			
			Arrays.sort(AUGMENTATION_BLACKLIST);
			
			ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Character.getBoolean("AltKarmaPlayerCanBeKilledInPeaceZone", false);
			ALT_GAME_KARMA_PLAYER_CAN_SHOP = Character.getBoolean("AltKarmaPlayerCanShop", true);
			ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Character.getBoolean("AltKarmaPlayerCanTeleport", true);
			ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Character.getBoolean("AltKarmaPlayerCanUseGK", false);
			ALT_GAME_KARMA_PLAYER_CAN_TRADE = Character.getBoolean("AltKarmaPlayerCanTrade", true);
			ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Character.getBoolean("AltKarmaPlayerCanUseWareHouse", true);
			MAX_PERSONAL_FAME_POINTS = Character.getInt("MaxPersonalFamePoints", 100000);
			FORTRESS_ZONE_FAME_TASK_FREQUENCY = Character.getInt("FortressZoneFameTaskFrequency", 300);
			FORTRESS_ZONE_FAME_AQUIRE_POINTS = Character.getInt("FortressZoneFameAquirePoints", 31);
			CASTLE_ZONE_FAME_TASK_FREQUENCY = Character.getInt("CastleZoneFameTaskFrequency", 300);
			CASTLE_ZONE_FAME_AQUIRE_POINTS = Character.getInt("CastleZoneFameAquirePoints", 125);
			FAME_FOR_DEAD_PLAYERS = Character.getBoolean("FameForDeadPlayers", true);
			IS_CRAFTING_ENABLED = Character.getBoolean("CraftingEnabled", true);
			CRAFT_MASTERWORK = Character.getBoolean("CraftMasterwork", true);
			DWARF_RECIPE_LIMIT = Character.getInt("DwarfRecipeLimit", 50);
			COMMON_RECIPE_LIMIT = Character.getInt("CommonRecipeLimit", 50);
			ALT_GAME_CREATION = Character.getBoolean("AltGameCreation", false);
			ALT_GAME_CREATION_SPEED = Character.getDouble("AltGameCreationSpeed", 1);
			ALT_GAME_CREATION_XP_RATE = Character.getDouble("AltGameCreationXpRate", 1);
			ALT_GAME_CREATION_SP_RATE = Character.getDouble("AltGameCreationSpRate", 1);
			ALT_GAME_CREATION_RARE_XPSP_RATE = Character.getDouble("AltGameCreationRareXpSpRate", 2);
			ALT_BLACKSMITH_USE_RECIPES = Character.getBoolean("AltBlacksmithUseRecipes", true);
			ALT_CLAN_LEADER_DATE_CHANGE = Character.getInt("AltClanLeaderDateChange", 3);
			if ((ALT_CLAN_LEADER_DATE_CHANGE < 1) || (ALT_CLAN_LEADER_DATE_CHANGE > 7))
			{
				_log.warn("Wrong value specified for AltClanLeaderDateChange: " + ALT_CLAN_LEADER_DATE_CHANGE);
				ALT_CLAN_LEADER_DATE_CHANGE = 3;
			}
			ALT_CLAN_LEADER_HOUR_CHANGE = Character.getString("AltClanLeaderHourChange", "00:00:00");
			ALT_CLAN_LEADER_INSTANT_ACTIVATION = Character.getBoolean("AltClanLeaderInstantActivation", false);
			ALT_CLAN_JOIN_DAYS = Character.getInt("DaysBeforeJoinAClan", 1);
			ALT_CLAN_CREATE_DAYS = Character.getInt("DaysBeforeCreateAClan", 10);
			ALT_CLAN_DISSOLVE_DAYS = Character.getInt("DaysToPassToDissolveAClan", 7);
			ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = Character.getInt("DaysBeforeJoinAllyWhenLeaved", 1);
			ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = Character.getInt("DaysBeforeJoinAllyWhenDismissed", 1);
			ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = Character.getInt("DaysBeforeAcceptNewClanWhenDismissed", 1);
			ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = Character.getInt("DaysBeforeCreateNewAllyWhenDissolved", 1);
			ALT_MAX_NUM_OF_CLANS_IN_ALLY = Character.getInt("AltMaxNumOfClansInAlly", 3);
			ALT_CLAN_MEMBERS_FOR_WAR = Character.getInt("AltClanMembersForWar", 15);
			ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = Character.getBoolean("AltMembersCanWithdrawFromClanWH", false);
			REMOVE_CASTLE_CIRCLETS = Character.getBoolean("RemoveCastleCirclets", true);
			ALT_PARTY_RANGE = Character.getInt("AltPartyRange", 1600);
			ALT_PARTY_RANGE2 = Character.getInt("AltPartyRange2", 1400);
			ALT_LEAVE_PARTY_LEADER = Character.getBoolean("AltLeavePartyLeader", false);
			INITIAL_EQUIPMENT_EVENT = Character.getBoolean("InitialEquipmentEvent", false);
			STARTING_ADENA = Character.getLong("StartingAdena", 0);
			STARTING_LEVEL = Character.getByte("StartingLevel", (byte) 1);
			STARTING_SP = Character.getInt("StartingSP", 0);
			MAX_ADENA = Character.getLong("MaxAdena", 99900000000L);
			if (MAX_ADENA < 0)
			{
				MAX_ADENA = Long.MAX_VALUE;
			}
			AUTO_LOOT = Character.getBoolean("AutoLoot", false);
			AUTO_LOOT_RAIDS = Character.getBoolean("AutoLootRaids", false);
			LOOT_RAIDS_PRIVILEGE_INTERVAL = Character.getInt("RaidLootRightsInterval", 900) * 1000;
			LOOT_RAIDS_PRIVILEGE_CC_SIZE = Character.getInt("RaidLootRightsCCSize", 45);
			UNSTUCK_INTERVAL = Character.getInt("UnstuckInterval", 300);
			TELEPORT_WATCHDOG_TIMEOUT = Character.getInt("TeleportWatchdogTimeout", 0);
			PLAYER_SPAWN_PROTECTION = Character.getInt("PlayerSpawnProtection", 0);
			String[] items = Character.getString("PlayerSpawnProtectionAllowedItems", "0").split(",");
			SPAWN_PROTECTION_ALLOWED_ITEMS = new ArrayList<>(items.length);
			for (String item : items)
			{
				Integer itm = 0;
				try
				{
					itm = Integer.parseInt(item);
				}
				catch (NumberFormatException nfe)
				{
					_log.warn("Player Spawn Protection: Wrong ItemId passed: " + item);
					_log.warn(nfe.getMessage());
				}
				if (itm != 0)
				{
					SPAWN_PROTECTION_ALLOWED_ITEMS.add(itm);
				}
			}
			PLAYER_TELEPORT_PROTECTION = Character.getInt("PlayerTeleportProtection", 0);
			RANDOM_RESPAWN_IN_TOWN_ENABLED = Character.getBoolean("RandomRespawnInTownEnabled", true);
			OFFSET_ON_TELEPORT_ENABLED = Character.getBoolean("OffsetOnTeleportEnabled", true);
			MAX_OFFSET_ON_TELEPORT = Character.getInt("MaxOffsetOnTeleport", 50);
			RESTORE_PLAYER_INSTANCE = Character.getBoolean("RestorePlayerInstance", false);
			ALLOW_SUMMON_TO_INSTANCE = Character.getBoolean("AllowSummonToInstance", true);
			EJECT_DEAD_PLAYER_TIME = 1000 * Character.getInt("EjectDeadPlayerTime", 60);
			PETITIONING_ALLOWED = Character.getBoolean("PetitioningAllowed", true);
			MAX_PETITIONS_PER_PLAYER = Character.getInt("MaxPetitionsPerPlayer", 5);
			MAX_PETITIONS_PENDING = Character.getInt("MaxPetitionsPending", 25);
			ALT_GAME_FREE_TELEPORT = Character.getBoolean("AltFreeTeleporting", false);
			DELETE_DAYS = Character.getInt("DeleteCharAfterDays", 7);
			ALT_GAME_EXPONENT_XP = Character.getFloat("AltGameExponentXp", 0);
			ALT_GAME_EXPONENT_SP = Character.getFloat("AltGameExponentSp", 0);
			PARTY_XP_CUTOFF_METHOD = Character.getString("PartyXpCutoffMethod", "highfive");
			PARTY_XP_CUTOFF_PERCENT = Character.getDouble("PartyXpCutoffPercent", 3);
			PARTY_XP_CUTOFF_LEVEL = Character.getInt("PartyXpCutoffLevel", 20);
			final String[] gaps = Character.getString("PartyXpCutoffGaps", "0,9;10,14;15,99").split(";");
			PARTY_XP_CUTOFF_GAPS = new int[gaps.length][2];
			for (int i = 0; i < gaps.length; i++)
			{
				PARTY_XP_CUTOFF_GAPS[i] = new int[]
				{
					Integer.parseInt(gaps[i].split(",")[0]),
					Integer.parseInt(gaps[i].split(",")[1])
				};
			}
			final String[] percents = Character.getString("PartyXpCutoffGapPercent", "100;30;0").split(";");
			PARTY_XP_CUTOFF_GAP_PERCENTS = new int[percents.length];
			for (int i = 0; i < percents.length; i++)
			{
				PARTY_XP_CUTOFF_GAP_PERCENTS[i] = Integer.parseInt(percents[i]);
			}
			DISABLE_TUTORIAL = Character.getBoolean("DisableTutorial", false);
			EXPERTISE_PENALTY = Character.getBoolean("ExpertisePenalty", true);
			STORE_RECIPE_SHOPLIST = Character.getBoolean("StoreRecipeShopList", false);
			STORE_UI_SETTINGS = Character.getBoolean("StoreCharUiSettings", false);
			FORBIDDEN_NAMES = Character.getString("ForbiddenNames", "").split(",");
			SILENCE_MODE_EXCLUDE = Character.getBoolean("SilenceModeExclude", false);
			ALT_VALIDATE_TRIGGER_SKILLS = Character.getBoolean("AltValidateTriggerSkills", false);
			PLAYER_MOVEMENT_BLOCK_TIME = Character.getInt("NpcTalkBlockingTime", 0) * 1000;
			
			// Load Telnet L2Properties file (if exists)
			final PropertiesParser telnetSettings = new PropertiesParser(TELNET_FILE);
			
			IS_TELNET_ENABLED = telnetSettings.getBoolean("EnableTelnet", false);
			
			// MMO
			final PropertiesParser mmoSettings = new PropertiesParser(MMO_CONFIG_FILE);
			
			MMO_SELECTOR_SLEEP_TIME = mmoSettings.getInt("SleepTime", 20);
			MMO_MAX_SEND_PER_PASS = mmoSettings.getInt("MaxSendPerPass", 12);
			MMO_MAX_READ_PER_PASS = mmoSettings.getInt("MaxReadPerPass", 12);
			MMO_HELPER_BUFFER_COUNT = mmoSettings.getInt("HelperBufferCount", 20);
			MMO_TCP_NODELAY = mmoSettings.getBoolean("TcpNoDelay", false);
			
			// Load IdFactory L2Properties file (if exists)
			final PropertiesParser IdFactory = new PropertiesParser(ID_CONFIG_FILE);
			
			IDFACTORY_TYPE = IdFactory.getEnum("IDFactory", IdFactoryType.class, IdFactoryType.BitSet);
			BAD_ID_CHECKING = IdFactory.getBoolean("BadIdChecking", true);
			
			// Load General L2Properties file (if exists)
			final PropertiesParser General = new PropertiesParser(GENERAL_CONFIG_FILE);
			EVERYBODY_HAS_ADMIN_RIGHTS = General.getBoolean("EverybodyHasAdminRights", false);
			DISPLAY_SERVER_VERSION = General.getBoolean("DisplayServerRevision", true);
			SERVER_LIST_BRACKET = General.getBoolean("ServerListBrackets", false);
			SERVER_LIST_TYPE = getServerTypeId(General.getString("ServerListType", "Normal").split(","));
			SERVER_LIST_AGE = General.getInt("ServerListAge", 0);
			SERVER_GMONLY = General.getBoolean("ServerGMOnly", false);
			GM_HERO_AURA = General.getBoolean("GMHeroAura", false);
			GM_STARTUP_INVULNERABLE = General.getBoolean("GMStartupInvulnerable", false);
			GM_STARTUP_INVISIBLE = General.getBoolean("GMStartupInvisible", false);
			GM_STARTUP_SILENCE = General.getBoolean("GMStartupSilence", false);
			GM_STARTUP_AUTO_LIST = General.getBoolean("GMStartupAutoList", false);
			GM_STARTUP_DIET_MODE = General.getBoolean("GMStartupDietMode", false);
			GM_ITEM_RESTRICTION = General.getBoolean("GMItemRestriction", true);
			GM_SKILL_RESTRICTION = General.getBoolean("GMSkillRestriction", true);
			GM_TRADE_RESTRICTED_ITEMS = General.getBoolean("GMTradeRestrictedItems", false);
			GM_RESTART_FIGHTING = General.getBoolean("GMRestartFighting", true);
			GM_ANNOUNCER_NAME = General.getBoolean("GMShowAnnouncerName", false);
			GM_CRITANNOUNCER_NAME = General.getBoolean("GMShowCritAnnouncerName", false);
			GM_GIVE_SPECIAL_SKILLS = General.getBoolean("GMGiveSpecialSkills", false);
			GM_GIVE_SPECIAL_AURA_SKILLS = General.getBoolean("GMGiveSpecialAuraSkills", false);
			BYPASS_VALIDATION = General.getBoolean("BypassValidation", true);
			GAMEGUARD_ENFORCE = General.getBoolean("GameGuardEnforce", false);
			GAMEGUARD_PROHIBITACTION = General.getBoolean("GameGuardProhibitAction", false);
			LOG_CHAT = General.getBoolean("LogChat", false);
			LOG_AUTO_ANNOUNCEMENTS = General.getBoolean("LogAutoAnnouncements", false);
			LOG_ITEMS = General.getBoolean("LogItems", false);
			LOG_ITEMS_SMALL_LOG = General.getBoolean("LogItemsSmallLog", false);
			LOG_ITEM_ENCHANTS = General.getBoolean("LogItemEnchants", false);
			LOG_SKILL_ENCHANTS = General.getBoolean("LogSkillEnchants", false);
			GMAUDIT = General.getBoolean("GMAudit", false);
			LOG_GAME_DAMAGE = General.getBoolean("LogGameDamage", false);
			LOG_GAME_DAMAGE_THRESHOLD = General.getInt("LogGameDamageThreshold", 5000);
			SKILL_CHECK_ENABLE = General.getBoolean("SkillCheckEnable", false);
			SKILL_CHECK_REMOVE = General.getBoolean("SkillCheckRemove", false);
			SKILL_CHECK_GM = General.getBoolean("SkillCheckGM", true);
			THREAD_P_EFFECTS = General.getInt("ThreadPoolSizeEffects", 10);
			THREAD_P_GENERAL = General.getInt("ThreadPoolSizeGeneral", 13);
			IO_PACKET_THREAD_CORE_SIZE = General.getInt("UrgentPacketThreadCoreSize", 2);
			GENERAL_PACKET_THREAD_CORE_SIZE = General.getInt("GeneralPacketThreadCoreSize", 4);
			GENERAL_THREAD_CORE_SIZE = General.getInt("GeneralThreadCoreSize", 4);
			AI_MAX_THREAD = General.getInt("AiMaxThread", 6);
			CLIENT_PACKET_QUEUE_SIZE = General.getInt("ClientPacketQueueSize", 0);
			if (CLIENT_PACKET_QUEUE_SIZE == 0)
			{
				CLIENT_PACKET_QUEUE_SIZE = MMO_MAX_READ_PER_PASS + 2;
			}
			CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = General.getInt("ClientPacketQueueMaxBurstSize", 0);
			if (CLIENT_PACKET_QUEUE_MAX_BURST_SIZE == 0)
			{
				CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = MMO_MAX_READ_PER_PASS + 1;
			}
			CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND = General.getInt("ClientPacketQueueMaxPacketsPerSecond", 80);
			CLIENT_PACKET_QUEUE_MEASURE_INTERVAL = General.getInt("ClientPacketQueueMeasureInterval", 5);
			CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND = General.getInt("ClientPacketQueueMaxAveragePacketsPerSecond", 40);
			CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN = General.getInt("ClientPacketQueueMaxFloodsPerMin", 2);
			CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN = General.getInt("ClientPacketQueueMaxOverflowsPerMin", 1);
			CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN = General.getInt("ClientPacketQueueMaxUnderflowsPerMin", 1);
			CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN = General.getInt("ClientPacketQueueMaxUnknownPerMin", 5);
			DEADLOCK_DETECTOR = General.getBoolean("DeadLockDetector", true);
			DEADLOCK_CHECK_INTERVAL = General.getInt("DeadLockCheckInterval", 20);
			RESTART_ON_DEADLOCK = General.getBoolean("RestartOnDeadlock", false);
			ALLOW_DISCARDITEM = General.getBoolean("AllowDiscardItem", true);
			AUTODESTROY_ITEM_AFTER = General.getInt("AutoDestroyDroppedItemAfter", 600);
			HERB_AUTO_DESTROY_TIME = General.getInt("AutoDestroyHerbTime", 60) * 1000;
			String[] split = General.getString("ListOfProtectedItems", "0").split(",");
			LIST_PROTECTED_ITEMS = new ArrayList<>(split.length);
			for (String id : split)
			{
				LIST_PROTECTED_ITEMS.add(Integer.parseInt(id));
			}
			DATABASE_CLEAN_UP = General.getBoolean("DatabaseCleanUp", true);
			CONNECTION_CLOSE_TIME = General.getLong("ConnectionCloseTime", 60000);
			CHAR_STORE_INTERVAL = General.getInt("CharacterDataStoreInterval", 15);
			LAZY_ITEMS_UPDATE = General.getBoolean("LazyItemsUpdate", false);
			UPDATE_ITEMS_ON_CHAR_STORE = General.getBoolean("UpdateItemsOnCharStore", false);
			DESTROY_DROPPED_PLAYER_ITEM = General.getBoolean("DestroyPlayerDroppedItem", false);
			DESTROY_EQUIPABLE_PLAYER_ITEM = General.getBoolean("DestroyEquipableItem", false);
			SAVE_DROPPED_ITEM = General.getBoolean("SaveDroppedItem", false);
			EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = General.getBoolean("EmptyDroppedItemTableAfterLoad", false);
			SAVE_DROPPED_ITEM_INTERVAL = General.getInt("SaveDroppedItemInterval", 60) * 60000;
			CLEAR_DROPPED_ITEM_TABLE = General.getBoolean("ClearDroppedItemTable", false);
			AUTODELETE_INVALID_QUEST_DATA = General.getBoolean("AutoDeleteInvalidQuestData", false);
			PRECISE_DROP_CALCULATION = General.getBoolean("PreciseDropCalculation", true);
			MULTIPLE_ITEM_DROP = General.getBoolean("MultipleItemDrop", true);
			FORCE_INVENTORY_UPDATE = General.getBoolean("ForceInventoryUpdate", false);
			LAZY_CACHE = General.getBoolean("LazyCache", true);
			CACHE_CHAR_NAMES = General.getBoolean("CacheCharNames", true);
			MIN_NPC_ANIMATION = General.getInt("MinNPCAnimation", 10);
			MAX_NPC_ANIMATION = General.getInt("MaxNPCAnimation", 20);
			MIN_MONSTER_ANIMATION = General.getInt("MinMonsterAnimation", 5);
			MAX_MONSTER_ANIMATION = General.getInt("MaxMonsterAnimation", 20);
			MOVE_BASED_KNOWNLIST = General.getBoolean("MoveBasedKnownlist", false);
			KNOWNLIST_UPDATE_INTERVAL = General.getLong("KnownListUpdateInterval", 1250);
			GRIDS_ALWAYS_ON = General.getBoolean("GridsAlwaysOn", false);
			GRID_NEIGHBOR_TURNON_TIME = General.getInt("GridNeighborTurnOnTime", 1);
			GRID_NEIGHBOR_TURNOFF_TIME = General.getInt("GridNeighborTurnOffTime", 90);
			
			PEACE_ZONE_MODE = General.getInt("PeaceZoneMode", 0);
			DEFAULT_GLOBAL_CHAT = General.getString("GlobalChat", "ON");
			DEFAULT_TRADE_CHAT = General.getString("TradeChat", "ON");
			ALLOW_WAREHOUSE = General.getBoolean("AllowWarehouse", true);
			WAREHOUSE_CACHE = General.getBoolean("WarehouseCache", false);
			WAREHOUSE_CACHE_TIME = General.getInt("WarehouseCacheTime", 15);
			ALLOW_REFUND = General.getBoolean("AllowRefund", true);
			ALLOW_MAIL = General.getBoolean("AllowMail", true);
			ALLOW_ATTACHMENTS = General.getBoolean("AllowAttachments", true);
			ALLOW_WEAR = General.getBoolean("AllowWear", true);
			WEAR_DELAY = General.getInt("WearDelay", 5);
			WEAR_PRICE = General.getInt("WearPrice", 10);
			ALLOW_LOTTERY = General.getBoolean("AllowLottery", true);
			ALLOW_RACE = General.getBoolean("AllowRace", true);
			ALLOW_WATER = General.getBoolean("AllowWater", true);
			ALLOW_RENTPET = General.getBoolean("AllowRentPet", false);
			ALLOWFISHING = General.getBoolean("AllowFishing", true);
			ALLOW_MANOR = General.getBoolean("AllowManor", true);
			ALLOW_BOAT = General.getBoolean("AllowBoat", true);
			BOAT_BROADCAST_RADIUS = General.getInt("BoatBroadcastRadius", 20000);
			ALLOW_CURSED_WEAPONS = General.getBoolean("AllowCursedWeapons", true);
			ALLOW_PET_WALKERS = General.getBoolean("AllowPetWalkers", true);
			SERVER_NEWS = General.getBoolean("ShowServerNews", false);
			ENABLE_COMMUNITY = General.getBoolean("EnableCommunity", true);
			BBS_DEFAULT = General.getString("BBSDefault", "_bbshome");
			USE_SAY_FILTER = General.getBoolean("UseChatFilter", false);
			CHAT_FILTER_CHARS = General.getString("ChatFilterChars", "^_^");
			String[] propertySplit4 = General.getString("BanChatChannels", "0;1;8;17").trim().split(";");
			BAN_CHAT_CHANNELS = new int[propertySplit4.length];
			try
			{
				int i = 0;
				for (String chatId : propertySplit4)
				{
					BAN_CHAT_CHANNELS[i++] = Integer.parseInt(chatId);
				}
			}
			catch (NumberFormatException nfe)
			{
				_log.warn(nfe.getMessage(), nfe);
			}
			ALT_MANOR_REFRESH_TIME = General.getInt("AltManorRefreshTime", 20);
			ALT_MANOR_REFRESH_MIN = General.getInt("AltManorRefreshMin", 0);
			ALT_MANOR_APPROVE_TIME = General.getInt("AltManorApproveTime", 6);
			ALT_MANOR_APPROVE_MIN = General.getInt("AltManorApproveMin", 0);
			ALT_MANOR_MAINTENANCE_PERIOD = General.getInt("AltManorMaintenancePeriod", 360000);
			ALT_MANOR_SAVE_ALL_ACTIONS = General.getBoolean("AltManorSaveAllActions", false);
			ALT_MANOR_SAVE_PERIOD_RATE = General.getInt("AltManorSavePeriodRate", 2);
			ALT_LOTTERY_PRIZE = General.getLong("AltLotteryPrize", 50000);
			ALT_LOTTERY_TICKET_PRICE = General.getLong("AltLotteryTicketPrice", 2000);
			ALT_LOTTERY_5_NUMBER_RATE = General.getFloat("AltLottery5NumberRate", 0.6f);
			ALT_LOTTERY_4_NUMBER_RATE = General.getFloat("AltLottery4NumberRate", 0.2f);
			ALT_LOTTERY_3_NUMBER_RATE = General.getFloat("AltLottery3NumberRate", 0.2f);
			ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = General.getLong("AltLottery2and1NumberPrize", 200);
			ALT_ITEM_AUCTION_ENABLED = General.getBoolean("AltItemAuctionEnabled", true);
			ALT_ITEM_AUCTION_EXPIRED_AFTER = General.getInt("AltItemAuctionExpiredAfter", 14);
			ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID = General.getInt("AltItemAuctionTimeExtendsOnBid", 0) * 1000;
			FS_TIME_ATTACK = General.getInt("TimeOfAttack", 50);
			FS_TIME_COOLDOWN = General.getInt("TimeOfCoolDown", 5);
			FS_TIME_ENTRY = General.getInt("TimeOfEntry", 3);
			FS_TIME_WARMUP = General.getInt("TimeOfWarmUp", 2);
			FS_PARTY_MEMBER_COUNT = General.getInt("NumberOfNecessaryPartyMembers", 4);
			if (FS_TIME_ATTACK <= 0)
			{
				FS_TIME_ATTACK = 50;
			}
			if (FS_TIME_COOLDOWN <= 0)
			{
				FS_TIME_COOLDOWN = 5;
			}
			if (FS_TIME_ENTRY <= 0)
			{
				FS_TIME_ENTRY = 3;
			}
			if (FS_TIME_ENTRY <= 0)
			{
				FS_TIME_ENTRY = 3;
			}
			if (FS_TIME_ENTRY <= 0)
			{
				FS_TIME_ENTRY = 3;
			}
			RIFT_MIN_PARTY_SIZE = General.getInt("RiftMinPartySize", 5);
			RIFT_MAX_JUMPS = General.getInt("MaxRiftJumps", 4);
			RIFT_SPAWN_DELAY = General.getInt("RiftSpawnDelay", 10000);
			RIFT_AUTO_JUMPS_TIME_MIN = General.getInt("AutoJumpsDelayMin", 480);
			RIFT_AUTO_JUMPS_TIME_MAX = General.getInt("AutoJumpsDelayMax", 600);
			RIFT_BOSS_ROOM_TIME_MUTIPLY = General.getFloat("BossRoomTimeMultiply", 1.5f);
			RIFT_ENTER_COST_RECRUIT = General.getInt("RecruitCost", 18);
			RIFT_ENTER_COST_SOLDIER = General.getInt("SoldierCost", 21);
			RIFT_ENTER_COST_OFFICER = General.getInt("OfficerCost", 24);
			RIFT_ENTER_COST_CAPTAIN = General.getInt("CaptainCost", 27);
			RIFT_ENTER_COST_COMMANDER = General.getInt("CommanderCost", 30);
			RIFT_ENTER_COST_HERO = General.getInt("HeroCost", 33);
			DEFAULT_PUNISH = IllegalActionPunishmentType.findByName(General.getString("DefaultPunish", "KICK"));
			DEFAULT_PUNISH_PARAM = General.getInt("DefaultPunishParam", 0);
			ONLY_GM_ITEMS_FREE = General.getBoolean("OnlyGMItemsFree", true);
			JAIL_IS_PVP = General.getBoolean("JailIsPvp", false);
			JAIL_DISABLE_CHAT = General.getBoolean("JailDisableChat", true);
			JAIL_DISABLE_TRANSACTION = General.getBoolean("JailDisableTransaction", false);
			CUSTOM_SPAWNLIST_TABLE = General.getBoolean("CustomSpawnlistTable", false);
			SAVE_GMSPAWN_ON_CUSTOM = General.getBoolean("SaveGmSpawnOnCustom", false);
			CUSTOM_NPC_TABLE = General.getBoolean("CustomNpcTable", false);
			CUSTOM_NPC_SKILLS_TABLE = General.getBoolean("CustomNpcSkillsTable", false);
			CUSTOM_TELEPORT_TABLE = General.getBoolean("CustomTeleportTable", false);
			CUSTOM_DROPLIST_TABLE = General.getBoolean("CustomDroplistTable", false);
			CUSTOM_SKILLS_LOAD = General.getBoolean("CustomSkillsLoad", false);
			CUSTOM_ITEMS_LOAD = General.getBoolean("CustomItemsLoad", false);
			CUSTOM_MULTISELL_LOAD = General.getBoolean("CustomMultisellLoad", false);
			CUSTOM_BUYLIST_LOAD = General.getBoolean("CustomBuyListLoad", false);
			ALT_BIRTHDAY_GIFT = General.getInt("AltBirthdayGift", 22187);
			ALT_BIRTHDAY_MAIL_SUBJECT = General.getString("AltBirthdayMailSubject", "Happy Birthday!");
			ALT_BIRTHDAY_MAIL_TEXT = General.getString("AltBirthdayMailText", "Hello Adventurer!! Seeing as you're one year older now, I thought I would send you some birthday cheer :) Please find your birthday pack attached. May these gifts bring you joy and happiness on this very special day." + EOL + EOL + "Sincerely, Alegria");
			ENABLE_BLOCK_CHECKER_EVENT = General.getBoolean("EnableBlockCheckerEvent", false);
			MIN_BLOCK_CHECKER_TEAM_MEMBERS = General.getInt("BlockCheckerMinTeamMembers", 2);
			if (MIN_BLOCK_CHECKER_TEAM_MEMBERS < 1)
			{
				MIN_BLOCK_CHECKER_TEAM_MEMBERS = 1;
			}
			else if (MIN_BLOCK_CHECKER_TEAM_MEMBERS > 6)
			{
				MIN_BLOCK_CHECKER_TEAM_MEMBERS = 6;
			}
			HBCE_FAIR_PLAY = General.getBoolean("HBCEFairPlay", false);
			HELLBOUND_WITHOUT_QUEST = General.getBoolean("HellboundWithoutQuest", false);
			
			NORMAL_ENCHANT_COST_MULTIPLIER = General.getInt("NormalEnchantCostMultipiler", 1);
			SAFE_ENCHANT_COST_MULTIPLIER = General.getInt("SafeEnchantCostMultipiler", 5);
			
			BOTREPORT_ENABLE = General.getBoolean("EnableBotReportButton", false);
			BOTREPORT_RESETPOINT_HOUR = General.getString("BotReportPointsResetHour", "00:00").split(":");
			BOTREPORT_REPORT_DELAY = General.getInt("BotReportDelay", 30) * 60000;
			BOTREPORT_ALLOW_REPORTS_FROM_SAME_CLAN_MEMBERS = General.getBoolean("AllowReportsFromSameClanMembers", false);
			
			// Load FloodProtector L2Properties file
			final PropertiesParser FloodProtectors = new PropertiesParser(FLOOD_PROTECTOR_FILE);
			
			loadFloodProtectorConfigs(FloodProtectors);
			
			// Load NPC L2Properties file (if exists)
			final PropertiesParser NPC = new PropertiesParser(NPC_CONFIG_FILE);
			
			ANNOUNCE_MAMMON_SPAWN = NPC.getBoolean("AnnounceMammonSpawn", false);
			ALT_MOB_AGRO_IN_PEACEZONE = NPC.getBoolean("AltMobAgroInPeaceZone", true);
			ALT_ATTACKABLE_NPCS = NPC.getBoolean("AltAttackableNpcs", true);
			ALT_GAME_VIEWNPC = NPC.getBoolean("AltGameViewNpc", false);
			MAX_DRIFT_RANGE = NPC.getInt("MaxDriftRange", 300);
			DEEPBLUE_DROP_RULES = NPC.getBoolean("UseDeepBlueDropRules", true);
			DEEPBLUE_DROP_RULES_RAID = NPC.getBoolean("UseDeepBlueDropRulesRaid", true);
			SHOW_NPC_LVL = NPC.getBoolean("ShowNpcLevel", false);
			SHOW_CREST_WITHOUT_QUEST = NPC.getBoolean("ShowCrestWithoutQuest", false);
			ENABLE_RANDOM_ENCHANT_EFFECT = NPC.getBoolean("EnableRandomEnchantEffect", false);
			MIN_NPC_LVL_DMG_PENALTY = NPC.getInt("MinNPCLevelForDmgPenalty", 78);
			NPC_DMG_PENALTY = parseConfigLine(NPC.getString("DmgPenaltyForLvLDifferences", "0.7, 0.6, 0.6, 0.55"));
			NPC_CRIT_DMG_PENALTY = parseConfigLine(NPC.getString("CritDmgPenaltyForLvLDifferences", "0.75, 0.65, 0.6, 0.58"));
			NPC_SKILL_DMG_PENALTY = parseConfigLine(NPC.getString("SkillDmgPenaltyForLvLDifferences", "0.8, 0.7, 0.65, 0.62"));
			MIN_NPC_LVL_MAGIC_PENALTY = NPC.getInt("MinNPCLevelForMagicPenalty", 78);
			NPC_SKILL_CHANCE_PENALTY = parseConfigLine(NPC.getString("SkillChancePenaltyForLvLDifferences", "2.5, 3.0, 3.25, 3.5"));
			DECAY_TIME_TASK = NPC.getInt("DecayTimeTask", 5000);
			NPC_DECAY_TIME = NPC.getInt("NpcDecayTime", 8500);
			RAID_BOSS_DECAY_TIME = NPC.getInt("RaidBossDecayTime", 30000);
			SPOILED_DECAY_TIME = NPC.getInt("SpoiledDecayTime", 18500);
			MAX_SWEEPER_TIME = NPC.getInt("MaxSweeperTime", 15000);
			ENABLE_DROP_VITALITY_HERBS = NPC.getBoolean("EnableVitalityHerbs", true);
			GUARD_ATTACK_AGGRO_MOB = NPC.getBoolean("GuardAttackAggroMob", false);
			ALLOW_WYVERN_UPGRADER = NPC.getBoolean("AllowWyvernUpgrader", false);
			String[] listPetRentNpc = NPC.getString("ListPetRentNpc", "30827").split(",");
			LIST_PET_RENT_NPC = new ArrayList<>(listPetRentNpc.length);
			for (String id : listPetRentNpc)
			{
				LIST_PET_RENT_NPC.add(Integer.valueOf(id));
			}
			RAID_HP_REGEN_MULTIPLIER = NPC.getDouble("RaidHpRegenMultiplier", 100) / 100;
			RAID_MP_REGEN_MULTIPLIER = NPC.getDouble("RaidMpRegenMultiplier", 100) / 100;
			RAID_PDEFENCE_MULTIPLIER = NPC.getDouble("RaidPDefenceMultiplier", 100) / 100;
			RAID_MDEFENCE_MULTIPLIER = NPC.getDouble("RaidMDefenceMultiplier", 100) / 100;
			RAID_PATTACK_MULTIPLIER = NPC.getDouble("RaidPAttackMultiplier", 100) / 100;
			RAID_MATTACK_MULTIPLIER = NPC.getDouble("RaidMAttackMultiplier", 100) / 100;
			RAID_MIN_RESPAWN_MULTIPLIER = NPC.getFloat("RaidMinRespawnMultiplier", 1.0f);
			RAID_MAX_RESPAWN_MULTIPLIER = NPC.getFloat("RaidMaxRespawnMultiplier", 1.0f);
			RAID_MINION_RESPAWN_TIMER = NPC.getInt("RaidMinionRespawnTime", 300000);
			String[] propertySplit = NPC.getString("CustomMinionsRespawnTime", "").split(";");
			MINIONS_RESPAWN_TIME = new HashMap<>(propertySplit.length);
			for (String prop : propertySplit)
			{
				String[] propSplit = prop.split(",");
				if (propSplit.length != 2)
				{
					_log.warn(StringUtil.concat("[CustomMinionsRespawnTime]: invalid config property -> CustomMinionsRespawnTime \"", prop, "\""));
				}
				
				try
				{
					MINIONS_RESPAWN_TIME.put(Integer.valueOf(propSplit[0]), Integer.valueOf(propSplit[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!prop.isEmpty())
					{
						_log.warn(StringUtil.concat("[CustomMinionsRespawnTime]: invalid config property -> CustomMinionsRespawnTime \"", propSplit[0], "\"", propSplit[1]));
					}
				}
			}
			
			RAID_DISABLE_CURSE = NPC.getBoolean("DisableRaidCurse", false);
			RAID_CHAOS_TIME = NPC.getInt("RaidChaosTime", 10);
			GRAND_CHAOS_TIME = NPC.getInt("GrandChaosTime", 10);
			MINION_CHAOS_TIME = NPC.getInt("MinionChaosTime", 10);
			INVENTORY_MAXIMUM_PET = NPC.getInt("MaximumSlotsForPet", 12);
			PET_HP_REGEN_MULTIPLIER = NPC.getDouble("PetHpRegenMultiplier", 100) / 100;
			PET_MP_REGEN_MULTIPLIER = NPC.getDouble("PetMpRegenMultiplier", 100) / 100;
			
			// Load Rates L2Properties file (if exists)
			final PropertiesParser RatesSettings = new PropertiesParser(RATES_CONFIG_FILE);
			
			RATE_XP = RatesSettings.getFloat("RateXp", 1);
			RATE_SP = RatesSettings.getFloat("RateSp", 1);
			RATE_PARTY_XP = RatesSettings.getFloat("RatePartyXp", 1);
			RATE_PARTY_SP = RatesSettings.getFloat("RatePartySp", 1);
			RATE_CONSUMABLE_COST = RatesSettings.getFloat("RateConsumableCost", 1);
			RATE_EXTRACTABLE = RatesSettings.getFloat("RateExtractable", 1);
			RATE_DROP_ITEMS = RatesSettings.getFloat("RateDropItems", 1);
			RATE_DROP_ITEMS_BY_RAID = RatesSettings.getFloat("RateRaidDropItems", 1);
			RATE_DROP_SPOIL = RatesSettings.getFloat("RateDropSpoil", 1);
			RATE_DROP_MANOR = RatesSettings.getInt("RateDropManor", 1);
			RATE_QUEST_DROP = RatesSettings.getFloat("RateQuestDrop", 1);
			RATE_QUEST_REWARD = RatesSettings.getFloat("RateQuestReward", 1);
			RATE_QUEST_REWARD_XP = RatesSettings.getFloat("RateQuestRewardXP", 1);
			RATE_QUEST_REWARD_SP = RatesSettings.getFloat("RateQuestRewardSP", 1);
			RATE_QUEST_REWARD_ADENA = RatesSettings.getFloat("RateQuestRewardAdena", 1);
			RATE_QUEST_REWARD_USE_MULTIPLIERS = RatesSettings.getBoolean("UseQuestRewardMultipliers", false);
			RATE_QUEST_REWARD_POTION = RatesSettings.getFloat("RateQuestRewardPotion", 1);
			RATE_QUEST_REWARD_SCROLL = RatesSettings.getFloat("RateQuestRewardScroll", 1);
			RATE_QUEST_REWARD_RECIPE = RatesSettings.getFloat("RateQuestRewardRecipe", 1);
			RATE_QUEST_REWARD_MATERIAL = RatesSettings.getFloat("RateQuestRewardMaterial", 1);
			HELLBOUND_ALWAYS_MAX_LEVEL = RatesSettings.getBoolean("HellboundAlwaysMaxLevel", false);
			HELLBOUND_ALWAYS_MAX_TRUST = RatesSettings.getBoolean("HellboundAlwaysMaxTrust", false);
			RATE_HB_TRUST_INCREASE = RatesSettings.getFloat("RateHellboundTrustIncrease", 1);
			RATE_HB_TRUST_DECREASE = RatesSettings.getFloat("RateHellboundTrustDecrease", 1);
			
			RATE_VITALITY_LEVEL_1 = RatesSettings.getFloat("RateVitalityLevel1", 1.5f);
			RATE_VITALITY_LEVEL_2 = RatesSettings.getFloat("RateVitalityLevel2", 2);
			RATE_VITALITY_LEVEL_3 = RatesSettings.getFloat("RateVitalityLevel3", 2.5f);
			RATE_VITALITY_LEVEL_4 = RatesSettings.getFloat("RateVitalityLevel4", 3);
			RATE_RECOVERY_VITALITY_PEACE_ZONE = RatesSettings.getFloat("RateRecoveryPeaceZone", 1);
			RATE_VITALITY_LOST = RatesSettings.getFloat("RateVitalityLost", 1);
			RATE_VITALITY_GAIN = RatesSettings.getFloat("RateVitalityGain", 1);
			RATE_RECOVERY_ON_RECONNECT = RatesSettings.getFloat("RateRecoveryOnReconnect", 4);
			RATE_KARMA_LOST = RatesSettings.getFloat("RateKarmaLost", -1);
			if (RATE_KARMA_LOST == -1)
			{
				RATE_KARMA_LOST = RATE_XP;
			}
			RATE_KARMA_EXP_LOST = RatesSettings.getFloat("RateKarmaExpLost", 1);
			RATE_SIEGE_GUARDS_PRICE = RatesSettings.getFloat("RateSiegeGuardsPrice", 1);
			RATE_DROP_COMMON_HERBS = RatesSettings.getFloat("RateCommonHerbs", 1);
			RATE_DROP_HP_HERBS = RatesSettings.getFloat("RateHpHerbs", 1);
			RATE_DROP_MP_HERBS = RatesSettings.getFloat("RateMpHerbs", 1);
			RATE_DROP_SPECIAL_HERBS = RatesSettings.getFloat("RateSpecialHerbs", 1);
			RATE_DROP_VITALITY_HERBS = RatesSettings.getFloat("RateVitalityHerbs", 1);
			PLAYER_DROP_LIMIT = RatesSettings.getInt("PlayerDropLimit", 3);
			PLAYER_RATE_DROP = RatesSettings.getInt("PlayerRateDrop", 5);
			PLAYER_RATE_DROP_ITEM = RatesSettings.getInt("PlayerRateDropItem", 70);
			PLAYER_RATE_DROP_EQUIP = RatesSettings.getInt("PlayerRateDropEquip", 25);
			PLAYER_RATE_DROP_EQUIP_WEAPON = RatesSettings.getInt("PlayerRateDropEquipWeapon", 5);
			PET_XP_RATE = RatesSettings.getFloat("PetXpRate", 1);
			PET_FOOD_RATE = RatesSettings.getInt("PetFoodRate", 1);
			SINEATER_XP_RATE = RatesSettings.getFloat("SinEaterXpRate", 1);
			KARMA_DROP_LIMIT = RatesSettings.getInt("KarmaDropLimit", 10);
			KARMA_RATE_DROP = RatesSettings.getInt("KarmaRateDrop", 70);
			KARMA_RATE_DROP_ITEM = RatesSettings.getInt("KarmaRateDropItem", 50);
			KARMA_RATE_DROP_EQUIP = RatesSettings.getInt("KarmaRateDropEquip", 40);
			KARMA_RATE_DROP_EQUIP_WEAPON = RatesSettings.getInt("KarmaRateDropEquipWeapon", 10);
			
			// Initializing table
			PLAYER_XP_PERCENT_LOST = new double[Byte.MAX_VALUE + 1];
			
			// Default value
			for (int i = 0; i <= Byte.MAX_VALUE; i++)
			{
				PLAYER_XP_PERCENT_LOST[i] = 1.;
			}
			
			// Now loading into table parsed values
			try
			{
				String[] values = RatesSettings.getString("PlayerXPPercentLost", "0,39-7.0;40,75-4.0;76,76-2.5;77,77-2.0;78,78-1.5").split(";");
				
				for (String s : values)
				{
					int min;
					int max;
					double val;
					
					String[] vals = s.split("-");
					String[] mM = vals[0].split(",");
					
					min = Integer.parseInt(mM[0]);
					max = Integer.parseInt(mM[1]);
					val = Double.parseDouble(vals[1]);
					
					for (int i = min; i <= max; i++)
					{
						PLAYER_XP_PERCENT_LOST[i] = val;
					}
				}
			}
			catch (Exception e)
			{
				_log.warn("Error while loading Player XP percent lost!", e);
			}
			
			String[] rateDropItemsById = RatesSettings.getString("RateDropItemsById", "").split(";");
			RATE_DROP_ITEMS_ID = new HashMap<>(rateDropItemsById.length);
			if (!rateDropItemsById[0].isEmpty())
			{
				for (String item : rateDropItemsById)
				{
					String[] itemSplit = item.split(",");
					if (itemSplit.length != 2)
					{
						_log.warn(StringUtil.concat("Config.load(): invalid config property -> RateDropItemsById \"", item, "\""));
					}
					else
					{
						try
						{
							RATE_DROP_ITEMS_ID.put(Integer.valueOf(itemSplit[0]), Float.valueOf(itemSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!item.isEmpty())
							{
								_log.warn(StringUtil.concat("Config.load(): invalid config property -> RateDropItemsById \"", item, "\""));
							}
						}
					}
				}
			}
			if (!RATE_DROP_ITEMS_ID.containsKey(Inventory.ADENA_ID))
			{
				RATE_DROP_ITEMS_ID.put(Inventory.ADENA_ID, RATE_DROP_ITEMS); // for Adena rate if not defined
			}
			
			// Load L2JMod L2Properties file (if exists)
			final PropertiesParser L2JModSettings = new PropertiesParser(L2JMOD_CONFIG_FILE);
			
			L2JMOD_CHAMPION_ENABLE = L2JModSettings.getBoolean("ChampionEnable", false);
			L2JMOD_CHAMPION_PASSIVE = L2JModSettings.getBoolean("ChampionPassive", false);
			L2JMOD_CHAMPION_FREQUENCY = L2JModSettings.getInt("ChampionFrequency", 0);
			L2JMOD_CHAMP_TITLE = L2JModSettings.getString("ChampionTitle", "Champion");
			L2JMOD_CHAMP_MIN_LVL = L2JModSettings.getInt("ChampionMinLevel", 20);
			L2JMOD_CHAMP_MAX_LVL = L2JModSettings.getInt("ChampionMaxLevel", 60);
			L2JMOD_CHAMPION_HP = L2JModSettings.getInt("ChampionHp", 7);
			L2JMOD_CHAMPION_HP_REGEN = L2JModSettings.getFloat("ChampionHpRegen", 1);
			L2JMOD_CHAMPION_REWARDS = L2JModSettings.getInt("ChampionRewards", 8);
			L2JMOD_CHAMPION_ADENAS_REWARDS = L2JModSettings.getFloat("ChampionAdenasRewards", 1);
			L2JMOD_CHAMPION_ATK = L2JModSettings.getFloat("ChampionAtk", 1);
			L2JMOD_CHAMPION_SPD_ATK = L2JModSettings.getFloat("ChampionSpdAtk", 1);
			L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE = L2JModSettings.getInt("ChampionRewardLowerLvlItemChance", 0);
			L2JMOD_CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE = L2JModSettings.getInt("ChampionRewardHigherLvlItemChance", 0);
			L2JMOD_CHAMPION_REWARD_ID = L2JModSettings.getInt("ChampionRewardItemID", 6393);
			L2JMOD_CHAMPION_REWARD_QTY = L2JModSettings.getInt("ChampionRewardItemQty", 1);
			L2JMOD_CHAMPION_ENABLE_VITALITY = L2JModSettings.getBoolean("ChampionEnableVitality", false);
			L2JMOD_CHAMPION_ENABLE_IN_INSTANCES = L2JModSettings.getBoolean("ChampionEnableInInstances", false);
			
			L2JMOD_ALLOW_WEDDING = L2JModSettings.getBoolean("AllowWedding", false);
			L2JMOD_WEDDING_PRICE = L2JModSettings.getInt("WeddingPrice", 250000000);
			L2JMOD_WEDDING_PUNISH_INFIDELITY = L2JModSettings.getBoolean("WeddingPunishInfidelity", true);
			L2JMOD_WEDDING_TELEPORT = L2JModSettings.getBoolean("WeddingTeleport", true);
			L2JMOD_WEDDING_TELEPORT_PRICE = L2JModSettings.getInt("WeddingTeleportPrice", 50000);
			L2JMOD_WEDDING_TELEPORT_DURATION = L2JModSettings.getInt("WeddingTeleportDuration", 60);
			L2JMOD_WEDDING_SAMESEX = L2JModSettings.getBoolean("WeddingAllowSameSex", false);
			L2JMOD_WEDDING_FORMALWEAR = L2JModSettings.getBoolean("WeddingFormalWear", true);
			L2JMOD_WEDDING_DIVORCE_COSTS = L2JModSettings.getInt("WeddingDivorceCosts", 20);
			
			L2JMOD_ENABLE_WAREHOUSESORTING_CLAN = L2JModSettings.getBoolean("EnableWarehouseSortingClan", false);
			L2JMOD_ENABLE_WAREHOUSESORTING_PRIVATE = L2JModSettings.getBoolean("EnableWarehouseSortingPrivate", false);
			
			BANKING_SYSTEM_ENABLED = L2JModSettings.getBoolean("BankingEnabled", false);
			BANKING_SYSTEM_GOLDBARS = L2JModSettings.getInt("BankingGoldbarCount", 1);
			BANKING_SYSTEM_ADENA = L2JModSettings.getInt("BankingAdenaCount", 500000000);
			
			OFFLINE_TRADE_ENABLE = L2JModSettings.getBoolean("OfflineTradeEnable", false);
			OFFLINE_CRAFT_ENABLE = L2JModSettings.getBoolean("OfflineCraftEnable", false);
			OFFLINE_MODE_IN_PEACE_ZONE = L2JModSettings.getBoolean("OfflineModeInPaceZone", false);
			OFFLINE_MODE_NO_DAMAGE = L2JModSettings.getBoolean("OfflineModeNoDamage", false);
			OFFLINE_SET_NAME_COLOR = L2JModSettings.getBoolean("OfflineSetNameColor", false);
			OFFLINE_NAME_COLOR = Integer.decode("0x" + L2JModSettings.getString("OfflineNameColor", "808080"));
			OFFLINE_FAME = L2JModSettings.getBoolean("OfflineFame", true);
			RESTORE_OFFLINERS = L2JModSettings.getBoolean("RestoreOffliners", false);
			OFFLINE_MAX_DAYS = L2JModSettings.getInt("OfflineMaxDays", 10);
			OFFLINE_DISCONNECT_FINISHED = L2JModSettings.getBoolean("OfflineDisconnectFinished", true);
			
			L2JMOD_ENABLE_MANA_POTIONS_SUPPORT = L2JModSettings.getBoolean("EnableManaPotionSupport", false);
			
			L2JMOD_DISPLAY_SERVER_TIME = L2JModSettings.getBoolean("DisplayServerTime", false);
			
			WELCOME_MESSAGE_ENABLED = L2JModSettings.getBoolean("ScreenWelcomeMessageEnable", false);
			WELCOME_MESSAGE_TEXT = L2JModSettings.getString("ScreenWelcomeMessageText", "Welcome to L2J server!");
			WELCOME_MESSAGE_TIME = L2JModSettings.getInt("ScreenWelcomeMessageTime", 10) * 1000;
			
			L2JMOD_ANTIFEED_ENABLE = L2JModSettings.getBoolean("AntiFeedEnable", false);
			L2JMOD_ANTIFEED_DUALBOX = L2JModSettings.getBoolean("AntiFeedDualbox", true);
			L2JMOD_ANTIFEED_DISCONNECTED_AS_DUALBOX = L2JModSettings.getBoolean("AntiFeedDisconnectedAsDualbox", true);
			L2JMOD_ANTIFEED_INTERVAL = L2JModSettings.getInt("AntiFeedInterval", 120) * 1000;
			ANNOUNCE_PK_PVP = L2JModSettings.getBoolean("AnnouncePkPvP", false);
			ANNOUNCE_PK_PVP_NORMAL_MESSAGE = L2JModSettings.getBoolean("AnnouncePkPvPNormalMessage", true);
			ANNOUNCE_PK_MSG = L2JModSettings.getString("AnnouncePkMsg", "$killer has slaughtered $target");
			ANNOUNCE_PVP_MSG = L2JModSettings.getString("AnnouncePvpMsg", "$killer has defeated $target");
			
			L2JMOD_CHAT_ADMIN = L2JModSettings.getBoolean("ChatAdmin", false);
			
			L2JMOD_MULTILANG_DEFAULT = L2JModSettings.getString("MultiLangDefault", "en");
			L2JMOD_MULTILANG_ENABLE = L2JModSettings.getBoolean("MultiLangEnable", false);
			String[] allowed = L2JModSettings.getString("MultiLangAllowed", L2JMOD_MULTILANG_DEFAULT).split(";");
			L2JMOD_MULTILANG_ALLOWED = new ArrayList<>(allowed.length);
			for (String lang : allowed)
			{
				L2JMOD_MULTILANG_ALLOWED.add(lang);
			}
			
			if (!L2JMOD_MULTILANG_ALLOWED.contains(L2JMOD_MULTILANG_DEFAULT))
			{
				_log.warn("MultiLang[Config.load()]: default language: " + L2JMOD_MULTILANG_DEFAULT + " is not in allowed list !");
			}
			
			L2JMOD_HELLBOUND_STATUS = L2JModSettings.getBoolean("HellboundStatus", false);
			L2JMOD_MULTILANG_VOICED_ALLOW = L2JModSettings.getBoolean("MultiLangVoiceCommand", true);
			L2JMOD_MULTILANG_SM_ENABLE = L2JModSettings.getBoolean("MultiLangSystemMessageEnable", false);
			allowed = L2JModSettings.getString("MultiLangSystemMessageAllowed", "").split(";");
			L2JMOD_MULTILANG_SM_ALLOWED = new ArrayList<>(allowed.length);
			for (String lang : allowed)
			{
				if (!lang.isEmpty())
				{
					L2JMOD_MULTILANG_SM_ALLOWED.add(lang);
				}
			}
			L2JMOD_MULTILANG_NS_ENABLE = L2JModSettings.getBoolean("MultiLangNpcStringEnable", false);
			allowed = L2JModSettings.getString("MultiLangNpcStringAllowed", "").split(";");
			L2JMOD_MULTILANG_NS_ALLOWED = new ArrayList<>(allowed.length);
			for (String lang : allowed)
			{
				if (!lang.isEmpty())
				{
					L2JMOD_MULTILANG_NS_ALLOWED.add(lang);
				}
			}
			
			L2WALKER_PROTECTION = L2JModSettings.getBoolean("L2WalkerProtection", false);
			L2JMOD_DEBUG_VOICE_COMMAND = L2JModSettings.getBoolean("DebugVoiceCommand", false);
			
			L2JMOD_DUALBOX_CHECK_MAX_PLAYERS_PER_IP = L2JModSettings.getInt("DualboxCheckMaxPlayersPerIP", 0);
			L2JMOD_DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP = L2JModSettings.getInt("DualboxCheckMaxOlympiadParticipantsPerIP", 0);
			L2JMOD_DUALBOX_CHECK_MAX_L2EVENT_PARTICIPANTS_PER_IP = L2JModSettings.getInt("DualboxCheckMaxL2EventParticipantsPerIP", 0);
			String[] dualboxCheckWhiteList = L2JModSettings.getString("DualboxCheckWhitelist", "127.0.0.1,0").split(";");
			L2JMOD_DUALBOX_CHECK_WHITELIST = new HashMap<>(dualboxCheckWhiteList.length);
			for (String entry : dualboxCheckWhiteList)
			{
				String[] entrySplit = entry.split(",");
				if (entrySplit.length != 2)
				{
					_log.warn(StringUtil.concat("DualboxCheck[Config.load()]: invalid config property -> DualboxCheckWhitelist \"", entry, "\""));
				}
				else
				{
					try
					{
						int num = Integer.parseInt(entrySplit[1]);
						num = num == 0 ? -1 : num;
						L2JMOD_DUALBOX_CHECK_WHITELIST.put(InetAddress.getByName(entrySplit[0]).hashCode(), num);
					}
					catch (UnknownHostException e)
					{
						_log.warn(StringUtil.concat("DualboxCheck[Config.load()]: invalid address -> DualboxCheckWhitelist \"", entrySplit[0], "\""));
					}
					catch (NumberFormatException e)
					{
						_log.warn(StringUtil.concat("DualboxCheck[Config.load()]: invalid number -> DualboxCheckWhitelist \"", entrySplit[1], "\""));
					}
				}
			}
			L2JMOD_ALLOW_CHANGE_PASSWORD = L2JModSettings.getBoolean("AllowChangePassword", false);
			
			// Load PvP L2Properties file (if exists)
			final PropertiesParser PVPSettings = new PropertiesParser(PVP_CONFIG_FILE);
			
			KARMA_DROP_GM = PVPSettings.getBoolean("CanGMDropEquipment", false);
			KARMA_AWARD_PK_KILL = PVPSettings.getBoolean("AwardPKKillPVPPoint", false);
			KARMA_PK_LIMIT = PVPSettings.getInt("MinimumPKRequiredToDrop", 5);
			KARMA_NONDROPPABLE_PET_ITEMS = PVPSettings.getString("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650,9882");
			KARMA_NONDROPPABLE_ITEMS = PVPSettings.getString("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369,6842,6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,7694,8181,5575,7694,9388,9389,9390");
			
			String[] karma = KARMA_NONDROPPABLE_PET_ITEMS.split(",");
			KARMA_LIST_NONDROPPABLE_PET_ITEMS = new int[karma.length];
			
			for (int i = 0; i < karma.length; i++)
			{
				KARMA_LIST_NONDROPPABLE_PET_ITEMS[i] = Integer.parseInt(karma[i]);
			}
			
			karma = KARMA_NONDROPPABLE_ITEMS.split(",");
			KARMA_LIST_NONDROPPABLE_ITEMS = new int[karma.length];
			
			for (int i = 0; i < karma.length; i++)
			{
				KARMA_LIST_NONDROPPABLE_ITEMS[i] = Integer.parseInt(karma[i]);
			}
			
			// sorting so binarySearch can be used later
			Arrays.sort(KARMA_LIST_NONDROPPABLE_PET_ITEMS);
			Arrays.sort(KARMA_LIST_NONDROPPABLE_ITEMS);
			
			PVP_NORMAL_TIME = PVPSettings.getInt("PvPVsNormalTime", 120000);
			PVP_PVP_TIME = PVPSettings.getInt("PvPVsPvPTime", 60000);
			
			// Load Olympiad L2Properties file (if exists)
			final PropertiesParser Olympiad = new PropertiesParser(OLYMPIAD_CONFIG_FILE);
			
			ALT_OLY_START_TIME = Olympiad.getInt("AltOlyStartTime", 18);
			ALT_OLY_MIN = Olympiad.getInt("AltOlyMin", 0);
			ALT_OLY_CPERIOD = Olympiad.getLong("AltOlyCPeriod", 21600000);
			ALT_OLY_BATTLE = Olympiad.getLong("AltOlyBattle", 300000);
			ALT_OLY_WPERIOD = Olympiad.getLong("AltOlyWPeriod", 604800000);
			ALT_OLY_VPERIOD = Olympiad.getLong("AltOlyVPeriod", 86400000);
			ALT_OLY_START_POINTS = Olympiad.getInt("AltOlyStartPoints", 10);
			ALT_OLY_WEEKLY_POINTS = Olympiad.getInt("AltOlyWeeklyPoints", 10);
			ALT_OLY_CLASSED = Olympiad.getInt("AltOlyClassedParticipants", 11);
			ALT_OLY_NONCLASSED = Olympiad.getInt("AltOlyNonClassedParticipants", 11);
			ALT_OLY_TEAMS = Olympiad.getInt("AltOlyTeamsParticipants", 6);
			ALT_OLY_REG_DISPLAY = Olympiad.getInt("AltOlyRegistrationDisplayNumber", 100);
			ALT_OLY_CLASSED_REWARD = parseItemsList(Olympiad.getString("AltOlyClassedReward", "13722,50"));
			ALT_OLY_NONCLASSED_REWARD = parseItemsList(Olympiad.getString("AltOlyNonClassedReward", "13722,40"));
			ALT_OLY_TEAM_REWARD = parseItemsList(Olympiad.getString("AltOlyTeamReward", "13722,85"));
			ALT_OLY_COMP_RITEM = Olympiad.getInt("AltOlyCompRewItem", 13722);
			ALT_OLY_MIN_MATCHES = Olympiad.getInt("AltOlyMinMatchesForPoints", 15);
			ALT_OLY_GP_PER_POINT = Olympiad.getInt("AltOlyGPPerPoint", 1000);
			ALT_OLY_HERO_POINTS = Olympiad.getInt("AltOlyHeroPoints", 200);
			ALT_OLY_RANK1_POINTS = Olympiad.getInt("AltOlyRank1Points", 100);
			ALT_OLY_RANK2_POINTS = Olympiad.getInt("AltOlyRank2Points", 75);
			ALT_OLY_RANK3_POINTS = Olympiad.getInt("AltOlyRank3Points", 55);
			ALT_OLY_RANK4_POINTS = Olympiad.getInt("AltOlyRank4Points", 40);
			ALT_OLY_RANK5_POINTS = Olympiad.getInt("AltOlyRank5Points", 30);
			ALT_OLY_MAX_POINTS = Olympiad.getInt("AltOlyMaxPoints", 10);
			ALT_OLY_DIVIDER_CLASSED = Olympiad.getInt("AltOlyDividerClassed", 5);
			ALT_OLY_DIVIDER_NON_CLASSED = Olympiad.getInt("AltOlyDividerNonClassed", 5);
			ALT_OLY_MAX_WEEKLY_MATCHES = Olympiad.getInt("AltOlyMaxWeeklyMatches", 70);
			ALT_OLY_MAX_WEEKLY_MATCHES_NON_CLASSED = Olympiad.getInt("AltOlyMaxWeeklyMatchesNonClassed", 60);
			ALT_OLY_MAX_WEEKLY_MATCHES_CLASSED = Olympiad.getInt("AltOlyMaxWeeklyMatchesClassed", 30);
			ALT_OLY_MAX_WEEKLY_MATCHES_TEAM = Olympiad.getInt("AltOlyMaxWeeklyMatchesTeam", 10);
			ALT_OLY_LOG_FIGHTS = Olympiad.getBoolean("AltOlyLogFights", false);
			ALT_OLY_SHOW_MONTHLY_WINNERS = Olympiad.getBoolean("AltOlyShowMonthlyWinners", true);
			ALT_OLY_ANNOUNCE_GAMES = Olympiad.getBoolean("AltOlyAnnounceGames", true);
			String[] olyRestrictedItems = Olympiad.getString("AltOlyRestrictedItems", "6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,9388,9389,9390,17049,17050,17051,17052,17053,17054,17055,17056,17057,17058,17059,17060,17061,20759,20775,20776,20777,20778,14774").split(",");
			LIST_OLY_RESTRICTED_ITEMS = new ArrayList<>(olyRestrictedItems.length);
			for (String id : olyRestrictedItems)
			{
				LIST_OLY_RESTRICTED_ITEMS.add(Integer.parseInt(id));
			}
			ALT_OLY_ENCHANT_LIMIT = Olympiad.getInt("AltOlyEnchantLimit", -1);
			ALT_OLY_WAIT_TIME = Olympiad.getInt("AltOlyWaitTime", 120);
			
			// More olympiad settings
			ENABLE_OLY_ANTIFEED = Olympiad.getBoolean("EnableOlyAntifeed", false);
			ENABLE_REUNION_PERIOD = Olympiad.getBoolean("EnableReunionPeriod", false);
			propertySplit = Olympiad.getString("AltOlyEndDate", "1").split(",");
			ALT_OLY_END_DATE = new int[propertySplit.length];
			for (int i = 0; i < propertySplit.length; i++)
			{
				ALT_OLY_END_DATE[i] = Integer.parseInt(propertySplit[i]);
			}
			OLYMPIAD_PERIOD = Olympiad.getString("CustomOlyPeriod", "MONTH");
			ENABLE_OLYMPIAD = Olympiad.getBoolean("EnableOlympiad", true);
			String[] times = Olympiad.getString("AltOlyEndHour", "12:00:00").split(":");
			for (int i = 0; i < 3; i++)
			{
				ALT_OLY_END_HOUR[i] = Integer.parseInt(times[i]);
			}
			
			// Load Debug L2Properties file (if exists)
			final PropertiesParser Debug = new PropertiesParser(DEBUG_CONFIG_FILE);
			
			DEBUG = Debug.getBoolean("Debug", false);
			DEBUG_SKILL_PARSING = Debug.getBoolean("DebugSkillParsing", false);
			DEBUG_SKILL_DURATION = Debug.getBoolean("DebugSkillDuration", false);
			DEBUG_MULTIPLE_REGISTRATIONS_OF_NPCS = Debug.getBoolean("DebugMultipleRegistrationsOfNpcs", false);
			DEBUG_SCRIPT_NOTIFIES = Debug.getBoolean("DebugScriptsNotifies", false);
			PACKET_HANDLER_DEBUG = Debug.getBoolean("PacketHandlerDebug", false);
			DEVELOPER = Debug.getBoolean("Developer", false);
			ACCEPT_GEOEDITOR_CONN = Debug.getBoolean("AcceptGeoeditorConn", false);
			ALT_DEV_NO_HANDLERS = Debug.getBoolean("AltDevNoHandlers", false);
			ALT_DEV_NO_QUESTS = Debug.getBoolean("AltDevNoQuests", false);
			ALT_DEV_NO_SPAWNS = Debug.getBoolean("AltDevNoSpawns", false);
			ALT_DEV_SHOW_QUESTS_LOAD_IN_LOGS = Debug.getBoolean("AltDevShowQuestsLoadInLogs", false);
			ALT_DEV_SHOW_SCRIPTS_LOAD_IN_LOGS = Debug.getBoolean("AltDevShowScriptsLoadInLogs", false);
			
			// Load General L2Properties file (if exists)
			final PropertiesParser Geodata = new PropertiesParser(GEODATA_CONFIG_FILE);
			
			GEODATA = Geodata.getInt("GeoData", 0);
			GEODATA_DRIVER = Geodata.getString("GeoDataDriver", "l2r.gameserver.geoengine.NullDriver");
			PATHFIND_BUFFERS = Geodata.getString("PathFindBuffers", "100x6;128x6;192x6;256x4;320x4;384x4;500x2");
			LOW_WEIGHT = Geodata.getFloat("LowWeight", 0.5f);
			MEDIUM_WEIGHT = Geodata.getFloat("MediumWeight", 2);
			HIGH_WEIGHT = Geodata.getFloat("HighWeight", 3);
			ADVANCED_DIAGONAL_STRATEGY = Geodata.getBoolean("AdvancedDiagonalStrategy", true);
			DIAGONAL_WEIGHT = Geodata.getFloat("DiagonalWeight", 0.707f);
			MAX_POSTFILTER_PASSES = Geodata.getInt("MaxPostfilterPasses", 3);
			DEBUG_PATH = Geodata.getBoolean("DebugPath", false);
			FORCE_GEODATA = Geodata.getBoolean("ForceGeodata", true);
			COORD_SYNCHRONIZE = Geodata.getInt("CoordSynchronize", -1);
			String str = Geodata.getString("EnableFallingDamage", "auto");
			ENABLE_FALLING_DAMAGE = "auto".equalsIgnoreCase(str) ? GEODATA > 0 : Boolean.parseBoolean(str);
			
			final File hexIdFile = new File(HEXID_FILE);
			if (hexIdFile.exists())
			{
				final PropertiesParser hexId = new PropertiesParser(hexIdFile);
				
				if (hexId.containskey("ServerID") && hexId.containskey("HexID"))
				{
					SERVER_ID = hexId.getInt("ServerID", 1);
					try
					{
						HEX_ID = new BigInteger(hexId.getString("HexID", null), 16).toByteArray();
					}
					catch (Exception e)
					{
						_log.warn("Could not load HexID file (" + HEXID_FILE + "). Hopefully login will give us one.");
					}
				}
				else
				{
					_log.warn("Could not load HexID file (" + HEXID_FILE + "). Hopefully login will give us one.");
				}
			}
			else
			{
				_log.warn("Could not load HexID file (" + HEXID_FILE + "). Hopefully login will give us one.");
			}
			
			// Bosses
			// Load ANTHARAS_CONFIG L2Properties file (if exists)
			final PropertiesParser antharas_load = new PropertiesParser(ANTHARAS_CONFIG);
			
			ANTHARAS_WAIT_TIME = antharas_load.getInt("AntharasWaitTime", 30);
			ANTHARAS_SPAWN_INTERVAL = antharas_load.getInt("IntervalOfAntharasSpawn", 264);
			ANTHARAS_SPAWN_RANDOM = antharas_load.getInt("RandomOfAntharasSpawn", 72);
			
			// Load VALAKAS_CONFIG L2Properties file (if exists)
			final PropertiesParser valakas_load = new PropertiesParser(VALAKAS_CONFIG);
			
			VALAKAS_WAIT_TIME = valakas_load.getInt("ValakasWaitTime", 30);
			VALAKAS_SPAWN_INTERVAL = valakas_load.getInt("IntervalOfValakasSpawn", 264);
			VALAKAS_SPAWN_RANDOM = valakas_load.getInt("RandomOfValakasSpawn", 72);
			
			// Load BAIUM_CONFIG L2Properties file (if exists)
			final PropertiesParser baium_load = new PropertiesParser(BAIUM_CONFIG);
			
			BAIUM_SPAWN_INTERVAL = baium_load.getInt("IntervalOfBaiumSpawn", 168);
			BAIUM_SPAWN_RANDOM = baium_load.getInt("RandomOfBaiumSpawn", 48);
			
			// Load CORE_CONFIG L2Properties file (if exists)
			final PropertiesParser core_load = new PropertiesParser(CORE_CONFIG);
			
			CORE_SPAWN_INTERVAL = core_load.getInt("IntervalOfCoreSpawn", 60);
			CORE_SPAWN_RANDOM = core_load.getInt("RandomOfCoreSpawn", 24);
			
			// Load FREYA_EASY_CONFIG L2Properties file (if exists)
			final PropertiesParser freyaeasy_load = new PropertiesParser(FREYA_EASY_CONFIG);
			
			MIN_PLAYER_LEVEL_TO_EASY = freyaeasy_load.getInt("MinLevel", 78);
			MIN_PLAYERS_TO_EASY = freyaeasy_load.getInt("MinPlayers", 18);
			MAX_PLAYERS_TO_EASY = freyaeasy_load.getInt("MaxPlayers", 27);
			
			// Load FREYA_HARDCORE_CONFIG L2Properties file (if exists)
			final PropertiesParser freyahardcore_load = new PropertiesParser(FREYA_HARDCORE_CONFIG);
			
			MIN_PLAYER_LEVEL_TO_HARD = freyahardcore_load.getInt("MinLevel", 82);
			MIN_PLAYERS_TO_HARD = freyahardcore_load.getInt("MinPlayers", 36);
			MAX_PLAYERS_TO_HARD = freyahardcore_load.getInt("MaxPlayers", 45);
			
			// Load ORFEN_CONFIG L2Properties file (if exists)
			final PropertiesParser orfen_load = new PropertiesParser(ORFEN_CONFIG);
			
			ORFEN_SPAWN_INTERVAL = orfen_load.getInt("IntervalOfOrfenSpawn", 48);
			ORFEN_SPAWN_RANDOM = orfen_load.getInt("RandomOfOrfenSpawn", 20);
			
			// Load QUEEN_ANT_CONFIG L2Properties file (if exists)
			final PropertiesParser queen_load = new PropertiesParser(QUEEN_ANT_CONFIG);
			
			QUEEN_ANT_SPAWN_INTERVAL = queen_load.getInt("IntervalOfQueenAntSpawn", 36);
			QUEEN_ANT_SPAWN_RANDOM = queen_load.getInt("RandomOfQueenAntSpawn", 17);
			QUEEN_ANT_CHAR_ENTER_LEVEL_RESTRICTION = queen_load.getInt("QueenAntCharEnterLevelRestriction", 0);
			
			// Load BELETH_CONFIG L2Properties file (if exists)
			final PropertiesParser beleth_load = new PropertiesParser(BELETH_CONFIG);
			
			BELETH_SPAWN_INTERVAL = beleth_load.getInt("IntervalOfBelethSpawn", 192);
			BELETH_SPAWN_RANDOM = beleth_load.getInt("RandomOfBelethSpawn", 148);
			BELETH_MIN_PLAYERS = beleth_load.getInt("BelethMinPlayers", 36);
			
			// Load FRINTEZZA_CONFIG L2Properties file (if exists)
			final PropertiesParser frintezza_load = new PropertiesParser(FRINTEZZA_CONFIG);
			
			MIN_PLAYER_TO_FE = frintezza_load.getInt("MinPlayers", 36);
			MAX_PLAYER_TO_FE = frintezza_load.getInt("MaxPlayers", 45);
			MIN_LEVEL_TO_FE = frintezza_load.getInt("MinLevel", 80);
			
			// Load SAILREN_CONFIG L2Properties file (if exists)
			final PropertiesParser sailren_load = new PropertiesParser(SAILREN_CONFIG);
			
			INTERVAL_OF_SAILREN_SPAWN = sailren_load.getInt("IntervalOfSailrenSpawn", 12);
			RANDOM_OF_SAILREN_SPAWN = sailren_load.getInt("RandomOfSailrenSpawn", 24);
			
			// Load VAN_HALTER_CONFIG L2Properties file (if exists)
			final PropertiesParser vanhalter_load = new PropertiesParser(VAN_HALTER_CONFIG);
			
			// High Priestess van Halter
			HPH_FIXINTERVALOFHALTER = vanhalter_load.getInt("FixIntervalOfHalter", 172800);
			if ((HPH_FIXINTERVALOFHALTER < 300) || (HPH_FIXINTERVALOFHALTER > 864000))
			{
				HPH_FIXINTERVALOFHALTER = 172800;
			}
			HPH_FIXINTERVALOFHALTER *= 6000;
			HPH_RANDOMINTERVALOFHALTER = vanhalter_load.getInt("RandomIntervalOfHalter", 86400);
			if ((HPH_RANDOMINTERVALOFHALTER < 300) || (HPH_RANDOMINTERVALOFHALTER > 864000))
			{
				HPH_RANDOMINTERVALOFHALTER = 86400;
			}
			HPH_RANDOMINTERVALOFHALTER *= 6000;
			HPH_APPTIMEOFHALTER = vanhalter_load.getInt("AppTimeOfHalter", 20);
			if ((HPH_APPTIMEOFHALTER < 5) || (HPH_APPTIMEOFHALTER > 60))
			{
				HPH_APPTIMEOFHALTER = 20;
			}
			HPH_APPTIMEOFHALTER *= 6000;
			HPH_ACTIVITYTIMEOFHALTER = vanhalter_load.getInt("ActivityTimeOfHalter", 21600);
			if ((HPH_ACTIVITYTIMEOFHALTER < 7200) || (HPH_ACTIVITYTIMEOFHALTER > 86400))
			{
				HPH_ACTIVITYTIMEOFHALTER = 21600;
			}
			HPH_ACTIVITYTIMEOFHALTER *= 1000;
			HPH_FIGHTTIMEOFHALTER = vanhalter_load.getInt("FightTimeOfHalter", 7200);
			if ((HPH_FIGHTTIMEOFHALTER < 7200) || (HPH_FIGHTTIMEOFHALTER > 21600))
			{
				HPH_FIGHTTIMEOFHALTER = 7200;
			}
			HPH_FIGHTTIMEOFHALTER *= 6000;
			HPH_CALLROYALGUARDHELPERCOUNT = vanhalter_load.getInt("CallRoyalGuardHelperCount", 6);
			if ((HPH_CALLROYALGUARDHELPERCOUNT < 1) || (HPH_CALLROYALGUARDHELPERCOUNT > 6))
			{
				HPH_CALLROYALGUARDHELPERCOUNT = 6;
			}
			HPH_CALLROYALGUARDHELPERINTERVAL = vanhalter_load.getInt("CallRoyalGuardHelperInterval", 10);
			if ((HPH_CALLROYALGUARDHELPERINTERVAL < 1) || (HPH_CALLROYALGUARDHELPERINTERVAL > 60))
			{
				HPH_CALLROYALGUARDHELPERINTERVAL = 10;
			}
			HPH_CALLROYALGUARDHELPERINTERVAL *= 6000;
			HPH_INTERVALOFDOOROFALTER = vanhalter_load.getInt("IntervalOfDoorOfAlter", 5400);
			if ((HPH_INTERVALOFDOOROFALTER < 60) || (HPH_INTERVALOFDOOROFALTER > 5400))
			{
				HPH_INTERVALOFDOOROFALTER = 5400;
			}
			HPH_INTERVALOFDOOROFALTER *= 6000;
			HPH_TIMEOFLOCKUPDOOROFALTAR = vanhalter_load.getInt("TimeOfLockUpDoorOfAltar", 180);
			if ((HPH_TIMEOFLOCKUPDOOROFALTAR < 60) || (HPH_TIMEOFLOCKUPDOOROFALTAR > 600))
			{
				HPH_TIMEOFLOCKUPDOOROFALTAR = 180;
			}
			HPH_TIMEOFLOCKUPDOOROFALTAR *= 6000;
			
			// Load ZAKEN_CONFIG L2Properties file (if exists)
			final PropertiesParser zaken_load = new PropertiesParser(ZAKEN_CONFIG);
			
			ZAKEN_MINLEVEL_DAYTIME = zaken_load.getInt("ZakenMinLevelDaytime", 55);
			ZAKEN_MINLEVEL_DAYTIME83 = zaken_load.getInt("ZakenMinLevelDaytime83", 78);
			ZAKEN_MINMEMBERS_DAYTIME = zaken_load.getInt("ZakenMinMembersDaytime", 9);
			ZAKEN_MINMEMBERS_NIGHTTIME = zaken_load.getInt("ZakenMinMembersNighttime", 72);
			ZAKEN_MAXMEMBERS_DAYTIME = zaken_load.getInt("ZakenMaxMembersDaytime", 27);
			ZAKEN_MAXMEMBERS_NIGHTTIME = zaken_load.getInt("ZakenMaxMembersNighttime", 450);
			
			// Load DESTRUCTION_BOSSES_CONFIG L2Properties file (if exists)
			final PropertiesParser destr_load = new PropertiesParser(DESTRUCTION_BOSSES_CONFIG);
			
			CHANGE_STATUS = destr_load.getInt("ChangeStatus", 30);
			CHANCE_SPAWN = destr_load.getInt("ChanceSpawn", 50);
			RESPAWN_TIME = destr_load.getInt("RespawnTime", 720);
			
			// Load EKIMUS_CONFIG L2Properties file (if exists)
			final PropertiesParser soi_load = new PropertiesParser(EKIMUS_CONFIG);
			
			SOI_EKIMUS_KILL_COUNT = soi_load.getInt("EkimusKillCount", 5);
			EROSION_ATTACK_MIN_PLAYERS = soi_load.getInt("MinEroAttPlayers", 18);
			EROSION_ATTACK_MAX_PLAYERS = soi_load.getInt("MaxEroAttPlayers", 27);
			EROSION_DEFENCE_MIN_PLAYERS = soi_load.getInt("MinEroDefPlayers", 18);
			EROSION_DEFENCE_MAX_PLAYERS = soi_load.getInt("MaxEroDefPlayers", 27);
			HEART_ATTACK_MIN_PLAYERS = soi_load.getInt("MinHeaAttPlayers", 18);
			HEART_ATTACK_MAX_PLAYERS = soi_load.getInt("MaxHeaAttPlayers", 27);
			HEART_DEFENCE_MIN_PLAYERS = soi_load.getInt("MinHeaDefPlayers", 18);
			HEART_DEFENCE_MAX_PLAYERS = soi_load.getInt("MaxHeaDefPlayers", 27);
			
			// Gracia Seeds
			final PropertiesParser GraciaSeedsSettings = new PropertiesParser(GRACIASEEDS_CONFIG_FILE);
			
			// Seed of Destruction
			SOD_TIAT_KILL_COUNT = GraciaSeedsSettings.getInt("TiatKillCountForNextState", 10);
			SOD_STAGE_2_LENGTH = GraciaSeedsSettings.getLong("Stage2Length", 720) * 60000;
			
			final File chat_filter = new File(CHAT_FILTER_FILE);
			try (FileReader fr = new FileReader(chat_filter);
				BufferedReader br = new BufferedReader(fr);
				LineNumberReader lnr = new LineNumberReader(br))
			{
				FILTER_LIST = new ArrayList<>();
				
				String line = null;
				while ((line = lnr.readLine()) != null)
				{
					if (line.trim().isEmpty() || (line.charAt(0) == '#'))
					{
						continue;
					}
					
					FILTER_LIST.add(line.trim());
				}
				_log.info("Loaded " + FILTER_LIST.size() + " Filter Words.");
			}
			catch (Exception e)
			{
				_log.warn("Error while loading chat filter words!", e);
			}
			
			final PropertiesParser ClanHallSiege = new PropertiesParser(CH_SIEGE_FILE);
			
			CHS_MAX_ATTACKERS = ClanHallSiege.getInt("MaxAttackers", 500);
			CHS_CLAN_MINLEVEL = ClanHallSiege.getInt("MinClanLevel", 4);
			CHS_MAX_FLAGS_PER_CLAN = ClanHallSiege.getInt("MaxFlagsPerClan", 1);
			CHS_ENABLE_FAME = ClanHallSiege.getBoolean("EnableFame", false);
			CHS_FAME_AMOUNT = ClanHallSiege.getInt("FameAmount", 0);
			CHS_FAME_FREQUENCY = ClanHallSiege.getInt("FameFrequency", 0);
			
			// ItemMall Settings
			final PropertiesParser itemMallSettigns = new PropertiesParser(ITEM_MALL_CONFIG_FILE);
			
			GAME_POINT_ITEM_ID = itemMallSettigns.getInt("GamePointItemId", -1);
		}
		else if (Server.serverMode == Server.MODE_LOGINSERVER)
		{
			final PropertiesParser ServerSettings = new PropertiesParser(LOGIN_CONFIGURATION_FILE);
			
			GAME_SERVER_LOGIN_HOST = ServerSettings.getString("LoginHostname", "127.0.0.1");
			GAME_SERVER_LOGIN_PORT = ServerSettings.getInt("LoginPort", 9013);
			
			LOGIN_BIND_ADDRESS = ServerSettings.getString("LoginserverHostname", "*");
			PORT_LOGIN = ServerSettings.getInt("LoginserverPort", 2106);
			
			try
			{
				DATAPACK_ROOT = new File(ServerSettings.getString("DatapackRoot", ".").replaceAll("\\\\", "/")).getCanonicalFile();
			}
			catch (IOException e)
			{
				_log.warn("Error setting datapack root!", e);
				DATAPACK_ROOT = new File(".");
			}
			
			DEBUG = ServerSettings.getBoolean("Debug", false);
			
			ACCEPT_NEW_GAMESERVER = ServerSettings.getBoolean("AcceptNewGameServer", true);
			
			LOGIN_TRY_BEFORE_BAN = ServerSettings.getInt("LoginTryBeforeBan", 5);
			LOGIN_BLOCK_AFTER_BAN = ServerSettings.getInt("LoginBlockAfterBan", 900);
			
			LOGIN_SERVER_SCHEDULE_RESTART = ServerSettings.getBoolean("LoginRestartSchedule", false);
			LOGIN_SERVER_SCHEDULE_RESTART_TIME = ServerSettings.getLong("LoginRestartTime", 24);
			
			DATABASE_DRIVER = ServerSettings.getString("Driver", "com.mysql.jdbc.Driver");
			DATABASE_URL = ServerSettings.getString("URL", "jdbc:mysql://localhost/l2jls");
			DATABASE_LOGIN = ServerSettings.getString("Login", "root");
			DATABASE_PASSWORD = ServerSettings.getString("Password", "");
			DATABASE_MAX_CONNECTIONS = ServerSettings.getInt("MaximumDbConnections", 10);
			DATABASE_MAX_IDLE_TIME = ServerSettings.getInt("MaximumDbIdleTime", 0);
			CONNECTION_CLOSE_TIME = ServerSettings.getLong("ConnectionCloseTime", 60000);
			
			SHOW_LICENCE = ServerSettings.getBoolean("ShowLicence", true);
			
			AUTO_CREATE_ACCOUNTS = ServerSettings.getBoolean("AutoCreateAccounts", true);
			
			FLOOD_PROTECTION = ServerSettings.getBoolean("EnableFloodProtection", true);
			FAST_CONNECTION_LIMIT = ServerSettings.getInt("FastConnectionLimit", 15);
			NORMAL_CONNECTION_TIME = ServerSettings.getInt("NormalConnectionTime", 700);
			FAST_CONNECTION_TIME = ServerSettings.getInt("FastConnectionTime", 350);
			MAX_CONNECTION_PER_IP = ServerSettings.getInt("MaxConnectionPerIP", 50);
			
			// MMO
			final PropertiesParser mmoSettings = new PropertiesParser(MMO_CONFIG_FILE);
			
			MMO_SELECTOR_SLEEP_TIME = mmoSettings.getInt("SleepTime", 20);
			MMO_MAX_SEND_PER_PASS = mmoSettings.getInt("MaxSendPerPass", 12);
			MMO_MAX_READ_PER_PASS = mmoSettings.getInt("MaxReadPerPass", 12);
			MMO_HELPER_BUFFER_COUNT = mmoSettings.getInt("HelperBufferCount", 20);
			MMO_TCP_NODELAY = mmoSettings.getBoolean("TcpNoDelay", false);
			
			// Load Telnet L2Properties file (if exists)
			final PropertiesParser telnetSettings = new PropertiesParser(TELNET_FILE);
			
			IS_TELNET_ENABLED = telnetSettings.getBoolean("EnableTelnet", false);
			
			// Email
			final PropertiesParser emailSettings = new PropertiesParser(EMAIL_CONFIG_FILE);
			
			EMAIL_SERVERINFO_NAME = emailSettings.getString("ServerInfoName", "Unconfigured L2J Server");
			EMAIL_SERVERINFO_ADDRESS = emailSettings.getString("ServerInfoAddress", "info@myl2jserver.com");
			
			EMAIL_SYS_ENABLED = emailSettings.getBoolean("EmailSystemEnabled", false);
			EMAIL_SYS_HOST = emailSettings.getString("SmtpServerHost", "smtp.gmail.com");
			EMAIL_SYS_PORT = emailSettings.getInt("SmtpServerPort", 465);
			EMAIL_SYS_SMTP_AUTH = emailSettings.getBoolean("SmtpAuthRequired", true);
			EMAIL_SYS_FACTORY = emailSettings.getString("SmtpFactory", "javax.net.ssl.SSLSocketFactory");
			EMAIL_SYS_FACTORY_CALLBACK = emailSettings.getBoolean("SmtpFactoryCallback", false);
			EMAIL_SYS_USERNAME = emailSettings.getString("SmtpUsername", "user@gmail.com");
			EMAIL_SYS_PASSWORD = emailSettings.getString("SmtpPassword", "password");
			EMAIL_SYS_ADDRESS = emailSettings.getString("EmailSystemAddress", "noreply@myl2jserver.com");
			EMAIL_SYS_SELECTQUERY = emailSettings.getString("EmailDBSelectQuery", "SELECT value FROM account_data WHERE account_name=? AND var='email_addr'");
			EMAIL_SYS_DBFIELD = emailSettings.getString("EmailDBField", "value");
		}
		else
		{
			_log.error("Could not Load Config: server mode was not set!");
		}
	}
	
	/**
	 * Set a new value to a config parameter.
	 * @param pName the name of the parameter whose value to change
	 * @param pValue the new value of the parameter
	 * @return {@code true} if the value of the parameter was changed, {@code false} otherwise
	 */
	public static boolean setParameterValue(String pName, String pValue)
	{
		switch (pName.trim().toLowerCase())
		{
		// rates.ini
			case "ratexp":
				RATE_XP = Float.parseFloat(pValue);
				break;
			case "ratesp":
				RATE_SP = Float.parseFloat(pValue);
				break;
			case "ratepartyxp":
				RATE_PARTY_XP = Float.parseFloat(pValue);
				break;
			case "rateconsumablecost":
				RATE_CONSUMABLE_COST = Float.parseFloat(pValue);
				break;
			case "rateextractable":
				RATE_EXTRACTABLE = Float.parseFloat(pValue);
				break;
			case "ratedropitems":
				RATE_DROP_ITEMS = Float.parseFloat(pValue);
				break;
			case "ratedropadena":
				RATE_DROP_ITEMS_ID.put(Inventory.ADENA_ID, Float.parseFloat(pValue));
				break;
			case "rateraiddropitems":
				RATE_DROP_ITEMS_BY_RAID = Float.parseFloat(pValue);
				break;
			case "ratedropspoil":
				RATE_DROP_SPOIL = Float.parseFloat(pValue);
				break;
			case "ratedropmanor":
				RATE_DROP_MANOR = Integer.parseInt(pValue);
				break;
			case "ratequestdrop":
				RATE_QUEST_DROP = Float.parseFloat(pValue);
				break;
			case "ratequestreward":
				RATE_QUEST_REWARD = Float.parseFloat(pValue);
				break;
			case "ratequestrewardxp":
				RATE_QUEST_REWARD_XP = Float.parseFloat(pValue);
				break;
			case "ratequestrewardsp":
				RATE_QUEST_REWARD_SP = Float.parseFloat(pValue);
				break;
			case "ratequestrewardadena":
				RATE_QUEST_REWARD_ADENA = Float.parseFloat(pValue);
				break;
			case "usequestrewardmultipliers":
				RATE_QUEST_REWARD_USE_MULTIPLIERS = Boolean.parseBoolean(pValue);
				break;
			case "ratequestrewardpotion":
				RATE_QUEST_REWARD_POTION = Float.parseFloat(pValue);
				break;
			case "ratequestrewardscroll":
				RATE_QUEST_REWARD_SCROLL = Float.parseFloat(pValue);
				break;
			case "ratequestrewardrecipe":
				RATE_QUEST_REWARD_RECIPE = Float.parseFloat(pValue);
				break;
			case "ratequestrewardmaterial":
				RATE_QUEST_REWARD_MATERIAL = Float.parseFloat(pValue);
				break;
			case "ratehellboundtrustincrease":
				RATE_HB_TRUST_INCREASE = Float.parseFloat(pValue);
				break;
			case "ratehellboundtrustdecrease":
				RATE_HB_TRUST_DECREASE = Float.parseFloat(pValue);
				break;
			case "ratevitalitylevel1":
				RATE_VITALITY_LEVEL_1 = Float.parseFloat(pValue);
				break;
			case "ratevitalitylevel2":
				RATE_VITALITY_LEVEL_2 = Float.parseFloat(pValue);
				break;
			case "ratevitalitylevel3":
				RATE_VITALITY_LEVEL_3 = Float.parseFloat(pValue);
				break;
			case "ratevitalitylevel4":
				RATE_VITALITY_LEVEL_4 = Float.parseFloat(pValue);
				break;
			case "raterecoverypeacezone":
				RATE_RECOVERY_VITALITY_PEACE_ZONE = Float.parseFloat(pValue);
				break;
			case "ratevitalitylost":
				RATE_VITALITY_LOST = Float.parseFloat(pValue);
				break;
			case "ratevitalitygain":
				RATE_VITALITY_GAIN = Float.parseFloat(pValue);
				break;
			case "raterecoveryonreconnect":
				RATE_RECOVERY_ON_RECONNECT = Float.parseFloat(pValue);
				break;
			case "ratekarmaexplost":
				RATE_KARMA_EXP_LOST = Float.parseFloat(pValue);
				break;
			case "ratesiegeguardsprice":
				RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(pValue);
				break;
			case "ratecommonherbs":
				RATE_DROP_COMMON_HERBS = Float.parseFloat(pValue);
				break;
			case "ratehpherbs":
				RATE_DROP_HP_HERBS = Float.parseFloat(pValue);
				break;
			case "ratempherbs":
				RATE_DROP_MP_HERBS = Float.parseFloat(pValue);
				break;
			case "ratespecialherbs":
				RATE_DROP_SPECIAL_HERBS = Float.parseFloat(pValue);
				break;
			case "ratevitalityherbs":
				RATE_DROP_VITALITY_HERBS = Float.parseFloat(pValue);
				break;
			case "playerdroplimit":
				PLAYER_DROP_LIMIT = Integer.parseInt(pValue);
				break;
			case "playerratedrop":
				PLAYER_RATE_DROP = Integer.parseInt(pValue);
				break;
			case "playerratedropitem":
				PLAYER_RATE_DROP_ITEM = Integer.parseInt(pValue);
				break;
			case "playerratedropequip":
				PLAYER_RATE_DROP_EQUIP = Integer.parseInt(pValue);
				break;
			case "playerratedropequipweapon":
				PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);
				break;
			case "petxprate":
				PET_XP_RATE = Float.parseFloat(pValue);
				break;
			case "petfoodrate":
				PET_FOOD_RATE = Integer.parseInt(pValue);
				break;
			case "sineaterxprate":
				SINEATER_XP_RATE = Float.parseFloat(pValue);
				break;
			case "karmadroplimit":
				KARMA_DROP_LIMIT = Integer.parseInt(pValue);
				break;
			case "karmaratedrop":
				KARMA_RATE_DROP = Integer.parseInt(pValue);
				break;
			case "karmaratedropitem":
				KARMA_RATE_DROP_ITEM = Integer.parseInt(pValue);
				break;
			case "karmaratedropequip":
				KARMA_RATE_DROP_EQUIP = Integer.parseInt(pValue);
				break;
			case "karmaratedropequipweapon":
				KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);
				break;
			case "autodestroydroppeditemafter":
				AUTODESTROY_ITEM_AFTER = Integer.parseInt(pValue);
				break;
			case "destroyplayerdroppeditem":
				DESTROY_DROPPED_PLAYER_ITEM = Boolean.parseBoolean(pValue);
				break;
			case "destroyequipableitem":
				DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.parseBoolean(pValue);
				break;
			case "savedroppeditem":
				SAVE_DROPPED_ITEM = Boolean.parseBoolean(pValue);
				break;
			case "emptydroppeditemtableafterload":
				EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.parseBoolean(pValue);
				break;
			case "savedroppediteminterval":
				SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(pValue);
				break;
			case "cleardroppeditemtable":
				CLEAR_DROPPED_ITEM_TABLE = Boolean.parseBoolean(pValue);
				break;
			case "precisedropcalculation":
				PRECISE_DROP_CALCULATION = Boolean.parseBoolean(pValue);
				break;
			case "multipleitemdrop":
				MULTIPLE_ITEM_DROP = Boolean.parseBoolean(pValue);
				break;
			case "lowweight":
				LOW_WEIGHT = Float.parseFloat(pValue);
				break;
			case "mediumweight":
				MEDIUM_WEIGHT = Float.parseFloat(pValue);
				break;
			case "highweight":
				HIGH_WEIGHT = Float.parseFloat(pValue);
				break;
			case "advanceddiagonalstrategy":
				ADVANCED_DIAGONAL_STRATEGY = Boolean.parseBoolean(pValue);
				break;
			case "diagonalweight":
				DIAGONAL_WEIGHT = Float.parseFloat(pValue);
				break;
			case "maxpostfilterpasses":
				MAX_POSTFILTER_PASSES = Integer.parseInt(pValue);
				break;
			case "coordsynchronize":
				COORD_SYNCHRONIZE = Integer.parseInt(pValue);
				break;
			case "deletecharafterdays":
				DELETE_DAYS = Integer.parseInt(pValue);
				break;
			case "clientpacketqueuesize":
				CLIENT_PACKET_QUEUE_SIZE = Integer.parseInt(pValue);
				if (CLIENT_PACKET_QUEUE_SIZE == 0)
				{
					CLIENT_PACKET_QUEUE_SIZE = MMO_MAX_READ_PER_PASS + 1;
				}
				break;
			case "clientpacketqueuemaxburstsize":
				CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = Integer.parseInt(pValue);
				if (CLIENT_PACKET_QUEUE_MAX_BURST_SIZE == 0)
				{
					CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = MMO_MAX_READ_PER_PASS;
				}
				break;
			case "clientpacketqueuemaxpacketspersecond":
				CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND = Integer.parseInt(pValue);
				break;
			case "clientpacketqueuemeasureinterval":
				CLIENT_PACKET_QUEUE_MEASURE_INTERVAL = Integer.parseInt(pValue);
				break;
			case "clientpacketqueuemaxaveragepacketspersecond":
				CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND = Integer.parseInt(pValue);
				break;
			case "clientpacketqueuemaxfloodspermin":
				CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN = Integer.parseInt(pValue);
				break;
			case "clientpacketqueuemaxoverflowspermin":
				CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN = Integer.parseInt(pValue);
				break;
			case "clientpacketqueuemaxunderflowspermin":
				CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN = Integer.parseInt(pValue);
				break;
			case "clientpacketqueuemaxunknownpermin":
				CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN = Integer.parseInt(pValue);
				break;
			case "allowdiscarditem":
				ALLOW_DISCARDITEM = Boolean.parseBoolean(pValue);
				break;
			case "allowrefund":
				ALLOW_REFUND = Boolean.parseBoolean(pValue);
				break;
			case "allowwarehouse":
				ALLOW_WAREHOUSE = Boolean.parseBoolean(pValue);
				break;
			case "allowwear":
				ALLOW_WEAR = Boolean.parseBoolean(pValue);
				break;
			case "weardelay":
				WEAR_DELAY = Integer.parseInt(pValue);
				break;
			case "wearprice":
				WEAR_PRICE = Integer.parseInt(pValue);
				break;
			case "allowwater":
				ALLOW_WATER = Boolean.parseBoolean(pValue);
				break;
			case "allowrentpet":
				ALLOW_RENTPET = Boolean.parseBoolean(pValue);
				break;
			case "boatbroadcastradius":
				BOAT_BROADCAST_RADIUS = Integer.parseInt(pValue);
				break;
			case "allowcursedweapons":
				ALLOW_CURSED_WEAPONS = Boolean.parseBoolean(pValue);
				break;
			case "allowmanor":
				ALLOW_MANOR = Boolean.parseBoolean(pValue);
				break;
			case "allowpetwalkers":
				ALLOW_PET_WALKERS = Boolean.parseBoolean(pValue);
				break;
			case "bypassvalidation":
				BYPASS_VALIDATION = Boolean.parseBoolean(pValue);
				break;
			case "enablecommunity":
				ENABLE_COMMUNITY = Boolean.parseBoolean(pValue);
				break;
			case "bbsdefault":
				BBS_DEFAULT = pValue;
				break;
			case "showservernews":
				SERVER_NEWS = Boolean.parseBoolean(pValue);
				break;
			case "shownpclevel":
				SHOW_NPC_LVL = Boolean.parseBoolean(pValue);
				break;
			case "showcrestwithoutquest":
				SHOW_CREST_WITHOUT_QUEST = Boolean.parseBoolean(pValue);
				break;
			case "forceinventoryupdate":
				FORCE_INVENTORY_UPDATE = Boolean.parseBoolean(pValue);
				break;
			case "autodeleteinvalidquestdata":
				AUTODELETE_INVALID_QUEST_DATA = Boolean.parseBoolean(pValue);
				break;
			case "maximumonlineusers":
				MAXIMUM_ONLINE_USERS = Integer.parseInt(pValue);
				break;
			case "peacezonemode":
				PEACE_ZONE_MODE = Integer.parseInt(pValue);
				break;
			case "checkknownlist":
				CHECK_KNOWN = Boolean.parseBoolean(pValue);
				break;
			case "maxdriftrange":
				MAX_DRIFT_RANGE = Integer.parseInt(pValue);
				break;
			case "usedeepbluedroprules":
				DEEPBLUE_DROP_RULES = Boolean.parseBoolean(pValue);
				break;
			case "usedeepbluedroprulesraid":
				DEEPBLUE_DROP_RULES_RAID = Boolean.parseBoolean(pValue);
				break;
			case "guardattackaggromob":
				GUARD_ATTACK_AGGRO_MOB = Boolean.parseBoolean(pValue);
				break;
			case "cancellessereffect":
				EFFECT_CANCELING = Boolean.parseBoolean(pValue);
				break;
			case "maximumslotsfornodwarf":
				INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(pValue);
				break;
			case "maximumslotsfordwarf":
				INVENTORY_MAXIMUM_DWARF = Integer.parseInt(pValue);
				break;
			case "maximumslotsforgmplayer":
				INVENTORY_MAXIMUM_GM = Integer.parseInt(pValue);
				break;
			case "maximumslotsforquestitems":
				INVENTORY_MAXIMUM_QUEST_ITEMS = Integer.parseInt(pValue);
				break;
			case "maximumwarehouseslotsfornodwarf":
				WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(pValue);
				break;
			case "maximumwarehouseslotsfordwarf":
				WAREHOUSE_SLOTS_DWARF = Integer.parseInt(pValue);
				break;
			case "maximumwarehouseslotsforclan":
				WAREHOUSE_SLOTS_CLAN = Integer.parseInt(pValue);
				break;
			case "enchantchanceelementstone":
				ENCHANT_CHANCE_ELEMENT_STONE = Integer.parseInt(pValue);
				break;
			case "enchantchanceelementcrystal":
				ENCHANT_CHANCE_ELEMENT_CRYSTAL = Integer.parseInt(pValue);
				break;
			case "enchantchanceelementjewel":
				ENCHANT_CHANCE_ELEMENT_JEWEL = Integer.parseInt(pValue);
				break;
			case "enchantchanceelementenergy":
				ENCHANT_CHANCE_ELEMENT_ENERGY = Integer.parseInt(pValue);
				break;
			case "augmentationngskillchance":
				AUGMENTATION_NG_SKILL_CHANCE = Integer.parseInt(pValue);
				break;
			case "augmentationngglowchance":
				AUGMENTATION_NG_GLOW_CHANCE = Integer.parseInt(pValue);
				break;
			case "augmentationmidskillchance":
				AUGMENTATION_MID_SKILL_CHANCE = Integer.parseInt(pValue);
				break;
			case "augmentationmidglowchance":
				AUGMENTATION_MID_GLOW_CHANCE = Integer.parseInt(pValue);
				break;
			case "augmentationhighskillchance":
				AUGMENTATION_HIGH_SKILL_CHANCE = Integer.parseInt(pValue);
				break;
			case "augmentationhighglowchance":
				AUGMENTATION_HIGH_GLOW_CHANCE = Integer.parseInt(pValue);
				break;
			case "augmentationtopskillchance":
				AUGMENTATION_TOP_SKILL_CHANCE = Integer.parseInt(pValue);
				break;
			case "augmentationtopglowchance":
				AUGMENTATION_TOP_GLOW_CHANCE = Integer.parseInt(pValue);
				break;
			case "augmentationbasestatchance":
				AUGMENTATION_BASESTAT_CHANCE = Integer.parseInt(pValue);
				break;
			case "hpregenmultiplier":
				HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
				break;
			case "mpregenmultiplier":
				MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
				break;
			case "cpregenmultiplier":
				CP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
				break;
			case "raidhpregenmultiplier":
				RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
				break;
			case "raidmpregenmultiplier":
				RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
				break;
			case "raidpdefencemultiplier":
				RAID_PDEFENCE_MULTIPLIER = Double.parseDouble(pValue) / 100;
				break;
			case "raidmdefencemultiplier":
				RAID_MDEFENCE_MULTIPLIER = Double.parseDouble(pValue) / 100;
				break;
			case "raidpattackmultiplier":
				RAID_PATTACK_MULTIPLIER = Double.parseDouble(pValue) / 100;
				break;
			case "raidmattackmultiplier":
				RAID_MATTACK_MULTIPLIER = Double.parseDouble(pValue) / 100;
				break;
			case "raidminionrespawntime":
				RAID_MINION_RESPAWN_TIMER = Integer.parseInt(pValue);
				break;
			case "raidchaostime":
				RAID_CHAOS_TIME = Integer.parseInt(pValue);
				break;
			case "grandchaostime":
				GRAND_CHAOS_TIME = Integer.parseInt(pValue);
				break;
			case "minionchaostime":
				MINION_CHAOS_TIME = Integer.parseInt(pValue);
				break;
			case "startingadena":
				STARTING_ADENA = Long.parseLong(pValue);
				break;
			case "startinglevel":
				STARTING_LEVEL = Byte.parseByte(pValue);
				break;
			case "startingsp":
				STARTING_SP = Integer.parseInt(pValue);
				break;
			case "unstuckinterval":
				UNSTUCK_INTERVAL = Integer.parseInt(pValue);
				break;
			case "teleportwatchdogtimeout":
				TELEPORT_WATCHDOG_TIMEOUT = Integer.parseInt(pValue);
				break;
			case "playerspawnprotection":
				PLAYER_SPAWN_PROTECTION = Integer.parseInt(pValue);
				break;
			case "playerfakedeathupprotection":
				PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(pValue);
				break;
			case "restoreplayerinstance":
				RESTORE_PLAYER_INSTANCE = Boolean.parseBoolean(pValue);
				break;
			case "allowsummontoinstance":
				ALLOW_SUMMON_TO_INSTANCE = Boolean.parseBoolean(pValue);
				break;
			case "partyxpcutoffmethod":
				PARTY_XP_CUTOFF_METHOD = pValue;
				break;
			case "partyxpcutoffpercent":
				PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(pValue);
				break;
			case "partyxpcutofflevel":
				PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(pValue);
				break;
			case "respawnrestorecp":
				RESPAWN_RESTORE_CP = Double.parseDouble(pValue) / 100;
				break;
			case "respawnrestorehp":
				RESPAWN_RESTORE_HP = Double.parseDouble(pValue) / 100;
				break;
			case "respawnrestoremp":
				RESPAWN_RESTORE_MP = Double.parseDouble(pValue) / 100;
				break;
			case "maxpvtstoresellslotsdwarf":
				MAX_PVTSTORESELL_SLOTS_DWARF = Integer.parseInt(pValue);
				break;
			case "maxpvtstoresellslotsother":
				MAX_PVTSTORESELL_SLOTS_OTHER = Integer.parseInt(pValue);
				break;
			case "maxpvtstorebuyslotsdwarf":
				MAX_PVTSTOREBUY_SLOTS_DWARF = Integer.parseInt(pValue);
				break;
			case "maxpvtstorebuyslotsother":
				MAX_PVTSTOREBUY_SLOTS_OTHER = Integer.parseInt(pValue);
				break;
			case "storeskillcooltime":
				STORE_SKILL_COOLTIME = Boolean.parseBoolean(pValue);
				break;
			case "subclassstoreskillcooltime":
				SUBCLASS_STORE_SKILL_COOLTIME = Boolean.parseBoolean(pValue);
				break;
			case "announcemammonspawn":
				ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(pValue);
				break;
			case "altgametiredness":
				ALT_GAME_TIREDNESS = Boolean.parseBoolean(pValue);
				break;
			case "enablefallingdamage":
				ENABLE_FALLING_DAMAGE = Boolean.parseBoolean(pValue);
				break;
			case "altgamecreation":
				ALT_GAME_CREATION = Boolean.parseBoolean(pValue);
				break;
			case "altgamecreationspeed":
				ALT_GAME_CREATION_SPEED = Double.parseDouble(pValue);
				break;
			case "altgamecreationxprate":
				ALT_GAME_CREATION_XP_RATE = Double.parseDouble(pValue);
				break;
			case "altgamecreationrarexpsprate":
				ALT_GAME_CREATION_RARE_XPSP_RATE = Double.parseDouble(pValue);
				break;
			case "altgamecreationsprate":
				ALT_GAME_CREATION_SP_RATE = Double.parseDouble(pValue);
				break;
			case "altweightlimit":
				ALT_WEIGHT_LIMIT = Double.parseDouble(pValue);
				break;
			case "altblacksmithuserecipes":
				ALT_BLACKSMITH_USE_RECIPES = Boolean.parseBoolean(pValue);
				break;
			case "altgameskilllearn":
				ALT_GAME_SKILL_LEARN = Boolean.parseBoolean(pValue);
				break;
			case "removecastlecirclets":
				REMOVE_CASTLE_CIRCLETS = Boolean.parseBoolean(pValue);
				break;
			case "reputationscoreperkill":
				REPUTATION_SCORE_PER_KILL = Integer.parseInt(pValue);
				break;
			case "altgamecancelbyhit":
				ALT_GAME_CANCEL_BOW = pValue.equalsIgnoreCase("bow") || pValue.equalsIgnoreCase("all");
				ALT_GAME_CANCEL_CAST = pValue.equalsIgnoreCase("cast") || pValue.equalsIgnoreCase("all");
				break;
			case "altshieldblocks":
				ALT_GAME_SHIELD_BLOCKS = Boolean.parseBoolean(pValue);
				break;
			case "altperfectshieldblockrate":
				ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(pValue);
				break;
			case "delevel":
				ALT_GAME_DELEVEL = Boolean.parseBoolean(pValue);
				break;
			case "magicfailures":
				ALT_GAME_MAGICFAILURES = Boolean.parseBoolean(pValue);
				break;
			case "altmobagroinpeacezone":
				ALT_MOB_AGRO_IN_PEACEZONE = Boolean.parseBoolean(pValue);
				break;
			case "altgameexponentxp":
				ALT_GAME_EXPONENT_XP = Float.parseFloat(pValue);
				break;
			case "altgameexponentsp":
				ALT_GAME_EXPONENT_SP = Float.parseFloat(pValue);
				break;
			case "allowclassmasters":
				ALLOW_CLASS_MASTERS = Boolean.parseBoolean(pValue);
				break;
			case "allowentiretree":
				ALLOW_ENTIRE_TREE = Boolean.parseBoolean(pValue);
				break;
			case "alternateclassmaster":
				ALTERNATE_CLASS_MASTER = Boolean.parseBoolean(pValue);
				break;
			case "altpartyrange":
				ALT_PARTY_RANGE = Integer.parseInt(pValue);
				break;
			case "altpartyrange2":
				ALT_PARTY_RANGE2 = Integer.parseInt(pValue);
				break;
			case "altleavepartyleader":
				ALT_LEAVE_PARTY_LEADER = Boolean.parseBoolean(pValue);
				break;
			case "craftingenabled":
				IS_CRAFTING_ENABLED = Boolean.parseBoolean(pValue);
				break;
			case "craftmasterwork":
				CRAFT_MASTERWORK = Boolean.parseBoolean(pValue);
				break;
			case "lifecrystalneeded":
				LIFE_CRYSTAL_NEEDED = Boolean.parseBoolean(pValue);
				break;
			case "autoloot":
				AUTO_LOOT = Boolean.parseBoolean(pValue);
				break;
			case "autolootraids":
				AUTO_LOOT_RAIDS = Boolean.parseBoolean(pValue);
				break;
			case "autolootherbs":
				AUTO_LOOT_HERBS = Boolean.parseBoolean(pValue);
				break;
			case "altkarmaplayercanbekilledinpeacezone":
				ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.parseBoolean(pValue);
				break;
			case "altkarmaplayercanshop":
				ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.parseBoolean(pValue);
				break;
			case "altkarmaplayercanusegk":
				ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.parseBoolean(pValue);
				break;
			case "altkarmaplayercanteleport":
				ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.parseBoolean(pValue);
				break;
			case "altkarmaplayercantrade":
				ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.parseBoolean(pValue);
				break;
			case "altkarmaplayercanusewarehouse":
				ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.parseBoolean(pValue);
				break;
			case "maxpersonalfamepoints":
				MAX_PERSONAL_FAME_POINTS = Integer.parseInt(pValue);
				break;
			case "fortresszonefametaskfrequency":
				FORTRESS_ZONE_FAME_TASK_FREQUENCY = Integer.parseInt(pValue);
				break;
			case "fortresszonefameaquirepoints":
				FORTRESS_ZONE_FAME_AQUIRE_POINTS = Integer.parseInt(pValue);
				break;
			case "castlezonefametaskfrequency":
				CASTLE_ZONE_FAME_TASK_FREQUENCY = Integer.parseInt(pValue);
				break;
			case "castlezonefameaquirepoints":
				CASTLE_ZONE_FAME_AQUIRE_POINTS = Integer.parseInt(pValue);
				break;
			case "altcastlefordawn":
				ALT_GAME_CASTLE_DAWN = Boolean.parseBoolean(pValue);
				break;
			case "altcastlefordusk":
				ALT_GAME_CASTLE_DUSK = Boolean.parseBoolean(pValue);
				break;
			case "altrequireclancastle":
				ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.parseBoolean(pValue);
				break;
			case "altfreeteleporting":
				ALT_GAME_FREE_TELEPORT = Boolean.parseBoolean(pValue);
				break;
			case "altsubclasswithoutquests":
				ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.parseBoolean(pValue);
				break;
			case "altsubclasseverywhere":
				ALT_GAME_SUBCLASS_EVERYWHERE = Boolean.parseBoolean(pValue);
				break;
			case "altmemberscanwithdrawfromclanwh":
				ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = Boolean.parseBoolean(pValue);
				break;
			case "dwarfrecipelimit":
				DWARF_RECIPE_LIMIT = Integer.parseInt(pValue);
				break;
			case "commonrecipelimit":
				COMMON_RECIPE_LIMIT = Integer.parseInt(pValue);
				break;
			case "championenable":
				L2JMOD_CHAMPION_ENABLE = Boolean.parseBoolean(pValue);
				break;
			case "championfrequency":
				L2JMOD_CHAMPION_FREQUENCY = Integer.parseInt(pValue);
				break;
			case "championminlevel":
				L2JMOD_CHAMP_MIN_LVL = Integer.parseInt(pValue);
				break;
			case "championmaxlevel":
				L2JMOD_CHAMP_MAX_LVL = Integer.parseInt(pValue);
				break;
			case "championhp":
				L2JMOD_CHAMPION_HP = Integer.parseInt(pValue);
				break;
			case "championhpregen":
				L2JMOD_CHAMPION_HP_REGEN = Float.parseFloat(pValue);
				break;
			case "championrewards":
				L2JMOD_CHAMPION_REWARDS = Integer.parseInt(pValue);
				break;
			case "championadenasrewards":
				L2JMOD_CHAMPION_ADENAS_REWARDS = Float.parseFloat(pValue);
				break;
			case "championatk":
				L2JMOD_CHAMPION_ATK = Float.parseFloat(pValue);
				break;
			case "championspdatk":
				L2JMOD_CHAMPION_SPD_ATK = Float.parseFloat(pValue);
				break;
			case "championrewardlowerlvlitemchance":
				L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE = Integer.parseInt(pValue);
				break;
			case "championrewardhigherlvlitemchance":
				L2JMOD_CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE = Integer.parseInt(pValue);
				break;
			case "championrewarditemid":
				L2JMOD_CHAMPION_REWARD_ID = Integer.parseInt(pValue);
				break;
			case "championrewarditemqty":
				L2JMOD_CHAMPION_REWARD_QTY = Integer.parseInt(pValue);
				break;
			case "championenableininstances":
				L2JMOD_CHAMPION_ENABLE_IN_INSTANCES = Boolean.parseBoolean(pValue);
				break;
			case "allowwedding":
				L2JMOD_ALLOW_WEDDING = Boolean.parseBoolean(pValue);
				break;
			case "weddingprice":
				L2JMOD_WEDDING_PRICE = Integer.parseInt(pValue);
				break;
			case "weddingpunishinfidelity":
				L2JMOD_WEDDING_PUNISH_INFIDELITY = Boolean.parseBoolean(pValue);
				break;
			case "weddingteleport":
				L2JMOD_WEDDING_TELEPORT = Boolean.parseBoolean(pValue);
				break;
			case "weddingteleportprice":
				L2JMOD_WEDDING_TELEPORT_PRICE = Integer.parseInt(pValue);
				break;
			case "weddingteleportduration":
				L2JMOD_WEDDING_TELEPORT_DURATION = Integer.parseInt(pValue);
				break;
			case "weddingallowsamesex":
				L2JMOD_WEDDING_SAMESEX = Boolean.parseBoolean(pValue);
				break;
			case "weddingformalwear":
				L2JMOD_WEDDING_FORMALWEAR = Boolean.parseBoolean(pValue);
				break;
			case "weddingdivorcecosts":
				L2JMOD_WEDDING_DIVORCE_COSTS = Integer.parseInt(pValue);
				break;
			case "enablewarehousesortingclan":
				L2JMOD_ENABLE_WAREHOUSESORTING_CLAN = Boolean.parseBoolean(pValue);
				break;
			case "enablewarehousesortingprivate":
				L2JMOD_ENABLE_WAREHOUSESORTING_PRIVATE = Boolean.parseBoolean(pValue);
				break;
			case "enablemanapotionsupport":
				L2JMOD_ENABLE_MANA_POTIONS_SUPPORT = Boolean.parseBoolean(pValue);
				break;
			case "displayservertime":
				L2JMOD_DISPLAY_SERVER_TIME = Boolean.parseBoolean(pValue);
				break;
			case "antifeedenable":
				L2JMOD_ANTIFEED_ENABLE = Boolean.parseBoolean(pValue);
				break;
			case "antifeeddualbox":
				L2JMOD_ANTIFEED_DUALBOX = Boolean.parseBoolean(pValue);
				break;
			case "antifeeddisconnectedasdualbox":
				L2JMOD_ANTIFEED_DISCONNECTED_AS_DUALBOX = Boolean.parseBoolean(pValue);
				break;
			case "antifeedinterval":
				L2JMOD_ANTIFEED_INTERVAL = 1000 * Integer.parseInt(pValue);
				break;
			case "cangmdropequipment":
				KARMA_DROP_GM = Boolean.parseBoolean(pValue);
				break;
			case "awardpkkillpvppoint":
				KARMA_AWARD_PK_KILL = Boolean.parseBoolean(pValue);
				break;
			case "minimumpkrequiredtodrop":
				KARMA_PK_LIMIT = Integer.parseInt(pValue);
				break;
			case "pvpvsnormaltime":
				PVP_NORMAL_TIME = Integer.parseInt(pValue);
				break;
			case "pvpvspvptime":
				PVP_PVP_TIME = Integer.parseInt(pValue);
				break;
			case "globalchat":
				DEFAULT_GLOBAL_CHAT = pValue;
				break;
			case "tradechat":
				DEFAULT_TRADE_CHAT = pValue;
				break;
			default:
				try
				{
					// TODO: stupid GB configs...
					if (!pName.startsWith("Interval_") && !pName.startsWith("Random_"))
					{
						pName = pName.toUpperCase();
					}
					Field clazField = Config.class.getField(pName);
					int modifiers = clazField.getModifiers();
					// just in case :)
					if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers) || Modifier.isFinal(modifiers))
					{
						throw new SecurityException("Cannot modify non public, non static or final config!");
					}
					
					if (clazField.getType() == int.class)
					{
						clazField.setInt(clazField, Integer.parseInt(pValue));
					}
					else if (clazField.getType() == short.class)
					{
						clazField.setShort(clazField, Short.parseShort(pValue));
					}
					else if (clazField.getType() == byte.class)
					{
						clazField.setByte(clazField, Byte.parseByte(pValue));
					}
					else if (clazField.getType() == long.class)
					{
						clazField.setLong(clazField, Long.parseLong(pValue));
					}
					else if (clazField.getType() == float.class)
					{
						clazField.setFloat(clazField, Float.parseFloat(pValue));
					}
					else if (clazField.getType() == double.class)
					{
						clazField.setDouble(clazField, Double.parseDouble(pValue));
					}
					else if (clazField.getType() == boolean.class)
					{
						clazField.setBoolean(clazField, Boolean.parseBoolean(pValue));
					}
					else if (clazField.getType() == String.class)
					{
						clazField.set(clazField, pValue);
					}
					else
					{
						return false;
					}
				}
				catch (NoSuchFieldException e)
				{
					return false;
				}
				catch (Exception e)
				{
					_log.warn(String.valueOf(e));
					return false;
				}
		}
		return true;
	}
	
	/**
	 * Save hexadecimal ID of the server in the L2Properties file.<br>
	 * Check {@link #HEXID_FILE}.
	 * @param serverId the ID of the server whose hexId to save
	 * @param hexId the hexadecimal ID to store
	 */
	public static void saveHexid(int serverId, String hexId)
	{
		Config.saveHexid(serverId, hexId, HEXID_FILE);
	}
	
	/**
	 * Save hexadecimal ID of the server in the L2Properties file.
	 * @param serverId the ID of the server whose hexId to save
	 * @param hexId the hexadecimal ID to store
	 * @param fileName name of the L2Properties file
	 */
	public static void saveHexid(int serverId, String hexId, String fileName)
	{
		try
		{
			Properties hexSetting = new Properties();
			File file = new File(fileName);
			// Create a new empty file only if it doesn't exist
			file.createNewFile();
			try (OutputStream out = new FileOutputStream(file))
			{
				hexSetting.setProperty("ServerID", String.valueOf(serverId));
				hexSetting.setProperty("HexID", hexId);
				hexSetting.store(out, "the hexID to auth into login");
			}
		}
		catch (Exception e)
		{
			_log.warn(StringUtil.concat("Failed to save hex id to ", fileName, " File."));
			_log.warn("Config: " + e.getMessage());
		}
	}
	
	/**
	 * Loads flood protector configurations.
	 * @param properties the properties object containing the actual values of the flood protector configs
	 */
	private static void loadFloodProtectorConfigs(final PropertiesParser properties)
	{
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_USE_ITEM, "UseItem", 4);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ROLL_DICE, "RollDice", 42);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_FIREWORK, "Firework", 42);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ITEM_PET_SUMMON, "ItemPetSummon", 16);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_HERO_VOICE, "HeroVoice", 100);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_GLOBAL_CHAT, "GlobalChat", 5);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SUBCLASS, "Subclass", 20);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_DROP_ITEM, "DropItem", 10);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SERVER_BYPASS, "ServerBypass", 5);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MULTISELL, "MultiSell", 1);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_TRANSACTION, "Transaction", 10);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MANUFACTURE, "Manufacture", 3);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MANOR, "Manor", 30);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SENDMAIL, "SendMail", 100);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_CHARACTER_SELECT, "CharacterSelect", 30);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ITEM_AUCTION, "ItemAuction", 9);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SHOUT_CHAT, "ShoutChat", 15);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_TRADE_CHAT, "TradeChat", 20);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_CLAN_CHAT, "ClanChat", 8);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ALLY_CHAT, "AllyChat", 8);
	}
	
	/**
	 * Loads single flood protector configuration.
	 * @param properties properties file reader
	 * @param config flood protector configuration instance
	 * @param configString flood protector configuration string that determines for which flood protector configuration should be read
	 * @param defaultInterval default flood protector interval
	 */
	private static void loadFloodProtectorConfig(final PropertiesParser properties, final FloodProtectorConfig config, final String configString, final int defaultInterval)
	{
		config.FLOOD_PROTECTION_INTERVAL = properties.getInt(StringUtil.concat("FloodProtector", configString, "Interval"), defaultInterval);
		config.LOG_FLOODING = properties.getBoolean(StringUtil.concat("FloodProtector", configString, "LogFlooding"), false);
		config.PUNISHMENT_LIMIT = properties.getInt(StringUtil.concat("FloodProtector", configString, "PunishmentLimit"), 0);
		config.PUNISHMENT_TYPE = properties.getString(StringUtil.concat("FloodProtector", configString, "PunishmentType"), "none");
		config.PUNISHMENT_TIME = properties.getInt(StringUtil.concat("FloodProtector", configString, "PunishmentTime"), 0);
	}
	
	public static int getServerTypeId(String[] serverTypes)
	{
		int tType = 0;
		for (String cType : serverTypes)
		{
			switch (cType.trim().toLowerCase())
			{
				case "Normal":
					tType |= 0x01;
					break;
				case "Relax":
					tType |= 0x02;
					break;
				case "Test":
					tType |= 0x04;
					break;
				case "NoLabel":
					tType |= 0x08;
					break;
				case "Restricted":
					tType |= 0x10;
					break;
				case "Event":
					tType |= 0x20;
					break;
				case "Free":
					tType |= 0x40;
					break;
				default:
					break;
			}
		}
		return tType;
	}
	
	public static class ClassMasterSettings
	{
		private final Map<Integer, Map<Integer, Integer>> _claimItems;
		private final Map<Integer, Map<Integer, Integer>> _rewardItems;
		private final Map<Integer, Boolean> _allowedClassChange;
		
		public ClassMasterSettings(String _configLine)
		{
			_claimItems = new HashMap<>(3);
			_rewardItems = new HashMap<>(3);
			_allowedClassChange = new HashMap<>(3);
			if (_configLine != null)
			{
				parseConfigLine(_configLine.trim());
			}
		}
		
		private void parseConfigLine(String _configLine)
		{
			StringTokenizer st = new StringTokenizer(_configLine, ";");
			
			while (st.hasMoreTokens())
			{
				// get allowed class change
				int job = Integer.parseInt(st.nextToken());
				
				_allowedClassChange.put(job, true);
				
				Map<Integer, Integer> _items = new HashMap<>();
				// parse items needed for class change
				if (st.hasMoreTokens())
				{
					StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
					
					while (st2.hasMoreTokens())
					{
						StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
						int _itemId = Integer.parseInt(st3.nextToken());
						int _quantity = Integer.parseInt(st3.nextToken());
						_items.put(_itemId, _quantity);
					}
				}
				
				_claimItems.put(job, _items);
				
				_items = new HashMap<>();
				// parse gifts after class change
				if (st.hasMoreTokens())
				{
					StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
					
					while (st2.hasMoreTokens())
					{
						StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
						int _itemId = Integer.parseInt(st3.nextToken());
						int _quantity = Integer.parseInt(st3.nextToken());
						_items.put(_itemId, _quantity);
					}
				}
				
				_rewardItems.put(job, _items);
			}
		}
		
		public boolean isAllowed(int job)
		{
			if (_allowedClassChange == null)
			{
				return false;
			}
			if (_allowedClassChange.containsKey(job))
			{
				return _allowedClassChange.get(job);
			}
			
			return false;
		}
		
		public Map<Integer, Integer> getRewardItems(int job)
		{
			if (_rewardItems.containsKey(job))
			{
				return _rewardItems.get(job);
			}
			
			return null;
		}
		
		public Map<Integer, Integer> getRequireItems(int job)
		{
			if (_claimItems.containsKey(job))
			{
				return _claimItems.get(job);
			}
			
			return null;
		}
	}
	
	/**
	 * @param line the string line to parse
	 * @return a parsed float map
	 */
	private static Map<Integer, Float> parseConfigLine(String line)
	{
		String[] propertySplit = line.split(",");
		Map<Integer, Float> ret = new HashMap<>(propertySplit.length);
		int i = 0;
		for (String value : propertySplit)
		{
			ret.put(i++, Float.parseFloat(value));
		}
		return ret;
	}
	
	/**
	 * Parse a config value from its string representation to a two-dimensional int array.<br>
	 * The format of the value to be parsed should be as follows: "item1Id,item1Amount;item2Id,item2Amount;...itemNId,itemNAmount".
	 * @param line the value of the parameter to parse
	 * @return the parsed list or {@code null} if nothing was parsed
	 */
	private static int[][] parseItemsList(String line)
	{
		final String[] propertySplit = line.split(";");
		if (propertySplit.length == 0)
		{
			// nothing to do here
			return null;
		}
		
		int i = 0;
		String[] valueSplit;
		final int[][] result = new int[propertySplit.length][];
		int[] tmp;
		for (String value : propertySplit)
		{
			valueSplit = value.split(",");
			if (valueSplit.length != 2)
			{
				_log.warn(StringUtil.concat("parseItemsList[Config.load()]: invalid entry -> \"", valueSplit[0], "\", should be itemId,itemNumber. Skipping to the next entry in the list."));
				continue;
			}
			
			tmp = new int[2];
			try
			{
				tmp[0] = Integer.parseInt(valueSplit[0]);
			}
			catch (NumberFormatException e)
			{
				_log.warn(StringUtil.concat("parseItemsList[Config.load()]: invalid itemId -> \"", valueSplit[0], "\", value must be an integer. Skipping to the next entry in the list."));
				continue;
			}
			try
			{
				tmp[1] = Integer.parseInt(valueSplit[1]);
			}
			catch (NumberFormatException e)
			{
				_log.warn(StringUtil.concat("parseItemsList[Config.load()]: invalid item number -> \"", valueSplit[1], "\", value must be an integer. Skipping to the next entry in the list."));
				continue;
			}
			result[i++] = tmp;
		}
		return result;
	}
	
	private static class IPConfigData extends DocumentParser
	{
		private static final List<String> _subnets = new ArrayList<>(5);
		private static final List<String> _hosts = new ArrayList<>(5);
		
		public IPConfigData()
		{
			load();
		}
		
		@Override
		public void load()
		{
			File f = new File(IP_CONFIG_FILE);
			if (f.exists())
			{
				_log.info("Network Config: ipconfig.xml exists using manual configuration...");
				parseFile(new File(IP_CONFIG_FILE));
			}
			else
			// Auto configuration...
			{
				_log.info("Network Config: ipconfig.xml doesn't exists using automatic configuration...");
				autoIpConfig();
			}
		}
		
		@Override
		protected void parseDocument()
		{
			NamedNodeMap attrs;
			for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("gameserver".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("define".equalsIgnoreCase(d.getNodeName()))
						{
							attrs = d.getAttributes();
							_subnets.add(attrs.getNamedItem("subnet").getNodeValue());
							_hosts.add(attrs.getNamedItem("address").getNodeValue());
							
							if (_hosts.size() != _subnets.size())
							{
								_log.warn("Failed to Load " + IP_CONFIG_FILE + " File - subnets does not match server addresses.");
							}
						}
					}
					
					Node att = n.getAttributes().getNamedItem("address");
					if (att == null)
					{
						_log.warn("Failed to load " + IP_CONFIG_FILE + " file - default server address is missing.");
						_hosts.add("127.0.0.1");
					}
					else
					{
						_hosts.add(att.getNodeValue());
					}
					_subnets.add("0.0.0.0/0");
				}
			}
		}
		
		protected void autoIpConfig()
		{
			String externalIp = "127.0.0.1";
			try
			{
				URL autoIp = new URL("http://ip1.dynupdate.no-ip.com:8245/");
				try (BufferedReader in = new BufferedReader(new InputStreamReader(autoIp.openStream())))
				{
					externalIp = in.readLine();
				}
			}
			catch (IOException e)
			{
				_log.info("Network Config: Failed to connect to ip1.dynupdate.no-ip.com please check your internet connection using 127.0.0.1!");
				externalIp = "127.0.0.1";
			}
			
			try
			{
				Enumeration<NetworkInterface> niList = NetworkInterface.getNetworkInterfaces();
				
				Subnet sub = new Subnet();
				while (niList.hasMoreElements())
				{
					NetworkInterface ni = niList.nextElement();
					
					if (!ni.isUp() || ni.isVirtual())
					{
						continue;
					}
					
					if (!ni.isLoopback() && ((ni.getHardwareAddress() == null) || (ni.getHardwareAddress().length != 6)))
					{
						continue;
					}
					
					for (InterfaceAddress ia : ni.getInterfaceAddresses())
					{
						if (ia.getAddress() instanceof Inet6Address)
						{
							continue;
						}
						
						sub.setIPAddress(ia.getAddress().getHostAddress());
						sub.setMaskedBits(ia.getNetworkPrefixLength());
						String subnet = sub.getSubnetAddress() + '/' + sub.getMaskedBits();
						if (!_subnets.contains(subnet) && !subnet.equals("0.0.0.0/0"))
						{
							_subnets.add(subnet);
							_hosts.add(sub.getIPAddress());
							_log.info("Network Config: Adding new subnet: " + subnet + " address: " + sub.getIPAddress());
						}
					}
				}
				
				// External host and subnet
				_hosts.add(externalIp);
				_subnets.add("0.0.0.0/0");
				_log.info("Network Config: Adding new subnet: 0.0.0.0/0 address: " + externalIp);
			}
			catch (SocketException e)
			{
				_log.info("Network Config: Configuration failed please configure manually using ipconfig.xml", e);
				System.exit(0);
			}
		}
		
		protected List<String> getSubnets()
		{
			if (_subnets.isEmpty())
			{
				return Arrays.asList("0.0.0.0/0");
			}
			return _subnets;
		}
		
		protected List<String> getHosts()
		{
			if (_hosts.isEmpty())
			{
				return Arrays.asList("127.0.0.1");
			}
			return _hosts;
		}
	}
}
