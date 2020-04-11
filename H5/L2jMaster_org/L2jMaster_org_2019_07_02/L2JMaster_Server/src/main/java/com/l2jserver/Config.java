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
package com.l2jserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jserver.gameserver.GameServer;
import com.l2jserver.gameserver.enums.IllegalActionPunishmentType;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;
import com.l2jserver.gameserver.util.FloodProtectorConfig;
import com.l2jserver.gameserver.util.Util;
import com.l2jserver.util.PropertiesParser;
import com.l2jserver.util.StringUtil;
import com.l2jserver.util.data.xml.IXmlReader;

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
	public static final String EOL = System.lineSeparator();
	
	// --------------------------------------------------
	// L2J Property File Definitions
	// --------------------------------------------------
	public static final String CHARACTER_CONFIG_FILE = "./config/Character.ini";
	public static final String FEATURE_CONFIG_FILE = "./config/Feature.ini";
	public static final String FORTSIEGE_CONFIGURATION_FILE = "./config/FortSiege.ini";
	public static final String GENERAL_CONFIG_FILE = "./config/General.ini";
	public static final String HEXID_FILE = "./config/hexid.txt";
	public static final String ID_CONFIG_FILE = "./config/IdFactory.ini";
	public static final String L2JMOD_CONFIG_FILE = "./config/L2JMods.ini";
	public static final String LOGIN_CONFIGURATION_FILE = "./config/LoginServer.ini";
	public static final String MASTERMODS_FILE = "./config/MasterMods.ini";
	public static final String MASTERPREMIUM_FILE = "./config/MasterPremium.ini";
	public static final String MASTERCB_FILE = "./config/MasterCB.ini";
	public static final String NPC_CONFIG_FILE = "./config/NPC.ini";
	public static final String PVP_CONFIG_FILE = "./config/PVP.ini";
	public static final String RATES_CONFIG_FILE = "./config/Rates.ini";
	public static final String CONFIGURATION_FILE = "./config/Server.ini";
	public static final String IP_CONFIG_FILE = "./config/ipconfig.xml";
	public static final String SIEGE_CONFIGURATION_FILE = "./config/Siege.ini";
	public static final String TW_CONFIGURATION_FILE = "./config/TerritoryWar.ini";
	public static final String TELNET_FILE = "./config/Telnet.ini";
	public static final String FLOOD_PROTECTOR_FILE = "./config/FloodProtector.ini";
	public static final String MMO_CONFIG_FILE = "./config/MMO.ini";
	public static final String OLYMPIAD_CONFIG_FILE = "./config/Olympiad.ini";
	public static final String GRANDBOSS_CONFIG_FILE = "./config/GrandBoss.ini";
	public static final String GRACIASEEDS_CONFIG_FILE = "./config/GraciaSeeds.ini";
	public static final String CHAT_FILTER_FILE = "./config/chatfilter.txt";
	public static final String CH_SIEGE_FILE = "./config/ConquerableHallSiege.ini";
	public static final String GEODATA_FILE = "./config/GeoData.ini";
	public static final String VOTE_FILE = "./config/VoteSystem.ini";
	
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
	public static boolean ALT_GAME_MAGICFAILURES;
	public static int PLAYER_FAKEDEATH_UP_PROTECTION;
	public static boolean STORE_SKILL_COOLTIME;
	public static boolean SUBCLASS_STORE_SKILL_COOLTIME;
	public static boolean SUMMON_STORE_SKILL_COOLTIME;
	public static boolean ALT_GAME_SHIELD_BLOCKS;
	public static int ALT_PERFECT_SHLD_BLOCK;
	public static long EFFECT_TICK_RATIO;
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
	public static int MAX_RUN_SPEED;
	public static int MAX_PCRIT_RATE;
	public static int MAX_MCRIT_RATE;
	public static int MAX_PATK_SPEED;
	public static int MAX_MATK_SPEED;
	public static int MAX_EVASION;
	public static int MIN_ABNORMAL_STATE_SUCCESS_RATE;
	public static int MAX_ABNORMAL_STATE_SUCCESS_RATE;
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
	public static boolean CASTLE_BUFF_FREE;
	
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
	public static boolean FORTRESS_BUFF_FREE;
	
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
	public static boolean GAMEGUARD_ENFORCE;
	public static boolean GAMEGUARD_PROHIBITACTION;
	public static boolean LOG_CHAT;
	public static boolean LOG_AUTO_ANNOUNCEMENTS;
	public static boolean LOG_ITEMS;
	public static boolean LOG_ITEMS_SMALL_LOG;
	public static boolean LOG_ITEM_ENCHANTS;
	public static boolean LOG_SKILL_ENCHANTS;
	public static boolean GMAUDIT;
	public static boolean SKILL_CHECK_ENABLE;
	public static boolean SKILL_CHECK_REMOVE;
	public static boolean SKILL_CHECK_GM;
	public static boolean DEBUG;
	public static boolean DEBUG_INSTANCES;
	public static boolean HTML_ACTION_CACHE_DEBUG;
	public static boolean PACKET_HANDLER_DEBUG;
	public static boolean DEVELOPER;
	public static boolean ALT_DEV_NO_HANDLERS;
	public static boolean ALT_DEV_NO_QUESTS;
	public static boolean ALT_DEV_NO_SPAWNS;
	public static boolean ALT_DEV_SHOW_QUESTS_LOAD_IN_LOGS;
	public static boolean ALT_DEV_SHOW_SCRIPTS_LOAD_IN_LOGS;
	public static int THREAD_P_EFFECTS;
	public static int THREAD_P_GENERAL;
	public static int THREAD_E_EVENTS;
	public static int GENERAL_PACKET_THREAD_CORE_SIZE;
	public static int IO_PACKET_THREAD_CORE_SIZE;
	public static int GENERAL_THREAD_CORE_SIZE;
	public static int AI_MAX_THREAD;
	public static int EVENT_MAX_THREAD;
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
	public static boolean ENABLE_FALLING_DAMAGE;
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
	public static int INSTANCE_FINISH_TIME;
	public static boolean RESTORE_PLAYER_INSTANCE;
	public static boolean ALLOW_SUMMON_IN_INSTANCE;
	public static int EJECT_DEAD_PLAYER_TIME;
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
	public static boolean USE_SAY_FILTER;
	public static String CHAT_FILTER_CHARS;
	public static int[] BAN_CHAT_CHANNELS;
	public static boolean ENABLE_CUSTOM_PERIOD;
	public static int[] ALT_OLY_END_DATE;
	public static int[] ALT_OLY_END_HOUR = new int[3];
	public static int ALT_OLY_START_TIME;
	public static int ALT_OLY_MIN;
	public static int ALT_OLY_MAX_BUFFS;
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
	public static int ALT_MANOR_REFRESH_TIME;
	public static int ALT_MANOR_REFRESH_MIN;
	public static int ALT_MANOR_APPROVE_TIME;
	public static int ALT_MANOR_APPROVE_MIN;
	public static int ALT_MANOR_MAINTENANCE_MIN;
	public static boolean ALT_MANOR_SAVE_ALL_ACTIONS;
	public static int ALT_MANOR_SAVE_PERIOD_RATE;
	public static long ALT_LOTTERY_PRIZE;
	public static long ALT_LOTTERY_TICKET_PRICE;
	public static float ALT_LOTTERY_5_NUMBER_RATE;
	public static float ALT_LOTTERY_4_NUMBER_RATE;
	public static float ALT_LOTTERY_3_NUMBER_RATE;
	public static long ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
	public static boolean ALT_FISH_CHAMPIONSHIP_ENABLED;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_ITEM;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_1;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_2;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_3;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_4;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_5;
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
	public static boolean CUSTOM_NPC_DATA;
	public static boolean CUSTOM_TELEPORT_TABLE;
	public static boolean CUSTOM_NPCBUFFER_TABLES;
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
	public static boolean KRATEIS_CUBE_EVENT_ENABLED;
	public static boolean KRATEIS_CUBE_EVENT_LOG_ENABLED;
	public static int KRATEIS_CUBE_REGISTRATION_TIME;
	public static int KRATEIS_CUBE_RUNNING_TIME;
	public static int KRATEIS_CUBE_TIME_TO_REVIVE;
	public static int KRATEIS_CUBE_TIME_OF_PROTECTION;
	public static int KRATEIS_CUBE_MIN_PLAYER;
	public static int KRATEIS_CUBE_MAX_PLAYER;
	public static int KRATEIS_REWARD_TO_KILL_PLAYER;
	public static int KRATEIS_REWARD_TO_KILL_MOB;
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
	public static float L2JMOD_CHAMPION_REWARDS_EXP_SP;
	public static float L2JMOD_CHAMPION_REWARDS_CHANCE;
	public static float L2JMOD_CHAMPION_REWARDS_AMOUNT;
	public static float L2JMOD_CHAMPION_ADENAS_REWARDS_CHANCE;
	public static float L2JMOD_CHAMPION_ADENAS_REWARDS_AMOUNT;
	public static float L2JMOD_CHAMPION_HP_REGEN;
	public static float L2JMOD_CHAMPION_ATK;
	public static float L2JMOD_CHAMPION_SPD_ATK;
	public static int L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE;
	public static int L2JMOD_CHAMPION_REWARD_SAME_LVL_ITEM_CHANCE;
	public static int L2JMOD_CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE;
	public static int L2JMOD_CHAMPION_REWARD_ID;
	public static int L2JMOD_CHAMPION_REWARD_QTY;
	public static boolean L2JMOD_CHAMPION_ENABLE_VITALITY;
	public static boolean L2JMOD_CHAMPION_ENABLE_IN_INSTANCES;
	public static int L2JMOD_CHAMPION_ENABLE_AURA;
	public static boolean TVT_EVENT_ENABLED;
	public static boolean TVT_EVENT_IN_INSTANCE;
	public static String TVT_EVENT_INSTANCE_FILE;
	public static String[] TVT_EVENT_INTERVAL;
	public static int TVT_EVENT_PARTICIPATION_TIME;
	public static int TVT_EVENT_RUNNING_TIME;
	public static int TVT_EVENT_PARTICIPATION_NPC_ID;
	public static int[] TVT_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
	public static int[] TVT_EVENT_PARTICIPATION_FEE = new int[2];
	public static int TVT_EVENT_MIN_PLAYERS_IN_TEAMS;
	public static int TVT_EVENT_MAX_PLAYERS_IN_TEAMS;
	public static int TVT_EVENT_RESPAWN_TELEPORT_DELAY;
	public static int TVT_EVENT_START_LEAVE_TELEPORT_DELAY;
	public static String TVT_EVENT_TEAM_1_NAME;
	public static int[] TVT_EVENT_TEAM_1_COORDINATES = new int[3];
	public static String TVT_EVENT_TEAM_2_NAME;
	public static int[] TVT_EVENT_TEAM_2_COORDINATES = new int[3];
	public static List<int[]> TVT_EVENT_REWARDS;
	public static boolean TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED;
	public static boolean TVT_EVENT_SCROLL_ALLOWED;
	public static boolean TVT_EVENT_POTIONS_ALLOWED;
	public static boolean TVT_EVENT_SUMMON_BY_ITEM_ALLOWED;
	public static List<Integer> TVT_DOORS_IDS_TO_OPEN;
	public static List<Integer> TVT_DOORS_IDS_TO_CLOSE;
	public static boolean TVT_REWARD_TEAM_TIE;
	public static byte TVT_EVENT_MIN_LVL;
	public static byte TVT_EVENT_MAX_LVL;
	public static int TVT_EVENT_EFFECTS_REMOVAL;
	public static Map<Integer, Integer> TVT_EVENT_FIGHTER_BUFFS;
	public static Map<Integer, Integer> TVT_EVENT_MAGE_BUFFS;
	public static int TVT_EVENT_MAX_PARTICIPANTS_PER_IP;
	public static boolean TVT_ALLOW_VOICED_COMMAND;
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
	// MasterMods Settings
	// --------------------------------------------------
	public static boolean ENABLE_ARCHIEVEMENTS;
	public static boolean ANNOUNCE_RAIDBOSS_ALIVE_KILL;
	public static boolean ANNOUNCE_GRANDBOSS_ALIVE_KILL;
	public static boolean ANNOUNCE_BOSS_SPAWN;
	public static boolean ANNOUNCE_BOSS_SPAWN_CRIT;
	public static String ANNOUNCE_BOSS_MSG;
	public static boolean ANNOUNCE_CASTLE_LORDS_ON_ENTER;
	public static boolean ANNOUNCE_HERO_ON_ENTER;
	public static boolean DEAD_DISABLE_CHAT;
	public static boolean CHAT_PROTECTION;
	public static int CHAT_LEVEL_PROTECTION;
	public static boolean GAME_SERVER_AUTO_RESTART;
	public static boolean GAME_SERVER_SHOW_MESSAGE_INFO;
	public static int GAME_SERVER_AUTO_RESTART_COUNTDOWN;
	public static String[] GAME_SERVER_AUTO_RESTART_INTERVAL;
	public static boolean RESTORE_CANCELED_BUFFS_ENABLED;
	public static boolean RESTORE_STOLEN_BUFFS_ENABLED;
	public static int RESTORE_CANCELED_BUFFS_TIME;
	public static int RESTORE_STOLEN_BUFFS_TIME;
	public static boolean CLAN_LEADER_COLOR_ENABLED;
	public static int CLAN_LEADER_COLOR;
	public static int CLAN_LEADER_COLOR_CLAN_LEVEL;
	public static boolean CLAN_LEADER_TITLE_ENABLED;
	public static int CLAN_LEADER_TITLE;
	public static int CLAN_LEADER_TITLE_CLAN_LEVEL;
	public static boolean USE_CUSTOM_CLANHALLS;
	public static String STARTING_TITLE;
	public static boolean ALLOW_LIGHT_USE_HEAVY;
	public static String NOTALLOWCLASS;
	public static List<Integer> NOTALLOWEDUSEHEAVY;
	public static boolean ALLOW_HEAVY_USE_LIGHT;
	public static String NOTALLOWCLASSE;
	public static List<Integer> NOTALLOWEDUSELIGHT;
	public static boolean ALT_DISABLE_BOW_CLASSES;
	public static String DISABLE_BOW_CLASSES_STRING;
	public static ArrayList<Integer> DISABLE_BOW_CLASSES = new ArrayList<>();
	public static boolean ALT_DISABLE_DAGGER_CLASSES;
	public static String DISABLE_DAGGER_CLASSES_STRING;
	public static ArrayList<Integer> DISABLE_DAGGER_CLASSES = new ArrayList<>();
	public static boolean ALT_DISABLE_SWORD_CLASSES;
	public static String DISABLE_SWORD_CLASSES_STRING;
	public static ArrayList<Integer> DISABLE_SWORD_CLASSES = new ArrayList<>();
	public static boolean ALT_DISABLE_BLUNT_CLASSES;
	public static String DISABLE_BLUNT_CLASSES_STRING;
	public static ArrayList<Integer> DISABLE_BLUNT_CLASSES = new ArrayList<>();
	public static boolean ALT_DISABLE_DUAL_CLASSES;
	public static String DISABLE_DUAL_CLASSES_STRING;
	public static ArrayList<Integer> DISABLE_DUAL_CLASSES = new ArrayList<>();
	public static boolean ALT_DISABLE_POLE_CLASSES;
	public static String DISABLE_POLE_CLASSES_STRING;
	public static ArrayList<Integer> DISABLE_POLE_CLASSES = new ArrayList<>();
	public static boolean ALT_DISABLE_BIGSWORD_CLASSES;
	public static String DISABLE_BIGSWORD_CLASSES_STRING;
	public static ArrayList<Integer> DISABLE_BIGSWORD_CLASSES = new ArrayList<>();
	public static boolean RANK_ARENA_ACCEPT_SAME_IP;
	public static boolean RANK_ARENA_ENABLED;
	public static int RANK_ARENA_INTERVAL;
	public static int RANK_ARENA_REWARD_ID;
	public static int RANK_ARENA_REWARD_COUNT;
	public static boolean RANK_FISHERMAN_ENABLED;
	public static int RANK_FISHERMAN_INTERVAL;
	public static int RANK_FISHERMAN_REWARD_ID;
	public static int RANK_FISHERMAN_REWARD_COUNT;
	public static boolean RANK_CRAFT_ENABLED;
	public static int RANK_CRAFT_INTERVAL;
	public static int RANK_CRAFT_REWARD_ID;
	public static int RANK_CRAFT_REWARD_COUNT;
	public static boolean RANK_TVT_ENABLED;
	public static int RANK_TVT_INTERVAL;
	public static int RANK_TVT_REWARD_ID;
	public static int RANK_TVT_REWARD_COUNT;
	public static boolean ALLOW_REPAIR_COMMAND;
	public static boolean ENABLE_ONLINE_STATUS;
	public static boolean ONLINE_STATUS_HTML;
	public static boolean ONLINE_STATUS_SHOW_OFFLINE;
	public static boolean TELEPORT_TO_MOB_COMMAND;
	public static boolean TELEPORT_TO_TOWN_COMMNAND;
	public static boolean COMBINE_TALISMAN_ENABLED;
	public static boolean ALLOW_EPIC_COMMAND;
	public static boolean ALLOW_CL_COMMAND;
	public static boolean ALLOW_EXP_COMMAND;
	public static boolean PC_BANG_ENABLED;
	public static int PC_BANG_MAX_POINTS;
	public static boolean PC_BANG_ENABLE_DOUBLE_POINTS;
	public static int PC_BANG_DOUBLE_POINTS_CHANCE;
	public static double PC_BANG_POINT_RATE;
	public static boolean PC_BANG_RANDOM_POINT;
	public static boolean PC_BANG_REWARD_LOW_EXP_KILLS;
	public static int PC_BANG_LOW_EXP_KILLS_CHANCE;
	public static boolean PRIME_SHOP_POINTS_ENABLED;
	public static int PRIME_SHOP_POINTS_MAX;
	public static int PRIME_SHOP_POINTS_ID;
	public static boolean PVP_SYSTEM_QUAKE;
	public static boolean PK_SYSTEM_QUAKE;
	public static boolean VISUAL_EQUIP_ENABLE;
	public static boolean ALLOW_HTML_ON_VISUAL_EQUIP;
	public static boolean ALLOW_SHIELD_ON_VISUALEQUIP;
	public static boolean ALLOW_WEAPON_ON_VISUALEQUIP;
	public static boolean VISUAL_EQUIP_PREMIUM_FREE;
	public static boolean VISUAL_EQUIP_COST_ENABLE;
	public static boolean VISUAL_EQUIP_INFINITE_ENABLE;
	public static boolean VISUAL_EQUIP_INFINITE_ARMOR_ENABLE;
	public static boolean VISUAL_EQUIP_INFINITE_RACE_ENABLE;
	public static boolean VISUAL_EQUIP_INFINITE_SHIELD_ENABLE;
	public static boolean VISUAL_EQUIP_INFINITE_WEAPON_ENABLE;
	public static int VISUAL_EQUIP_COST;
	public static int VISUAL_EQUIP_CONSUME_ID;
	public static boolean VISUAL_EQUIP_PREMIUM_ONLY;
	public static boolean VISUAL_EQUIP_HERO_FREE;
	public static boolean PVP_COLOR_SYSTEM_ENABLED;
	public static int PVP_AMOUNT1;
	public static int PVP_AMOUNT2;
	public static int PVP_AMOUNT3;
	public static int PVP_AMOUNT4;
	public static int PVP_AMOUNT5;
	public static int NAME_COLOR_FOR_PVP_AMOUNT1;
	public static int NAME_COLOR_FOR_PVP_AMOUNT2;
	public static int NAME_COLOR_FOR_PVP_AMOUNT3;
	public static int NAME_COLOR_FOR_PVP_AMOUNT4;
	public static int NAME_COLOR_FOR_PVP_AMOUNT5;
	public static boolean PK_COLOR_SYSTEM_ENABLED;
	public static int PK_AMOUNT1;
	public static int PK_AMOUNT2;
	public static int PK_AMOUNT3;
	public static int PK_AMOUNT4;
	public static int PK_AMOUNT5;
	public static int TITLE_COLOR_FOR_PK_AMOUNT1;
	public static int TITLE_COLOR_FOR_PK_AMOUNT2;
	public static int TITLE_COLOR_FOR_PK_AMOUNT3;
	public static int TITLE_COLOR_FOR_PK_AMOUNT4;
	public static int TITLE_COLOR_FOR_PK_AMOUNT5;
	public static boolean DUALBOX_PROTECTION_ENABLED;
	public static Integer DUALBOX_MAX_ALLOWED_PER_PC;
	public static Integer DUALBOX_PREMIUM_MAX_ALLOWED_PER_PC;
	public static boolean ALLOW_CUSTOM_MAX_AGGRO_RANGE;
	public static int CUSTOM_MAX_AGGRO_RANGE;
	public static boolean SPAWN_CHAR;
	public static int SPAWN_X;
	public static int SPAWN_Y;
	public static int SPAWN_Z;
	public static boolean CUSTOM_SPAWN_FOR_RACE;
	public static int SPAWN_HUMAN_X;
	public static int SPAWN_HUMAN_Y;
	public static int SPAWN_HUMAN_Z;
	public static int SPAWN_ELF_X;
	public static int SPAWN_ELF_Y;
	public static int SPAWN_ELF_Z;
	public static int SPAWN_DARKELF_X;
	public static int SPAWN_DARKELF_Y;
	public static int SPAWN_DARKELF_Z;
	public static int SPAWN_ORC_X;
	public static int SPAWN_ORC_Y;
	public static int SPAWN_ORC_Z;
	public static int SPAWN_DWARF_X;
	public static int SPAWN_DWARF_Y;
	public static int SPAWN_DWARF_Z;
	public static int SPAWN_KAMAEL_X;
	public static int SPAWN_KAMAEL_Y;
	public static int SPAWN_KAMAEL_Z;
	
	// --------------------------------------------------
	// MasterCB Settings
	// --------------------------------------------------
	public static boolean ENABLE_COMMUNITY_BOARD;
	public static boolean ALLOW_CUSTOM_CB;
	public static boolean ZEUS_ACTIVE;
	public static boolean COMMUNITY_BOARD_ON_PEACE_ZONE;
	public static boolean COMMUNITY_BOARD_ON_COMBAT;
	public static String BBS_DEFAULT;
	public static int COMMUNITY_TYPE;
	public static boolean ALLOW_COMMUNITY_STATS;
	public static boolean ALLOW_COMMUNITY_TELEPORT;
	public static boolean ALLOW_COMMUNITY_SERVICES;
	public static boolean ALLOW_COMMUNITY_MULTISELL;
	public static boolean ALLOW_COMMUNITY_BUFFER;
	public static int BUFF_CONSUMABLE_ID;
	public static boolean RAID_LIST_SORT_ASC;
	public static int RAID_LIST_RESULTS;
	public static int RAID_LIST_ROW_HEIGHT;
	public static int TOP_PLAYER_RESULTS;
	public static int TOP_PLAYER_ROW_HEIGHT;
	public static boolean SHOW_LEVEL_COMMUNITYBOARD;
	public static int NAME_PAGE_SIZE_COMMUNITYBOARD;
	public static boolean BBS_SHOW_PLAYERLIST;
	public static int NAME_PER_ROW_COMMUNITYBOARD;
	public static int NoblItemId;
	public static long NoblItemCount;
	public static int GenderItemId;
	public static long GenderItemCount;
	public static int DelevelItemId;
	public static long DelevelItemCount;
	public static int HeroItemId;
	public static long HeroItemCount;
	public static int RecoveryVitalityItemId;
	public static long RecoveryVitalityItemCount;
	public static int SPItemId;
	public static long SPItemCount;
	public static int REGION_LOAD_TIMER;
	public static boolean ONLY_USE_SHOP_PEACE_ZONE;
	
	// --------------------------------------------------
	// MasterPremium Settings
	// --------------------------------------------------
	public static boolean ANTIBOT_CAPTCHA_ENABLE;
	public static int ANTIBOT_CAPTCHA_KILL_AMOUNT;
	public static long ANTIBOT_CAPTCHA_TIME;
	public static int ANTIBOT_CAPTCHA_JAIL_TIME;
	public static boolean COMMAND_SELL_BUFF;
	public static int COMMAND_SELL_BUFF_MAX_PRICE;
	public static int COMMAND_SELL_BUFF_MIN_PRICE;
	public static int[] COMMAND_SELL_BUFF_LIST_SKILL_OFF;
	public static int[] COMMAND_SELL_BUFF_LIST_SKILL_ON;
	public static boolean ENABLE_LIST_SKILL_ALLOWED;
	public static boolean SELLBUFF_ONLY_CLAN_ALY_MEMBERS;
	public static boolean ITEM_REWARD_PVP_ENABLE;
	public static int ITEM_REWARD_ID_PVP;
	public static int ITEM_REWARD_COUNT_PVP;
	public static boolean FAME_REWARD_PVP_ENABLE;
	public static int FAME_REWARD_COUNT_PVP;
	public static boolean ITEM_REWARD_PK_ENABLE;
	public static int ITEM_REWARD_ID_PK;
	public static int ITEM_REWARD_COUNT_PK;
	public static boolean FAME_REWARD_PK_ENABLE;
	public static int FAME_REWARD_COUNT_PK;
	public static boolean PREMIUM_SYSTEM_ENABLED;
	public static float PREMIUM_RATE_XP;
	public static float PREMIUM_RATE_SP;
	public static float PREMIUM_RATE_DROP_CHANCE;
	public static float PREMIUM_RATE_DROP_AMOUNT;
	public static Map<Integer, Float> PREMIUM_RATE_DROP_CHANCE_MULTIPLIER;
	public static Map<Integer, Float> PREMIUM_RATE_DROP_AMOUNT_MULTIPLIER;
	public static int SHOP_MIN_RANGE_FROM_NPC;
	public static int SHOP_MIN_RANGE_FROM_PLAYER;
	public static boolean ALLOW_SUB_RACE;
	public static boolean NO_CANCELL_BUFF_IF_DIE;
	public static int NO_CANCELL_BUFF_IF_DIE_MAX_LVL;
	public static boolean ITEM_BROKER_BID_RADIUS_ENABLE;
	public static int ITEM_BROKER_BID_RADIUS;
	public static boolean SPAWN_RETURN_TELEPORT_RAID;
	public static boolean SPAWN_RETURN_TELEPORT_MONSTER;
	public static ArrayList<Integer> SPAWN_RETURN_LIST_EXCLUDED;
	
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
	public static int DEFAULT_CORPSE_TIME;
	public static int SPOILED_CORPSE_EXTEND_TIME;
	public static int CORPSE_CONSUME_SKILL_ALLOWED_TIME_BEFORE_DECAY;
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
	public static int RAID_CURSE_LEVEL;
	public static int RAID_CHAOS_TIME;
	public static int GRAND_CHAOS_TIME;
	public static int MINION_CHAOS_TIME;
	public static int INVENTORY_MAXIMUM_PET;
	public static double PET_HP_REGEN_MULTIPLIER;
	public static double PET_MP_REGEN_MULTIPLIER;
	public static int CHANGE_STATUS;
	public static int CHANCE_SPAWN;
	public static int RESPAWN_TIME;
	public static int DROP_ADENA_MIN_LEVEL_DIFFERENCE;
	public static int DROP_ADENA_MAX_LEVEL_DIFFERENCE;
	public static double DROP_ADENA_MIN_LEVEL_GAP_CHANCE;
	public static int DROP_ITEM_MIN_LEVEL_DIFFERENCE;
	public static int DROP_ITEM_MAX_LEVEL_DIFFERENCE;
	public static double DROP_ITEM_MIN_LEVEL_GAP_CHANCE;
	
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
	public static float RATE_HB_TRUST_INCREASE;
	public static float RATE_HB_TRUST_DECREASE;
	public static float RATE_EXTRACTABLE;
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
	public static float RATE_DEATH_DROP_AMOUNT_MULTIPLIER;
	public static float RATE_CORPSE_DROP_AMOUNT_MULTIPLIER;
	public static float RATE_HERB_DROP_AMOUNT_MULTIPLIER;
	public static float RATE_RAID_DROP_AMOUNT_MULTIPLIER;
	public static float RATE_DEATH_DROP_CHANCE_MULTIPLIER;
	public static float RATE_CORPSE_DROP_CHANCE_MULTIPLIER;
	public static float RATE_HERB_DROP_CHANCE_MULTIPLIER;
	public static float RATE_RAID_DROP_CHANCE_MULTIPLIER;
	public static Map<Integer, Float> RATE_DROP_AMOUNT_MULTIPLIER;
	public static Map<Integer, Float> RATE_DROP_CHANCE_MULTIPLIER;
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
	public static boolean ENABLE_UPNP;
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
	public static String DATABASE_CONNECTION_POOL;
	public static int DATABASE_MAX_CONNECTIONS;
	public static int DATABASE_MAX_IDLE_TIME;
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
	
	public static enum IdFactoryType
	{
		Compaction,
		BitSet,
		Stack
	}
	
	public static IdFactoryType IDFACTORY_TYPE;
	public static boolean BAD_ID_CHECKING;
	
	public static double ENCHANT_CHANCE_ELEMENT_STONE;
	public static double ENCHANT_CHANCE_ELEMENT_CRYSTAL;
	public static double ENCHANT_CHANCE_ELEMENT_JEWEL;
	public static double ENCHANT_CHANCE_ELEMENT_ENERGY;
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
	public static boolean ALT_ALLOW_AUGMENT_PVP_ITEMS;
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
	
	// GrandBoss Settings
	
	// Antharas
	public static int ANTHARAS_WAIT_TIME;
	public static int ANTHARAS_SPAWN_INTERVAL;
	public static int ANTHARAS_SPAWN_RANDOM;
	
	// Valakas
	public static int VALAKAS_WAIT_TIME;
	public static int VALAKAS_SPAWN_INTERVAL;
	public static int VALAKAS_SPAWN_RANDOM;
	
	// Baium
	public static int BAIUM_SPAWN_INTERVAL;
	public static int BAIUM_SPAWN_RANDOM;
	
	// Core
	public static int CORE_SPAWN_INTERVAL;
	public static int CORE_SPAWN_RANDOM;
	
	// Offen
	public static int ORFEN_SPAWN_INTERVAL;
	public static int ORFEN_SPAWN_RANDOM;
	
	// Queen Ant
	public static int QUEEN_ANT_SPAWN_INTERVAL;
	public static int QUEEN_ANT_SPAWN_RANDOM;
	
	// Beleth
	public static int BELETH_MIN_PLAYERS;
	public static int BELETH_SPAWN_INTERVAL;
	public static int BELETH_SPAWN_RANDOM;
	
	// Gracia Seeds Settings
	public static int SOD_TIAT_KILL_COUNT;
	public static long SOD_STAGE_2_LENGTH;
	
	// chatfilter
	public static List<String> FILTER_LIST;
	
	// Email
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
	
	// Conquerable Halls Settings
	public static int CHS_CLAN_MINLEVEL;
	public static int CHS_MAX_ATTACKERS;
	public static int CHS_MAX_FLAGS_PER_CLAN;
	public static boolean CHS_ENABLE_FAME;
	public static int CHS_FAME_AMOUNT;
	public static int CHS_FAME_FREQUENCY;
	
	// GeoData Settings
	public static int PATHFINDING;
	public static File PATHNODE_DIR;
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
	public static Path GEODATA_PATH;
	public static boolean TRY_LOAD_UNSPECIFIED_REGIONS;
	public static Map<String, Boolean> GEODATA_REGIONS;
	
	// API Vote System Settings
	public static String API_KEYTOPZONE;
	public static int SERVERID_KEYTOPZONE;
	public static String APIKEY_HOPZONE;
	public static String SERVERID_NETWORK;
	public static int API_REWARD;
	public static int API_REWARD_AMOUNT;
	
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
			
			final PropertiesParser serverSettings = new PropertiesParser(CONFIGURATION_FILE);
			
			ENABLE_UPNP = serverSettings.getBoolean("EnableUPnP", true);
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
			DATABASE_CONNECTION_POOL = serverSettings.getString("ConnectionPool", "C3P0");
			DATABASE_MAX_CONNECTIONS = serverSettings.getInt("MaximumDbConnections", 10);
			DATABASE_MAX_IDLE_TIME = serverSettings.getInt("MaximumDbIdleTime", 0);
			
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
					_log.warn("Wrong config protocol version: {}, skipped.", protocol);
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
			CASTLE_BUFF_FREE = Feature.getBoolean("AltCastleMpBuffFree", false);
			FORTRESS_BUFF_FREE = Feature.getBoolean("AltFortressMpBuffFree", false);
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
			final PropertiesParser character = new PropertiesParser(CHARACTER_CONFIG_FILE);
			
			ALT_GAME_DELEVEL = character.getBoolean("Delevel", true);
			DECREASE_SKILL_LEVEL = character.getBoolean("DecreaseSkillOnDelevel", true);
			ALT_WEIGHT_LIMIT = character.getDouble("AltWeightLimit", 1);
			RUN_SPD_BOOST = character.getInt("RunSpeedBoost", 0);
			DEATH_PENALTY_CHANCE = character.getInt("DeathPenaltyChance", 20);
			RESPAWN_RESTORE_CP = character.getDouble("RespawnRestoreCP", 0) / 100;
			RESPAWN_RESTORE_HP = character.getDouble("RespawnRestoreHP", 65) / 100;
			RESPAWN_RESTORE_MP = character.getDouble("RespawnRestoreMP", 0) / 100;
			HP_REGEN_MULTIPLIER = character.getDouble("HpRegenMultiplier", 100) / 100;
			MP_REGEN_MULTIPLIER = character.getDouble("MpRegenMultiplier", 100) / 100;
			CP_REGEN_MULTIPLIER = character.getDouble("CpRegenMultiplier", 100) / 100;
			ALT_GAME_TIREDNESS = character.getBoolean("AltGameTiredness", false);
			ENABLE_MODIFY_SKILL_DURATION = character.getBoolean("EnableModifySkillDuration", false);
			
			// Create Map only if enabled
			if (ENABLE_MODIFY_SKILL_DURATION)
			{
				String[] propertySplit = character.getString("SkillDurationList", "").split(";");
				SKILL_DURATION_LIST = new HashMap<>(propertySplit.length);
				for (String skill : propertySplit)
				{
					String[] skillSplit = skill.split(",");
					if (skillSplit.length != 2)
					{
						_log.warn("[SkillDurationList]: invalid config property -> SkillDurationList {}", skill);
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
								_log.warn("[SkillDurationList]: invalid config property -> SkillList {}", skill);
							}
						}
					}
				}
			}
			ENABLE_MODIFY_SKILL_REUSE = character.getBoolean("EnableModifySkillReuse", false);
			// Create Map only if enabled
			if (ENABLE_MODIFY_SKILL_REUSE)
			{
				String[] propertySplit = character.getString("SkillReuseList", "").split(";");
				SKILL_REUSE_LIST = new HashMap<>(propertySplit.length);
				for (String skill : propertySplit)
				{
					String[] skillSplit = skill.split(",");
					if (skillSplit.length != 2)
					{
						_log.warn("[SkillReuseList]: invalid config property -> SkillReuseList {}", skill);
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
								_log.warn("[SkillReuseList]: invalid config property -> SkillList {}", skill);
							}
						}
					}
				}
			}
			
			AUTO_LEARN_SKILLS = character.getBoolean("AutoLearnSkills", false);
			AUTO_LEARN_FS_SKILLS = character.getBoolean("AutoLearnForgottenScrollSkills", false);
			AUTO_LOOT_HERBS = character.getBoolean("AutoLootHerbs", false);
			BUFFS_MAX_AMOUNT = character.getByte("MaxBuffAmount", (byte) 20);
			TRIGGERED_BUFFS_MAX_AMOUNT = character.getByte("MaxTriggeredBuffAmount", (byte) 12);
			DANCES_MAX_AMOUNT = character.getByte("MaxDanceAmount", (byte) 12);
			DANCE_CANCEL_BUFF = character.getBoolean("DanceCancelBuff", false);
			DANCE_CONSUME_ADDITIONAL_MP = character.getBoolean("DanceConsumeAdditionalMP", true);
			ALT_STORE_DANCES = character.getBoolean("AltStoreDances", false);
			AUTO_LEARN_DIVINE_INSPIRATION = character.getBoolean("AutoLearnDivineInspiration", false);
			ALT_GAME_CANCEL_BOW = character.getString("AltGameCancelByHit", "Cast").equalsIgnoreCase("bow") || character.getString("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
			ALT_GAME_CANCEL_CAST = character.getString("AltGameCancelByHit", "Cast").equalsIgnoreCase("cast") || character.getString("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
			ALT_GAME_MAGICFAILURES = character.getBoolean("MagicFailures", true);
			PLAYER_FAKEDEATH_UP_PROTECTION = character.getInt("PlayerFakeDeathUpProtection", 10);
			STORE_SKILL_COOLTIME = character.getBoolean("StoreSkillCooltime", true);
			SUBCLASS_STORE_SKILL_COOLTIME = character.getBoolean("SubclassStoreSkillCooltime", false);
			SUMMON_STORE_SKILL_COOLTIME = character.getBoolean("SummonStoreSkillCooltime", true);
			ALT_GAME_SHIELD_BLOCKS = character.getBoolean("AltShieldBlocks", false);
			ALT_PERFECT_SHLD_BLOCK = character.getInt("AltPerfectShieldBlockRate", 10);
			EFFECT_TICK_RATIO = character.getLong("EffectTickRatio", 666);
			ALLOW_CLASS_MASTERS = character.getBoolean("AllowClassMasters", false);
			ALLOW_ENTIRE_TREE = character.getBoolean("AllowEntireTree", false);
			ALTERNATE_CLASS_MASTER = character.getBoolean("AlternateClassMaster", false);
			if (ALLOW_CLASS_MASTERS || ALTERNATE_CLASS_MASTER)
			{
				CLASS_MASTER_SETTINGS = new ClassMasterSettings(character.getString("ConfigClassMaster", ""));
			}
			LIFE_CRYSTAL_NEEDED = character.getBoolean("LifeCrystalNeeded", true);
			ES_SP_BOOK_NEEDED = character.getBoolean("EnchantSkillSpBookNeeded", true);
			DIVINE_SP_BOOK_NEEDED = character.getBoolean("DivineInspirationSpBookNeeded", true);
			ALT_GAME_SKILL_LEARN = character.getBoolean("AltGameSkillLearn", false);
			ALT_GAME_SUBCLASS_WITHOUT_QUESTS = character.getBoolean("AltSubClassWithoutQuests", false);
			ALT_GAME_SUBCLASS_EVERYWHERE = character.getBoolean("AltSubclassEverywhere", false);
			RESTORE_SERVITOR_ON_RECONNECT = character.getBoolean("RestoreServitorOnReconnect", true);
			RESTORE_PET_ON_RECONNECT = character.getBoolean("RestorePetOnReconnect", true);
			ALLOW_TRANSFORM_WITHOUT_QUEST = character.getBoolean("AltTransformationWithoutQuest", false);
			FEE_DELETE_TRANSFER_SKILLS = character.getInt("FeeDeleteTransferSkills", 10000000);
			FEE_DELETE_SUBCLASS_SKILLS = character.getInt("FeeDeleteSubClassSkills", 10000000);
			ENABLE_VITALITY = character.getBoolean("EnableVitality", true);
			RECOVER_VITALITY_ON_RECONNECT = character.getBoolean("RecoverVitalityOnReconnect", true);
			STARTING_VITALITY_POINTS = character.getInt("StartingVitalityPoints", 20000);
			MAX_BONUS_EXP = character.getDouble("MaxExpBonus", 3.5);
			MAX_BONUS_SP = character.getDouble("MaxSpBonus", 3.5);
			MAX_RUN_SPEED = character.getInt("MaxRunSpeed", 250);
			MAX_PCRIT_RATE = character.getInt("MaxPCritRate", 500);
			MAX_MCRIT_RATE = character.getInt("MaxMCritRate", 200);
			MAX_PATK_SPEED = character.getInt("MaxPAtkSpeed", 1500);
			MAX_MATK_SPEED = character.getInt("MaxMAtkSpeed", 1999);
			MAX_EVASION = character.getInt("MaxEvasion", 250);
			MIN_ABNORMAL_STATE_SUCCESS_RATE = character.getInt("MinAbnormalStateSuccessRate", 10);
			MAX_ABNORMAL_STATE_SUCCESS_RATE = character.getInt("MaxAbnormalStateSuccessRate", 90);
			MAX_SUBCLASS = character.getByte("MaxSubclass", (byte) 3);
			BASE_SUBCLASS_LEVEL = character.getByte("BaseSubclassLevel", (byte) 40);
			MAX_SUBCLASS_LEVEL = character.getByte("MaxSubclassLevel", (byte) 80);
			MAX_PVTSTORESELL_SLOTS_DWARF = character.getInt("MaxPvtStoreSellSlotsDwarf", 4);
			MAX_PVTSTORESELL_SLOTS_OTHER = character.getInt("MaxPvtStoreSellSlotsOther", 3);
			MAX_PVTSTOREBUY_SLOTS_DWARF = character.getInt("MaxPvtStoreBuySlotsDwarf", 5);
			MAX_PVTSTOREBUY_SLOTS_OTHER = character.getInt("MaxPvtStoreBuySlotsOther", 4);
			INVENTORY_MAXIMUM_NO_DWARF = character.getInt("MaximumSlotsForNoDwarf", 80);
			INVENTORY_MAXIMUM_DWARF = character.getInt("MaximumSlotsForDwarf", 100);
			INVENTORY_MAXIMUM_GM = character.getInt("MaximumSlotsForGMPlayer", 250);
			INVENTORY_MAXIMUM_QUEST_ITEMS = character.getInt("MaximumSlotsForQuestItems", 100);
			MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));
			WAREHOUSE_SLOTS_DWARF = character.getInt("MaximumWarehouseSlotsForDwarf", 120);
			WAREHOUSE_SLOTS_NO_DWARF = character.getInt("MaximumWarehouseSlotsForNoDwarf", 100);
			WAREHOUSE_SLOTS_CLAN = character.getInt("MaximumWarehouseSlotsForClan", 150);
			ALT_FREIGHT_SLOTS = character.getInt("MaximumFreightSlots", 200);
			ALT_FREIGHT_PRICE = character.getInt("FreightPrice", 1000);
			ENCHANT_CHANCE_ELEMENT_STONE = character.getDouble("EnchantChanceElementStone", 50);
			ENCHANT_CHANCE_ELEMENT_CRYSTAL = character.getDouble("EnchantChanceElementCrystal", 30);
			ENCHANT_CHANCE_ELEMENT_JEWEL = character.getDouble("EnchantChanceElementJewel", 20);
			ENCHANT_CHANCE_ELEMENT_ENERGY = character.getDouble("EnchantChanceElementEnergy", 10);
			String[] notenchantable = character.getString("EnchantBlackList", "7816,7817,7818,7819,7820,7821,7822,7823,7824,7825,7826,7827,7828,7829,7830,7831,13293,13294,13296").split(",");
			ENCHANT_BLACKLIST = new int[notenchantable.length];
			for (int i = 0; i < notenchantable.length; i++)
			{
				ENCHANT_BLACKLIST[i] = Integer.parseInt(notenchantable[i]);
			}
			Arrays.sort(ENCHANT_BLACKLIST);
			
			AUGMENTATION_NG_SKILL_CHANCE = character.getInt("AugmentationNGSkillChance", 15);
			AUGMENTATION_NG_GLOW_CHANCE = character.getInt("AugmentationNGGlowChance", 0);
			AUGMENTATION_MID_SKILL_CHANCE = character.getInt("AugmentationMidSkillChance", 30);
			AUGMENTATION_MID_GLOW_CHANCE = character.getInt("AugmentationMidGlowChance", 40);
			AUGMENTATION_HIGH_SKILL_CHANCE = character.getInt("AugmentationHighSkillChance", 45);
			AUGMENTATION_HIGH_GLOW_CHANCE = character.getInt("AugmentationHighGlowChance", 70);
			AUGMENTATION_TOP_SKILL_CHANCE = character.getInt("AugmentationTopSkillChance", 60);
			AUGMENTATION_TOP_GLOW_CHANCE = character.getInt("AugmentationTopGlowChance", 100);
			AUGMENTATION_BASESTAT_CHANCE = character.getInt("AugmentationBaseStatChance", 1);
			AUGMENTATION_ACC_SKILL_CHANCE = character.getInt("AugmentationAccSkillChance", 0);
			
			RETAIL_LIKE_AUGMENTATION = character.getBoolean("RetailLikeAugmentation", true);
			String[] array = character.getString("RetailLikeAugmentationNoGradeChance", "55,35,7,3").split(",");
			RETAIL_LIKE_AUGMENTATION_NG_CHANCE = new int[array.length];
			for (int i = 0; i < 4; i++)
			{
				RETAIL_LIKE_AUGMENTATION_NG_CHANCE[i] = Integer.parseInt(array[i]);
			}
			array = character.getString("RetailLikeAugmentationMidGradeChance", "55,35,7,3").split(",");
			RETAIL_LIKE_AUGMENTATION_MID_CHANCE = new int[array.length];
			for (int i = 0; i < 4; i++)
			{
				RETAIL_LIKE_AUGMENTATION_MID_CHANCE[i] = Integer.parseInt(array[i]);
			}
			array = character.getString("RetailLikeAugmentationHighGradeChance", "55,35,7,3").split(",");
			RETAIL_LIKE_AUGMENTATION_HIGH_CHANCE = new int[array.length];
			for (int i = 0; i < 4; i++)
			{
				RETAIL_LIKE_AUGMENTATION_HIGH_CHANCE[i] = Integer.parseInt(array[i]);
			}
			array = character.getString("RetailLikeAugmentationTopGradeChance", "55,35,7,3").split(",");
			RETAIL_LIKE_AUGMENTATION_TOP_CHANCE = new int[array.length];
			for (int i = 0; i < 4; i++)
			{
				RETAIL_LIKE_AUGMENTATION_TOP_CHANCE[i] = Integer.parseInt(array[i]);
			}
			RETAIL_LIKE_AUGMENTATION_ACCESSORY = character.getBoolean("RetailLikeAugmentationAccessory", true);
			
			array = character.getString("AugmentationBlackList", "6656,6657,6658,6659,6660,6661,6662,8191,10170,10314,13740,13741,13742,13743,13744,13745,13746,13747,13748,14592,14593,14594,14595,14596,14597,14598,14599,14600,14664,14665,14666,14667,14668,14669,14670,14671,14672,14801,14802,14803,14804,14805,14806,14807,14808,14809,15282,15283,15284,15285,15286,15287,15288,15289,15290,15291,15292,15293,15294,15295,15296,15297,15298,15299,16025,16026,21712,22173,22174,22175").split(",");
			AUGMENTATION_BLACKLIST = new int[array.length];
			
			for (int i = 0; i < array.length; i++)
			{
				AUGMENTATION_BLACKLIST[i] = Integer.parseInt(array[i]);
			}
			
			Arrays.sort(AUGMENTATION_BLACKLIST);
			ALT_ALLOW_AUGMENT_PVP_ITEMS = character.getBoolean("AltAllowAugmentPvPItems", false);
			ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = character.getBoolean("AltKarmaPlayerCanBeKilledInPeaceZone", false);
			ALT_GAME_KARMA_PLAYER_CAN_SHOP = character.getBoolean("AltKarmaPlayerCanShop", true);
			ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = character.getBoolean("AltKarmaPlayerCanTeleport", true);
			ALT_GAME_KARMA_PLAYER_CAN_USE_GK = character.getBoolean("AltKarmaPlayerCanUseGK", false);
			ALT_GAME_KARMA_PLAYER_CAN_TRADE = character.getBoolean("AltKarmaPlayerCanTrade", true);
			ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = character.getBoolean("AltKarmaPlayerCanUseWareHouse", true);
			MAX_PERSONAL_FAME_POINTS = character.getInt("MaxPersonalFamePoints", 100000);
			FORTRESS_ZONE_FAME_TASK_FREQUENCY = character.getInt("FortressZoneFameTaskFrequency", 300);
			FORTRESS_ZONE_FAME_AQUIRE_POINTS = character.getInt("FortressZoneFameAquirePoints", 31);
			CASTLE_ZONE_FAME_TASK_FREQUENCY = character.getInt("CastleZoneFameTaskFrequency", 300);
			CASTLE_ZONE_FAME_AQUIRE_POINTS = character.getInt("CastleZoneFameAquirePoints", 125);
			FAME_FOR_DEAD_PLAYERS = character.getBoolean("FameForDeadPlayers", true);
			IS_CRAFTING_ENABLED = character.getBoolean("CraftingEnabled", true);
			CRAFT_MASTERWORK = character.getBoolean("CraftMasterwork", true);
			DWARF_RECIPE_LIMIT = character.getInt("DwarfRecipeLimit", 50);
			COMMON_RECIPE_LIMIT = character.getInt("CommonRecipeLimit", 50);
			ALT_GAME_CREATION = character.getBoolean("AltGameCreation", false);
			ALT_GAME_CREATION_SPEED = character.getDouble("AltGameCreationSpeed", 1);
			ALT_GAME_CREATION_XP_RATE = character.getDouble("AltGameCreationXpRate", 1);
			ALT_GAME_CREATION_SP_RATE = character.getDouble("AltGameCreationSpRate", 1);
			ALT_GAME_CREATION_RARE_XPSP_RATE = character.getDouble("AltGameCreationRareXpSpRate", 2);
			ALT_BLACKSMITH_USE_RECIPES = character.getBoolean("AltBlacksmithUseRecipes", true);
			ALT_CLAN_LEADER_DATE_CHANGE = character.getInt("AltClanLeaderDateChange", 3);
			if ((ALT_CLAN_LEADER_DATE_CHANGE < 1) || (ALT_CLAN_LEADER_DATE_CHANGE > 7))
			{
				_log.warn("Wrong value specified for AltClanLeaderDateChange: {}", ALT_CLAN_LEADER_DATE_CHANGE);
				ALT_CLAN_LEADER_DATE_CHANGE = 3;
			}
			ALT_CLAN_LEADER_HOUR_CHANGE = character.getString("AltClanLeaderHourChange", "00:00:00");
			ALT_CLAN_LEADER_INSTANT_ACTIVATION = character.getBoolean("AltClanLeaderInstantActivation", false);
			ALT_CLAN_JOIN_DAYS = character.getInt("DaysBeforeJoinAClan", 1);
			ALT_CLAN_CREATE_DAYS = character.getInt("DaysBeforeCreateAClan", 10);
			ALT_CLAN_DISSOLVE_DAYS = character.getInt("DaysToPassToDissolveAClan", 7);
			ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = character.getInt("DaysBeforeJoinAllyWhenLeaved", 1);
			ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = character.getInt("DaysBeforeJoinAllyWhenDismissed", 1);
			ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = character.getInt("DaysBeforeAcceptNewClanWhenDismissed", 1);
			ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = character.getInt("DaysBeforeCreateNewAllyWhenDissolved", 1);
			ALT_MAX_NUM_OF_CLANS_IN_ALLY = character.getInt("AltMaxNumOfClansInAlly", 3);
			ALT_CLAN_MEMBERS_FOR_WAR = character.getInt("AltClanMembersForWar", 15);
			ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = character.getBoolean("AltMembersCanWithdrawFromClanWH", false);
			REMOVE_CASTLE_CIRCLETS = character.getBoolean("RemoveCastleCirclets", true);
			ALT_PARTY_RANGE = character.getInt("AltPartyRange", 1600);
			ALT_PARTY_RANGE2 = character.getInt("AltPartyRange2", 1400);
			ALT_LEAVE_PARTY_LEADER = character.getBoolean("AltLeavePartyLeader", false);
			INITIAL_EQUIPMENT_EVENT = character.getBoolean("InitialEquipmentEvent", false);
			STARTING_ADENA = character.getLong("StartingAdena", 0);
			STARTING_LEVEL = character.getByte("StartingLevel", (byte) 1);
			STARTING_SP = character.getInt("StartingSP", 0);
			MAX_ADENA = character.getLong("MaxAdena", 99900000000L);
			if (MAX_ADENA < 0)
			{
				MAX_ADENA = Long.MAX_VALUE;
			}
			AUTO_LOOT = character.getBoolean("AutoLoot", false);
			AUTO_LOOT_RAIDS = character.getBoolean("AutoLootRaids", false);
			LOOT_RAIDS_PRIVILEGE_INTERVAL = character.getInt("RaidLootRightsInterval", 900) * 1000;
			LOOT_RAIDS_PRIVILEGE_CC_SIZE = character.getInt("RaidLootRightsCCSize", 45);
			UNSTUCK_INTERVAL = character.getInt("UnstuckInterval", 300);
			TELEPORT_WATCHDOG_TIMEOUT = character.getInt("TeleportWatchdogTimeout", 0);
			PLAYER_SPAWN_PROTECTION = character.getInt("PlayerSpawnProtection", 0);
			String[] items = character.getString("PlayerSpawnProtectionAllowedItems", "").split(",");
			SPAWN_PROTECTION_ALLOWED_ITEMS = new ArrayList<>(items.length);
			for (String item : items)
			{
				try
				{
					if (!item.isEmpty())
					{
						SPAWN_PROTECTION_ALLOWED_ITEMS.add(Integer.parseInt(item));
					}
				}
				catch (NumberFormatException nfe)
				{
					_log.warn("Player Spawn Protection: Wrong Item ID passed: {}", item, nfe);
				}
			}
			
			PLAYER_TELEPORT_PROTECTION = character.getInt("PlayerTeleportProtection", 0);
			RANDOM_RESPAWN_IN_TOWN_ENABLED = character.getBoolean("RandomRespawnInTownEnabled", true);
			OFFSET_ON_TELEPORT_ENABLED = character.getBoolean("OffsetOnTeleportEnabled", true);
			MAX_OFFSET_ON_TELEPORT = character.getInt("MaxOffsetOnTeleport", 50);
			PETITIONING_ALLOWED = character.getBoolean("PetitioningAllowed", true);
			MAX_PETITIONS_PER_PLAYER = character.getInt("MaxPetitionsPerPlayer", 5);
			MAX_PETITIONS_PENDING = character.getInt("MaxPetitionsPending", 25);
			ALT_GAME_FREE_TELEPORT = character.getBoolean("AltFreeTeleporting", false);
			DELETE_DAYS = character.getInt("DeleteCharAfterDays", 7);
			ALT_GAME_EXPONENT_XP = character.getFloat("AltGameExponentXp", 0);
			ALT_GAME_EXPONENT_SP = character.getFloat("AltGameExponentSp", 0);
			PARTY_XP_CUTOFF_METHOD = character.getString("PartyXpCutoffMethod", "highfive");
			PARTY_XP_CUTOFF_PERCENT = character.getDouble("PartyXpCutoffPercent", 3);
			PARTY_XP_CUTOFF_LEVEL = character.getInt("PartyXpCutoffLevel", 20);
			final String[] gaps = character.getString("PartyXpCutoffGaps", "0,9;10,14;15,99").split(";");
			PARTY_XP_CUTOFF_GAPS = new int[gaps.length][2];
			for (int i = 0; i < gaps.length; i++)
			{
				PARTY_XP_CUTOFF_GAPS[i] = new int[]
				{
					Integer.parseInt(gaps[i].split(",")[0]),
					Integer.parseInt(gaps[i].split(",")[1])
				};
			}
			final String[] percents = character.getString("PartyXpCutoffGapPercent", "100;30;0").split(";");
			PARTY_XP_CUTOFF_GAP_PERCENTS = new int[percents.length];
			for (int i = 0; i < percents.length; i++)
			{
				PARTY_XP_CUTOFF_GAP_PERCENTS[i] = Integer.parseInt(percents[i]);
			}
			DISABLE_TUTORIAL = character.getBoolean("DisableTutorial", false);
			EXPERTISE_PENALTY = character.getBoolean("ExpertisePenalty", true);
			STORE_RECIPE_SHOPLIST = character.getBoolean("StoreRecipeShopList", false);
			STORE_UI_SETTINGS = character.getBoolean("StoreCharUiSettings", false);
			FORBIDDEN_NAMES = character.getString("ForbiddenNames", "").split(",");
			SILENCE_MODE_EXCLUDE = character.getBoolean("SilenceModeExclude", false);
			ALT_VALIDATE_TRIGGER_SKILLS = character.getBoolean("AltValidateTriggerSkills", false);
			PLAYER_MOVEMENT_BLOCK_TIME = character.getInt("NpcTalkBlockingTime", 0) * 1000;
			
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
			GAMEGUARD_ENFORCE = General.getBoolean("GameGuardEnforce", false);
			GAMEGUARD_PROHIBITACTION = General.getBoolean("GameGuardProhibitAction", false);
			LOG_CHAT = General.getBoolean("LogChat", false);
			LOG_AUTO_ANNOUNCEMENTS = General.getBoolean("LogAutoAnnouncements", false);
			LOG_ITEMS = General.getBoolean("LogItems", false);
			LOG_ITEMS_SMALL_LOG = General.getBoolean("LogItemsSmallLog", false);
			LOG_ITEM_ENCHANTS = General.getBoolean("LogItemEnchants", false);
			LOG_SKILL_ENCHANTS = General.getBoolean("LogSkillEnchants", false);
			GMAUDIT = General.getBoolean("GMAudit", false);
			SKILL_CHECK_ENABLE = General.getBoolean("SkillCheckEnable", false);
			SKILL_CHECK_REMOVE = General.getBoolean("SkillCheckRemove", false);
			SKILL_CHECK_GM = General.getBoolean("SkillCheckGM", true);
			DEBUG = General.getBoolean("Debug", false);
			DEBUG_INSTANCES = General.getBoolean("InstanceDebug", false);
			HTML_ACTION_CACHE_DEBUG = General.getBoolean("HtmlActionCacheDebug", false);
			PACKET_HANDLER_DEBUG = General.getBoolean("PacketHandlerDebug", false);
			DEVELOPER = General.getBoolean("Developer", false);
			ALT_DEV_NO_HANDLERS = General.getBoolean("AltDevNoHandlers", false) || Boolean.getBoolean("nohandlers");
			ALT_DEV_NO_QUESTS = General.getBoolean("AltDevNoQuests", false) || Boolean.getBoolean("noquests");
			ALT_DEV_NO_SPAWNS = General.getBoolean("AltDevNoSpawns", false) || Boolean.getBoolean("nospawns");
			ALT_DEV_SHOW_QUESTS_LOAD_IN_LOGS = General.getBoolean("AltDevShowQuestsLoadInLogs", false);
			ALT_DEV_SHOW_SCRIPTS_LOAD_IN_LOGS = General.getBoolean("AltDevShowScriptsLoadInLogs", false);
			THREAD_P_EFFECTS = General.getInt("ThreadPoolSizeEffects", 10);
			THREAD_P_GENERAL = General.getInt("ThreadPoolSizeGeneral", 13);
			THREAD_E_EVENTS = General.getInt("ThreadPoolSizeEvents", 2);
			IO_PACKET_THREAD_CORE_SIZE = General.getInt("UrgentPacketThreadCoreSize", 2);
			GENERAL_PACKET_THREAD_CORE_SIZE = General.getInt("GeneralPacketThreadCoreSize", 4);
			GENERAL_THREAD_CORE_SIZE = General.getInt("GeneralThreadCoreSize", 4);
			AI_MAX_THREAD = General.getInt("AiMaxThread", 6);
			EVENT_MAX_THREAD = General.getInt("EventsMaxThread", 5);
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
			INSTANCE_FINISH_TIME = 1000 * General.getInt("DefaultFinishTime", 300);
			RESTORE_PLAYER_INSTANCE = General.getBoolean("RestorePlayerInstance", false);
			ALLOW_SUMMON_IN_INSTANCE = General.getBoolean("AllowSummonInInstance", false);
			EJECT_DEAD_PLAYER_TIME = 1000 * General.getInt("EjectDeadPlayerTime", 60);
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
				_log.warn("Unable to load banned channels!", nfe);
			}
			ALT_MANOR_REFRESH_TIME = General.getInt("AltManorRefreshTime", 20);
			ALT_MANOR_REFRESH_MIN = General.getInt("AltManorRefreshMin", 0);
			ALT_MANOR_APPROVE_TIME = General.getInt("AltManorApproveTime", 4);
			ALT_MANOR_APPROVE_MIN = General.getInt("AltManorApproveMin", 30);
			ALT_MANOR_MAINTENANCE_MIN = General.getInt("AltManorMaintenanceMin", 6);
			ALT_MANOR_SAVE_ALL_ACTIONS = General.getBoolean("AltManorSaveAllActions", false);
			ALT_MANOR_SAVE_PERIOD_RATE = General.getInt("AltManorSavePeriodRate", 2);
			ALT_LOTTERY_PRIZE = General.getLong("AltLotteryPrize", 50000);
			ALT_LOTTERY_TICKET_PRICE = General.getLong("AltLotteryTicketPrice", 2000);
			ALT_LOTTERY_5_NUMBER_RATE = General.getFloat("AltLottery5NumberRate", 0.6f);
			ALT_LOTTERY_4_NUMBER_RATE = General.getFloat("AltLottery4NumberRate", 0.2f);
			ALT_LOTTERY_3_NUMBER_RATE = General.getFloat("AltLottery3NumberRate", 0.2f);
			ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = General.getLong("AltLottery2and1NumberPrize", 200);
			ALT_FISH_CHAMPIONSHIP_ENABLED = General.getBoolean("AltFishChampionshipEnabled", true);
			ALT_FISH_CHAMPIONSHIP_REWARD_ITEM = General.getInt("AltFishChampionshipRewardItemId", 57);
			ALT_FISH_CHAMPIONSHIP_REWARD_1 = General.getInt("AltFishChampionshipReward1", 800000);
			ALT_FISH_CHAMPIONSHIP_REWARD_2 = General.getInt("AltFishChampionshipReward2", 500000);
			ALT_FISH_CHAMPIONSHIP_REWARD_3 = General.getInt("AltFishChampionshipReward3", 300000);
			ALT_FISH_CHAMPIONSHIP_REWARD_4 = General.getInt("AltFishChampionshipReward4", 200000);
			ALT_FISH_CHAMPIONSHIP_REWARD_5 = General.getInt("AltFishChampionshipReward5", 100000);
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
			CUSTOM_NPC_DATA = General.getBoolean("CustomNpcData", false);
			CUSTOM_TELEPORT_TABLE = General.getBoolean("CustomTeleportTable", false);
			CUSTOM_NPCBUFFER_TABLES = General.getBoolean("CustomNpcBufferTables", false);
			CUSTOM_SKILLS_LOAD = General.getBoolean("CustomSkillsLoad", false);
			CUSTOM_ITEMS_LOAD = General.getBoolean("CustomItemsLoad", false);
			CUSTOM_MULTISELL_LOAD = General.getBoolean("CustomMultisellLoad", false);
			CUSTOM_BUYLIST_LOAD = General.getBoolean("CustomBuyListLoad", false);
			ALT_BIRTHDAY_GIFT = General.getInt("AltBirthdayGift", 22187);
			ALT_BIRTHDAY_MAIL_SUBJECT = General.getString("AltBirthdayMailSubject", "Happy Birthday!");
			ALT_BIRTHDAY_MAIL_TEXT = General.getString("AltBirthdayMailText", "Hello Adventurer!! Seeing as you're one year older now, I thought I would send you some birthday cheer :) Please find your birthday pack attached. May these gifts bring you joy and happiness on this very special day."
				+ EOL + EOL + "Sincerely, Alegria");
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
			KRATEIS_CUBE_EVENT_ENABLED = General.getBoolean("KrateisCubeEventEnabled", true);
			KRATEIS_CUBE_EVENT_LOG_ENABLED = General.getBoolean("KrateisCubeEventLogEnabled", true);
			KRATEIS_CUBE_REGISTRATION_TIME = General.getInt("KrateisCubeEventRegistrationTime", 3);
			KRATEIS_CUBE_RUNNING_TIME = General.getInt("KrateisCubeEventRunningTime", 20);
			KRATEIS_CUBE_TIME_TO_REVIVE = General.getInt("KrateisCubeEventTimeToRevive", 10);
			KRATEIS_CUBE_TIME_OF_PROTECTION = General.getInt("KrateisCubeEventTimeOfProtection", 3);
			KRATEIS_CUBE_MIN_PLAYER = General.getInt("KrateisCubeEventMinPlayer", 1);
			KRATEIS_CUBE_MAX_PLAYER = General.getInt("KrateisCubeEventMaxPlayer", 25);
			KRATEIS_REWARD_TO_KILL_PLAYER = General.getInt("KrateisCubeEventRewardToKillPlayer", 10);
			KRATEIS_REWARD_TO_KILL_MOB = General.getInt("KrateisCubeEventRewardToKillMob", 3);
			
			HELLBOUND_WITHOUT_QUEST = General.getBoolean("HellboundWithoutQuest", false);
			
			NORMAL_ENCHANT_COST_MULTIPLIER = General.getInt("NormalEnchantCostMultipiler", 1);
			SAFE_ENCHANT_COST_MULTIPLIER = General.getInt("SafeEnchantCostMultipiler", 5);
			
			BOTREPORT_ENABLE = General.getBoolean("EnableBotReportButton", false);
			BOTREPORT_RESETPOINT_HOUR = General.getString("BotReportPointsResetHour", "00:00").split(":");
			BOTREPORT_REPORT_DELAY = General.getInt("BotReportDelay", 30) * 60000;
			BOTREPORT_ALLOW_REPORTS_FROM_SAME_CLAN_MEMBERS = General.getBoolean("AllowReportsFromSameClanMembers", false);
			ENABLE_FALLING_DAMAGE = General.getBoolean("EnableFallingDamage", true);
			
			// Load FloodProtector L2Properties file
			final PropertiesParser FloodProtectors = new PropertiesParser(FLOOD_PROTECTOR_FILE);
			
			loadFloodProtectorConfigs(FloodProtectors);
			
			// Load MasterMods L2Properties file
			final PropertiesParser MasterMods = new PropertiesParser(MASTERMODS_FILE);
			
			ENABLE_ARCHIEVEMENTS = MasterMods.getBoolean("EnabledAchievements", false);
			ANNOUNCE_RAIDBOSS_ALIVE_KILL = MasterMods.getBoolean("AnnounceRaidBosKill", false);
			ANNOUNCE_GRANDBOSS_ALIVE_KILL = MasterMods.getBoolean("AnnounceGranBosKill", false);
			ANNOUNCE_BOSS_SPAWN = MasterMods.getBoolean("AnnounceBossSpawn", false);
			ANNOUNCE_BOSS_SPAWN_CRIT = MasterMods.getBoolean("CriticalAnnounce", false);
			ANNOUNCE_BOSS_MSG = MasterMods.getString("AnnounceBossMsg", "[Info] Raidboss %raidboss% has been spawned!");
			ANNOUNCE_CASTLE_LORDS_ON_ENTER = MasterMods.getBoolean("AnnounceCastleLordsOnEnter", false);
			ANNOUNCE_HERO_ON_ENTER = MasterMods.getBoolean("AnnounceHeroOnEnter", false);
			DEAD_DISABLE_CHAT = MasterMods.getBoolean("ChatDeadProtection", false);
			CHAT_PROTECTION = MasterMods.getBoolean("ChatProtection", false);
			CHAT_LEVEL_PROTECTION = MasterMods.getInt("ChatLevelProtection", 40);
			GAME_SERVER_AUTO_RESTART = MasterMods.getBoolean("GameServerAutoRestart", false);
			GAME_SERVER_SHOW_MESSAGE_INFO = MasterMods.getBoolean("GameServerShowMessageInfo", false);
			GAME_SERVER_AUTO_RESTART_COUNTDOWN = MasterMods.getInt("GameServerAutoRestartCountdown", 300);
			GAME_SERVER_AUTO_RESTART_INTERVAL = MasterMods.getString("GameServerAutoRestartInterval", "00:00").split(",");
			RESTORE_CANCELED_BUFFS_ENABLED = MasterMods.getBoolean("RestoreCanceledBuffs", false);
			RESTORE_STOLEN_BUFFS_ENABLED = MasterMods.getBoolean("RestoreStolenBuffs", false);
			RESTORE_CANCELED_BUFFS_TIME = MasterMods.getInt("RestoreCanceledBuffsTime", 60);
			RESTORE_STOLEN_BUFFS_TIME = MasterMods.getInt("RestoreStolenBuffsTime", 60);
			CLAN_LEADER_COLOR_ENABLED = MasterMods.getBoolean("ClanLeaderNameColorEnabled", false);
			CLAN_LEADER_COLOR = MasterMods.getInt("ClanLeaderColor", 889966);
			CLAN_LEADER_COLOR_CLAN_LEVEL = MasterMods.getInt("ClanLeaderColorAtClanLevel", 11);
			CLAN_LEADER_TITLE_ENABLED = MasterMods.getBoolean("ClanLeaderTitleColorEnabled", false);
			CLAN_LEADER_TITLE = MasterMods.getInt("ClanLeaderTitle", 889966);
			CLAN_LEADER_TITLE_CLAN_LEVEL = MasterMods.getInt("ClanLeaderTitleAtClanLevel", 11);
			USE_CUSTOM_CLANHALLS = MasterMods.getBoolean("UseCustomClanHalls", false);
			STARTING_TITLE = MasterMods.getString("StartingTitle", "L2JMaster");
			ALLOW_HEAVY_USE_LIGHT = MasterMods.getBoolean("AllowHeavyUseLight", false);
			NOTALLOWCLASSE = MasterMods.getString("NotAllowedUseLight", "");
			NOTALLOWEDUSELIGHT = new ArrayList<>();
			for (String classId : NOTALLOWCLASSE.split(","))
			{
				NOTALLOWEDUSELIGHT.add(Integer.parseInt(classId));
			}
			ALLOW_LIGHT_USE_HEAVY = MasterMods.getBoolean("AllowLightUseHeavy", false);
			NOTALLOWCLASS = MasterMods.getString("NotAllowedUseHeavy", "");
			NOTALLOWEDUSEHEAVY = new ArrayList<>();
			for (String classId : NOTALLOWCLASS.split(","))
			{
				NOTALLOWEDUSEHEAVY.add(Integer.parseInt(classId));
			}
			
			ALT_DISABLE_BOW_CLASSES = MasterMods.getBoolean("AltDisableBow", false);
			DISABLE_BOW_CLASSES_STRING = MasterMods.getString("DisableBowForClasses", "");
			DISABLE_BOW_CLASSES = new ArrayList<>();
			for (String class_id : DISABLE_BOW_CLASSES_STRING.split(","))
			{
				if (!class_id.equals(""))
				{
					DISABLE_BOW_CLASSES.add(Integer.parseInt(class_id));
				}
			}
			ALT_DISABLE_DAGGER_CLASSES = MasterMods.getBoolean("AltDisableDagger", false);
			DISABLE_DAGGER_CLASSES_STRING = MasterMods.getString("DisableDaggerForClasses", "");
			DISABLE_DAGGER_CLASSES = new ArrayList<>();
			for (String class_id : DISABLE_DAGGER_CLASSES_STRING.split(","))
			{
				if (!class_id.equals(""))
				{
					DISABLE_DAGGER_CLASSES.add(Integer.parseInt(class_id));
				}
			}
			ALT_DISABLE_SWORD_CLASSES = MasterMods.getBoolean("AltDisableSword", false);
			DISABLE_SWORD_CLASSES_STRING = MasterMods.getString("DisableSwordForClasses", "");
			DISABLE_SWORD_CLASSES = new ArrayList<>();
			for (String class_id : DISABLE_SWORD_CLASSES_STRING.split(","))
			{
				if (!class_id.equals(""))
				{
					DISABLE_SWORD_CLASSES.add(Integer.parseInt(class_id));
				}
			}
			ALT_DISABLE_BLUNT_CLASSES = MasterMods.getBoolean("AltDisableBlunt", false);
			DISABLE_BLUNT_CLASSES_STRING = MasterMods.getString("DisableBluntForClasses", "");
			DISABLE_BLUNT_CLASSES = new ArrayList<>();
			for (String class_id : DISABLE_BLUNT_CLASSES_STRING.split(","))
			{
				if (!class_id.equals(""))
				{
					DISABLE_BLUNT_CLASSES.add(Integer.parseInt(class_id));
				}
			}
			ALT_DISABLE_DUAL_CLASSES = MasterMods.getBoolean("AltDisableDual", false);
			DISABLE_DUAL_CLASSES_STRING = MasterMods.getString("DisableDualForClasses", "");
			DISABLE_DUAL_CLASSES = new ArrayList<>();
			for (String class_id : DISABLE_DUAL_CLASSES_STRING.split(","))
			{
				if (!class_id.equals(""))
				{
					DISABLE_DUAL_CLASSES.add(Integer.parseInt(class_id));
				}
			}
			ALT_DISABLE_POLE_CLASSES = MasterMods.getBoolean("AltDisablePolle", false);
			DISABLE_POLE_CLASSES_STRING = MasterMods.getString("DisablePolleForClasses", "");
			DISABLE_POLE_CLASSES = new ArrayList<>();
			for (String class_id : DISABLE_POLE_CLASSES_STRING.split(","))
			{
				if (!class_id.equals(""))
				{
					DISABLE_POLE_CLASSES.add(Integer.parseInt(class_id));
				}
			}
			RANK_ARENA_ACCEPT_SAME_IP = MasterMods.getBoolean("ArenaAcceptSameIP", true);
			
			RANK_ARENA_ENABLED = MasterMods.getBoolean("RankArenaEnabled", false);
			RANK_ARENA_INTERVAL = MasterMods.getInt("RankArenaInterval", 120);
			RANK_ARENA_REWARD_ID = MasterMods.getInt("RankArenaRewardId", 57);
			RANK_ARENA_REWARD_COUNT = MasterMods.getInt("RankArenaRewardCount", 1000);
			
			RANK_FISHERMAN_ENABLED = MasterMods.getBoolean("RankFishermanEnabled", false);
			RANK_FISHERMAN_INTERVAL = MasterMods.getInt("RankFishermanInterval", 120);
			RANK_FISHERMAN_REWARD_ID = MasterMods.getInt("RankFishermanRewardId", 57);
			RANK_FISHERMAN_REWARD_COUNT = MasterMods.getInt("RankFishermanRewardCount", 1000);
			
			RANK_CRAFT_ENABLED = MasterMods.getBoolean("RankCraftEnabled", false);
			RANK_CRAFT_INTERVAL = MasterMods.getInt("RankCraftInterval", 120);
			RANK_CRAFT_REWARD_ID = MasterMods.getInt("RankCraftRewardId", 57);
			RANK_CRAFT_REWARD_COUNT = MasterMods.getInt("RankCraftRewardCount", 1000);
			
			RANK_TVT_ENABLED = MasterMods.getBoolean("RankTvTEnabled", false);
			RANK_TVT_INTERVAL = MasterMods.getInt("RankTvTInterval", 120);
			RANK_TVT_REWARD_ID = MasterMods.getInt("RankTvTRewardId", 57);
			RANK_TVT_REWARD_COUNT = MasterMods.getInt("RankTvTRewardCount", 1000);
			ALLOW_REPAIR_COMMAND = MasterMods.getBoolean("AllowRepairCommand", false);
			ENABLE_ONLINE_STATUS = MasterMods.getBoolean("EnableOnlineStatus", false);
			ONLINE_STATUS_HTML = MasterMods.getBoolean("EnableAdvancedStatus", false);
			ONLINE_STATUS_SHOW_OFFLINE = MasterMods.getBoolean("EnableOfflineTraders", false);
			TELEPORT_TO_MOB_COMMAND = MasterMods.getBoolean("EnabledToMobCommand", false);
			TELEPORT_TO_TOWN_COMMNAND = MasterMods.getBoolean("EnabledToTownCommand", false);
			COMBINE_TALISMAN_ENABLED = MasterMods.getBoolean("CombineTalismanCommand", false);
			ALLOW_EPIC_COMMAND = MasterMods.getBoolean("EpicCommand", false);
			ALLOW_CL_COMMAND = MasterMods.getBoolean("GoToClanLeaderCommand", false);
			PC_BANG_ENABLED = MasterMods.getBoolean("Enabled", false);
			PC_BANG_MAX_POINTS = MasterMods.getInt("MaxPcBangPoints", 200000);
			if (PC_BANG_MAX_POINTS < 1)
			{
				PC_BANG_MAX_POINTS = 0;
			}
			PC_BANG_ENABLE_DOUBLE_POINTS = MasterMods.getBoolean("DoublingAcquisitionPoints", false);
			PC_BANG_DOUBLE_POINTS_CHANCE = MasterMods.getInt("DoublingAcquisitionPointsChance", 1);
			if ((PC_BANG_DOUBLE_POINTS_CHANCE < 0) || (PC_BANG_DOUBLE_POINTS_CHANCE > 100))
			{
				PC_BANG_DOUBLE_POINTS_CHANCE = 1;
			}
			PC_BANG_POINT_RATE = MasterMods.getDouble("AcquisitionPointsRate", 1.0);
			PC_BANG_RANDOM_POINT = MasterMods.getBoolean("AcquisitionPointsRandom", false);
			if (PC_BANG_POINT_RATE < 0)
			{
				PC_BANG_POINT_RATE = 1;
			}
			PC_BANG_REWARD_LOW_EXP_KILLS = MasterMods.getBoolean("RewardLowExpKills", true);
			PC_BANG_LOW_EXP_KILLS_CHANCE = MasterMods.getInt("RewardLowExpKillsChance", 5);
			if (PC_BANG_LOW_EXP_KILLS_CHANCE < 1)
			{
				PC_BANG_LOW_EXP_KILLS_CHANCE = 0;
			}
			if (PC_BANG_LOW_EXP_KILLS_CHANCE > 100)
			{
				PC_BANG_LOW_EXP_KILLS_CHANCE = 100;
			}
			PRIME_SHOP_POINTS_ENABLED = MasterMods.getBoolean("PrimeShopPointsEnabled", false);
			PRIME_SHOP_POINTS_MAX = MasterMods.getInt("PrimeShopPointsMax", 1000000);
			PRIME_SHOP_POINTS_ID = MasterMods.getInt("PrimeShopPointsId", -1);
			PK_SYSTEM_QUAKE = MasterMods.getBoolean("AllowKillingPkSystem", false);
			PVP_SYSTEM_QUAKE = MasterMods.getBoolean("AllowKillingPvPSystem", false);
			ALLOW_HTML_ON_VISUAL_EQUIP = MasterMods.getBoolean("HtmlOnVisualEquip", false);
			VISUAL_EQUIP_ENABLE = MasterMods.getBoolean("EnableVisualEquip", false);
			ALLOW_SHIELD_ON_VISUALEQUIP = MasterMods.getBoolean("AllowShield", false);
			ALLOW_WEAPON_ON_VISUALEQUIP = MasterMods.getBoolean("AllowWeapon", false);
			VISUAL_EQUIP_PREMIUM_FREE = MasterMods.getBoolean("VisualEquipPremiumFree", false);
			VISUAL_EQUIP_COST_ENABLE = MasterMods.getBoolean("VisualEquipNotFree", false);
			VISUAL_EQUIP_CONSUME_ID = MasterMods.getInt("VisualEquipConsumeId", 23014);
			VISUAL_EQUIP_COST = MasterMods.getInt("VisualEquipConsumeAmount", 1);
			VISUAL_EQUIP_PREMIUM_ONLY = MasterMods.getBoolean("VisualEquipOnlyPremium", false);
			VISUAL_EQUIP_HERO_FREE = MasterMods.getBoolean("VisualEquipFreeForHero", false);
			VISUAL_EQUIP_INFINITE_ENABLE = MasterMods.getBoolean("EnableInfiniteConfigs", false);
			VISUAL_EQUIP_INFINITE_ARMOR_ENABLE = MasterMods.getBoolean("AllowInfiniteArmor", false);
			VISUAL_EQUIP_INFINITE_SHIELD_ENABLE = MasterMods.getBoolean("AllowInfiniteShield", false);
			VISUAL_EQUIP_INFINITE_RACE_ENABLE = MasterMods.getBoolean("AllowInfiniteRace", false);
			VISUAL_EQUIP_INFINITE_WEAPON_ENABLE = MasterMods.getBoolean("AllowInfiniteWeapon", false);
			PVP_COLOR_SYSTEM_ENABLED = MasterMods.getBoolean("EnablePvPColorSystem", false);
			PVP_AMOUNT1 = MasterMods.getInt("PvpAmount1", 500);
			PVP_AMOUNT2 = MasterMods.getInt("PvpAmount2", 1000);
			PVP_AMOUNT3 = MasterMods.getInt("PvpAmount3", 1500);
			PVP_AMOUNT4 = MasterMods.getInt("PvpAmount4", 2500);
			PVP_AMOUNT5 = MasterMods.getInt("PvpAmount5", 5000);
			NAME_COLOR_FOR_PVP_AMOUNT1 = Integer.decode("0x" + MasterMods.getString("ColorForAmount1", "00FF00"));
			NAME_COLOR_FOR_PVP_AMOUNT2 = Integer.decode("0x" + MasterMods.getString("ColorForAmount2", "00FF00"));
			NAME_COLOR_FOR_PVP_AMOUNT3 = Integer.decode("0x" + MasterMods.getString("ColorForAmount3", "00FF00"));
			NAME_COLOR_FOR_PVP_AMOUNT4 = Integer.decode("0x" + MasterMods.getString("ColorForAmount4", "00FF00"));
			NAME_COLOR_FOR_PVP_AMOUNT5 = Integer.decode("0x" + MasterMods.getString("ColorForAmount5", "00FF00"));
			PK_COLOR_SYSTEM_ENABLED = MasterMods.getBoolean("EnablePkColorSystem", false);
			PK_AMOUNT1 = MasterMods.getInt("PkAmount1", 500);
			PK_AMOUNT2 = MasterMods.getInt("PkAmount2", 1000);
			PK_AMOUNT3 = MasterMods.getInt("PkAmount3", 1500);
			PK_AMOUNT4 = MasterMods.getInt("PkAmount4", 2500);
			PK_AMOUNT5 = MasterMods.getInt("PkAmount5", 5000);
			TITLE_COLOR_FOR_PK_AMOUNT1 = Integer.decode("0x" + MasterMods.getString("TitleForAmount1", "00FF00"));
			TITLE_COLOR_FOR_PK_AMOUNT2 = Integer.decode("0x" + MasterMods.getString("TitleForAmount2", "00FF00"));
			TITLE_COLOR_FOR_PK_AMOUNT3 = Integer.decode("0x" + MasterMods.getString("TitleForAmount3", "00FF00"));
			TITLE_COLOR_FOR_PK_AMOUNT4 = Integer.decode("0x" + MasterMods.getString("TitleForAmount4", "00FF00"));
			TITLE_COLOR_FOR_PK_AMOUNT5 = Integer.decode("0x" + MasterMods.getString("TitleForAmount5", "00FF00"));
			DUALBOX_PROTECTION_ENABLED = MasterMods.getBoolean("DualBoxProtection", false);
			DUALBOX_MAX_ALLOWED_PER_PC = MasterMods.getInt("DualBoxMaxAllowedPerPC", 1);
			DUALBOX_PREMIUM_MAX_ALLOWED_PER_PC = MasterMods.getInt("DualBoxPremiumMaxAllowedPerPC", 2);
			ALLOW_CUSTOM_MAX_AGGRO_RANGE = MasterMods.getBoolean("AllowCustomMaxAggroRange", true);
			CUSTOM_MAX_AGGRO_RANGE = MasterMods.getInt("CustomMaxAggroRange", 500);
			SPAWN_CHAR = MasterMods.getBoolean("CustomSpawn", false);
			SPAWN_X = MasterMods.getInt("SpawnX", 83400);
			SPAWN_Y = MasterMods.getInt("SpawnY", 147943);
			SPAWN_Z = MasterMods.getInt("SpawnZ", -3404);
			CUSTOM_SPAWN_FOR_RACE = MasterMods.getBoolean("CustomSpawnForRace", false);
			SPAWN_HUMAN_X = MasterMods.getInt("SpawnHumanX", 0);
			SPAWN_HUMAN_Y = MasterMods.getInt("SpawnHumanY", 0);
			SPAWN_HUMAN_Z = MasterMods.getInt("SpawnHumanZ", 0);
			SPAWN_ELF_X = MasterMods.getInt("SpawnElfX", 0);
			SPAWN_ELF_Y = MasterMods.getInt("SpawnElfY", 0);
			SPAWN_ELF_Z = MasterMods.getInt("SpawnElfZ", 0);
			SPAWN_DARKELF_X = MasterMods.getInt("SpawnDarkElfX", 0);
			SPAWN_DARKELF_Y = MasterMods.getInt("SpawnDarkElfY", 0);
			SPAWN_DARKELF_Z = MasterMods.getInt("SpawnDarkElfZ", 0);
			SPAWN_ORC_X = MasterMods.getInt("SpawnOrcX", 0);
			SPAWN_ORC_Y = MasterMods.getInt("SpawnOrcY", 0);
			SPAWN_ORC_Z = MasterMods.getInt("SpawnOrcZ", 0);
			SPAWN_DWARF_X = MasterMods.getInt("SpawnDwarfX", 0);
			SPAWN_DWARF_Y = MasterMods.getInt("SpawnDwarfY", 0);
			SPAWN_DWARF_Z = MasterMods.getInt("SpawnDwarfZ", 0);
			SPAWN_KAMAEL_X = MasterMods.getInt("SpawnKamaelX", 0);
			SPAWN_KAMAEL_Y = MasterMods.getInt("SpawnKamaelY", 0);
			SPAWN_KAMAEL_Z = MasterMods.getInt("SpawnKamaelZ", 0);
			
			// Load MasterCB L2Properties file (if exists)
			final PropertiesParser MasterCB = new PropertiesParser(MASTERCB_FILE);
			
			ENABLE_COMMUNITY_BOARD = MasterCB.getBoolean("EnableCommunityBoard", true);
			ALLOW_CUSTOM_CB = MasterCB.getBoolean("AllowCustomCB", true);
			ZEUS_ACTIVE = MasterCB.getBoolean("ZeusActivation", false);
			COMMUNITY_BOARD_ON_PEACE_ZONE = MasterCB.getBoolean("CBOnlyPeaceZone", true);
			COMMUNITY_BOARD_ON_COMBAT = MasterCB.getBoolean("CBOnCombat", true);
			BBS_DEFAULT = MasterCB.getString("BBSDefault", "_bbshome");
			COMMUNITY_TYPE = MasterCB.getInt("CommunityType", 1);
			BBS_SHOW_PLAYERLIST = MasterCB.getBoolean("BBSShowPlayerList", true);
			SHOW_LEVEL_COMMUNITYBOARD = MasterCB.getBoolean("ShowLevelOnCommunityBoard", true);
			MasterCB.getBoolean("ShowStatusOnCommunityBoard", true);
			NAME_PAGE_SIZE_COMMUNITYBOARD = MasterCB.getInt("NamePageSizeOnCommunityBoard", 50);
			NAME_PER_ROW_COMMUNITYBOARD = MasterCB.getInt("NamePerRowOnCommunityBoard", 5);
			ALLOW_COMMUNITY_MULTISELL = MasterCB.getBoolean("AllowCommunityMultisell", true);
			ALLOW_COMMUNITY_BUFFER = MasterCB.getBoolean("AllowCommunityBuffer", true);
			BUFF_CONSUMABLE_ID = MasterCB.getInt("BuffConsumableId", 57);
			ALLOW_COMMUNITY_STATS = MasterCB.getBoolean("AllowCommunityStats", true);
			ALLOW_COMMUNITY_TELEPORT = MasterCB.getBoolean("AllowCommunityTeleport", true);
			ALLOW_COMMUNITY_SERVICES = MasterCB.getBoolean("AllowCommunityServices", true);
			DelevelItemId = MasterCB.getInt("DelevelItemId", 23004);
			DelevelItemCount = MasterCB.getInt("DelevelItemCount", 1);
			NoblItemId = MasterCB.getInt("NoblItemId", 23004);
			NoblItemCount = MasterCB.getInt("NoblItemCount", 1);
			GenderItemId = MasterCB.getInt("GenderItemId", 23004);
			GenderItemCount = MasterCB.getInt("GenderItemCount", 1);
			HeroItemId = MasterCB.getInt("HeroItemId", 23004);
			HeroItemCount = MasterCB.getInt("HeroItemCount", 20);
			RecoveryVitalityItemId = MasterCB.getInt("RecoveryVitalityItemId", 23004);
			RecoveryVitalityItemCount = MasterCB.getInt("RecoveryVitalityItemCount", 1);
			SPItemId = MasterCB.getInt("SPItemId", 23004);
			SPItemCount = MasterCB.getInt("SPItemCount", 1);
			TOP_PLAYER_ROW_HEIGHT = MasterCB.getInt("TopPlayerRowHeight", 19);
			TOP_PLAYER_RESULTS = MasterCB.getInt("TopPlayerResults", 20);
			RAID_LIST_ROW_HEIGHT = MasterCB.getInt("RaidListRowHeight", 18);
			RAID_LIST_RESULTS = MasterCB.getInt("RaidListResults", 20);
			RAID_LIST_SORT_ASC = MasterCB.getBoolean("RaidListSortAsc", true);
			REGION_LOAD_TIMER = MasterCB.getInt("ReloadCommunityRegionTimer", 2);
			ONLY_USE_SHOP_PEACE_ZONE = MasterCB.getBoolean("OnlyUseShopPeaceZone", true);
			
			// Load MasterPremium L2Properties file (if exists)
			final PropertiesParser MasterPremium = new PropertiesParser(MASTERPREMIUM_FILE);
			
			ANTIBOT_CAPTCHA_ENABLE = MasterPremium.getBoolean("AntibotCaptchaEnabled", false);
			ANTIBOT_CAPTCHA_KILL_AMOUNT = MasterPremium.getInt("AntibotCaptchaKillAmount", 100);
			ANTIBOT_CAPTCHA_TIME = MasterPremium.getLong("AntibotCaptchaTime", 10);
			ANTIBOT_CAPTCHA_JAIL_TIME = MasterPremium.getInt("AntibotCaptchaJailTime", 10);
			COMMAND_SELL_BUFF = MasterPremium.getBoolean("CommandSellBuff", false);
			COMMAND_SELL_BUFF_MIN_PRICE = MasterPremium.getInt("CommandSellBuffMinPrice", 10);
			COMMAND_SELL_BUFF_MAX_PRICE = MasterPremium.getInt("CommandSellBuffMaxPrice", 1000000);
			ENABLE_LIST_SKILL_ALLOWED = MasterPremium.getBoolean("EnableListOfAllowedSkills", false);
			SELLBUFF_ONLY_CLAN_ALY_MEMBERS = MasterPremium.getBoolean("OnlySellToClanAllyMembers", false);
			array = MasterPremium.getString("CommandSellBuffListSkillsOff", "1323,327,1325,1326,1327").split(",");
			COMMAND_SELL_BUFF_LIST_SKILL_OFF = new int[array.length];
			for (int i = 0; i < array.length; i++)
			{
				COMMAND_SELL_BUFF_LIST_SKILL_OFF[i] = Integer.parseInt(array[i]);
			}
			Arrays.sort(COMMAND_SELL_BUFF_LIST_SKILL_OFF);
			array = MasterPremium.getString("CommandSellBuffListSkillsOn", "1059").split(",");
			COMMAND_SELL_BUFF_LIST_SKILL_ON = new int[array.length];
			for (int i = 0; i < array.length; i++)
			{
				COMMAND_SELL_BUFF_LIST_SKILL_ON[i] = Integer.parseInt(array[i]);
			}
			Arrays.sort(COMMAND_SELL_BUFF_LIST_SKILL_ON);
			
			ITEM_REWARD_PVP_ENABLE = MasterPremium.getBoolean("ItemRewardPVP", false);
			ITEM_REWARD_ID_PVP = MasterPremium.getInt("ItemRewardIDPVP", 57);
			ITEM_REWARD_COUNT_PVP = MasterPremium.getInt("ItemRewardCountPVP", 1000);
			FAME_REWARD_PVP_ENABLE = MasterPremium.getBoolean("FameRewardPVP", false);
			FAME_REWARD_COUNT_PVP = MasterPremium.getInt("FameRewardCountPVP", 100);
			ITEM_REWARD_PK_ENABLE = MasterPremium.getBoolean("ItemRewardPK", false);
			ITEM_REWARD_ID_PK = MasterPremium.getInt("ItemRewardIDPK", 57);
			ITEM_REWARD_COUNT_PK = MasterPremium.getInt("ItemRewardCountPK", 1000);
			FAME_REWARD_PK_ENABLE = MasterPremium.getBoolean("FameRewardPK", false);
			FAME_REWARD_COUNT_PK = MasterPremium.getInt("FameRewardCountPK", 100);
			PREMIUM_SYSTEM_ENABLED = MasterPremium.getBoolean("EnablePremiumSystem", false);
			PREMIUM_RATE_XP = MasterPremium.getFloat("PremiumRateXp", 2);
			PREMIUM_RATE_SP = MasterPremium.getFloat("PremiumRateSp", 2);
			PREMIUM_RATE_DROP_CHANCE = MasterPremium.getFloat("PremiumRateDropChance", 1);
			PREMIUM_RATE_DROP_AMOUNT = MasterPremium.getFloat("PremiumRateDropAmount", 2);
			String[] premiumDropChanceMultiplier = MasterPremium.getString("PremiumDropChanceMultiplierByItemId", "").split(";");
			PREMIUM_RATE_DROP_CHANCE_MULTIPLIER = new HashMap<>(premiumDropChanceMultiplier.length);
			if (!premiumDropChanceMultiplier[0].isEmpty())
			{
				for (String item : premiumDropChanceMultiplier)
				{
					String[] itemSplit = item.split(",");
					if (itemSplit.length != 2)
					{
						_log.warn(StringUtil.concat("Config.load(): invalid config property -> PremiumDropChanceMultiplierByItemId \"", item, "\""));
					}
					else
					{
						try
						{
							PREMIUM_RATE_DROP_CHANCE_MULTIPLIER.put(Integer.valueOf(itemSplit[0]), Float.valueOf(itemSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!item.isEmpty())
							{
								_log.warn(StringUtil.concat("Config.load(): invalid config property -> PremiumDropChanceMultiplierByItemId \"", item, "\""));
							}
						}
					}
				}
			}
			String[] premiumDropAmountMultiplier = MasterPremium.getString("PremiumDropAmountMultiplierByItemId", "").split(";");
			PREMIUM_RATE_DROP_AMOUNT_MULTIPLIER = new HashMap<>(premiumDropAmountMultiplier.length);
			if (!premiumDropAmountMultiplier[0].isEmpty())
			{
				for (String item : premiumDropAmountMultiplier)
				{
					String[] itemSplit = item.split(",");
					if (itemSplit.length != 2)
					{
						_log.warn(StringUtil.concat("Config.load(): invalid config property -> PremiumDropAmountMultiplierByItemId \"", item, "\""));
					}
					else
					{
						try
						{
							PREMIUM_RATE_DROP_AMOUNT_MULTIPLIER.put(Integer.valueOf(itemSplit[0]), Float.valueOf(itemSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!item.isEmpty())
							{
								_log.warn(StringUtil.concat("Config.load(): invalid config property -> PremiumDropAmountMultiplierByItemId \"", item, "\""));
							}
						}
					}
				}
			}
			SHOP_MIN_RANGE_FROM_PLAYER = MasterPremium.getInt("ShopMinRangeFromPlayer", 50);
			SHOP_MIN_RANGE_FROM_NPC = MasterPremium.getInt("ShopMinRangeFromNpc", 100);
			ALLOW_SUB_RACE = MasterPremium.getBoolean("AllowSubRace", false);
			NO_CANCELL_BUFF_IF_DIE = MasterPremium.getBoolean("RemainBuffWhenDie", false);
			NO_CANCELL_BUFF_IF_DIE_MAX_LVL = MasterPremium.getInt("RemainBuffWhenDieMaxLvl", 75);
			
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
			DEFAULT_CORPSE_TIME = NPC.getInt("DefaultCorpseTime", 7);
			SPOILED_CORPSE_EXTEND_TIME = NPC.getInt("SpoiledCorpseExtendTime", 10);
			CORPSE_CONSUME_SKILL_ALLOWED_TIME_BEFORE_DECAY = NPC.getInt("CorpseConsumeSkillAllowedTimeBeforeDecay", 2000);
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
					_log.warn("[CustomMinionsRespawnTime]: invalid config property -> CustomMinionsRespawnTime {}", prop);
				}
				
				try
				{
					MINIONS_RESPAWN_TIME.put(Integer.valueOf(propSplit[0]), Integer.valueOf(propSplit[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!prop.isEmpty())
					{
						_log.warn("[CustomMinionsRespawnTime]: invalid config property -> CustomMinionsRespawnTime {}", prop);
					}
				}
			}
			
			RAID_DISABLE_CURSE = NPC.getBoolean("DisableRaidCurse", false);
			RAID_CURSE_LEVEL = NPC.getInt("RaidCurseLevel", 8);
			RAID_CHAOS_TIME = NPC.getInt("RaidChaosTime", 10);
			GRAND_CHAOS_TIME = NPC.getInt("GrandChaosTime", 10);
			MINION_CHAOS_TIME = NPC.getInt("MinionChaosTime", 10);
			INVENTORY_MAXIMUM_PET = NPC.getInt("MaximumSlotsForPet", 12);
			PET_HP_REGEN_MULTIPLIER = NPC.getDouble("PetHpRegenMultiplier", 100) / 100;
			PET_MP_REGEN_MULTIPLIER = NPC.getDouble("PetMpRegenMultiplier", 100) / 100;
			
			CHANGE_STATUS = NPC.getInt("ChangeStatus", 30);
			CHANCE_SPAWN = NPC.getInt("ChanceSpawn", 50);
			RESPAWN_TIME = NPC.getInt("RespawnTime", 720);
			
			DROP_ADENA_MIN_LEVEL_DIFFERENCE = NPC.getInt("DropAdenaMinLevelDifference", 8);
			DROP_ADENA_MAX_LEVEL_DIFFERENCE = NPC.getInt("DropAdenaMaxLevelDifference", 15);
			DROP_ADENA_MIN_LEVEL_GAP_CHANCE = NPC.getDouble("DropAdenaMinLevelGapChance", 10);
			
			DROP_ITEM_MIN_LEVEL_DIFFERENCE = NPC.getInt("DropItemMinLevelDifference", 5);
			DROP_ITEM_MAX_LEVEL_DIFFERENCE = NPC.getInt("DropItemMaxLevelDifference", 10);
			DROP_ITEM_MIN_LEVEL_GAP_CHANCE = NPC.getDouble("DropItemMinLevelGapChance", 10);
			ITEM_BROKER_BID_RADIUS_ENABLE = NPC.getBoolean("EnableItemBrokerBidRadius", false);
			ITEM_BROKER_BID_RADIUS = NPC.getInt("MaxItemBrokerBidRadius", 700);
			SPAWN_RETURN_TELEPORT_RAID = NPC.getBoolean("TeleportRaid", false);
			SPAWN_RETURN_TELEPORT_MONSTER = NPC.getBoolean("TeleportNpc", false);
			String[] spawnReturnNPCExcluded = NPC.getString("ListSpawnReturnExcluded", "").split(",");
			SPAWN_RETURN_LIST_EXCLUDED = new ArrayList<>(spawnReturnNPCExcluded.length);
			for (String id : spawnReturnNPCExcluded)
			{
				SPAWN_RETURN_LIST_EXCLUDED.add(Integer.valueOf(id));
			}
			
			// Load Rates L2Properties file (if exists)
			final PropertiesParser RatesSettings = new PropertiesParser(RATES_CONFIG_FILE);
			
			RATE_XP = RatesSettings.getFloat("RateXp", 1);
			RATE_SP = RatesSettings.getFloat("RateSp", 1);
			RATE_PARTY_XP = RatesSettings.getFloat("RatePartyXp", 1);
			RATE_PARTY_SP = RatesSettings.getFloat("RatePartySp", 1);
			RATE_EXTRACTABLE = RatesSettings.getFloat("RateExtractable", 1);
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
			
			RATE_DEATH_DROP_AMOUNT_MULTIPLIER = RatesSettings.getFloat("DeathDropAmountMultiplier", 1);
			RATE_CORPSE_DROP_AMOUNT_MULTIPLIER = RatesSettings.getFloat("CorpseDropAmountMultiplier", 1);
			RATE_HERB_DROP_AMOUNT_MULTIPLIER = RatesSettings.getFloat("HerbDropAmountMultiplier", 1);
			RATE_RAID_DROP_AMOUNT_MULTIPLIER = RatesSettings.getFloat("RaidDropAmountMultiplier", 1);
			RATE_DEATH_DROP_CHANCE_MULTIPLIER = RatesSettings.getFloat("DeathDropChanceMultiplier", 1);
			RATE_CORPSE_DROP_CHANCE_MULTIPLIER = RatesSettings.getFloat("CorpseDropChanceMultiplier", 1);
			RATE_HERB_DROP_CHANCE_MULTIPLIER = RatesSettings.getFloat("HerbDropChanceMultiplier", 1);
			RATE_RAID_DROP_CHANCE_MULTIPLIER = RatesSettings.getFloat("RaidDropChanceMultiplier", 1);
			String[] dropAmountMultiplier = RatesSettings.getString("DropAmountMultiplierByItemId", "").split(";");
			RATE_DROP_AMOUNT_MULTIPLIER = new HashMap<>(dropAmountMultiplier.length);
			if (!dropAmountMultiplier[0].isEmpty())
			{
				for (String item : dropAmountMultiplier)
				{
					String[] itemSplit = item.split(",");
					if (itemSplit.length != 2)
					{
						_log.warn("Config.load(): invalid config property -> RateDropItemsById {}", item);
					}
					else
					{
						try
						{
							RATE_DROP_AMOUNT_MULTIPLIER.put(Integer.valueOf(itemSplit[0]), Float.valueOf(itemSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!item.isEmpty())
							{
								_log.warn("Config.load(): invalid config property -> RateDropItemsById {}", item);
							}
						}
					}
				}
			}
			
			String[] dropChanceMultiplier = RatesSettings.getString("DropChanceMultiplierByItemId", "").split(";");
			RATE_DROP_CHANCE_MULTIPLIER = new HashMap<>(dropChanceMultiplier.length);
			if (!dropChanceMultiplier[0].isEmpty())
			{
				for (String item : dropChanceMultiplier)
				{
					String[] itemSplit = item.split(",");
					if (itemSplit.length != 2)
					{
						_log.warn("Config.load(): invalid config property -> RateDropItemsById {}", item);
					}
					else
					{
						try
						{
							RATE_DROP_CHANCE_MULTIPLIER.put(Integer.valueOf(itemSplit[0]), Float.valueOf(itemSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!item.isEmpty())
							{
								_log.warn("Config.load(): invalid config property -> RateDropItemsById {}", item);
							}
						}
					}
				}
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
			L2JMOD_CHAMPION_REWARDS_EXP_SP = L2JModSettings.getFloat("ChampionRewardsExpSp", 8);
			L2JMOD_CHAMPION_REWARDS_CHANCE = L2JModSettings.getFloat("ChampionRewardsChance", 8);
			L2JMOD_CHAMPION_REWARDS_AMOUNT = L2JModSettings.getFloat("ChampionRewardsAmount", 1);
			L2JMOD_CHAMPION_ADENAS_REWARDS_CHANCE = L2JModSettings.getFloat("ChampionAdenasRewardsChance", 1);
			L2JMOD_CHAMPION_ADENAS_REWARDS_AMOUNT = L2JModSettings.getFloat("ChampionAdenasRewardsAmount", 1);
			L2JMOD_CHAMPION_ATK = L2JModSettings.getFloat("ChampionAtk", 1);
			L2JMOD_CHAMPION_SPD_ATK = L2JModSettings.getFloat("ChampionSpdAtk", 1);
			L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE = L2JModSettings.getInt("ChampionRewardLowerLvlItemChance", 0);
			L2JMOD_CHAMPION_REWARD_SAME_LVL_ITEM_CHANCE = L2JModSettings.getInt("ChampionRewardSameLvlItemChance", 0);
			L2JMOD_CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE = L2JModSettings.getInt("ChampionRewardHigherLvlItemChance", 0);
			L2JMOD_CHAMPION_REWARD_ID = L2JModSettings.getInt("ChampionRewardItemID", 6393);
			L2JMOD_CHAMPION_REWARD_QTY = L2JModSettings.getInt("ChampionRewardItemQty", 1);
			L2JMOD_CHAMPION_ENABLE_VITALITY = L2JModSettings.getBoolean("ChampionEnableVitality", false);
			L2JMOD_CHAMPION_ENABLE_AURA = L2JModSettings.getInt("ChampionEnableAura", 0);
			L2JMOD_CHAMPION_ENABLE_IN_INSTANCES = L2JModSettings.getBoolean("ChampionEnableInInstances", false);
			
			TVT_EVENT_ENABLED = L2JModSettings.getBoolean("TvTEventEnabled", false);
			TVT_EVENT_IN_INSTANCE = L2JModSettings.getBoolean("TvTEventInInstance", false);
			TVT_EVENT_INSTANCE_FILE = L2JModSettings.getString("TvTEventInstanceFile", "coliseum.xml");
			TVT_EVENT_INTERVAL = L2JModSettings.getString("TvTEventInterval", "20:00").split(",");
			TVT_EVENT_PARTICIPATION_TIME = L2JModSettings.getInt("TvTEventParticipationTime", 3600);
			TVT_EVENT_RUNNING_TIME = L2JModSettings.getInt("TvTEventRunningTime", 1800);
			TVT_EVENT_PARTICIPATION_NPC_ID = L2JModSettings.getInt("TvTEventParticipationNpcId", 0);
			
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
			
			if (TVT_EVENT_PARTICIPATION_NPC_ID == 0)
			{
				TVT_EVENT_ENABLED = false;
				_log.warn("TvTEventEngine[Config.load()]: invalid config property -> TvTEventParticipationNpcId");
			}
			else
			{
				String[] tvtNpcCoords = L2JModSettings.getString("TvTEventParticipationNpcCoordinates", "0,0,0").split(",");
				if (tvtNpcCoords.length < 3)
				{
					TVT_EVENT_ENABLED = false;
					_log.warn("TvTEventEngine[Config.load()]: invalid config property -> TvTEventParticipationNpcCoordinates");
				}
				else
				{
					TVT_EVENT_REWARDS = new ArrayList<>();
					TVT_DOORS_IDS_TO_OPEN = new ArrayList<>();
					TVT_DOORS_IDS_TO_CLOSE = new ArrayList<>();
					TVT_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
					TVT_EVENT_TEAM_1_COORDINATES = new int[3];
					TVT_EVENT_TEAM_2_COORDINATES = new int[3];
					TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0] = Integer.parseInt(tvtNpcCoords[0]);
					TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1] = Integer.parseInt(tvtNpcCoords[1]);
					TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2] = Integer.parseInt(tvtNpcCoords[2]);
					if (tvtNpcCoords.length == 4)
					{
						TVT_EVENT_PARTICIPATION_NPC_COORDINATES[3] = Integer.parseInt(tvtNpcCoords[3]);
					}
					TVT_EVENT_MIN_PLAYERS_IN_TEAMS = L2JModSettings.getInt("TvTEventMinPlayersInTeams", 1);
					TVT_EVENT_MAX_PLAYERS_IN_TEAMS = L2JModSettings.getInt("TvTEventMaxPlayersInTeams", 20);
					TVT_EVENT_MIN_LVL = L2JModSettings.getByte("TvTEventMinPlayerLevel", (byte) 1);
					TVT_EVENT_MAX_LVL = L2JModSettings.getByte("TvTEventMaxPlayerLevel", (byte) 80);
					TVT_EVENT_RESPAWN_TELEPORT_DELAY = L2JModSettings.getInt("TvTEventRespawnTeleportDelay", 20);
					TVT_EVENT_START_LEAVE_TELEPORT_DELAY = L2JModSettings.getInt("TvTEventStartLeaveTeleportDelay", 20);
					TVT_EVENT_EFFECTS_REMOVAL = L2JModSettings.getInt("TvTEventEffectsRemoval", 0);
					TVT_EVENT_MAX_PARTICIPANTS_PER_IP = L2JModSettings.getInt("TvTEventMaxParticipantsPerIP", 0);
					TVT_ALLOW_VOICED_COMMAND = L2JModSettings.getBoolean("TvTAllowVoicedInfoCommand", false);
					TVT_EVENT_TEAM_1_NAME = L2JModSettings.getString("TvTEventTeam1Name", "Team1");
					tvtNpcCoords = L2JModSettings.getString("TvTEventTeam1Coordinates", "0,0,0").split(",");
					if (tvtNpcCoords.length < 3)
					{
						TVT_EVENT_ENABLED = false;
						_log.warn("TvTEventEngine[Config.load()]: invalid config property -> TvTEventTeam1Coordinates");
					}
					else
					{
						TVT_EVENT_TEAM_1_COORDINATES[0] = Integer.parseInt(tvtNpcCoords[0]);
						TVT_EVENT_TEAM_1_COORDINATES[1] = Integer.parseInt(tvtNpcCoords[1]);
						TVT_EVENT_TEAM_1_COORDINATES[2] = Integer.parseInt(tvtNpcCoords[2]);
						TVT_EVENT_TEAM_2_NAME = L2JModSettings.getString("TvTEventTeam2Name", "Team2");
						tvtNpcCoords = L2JModSettings.getString("TvTEventTeam2Coordinates", "0,0,0").split(",");
						if (tvtNpcCoords.length < 3)
						{
							TVT_EVENT_ENABLED = false;
							_log.warn("TvTEventEngine[Config.load()]: invalid config property -> TvTEventTeam2Coordinates");
						}
						else
						{
							TVT_EVENT_TEAM_2_COORDINATES[0] = Integer.parseInt(tvtNpcCoords[0]);
							TVT_EVENT_TEAM_2_COORDINATES[1] = Integer.parseInt(tvtNpcCoords[1]);
							TVT_EVENT_TEAM_2_COORDINATES[2] = Integer.parseInt(tvtNpcCoords[2]);
							tvtNpcCoords = L2JModSettings.getString("TvTEventParticipationFee", "0,0").split(",");
							try
							{
								TVT_EVENT_PARTICIPATION_FEE[0] = Integer.parseInt(tvtNpcCoords[0]);
								TVT_EVENT_PARTICIPATION_FEE[1] = Integer.parseInt(tvtNpcCoords[1]);
							}
							catch (NumberFormatException nfe)
							{
								if (tvtNpcCoords.length > 0)
								{
									_log.warn("TvTEventEngine[Config.load()]: invalid config property -> TvTEventParticipationFee");
								}
							}
							tvtNpcCoords = L2JModSettings.getString("TvTEventReward", "57,100000").split(";");
							for (String reward : tvtNpcCoords)
							{
								String[] rewardSplit = reward.split(",");
								if (rewardSplit.length != 2)
								{
									_log.warn("TvTEventEngine[Config.load()]: invalid config property -> TvTEventReward {}", reward);
								}
								else
								{
									try
									{
										TVT_EVENT_REWARDS.add(new int[]
										{
											Integer.parseInt(rewardSplit[0]),
											Integer.parseInt(rewardSplit[1])
										});
									}
									catch (NumberFormatException nfe)
									{
										if (!reward.isEmpty())
										{
											_log.warn("TvTEventEngine[Config.load()]: invalid config property -> TvTEventReward {}", reward);
										}
									}
								}
							}
							
							TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED = L2JModSettings.getBoolean("TvTEventTargetTeamMembersAllowed", true);
							TVT_EVENT_SCROLL_ALLOWED = L2JModSettings.getBoolean("TvTEventScrollsAllowed", false);
							TVT_EVENT_POTIONS_ALLOWED = L2JModSettings.getBoolean("TvTEventPotionsAllowed", false);
							TVT_EVENT_SUMMON_BY_ITEM_ALLOWED = L2JModSettings.getBoolean("TvTEventSummonByItemAllowed", false);
							TVT_REWARD_TEAM_TIE = L2JModSettings.getBoolean("TvTRewardTeamTie", false);
							tvtNpcCoords = L2JModSettings.getString("TvTDoorsToOpen", "").split(";");
							for (String door : tvtNpcCoords)
							{
								try
								{
									TVT_DOORS_IDS_TO_OPEN.add(Integer.parseInt(door));
								}
								catch (NumberFormatException nfe)
								{
									if (!door.isEmpty())
									{
										_log.warn("TvTEventEngine[Config.load()]: invalid config property -> TvTDoorsToOpen {}", door);
									}
								}
							}
							
							tvtNpcCoords = L2JModSettings.getString("TvTDoorsToClose", "").split(";");
							for (String door : tvtNpcCoords)
							{
								try
								{
									TVT_DOORS_IDS_TO_CLOSE.add(Integer.parseInt(door));
								}
								catch (NumberFormatException nfe)
								{
									if (!door.isEmpty())
									{
										_log.warn("TvTEventEngine[Config.load()]: invalid config property -> TvTDoorsToClose {}", door);
									}
								}
							}
							
							tvtNpcCoords = L2JModSettings.getString("TvTEventFighterBuffs", "").split(";");
							if (!tvtNpcCoords[0].isEmpty())
							{
								TVT_EVENT_FIGHTER_BUFFS = new HashMap<>(tvtNpcCoords.length);
								for (String skill : tvtNpcCoords)
								{
									String[] skillSplit = skill.split(",");
									if (skillSplit.length != 2)
									{
										_log.warn("TvTEventEngine[Config.load()]: invalid config property -> TvTEventFighterBuffs {}", skill);
									}
									else
									{
										try
										{
											TVT_EVENT_FIGHTER_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
										}
										catch (NumberFormatException nfe)
										{
											if (!skill.isEmpty())
											{
												_log.warn("TvTEventEngine[Config.load()]: invalid config property -> TvTEventFighterBuffs {}", skill);
											}
										}
									}
								}
							}
							
							tvtNpcCoords = L2JModSettings.getString("TvTEventMageBuffs", "").split(";");
							if (!tvtNpcCoords[0].isEmpty())
							{
								TVT_EVENT_MAGE_BUFFS = new HashMap<>(tvtNpcCoords.length);
								for (String skill : tvtNpcCoords)
								{
									String[] skillSplit = skill.split(",");
									if (skillSplit.length != 2)
									{
										_log.warn("TvTEventEngine[Config.load()]: invalid config property -> TvTEventMageBuffs {}", skill);
									}
									else
									{
										try
										{
											TVT_EVENT_MAGE_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
										}
										catch (NumberFormatException nfe)
										{
											if (!skill.isEmpty())
											{
												_log.warn("TvTEventEngine[Config.load()]: invalid config property -> TvTEventMageBuffs {}", skill);
											}
										}
									}
								}
							}
						}
					}
				}
			}
			
			BANKING_SYSTEM_ENABLED = L2JModSettings.getBoolean("BankingEnabled", false);
			BANKING_SYSTEM_GOLDBARS = L2JModSettings.getInt("BankingGoldbarCount", 1);
			BANKING_SYSTEM_ADENA = L2JModSettings.getInt("BankingAdenaCount", 500000000);
			
			OFFLINE_TRADE_ENABLE = L2JModSettings.getBoolean("OfflineTradeEnable", false);
			OFFLINE_CRAFT_ENABLE = L2JModSettings.getBoolean("OfflineCraftEnable", false);
			OFFLINE_MODE_IN_PEACE_ZONE = L2JModSettings.getBoolean("OfflineModeInPeaceZone", false);
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
				_log.warn("MultiLang[Config.load()]: default language: {} is not in allowed list !", L2JMOD_MULTILANG_DEFAULT);
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
					_log.warn("DualboxCheck[Config.load()]: invalid config property -> DualboxCheckWhitelist {}", entry);
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
						_log.warn("DualboxCheck[Config.load()]: invalid address -> DualboxCheckWhitelist {}", entrySplit[0]);
					}
					catch (NumberFormatException e)
					{
						_log.warn("DualboxCheck[Config.load()]: invalid number -> DualboxCheckWhitelist {}", entrySplit[1]);
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
			
			ENABLE_CUSTOM_PERIOD = Olympiad.getBoolean("EnableCustomPeriod", false);
			propertySplit = Olympiad.getString("AltOlyEndDate", "1").split(",");
			ALT_OLY_END_DATE = new int[propertySplit.length];
			for (int i = 0; i < propertySplit.length; i++)
			{
				ALT_OLY_END_DATE[i] = Integer.parseInt(propertySplit[i]);
			}
			propertySplit = Olympiad.getString("AltOlyEndHour", "12:00:00").split(":");
			for (int i = 0; i < 3; i++)
			{
				ALT_OLY_END_HOUR[i] = Integer.parseInt(propertySplit[i]);
			}
			ALT_OLY_START_TIME = Olympiad.getInt("AltOlyStartTime", 18);
			ALT_OLY_MIN = Olympiad.getInt("AltOlyMin", 0);
			ALT_OLY_MAX_BUFFS = Olympiad.getInt("AltOlyMaxBuffs", 5);
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
						_log.warn("Could not load HexID file ({}). Hopefully login will give us one.", HEXID_FILE);
					}
				}
				else
				{
					_log.warn("Could not load HexID file ({}). Hopefully login will give us one.", HEXID_FILE);
				}
			}
			else
			{
				_log.warn("Could not load HexID file ({}). Hopefully login will give us one.", HEXID_FILE);
			}
			
			// Grand bosses
			final PropertiesParser GrandBossSettings = new PropertiesParser(GRANDBOSS_CONFIG_FILE);
			
			ANTHARAS_WAIT_TIME = GrandBossSettings.getInt("AntharasWaitTime", 30);
			ANTHARAS_SPAWN_INTERVAL = GrandBossSettings.getInt("IntervalOfAntharasSpawn", 264);
			ANTHARAS_SPAWN_RANDOM = GrandBossSettings.getInt("RandomOfAntharasSpawn", 72);
			
			VALAKAS_WAIT_TIME = GrandBossSettings.getInt("ValakasWaitTime", 30);
			VALAKAS_SPAWN_INTERVAL = GrandBossSettings.getInt("IntervalOfValakasSpawn", 264);
			VALAKAS_SPAWN_RANDOM = GrandBossSettings.getInt("RandomOfValakasSpawn", 72);
			
			BAIUM_SPAWN_INTERVAL = GrandBossSettings.getInt("IntervalOfBaiumSpawn", 168);
			BAIUM_SPAWN_RANDOM = GrandBossSettings.getInt("RandomOfBaiumSpawn", 48);
			
			CORE_SPAWN_INTERVAL = GrandBossSettings.getInt("IntervalOfCoreSpawn", 60);
			CORE_SPAWN_RANDOM = GrandBossSettings.getInt("RandomOfCoreSpawn", 24);
			
			ORFEN_SPAWN_INTERVAL = GrandBossSettings.getInt("IntervalOfOrfenSpawn", 48);
			ORFEN_SPAWN_RANDOM = GrandBossSettings.getInt("RandomOfOrfenSpawn", 20);
			
			QUEEN_ANT_SPAWN_INTERVAL = GrandBossSettings.getInt("IntervalOfQueenAntSpawn", 36);
			QUEEN_ANT_SPAWN_RANDOM = GrandBossSettings.getInt("RandomOfQueenAntSpawn", 17);
			
			BELETH_SPAWN_INTERVAL = GrandBossSettings.getInt("IntervalOfBelethSpawn", 192);
			BELETH_SPAWN_RANDOM = GrandBossSettings.getInt("RandomOfBelethSpawn", 148);
			BELETH_MIN_PLAYERS = GrandBossSettings.getInt("BelethMinPlayers", 36);
			
			// Gracia Seeds
			final PropertiesParser GraciaSeedsSettings = new PropertiesParser(GRACIASEEDS_CONFIG_FILE);
			
			// Seed of Destruction
			SOD_TIAT_KILL_COUNT = GraciaSeedsSettings.getInt("TiatKillCountForNextState", 10);
			SOD_STAGE_2_LENGTH = GraciaSeedsSettings.getLong("Stage2Length", 720) * 60000;
			
			try
			{
				//@formatter:off
				FILTER_LIST = Files.lines(Paths.get(CHAT_FILTER_FILE), StandardCharsets.UTF_8)
					.map(String::trim)
					.filter(line -> (!line.isEmpty() && (line.charAt(0) != '#')))
					.collect(Collectors.toList());
				//@formatter:on
				_log.info("Loaded {} filter words.", FILTER_LIST.size());
			}
			catch (IOException ioe)
			{
				_log.warn("Error while loading chat filter words!", ioe);
			}
			
			final PropertiesParser ClanHallSiege = new PropertiesParser(CH_SIEGE_FILE);
			
			CHS_MAX_ATTACKERS = ClanHallSiege.getInt("MaxAttackers", 500);
			CHS_CLAN_MINLEVEL = ClanHallSiege.getInt("MinClanLevel", 4);
			CHS_MAX_FLAGS_PER_CLAN = ClanHallSiege.getInt("MaxFlagsPerClan", 1);
			CHS_ENABLE_FAME = ClanHallSiege.getBoolean("EnableFame", false);
			CHS_FAME_AMOUNT = ClanHallSiege.getInt("FameAmount", 0);
			CHS_FAME_FREQUENCY = ClanHallSiege.getInt("FameFrequency", 0);
			
			final PropertiesParser geoData = new PropertiesParser(GEODATA_FILE);
			
			try
			{
				PATHNODE_DIR = new File(geoData.getString("PathnodeDirectory", "data/pathnode").replaceAll("\\\\", "/")).getCanonicalFile();
			}
			catch (IOException e)
			{
				_log.warn("Error setting pathnode directory!", e);
				PATHNODE_DIR = new File("data/pathnode");
			}
			
			PATHFINDING = geoData.getInt("PathFinding", 0);
			PATHFIND_BUFFERS = geoData.getString("PathFindBuffers", "100x6;128x6;192x6;256x4;320x4;384x4;500x2");
			LOW_WEIGHT = geoData.getFloat("LowWeight", 0.5f);
			MEDIUM_WEIGHT = geoData.getFloat("MediumWeight", 2);
			HIGH_WEIGHT = geoData.getFloat("HighWeight", 3);
			ADVANCED_DIAGONAL_STRATEGY = geoData.getBoolean("AdvancedDiagonalStrategy", true);
			DIAGONAL_WEIGHT = geoData.getFloat("DiagonalWeight", 0.707f);
			MAX_POSTFILTER_PASSES = geoData.getInt("MaxPostfilterPasses", 3);
			DEBUG_PATH = geoData.getBoolean("DebugPath", false);
			FORCE_GEODATA = geoData.getBoolean("ForceGeoData", true);
			COORD_SYNCHRONIZE = geoData.getInt("CoordSynchronize", -1);
			GEODATA_PATH = Paths.get(geoData.getString("GeoDataPath", "./data/geodata"));
			TRY_LOAD_UNSPECIFIED_REGIONS = geoData.getBoolean("TryLoadUnspecifiedRegions", true);
			GEODATA_REGIONS = new HashMap<>();
			for (int regionX = L2World.TILE_X_MIN; regionX <= L2World.TILE_X_MAX; regionX++)
			{
				for (int regionY = L2World.TILE_Y_MIN; regionY <= L2World.TILE_Y_MAX; regionY++)
				{
					String key = regionX + "_" + regionY;
					if (geoData.containskey(regionX + "_" + regionY))
					{
						GEODATA_REGIONS.put(key, geoData.getBoolean(key, false));
					}
				}
			}
			
			final PropertiesParser VoteSystem = new PropertiesParser(VOTE_FILE);
			
			API_KEYTOPZONE = VoteSystem.getString("ApiKeyTopzone", "dsad");
			SERVERID_KEYTOPZONE = VoteSystem.getInt("ServerIdTopzone", 12345);
			APIKEY_HOPZONE = VoteSystem.getString("ApiKeyHopzone", "dsada");
			SERVERID_NETWORK = VoteSystem.getString("ServerIdNetwork", "dasa");
			API_REWARD = VoteSystem.getInt("ApiVoteRewardId", 57);
			API_REWARD_AMOUNT = VoteSystem.getInt("ApiVoteRewardAmount", 100);
			
		}
		else if (Server.serverMode == Server.MODE_LOGINSERVER)
		{
			final PropertiesParser ServerSettings = new PropertiesParser(LOGIN_CONFIGURATION_FILE);
			
			ENABLE_UPNP = ServerSettings.getBoolean("EnableUPnP", true);
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
			DATABASE_CONNECTION_POOL = ServerSettings.getString("ConnectionPool", "HikariCP");
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
			case "rateextractable":
				RATE_EXTRACTABLE = Float.parseFloat(pValue);
				break;
			case "ratedropadena":
				RATE_DROP_AMOUNT_MULTIPLIER.put(Inventory.ADENA_ID, Float.parseFloat(pValue));
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
			case "defaultfinishtime":
				INSTANCE_FINISH_TIME = Integer.parseInt(pValue);
				break;
			case "restoreplayerinstance":
				RESTORE_PLAYER_INSTANCE = Boolean.parseBoolean(pValue);
				break;
			case "allowsummonininstance":
				ALLOW_SUMMON_IN_INSTANCE = Boolean.parseBoolean(pValue);
				break;
			case "ejectdeadplayertime":
				EJECT_DEAD_PLAYER_TIME = Integer.parseInt(pValue);
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
			case "enablecommunityboard":
				ENABLE_COMMUNITY_BOARD = Boolean.parseBoolean(pValue);
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
				ENCHANT_CHANCE_ELEMENT_STONE = Double.parseDouble(pValue);
				break;
			case "enchantchanceelementcrystal":
				ENCHANT_CHANCE_ELEMENT_CRYSTAL = Double.parseDouble(pValue);
				break;
			case "enchantchanceelementjewel":
				ENCHANT_CHANCE_ELEMENT_JEWEL = Double.parseDouble(pValue);
				break;
			case "enchantchanceelementenergy":
				ENCHANT_CHANCE_ELEMENT_ENERGY = Double.parseDouble(pValue);
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
			case "championrewardsexpsp":
				L2JMOD_CHAMPION_REWARDS_EXP_SP = Float.parseFloat(pValue);
				break;
			case "championrewardschance":
				L2JMOD_CHAMPION_REWARDS_CHANCE = Float.parseFloat(pValue);
				break;
			case "championrewardsamount":
				L2JMOD_CHAMPION_REWARDS_AMOUNT = Float.parseFloat(pValue);
				break;
			case "championadenasrewardschance":
				L2JMOD_CHAMPION_ADENAS_REWARDS_CHANCE = Float.parseFloat(pValue);
				break;
			case "championadenasrewardsamount":
				L2JMOD_CHAMPION_ADENAS_REWARDS_AMOUNT = Float.parseFloat(pValue);
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
			case "championrewardsamelvlitemchance":
				L2JMOD_CHAMPION_REWARD_SAME_LVL_ITEM_CHANCE = Integer.parseInt(pValue);
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
			case "tvteventenabled":
				TVT_EVENT_ENABLED = Boolean.parseBoolean(pValue);
				break;
			case "tvteventinterval":
				TVT_EVENT_INTERVAL = pValue.split(",");
				break;
			case "tvteventparticipationtime":
				TVT_EVENT_PARTICIPATION_TIME = Integer.parseInt(pValue);
				break;
			case "tvteventrunningtime":
				TVT_EVENT_RUNNING_TIME = Integer.parseInt(pValue);
				break;
			case "tvteventparticipationnpcid":
				TVT_EVENT_PARTICIPATION_NPC_ID = Integer.parseInt(pValue);
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
					_log.warn("Unable to set parameter value!", e);
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
			_log.warn("Failed to save hex id to {} file.", fileName, e);
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
		config.PUNISHMENT_TIME = properties.getInt(StringUtil.concat("FloodProtector", configString, "PunishmentTime"), 0) * 60000;
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
	
	public static final class ClassMasterSettings
	{
		private final Map<Integer, List<ItemHolder>> _claimItems = new HashMap<>(3);
		private final Map<Integer, List<ItemHolder>> _rewardItems = new HashMap<>(3);
		private final Map<Integer, Boolean> _allowedClassChange = new HashMap<>(3);
		
		public ClassMasterSettings(String configLine)
		{
			parseConfigLine(configLine.trim());
		}
		
		private void parseConfigLine(String configLine)
		{
			if (configLine.isEmpty())
			{
				return;
			}
			
			final StringTokenizer st = new StringTokenizer(configLine, ";");
			
			while (st.hasMoreTokens())
			{
				// get allowed class change
				final int job = Integer.parseInt(st.nextToken());
				
				_allowedClassChange.put(job, true);
				
				final List<ItemHolder> requiredItems = new ArrayList<>();
				// parse items needed for class change
				if (st.hasMoreTokens())
				{
					final StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
					
					while (st2.hasMoreTokens())
					{
						final StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
						final int itemId = Integer.parseInt(st3.nextToken());
						final int quantity = Integer.parseInt(st3.nextToken());
						requiredItems.add(new ItemHolder(itemId, quantity));
					}
				}
				
				_claimItems.put(job, requiredItems);
				
				final List<ItemHolder> rewardItems = new ArrayList<>();
				// parse gifts after class change
				if (st.hasMoreTokens())
				{
					final StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
					
					while (st2.hasMoreTokens())
					{
						final StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
						final int itemId = Integer.parseInt(st3.nextToken());
						final int quantity = Integer.parseInt(st3.nextToken());
						rewardItems.add(new ItemHolder(itemId, quantity));
					}
				}
				
				_rewardItems.put(job, rewardItems);
			}
		}
		
		public boolean isAllowed(int job)
		{
			if ((_allowedClassChange == null) || !_allowedClassChange.containsKey(job))
			{
				return false;
			}
			return _allowedClassChange.get(job);
		}
		
		public List<ItemHolder> getRewardItems(int job)
		{
			return _rewardItems.get(job);
		}
		
		public List<ItemHolder> getRequireItems(int job)
		{
			return _claimItems.get(job);
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
				_log.warn("parseItemsList[Config.load()]: invalid entry -> {}, should be itemId,itemNumber. Skipping to the next entry in the list.", valueSplit[0]);
				continue;
			}
			
			tmp = new int[2];
			try
			{
				tmp[0] = Integer.parseInt(valueSplit[0]);
			}
			catch (NumberFormatException e)
			{
				_log.warn("parseItemsList[Config.load()]: invalid itemId -> {}, value must be an integer. Skipping to the next entry in the list.", valueSplit[0]);
				continue;
			}
			try
			{
				tmp[1] = Integer.parseInt(valueSplit[1]);
			}
			catch (NumberFormatException e)
			{
				_log.warn("parseItemsList[Config.load()]: invalid item number -> {}, value must be an integer. Skipping to the next entry in the list.", valueSplit[1]);
				continue;
			}
			result[i++] = tmp;
		}
		return result;
	}
	
	private static class IPConfigData implements IXmlReader
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
			GameServer.printSection("Network Configuration");
			final File f = new File(IP_CONFIG_FILE);
			if (f.exists())
			{
				LOG.info("Using existing ipconfig.xml.");
				parseFile(new File(IP_CONFIG_FILE));
			}
			else
			// Auto configuration...
			{
				LOG.info("Using automatic network configuration.");
				autoIpConfig();
			}
		}
		
		@Override
		public void parseDocument(Document doc)
		{
			NamedNodeMap attrs;
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
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
								LOG.warn("Failed to load {} file - subnets does not match server addresses.", IP_CONFIG_FILE);
							}
						}
					}
					
					Node att = n.getAttributes().getNamedItem("address");
					if (att == null)
					{
						LOG.warn("Failed to load {} file - default server address is missing.", IP_CONFIG_FILE);
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
				LOG.warn("Failed to connect to api.externalip.net please check your internet connection using 127.0.0.1!");
				externalIp = "127.0.0.1";
			}
			
			try
			{
				Enumeration<NetworkInterface> niList = NetworkInterface.getNetworkInterfaces();
				
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
						
						final String hostAddress = ia.getAddress().getHostAddress();
						final int subnetPrefixLength = ia.getNetworkPrefixLength();
						final int subnetMaskInt = IntStream.rangeClosed(1, subnetPrefixLength).reduce((r, e) -> (r << 1) + 1).orElse(0) << (32 - subnetPrefixLength);
						final int hostAddressInt = Arrays.stream(hostAddress.split("\\.")).mapToInt(Integer::parseInt).reduce((r, e) -> (r << 8) + e).orElse(0);
						final int subnetAddressInt = hostAddressInt & subnetMaskInt;
						final String subnetAddress = ((subnetAddressInt >> 24) & 0xFF) + "." + ((subnetAddressInt >> 16) & 0xFF) + "." + ((subnetAddressInt >> 8) & 0xFF) + "." + (subnetAddressInt & 0xFF);
						final String subnet = subnetAddress + '/' + subnetPrefixLength;
						if (!_subnets.contains(subnet) && !subnet.equals("0.0.0.0/0"))
						{
							_subnets.add(subnet);
							_hosts.add(hostAddress);
							LOG.info("Adding new subnet: " + subnet + " address: " + hostAddress);
						}
					}
				}
				
				// External host and subnet
				_hosts.add(externalIp);
				_subnets.add("0.0.0.0/0");
				LOG.info("Adding new subnet: 0.0.0.0/0 address: {}", externalIp);
			}
			catch (SocketException e)
			{
				LOG.error("Configuration failed please manually configure ipconfig.xml", e);
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
