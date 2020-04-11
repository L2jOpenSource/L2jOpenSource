package com.l2jfrozen.gameserver.model.actor.instance;

import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_MOVE_TO;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.l2jfrozen.Config;
import com.l2jfrozen.crypt.nProtect;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.ai.L2CharacterAI;
import com.l2jfrozen.gameserver.ai.L2PlayerAI;
import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.cache.WarehouseCacheManager;
import com.l2jfrozen.gameserver.communitybbs.BB.Forum;
import com.l2jfrozen.gameserver.communitybbs.Manager.ForumsBBSManager;
import com.l2jfrozen.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.controllers.RecipeController;
import com.l2jfrozen.gameserver.datatables.AccessLevel;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.datatables.HeroSkillTable;
import com.l2jfrozen.gameserver.datatables.NobleSkillTable;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.csv.FishTable;
import com.l2jfrozen.gameserver.datatables.csv.HennaTable;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.datatables.csv.RecipeTable;
import com.l2jfrozen.gameserver.datatables.sql.AccessLevels;
import com.l2jfrozen.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2jfrozen.gameserver.datatables.sql.CharTemplateTable;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.sql.SkillTreeTable;
import com.l2jfrozen.gameserver.datatables.xml.ExperienceData;
import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.handler.ItemHandler;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminEditChar;
import com.l2jfrozen.gameserver.handler.skillhandlers.SiegeFlag;
import com.l2jfrozen.gameserver.handler.skillhandlers.StrSiegeAssault;
import com.l2jfrozen.gameserver.handler.skillhandlers.TakeCastle;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.CoupleManager;
import com.l2jfrozen.gameserver.managers.CursedWeaponsManager;
import com.l2jfrozen.gameserver.managers.DimensionalRiftManager;
import com.l2jfrozen.gameserver.managers.DuelManager;
import com.l2jfrozen.gameserver.managers.FortSiegeManager;
import com.l2jfrozen.gameserver.managers.ItemsOnGroundManager;
import com.l2jfrozen.gameserver.managers.QuestManager;
import com.l2jfrozen.gameserver.managers.SiegeManager;
import com.l2jfrozen.gameserver.managers.TownManager;
import com.l2jfrozen.gameserver.model.BlockList;
import com.l2jfrozen.gameserver.model.FishData;
import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.model.ItemContainer;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2ClanMember;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Fishing;
import com.l2jfrozen.gameserver.model.L2Macro;
import com.l2jfrozen.gameserver.model.L2ManufactureList;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.L2Radar;
import com.l2jfrozen.gameserver.model.L2RecipeList;
import com.l2jfrozen.gameserver.model.L2Request;
import com.l2jfrozen.gameserver.model.L2ShortCut;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillTargetType;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.L2SkillLearn;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.MacroList;
import com.l2jfrozen.gameserver.model.PartyMatchRoom;
import com.l2jfrozen.gameserver.model.PartyMatchRoomList;
import com.l2jfrozen.gameserver.model.PartyMatchWaitingList;
import com.l2jfrozen.gameserver.model.PcFreight;
import com.l2jfrozen.gameserver.model.PcInventory;
import com.l2jfrozen.gameserver.model.PcWarehouse;
import com.l2jfrozen.gameserver.model.PetInventory;
import com.l2jfrozen.gameserver.model.PlayerStatus;
import com.l2jfrozen.gameserver.model.ShortCuts;
import com.l2jfrozen.gameserver.model.TradeList;
import com.l2jfrozen.gameserver.model.actor.appearance.PcAppearance;
import com.l2jfrozen.gameserver.model.actor.knownlist.PcKnownList;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.model.actor.stat.PcStat;
import com.l2jfrozen.gameserver.model.actor.status.PcStatus;
import com.l2jfrozen.gameserver.model.base.ClassId;
import com.l2jfrozen.gameserver.model.base.ClassLevel;
import com.l2jfrozen.gameserver.model.base.PlayerClass;
import com.l2jfrozen.gameserver.model.base.Race;
import com.l2jfrozen.gameserver.model.base.SubClass;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.entity.Duel;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.L2Event;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSignsFestival;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.entity.siege.FortSiege;
import com.l2jfrozen.gameserver.model.entity.siege.Siege;
import com.l2jfrozen.gameserver.model.entity.siege.clanhalls.DevastatedCastle;
import com.l2jfrozen.gameserver.model.extender.BaseExtender.EventType;
import com.l2jfrozen.gameserver.model.holder.TimeStamp;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.model.quest.QuestState;
import com.l2jfrozen.gameserver.model.zone.type.L2TownZone;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.ChangeWaitType;
import com.l2jfrozen.gameserver.network.serverpackets.CharInfo;
import com.l2jfrozen.gameserver.network.serverpackets.ConfirmDlg;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ExAutoSoulShot;
import com.l2jfrozen.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import com.l2jfrozen.gameserver.network.serverpackets.ExFishingEnd;
import com.l2jfrozen.gameserver.network.serverpackets.ExFishingStart;
import com.l2jfrozen.gameserver.network.serverpackets.ExOlympiadMode;
import com.l2jfrozen.gameserver.network.serverpackets.ExOlympiadUserInfo;
import com.l2jfrozen.gameserver.network.serverpackets.ExPCCafePointInfo;
import com.l2jfrozen.gameserver.network.serverpackets.ExSetCompassZoneCode;
import com.l2jfrozen.gameserver.network.serverpackets.FriendList;
import com.l2jfrozen.gameserver.network.serverpackets.HennaInfo;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfrozen.gameserver.network.serverpackets.LeaveWorld;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillCanceld;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.NpcInfo;
import com.l2jfrozen.gameserver.network.serverpackets.ObservationMode;
import com.l2jfrozen.gameserver.network.serverpackets.ObservationReturn;
import com.l2jfrozen.gameserver.network.serverpackets.PartySmallWindowUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.PetInventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.PrivateStoreListBuy;
import com.l2jfrozen.gameserver.network.serverpackets.PrivateStoreListSell;
import com.l2jfrozen.gameserver.network.serverpackets.QuestList;
import com.l2jfrozen.gameserver.network.serverpackets.RecipeShopSellList;
import com.l2jfrozen.gameserver.network.serverpackets.RelationChanged;
import com.l2jfrozen.gameserver.network.serverpackets.Ride;
import com.l2jfrozen.gameserver.network.serverpackets.SendTradeDone;
import com.l2jfrozen.gameserver.network.serverpackets.SetupGauge;
import com.l2jfrozen.gameserver.network.serverpackets.ShortBuffStatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ShortCutInit;
import com.l2jfrozen.gameserver.network.serverpackets.SkillCoolTime;
import com.l2jfrozen.gameserver.network.serverpackets.SkillList;
import com.l2jfrozen.gameserver.network.serverpackets.Snoop;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.StopMove;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.TargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.TitleUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.TradePressOtherOk;
import com.l2jfrozen.gameserver.network.serverpackets.TradePressOwnOk;
import com.l2jfrozen.gameserver.network.serverpackets.TradeStart;
import com.l2jfrozen.gameserver.network.serverpackets.UserInfo;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.skills.BaseStats;
import com.l2jfrozen.gameserver.skills.Formulas;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.skills.effects.EffectCharge;
import com.l2jfrozen.gameserver.skills.l2skills.L2SkillSummon;
import com.l2jfrozen.gameserver.templates.L2Armor;
import com.l2jfrozen.gameserver.templates.L2ArmorType;
import com.l2jfrozen.gameserver.templates.L2EtcItemType;
import com.l2jfrozen.gameserver.templates.L2Henna;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2PcTemplate;
import com.l2jfrozen.gameserver.templates.L2Weapon;
import com.l2jfrozen.gameserver.templates.L2WeaponType;
import com.l2jfrozen.gameserver.thread.LoginServerThread;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.thread.daemons.ItemsAutoDestroy;
import com.l2jfrozen.gameserver.util.Broadcast;
import com.l2jfrozen.gameserver.util.FloodProtectors;
import com.l2jfrozen.gameserver.util.IllegalPlayerAction;
import com.l2jfrozen.gameserver.util.Util;
import com.l2jfrozen.logs.Log;
import com.l2jfrozen.netcore.MMOConnection;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.Point3D;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.random.Rnd;

import javolution.text.TextBuilder;
import main.EngineModsManager;
import main.data.memory.ObjectData;
import main.holders.objects.PlayerHolder;

/**
 * This class represents all player characters in the world.<br>
 * There is always a client-thread connected to this (except if a player-store is activated upon logout).
 * @version $Revision: 1.6.4 $ $Date: 2009/05/12 19:46:09 $
 * @author  l2jfrozen dev
 */
public class L2PcInstance extends L2PlayableInstance
{
	private static final String RESTORE_SKILLS_FOR_CHAR = "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?";
	
	private static final String ADD_NEW_SKILL = "INSERT INTO character_skills (char_obj_id,skill_id,skill_level,skill_name,class_index) VALUES (?,?,?,?,?)";
	
	private static final String UPDATE_CHARACTER_SKILL_LEVEL = "UPDATE character_skills SET skill_level=? WHERE skill_id=? AND char_obj_id=? AND class_index=?";
	
	private static final String DELETE_SKILL_FROM_CHAR = "DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?";
	
	private static final String DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?";
	
	private static final String INSERT_CHARACTER_SKILLS_SAVE = "INSERT INTO character_skills_save (char_obj_id,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?)";
	
	private static final String SELECT_CHARACTER_SKILLS_SAVE = "SELECT skill_id,skill_level,effect_count,effect_cur_time, reuse_delay, systime FROM character_skills_save WHERE char_obj_id=? AND class_index=? AND restore_type=? ORDER BY buff_index ASC";
	
	private static final String DELETE_CHARACTER_SKILLS_SAVE = "DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?";
	
	private static final String SELECT_PK_KILLS = "SELECT kills FROM pkkills WHERE killerId=? AND killedId=?";
	private static final String UPDATE_PK_KILLS = "UPDATE pkkills SET kills=? WHERE killerId=? AND killedId=?";
	private static final String INSERT_PK_KILLS = "INSERT INTO pkkills (killerId,killedId,kills) VALUES (?,?,?)";
	
	private static final String UPDATE_CHARACTER_ONLINE_STATUS = "UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?";
	
	private static final String UPDATE_CHARACTER_IS_IN_7S_DUNGEON = "UPDATE characters SET isIn7sDungeon=?, lastAccess=? WHERE obj_id=?";
	
	private static final String UPDATE_FIRST_LOG = "UPDATE characters SET first_log=? WHERE obj_id=?";
	
	private static final String SELECT_CHARACTERS_IN_ACCOUNT = "SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id<>?";
	
	private static final String SELECT_CHARACTER_RECIPE_BOOK = "SELECT id, type FROM character_recipebook WHERE char_id=?";
	
	private static final String UPDATE_CHARACTER_SEX = "UPDATE characters SET sex=? WHERE obj_Id=?";
	
	private static final String SELECT_CHARACTER_FRIENDS = "SELECT friend_name,not_blocked FROM character_friends WHERE char_id=?";
	
	private static final String INSERT_NEW_CHARACTER = "INSERT INTO characters (account_name,obj_Id,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,acc,crit,evasion,mAtk,mDef,mSpd,pAtk,pDef,pSpd,runSpd,walkSpd,str,con,dex,_int,men,wit,face,hairStyle,hairColor,sex,movement_multiplier,attack_speed_multiplier,colRad,colHeight,exp,sp,karma,pvpkills,pkkills,clanid,maxload,race,classid,deletetime,cancraft,title,accesslevel,online,isin7sdungeon,clan_privs,wantspeace,base_class,newbie,nobless,power_grade,last_recom_date,name_color,title_color) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	private static final String SELECT_CHARACTER_BY_OBJ_ID = "SELECT account_name, obj_Id, char_name, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, acc, crit, evasion, mAtk, mDef, mSpd, pAtk, pDef, pSpd, runSpd, walkSpd, str, con, dex, _int, men, wit, face, hairStyle, hairColor, sex, heading, x, y, z, movement_multiplier, attack_speed_multiplier, colRad, colHeight, exp, expBeforeDeath, sp, karma, pvpkills, pkkills, clanid, maxload, race, classid, deletetime, cancraft, title, rec_have, rec_left, accesslevel, online, char_slot, lastAccess, clan_privs, wantspeace, base_class, onlinetime, isin7sdungeon,punish_level,punish_timer,newbie, nobless, power_grade, subpledge, last_recom_date, lvl_joined_academy, apprentice, sponsor, varka_ketra_ally,clan_join_expiry_time,clan_create_expiry_time,death_penalty_level,pc_point,name_color,title_color,first_log FROM characters WHERE obj_id=?";
	
	private static final String UPDATE_CHARACTER_BY_OBJ_ID = "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,str=?,con=?,dex=?,_int=?,men=?,wit=?,face=?,hairStyle=?,hairColor=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,pvpkills=?,pkkills=?,rec_have=?,rec_left=?,clanid=?,maxload=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,punish_level=?,punish_timer=?,newbie=?,nobless=?,power_grade=?,subpledge=?,last_recom_date=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=?,pc_point=?,name_color=?,title_color=? WHERE obj_id=?";
	
	private static final String RESTORE_SKILLS_FOR_CHAR_ALT_SUBCLASS = "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? ORDER BY skill_level";
	
	private static final String RESTORE_CHAR_SUBCLASSES = "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE char_obj_id=? ORDER BY class_index ASC";
	
	private static final String ADD_CHAR_SUBCLASS = "INSERT INTO character_subclasses (char_obj_id,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)";
	
	private static final String UPDATE_CHAR_SUBCLASS = "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE char_obj_id=? AND class_index=?";
	
	private static final String DELETE_CHAR_SUBCLASS = "DELETE FROM character_subclasses WHERE char_obj_id=? AND class_index=?";
	
	private static final String RESTORE_CHAR_HENNAS = "SELECT slot,symbol_id FROM character_hennas WHERE char_obj_id=? AND class_index=?";
	
	private static final String ADD_CHAR_HENNA = "INSERT INTO character_hennas (char_obj_id,symbol_id,slot,class_index) VALUES (?,?,?,?)";
	
	private static final String DELETE_CHAR_HENNA = "DELETE FROM character_hennas WHERE char_obj_id=? AND slot=? AND class_index=?";
	
	private static final String DELETE_CHAR_HENNAS = "DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?";
	
	private static final String DELETE_CHAR_SHORTCUTS = "DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=?";
	
	private static final String RESTORE_CHAR_RECOMS = "SELECT char_id,target_id FROM character_recommends WHERE char_id=?";
	
	private static final String ADD_CHAR_RECOM = "INSERT INTO character_recommends (char_id,target_id) VALUES (?,?)";
	
	private static final String DELETE_CHAR_RECOMS = "DELETE FROM character_recommends WHERE char_id=?";
	
	private static final String INSERT_CHAR_RECIPEBOOK_NORMAL = "INSERT INTO character_recipebook (char_id, id, type) VALUES(?,?,0)";
	private static final String INSERT_CHAR_RECIPEBOOK_DWARF = "INSERT INTO character_recipebook (char_id, id, type) VALUES(?,?,1)";
	
	public static final String SELECT_HERO_COUNT = "SELECT count FROM heroes WHERE char_name=?";
	
	public static final String SELECT_CHARACTER_VARIABLES = "SELECT variable, value FROM character_variables WHERE char_obj_id=?";
	public static final String INSERT_CHARACTER_VARIABLE = "INSERT INTO character_variables (char_obj_id, variable, value) VALUES (?,?,?)";
	public static final String DELETE_CHARACTER_VARIABLE = "DELETE FROM character_variables WHERE char_obj_id=? AND variable=?";
	
	public static final String SELECT_ACCOUNT_VARIABLES = "SELECT variable, value FROM account_variables WHERE account_name=?";
	public static final String INSERT_ACCOUNT_VARIABLE = "INSERT INTO account_variables (account_name, variable, value) VALUES (?,?,?)";
	public static final String DELETE_ACCOUNT_VARIABLE = "DELETE FROM account_variables WHERE account_name=? AND variable=?";
	
	public static final int REQUEST_TIMEOUT = 15;
	
	public static final int STORE_PRIVATE_NONE = 0;
	public static final int STORE_PRIVATE_SELL = 1;
	public static final int STORE_PRIVATE_BUY = 3;
	public static final int STORE_PRIVATE_MANUFACTURE = 5;
	public static final int STORE_PRIVATE_PACKAGE_SELL = 8;
	
	private static final int[] EXPERTISE_LEVELS =
	{
		SkillTreeTable.getInstance().getExpertiseLevel(0), // NONE
		SkillTreeTable.getInstance().getExpertiseLevel(1), // D
		SkillTreeTable.getInstance().getExpertiseLevel(2), // C
		SkillTreeTable.getInstance().getExpertiseLevel(3), // B
		SkillTreeTable.getInstance().getExpertiseLevel(4), // A
		SkillTreeTable.getInstance().getExpertiseLevel(5), // S
	};
	
	private static final int[] COMMON_CRAFT_LEVELS =
	{
		5,
		20,
		28,
		36,
		43,
		49,
		55,
		62
	};
	
	// Player / Character variables to control the name in one place
	public static final String HERO_END = "heroEnd";
	public static final String VIP_END = "vipEnd";
	public static final String AIO_END = "aioEnd";
	
	private boolean posticipateSit;
	protected boolean sittingTaskLaunched;
	private PlayerStatus saved_status = null;
	private final long instanceLoginTime;
	private long lastTeleportAction = 0;
	protected long TOGGLE_USE = 0;
	
	public int activeBoxesCount = -1;
	public List<String> active_boxes_characters = new ArrayList<>();
	
	private L2GameClient playerClient;
	private String accountName;
	private long deleteTimer;
	private boolean online = false;
	private long onlineTime;
	private long onlineBeginTime;
	private long lastAccess;
	private long uptime;
	protected int baseClass;
	protected int activeClass;
	protected int playerClassIndex = 0;
	private boolean isFirstLog;
	private int pcBangPoint = 0;
	private Map<Integer, SubClass> subClasses;
	private PcAppearance appearance;
	
	/** The Identifier of the L2PcInstance. */
	private int charId = 0x00030b7a;
	
	/** The Experience of the L2PcInstance before the last Death Penalty. */
	private long expBeforeDeath;
	
	/** The Karma of the L2PcInstance (if higher than 0, the name of the L2PcInstance appears in red). */
	private int playerKarma;
	
	/** The number of player killed during a PvP (the player killed was PvP Flagged). */
	private int pvpKills;
	
	/** The PK counter of the L2PcInstance (= Number of non PvP Flagged player killed). */
	private int pkKills;
	
	private int lastKill = 0;
	private int count = 0;
	
	/** The PvP Flag state of the L2PcInstance (0=White, 1=Purple). */
	private byte pvpFlag;
	
	private byte siegeState = 0;
	private int curWeightPenalty = 0;
	
	/** The last compass zone. */
	private int lastCompassZone; // the last compass zone update send to the client
	private byte zoneValidateCounter = 4;
	private boolean playerIsIn7sDungeon = false;
	
	/** Special hero aura values. */
	private int heroConsecutiveKillCount = 0;
	private boolean isPVPHero = false;
	
	/** character away mode *. */
	private boolean awaying = false;
	private boolean isAway = false;
	public int originalTitleColorAway;
	public String originalTitleAway;
	
	private boolean isAio = false;
	private long aioEndTime = 0;
	
	/** Event parameters. */
	public int eventX;
	public int eventY;
	public int eventZ;
	
	public int eventKarma;
	public int eventPvpKills;
	public int eventPkKills;
	public String eventTitle;
	public List<String> kills = new LinkedList<>();
	public boolean eventSitForced = false;
	public boolean atEvent = false;
	
	/** TvT Engine parameters. */
	public String teamNameTvT;
	public String originalTitleTvT;
	public int originalNameColorTvT = 0;
	public int countTvTkills;
	public int countTvTdies;
	public int originalKarmaTvT;
	public boolean inEventTvT = false;
	
	/** CTF Engine parameters. */
	public String teamNameCTF;
	public String teamNameHaveFlagCTF;
	public String originalTitleCTF;
	public int originalNameColorCTF = 0, originalKarmaCTF, countCTFflags;
	public boolean inEventCTF = false, haveFlagCTF = false;
	public Future<?> posCheckerCTF = null;
	
	/** DM Engine parameters. */
	public String originalTitleDM;
	public int originalNameColorDM = 0;
	public int countDMkills;
	public int originalKarmaDM;
	public boolean inEventDM = false;
	
	/** Event Engine parameters. */
	public int originalNameColor;
	public int countKills;
	public int originalKarma;
	public int eventKills;
	
	public boolean inEvent = false;
	private boolean inOlympiadMode = false;
	private boolean inOlympiadFight = false;
	private int[] olympiadPosition;
	private int olympiadGameId = -1;
	private int olympiadSide = -1;
	private boolean isInDuel = false;
	private int duelState = Duel.DUELSTATE_NODUEL;
	private int playerDuelId = 0;
	private SystemMessageId noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
	
	private boolean inBoat;
	private L2BoatInstance boat;
	private Point3D inBoatPosition;
	private int mountType;
	
	/** Store object used to summon the strider you are mounting *. */
	private int mountObjectID = 0;
	
	public int telemode = 0;
	private int isSilentMoving = 0;
	private boolean inCrystallize;
	private boolean inCraftMode;
	
	/** The table containing all L2RecipeList of the L2PcInstance. */
	private final Map<Integer, L2RecipeList> dwarvenRecipeBook = new HashMap<>();
	private final Map<Integer, L2RecipeList> commonRecipeBook = new HashMap<>();
	
	/** True if the L2PcInstance is sitting. */
	private boolean waitTypeSitting;
	
	/** True if the L2PcInstance is using the relax skill. */
	private boolean relax;
	
	/** Location before entering Observer Mode. */
	private int obsX;
	private int obsY;
	private int obsZ;
	private boolean observerMode = false;
	
	/** Stored from last ValidatePosition *. */
	private Location lastClientPosition = new Location(0, 0, 0);
	private Location lastServerPosition = new Location(0, 0, 0);
	
	/** The number of recommandation obtained by the L2PcInstance. */
	private int recomHave; // how much I was recommended by others
	
	/** The number of recommandation that the L2PcInstance can give. */
	private int recomLeft; // how many recomendations I can give to others
	
	/** Date when recom points were updated last time. */
	private long lastRecomUpdate;
	
	/** List with the recomendations that I've give. */
	private final List<Integer> recomChars = new ArrayList<>();
	
	private final PcInventory inventory = new PcInventory(this);
	private PcWarehouse warehouse;
	private final PcFreight freight = new PcFreight(this);
	
	/** The Private Store type of the L2PcInstance (STORE_PRIVATE_NONE=0, STORE_PRIVATE_SELL=1, sellmanage=2, STORE_PRIVATE_BUY=3, buymanage=4, STORE_PRIVATE_MANUFACTURE=5). */
	private int privatestore;
	
	private TradeList activeTradeList;
	private ItemContainer activeWarehouse;
	private L2ManufactureList createList;
	private TradeList sellList;
	private TradeList buyList;
	private boolean newbie;
	private boolean isNoblePlayer = false;
	private boolean isHeroPlayer = false;
	private long heroEndDate = 0;
	private boolean vip = false;
	private long vipEndDate = 0;
	
	/** The L2FolkInstance corresponding to the last Folk wich one the player talked. */
	private L2FolkInstance lastFolkNpc = null;
	
	/** Last NPC Id talked on a quest. */
	private int questNpcObject = 0;
	
	private int party_find = 0;
	private final SummonRequest summonRequest = new SummonRequest();
	
	/** The table containing all Quests began by the L2PcInstance. */
	private final Map<String, QuestState> playerQuests = new HashMap<>();
	
	/** The list containing all shortCuts of this L2PcInstance. */
	private final ShortCuts shortCuts = new ShortCuts(this);
	
	/** The list containing all macroses of this L2PcInstance. */
	private final MacroList macroses = new MacroList(this);
	
	/** The snoop listener. */
	private final List<L2PcInstance> snoopListener = new ArrayList<>();
	
	/** The snooped player. */
	private final List<L2PcInstance> snoopedPlayer = new ArrayList<>();
	
	/** The skill learning class id. */
	private ClassId skillLearningClassId;
	
	// hennas
	/** The henna. */
	private final L2HennaInstance[] playerHenna = new L2HennaInstance[3];
	
	private int hennaSTR;
	private int hennaINT;
	private int hennaDEX;
	private int hennaMEN;
	private int hennaWIT;
	private int hennaCON;
	
	/** The L2Summon of the L2PcInstance. */
	private L2Summon summon = null;
	
	// apparently, a L2PcInstance CAN have both a summon AND a tamed beast at the same time!!
	/** The tamed beast. */
	private L2TamedBeastInstance tamedBeast = null;
	
	/** client radar. */
	private L2Radar radar;
	
	/** Clan related attributes. */
	private int clanId = 0;
	private L2Clan clan;
	private int apprentice = 0;
	private int sponsor = 0;
	private long clanJoinExpiryTime;
	private long clanCreateExpiryTime;
	private int powerGrade = 0;
	private int clanPrivileges = 0;
	
	/** L2PcInstance's pledge class (knight, Baron, etc.) */
	private int pledgeClass = 0;
	
	private int pledgeType = 0;
	
	/** Level at which the player joined the clan as an academy member. */
	private int lvlJoinedAcademy = 0;
	
	private int wantsPeace = 0;
	private int deathPenaltyBuffLevel = 0;
	private AccessLevel playerAccessLevel;
	private boolean messageRefusal = false; // message refusal mode
	private boolean dietMode = false; // ignore weight penalty
	private boolean exchangeRefusal = false; // Exchange refusal
	private L2Party party;
	private long lastAttackPacket = 0;
	
	// this is needed to find the inviting player for Party response
	// there can only be one active party request at once
	/** The active requester. */
	private L2PcInstance activeRequester;
	
	private long requestExpireTime = 0;
	private final L2Request request = new L2Request(this);
	/** The arrow item. */
	private L2ItemInstance arrowItem;
	
	/** Used for protection after teleport. */
	private long protectEndTime = 0;
	
	private long teleportProtectEndTime = 0;
	
	/** protects a char from agro mobs when getting up from fake death. */
	private long recentFakeDeathEndTime = 0;
	
	/** The fists L2Weapon of the L2PcInstance (used when no weapon is equiped). */
	private L2Weapon fistsWeaponItem;
	
	private final Map<Integer, String> characters = new HashMap<>();
	
	/** The current higher Expertise of the L2PcInstance (None=0, D=1, C=2, B=3, A=4, S=5). */
	private int expertiseIndex; // index in EXPERTISE_LEVELS
	
	private int expertisePenalty = 0;
	private boolean heavy_mastery = false;
	private boolean light_mastery = false;
	private boolean robe_mastery = false;
	private int masteryPenalty = 0;
	private L2ItemInstance activeEnchantItem = null;
	protected boolean inventoryDisable = false;
	protected Map<Integer, L2CubicInstance> cubics = new HashMap<>();
	
	/** Active shots. A FastSet variable would actually suffice but this was changed to fix threading stability... */
	protected Map<Integer, Integer> activeSoulShots = new ConcurrentHashMap<>();
	
	/** The soul shot lock. */
	public final ReentrantLock soulShotLock = new ReentrantLock();
	
	public Quest dialog = null;
	
	private final int loto[] = new int[5];
	private final int race[] = new int[2];
	private final BlockList blockList = new BlockList(this);
	private int team = 0;
	
	/** lvl of alliance with ketra orcs or varka silenos, used in quests and aggro checks [-5,-1] varka, 0 neutral, [1,5] ketra. */
	private int alliedVarkaKetra = 0;
	
	/** ********************************************************************* Adventurers' coupon (0-no 1-NG 2-D 3-NG & D) 0 = No coupon 1 = coupon for No Grade 2 = coupon for D Grade 3 = coupon for No & D Grade ********************************************************************. */
	private int hasCoupon = 0;
	
	private L2Fishing fishCombat;
	private boolean fishing = false;
	private int fishX = 0;
	private int fishY = 0;
	private int fishZ = 0;
	
	private ScheduledFuture<?> taskRentPet;
	private ScheduledFuture<?> taskWater;
	private final List<String> validBypass = new ArrayList<>();
	private final List<String> validBypass2 = new ArrayList<>();
	private final List<String> validLink = new ArrayList<>();
	private Forum forumMail;
	private Forum forumMemo;
	
	/** Current skill in use. */
	private SkillDat playerCurrentSkill;
	private SkillDat currentPetSkill;
	
	/** Skills queued because a skill is already in progress. */
	private SkillDat playerQueuedSkill;
	
	/* Flag to disable equipment/skills while wearing formal wear * */
	/** The is wearing formal wear. */
	private boolean isWearingFormalWear = false;
	
	/** The current skill world position. */
	private Point3D currentSkillWorldPosition;
	
	/** The cursed weapon equiped id. */
	private int cursedWeaponEquipedId = 0;
	
	private int reviveRequested = 0;
	private double revivePower = 0;
	private boolean revivePet = false;
	
	/** The cp update inc check. */
	private double cpUpdateIncCheck = .0;
	
	/** The cp update dec check. */
	private double cpUpdateDecCheck = .0;
	
	/** The cp update interval. */
	private double cpUpdateInterval = .0;
	
	/** The mp update inc check. */
	private double mpUpdateIncCheck = .0;
	
	/** The mp update dec check. */
	private double mpUpdateDecCheck = .0;
	
	/** The mp update interval. */
	private double mpUpdateInterval = .0;
	
	private long timerToAttack;
	
	private boolean isInOfflineMode = false;
	private boolean isTradeOff = false;
	private long offlineShopStart = 0;
	public int originalNameColorOffline = 0xFFFFFF;
	private int herbsTaskTime = 0;
	
	// L2JMOD Wedding
	private boolean married = false;
	private int marriedType = 0;
	private int partnerId = 0;
	private int coupleId = 0;
	private boolean engageRequest = false;
	private int engageId = 0;
	private boolean marryRequest = false;
	private boolean marryAccepted = false;
	
	private int quakeSystem = 0;
	private boolean isLocked = false;
	private boolean isStored = false;
	
	// Data from character_variables table
	private Map<String, String> variables = new ConcurrentHashMap<>();
	
	// Data from account_variables table
	private Map<String, String> accountVariables = new ConcurrentHashMap<>();
	
	public class AIAccessor extends L2Character.AIAccessor
	{
		public L2PcInstance getPlayer()
		{
			return L2PcInstance.this;
		}
		
		public void doPickupItem(final L2Object object)
		{
			L2PcInstance.this.doPickupItem(object);
		}
		
		public void doInteract(final L2Character target)
		{
			L2PcInstance.this.doInteract(target);
		}
		
		@Override
		public void doAttack(final L2Character target)
		{
			if (isInsidePeaceZone(L2PcInstance.this, target))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// during teleport phase, players cant do any attack
			if (TvT.isTeleport() && inEventTvT || CTF.isTeleport() && inEventCTF || DM.is_teleport() && inEventDM)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Pk protection config
			if (!isGM() && target instanceof L2PcInstance && ((L2PcInstance) target).getPvpFlag() == 0 && ((L2PcInstance) target).getKarma() == 0 && (getLevel() < Config.ALT_PLAYER_PROTECTION_LEVEL || target.getLevel() < Config.ALT_PLAYER_PROTECTION_LEVEL))
			{
				sendMessage("You can't hit a player that is lower level from you. Target's level: " + String.valueOf(Config.ALT_PLAYER_PROTECTION_LEVEL) + ".");
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			super.doAttack(target);
			
			// cancel the recent fake-death protection instantly if the player attacks or casts spells
			getPlayer().setRecentFakeDeath(false);
			
			synchronized (cubics)
			{
				for (final L2CubicInstance cubic : cubics.values())
				{
					if (cubic.getId() != L2CubicInstance.LIFE_CUBIC)
					{
						cubic.doAction();
					}
				}
			}
		}
		
		@Override
		public void doCast(final L2Skill skill)
		{
			// cancel the recent fake-death protection instantly if the player attacks or casts spells
			getPlayer().setRecentFakeDeath(false);
			if (skill == null)
			{
				return;
			}
			
			// Like L2OFF you can use cupid bow skills on peace zone
			// Like L2OFF players can use TARGET_AURA skills on peace zone, all targets will be ignored.
			if (skill.isOffensive() && isInsidePeaceZone(L2PcInstance.this, getTarget()) && skill.getTargetType() != SkillTargetType.TARGET_AURA && skill.getId() != 3261 && skill.getId() != 3260 && skill.getId() != 3262) // check limited to active target
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
				
			}
			
			// during teleport phase, players cant do any attack
			if (TvT.isTeleport() && inEventTvT || CTF.isTeleport() && inEventCTF || DM.is_teleport() && inEventDM)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			super.doCast(skill);
			
			if (!skill.isOffensive())
			{
				return;
			}
			
			switch (skill.getTargetType())
			{
				case TARGET_GROUND:
					return;
				default:
				{
					L2Object mainTarget = skill.getFirstOfTargetList(L2PcInstance.this);
					if (mainTarget == null || !(mainTarget instanceof L2Character))
					{
						return;
					}
					
					synchronized (cubics)
					{
						for (final L2CubicInstance cubic : cubics.values())
						{
							if (cubic != null && cubic.getId() != L2CubicInstance.LIFE_CUBIC)
							{
								cubic.doAction();
							}
						}
					}
					
					mainTarget = null;
				}
					break;
			}
		}
	}
	
	protected static class SummonRequest
	{
		private L2PcInstance target = null;
		private L2Skill skill = null;
		
		/**
		 * Sets the target.
		 * @param destination the destination
		 * @param skill       the skill
		 */
		public void setTarget(final L2PcInstance destination, final L2Skill skill)
		{
			target = destination;
			this.skill = skill;
		}
		
		public L2PcInstance getTarget()
		{
			return target;
		}
		
		public L2Skill getSkill()
		{
			return skill;
		}
	}
	
	public class HerbTask implements Runnable
	{
		private final String process;
		private final int itemId;
		private final int herbCount;
		private final L2Object reference;
		private final boolean sendMessage;
		
		/**
		 * Instantiates a new herb task.
		 * @param process     the process
		 * @param itemId      the item id
		 * @param count       the count
		 * @param reference   the reference
		 * @param sendMessage the send message
		 */
		HerbTask(final String process, final int itemId, final int count, final L2Object reference, final boolean sendMessage)
		{
			this.process = process;
			this.itemId = itemId;
			herbCount = count;
			this.reference = reference;
			this.sendMessage = sendMessage;
		}
		
		@Override
		public void run()
		{
			try
			{
				addItem(process, itemId, herbCount, reference, sendMessage);
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					t.printStackTrace();
				}
				
				LOGGER.warn("", t);
			}
		}
	}
	
	/**
	 * Skill casting information (used to queue when several skills are cast in a short time) *.
	 */
	public class SkillDat
	{
		private final L2Skill skill;
		private final boolean ctrlPressed;
		private final boolean shiftPressed;
		
		/**
		 * Instantiates a new skill dat.
		 * @param skill        the skill
		 * @param ctrlPressed  the ctrl pressed
		 * @param shiftPressed the shift pressed
		 */
		protected SkillDat(final L2Skill skill, final boolean ctrlPressed, final boolean shiftPressed)
		{
			this.skill = skill;
			this.ctrlPressed = ctrlPressed;
			this.shiftPressed = shiftPressed;
		}
		
		public boolean isCtrlPressed()
		{
			return ctrlPressed;
		}
		
		public boolean isShiftPressed()
		{
			return shiftPressed;
		}
		
		public L2Skill getSkill()
		{
			return skill;
		}
		
		/**
		 * @return -1 if the skill is null
		 */
		public int getSkillId()
		{
			return getSkill() != null ? getSkill().getId() : -1;
		}
	}
	
	public boolean isSpawnProtected()
	{
		return protectEndTime > GameTimeController.getGameTicks();
	}
	
	public boolean isTeleportProtected()
	{
		return teleportProtectEndTime > GameTimeController.getGameTicks();
	}
	
	public PlayerStatus getActualStatus()
	{
		saved_status = new PlayerStatus(this);
		return saved_status;
	}
	
	public PlayerStatus getLastSavedStatus()
	{
		return saved_status;
	}
	
	/**
	 * Create a new L2PcInstance and add it in the characters table of the database.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Create a new L2PcInstance with an account name</li>
	 * <li>Set the name, the Hair Style, the Hair Color and the Face type of the L2PcInstance</li>
	 * <li>Add the player in the characters table of the database</li><BR>
	 * <BR>
	 * @param  objectId    Identifier of the object to initialized
	 * @param  template    The L2PcTemplate to apply to the L2PcInstance
	 * @param  accountName The name of the L2PcInstance
	 * @param  name        The name of the L2PcInstance
	 * @param  hairStyle   The hair style Identifier of the L2PcInstance
	 * @param  hairColor   The hair color Identifier of the L2PcInstance
	 * @param  face        The face type Identifier of the L2PcInstance
	 * @param  sex         the sex
	 * @return             The L2PcInstance added to the database or null
	 */
	public static L2PcInstance create(final int objectId, final L2PcTemplate template, final String accountName, final String name, final byte hairStyle, final byte hairColor, final byte face, final boolean sex)
	{
		ObjectData.addPlayer(objectId, name, accountName);
		// Create a new L2PcInstance with an account name
		PcAppearance app = new PcAppearance(face, hairColor, hairStyle, sex);
		final L2PcInstance player = new L2PcInstance(objectId, template, accountName, app);
		app = null;
		
		// Set the name of the L2PcInstance
		player.setName(name);
		
		// Set the base class ID to that of the actual class ID.
		player.setBaseClass(player.getClassId());
		
		if (Config.ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE)
		{
			player.setNewbie(true);
		}
		
		// Add the player in the characters table of the database
		final boolean ok = player.createDb();
		
		if (!ok)
		{
			return null;
		}
		
		return player;
	}
	
	/**
	 * Creates the dummy player.
	 * @param  objectId the object id
	 * @param  name     the name
	 * @return          the l2 pc instance
	 */
	public static L2PcInstance createDummyPlayer(final int objectId, final String name)
	{
		// Create a new L2PcInstance with an account name
		final L2PcInstance player = new L2PcInstance(objectId);
		player.setName(name);
		
		return player;
	}
	
	/**
	 * Gets the account name.
	 * @return the account name
	 */
	public String getAccountName()
	{
		if (getClient() != null)
		{
			return getClient().getAccountName();
		}
		return accountName;
	}
	
	/**
	 * Gets the account chars.
	 * @return the account chars
	 */
	public Map<Integer, String> getAccountChars()
	{
		return characters;
	}
	
	/**
	 * Gets the relation.
	 * @param  target the target
	 * @return        the relation
	 */
	public int getRelation(final L2PcInstance target)
	{
		int result = 0;
		
		// karma and pvp may not be required
		if (getPvpFlag() != 0)
		{
			result |= RelationChanged.RELATION_PVP_FLAG;
		}
		if (getKarma() > 0)
		{
			result |= RelationChanged.RELATION_HAS_KARMA;
		}
		
		if (isClanLeader())
		{
			result |= RelationChanged.RELATION_LEADER;
		}
		
		if (getSiegeState() != 0)
		{
			result |= RelationChanged.RELATION_INSIEGE;
			if (getSiegeState() != target.getSiegeState())
			{
				result |= RelationChanged.RELATION_ENEMY;
			}
			else
			{
				result |= RelationChanged.RELATION_ALLY;
			}
			if (getSiegeState() == 1)
			{
				result |= RelationChanged.RELATION_ATTACKER;
			}
		}
		
		if (getClan() != null && target.getClan() != null)
		{
			if (target.getPledgeType() != L2Clan.SUBUNIT_ACADEMY && getPledgeType() != L2Clan.SUBUNIT_ACADEMY && target.getClan().isAtWarWith(getClan().getClanId()))
			{
				result |= RelationChanged.RELATION_1SIDED_WAR;
				if (getClan().isAtWarWith(target.getClan().getClanId()))
				{
					result |= RelationChanged.RELATION_MUTUAL_WAR;
				}
			}
		}
		return result;
	}
	
	/**
	 * Retrieve a L2PcInstance from the characters table of the database and add it in allObjects of the L2world (call restore method).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Retrieve the L2PcInstance from the characters table of the database</li>
	 * <li>Add the L2PcInstance object in allObjects</li>
	 * <li>Set the x,y,z position of the L2PcInstance and make it invisible</li>
	 * <li>Update the overloaded status of the L2PcInstance</li><BR>
	 * <BR>
	 * @param  objectId Identifier of the object to initialized
	 * @return          The L2PcInstance loaded from the database
	 */
	public static L2PcInstance load(final int objectId)
	{
		return restore(objectId);
	}
	
	/**
	 * Inits the pc status update values.
	 */
	private void initPcStatusUpdateValues()
	{
		cpUpdateInterval = getMaxCp() / 352.0;
		cpUpdateIncCheck = getMaxCp();
		cpUpdateDecCheck = getMaxCp() - cpUpdateInterval;
		mpUpdateInterval = getMaxMp() / 352.0;
		mpUpdateIncCheck = getMaxMp();
		mpUpdateDecCheck = getMaxMp() - mpUpdateInterval;
	}
	
	/**
	 * Constructor of L2PcInstance (use L2Character constructor).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the L2Character constructor to create an empty skills slot and copy basic Calculator set to this L2PcInstance</li>
	 * <li>Set the name of the L2PcInstance</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method SET the level of the L2PcInstance to 1</B></FONT><BR>
	 * <BR>
	 * @param objectId    Identifier of the object to initialized
	 * @param template    The L2PcTemplate to apply to the L2PcInstance
	 * @param accountName The name of the account including this L2PcInstance
	 * @param app         the app
	 */
	private L2PcInstance(final int objectId, final L2PcTemplate template, final String accountName, final PcAppearance app)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		getStat(); // init stats
		getStatus(); // init status
		
		ObjectData.get(PlayerHolder.class, this).setInstance(this);
		
		super.initCharStatusUpdateValues();
		initPcStatusUpdateValues();
		
		this.accountName = accountName;
		appearance = app;
		
		// Create an AI
		aiCharacter = new L2PlayerAI(new L2PcInstance.AIAccessor());
		
		// Create a L2Radar object
		radar = new L2Radar(this);
		
		// Retrieve from the database all skills of this L2PcInstance and add them to skills
		// Retrieve from the database all items of this L2PcInstance and add them to inventory
		getInventory().restore();
		if (!Config.WAREHOUSE_CACHE)
		{
			getWarehouse();
		}
		getFreight().restore();
		
		instanceLoginTime = System.currentTimeMillis();
	}
	
	/**
	 * Instantiates a new l2 pc instance.
	 * @param objectId the object id
	 */
	private L2PcInstance(final int objectId)
	{
		super(objectId, null);
		getKnownList(); // init knownlist
		getStat(); // init stats
		getStatus(); // init status
		super.initCharStatusUpdateValues();
		initPcStatusUpdateValues();
		
		instanceLoginTime = System.currentTimeMillis();
	}
	
	@Override
	public final PcKnownList getKnownList()
	{
		if (super.getKnownList() == null || !(super.getKnownList() instanceof PcKnownList))
		{
			setKnownList(new PcKnownList(this));
		}
		return (PcKnownList) super.getKnownList();
	}
	
	@Override
	public final PcStat getStat()
	{
		if (super.getStat() == null || !(super.getStat() instanceof PcStat))
		{
			setStat(new PcStat(this));
		}
		return (PcStat) super.getStat();
	}
	
	@Override
	public final PcStatus getStatus()
	{
		if (super.getStatus() == null || !(super.getStatus() instanceof PcStatus))
		{
			setStatus(new PcStatus(this));
		}
		return (PcStatus) super.getStatus();
	}
	
	/**
	 * Gets the appearance.
	 * @return the appearance
	 */
	public final PcAppearance getAppearance()
	{
		return appearance;
	}
	
	/**
	 * Return the base L2PcTemplate link to the L2PcInstance.<BR>
	 * <BR>
	 * @return the base template
	 */
	public final L2PcTemplate getBaseTemplate()
	{
		return CharTemplateTable.getInstance().getTemplate(baseClass);
	}
	
	/**
	 * Return the L2PcTemplate link to the L2PcInstance.
	 * @return the template
	 */
	@Override
	public final L2PcTemplate getTemplate()
	{
		return (L2PcTemplate) super.getTemplate();
	}
	
	/**
	 * Sets the template.
	 * @param newclass the new template
	 */
	public void setTemplate(final ClassId newclass)
	{
		super.setTemplate(CharTemplateTable.getInstance().getTemplate(newclass));
	}
	
	public void setTimerToAttack(final long time)
	{
		timerToAttack = time;
	}
	
	public long getTimerToAttack()
	{
		return timerToAttack;
	}
	
	/**
	 * Return the AI of the L2PcInstance (create it if necessary).<BR>
	 * <BR>
	 * @return the aI
	 */
	@Override
	public L2CharacterAI getAI()
	{
		if (aiCharacter == null)
		{
			synchronized (this)
			{
				if (aiCharacter == null)
				{
					aiCharacter = new L2PlayerAI(new L2PcInstance.AIAccessor());
				}
			}
		}
		
		return aiCharacter;
	}
	
	/**
	 * Calculate a destination to explore the area and set the AI Intension to AI_INTENTION_MOVE_TO.<BR>
	 * <BR>
	 * @return the level
	 */
	/*
	 * TODO public void explore() { if(!_exploring) return; if(getMountType() == 2) return; // Calculate the destination point (random) int x = getX() + Rnd.nextInt(6000) - 3000; int y = getY() + Rnd.nextInt(6000) - 3000; if(x > Universe.MAX_X) { x = Universe.MAX_X; } if(x < Universe.MIN_X) { x =
	 * Universe.MIN_X; } if(y > Universe.MAX_Y) { y = Universe.MAX_Y; } if(y < Universe.MIN_Y) { y = Universe.MIN_Y; } int z = getZ(); L2CharPosition pos = new L2CharPosition(x, y, z, 0); // Set the AI Intention to AI_INTENTION_MOVE_TO getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos); pos =
	 * null; }
	 */
	
	/** Return the Level of the L2PcInstance. */
	@Override
	public final int getLevel()
	{
		int level = getStat().getLevel();
		
		if (level == -1)
		{
			
			final L2PcInstance local_char = restore(getObjectId());
			
			if (local_char != null)
			{
				level = local_char.getLevel();
			}
			
		}
		
		if (level < 0)
		{
			level = 1;
		}
		
		return level;
	}
	
	/**
	 * Return the newbie state of the L2PcInstance.<BR>
	 * <BR>
	 * @return true, if is newbie
	 */
	public boolean isNewbie()
	{
		return newbie;
	}
	
	/**
	 * Set the newbie state of the L2PcInstance.<BR>
	 * <BR>
	 * @param isNewbie The Identifier of the newbie state<BR>
	 *                     <BR>
	 */
	public void setNewbie(final boolean isNewbie)
	{
		newbie = isNewbie;
	}
	
	/**
	 * Sets the base class.
	 * @param baseClass the new base class
	 */
	public void setBaseClass(final int baseClass)
	{
		this.baseClass = baseClass;
	}
	
	/**
	 * Sets the base class.
	 * @param classId the new base class
	 */
	public void setBaseClass(final ClassId classId)
	{
		baseClass = classId.ordinal();
	}
	
	/**
	 * Checks if is in store mode.
	 * @return true, if is in store mode
	 */
	public boolean isInStoreMode()
	{
		return getPrivateStoreType() > 0;
	}
	
	// public boolean isInCraftMode() { return (getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE); }
	
	/**
	 * Checks if is in craft mode.
	 * @return true, if is in craft mode
	 */
	public boolean isInCraftMode()
	{
		return inCraftMode;
	}
	
	/**
	 * Checks if is in craft mode.
	 * @param b the b
	 */
	public void isInCraftMode(final boolean b)
	{
		inCraftMode = b;
	}
	
	/** The kicked. */
	private boolean kicked = false;
	
	/**
	 * Manage Logout Task.<BR>
	 * <BR>
	 * @param kicked the kicked
	 */
	public void logout(final boolean kicked)
	{
		// prevent from player disconnect when in Event
		if (atEvent)
		{
			sendMessage("A superior power doesn't allow you to leave the event.");
			sendPacket(ActionFailed.STATIC_PACKET);
		}
		
		this.kicked = kicked;
		
		closeNetConnection();
		
	}
	
	/**
	 * Checks if is kicked.
	 * @return true, if is kicked
	 */
	public boolean isKicked()
	{
		return kicked;
	}
	
	/**
	 * Sets the kicked.
	 * @param value the new kicked
	 */
	public void setKicked(final boolean value)
	{
		kicked = value;
	}
	
	/**
	 * Manage Logout Task.<BR>
	 * <BR>
	 */
	public void logout()
	{
		logout(false);
	}
	
	/**
	 * Return a table containing all Common L2RecipeList of the L2PcInstance.<BR>
	 * <BR>
	 * @return the common recipe book
	 */
	public L2RecipeList[] getCommonRecipeBook()
	{
		return commonRecipeBook.values().toArray(new L2RecipeList[commonRecipeBook.values().size()]);
	}
	
	/**
	 * Return a table containing all Dwarf L2RecipeList of the L2PcInstance.<BR>
	 * <BR>
	 * @return the dwarven recipe book
	 */
	public L2RecipeList[] getDwarvenRecipeBook()
	{
		return dwarvenRecipeBook.values().toArray(new L2RecipeList[dwarvenRecipeBook.values().size()]);
	}
	
	/**
	 * Add a new L2RecipList to the table commonrecipebook containing all L2RecipeList of the L2PcInstance <BR>
	 * <BR>
	 * .
	 * @param recipe The L2RecipeList to add to the recipebook
	 */
	public void registerCommonRecipeList(final L2RecipeList recipe)
	{
		commonRecipeBook.put(recipe.getId(), recipe);
	}
	
	/**
	 * Add a new L2RecipList to the table recipebook containing all L2RecipeList of the L2PcInstance <BR>
	 * <BR>
	 * .
	 * @param recipe The L2RecipeList to add to the recipebook
	 */
	public void registerDwarvenRecipeList(final L2RecipeList recipe)
	{
		dwarvenRecipeBook.put(recipe.getId(), recipe);
	}
	
	/**
	 * Checks for recipe list.
	 * @param  recipeId the recipe id
	 * @return          <b>TRUE</b> if player has the recipe on Common or Dwarven Recipe book else returns <b>FALSE</b>
	 */
	public boolean hasRecipeList(final int recipeId)
	{
		if (dwarvenRecipeBook.containsKey(recipeId))
		{
			return true;
		}
		else if (commonRecipeBook.containsKey(recipeId))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Tries to remove a L2RecipList from the table dwarvenRecipeBook or from table commonRecipeBook, those table contain all L2RecipeList of the L2PcInstance <BR>
	 * <BR>
	 * .
	 * @param recipeId the recipe id
	 */
	public void unregisterRecipeList(int recipeId)
	{
		if (dwarvenRecipeBook.containsKey(recipeId))
		{
			dwarvenRecipeBook.remove(recipeId);
		}
		else if (commonRecipeBook.containsKey(recipeId))
		{
			commonRecipeBook.remove(recipeId);
		}
		else
		{
			LOGGER.warn("Attempted to remove unknown RecipeList: " + recipeId);
		}
		
		for (L2ShortCut sc : getAllShortCuts())
		{
			if (sc != null && sc.getId() == recipeId && sc.getType() == L2ShortCut.TYPE_RECIPE)
			{
				deleteShortCut(sc.getSlot(), sc.getPage());
			}
		}
		
	}
	
	/**
	 * Returns the Id for the last talked quest NPC.<BR>
	 * <BR>
	 * @return the last quest npc object
	 */
	public int getLastQuestNpcObject()
	{
		return questNpcObject;
	}
	
	/**
	 * Sets the last quest npc object.
	 * @param npcId the new last quest npc object
	 */
	public void setLastQuestNpcObject(final int npcId)
	{
		questNpcObject = npcId;
	}
	
	/**
	 * Return the QuestState object corresponding to the quest name.<BR>
	 * <BR>
	 * @param  quest The name of the quest
	 * @return       the quest state
	 */
	public QuestState getQuestState(final String quest)
	{
		return playerQuests.get(quest);
	}
	
	/**
	 * Add a QuestState to the table quest containing all quests began by the L2PcInstance.<BR>
	 * <BR>
	 * @param qs The QuestState to add to quest
	 */
	public void setQuestState(final QuestState qs)
	{
		playerQuests.put(qs.getQuestName(), qs);
	}
	
	/**
	 * Remove a QuestState from the table quest containing all quests began by the L2PcInstance.<BR>
	 * <BR>
	 * @param quest The name of the quest
	 */
	public void delQuestState(final String quest)
	{
		playerQuests.remove(quest);
	}
	
	/**
	 * Adds the to quest state array.
	 * @param  questStateArray the quest state array
	 * @param  state           the state
	 * @return                 the quest state[]
	 */
	private QuestState[] addToQuestStateArray(final QuestState[] questStateArray, final QuestState state)
	{
		final int len = questStateArray.length;
		final QuestState[] tmp = new QuestState[len + 1];
		for (int i = 0; i < len; i++)
		{
			tmp[i] = questStateArray[i];
		}
		tmp[len] = state;
		return tmp;
	}
	
	/**
	 * Return a table containing all Quest in progress from the table quests.<BR>
	 * <BR>
	 * @return the all active quests
	 */
	public Quest[] getAllActiveQuests()
	{
		List<Quest> quests = new ArrayList<>();
		
		for (QuestState qs : playerQuests.values())
		{
			if (qs != null)
			{
				if (qs.getQuest().getQuestIntId() >= 1999)
				{
					continue;
				}
				
				if (qs.isCompleted() && !Config.DEVELOPER)
				{
					continue;
				}
				
				if (!qs.isStarted() && !Config.DEVELOPER)
				{
					continue;
				}
				
				quests.add(qs.getQuest());
			}
		}
		
		return quests.toArray(new Quest[quests.size()]);
	}
	
	/**
	 * Return a table containing all QuestState to modify after a L2Attackable killing.<BR>
	 * <BR>
	 * @param  npc the npc
	 * @return     the quests for attacks
	 */
	public QuestState[] getQuestsForAttacks(final L2NpcInstance npc)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;
		
		// Go through the QuestState of the L2PcInstance quests
		for (final Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK))
		{
			// Check if the Identifier of the L2Attackable attck is needed for the current quest
			if (getQuestState(quest.getName()) != null)
			{
				// Copy the current L2PcInstance QuestState in the QuestState table
				if (states == null)
				{
					states = new QuestState[]
					{
						getQuestState(quest.getName())
					};
				}
				else
				{
					states = addToQuestStateArray(states, getQuestState(quest.getName()));
				}
			}
		}
		
		// Return a table containing all QuestState to modify
		return states;
	}
	
	/**
	 * Return a table containing all QuestState to modify after a L2Attackable killing.<BR>
	 * <BR>
	 * @param  npc the npc
	 * @return     the quests for kills
	 */
	public QuestState[] getQuestsForKills(final L2NpcInstance npc)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;
		
		// Go through the QuestState of the L2PcInstance quests
		for (final Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL))
		{
			// Check if the Identifier of the L2Attackable killed is needed for the current quest
			if (getQuestState(quest.getName()) != null)
			{
				// Copy the current L2PcInstance QuestState in the QuestState table
				if (states == null)
				{
					states = new QuestState[]
					{
						getQuestState(quest.getName())
					};
				}
				else
				{
					states = addToQuestStateArray(states, getQuestState(quest.getName()));
				}
			}
		}
		
		// Return a table containing all QuestState to modify
		return states;
	}
	
	/**
	 * Return a table containing all QuestState from the table quests in which the L2PcInstance must talk to the NPC.<BR>
	 * <BR>
	 * @param  npcId The Identifier of the NPC
	 * @return       the quests for talk
	 */
	public QuestState[] getQuestsForTalk(final int npcId)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;
		
		// Go through the QuestState of the L2PcInstance quests
		for (final Quest quest : NpcTable.getInstance().getTemplate(npcId).getEventQuests(Quest.QuestEventType.QUEST_TALK))
		{
			if (quest != null)
			{
				// Copy the current L2PcInstance QuestState in the QuestState table
				if (getQuestState(quest.getName()) != null)
				{
					if (states == null)
					{
						states = new QuestState[]
						{
							getQuestState(quest.getName())
						};
					}
					else
					{
						states = addToQuestStateArray(states, getQuestState(quest.getName()));
					}
				}
			}
		}
		
		// Return a table containing all QuestState to modify
		return states;
	}
	
	/**
	 * Process quest event.
	 * @param  quest the quest
	 * @param  event the event
	 * @return       the quest state
	 */
	public QuestState processQuestEvent(final String quest, String event)
	{
		QuestState retval = null;
		if (event == null)
		{
			event = "";
		}
		
		if (!playerQuests.containsKey(quest))
		{
			return retval;
		}
		
		QuestState qs = getQuestState(quest);
		if (qs == null && event.length() == 0)
		{
			return retval;
		}
		
		if (qs == null)
		{
			Quest q = null;
			if (!Config.ALT_DEV_NO_QUESTS)
			{
				q = QuestManager.getInstance().getQuest(quest);
			}
			
			if (q == null)
			{
				return retval;
			}
			qs = q.newQuestState(this);
		}
		if (qs != null)
		{
			if (getLastQuestNpcObject() > 0)
			{
				final L2Object object = L2World.getInstance().findObject(getLastQuestNpcObject());
				if (object instanceof L2NpcInstance && isInsideRadius(object, L2NpcInstance.INTERACTION_DISTANCE, false, false))
				{
					final L2NpcInstance npc = (L2NpcInstance) object;
					final QuestState[] states = getQuestsForTalk(npc.getNpcId());
					
					if (states != null)
					{
						for (final QuestState state : states)
						{
							if (state.getQuest().getQuestIntId() == qs.getQuest().getQuestIntId() && !qs.isCompleted())
							{
								if (qs.getQuest().notifyEvent(event, npc, this))
								{
									showQuestWindow(quest, qs.getStateId());
								}
								
								retval = qs;
							}
						}
						sendPacket(new QuestList());
					}
				}
			}
			qs = null;
		}
		
		return retval;
	}
	
	/**
	 * Show quest window.
	 * @param questId the quest id
	 * @param stateId the state id
	 */
	private void showQuestWindow(final String questId, final String stateId)
	{
		String path = "data/scripts/quests/" + questId + "/" + stateId + ".htm";
		String content = HtmCache.getInstance().getHtm(path);
		
		if (content != null)
		{
			if (Config.DEBUG)
			{
				LOGGER.debug("Showing quest window for quest " + questId + " state " + stateId + " html path: " + path);
			}
			
			NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(content);
			sendPacket(npcReply);
			content = null;
			npcReply = null;
		}
		
		sendPacket(ActionFailed.STATIC_PACKET);
		path = null;
	}
	
	/**
	 * Return a table containing all L2ShortCut of the L2PcInstance.<BR>
	 * <BR>
	 * @return the all short cuts
	 */
	public List<L2ShortCut> getAllShortCuts()
	{
		return shortCuts.getAllShortCuts();
	}
	
	/**
	 * Return the L2ShortCut of the L2PcInstance corresponding to the position (page-slot).<BR>
	 * <BR>
	 * @param  slot The slot in wich the shortCuts is equiped
	 * @param  page The page of shortCuts containing the slot
	 * @return      the short cut
	 */
	public L2ShortCut getShortCut(final int slot, final int page)
	{
		return shortCuts.getShortCut(slot, page);
	}
	
	/**
	 * Add a L2shortCut to the L2PcInstance shortCuts<BR>
	 * <BR>
	 * .
	 * @param shortcut the shortcut
	 */
	public void registerShortCut(final L2ShortCut shortcut)
	{
		shortCuts.registerShortCut(shortcut);
	}
	
	/**
	 * Delete the L2ShortCut corresponding to the position (page-slot) from the L2PcInstance shortCuts.<BR>
	 * <BR>
	 * @param slot the slot
	 * @param page the page
	 */
	public void deleteShortCut(final int slot, final int page)
	{
		shortCuts.deleteShortCut(slot, page);
	}
	
	/**
	 * Add a L2Macro to the L2PcInstance macroses<BR>
	 * <BR>
	 * .
	 * @param macro the macro
	 */
	public void registerMacro(final L2Macro macro)
	{
		macroses.registerMacro(macro);
	}
	
	/**
	 * Delete the L2Macro corresponding to the Identifier from the L2PcInstance macroses.<BR>
	 * <BR>
	 * @param id the id
	 */
	public void deleteMacro(final int id)
	{
		macroses.deleteMacro(id);
	}
	
	/**
	 * Return all L2Macro of the L2PcInstance.<BR>
	 * <BR>
	 * @return the macroses
	 */
	public MacroList getMacroses()
	{
		return macroses;
	}
	
	/**
	 * Set the siege state of the L2PcInstance.<BR>
	 * <BR>
	 * 1 = attacker, 2 = defender, 0 = not involved
	 * @param siegeState the new siege state
	 */
	public void setSiegeState(final byte siegeState)
	{
		this.siegeState = siegeState;
	}
	
	/**
	 * Get the siege state of the L2PcInstance.<BR>
	 * <BR>
	 * 1 = attacker, 2 = defender, 0 = not involved
	 * @return the siege state
	 */
	public byte getSiegeState()
	{
		return siegeState;
	}
	
	/**
	 * Set the PvP Flag of the L2PcInstance.<BR>
	 * <BR>
	 * @param pvpFlag the new pvp flag
	 */
	public void setPvpFlag(final int pvpFlag)
	{
		this.pvpFlag = (byte) pvpFlag;
	}
	
	/**
	 * Gets the pvp flag.
	 * @return the pvp flag
	 */
	public byte getPvpFlag()
	{
		return pvpFlag;
	}
	
	@Override
	public void updatePvPFlag(final int value)
	{
		if (getPvpFlag() == value)
		{
			return;
		}
		setPvpFlag(value);
		
		sendPacket(new UserInfo(this));
		
		// If this player has a pet update the pets pvp flag as well
		if (getPet() != null)
		{
			sendPacket(new RelationChanged(getPet(), getRelation(this), false));
		}
		
		for (final L2PcInstance target : getKnownList().getKnownPlayers().values())
		{
			if (target == null)
			{
				continue;
			}
			
			target.sendPacket(new RelationChanged(this, getRelation(this), isAutoAttackable(target)));
			if (getPet() != null)
			{
				target.sendPacket(new RelationChanged(getPet(), getRelation(this), isAutoAttackable(target)));
			}
		}
	}
	
	@Override
	public void revalidateZone(final boolean force)
	{
		// Cannot validate if not in a world region (happens during teleport)
		if (getWorldRegion() == null)
		{
			return;
		}
		
		if (Config.ALLOW_WATER)
		{
			checkWaterState();
		}
		
		// This function is called very often from movement code
		if (force)
		{
			zoneValidateCounter = 4;
		}
		else
		{
			zoneValidateCounter--;
			if (zoneValidateCounter < 0)
			{
				zoneValidateCounter = 4;
			}
			else
			{
				return;
			}
		}
		
		getWorldRegion().revalidateZones(this);
		
		if (isInsideZone(ZONE_SIEGE))
		{
			if (lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
			{
				return;
			}
			lastCompassZone = ExSetCompassZoneCode.SIEGEWARZONE2;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.SIEGEWARZONE2);
			sendPacket(cz);
			cz = null;
		}
		else if (isInsideZone(ZONE_PVP))
		{
			if (lastCompassZone == ExSetCompassZoneCode.PVPZONE)
			{
				return;
			}
			lastCompassZone = ExSetCompassZoneCode.PVPZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.PVPZONE);
			sendPacket(cz);
			cz = null;
		}
		else if (isIn7sDungeon())
		{
			if (lastCompassZone == ExSetCompassZoneCode.SEVENSIGNSZONE)
			{
				return;
			}
			lastCompassZone = ExSetCompassZoneCode.SEVENSIGNSZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.SEVENSIGNSZONE);
			sendPacket(cz);
			cz = null;
		}
		else if (isInsideZone(ZONE_PEACE))
		{
			if (lastCompassZone == ExSetCompassZoneCode.PEACEZONE)
			{
				return;
			}
			lastCompassZone = ExSetCompassZoneCode.PEACEZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.PEACEZONE);
			sendPacket(cz);
			cz = null;
		}
		else
		{
			if (lastCompassZone == ExSetCompassZoneCode.GENERALZONE)
			{
				return;
			}
			if (lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
			{
				updatePvPStatus();
			}
			lastCompassZone = ExSetCompassZoneCode.GENERALZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.GENERALZONE);
			sendPacket(cz);
			cz = null;
		}
	}
	
	/**
	 * Return True if the L2PcInstance can Craft Dwarven Recipes.<BR>
	 * <BR>
	 * @return true, if successful
	 */
	public boolean hasDwarvenCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN) >= 1;
	}
	
	/**
	 * Gets the dwarven craft.
	 * @return the dwarven craft
	 */
	public int getDwarvenCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN);
	}
	
	/**
	 * Return True if the L2PcInstance can Craft Dwarven Recipes.<BR>
	 * <BR>
	 * @return true, if successful
	 */
	public boolean hasCommonCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON) >= 1;
	}
	
	/**
	 * Gets the common craft.
	 * @return the common craft
	 */
	public int getCommonCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON);
	}
	
	/**
	 * Return the PK counter of the L2PcInstance.<BR>
	 * <BR>
	 * @return the pk kills
	 */
	public int getPkKills()
	{
		return pkKills;
	}
	
	/**
	 * Set the PK counter of the L2PcInstance.<BR>
	 * <BR>
	 * @param pkKills the new pk kills
	 */
	public void setPkKills(final int pkKills)
	{
		this.pkKills = pkKills;
	}
	
	/**
	 * Return the deleteTimer of the L2PcInstance.<BR>
	 * <BR>
	 * @return the delete timer
	 */
	public long getDeleteTimer()
	{
		return deleteTimer;
	}
	
	/**
	 * Set the deleteTimer of the L2PcInstance.<BR>
	 * <BR>
	 * @param deleteTimer the new delete timer
	 */
	public void setDeleteTimer(final long deleteTimer)
	{
		this.deleteTimer = deleteTimer;
	}
	
	/**
	 * Return the current weight of the L2PcInstance.<BR>
	 * <BR>
	 * @return the current load
	 */
	public int getCurrentLoad()
	{
		return inventory.getTotalWeight();
	}
	
	/**
	 * Return date of las update of recomPoints.
	 * @return the last recom update
	 */
	public long getLastRecomUpdate()
	{
		return lastRecomUpdate;
	}
	
	/**
	 * Sets the last recom update.
	 * @param date the new last recom update
	 */
	public void setLastRecomUpdate(final long date)
	{
		lastRecomUpdate = date;
	}
	
	/**
	 * Return the number of recommandation obtained by the L2PcInstance.<BR>
	 * <BR>
	 * @return the recom have
	 */
	public int getRecomHave()
	{
		return recomHave;
	}
	
	/**
	 * Increment the number of recommandation obtained by the L2PcInstance (Max : 255).<BR>
	 * <BR>
	 */
	protected void incRecomHave()
	{
		if (recomHave < 255)
		{
			recomHave++;
		}
	}
	
	/**
	 * Set the number of recommandation obtained by the L2PcInstance (Max : 255).<BR>
	 * <BR>
	 * @param value the new recom have
	 */
	public void setRecomHave(final int value)
	{
		if (value > 255)
		{
			recomHave = 255;
		}
		else if (value < 0)
		{
			recomHave = 0;
		}
		else
		{
			recomHave = value;
		}
	}
	
	/**
	 * Return the number of recommandation that the L2PcInstance can give.<BR>
	 * <BR>
	 * @return the recom left
	 */
	public int getRecomLeft()
	{
		return recomLeft;
	}
	
	/**
	 * Increment the number of recommandation that the L2PcInstance can give.<BR>
	 * <BR>
	 */
	protected void decRecomLeft()
	{
		if (recomLeft > 0)
		{
			recomLeft--;
		}
	}
	
	/**
	 * Give recom.
	 * @param target the target
	 */
	public void giveRecom(final L2PcInstance target)
	{
		if (Config.ALT_RECOMMEND)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(ADD_CHAR_RECOM);)
			{
				statement.setInt(1, getObjectId());
				statement.setInt(2, target.getObjectId());
				statement.executeUpdate();
			}
			catch (final Exception e)
			{
				LOGGER.error("could not update char recommendations ", e);
			}
		}
		target.incRecomHave();
		decRecomLeft();
		recomChars.add(target.getObjectId());
	}
	
	/**
	 * Can recom.
	 * @param  target the target
	 * @return        true, if successful
	 */
	public boolean canRecom(final L2PcInstance target)
	{
		return !recomChars.contains(target.getObjectId());
	}
	
	/**
	 * Set the exp of the L2PcInstance before a death.
	 * @param exp the new exp before death
	 */
	public void setExpBeforeDeath(final long exp)
	{
		expBeforeDeath = exp;
	}
	
	/**
	 * Gets the exp before death.
	 * @return the exp before death
	 */
	public long getExpBeforeDeath()
	{
		return expBeforeDeath;
	}
	
	/**
	 * Return the Karma of the L2PcInstance.<BR>
	 * <BR>
	 * @return the karma
	 */
	public int getKarma()
	{
		return playerKarma;
	}
	
	/**
	 * Set the Karma of the L2PcInstance and send a Server->Client packet StatusUpdate (broadcast).<BR>
	 * <BR>
	 * @param karma the new karma
	 */
	public void setKarma(int karma)
	{
		if (karma < 0)
		{
			karma = 0;
		}
		
		if (playerKarma == 0 && karma > 0)
		{
			for (final L2Object object : getKnownList().getKnownObjects().values())
			{
				if (object == null || !(object instanceof L2GuardInstance))
				{
					continue;
				}
				
				if (((L2GuardInstance) object).getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				{
					((L2GuardInstance) object).getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
				}
			}
		}
		else if (playerKarma > 0 && karma == 0)
		{
			// Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the L2PcInstance and all L2PcInstance to inform (broadcast)
			setKarmaFlag(0);
		}
		
		playerKarma = karma;
		broadcastKarma();
	}
	
	/**
	 * Return the max weight that the L2PcInstance can load.<BR>
	 * <BR>
	 * @return the max load
	 */
	public int getMaxLoad()
	{
		
		// Weight Limit = (CON Modifier*69000)*Skills
		// Source http://l2p.bravehost.com/weightlimit.html (May 2007)
		
		final int con = getCON();
		
		if (con < 1)
		{
			return 31000;
		}
		
		if (con > 59)
		{
			return 176000;
		}
		
		final double baseLoad = Math.floor(BaseStats.CON.calcBonus(this) * 69000 * Config.ALT_WEIGHT_LIMIT);
		return (int) calcStat(Stats.MAX_LOAD, baseLoad, this, null);
	}
	
	/**
	 * Gets the expertise penalty.
	 * @return the expertise penalty
	 */
	public int getExpertisePenalty()
	{
		return expertisePenalty;
	}
	
	/**
	 * Gets the mastery penalty.
	 * @return the mastery penalty
	 */
	public int getMasteryPenalty()
	{
		return masteryPenalty;
	}
	
	/**
	 * Gets the mastery weap penalty.
	 * @return the mastery weap penalty
	 */
	public int getMasteryWeapPenalty()
	{
		return masteryWeapPenalty;
	}
	
	/**
	 * Gets the weight penalty.
	 * @return the weight penalty
	 */
	public int getWeightPenalty()
	{
		if (dietMode)
		{
			return 0;
		}
		return curWeightPenalty;
	}
	
	/**
	 * Update the overloaded status of the L2PcInstance.<BR>
	 * <BR>
	 */
	public void refreshOverloaded()
	{
		if (Config.DISABLE_WEIGHT_PENALTY)
		{
			setIsOverloaded(false);
		}
		else if (dietMode)
		{
			setIsOverloaded(false);
			curWeightPenalty = 0;
			super.removeSkill(getKnownSkill(4270));
			sendPacket(new EtcStatusUpdate(this));
			Broadcast.toKnownPlayers(this, new CharInfo(this));
		}
		else
		{
			final int maxLoad = getMaxLoad();
			if (maxLoad > 0)
			{
				// setIsOverloaded(getCurrentLoad() > maxLoad);
				// int weightproc = getCurrentLoad() * 1000 / maxLoad;
				final long weightproc = (long) ((getCurrentLoad() - calcStat(Stats.WEIGHT_PENALTY, 1, this, null)) * 1000 / maxLoad);
				int newWeightPenalty;
				
				if (weightproc < 500)
				{
					newWeightPenalty = 0;
				}
				else if (weightproc < 666)
				{
					newWeightPenalty = 1;
				}
				else if (weightproc < 800)
				{
					newWeightPenalty = 2;
				}
				else if (weightproc < 1000)
				{
					newWeightPenalty = 3;
				}
				else
				{
					newWeightPenalty = 4;
				}
				
				if (curWeightPenalty != newWeightPenalty)
				{
					curWeightPenalty = newWeightPenalty;
					if (newWeightPenalty > 0)
					{
						super.addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty));
						sendSkillList(); // Fix visual bug
					}
					else
					{
						super.removeSkill(getKnownSkill(4270));
						sendSkillList(); // Fix visual bug
					}
					
					sendPacket(new EtcStatusUpdate(this));
					Broadcast.toKnownPlayers(this, new CharInfo(this));
				}
			}
		}
		
		sendPacket(new UserInfo(this));
	}
	
	/**
	 * Refresh mastery penality.
	 */
	public void refreshMasteryPenality()
	{
		if (!Config.MASTERY_PENALTY || getLevel() <= Config.LEVEL_TO_GET_PENALITY)
		{
			return;
		}
		
		heavy_mastery = false;
		light_mastery = false;
		robe_mastery = false;
		
		final L2Skill[] char_skills = getAllSkills();
		
		for (final L2Skill actual_skill : char_skills)
		{
			if (actual_skill.getName().contains("Heavy Armor Mastery"))
			{
				heavy_mastery = true;
			}
			
			if (actual_skill.getName().contains("Light Armor Mastery"))
			{
				light_mastery = true;
			}
			
			if (actual_skill.getName().contains("Robe Mastery"))
			{
				robe_mastery = true;
			}
		}
		
		int newMasteryPenalty = 0;
		
		if (!heavy_mastery && !light_mastery && !robe_mastery)
		{
			// not completed 1st class transfer or not acquired yet the mastery skills
			newMasteryPenalty = 0;
		}
		else
		{
			for (final L2ItemInstance item : getInventory().getItems())
			{
				if (item != null && item.isEquipped() && item.getItem() instanceof L2Armor)
				{
					// No penality for formal wear
					if (item.getItemId() == 6408)
					{
						continue;
					}
					
					final L2Armor armor_item = (L2Armor) item.getItem();
					
					switch (armor_item.getItemType())
					{
						case HEAVY:
						{
							if (!heavy_mastery)
							{
								newMasteryPenalty++;
							}
						}
							break;
						case LIGHT:
						{
							if (!light_mastery)
							{
								newMasteryPenalty++;
							}
						}
							break;
						case MAGIC:
						{
							if (!robe_mastery)
							{
								newMasteryPenalty++;
							}
						}
							break;
					}
				}
			}
		}
		
		if (masteryPenalty != newMasteryPenalty)
		{
			int penalties = masteryWeapPenalty + expertisePenalty + newMasteryPenalty;
			
			if (penalties > 0)
			{
				addSkill(SkillTable.getInstance().getInfo(4267, 1), false); // level used to be newPenalty
			}
			else
			{
				removeSkill(getKnownSkill(4267));
			}
			
			sendPacket(new EtcStatusUpdate(this));
			masteryPenalty = newMasteryPenalty;
		}
	}
	
	/**
	 * Can interact.
	 * @param  player the player
	 * @return        true, if successful
	 */
	protected boolean canInteract(final L2PcInstance player)
	{
		if (!isInsideRadius(player, 50, false, false))
		{
			return false;
		}
		
		return true;
	}
	
	/** The blunt_mastery. */
	private boolean blunt_mastery = false;
	
	/** The pole_mastery. */
	private boolean pole_mastery = false;
	
	/** The dagger_mastery. */
	private boolean dagger_mastery = false;
	
	/** The sword_mastery. */
	private boolean sword_mastery = false;
	
	/** The bow_mastery. */
	private boolean bow_mastery = false;
	
	/** The fist_mastery. */
	private boolean fist_mastery = false;
	
	/** The dual_mastery. */
	private boolean dual_mastery = false;
	
	/** The two_hands_mastery. */
	private boolean two_hands_mastery = false;
	
	/** The mastery weap penalty. */
	private int masteryWeapPenalty = 0;
	
	/**
	 * Refresh mastery weap penality.
	 */
	public void refreshMasteryWeapPenality()
	{
		if (!Config.MASTERY_WEAPON_PENALTY || getLevel() <= Config.LEVEL_TO_GET_WEAPON_PENALITY)
		{
			return;
		}
		
		blunt_mastery = false;
		bow_mastery = false;
		dagger_mastery = false;
		fist_mastery = false;
		dual_mastery = false;
		pole_mastery = false;
		sword_mastery = false;
		two_hands_mastery = false;
		
		final L2Skill[] char_skills = getAllSkills();
		
		for (final L2Skill actual_skill : char_skills)
		{
			
			if (actual_skill.getName().contains("Sword Blunt Mastery"))
			{
				sword_mastery = true;
				blunt_mastery = true;
				continue;
			}
			
			if (actual_skill.getName().contains("Blunt Mastery"))
			{
				blunt_mastery = true;
				continue;
			}
			
			if (actual_skill.getName().contains("Bow Mastery"))
			{
				bow_mastery = true;
				continue;
			}
			
			if (actual_skill.getName().contains("Dagger Mastery"))
			{
				dagger_mastery = true;
				continue;
			}
			
			if (actual_skill.getName().contains("Fist Mastery"))
			{
				fist_mastery = true;
				continue;
			}
			
			if (actual_skill.getName().contains("Dual Weapon Mastery"))
			{
				dual_mastery = true;
				continue;
			}
			
			if (actual_skill.getName().contains("Polearm Mastery"))
			{
				pole_mastery = true;
				continue;
			}
			
			if (actual_skill.getName().contains("Two-handed Weapon Mastery"))
			{
				two_hands_mastery = true;
				continue;
			}
		}
		
		int newMasteryPenalty = 0;
		
		if (!bow_mastery && !blunt_mastery && !dagger_mastery && !fist_mastery && !dual_mastery && !pole_mastery && !sword_mastery && !two_hands_mastery)
		{ // not completed 1st class transfer or not acquired yet the mastery skills
			newMasteryPenalty = 0;
		}
		else
		{
			for (final L2ItemInstance item : getInventory().getItems())
			{
				if (item != null && item.isEquipped() && item.getItem() instanceof L2Weapon && !isCursedWeaponEquiped())
				{
					// No penality for cupid's bow
					if (item.isCupidBow())
					{
						continue;
					}
					
					final L2Weapon weap_item = (L2Weapon) item.getItem();
					
					switch (weap_item.getItemType())
					{
						
						case BIGBLUNT:
						case BIGSWORD:
						{
							if (!two_hands_mastery)
							{
								newMasteryPenalty++;
							}
						}
							break;
						case BLUNT:
						{
							if (!blunt_mastery)
							{
								newMasteryPenalty++;
							}
						}
							break;
						case BOW:
						{
							if (!bow_mastery)
							{
								newMasteryPenalty++;
							}
						}
							break;
						case DAGGER:
						{
							if (!dagger_mastery)
							{
								newMasteryPenalty++;
							}
						}
							break;
						case DUAL:
						{
							if (!dual_mastery)
							{
								newMasteryPenalty++;
							}
						}
							break;
						case DUALFIST:
						case FIST:
						{
							if (!fist_mastery)
							{
								newMasteryPenalty++;
							}
						}
							break;
						case POLE:
						{
							if (!pole_mastery)
							{
								newMasteryPenalty++;
							}
						}
							break;
						case SWORD:
						{
							if (!sword_mastery)
							{
								newMasteryPenalty++;
							}
						}
							break;
						
					}
				}
			}
			
		}
		
		if (masteryWeapPenalty != newMasteryPenalty)
		{
			int penalties = masteryPenalty + expertisePenalty + newMasteryPenalty;
			
			if (penalties > 0)
			{
				addSkill(SkillTable.getInstance().getInfo(4267, 1), false); // level used to be newPenalty
			}
			else
			{
				removeSkill(getKnownSkill(4267));
			}
			
			sendPacket(new EtcStatusUpdate(this));
			masteryWeapPenalty = newMasteryPenalty;
		}
	}
	
	/**
	 * Refresh expertise penalty.
	 */
	public void refreshExpertisePenalty()
	{
		if (!Config.EXPERTISE_PENALTY)
		{
			return;
		}
		
		// This code works on principle that first 1-5 levels of penalty is for weapon and 6-10levels are for armor
		int intensityW = 0; // Default value
		int intensityA = 5; // Default value.
		int intensity = 0; // Level of grade penalty.
		
		for (final L2ItemInstance item : getInventory().getItems())
		{
			if (item != null && item.isEquipped()) // Checks if items equipped
			{
				
				final int crystaltype = item.getItem().getCrystalType(); // Gets grade of item
				// Checks if item crystal levels is above character levels and also if last penalty for weapon was lower.
				if (crystaltype > getExpertiseIndex() && item.isWeapon() && crystaltype > intensityW)
				{
					intensityW = crystaltype - getExpertiseIndex();
				}
				// Checks if equiped armor, accesories are above character level and adds each armor penalty.
				if (crystaltype > getExpertiseIndex() && !item.isWeapon())
				{
					intensityA += crystaltype - getExpertiseIndex();
				}
			}
		}
		
		if (intensityA == 5)// Means that there isn't armor penalty.
		{
			intensity = intensityW;
		}
		
		else
		{
			intensity = intensityW + intensityA;
		}
		
		// Checks if penalty is above maximum and sets it to maximum.
		if (intensity > 10)
		{
			intensity = 10;
		}
		
		if (getExpertisePenalty() != intensity)
		{
			int penalties = masteryPenalty + masteryWeapPenalty + intensity;
			if (penalties > 10) // Checks if penalties are out of bounds for skill level on XML
			{
				penalties = 10;
			}
			
			expertisePenalty = intensity;
			
			if (penalties > 0)
			{
				addSkill(SkillTable.getInstance().getInfo(4267, 1), false);
				sendSkillList();
			}
			else
			{
				removeSkill(getKnownSkill(4267));
				sendSkillList();
				expertisePenalty = 0;
			}
		}
	}
	
	public void checkIfWeaponIsAllowed()
	{
		// Override for Gamemasters
		if (isGM())
		{
			return;
		}
		// Iterate through all effects currently on the character.
		for (final L2Effect currenteffect : getAllEffects())
		{
			final L2Skill effectSkill = currenteffect.getSkill();
			// Ignore all buff skills that are party related (ie. songs, dances) while still remaining weapon dependant on cast though.
			if (!effectSkill.isOffensive() && !(effectSkill.getTargetType() == SkillTargetType.TARGET_PARTY && effectSkill.getSkillType() == SkillType.BUFF))
			{
				// Check to rest to assure current effect meets weapon requirements.
				if (!effectSkill.getWeaponDependancy(this))
				{
					sendMessage(effectSkill.getName() + " cannot be used with this weapon.");
					if (Config.DEBUG)
					{
						LOGGER.info("   | Skill " + effectSkill.getName() + " has been disabled for (" + getName() + "); Reason: Incompatible Weapon Type.");
					}
					currenteffect.exit();
				}
			}
			continue;
		}
	}
	
	/**
	 * Check ss match.
	 * @param equipped   the equipped
	 * @param unequipped the unequipped
	 */
	public void checkSSMatch(final L2ItemInstance equipped, final L2ItemInstance unequipped)
	{
		if (unequipped == null)
		{
			return;
		}
		
		if (unequipped.getItem().getType2() == L2Item.TYPE2_WEAPON && (equipped == null ? true : equipped.getItem().getCrystalType() != unequipped.getItem().getCrystalType()))
		// && getInventory().getItem() != null - must be fixed.
		{
			for (final L2ItemInstance ss : getInventory().getItems())
			{
				final int itemId = ss.getItemId();
				
				if ((itemId >= 2509 && itemId <= 2514 || itemId >= 3947 && itemId <= 3952 || itemId <= 1804 && itemId >= 1808 || itemId == 5789 || itemId == 5790 || itemId == 1835) && ss.getItem().getCrystalType() == unequipped.getItem().getCrystalType())
				{
					sendPacket(new ExAutoSoulShot(itemId, 0));
					
					final SystemMessage sm = new SystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
					sm.addString(ss.getItemName());
					sendPacket(sm);
				}
			}
		}
	}
	
	/**
	 * Return the the PvP Kills of the L2PcInstance (Number of player killed during a PvP).<BR>
	 * <BR>
	 * @return the pvp kills
	 */
	public int getPvpKills()
	{
		return pvpKills;
	}
	
	/**
	 * Set the the PvP Kills of the L2PcInstance (Number of player killed during a PvP).<BR>
	 * <BR>
	 * @param pvpKills the new pvp kills
	 */
	public void setPvpKills(final int pvpKills)
	{
		this.pvpKills = pvpKills;
		
		/*
		 * // Set hero aura if pvp kills > 100 if (pvpKills > 100) { isPermaHero = true; setHeroAura(true); }
		 */
	}
	
	/**
	 * Return the ClassId object of the L2PcInstance contained in L2PcTemplate.<BR>
	 * <BR>
	 * @return the class id
	 */
	public ClassId getClassId()
	{
		return getTemplate().classId;
	}
	
	/**
	 * Set the template of the L2PcInstance.<BR>
	 * <BR>
	 * @param Id The Identifier of the L2PcTemplate to set to the L2PcInstance
	 */
	public void setClassId(final int Id)
	{
		
		if (getLvlJoinedAcademy() != 0 && clan != null && PlayerClass.values()[Id].getLevel() == ClassLevel.Third)
		{
			if (getLvlJoinedAcademy() <= 16)
			{
				clan.setReputationScore(clan.getReputationScore() + 400, true);
			}
			else if (getLvlJoinedAcademy() >= 39)
			{
				clan.setReputationScore(clan.getReputationScore() + 170, true);
			}
			else
			{
				clan.setReputationScore(clan.getReputationScore() + 400 - (getLvlJoinedAcademy() - 16) * 10, true);
			}
			
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
			setLvlJoinedAcademy(0);
			// oust pledge member from the academy, cuz he has finished his 2nd class transfer
			SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED);
			msg.addString(getName());
			clan.broadcastToOnlineMembers(msg);
			clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(getName()));
			clan.removeClanMember(getName(), 0);
			sendPacket(new SystemMessage(SystemMessageId.ACADEMY_MEMBERSHIP_TERMINATED));
			msg = null;
			
			// receive graduation gift
			getInventory().addItem("Gift", 8181, 1, this, null); // give academy circlet
			getInventory().updateDatabase(); // update database
		}
		if (isSubClassActive())
		{
			getSubClasses().get(playerClassIndex).setClassId(Id);
		}
		// The efects of production - Clan/ Transfer
		broadcastPacket(new MagicSkillUser(this, this, 5103, 1, 100, 0));
		setClassTemplate(Id);
	}
	
	/**
	 * Return the Experience of the L2PcInstance.
	 * @return the exp
	 */
	public long getExp()
	{
		return getStat().getExp();
	}
	
	/**
	 * Sets the active enchant item.
	 * @param scroll the new active enchant item
	 */
	public void setActiveEnchantItem(final L2ItemInstance scroll)
	{
		activeEnchantItem = scroll;
	}
	
	/**
	 * Gets the active enchant item.
	 * @return the active enchant item
	 */
	public L2ItemInstance getActiveEnchantItem()
	{
		return activeEnchantItem;
	}
	
	/**
	 * Set the fists weapon of the L2PcInstance (used when no weapon is equiped).<BR>
	 * <BR>
	 * @param weaponItem The fists L2Weapon to set to the L2PcInstance
	 */
	public void setFistsWeaponItem(final L2Weapon weaponItem)
	{
		fistsWeaponItem = weaponItem;
	}
	
	/**
	 * Return the fists weapon of the L2PcInstance (used when no weapon is equiped).<BR>
	 * <BR>
	 * @return the fists weapon item
	 */
	public L2Weapon getFistsWeaponItem()
	{
		return fistsWeaponItem;
	}
	
	/**
	 * Return the fists weapon of the L2PcInstance Class (used when no weapon is equiped).<BR>
	 * <BR>
	 * @param  classId the class id
	 * @return         the l2 weapon
	 */
	public L2Weapon findFistsWeaponItem(final int classId)
	{
		L2Weapon weaponItem = null;
		if (classId >= 0x00 && classId <= 0x09)
		{
			// human fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(246);
			weaponItem = (L2Weapon) temp;
			temp = null;
		}
		else if (classId >= 0x0a && classId <= 0x11)
		{
			// human mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(251);
			weaponItem = (L2Weapon) temp;
			temp = null;
		}
		else if (classId >= 0x12 && classId <= 0x18)
		{
			// elven fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(244);
			weaponItem = (L2Weapon) temp;
			temp = null;
		}
		else if (classId >= 0x19 && classId <= 0x1e)
		{
			// elven mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(249);
			weaponItem = (L2Weapon) temp;
			temp = null;
		}
		else if (classId >= 0x1f && classId <= 0x25)
		{
			// dark elven fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(245);
			weaponItem = (L2Weapon) temp;
			temp = null;
		}
		else if (classId >= 0x26 && classId <= 0x2b)
		{
			// dark elven mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(250);
			weaponItem = (L2Weapon) temp;
			temp = null;
		}
		else if (classId >= 0x2c && classId <= 0x30)
		{
			// orc fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(248);
			weaponItem = (L2Weapon) temp;
			temp = null;
		}
		else if (classId >= 0x31 && classId <= 0x34)
		{
			// orc mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(252);
			weaponItem = (L2Weapon) temp;
			temp = null;
		}
		else if (classId >= 0x35 && classId <= 0x39)
		{
			// dwarven fists
			L2Item temp = ItemTable.getInstance().getTemplate(247);
			weaponItem = (L2Weapon) temp;
			temp = null;
		}
		
		return weaponItem;
	}
	
	/**
	 * Give Expertise skill of this level and remove beginner Lucky skill.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the Level of the L2PcInstance</li>
	 * <li>If L2PcInstance Level is 5, remove beginner Lucky skill</li>
	 * <li>Add the Expertise skill corresponding to its Expertise level</li>
	 * <li>Update the overloaded status of the L2PcInstance</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T give other free skills (SP needed = 0)</B></FONT><BR>
	 * <BR>
	 */
	public synchronized void rewardSkills()
	{
		rewardSkills(false);
	}
	
	public synchronized void rewardSkills(final boolean restore)
	{
		// Get the Level of the L2PcInstance
		final int lvl = getLevel();
		
		// Remove beginner Lucky skill
		if (lvl == 10)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(194, 1);
			skill = removeSkill(skill);
			
			if (Config.DEBUG && skill != null)
			{
				LOGGER.debug("Removed skill 'Lucky' from " + getName());
			}
			
			skill = null;
		}
		
		// Calculate the current higher Expertise of the L2PcInstance
		for (int i = 0; i < EXPERTISE_LEVELS.length; i++)
		{
			if (lvl >= EXPERTISE_LEVELS[i])
			{
				setExpertiseIndex(i);
			}
		}
		
		// Add the Expertise skill corresponding to its Expertise level
		if (getExpertiseIndex() > 0)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(239, getExpertiseIndex());
			addSkill(skill, !restore);
			
			if (Config.DEBUG)
			{
				LOGGER.debug("Awarded " + getName() + " with new expertise.");
			}
			
			skill = null;
		}
		else
		{
			if (Config.DEBUG)
			{
				LOGGER.debug("No skills awarded at lvl: " + lvl);
			}
		}
		
		// Active skill dwarven craft
		
		if (getSkillLevel(1321) < 1 && getRace() == Race.dwarf)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(1321, 1);
			addSkill(skill, !restore);
			skill = null;
		}
		
		// Active skill common craft
		if (getSkillLevel(1322) < 1)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(1322, 1);
			addSkill(skill, !restore);
			skill = null;
		}
		
		for (int i = 0; i < COMMON_CRAFT_LEVELS.length; i++)
		{
			if (lvl >= COMMON_CRAFT_LEVELS[i] && getSkillLevel(1320) < i + 1)
			{
				L2Skill skill = SkillTable.getInstance().getInfo(1320, i + 1);
				addSkill(skill, !restore);
				skill = null;
			}
		}
		
		// Auto-Learn skills if activated
		if (Config.AUTO_LEARN_SKILLS)
		{
			giveAvailableSkills();
		}
		sendSkillList();
		
		if (clan != null)
		{
			if (clan.getLevel() > 3 && isClanLeader())
			{
				SiegeManager.getInstance().addSiegeSkills(this);
			}
		}
		
		// This function gets called on login, so not such a bad place to check weight
		refreshOverloaded(); // Update the overloaded status of the L2PcInstance
		
		refreshExpertisePenalty(); // Update the expertise status of the L2PcInstance
		
		refreshMasteryPenality();
		
		refreshMasteryWeapPenality();
		
	}
	
	/**
	 * Regive all skills which aren't saved to database, like Noble, Hero, Clan Skills<BR>
	 * <BR>
	 * .
	 */
	private synchronized void regiveTemporarySkills()
	{
		// Do not call this on enterworld or char load
		
		// Add noble skills if noble
		if (isNoble())
		{
			setNoble(true);
		}
		
		// Add Hero skills if hero
		if (isHero())
		{
			setHero(true);
		}
		
		/*
		 * // Add clan leader skills if clanleader if(isClanLeader()) { setClanLeader(true); }
		 */
		
		// Add clan skills
		if (getClan() != null && getClan().getReputationScore() >= 0)
		{
			L2Skill[] skills = getClan().getAllSkills();
			for (final L2Skill sk : skills)
			{
				if (sk.getMinPledgeClass() <= getPledgeClass())
				{
					addSkill(sk, false);
				}
			}
			skills = null;
		}
		
		// Reload passive skills from armors / jewels / weapons
		getInventory().reloadEquippedItems();
		
	}
	
	/**
	 * Give all available skills to the player.<br>
	 * <br>
	 */
	public void giveAvailableSkills()
	{
		// ========================= OLD METHOD
		// // int unLearnable = 0;
		// int skillCounter = 0;
		//
		// // Get available skills
		// // L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(this, getClassId());
		// // while(skills.length > unLearnable)
		// // {
		// // unLearnable = 0;
		// // for(L2SkillLearn s : skills)
		// Collection<L2Skill> skills = SkillTreeTable.getInstance().getAllAvailableSkills(this, getClassId());
		// for (final L2Skill sk : skills)
		// {
		// // {
		// // L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
		// // if(sk == null || (sk.getId() == L2Skill.SKILL_DIVINE_INSPIRATION && !Config.AUTO_LEARN_DIVINE_INSPIRATION))
		// // {
		// // unLearnable++;
		// // continue;
		// // }
		//
		// if (getSkillLevel(sk.getId()) == -1)
		// {
		// skillCounter++;
		// }
		//
		// // Penality skill are not auto learn
		// if (sk.getId() == 4267 || sk.getId() == 4270)
		// continue;
		//
		// // fix when learning toggle skills
		// if (sk.isToggle())
		// {
		// final L2Effect toggleEffect = getFirstEffect(sk.getId());
		// if (toggleEffect != null)
		// {
		// // stop old toggle skill effect, and give new toggle skill effect back
		// toggleEffect.exit(false);
		// sk.getEffects(this, this, false, false, false);
		// }
		// }
		//
		// addSkill(sk, true);
		// }
		//
		// // // Get new available skills
		// // skills = SkillTreeTable.getInstance().getAvailableSkills(this, getClassId());
		// // }
		//
		// sendMessage("You have learned " + skillCounter + " new skills.");
		// skills = null;
		
		int skillCounter = 0;
		Collection<L2Skill> skills = SkillTreeTable.getInstance().getAllAvailableSkills(this, getClassId());
		
		for (final L2Skill skillToLearn : skills)
		{
			// if AUTO_LEARN_DIVINE_INSPIRATION = false, dont learn divine inspiration
			if (skillToLearn.getId() == L2Skill.SKILL_DIVINE_INSPIRATION && !Config.AUTO_LEARN_DIVINE_INSPIRATION)
			{
				continue;
			}
			
			L2Skill playerSkill = getSkills().get(skillToLearn.getId());
			
			if (playerSkill == null)
			{
				addSkill(skillToLearn, true);
				skillCounter++;
			}
			else if (playerSkill.getLevel() < skillToLearn.getLevel())
			{
				addSkill(skillToLearn, true);
				skillCounter++;
			}
		}
		
		if (skillCounter > 0)
		{
			sendMessage("You have learned " + skillCounter + " new skills.");
			sendSkillList();
		}
	}
	
	/**
	 * Set the Experience value of the L2PcInstance.
	 * @param exp the new exp
	 */
	public void setExp(final long exp)
	{
		getStat().setExp(exp);
	}
	
	public Race getRace()
	{
		if (!isSubClassActive())
		{
			return getTemplate().race;
		}
		
		final L2PcTemplate charTemp = CharTemplateTable.getInstance().getTemplate(baseClass);
		return charTemp.race;
	}
	
	public L2Radar getRadar()
	{
		return radar;
	}
	
	/**
	 * @return the SP amount of the L2PcInstance.
	 */
	public int getSp()
	{
		return getStat().getSp();
	}
	
	/**
	 * @param sp SP amount of the L2PcInstance.
	 */
	public void setSp(final int sp)
	{
		super.getStat().setSp(sp);
	}
	
	/**
	 * Return true if this L2PcInstance is a clan leader in ownership of the passed castle.
	 * @param  castleId the castle id
	 * @return          true, if is castle lord
	 */
	public boolean isCastleLord(final int castleId)
	{
		L2Clan clan = getClan();
		
		// player has clan and is the clan leader, check the castle info
		if (clan != null && clan.getLeader().getPlayerInstance() == this)
		{
			// if the clan has a castle and it is actually the queried castle, return true
			Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
			if (castle != null && castle == CastleManager.getInstance().getCastleById(castleId))
			{
				castle = null;
				return true;
			}
			castle = null;
		}
		clan = null;
		return false;
	}
	
	/**
	 * Return the Clan Identifier of the L2PcInstance.<BR>
	 * <BR>
	 * @return the clan id
	 */
	public int getClanId()
	{
		return clanId;
	}
	
	/**
	 * Return the Clan Crest Identifier of the L2PcInstance or 0.<BR>
	 * <BR>
	 * @return the clan crest id
	 */
	public int getClanCrestId()
	{
		if (clan != null && clan.hasCrest())
		{
			return clan.getCrestId();
		}
		
		return 0;
	}
	
	/**
	 * Gets the clan crest large id.
	 * @return The Clan CrestLarge Identifier or 0
	 */
	public int getClanCrestLargeId()
	{
		if (clan != null && clan.hasCrestLarge())
		{
			return clan.getCrestLargeId();
		}
		
		return 0;
	}
	
	/**
	 * Gets the clan join expiry time.
	 * @return the clan join expiry time
	 */
	public long getClanJoinExpiryTime()
	{
		return clanJoinExpiryTime;
	}
	
	/**
	 * Sets the clan join expiry time.
	 * @param time the new clan join expiry time
	 */
	public void setClanJoinExpiryTime(final long time)
	{
		clanJoinExpiryTime = time;
	}
	
	/**
	 * Gets the clan create expiry time.
	 * @return the clan create expiry time
	 */
	public long getClanCreateExpiryTime()
	{
		return clanCreateExpiryTime;
	}
	
	/**
	 * Sets the clan create expiry time.
	 * @param time the new clan create expiry time
	 */
	public void setClanCreateExpiryTime(final long time)
	{
		clanCreateExpiryTime = time;
	}
	
	/**
	 * Sets the online time.
	 * @param time the new online time
	 */
	public void setOnlineTime(final long time)
	{
		onlineTime = time;
		onlineBeginTime = System.currentTimeMillis();
	}
	
	public long getOnlineTime()
	{
		return onlineTime;
	}
	
	/**
	 * Return the PcInventory Inventory of the L2PcInstance contained in inventory.<BR>
	 * <BR>
	 * @return the inventory
	 */
	public PcInventory getInventory()
	{
		return inventory;
	}
	
	/**
	 * Delete a ShortCut of the L2PcInstance shortCuts.<BR>
	 * <BR>
	 * @param objectId the object id
	 */
	public void removeItemFromShortCut(final int objectId)
	{
		shortCuts.deleteShortCutByObjectId(objectId);
	}
	
	// MOVING on attack TASK, L2OFF FIX
	/** The launched moving task. */
	protected MoveOnAttack launchedMovingTask = null;
	
	/** The moving task defined. */
	protected Boolean movingTaskDefined = false;
	
	/**
	 * MoveOnAttack Task.
	 */
	public class MoveOnAttack implements Runnable
	{
		/** The player. */
		final L2PcInstance player;
		
		/** The pos. */
		L2CharPosition pos;
		
		/**
		 * Instantiates a new move on attack.
		 * @param player the player
		 * @param pos    the pos
		 */
		public MoveOnAttack(final L2PcInstance player, final L2CharPosition pos)
		{
			this.player = player;
			this.pos = pos;
			// launchedMovingTask = this;
		}
		
		@Override
		public void run()
		{
			synchronized (movingTaskDefined)
			{
				launchedMovingTask = null;
				movingTaskDefined = false;
			}
			// Set the Intention of this AbstractAI to AI_INTENTION_MOVE_TO
			player.getAI().changeIntention(AI_INTENTION_MOVE_TO, pos, null);
			
			// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
			player.getAI().clientStopAutoAttack();
			
			// Abort the attack of the L2Character and send Server->Client ActionFailed packet
			player.abortAttack();
			
			// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
			player.getAI().moveTo(pos.x, pos.y, pos.z);
		}
		
		/**
		 * Sets the new position.
		 * @param pos the new new position
		 */
		public void setNewPosition(final L2CharPosition pos)
		{
			this.pos = pos;
		}
	}
	
	/**
	 * Checks if is moving task defined.
	 * @return true, if is moving task defined
	 */
	public boolean isMovingTaskDefined()
	{
		return movingTaskDefined;
		// return launchedMovingTask != null;
	}
	
	public final void setMovingTaskDefined(final boolean value)
	{
		movingTaskDefined = value;
	}
	
	/**
	 * Define new moving task.
	 * @param pos the pos
	 */
	public void defineNewMovingTask(final L2CharPosition pos)
	{
		synchronized (movingTaskDefined)
		{
			launchedMovingTask = new MoveOnAttack(this, pos);
			movingTaskDefined = true;
		}
	}
	
	/**
	 * Modify moving task.
	 * @param pos the pos
	 */
	public void modifyMovingTask(final L2CharPosition pos)
	{
		synchronized (movingTaskDefined)
		{
			
			if (!movingTaskDefined)
			{
				return;
			}
			
			launchedMovingTask.setNewPosition(pos);
		}
	}
	
	/**
	 * Start moving task.
	 */
	public void startMovingTask()
	{
		synchronized (movingTaskDefined)
		{
			if (!movingTaskDefined)
			{
				return;
			}
			
			if (isMoving() && isAttackingNow())
			{
				return;
			}
			
			ThreadPoolManager.getInstance().executeTask(launchedMovingTask);
		}
	}
	
	/**
	 * Return True if the L2PcInstance is sitting.<BR>
	 * <BR>
	 * @return true, if is sitting
	 */
	public boolean isSitting()
	{
		return waitTypeSitting || sittingTaskLaunched;
	}
	
	/**
	 * Return True if the L2PcInstance is sitting task launched.<BR>
	 * <BR>
	 * @return true, if is sitting task launched
	 */
	public boolean isSittingTaskLaunched()
	{
		return sittingTaskLaunched;
	}
	
	/**
	 * Set waitTypeSitting to given value.
	 * @param state the new checks if is sitting
	 */
	public void setIsSitting(final boolean state)
	{
		waitTypeSitting = state;
	}
	
	/**
	 * Sets the posticipate sit.
	 * @param act the new posticipate sit
	 */
	public void setPosticipateSit(final boolean act)
	{
		posticipateSit = act;
	}
	
	/**
	 * Gets the posticipate sit.
	 * @return the posticipate sit
	 */
	public boolean getPosticipateSit()
	{
		return posticipateSit;
	}
	
	/**
	 * Sit down the L2PcInstance, set the AI Intention to AI_INTENTION_REST and send a Server->Client ChangeWaitType packet (broadcast)<BR>
	 * <BR>
	 * .
	 */
	public void sitDown()
	{
		if (isFakeDeath())
		{
			stopFakeDeath(null);
		}
		
		if (isMoving()) // since you are moving and want sit down
		// the posticipate sitdown task will be always true
		{
			setPosticipateSit(true);
			return;
		}
		
		// we are going to sitdown, so posticipate is false
		setPosticipateSit(false);
		
		if (isCastingNow() && !relax)
		{
			return;
		}
		
		if (sittingTaskLaunched)
		{
			// just return
			return;
		}
		
		if (!waitTypeSitting && !isAttackingDisabled() && !isOutOfControl() && !isImobilised())
		{
			breakAttack();
			setIsSitting(true);
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_SITTING));
			sittingTaskLaunched = true;
			// Schedule a sit down task to wait for the animation to finish
			ThreadPoolManager.getInstance().scheduleGeneral(new SitDownTask(this), 2500);
			setIsParalyzed(true);
		}
	}
	
	/**
	 * Sit down Task.
	 */
	class SitDownTask implements Runnable
	{
		
		/** The player. */
		L2PcInstance player;
		
		/** The this$0. */
		final L2PcInstance this$0;
		
		/**
		 * Instantiates a new sit down task.
		 * @param player the player
		 */
		SitDownTask(final L2PcInstance player)
		{
			this$0 = L2PcInstance.this;
			this.player = player;
		}
		
		@Override
		public void run()
		{
			setIsSitting(true);
			player.setIsParalyzed(false);
			sittingTaskLaunched = false;
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		}
	}
	
	/**
	 * Stand up Task.
	 */
	class StandUpTask implements Runnable
	{
		
		/** The player. */
		L2PcInstance player;
		
		/**
		 * Instantiates a new stand up task.
		 * @param player the player
		 */
		StandUpTask(final L2PcInstance player)
		{
			this.player = player;
		}
		
		@Override
		public void run()
		{
			player.setIsSitting(false);
			player.setIsImobilised(false);
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}
	
	/**
	 * Stand up the L2PcInstance, set the AI Intention to AI_INTENTION_IDLE and send a Server->Client ChangeWaitType packet (broadcast)<BR>
	 * <BR>
	 * .
	 */
	public void standUp()
	{
		if (isFakeDeath())
		{
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));
			// Schedule a stand up task to wait for the animation to finish
			setIsImobilised(true);
			ThreadPoolManager.getInstance().scheduleGeneral(new StandUpTask(this), 2000);
			stopFakeDeath(null);
		}
		
		if (sittingTaskLaunched)
		{
			return;
		}
		
		if (L2Event.active && eventSitForced)
		{
			sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up ...");
		}
		else if (TvT.isSitForced() && inEventTvT || CTF.isSitForced() && inEventCTF || DM.is_sitForced() && inEventDM)
		{
			sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up...");
		}
		else if (isAway())
		{
			sendMessage("You can't stand up if your Status is Away.");
		}
		else if (waitTypeSitting && !isInStoreMode() && !isAlikeDead())
		{
			if (relax)
			{
				setRelax(false);
				stopEffects(L2Effect.EffectType.RELAXING);
			}
			
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));
			// Schedule a stand up task to wait for the animation to finish
			setIsImobilised(true);
			ThreadPoolManager.getInstance().scheduleGeneral(new StandUpTask(this), 2500);
			
		}
	}
	
	/**
	 * Set the value of the relax value. Must be True if using skill Relax and False if not.
	 * @param val the new relax
	 */
	public void setRelax(final boolean val)
	{
		relax = val;
	}
	
	/**
	 * Return the PcWarehouse object of the L2PcInstance.<BR>
	 * <BR>
	 * @return the warehouse
	 */
	public PcWarehouse getWarehouse()
	{
		if (warehouse == null)
		{
			warehouse = new PcWarehouse(this);
			warehouse.restore();
		}
		if (Config.WAREHOUSE_CACHE)
		{
			WarehouseCacheManager.getInstance().addCacheTask(this);
		}
		return warehouse;
	}
	
	/**
	 * Free memory used by Warehouse.
	 */
	public void clearWarehouse()
	{
		if (warehouse != null)
		{
			warehouse.deleteMe();
		}
		warehouse = null;
	}
	
	/**
	 * Return the PcFreight object of the L2PcInstance.<BR>
	 * <BR>
	 * @return the freight
	 */
	public PcFreight getFreight()
	{
		return freight;
	}
	
	/**
	 * Return the Identifier of the L2PcInstance.<BR>
	 * <BR>
	 * @return the char id
	 */
	public int getCharId()
	{
		return charId;
	}
	
	/**
	 * Set the Identifier of the L2PcInstance.<BR>
	 * <BR>
	 * @param charId the new char id
	 */
	public void setCharId(final int charId)
	{
		this.charId = charId;
	}
	
	/**
	 * Return the Adena amount of the L2PcInstance.<BR>
	 * <BR>
	 * @return the adena
	 */
	public int getAdena()
	{
		return inventory.getAdena();
	}
	
	public int getItemCount(int itemId)
	{
		return getItemCount(itemId, 0);
	}
	
	/**
	 * Return the Item amount of the L2PcInstance.<BR>
	 * <BR>
	 * @param  itemId       the item id
	 * @param  enchantLevel the enchant level
	 * @return              the item count
	 */
	public int getItemCount(final int itemId, final int enchantLevel)
	{
		return inventory.getInventoryItemCount(itemId, enchantLevel);
	}
	
	/**
	 * Return the Ancient Adena amount of the L2PcInstance.<BR>
	 * <BR>
	 * @return the ancient adena
	 */
	public int getAncientAdena()
	{
		return inventory.getAncientAdena();
	}
	
	/**
	 * Add adena to Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process     : String Identifier of process triggering this action
	 * @param count       : int Quantity of adena to be added
	 * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addAdena(final String process, int count, final L2Object reference, final boolean sendMessage)
	{
		if (count > 0)
		{
			if (inventory.getAdena() == Integer.MAX_VALUE)
			{
				return;
			}
			else if (inventory.getAdena() >= Integer.MAX_VALUE - count)
			{
				count = Integer.MAX_VALUE - inventory.getAdena();
				inventory.addAdena(process, count, this, reference);
			}
			else if (inventory.getAdena() < Integer.MAX_VALUE - count)
			{
				inventory.addAdena(process, count, this, reference);
			}
			if (sendMessage)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_ADENA);
				sm.addNumber(count);
				sendPacket(sm);
				sm = null;
			}
			
			// Send update packet
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(inventory.getAdenaInstance());
				sendPacket(iu);
				iu = null;
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
		}
	}
	
	/**
	 * Reduce adena in Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param  process     : String Identifier of process triggering this action
	 * @param  count       : int Quantity of adena to be reduced
	 * @param  reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param  sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return             boolean informing if the action was successfull
	 */
	public boolean reduceAdena(final String process, final int count, final L2Object reference, final boolean sendMessage)
	{
		// Game master don't need to pay
		if (isGM())
		{
			sendMessage("You are a Gm, you don't need to pay! reduceAdena = 0.");
			return true;
		}
		if (count > getAdena())
		{
			
			if (sendMessage)
			{
				sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			}
			
			return false;
		}
		
		if (count > 0)
		{
			L2ItemInstance adenaItem = inventory.getAdenaInstance();
			inventory.reduceAdena(process, count, this, reference);
			
			// Send update packet
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(adenaItem);
				sendPacket(iu);
				iu = null;
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
			
			if (sendMessage)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ADENA);
				sm.addNumber(count);
				sendPacket(sm);
				sm = null;
			}
			adenaItem = null;
		}
		
		return true;
	}
	
	/**
	 * Add ancient adena to Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process     : String Identifier of process triggering this action
	 * @param count       : int Quantity of ancient adena to be added
	 * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addAncientAdena(final String process, final int count, final L2Object reference, final boolean sendMessage)
	{
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(PcInventory.ANCIENT_ADENA_ID);
			sm.addNumber(count);
			sendPacket(sm);
			sm = null;
		}
		
		if (count > 0)
		{
			inventory.addAncientAdena(process, count, this, reference);
			
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(inventory.getAncientAdenaInstance());
				sendPacket(iu);
				iu = null;
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
		}
	}
	
	/**
	 * Reduce ancient adena in Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param  process     : String Identifier of process triggering this action
	 * @param  count       : int Quantity of ancient adena to be reduced
	 * @param  reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param  sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return             boolean informing if the action was successfull
	 */
	public boolean reduceAncientAdena(final String process, final int count, final L2Object reference, final boolean sendMessage)
	{
		if (count > getAncientAdena())
		{
			if (sendMessage)
			{
				sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			}
			
			return false;
		}
		
		if (count > 0)
		{
			L2ItemInstance ancientAdenaItem = inventory.getAncientAdenaInstance();
			inventory.reduceAncientAdena(process, count, this, reference);
			
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(ancientAdenaItem);
				sendPacket(iu);
				iu = null;
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
			
			if (sendMessage)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
				sm.addNumber(count);
				sm.addItemName(PcInventory.ANCIENT_ADENA_ID);
				sendPacket(sm);
				sm = null;
			}
			ancientAdenaItem = null;
		}
		
		return true;
	}
	
	/**
	 * Adds item to inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process     : String Identifier of process triggering this action
	 * @param item        : L2ItemInstance to be added
	 * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addItem(final String process, final L2ItemInstance item, final L2Object reference, final boolean sendMessage)
	{
		if (item.getCount() > 0)
		{
			// Sends message to client if requested
			if (sendMessage)
			{
				if (item.getCount() > 1)
				{
					if (item.isStackable() && !Config.MULTIPLE_ITEM_DROP)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
						sm.addItemName(item.getItemId());
						sendPacket(sm);
						sm = null;
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
						sm.addItemName(item.getItemId());
						sm.addNumber(item.getCount());
						sendPacket(sm);
						sm = null;
					}
					
				}
				else if (item.getEnchantLevel() > 0)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(item.getItemId());
					sendPacket(sm);
					sm = null;
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
					sm.addItemName(item.getItemId());
					sendPacket(sm);
					sm = null;
				}
			}
			
			// Add the item to inventory
			L2ItemInstance newitem = inventory.addItem(process, item, this, reference);
			
			// Send inventory update packet
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate playerIU = new InventoryUpdate();
				playerIU.addItem(newitem);
				sendPacket(playerIU);
				playerIU = null;
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
			
			// Update current load as well
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
			sendPacket(su);
			su = null;
			
			// If over capacity, Drop the item
			if (!isGM() && !inventory.validateCapacity(0))
			{
				dropItem("InvDrop", newitem, null, true, true);
			}
			else if (CursedWeaponsManager.getInstance().isCursed(newitem.getItemId()))
			{
				CursedWeaponsManager.getInstance().activate(this, newitem);
			}
			newitem = null;
		}
		
		// If you pickup arrows.
		if (item.getItem().getItemType() == L2EtcItemType.ARROW)
		{
			// If a bow is equipped, try to equip them if no arrows is currently equipped.
			final L2Weapon currentWeapon = getActiveWeaponItem();
			if (currentWeapon != null && currentWeapon.getItemType() == L2WeaponType.BOW && getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
			{
				checkAndEquipArrows();
			}
		}
	}
	
	/**
	 * Adds item to Inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process     : String Identifier of process triggering this action
	 * @param itemId      : int Item Identifier of the item to be added
	 * @param count       : int Quantity of items to be added
	 * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addItem(final String process, final int itemId, final int count, final L2Object reference, final boolean sendMessage)
	{
		if (count > 0)
		{
			// Sends message to client if requested
			if (sendMessage && (!isCastingNow() && ItemTable.getInstance().createDummyItem(itemId).getItemType() == L2EtcItemType.HERB || ItemTable.getInstance().createDummyItem(itemId).getItemType() != L2EtcItemType.HERB))
			{
				if (count > 1)
				{
					if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
						sm.addItemName(itemId);
						sm.addNumber(count);
						sendPacket(sm);
						sm = null;
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
						sm.addItemName(itemId);
						sm.addNumber(count);
						sendPacket(sm);
						sm = null;
					}
				}
				else
				{
					if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
						sm.addItemName(itemId);
						sendPacket(sm);
						sm = null;
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
						sm.addItemName(itemId);
						sendPacket(sm);
						sm = null;
					}
				}
			}
			// Auto use herbs - autoloot
			if (ItemTable.getInstance().createDummyItem(itemId).getItemType() == L2EtcItemType.HERB) // If item is herb dont add it to iv :]
			{
				if (!isCastingNow() && !isCastingPotionNow())
				{
					L2ItemInstance herb = new L2ItemInstance(charId, itemId);
					IItemHandler handler = ItemHandler.getInstance().getItemHandler(herb.getItemId());
					
					if (handler == null)
					{
						LOGGER.warn("No item handler registered for Herb - item ID " + herb.getItemId() + ".");
					}
					else
					{
						handler.useItem(this, herb);
						
						if (herbsTaskTime >= 100)
						{
							herbsTaskTime -= 100;
						}
						
						handler = null;
					}
					
					herb = null;
				}
				else
				{
					herbsTaskTime += 100;
					ThreadPoolManager.getInstance().scheduleAi(new HerbTask(process, itemId, count, reference, sendMessage), herbsTaskTime);
				}
			}
			else
			{
				// Add the item to inventory
				L2ItemInstance item = inventory.addItem(process, itemId, count, this, reference);
				
				// Send inventory update packet
				if (!Config.FORCE_INVENTORY_UPDATE)
				{
					InventoryUpdate playerIU = new InventoryUpdate();
					playerIU.addItem(item);
					sendPacket(playerIU);
					playerIU = null;
				}
				else
				{
					sendPacket(new ItemList(this, false));
				}
				
				// Update current load as well
				StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
				sendPacket(su);
				su = null;
				
				// If over capacity, drop the item
				if (!isGM() && !inventory.validateCapacity(item))
				{
					dropItem("InvDrop", item, null, true, true);
				}
				else if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
				{
					CursedWeaponsManager.getInstance().activate(this, item);
				}
				
				item = null;
			}
		}
	}
	
	/**
	 * Destroy item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param  process     : String Identifier of process triggering this action
	 * @param  item        : L2ItemInstance to be destroyed
	 * @param  reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param  sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return             boolean informing if the action was successfull
	 */
	public boolean destroyItem(final String process, L2ItemInstance item, final L2Object reference, final boolean sendMessage)
	{
		item = inventory.destroyItem(process, item, this, reference);
		
		if (item == null)
		{
			if (sendMessage)
			{
				sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			}
			
			return false;
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
			playerIU = null;
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		su = null;
		
		// Sends message to client if requested
		if (sendMessage)
		{
			if (count > 1)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
				sm.addItemName(item.getItemId());
				sm.addNumber(count);
				sendPacket(sm);
				sm = null;
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
				sm.addItemName(item.getItemId());
				sendPacket(sm);
				sm = null;
			}
		}
		
		return true;
	}
	
	/**
	 * Destroys item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param  process     : String Identifier of process triggering this action
	 * @param  objectId    : int Item Instance identifier of the item to be destroyed
	 * @param  count       : int Quantity of items to be destroyed
	 * @param  reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param  sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return             boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItem(final String process, final int objectId, final int count, final L2Object reference, final boolean sendMessage)
	{
		L2ItemInstance item = inventory.getItemByObjectId(objectId);
		
		if (item == null || item.getCount() < count || inventory.destroyItem(process, objectId, count, this, reference) == null)
		{
			if (sendMessage)
			{
				sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			}
			
			return false;
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
			playerIU = null;
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		su = null;
		
		// Sends message to client if requested
		if (sendMessage)
		{
			if (count > 1)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
				sm.addItemName(item.getItemId());
				sm.addNumber(count);
				sendPacket(sm);
				sm = null;
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
				sm.addItemName(item.getItemId());
				sendPacket(sm);
				sm = null;
			}
		}
		item = null;
		
		return true;
	}
	
	/**
	 * Destroys shots from inventory without logging and only occasional saving to database. Sends a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param  process     : String Identifier of process triggering this action
	 * @param  objectId    : int Item Instance identifier of the item to be destroyed
	 * @param  count       : int Quantity of items to be destroyed
	 * @param  reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param  sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return             boolean informing if the action was successfull
	 */
	public boolean destroyItemWithoutTrace(final String process, final int objectId, final int count, final L2Object reference, final boolean sendMessage)
	{
		L2ItemInstance item = inventory.getItemByObjectId(objectId);
		
		if (item == null || item.getCount() < count)
		{
			if (sendMessage)
			{
				sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			}
			return false;
		}
		
		// Adjust item quantity
		if (item.getCount() > count)
		{
			synchronized (item)
			{
				item.changeCountWithoutTrace(process, -count, this, reference);
				item.setLastChange(L2ItemInstance.MODIFIED);
				
				// could do also without saving, but let's save approx 1 of 10
				if (GameTimeController.getGameTicks() % 10 == 0)
				{
					item.updateDatabase();
				}
				inventory.refreshWeight();
			}
		}
		else
		{
			// Destroy entire item and save to database
			inventory.destroyItem(process, item, this, reference);
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
			playerIU = null;
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		su = null;
		
		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
			sm.addNumber(count);
			sm.addItemName(item.getItemId());
			sendPacket(sm);
			sm = null;
		}
		item = null;
		
		return true;
	}
	
	/**
	 * Destroy item from inventory by using its <B>itemId</B> and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param  process     : String Identifier of process triggering this action
	 * @param  itemId      : int Item identifier of the item to be destroyed
	 * @param  count       : int Quantity of items to be destroyed
	 * @param  reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param  sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return             boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItemByItemId(final String process, final int itemId, final int count, final L2Object reference, final boolean sendMessage)
	{
		L2ItemInstance item = inventory.getItemByItemId(itemId);
		
		if (item == null || item.getCount() < count || inventory.destroyItemByItemId(process, itemId, count, this, reference) == null)
		{
			if (sendMessage)
			{
				sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			}
			
			return false;
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
			playerIU = null;
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		su = null;
		
		// Sends message to client if requested
		if (sendMessage)
		{
			if (count > 1)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
				sm.addItemName(item.getItemId());
				sm.addNumber(count);
				sendPacket(sm);
				sm = null;
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
				sm.addItemName(item.getItemId());
				sendPacket(sm);
				sm = null;
			}
		}
		item = null;
		
		return true;
	}
	
	/**
	 * Destroy all weared items from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process     : String Identifier of process triggering this action
	 * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void destroyWearedItems(final String process, final L2Object reference, final boolean sendMessage)
	{
		
		// Go through all Items of the inventory
		for (final L2ItemInstance item : getInventory().getItems())
		{
			// Check if the item is a Try On item in order to remove it
			if (item.isWear())
			{
				if (item.isEquipped())
				{
					getInventory().unEquipItemInSlotAndRecord(item.getEquipSlot());
				}
				
				if (inventory.destroyItem(process, item, this, reference) == null)
				{
					LOGGER.warn("Player " + getName() + " can't destroy weared item: " + item.getName() + "[ " + item.getObjectId() + " ]");
					continue;
				}
				
				// Send an Unequipped Message in system window of the player for each Item
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(item.getItemId());
				sendPacket(sm);
				sm = null;
			}
		}
		
		// Send the StatusUpdate Server->Client Packet to the player with new CUR_LOAD (0x0e) information
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		su = null;
		
		// Send the ItemList Server->Client Packet to the player in order to refresh its Inventory
		ItemList il = new ItemList(getInventory().getItems(), true);
		sendPacket(il);
		il = null;
		
		// Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its knownPlayers
		broadcastUserInfo();
		
		// Sends message to client if requested
		sendMessage("Trying-on mode has ended.");
		
	}
	
	/**
	 * Transfers item to another ItemContainer and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param  process   : String Identifier of process triggering this action
	 * @param  objectId  the object id
	 * @param  count     : int Quantity of items to be transfered
	 * @param  target    the target
	 * @param  reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return           L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public L2ItemInstance transferItem(final String process, final int objectId, final int count, final Inventory target, final L2Object reference)
	{
		L2ItemInstance oldItem = checkItemManipulation(objectId, count, "transfer");
		if (oldItem == null)
		{
			return null;
		}
		
		final L2ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, this, reference);
		if (newItem == null)
		{
			return null;
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			
			if (oldItem.getCount() > 0 && oldItem != newItem)
			{
				playerIU.addModifiedItem(oldItem);
			}
			else
			{
				playerIU.addRemovedItem(oldItem);
			}
			
			sendPacket(playerIU);
			playerIU = null;
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate playerSU = new StatusUpdate(getObjectId());
		playerSU.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(playerSU);
		playerSU = null;
		
		// Send target update packet
		if (target instanceof PcInventory)
		{
			L2PcInstance targetPlayer = ((PcInventory) target).getOwner();
			
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				final InventoryUpdate playerIU = new InventoryUpdate();
				
				if (newItem.getCount() > count)
				{
					playerIU.addModifiedItem(newItem);
				}
				else
				{
					playerIU.addNewItem(newItem);
				}
				
				targetPlayer.sendPacket(playerIU);
			}
			else
			{
				targetPlayer.sendPacket(new ItemList(targetPlayer, false));
			}
			
			// Update current load as well
			playerSU = new StatusUpdate(targetPlayer.getObjectId());
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, targetPlayer.getCurrentLoad());
			targetPlayer.sendPacket(playerSU);
			targetPlayer = null;
			playerSU = null;
		}
		else if (target instanceof PetInventory)
		{
			PetInventoryUpdate petIU = new PetInventoryUpdate();
			
			if (newItem.getCount() > count)
			{
				petIU.addModifiedItem(newItem);
			}
			else
			{
				petIU.addNewItem(newItem);
			}
			
			((PetInventory) target).getOwner().getOwner().sendPacket(petIU);
			petIU = null;
		}
		oldItem = null;
		
		return newItem;
	}
	
	/**
	 * Drop item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param  process     : String Identifier of process triggering this action
	 * @param  item        : L2ItemInstance to be dropped
	 * @param  reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param  sendMessage : boolean Specifies whether to send message to Client about this action
	 * @param  protectItem the protect item
	 * @return             boolean informing if the action was successfull
	 */
	public boolean dropItem(final String process, L2ItemInstance item, final L2Object reference, final boolean sendMessage, final boolean protectItem)
	{
		
		if (freight.getItemByObjectId(item.getObjectId()) != null)
		{
			
			// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
			this.sendPacket(ActionFailed.STATIC_PACKET);
			
			Util.handleIllegalPlayerAction(this, "Warning!! Character " + getName() + " of account " + getAccountName() + " tried to drop Freight Items", IllegalPlayerAction.PUNISH_KICK);
			return false;
			
		}
		
		item = inventory.dropItem(process, item, this, reference);
		
		if (item == null)
		{
			if (sendMessage)
			{
				sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			}
			
			return false;
		}
		
		item.dropMe(this, getClientX() + Rnd.get(50) - 25, getClientY() + Rnd.get(50) - 25, getClientZ() + 20);
		
		if (Config.DESTROY_DROPPED_PLAYER_ITEM && !Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
		{
			
			if (Config.AUTODESTROY_ITEM_AFTER > 0)
			{ // autodestroy enabled
				
				if (item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM || !item.isEquipable())
				{
					ItemsAutoDestroy.getInstance().addItem(item);
					item.setProtected(false);
				}
				else
				{
					item.setProtected(true);
				}
				
			}
			else
			{
				item.setProtected(true);
			}
			
		}
		else
		{
			item.setProtected(true);
			
		}
		
		if (protectItem)
		{
			item.getDropProtection().protect(this);
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
			playerIU = null;
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		su = null;
		
		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DROPPED_S1);
			sm.addItemName(item.getItemId());
			sendPacket(sm);
			sm = null;
		}
		
		return true;
	}
	
	/**
	 * Drop item from inventory by using its <B>objectID</B> and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param  process     : String Identifier of process triggering this action
	 * @param  objectId    : int Item Instance identifier of the item to be dropped
	 * @param  count       : int Quantity of items to be dropped
	 * @param  x           : int coordinate for drop X
	 * @param  y           : int coordinate for drop Y
	 * @param  z           : int coordinate for drop Z
	 * @param  reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param  sendMessage : boolean Specifies whether to send message to Client about this action
	 * @param  protectItem the protect item
	 * @return             L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public L2ItemInstance dropItem(final String process, final int objectId, final int count, final int x, final int y, final int z, final L2Object reference, final boolean sendMessage, final boolean protectItem)
	{
		
		if (freight.getItemByObjectId(objectId) != null)
		{
			
			// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
			this.sendPacket(ActionFailed.STATIC_PACKET);
			
			Util.handleIllegalPlayerAction(this, "Warning!! Character " + getName() + " of account " + getAccountName() + " tried to drop Freight Items", IllegalPlayerAction.PUNISH_KICK);
			return null;
			
		}
		
		L2ItemInstance invitem = inventory.getItemByObjectId(objectId);
		final L2ItemInstance item = inventory.dropItem(process, objectId, count, this, reference);
		
		if (item == null)
		{
			if (sendMessage)
			{
				sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			}
			
			return null;
		}
		
		item.dropMe(this, x, y, z);
		
		if (Config.AUTODESTROY_ITEM_AFTER > 0 && Config.DESTROY_DROPPED_PLAYER_ITEM && !Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
		{
			if (item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM || !item.isEquipable())
			{
				ItemsAutoDestroy.getInstance().addItem(item);
			}
		}
		if (Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			if (!item.isEquipable() || item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
			{
				item.setProtected(false);
			}
			else
			{
				item.setProtected(true);
			}
		}
		else
		{
			item.setProtected(true);
		}
		
		if (protectItem)
		{
			item.getDropProtection().protect(this);
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(invitem);
			sendPacket(playerIU);
			playerIU = null;
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		su = null;
		
		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DROPPED_S1);
			sm.addItemName(item.getItemId());
			sendPacket(sm);
			sm = null;
		}
		invitem = null;
		
		return item;
	}
	
	/**
	 * Check item manipulation.
	 * @param  objectId the object id
	 * @param  count    the count
	 * @param  action   the action
	 * @return          the l2 item instance
	 */
	public L2ItemInstance checkItemManipulation(final int objectId, final int count, final String action)
	{
		if (L2World.getInstance().findObject(objectId) == null)
		{
			LOGGER.warn(getObjectId() + ": player tried to " + action + " item not available in L2World");
			return null;
		}
		
		final L2ItemInstance item = getInventory().getItemByObjectId(objectId);
		
		if (item == null || item.getOwnerId() != getObjectId())
		{
			LOGGER.warn(getObjectId() + ": player tried to " + action + " item he is not owner of");
			return null;
		}
		
		if (count < 0 || count > 1 && !item.isStackable())
		{
			LOGGER.warn(getObjectId() + ": player tried to " + action + " item with invalid count: " + count);
			return null;
		}
		
		if (count > item.getCount())
		{
			LOGGER.warn(getObjectId() + ": player tried to " + action + " more items than he owns");
			return null;
		}
		
		// Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
		if (getPet() != null && getPet().getControlItemId() == objectId || getMountObjectID() == objectId)
		{
			if (Config.DEBUG)
			{
				LOGGER.debug(getObjectId() + ": player tried to " + action + " item controling pet");
			}
			
			return null;
		}
		
		if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
		{
			if (Config.DEBUG)
			{
				LOGGER.debug(getObjectId() + ":player tried to " + action + " an enchant scroll he was using");
			}
			
			return null;
		}
		
		if (item.isWear())
		{
			// cannot drop/trade wear-items
			return null;
		}
		
		return item;
	}
	
	/**
	 * Set protectEndTime according settings.
	 * @param protect the new protection
	 */
	public void setProtection(final boolean protect)
	{
		if (Config.DEVELOPER && (protect || protectEndTime > 0))
		{
			LOGGER.info(getName() + ": Protection " + (protect ? "ON " + (GameTimeController.getGameTicks() + Config.PLAYER_SPAWN_PROTECTION * GameTimeController.TICKS_PER_SECOND) : "OFF") + " (currently " + GameTimeController.getGameTicks() + ")");
		}
		
		if (isInOlympiadMode())
		{
			return;
		}
		
		protectEndTime = protect ? GameTimeController.getGameTicks() + Config.PLAYER_SPAWN_PROTECTION * GameTimeController.TICKS_PER_SECOND : 0;
		
		if (protect)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new TeleportProtectionFinalizer(this), (Config.PLAYER_SPAWN_PROTECTION - 1) * 1000);
		}
	}
	
	/**
	 * Set teleportProtectEndTime according settings.
	 * @param protect the new protection
	 */
	public void setTeleportProtection(final boolean protect)
	{
		if (Config.DEVELOPER && (protect || teleportProtectEndTime > 0))
		{
			LOGGER.warn(getName() + ": Tele Protection " + (protect ? "ON " + (GameTimeController.getGameTicks() + Config.PLAYER_TELEPORT_PROTECTION * GameTimeController.TICKS_PER_SECOND) : "OFF") + " (currently " + GameTimeController.getGameTicks() + ")");
		}
		
		teleportProtectEndTime = protect ? GameTimeController.getGameTicks() + Config.PLAYER_TELEPORT_PROTECTION * GameTimeController.TICKS_PER_SECOND : 0;
		
		if (protect)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new TeleportProtectionFinalizer(this), (Config.PLAYER_TELEPORT_PROTECTION - 1) * 1000);
		}
	}
	
	static class TeleportProtectionFinalizer implements Runnable
	{
		private final L2PcInstance activeChar;
		
		TeleportProtectionFinalizer(final L2PcInstance activeChar)
		{
			this.activeChar = activeChar;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (activeChar.isSpawnProtected())
				{
					activeChar.sendMessage("The effect of Spawn Protection has been removed.");
				}
				else if (activeChar.isTeleportProtected())
				{
					activeChar.sendMessage("The effect of Teleport Spawn Protection has been removed.");
				}
				
				if (Config.PLAYER_SPAWN_PROTECTION > 0)
				{
					activeChar.setProtection(false);
				}
				
				if (Config.PLAYER_TELEPORT_PROTECTION > 0)
				{
					activeChar.setTeleportProtection(false);
				}
			}
			catch (final Throwable e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Set protection from agro mobs when getting up from fake death, according settings.
	 * @param protect the new recent fake death
	 */
	public void setRecentFakeDeath(final boolean protect)
	{
		recentFakeDeathEndTime = protect ? GameTimeController.getGameTicks() + Config.PLAYER_FAKEDEATH_UP_PROTECTION * GameTimeController.TICKS_PER_SECOND : 0;
	}
	
	/**
	 * Checks if is recent fake death.
	 * @return true, if is recent fake death
	 */
	public boolean isRecentFakeDeath()
	{
		return recentFakeDeathEndTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Get the client owner of this char.<BR>
	 * <BR>
	 * @return the client
	 */
	public L2GameClient getClient()
	{
		return playerClient;
	}
	
	/**
	 * Sets the client.
	 * @param client the new client
	 */
	public void setClient(final L2GameClient client)
	{
		if (client == null && playerClient != null)
		{
			playerClient.stopGuardTask();
			nProtect.getInstance().closeSession(playerClient);
		}
		playerClient = client;
	}
	
	/**
	 * Close the active connection with the client.<BR>
	 * <BR>
	 */
	public void closeNetConnection()
	{
		if (playerClient != null)
		{
			playerClient.close(new LeaveWorld());
			setClient(null);
		}
	}
	
	/**
	 * Manage actions when a player click on this L2PcInstance.<BR>
	 * <BR>
	 * <B><U> Actions on first click on the L2PcInstance (Select it)</U> :</B><BR>
	 * <BR>
	 * <li>Set the target of the player</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li><BR>
	 * <BR>
	 * <B><U> Actions on second click on the L2PcInstance (Follow it/Attack it/Intercat with it)</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li>
	 * <li>If this L2PcInstance has a Private Store, notify the player AI with AI_INTENTION_INTERACT</li>
	 * <li>If this L2PcInstance is autoAttackable, notify the player AI with AI_INTENTION_ATTACK</li><BR>
	 * <BR>
	 * <li>If this L2PcInstance is NOT autoAttackable, notify the player AI with AI_INTENTION_FOLLOW</li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Client packet : Action, AttackRequest</li><BR>
	 * <BR>
	 * @param player The player that start an action on this L2PcInstance
	 */
	@Override
	public void onAction(final L2PcInstance player)
	{
		// if ((TvT._started && !Config.TVT_ALLOW_INTERFERENCE) || (CTF._started && !Config.CTF_ALLOW_INTERFERENCE) || (DM._started && !Config.DM_ALLOW_INTERFERENCE))
		// no Interaction with not participant to events
		if ((TvT.isStarted() || TvT.isTeleport()) && !Config.TVT_ALLOW_INTERFERENCE || (CTF.isStarted() || CTF.isTeleport()) && !Config.CTF_ALLOW_INTERFERENCE || (DM.isStarted() || DM.is_teleport()) && !Config.DM_ALLOW_INTERFERENCE)
		{
			if (inEventTvT && !player.inEventTvT || !inEventTvT && player.inEventTvT)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			else if (inEventCTF && !player.inEventCTF || !inEventCTF && player.inEventCTF)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			else if (inEventDM && !player.inEventDM || !inEventDM && player.inEventDM)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		// Check if the L2PcInstance is confused
		if (player.isOutOfControl())
		{
			// Send a Server->Client packet ActionFailed to the player
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the player already target this L2PcInstance
		if (player.getTarget() != this)
		{
			// Set the target of the player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the player
			// The color to display in the select window is White
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			if (player != this)
			{
				player.sendPacket(new ValidateLocation(this));
			}
		}
		else
		{
			if (player != this)
			{
				player.sendPacket(new ValidateLocation(this));
			}
			// Check if this L2PcInstance has a Private Store
			if (getPrivateStoreType() != 0)
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				
				// Calculate the distance between the L2PcInstance
				if (canInteract(player))
				{
					// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
				}
			}
			else
			{
				if (EngineModsManager.onInteract(player, this))
				{
					// Send a Server->Client packet ActionFailed to the player
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				if (isAutoAttackable(player))
				{
					
					if (Config.ALLOW_CHAR_KILL_PROTECT)
					{
						Siege siege = SiegeManager.getInstance().getSiege(player);
						
						if (siege != null && siege.getIsInProgress())
						{
							if (player.getLevel() > 20 && ((L2Character) player.getTarget()).getLevel() < 20)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
							
							if (player.getLevel() > 40 && ((L2Character) player.getTarget()).getLevel() < 40)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
							
							if (player.getLevel() > 52 && ((L2Character) player.getTarget()).getLevel() < 52)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
							
							if (player.getLevel() > 61 && ((L2Character) player.getTarget()).getLevel() < 61)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
							
							if (player.getLevel() > 76 && ((L2Character) player.getTarget()).getLevel() < 76)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
							
							if (player.getLevel() < 20 && ((L2Character) player.getTarget()).getLevel() > 20)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
							
							if (player.getLevel() < 40 && ((L2Character) player.getTarget()).getLevel() > 40)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
							
							if (player.getLevel() < 52 && ((L2Character) player.getTarget()).getLevel() > 52)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
							
							if (player.getLevel() < 61 && ((L2Character) player.getTarget()).getLevel() > 61)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
							
							if (player.getLevel() < 76 && ((L2Character) player.getTarget()).getLevel() > 76)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
						}
						siege = null;
					}
					
					// Player with lvl < 21 can't attack a cursed weapon holder
					// And a cursed weapon holder can't attack players with lvl < 21
					if (isCursedWeaponEquiped() && player.getLevel() < 21 || player.isCursedWeaponEquiped() && getLevel() < 21)
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else
					{
						if (Config.GEODATA > 0)
						{
							if (GeoData.getInstance().canSeeTarget(player, this))
							{
								player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
								player.onActionRequest();
							}
						}
						else
						{
							player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
							player.onActionRequest();
						}
					}
				}
				else
				{
					if (Config.GEODATA > 0)
					{
						if (GeoData.getInstance().canSeeTarget(player, this))
						{
							player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
						}
					}
					else
					{
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
					}
				}
			}
		}
	}
	
	@Override
	public void onActionShift(final L2PcInstance player)
	{
		final L2Weapon currentWeapon = player.getActiveWeaponItem();
		
		if (player.isGM())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			player.sendPacket(new ValidateLocation(this));
			AdminEditChar.gatherCharacterInfo(player, this, "charinfo.htm");
		}
		else
		// Like L2OFF set the target of the L2PcInstance player
		{
			if ((TvT.isStarted() || TvT.isTeleport()) && !Config.TVT_ALLOW_INTERFERENCE || (CTF.isStarted() || CTF.isTeleport()) && !Config.CTF_ALLOW_INTERFERENCE || (DM.isStarted() || DM.is_teleport()) && !Config.DM_ALLOW_INTERFERENCE)
			{
				if (inEventTvT && !player.inEventTvT || !inEventTvT && player.inEventTvT)
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				else if (inEventCTF && !player.inEventCTF || !inEventCTF && player.inEventCTF)
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				else if (inEventDM && !player.inEventDM || !inEventDM && player.inEventDM)
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			// Check if the L2PcInstance is confused
			if (player.isOutOfControl())
			{
				// Send a Server->Client packet ActionFailed to the player
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Check if the player already target this L2PcInstance
			if (player.getTarget() != this)
			{
				// Set the target of the player
				player.setTarget(this);
				
				// Send a Server->Client packet MyTargetSelected to the player
				// The color to display in the select window is White
				player.sendPacket(new MyTargetSelected(getObjectId(), 0));
				if (player != this)
				{
					player.sendPacket(new ValidateLocation(this));
				}
			}
			else
			{
				if (player != this)
				{
					player.sendPacket(new ValidateLocation(this));
				}
				// Check if this L2PcInstance has a Private Store
				if (getPrivateStoreType() != 0)
				{
					// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
					
					// Calculate the distance between the L2PcInstance
					if (canInteract(player))
					{
						// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
					}
				}
				else
				{
					// Check if this L2PcInstance is autoAttackable
					if (isAutoAttackable(player))
					{
						
						if (Config.ALLOW_CHAR_KILL_PROTECT)
						{
							Siege siege = SiegeManager.getInstance().getSiege(player);
							
							if (siege != null && siege.getIsInProgress())
							{
								if (player.getLevel() > 20 && ((L2Character) player.getTarget()).getLevel() < 20)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
								if (player.getLevel() > 40 && ((L2Character) player.getTarget()).getLevel() < 40)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
								if (player.getLevel() > 52 && ((L2Character) player.getTarget()).getLevel() < 52)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
								if (player.getLevel() > 61 && ((L2Character) player.getTarget()).getLevel() < 61)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
								if (player.getLevel() > 76 && ((L2Character) player.getTarget()).getLevel() < 76)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
								if (player.getLevel() < 20 && ((L2Character) player.getTarget()).getLevel() > 20)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
								if (player.getLevel() < 40 && ((L2Character) player.getTarget()).getLevel() > 40)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
								if (player.getLevel() < 52 && ((L2Character) player.getTarget()).getLevel() > 52)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
								if (player.getLevel() < 61 && ((L2Character) player.getTarget()).getLevel() > 61)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
								if (player.getLevel() < 76 && ((L2Character) player.getTarget()).getLevel() > 76)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
							}
							siege = null;
						}
						
						// Player with lvl < 21 can't attack a cursed weapon holder
						// And a cursed weapon holder can't attack players with lvl < 21
						if (isCursedWeaponEquiped() && player.getLevel() < 21 || player.isCursedWeaponEquiped() && getLevel() < 21)
						{
							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
						else
						{
							if (Config.GEODATA > 0)
							{
								if (GeoData.getInstance().canSeeTarget(player, this))
								{
									// Calculate the distance between the L2PcInstance
									// Only archer can hit from long
									if (currentWeapon != null && currentWeapon.getItemType() == L2WeaponType.BOW)
									{
										player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
										player.onActionRequest();
									}
									else if (canInteract(player))
									{
										player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
										player.onActionRequest();
									}
									else
									{
										player.sendPacket(ActionFailed.STATIC_PACKET);
									}
								}
							}
							else
							{
								// Calculate the distance between the L2PcInstance
								// Only archer can hit from long
								if (currentWeapon != null && currentWeapon.getItemType() == L2WeaponType.BOW)
								{
									player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
									player.onActionRequest();
								}
								else if (canInteract(player))
								{
									player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
									player.onActionRequest();
								}
								else
								{
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
							}
						}
					}
					else
					{
						if (Config.GEODATA > 0)
						{
							if (GeoData.getInstance().canSeeTarget(player, this))
							{
								// Calculate the distance between the L2PcInstance
								// Only archer can hit from long
								if (currentWeapon != null && currentWeapon.getItemType() == L2WeaponType.BOW)
								{
									player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
								}
								else if (canInteract(player))
								{
									player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
								}
								else
								{
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
							}
						}
						else
						{
							// Calculate the distance between the L2PcInstance
							// Only archer can hit from long
							if (currentWeapon != null && currentWeapon.getItemType() == L2WeaponType.BOW)
							{
								player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
							}
							else if (canInteract(player))
							{
								player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
							}
							else
							{
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * If player is in Event, TvT started, DM started, CTF started
	 */
	@Override
	public boolean isInFunEvent()
	{
		return atEvent || isInStartedTVTEvent() || isInStartedDMEvent() || isInStartedCTFEvent();
	}
	
	public boolean isInStartedTVTEvent()
	{
		return TvT.isStarted() && inEventTvT;
	}
	
	public boolean isRegisteredInTVTEvent()
	{
		return inEventTvT;
	}
	
	public boolean isInStartedDMEvent()
	{
		return DM.isStarted() && inEventDM;
	}
	
	public boolean isRegisteredInDMEvent()
	{
		return inEventDM;
	}
	
	public boolean isInStartedCTFEvent()
	{
		return CTF.isStarted() && inEventCTF;
	}
	
	public boolean isRegisteredInCTFEvent()
	{
		return inEventCTF;
	}
	
	/**
	 * @return If player is in Event, TvT, DM, CTF, Olympiad
	 */
	public boolean isRegisteredInFunEvent()
	{
		return atEvent || inEventTvT || inEventDM || inEventCTF || isInOlympiadMode();
	}
	
	// To Avoid Offensive skills when locked (during oly start or TODO other events start)
	/**
	 * Are player offensive skills locked.
	 * @return true, if successful
	 */
	public boolean arePlayerOffensiveSkillsLocked()
	{
		return isInOlympiadMode() && !isInOlympiadFight();
	}
	
	/**
	 * Returns true if cp update should be done, false if not.
	 * @param  barPixels the bar pixels
	 * @return           boolean
	 */
	private boolean needCpUpdate(final int barPixels)
	{
		final double currentCp = getCurrentCp();
		
		if (currentCp <= 1.0 || getMaxCp() < barPixels)
		{
			return true;
		}
		
		if (currentCp <= cpUpdateDecCheck || currentCp >= cpUpdateIncCheck)
		{
			if (currentCp == getMaxCp())
			{
				cpUpdateIncCheck = currentCp + 1;
				cpUpdateDecCheck = currentCp - cpUpdateInterval;
			}
			else
			{
				final double doubleMulti = currentCp / cpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				cpUpdateDecCheck = cpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				cpUpdateIncCheck = cpUpdateDecCheck + cpUpdateInterval;
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns true if mp update should be done, false if not.
	 * @param  barPixels the bar pixels
	 * @return           boolean
	 */
	private boolean needMpUpdate(final int barPixels)
	{
		final double currentMp = getCurrentMp();
		
		if (currentMp <= 1.0 || getMaxMp() < barPixels)
		{
			return true;
		}
		
		if (currentMp <= mpUpdateDecCheck || currentMp >= mpUpdateIncCheck)
		{
			if (currentMp == getMaxMp())
			{
				mpUpdateIncCheck = currentMp + 1;
				mpUpdateDecCheck = currentMp - mpUpdateInterval;
			}
			else
			{
				final double doubleMulti = currentMp / mpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				mpUpdateDecCheck = mpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				mpUpdateIncCheck = mpUpdateDecCheck + mpUpdateInterval;
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Send packet StatusUpdate with current HP,MP and CP to the L2PcInstance and only current HP, MP and Level to all other L2PcInstance of the Party.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send the Server->Client packet StatusUpdate with current HP, MP and CP to this L2PcInstance</li><BR>
	 * <li>Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other L2PcInstance of the Party</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND current HP and MP to all L2PcInstance of the statusListener</B></FONT><BR>
	 * <BR>
	 */
	@Override
	public void broadcastStatusUpdate()
	{
		// We mustn't send these informations to other players
		// Send the Server->Client packet StatusUpdate with current HP and MP to all L2PcInstance that must be informed of HP/MP updates of this L2PcInstance
		// super.broadcastStatusUpdate();
		
		// Send the Server->Client packet StatusUpdate with current HP, MP and CP to this L2PcInstance
		if (Config.FORCE_COMPLETE_STATUS_UPDATE)
		{
			StatusUpdate su = new StatusUpdate(this);
			sendPacket(su);
			su = null;
		}
		else
		{
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			su.addAttribute(StatusUpdate.CUR_CP, (int) getCurrentCp());
			su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
			sendPacket(su);
			su = null;
		}
		
		// Check if a party is in progress and party window update is usefull
		if (isInParty() && (needCpUpdate(352) || super.needHpUpdate(352) || needMpUpdate(352)))
		{
			if (Config.DEBUG)
			{
				LOGGER.debug("Send status for party window of " + getObjectId() + "(" + getName() + ") to his party. CP: " + getCurrentCp() + " HP: " + getCurrentHp() + " MP: " + getCurrentMp());
			}
			// Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other L2PcInstance of the Party
			PartySmallWindowUpdate update = new PartySmallWindowUpdate(this);
			getParty().broadcastToPartyMembers(this, update);
			update = null;
		}
		
		if (isInOlympiadMode())
		{
			// TODO: implement new OlympiadUserInfo
			for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
			{
				if (player.getOlympiadGameId() == getOlympiadGameId() && player.isInOlympiadFight())
				{
					if (Config.DEBUG)
					{
						LOGGER.debug("Send status for Olympia window of " + getObjectId() + "(" + getName() + ") to " + player.getObjectId() + "(" + player.getName() + "). CP: " + getCurrentCp() + " HP: " + getCurrentHp() + " MP: " + getCurrentMp());
					}
					player.sendPacket(new ExOlympiadUserInfo(this, 1));
				}
			}
			if (Olympiad.getInstance().getSpectators(olympiadGameId) != null && isInOlympiadFight())
			{
				for (final L2PcInstance spectator : Olympiad.getInstance().getSpectators(olympiadGameId))
				{
					if (spectator == null)
					{
						continue;
					}
					spectator.sendPacket(new ExOlympiadUserInfo(this, getOlympiadSide()));
				}
			}
		}
		if (isInDuel())
		{
			ExDuelUpdateUserInfo update = new ExDuelUpdateUserInfo(this);
			DuelManager.getInstance().broadcastToOppositTeam(this, update);
			update = null;
		}
	}
	
	public void updatePvPColor(final int pvpKillAmount)
	{
		if (Config.PVP_COLOR_SYSTEM_ENABLED)
		{
			// Check if the character has GM access and if so, let them be.
			if (isGM())
			{
				return;
			}
			
			if (pvpKillAmount >= Config.PVP_AMOUNT1 && pvpKillAmount < Config.PVP_AMOUNT2)
			{
				getAppearance().setNameColor(Config.NAME_COLOR_FOR_PVP_AMOUNT1);
			}
			else if (pvpKillAmount >= Config.PVP_AMOUNT2 && pvpKillAmount < Config.PVP_AMOUNT3)
			{
				getAppearance().setNameColor(Config.NAME_COLOR_FOR_PVP_AMOUNT2);
			}
			else if (pvpKillAmount >= Config.PVP_AMOUNT3 && pvpKillAmount < Config.PVP_AMOUNT4)
			{
				getAppearance().setNameColor(Config.NAME_COLOR_FOR_PVP_AMOUNT3);
			}
			else if (pvpKillAmount >= Config.PVP_AMOUNT4 && pvpKillAmount < Config.PVP_AMOUNT5)
			{
				getAppearance().setNameColor(Config.NAME_COLOR_FOR_PVP_AMOUNT4);
			}
			else if (pvpKillAmount >= Config.PVP_AMOUNT5)
			{
				getAppearance().setNameColor(Config.NAME_COLOR_FOR_PVP_AMOUNT5);
			}
		}
	}
	
	/**
	 * Update pk color.
	 * @param pkKillAmount the pk kill amount
	 */
	public void updatePkColor(final int pkKillAmount)
	{
		if (Config.PK_COLOR_SYSTEM_ENABLED)
		{
			// Check if the character has GM access and if so, let them be, like above.
			if (isGM())
			{
				return;
			}
			
			if (pkKillAmount >= Config.PK_AMOUNT1 && pkKillAmount < Config.PVP_AMOUNT2)
			{
				getAppearance().setTitleColor(Config.TITLE_COLOR_FOR_PK_AMOUNT1);
			}
			else if (pkKillAmount >= Config.PK_AMOUNT2 && pkKillAmount < Config.PVP_AMOUNT3)
			{
				getAppearance().setTitleColor(Config.TITLE_COLOR_FOR_PK_AMOUNT2);
			}
			else if (pkKillAmount >= Config.PK_AMOUNT3 && pkKillAmount < Config.PVP_AMOUNT4)
			{
				getAppearance().setTitleColor(Config.TITLE_COLOR_FOR_PK_AMOUNT3);
			}
			else if (pkKillAmount >= Config.PK_AMOUNT4 && pkKillAmount < Config.PVP_AMOUNT5)
			{
				getAppearance().setTitleColor(Config.TITLE_COLOR_FOR_PK_AMOUNT4);
			}
			else if (pkKillAmount >= Config.PK_AMOUNT5)
			{
				getAppearance().setTitleColor(Config.TITLE_COLOR_FOR_PK_AMOUNT5);
			}
		}
	}
	
	// Custom Pk Color System - End
	
	/**
	 * Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its knownPlayers.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Others L2PcInstance in the detection area of the L2PcInstance are identified in <B>_knownPlayers</B>. In order to inform other players of this L2PcInstance state modifications, server just need to go through knownPlayers to send Server->Client Packet<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client packet UserInfo to this L2PcInstance (Public and Private Data)</li>
	 * <li>Send a Server->Client packet CharInfo to all L2PcInstance in knownPlayers of the L2PcInstance (Public data only)</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : DON'T SEND UserInfo packet to other players instead of CharInfo packet. Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</B></FONT><BR>
	 * <BR>
	 */
	public final void broadcastUserInfo()
	{
		// Send a Server->Client packet UserInfo to this L2PcInstance
		sendPacket(new UserInfo(this));
		
		// Send a Server->Client packet CharInfo to all L2PcInstance in knownPlayers of the L2PcInstance
		if (Config.DEBUG)
		{
			LOGGER.debug("players to notify:" + getKnownList().getKnownPlayers().size() + " packet: [S] 03 CharInfo");
		}
		
		Broadcast.toKnownPlayers(this, new CharInfo(this));
	}
	
	/**
	 * Broadcast title info.
	 */
	public final void broadcastTitleInfo()
	{
		// Send a Server->Client packet UserInfo to this L2PcInstance
		sendPacket(new UserInfo(this));
		
		// Send a Server->Client packet TitleUpdate to all L2PcInstance in knownPlayers of the L2PcInstance
		if (Config.DEBUG)
		{
			LOGGER.debug("players to notify:" + getKnownList().getKnownPlayers().size() + " packet: [S] cc TitleUpdate");
		}
		
		Broadcast.toKnownPlayers(this, new TitleUpdate(this));
	}
	
	/**
	 * Return the Alliance Identifier of the L2PcInstance.<BR>
	 * <BR>
	 * @return the ally id
	 */
	public int getAllyId()
	{
		if (clan == null)
		{
			return 0;
		}
		return clan.getAllyId();
	}
	
	/**
	 * Gets the ally crest id.
	 * @return the ally crest id
	 */
	public int getAllyCrestId()
	{
		if (getClanId() == 0 || getClan() == null)
		{
			return 0;
		}
		if (getClan().getAllyId() == 0)
		{
			return 0;
		}
		return getClan().getAllyCrestId();
	}
	
	/**
	 * Manage Interact Task with another L2PcInstance.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If the private store is a STORE_PRIVATE_SELL, send a Server->Client PrivateBuyListSell packet to the L2PcInstance</li>
	 * <li>If the private store is a STORE_PRIVATE_BUY, send a Server->Client PrivateBuyListBuy packet to the L2PcInstance</li>
	 * <li>If the private store is a STORE_PRIVATE_MANUFACTURE, send a Server->Client RecipeShopSellList packet to the L2PcInstance</li><BR>
	 * <BR>
	 * @param target The L2Character targeted
	 */
	public void doInteract(final L2Character target)
	{
		if (target instanceof L2PcInstance)
		{
			L2PcInstance temp = (L2PcInstance) target;
			sendPacket(ActionFailed.STATIC_PACKET);
			
			if (temp.getPrivateStoreType() == STORE_PRIVATE_SELL || temp.getPrivateStoreType() == STORE_PRIVATE_PACKAGE_SELL)
			{
				sendPacket(new PrivateStoreListSell(this, temp));
			}
			else if (temp.getPrivateStoreType() == STORE_PRIVATE_BUY)
			{
				sendPacket(new PrivateStoreListBuy(this, temp));
			}
			else if (temp.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
			{
				sendPacket(new RecipeShopSellList(this, temp));
			}
			
			temp = null;
		}
		else
		{
			// interactTarget=null should never happen but one never knows ^^;
			if (target != null)
			{
				target.onAction(this);
			}
		}
	}
	
	/**
	 * Manage AutoLoot Task.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a System Message to the L2PcInstance : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
	 * <li>Add the Item to the L2PcInstance inventory</li>
	 * <li>Send a Server->Client packet InventoryUpdate to this L2PcInstance with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
	 * <li>Send a Server->Client packet StatusUpdate to this L2PcInstance with current weight</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT><BR>
	 * <BR>
	 * @param target The L2ItemInstance dropped
	 * @param item   the item
	 */
	public void doAutoLoot(final L2Attackable target, final L2Attackable.RewardItem item)
	{
		if (isInParty())
		{
			getParty().distributeItem(this, item, false, target);
		}
		else if (item.getItemId() == 57)
		{
			addAdena("AutoLoot", item.getCount(), target, true);
		}
		else
		{
			addItem("AutoLoot", item.getItemId(), item.getCount(), target, true);
		}
	}
	
	/**
	 * Manage Pickup Task.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client packet StopMove to this L2PcInstance</li>
	 * <li>Remove the L2ItemInstance from the world and send server->client GetItem packets</li>
	 * <li>Send a System Message to the L2PcInstance : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
	 * <li>Add the Item to the L2PcInstance inventory</li>
	 * <li>Send a Server->Client packet InventoryUpdate to this L2PcInstance with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
	 * <li>Send a Server->Client packet StatusUpdate to this L2PcInstance with current weight</li> <BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT><BR>
	 * <BR>
	 * @param object The L2ItemInstance to pick up
	 */
	protected void doPickupItem(final L2Object object)
	{
		if (isAlikeDead() || isFakeDeath())
		{
			return;
		}
		
		// Set the AI Intention to AI_INTENTION_IDLE
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		// Check if the L2Object to pick up is a L2ItemInstance
		if (!(object instanceof L2ItemInstance))
		{
			// dont try to pickup anything that is not an item :)
			LOGGER.warn(this + "trying to pickup wrong target." + getTarget());
			return;
		}
		
		L2ItemInstance target = (L2ItemInstance) object;
		
		// Send a Server->Client packet ActionFailed to this L2PcInstance
		sendPacket(ActionFailed.STATIC_PACKET);
		
		// Send a Server->Client packet StopMove to this L2PcInstance
		StopMove sm = new StopMove(this);
		if (Config.DEBUG)
		{
			LOGGER.debug("pickup pos: " + target.getX() + " " + target.getY() + " " + target.getZ());
		}
		sendPacket(sm);
		sm = null;
		
		synchronized (target)
		{
			// Check if the target to pick up is visible
			if (!target.isVisible())
			{
				// Send a Server->Client packet ActionFailed to this L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Like L2OFF you can't pickup items with private store opened
			if (getPrivateStoreType() != 0)
			{
				// Send a Server->Client packet ActionFailed to this L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (!target.getDropProtection().tryPickUp(this) && target.getItemId() != 8190 && target.getItemId() != 8689)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				final SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
				smsg.addItemName(target.getItemId());
				sendPacket(smsg);
				return;
			}
			if ((isInParty() && getParty().getLootDistribution() == L2Party.ITEM_LOOTER || !isInParty()) && !inventory.validateCapacity(target))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
				return;
			}
			if (isInvul() && !isGM())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
				smsg.addItemName(target.getItemId());
				sendPacket(smsg);
				smsg = null;
				return;
			}
			if (target.getOwnerId() != 0 && target.getOwnerId() != getObjectId() && !isInLooterParty(target.getOwnerId()))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				
				if (target.getItemId() == 57)
				{
					SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA);
					smsg.addNumber(target.getCount());
					sendPacket(smsg);
					smsg = null;
				}
				else if (target.getCount() > 1)
				{
					SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S);
					smsg.addItemName(target.getItemId());
					smsg.addNumber(target.getCount());
					sendPacket(smsg);
					smsg = null;
				}
				else
				{
					SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
					smsg.addItemName(target.getItemId());
					sendPacket(smsg);
					smsg = null;
				}
				return;
			}
			
			if (target.getItemId() == 57 && inventory.getAdena() == Integer.MAX_VALUE)
			{
				sendMessage("You have reached the maximum amount of adena, please spend or deposit the adena so you may continue obtaining adena.");
				return;
			}
			
			if (target.getItemLootShedule() != null && (target.getOwnerId() == getObjectId() || isInLooterParty(target.getOwnerId())))
			{
				target.resetOwnerTimer();
			}
			
			// Fixed it's not possible pick up the object if you exceed the maximum weight.
			if (inventory.getTotalWeight() + target.getItem().getWeight() * target.getCount() > getMaxLoad())
			{
				sendMessage("You have reached the maximun weight.");
				return;
			}
			
			// Remove the L2ItemInstance from the world and send server->client GetItem packets
			target.pickupMe(this);
			if (Config.SAVE_DROPPED_ITEM)
			{
				ItemsOnGroundManager.getInstance().removeObject(target);
			}
		}
		
		// Auto use herbs - pick up
		if (target.getItemType() == L2EtcItemType.HERB)
		{
			IItemHandler handler = ItemHandler.getInstance().getItemHandler(target.getItemId());
			if (handler == null)
			{
				LOGGER.debug("No item handler registered for item ID " + target.getItemId() + ".");
			}
			else
			{
				handler.useItem(this, target);
			}
			ItemTable.getInstance().destroyItem("Consume", target, this, null);
			handler = null;
		}
		// Cursed Weapons are not distributed
		else if (CursedWeaponsManager.getInstance().isCursed(target.getItemId()))
		{
			/*
			 * Lineage2.com: When a player that controls Akamanah acquires Zariche, the newly-acquired Zariche automatically disappeared, and the equipped Akamanah's level increases by 1. The same rules also apply in the opposite instance.
			 */
			addItem("Pickup", target, null, true);
		}
		else if (FortSiegeManager.getInstance().isCombat(target.getItemId()))
		{
			addItem("Pickup", target, null, true);
		}
		else
		{
			// if item is instance of L2ArmorType or L2WeaponType broadcast an "Attention" system message
			if (target.getItemType() instanceof L2ArmorType || target.getItemType() instanceof L2WeaponType || target.getItem() instanceof L2Armor || target.getItem() instanceof L2Weapon)
			{
				if (target.getEnchantLevel() > 0)
				{
					final SystemMessage msg = new SystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2_S3);
					msg.addString(getName());
					msg.addNumber(target.getEnchantLevel());
					msg.addItemName(target.getItemId());
					broadcastPacket(msg, 1400);
				}
				else
				{
					final SystemMessage msg = new SystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2);
					msg.addString(getName());
					msg.addItemName(target.getItemId());
					broadcastPacket(msg, 1400);
				}
			}
			
			// Check if a Party is in progress
			if (isInParty())
			{
				getParty().distributeItem(this, target);
			}
			else if (target.getItemId() == 57 && getInventory().getAdenaInstance() != null)
			{
				addAdena("Pickup", target.getCount(), null, true);
				ItemTable.getInstance().destroyItem("Pickup", target, this, null);
			}
			// Target is regular item
			else
			{
				addItem("Pickup", target, null, true);
				
				// Like L2OFF Auto-Equip arrows if player has a bow and player picks up arrows.
				if (target.getItem() != null && target.getItem().getItemType() == L2EtcItemType.ARROW)
				{
					checkAndEquipArrows();
				}
			}
		}
		target = null;
	}
	
	/**
	 * Set a target.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove the L2PcInstance from the statusListener of the old target if it was a L2Character</li>
	 * <li>Add the L2PcInstance to the statusListener of the new target if it's a L2Character</li>
	 * <li>Target the new L2Object (add the target to the L2PcInstance target, knownObject and L2PcInstance to knownObject of the L2Object)</li><BR>
	 * <BR>
	 * @param newTarget The L2Object to target
	 */
	@Override
	public void setTarget(L2Object newTarget)
	{
		// Check if the new target is visible
		if (newTarget != null && !newTarget.isVisible())
		{
			newTarget = null;
		}
		
		// Prevents /target exploiting
		if (newTarget != null)
		{
			if (!(newTarget instanceof L2PcInstance) || !isInParty() || !((L2PcInstance) newTarget).isInParty() || getParty().getPartyLeaderOID() != ((L2PcInstance) newTarget).getParty().getPartyLeaderOID())
			{
				if (Math.abs(newTarget.getZ() - getZ()) > Config.DIFFERENT_Z_NEW_MOVIE)
				{
					newTarget = null;
				}
			}
		}
		
		if (!isGM())
		{
			// Can't target and attack festival monsters if not participant
			if (newTarget instanceof L2FestivalMonsterInstance && !isFestivalParticipant())
			{
				newTarget = null;
			}
			else if (isInParty() && getParty().isInDimensionalRift())
			{
				final byte riftType = getParty().getDimensionalRift().getType();
				final byte riftRoom = getParty().getDimensionalRift().getCurrentRoom();
				
				if (newTarget != null && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(newTarget.getX(), newTarget.getY(), newTarget.getZ()))
				{
					newTarget = null;
				}
			}
		}
		
		// Get the current target
		L2Object oldTarget = getTarget();
		
		if (oldTarget != null)
		{
			if (oldTarget.equals(newTarget))
			{
				return; // no target change
			}
			
			// Remove the L2PcInstance from the statusListener of the old target if it was a L2Character
			if (oldTarget instanceof L2Character)
			{
				((L2Character) oldTarget).removeStatusListener(this);
			}
		}
		oldTarget = null;
		
		// Add the L2PcInstance to the statusListener of the new target if it's a L2Character
		if (newTarget != null && newTarget instanceof L2Character)
		{
			((L2Character) newTarget).addStatusListener(this);
			TargetSelected my = new TargetSelected(getObjectId(), newTarget.getObjectId(), getX(), getY(), getZ());
			
			// Send packet just to me and to party, not to any other that does not use the information
			if (!isInParty())
			{
				this.sendPacket(my);
			}
			else
			{
				party.broadcastToPartyMembers(my);
			}
			
			my = null;
		}
		
		// Target the new L2Object (add the target to the L2PcInstance target, knownObject and L2PcInstance to knownObject of the L2Object)
		super.setTarget(newTarget);
	}
	
	/**
	 * Return the active weapon instance (always equiped in the right hand).<BR>
	 * <BR>
	 * @return the active weapon instance
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}
	
	/**
	 * Return the active weapon item (always equiped in the right hand).<BR>
	 * <BR>
	 * @return the active weapon item
	 */
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		final L2ItemInstance weapon = getActiveWeaponInstance();
		
		if (weapon == null)
		{
			return getFistsWeaponItem();
		}
		
		return (L2Weapon) weapon.getItem();
	}
	
	/**
	 * Gets the chest armor instance.
	 * @return the chest armor instance
	 */
	public L2ItemInstance getChestArmorInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
	}
	
	/**
	 * Gets the legs armor instance.
	 * @return the legs armor instance
	 */
	public L2ItemInstance getLegsArmorInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
	}
	
	/**
	 * Gets the active chest armor item.
	 * @return the active chest armor item
	 */
	public L2Armor getActiveChestArmorItem()
	{
		final L2ItemInstance armor = getChestArmorInstance();
		
		if (armor == null)
		{
			return null;
		}
		
		return (L2Armor) armor.getItem();
	}
	
	/**
	 * Gets the active legs armor item.
	 * @return the active legs armor item
	 */
	public L2Armor getActiveLegsArmorItem()
	{
		final L2ItemInstance legs = getLegsArmorInstance();
		
		if (legs == null)
		{
			return null;
		}
		
		return (L2Armor) legs.getItem();
	}
	
	/**
	 * Checks if is wearing heavy armor.
	 * @return true, if is wearing heavy armor
	 */
	public boolean isWearingHeavyArmor()
	{
		final L2ItemInstance legs = getLegsArmorInstance();
		final L2ItemInstance armor = getChestArmorInstance();
		
		if (armor != null && legs != null)
		{
			if ((L2ArmorType) legs.getItemType() == L2ArmorType.HEAVY && (L2ArmorType) armor.getItemType() == L2ArmorType.HEAVY)
			{
				return true;
			}
		}
		if (armor != null)
		{
			if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR && (L2ArmorType) armor.getItemType() == L2ArmorType.HEAVY)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if is wearing light armor.
	 * @return true, if is wearing light armor
	 */
	public boolean isWearingLightArmor()
	{
		final L2ItemInstance legs = getLegsArmorInstance();
		final L2ItemInstance armor = getChestArmorInstance();
		
		if (armor != null && legs != null)
		{
			if ((L2ArmorType) legs.getItemType() == L2ArmorType.LIGHT && (L2ArmorType) armor.getItemType() == L2ArmorType.LIGHT)
			{
				return true;
			}
		}
		if (armor != null)
		{
			if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR && (L2ArmorType) armor.getItemType() == L2ArmorType.LIGHT)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if is wearing magic armor.
	 * @return true, if is wearing magic armor
	 */
	public boolean isWearingMagicArmor()
	{
		final L2ItemInstance legs = getLegsArmorInstance();
		final L2ItemInstance armor = getChestArmorInstance();
		
		if (armor != null && legs != null)
		{
			if ((L2ArmorType) legs.getItemType() == L2ArmorType.MAGIC && (L2ArmorType) armor.getItemType() == L2ArmorType.MAGIC)
			{
				return true;
			}
		}
		if (armor != null)
		{
			if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR && (L2ArmorType) armor.getItemType() == L2ArmorType.MAGIC)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if is wearing formal wear.
	 * @return true, if is wearing formal wear
	 */
	public boolean isWearingFormalWear()
	{
		return isWearingFormalWear;
	}
	
	/**
	 * Sets the checks if is wearing formal wear.
	 * @param value the new checks if is wearing formal wear
	 */
	public void setIsWearingFormalWear(final boolean value)
	{
		isWearingFormalWear = value;
	}
	
	/**
	 * Checks if is married.
	 * @return true, if is married
	 */
	public boolean isMarried()
	{
		return married;
	}
	
	/**
	 * Sets the married.
	 * @param state the new married
	 */
	public void setMarried(final boolean state)
	{
		married = state;
	}
	
	/**
	 * Married type.
	 * @return the int
	 */
	public int marriedType()
	{
		return marriedType;
	}
	
	/**
	 * Sets the married type.
	 * @param type the new married type
	 */
	public void setmarriedType(final int type)
	{
		marriedType = type;
	}
	
	/**
	 * Checks if is engage request.
	 * @return true, if is engage request
	 */
	public boolean isEngageRequest()
	{
		return engageRequest;
	}
	
	/**
	 * Sets the engage request.
	 * @param state    the state
	 * @param playerid the playerid
	 */
	public void setEngageRequest(final boolean state, final int playerid)
	{
		engageRequest = state;
		engageId = playerid;
	}
	
	/**
	 * Sets the mary request.
	 * @param state the new mary request
	 */
	public void setMaryRequest(final boolean state)
	{
		marryRequest = state;
	}
	
	/**
	 * Checks if is mary request.
	 * @return true, if is mary request
	 */
	public boolean isMaryRequest()
	{
		return marryRequest;
	}
	
	/**
	 * Sets the marry accepted.
	 * @param state the new marry accepted
	 */
	public void setMarryAccepted(final boolean state)
	{
		marryAccepted = state;
	}
	
	/**
	 * Checks if is marry accepted.
	 * @return true, if is marry accepted
	 */
	public boolean isMarryAccepted()
	{
		return marryAccepted;
	}
	
	/**
	 * Gets the engage id.
	 * @return the engage id
	 */
	public int getEngageId()
	{
		return engageId;
	}
	
	/**
	 * Gets the partner id.
	 * @return the partner id
	 */
	public int getPartnerId()
	{
		return partnerId;
	}
	
	/**
	 * Sets the partner id.
	 * @param partnerid the new partner id
	 */
	public void setPartnerId(final int partnerid)
	{
		partnerId = partnerid;
	}
	
	/**
	 * Gets the couple id.
	 * @return the couple id
	 */
	public int getCoupleId()
	{
		return coupleId;
	}
	
	/**
	 * Sets the couple id.
	 * @param coupleId the new couple id
	 */
	public void setCoupleId(final int coupleId)
	{
		this.coupleId = coupleId;
	}
	
	/**
	 * @param answer <BR>
	 *                   0 = no / cancel 1 = yes / accept
	 */
	public void engageAnswer(final int answer)
	{
		if (!engageRequest)
		{
			return;
		}
		else if (engageId == 0)
		{
			return;
		}
		else
		{
			L2PcInstance ptarget = (L2PcInstance) L2World.getInstance().findObject(engageId);
			setEngageRequest(false, 0);
			if (ptarget != null)
			{
				if (answer == 1)
				{
					CoupleManager.getInstance().createCouple(ptarget, L2PcInstance.this);
					ptarget.sendMessage("Request to Engage has been >ACCEPTED<");
				}
				else
				{
					ptarget.sendMessage("Request to Engage has been >DENIED<!");
				}
				
				ptarget = null;
			}
		}
	}
	
	/**
	 * Return the secondary weapon instance (always equiped in the left hand).<BR>
	 * <BR>
	 * @return the secondary weapon instance
	 */
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}
	
	/**
	 * Return the secondary weapon item (always equiped in the left hand) or the fists weapon.<BR>
	 * <BR>
	 * @return the secondary weapon item
	 */
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		L2ItemInstance weapon = getSecondaryWeaponInstance();
		
		if (weapon == null)
		{
			return getFistsWeaponItem();
		}
		
		final L2Item item = weapon.getItem();
		
		if (item instanceof L2Weapon)
		{
			return (L2Weapon) item;
		}
		
		weapon = null;
		return null;
	}
	
	/**
	 * Kill the L2Character, Apply Death Penalty, Manage gain/loss Karma and Item Drop.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Reduce the Experience of the L2PcInstance in function of the calculated Death Penalty</li>
	 * <li>If necessary, unsummon the Pet of the killed L2PcInstance</li>
	 * <li>Manage Karma gain for attacker and Karam loss for the killed L2PcInstance</li>
	 * <li>If the killed L2PcInstance has Karma, manage Drop Item</li>
	 * <li>Kill the L2PcInstance</li><BR>
	 * <BR>
	 * @param  killer The Player WHO killed this Player
	 * @return        true, if successful
	 */
	@Override
	public boolean doDie(final L2Character killer)
	{
		if (Config.TW_RESS_ON_DIE)
		{
			L2TownZone town = TownManager.getInstance().getTown(getX(), getY(), getZ());
			if (town != null && isinTownWar())
			{
				if (town.getTownId() == Config.TW_TOWN_ID && !Config.TW_ALL_TOWNS)
				{
					reviveRequest(this, null, false);
				}
				else if (Config.TW_ALL_TOWNS)
				{
					reviveRequest(this, null, false);
				}
			}
		}
		
		// Kill the L2PcInstance
		if (!super.doDie(killer))
		{
			return false;
		}
		
		Castle castle = null;
		if (getClan() != null)
		{
			castle = CastleManager.getInstance().getCastleByOwner(getClan());
			if (castle != null)
			{
				castle.destroyClanGate();
				castle = null;
			}
		}
		
		if (killer != null)
		{
			final L2PcInstance pk = killer.getActingPlayer();
			if (pk != null)
			{
				if (Config.ENABLE_PK_INFO)
				{
					doPkInfo(pk);
				}
				
				if (atEvent)
				{
					pk.kills.add(getName());
				}
				
				if (inEventTvT && pk.inEventTvT)
				{
					if (TvT.isTeleport() || TvT.isStarted())
					{
						if (!pk.teamNameTvT.equals(teamNameTvT))
						{
							final PlaySound ps = new PlaySound(0, "ItemSound.quest_itemget", 1, getObjectId(), getX(), getY(), getZ());
							countTvTdies++;
							pk.countTvTkills++;
							pk.setTitle("Kills: " + pk.countTvTkills);
							pk.sendPacket(ps);
							pk.broadcastUserInfo();
							TvT.setTeamKillsCount(pk.teamNameTvT, TvT.teamKillsCount(pk.teamNameTvT) + 1);
							pk.broadcastUserInfo();
						}
						else
						{
							pk.sendMessage("You are a teamkiller !!! Teamkills not counting.");
						}
						sendMessage("You will be revived and teleported to team spot in " + Config.TVT_REVIVE_DELAY / 1000 + " seconds!");
						ThreadPoolManager.getInstance().scheduleGeneral(() ->
						{
							teleToLocation(TvT.teamsX.get(TvT.tvtTeams.indexOf(teamNameTvT)) + Rnd.get(201) - 100, TvT.teamsY.get(TvT.tvtTeams.indexOf(teamNameTvT)) + Rnd.get(201) - 100, TvT.teamsZ.get(TvT.tvtTeams.indexOf(teamNameTvT)), false);
							doRevive();
						}, Config.TVT_REVIVE_DELAY);
					}
				}
				else if (inEventTvT)
				{
					if (TvT.isTeleport() || TvT.isStarted())
					{
						sendMessage("You will be revived and teleported to team spot in " + Config.TVT_REVIVE_DELAY / 1000 + " seconds!");
						ThreadPoolManager.getInstance().scheduleGeneral(() ->
						{
							teleToLocation(TvT.teamsX.get(TvT.tvtTeams.indexOf(teamNameTvT)), TvT.teamsY.get(TvT.tvtTeams.indexOf(teamNameTvT)), TvT.teamsZ.get(TvT.tvtTeams.indexOf(teamNameTvT)), false);
							doRevive();
							broadcastPacket(new SocialAction(getObjectId(), 15));
						}, Config.TVT_REVIVE_DELAY);
					}
				}
				else if (inEventCTF)
				{
					if (CTF.isTeleport() || CTF.isStarted())
					{
						sendMessage("You will be revived and teleported to team flag in 20 seconds!");
						if (haveFlagCTF)
						{
							removeCTFFlagOnDie();
						}
						ThreadPoolManager.getInstance().scheduleGeneral(() ->
						{
							teleToLocation(CTF.teamsX.get(CTF.ctfTeams.indexOf(teamNameCTF)), CTF.teamsY.get(CTF.ctfTeams.indexOf(teamNameCTF)), CTF.teamsZ.get(CTF.ctfTeams.indexOf(teamNameCTF)), false);
							doRevive();
						}, 20000);
					}
				}
				else if (inEventDM && pk.inEventDM)
				{
					if (DM.is_teleport() || DM.isStarted())
					{
						pk.countDMkills++;
						final PlaySound ps = new PlaySound(0, "ItemSound.quest_itemget", 1, getObjectId(), getX(), getY(), getZ());
						pk.setTitle("Kills: " + pk.countDMkills);
						pk.sendPacket(ps);
						pk.broadcastUserInfo();
						
						if (Config.DM_ENABLE_KILL_REWARD)
						{
							
							final L2Item reward = ItemTable.getInstance().getTemplate(Config.DM_KILL_REWARD_ID);
							pk.getInventory().addItem("DM Kill Reward", Config.DM_KILL_REWARD_ID, Config.DM_KILL_REWARD_AMOUNT, this, null);
							pk.sendMessage("You have earned " + Config.DM_KILL_REWARD_AMOUNT + " item(s) of ID " + reward.getName() + ".");
							
						}
						
						sendMessage("You will be revived and teleported to spot in 20 seconds!");
						ThreadPoolManager.getInstance().scheduleGeneral(() ->
						{
							final Location p_loc = DM.get_playersSpawnLocation();
							teleToLocation(p_loc.x, p_loc.y, p_loc.z, false);
							doRevive();
						}, Config.DM_REVIVE_DELAY);
					}
				}
				else if (inEventDM)
				{
					if (DM.is_teleport() || DM.isStarted())
					{
						sendMessage("You will be revived and teleported to spot in 20 seconds!");
						ThreadPoolManager.getInstance().scheduleGeneral(() ->
						{
							final Location players_loc = DM.get_playersSpawnLocation();
							teleToLocation(players_loc.x, players_loc.y, players_loc.z, false);
							doRevive();
						}, 20000);
					}
				}
			}
			
			// Clear resurrect xp calculation
			setExpBeforeDeath(0);
			
			if (isCursedWeaponEquiped())
			{
				CursedWeaponsManager.getInstance().drop(cursedWeaponEquipedId, killer);
			}
			else
			{
				if (pk == null || !pk.isCursedWeaponEquiped())
				{
					// if (getKarma() > 0)
					onDieDropItem(killer); // Check if any item should be dropped
					
					if (!(isInsideZone(ZONE_PVP) && !isInsideZone(ZONE_SIEGE)))
					{
						if (pk != null && pk.getClan() != null && getClan() != null && !isAcademyMember() && !pk.isAcademyMember() && clan.isAtWarWith(pk.getClanId()) && pk.getClan().isAtWarWith(clan.getClanId()))
						{
							if (getClan().getReputationScore() > 0)
							{
								pk.getClan().setReputationScore(((L2PcInstance) killer).getClan().getReputationScore() + 2, true);
								pk.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(pk.getClan())); // Update status to all members
							}
							if (pk.getClan().getReputationScore() > 0)
							{
								clan.setReputationScore(clan.getReputationScore() - 2, true);
								clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan)); // Update status to all members
							}
						}
						if (Config.ALT_GAME_DELEVEL)
						{
							// Reduce the Experience of the L2PcInstance in function of the calculated Death Penalty
							// NOTE: deathPenalty +- Exp will update karma
							if (getSkillLevel(L2Skill.SKILL_LUCKY) < 0 || getStat().getLevel() > 9)
							{
								deathPenalty(pk != null && getClan() != null && pk.getClan() != null && pk.getClan().isAtWarWith(getClanId()));
							}
						}
						else
						{
							onDieUpdateKarma(); // Update karma if delevel is not allowed
						}
					}
				}
			}
		}
		
		// Unsummon Cubics
		unsummonAllCubics();
		
		if (forceBuff != null)
		{
			abortCast();
		}
		
		for (L2Character character : getKnownList().getKnownCharacters())
		{
			if (character.getTarget() == this)
			{
				if (character.isCastingNow())
				{
					character.abortCast();
				}
			}
		}
		
		if (isInParty() && getParty().isInDimensionalRift())
		{
			getParty().getDimensionalRift().getDeadMemberList().add(this);
		}
		
		// calculate death penalty buff
		calculateDeathPenaltyBuffLevel(killer);
		
		stopRentPet();
		stopWaterTask();
		quakeSystem = 0;
		
		// leave war legend aura if enabled
		heroConsecutiveKillCount = 0;
		if (Config.WAR_LEGEND_AURA && !isHeroPlayer && isPVPHero)
		{
			setHeroAura(false);
			sendMessage("You leaved War Legend State");
		}
		
		// Refresh focus force like L2OFF
		sendPacket(new EtcStatusUpdate(this));
		
		// After dead mob check if the killer got a moving task actived
		if (killer instanceof L2PcInstance)
		{
			if (((L2PcInstance) killer).isMovingTaskDefined())
			{
				((L2PcInstance) killer).startMovingTask();
			}
		}
		
		return true;
	}
	
	/**
	 * Removes the ctf flag on die.
	 */
	public void removeCTFFlagOnDie()
	{
		CTF.ctfFlagsTaken.set(CTF.ctfTeams.indexOf(teamNameHaveFlagCTF), false);
		CTF.spawnFlag(teamNameHaveFlagCTF);
		CTF.removeFlagFromPlayer(this);
		broadcastUserInfo();
		haveFlagCTF = false;
		Announcements.getInstance().gameAnnounceToAll(CTF.getEventName() + "(CTF): " + teamNameHaveFlagCTF + "'s flag returned.");
	}
	
	/**
	 * On die drop item.
	 * @param killer the killer
	 */
	private void onDieDropItem(final L2Character killer)
	{
		if (atEvent || TvT.isStarted() && inEventTvT || DM.isStarted() && inEventDM || CTF.isStarted() && inEventCTF || killer == null)
		{
			return;
		}
		
		if (getKarma() <= 0 && killer instanceof L2PcInstance && ((L2PcInstance) killer).getClan() != null && getClan() != null && ((L2PcInstance) killer).getClan().isAtWarWith(getClanId()))
		{
			// || this.getClan().isAtWarWith(((L2PcInstance)killer).getClanId()))
			return;
		}
		
		if (!isInsideZone(ZONE_PVP) && (!isGM() || Config.KARMA_DROP_GM))
		{
			boolean isKarmaDrop = false;
			final boolean isKillerNpc = killer instanceof L2NpcInstance;
			final int pkLimit = Config.KARMA_PK_LIMIT;
			
			int dropEquip = 0;
			int dropEquipWeapon = 0;
			int dropItem = 0;
			int dropLimit = 0;
			int dropPercent = 0;
			
			if (getKarma() > 0 && getPkKills() >= pkLimit)
			{
				isKarmaDrop = true;
				dropPercent = Config.KARMA_RATE_DROP;
				dropEquip = Config.KARMA_RATE_DROP_EQUIP;
				dropEquipWeapon = Config.KARMA_RATE_DROP_EQUIP_WEAPON;
				dropItem = Config.KARMA_RATE_DROP_ITEM;
				dropLimit = Config.KARMA_DROP_LIMIT;
			}
			else if (isKillerNpc && getLevel() > 4 && !isFestivalParticipant())
			{
				dropPercent = Config.PLAYER_RATE_DROP;
				dropEquip = Config.PLAYER_RATE_DROP_EQUIP;
				dropEquipWeapon = Config.PLAYER_RATE_DROP_EQUIP_WEAPON;
				dropItem = Config.PLAYER_RATE_DROP_ITEM;
				dropLimit = Config.PLAYER_DROP_LIMIT;
			}
			
			int dropCount = 0;
			while (dropPercent > 0 && Rnd.get(100) < dropPercent && dropCount < dropLimit)
			{
				int itemDropPercent = 0;
				List<Integer> nonDroppableList = new ArrayList<>();
				List<Integer> nonDroppableListPet = new ArrayList<>();
				
				nonDroppableList = Config.KARMA_LIST_NONDROPPABLE_ITEMS;
				nonDroppableListPet = Config.KARMA_LIST_NONDROPPABLE_ITEMS;
				
				for (final L2ItemInstance itemDrop : getInventory().getItems())
				{
					// Don't drop
					if (itemDrop.isAugmented() || // Dont drop augmented items
						itemDrop.isShadowItem() || // Dont drop Shadow Items
						itemDrop.getItemId() == 57 || // Adena
						itemDrop.getItem().getType2() == L2Item.TYPE2_QUEST || // Quest Items
						nonDroppableList.contains(itemDrop.getItemId()) || // Item listed in the non droppable item list
						nonDroppableListPet.contains(itemDrop.getItemId()) || // Item listed in the non droppable pet item list
						getPet() != null && getPet().getControlItemId() == itemDrop.getItemId() // Control Item of active pet
					)
					{
						continue;
					}
					
					if (itemDrop.isEquipped())
					{
						// Set proper chance according to Item type of equipped Item
						itemDropPercent = itemDrop.getItem().getType2() == L2Item.TYPE2_WEAPON ? dropEquipWeapon : dropEquip;
						getInventory().unEquipItemInSlotAndRecord(itemDrop.getEquipSlot());
					}
					else
					{
						itemDropPercent = dropItem; // Item in inventory
					}
					
					// NOTE: Each time an item is dropped, the chance of another item being dropped gets lesser (dropCount * 2)
					if (Rnd.get(100) < itemDropPercent)
					{
						if (isKarmaDrop)
						{
							dropItem("DieDrop", itemDrop, killer, true, false);
							final String text = getName() + " has karma and dropped id = " + itemDrop.getItemId() + ", count = " + itemDrop.getCount();
							Log.add(text, "karma_dieDrop");
						}
						else
						{
							dropItem("DieDrop", itemDrop, killer, true, true);
							final String text = getName() + " dropped id = " + itemDrop.getItemId() + ", count = " + itemDrop.getCount();
							Log.add(text, "dieDrop");
						}
						
						dropCount++;
						break;
					}
				}
			}
		}
	}
	
	/**
	 * On die update karma.
	 */
	private void onDieUpdateKarma()
	{
		// Karma lose for server that does not allow delevel
		if (getKarma() > 0)
		{
			// this formula seems to work relatively well:
			// baseKarma * thisLVL * (thisLVL/100)
			// Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*lvlDiffMulti
			double karmaLost = Config.KARMA_LOST_BASE;
			karmaLost *= getLevel(); // multiply by char lvl
			karmaLost *= getLevel() / 100.0; // divide by 0.charLVL
			karmaLost = Math.round(karmaLost);
			if (karmaLost < 0)
			{
				karmaLost = 1;
			}
			
			// Decrease Karma of the L2PcInstance and Send it a Server->Client StatusUpdate packet with Karma and PvP Flag if necessary
			setKarma(getKarma() - (int) karmaLost);
		}
	}
	
	/**
	 * On kill update pvp karma.
	 * @param target the target
	 */
	public void onKillUpdatePvPKarma(final L2Character target)
	{
		if (target == null)
		{
			return;
		}
		
		if (!(target instanceof L2PlayableInstance))
		{
			return;
		}
		
		if (inEventCTF && CTF.isStarted() || inEventTvT && TvT.isStarted() || inEventDM && DM.isStarted())
		{
			return;
		}
		
		L2TownZone town = TownManager.getInstance().getTown(getX(), getY(), getZ());
		
		if (town != null && isinTownWar())
		{
			if (town.getTownId() == Config.TW_TOWN_ID && !Config.TW_ALL_TOWNS)
			{
				addItem("Town War kill", Config.TW_ITEM_ID, Config.TW_ITEM_AMOUNT, this, false);
				sendMessage("You received your prize for a town war kill!");
			}
			else if (Config.TW_ALL_TOWNS)
			{
				addItem("Town War kill", Config.TW_ITEM_ID, Config.TW_ITEM_AMOUNT, this, false);
				sendMessage("You received your prize for a town war kill!");
			}
		}
		
		if (isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().increaseKills(cursedWeaponEquipedId);
			// Custom message for time left
			// CursedWeapon cw = CursedWeaponsManager.getInstance().getCursedWeapon(_cursedWeaponEquipedId);
			// SystemMessage msg = new SystemMessage(SystemMessageId.THERE_IS_S1_HOUR_AND_S2_MINUTE_LEFT_OF_THE_FIXED_USAGE_TIME);
			// int timeLeftInHours = (int)(((cw.getTimeLeft()/60000)/60));
			// msg.addItemName(_cursedWeaponEquipedId);
			// msg.addNumber(timeLeftInHours);
			// sendPacket(msg);
			return;
		}
		
		L2PcInstance targetPlayer = null;
		
		if (target instanceof L2PcInstance)
		{
			targetPlayer = (L2PcInstance) target;
		}
		else if (target instanceof L2Summon)
		{
			targetPlayer = ((L2Summon) target).getOwner();
		}
		
		if (targetPlayer == null)
		{
			return; // Target player is null
		}
		
		if (targetPlayer == this)
		{
			targetPlayer = null;
			return; // Target player is self
		}
		
		if (isCursedWeaponEquiped())
		{
			CursedWeaponsManager.getInstance().increaseKills(cursedWeaponEquipedId);
			return;
		}
		
		// If in duel and you kill (only can kill l2summon), do nothing
		if (isInDuel() && targetPlayer.isInDuel())
		{
			return;
		}
		
		// If in Arena, do nothing
		if (isInsideZone(ZONE_PVP) || targetPlayer.isInsideZone(ZONE_PVP))
		{
			return;
		}
		
		// check anti-farm
		if (!checkAntiFarm(targetPlayer))
		{
			return;
		}
		
		if (Config.ANTI_FARM_SUMMON)
		{
			if (target instanceof L2SummonInstance)
			{
				return;
			}
		}
		
		// Check if it's pvp
		if (checkIfPvP(target) && targetPlayer.getPvpFlag() != 0 || isInsideZone(ZONE_PVP) && targetPlayer.isInsideZone(ZONE_PVP))
		{
			increasePvpKills();
		}
		else
		{
			// check about wars
			if (targetPlayer.getClan() != null && getClan() != null)
			{
				if (getClan().isAtWarWith(targetPlayer.getClanId()))
				{
					if (targetPlayer.getClan().isAtWarWith(getClanId()))
					{
						// 'Both way war' -> 'PvP Kill'
						increasePvpKills();
						if (target instanceof L2PcInstance && Config.ANNOUNCE_PVP_KILL)
						{
							Announcements.getInstance().announceToAll("Player " + getName() + " hunted Player " + target.getName());
						}
						else if (target instanceof L2PcInstance && Config.ANNOUNCE_ALL_KILL)
						{
							Announcements.getInstance().announceToAll("Player " + getName() + " killed Player " + target.getName());
						}
						addItemReward(targetPlayer);
						return;
					}
				}
			}
			
			// 'No war' or 'One way war' -> 'Normal PK'
			if (!(inEventTvT && TvT.isStarted()) || !(inEventCTF && CTF.isStarted()) || !(inEventDM && DM.isStarted()))
			{
				if (targetPlayer.getKarma() > 0) // Target player has karma
				{
					if (Config.KARMA_AWARD_PK_KILL)
					{
						increasePvpKills();
					}
					
					if (target instanceof L2PcInstance && Config.ANNOUNCE_PVP_KILL)
					{
						Announcements.getInstance().announceToAll("Player " + getName() + " hunted Player " + target.getName());
					}
				}
				else if (targetPlayer.getPvpFlag() == 0) // Target player doesn't have karma
				{
					increasePkKillsAndKarma(targetPlayer.getLevel());
					if (target instanceof L2PcInstance && Config.ANNOUNCE_PK_KILL)
					{
						Announcements.getInstance().announceToAll("Player " + getName() + " has assassinated Player " + target.getName());
					}
				}
			}
		}
		if (target instanceof L2PcInstance && Config.ANNOUNCE_ALL_KILL)
		{
			Announcements.getInstance().announceToAll("Player " + getName() + " killed Player " + target.getName());
		}
		
		if (inEventDM && DM.isStarted())
		{
			return;
		}
		
		if (targetPlayer.getObjectId() == lastKill)
		{
			count += 1;
		}
		else
		{
			count = 1;
			lastKill = targetPlayer.getObjectId();
		}
		
		if (Config.REWARD_PROTECT == 0 || count <= Config.REWARD_PROTECT)
		{
			addItemReward(targetPlayer);
		}
	}
	
	/**
	 * Check anti farm.
	 * @param  targetPlayer the target player
	 * @return              true, if successful
	 */
	private boolean checkAntiFarm(final L2PcInstance targetPlayer)
	{
		
		if (Config.ANTI_FARM_ENABLED)
		{
			
			// Anti FARM Clan - Ally
			if (Config.ANTI_FARM_CLAN_ALLY_ENABLED && getClanId() > 0 && targetPlayer.getClanId() > 0 && getClanId() == targetPlayer.getClanId() || getAllyId() > 0 && targetPlayer.getAllyId() > 0 && getAllyId() == targetPlayer.getAllyId())
			{
				sendMessage("Farm is punishable with Ban! Gm informed.");
				LOGGER.info("PVP POINT FARM ATTEMPT, " + getName() + " and " + targetPlayer.getName() + ". CLAN or ALLY.");
				return false;
			}
			
			// Anti FARM level player < 40
			if (Config.ANTI_FARM_LVL_DIFF_ENABLED && targetPlayer.getLevel() < Config.ANTI_FARM_MAX_LVL_DIFF)
			{
				sendMessage("Farm is punishable with Ban! Don't kill new players! Gm informed.");
				LOGGER.info("PVP POINT FARM ATTEMPT, " + getName() + " and " + targetPlayer.getName() + ". LVL DIFF.");
				return false;
			}
			
			// Anti FARM pdef < 300
			if (Config.ANTI_FARM_PDEF_DIFF_ENABLED && targetPlayer.getPDef(targetPlayer) < Config.ANTI_FARM_MAX_PDEF_DIFF)
			{
				sendMessage("Farm is punishable with Ban! Gm informed.");
				LOGGER.info("PVP POINT FARM ATTEMPT, " + getName() + " and " + targetPlayer.getName() + ". MAX PDEF DIFF.");
				return false;
			}
			
			// Anti FARM p atk < 300
			if (Config.ANTI_FARM_PATK_DIFF_ENABLED && targetPlayer.getPAtk(targetPlayer) < Config.ANTI_FARM_MAX_PATK_DIFF)
			{
				sendMessage("Farm is punishable with Ban! Gm informed.");
				LOGGER.info("PVP POINT FARM ATTEMPT, " + getName() + " and " + targetPlayer.getName() + ". MAX PATK DIFF.");
				return false;
			}
			
			// Anti FARM Party
			if (Config.ANTI_FARM_PARTY_ENABLED && getParty() != null && targetPlayer.getParty() != null && getParty().equals(targetPlayer.getParty()))
			{
				sendMessage("Farm is punishable with Ban! Gm informed.");
				LOGGER.info("PVP POINT FARM ATTEMPT, " + getName() + " and " + targetPlayer.getName() + ". SAME PARTY.");
				return false;
			}
			
			// Anti FARM same Ip
			if (Config.ANTI_FARM_IP_ENABLED)
			{
				
				if (getClient() != null && targetPlayer.getClient() != null)
				{
					final String ip1 = getClient().getConnection().getInetAddress().getHostAddress();
					final String ip2 = targetPlayer.getClient().getConnection().getInetAddress().getHostAddress();
					
					if (ip1.equals(ip2))
					{
						sendMessage("Farm is punishable with Ban! Gm informed.");
						LOGGER.info("PVP POINT FARM ATTEMPT: " + getName() + " and " + targetPlayer.getName() + ". SAME IP.");
						return false;
					}
				}
			}
			return true;
		}
		return true;
	}
	
	/**
	 * Adds the item reword.
	 * @param targetPlayer the target player
	 */
	private void addItemReward(final L2PcInstance targetPlayer)
	{
		// IP check
		if (targetPlayer.getClient() != null && targetPlayer.getClient().getConnection() != null)
		{
			if (targetPlayer.getClient().getConnection().getInetAddress() != getClient().getConnection().getInetAddress())
			{
				
				if (targetPlayer.getKarma() > 0 || targetPlayer.getPvpFlag() > 0) // killing target pk or in pvp
				{
					if (Config.PVP_REWARD_ENABLED)
					{
						final int item = Config.PVP_REWARD_ID;
						final L2Item reward = ItemTable.getInstance().getTemplate(item);
						
						final int amount = Config.PVP_REWARD_AMOUNT;
						
						getInventory().addItem("Winning PvP", Config.PVP_REWARD_ID, Config.PVP_REWARD_AMOUNT, this, null);
						sendMessage("You have earned " + amount + " item(s) of " + reward.getName() + ".");
					}
					
					if (!Config.FORCE_INVENTORY_UPDATE)
					{
						InventoryUpdate iu = new InventoryUpdate();
						iu.addItem(inventory.getItemByItemId(Config.PVP_REWARD_ID));
						sendPacket(iu);
						iu = null;
					}
				}
				else
				// target is not pk and not in pvp ---> PK KILL
				{
					if (Config.PK_REWARD_ENABLED)
					{
						final int item = Config.PK_REWARD_ID;
						final L2Item reward = ItemTable.getInstance().getTemplate(item);
						final int amount = Config.PK_REWARD_AMOUNT;
						getInventory().addItem("Winning PK", Config.PK_REWARD_ID, Config.PK_REWARD_AMOUNT, this, null);
						sendMessage("You have earned " + amount + " item(s) of " + reward.getName() + ".");
					}
					
					if (!Config.FORCE_INVENTORY_UPDATE)
					{
						InventoryUpdate iu = new InventoryUpdate();
						iu.addItem(inventory.getItemByItemId(Config.PK_REWARD_ID));
						sendPacket(iu);
						iu = null;
					}
				}
			}
			else
			{
				sendMessage("Farm is punishable with Ban! Don't kill your Box!");
				LOGGER.warn("PVP POINT FARM ATTEMPT: " + getName() + " and " + targetPlayer.getName() + ". SAME IP.");
			}
		}
	}
	
	/**
	 * Increase the pvp kills count and send the info to the player.
	 */
	public void increasePvpKills()
	{
		if (TvT.isStarted() && inEventTvT || DM.isStarted() && inEventDM || CTF.isStarted() && inEventCTF)
		{
			return;
		}
		
		// Add karma to attacker and increase its PK counter
		setPvpKills(getPvpKills() + 1);
		
		// Increase the kill count for a special hero aura
		heroConsecutiveKillCount++;
		
		// If heroConsecutiveKillCount == 30 give hero aura
		if (heroConsecutiveKillCount == Config.KILLS_TO_GET_WAR_LEGEND_AURA && Config.WAR_LEGEND_AURA)
		{
			setHeroAura(true);
			Announcements.getInstance().gameAnnounceToAll(getName() + " becames War Legend with " + Config.KILLS_TO_GET_WAR_LEGEND_AURA + " PvP!!");
			
		}
		
		if (Config.PVPEXPSP_SYSTEM)
		{
			addExpAndSp(Config.ADD_EXP, Config.ADD_SP);
			{
				sendMessage("Earned Exp & SP for a pvp kill");
			}
		}
		
		if (Config.PVP_PK_TITLE)
		{
			updateTitle();
		}
		
		// Update the character's name color if they reached any of the 5 PvP levels.
		updatePvPColor(getPvpKills());
		broadcastUserInfo();
		
		if (Config.ALLOW_QUAKE_SYSTEM)
		{
			QuakeSystem();
		}
		
		// Send a Server->Client UserInfo packet to attacker with its Karma and PK Counter
		sendPacket(new UserInfo(this));
	}
	
	/**
	 * Quake system.
	 */
	public void QuakeSystem()
	{
		quakeSystem++;
		switch (quakeSystem)
		{
			case 5:
				if (Config.ENABLE_ANTI_PVP_FARM_MSG)
				{
					final CreatureSay cs12 = new CreatureSay(0, 15, "", getName() + " 5 consecutive kill! Only Gm."); // 8D
					
					for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
					{
						if (player != null)
						{
							if (player.isOnline())
							{
								if (player.isGM())
								{
									player.sendPacket(cs12);
								}
							}
						}
					}
				}
				break;
			case 6:
				final CreatureSay cs = new CreatureSay(0, 15, "", getName() + " is Dominating!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
					{
						if (player.isOnline())
						{
							player.sendPacket(cs);
						}
					}
				}
				break;
			case 9:
				final CreatureSay cs2 = new CreatureSay(0, 15, "", getName() + " is on a Rampage!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
					{
						if (player.isOnline())
						{
							player.sendPacket(cs2);
						}
					}
				}
				break;
			case 14:
				final CreatureSay cs3 = new CreatureSay(0, 15, "", getName() + " is on a Killing Spree!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
					{
						if (player.isOnline())
						{
							player.sendPacket(cs3);
						}
					}
				}
				break;
			case 18:
				final CreatureSay cs4 = new CreatureSay(0, 15, "", getName() + " is on a Monster Kill!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
					{
						if (player.isOnline())
						{
							player.sendPacket(cs4);
						}
					}
				}
				break;
			case 22:
				final CreatureSay cs5 = new CreatureSay(0, 15, "", getName() + " is Unstoppable!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
					{
						if (player.isOnline())
						{
							player.sendPacket(cs5);
						}
					}
				}
				break;
			case 25:
				final CreatureSay cs6 = new CreatureSay(0, 15, "", getName() + " is on an Ultra Kill!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
					{
						if (player.isOnline())
						{
							player.sendPacket(cs6);
						}
					}
				}
				break;
			case 28:
				final CreatureSay cs7 = new CreatureSay(0, 15, "", getName() + " God Blessed!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
					{
						if (player.isOnline())
						{
							player.sendPacket(cs7);
						}
					}
				}
				break;
			case 32:
				final CreatureSay cs8 = new CreatureSay(0, 15, "", getName() + " is Wicked Sick!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
					{
						if (player.isOnline())
						{
							player.sendPacket(cs8);
						}
					}
				}
				break;
			case 35:
				final CreatureSay cs9 = new CreatureSay(0, 15, "", getName() + " is on a Ludricrous Kill!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
					{
						if (player.isOnline())
						{
							player.sendPacket(cs9);
						}
					}
				}
				break;
			case 40:
				final CreatureSay cs10 = new CreatureSay(0, 15, "", getName() + " is GodLike!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
					{
						if (player.isOnline())
						{
							player.sendPacket(cs10);
						}
					}
				}
		}
	}
	
	/**
	 * Get info on pk's from pk table.
	 * @param PlayerWhoKilled the player who killed
	 */
	public void doPkInfo(final L2PcInstance PlayerWhoKilled)
	{
		String killer = PlayerWhoKilled.getName();
		String killed = getName();
		int kills = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_PK_KILLS))
		{
			statement.setString(1, killer);
			statement.setString(2, killed);
			ResultSet rset = statement.executeQuery();
			
			if (rset.next())
			{
				kills = rset.getInt("kills");
			}
			
			DatabaseUtils.close(rset);
		}
		catch (SQLException e)
		{
			LOGGER.error("L2PCInstance.doPkInfo: Could not select from pkkills table ", e);
		}
		
		if (kills >= 1)
		{
			kills++;
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(UPDATE_PK_KILLS))
			{
				statement.setInt(1, kills);
				statement.setString(2, killer);
				statement.setString(3, killed);
				statement.executeUpdate();
			}
			catch (SQLException e)
			{
				LOGGER.error("Could not update in pkkills table ", e);
			}
			
			sendMessage("You have been killed " + kills + " times by " + PlayerWhoKilled.getName() + ".");
			PlayerWhoKilled.sendMessage("You have killed " + getName() + " " + kills + " times.");
		}
		else
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(INSERT_PK_KILLS))
			{
				statement.setString(1, killer);
				statement.setString(2, killed);
				statement.setInt(3, 1);
				statement.executeUpdate();
			}
			catch (SQLException e)
			{
				LOGGER.error("Could not insert in pkkills table ", e);
			}
			
			sendMessage("This is the first time you have been killed by " + PlayerWhoKilled.getName() + ".");
			PlayerWhoKilled.sendMessage("You have killed " + getName() + " for the first time.");
		}
		killer = null;
		killed = null;
	}
	
	/**
	 * Increase pk count, karma and send the info to the player.
	 * @param targLVL : level of the killed player
	 */
	public void increasePkKillsAndKarma(final int targLVL)
	{
		if (TvT.isStarted() && inEventTvT || DM.isStarted() && inEventDM || CTF.isStarted() && inEventCTF)
		{
			return;
		}
		
		final int baseKarma = Config.KARMA_MIN_KARMA;
		int newKarma = baseKarma;
		final int karmaLimit = Config.KARMA_MAX_KARMA;
		
		final int pkLVL = getLevel();
		final int pkPKCount = getPkKills();
		
		int lvlDiffMulti = 0;
		int pkCountMulti = 0;
		
		// Check if the attacker has a PK counter greater than 0
		if (pkPKCount > 0)
		{
			pkCountMulti = pkPKCount / 2;
		}
		else
		{
			pkCountMulti = 1;
		}
		
		if (pkCountMulti < 1)
		{
			pkCountMulti = 1;
		}
		
		// Calculate the level difference Multiplier between attacker and killed L2PcInstance
		if (pkLVL > targLVL)
		{
			lvlDiffMulti = pkLVL / targLVL;
		}
		else
		{
			lvlDiffMulti = 1;
		}
		
		if (lvlDiffMulti < 1)
		{
			lvlDiffMulti = 1;
		}
		
		// Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*lvlDiffMulti
		newKarma *= pkCountMulti;
		newKarma *= lvlDiffMulti;
		
		// Make sure newKarma is less than karmaLimit and higher than baseKarma
		if (newKarma < baseKarma)
		{
			newKarma = baseKarma;
		}
		
		if (newKarma > karmaLimit)
		{
			newKarma = karmaLimit;
		}
		
		// Fix to prevent overflow (=> karma has a max value of 2 147 483 647)
		if (getKarma() > Integer.MAX_VALUE - newKarma)
		{
			newKarma = Integer.MAX_VALUE - getKarma();
		}
		
		// Add karma to attacker and increase its PK counter
		setPkKills(getPkKills() + 1);
		setKarma(getKarma() + newKarma);
		
		if (Config.PVP_PK_TITLE)
		{
			updateTitle();
		}
		
		// Update the character's title color if they reached any of the 5 PK levels.
		updatePkColor(getPkKills());
		broadcastUserInfo();
		
		// Send a Server->Client UserInfo packet to attacker with its Karma and PK Counter
		sendPacket(new UserInfo(this));
	}
	
	/**
	 * Calculate karma lost.
	 * @param  exp the exp
	 * @return     the int
	 */
	public int calculateKarmaLost(final long exp)
	{
		// KARMA LOSS
		// When a PKer gets killed by another player or a L2MonsterInstance, it loses a certain amount of Karma based on their level.
		// this (with defaults) results in a level 1 losing about ~2 karma per death, and a lvl 70 loses about 11760 karma per death...
		// You lose karma as long as you were not in a pvp zone and you did not kill urself.
		// NOTE: exp for death (if delevel is allowed) is based on the players level
		
		long expGained = Math.abs(exp);
		expGained /= Config.KARMA_XP_DIVIDER;
		
		int karmaLost = 0;
		if (expGained > Integer.MAX_VALUE)
		{
			karmaLost = Integer.MAX_VALUE;
		}
		else
		{
			karmaLost = (int) expGained;
		}
		
		if (karmaLost < Config.KARMA_LOST_BASE)
		{
			karmaLost = Config.KARMA_LOST_BASE;
		}
		if (karmaLost > getKarma())
		{
			karmaLost = getKarma();
		}
		
		return karmaLost;
	}
	
	/**
	 * Update pvp status.
	 */
	public void updatePvPStatus()
	{
		if (TvT.isStarted() && inEventTvT || CTF.isStarted() && inEventCTF || DM.isStarted() && inEventDM)
		{
			return;
		}
		
		if (isInsideZone(ZONE_PVP))
		{
			return;
		}
		
		setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);
		
		if (getPvpFlag() == 0)
		{
			startPvPFlag();
		}
	}
	
	/**
	 * Update pvp status.
	 * @param target the target
	 */
	public void updatePvPStatus(final L2Character target)
	{
		L2PcInstance player_target = null;
		
		if (target instanceof L2PcInstance)
		{
			player_target = (L2PcInstance) target;
		}
		else if (target instanceof L2Summon)
		{
			player_target = ((L2Summon) target).getOwner();
		}
		
		if (player_target == null)
		{
			return;
		}
		
		if (TvT.isStarted() && inEventTvT && player_target.inEventTvT || DM.isStarted() && inEventDM && player_target.inEventDM || CTF.isStarted() && inEventCTF && player_target.inEventCTF)
		{
			return;
		}
		
		if (isInDuel() && player_target.getDuelId() == getDuelId())
		{
			return;
		}
		
		if ((!isInsideZone(ZONE_PVP) || !player_target.isInsideZone(ZONE_PVP)) && player_target.getKarma() == 0)
		{
			if (checkIfPvP(player_target))
			{
				setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_PVP_TIME);
			}
			else
			{
				setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);
			}
			if (getPvpFlag() == 0)
			{
				startPvPFlag();
			}
		}
		player_target = null;
	}
	
	/**
	 * Restore the specified % of experience this L2PcInstance has lost and sends a Server->Client StatusUpdate packet.<BR>
	 * <BR>
	 * @param restorePercent the restore percent
	 */
	public void restoreExp(final double restorePercent)
	{
		if (getExpBeforeDeath() > 0)
		{
			// Restore the specified % of lost experience.
			getStat().addExp((int) Math.round((getExpBeforeDeath() - getExp()) * restorePercent / 100));
			setExpBeforeDeath(0);
		}
	}
	
	/**
	 * Reduce the Experience (and level if necessary) of the L2PcInstance in function of the calculated Death Penalty.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate the Experience loss</li>
	 * <li>Set the value of expBeforeDeath</li>
	 * <li>Set the new Experience value of the L2PcInstance and Decrease its level if necessary</li>
	 * <li>Send a Server->Client StatusUpdate packet with its new Experience</li><BR>
	 * <BR>
	 * @param atwar the atwar
	 */
	public void deathPenalty(final boolean atwar)
	{
		// Get the level of the L2PcInstance
		final int lvl = getLevel();
		
		// The death steal you some Exp
		double percentLost = 4.0; // standart 4% (lvl>20)
		
		if (getLevel() < 20)
		{
			percentLost = 10.0;
		}
		else if (getLevel() >= 20 && getLevel() < 40)
		{
			percentLost = 7.0;
		}
		else if (getLevel() >= 40 && getLevel() < 75)
		{
			percentLost = 4.0;
		}
		else if (getLevel() >= 75 && getLevel() < 81)
		{
			percentLost = 2.0;
		}
		
		if (getKarma() > 0)
		{
			percentLost *= Config.RATE_KARMA_EXP_LOST;
		}
		
		if (isFestivalParticipant() || atwar || isInsideZone(ZONE_SIEGE))
		{
			percentLost /= 4.0;
		}
		
		// Calculate the Experience loss
		long lostExp = 0;
		if (!atEvent && !(inEventTvT && TvT.isStarted()) && !(inEventDM && DM.isStarted()) && !(inEventCTF && CTF.isStarted()))
		{
			final byte maxLvl = ExperienceData.getInstance().getMaxLevel();
			if (lvl < maxLvl)
			{
				lostExp = Math.round((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost / 100);
			}
			else
			{
				lostExp = Math.round((getStat().getExpForLevel(maxLvl) - getStat().getExpForLevel(maxLvl - 1)) * percentLost / 100);
			}
		}
		// Get the Experience before applying penalty
		setExpBeforeDeath(getExp());
		
		if (getCharmOfCourage())
		{
			if (getSiegeState() > 0 && isInsideZone(ZONE_SIEGE))
			{
				lostExp = 0;
			}
			setCharmOfCourage(false);
		}
		
		if (Config.DEBUG)
		{
			LOGGER.debug(getName() + " died and lost " + lostExp + " experience.");
		}
		
		// Set the new Experience value of the L2PcInstance
		getStat().addExp(-lostExp);
	}
	
	/**
	 * Manage the increase level task of a L2PcInstance (Max MP, Max MP, Recommandation, Expertise and beginner skills...).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client System Message to the L2PcInstance : YOU_INCREASED_YOUR_LEVEL</li>
	 * <li>Send a Server->Client packet StatusUpdate to the L2PcInstance with new LEVEL, MAX_HP and MAX_MP</li>
	 * <li>Set the current HP and MP of the L2PcInstance, Launch/Stop a HP/MP/CP Regeneration Task and send StatusUpdate packet to all other L2PcInstance to inform (exclusive broadcast)</li>
	 * <li>Recalculate the party level</li>
	 * <li>Recalculate the number of Recommandation that the L2PcInstance can give</li>
	 * <li>Give Expertise skill of this level and remove beginner Lucky skill</li><BR>
	 * <BR>
	 */
	public void increaseLevel()
	{
		// Set the current HP and MP of the L2Character, Launch/Stop a HP/MP/CP Regeneration Task and send StatusUpdate packet to all other L2PcInstance to inform (exclusive broadcast)
		setCurrentHpMp(getMaxHp(), getMaxMp());
		setCurrentCp(getMaxCp());
	}
	
	/**
	 * Stop the HP/MP/CP Regeneration task.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the RegenActive flag to False</li>
	 * <li>Stop the HP/MP/CP Regeneration task</li><BR>
	 * <BR>
	 */
	public void stopAllTimers()
	{
		stopHpMpRegeneration();
		stopWarnUserTakeBreak();
		stopWaterTask();
		stopRentPet();
		stopPvpRegTask();
		stopPunishTask(true);
		quakeSystem = 0;
	}
	
	/**
	 * Return the L2Summon of the L2PcInstance or null.<BR>
	 * <BR>
	 * @return the pet
	 */
	@Override
	public L2Summon getPet()
	{
		return summon;
	}
	
	/**
	 * Set the L2Summon of the L2PcInstance.<BR>
	 * <BR>
	 * @param summon the new pet
	 */
	public void setPet(final L2Summon summon)
	{
		ObjectData.get(PlayerHolder.class, this).setSummon(summon);
		this.summon = summon;
	}
	
	/**
	 * Return the L2Summon of the L2PcInstance or null.<BR>
	 * <BR>
	 * @return the trained beast
	 */
	public L2TamedBeastInstance getTrainedBeast()
	{
		return tamedBeast;
	}
	
	/**
	 * Set the L2Summon of the L2PcInstance.<BR>
	 * <BR>
	 * @param tamedBeast the new trained beast
	 */
	public void setTrainedBeast(final L2TamedBeastInstance tamedBeast)
	{
		this.tamedBeast = tamedBeast;
	}
	
	/**
	 * Return the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR>
	 * <BR>
	 * @return the request
	 */
	public L2Request getRequest()
	{
		return request;
	}
	
	/**
	 * Set the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR>
	 * <BR>
	 * @param requester the new active requester
	 */
	public synchronized void setActiveRequester(final L2PcInstance requester)
	{
		activeRequester = requester;
	}
	
	/**
	 * Return the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR>
	 * <BR>
	 * @return the active requester
	 */
	public synchronized L2PcInstance getActiveRequester()
	{
		final L2PcInstance requester = activeRequester;
		if (requester != null)
		{
			if (requester.isRequestExpired() && activeTradeList == null)
			{
				activeRequester = null;
			}
		}
		return activeRequester;
	}
	
	/**
	 * Return True if a transaction is in progress.<BR>
	 * <BR>
	 * @return true, if is processing request
	 */
	public boolean isProcessingRequest()
	{
		return activeRequester != null || requestExpireTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if a transaction is in progress.<BR>
	 * <BR>
	 * @return true, if is processing transaction
	 */
	public boolean isProcessingTransaction()
	{
		return activeRequester != null || activeTradeList != null || requestExpireTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Select the Warehouse to be used in next activity.<BR>
	 * <BR>
	 * @param partner the partner
	 */
	public void onTransactionRequest(final L2PcInstance partner)
	{
		requestExpireTime = GameTimeController.getGameTicks() + REQUEST_TIMEOUT * GameTimeController.TICKS_PER_SECOND;
		if (partner != null)
		{
			partner.setActiveRequester(this);
		}
	}
	
	/**
	 * Select the Warehouse to be used in next activity.<BR>
	 * <BR>
	 */
	public void onTransactionResponse()
	{
		requestExpireTime = 0;
	}
	
	/**
	 * Select the Warehouse to be used in next activity.<BR>
	 * <BR>
	 * @param warehouse the new active warehouse
	 */
	public void setActiveWarehouse(final ItemContainer warehouse)
	{
		activeWarehouse = warehouse;
	}
	
	/**
	 * Return active Warehouse.<BR>
	 * <BR>
	 * @return the active warehouse
	 */
	public ItemContainer getActiveWarehouse()
	{
		return activeWarehouse;
	}
	
	/**
	 * Select the TradeList to be used in next activity.<BR>
	 * <BR>
	 * @param tradeList the new active trade list
	 */
	public void setActiveTradeList(final TradeList tradeList)
	{
		activeTradeList = tradeList;
	}
	
	/**
	 * Return active TradeList.<BR>
	 * <BR>
	 * @return the active trade list
	 */
	public TradeList getActiveTradeList()
	{
		return activeTradeList;
	}
	
	/**
	 * On trade start.
	 * @param partner the partner
	 */
	public void onTradeStart(final L2PcInstance partner)
	{
		activeTradeList = new TradeList(this);
		activeTradeList.setPartner(partner);
		
		SystemMessage msg = new SystemMessage(SystemMessageId.BEGIN_TRADE_WITH_S1);
		msg.addString(partner.getName());
		sendPacket(msg);
		sendPacket(new TradeStart(this));
		msg = null;
	}
	
	/**
	 * On trade confirm.
	 * @param partner the partner
	 */
	public void onTradeConfirm(final L2PcInstance partner)
	{
		SystemMessage msg = new SystemMessage(SystemMessageId.S1_CONFIRMED_TRADE);
		msg.addString(partner.getName());
		sendPacket(msg);
		msg = null;
		partner.sendPacket(TradePressOwnOk.STATIC_PACKET);
		sendPacket(TradePressOtherOk.STATIC_PACKET);
	}
	
	/**
	 * On trade cancel.
	 * @param partner the partner
	 */
	public void onTradeCancel(final L2PcInstance partner)
	{
		if (activeTradeList == null)
		{
			return;
		}
		
		activeTradeList.lock();
		activeTradeList = null;
		
		sendPacket(new SendTradeDone(0));
		SystemMessage msg = new SystemMessage(SystemMessageId.S1_CANCELED_TRADE);
		msg.addString(partner.getName());
		sendPacket(msg);
		msg = null;
	}
	
	/**
	 * On trade finish.
	 * @param successfull the successfull
	 */
	public void onTradeFinish(final boolean successfull)
	{
		activeTradeList = null;
		sendPacket(new SendTradeDone(1));
		if (successfull)
		{
			sendPacket(new SystemMessage(SystemMessageId.TRADE_SUCCESSFUL));
		}
	}
	
	/**
	 * Start trade.
	 * @param partner the partner
	 */
	public void startTrade(final L2PcInstance partner)
	{
		onTradeStart(partner);
		partner.onTradeStart(this);
	}
	
	/**
	 * Cancel active trade.
	 */
	public void cancelActiveTrade()
	{
		if (activeTradeList == null)
		{
			return;
		}
		
		L2PcInstance partner = activeTradeList.getPartner();
		if (partner != null)
		{
			partner.onTradeCancel(this);
			partner = null;
		}
		onTradeCancel(this);
	}
	
	/**
	 * Return the createList object of the L2PcInstance.<BR>
	 * <BR>
	 * @return the creates the list
	 */
	public L2ManufactureList getCreateList()
	{
		return createList;
	}
	
	/**
	 * Set the createList object of the L2PcInstance.<BR>
	 * <BR>
	 * @param x the new creates the list
	 */
	public void setCreateList(final L2ManufactureList x)
	{
		createList = x;
	}
	
	/**
	 * Return the sellList object of the L2PcInstance.<BR>
	 * <BR>
	 * @return the sell list
	 */
	public TradeList getSellList()
	{
		if (sellList == null)
		{
			sellList = new TradeList(this);
		}
		return sellList;
	}
	
	/**
	 * Return the buyList object of the L2PcInstance.<BR>
	 * <BR>
	 * @return the buy list
	 */
	public TradeList getBuyList()
	{
		if (buyList == null)
		{
			buyList = new TradeList(this);
		}
		return buyList;
	}
	
	/**
	 * Set the Private Store type of the L2PcInstance.<BR>
	 * <BR>
	 * <B><U> Values </U> :</B><BR>
	 * <li>0 : STORE_PRIVATE_NONE</li>
	 * <li>1 : STORE_PRIVATE_SELL</li>
	 * <li>2 : STORE_PRIVATE_SELL_MANAGE</li>
	 * <li>3 : STORE_PRIVATE_BUY</li>
	 * <li>4 : STORE_PRIVATE_BUY_MANAGE</li>
	 * <li>5 : STORE_PRIVATE_MANUFACTURE</li>
	 * @param type the new private store type
	 */
	public void setPrivateStoreType(int type)
	{
		privatestore = type;
		
		if (privatestore == STORE_PRIVATE_NONE && (getClient() == null || isInOfflineMode()))
		{
			this.store();
			if (Config.OFFLINE_DISCONNECT_FINISHED)
			{
				deleteMe();
				ObjectData.get(PlayerHolder.class, this).setOffline(false);
				if (getClient() != null)
				{
					getClient().setActiveChar(null); // prevent deleteMe from being called a second time on disconnection
				}
			}
		}
	}
	
	/**
	 * Return the Private Store type of the L2PcInstance.<BR>
	 * <BR>
	 * <B><U> Values </U> :</B><BR>
	 * <BR>
	 * <li>0 : STORE_PRIVATE_NONE</li>
	 * <li>1 : STORE_PRIVATE_SELL</li>
	 * <li>2 : sellmanage</li><BR>
	 * <li>3 : STORE_PRIVATE_BUY</li><BR>
	 * <li>4 : buymanage</li><BR>
	 * <li>5 : STORE_PRIVATE_MANUFACTURE</li><BR>
	 * @return the private store type
	 */
	public int getPrivateStoreType()
	{
		return privatestore;
	}
	
	/**
	 * Set the skillLearningClassId object of the L2PcInstance.<BR>
	 * <BR>
	 * @param classId the new skill learning class id
	 */
	public void setSkillLearningClassId(final ClassId classId)
	{
		skillLearningClassId = classId;
	}
	
	/**
	 * Return the skillLearningClassId object of the L2PcInstance.<BR>
	 * <BR>
	 * @return the skill learning class id
	 */
	public ClassId getSkillLearningClassId()
	{
		return skillLearningClassId;
	}
	
	/**
	 * Set the clan object, clanId, clanLeader Flag and title of the L2PcInstance.<BR>
	 * <BR>
	 * @param clan the new clan
	 */
	public void setClan(final L2Clan clan)
	{
		this.clan = clan;
		setTitle("");
		
		if (clan == null)
		{
			clanId = 0;
			clanPrivileges = 0;
			pledgeType = 0;
			powerGrade = 0;
			lvlJoinedAcademy = 0;
			apprentice = 0;
			sponsor = 0;
			return;
		}
		
		if (!clan.isMember(getName()))
		{
			// char has been kicked from clan
			setClan(null);
			return;
		}
		
		clanId = clan.getClanId();
		
		// Add clan leader skills if clanleader
		if (isClanLeader() && clan.getLevel() >= 4)
		{
			addClanLeaderSkills(true);
		}
		else
		{
			addClanLeaderSkills(false);
		}
		
	}
	
	/**
	 * Return the clan object of the L2PcInstance.<BR>
	 * <BR>
	 * @return the clan
	 */
	public L2Clan getClan()
	{
		return clan;
	}
	
	/**
	 * Return True if the L2PcInstance is the leader of its clan.<BR>
	 * <BR>
	 * @return true, if is clan leader
	 */
	public boolean isClanLeader()
	{
		if (getClan() == null)
		{
			return false;
		}
		return getObjectId() == getClan().getLeaderId();
	}
	
	/**
	 * Reduce the number of arrows owned by the L2PcInstance and send it Server->Client Packet InventoryUpdate or ItemList (to unequip if the last arrow was consummed).<BR>
	 * <BR>
	 */
	@Override
	protected void reduceArrowCount()
	{
		L2ItemInstance arrows = getInventory().destroyItem("Consume", getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1, this, null);
		
		if (Config.DEBUG)
		{
			LOGGER.debug("arrow count:" + (arrows == null ? 0 : arrows.getCount()));
		}
		
		if (arrows == null || arrows.getCount() == 0)
		{
			getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
			arrowItem = null;
			
			if (Config.DEBUG)
			{
				LOGGER.debug("removed arrows count");
			}
			
			sendPacket(new ItemList(this, false));
		}
		else
		{
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				final InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(arrows);
				sendPacket(iu);
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
			
			arrows = null;
		}
	}
	
	/**
	 * Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True.<BR>
	 * <BR>
	 * @return true, if successful
	 */
	@Override
	protected boolean checkAndEquipArrows()
	{
		// Check if nothing is equiped in left hand
		if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
		{
			// Get the L2ItemInstance of the arrows needed for this bow
			arrowItem = getInventory().findArrowForBow(getActiveWeaponItem());
			
			if (arrowItem != null)
			{
				// Equip arrows needed in left hand
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, arrowItem);
				
				// Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipement
				final ItemList il = new ItemList(this, false);
				sendPacket(il);
			}
		}
		else
		{
			// Get the L2ItemInstance of arrows equiped in left hand
			arrowItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}
		
		return arrowItem != null;
	}
	
	/**
	 * Disarm the player's weapon and shield.<BR>
	 * <BR>
	 * @return true, if successful
	 */
	public boolean disarmWeapons()
	{
		// Don't allow disarming a cursed weapon
		if (isCursedWeaponEquiped() && !getAccessLevel().isGm())
		{
			return false;
		}
		
		// Unequip the weapon
		L2ItemInstance wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (wpn == null)
		{
			wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
		}
		
		if (wpn != null)
		{
			if (wpn.isWear())
			{
				return false;
			}
			
			// Remove augementation boni on unequip
			if (wpn.isAugmented())
			{
				wpn.getAugmentation().removeBoni(this);
			}
			
			L2ItemInstance[] unequiped = getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (final L2ItemInstance element : unequiped)
			{
				iu.addModifiedItem(element);
			}
			sendPacket(iu);
			iu = null;
			
			abortAttack();
			broadcastUserInfo();
			
			// this can be 0 if the user pressed the right mousebutton twice very fast
			if (unequiped.length > 0)
			{
				SystemMessage sm = null;
				if (unequiped[0].getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(unequiped[0].getEnchantLevel());
					sm.addItemName(unequiped[0].getItemId());
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(unequiped[0].getItemId());
				}
				sendPacket(sm);
				sm = null;
			}
			wpn = null;
			unequiped = null;
		}
		
		// Unequip the shield
		L2ItemInstance sld = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (sld != null)
		{
			if (sld.isWear())
			{
				return false;
			}
			
			L2ItemInstance[] unequiped = getInventory().unEquipItemInBodySlotAndRecord(sld.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (final L2ItemInstance element : unequiped)
			{
				iu.addModifiedItem(element);
			}
			sendPacket(iu);
			iu = null;
			
			abortAttack();
			broadcastUserInfo();
			
			// this can be 0 if the user pressed the right mousebutton twice very fast
			if (unequiped.length > 0)
			{
				SystemMessage sm = null;
				if (unequiped[0].getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(unequiped[0].getEnchantLevel());
					sm.addItemName(unequiped[0].getItemId());
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(unequiped[0].getItemId());
				}
				sendPacket(sm);
				sm = null;
			}
			sld = null;
			unequiped = null;
		}
		return true;
	}
	
	/**
	 * Return True if the L2PcInstance use a dual weapon.<BR>
	 * <BR>
	 * @return true, if is using dual weapon
	 */
	@Override
	public boolean isUsingDualWeapon()
	{
		final L2Weapon weaponItem = getActiveWeaponItem();
		if (weaponItem == null)
		{
			return false;
		}
		
		if (weaponItem.getItemType() == L2WeaponType.DUAL)
		{
			return true;
		}
		else if (weaponItem.getItemType() == L2WeaponType.DUALFIST)
		{
			return true;
		}
		else if (weaponItem.getItemId() == 248)
		{
			return true;
		}
		else if (weaponItem.getItemId() == 252)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Sets the uptime.
	 * @param time the new uptime
	 */
	public void setUptime(final long time)
	{
		uptime = time;
	}
	
	/**
	 * Gets the uptime.
	 * @return the uptime
	 */
	public long getUptime()
	{
		return System.currentTimeMillis() - uptime;
	}
	
	/**
	 * Return True if the L2PcInstance is invulnerable.<BR>
	 * <BR>
	 * @return true, if is invul
	 */
	@Override
	public boolean isInvul()
	{
		return isInvul || isTeleporting || protectEndTime > GameTimeController.getGameTicks() || teleportProtectEndTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if the L2PcInstance has a Party in progress.<BR>
	 * <BR>
	 * @return true, if is in party
	 */
	@Override
	public boolean isInParty()
	{
		return party != null;
	}
	
	/**
	 * Set the party object of the L2PcInstance (without joining it).<BR>
	 * <BR>
	 * @param party the new party
	 */
	public void setParty(final L2Party party)
	{
		this.party = party;
	}
	
	/**
	 * Set the party object of the L2PcInstance AND join it.<BR>
	 * <BR>
	 * @param party the party
	 */
	public void joinParty(final L2Party party)
	{
		if (party == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (party.getMemberCount() == 9)
		{
			sendPacket(new SystemMessage(SystemMessageId.PARTY_FULL));
			return;
		}
		
		if (party.getPartyMembers().contains(this))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (party.getMemberCount() < 9)
		{
			// First set the party otherwise this wouldn't be considered
			// as in a party into the L2Character.updateEffectIcons() call.
			this.party = party;
			party.addPartyMember(this);
		}
	}
	
	/**
	 * Return true if the L2PcInstance is a GM.<BR>
	 * <BR>
	 * @return true, if is gM
	 */
	public boolean isGM()
	{
		return getAccessLevel().isGm();
	}
	
	/**
	 * Manage the Leave Party task of the L2PcInstance.<BR>
	 * <BR>
	 */
	public void leaveParty()
	{
		if (isInParty())
		{
			party.removePartyMember(this);
			party = null;
		}
	}
	
	/**
	 * Return the party object of the L2PcInstance.<BR>
	 * <BR>
	 * @return the party
	 */
	@Override
	public L2Party getParty()
	{
		return party;
	}
	
	/**
	 * Set the isGm Flag of the L2PcInstance.<BR>
	 * <BR>
	 * @param first_log the new first LOGGER
	 */
	// public void setIsGM(boolean status)
	// {
	// isGm = status;
	// }
	
	public void setFirstLog(final int first_log)
	{
		isFirstLog = false;
		if (first_log == 1)
		{
			isFirstLog = true;
		}
	}
	
	/**
	 * Sets the first LOGGER.
	 * @param first_log the new first LOGGER
	 */
	public void setFirstLog(final boolean first_log)
	{
		isFirstLog = first_log;
	}
	
	/**
	 * Gets the first LOGGER.
	 * @return the first LOGGER
	 */
	public boolean getFirstLog()
	{
		return isFirstLog;
	}
	
	/**
	 * Manage a cancel cast task for the L2PcInstance.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the Intention of the AI to AI_INTENTION_IDLE</li>
	 * <li>Enable all skills (set allSkillsDisabled to False)</li>
	 * <li>Send a Server->Client Packet MagicSkillCanceld to the L2PcInstance and all L2PcInstance in the knownPlayers of the L2Character (broadcast)</li><BR>
	 * <BR>
	 */
	public void cancelCastMagic()
	{
		// Set the Intention of the AI to AI_INTENTION_IDLE
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		// Enable all skills (set allSkillsDisabled to False)
		enableAllSkills();
		
		// Send a Server->Client Packet MagicSkillCanceld to the L2PcInstance and all L2PcInstance in the knownPlayers of the L2Character (broadcast)
		MagicSkillCanceld msc = new MagicSkillCanceld(getObjectId());
		
		// Broadcast the packet to self and known players.
		Broadcast.toSelfAndKnownPlayersInRadius(this, msc, 810000/* 900 */);
		msc = null;
	}
	
	/**
	 * Set the accessLevel of the L2PcInstance.<BR>
	 * <BR>
	 * @param level the new access level
	 */
	public void setAccessLevel(int level)
	{
		if (level > 0)
		{
			LOGGER.warn(getName() + " logs in game with AccessLevel " + level + ".");
			Log.add(getName() + " logs in game with Accesslevel " + level, "log/gm_login/", getName());
		}
		
		AccessLevel accessLevel = AccessLevels.getInstance().getAccessLevel(level);
		
		if (accessLevel == null)
		{
			if (level < 0)
			{
				AccessLevels.getInstance().addBanAccessLevel(level);
				playerAccessLevel = AccessLevels.getInstance().getAccessLevel(level);
			}
			else
			{
				LOGGER.warn("Tried to set unregistered access level " + level + " to character " + getName() + ". Setting access level without privileges!");
				playerAccessLevel = AccessLevels.getInstance().getUserAccessLevel();
			}
		}
		else
		{
			playerAccessLevel = accessLevel;
		}
		
		if (playerAccessLevel != AccessLevels.getInstance().getUserAccessLevel())
		{
			getAppearance().setNameColor(playerAccessLevel.getNameColor());
			getAppearance().setTitleColor(playerAccessLevel.getTitleColor());
			broadcastUserInfo();
		}
	}
	
	/**
	 * Sets the account accesslevel.
	 * @param level the new account accesslevel
	 */
	public void setAccountAccesslevel(final int level)
	{
		LoginServerThread.getInstance().sendAccessLevel(getAccountName(), level);
	}
	
	/**
	 * Return the accessLevel of the L2PcInstance.<BR>
	 * <BR>
	 * @return the access level
	 */
	public AccessLevel getAccessLevel()
	{
		if (playerAccessLevel == null)
		{
			setAccessLevel(AccessLevels.getInstance().getUserAccessLevel().getLevel());
		}
		
		return playerAccessLevel;
	}
	
	@Override
	public double getLevelMod()
	{
		return (100.0 - 11 + getLevel()) / 100.0;
	}
	
	/**
	 * Update Stats of the L2PcInstance client side by sending Server->Client packet UserInfo/StatusUpdate to this L2PcInstance and CharInfo/StatusUpdate to all L2PcInstance in its knownPlayers (broadcast).<BR>
	 * <BR>
	 * @param broadcastType the broadcast type
	 */
	public void updateAndBroadcastStatus(final int broadcastType)
	{
		refreshOverloaded();
		refreshExpertisePenalty();
		// Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its knownPlayers (broadcast)
		if (broadcastType == 1)
		{
			this.sendPacket(new UserInfo(this));
		}
		
		if (broadcastType == 2)
		{
			broadcastUserInfo();
		}
	}
	
	/**
	 * Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the L2PcInstance and all L2PcInstance to inform (broadcast).<BR>
	 * <BR>
	 * @param flag the new karma flag
	 */
	public void setKarmaFlag(final int flag)
	{
		sendPacket(new UserInfo(this));
		for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			player.sendPacket(new RelationChanged(this, getRelation(player), isAutoAttackable(player)));
			
			if (getPet() != null)
			{
				getPet().broadcastPacket(new NpcInfo(getPet(), null));
			}
		}
	}
	
	/**
	 * Send a Server->Client StatusUpdate packet with Karma to the L2PcInstance and all L2PcInstance to inform (broadcast).<BR>
	 * <BR>
	 */
	public void broadcastKarma()
	{
		sendPacket(new UserInfo(this));
		for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			if (player == null)
			{
				continue;
			}
			
			player.sendPacket(new RelationChanged(this, getRelation(player), isAutoAttackable(player)));
			
			if (getPet() != null)
			{
				getPet().broadcastPacket(new NpcInfo(getPet(), null));
			}
		}
	}
	
	/**
	 * Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout).<BR>
	 * <BR>
	 * @param isOnline the new online status
	 */
	public void setOnline(final boolean isOnline)
	{
		online = isOnline;
		
		// Update the characters table of the database with online status and lastAccess (called when login and logout)
		updateOnlineStatus();
	}
	
	/**
	 * Sets the checks if is in7s dungeon.
	 * @param isIn7sDungeon the new checks if is in7s dungeon
	 */
	public void setIsIn7sDungeon(final boolean isIn7sDungeon)
	{
		if (playerIsIn7sDungeon != isIn7sDungeon)
		{
			playerIsIn7sDungeon = isIn7sDungeon;
		}
		
		updateIsIn7sDungeonStatus();
	}
	
	/**
	 * Update the characters table of the database with online status and lastAccess of this L2PcInstance (called when login and logout).<BR>
	 * <BR>
	 */
	public void updateOnlineStatus()
	{
		
		if (isInOfflineMode())
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER_ONLINE_STATUS))
		{
			statement.setInt(1, isOnline() ? 1 : 0);
			statement.setLong(2, System.currentTimeMillis());
			statement.setInt(3, getObjectId());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("L2PcInstance.updateOnlineStatus: Could not update character online status for player" + getName(), e);
		}
	}
	
	/**
	 * Update is in7s dungeon status.
	 */
	public void updateIsIn7sDungeonStatus()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER_IS_IN_7S_DUNGEON))
		{
			statement.setInt(1, isIn7sDungeon() ? 1 : 0);
			statement.setLong(2, System.currentTimeMillis());
			statement.setInt(3, getObjectId());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("L2PcInstance.updateIsIn7sDungeonStatus: Could not update character is in 7s dungeon status for player" + getName(), e);
		}
	}
	
	/**
	 * Update first log
	 */
	public void updateFirstLog()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_FIRST_LOG))
		{
			statement.setInt(1, getFirstLog() ? 1 : 0);
			statement.setInt(2, getObjectId());
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			LOGGER.error("L2PcInstance.updateFirstLog : Could not set char first login for player " + getName(), e);
		}
	}
	
	/**
	 * Create a new player in the characters table of the database.<BR>
	 * <BR>
	 * @return true, if successful
	 */
	private boolean createDb()
	{
		boolean output = false;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_NEW_CHARACTER))
		{
			statement.setString(1, accountName);
			statement.setInt(2, getObjectId());
			statement.setString(3, getName());
			statement.setInt(4, getLevel());
			statement.setInt(5, getMaxHp());
			statement.setDouble(6, getCurrentHp());
			statement.setInt(7, getMaxCp());
			statement.setDouble(8, getCurrentCp());
			statement.setInt(9, getMaxMp());
			statement.setDouble(10, getCurrentMp());
			statement.setInt(11, getAccuracy());
			statement.setInt(12, getCriticalHit(null, null));
			statement.setInt(13, getEvasionRate(null));
			statement.setInt(14, getMAtk(null, null));
			statement.setInt(15, getMDef(null, null));
			statement.setInt(16, getMAtkSpd());
			statement.setInt(17, getPAtk(null));
			statement.setInt(18, getPDef(null));
			statement.setInt(19, getPAtkSpd());
			statement.setInt(20, getRunSpeed());
			statement.setInt(21, getWalkSpeed());
			statement.setInt(22, getSTR());
			statement.setInt(23, getCON());
			statement.setInt(24, getDEX());
			statement.setInt(25, getINT());
			statement.setInt(26, getMEN());
			statement.setInt(27, getWIT());
			statement.setInt(28, getAppearance().getFace());
			statement.setInt(29, getAppearance().getHairStyle());
			statement.setInt(30, getAppearance().getHairColor());
			statement.setInt(31, getAppearance().getSex() ? 1 : 0);
			statement.setDouble(32, 1/* getMovementMultiplier() */);
			statement.setDouble(33, 1/* getAttackSpeedMultiplier() */);
			statement.setDouble(34, getTemplate().collisionRadius/* getCollisionRadius() */);
			statement.setDouble(35, getTemplate().collisionHeight/* getCollisionHeight() */);
			statement.setLong(36, getExp());
			statement.setInt(37, getSp());
			statement.setInt(38, getKarma());
			statement.setInt(39, getPvpKills());
			statement.setInt(40, getPkKills());
			statement.setInt(41, getClanId());
			statement.setInt(42, getMaxLoad());
			statement.setInt(43, getRace().ordinal());
			statement.setInt(44, getClassId().getId());
			statement.setLong(45, getDeleteTimer());
			statement.setInt(46, hasDwarvenCraft() ? 1 : 0);
			statement.setString(47, getTitle());
			statement.setInt(48, getAccessLevel().getLevel());
			statement.setInt(49, isOnline() ? 1 : 0);
			statement.setInt(50, isIn7sDungeon() ? 1 : 0);
			statement.setInt(51, getClanPrivileges());
			statement.setInt(52, getWantsPeace());
			statement.setInt(53, getBaseClass());
			statement.setInt(54, isNewbie() ? 1 : 0);
			statement.setInt(55, isNoble() ? 1 : 0);
			statement.setLong(56, 0);
			statement.setLong(57, System.currentTimeMillis());
			
			statement.setString(58, StringToHex(Integer.toHexString(getAppearance().getNameColor()).toUpperCase()));
			statement.setString(59, StringToHex(Integer.toHexString(getAppearance().getTitleColor()).toUpperCase()));
			
			statement.executeUpdate();
			output = true;
		}
		catch (final Exception e)
		{
			LOGGER.error("L2PcInstance.createDb : Could not create new character data in characters table", e);
		}
		
		if (output)
		{
			final String text = "Created new character : " + getName() + " for account: " + accountName;
			Log.add(text, "new_chars");
		}
		
		return output;
	}
	
	/**
	 * Retrieve a L2PcInstance from the characters table of the database and add it in allObjects of the L2world.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Retrieve the L2PcInstance from the characters table of the database</li>
	 * <li>Add the L2PcInstance object in allObjects</li>
	 * <li>Set the x,y,z position of the L2PcInstance and make it invisible</li>
	 * <li>Update the overloaded status of the L2PcInstance</li><BR>
	 * <BR>
	 * @param  objectId Identifier of the object to initialized
	 * @return          The L2PcInstance loaded from the database
	 */
	private static L2PcInstance restore(final int objectId)
	{
		L2PcInstance player = null;
		double curHp = 0;
		double curCp = 0;
		double curMp = 0;
		
		// Retrieve the L2PcInstance from the characters table of the database
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHARACTER_BY_OBJ_ID);)
		{
			statement.setInt(1, objectId);
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				int activeClassId = rset.getInt("classid");
				boolean female = rset.getBoolean("sex");
				L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(activeClassId);
				PcAppearance app = new PcAppearance(rset.getByte("face"), rset.getByte("hairColor"), rset.getByte("hairStyle"), female);
				
				player = new L2PcInstance(objectId, template, rset.getString("account_name"), app);
				player.setName(rset.getString("char_name"));
				player.setLastAccess(rset.getLong("lastAccess"));
				
				player.getStat().setExp(rset.getLong("exp"));
				player.setExpBeforeDeath(rset.getLong("expBeforeDeath"));
				player.getStat().setLevel(rset.getByte("level"));
				player.getStat().setSp(rset.getInt("sp"));
				
				player.setWantsPeace(rset.getInt("wantspeace"));
				
				player.setHeading(rset.getInt("heading"));
				
				player.setKarma(rset.getInt("karma"));
				player.setPvpKills(rset.getInt("pvpkills"));
				player.setPkKills(rset.getInt("pkkills"));
				player.setOnlineTime(rset.getLong("onlinetime"));
				player.setNewbie(rset.getBoolean("newbie"));
				player.setNoble(rset.getBoolean("nobless"));
				player.setClanJoinExpiryTime(rset.getLong("clan_join_expiry_time"));
				player.setFirstLog(rset.getInt("first_log"));
				player.pcBangPoint = rset.getInt("pc_point");
				app = null;
				
				if (player.getClanJoinExpiryTime() < System.currentTimeMillis())
				{
					player.setClanJoinExpiryTime(0);
				}
				player.setClanCreateExpiryTime(rset.getLong("clan_create_expiry_time"));
				if (player.getClanCreateExpiryTime() < System.currentTimeMillis())
				{
					player.setClanCreateExpiryTime(0);
				}
				
				final int clanId = rset.getInt("clanid");
				player.setPowerGrade((int) rset.getLong("power_grade"));
				player.setPledgeType(rset.getInt("subpledge"));
				player.setLastRecomUpdate(rset.getLong("last_recom_date"));
				// player.setApprentice(rset.getInt("apprentice"));
				
				if (clanId > 0)
				{
					player.setClan(ClanTable.getInstance().getClan(clanId));
				}
				
				if (player.getClan() != null)
				{
					if (player.getClan().getLeaderId() != player.getObjectId())
					{
						if (player.getPowerGrade() == 0)
						{
							player.setPowerGrade(5);
						}
						player.setClanPrivileges(player.getClan().getRankPrivs(player.getPowerGrade()));
					}
					else
					{
						player.setClanPrivileges(L2Clan.CP_ALL);
						player.setPowerGrade(1);
					}
				}
				else
				{
					player.setClanPrivileges(L2Clan.CP_NOTHING);
				}
				
				player.setDeleteTimer(rset.getLong("deletetime"));
				
				player.setTitle(rset.getString("title"));
				player.setAccessLevel(Config.GM_PLAYERS.getOrDefault(player.getObjectId(), 0));
				player.setFistsWeaponItem(player.findFistsWeaponItem(activeClassId));
				player.setUptime(System.currentTimeMillis());
				
				curHp = rset.getDouble("curHp");
				curCp = rset.getDouble("curCp");
				curMp = rset.getDouble("curMp");
				
				/*
				 * player.setCurrentHp(rset.getDouble("curHp")); player.setCurrentCp(rset.getDouble("curCp")); player.setCurrentMp(rset.getDouble("curMp"));
				 */
				
				// Check recs
				player.checkRecom(rset.getInt("rec_have"), rset.getInt("rec_left"));
				
				player.playerClassIndex = 0;
				try
				{
					player.setBaseClass(rset.getInt("base_class"));
				}
				catch (final Exception e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
					player.setBaseClass(activeClassId);
				}
				
				// Restore Subclass Data (cannot be done earlier in function)
				if (restoreSubClassData(player))
				{
					if (activeClassId != player.getBaseClass())
					{
						for (final SubClass subClass : player.getSubClasses().values())
						{
							if (subClass.getClassId() == activeClassId)
							{
								player.playerClassIndex = subClass.getClassIndex();
							}
						}
					}
				}
				if (player.getClassIndex() == 0 && activeClassId != player.getBaseClass())
				{
					// Subclass in use but doesn't exist in DB -
					// a possible restart-while-modifysubclass cheat has been attempted.
					// Switching to use base class
					player.setClassId(player.getBaseClass());
					LOGGER.warn("Player " + player.getName() + " reverted to base class. Possibly has tried a relogin exploit while subclassing.");
				}
				else
				{
					player.activeClass = activeClassId;
				}
				
				player.setApprentice(rset.getInt("apprentice"));
				player.setSponsor(rset.getInt("sponsor"));
				player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
				player.setIsIn7sDungeon(rset.getBoolean("isin7sdungeon"));
				
				player.setPunishLevel(rset.getInt("punish_level"));
				if (player.getPunishLevel() != PunishLevel.NONE)
				{
					player.setPunishTimer(rset.getLong("punish_timer"));
				}
				else
				{
					player.setPunishTimer(0);
					/*
					 * player.setInJail(rset.getInt("in_jail") == 1 ? true : false); if(player.isInJail()) { player.setJailTimer(rset.getLong("jail_timer")); } else { player.setJailTimer(0); } player.setChatBanTimer(rset.getLong("banchat_time")); player.updateChatBanState();
					 */
				}
				
				try
				{
					player.getAppearance().setNameColor(Integer.decode(new StringBuilder().append("0x").append(rset.getString("name_color")).toString()).intValue());
					player.getAppearance().setTitleColor(Integer.decode(new StringBuilder().append("0x").append(rset.getString("title_color")).toString()).intValue());
				}
				catch (final Exception e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
					// leave them as default
				}
				
				CursedWeaponsManager.getInstance().checkPlayer(player);
				
				player.setAllianceWithVarkaKetra(rset.getInt("varka_ketra_ally"));
				
				player.setDeathPenaltyBuffLevel(rset.getInt("death_penalty_level"));
				
				// Set the x,y,z position of the L2PcInstance and make it invisible
				player.setXYZInvisible(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
				
				// Retrieve the name and ID of the other characters assigned to this account.
				try (PreparedStatement stmt = con.prepareStatement(SELECT_CHARACTERS_IN_ACCOUNT))
				{
					stmt.setString(1, player.accountName);
					stmt.setInt(2, objectId);
					ResultSet chars = stmt.executeQuery();
					
					while (chars.next())
					{
						final Integer charId = chars.getInt("obj_Id");
						final String charName = chars.getString("char_name");
						player.characters.put(charId, charName);
					}
					
					chars.close();
					chars = null;
				}
				
				break;
			}
			
			DatabaseUtils.close(rset);
			
			if (player == null)
			{
				// TODO: Log this!
				return null;
			}
			
			// Retrieve from the database all secondary data of this L2PcInstance
			// and reward expertise/lucky skills if necessary.
			// Note that Clan, Noblesse and Hero skills are given separately and not here.
			player.restoreCharData();
			// reward skill restore mode in order to avoid duplicate storage of already stored skills
			player.rewardSkills(true);
			
			// Restore pet if exists in the world
			player.setPet(L2World.getInstance().getPet(player.getObjectId()));
			if (player.getPet() != null)
			{
				player.getPet().setOwner(player);
			}
			
			// Update the overloaded status of the L2PcInstance
			player.refreshOverloaded();
			
			player.restoreFriendList();
		}
		catch (final Exception e)
		{
			LOGGER.error("L2PcInstance.restore : Could not restore char data", e);
		}
		
		if (player != null)
		{
			player.fireEvent(EventType.LOAD.name, (Object[]) null);
			
			try
			{
				Thread.sleep(100);
			}
			catch (final InterruptedException e)
			{
				e.printStackTrace();
			}
			
			// once restored all the skill status, update current CP, MP and HP
			player.setCurrentHpDirect(curHp);
			player.setCurrentCpDirect(curCp);
			player.setCurrentMpDirect(curMp);
			// player.setCurrentCp(curCp);
			// player.setCurrentMp(curMp);
		}
		return player;
	}
	
	/**
	 * Gets the mail.
	 * @return the mail
	 */
	public Forum getMail()
	{
		if (forumMail == null)
		{
			setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));
			
			if (forumMail == null)
			{
				ForumsBBSManager.getInstance().createNewForum(getName(), ForumsBBSManager.getInstance().getForumByName("MailRoot"), Forum.MAIL, Forum.OWNERONLY, getObjectId());
				setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));
			}
		}
		
		return forumMail;
	}
	
	/**
	 * Sets the mail.
	 * @param forum the new mail
	 */
	public void setMail(final Forum forum)
	{
		forumMail = forum;
	}
	
	/**
	 * Gets the memo.
	 * @return the memo
	 */
	public Forum getMemo()
	{
		if (forumMemo == null)
		{
			setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(accountName));
			
			if (forumMemo == null)
			{
				ForumsBBSManager.getInstance().createNewForum(accountName, ForumsBBSManager.getInstance().getForumByName("MemoRoot"), Forum.MEMO, Forum.OWNERONLY, getObjectId());
				setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(accountName));
			}
		}
		
		return forumMemo;
	}
	
	/**
	 * Sets the memo.
	 * @param forum the new memo
	 */
	public void setMemo(final Forum forum)
	{
		forumMemo = forum;
	}
	
	/**
	 * Restores sub-class data for the L2PcInstance, used to check the current class index for the character.
	 * @param  player the player
	 * @return        true, if successful
	 */
	private static boolean restoreSubClassData(final L2PcInstance player)
	{
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_SUBCLASSES))
		{
			statement.setInt(1, player.getObjectId());
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final SubClass subClass = new SubClass();
				subClass.setClassId(rset.getInt("class_id"));
				subClass.setLevel(rset.getByte("level"));
				subClass.setExp(rset.getLong("exp"));
				subClass.setSp(rset.getInt("sp"));
				subClass.setClassIndex(rset.getInt("class_index"));
				
				// Enforce the correct indexing of subClasses against their class indexes.
				player.getSubClasses().put(subClass.getClassIndex(), subClass);
			}
			
			DatabaseUtils.close(rset);
			rset = null;
		}
		catch (final Exception e)
		{
			LOGGER.error("L2PCInstance.restoreSubClassData : Could not restore classes for " + player.getName(), e);
		}
		
		return true;
	}
	
	/**
	 * Restores secondary data for the L2PcInstance, based on the current class index.
	 */
	private void restoreCharData()
	{
		// Retrieve from the database all skills of this L2PcInstance and add them to skills.
		restoreSkills();
		
		// Retrieve from the database all macroses of this L2PcInstance and add them to macroses.
		macroses.restore();
		
		// Retrieve from the database all shortCuts of this L2PcInstance and add them to shortCuts.
		shortCuts.restore();
		
		// Retrieve from the database all henna of this L2PcInstance and add them to henna.
		restoreHenna();
		
		// Retrieve from the database all recom data of this L2PcInstance and add to recomChars.
		if (Config.ALT_RECOMMEND)
		{
			restoreRecom();
		}
		
		// Retrieve from the database the recipe book of this L2PcInstance.
		if (!isSubClassActive())
		{
			restoreRecipeBook();
		}
	}
	
	/**
	 * Store recipe book data for this L2PcInstance, if not on an active sub-class.
	 */
	private synchronized void storeRecipeBook()
	{
		// If the player is on a sub-class don't even attempt to store a recipe book.
		if (isSubClassActive())
		{
			return;
		}
		
		if (getCommonRecipeBook().length == 0 && getDwarvenRecipeBook().length == 0)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement2 = con.prepareStatement(INSERT_CHAR_RECIPEBOOK_NORMAL);
			PreparedStatement statement3 = con.prepareStatement(INSERT_CHAR_RECIPEBOOK_DWARF))
		{
			for (L2RecipeList recipe : getCommonRecipeBook())
			{
				if (!recipe.isFromDB()) // Check the recipe, if its from DB, no point to overwrite
				{
					statement2.setInt(1, getObjectId());
					statement2.setInt(2, recipe.getId());
					statement2.executeUpdate();
				}
			}
			
			for (L2RecipeList recipe : getDwarvenRecipeBook())
			{
				if (!recipe.isFromDB()) // Check the recipe, if its from DB, no point to overwrite
				{
					statement3.setInt(1, getObjectId());
					statement3.setInt(2, recipe.getId());
					statement3.executeUpdate();
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("L2PCInstance.storeRecipeBook : Could not store recipe book data", e);
		}
	}
	
	/**
	 * Restore recipe book data for this L2PcInstance.
	 */
	private void restoreRecipeBook()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHARACTER_RECIPE_BOOK))
		{
			statement.setInt(1, getObjectId());
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					L2RecipeList recipe = RecipeTable.getInstance().getRecipeList(rset.getInt("id") - 1);
					recipe.setIsFromDB(true);
					
					if (rset.getInt("type") == 1)
					{
						registerDwarvenRecipeList(recipe);
					}
					else
					{
						registerCommonRecipeList(recipe);
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("L2PCInstance.restoreRecipeBook : Could not restore recipe book data", e);
		}
	}
	
	public synchronized void store(final boolean force)
	{
		// update client coords, if these look like true
		if (!force && isInsideRadius(getClientX(), getClientY(), 1000, true))
		{
			setXYZ(getClientX(), getClientY(), getClientZ());
		}
		
		storeCharBase();
		storeCharSub();
		
		// Dont store effect if the char was on Offline trade
		if (!isStored())
		{
			storeEffect();
		}
		
		storeRecipeBook();
		fireEvent(EventType.STORE.name, (Object[]) null);
		
		// If char is in Offline trade, setStored must be true
		if (isInOfflineMode())
		{
			setStored(true);
		}
		else
		{
			setStored(false);
		}
	}
	
	/**
	 * Update L2PcInstance stats in the characters table of the database.<BR>
	 * <BR>
	 */
	public synchronized void store()
	{
		store(false);
	}
	
	/**
	 * Store char base.
	 */
	private synchronized void storeCharBase()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER_BY_OBJ_ID))
		{
			// Get the exp, level, and sp of base class to store in base table
			final int currentClassIndex = getClassIndex();
			playerClassIndex = 0;
			final long exp = getStat().getExp();
			final int level = getStat().getLevel();
			final int sp = getStat().getSp();
			playerClassIndex = currentClassIndex;
			
			// Update base class
			statement.setInt(1, level);
			statement.setInt(2, getMaxHp());
			statement.setDouble(3, getCurrentHp());
			statement.setInt(4, getMaxCp());
			statement.setDouble(5, getCurrentCp());
			statement.setInt(6, getMaxMp());
			statement.setDouble(7, getCurrentMp());
			statement.setInt(8, getSTR());
			statement.setInt(9, getCON());
			statement.setInt(10, getDEX());
			statement.setInt(11, getINT());
			statement.setInt(12, getMEN());
			statement.setInt(13, getWIT());
			statement.setInt(14, getAppearance().getFace());
			statement.setInt(15, getAppearance().getHairStyle());
			statement.setInt(16, getAppearance().getHairColor());
			statement.setInt(17, getHeading());
			statement.setInt(18, observerMode ? obsX : getX());
			statement.setInt(19, observerMode ? obsY : getY());
			statement.setInt(20, observerMode ? obsZ : getZ());
			statement.setLong(21, exp);
			statement.setLong(22, getExpBeforeDeath());
			statement.setInt(23, sp);
			statement.setInt(24, getKarma());
			statement.setInt(25, getPvpKills());
			statement.setInt(26, getPkKills());
			statement.setInt(27, getRecomHave());
			statement.setInt(28, getRecomLeft());
			statement.setInt(29, getClanId());
			statement.setInt(30, getMaxLoad());
			statement.setInt(31, getRace().ordinal());
			statement.setInt(32, getClassId().getId());
			statement.setLong(33, getDeleteTimer());
			statement.setString(34, getTitle());
			statement.setInt(35, getAccessLevel().getLevel());
			
			if (isInOfflineMode || isOnline())
			{
				statement.setInt(36, 1);// in offline mode or online
			}
			else
			{
				statement.setInt(36, isOnline() ? 1 : 0);
			}
			
			statement.setInt(37, isIn7sDungeon() ? 1 : 0);
			statement.setInt(38, getClanPrivileges());
			statement.setInt(39, getWantsPeace());
			statement.setInt(40, getBaseClass());
			
			long totalOnlineTime = onlineTime;
			
			if (onlineBeginTime > 0)
			{
				totalOnlineTime += (System.currentTimeMillis() - onlineBeginTime) / 1000;
			}
			
			statement.setLong(41, totalOnlineTime);
			statement.setInt(42, getPunishLevel().value());
			statement.setLong(43, getPunishTimer());
			statement.setInt(44, isNewbie() ? 1 : 0);
			statement.setInt(45, isNoble() ? 1 : 0);
			statement.setLong(46, getPowerGrade());
			statement.setInt(47, getPledgeType());
			statement.setLong(48, getLastRecomUpdate());
			statement.setInt(49, getLvlJoinedAcademy());
			statement.setLong(50, getApprentice());
			statement.setLong(51, getSponsor());
			statement.setInt(52, getAllianceWithVarkaKetra());
			statement.setLong(53, getClanJoinExpiryTime());
			statement.setLong(54, getClanCreateExpiryTime());
			statement.setString(55, getName());
			statement.setLong(56, getDeathPenaltyBuffLevel());
			statement.setInt(57, getPcBangScore());
			
			statement.setString(58, StringToHex(Integer.toHexString(originalNameColorOffline).toUpperCase()));
			statement.setString(59, StringToHex(Integer.toHexString(getAppearance().getTitleColor()).toUpperCase()));
			
			statement.setInt(60, getObjectId());
			
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			LOGGER.error("Could not store char base data: ", e);
		}
	}
	
	/**
	 * Store char sub.
	 */
	private synchronized void storeCharSub()
	{
		int counter = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CHAR_SUBCLASS);)
		{
			if (getTotalSubClasses() > 0)
			{
				for (final SubClass subClass : getSubClasses().values())
				{
					statement.setLong(1, subClass.getExp());
					statement.setInt(2, subClass.getSp());
					statement.setInt(3, subClass.getLevel());
					statement.setInt(4, subClass.getClassId());
					statement.setInt(5, getObjectId());
					statement.setInt(6, subClass.getClassIndex());
					statement.addBatch();
					counter++;
				}
				
				if (counter > 0)
				{
					statement.executeBatch();
				}
			}
		}
		catch (final Exception e)
		{
			LOGGER.error("Could not store sub class data for " + getName(), e);
		}
	}
	
	private synchronized void storeEffect()
	{
		if (!Config.STORE_SKILL_COOLTIME)
		{
			return;
		}
		
		final L2Effect[] effects = getAllEffects();
		
		if (effects == null)
		{
			return;
		}
		
		if (effects.length == 0)
		{
			return;
		}
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			// Delete all current stored effects for char to avoid dupe
			statement = con.prepareStatement(DELETE_CHARACTER_SKILLS_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.execute();
			DatabaseUtils.close(statement);
			
			// Store all effect data along with calulated remaining
			// reuse delays for matching skills. 'restore_type'= 0.
			statement = con.prepareStatement(INSERT_CHARACTER_SKILLS_SAVE);
			
			final List<Integer> storedSkills = new ArrayList<>();
			int buff_index = 0;
			
			for (final L2Effect effect : effects)
			{
				if (effect == null)
				{
					continue;
				}
				
				if (effect.getEffectType() == L2Effect.EffectType.HEAL_OVER_TIME)
				{
					continue;
				}
				
				if (effect.getEffectType() == L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME)
				{
					continue;
				}
				
				final int skillId = effect.getSkill().getId();
				
				if (storedSkills.contains(skillId))
				{
					continue;
				}
				storedSkills.add(skillId);
				
				if (effect.getInUse() && !effect.getSkill().isToggle() && !effect.getStackType().equals("BattleForce") && !effect.getStackType().equals("SpellForce") && effect.getSkill().getSkillType() != SkillType.FORCE_BUFF)
				{
					statement.setInt(1, getObjectId());
					statement.setInt(2, skillId);
					statement.setInt(3, effect.getSkill().getLevel());
					statement.setInt(4, effect.getCount());
					statement.setInt(5, effect.getTime());
					if (reuseTimeStamps.containsKey(effect.getSkill().getReuseHashCode()))
					{
						final TimeStamp t = reuseTimeStamps.get(effect.getSkill().getReuseHashCode());
						statement.setLong(6, t.hasNotPassed() ? t.getReuse() : 0);
						statement.setLong(7, t.hasNotPassed() ? t.getStamp() : 0);
					}
					else
					{
						statement.setLong(6, 0);
						statement.setLong(7, 0);
					}
					statement.setInt(8, 0);
					statement.setInt(9, getClassIndex());
					statement.setInt(10, ++buff_index);
					statement.execute();
				}
			}
			// Store the reuse delays of remaining skills which
			// lost effect but still under reuse delay. 'restore_type' 1.
			for (final TimeStamp t : reuseTimeStamps.values())
			{
				if (t.hasNotPassed())
				{
					final int skillId = t.getSkill().getId();
					final int skillLvl = t.getSkill().getLevel();
					if (storedSkills.contains(skillId))
					{
						continue;
					}
					storedSkills.add(skillId);
					
					statement.setInt(1, getObjectId());
					statement.setInt(2, skillId);
					statement.setInt(3, skillLvl);
					statement.setInt(4, -1);
					statement.setInt(5, -1);
					statement.setLong(6, t.getReuse());
					statement.setLong(7, t.getStamp());
					statement.setInt(8, 1);
					statement.setInt(9, getClassIndex());
					statement.setInt(10, ++buff_index);
					statement.execute();
				}
			}
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			LOGGER.warn("Could not store char effect data: ");
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	/**
	 * Return True if the L2PcInstance is on line.<BR>
	 * <BR>
	 * @return the int
	 */
	public boolean isOnline()
	{
		return online;
	}
	
	/**
	 * Checks if is in7s dungeon.
	 * @return true, if is in7s dungeon
	 */
	public boolean isIn7sDungeon()
	{
		return playerIsIn7sDungeon;
	}
	
	/**
	 * Add a skill to the L2PcInstance skills and its Func objects to the calculator set of the L2PcInstance and save update in the character_skills table of the database.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills own by a L2PcInstance are identified in <B>_skills</B><BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Replace oldSkill by newSkill or Add the newSkill</li>
	 * <li>If an old skill has been replaced, remove all its Func objects of L2Character calculator set</li>
	 * <li>Add Func objects of newSkill to the calculator set of the L2Character</li><BR>
	 * <BR>
	 */
	private boolean learningSkill = false;
	
	/**
	 * @param  newSkill the new skill
	 * @param  store    : Add or update a L2PcInstance skill in the character_skills table of the database
	 * @return          oldSkill
	 */
	public L2Skill addSkill(final L2Skill newSkill, final boolean store)
	{
		learningSkill = true;
		// Add a skill to the L2PcInstance skills and its Func objects to the calculator set of the L2PcInstance
		final L2Skill oldSkill = super.addSkill(newSkill);
		
		if (store)
		{
			storeSkill(newSkill, oldSkill, -1);
		}
		
		learningSkill = false;
		
		return oldSkill;
	}
	
	/**
	 * Checks if is learning skill.
	 * @return true, if is learning skill
	 */
	public boolean isLearningSkill()
	{
		return learningSkill;
	}
	
	/**
	 * Removes the skill.
	 * @param  skill the skill
	 * @param  store the store
	 * @return       the l2 skill
	 */
	public L2Skill removeSkill(final L2Skill skill, final boolean store)
	{
		if (store)
		{
			return removeSkill(skill);
		}
		return super.removeSkill(skill);
	}
	
	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character and save update in the character_skills table of the database.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove the skill from the L2Character skills</li>
	 * <li>Remove all its Func objects from the L2Character calculator set</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance : Save update in the character_skills table of the database</li><BR>
	 * <BR>
	 * @param  skill The L2Skill to remove from the L2Character
	 * @return       The L2Skill removed
	 */
	@Override
	public L2Skill removeSkill(final L2Skill skill)
	{
		// Remove a skill from the L2Character and its Func objects from calculator set of the L2Character
		final L2Skill oldSkill = super.removeSkill(skill);
		
		if (oldSkill != null)
		{
			// Remove or update a L2PcInstance skill from the character_skills table of the database
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(DELETE_SKILL_FROM_CHAR))
			{
				statement.setInt(1, oldSkill.getId());
				statement.setInt(2, getObjectId());
				statement.setInt(3, getClassIndex());
				statement.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("L2PcInstance.removeSkill : Could not delete skill", e);
			}
		}
		
		for (L2ShortCut sc : getAllShortCuts())
		{
			if (sc != null && skill != null && sc.getId() == skill.getId() && sc.getType() == L2ShortCut.TYPE_SKILL)
			{
				deleteShortCut(sc.getSlot(), sc.getPage());
			}
		}
		
		return oldSkill;
	}
	
	/**
	 * Add or update a L2PcInstance skill in the character_skills table of the database. <BR>
	 * <BR>
	 * If newClassIndex > -1, the skill will be stored with that class index, not the current one.
	 * @param newSkill      the new skill
	 * @param oldSkill      the old skill
	 * @param newClassIndex the new class index
	 */
	private void storeSkill(final L2Skill newSkill, final L2Skill oldSkill, final int newClassIndex)
	{
		int classIndex = playerClassIndex;
		
		if (newClassIndex > -1)
		{
			classIndex = newClassIndex;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			if (oldSkill != null && newSkill != null)
			{
				try (PreparedStatement pstUpdateSkill = con.prepareStatement(UPDATE_CHARACTER_SKILL_LEVEL))
				{
					pstUpdateSkill.setInt(1, newSkill.getLevel());
					pstUpdateSkill.setInt(2, oldSkill.getId());
					pstUpdateSkill.setInt(3, getObjectId());
					pstUpdateSkill.setInt(4, classIndex);
					pstUpdateSkill.executeUpdate();
				}
			}
			else if (newSkill != null)
			{
				try (PreparedStatement pstInsertSkill = con.prepareStatement(ADD_NEW_SKILL))
				{
					pstInsertSkill.setInt(1, getObjectId());
					pstInsertSkill.setInt(2, newSkill.getId());
					pstInsertSkill.setInt(3, newSkill.getLevel());
					pstInsertSkill.setString(4, newSkill.getName());
					pstInsertSkill.setInt(5, classIndex);
					pstInsertSkill.executeUpdate();
				}
			}
			else
			{
				LOGGER.error("could not store new skill. its NULL");
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Error could not store char skills for player " + getName(), e);
		}
	}
	
	/**
	 * check player skills and remove unlegit ones (excludes hero, noblesse and cursed weapon skills).
	 */
	public void checkAllowedSkills()
	{
		boolean foundskill = false;
		if (!isGM())
		{
			// exclude Aio character
			if (isAio())
			{
				return;
			}
			
			Collection<L2SkillLearn> skillTree = SkillTreeTable.getInstance().getAllowedSkills(getClassId());
			// loop through all skills of player
			for (final L2Skill skill : getAllSkills())
			{
				final int skillid = skill.getId();
				// int skilllevel = skill.getLevel();
				
				foundskill = false;
				// loop through all skills in players skilltree
				for (final L2SkillLearn temp : skillTree)
				{
					// if the skill was found and the level is possible to obtain for his class everything is ok
					if (temp.getId() == skillid)
					{
						foundskill = true;
					}
				}
				
				// exclude noble skills
				if (isNoble() && skillid >= 325 && skillid <= 327)
				{
					foundskill = true;
				}
				
				// exclude noble skills
				if (isNoble() && skillid >= 1323 && skillid <= 1327)
				{
					foundskill = true;
				}
				
				// exclude hero skills
				if (isHero() && skillid >= 395 && skillid <= 396)
				{
					foundskill = true;
				}
				
				if (isHero() && skillid >= 1374 && skillid <= 1376)
				{
					foundskill = true;
				}
				
				// exclude cursed weapon skills
				if (isCursedWeaponEquiped() && skillid == CursedWeaponsManager.getInstance().getCursedWeapon(cursedWeaponEquipedId).getSkillId())
				{
					foundskill = true;
				}
				
				// exclude clan skills
				if (getClan() != null && skillid >= 370 && skillid <= 391)
				{
					foundskill = true;
				}
				
				// exclude seal of ruler / build siege hq
				if (getClan() != null && (skillid == 246 || skillid == 247))
				{
					if (getClan().getLeaderId() == getObjectId())
					{
						foundskill = true;
					}
				}
				
				// exclude fishing skills and common skills + dwarfen craft
				if (skillid >= 1312 && skillid <= 1322)
				{
					foundskill = true;
				}
				
				if (skillid >= 1368 && skillid <= 1373)
				{
					foundskill = true;
				}
				
				// exclude sa / enchant bonus / penality etc. skills
				if (skillid >= 3000 && skillid < 7000)
				{
					foundskill = true;
				}
				
				// exclude Skills from AllowedSkills in options.properties
				if (Config.ALLOWED_SKILLS_LIST.contains(skillid))
				{
					foundskill = true;
				}
				
				// remove skill and do a lil LOGGER message
				if (!foundskill)
				{
					removeSkill(skill);
					
					if (Config.DEBUG)
					{
						// sendMessage("Skill " + skill.getName() + " removed and gm informed!");
						LOGGER.warn("Character " + getName() + " of Account " + getAccountName() + " got skill " + skill.getName() + ".. Removed!"/* + IllegalPlayerAction.PUNISH_KICK */);
						
					}
				}
			}
			
			// Update skill list
			sendSkillList();
			
			skillTree = null;
		}
	}
	
	/**
	 * Retrieve from the database all skills of this L2PcInstance and add them to skills.<BR>
	 * <BR>
	 */
	public synchronized void restoreSkills()
	{
		if (EngineModsManager.onRestoreSkills(this))
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			if (!Config.KEEP_SUBCLASS_SKILLS)
			{
				// Retrieve all skills of this L2PcInstance from the database
				try (PreparedStatement statement = con.prepareStatement(RESTORE_SKILLS_FOR_CHAR))
				{
					statement.setInt(1, getObjectId());
					statement.setInt(2, getClassIndex());
					ResultSet rset = statement.executeQuery();
					
					// Go though the recordset of this SQL query
					while (rset.next())
					{
						final int id = rset.getInt("skill_id");
						final int level = rset.getInt("skill_level");
						
						if (id > 9000)
						{
							continue; // fake skills for base stats
						}
						
						// Create a L2Skill object for each record
						final L2Skill skill = SkillTable.getInstance().getInfo(id, level);
						
						// Add the L2Skill object to the L2Character skills and its Func objects to the calculator set of the L2Character
						super.addSkill(skill);
					}
					
					DatabaseUtils.close(rset);
					rset = null;
				}
			}
			else
			{
				// Retrieve all skills of this L2PcInstance from the database
				try (PreparedStatement statement = con.prepareStatement(RESTORE_SKILLS_FOR_CHAR_ALT_SUBCLASS))
				{
					statement.setInt(1, getObjectId());
					ResultSet rset = statement.executeQuery();
					
					// Go though the recordset of this SQL query
					while (rset.next())
					{
						final int id = rset.getInt("skill_id");
						final int level = rset.getInt("skill_level");
						
						if (id > 9000)
						{
							continue; // fake skills for base stats
						}
						
						// Create a L2Skill object for each record
						final L2Skill skill = SkillTable.getInstance().getInfo(id, level);
						
						// Add the L2Skill object to the L2Character skills and its Func objects to the calculator set of the L2Character
						super.addSkill(skill);
					}
					
					DatabaseUtils.close(rset);
					rset = null;
				}
			}
		}
		catch (final Exception e)
		{
			LOGGER.error("L2PCInstance.restoreSkills : Could not restore character skills for player " + getName(), e);
		}
	}
	
	public void restoreEffects()
	{
		restoreEffects(true);
	}
	
	/**
	 * Retrieve from the database all skill effects of this L2PcInstance and add them to the player.<BR>
	 * <BR>
	 * @param activateEffects
	 */
	public void restoreEffects(final boolean activateEffects)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement statement = con.prepareStatement(SELECT_CHARACTER_SKILLS_SAVE))
			{
				/**
				 * Restore Type 0 These skill were still in effect on the character upon logout. Some of which were self casted and might still have had a long reuse delay which also is restored.
				 */
				
				statement.setInt(1, getObjectId());
				statement.setInt(2, getClassIndex());
				statement.setInt(3, 0);
				
				try (ResultSet rset = statement.executeQuery())
				{
					while (rset.next())
					{
						int skillId = rset.getInt("skill_id");
						int skillLvl = rset.getInt("skill_level");
						int effectCount = rset.getInt("effect_count");
						int effectCurTime = rset.getInt("effect_cur_time");
						long reuseDelay = rset.getLong("reuse_delay");
						long systime = rset.getLong("systime");
						
						// Just incase the admin minipulated this table incorrectly :x
						if (skillId == -1 || effectCount == -1 || effectCurTime == -1 || reuseDelay < 0)
						{
							continue;
						}
						
						if (activateEffects)
						{
							L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
							
							skill.getEffects(this, this, false, false, false);
							
							// NEW
							getLastEffect().setCount(effectCount);
							getLastEffect().setFirstTime(effectCurTime);
							
							// OLD
							// skill = null;
							//
							// for (final L2Effect effect : getAllEffects())
							// {
							// if (effect.getSkill().getId() == skillId)
							// {
							// effect.setCount(effectCount);
							// effect.setFirstTime(effectCurTime);
							// }
							// }
						}
						long remainingTime = systime - System.currentTimeMillis();
						
						if (remainingTime > 10)
						
						{
							L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
							
							if (skill == null)
							{
								continue;
							}
							
							disableSkill(skill, remainingTime);
							addTimeStamp(new TimeStamp(skill, reuseDelay, systime));
						}
						
					}
					
				}
			}
			
			try (PreparedStatement statement = con.prepareStatement(SELECT_CHARACTER_SKILLS_SAVE))
			{
				/**
				 * Restore Type 1 The remaning skills lost effect upon logout but were still under a high reuse delay.
				 */
				
				statement.setInt(1, getObjectId());
				statement.setInt(2, getClassIndex());
				statement.setInt(3, 1);
				
				try (ResultSet rset = statement.executeQuery())
				{
					while (rset.next())
					{
						int skillId = rset.getInt("skill_id");
						int skillLvl = rset.getInt("skill_level");
						long reuseDelay = rset.getLong("reuse_delay");
						long systime = rset.getLong("systime");
						
						long remainingTime = systime - System.currentTimeMillis();
						
						if (remainingTime > 0)
						{
							L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
							
							if (skill == null)
							{
								continue;
							}
							
							disableSkill(skill, remainingTime);
							addTimeStamp(new TimeStamp(skill, reuseDelay, systime));
						}
						
					}
				}
			}
			
			try (PreparedStatement statement = con.prepareStatement(DELETE_CHARACTER_SKILLS_SAVE))
			{
				statement.setInt(1, getObjectId());
				statement.setInt(2, getClassIndex());
				statement.executeUpdate();
			}
		}
		catch (Exception e)
		{
			LOGGER.error("L2PcInstance.restoreEffects : Could not restore active effect data.", e);
		}
		
		updateEffectIcons();
	}
	
	/**
	 * Retrieve from the database all Henna of this L2PcInstance, add them to henna and calculate stats of the L2PcInstance.<BR>
	 * <BR>
	 */
	private void restoreHenna()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_HENNAS))
		{
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			ResultSet rset = statement.executeQuery();
			
			for (int i = 0; i < 3; i++)
			{
				playerHenna[i] = null;
			}
			
			while (rset.next())
			{
				final int slot = rset.getInt("slot");
				
				if (slot < 1 || slot > 3)
				{
					continue;
				}
				
				final int symbol_id = rset.getInt("symbol_id");
				
				L2HennaInstance sym = null;
				
				if (symbol_id != 0)
				{
					L2Henna tpl = HennaTable.getInstance().getTemplate(symbol_id);
					
					if (tpl != null)
					{
						sym = new L2HennaInstance(tpl);
						playerHenna[slot - 1] = sym;
						tpl = null;
						sym = null;
					}
				}
			}
			
			DatabaseUtils.close(rset);
			rset = null;
		}
		catch (final Exception e)
		{
			LOGGER.error("L2PcInstance.restoreHenna : Could not restore henna", e);
		}
		
		// Calculate Henna modifiers of this L2PcInstance
		recalcHennaStats();
	}
	
	/**
	 * Retrieve from the database all Recommendation data of this L2PcInstance, add to recomChars and calculate stats of the L2PcInstance.<BR>
	 * <BR>
	 */
	private void restoreRecom()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_RECOMS))
		{
			statement.setInt(1, getObjectId());
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				recomChars.add(rset.getInt("target_id"));
			}
			
			DatabaseUtils.close(rset);
			rset = null;
		}
		catch (Exception e)
		{
			LOGGER.error("L2PcInstance.restoreRecom : Could not restore recommendations", e);
		}
	}
	
	/**
	 * Return the number of Henna empty slot of the L2PcInstance.<BR>
	 * <BR>
	 * @return the henna empty slots
	 */
	public int getHennaEmptySlots()
	{
		int totalSlots = 1 + getClassId().level();
		
		for (int i = 0; i < 3; i++)
		{
			if (playerHenna[i] != null)
			{
				totalSlots--;
			}
		}
		
		if (totalSlots <= 0)
		{
			return 0;
		}
		
		return totalSlots;
	}
	
	/**
	 * Remove a Henna of the L2PcInstance, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2PcInstance.<BR>
	 * <BR>
	 * @param  slot the slot
	 * @return      true, if successful
	 */
	public boolean removeHenna(int slot)
	{
		if (slot < 1 || slot > 3)
		{
			return false;
		}
		
		slot--;
		
		if (playerHenna[slot] == null)
		{
			return false;
		}
		
		L2HennaInstance henna = playerHenna[slot];
		playerHenna[slot] = null;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_CHAR_HENNA))
		{
			statement.setInt(1, getObjectId());
			statement.setInt(2, slot + 1);
			statement.setInt(3, getClassIndex());
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			LOGGER.error("L2PcInstance.removeHenna : Could not remove char henna", e);
		}
		
		// Calculate Henna modifiers of this L2PcInstance
		recalcHennaStats();
		
		// Send Server->Client HennaInfo packet to this L2PcInstance
		sendPacket(new HennaInfo(this));
		
		// Send Server->Client UserInfo packet to this L2PcInstance
		sendPacket(new UserInfo(this));
		
		// Add the recovered dyes to the player's inventory and notify them.
		getInventory().addItem("Henna", henna.getItemIdDye(), henna.getAmountDyeRequire() / 2, this, null);
		
		SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
		sm.addItemName(henna.getItemIdDye());
		sm.addNumber(henna.getAmountDyeRequire() / 2);
		sendPacket(sm);
		sm = null;
		henna = null;
		
		return true;
	}
	
	/**
	 * Add a Henna to the L2PcInstance, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2PcInstance.<BR>
	 * <BR>
	 * @param  henna the henna
	 * @return       true, if successful
	 */
	public boolean addHenna(final L2HennaInstance henna)
	{
		if (getHennaEmptySlots() == 0)
		{
			sendMessage("You may not have more than three equipped symbols at a time.");
			return false;
		}
		
		// int slot = 0;
		for (int i = 0; i < 3; i++)
		{
			if (playerHenna[i] == null)
			{
				playerHenna[i] = henna;
				
				// Calculate Henna modifiers of this L2PcInstance
				recalcHennaStats();
				
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement(ADD_CHAR_HENNA);)
				{
					statement.setInt(1, getObjectId());
					statement.setInt(2, henna.getSymbolId());
					statement.setInt(3, i + 1);
					statement.setInt(4, getClassIndex());
					statement.executeUpdate();
				}
				catch (final Exception e)
				{
					LOGGER.warn("L2PcInstance.addHena : Could not save char henna", e);
				}
				
				// Send Server->Client HennaInfo packet to this L2PcInstance
				HennaInfo hi = new HennaInfo(this);
				sendPacket(hi);
				hi = null;
				
				// Send Server->Client UserInfo packet to this L2PcInstance
				UserInfo ui = new UserInfo(this);
				sendPacket(ui);
				ui = null;
				
				getInventory().refreshWeight();
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Calculate Henna modifiers of this L2PcInstance.<BR>
	 * <BR>
	 */
	private void recalcHennaStats()
	{
		hennaINT = 0;
		hennaSTR = 0;
		hennaCON = 0;
		hennaMEN = 0;
		hennaWIT = 0;
		hennaDEX = 0;
		
		for (int i = 0; i < 3; i++)
		{
			if (playerHenna[i] == null)
			{
				continue;
			}
			hennaINT += playerHenna[i].getStatINT();
			hennaSTR += playerHenna[i].getStatSTR();
			hennaMEN += playerHenna[i].getStatMEM();
			hennaCON += playerHenna[i].getStatCON();
			hennaWIT += playerHenna[i].getStatWIT();
			hennaDEX += playerHenna[i].getStatDEX();
		}
		
		if (hennaINT > 5)
		{
			hennaINT = 5;
		}
		
		if (hennaSTR > 5)
		{
			hennaSTR = 5;
		}
		
		if (hennaMEN > 5)
		{
			hennaMEN = 5;
		}
		
		if (hennaCON > 5)
		{
			hennaCON = 5;
		}
		
		if (hennaWIT > 5)
		{
			hennaWIT = 5;
		}
		
		if (hennaDEX > 5)
		{
			hennaDEX = 5;
		}
	}
	
	/**
	 * Return the Henna of this L2PcInstance corresponding to the selected slot.<BR>
	 * <BR>
	 * @param  slot the slot
	 * @return      the hennas
	 */
	public L2HennaInstance getHennas(final int slot)
	{
		if (slot < 1 || slot > 3)
		{
			return null;
		}
		
		return playerHenna[slot - 1];
	}
	
	/**
	 * Return the INT Henna modifier of this L2PcInstance.<BR>
	 * <BR>
	 * @return the henna stat int
	 */
	public int getHennaStatINT()
	{
		return hennaINT;
	}
	
	/**
	 * Return the STR Henna modifier of this L2PcInstance.<BR>
	 * <BR>
	 * @return the henna stat str
	 */
	public int getHennaStatSTR()
	{
		return hennaSTR;
	}
	
	/**
	 * Return the CON Henna modifier of this L2PcInstance.<BR>
	 * <BR>
	 * @return the henna stat con
	 */
	public int getHennaStatCON()
	{
		return hennaCON;
	}
	
	/**
	 * Return the MEN Henna modifier of this L2PcInstance.<BR>
	 * <BR>
	 * @return the henna stat men
	 */
	public int getHennaStatMEN()
	{
		return hennaMEN;
	}
	
	/**
	 * Return the WIT Henna modifier of this L2PcInstance.<BR>
	 * <BR>
	 * @return the henna stat wit
	 */
	public int getHennaStatWIT()
	{
		return hennaWIT;
	}
	
	/**
	 * Return the DEX Henna modifier of this L2PcInstance.<BR>
	 * <BR>
	 * @return the henna stat dex
	 */
	public int getHennaStatDEX()
	{
		return hennaDEX;
	}
	
	/**
	 * Return True if the L2PcInstance is autoAttackable.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Check if the attacker isn't the L2PcInstance Pet</li>
	 * <li>Check if the attacker is L2MonsterInstance</li>
	 * <li>If the attacker is a L2PcInstance, check if it is not in the same party</li>
	 * <li>Check if the L2PcInstance has Karma</li>
	 * <li>If the attacker is a L2PcInstance, check if it is not in the same siege clan (Attacker, Defender)</li> <BR>
	 * <BR>
	 * @param  attacker the attacker
	 * @return          true, if is auto attackable
	 */
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		// Check if the attacker isn't the L2PcInstance Pet
		if (attacker == this || attacker == getPet())
		{
			return false;
		}
		
		// Check if the attacker is a L2MonsterInstance
		if (attacker instanceof L2MonsterInstance)
		{
			return true;
		}
		
		// Check if the attacker is not in the same party, excluding duels like L2OFF
		if (getParty() != null && getParty().getPartyMembers().contains(attacker) && !(getDuelState() == Duel.DUELSTATE_DUELLING && getDuelId() == ((L2PcInstance) attacker).getDuelId()))
		{
			return false;
		}
		
		// Check if the attacker is in olympia and olympia start
		if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).isInOlympiadMode())
		{
			if (isInOlympiadMode() && isInOlympiadFight() && ((L2PcInstance) attacker).getOlympiadGameId() == getOlympiadGameId())
			{
				if (isFakeDeath())
				{
					return false;
				}
				return true;
			}
			return false;
		}
		
		// Check if the attacker is not in the same clan, excluding duels like L2OFF
		if (getClan() != null && attacker != null && getClan().isMember(attacker.getName()) && !(getDuelState() == Duel.DUELSTATE_DUELLING && getDuelId() == ((L2PcInstance) attacker).getDuelId()))
		{
			return false;
		}
		
		// Ally check
		if (attacker instanceof L2PlayableInstance)
		{
			L2PcInstance player = null;
			if (attacker instanceof L2PcInstance)
			{
				player = (L2PcInstance) attacker;
			}
			else if (attacker instanceof L2Summon)
			{
				player = ((L2Summon) attacker).getOwner();
			}
			
			// Check if the attacker is not in the same ally, excluding duels like L2OFF
			if (player != null && getAllyId() != 0 && player.getAllyId() != 0 && getAllyId() == player.getAllyId() && !(getDuelState() == Duel.DUELSTATE_DUELLING && getDuelId() == player.getDuelId()))
			{
				return false;
			}
		}
		
		if (attacker instanceof L2PlayableInstance && isInFunEvent())
		{
			
			L2PcInstance player = null;
			if (attacker instanceof L2PcInstance)
			{
				player = (L2PcInstance) attacker;
			}
			else if (attacker instanceof L2Summon)
			{
				player = ((L2Summon) attacker).getOwner();
			}
			
			if (player != null)
			{
				
				if (player.isInFunEvent())
				{
					
					// checks for events
					if (inEventTvT && player.inEventTvT && TvT.isStarted() && !teamNameTvT.equals(player.teamNameTvT) || inEventCTF && player.inEventCTF && CTF.isStarted() && !teamNameCTF.equals(player.teamNameCTF) || inEventDM && player.inEventDM && DM.isStarted())
					{
						return true;
					}
					return false;
				}
				return false;
			}
		}
		
		if (L2Character.isInsidePeaceZone(attacker, this))
		{
			return false;
		}
		
		// Check if the L2PcInstance has Karma
		if (getKarma() > 0 || getPvpFlag() > 0)
		{
			return true;
		}
		
		// Check if the attacker is a L2PcInstance
		if (attacker instanceof L2PcInstance)
		{
			// is AutoAttackable if both players are in the same duel and the duel is still going on
			if (getDuelState() == Duel.DUELSTATE_DUELLING && getDuelId() == ((L2PcInstance) attacker).getDuelId())
			{
				return true;
			}
			
			// Check if the L2PcInstance is in ArenaZone or SiegeZone
			if (isInsideZone(ZONE_PVP) && ((L2PcInstance) attacker).isInsideZone(ZONE_PVP))
			{
				return true;
			}
			
			if (getClan() != null)
			{
				Siege siege = SiegeManager.getInstance().getSiege(getX(), getY(), getZ());
				FortSiege fortsiege = FortSiegeManager.getInstance().getSiege(getX(), getY(), getZ());
				if (siege != null)
				{
					// Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Defender clan
					if (siege.checkIsDefender(((L2PcInstance) attacker).getClan()) && siege.checkIsDefender(getClan()))
					{
						siege = null;
						return false;
					}
					
					// Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Attacker clan
					if (siege.checkIsAttacker(((L2PcInstance) attacker).getClan()) && siege.checkIsAttacker(getClan()))
					{
						siege = null;
						return false;
					}
				}
				if (fortsiege != null)
				{
					// Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Defender clan
					if (fortsiege.checkIsDefender(((L2PcInstance) attacker).getClan()) && fortsiege.checkIsDefender(getClan()))
					{
						fortsiege = null;
						return false;
					}
					
					// Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Attacker clan
					if (fortsiege.checkIsAttacker(((L2PcInstance) attacker).getClan()) && fortsiege.checkIsAttacker(getClan()))
					{
						fortsiege = null;
						return false;
					}
				}
				
				// Check if clan is at war
				if (getClan() != null && ((L2PcInstance) attacker).getClan() != null && getClan().isAtWarWith(((L2PcInstance) attacker).getClanId()) && getWantsPeace() == 0 && ((L2PcInstance) attacker).getWantsPeace() == 0 && !isAcademyMember())
				{
					return true;
				}
			}
			
		}
		else if (attacker instanceof L2SiegeGuardInstance)
		{
			if (getClan() != null)
			{
				final Siege siege = SiegeManager.getInstance().getSiege(this);
				return siege != null && siege.checkIsAttacker(getClan()) || DevastatedCastle.getInstance().getIsInProgress();
			}
		}
		else if (attacker instanceof L2FortSiegeGuardInstance)
		{
			if (getClan() != null)
			{
				final FortSiege fortsiege = FortSiegeManager.getInstance().getSiege(this);
				return fortsiege != null && fortsiege.checkIsAttacker(getClan());
			}
		}
		
		return false;
	}
	
	/**
	 * Check if the active L2Skill can be casted.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Check if the skill isn't toggle and is offensive</li>
	 * <li>Check if the target is in the skill cast range</li>
	 * <li>Check if the skill is Spoil type and if the target isn't already spoiled</li>
	 * <li>Check if the caster owns enought consummed Item, enough HP and MP to cast the skill</li>
	 * <li>Check if the caster isn't sitting</li>
	 * <li>Check if all skills are enabled and this skill is enabled</li><BR>
	 * <BR>
	 * <li>Check if the caster own the weapon needed</li><BR>
	 * <BR>
	 * <li>Check if the skill is active</li><BR>
	 * <BR>
	 * <li>Check if all casting conditions are completed</li><BR>
	 * <BR>
	 * <li>Notify the AI with AI_INTENTION_CAST and target</li><BR>
	 * <BR>
	 * @param skill    The L2Skill to use
	 * @param forceUse used to force ATTACK on players
	 * @param dontMove used to prevent movement, if not in range
	 */
	public void useMagic(final L2Skill skill, final boolean forceUse, final boolean dontMove)
	{
		if (isDead())
		{
			abortCast();
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (skill == null)
		{
			abortCast();
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final int skill_id = skill.getId();
		int curr_skill_id = -1;
		SkillDat current = null;
		if ((current = getCurrentSkill()) != null)
		{
			curr_skill_id = current.getSkillId();
		}
		
		/*
		 * if (isWearingFormalWear() && !skill.isPotion()) { sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR)); sendPacket(ActionFailed.STATIC_PACKET); abortCast(); return; }
		 */
		if (inObserverMode())
		{
			sendPacket(new SystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
			abortCast();
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the caster is sitting
		if (isSitting() && !skill.isPotion())
		{
			// Send a System Message to the caster
			sendPacket(new SystemMessage(SystemMessageId.CANT_MOVE_SITTING));
			
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the skill type is TOGGLE
		if (skill.isToggle())
		{
			// Like L2OFF you can't use fake death if you are mounted
			if (skill.getId() == 60 && isMounted())
			{
				return;
			}
			
			// Get effects of the skill
			final L2Effect effect = getFirstEffect(skill);
			
			// Like L2OFF toogle skills have little delay
			if (TOGGLE_USE != 0 && TOGGLE_USE + 400 > System.currentTimeMillis())
			{
				TOGGLE_USE = 0;
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			TOGGLE_USE = System.currentTimeMillis();
			
			if (effect != null)
			{
				// fake death exception
				if (skill.getId() != 60)
				{
					effect.exit(false);
				}
				
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// Check if the skill is active
		if (skill.isPassive())
		{
			// just ignore the passive skill request. why does the client send it anyway ??
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if skill is in reause time
		if (isSkillDisabled(skill))
		{
			if (!(skill.getId() == 2166))
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
				sm.addSkillName(skill.getId(), skill.getLevel());
				sendPacket(sm);
				sm = null;
			}
			// Cp potion message like L2OFF
			else if (skill.getId() == 2166)
			{
				if (skill.getLevel() == 2)
				{
					sendMessage("Greater CP Potion is not available at this time: being prepared for reuse.");
				}
				else if (skill.getLevel() == 1)
				{
					sendMessage("CP Potion is not available at this time: being prepared for reuse.");
				}
			}
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if it's ok to summon
		// siege golem (13), Wild Hog Cannon (299), Swoop Cannon (448)
		if ((skill_id == 13 || skill_id == 299 || skill_id == 448) && !SiegeManager.getInstance().checkIfOkToSummon(this, false) && !FortSiegeManager.getInstance().checkIfOkToSummon(this, false))
		{
			return;
		}
		
		// ************************************* Check Casting in Progress *******************************************
		
		// If a skill is currently being used, queue this one if this is not the same
		// Note that this check is currently imperfect: getCurrentSkill() isn't always null when a skill has
		// failed to cast, or the casting is not yet in progress when this is rechecked
		if (curr_skill_id != -1 && (isCastingNow() || isCastingPotionNow()))
		{
			final SkillDat currentSkill = getCurrentSkill();
			// Check if new skill different from current skill in progress
			if (currentSkill != null && skill.getId() == currentSkill.getSkillId())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (Config.DEBUG && getQueuedSkill() != null)
			{
				LOGGER.info(getQueuedSkill().getSkill().getName() + " is already queued for " + getName() + ".");
			}
			
			// Create a new SkillDat object and queue it in the player queuedSkill
			setQueuedSkill(skill, forceUse, dontMove);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Create a new SkillDat object and set the player currentSkill
		// This is used mainly to save & queue the button presses, since L2Character has
		// lastSkillCast which could otherwise replace it
		setCurrentSkill(skill, forceUse, dontMove);
		
		if (getQueuedSkill() != null)
		{
			setQueuedSkill(null, false, false);
		}
		
		if (!EngineModsManager.onUseSkill(this, skill))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// triggered skills cannot be used directly
		if (triggeredSkills.size() > 0)
		{
			
			if (Config.DEBUG)
			{
				LOGGER.info("Checking if Triggherable Skill: " + skill.getId());
				LOGGER.info("Saved Triggherable Skills");
				
				for (final Integer skillId : triggeredSkills.keySet())
				{
					LOGGER.info(skillId);
				}
				
			}
			
			if (triggeredSkills.get(skill.getId()) != null)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// ************************************* Check Target *******************************************
		// Create and set a L2Object containing the target of the skill
		L2Object target = null;
		final SkillTargetType sklTargetType = skill.getTargetType();
		final SkillType sklType = skill.getSkillType();
		
		switch (sklTargetType)
		{
			// Target the player if skill type is AURA, PARTY, CLAN or SELF
			case TARGET_AURA:
				if (isInOlympiadMode() && !isInOlympiadFight())
				{
					setTarget(this);
				}
			case TARGET_PARTY:
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_GROUND:
			case TARGET_SELF:
				target = this;
				break;
			case TARGET_PET:
				target = getPet();
				break;
			default:
				target = getTarget();
				break;
		}
		
		// Check the validity of the target
		if (target == null)
		{
			sendPacket(new SystemMessage(SystemMessageId.TARGET_CANT_FOUND));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// skills can be used on Walls and Doors only durring siege
		// Ignore skill UNLOCK
		if (skill.isOffensive() && target instanceof L2DoorInstance)
		{
			final boolean isCastle = ((L2DoorInstance) target).getCastle() != null && ((L2DoorInstance) target).getCastle().getCastleId() > 0 && ((L2DoorInstance) target).getCastle().getSiege().getIsInProgress();
			final boolean isFort = ((L2DoorInstance) target).getFort() != null && ((L2DoorInstance) target).getFort().getFortId() > 0 && ((L2DoorInstance) target).getFort().getSiege().getIsInProgress();
			if (!isCastle && !isFort)
			{
				return;
			}
		}
		
		// Like L2OFF you can't heal random purple people without using CTRL
		final SkillDat skilldat = getCurrentSkill();
		if (skilldat != null && skill.getSkillType() == SkillType.HEAL && !skilldat.isCtrlPressed() && target instanceof L2PcInstance && ((L2PcInstance) target).getPvpFlag() == 1 && this != target)
		{
			if (getClanId() == 0 || ((L2PcInstance) target).getClanId() == 0 || getClanId() != ((L2PcInstance) target).getClanId())
			{
				if (getAllyId() == 0 || ((L2PcInstance) target).getAllyId() == 0 || getAllyId() != ((L2PcInstance) target).getAllyId())
				{
					if (getParty() == null || ((L2PcInstance) target).getParty() == null || !getParty().equals(((L2PcInstance) target).getParty()))
					{
						sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
			}
		}
		
		// Are the target and the player in the same duel?
		if (isInDuel())
		{
			if (!(target instanceof L2PcInstance && ((L2PcInstance) target).getDuelId() == getDuelId()) && !(target instanceof L2SummonInstance && ((L2Summon) target).getOwner().getDuelId() == getDuelId()))
			{
				sendMessage("You cannot do this while duelling.");
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// Pk protection config
		if (skill.isOffensive() && !isGM() && target instanceof L2PcInstance && ((L2PcInstance) target).getPvpFlag() == 0 && ((L2PcInstance) target).getKarma() == 0 && (getLevel() < Config.ALT_PLAYER_PROTECTION_LEVEL || ((L2PcInstance) target).getLevel() < Config.ALT_PLAYER_PROTECTION_LEVEL))
		{
			sendMessage("You can't hit a player that is lower level from you. Target's level: " + String.valueOf(Config.ALT_PLAYER_PROTECTION_LEVEL) + ".");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// ************************************* Check skill availability *******************************************
		
		// Check if this skill is enabled (ex : reuse time)
		// if(isSkillDisabled(skill_id) /* && !getAccessLevel().allowPeaceAttack() */)
		// {
		// SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_NOT_AVAILABLE);
		// sm.addString(skill.getName());
		// sendPacket(sm);
		//
		// Send a Server->Client packet ActionFailed to the L2PcInstance
		// sendPacket(ActionFailed.STATIC_PACKET);
		// return;
		// }
		
		// Check if all skills are disabled
		if (isAllSkillsDisabled() && !getAccessLevel().allowPeaceAttack())
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// prevent casting signets to peace zone
		if (skill.getSkillType() == SkillType.SIGNET || skill.getSkillType() == SkillType.SIGNET_CASTTIME)
		{
			if (isInsidePeaceZone(this))
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(skill_id);
				sendPacket(sm);
				return;
			}
		}
		// ************************************* Check Consumables *******************************************
		
		// Check if the caster has enough MP
		if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
		{
			// Send a System Message to the caster
			sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_MP));
			
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the caster has enough HP
		if (getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_HP));
			
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the spell consummes an Item
		if (skill.getItemConsume() > 0)
		{
			// Get the L2ItemInstance consummed by the spell
			final L2ItemInstance requiredItems = getInventory().getItemByItemId(skill.getItemConsumeId());
			
			// Check if the caster owns enought consummed Item to cast
			if (requiredItems == null || requiredItems.getCount() < skill.getItemConsume())
			{
				// Checked: when a summon skill failed, server show required consume item count
				if (sklType == L2Skill.SkillType.SUMMON)
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.SUMMONING_SERVITOR_COSTS_S2_S1);
					sm.addItemName(skill.getItemConsumeId());
					sm.addNumber(skill.getItemConsume());
					sendPacket(sm);
				}
				else
				{
					// Send a System Message to the caster
					sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
				}
				return;
			}
		}
		
		// Like L2OFF if you are mounted on wyvern you can't use own skills
		if (isFlying())
		{
			if (skill_id != 327 && skill_id != 4289 && !skill.isPotion())
			{
				sendMessage("You cannot use skills while riding a wyvern.");
				return;
			}
		}
		
		// Like L2OFF if you have a summon you can't summon another one (ignore cubics)
		if (sklType == L2Skill.SkillType.SUMMON && skill instanceof L2SkillSummon && !((L2SkillSummon) skill).isCubic())
		{
			if (getPet() != null || isMounted())
			{
				sendPacket(new SystemMessage(SystemMessageId.YOU_ALREADY_HAVE_A_PET));
				return;
			}
		}
		
		if (skill.getNumCharges() > 0 && skill.getSkillType() != SkillType.CHARGE && skill.getSkillType() != SkillType.CHARGEDAM && skill.getSkillType() != SkillType.CHARGE_EFFECT && skill.getSkillType() != SkillType.PDAM)
		{
			final EffectCharge effect = (EffectCharge) getFirstEffect(L2Effect.EffectType.CHARGE);
			if (effect == null || effect.numCharges < skill.getNumCharges())
			{
				sendPacket(new SystemMessage(SystemMessageId.SKILL_NOT_AVAILABLE));
				return;
			}
			
			effect.numCharges -= skill.getNumCharges();
			sendPacket(new EtcStatusUpdate(this));
			
			if (effect.numCharges == 0)
			{
				effect.exit(false);
			}
		}
		// ************************************* Check Casting Conditions *******************************************
		
		// Check if the caster own the weapon needed
		if (!skill.getWeaponDependancy(this))
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		// Check if all casting conditions are completed
		if (!skill.checkCondition(this, target, false))
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// ************************************* Check Player State *******************************************
		
		// Abnormal effects(ex : Stun, Sleep...) are checked in L2Character useMagic()
		
		// Check if the player use "Fake Death" skill
		if (isAlikeDead() && !skill.isPotion() && skill.getSkillType() != L2Skill.SkillType.FAKE_DEATH)
		{
			
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (isFishing() && sklType != SkillType.PUMPING && sklType != SkillType.REELING && sklType != SkillType.FISHING)
		{
			// Only fishing skills are available
			sendPacket(new SystemMessage(SystemMessageId.ONLY_FISHING_SKILLS_NOW));
			return;
		}
		
		// ************************************* Check Skill Type *******************************************
		
		// Check if this is offensive magic skill
		if (skill.isOffensive())
		{
			final Boolean peace = isInsidePeaceZone(this, target);
			
			if (peace && skill.getId() != 3261 // Like L2OFF you can use cupid bow skills on peace zone
				&& skill.getId() != 3260 && skill.getId() != 3262 && sklTargetType != SkillTargetType.TARGET_AURA) // Like L2OFF people can use TARGET_AURE skills on peace zone
			{
				// If L2Character or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
				sendPacket(new SystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (isInOlympiadMode() && !isInOlympiadFight() && sklTargetType != SkillTargetType.TARGET_AURA)
			{
				// if L2PcInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (!(target instanceof L2MonsterInstance) && sklType == SkillType.CONFUSE_MOB_ONLY)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Check if a Forced ATTACK is in progress on non-attackable target
			if (!target.isAutoAttackable(this) && !forceUse && skill.getId() != 3261 && skill.getId() != 3260 && skill.getId() != 3262 && !(inEventTvT && TvT.isStarted()) && !(inEventDM && DM.isStarted()) && !(inEventCTF && CTF.isStarted()) && sklTargetType != SkillTargetType.TARGET_AURA && sklTargetType != SkillTargetType.TARGET_CLAN && sklTargetType != SkillTargetType.TARGET_ALLY
				&& sklTargetType != SkillTargetType.TARGET_PARTY && sklTargetType != SkillTargetType.TARGET_SELF && sklTargetType != SkillTargetType.TARGET_GROUND)
			
			{
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Check if the target is in the skill cast range
			if (dontMove)
			{
				// Calculate the distance between the L2PcInstance and the target
				if (sklTargetType == SkillTargetType.TARGET_GROUND)
				{
					if (!isInsideRadius(getCurrentSkillWorldPosition().getX(), getCurrentSkillWorldPosition().getY(), getCurrentSkillWorldPosition().getZ(), (int) (skill.getCastRange() + getTemplate().getCollisionRadius()), false, false))
					{
						// Send a System Message to the caster
						sendPacket(SystemMessageId.TARGET_TOO_FAR);
						
						// Send a Server->Client packet ActionFailed to the L2PcInstance
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
				else if (skill.getCastRange() > 0 && !isInsideRadius(target, skill.getCastRange() + getTemplate().collisionRadius, false, false)) // Calculate the distance between the L2PcInstance and the target
				{
					// Send a System Message to the caster
					sendPacket(new SystemMessage(SystemMessageId.TARGET_TOO_FAR));
					
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			else if (sklType == SkillType.SIGNET) // Check range for SIGNET skills
			{
				if (!isInsideRadius(getCurrentSkillWorldPosition().getX(), getCurrentSkillWorldPosition().getY(), getCurrentSkillWorldPosition().getZ(), (int) (skill.getCastRange() + getTemplate().getCollisionRadius()), false, false))
				{
					// Send a System Message to the caster
					sendPacket(SystemMessageId.TARGET_TOO_FAR);
					
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		// Check if the skill is defensive
		if (!skill.isOffensive())
		{
			// check if the target is a monster and if force attack is set.. if not then we don't want to cast.
			if (target instanceof L2MonsterInstance && !forceUse && sklTargetType != SkillTargetType.TARGET_PET && sklTargetType != SkillTargetType.TARGET_AURA && sklTargetType != SkillTargetType.TARGET_CLAN && sklTargetType != SkillTargetType.TARGET_SELF && sklTargetType != SkillTargetType.TARGET_PARTY && sklTargetType != SkillTargetType.TARGET_ALLY
				&& sklTargetType != SkillTargetType.TARGET_CORPSE_MOB && sklTargetType != SkillTargetType.TARGET_AREA_CORPSE_MOB && sklTargetType != SkillTargetType.TARGET_GROUND && sklType != SkillType.BEAST_FEED && sklType != SkillType.DELUXE_KEY_UNLOCK && sklType != SkillType.UNLOCK)
			{
				// send the action failed so that the skill doens't go off.
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// Check if the skill is Spoil type and if the target isn't already spoiled
		if (sklType == SkillType.SPOIL)
		{
			if (!(target instanceof L2MonsterInstance))
			{
				// Send a System Message to the L2PcInstance
				sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// Check if the skill is Sweep type and if conditions not apply
		if (sklType == SkillType.SWEEP && target instanceof L2Attackable)
		{
			final int spoilerId = ((L2Attackable) target).getIsSpoiledBy();
			
			if (((L2Attackable) target).isDead())
			{
				if (!((L2Attackable) target).isSpoil())
				{
					// Send a System Message to the L2PcInstance
					sendPacket(new SystemMessage(SystemMessageId.SWEEPER_FAILED_TARGET_NOT_SPOILED));
					
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				if (getObjectId() != spoilerId && !isInLooterParty(spoilerId))
				{
					// Send a System Message to the L2PcInstance
					sendPacket(new SystemMessage(SystemMessageId.SWEEP_NOT_ALLOWED));
					
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		// Check if the skill is Drain Soul (Soul Crystals) and if the target is a MOB
		if (sklType == SkillType.DRAIN_SOUL)
		{
			if (!(target instanceof L2MonsterInstance))
			{
				// Send a System Message to the L2PcInstance
				sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		final Point3D worldPosition = getCurrentSkillWorldPosition();
		
		if (sklTargetType == SkillTargetType.TARGET_GROUND && worldPosition == null)
		{
			LOGGER.info("WorldPosition is null for skill: " + skill.getName() + ", player: " + getName() + ".");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if this is a Pvp skill and target isn't a non-flagged/non-karma player
		switch (sklTargetType)
		{
			case TARGET_PARTY:
			case TARGET_ALLY: // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
			case TARGET_CLAN: // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
			case TARGET_AURA:
			case TARGET_SELF:
			case TARGET_GROUND:
				break;
			default:
				// if pvp skill is not allowed for given target
				if (!checkPvpSkill(target, skill) && !getAccessLevel().allowPeaceAttack() && skill.getId() != 3261 && skill.getId() != 3260 && skill.getId() != 3262)
				{
					// Send a System Message to the L2PcInstance
					sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
		}
		
		if (sklTargetType == SkillTargetType.TARGET_HOLY && !TakeCastle.checkIfOkToCastSealOfRule(this, false))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			abortCast();
			return;
		}
		
		if (sklType == SkillType.SIEGEFLAG && !SiegeFlag.checkIfOkToPlaceFlag(this, false))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			abortCast();
			return;
		}
		else if (sklType == SkillType.STRSIEGEASSAULT && !StrSiegeAssault.checkIfOkToUseStriderSiegeAssault(this, false))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			abortCast();
			return;
		}
		
		/*
		 * TEMPFIX: Check client Z coordinate instead of server z to avoid exploit killing Zaken from others floor
		 */
		if (target instanceof L2GrandBossInstance && ((L2GrandBossInstance) target).getNpcId() == 29022)
		{
			if (Math.abs(getClientZ() - target.getZ()) > 200)
			{
				sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// GeoData Los Check here
		if (skill.getCastRange() > 0 && !GeoData.getInstance().canSeeTarget(this, target))
		{
			sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// If all conditions are checked, create a new SkillDat object and set the player currentSkill
		setCurrentSkill(skill, forceUse, dontMove);
		
		// Check if the active L2Skill can be casted (ex : not sleeping...), Check if the target is correct and Notify the AI with AI_INTENTION_CAST and target
		super.useMagic(skill);
	}
	
	/**
	 * Checks if is in looter party.
	 * @param  LooterId the looter id
	 * @return          true, if is in looter party
	 */
	public boolean isInLooterParty(final int LooterId)
	{
		final L2PcInstance looter = L2World.getInstance().getPlayer(LooterId);
		
		// if L2PcInstance is in a CommandChannel
		if (isInParty() && getParty().isInCommandChannel() && looter != null)
		{
			return getParty().getCommandChannel().getMembers().contains(looter);
		}
		
		if (isInParty() && looter != null)
		{
			return getParty().getPartyMembers().contains(looter);
		}
		
		return false;
	}
	
	/**
	 * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition.
	 * @param  target L2Object instance containing the target
	 * @param  skill  L2Skill instance with the skill being casted
	 * @return        False if the skill is a pvpSkill and target is not a valid pvp target
	 */
	public boolean checkPvpSkill(final L2Object target, final L2Skill skill)
	{
		return checkPvpSkill(target, skill, false);
	}
	
	/**
	 * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition.
	 * @param  target      L2Object instance containing the target
	 * @param  skill       L2Skill instance with the skill being casted
	 * @param  srcIsSummon is L2Summon - caster?
	 * @return             False if the skill is a pvpSkill and target is not a valid pvp target
	 */
	public boolean checkPvpSkill(L2Object target, final L2Skill skill, final boolean srcIsSummon)
	{
		// Check if player and target are in events and on the same team.
		if (target instanceof L2PcInstance)
		{
			if (skill.isOffensive() && inEventTvT && ((L2PcInstance) target).inEventTvT && TvT.isStarted() && !teamNameTvT.equals(((L2PcInstance) target).teamNameTvT) || inEventCTF && ((L2PcInstance) target).inEventCTF && CTF.isStarted() && !teamNameCTF.equals(((L2PcInstance) target).teamNameCTF) || inEventDM && ((L2PcInstance) target).inEventDM && DM.isStarted())
			{
				return true;
			}
			else if (isInFunEvent() && skill.isOffensive()) // same team return false
			{
				return false;
			}
		}
		
		// check for PC->PC Pvp status
		if (target instanceof L2Summon)
		{
			target = ((L2Summon) target).getOwner();
		}
		
		if (target != null && // target not null and
			target != this && // target is not self and
			target instanceof L2PcInstance && // target is L2PcInstance and
			!(isInDuel() && ((L2PcInstance) target).getDuelId() == getDuelId()) && // self is not in a duel and attacking opponent
			!isInsideZone(ZONE_PVP) && // Pc is not in PvP zone
			!((L2PcInstance) target).isInsideZone(ZONE_PVP) // target is not in PvP zone
		)
		{
			final SkillDat skilldat = getCurrentSkill();
			// SkillDat skilldatpet = getCurrentPetSkill();
			if (skill.isPvpSkill()) // pvp skill
			{
				if (getClan() != null && ((L2PcInstance) target).getClan() != null)
				{
					if (getClan().isAtWarWith(((L2PcInstance) target).getClan().getClanId()) && ((L2PcInstance) target).getClan().isAtWarWith(getClan().getClanId()))
					{
						return true; // in clan war player can attack whites even with sleep etc.
					}
				}
				if (((L2PcInstance) target).getPvpFlag() == 0 && // target's pvp flag is not set and
					((L2PcInstance) target).getKarma() == 0 // target has no karma
				)
				{
					return false;
				}
			}
			else if (skilldat != null && !skilldat.isCtrlPressed() && skill.isOffensive() && !srcIsSummon
			/* || (skilldatpet != null && !skilldatpet.isCtrlPressed() && skill.isOffensive() && srcIsSummon) */)
			{
				if (getClan() != null && ((L2PcInstance) target).getClan() != null)
				{
					if (getClan().isAtWarWith(((L2PcInstance) target).getClan().getClanId()) && ((L2PcInstance) target).getClan().isAtWarWith(getClan().getClanId()))
					{
						return true; // in clan war player can attack whites even without ctrl
					}
				}
				if (((L2PcInstance) target).getPvpFlag() == 0 && // target's pvp flag is not set and
					((L2PcInstance) target).getKarma() == 0 // target has no karma
				)
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Reduce Item quantity of the L2PcInstance Inventory and send it a Server->Client packet InventoryUpdate.<BR>
	 * <BR>
	 * @param itemConsumeId the item consume id
	 * @param itemCount     the item count
	 */
	@Override
	public void consumeItem(final int itemConsumeId, final int itemCount)
	{
		if (itemConsumeId != 0 && itemCount != 0)
		{
			destroyItemByItemId("Consume", itemConsumeId, itemCount, null, true);
		}
	}
	
	/**
	 * Return True if the L2PcInstance is a Mage.<BR>
	 * <BR>
	 * @return true, if is mage class
	 */
	public boolean isMageClass()
	{
		return getClassId().isMage();
	}
	
	/**
	 * Checks if is mounted.
	 * @return true, if is mounted
	 */
	public boolean isMounted()
	{
		return mountType > 0;
	}
	
	/**
	 * Set the type of Pet mounted (0 : none, 1 : Stridder, 2 : Wyvern) and send a Server->Client packet InventoryUpdate to the L2PcInstance.<BR>
	 * <BR>
	 * @return true, if successful
	 */
	public boolean checkLandingState()
	{
		// Check if char is in a no landing zone
		if (isInsideZone(ZONE_NOLANDING))
		{
			return true;
		}
		else
		// if this is a castle that is currently being sieged, and the rider is NOT a castle owner
		// he cannot land.
		// castle owner is the leader of the clan that owns the castle where the pc is
		if (isInsideZone(ZONE_SIEGE) && !(getClan() != null && CastleManager.getInstance().getCastle(this) == CastleManager.getInstance().getCastleByOwner(getClan()) && this == getClan().getLeader().getPlayerInstance()))
		{
			return true;
		}
		
		return false;
	}
	
	// returns false if the change of mount type fails.
	/**
	 * Sets the mount type.
	 * @param  mountType the mount type
	 * @return           true, if successful
	 */
	public boolean setMountType(final int mountType)
	{
		if (checkLandingState() && mountType == 2)
		{
			return false;
		}
		
		switch (mountType)
		{
			case 0:
				setIsFlying(false);
				setIsRiding(false);
				break; // Dismounted
			case 1:
				setIsRiding(true);
				if (isNoble())
				{
					final L2Skill striderAssaultSkill = SkillTable.getInstance().getInfo(325, 1);
					addSkill(striderAssaultSkill, false); // not saved to DB
				}
				break;
			case 2:
				setIsFlying(true);
				break; // Flying Wyvern
		}
		
		this.mountType = mountType;
		
		// Send a Server->Client packet InventoryUpdate to the L2PcInstance in order to update speed
		UserInfo ui = new UserInfo(this);
		sendPacket(ui);
		ui = null;
		return true;
	}
	
	/**
	 * Return the type of Pet mounted (0 : none, 1 : Stridder, 2 : Wyvern).<BR>
	 * <BR>
	 * @return the mount type
	 */
	public int getMountType()
	{
		return mountType;
	}
	
	/**
	 * Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its knownPlayers.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Others L2PcInstance in the detection area of the L2PcInstance are identified in <B>_knownPlayers</B>. In order to inform other players of this L2PcInstance state modifications, server just need to go through knownPlayers to send Server->Client Packet<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client packet UserInfo to this L2PcInstance (Public and Private Data)</li>
	 * <li>Send a Server->Client packet CharInfo to all L2PcInstance in knownPlayers of the L2PcInstance (Public data only)</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : DON'T SEND UserInfo packet to other players instead of CharInfo packet. Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</B></FONT><BR>
	 * <BR>
	 */
	@Override
	public void updateAbnormalEffect()
	{
		broadcastUserInfo();
	}
	
	/**
	 * Disable the Inventory and create a new task to enable it after 1.5s.<BR>
	 * <BR>
	 */
	public void tempInvetoryDisable()
	{
		inventoryDisable = true;
		
		ThreadPoolManager.getInstance().scheduleGeneral(new InventoryEnable(), 1500);
	}
	
	/**
	 * Return True if the Inventory is disabled.<BR>
	 * <BR>
	 * @return true, if is invetory disabled
	 */
	public boolean isInvetoryDisabled()
	{
		return inventoryDisable;
	}
	
	/**
	 * The Class InventoryEnable.
	 */
	class InventoryEnable implements Runnable
	{
		
		@Override
		public void run()
		{
			inventoryDisable = false;
		}
	}
	
	/**
	 * Gets the cubics.
	 * @return the cubics
	 */
	public Map<Integer, L2CubicInstance> getCubics()
	{
		synchronized (cubics)
		{
			// clean cubics instances
			final Set<Integer> cubicsIds = cubics.keySet();
			
			for (final Integer id : cubicsIds)
			{
				if (id == null || cubics.get(id) == null)
				{
					try
					{
						cubics.remove(id);
					}
					catch (final NullPointerException e)
					{
						// FIXME: tried to remove a null key, to be found where this action has been performed (DEGUB)
					}
				}
			}
			
			return cubics;
		}
	}
	
	/**
	 * Add a L2CubicInstance to the L2PcInstance cubics.<BR>
	 * <BR>
	 * @param id               the id
	 * @param level            the level
	 * @param matk             the matk
	 * @param activationtime   the activationtime
	 * @param activationchance the activationchance
	 * @param totalLifetime    the total lifetime
	 * @param givenByOther     the given by other
	 */
	/*
	 * public void addCubic(int id, int level, double d) { L2CubicInstance cubic = new L2CubicInstance(this, id, level,d); cubics.put(id, cubic); cubic = null; }
	 */
	
	public void addCubic(final int id, final int level, final double matk, final int activationtime, final int activationchance, final int totalLifetime, final boolean givenByOther)
	{
		if (Config.DEBUG)
		{
			LOGGER.info("L2PcInstance(" + getName() + "): addCubic(" + id + "|" + level + "|" + matk + ")");
		}
		final L2CubicInstance cubic = new L2CubicInstance(this, id, level, (int) matk, activationtime, activationchance, totalLifetime, givenByOther);
		
		synchronized (cubics)
		{
			cubics.put(id, cubic);
		}
		
	}
	
	/**
	 * Remove a L2CubicInstance from the L2PcInstance cubics.<BR>
	 * <BR>
	 * @param id the id
	 */
	public void delCubic(final int id)
	{
		synchronized (cubics)
		{
			cubics.remove(id);
		}
		
	}
	
	/**
	 * Return the L2CubicInstance corresponding to the Identifier of the L2PcInstance cubics.<BR>
	 * <BR>
	 * @param  id the id
	 * @return    the cubic
	 */
	public L2CubicInstance getCubic(final int id)
	{
		synchronized (cubics)
		{
			return cubics.get(id);
		}
		
	}
	
	public void unsummonAllCubics()
	{
		
		// Unsummon Cubics
		synchronized (cubics)
		{
			
			if (cubics.size() > 0)
			{
				for (final L2CubicInstance cubic : cubics.values())
				{
					cubic.stopAction();
					cubic.cancelDisappear();
				}
				
				cubics.clear();
			}
			
		}
		
	}
	
	@Override
	public String toString()
	{
		return "player " + getName();
	}
	
	/**
	 * Return the modifier corresponding to the Enchant Effect of the Active Weapon (Min : 127).<BR>
	 * <BR>
	 * @return the enchant effect
	 */
	public int getEnchantEffect()
	{
		final L2ItemInstance wpn = getActiveWeaponInstance();
		
		if (wpn == null)
		{
			return 0;
		}
		
		return Math.min(127, wpn.getEnchantLevel());
	}
	
	/**
	 * Set the lastFolkNpc of the L2PcInstance corresponding to the last Folk wich one the player talked.<BR>
	 * <BR>
	 * @param folkNpc the new last folk npc
	 */
	public void setLastFolkNPC(final L2FolkInstance folkNpc)
	{
		lastFolkNpc = folkNpc;
	}
	
	/**
	 * Return the lastFolkNpc of the L2PcInstance corresponding to the last Folk wich one the player talked.<BR>
	 * <BR>
	 * @return the last folk npc
	 */
	public L2FolkInstance getLastFolkNPC()
	{
		return lastFolkNpc;
	}
	
	/**
	 * Set the Silent Moving mode Flag.<BR>
	 * <BR>
	 * @param flag the new silent moving
	 */
	public void setSilentMoving(final boolean flag)
	{
		if (flag)
		{
			isSilentMoving++;
		}
		else
		{
			isSilentMoving--;
		}
	}
	
	/**
	 * Return True if the Silent Moving mode is active.<BR>
	 * <BR>
	 * @return true, if is silent moving
	 */
	public boolean isSilentMoving()
	{
		return isSilentMoving > 0;
	}
	
	/**
	 * Return True if L2PcInstance is a participant in the Festival of Darkness.<BR>
	 * <BR>
	 * @return true, if is festival participant
	 */
	public boolean isFestivalParticipant()
	{
		return SevenSignsFestival.getInstance().isPlayerParticipant(this);
	}
	
	/**
	 * Adds the auto soul shot.
	 * @param itemId the item id
	 */
	public void addAutoSoulShot(final int itemId)
	{
		activeSoulShots.put(itemId, itemId);
	}
	
	/**
	 * Removes the auto soul shot.
	 * @param itemId the item id
	 */
	public void removeAutoSoulShot(final int itemId)
	{
		activeSoulShots.remove(itemId);
	}
	
	/**
	 * Gets the auto soul shot.
	 * @return the auto soul shot
	 */
	public Map<Integer, Integer> getAutoSoulShot()
	{
		return activeSoulShots;
	}
	
	/**
	 * Recharge auto soul shot.
	 * @param physical the physical
	 * @param magic    the magic
	 * @param summon   the summon
	 */
	public void rechargeAutoSoulShot(final boolean physical, final boolean magic, final boolean summon)
	{
		L2ItemInstance item;
		IItemHandler handler;
		
		if (activeSoulShots == null || activeSoulShots.size() == 0)
		{
			return;
		}
		
		for (final int itemId : activeSoulShots.values())
		{
			item = getInventory().getItemByItemId(itemId);
			
			if (item != null)
			{
				if (magic)
				{
					if (!summon)
					{
						if (itemId == 2509 || itemId == 2510 || itemId == 2511 || itemId == 2512 || itemId == 2513 || itemId == 2514 || itemId == 3947 || itemId == 3948 || itemId == 3949 || itemId == 3950 || itemId == 3951 || itemId == 3952 || itemId == 5790)
						{
							handler = ItemHandler.getInstance().getItemHandler(itemId);
							
							if (handler != null)
							{
								handler.useItem(this, item);
							}
						}
					}
					else
					{
						if (itemId == 6646 || itemId == 6647)
						{
							handler = ItemHandler.getInstance().getItemHandler(itemId);
							
							if (handler != null)
							{
								handler.useItem(this, item);
							}
						}
					}
				}
				
				if (physical)
				{
					if (!summon)
					{
						if (itemId == 1463 || itemId == 1464 || itemId == 1465 || itemId == 1466 || itemId == 1467 || itemId == 1835 || itemId == 5789)
						{
							handler = ItemHandler.getInstance().getItemHandler(itemId);
							
							if (handler != null)
							{
								handler.useItem(this, item);
							}
						}
					}
					else
					{
						if (itemId == 6645)
						{
							handler = ItemHandler.getInstance().getItemHandler(itemId);
							
							if (handler != null)
							{
								handler.useItem(this, item);
							}
						}
					}
				}
			}
			else
			{
				removeAutoSoulShot(itemId);
			}
		}
		item = null;
		handler = null;
	}
	
	/**
	 * Recharge auto soul shot.
	 * @param physical the physical
	 * @param magic    the magic
	 * @param summon   the summon
	 * @param atkTime  TODO
	 */
	public void rechargeAutoSoulShot(final boolean physical, final boolean magic, final boolean summon, final int atkTime)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(() -> rechargeAutoSoulShot(physical, magic, summon), atkTime);
	}
	
	/** The task warn user take break. */
	private ScheduledFuture<?> taskWarnUserTakeBreak;
	
	class WarnUserTakeBreak implements Runnable
	{
		@Override
		public void run()
		{
			if (isOnline())
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.PLAYING_FOR_LONG_TIME);
				L2PcInstance.this.sendPacket(msg);
				msg = null;
			}
			else
			{
				stopWarnUserTakeBreak();
			}
		}
	}
	
	class RentPetTask implements Runnable
	{
		@Override
		public void run()
		{
			stopRentPet();
		}
	}
	
	/** The taskforfish. */
	public ScheduledFuture<?> taskforfish;
	
	class WaterTask implements Runnable
	{
		@Override
		public void run()
		{
			double reduceHp = getMaxHp() / 100.0;
			
			if (reduceHp < 1)
			{
				reduceHp = 1;
			}
			
			reduceCurrentHp(reduceHp, L2PcInstance.this, false);
			// reduced hp, becouse not rest
			SystemMessage sm = new SystemMessage(SystemMessageId.DROWN_DAMAGE_S1);
			sm.addNumber((int) reduceHp);
			sendPacket(sm);
			sm = null;
		}
	}
	
	/**
	 * The Class LookingForFishTask.
	 */
	class LookingForFishTask implements Runnable
	{
		
		/** The is upper grade. */
		boolean isNoob, isUpperGrade;
		
		/** The guts check time. */
		int fishType, fishGutsCheck, gutsCheckTime;
		
		/** The end task time. */
		long endTaskTime;
		
		/**
		 * Instantiates a new looking for fish task.
		 * @param fishWaitTime  the fish wait time
		 * @param fishGutsCheck the fish guts check
		 * @param fishType      the fish type
		 * @param isNoob        the is noob
		 * @param isUpperGrade  the is upper grade
		 */
		protected LookingForFishTask(final int fishWaitTime, final int fishGutsCheck, final int fishType, final boolean isNoob, final boolean isUpperGrade)
		{
			this.fishGutsCheck = fishGutsCheck;
			endTaskTime = System.currentTimeMillis() + fishWaitTime + 10000;
			this.fishType = fishType;
			this.isNoob = isNoob;
			this.isUpperGrade = isUpperGrade;
		}
		
		@Override
		public void run()
		{
			if (System.currentTimeMillis() >= endTaskTime)
			{
				EndFishing(false);
				return;
			}
			if (fishType == -1)
			{
				return;
			}
			final int check = Rnd.get(1000);
			if (fishGutsCheck > check)
			{
				stopLookingForFishTask();
				StartFishCombat(isNoob, isUpperGrade);
			}
		}
		
	}
	
	/**
	 * Gets the clan privileges.
	 * @return the clan privileges
	 */
	public int getClanPrivileges()
	{
		return clanPrivileges;
	}
	
	/**
	 * Sets the clan privileges.
	 * @param n the new clan privileges
	 */
	public void setClanPrivileges(final int n)
	{
		clanPrivileges = n;
	}
	
	// baron etc
	/**
	 * Sets the pledge class.
	 * @param classId the new pledge class
	 */
	public void setPledgeClass(final int classId)
	{
		pledgeClass = classId;
	}
	
	/**
	 * Gets the pledge class.
	 * @return the pledge class
	 */
	public int getPledgeClass()
	{
		return pledgeClass;
	}
	
	/**
	 * Sets the pledge type.
	 * @param typeId the new pledge type
	 */
	public void setPledgeType(final int typeId)
	{
		pledgeType = typeId;
	}
	
	/**
	 * Gets the pledge type.
	 * @return the pledge type
	 */
	public int getPledgeType()
	{
		return pledgeType;
	}
	
	/**
	 * Gets the apprentice.
	 * @return the apprentice
	 */
	public int getApprentice()
	{
		return apprentice;
	}
	
	/**
	 * Sets the apprentice.
	 * @param apprentice_id the new apprentice
	 */
	public void setApprentice(final int apprentice_id)
	{
		apprentice = apprentice_id;
	}
	
	/**
	 * Gets the sponsor.
	 * @return the sponsor
	 */
	public int getSponsor()
	{
		return sponsor;
	}
	
	/**
	 * Sets the sponsor.
	 * @param sponsor_id the new sponsor
	 */
	public void setSponsor(final int sponsor_id)
	{
		sponsor = sponsor_id;
	}
	
	/**
	 * Send message.
	 * @param message the message
	 */
	public void sendMessage(final String message)
	{
		sendPacket(SystemMessage.sendString(message));
	}
	
	/** The was invisible. */
	private boolean wasInvisible = false;
	
	/**
	 * Enter observer mode.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void enterObserverMode(final int x, final int y, final int z)
	{
		if (isInOlympiadMode())
		{
			sendMessage("You can not observe while you are in Olympiad.");
			return;
		}
		
		obsX = getX();
		obsY = getY();
		obsZ = getZ();
		
		// Unsummon pet while entering on Observer mode
		if (getPet() != null)
		{
			getPet().unSummon(this);
		}
		
		// Unsummon cubics while entering on Observer mode
		unsummonAllCubics();
		
		observerMode = true;
		setTarget(null);
		stopMove(null);
		setIsParalyzed(true);
		setIsInvul(true);
		
		wasInvisible = getAppearance().isInvisible();
		getAppearance().setInvisible();
		
		sendPacket(new ObservationMode(x, y, z));
		getKnownList().removeAllKnownObjects(); // reinit knownlist
		setXYZ(x, y, z);
		teleToLocation(x, y, z, false);
		broadcastUserInfo();
	}
	
	/**
	 * Enter olympiad observer mode.
	 * @param x  the x
	 * @param y  the y
	 * @param z  the z
	 * @param id the id
	 */
	public void enterOlympiadObserverMode(final int x, final int y, final int z, final int id)
	{
		if (isInOlympiadMode())
		{
			sendPacket(new SystemMessage(SystemMessageId.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME));
			return;
		}
		
		// Unsummon pet while entering on Observer mode
		if (getPet() != null)
		{
			getPet().unSummon(this);
		}
		
		// Unsummon cubics while entering on Observer mode
		unsummonAllCubics();
		
		if (getParty() != null)
		{
			getParty().removePartyMember(this);
		}
		
		olympiadGameId = id;
		
		if (isSitting())
		{
			standUp();
		}
		
		if (!observerMode)
		{
			obsX = getX();
			obsY = getY();
			obsZ = getZ();
		}
		
		observerMode = true;
		setTarget(null);
		setIsInvul(true);
		wasInvisible = getAppearance().isInvisible();
		getAppearance().setInvisible();
		
		teleToLocation(x, y, z, false);
		sendPacket(new ExOlympiadMode(3, this));
		broadcastUserInfo();
	}
	
	/**
	 * Leave observer mode.
	 */
	public void leaveObserverMode()
	{
		if (!observerMode)
		{
			LOGGER.warn("Player " + L2PcInstance.this.getName() + " request leave observer mode when he not use it!");
			Util.handleIllegalPlayerAction(L2PcInstance.this, "Warning!! Character " + L2PcInstance.this.getName() + " tried to cheat in observer mode.", Config.DEFAULT_PUNISH);
		}
		setTarget(null);
		setXYZ(obsX, obsY, obsZ);
		setIsParalyzed(false);
		
		if (wasInvisible)
		{
			getAppearance().setInvisible();
		}
		else
		{
			getAppearance().setVisible();
		}
		
		setIsInvul(false);
		
		if (getAI() != null)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		
		teleToLocation(obsX, obsY, obsZ, false);
		observerMode = false;
		sendPacket(new ObservationReturn(this));
		
		if (!wasInvisible)
		{
			broadcastUserInfo();
		}
	}
	
	/**
	 * Leave olympiad observer mode.
	 */
	public void leaveOlympiadObserverMode()
	{
		setTarget(null);
		sendPacket(new ExOlympiadMode(0, this));
		teleToLocation(obsX, obsY, obsZ, true);
		getAppearance().setVisible();
		setIsInvul(false);
		if (getAI() != null)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		Olympiad.getInstance();
		Olympiad.removeSpectator(olympiadGameId, this);
		olympiadGameId = -1;
		observerMode = false;
		
		if (!wasInvisible)
		{
			broadcastUserInfo();
		}
		
	}
	
	public void updateClanLeaderColor()
	{
		if (isClanLeader() && Config.CLAN_LEADER_COLOR_ENABLED && getClan().getLevel() >= Config.CLAN_LEADER_COLOR_CLAN_LEVEL)
		{
			if (Config.CLAN_LEADER_COLORED == 1)
			{
				getAppearance().setNameColor(Config.CLAN_LEADER_COLOR);
			}
			else
			{
				getAppearance().setTitleColor(Config.CLAN_LEADER_COLOR);
			}
		}
	}
	
	/**
	 * Update the name color and title color if character is AIO
	 */
	public void updateAIOColor()
	{
		if (Config.ENABLE_AIO_SYSTEM && isAio())
		{
			getAppearance().setNameColor(Config.AIO_NCOLOR);
			getAppearance().setTitleColor(Config.AIO_TCOLOR);
		}
	}
	
	/**
	 * Update the name color and title color if character is MARRIED
	 */
	public void updateMarriedColor()
	{
		if (Config.L2JMOD_ALLOW_WEDDING)
		{
			if (isMarried())
			{
				if (marriedType() == 1)
				{
					getAppearance().setNameColor(Config.L2JMOD_WEDDING_NAME_COLOR_LESBO);
				}
				else if (marriedType() == 2)
				{
					getAppearance().setNameColor(Config.L2JMOD_WEDDING_NAME_COLOR_GEY);
				}
				else
				{
					getAppearance().setNameColor(Config.L2JMOD_WEDDING_NAME_COLOR_NORMAL);
				}
			}
		}
	}
	
	/**
	 * Update the name color and title color if character is VIP
	 */
	public void updateVIPColor()
	{
		if (Config.VIP_NAME_COLOR_ENABLED && isVIP())
		{
			getAppearance().setNameColor(Config.VIP_NAME_COLOR);
			getAppearance().setTitleColor(Config.VIP_TITLE_COLOR);
		}
	}
	
	/**
	 * Set to player default name color and default title color
	 */
	public void updateDefaultColor()
	{
		getAppearance().setTitleColor(0x000000);
		getAppearance().setTitleColor(0xFFFF77);
	}
	
	/**
	 * Sets the olympiad side.
	 * @param i the new olympiad side
	 */
	public void setOlympiadSide(final int i)
	{
		olympiadSide = i;
	}
	
	/**
	 * Gets the olympiad side.
	 * @return the olympiad side
	 */
	public int getOlympiadSide()
	{
		return olympiadSide;
	}
	
	/**
	 * Sets the olympiad game id.
	 * @param id the new olympiad game id
	 */
	public void setOlympiadGameId(final int id)
	{
		olympiadGameId = id;
	}
	
	/**
	 * Gets the olympiad game id.
	 * @return the olympiad game id
	 */
	public int getOlympiadGameId()
	{
		return olympiadGameId;
	}
	
	/**
	 * Gets the obs x.
	 * @return the obs x
	 */
	public int getObsX()
	{
		return obsX;
	}
	
	/**
	 * Gets the obs y.
	 * @return the obs y
	 */
	public int getObsY()
	{
		return obsY;
	}
	
	/**
	 * Gets the obs z.
	 * @return the obs z
	 */
	public int getObsZ()
	{
		return obsZ;
	}
	
	/**
	 * In observer mode.
	 * @return true, if successful
	 */
	public boolean inObserverMode()
	{
		return observerMode;
	}
	
	/**
	 * set observer mode.
	 * @param mode
	 */
	public void setObserverMode(final boolean mode)
	{
		observerMode = mode;
	}
	
	/**
	 * Gets the tele mode.
	 * @return the tele mode
	 */
	public int getTeleMode()
	{
		return telemode;
	}
	
	/**
	 * Sets the tele mode.
	 * @param mode the new tele mode
	 */
	public void setTeleMode(final int mode)
	{
		telemode = mode;
	}
	
	/**
	 * Sets the loto.
	 * @param i   the i
	 * @param val the val
	 */
	public void setLoto(final int i, final int val)
	{
		loto[i] = val;
	}
	
	/**
	 * Gets the loto.
	 * @param  i the i
	 * @return   the loto
	 */
	public int getLoto(final int i)
	{
		return loto[i];
	}
	
	/**
	 * Sets the race.
	 * @param i   the i
	 * @param val the val
	 */
	public void setRace(final int i, final int val)
	{
		race[i] = val;
	}
	
	/**
	 * Gets the race.
	 * @param  i the i
	 * @return   the race
	 */
	public int getRace(final int i)
	{
		return race[i];
	}
	
	/*
	 * public void setChatBanned(boolean isBanned) { chatBanned = isBanned; if(isChatBanned()) { sendMessage("You have been chat banned by a server admin."); } else { sendMessage("Your chat ban has been lifted."); if(_chatUnbanTask != null) { chatUnbanTask.cancel(false); } chatUnbanTask = null; }
	 * sendPacket(new EtcStatusUpdate(this)); } public boolean isChatBanned() { return chatBanned; } public void setChatUnbanTask(ScheduledFuture<?> task) { chatUnbanTask = task; } public ScheduledFuture<?> getChatUnbanTask() { return chatUnbanTask; }
	 */
	/**
	 * Send a Server->Client packet StatusUpdate to the L2PcInstance.<BR>
	 * <BR>
	 */
	@Override
	public void sendPacket(final L2GameServerPacket packet)
	{
		if (playerClient != null)
		{
			playerClient.sendPacket(packet);
		}
	}
	
	/**
	 * Send SystemMessage packet.<BR>
	 * <BR>
	 * @param id
	 */
	public void sendPacket(final SystemMessageId id)
	{
		sendPacket(SystemMessage.getSystemMessage(id));
	}
	
	/**
	 * Gets the message refusal.
	 * @return the message refusal
	 */
	public boolean getMessageRefusal()
	{
		return messageRefusal;
	}
	
	/**
	 * Sets the message refusal.
	 * @param mode the new message refusal
	 */
	public void setMessageRefusal(final boolean mode)
	{
		messageRefusal = mode;
		sendPacket(new EtcStatusUpdate(this));
	}
	
	/**
	 * Sets the diet mode.
	 * @param mode the new diet mode
	 */
	public void setDietMode(final boolean mode)
	{
		dietMode = mode;
	}
	
	/**
	 * Gets the diet mode.
	 * @return the diet mode
	 */
	public boolean getDietMode()
	{
		return dietMode;
	}
	
	/**
	 * Sets the exchange refusal.
	 * @param mode the new exchange refusal
	 */
	public void setExchangeRefusal(final boolean mode)
	{
		exchangeRefusal = mode;
	}
	
	/**
	 * Gets the exchange refusal.
	 * @return the exchange refusal
	 */
	public boolean getExchangeRefusal()
	{
		return exchangeRefusal;
	}
	
	/**
	 * Gets the block list.
	 * @return the block list
	 */
	public BlockList getBlockList()
	{
		return blockList;
	}
	
	/**
	 * Sets the hero aura.
	 * @param heroAura the new hero aura
	 */
	public void setHeroAura(final boolean heroAura)
	{
		isPVPHero = heroAura;
		return;
	}
	
	/**
	 * Gets the checks if is pvp hero.
	 * @return the checks if is pvp hero
	 */
	public boolean getIsPVPHero()
	{
		return isPVPHero;
	}
	
	/**
	 * @return How many times this player has been Hero by winning in Olympiads
	 */
	public int getHeroCount()
	{
		int count = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_HERO_COUNT))
		{
			statement.setString(1, getName());
			
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					count = rset.getInt("count");
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("L2PcInstance.getHeroCount : Could not select hero from table heroes", e);
		}
		
		return count;
	}
	
	public void reloadPVPHeroAura()
	{
		sendPacket(new UserInfo(this));
	}
	
	/**
	 * Sets the vip.
	 * @param value the new vip
	 */
	public void setVIP(boolean value)
	{
		vip = value;
	}
	
	/**
	 * Checks if is vip.
	 * @return true, if is vip
	 */
	public boolean isVIP()
	{
		return vip;
	}
	
	/**
	 * @param epochTime time in miliseconds, format EPOCH UNIX TIME <br>
	 *                      Put 0 if you want <b>VIP forever</b>
	 */
	public void setVIPEndDate(long epochTime)
	{
		vipEndDate = epochTime;
	}
	
	/**
	 * @return VIP end time in EPOCH UNIX TIME formet, 0 means VIP for ever
	 */
	public long getVIPEndDate()
	{
		return vipEndDate;
	}
	
	/**
	 * Checks if is away.
	 * @return true, if is away
	 */
	public boolean isAway()
	{
		return isAway;
	}
	
	/**
	 * Sets the checks if is away.
	 * @param state the new checks if is away
	 */
	public void setIsAway(final boolean state)
	{
		isAway = state;
	}
	
	/**
	 * @param olympiadMode If player is register in Olympiad game
	 */
	public void setIsInOlympiadMode(final boolean olympiadMode)
	{
		inOlympiadMode = olympiadMode;
	}
	
	/**
	 * @return If player is registered in Olympiad game
	 */
	public boolean isInOlympiadMode()
	{
		return inOlympiadMode;
	}
	
	/**
	 * @param isFighting If player is ready to fight in the Olympiad stadium (L2OlympiadStadiumZone) I mean, if the player is already in the arena waiting for fight countdown, figthing or waiting to be ported to town
	 */
	public void setIsInOlympiadFight(boolean isFighting)
	{
		inOlympiadFight = isFighting;
	}
	
	/**
	 * @return If player is ready to fight in the Olympiad stadium (L2OlympiadStadiumZone) I mean, if the player is already in the arena waiting for fight countdown, figthing or waiting to be ported to town
	 */
	public boolean isInOlympiadFight()
	{
		return inOlympiadFight;
	}
	
	/**
	 * Sets the olympiad position.
	 * @param pos the new olympiad position
	 */
	public void setOlympiadPosition(final int[] pos)
	{
		olympiadPosition = pos;
	}
	
	/**
	 * Gets the olympiad position.
	 * @return the olympiad position
	 */
	public int[] getOlympiadPosition()
	{
		return olympiadPosition;
	}
	
	/**
	 * Checks if is hero.
	 * @return true, if is hero
	 */
	public boolean isHero()
	{
		return isHeroPlayer;
	}
	
	/**
	 * Checks if is in duel.
	 * @return true, if is in duel
	 */
	public boolean isInDuel()
	{
		return isInDuel;
	}
	
	/**
	 * Gets the duel id.
	 * @return the duel id
	 */
	public int getDuelId()
	{
		return playerDuelId;
	}
	
	/**
	 * Sets the duel state.
	 * @param mode the new duel state
	 */
	public void setDuelState(final int mode)
	{
		duelState = mode;
	}
	
	/**
	 * Gets the duel state.
	 * @return the duel state
	 */
	public int getDuelState()
	{
		return duelState;
	}
	
	/**
	 * Sets the coupon.
	 * @param coupon the new coupon
	 */
	public void setCoupon(final int coupon)
	{
		if (coupon >= 0 && coupon <= 3)
		{
			hasCoupon = coupon;
		}
	}
	
	/**
	 * Adds the coupon.
	 * @param coupon the coupon
	 */
	public void addCoupon(final int coupon)
	{
		if (coupon == 1 || coupon == 2 && !getCoupon(coupon - 1))
		{
			hasCoupon += coupon;
		}
	}
	
	/**
	 * Gets the coupon.
	 * @param  coupon the coupon
	 * @return        the coupon
	 */
	public boolean getCoupon(final int coupon)
	{
		return (hasCoupon == 1 || hasCoupon == 3) && coupon == 0 || (hasCoupon == 2 || hasCoupon == 3) && coupon == 1;
	}
	
	/**
	 * Sets up the duel state using a non 0 duelId.
	 * @param duelId 0=not in a duel
	 */
	public void setIsInDuel(final int duelId)
	{
		if (duelId > 0)
		{
			isInDuel = true;
			duelState = Duel.DUELSTATE_DUELLING;
			playerDuelId = duelId;
		}
		else
		{
			if (duelState == Duel.DUELSTATE_DEAD)
			{
				enableAllSkills();
				getStatus().startHpMpRegeneration();
			}
			isInDuel = false;
			duelState = Duel.DUELSTATE_NODUEL;
			playerDuelId = 0;
		}
	}
	
	/**
	 * This returns a SystemMessage stating why the player is not available for duelling.
	 * @return S1_CANNOT_DUEL... message
	 */
	public SystemMessage getNoDuelReason()
	{
		final SystemMessage sm = new SystemMessage(noDuelReason);
		sm.addString(getName());
		noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
		return sm;
	}
	
	/**
	 * Checks if this player might join / start a duel. To get the reason use getNoDuelReason() after calling this function.
	 * @return true if the player might join/start a duel.
	 */
	public boolean canDuel()
	{
		if (isInCombat() || isInJail())
		{
			noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_BATTLE;
			return false;
		}
		if (isDead() || isAlikeDead() || getCurrentHp() < getMaxHp() / 2 || getCurrentMp() < getMaxMp() / 2)
		{
			noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1S_HP_OR_MP_IS_BELOW_50_PERCENT;
			return false;
		}
		if (isInDuel())
		{
			noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL;
			return false;
		}
		if (isInOlympiadMode())
		{
			noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_THE_OLYMPIAD;
			return false;
		}
		if (isCursedWeaponEquiped())
		{
			noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_IN_A_CHAOTIC_STATE;
			return false;
		}
		if (getPrivateStoreType() != STORE_PRIVATE_NONE)
		{
			noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE;
			return false;
		}
		if (isMounted() || isInBoat())
		{
			noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_RIDING_A_BOAT_WYVERN_OR_STRIDER;
			return false;
		}
		if (isFishing())
		{
			noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_FISHING;
			return false;
		}
		if (isInsideZone(ZONE_PVP) || isInsideZone(ZONE_PEACE) || isInsideZone(ZONE_SIEGE))
		{
			noDuelReason = SystemMessageId.S1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_S1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA;
			return false;
		}
		return true;
	}
	
	/**
	 * Checks if is noble.
	 * @return true, if is noble
	 */
	public boolean isNoble()
	{
		return isNoblePlayer;
	}
	
	/**
	 * Sets the noble.
	 * @param val the new noble
	 */
	public void setNoble(final boolean val)
	{
		if (val)
		{
			for (final L2Skill s : NobleSkillTable.getInstance().GetNobleSkills())
			{
				addSkill(s, false); // Dont Save Noble skills to Sql
			}
		}
		else
		{
			for (final L2Skill s : NobleSkillTable.getInstance().GetNobleSkills())
			{
				super.removeSkill(s); // Just Remove skills without deleting from Sql
			}
		}
		isNoblePlayer = val;
		
		sendSkillList();
	}
	
	/**
	 * Adds the clan leader skills.
	 * @param val the val
	 */
	public void addClanLeaderSkills(final boolean val)
	{
		if (val)
		{
			SiegeManager.getInstance().addSiegeSkills(this);
		}
		else
		{
			SiegeManager.getInstance().removeSiegeSkills(this);
		}
		sendSkillList();
	}
	
	/**
	 * Sets the lvl joined academy.
	 * @param lvl the new lvl joined academy
	 */
	public void setLvlJoinedAcademy(final int lvl)
	{
		lvlJoinedAcademy = lvl;
	}
	
	/**
	 * Gets the lvl joined academy.
	 * @return the lvl joined academy
	 */
	public int getLvlJoinedAcademy()
	{
		return lvlJoinedAcademy;
	}
	
	/**
	 * Checks if is academy member.
	 * @return true, if is academy member
	 */
	public boolean isAcademyMember()
	{
		return lvlJoinedAcademy > 0;
	}
	
	/**
	 * Sets the team.
	 * @param team the new team
	 */
	public void setTeam(final int team)
	{
		this.team = team;
	}
	
	/**
	 * Gets the team.
	 * @return the team
	 */
	public int getTeam()
	{
		return team;
	}
	
	/**
	 * Sets the wants peace.
	 * @param wantsPeace the new wants peace
	 */
	public void setWantsPeace(final int wantsPeace)
	{
		this.wantsPeace = wantsPeace;
	}
	
	/**
	 * Gets the wants peace.
	 * @return the wants peace
	 */
	public int getWantsPeace()
	{
		return wantsPeace;
	}
	
	/**
	 * Checks if is fishing.
	 * @return true, if is fishing
	 */
	public boolean isFishing()
	{
		return fishing;
	}
	
	/**
	 * Sets the fishing.
	 * @param fishing the new fishing
	 */
	public void setFishing(final boolean fishing)
	{
		this.fishing = fishing;
	}
	
	/**
	 * Sets the alliance with varka ketra.
	 * @param sideAndLvlOfAlliance the new alliance with varka ketra
	 */
	public void setAllianceWithVarkaKetra(final int sideAndLvlOfAlliance)
	{
		// [-5,-1] varka, 0 neutral, [1,5] ketra
		alliedVarkaKetra = sideAndLvlOfAlliance;
	}
	
	/**
	 * Gets the alliance with varka ketra.
	 * @return the alliance with varka ketra
	 */
	public int getAllianceWithVarkaKetra()
	{
		return alliedVarkaKetra;
	}
	
	/**
	 * Checks if is allied with varka.
	 * @return true, if is allied with varka
	 */
	public boolean isAlliedWithVarka()
	{
		return alliedVarkaKetra < 0;
	}
	
	/**
	 * Checks if is allied with ketra.
	 * @return true, if is allied with ketra
	 */
	public boolean isAlliedWithKetra()
	{
		return alliedVarkaKetra > 0;
	}
	
	/**
	 * Send skill list.
	 */
	public void sendSkillList()
	{
		sendSkillList(this);
	}
	
	/**
	 * Send skill list.
	 * @param player the player
	 */
	public void sendSkillList(L2PcInstance player)
	{
		SkillList sl = new SkillList();
		
		if (player != null)
		{
			for (L2Skill s : player.getAllSkills())
			{
				if (s == null)
				{
					continue;
				}
				
				if (s.getId() > 9000)
				{
					continue; // Fake skills to change base stats
				}
				
				if (s.bestowed())
				{
					continue;
				}
				
				if (s.isChance())
				{
					sl.addSkill(s.getId(), s.getLevel(), s.isChance());
				}
				else
				{
					sl.addSkill(s.getId(), s.getLevel(), s.isPassive());
				}
			}
		}
		sendPacket(sl);
	}
	
	/**
	 * 1. Add the specified class ID as a subclass (up to the maximum number of <b>three</b>) for this character.<BR>
	 * 2. This method no longer changes the active classIndex of the player. This is only done by the calling of setActiveClass() method as that should be the only way to do so.
	 * @param  classId    the class id
	 * @param  classIndex the class index
	 * @return            boolean subclassAdded
	 */
	public synchronized boolean addSubClass(final int classId, final int classIndex)
	{
		// Reload skills from armors / jewels / weapons
		getInventory().reloadEquippedItems();
		
		// Remove Item RHAND
		if (Config.REMOVE_WEAPON_SUBCLASS)
		{
			final L2ItemInstance rhand = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if (rhand != null)
			{
				final L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(rhand.getItem().getBodyPart());
				final InventoryUpdate iu = new InventoryUpdate();
				for (final L2ItemInstance element : unequipped)
				{
					iu.addModifiedItem(element);
				}
				sendPacket(iu);
			}
		}
		
		// Remove Item CHEST
		if (Config.REMOVE_CHEST_SUBCLASS)
		{
			final L2ItemInstance chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			if (chest != null)
			{
				final L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(chest.getItem().getBodyPart());
				final InventoryUpdate iu = new InventoryUpdate();
				for (final L2ItemInstance element : unequipped)
				{
					iu.addModifiedItem(element);
				}
				sendPacket(iu);
			}
		}
		
		// Remove Item LEG
		if (Config.REMOVE_LEG_SUBCLASS)
		{
			final L2ItemInstance legs = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
			if (legs != null)
			{
				final L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(legs.getItem().getBodyPart());
				final InventoryUpdate iu = new InventoryUpdate();
				for (final L2ItemInstance element : unequipped)
				{
					iu.addModifiedItem(element);
				}
				sendPacket(iu);
			}
		}
		
		if (getTotalSubClasses() == Config.ALLOWED_SUBCLASS || classIndex == 0)
		{
			return false;
		}
		
		if (getSubClasses().containsKey(classIndex))
		{
			return false;
		}
		
		// Note: Never change classIndex in any method other than setActiveClass().
		
		final SubClass newClass = new SubClass();
		newClass.setClassId(classId);
		newClass.setClassIndex(classIndex);
		
		boolean output = false;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(ADD_CHAR_SUBCLASS);)
		{
			// Store the basic info about this new sub-class.
			statement.setInt(1, getObjectId());
			statement.setInt(2, newClass.getClassId());
			statement.setLong(3, newClass.getExp());
			statement.setInt(4, newClass.getSp());
			statement.setInt(5, newClass.getLevel());
			statement.setInt(6, newClass.getClassIndex()); // <-- Added
			statement.executeUpdate();
			output = true;
		}
		catch (Exception e)
		{
			LOGGER.error("L2PcInstance.addSubClass : Could not add character sub class for " + getName(), e);
		}
		
		if (output)
		{
			
			// Commit after database INSERT incase exception is thrown.
			getSubClasses().put(newClass.getClassIndex(), newClass);
			
			if (Config.DEBUG)
			{
				LOGGER.info(getName() + " added class ID " + classId + " as a sub class at index " + classIndex + ".");
			}
			
			ClassId subTemplate = ClassId.values()[classId];
			Collection<L2SkillLearn> skillTree = SkillTreeTable.getInstance().getAllowedSkills(subTemplate);
			subTemplate = null;
			
			if (skillTree == null)
			{
				return true;
			}
			
			Map<Integer, L2Skill> prevSkillList = new HashMap<>();
			
			for (final L2SkillLearn skillInfo : skillTree)
			{
				if (skillInfo.getMinLevel() <= 40)
				{
					final L2Skill prevSkill = prevSkillList.get(skillInfo.getId());
					final L2Skill newSkill = SkillTable.getInstance().getInfo(skillInfo.getId(), skillInfo.getLevel());
					
					if (newSkill == null || prevSkill != null && prevSkill.getLevel() > newSkill.getLevel())
					{
						continue;
					}
					
					prevSkillList.put(newSkill.getId(), newSkill);
					storeSkill(newSkill, prevSkill, classIndex);
				}
			}
			skillTree = null;
			prevSkillList = null;
			
			if (Config.DEBUG)
			{
				LOGGER.info(getName() + " was given " + getAllSkills().length + " skills for their new sub class.");
			}
			
		}
		
		return output;
	}
	
	/**
	 * 1. Completely erase all existance of the subClass linked to the classIndex.<BR>
	 * 2. Send over the newClassId to addSubClass()to create a new instance on this classIndex.<BR>
	 * 3. Upon Exception, revert the player to their BaseClass to avoid further problems.<BR>
	 * @param  classIndex the class index
	 * @param  newClassId the new class id
	 * @return            boolean subclassAdded
	 */
	public boolean modifySubClass(final int classIndex, final int newClassId)
	{
		final int oldClassId = getSubClasses().get(classIndex).getClassId();
		
		if (Config.DEBUG)
		{
			LOGGER.info(getName() + " has requested to modify sub class index " + classIndex + " from class ID " + oldClassId + " to " + newClassId + ".");
		}
		
		boolean output = false;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
		{
			// Remove all henna info stored for this sub-class.
			try (PreparedStatement statement = con.prepareStatement(DELETE_CHAR_HENNAS))
			{
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.executeUpdate();
			}
			
			// Remove all shortcuts info stored for this sub-class.
			try (PreparedStatement statement = con.prepareStatement(DELETE_CHAR_SHORTCUTS))
			{
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.executeUpdate();
			}
			
			// Remove all effects info stored for this sub-class.
			try (PreparedStatement statement = con.prepareStatement(DELETE_CHARACTER_SKILLS_SAVE))
			{
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.executeUpdate();
			}
			
			// Remove all skill info stored for this sub-class.
			try (PreparedStatement statement = con.prepareStatement(DELETE_CHAR_SKILLS))
			{
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.executeUpdate();
			}
			
			// Remove all basic info stored about this sub-class.
			try (PreparedStatement statement = con.prepareStatement(DELETE_CHAR_SUBCLASS))
			{
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.executeUpdate();
			}
			
			output = true;
		}
		catch (Exception e)
		{
			LOGGER.error("L2PcInstance.modifySubClass : Could not modify sub class for " + getName() + " to class index " + classIndex, e);
		}
		
		getSubClasses().remove(classIndex);
		
		if (output)
		{
			return addSubClass(newClassId, classIndex);
		}
		
		return false;
	}
	
	/**
	 * Checks if is sub class active.
	 * @return true, if is sub class active
	 */
	public boolean isSubClassActive()
	{
		return playerClassIndex > 0;
	}
	
	/**
	 * Gets the sub classes.
	 * @return the sub classes
	 */
	public Map<Integer, SubClass> getSubClasses()
	{
		if (subClasses == null)
		{
			subClasses = new HashMap<>();
		}
		
		return subClasses;
	}
	
	/**
	 * Gets the total sub classes.
	 * @return the total sub classes
	 */
	public int getTotalSubClasses()
	{
		return getSubClasses().size();
	}
	
	/**
	 * Gets the base class.
	 * @return the base class
	 */
	public int getBaseClass()
	{
		return baseClass;
	}
	
	/**
	 * Gets the active class.
	 * @return the active class
	 */
	public synchronized int getActiveClass()
	{
		return activeClass;
	}
	
	/**
	 * Gets the class index.
	 * @return the class index
	 */
	public int getClassIndex()
	{
		return playerClassIndex;
	}
	
	/**
	 * Sets the class template.
	 * @param classId the new class template
	 */
	private synchronized void setClassTemplate(final int classId)
	{
		activeClass = classId;
		
		L2PcTemplate t = CharTemplateTable.getInstance().getTemplate(classId);
		
		if (t == null)
		{
			LOGGER.warn("Missing template for classId: " + classId);
			throw new Error();
		}
		
		// Set the template of the L2PcInstance
		setTemplate(t);
		t = null;
	}
	
	/**
	 * Changes the character's class based on the given class index. <BR>
	 * <BR>
	 * An index of zero specifies the character's original (base) class, while indexes 1-3 specifies the character's sub-classes respectively.
	 * @param  classIndex the class index
	 * @return            true, if successful
	 */
	public synchronized boolean setActiveClass(final int classIndex)
	{
		if (isInCombat() || getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
		{
			sendMessage("Impossible switch class if in combat");
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Delete a force buff upon class change.
		// thank l2j-arhid
		if (forceBuff != null)
		{
			abortCast();
		}
		
		/**
		 * 1. Call store() before modifying classIndex to avoid skill effects rollover. 2. Register the correct classId against applied 'classIndex'.
		 */
		store();
		
		if (classIndex == 0)
		{
			setClassTemplate(getBaseClass());
		}
		else
		{
			try
			{
				setClassTemplate(getSubClasses().get(classIndex).getClassId());
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				LOGGER.info("Could not switch " + getName() + "'s sub class to class index " + classIndex + ": " + e);
				return false;
			}
		}
		playerClassIndex = classIndex;
		
		if (isInParty())
		{
			getParty().recalculatePartyLevel();
		}
		
		/*
		 * Update the character's change in class status. 1. Remove any active cubics from the player. 2. Renovate the characters table in the database with the new class info, storing also buff/effect data. 3. Remove all existing skills. 4. Restore all the learned skills for the current class from the
		 * database. 5. Restore effect/buff data for the new class. 6. Restore henna data for the class, applying the new stat modifiers while removing existing ones. 7. Reset HP/MP/CP stats and send Server->Client character status packet to reflect changes. 8. Restore shortcut data related to this class.
		 * 9. Resend a class change animation effect to broadcast to all nearby players. 10.Unsummon any active servitor from the player.
		 */
		
		if (getPet() != null && getPet() instanceof L2SummonInstance)
		{
			getPet().unSummon(this);
		}
		
		unsummonAllCubics();
		
		synchronized (getAllSkills())
		{
			
			for (final L2Skill oldSkill : getAllSkills())
			{
				super.removeSkill(oldSkill);
			}
			
		}
		
		// Yesod: Rebind CursedWeapon passive.
		if (isCursedWeaponEquiped())
		{
			CursedWeaponsManager.getInstance().givePassive(cursedWeaponEquipedId);
		}
		
		stopAllEffects();
		
		if (isSubClassActive())
		{
			dwarvenRecipeBook.clear();
			commonRecipeBook.clear();
		}
		else
		{
			restoreRecipeBook();
		}
		
		// Restore any Death Penalty Buff
		restoreDeathPenaltyBuffLevel();
		
		restoreSkills();
		regiveTemporarySkills();
		rewardSkills();
		restoreEffects(Config.ALT_RESTORE_EFFECTS_ON_SUBCLASS_CHANGE);
		
		// Reload skills from armors / jewels / weapons
		getInventory().reloadEquippedItems();
		
		// Remove Item RHAND
		if (Config.REMOVE_WEAPON_SUBCLASS)
		{
			final L2ItemInstance rhand = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if (rhand != null)
			{
				final L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(rhand.getItem().getBodyPart());
				final InventoryUpdate iu = new InventoryUpdate();
				for (final L2ItemInstance element : unequipped)
				{
					iu.addModifiedItem(element);
				}
				sendPacket(iu);
			}
		}
		// Remove Item CHEST
		if (Config.REMOVE_CHEST_SUBCLASS)
		{
			final L2ItemInstance chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			if (chest != null)
			{
				final L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(chest.getItem().getBodyPart());
				final InventoryUpdate iu = new InventoryUpdate();
				for (final L2ItemInstance element : unequipped)
				{
					iu.addModifiedItem(element);
				}
				sendPacket(iu);
			}
		}
		
		// Remove Item LEG
		if (Config.REMOVE_LEG_SUBCLASS)
		{
			final L2ItemInstance legs = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
			if (legs != null)
			{
				final L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(legs.getItem().getBodyPart());
				final InventoryUpdate iu = new InventoryUpdate();
				for (final L2ItemInstance element : unequipped)
				{
					iu.addModifiedItem(element);
				}
				sendPacket(iu);
			}
		}
		
		// Check player skills
		if (Config.CHECK_SKILLS_ON_ENTER && !Config.ALT_GAME_SKILL_LEARN)
		{
			checkAllowedSkills();
		}
		
		sendPacket(new EtcStatusUpdate(this));
		
		// if player has quest 422: Repent Your Sins, remove it
		QuestState st = getQuestState("422_RepentYourSins");
		
		if (st != null)
		{
			st.exitQuest(true);
		}
		
		for (int i = 0; i < 3; i++)
		{
			playerHenna[i] = null;
		}
		
		restoreHenna();
		sendPacket(new HennaInfo(this));
		
		if (getCurrentHp() > getMaxHp())
		{
			setCurrentHp(getMaxHp());
		}
		
		if (getCurrentMp() > getMaxMp())
		{
			setCurrentMp(getMaxMp());
		}
		
		if (getCurrentCp() > getMaxCp())
		{
			setCurrentCp(getMaxCp());
		}
		
		// Refresh player infos and update new status
		broadcastUserInfo();
		refreshOverloaded();
		refreshExpertisePenalty();
		refreshMasteryPenality();
		refreshMasteryWeapPenality();
		sendPacket(new UserInfo(this));
		sendPacket(new ItemList(this, false));
		getInventory().refreshWeight();
		
		// Clear resurrect xp calculation
		setExpBeforeDeath(0);
		macroses.restore();
		macroses.sendUpdate();
		shortCuts.restore();
		sendPacket(new ShortCutInit(this));
		
		broadcastPacket(new SocialAction(getObjectId(), 15));
		sendPacket(new SkillCoolTime(this));
		
		if (getClan() != null)
		{
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
			// decayMe();
			// spawnMe(getX(), getY(), getZ());
		}
		
		return true;
	}
	
	public void broadcastClassIcon()
	{
		// Update class icon in party and clan
		if (isInParty())
		{
			getParty().broadcastToPartyMembers(new PartySmallWindowUpdate(this));
		}
		
		if (getClan() != null)
		{
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
		}
	}
	
	public void stopWarnUserTakeBreak()
	{
		if (taskWarnUserTakeBreak != null)
		{
			taskWarnUserTakeBreak.cancel(true);
			taskWarnUserTakeBreak = null;
		}
	}
	
	public void startWarnUserTakeBreak()
	{
		if (taskWarnUserTakeBreak == null)
		{
			taskWarnUserTakeBreak = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new WarnUserTakeBreak(), 7200000, 7200000);
		}
	}
	
	public void stopRentPet()
	{
		if (taskRentPet != null)
		{
			// if the rent of a wyvern expires while over a flying zone, tp to down before unmounting
			if (checkLandingState() && getMountType() == 2)
			{
				teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
			
			if (setMountType(0)) // this should always be true now, since we teleported already
			{
				taskRentPet.cancel(true);
				Ride dismount = new Ride(getObjectId(), Ride.ACTION_DISMOUNT, 0);
				sendPacket(dismount);
				broadcastPacket(dismount);
				dismount = null;
				taskRentPet = null;
			}
		}
	}
	
	/**
	 * Start rent pet.
	 * @param seconds the seconds
	 */
	public void startRentPet(final int seconds)
	{
		if (taskRentPet == null)
		{
			taskRentPet = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RentPetTask(), seconds * 1000L, seconds * 1000L);
		}
	}
	
	/**
	 * Checks if is rented pet.
	 * @return true, if is rented pet
	 */
	public boolean isRentedPet()
	{
		if (taskRentPet != null)
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Stop water task.
	 */
	public void stopWaterTask()
	{
		if (taskWater != null)
		{
			taskWater.cancel(false);
			taskWater = null;
			sendPacket(new SetupGauge(2, 0));
			// for catacombs...
			broadcastUserInfo();
		}
	}
	
	/**
	 * Start water task.
	 */
	public void startWaterTask()
	{
		broadcastUserInfo();
		if (!isDead() && taskWater == null)
		{
			final int timeinwater = 86000;
			
			sendPacket(new SetupGauge(2, timeinwater));
			taskWater = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new WaterTask(), timeinwater, 1000);
		}
	}
	
	/**
	 * Checks if is in water.
	 * @return true, if is in water
	 */
	public boolean isInWater()
	{
		if (taskWater != null)
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Check water state.
	 */
	public void checkWaterState()
	{
		// checking if char is over base level of water (sea, rivers)
		if (getZ() > -3750)
		{
			stopWaterTask();
			return;
		}
		
		if (isInsideZone(ZONE_WATER))
		{
			startWaterTask();
		}
		else
		{
			stopWaterTask();
			return;
		}
	}
	
	/**
	 * On player enter.
	 */
	public void onPlayerEnter()
	{
		startWarnUserTakeBreak();
		
		if (SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod())
		{
			if (!isGM() && isIn7sDungeon() && SevenSigns.getInstance().getPlayerCabal(this) != SevenSigns.getInstance().getCabalHighestScore())
			{
				teleToLocation(MapRegionTable.TeleportWhereType.Town);
				setIsIn7sDungeon(false);
				sendMessage("You have been teleported to the nearest town due to the beginning of the Seal Validation period.");
			}
		}
		else
		{
			if (!isGM() && isIn7sDungeon() && SevenSigns.getInstance().getPlayerCabal(this) == SevenSigns.CABAL_NULL)
			{
				teleToLocation(MapRegionTable.TeleportWhereType.Town);
				setIsIn7sDungeon(false);
				sendMessage("You have been teleported to the nearest town because you have not signed for any cabal.");
			}
		}
		
		// jail task
		updatePunishState();
		
		if (isInvul)
		{
			sendMessage("Entering world in Invulnerable mode.");
		}
		
		if (getAppearance().isInvisible())
		{
			sendMessage("Entering world in Invisible mode.");
		}
		
		if (getMessageRefusal())
		{
			sendMessage("Entering world in Message Refusal mode.");
		}
		
		revalidateZone(true);
		
		notifyFriends(false);
		
		// Fix against exploit on anti-target on login
		decayMe();
		spawnMe();
		broadcastUserInfo();
		
	}
	
	/**
	 * Gets the last access.
	 * @return the last access
	 */
	public long getLastAccess()
	{
		return lastAccess;
	}
	
	public void setLastAccess(long lastAcces)
	{
		lastAccess = lastAcces;
	}
	
	/**
	 * Check recom.
	 * @param recsHave the recs have
	 * @param recsLeft the recs left
	 */
	private void checkRecom(final int recsHave, final int recsLeft)
	{
		final Calendar check = Calendar.getInstance();
		check.setTimeInMillis(lastRecomUpdate);
		check.add(Calendar.DAY_OF_MONTH, 1);
		
		final Calendar min = Calendar.getInstance();
		
		recomHave = recsHave;
		recomLeft = recsLeft;
		
		if (getStat().getLevel() < 10 || check.after(min))
		{
			return;
		}
		
		restartRecom();
	}
	
	/**
	 * Restart recom.
	 */
	public void restartRecom()
	{
		if (Config.ALT_RECOMMEND)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(DELETE_CHAR_RECOMS))
			{
				statement.setInt(1, getObjectId());
				statement.executeUpdate();
				recomChars.clear();
			}
			catch (Exception e)
			{
				LOGGER.error("L2PcInstance.restartRecom : Could not clear char recommendations", e);
			}
		}
		
		if (getStat().getLevel() < 20)
		{
			recomLeft = 3;
			recomHave--;
		}
		else if (getStat().getLevel() < 40)
		{
			recomLeft = 6;
			recomHave -= 2;
		}
		else
		{
			recomLeft = 9;
			recomHave -= 3;
		}
		
		if (recomHave < 0)
		{
			recomHave = 0;
		}
		
		// If we have to update last update time, but it's now before 13, we should set it to yesterday
		final Calendar update = Calendar.getInstance();
		if (update.get(Calendar.HOUR_OF_DAY) < 13)
		{
			update.add(Calendar.DAY_OF_MONTH, -1);
		}
		
		update.set(Calendar.HOUR_OF_DAY, 13);
		lastRecomUpdate = update.getTimeInMillis();
	}
	
	@Override
	public void doRevive()
	{
		super.doRevive();
		updateEffectIcons();
		sendPacket(new EtcStatusUpdate(this));
		reviveRequested = 0;
		revivePower = 0;
		
		if (isInParty() && getParty().isInDimensionalRift())
		{
			if (!DimensionalRiftManager.getInstance().checkIfInPeaceZone(getX(), getY(), getZ()))
			{
				getParty().getDimensionalRift().memberRessurected(this);
			}
		}
		
		if (inEventTvT && TvT.isStarted() && Config.TVT_REVIVE_RECOVERY || inEventCTF && CTF.isStarted() && Config.CTF_REVIVE_RECOVERY || inEventDM && DM.isStarted() && Config.DM_REVIVE_RECOVERY)
		{
			getStatus().setCurrentHp(getMaxHp());
			getStatus().setCurrentMp(getMaxMp());
			getStatus().setCurrentCp(getMaxCp());
		}
	}
	
	@Override
	public void doRevive(final double revivePower)
	{
		// Restore the player's lost experience,
		// depending on the % return of the skill used (based on its power).
		restoreExp(revivePower);
		doRevive();
	}
	
	/**
	 * Revive request.
	 * @param Reviver the reviver
	 * @param skill   the skill
	 * @param Pet     the pet
	 */
	public void reviveRequest(final L2PcInstance Reviver, final L2Skill skill, final boolean Pet)
	{
		if (reviveRequested == 1)
		{
			if (revivePet == Pet)
			{
				Reviver.sendPacket(new SystemMessage(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED)); // Resurrection is already been proposed.
			}
			else
			{
				if (Pet)
				{
					Reviver.sendPacket(new SystemMessage(SystemMessageId.PET_CANNOT_RES)); // A pet cannot be resurrected while it's owner is in the process of resurrecting.
				}
				else
				{
					Reviver.sendPacket(new SystemMessage(SystemMessageId.MASTER_CANNOT_RES)); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
				}
			}
			return;
		}
		if (Pet && getPet() != null && getPet().isDead() || !Pet && isDead())
		{
			reviveRequested = 1;
			if (isPhoenixBlessed())
			{
				revivePower = 100;
			}
			else if (skill != null)
			{
				revivePower = Formulas.getInstance().calculateSkillResurrectRestorePercent(skill.getPower(), Reviver);
			}
			else
			{
				revivePower = 0;
			}
			revivePet = Pet;
			ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST.getId());
			dlg.addString(Reviver.getName());
			sendPacket(dlg);
			dlg = null;
		}
	}
	
	/**
	 * Revive answer.
	 * @param answer the answer
	 */
	public void reviveAnswer(final int answer)
	{
		if (reviveRequested != 1 || !isDead() && !revivePet || revivePet && getPet() != null && !getPet().isDead())
		{
			return;
		}
		// If character refuse a PhoenixBlessed autoress, cancel all buffs he had
		if (answer == 0 && isPhoenixBlessed())
		{
			stopPhoenixBlessing(null);
			stopAllEffects();
		}
		if (answer == 1)
		{
			if (!revivePet)
			{
				if (revivePower != 0)
				{
					doRevive(revivePower);
				}
				else
				{
					doRevive();
				}
			}
			else if (getPet() != null)
			{
				if (revivePower != 0)
				{
					getPet().doRevive(revivePower);
				}
				else
				{
					getPet().doRevive();
				}
			}
		}
		reviveRequested = 0;
		revivePower = 0;
	}
	
	/**
	 * Checks if is revive requested.
	 * @return true, if is revive requested
	 */
	public boolean isReviveRequested()
	{
		return reviveRequested == 1;
	}
	
	/**
	 * Checks if is reviving pet.
	 * @return true, if is reviving pet
	 */
	public boolean isRevivingPet()
	{
		return revivePet;
	}
	
	/**
	 * Removes the reviving.
	 */
	public void removeReviving()
	{
		reviveRequested = 0;
		revivePower = 0;
	}
	
	/**
	 * On action request.
	 */
	public void onActionRequest()
	{
		/*
		 * Important: dont send here a broadcast like removeAbnornalstatus cause they will create mass lag on pvp
		 */
		
		if (isSpawnProtected())
		{
			sendMessage("The effect of Spawn Protection has been removed.");
		}
		else if (isTeleportProtected())
		{
			sendMessage("The effect of Teleport Spawn Protection has been removed.");
		}
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
		{
			setProtection(false);
		}
		
		if (Config.PLAYER_TELEPORT_PROTECTION > 0)
		{
			setTeleportProtection(false);
		}
	}
	
	/**
	 * Sets the expertise index.
	 * @param expertiseIndex The expertiseIndex to set.
	 */
	public void setExpertiseIndex(final int expertiseIndex)
	{
		this.expertiseIndex = expertiseIndex;
	}
	
	/**
	 * Gets the expertise index.
	 * @return Returns the expertiseIndex.
	 */
	public int getExpertiseIndex()
	{
		return expertiseIndex;
	}
	
	@Override
	public final void onTeleported()
	{
		super.onTeleported();
		
		// Force a revalidation
		revalidateZone(true);
		
		if (Config.PLAYER_TELEPORT_PROTECTION > 0 && !isInOlympiadMode())
		{
			setTeleportProtection(true);
			sendMessage("The effects of Teleport Spawn Protection flow through you.");
		}
		
		if (Config.ALLOW_WATER)
		{
			checkWaterState();
		}
		
		// Modify the position of the tamed beast if necessary (normal pets are handled by super...though
		// L2PcInstance is the only class that actually has pets!!! )
		if (getTrainedBeast() != null)
		{
			getTrainedBeast().getAI().stopFollow();
			getTrainedBeast().teleToLocation(getPosition().getX() + Rnd.get(-100, 100), getPosition().getY() + Rnd.get(-100, 100), getPosition().getZ(), false);
			getTrainedBeast().getAI().startFollow(this);
		}
		
		// To be sure update also the pvp flag / war tag status
		if (!inObserverMode())
		{
			broadcastUserInfo();
		}
	}
	
	@Override
	public final boolean updatePosition(final int gameTicks)
	{
		// Disables custom movement for L2PCInstance when Old Synchronization is selected
		if (Config.COORD_SYNCHRONIZE == -1)
		{
			return super.updatePosition(gameTicks);
		}
		
		// Get movement data
		final MoveData m = playerMove;
		
		if (playerMove == null)
		{
			return true;
		}
		
		if (!isVisible())
		{
			playerMove = null;
			return true;
		}
		
		// Check if the position has alreday be calculated
		if (m.moveTimestamp == 0)
		{
			m.moveTimestamp = m.moveStartTime;
		}
		
		// Check if the position has alreday be calculated
		if (m.moveTimestamp == gameTicks)
		{
			return false;
		}
		
		final double dx = m.xDestination - getX();
		final double dy = m.yDestination - getY();
		final double dz = m.zDestination - getZ();
		final int distPassed = (int) getStat().getMoveSpeed() * (gameTicks - m.moveTimestamp) / GameTimeController.TICKS_PER_SECOND;
		final double distFraction = distPassed / Math.sqrt(dx * dx + dy * dy + dz * dz);
		// if (Config.DEVELOPER) LOGGER.info("Move Ticks:" + (gameTicks - m._moveTimestamp) + ", distPassed:" + distPassed + ", distFraction:" + distFraction);
		
		if (distFraction > 1)
		{
			// Set the position of the L2Character to the destination
			super.setXYZ(m.xDestination, m.yDestination, m.zDestination);
		}
		else
		{
			// Set the position of the L2Character to estimated after parcial move
			super.setXYZ(getX() + (int) (dx * distFraction + 0.5), getY() + (int) (dy * distFraction + 0.5), getZ() + (int) (dz * distFraction));
		}
		
		// Set the timer of last position update to now
		m.moveTimestamp = gameTicks;
		
		revalidateZone(false);
		
		return distFraction > 1;
	}
	
	/**
	 * Sets the last client position.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void setLastClientPosition(final int x, final int y, final int z)
	{
		lastClientPosition.setXYZ(x, y, z);
	}
	
	/**
	 * Sets the last client position.
	 * @param loc the new last client position
	 */
	public void setLastClientPosition(final Location loc)
	{
		lastClientPosition = loc;
	}
	
	/**
	 * Check last client position.
	 * @param  x the x
	 * @param  y the y
	 * @param  z the z
	 * @return   true, if successful
	 */
	public boolean checkLastClientPosition(final int x, final int y, final int z)
	{
		return lastClientPosition.equals(x, y, z);
	}
	
	/**
	 * Gets the last client distance.
	 * @param  x the x
	 * @param  y the y
	 * @param  z the z
	 * @return   the last client distance
	 */
	public int getLastClientDistance(final int x, final int y, final int z)
	{
		final double dx = x - lastClientPosition.getX();
		final double dy = y - lastClientPosition.getY();
		final double dz = z - lastClientPosition.getZ();
		
		return (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	/**
	 * Sets the last server position.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void setLastServerPosition(final int x, final int y, final int z)
	{
		lastServerPosition.setXYZ(x, y, z);
	}
	
	/**
	 * Sets the last server position.
	 * @param loc the new last server position
	 */
	public void setLastServerPosition(final Location loc)
	{
		lastServerPosition = loc;
	}
	
	/**
	 * Check last server position.
	 * @param  x the x
	 * @param  y the y
	 * @param  z the z
	 * @return   true, if successful
	 */
	public boolean checkLastServerPosition(final int x, final int y, final int z)
	{
		return lastServerPosition.equals(x, y, z);
	}
	
	/**
	 * Gets the last server distance.
	 * @param  x the x
	 * @param  y the y
	 * @param  z the z
	 * @return   the last server distance
	 */
	public int getLastServerDistance(final int x, final int y, final int z)
	{
		final double dx = x - lastServerPosition.getX();
		final double dy = y - lastServerPosition.getY();
		final double dz = z - lastServerPosition.getZ();
		
		return (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	@Override
	public void addExpAndSp(final long addToExp, final int addToSp)
	{
		if (isVIP())
		{
			getStat().addExpAndSp((long) (addToExp * Config.VIP_XPSP_RATE), (int) (addToSp * Config.VIP_XPSP_RATE));
		}
		else
		{
			getStat().addExpAndSp(addToExp, addToSp);
		}
	}
	
	/**
	 * Removes the exp and sp.
	 * @param removeExp the remove exp
	 * @param removeSp  the remove sp
	 */
	public void removeExpAndSp(final long removeExp, final int removeSp)
	{
		getStat().removeExpAndSp(removeExp, removeSp);
	}
	
	@Override
	public void reduceCurrentHp(final double i, final L2Character attacker)
	{
		getStatus().reduceHp(i, attacker);
		
		// notify the tamed beast of attacks
		if (getTrainedBeast() != null)
		{
			getTrainedBeast().onOwnerGotAttacked(attacker);
		}
	}
	
	/*
	 * Function for skill summon friend or Gate Chant.
	 */
	/**
	 * Request Teleport *.
	 * @param  requester the requester
	 * @param  skill     the skill
	 * @return           true, if successful
	 */
	public boolean teleportRequest(final L2PcInstance requester, final L2Skill skill)
	{
		if (summonRequest.getTarget() != null && requester != null)
		{
			return false;
		}
		summonRequest.setTarget(requester, skill);
		return true;
	}
	
	/**
	 * Action teleport *.
	 * @param answer      the answer
	 * @param requesterId the requester id
	 */
	public void teleportAnswer(final int answer, final int requesterId)
	{
		if (summonRequest.getTarget() == null)
		{
			return;
		}
		if (answer == 1 && summonRequest.getTarget().getObjectId() == requesterId)
		{
			teleToTarget(this, summonRequest.getTarget(), summonRequest.getSkill());
		}
		summonRequest.setTarget(null, null);
	}
	
	/**
	 * Tele to target.
	 * @param targetChar   the target char
	 * @param summonerChar the summoner char
	 * @param summonSkill  the summon skill
	 */
	public static void teleToTarget(final L2PcInstance targetChar, final L2PcInstance summonerChar, final L2Skill summonSkill)
	{
		if (targetChar == null || summonerChar == null || summonSkill == null)
		{
			return;
		}
		
		if (!checkSummonerStatus(summonerChar))
		{
			return;
		}
		if (!checkSummonTargetStatus(targetChar, summonerChar))
		{
			return;
		}
		
		final int itemConsumeId = summonSkill.getTargetConsumeId();
		final int itemConsumeCount = summonSkill.getTargetConsume();
		if (itemConsumeId != 0 && itemConsumeCount != 0)
		{
			// Delete by rocknow
			if (targetChar.getInventory().getInventoryItemCount(itemConsumeId, 0) < itemConsumeCount)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_REQUIRED_FOR_SUMMONING);
				sm.addItemName(summonSkill.getTargetConsumeId());
				targetChar.sendPacket(sm);
				return;
			}
			targetChar.getInventory().destroyItemByItemId("Consume", itemConsumeId, itemConsumeCount, summonerChar, targetChar);
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
			sm.addItemName(summonSkill.getTargetConsumeId());
			targetChar.sendPacket(sm);
		}
		targetChar.teleToLocation(summonerChar.getX(), summonerChar.getY(), summonerChar.getZ(), true);
	}
	
	/**
	 * Check summoner status.
	 * @param  summonerChar the summoner char
	 * @return              true, if successful
	 */
	public static boolean checkSummonerStatus(final L2PcInstance summonerChar)
	{
		if (summonerChar == null)
		{
			return false;
		}
		
		if (summonerChar.isInOlympiadMode())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return false;
		}
		
		if (summonerChar.inObserverMode())
		{
			return false;
		}
		
		if (summonerChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND) || summonerChar.isFlying() || summonerChar.isMounted())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
			return false;
		}
		return true;
	}
	
	/**
	 * Check summon target status.
	 * @param  target       the target
	 * @param  summonerChar the summoner char
	 * @return              true, if successful
	 */
	public static boolean checkSummonTargetStatus(final L2Object target, final L2PcInstance summonerChar)
	{
		if (target == null || !(target instanceof L2PcInstance))
		{
			return false;
		}
		
		final L2PcInstance targetChar = (L2PcInstance) target;
		
		if (targetChar.isAlikeDead())
		{
			return false;
		}
		
		if (targetChar.isInStoreMode())
		{
			return false;
		}
		
		if (targetChar.isRooted() || targetChar.isInCombat())
		{
			return false;
		}
		
		if (targetChar.isInOlympiadMode())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD));
			return false;
		}
		
		if (targetChar.isFestivalParticipant() || targetChar.isFlying())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
			return false;
		}
		
		if (targetChar.inObserverMode())
		{
			return false;
		}
		
		if (targetChar.isInCombat())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
			return false;
		}
		
		if (targetChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public void reduceCurrentHp(final double value, final L2Character attacker, final boolean awake)
	{
		getStatus().reduceHp(value, attacker, awake);
		
		// notify the tamed beast of attacks
		if (getTrainedBeast() != null)
		{
			getTrainedBeast().onOwnerGotAttacked(attacker);
		}
	}
	
	public void broadcastSnoop(final int type, final String name, final String text, final CreatureSay cs)
	{
		if (snoopListener.size() > 0)
		{
			final Snoop sn = new Snoop(this, type, name, text);
			for (final L2PcInstance pci : snoopListener)
			{
				if (pci != null)
				{
					pci.sendPacket(cs);
					pci.sendPacket(sn);
				}
			}
		}
	}
	
	public void addSnooper(final L2PcInstance pci)
	{
		if (!snoopListener.contains(pci))
		{
			snoopListener.add(pci);
		}
	}
	
	public void removeSnooper(final L2PcInstance pci)
	{
		snoopListener.remove(pci);
	}
	
	public void addSnooped(final L2PcInstance pci)
	{
		if (!snoopedPlayer.contains(pci))
		{
			snoopedPlayer.add(pci);
		}
	}
	
	public void removeSnooped(final L2PcInstance pci)
	{
		snoopedPlayer.remove(pci);
	}
	
	/**
	 * Adds the bypass.
	 * @param bypass the bypass
	 */
	public synchronized void addBypass(final String bypass)
	{
		if (bypass == null)
		{
			return;
		}
		validBypass.add(bypass);
		// LOGGER.warn("[BypassAdd]"+getName()+" '"+bypass+"'");
	}
	
	/**
	 * Adds the bypass2.
	 * @param bypass the bypass
	 */
	public synchronized void addBypass2(final String bypass)
	{
		if (bypass == null)
		{
			return;
		}
		validBypass2.add(bypass);
		// LOGGER.warn("[BypassAdd]"+getName()+" '"+bypass+"'");
	}
	
	/**
	 * Validate bypass.
	 * @param  cmd the cmd
	 * @return     true, if successful
	 */
	public synchronized boolean validateBypass(final String cmd)
	{
		if (!Config.BYPASS_VALIDATION)
		{
			return true;
		}
		
		for (final String bp : validBypass)
		{
			if (bp == null)
			{
				continue;
			}
			
			// LOGGER.warn("[BypassValidation]"+getName()+" '"+bp+"'");
			if (bp.equals(cmd))
			{
				return true;
			}
		}
		
		for (final String bp : validBypass2)
		{
			if (bp == null)
			{
				continue;
			}
			
			// LOGGER.warn("[BypassValidation]"+getName()+" '"+bp+"'");
			if (cmd.startsWith(bp))
			{
				return true;
			}
		}
		if (cmd.startsWith("npc_") && cmd.endsWith("_SevenSigns 7"))
		{
			return true;
		}
		
		final L2PcInstance player = getClient().getActiveChar();
		// We decided to put a kick because when a player is doing quest with a BOT he sends invalid bypass.
		Util.handleIllegalPlayerAction(player, "[L2PcInstance] player [" + player.getName() + "] sent invalid bypass '" + cmd + "'", Config.DEFAULT_PUNISH);
		return false;
	}
	
	/**
	 * Validate item manipulation by item id.
	 * @param  itemId the item id
	 * @param  action the action
	 * @return        true, if successful
	 */
	public boolean validateItemManipulationByItemId(final int itemId, final String action)
	{
		L2ItemInstance item = getInventory().getItemByItemId(itemId);
		
		if (item == null || item.getOwnerId() != getObjectId())
		{
			LOGGER.warn(getObjectId() + ": player tried to " + action + " item he is not owner of");
			return false;
		}
		if (getActiveEnchantItem() != null && getActiveEnchantItem().getItemId() == itemId)
		{
			LOGGER.warn(getObjectId() + ":player tried to " + action + " an enchant scroll he was using");
			return false;
		}
		
		if (CursedWeaponsManager.getInstance().isCursed(itemId))
		{
			// can not trade a cursed weapon
			return false;
		}
		
		if (item.isWear())
		{
			// cannot drop/trade wear-items
			return false;
		}
		
		item = null;
		
		return true;
	}
	
	/**
	 * Validate item manipulation.
	 * @param  objectId the object id
	 * @param  action   the action
	 * @return          true, if successful
	 */
	public boolean validateItemManipulation(final int objectId, final String action)
	{
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);
		
		if (item == null || item.getOwnerId() != getObjectId())
		{
			LOGGER.warn(getObjectId() + ": player tried to " + action + " item he is not owner of");
			return false;
		}
		
		// Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
		if (getPet() != null && getPet().getControlItemId() == objectId || getMountObjectID() == objectId)
		{
			if (Config.DEBUG)
			{
				LOGGER.debug(getObjectId() + ": player tried to " + action + " item controling pet");
			}
			
			return false;
		}
		
		if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
		{
			if (Config.DEBUG)
			{
				LOGGER.debug(getObjectId() + ":player tried to " + action + " an enchant scroll he was using");
			}
			
			return false;
		}
		
		if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
		{
			// can not trade a cursed weapon
			return false;
		}
		
		if (item.isWear())
		{
			// cannot drop/trade wear-items
			return false;
		}
		
		item = null;
		
		return true;
	}
	
	/**
	 * Clear bypass.
	 */
	public synchronized void clearBypass()
	{
		validBypass.clear();
		validBypass2.clear();
	}
	
	/**
	 * Validate link.
	 * @param  cmd the cmd
	 * @return     true, if successful
	 */
	public synchronized boolean validateLink(final String cmd)
	{
		if (!Config.BYPASS_VALIDATION)
		{
			return true;
		}
		
		for (final String bp : validLink)
		{
			if (bp == null)
			{
				continue;
			}
			
			if (bp.equals(cmd))
			{
				return true;
			}
		}
		LOGGER.warn("[L2PcInstance] player [" + getName() + "] sent invalid link '" + cmd + "', ban this player!");
		return false;
	}
	
	/**
	 * Clear links.
	 */
	public synchronized void clearLinks()
	{
		validLink.clear();
	}
	
	/**
	 * Adds the link.
	 * @param link the link
	 */
	public synchronized void addLink(final String link)
	{
		if (link == null)
		{
			return;
		}
		validLink.add(link);
	}
	
	/**
	 * Checks if is in boat.
	 * @return Returns the inBoat.
	 */
	public boolean isInBoat()
	{
		return inBoat;
	}
	
	/**
	 * Sets the in boat.
	 * @param inBoat The inBoat to set.
	 */
	public void setInBoat(final boolean inBoat)
	{
		this.inBoat = inBoat;
	}
	
	/**
	 * Gets the boat.
	 * @return the boat
	 */
	public L2BoatInstance getBoat()
	{
		return boat;
	}
	
	/**
	 * Sets the boat.
	 * @param boat the new boat
	 */
	public void setBoat(final L2BoatInstance boat)
	{
		this.boat = boat;
	}
	
	/**
	 * Sets the in crystallize.
	 * @param inCrystallize the new in crystallize
	 */
	public void setInCrystallize(final boolean inCrystallize)
	{
		this.inCrystallize = inCrystallize;
	}
	
	/**
	 * Checks if is in crystallize.
	 * @return true, if is in crystallize
	 */
	public boolean isInCrystallize()
	{
		return inCrystallize;
	}
	
	/**
	 * Gets the in boat position.
	 * @return the in boat position
	 */
	public Point3D getInBoatPosition()
	{
		return inBoatPosition;
	}
	
	/**
	 * Sets the in boat position.
	 * @param pt the new in boat position
	 */
	public void setInBoatPosition(final Point3D pt)
	{
		inBoatPosition = pt;
	}
	
	/**
	 * Manage the delete task of a L2PcInstance (Leave Party, Unsummon pet, Save its inventory in the database, Remove it from the world...).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If the L2PcInstance is in observer mode, set its position to its position before entering in observer mode</li>
	 * <li>Set the online Flag to True or False and update the characters table of the database with online status and lastAccess</li>
	 * <li>Stop the HP/MP/CP Regeneration task</li>
	 * <li>Cancel Crafting, Attak or Cast</li>
	 * <li>Remove the L2PcInstance from the world</li>
	 * <li>Stop Party and Unsummon Pet</li>
	 * <li>Update database with items in its inventory and remove them from the world</li>
	 * <li>Remove all L2Object from knownObjects and knownPlayer of the L2Character then cancel Attak or Cast and notify AI</li>
	 * <li>Close the connection with the client</li><BR>
	 * <BR>
	 */
	public synchronized void deleteMe()
	{
		// Check if the L2PcInstance is in observer mode to set its position to its position before entering in observer mode
		if (inObserverMode())
		{
			setXYZ(obsX, obsY, obsZ);
		}
		
		if (isTeleporting())
		{
			try
			{
				wait(2000);
			}
			catch (final InterruptedException e)
			{
				e.printStackTrace();
			}
			onTeleported();
		}
		
		Castle castle = null;
		if (getClan() != null)
		{
			castle = CastleManager.getInstance().getCastleByOwner(getClan());
			if (castle != null)
			{
				castle.destroyClanGate();
			}
		}
		
		// Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout)
		try
		{
			setOnline(false);
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				t.printStackTrace();
			}
			
			LOGGER.error("deleteMe()", t);
		}
		
		// Stop the HP/MP/CP Regeneration task (scheduled tasks)
		try
		{
			stopAllTimers();
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				t.printStackTrace();
			}
			
			LOGGER.error("deleteMe()", t);
		}
		
		// Stop crafting, if in progress
		try
		{
			RecipeController.getInstance().requestMakeItemAbort(this);
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				t.printStackTrace();
			}
			
			LOGGER.error("deleteMe()", t);
		}
		
		// Cancel Attak or Cast
		try
		{
			abortAttack();
			abortCast();
			setTarget(null);
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				t.printStackTrace();
			}
			
			LOGGER.error("deleteMe()", t);
		}
		
		PartyMatchWaitingList.getInstance().removePlayer(this);
		if (partyroom != 0)
		{
			final PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(partyroom);
			if (room != null)
			{
				room.deleteMember(this);
			}
		}
		
		// Remove from world regions zones
		if (getWorldRegion() != null)
		{
			getWorldRegion().removeFromZones(this);
		}
		
		try
		{
			if (forceBuff != null)
			{
				abortCast();
			}
			
			for (final L2Character character : getKnownList().getKnownCharacters())
			{
				if (character.getForceBuff() != null && character.getForceBuff().getTarget() == this)
				{
					character.abortCast();
				}
			}
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				t.printStackTrace();
			}
			
			LOGGER.error("deleteMe()", t);
		}
		
		// Remove the L2PcInstance from the world
		if (isVisible())
		{
			try
			{
				decayMe();
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					t.printStackTrace();
				}
				
				LOGGER.error("deleteMe()", t);
			}
		}
		
		// If a Party is in progress, leave it
		if (isInParty())
		{
			try
			{
				leaveParty();
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					t.printStackTrace();
				}
				
				LOGGER.error("deleteMe()", t);
			}
		}
		
		// If the L2PcInstance has Pet, unsummon it
		if (getPet() != null)
		{
			try
			{
				getPet().unSummon(this);
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					t.printStackTrace();
				}
				
				LOGGER.error("deleteMe()", t);
			} // returns pet to control item
		}
		
		if (getClanId() != 0 && getClan() != null)
		{
			// set the status for pledge member list to OFFLINE
			try
			{
				L2ClanMember clanMember = getClan().getClanMember(getName());
				if (clanMember != null)
				{
					clanMember.setPlayerInstance(null);
				}
				clanMember = null;
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					t.printStackTrace();
				}
				
				LOGGER.error("deleteMe()", t);
			}
		}
		
		if (getActiveRequester() != null)
		{
			// deals with sudden exit in the middle of transaction
			setActiveRequester(null);
		}
		
		if (getOlympiadGameId() != -1)
		{
			Olympiad.getInstance().removeDisconnectedCompetitor(this);
		}
		
		// If the L2PcInstance is a GM, remove it from the GM List
		if (isGM())
		{
			try
			{
				GmListTable.getInstance().deleteGm(this);
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					t.printStackTrace();
				}
				
				LOGGER.error("deleteMe()", t);
			}
		}
		
		// Update database with items in its inventory and remove them from the world
		try
		{
			getInventory().deleteMe();
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				t.printStackTrace();
			}
			
			LOGGER.error("deleteMe()", t);
		}
		
		// Update database with items in its warehouse and remove them from the world
		try
		{
			clearWarehouse();
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				t.printStackTrace();
			}
			
			LOGGER.error("deleteMe()", t);
		}
		
		if (Config.WAREHOUSE_CACHE)
		{
			WarehouseCacheManager.getInstance().remCacheTask(this);
		}
		
		// Update database with items in its freight and remove them from the world
		try
		{
			getFreight().deleteMe();
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				t.printStackTrace();
			}
			
			LOGGER.error("deleteMe()", t);
		}
		
		// Remove all L2Object from knownObjects and knownPlayer of the L2Character then cancel Attak or Cast and notify AI
		try
		{
			getKnownList().removeAllKnownObjects();
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				t.printStackTrace();
			}
			
			LOGGER.error("deleteMe()", t);
		}
		
		// Close the connection with the client
		closeNetConnection();
		
		// remove from flood protector
		// FloodProtector.getInstance().removePlayer(getObjectId());
		
		if (getClanId() > 0)
		{
			getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);
			// ClanTable.getInstance().getClan(getClanId()).broadcastToOnlineMembers(new PledgeShowMemberListAdd(this));
		}
		
		for (final L2PcInstance player : snoopedPlayer)
		{
			player.removeSnooper(this);
		}
		
		for (final L2PcInstance player : snoopListener)
		{
			player.removeSnooped(this);
		}
		
		if (chanceSkills != null)
		{
			chanceSkills.setOwner(null);
			chanceSkills = null;
		}
		
		notifyFriends(true);
		
		// Remove L2Object object from allObjects of L2World
		L2World.getInstance().removeObject(this);
		L2World.getInstance().removeFromAllPlayers(this); // force remove in case of crash during teleport
		
	}
	
	/** ShortBuff clearing Task */
	private ScheduledFuture<?> shortBuffTask = null;
	
	private class ShortBuffTask implements Runnable
	{
		private L2PcInstance player = null;
		
		public ShortBuffTask(final L2PcInstance activeChar)
		{
			player = activeChar;
		}
		
		@Override
		public void run()
		{
			if (player == null)
			{
				return;
			}
			
			player.sendPacket(new ShortBuffStatusUpdate(0, 0, 0));
		}
	}
	
	/**
	 * @param magicId
	 * @param level
	 * @param time
	 */
	public void shortBuffStatusUpdate(final int magicId, final int level, final int time)
	{
		if (shortBuffTask != null)
		{
			shortBuffTask.cancel(false);
			shortBuffTask = null;
		}
		shortBuffTask = ThreadPoolManager.getInstance().scheduleGeneral(new ShortBuffTask(this), 15000);
		
		sendPacket(new ShortBuffStatusUpdate(magicId, level, time));
	}
	
	/** list of character friends. */
	private final List<String> friendList = new ArrayList<>();
	
	/**
	 * Gets the friend list.
	 * @return the friend list
	 */
	public List<String> getFriendList()
	{
		return friendList;
	}
	
	/**
	 * Restore friend list.
	 */
	public void restoreFriendList()
	{
		friendList.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHARACTER_FRIENDS))
		{
			statement.setInt(1, getObjectId());
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final String friendName = rset.getString("friend_name");
				
				if (friendName.equals(getName()))
				{
					continue;
				}
				
				final Integer blockedType = rset.getInt("not_blocked");
				
				if (blockedType == 1)
				{
					friendList.add(friendName);
				}
				else
				{
					blockList.getBlockList().add(friendName);
				}
			}
			DatabaseUtils.close(rset);
		}
		catch (final Exception e)
		{
			LOGGER.error("L2PcInstance.restoreFriendList : Could not restore friend data", e);
		}
	}
	
	/**
	 * Notify friends.
	 * @param closing the closing
	 */
	private void notifyFriends(final boolean closing)
	{
		for (final String friendName : friendList)
		{
			final L2PcInstance friend = L2World.getInstance().getPlayer(friendName);
			
			if (friend != null) // friend logged in.
			{
				friend.sendPacket(new FriendList(friend));
			}
		}
	}
	
	/** The fish. */
	private FishData fish;
	
	/*
	 * startFishing() was stripped of any pre-fishing related checks, namely the fishing zone check. Also worthy of note is the fact the code to find the hook landing position was also striped. The stripped code was moved into fishing.java. In my opinion it makes more sense for it to be there since all
	 * other skill related checks were also there. Last but not least, moving the zone check there, fixed a bug where baits would always be consumed no matter if fishing actualy took place. startFishing() now takes up 3 arguments, wich are acurately described as being the hook landing coordinates.
	 */
	/**
	 * Start fishing.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void startFishing(final int x, final int y, final int z)
	{
		stopMove(null);
		setIsImobilised(true);
		fishing = true;
		fishX = x;
		fishY = y;
		fishZ = z;
		broadcastUserInfo();
		// Starts fishing
		final int lvl = GetRandomFishLvl();
		final int group = GetRandomGroup();
		final int type = GetRandomFishType(group);
		List<FishData> fishs = FishTable.getInstance().getfish(lvl, type, group);
		if (fishs == null || fishs.size() == 0)
		{
			sendMessage("Error - Fishes are not definied");
			EndFishing(false);
			return;
		}
		final int check = Rnd.get(fishs.size());
		// Use a copy constructor else the fish data may be over-written below
		fish = new FishData(fishs.get(check));
		fishs.clear();
		fishs = null;
		sendPacket(new SystemMessage(SystemMessageId.CAST_LINE_AND_START_FISHING));
		ExFishingStart efs = null;
		
		if (!GameTimeController.getInstance().isNowNight() && lure.isNightLure())
		{
			fish.setType(-1);
		}
		
		// sendMessage("Hook x,y: " + x + "," + y + " - Water Z, Player Z:" + z + ", " + getZ()); //debug line, uncoment to show coordinates used in fishing.
		efs = new ExFishingStart(this, fish.getType(), x, y, z, lure.isNightLure());
		broadcastPacket(efs);
		efs = null;
		StartLookingForFishTask();
	}
	
	/**
	 * Stop looking for fish task.
	 */
	public void stopLookingForFishTask()
	{
		if (taskforfish != null)
		{
			taskforfish.cancel(false);
			taskforfish = null;
		}
	}
	
	/**
	 * Start looking for fish task.
	 */
	public void StartLookingForFishTask()
	{
		if (!isDead() && taskforfish == null)
		{
			int checkDelay = 0;
			boolean isNoob = false;
			boolean isUpperGrade = false;
			
			if (lure != null)
			{
				final int lureid = lure.getItemId();
				isNoob = fish.getGroup() == 0;
				isUpperGrade = fish.getGroup() == 2;
				if (lureid == 6519 || lureid == 6522 || lureid == 6525 || lureid == 8505 || lureid == 8508 || lureid == 8511)
				{
					checkDelay = Math.round((float) (fish.getGutsCheckTime() * 1.33));
				}
				else if (lureid == 6520 || lureid == 6523 || lureid == 6526 || lureid >= 8505 && lureid <= 8513 || lureid >= 7610 && lureid <= 7613 || lureid >= 7807 && lureid <= 7809 || lureid >= 8484 && lureid <= 8486)
				{
					checkDelay = Math.round((float) (fish.getGutsCheckTime() * 1.00));
				}
				else if (lureid == 6521 || lureid == 6524 || lureid == 6527 || lureid == 8507 || lureid == 8510 || lureid == 8513)
				{
					checkDelay = Math.round((float) (fish.getGutsCheckTime() * 0.66));
				}
			}
			taskforfish = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new LookingForFishTask(fish.getWaitTime(), fish.getFishGuts(), fish.getType(), isNoob, isUpperGrade), 10000, checkDelay);
		}
	}
	
	/**
	 * Gets the random group.
	 * @return the int
	 */
	private int GetRandomGroup()
	{
		switch (lure.getItemId())
		{
			case 7807: // green for beginners
			case 7808: // purple for beginners
			case 7809: // yellow for beginners
			case 8486: // prize-winning for beginners
				return 0;
			case 8485: // prize-winning luminous
			case 8506: // green luminous
			case 8509: // purple luminous
			case 8512: // yellow luminous
				return 2;
			default:
				return 1;
		}
	}
	
	/**
	 * Gets the random fish type.
	 * @param  group the group
	 * @return       the int
	 */
	private int GetRandomFishType(final int group)
	{
		final int check = Rnd.get(100);
		int type = 1;
		switch (group)
		{
			case 0: // fish for novices
				switch (lure.getItemId())
				{
					case 7807: // green lure, preferred by fast-moving (nimble) fish (type 5)
						if (check <= 54)
						{
							type = 5;
						}
						else if (check <= 77)
						{
							type = 4;
						}
						else
						{
							type = 6;
						}
						break;
					case 7808: // purple lure, preferred by fat fish (type 4)
						if (check <= 54)
						{
							type = 4;
						}
						else if (check <= 77)
						{
							type = 6;
						}
						else
						{
							type = 5;
						}
						break;
					case 7809: // yellow lure, preferred by ugly fish (type 6)
						if (check <= 54)
						{
							type = 6;
						}
						else if (check <= 77)
						{
							type = 5;
						}
						else
						{
							type = 4;
						}
						break;
					case 8486: // prize-winning fishing lure for beginners
						if (check <= 33)
						{
							type = 4;
						}
						else if (check <= 66)
						{
							type = 5;
						}
						else
						{
							type = 6;
						}
						break;
				}
				break;
			case 1: // normal fish
				switch (lure.getItemId())
				{
					case 7610:
					case 7611:
					case 7612:
					case 7613:
						type = 3;
						break;
					case 6519: // all theese lures (green) are prefered by fast-moving (nimble) fish (type 1)
					case 8505:
					case 6520:
					case 6521:
					case 8507:
						if (check <= 54)
						{
							type = 1;
						}
						else if (check <= 74)
						{
							type = 0;
						}
						else if (check <= 94)
						{
							type = 2;
						}
						else
						{
							type = 3;
						}
						break;
					case 6522: // all theese lures (purple) are prefered by fat fish (type 0)
					case 8508:
					case 6523:
					case 6524:
					case 8510:
						if (check <= 54)
						{
							type = 0;
						}
						else if (check <= 74)
						{
							type = 1;
						}
						else if (check <= 94)
						{
							type = 2;
						}
						else
						{
							type = 3;
						}
						break;
					case 6525: // all theese lures (yellow) are prefered by ugly fish (type 2)
					case 8511:
					case 6526:
					case 6527:
					case 8513:
						if (check <= 55)
						{
							type = 2;
						}
						else if (check <= 74)
						{
							type = 1;
						}
						else if (check <= 94)
						{
							type = 0;
						}
						else
						{
							type = 3;
						}
						break;
					case 8484: // prize-winning fishing lure
						if (check <= 33)
						{
							type = 0;
						}
						else if (check <= 66)
						{
							type = 1;
						}
						else
						{
							type = 2;
						}
						break;
				}
				break;
			case 2: // upper grade fish, luminous lure
				switch (lure.getItemId())
				{
					case 8506: // green lure, preferred by fast-moving (nimble) fish (type 8)
						if (check <= 54)
						{
							type = 8;
						}
						else if (check <= 77)
						{
							type = 7;
						}
						else
						{
							type = 9;
						}
						break;
					case 8509: // purple lure, preferred by fat fish (type 7)
						if (check <= 54)
						{
							type = 7;
						}
						else if (check <= 77)
						{
							type = 9;
						}
						else
						{
							type = 8;
						}
						break;
					case 8512: // yellow lure, preferred by ugly fish (type 9)
						if (check <= 54)
						{
							type = 9;
						}
						else if (check <= 77)
						{
							type = 8;
						}
						else
						{
							type = 7;
						}
						break;
					case 8485: // prize-winning fishing lure
						if (check <= 33)
						{
							type = 7;
						}
						else if (check <= 66)
						{
							type = 8;
						}
						else
						{
							type = 9;
						}
						break;
				}
		}
		return type;
	}
	
	/**
	 * Gets the random fish lvl.
	 * @return the int
	 */
	private int GetRandomFishLvl()
	{
		L2Effect[] effects = getAllEffects();
		int skilllvl = getSkillLevel(1315);
		for (final L2Effect e : effects)
		{
			if (e.getSkill().getId() == 2274)
			{
				skilllvl = (int) e.getSkill().getPower(this);
			}
		}
		if (skilllvl <= 0)
		{
			return 1;
		}
		int randomlvl;
		final int check = Rnd.get(100);
		
		if (check <= 50)
		{
			randomlvl = skilllvl;
		}
		else if (check <= 85)
		{
			randomlvl = skilllvl - 1;
			if (randomlvl <= 0)
			{
				randomlvl = 1;
			}
		}
		else
		{
			randomlvl = skilllvl + 1;
			if (randomlvl > 27)
			{
				randomlvl = 27;
			}
		}
		effects = null;
		
		return randomlvl;
	}
	
	/**
	 * Start fish combat.
	 * @param isNoob       the is noob
	 * @param isUpperGrade the is upper grade
	 */
	public void StartFishCombat(final boolean isNoob, final boolean isUpperGrade)
	{
		fishCombat = new L2Fishing(this, fish, isNoob, isUpperGrade);
	}
	
	/**
	 * End fishing.
	 * @param win the win
	 */
	public void EndFishing(final boolean win)
	{
		ExFishingEnd efe = new ExFishingEnd(win, this);
		broadcastPacket(efe);
		efe = null;
		fishing = false;
		fishX = 0;
		fishY = 0;
		fishZ = 0;
		broadcastUserInfo();
		
		if (fishCombat == null)
		{
			sendPacket(new SystemMessage(SystemMessageId.BAIT_LOST_FISH_GOT_AWAY));
		}
		
		fishCombat = null;
		lure = null;
		// Ends fishing
		sendPacket(new SystemMessage(SystemMessageId.REEL_LINE_AND_STOP_FISHING));
		setIsImobilised(false);
		stopLookingForFishTask();
	}
	
	/**
	 * Gets the fish combat.
	 * @return the l2 fishing
	 */
	public L2Fishing getFishCombat()
	{
		return fishCombat;
	}
	
	/**
	 * Gets the fishx.
	 * @return the int
	 */
	public int getFishx()
	{
		return fishX;
	}
	
	/**
	 * Gets the fishy.
	 * @return the int
	 */
	public int getFishy()
	{
		return fishY;
	}
	
	/**
	 * Gets the fishz.
	 * @return the int
	 */
	public int getFishz()
	{
		return fishZ;
	}
	
	public void setPartyFind(final int find)
	{
		party_find = find;
	}
	
	public int getPartyFind()
	{
		return party_find;
	}
	
	/**
	 * Sets the lure.
	 * @param lure the lure
	 */
	public void setLure(final L2ItemInstance lure)
	{
		this.lure = lure;
	}
	
	/**
	 * Gets the lure.
	 * @return the l2 item instance
	 */
	public L2ItemInstance GetLure()
	{
		return lure;
	}
	
	/**
	 * Gets the inventory limit.
	 * @return the int
	 */
	public int getInventoryLimit()
	{
		int ivlim;
		if (isGM())
		{
			ivlim = Config.INVENTORY_MAXIMUM_GM;
		}
		else if (getRace() == Race.dwarf)
		{
			ivlim = Config.INVENTORY_MAXIMUM_DWARF;
		}
		else
		{
			ivlim = Config.INVENTORY_MAXIMUM_NO_DWARF;
		}
		ivlim += (int) getStat().calcStat(Stats.INV_LIM, 0, null, null);
		
		return ivlim;
	}
	
	/**
	 * Gets the ware house limit.
	 * @return the int
	 */
	public int GetWareHouseLimit()
	{
		int whlim;
		if (getRace() == Race.dwarf)
		{
			whlim = Config.WAREHOUSE_SLOTS_DWARF;
		}
		else
		{
			whlim = Config.WAREHOUSE_SLOTS_NO_DWARF;
		}
		whlim += (int) getStat().calcStat(Stats.WH_LIM, 0, null, null);
		
		return whlim;
	}
	
	/**
	 * Gets the private sell store limit.
	 * @return the int
	 */
	public int GetPrivateSellStoreLimit()
	{
		int pslim;
		if (getRace() == Race.dwarf)
		{
			pslim = Config.MAX_PVTSTORE_SLOTS_DWARF;
		}
		
		else
		{
			pslim = Config.MAX_PVTSTORE_SLOTS_OTHER;
		}
		pslim += (int) getStat().calcStat(Stats.P_SELL_LIM, 0, null, null);
		
		return pslim;
	}
	
	/**
	 * Gets the private buy store limit.
	 * @return the int
	 */
	public int GetPrivateBuyStoreLimit()
	{
		int pblim;
		if (getRace() == Race.dwarf)
		{
			pblim = Config.MAX_PVTSTORE_SLOTS_DWARF;
		}
		else
		{
			pblim = Config.MAX_PVTSTORE_SLOTS_OTHER;
		}
		pblim += (int) getStat().calcStat(Stats.P_BUY_LIM, 0, null, null);
		
		return pblim;
	}
	
	/**
	 * Gets the freight limit.
	 * @return the int
	 */
	public int GetFreightLimit()
	{
		return Config.FREIGHT_SLOTS + (int) getStat().calcStat(Stats.FREIGHT_LIM, 0, null, null);
	}
	
	/**
	 * Gets the dwarf recipe limit.
	 * @return the int
	 */
	public int GetDwarfRecipeLimit()
	{
		int recdlim = Config.DWARF_RECIPE_LIMIT;
		recdlim += (int) getStat().calcStat(Stats.REC_D_LIM, 0, null, null);
		return recdlim;
	}
	
	/**
	 * Gets the common recipe limit.
	 * @return the int
	 */
	public int GetCommonRecipeLimit()
	{
		int recclim = Config.COMMON_RECIPE_LIMIT;
		recclim += (int) getStat().calcStat(Stats.REC_C_LIM, 0, null, null);
		return recclim;
	}
	
	/**
	 * Sets the mount object id.
	 * @param newID the new mount object id
	 */
	public void setMountObjectID(final int newID)
	{
		mountObjectID = newID;
	}
	
	/**
	 * Gets the mount object id.
	 * @return the mount object id
	 */
	public int getMountObjectID()
	{
		return mountObjectID;
	}
	
	/** The lure. */
	private L2ItemInstance lure = null;
	
	/**
	 * Get the current skill in use or return null.<BR>
	 * <BR>
	 * @return the current skill
	 */
	public SkillDat getCurrentSkill()
	{
		return playerCurrentSkill;
	}
	
	/**
	 * Create a new SkillDat object and set the player currentSkill.<BR>
	 * <BR>
	 * @param currentSkill the current skill
	 * @param ctrlPressed  the ctrl pressed
	 * @param shiftPressed the shift pressed
	 */
	public void setCurrentSkill(final L2Skill currentSkill, final boolean ctrlPressed, final boolean shiftPressed)
	{
		if (currentSkill == null)
		{
			if (Config.DEBUG)
			{
				LOGGER.info("Setting current skill: NULL for " + getName() + ".");
			}
			
			playerCurrentSkill = null;
			return;
		}
		
		if (Config.DEBUG)
		{
			LOGGER.info("Setting current skill: " + currentSkill.getName() + " (ID: " + currentSkill.getId() + ") for " + getName() + ".");
		}
		
		playerCurrentSkill = new SkillDat(currentSkill, ctrlPressed, shiftPressed);
	}
	
	/**
	 * Gets the queued skill.
	 * @return the queued skill
	 */
	public SkillDat getQueuedSkill()
	{
		return playerQueuedSkill;
	}
	
	/**
	 * Create a new SkillDat object and queue it in the player queuedSkill.<BR>
	 * <BR>
	 * @param queuedSkill  the queued skill
	 * @param ctrlPressed  the ctrl pressed
	 * @param shiftPressed the shift pressed
	 */
	public void setQueuedSkill(final L2Skill queuedSkill, final boolean ctrlPressed, final boolean shiftPressed)
	{
		if (queuedSkill == null)
		{
			if (Config.DEBUG)
			{
				LOGGER.info("Setting queued skill: NULL for " + getName() + ".");
			}
			
			playerQueuedSkill = null;
			return;
		}
		
		if (Config.DEBUG)
		{
			LOGGER.info("Setting queued skill: " + queuedSkill.getName() + " (ID: " + queuedSkill.getId() + ") for " + getName() + ".");
		}
		
		playerQueuedSkill = new SkillDat(queuedSkill, ctrlPressed, shiftPressed);
	}
	
	/**
	 * Gets the power grade.
	 * @return the power grade
	 */
	public int getPowerGrade()
	{
		return powerGrade;
	}
	
	/**
	 * Sets the power grade.
	 * @param power the new power grade
	 */
	public void setPowerGrade(final int power)
	{
		powerGrade = power;
	}
	
	/**
	 * Checks if is cursed weapon equiped.
	 * @return true, if is cursed weapon equiped
	 */
	public boolean isCursedWeaponEquiped()
	{
		return cursedWeaponEquipedId != 0;
	}
	
	/**
	 * Sets the cursed weapon equiped id.
	 * @param value the new cursed weapon equiped id
	 */
	public void setCursedWeaponEquipedId(final int value)
	{
		cursedWeaponEquipedId = value;
	}
	
	/**
	 * Gets the cursed weapon equiped id.
	 * @return the cursed weapon equiped id
	 */
	public int getCursedWeaponEquipedId()
	{
		return cursedWeaponEquipedId;
	}
	
	/** The charm of courage. */
	private boolean charmOfCourage = false;
	
	/**
	 * Gets the charm of courage.
	 * @return the charm of courage
	 */
	public boolean getCharmOfCourage()
	{
		return charmOfCourage;
	}
	
	/**
	 * Sets the charm of courage.
	 * @param val the new charm of courage
	 */
	public void setCharmOfCourage(final boolean val)
	{
		charmOfCourage = val;
		sendPacket(new EtcStatusUpdate(this));
	}
	
	/**
	 * Gets the death penalty buff level.
	 * @return the death penalty buff level
	 */
	public int getDeathPenaltyBuffLevel()
	{
		return deathPenaltyBuffLevel;
	}
	
	/**
	 * Sets the death penalty buff level.
	 * @param level the new death penalty buff level
	 */
	public void setDeathPenaltyBuffLevel(final int level)
	{
		deathPenaltyBuffLevel = level;
	}
	
	/**
	 * Calculate death penalty buff level.
	 * @param killer the killer
	 */
	public void calculateDeathPenaltyBuffLevel(final L2Character killer)
	{
		if (Rnd.get(100) <= Config.DEATH_PENALTY_CHANCE && !(killer instanceof L2PcInstance) && !isGM() && !(getCharmOfLuck() && (killer instanceof L2GrandBossInstance || killer instanceof L2RaidBossInstance)) && !(isInsideZone(L2Character.ZONE_PVP) || isInsideZone(L2Character.ZONE_SIEGE)))
		{
			increaseDeathPenaltyBuffLevel();
		}
	}
	
	/**
	 * Increase death penalty buff level.
	 */
	public void increaseDeathPenaltyBuffLevel()
	{
		if (getDeathPenaltyBuffLevel() >= 15)
		{
			return;
		}
		
		if (getDeathPenaltyBuffLevel() != 0)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());
			
			if (skill != null)
			{
				removeSkill(skill, true);
				skill = null;
			}
		}
		
		deathPenaltyBuffLevel++;
		
		addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
		sendPacket(new EtcStatusUpdate(this));
		SystemMessage sm = new SystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
		sm.addNumber(getDeathPenaltyBuffLevel());
		sendPacket(sm);
		sm = null;
		sendSkillList();
	}
	
	/**
	 * Reduce death penalty buff level.
	 */
	public void reduceDeathPenaltyBuffLevel()
	{
		if (getDeathPenaltyBuffLevel() <= 0)
		{
			return;
		}
		
		L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());
		
		if (skill != null)
		{
			removeSkill(skill, true);
			skill = null;
			sendSkillList();
		}
		
		deathPenaltyBuffLevel--;
		
		if (getDeathPenaltyBuffLevel() > 0)
		{
			addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
			sendPacket(new EtcStatusUpdate(this));
			SystemMessage sm = new SystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
			sm.addNumber(getDeathPenaltyBuffLevel());
			sendPacket(sm);
			sm = null;
			sendSkillList();
		}
		else
		{
			sendPacket(new EtcStatusUpdate(this));
			sendPacket(new SystemMessage(SystemMessageId.DEATH_PENALTY_LIFTED));
		}
	}
	
	/**
	 * Restore death penalty buff level.
	 */
	public void restoreDeathPenaltyBuffLevel()
	{
		L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());
		
		if (skill != null)
		{
			removeSkill(skill, true);
			skill = null;
		}
		
		if (getDeathPenaltyBuffLevel() > 0)
		{
			addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
			SystemMessage sm = new SystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
			sm.addNumber(getDeathPenaltyBuffLevel());
			sendPacket(sm);
			sm = null;
		}
		sendPacket(new EtcStatusUpdate(this));
	}
	
	/** The Reuse time stamps. */
	private Map<Integer, TimeStamp> reuseTimeStamps = new ConcurrentHashMap<>();
	
	/**
	 * Index according to skill id the current timestamp of use.
	 * @param s the s
	 * @param r the r
	 */
	@Override
	public void addTimeStamp(final L2Skill s, final int r)
	{
		reuseTimeStamps.put(s.getReuseHashCode(), new TimeStamp(s, r));
	}
	
	/**
	 * Index according to skill this TimeStamp instance for restoration purposes only.
	 * @param T the t
	 */
	private void addTimeStamp(final TimeStamp T)
	{
		reuseTimeStamps.put(T.getSkill().getReuseHashCode(), T);
	}
	
	/**
	 * Index according to skill id the current timestamp of use.
	 * @param s the s
	 */
	@Override
	public void removeTimeStamp(final L2Skill s)
	{
		reuseTimeStamps.remove(s.getReuseHashCode());
	}
	
	public Collection<TimeStamp> getReuseTimeStamps()
	{
		return reuseTimeStamps.values();
	}
	
	public void resetSkillTime(final boolean ssl)
	{
		final L2Skill arr$[] = getAllSkills();
		for (final L2Skill skill : arr$)
		{
			if (skill != null && skill.isActive() && skill.getId() != 1324)
			{
				enableSkill(skill);
			}
		}
		
		if (ssl)
		{
			sendSkillList();
		}
		sendPacket(new SkillCoolTime(this));
	}
	
	/*
	 * public boolean isInDangerArea() { return isInDangerArea; } public void enterDangerArea() { L2Skill skill = SkillTable.getInstance().getInfo(4268, 1); if(skill != null) { removeSkill(skill, true); skill = null; } addSkill(skill, false); isInDangerArea = true; sendPacket(new EtcStatusUpdate(this));
	 * SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2); sm.addString("You have entered a danger area"); sendPacket(sm); sm = null; } public void exitDangerArea() { L2Skill skill = SkillTable.getInstance().getInfo(4268, 1); if(skill != null) { removeSkill(skill, true); skill = null; }
	 * isInDangerArea = false; sendPacket(new EtcStatusUpdate(this)); SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2); sm.addString("You have left a danger area"); sendPacket(sm); sm = null; }
	 */
	
	@Override
	public final void sendDamageMessage(final L2Character target, final int damage, final boolean mcrit, final boolean pcrit, final boolean miss)
	{
		// Check if hit is missed
		if (miss)
		{
			sendPacket(new SystemMessage(SystemMessageId.MISSED_TARGET));
			return;
		}
		
		// Check if hit is critical
		if (pcrit)
		{
			sendPacket(new SystemMessage(SystemMessageId.CRITICAL_HIT));
			
		}
		
		if (mcrit)
		{
			sendPacket(new SystemMessage(SystemMessageId.CRITICAL_HIT_MAGIC));
			
		}
		
		if (isInOlympiadMode() && target instanceof L2PcInstance && ((L2PcInstance) target).isInOlympiadMode() && ((L2PcInstance) target).getOlympiadGameId() == getOlympiadGameId())
		{
			Olympiad.getInstance().notifyCompetitorDamage(this, damage, getOlympiadGameId());
		}
		
		SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DID_S1_DMG);
		sm.addNumber(damage);
		sendPacket(sm);
		sm = null;
	}
	
	/**
	 * Update title.
	 */
	public void updateTitle()
	{
		setTitle(Config.PVP_TITLE_PREFIX + getPvpKills() + Config.PK_TITLE_PREFIX + getPkKills() + " ");
	}
	
	/**
	 * Return true if last request is expired.
	 * @return true, if is request expired
	 */
	public boolean isRequestExpired()
	{
		return !(requestExpireTime > GameTimeController.getGameTicks());
	}
	
	/** The gm status. */
	boolean gmStatus = true; // true by default sincce this is used by GMS
	
	// private Object BanChatTask;
	
	// private long banchat_timer;
	
	/**
	 * Sets the gm status active.
	 * @param state the new gm status active
	 */
	public void setGmStatusActive(final boolean state)
	{
		gmStatus = state;
	}
	
	/**
	 * Checks for gm status active.
	 * @return true, if successful
	 */
	public boolean hasGmStatusActive()
	{
		return gmStatus;
	}
	
	/** The saymode. */
	public L2Object saymode = null;
	
	/**
	 * Gets the say mode.
	 * @return the say mode
	 */
	public L2Object getSayMode()
	{
		return saymode;
	}
	
	/**
	 * Sets the say mode.
	 * @param say the new say mode
	 */
	public void setSayMode(final L2Object say)
	{
		saymode = say;
	}
	
	/**
	 * Save event stats.
	 */
	public void saveEventStats()
	{
		originalNameColor = getAppearance().getNameColor();
		originalKarma = getKarma();
		eventKills = 0;
	}
	
	/**
	 * Restore event stats.
	 */
	public void restoreEventStats()
	{
		getAppearance().setNameColor(originalNameColor);
		setKarma(originalKarma);
		eventKills = 0;
	}
	
	/**
	 * Gets the current skill world position.
	 * @return the current skill world position
	 */
	public Point3D getCurrentSkillWorldPosition()
	{
		return currentSkillWorldPosition;
	}
	
	/**
	 * Sets the current skill world position.
	 * @param worldPosition the new current skill world position
	 */
	public void setCurrentSkillWorldPosition(final Point3D worldPosition)
	{
		currentSkillWorldPosition = worldPosition;
	}
	
	// //////////////////////////////////////////////
	/**
	 * Checks if is cursed weapon equipped.
	 * @return true, if is cursed weapon equipped
	 */
	public boolean isCursedWeaponEquipped()
	{
		return cursedWeaponEquipedId != 0;
	}
	
	// public void setCombatFlagEquipped(boolean value)
	// {
	// combatFlagEquippedId = value;
	// }
	
	/**
	 * Dismount.
	 * @return true, if successful
	 */
	public boolean dismount()
	{
		if (setMountType(0))
		{
			if (isFlying())
			{
				removeSkill(SkillTable.getInstance().getInfo(4289, 1));
			}
			
			Ride dismount = new Ride(getObjectId(), Ride.ACTION_DISMOUNT, 0);
			broadcastPacket(dismount);
			dismount = null;
			setMountObjectID(0);
			
			// Notify self and others about speed change
			broadcastUserInfo();
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the pc bang score.
	 * @return the pc bang score
	 */
	public int getPcBangScore()
	{
		return pcBangPoint;
	}
	
	/**
	 * Reduce pc bang score.
	 * @param to the to
	 */
	public void reducePcBangScore(final int to)
	{
		pcBangPoint -= to;
		updatePcBangWnd(to, false, false);
	}
	
	/**
	 * Adds the pc bang score.
	 * @param to the to
	 */
	public void addPcBangScore(final int to)
	{
		pcBangPoint += to;
	}
	
	/**
	 * Update pc bang wnd.
	 * @param score the score
	 * @param add   the add
	 * @param duble the duble
	 */
	public void updatePcBangWnd(final int score, final boolean add, final boolean duble)
	{
		final ExPCCafePointInfo wnd = new ExPCCafePointInfo(this, score, add, 24, duble);
		sendPacket(wnd);
	}
	
	/**
	 * Show pc bang window.
	 */
	public void showPcBangWindow()
	{
		final ExPCCafePointInfo wnd = new ExPCCafePointInfo(this, 0, false, 24, false);
		sendPacket(wnd);
	}
	
	/**
	 * String to hex.
	 * @param  color the color
	 * @return       the string
	 */
	private String StringToHex(String color)
	{
		switch (color.length())
		{
			case 1:
				color = new StringBuilder().append("00000").append(color).toString();
				break;
			
			case 2:
				color = new StringBuilder().append("0000").append(color).toString();
				break;
			
			case 3:
				color = new StringBuilder().append("000").append(color).toString();
				break;
			
			case 4:
				color = new StringBuilder().append("00").append(color).toString();
				break;
			
			case 5:
				color = new StringBuilder().append('0').append(color).toString();
				break;
		}
		return color;
	}
	
	/**
	 * Checks if is offline.
	 * @return true, if is offline
	 */
	public boolean isInOfflineMode()
	{
		return isInOfflineMode;
	}
	
	/**
	 * Sets the offline.
	 * @param set the new offline
	 */
	public void setOfflineMode(final boolean set)
	{
		isInOfflineMode = set;
	}
	
	/**
	 * Checks if is trade disabled.
	 * @return true, if is trade disabled
	 */
	public boolean isTradeDisabled()
	{
		return isTradeOff || isCastingNow();
	}
	
	/**
	 * Sets the trade disabled.
	 * @param set the new trade disabled
	 */
	public void setTradeDisabled(final boolean set)
	{
		isTradeOff = set;
	}
	
	/**
	 * Show teleport html.
	 */
	public void showTeleportHtml()
	{
		final TextBuilder text = new TextBuilder();
		text.append("<html>");
		text.append("<body>");
		text.append("<title></title>");
		text.append("<center>");
		text.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>");
		text.append("<br><br>");
		text.append("<table width=\"85%\"><tr><td>Your party leader, " + getParty().getLeader().getName() + ", requested a group teleport to raidboss. You have 30 seconds from this popup to teleport, or the teleport windows will close</td></tr></table><br>");
		text.append("<a action=\"bypass -h rbAnswear\">Port with my party</a><br>");
		text.append("<a action=\"bypass -h rbAnswearDenied\">Don't port</a><br1>");
		text.append("<center><img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\">");
		text.append("<font color=\"999999\">Gates of Fire</font></center>");
		text.append("</body>");
		text.append("</html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(text.toString());
		sendPacket(html);
	}
	
	/** The Dropzor. */
	String Dropzor = "Coin of Luck";
	
	/**
	 * Show raidboss info level40.
	 */
	public void showRaidbossInfoLevel40()
	{
		final TextBuilder text = new TextBuilder();
		text.append("<html>");
		text.append("<body>");
		text.append("<title>Raidboss Level (40-45)</title>");
		text.append("<center>");
		text.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>");
		text.append("</center>");
		text.append("<br><br>");
		text.append("<table width=\"85%\"><tr><td>Drop: " + Dropzor + "</td></tr></table>");
		text.append("Leto Chief Talkin (40)<br1>");
		text.append("Water Spirit Lian (40) <br1>");
		text.append("Shaman King Selu (40) <br1>");
		text.append("Gwindorr (40) <br1>");
		text.append("Icarus Sample 1 (40) <br1>");
		text.append("Fafurion's Page Sika (40) <br1>");
		text.append("Nakondas (40) <br1>");
		text.append("Road Scavenger Leader (40)<br1>");
		text.append("Wizard of Storm Teruk (40) <br1>");
		text.append("Water Couatle Ateka (40)<br1>");
		text.append("Crazy Mechanic Golem (43) <br1>");
		text.append("Earth Protector Panathen (43) <br1>");
		text.append("Thief Kelbar (44) <br1>");
		text.append("Timak Orc Chief Ranger (44) <br1>");
		text.append("Rotten Tree Repiro (44) <br1>");
		text.append("Dread Avenger Kraven (44) <br1>");
		text.append("Biconne of Blue Sky (45)<br1>");
		text.append("Evil Spirit Cyrion (45) <br1>");
		text.append("Iron Giant Totem (45) <br1>");
		text.append("Timak Orc Gosmos (45) <br1>");
		text.append("Shacram (45) <br1>");
		text.append("Fafurion's Henchman Istary (45) <br1>");
		text.append("<center><img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\">");
		text.append("<font color=\"999999\">Gates of Fire</font></center>");
		text.append("</body>");
		text.append("</html>");
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(text.toString());
		sendPacket(html);
	}
	
	/**
	 * Show raidboss info level45.
	 */
	public void showRaidbossInfoLevel45()
	{
		final TextBuilder text = new TextBuilder();
		text.append("<html>");
		text.append("<body>");
		text.append("<title>Raidboss Level (45-50)</title>");
		text.append("<center>");
		text.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>");
		text.append("</center>");
		text.append("<br><br>");
		text.append("<table width=\"85%\"><tr><td>Drop: " + Dropzor + "</td></tr></table>");
		text.append("Necrosentinel Royal Guard (47) <br1>");
		text.append("Barion (47) <br1>");
		text.append("Orfen's Handmaiden (48) <br1>");
		text.append("King Tarlk (48) <br1>");
		text.append("Katu Van Leader Atui (49) <br1>");
		text.append("Mirror of Oblivion (49) <br1>");
		text.append("Karte (49) <br1>");
		text.append("Ghost of Peasant Leader (50) <br1>");
		text.append("Cursed Clara (50) <br1>");
		text.append("Carnage Lord Gato (50) <br1>");
		text.append("Fafurion's Henchman Istary (45) <br1>");
		text.append("<center><img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\">");
		text.append("<font color=\"999999\">Gates of Fire</font></center>");
		text.append("</body>");
		text.append("</html>");
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(text.toString());
		sendPacket(html);
	}
	
	/**
	 * Show raidboss info level50.
	 */
	public void showRaidbossInfoLevel50()
	{
		final TextBuilder text = new TextBuilder();
		text.append("<html>");
		text.append("<body>");
		text.append("<title>Raidboss Level (50-55)</title>");
		text.append("<center>");
		text.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>");
		text.append("</center>");
		text.append("<br><br>");
		text.append("<table width=\"85%\"><tr><td>Drop: " + Dropzor + "</td></tr></table>");
		text.append("Verfa (51) <br1>");
		text.append("Deadman Ereve (51) <br1>");
		text.append("Captain of Red Flag Shaka (52) <br1>");
		text.append("Grave Robber Kim (52) <br1>");
		text.append("Paniel the Unicorn (54) <br1>");
		text.append("Bandit Leader Barda (55) <br1>");
		text.append("Eva's Spirit Niniel (55) <br1>");
		text.append("Beleth's Seer Sephia (55) <br1>");
		text.append("Pagan Watcher Cerberon (55) <br1>");
		text.append("Shaman King Selu (55) <br1>");
		text.append("Black Lily (55) <br1>");
		text.append("Ghost Knight Kabed (55) <br1>");
		text.append("Sorcerer Isirr (55) <br1>");
		text.append("Furious Thieles (55) <br1>");
		text.append("Enchanted Forest Watcher Ruell (55) <br1>");
		text.append("<center><img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\">");
		text.append("<font color=\"999999\">Gates of Fire</font></center>");
		text.append("</body>");
		text.append("</html>");
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(text.toString());
		sendPacket(html);
	}
	
	/**
	 * Show raidboss info level55.
	 */
	public void showRaidbossInfoLevel55()
	{
		final TextBuilder text = new TextBuilder();
		text.append("<html>");
		text.append("<body>");
		text.append("<title>Raidboss Level (55-60)</title>");
		text.append("<center>");
		text.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>");
		text.append("</center>");
		text.append("<br><br>");
		text.append("<table width=\"85%\"><tr><td>Drop: " + Dropzor + "</td></tr></table>");
		text.append("Fairy Queen Timiniel (56) <br1>");
		text.append("Harit Guardian Garangky (56) <br1>");
		text.append("Refugee Hopeful Leo (56) <br1>");
		text.append("Timak Seer Ragoth (57) <br1>");
		text.append("Soulless Wild Boar (59) <br1>");
		text.append("Abyss Brukunt (59) <br1>");
		text.append("Giant Marpanak (60) <br1>");
		text.append("Ghost of the Well Lidia (60) <br1>");
		text.append("Guardian of the Statue of Giant Karum (60) <br1>");
		text.append("The 3rd Underwater Guardian (60) <br1>");
		text.append("Taik High Prefect Arak (60) <br1>");
		text.append("Ancient Weird Drake (60) <br1>");
		text.append("Lord Ishka (60) <br1>");
		text.append("<center><img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\">");
		text.append("<font color=\"999999\">Gates of Fire</font></center>");
		text.append("</body>");
		text.append("</html>");
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(text.toString());
		sendPacket(html);
	}
	
	/**
	 * Show raidboss info level60.
	 */
	public void showRaidbossInfoLevel60()
	{
		final TextBuilder text = new TextBuilder();
		text.append("<html>");
		text.append("<body>");
		text.append("<title>Raidboss Level (60-65)</title>");
		text.append("<center>");
		text.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>");
		text.append("</center>");
		text.append("<br><br>");
		text.append("<table width=\"85%\"><tr><td>Drop: " + Dropzor + "</td></tr></table>");
		text.append("Roaring Lord Kastor (62) <br1>");
		text.append("Gorgolos (64) <br1>");
		text.append("Hekaton Prime (65) <br1>");
		text.append("Gargoyle Lord Tiphon (65) <br1>");
		text.append("Fierce Tiger King Angel (65) <br1>");
		text.append("Enmity Ghost Ramdal (65) <br1>");
		text.append("Rahha (65) <br1>");
		text.append("Shilen's Priest Hisilrome (65) <br1>");
		text.append("<center><img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\">");
		text.append("<font color=\"999999\">Gates of Fire</font></center>");
		text.append("</body>");
		text.append("</html>");
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(text.toString());
		sendPacket(html);
	}
	
	/**
	 * Show raidboss info level65.
	 */
	public void showRaidbossInfoLevel65()
	{
		final TextBuilder text = new TextBuilder();
		text.append("<html>");
		text.append("<body>");
		text.append("<title>Raidboss Level (65-70)</title>");
		text.append("<br><br>");
		text.append("<center>");
		text.append("<table width=\"85%\"><tr><td>Drop: " + Dropzor + "</td></tr></table>");
		text.append("</center>");
		text.append("Demon's Agent Falston (66) <br1>");
		text.append("Last Titan utenus (66) <br1>");
		text.append("Kernon's Faithful Servant Kelone (67) <br1>");
		text.append("Spirit of Andras, the Betrayer (69) <br1>");
		text.append("Bloody Priest Rudelto (69) <br1>");
		text.append("Shilen's Messenger Cabrio (70) <br1>");
		text.append("Anakim's Nemesis Zakaron (70) <br1>");
		text.append("Flame of Splendor Barakiel (70) <br1>");
		text.append("Roaring Skylancer (70) <br1>");
		text.append("Beast Lord Behemoth (70) <br1>");
		text.append("Palibati Queen Themis (70) <br1>");
		text.append("Fafurion''s Herald Lokness (70) <br1>");
		text.append("Meanas Anor (70) <br1>");
		text.append("Korim (70) <br1>");
		text.append("<center><img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\">");
		text.append("<font color=\"999999\">Gates of Fire</font></center>");
		text.append("</body>");
		text.append("</html>");
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(text.toString());
		sendPacket(html);
	}
	
	/**
	 * Show raidboss info level70.
	 */
	public void showRaidbossInfoLevel70()
	{
		final TextBuilder text = new TextBuilder();
		text.append("<html>");
		text.append("<body>");
		text.append("<title>Raidboss Level (70-75)</title>");
		text.append("<center>");
		text.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>");
		text.append("</center>");
		text.append("<br><br>");
		text.append("<table width=\"85%\"><tr><td>Drop: " + Dropzor + "</td></tr></table>");
		text.append("Immortal Savior Mardil (71) <br1>");
		text.append("Vanor Chief Kandra (72) <br1>");
		text.append("Water Dragon Seer Sheshark (72) <br1>");
		text.append("Doom Blade Tanatos (72) <br1>");
		text.append("Death Lord Hallate (73) <br1>");
		text.append("Plague Golem (73) <br1>");
		text.append("Icicle Emperor Bumbalump (74) <br1>");
		text.append("Antharas Priest Cloe (74) <br1>");
		text.append("Krokian Padisha Sobekk (74) <br1>");
		text.append("<center><img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\">");
		text.append("<font color=\"999999\">Gates of Fire</font></center>");
		text.append("</body>");
		text.append("</html>");
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(text.toString());
		sendPacket(html);
	}
	
	/** The isintwtown. */
	private boolean isintwtown = false;
	
	/**
	 * Checks if is inside tw town.
	 * @return true, if is inside tw town
	 */
	public boolean isInsideTWTown()
	{
		if (isintwtown)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Sets the inside tw town.
	 * @param b the new inside tw town
	 */
	public void setInsideTWTown(final boolean b)
	{
		isintwtown = true;
	}
	
	/**
	 * check if local player can make multibox and also refresh local boxes instances number.
	 * @return true, if successful
	 */
	public boolean checkMultiBox()
	{
		
		boolean output = true;
		
		int boxes_number = 0; // this one
		final List<String> active_boxes = new ArrayList<>();
		
		if (getClient() != null && getClient().getConnection() != null && !getClient().getConnection().isClosed() && getClient().getConnection().getInetAddress() != null)
		{
			
			final String thisip = getClient().getConnection().getInetAddress().getHostAddress();
			final Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers();
			for (final L2PcInstance player : allPlayers)
			{
				if (player != null)
				{
					if (player.isOnline() && player.getClient() != null && player.getClient().getConnection() != null && !player.getClient().getConnection().isClosed() && player.getClient().getConnection().getInetAddress() != null && !player.getName().equals(getName()))
					{
						
						final String ip = player.getClient().getConnection().getInetAddress().getHostAddress();
						if (thisip.equals(ip) && this != player)
						{
							if (!Config.ALLOW_DUALBOX)
							{
								
								output = false;
								break;
								
							}
							
							if (boxes_number + 1 > Config.ALLOWED_BOXES)
							{ // actual count+actual player one
								output = false;
								break;
							}
							boxes_number++;
							active_boxes.add(player.getName());
						}
					}
				}
			}
		}
		
		if (output)
		{
			activeBoxesCount = boxes_number + 1; // current number of boxes+this one
			if (!active_boxes.contains(getName()))
			{
				active_boxes.add(getName());
				
				active_boxes_characters = active_boxes;
			}
			refreshOtherBoxes();
		}
		return output;
	}
	
	/**
	 * increase active boxes number for local player and other boxer for same ip.
	 */
	public void refreshOtherBoxes()
	{
		
		if (getClient() != null && getClient().getConnection() != null && !getClient().getConnection().isClosed() && getClient().getConnection().getInetAddress() != null)
		{
			
			final String thisip = getClient().getConnection().getInetAddress().getHostAddress();
			final Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers();
			final L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);
			
			for (final L2PcInstance player : players)
			{
				if (player != null && player.isOnline())
				{
					if (player.getClient() != null && player.getClient().getConnection() != null && !player.getClient().getConnection().isClosed() && !player.getName().equals(getName()))
					{
						
						final String ip = player.getClient().getConnection().getInetAddress().getHostAddress();
						if (thisip.equals(ip) && this != player)
						{
							player.activeBoxesCount = activeBoxesCount;
							player.active_boxes_characters = active_boxes_characters;
						}
					}
				}
			}
		}
		
	}
	
	/**
	 * descrease active boxes number for local player and other boxer for same ip.
	 */
	public void decreaseBoxes()
	{
		
		activeBoxesCount = activeBoxesCount - 1;
		active_boxes_characters.remove(getName());
		
		refreshOtherBoxes();
	}
	
	public boolean isAio()
	{
		return isAio;
	}
	
	public void setAio(final boolean val)
	{
		isAio = val;
	}
	
	public void giveAioSkills()
	{
		for (Integer skillid : Config.AIO_SKILLS.keySet())
		{
			int skilllvl = Config.AIO_SKILLS.get(skillid);
			L2Skill skill = SkillTable.getInstance().getInfo(skillid, skilllvl);
			
			if (skill != null)
			{
				addSkill(skill, true);
			}
		}
		sendMessage("GM give to you Aio's skills");
	}
	
	public void removeAioSkills()
	{
		for (Integer skillid : Config.AIO_SKILLS.keySet())
		{
			int skilllvl = Config.AIO_SKILLS.get(skillid);
			L2Skill skill = SkillTable.getInstance().getInfo(skillid, skilllvl);
			removeSkill(skill);
		}
	}
	
	/**
	 * @param epochTime 0 = AIO forever
	 */
	public void setAioEndDate(long epochTime)
	{
		aioEndTime = epochTime;
	}
	
	public long getAioEndTime()
	{
		return aioEndTime;
	}
	
	/**
	 * Gets the offline start time.
	 * @return the offline start time
	 */
	public long getOfflineStartTime()
	{
		return offlineShopStart;
	}
	
	/**
	 * Sets the offline start time.
	 * @param time the new offline start time
	 */
	public void setOfflineStartTime(final long time)
	{
		offlineShopStart = time;
	}
	
	// during fall validations will be disabled for 10 ms.
	/** The Constant FALLING_VALIDATION_DELAY. */
	private static final int FALLING_VALIDATION_DELAY = 10000;
	
	/** The falling timestamp. */
	private long fallingTimestamp = 0;
	
	/**
	 * Return true if character falling now On the start of fall return false for correct coord sync !.
	 * @param  z the z
	 * @return   true, if is falling
	 */
	public final boolean isFalling(final int z)
	{
		if (isDead() || isFlying() || isInvul() || isInFunEvent() || isInsideZone(ZONE_WATER))
		{
			return false;
		}
		
		if (System.currentTimeMillis() < fallingTimestamp)
		{
			return true;
		}
		
		final int deltaZ = getZ() - z;
		if (deltaZ <= getBaseTemplate().getFallHeight())
		{
			return false;
		}
		
		final int damage = (int) Formulas.calcFallDam(this, deltaZ);
		if (damage > 0)
		{
			reduceCurrentHp(Math.min(damage, getCurrentHp() - 1), null, false);
			sendPacket(new SystemMessage(SystemMessageId.FALL_DAMAGE_S1).addNumber(damage));
		}
		
		setFalling();
		
		return false;
	}
	
	/**
	 * Set falling timestamp.
	 */
	public final void setFalling()
	{
		fallingTimestamp = System.currentTimeMillis() + FALLING_VALIDATION_DELAY;
	}
	
	/** Previous coordinate sent to party in ValidatePosition *. */
	private final Point3D lastPartyPosition = new Point3D(0, 0, 0);
	
	/**
	 * Sets the last party position.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void setLastPartyPosition(final int x, final int y, final int z)
	{
		lastPartyPosition.setXYZ(x, y, z);
	}
	
	/**
	 * Gets the last party position distance.
	 * @param  x the x
	 * @param  y the y
	 * @param  z the z
	 * @return   the last party position distance
	 */
	public int getLastPartyPositionDistance(final int x, final int y, final int z)
	{
		final double dx = x - lastPartyPosition.getX();
		final double dy = y - lastPartyPosition.getY();
		final double dz = z - lastPartyPosition.getZ();
		
		return (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	/**
	 * Checks if is awaying.
	 * @return the awaying
	 */
	public boolean isAwaying()
	{
		return awaying;
	}
	
	/**
	 * Sets the awaying.
	 * @param awaying the awaying to set
	 */
	public void set_awaying(final boolean awaying)
	{
		this.awaying = awaying;
	}
	
	/**
	 * Checks if is locked.
	 * @return true, if is locked
	 */
	public boolean isLocked()
	{
		return isLocked;
	}
	
	/**
	 * Sets the locked.
	 * @param a the new locked
	 */
	public void setLocked(final boolean a)
	{
		isLocked = a;
	}
	
	/**
	 * Checks if is stored.
	 * @return true, if is stored
	 */
	public boolean isStored()
	{
		return isStored;
	}
	
	/**
	 * Sets the stored.
	 * @param a the new stored
	 */
	public void setStored(final boolean a)
	{
		isStored = a;
	}
	
	/** The punish level. */
	private PunishLevel punishLevel = PunishLevel.NONE;
	
	/** The punish timer. */
	private long punishTimer = 0;
	
	/** The punish task. */
	private ScheduledFuture<?> punishTask;
	
	/**
	 * The Enum PunishLevel.
	 */
	public enum PunishLevel
	{
		
		/** The NONE. */
		NONE(0, ""),
		
		/** The CHAT. */
		CHAT(1, "chat banned"),
		
		/** The JAIL. */
		JAIL(2, "jailed"),
		
		/** The CHAR. */
		CHAR(3, "banned"),
		
		/** The ACC. */
		ACC(4, "banned");
		
		/** The pun value. */
		private final int punValue;
		
		/** The pun string. */
		private final String punString;
		
		/**
		 * Instantiates a new punish level.
		 * @param value  the value
		 * @param string the string
		 */
		PunishLevel(final int value, final String string)
		{
			punValue = value;
			punString = string;
		}
		
		/**
		 * Value.
		 * @return the int
		 */
		public int value()
		{
			return punValue;
		}
		
		/**
		 * String.
		 * @return the string
		 */
		public String string()
		{
			return punString;
		}
	}
	
	// open/close gates
	private final GatesRequest gatesRequest = new GatesRequest();
	
	private static class GatesRequest
	{
		private L2DoorInstance target = null;
		
		public GatesRequest()
		{
			// Nothing to do
		}
		
		public void setTarget(final L2DoorInstance door)
		{
			target = door;
		}
		
		public L2DoorInstance getDoor()
		{
			return target;
		}
	}
	
	public void gatesRequest(final L2DoorInstance door)
	{
		gatesRequest.setTarget(door);
	}
	
	public void gatesAnswer(final int answer, final int type)
	{
		if (gatesRequest.getDoor() == null)
		{
			return;
		}
		
		if (answer == 1 && getTarget() == gatesRequest.getDoor() && type == 1)
		{
			gatesRequest.getDoor().openMe();
		}
		else if (answer == 1 && getTarget() == gatesRequest.getDoor() && type == 0)
		{
			gatesRequest.getDoor().closeMe();
		}
		
		gatesRequest.setTarget(null);
	}
	
	/**
	 * returns punishment level of player.
	 * @return the punish level
	 */
	public PunishLevel getPunishLevel()
	{
		return punishLevel;
	}
	
	/**
	 * Checks if is in jail.
	 * @return True if player is jailed
	 */
	public boolean isInJail()
	{
		return punishLevel == PunishLevel.JAIL;
	}
	
	/**
	 * Checks if is chat banned.
	 * @return True if player is chat banned
	 */
	public boolean isChatBanned()
	{
		return punishLevel == PunishLevel.CHAT;
	}
	
	/**
	 * Sets the punish level.
	 * @param state the new punish level
	 */
	public void setPunishLevel(final int state)
	{
		switch (state)
		{
			case 0:
			{
				punishLevel = PunishLevel.NONE;
				break;
			}
			case 1:
			{
				punishLevel = PunishLevel.CHAT;
				break;
			}
			case 2:
			{
				punishLevel = PunishLevel.JAIL;
				break;
			}
			case 3:
			{
				punishLevel = PunishLevel.CHAR;
				break;
			}
			case 4:
			{
				punishLevel = PunishLevel.ACC;
				break;
			}
		}
	}
	
	/**
	 * Sets the punish level.
	 * @param state          the state
	 * @param delayInMinutes the delay in minutes
	 */
	public void setPunishLevel(final PunishLevel state, final int delayInMinutes)
	{
		final long delayInMilliseconds = delayInMinutes * 60000L;
		setPunishLevel(state, delayInMilliseconds);
		
	}
	
	/**
	 * Sets punish level for player based on delay.
	 * @param state               the state
	 * @param delayInMilliseconds 0 - Indefinite
	 */
	public void setPunishLevel(final PunishLevel state, final long delayInMilliseconds)
	{
		switch (state)
		{
			case NONE: // Remove Punishments
			{
				switch (punishLevel)
				{
					case CHAT:
					{
						punishLevel = state;
						stopPunishTask(true);
						sendPacket(new EtcStatusUpdate(this));
						sendMessage("Your Chat ban has been lifted");
						break;
					}
					case JAIL:
					{
						punishLevel = state;
						// Open a Html message to inform the player
						final NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
						final String jailInfos = HtmCache.getInstance().getHtm("data/html/jail_out.htm");
						if (jailInfos != null)
						{
							htmlMsg.setHtml(jailInfos);
						}
						else
						{
							htmlMsg.setHtml("<html><body>You are free for now, respect server rules!</body></html>");
						}
						sendPacket(htmlMsg);
						stopPunishTask(true);
						teleToLocation(17836, 170178, -3507, true); // Floran
						break;
					}
				}
				break;
			}
			case CHAT: // Chat Ban
			{
				// not allow player to escape jail using chat ban
				if (punishLevel == PunishLevel.JAIL)
				{
					break;
				}
				punishLevel = state;
				punishTimer = 0;
				sendPacket(new EtcStatusUpdate(this));
				// Remove the task if any
				stopPunishTask(false);
				
				if (delayInMilliseconds > 0)
				{
					punishTimer = delayInMilliseconds;
					
					// start the countdown
					final int minutes = (int) (delayInMilliseconds / 60000);
					punishTask = ThreadPoolManager.getInstance().scheduleGeneral(new PunishTask(this), punishTimer);
					sendMessage("You are chat banned for " + minutes + " minutes.");
				}
				else
				{
					sendMessage("You have been chat banned");
				}
				break;
				
			}
			case JAIL: // Jail Player
			{
				punishLevel = state;
				punishTimer = 0;
				// Remove the task if any
				stopPunishTask(false);
				
				if (delayInMilliseconds > 0)
				{
					punishTimer = delayInMilliseconds; // Delay in milliseconds
					
					// start the countdown
					punishTask = ThreadPoolManager.getInstance().scheduleGeneral(new PunishTask(this), punishTimer);
					sendMessage("You are in jail for " + delayInMilliseconds / 60000 + " minutes.");
				}
				
				if (inEventCTF)
				{
					CTF.onDisconnect(this);
				}
				else if (inEventDM)
				{
					DM.onDisconnect(this);
				}
				else if (inEventTvT)
				{
					TvT.onDisconnect(this);
				}
				if (isInOlympiadMode())
				{
					Olympiad.getInstance().removeDisconnectedCompetitor(this);
				}
				
				// Open a Html message to inform the player
				final NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
				final String jailInfos = HtmCache.getInstance().getHtm("data/html/jail_in.htm");
				if (jailInfos != null)
				{
					htmlMsg.setHtml(jailInfos);
				}
				else
				{
					htmlMsg.setHtml("<html><body>You have been put in jail by an admin.</body></html>");
				}
				sendPacket(htmlMsg);
				setInstanceId(0);
				setIsIn7sDungeon(false);
				
				teleToLocation(-114356, -249645, -2984, false); // Jail
				break;
			}
			case CHAR: // Ban Character
			{
				setAccessLevel(-100);
				logout();
				break;
			}
			case ACC: // Ban Account
			{
				setAccountAccesslevel(-100);
				logout();
				break;
			}
			default:
			{
				punishLevel = state;
				break;
			}
		}
		
		// store in database
		storeCharBase();
	}
	
	/**
	 * Gets the punish timer.
	 * @return the punish timer
	 */
	public long getPunishTimer()
	{
		return punishTimer;
	}
	
	/**
	 * Sets the punish timer.
	 * @param time the new punish timer
	 */
	public void setPunishTimer(final long time)
	{
		punishTimer = time;
	}
	
	/**
	 * Update punish state.
	 */
	private void updatePunishState()
	{
		if (getPunishLevel() != PunishLevel.NONE)
		{
			// If punish timer exists, restart punishtask.
			if (punishTimer > 0)
			{
				punishTask = ThreadPoolManager.getInstance().scheduleGeneral(new PunishTask(this), punishTimer);
				sendMessage("You are still " + getPunishLevel().string() + " for " + punishTimer / 60000 + " minutes.");
			}
			if (getPunishLevel() == PunishLevel.JAIL)
			{
				// If player escaped, put him back in jail
				if (!isInsideZone(ZONE_JAIL))
				{
					teleToLocation(-114356, -249645, -2984, true);
				}
			}
		}
	}
	
	/**
	 * Stop punish task.
	 * @param save the save
	 */
	public void stopPunishTask(final boolean save)
	{
		if (punishTask != null)
		{
			if (save)
			{
				long delay = punishTask.getDelay(TimeUnit.MILLISECONDS);
				if (delay < 0)
				{
					delay = 0;
				}
				setPunishTimer(delay);
			}
			punishTask.cancel(false);
			ThreadPoolManager.getInstance().removeGeneral((Runnable) punishTask);
			punishTask = null;
		}
	}
	
	/**
	 * The Class PunishTask.
	 */
	private class PunishTask implements Runnable
	{
		
		/** The player. */
		L2PcInstance player;
		
		// protected long startedAt;
		
		/**
		 * Instantiates a new punish task.
		 * @param player the player
		 */
		protected PunishTask(final L2PcInstance player)
		{
			this.player = player;
			// startedAt = System.currentTimeMillis();
		}
		
		@Override
		public void run()
		{
			player.setPunishLevel(PunishLevel.NONE, 0);
		}
	}
	
	private final HashMap<Integer, Long> confirmDlgRequests = new HashMap<>();
	
	public void addConfirmDlgRequestTime(final int requestId, final int time)
	
	{
		confirmDlgRequests.put(requestId, System.currentTimeMillis() + time + 2000);
	}
	
	public Long getConfirmDlgRequestTime(final int requestId)
	{
		return confirmDlgRequests.get(requestId);
	}
	
	public void removeConfirmDlgRequestTime(final int requestId)
	{
		confirmDlgRequests.remove(requestId);
	}
	
	/**
	 * Gets the flood protectors.
	 * @return the flood protectors
	 */
	public FloodProtectors getFloodProtectors()
	{
		return getClient().getFloodProtectors();
	}
	
	/**
	 * Test if player inventory is under 80% capaity.
	 * @return true, if is inventory under80
	 */
	public boolean isInventoryUnder80()
	{
		if (getInventory().getSize() <= getInventoryLimit() * 0.8)
		{
			return true;
		}
		return false;
	}
	
	// Multisell
	/** The current multi sell id. */
	private int currentMultiSellId = -1;
	
	/**
	 * Gets the multi sell id.
	 * @return the multi sell id
	 */
	public final int getMultiSellId()
	{
		return currentMultiSellId;
	}
	
	/**
	 * Sets the multi sell id.
	 * @param listid the new multi sell id
	 */
	public final void setMultiSellId(final int listid)
	{
		currentMultiSellId = listid;
	}
	
	/**
	 * Checks if is party waiting.
	 * @return true, if is party waiting
	 */
	public boolean isPartyWaiting()
	{
		return PartyMatchWaitingList.getInstance().getPlayers().contains(this);
	}
	
	// these values are only stored temporarily
	/** The partyroom. */
	private int partyroom = 0;
	
	/**
	 * Sets the party room.
	 * @param id the new party room
	 */
	public void setPartyRoom(final int id)
	{
		partyroom = id;
	}
	
	/**
	 * Gets the party room.
	 * @return the party room
	 */
	public int getPartyRoom()
	{
		return partyroom;
	}
	
	/**
	 * Checks if is in party match room.
	 * @return true, if is in party match room
	 */
	public boolean isInPartyMatchRoom()
	{
		return partyroom > 0;
	}
	
	/**
	 * Checks if is item equipped by item id.
	 * @param  item_id the item_id
	 * @return         true, if is item equipped by item id
	 */
	public boolean isItemEquippedByItemId(final int item_id)
	{
		if (inventory == null)
		{
			return false;
		}
		
		if (inventory.getAllItemsByItemId(item_id) == null || inventory.getAllItemsByItemId(item_id).length == 0)
		{
			return false;
		}
		
		return inventory.checkIfEquipped(item_id);
	}
	
	/**
	 * Gets the instance login time.
	 * @return the instanceLoginTime
	 */
	public long get_instanceLoginTime()
	{
		return instanceLoginTime;
	}
	
	/**
	 * Sets the sex db.
	 * @param player the player
	 * @param mode   the mode
	 */
	public static void setSexDB(final L2PcInstance player, final int mode)
	{
		if (player == null)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER_SEX))
		{
			statement.setInt(1, player.getAppearance().getSex() ? 1 : 0);
			statement.setInt(2, player.getObjectId());
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			LOGGER.error("L2PcInstance.setSex : Could not store sex in database", e);
		}
	}
	
	public boolean checkTeleportOverTime()
	{
		
		if (!isTeleporting())
		{
			return false;
		}
		
		if (System.currentTimeMillis() - lastTeleportAction > Config.CHECK_TELEPORT_ZOMBIE_DELAY_TIME)
		{
			
			LOGGER.warn("Player " + getName() + " has been in teleport more then " + Config.CHECK_TELEPORT_ZOMBIE_DELAY_TIME / 1000 + " seconds.. --> Kicking it");
			
			return true;
			
		}
		
		return false;
		
	}
	
	@Override
	public void setIsTeleporting(final boolean value)
	{
		super.setIsTeleporting(value);
		if (value)
		{
			lastTeleportAction = System.currentTimeMillis();
		}
		
	}
	
	@Override
	public L2PcInstance getActingPlayer()
	{
		return this;
	}
	
	public void sendBlockList()
	{
		sendMessage("======<Ignore List>======");
		
		int i = 1;
		final Iterator<String> blockListIt = getBlockList().getBlockList().iterator();
		while (blockListIt.hasNext())
		{
			final String playerId = blockListIt.next();
			sendMessage(new StringBuilder().append(i++).append(". ").append(playerId).toString());
			
		}
		
		sendMessage("========================");
		
	}
	
	public long getLastAttackPacket()
	{
		return lastAttackPacket;
	}
	
	public void setLastAttackPacket()
	{
		lastAttackPacket = System.currentTimeMillis();
	}
	
	public void checkItemRestriction()
	{
		for (int i = 0; i < Inventory.PAPERDOLL_TOTALSLOTS; i++)
		{
			final L2ItemInstance equippedItem = getInventory().getPaperdollItem(i);
			if (equippedItem != null && !equippedItem.checkOlympCondition())
			{
				if (equippedItem.isAugmented())
				{
					equippedItem.getAugmentation().removeBoni(this);
				}
				final L2ItemInstance[] items = getInventory().unEquipItemInSlotAndRecord(i);
				if (equippedItem.isWear())
				{
					continue;
				}
				SystemMessage sm = null;
				if (equippedItem.getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(equippedItem.getEnchantLevel());
					sm.addItemName(equippedItem.getItemId());
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(equippedItem.getItemId());
				}
				sendPacket(sm);
				final InventoryUpdate iu = new InventoryUpdate();
				iu.addItems(Arrays.asList(items));
				sendPacket(iu);
				broadcastUserInfo();
			}
		}
	}
	
	public void enterOlympiadObserverMode(final int x, final int y, final int z, final int id, final boolean storeCoords)
	{
		if (isInOlympiadMode())
		{
			sendPacket(new SystemMessage(SystemMessageId.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME));
			return;
		}
		
		if (getPet() != null)
		{
			getPet().unSummon(this);
		}
		
		unsummonAllCubics();
		
		olympiadGameId = id;
		if (isSitting())
		{
			standUp();
		}
		if (storeCoords)
		{
			obsX = getX();
			obsY = getY();
			obsZ = getZ();
		}
		setTarget(null);
		setIsInvul(true);
		getAppearance().setInvisible();
		// sendPacket(new GMHide(1));
		teleToLocation(x, y, z, true);
		sendPacket(new ExOlympiadMode(3, this));
		observerMode = true;
		broadcastUserInfo();
	}
	
	public void leaveOlympiadObserverMode(final boolean olymp)
	{
		setTarget(null);
		sendPacket(new ExOlympiadMode(0, this));
		teleToLocation(obsX, obsY, obsZ, true);
		if (!AdminCommandAccessRights.getInstance().hasAccess("admin_invis", getAccessLevel()))
		{
			getAppearance().setVisible();
		}
		if (!AdminCommandAccessRights.getInstance().hasAccess("admin_invul", getAccessLevel()))
		{
			setIsInvul(false);
		}
		if (getAI() != null)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		if (!olymp)
		{
			Olympiad.removeSpectator(olympiadGameId, this);
		}
		olympiadGameId = -1;
		observerMode = false;
		broadcastUserInfo();
	}
	
	public void setHero(boolean hero)
	{
		isHeroPlayer = hero;
		
		if (isHeroPlayer && baseClass == activeClass)
		{
			giveHeroSkills();
		}
		else if (getHeroCount() >= Config.HERO_COUNT && isHeroPlayer && Config.ALLOW_HERO_SUBSKILL)
		{
			giveHeroSkills();
		}
		else
		{
			removeHeroSkills();
		}
	}
	
	/**
	 * @param endDate value in miliseconds<br>
	 *                    Value 0 = hero for ever<br>
	 */
	public void setHeroEndDate(long endDate)
	{
		heroEndDate = endDate;
	}
	
	public long getHeroEndDate()
	{
		return heroEndDate;
	}
	
	public void giveHeroSkills()
	{
		for (L2Skill s : HeroSkillTable.getHeroSkills())
		{
			addSkill(s, false); // Dont Save Hero skills to database
		}
		
		sendSkillList();
	}
	
	public void removeHeroSkills()
	{
		for (L2Skill s : HeroSkillTable.getHeroSkills())
		{
			super.removeSkill(s); // Just Remove skills from nonHero characters
		}
		
		sendSkillList();
	}
	
	/**
	 * Get the current pet skill in use or return null.<br>
	 * <br>
	 * @return
	 */
	public SkillDat getCurrentPetSkill()
	{
		return currentPetSkill;
	}
	
	/**
	 * Create a new SkillDat object and set the player currentPetSkill.<br>
	 * <br>
	 * @param currentSkill
	 * @param ctrlPressed
	 * @param shiftPressed
	 */
	public void setCurrentPetSkill(final L2Skill currentSkill, final boolean ctrlPressed, final boolean shiftPressed)
	{
		if (currentSkill == null)
		{
			if (Config.DEBUG)
			{
				LOGGER.info("Setting current pet skill: NULL for " + getName() + ".");
			}
			
			currentPetSkill = null;
			return;
		}
		
		if (Config.DEBUG)
		{
			LOGGER.info("Setting current Pet skill: " + currentSkill.getName() + " (ID: " + currentSkill.getId() + ") for " + getName() + ".");
		}
		
		currentPetSkill = new SkillDat(currentSkill, ctrlPressed, shiftPressed);
	}
	
	public void setNameColor(int nameColor)
	{
		getAppearance().setNameColor(nameColor);
	}
	
	public void setTitleColor(int titleColor)
	{
		getAppearance().setTitleColor(titleColor);
	}
	
	public String getIpAddress()
	{
		try
		{
			L2GameClient client = getClient();
			MMOConnection<L2GameClient> conection = client.getConnection();
			InetAddress ipAddress = conection.getInetAddress();
			return ipAddress.getHostAddress();
		}
		catch (Exception e)
		{
			LOGGER.warn("Something went wrong while getting IP ADDRESS from player " + getName(), e);
			return "0.0.0.0";
		}
	}
	
	public void kick()
	{
		logout(true);
		RegionBBSManager.getInstance().changeCommunityBoard();
	}
	
	/**
	 * @param  variable
	 * @param  defaultValue In case that <b>variable</b> does not exists, you need to indicate a default value to be return
	 * @return              the value of the variable mapped
	 */
	public String getVariable(String variable, String defaultValue)
	{
		return variables.getOrDefault(variable, defaultValue);
	}
	
	/**
	 * @param  variable
	 * @param  defaultValue In case that <b>variable</b> does not exists, you need to indicate a default value to be return
	 * @return              the value of the variable mapped
	 */
	public int getVariableInt(String variable, int defaultValue)
	{
		if (variables.get(variable) == null)
		{
			return defaultValue;
		}
		
		return Integer.parseInt(variables.get(variable));
	}
	
	/**
	 * @param  variable
	 * @param  defaultValue In case that <b>variable</b> does not exists, you need to indicate a default value to be return
	 * @return              the value of the variable mapped
	 */
	public long getVariableLong(String variable, long defaultValue)
	{
		if (variables.get(variable) == null)
		{
			return defaultValue;
		}
		
		return Long.parseLong(variables.get(variable));
	}
	
	/**
	 * @param  variable
	 * @param  defaultValue In case that <b>variable</b> does not exists, you need to indicate a default value to be return
	 * @return              the value of the variable mapped
	 */
	public boolean getVariableBoolean(String variable, boolean defaultValue)
	{
		if (variables.get(variable) == null)
		{
			return defaultValue;
		}
		
		return Boolean.parseBoolean(variables.get(variable));
	}
	
	public void setVariable(String variable, int value, boolean saveInDB)
	{
		setVariable(variable, String.valueOf(value), saveInDB);
	}
	
	public void setVariable(String variable, long value, boolean saveInDB)
	{
		setVariable(variable, String.valueOf(value), saveInDB);
	}
	
	public void setVariable(String variable, boolean value, boolean saveInDB)
	{
		setVariable(variable, String.valueOf(value), saveInDB);
	}
	
	/**
	 * @param variable
	 * @param value
	 * @param saveInDB (Optional) If you <b>dont want</b> to save the variable and value in the database put <b>false</b><br>
	 *                     When you set a variable with the same name, the old value will be replaced. <br>
	 *                     If you replace the variable with the new value <b>and do not save in the data base</b>, the old variable will be read from database when player log in game.
	 */
	public void setVariable(String variable, String value, boolean saveInDB)
	{
		variables.put(variable, value);
		
		if (saveInDB)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement pstDelete = con.prepareStatement(DELETE_CHARACTER_VARIABLE);
				PreparedStatement pst = con.prepareStatement(INSERT_CHARACTER_VARIABLE))
			{
				pstDelete.setInt(1, getObjectId());
				pstDelete.setString(2, variable);
				pstDelete.executeUpdate();
				
				pst.setInt(1, getObjectId());
				pst.setString(2, variable.trim());
				pst.setString(3, value);
				pst.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("L2PcInstance.setVariable : Problem when tried to save variable into database for player " + getName() + "(" + getObjectId() + ")", e);
			}
		}
	}
	
	/**
	 * @param variable
	 * @param removeInDB (Optional) If you want to keep the variable and value in the database put <b>false</b>
	 */
	public void removeVariable(String variable, boolean removeInDB)
	{
		variables.remove(variable);
		
		if (removeInDB)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement pst = con.prepareStatement(DELETE_CHARACTER_VARIABLE))
			{
				pst.setInt(1, getObjectId());
				pst.setString(2, variable);
				pst.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("L2PcInstance.removeVariable : Problem when tried to remove variable from database for player " + getName() + "(" + getObjectId() + ")", e);
			}
		}
	}
	
	/**
	 * Read the character variables from database
	 */
	public void loadVariables()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement pst = con.prepareStatement(SELECT_CHARACTER_VARIABLES))
		{
			pst.setInt(1, getObjectId());
			
			try (ResultSet rset = pst.executeQuery())
			{
				while (rset.next())
				{
					variables.put(rset.getString("variable"), rset.getString("value"));
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("L2PcInstance.loadVariables : Problem when tried to get variables for player " + getName() + "(" + getObjectId() + ")", e);
		}
	}
	
	/**
	 * @param  variable
	 * @param  defaultValue In case that <b>variable</b> does not exists, you need to indicate a default value to be return
	 * @return              the value of the variable mapped
	 */
	public String getAccountVariable(String variable, String defaultValue)
	{
		return accountVariables.getOrDefault(variable, defaultValue);
	}
	
	/**
	 * @param  variable
	 * @param  defaultValue In case that <b>variable</b> does not exists, you need to indicate a default value to be return
	 * @return              the value of the variable mapped
	 */
	public int getAccountVariableInt(String variable, int defaultValue)
	{
		if (accountVariables.get(variable) == null)
		{
			return defaultValue;
		}
		
		return Integer.parseInt(accountVariables.get(variable));
	}
	
	/**
	 * @param  variable
	 * @param  defaultValue In case that <b>variable</b> does not exists, you need to indicate a default value to be return
	 * @return              the value of the variable mapped
	 */
	public long getAccountVariableLong(String variable, long defaultValue)
	{
		if (accountVariables.get(variable) == null)
		{
			return defaultValue;
		}
		
		return Long.parseLong(accountVariables.get(variable));
	}
	
	/**
	 * @param  variable
	 * @param  defaultValue In case that <b>variable</b> does not exists, you need to indicate a default value to be return
	 * @return              the value of the variable mapped
	 */
	public boolean getAccountVariableBoolean(String variable, boolean defaultValue)
	{
		if (accountVariables.get(variable) == null)
		{
			return defaultValue;
		}
		
		return Boolean.parseBoolean(accountVariables.get(variable));
	}
	
	public void setAccountVariable(String variable, int value, boolean saveInDB)
	{
		setAccountVariable(variable, String.valueOf(value), saveInDB);
	}
	
	public void setAccountVariable(String variable, long value, boolean saveInDB)
	{
		setAccountVariable(variable, String.valueOf(value), saveInDB);
	}
	
	public void setAccountVariable(String variable, boolean value, boolean saveInDB)
	{
		setAccountVariable(variable, String.valueOf(value), saveInDB);
	}
	
	/**
	 * @param variable
	 * @param value
	 * @param saveInDB (Optional) If you <b>dont want</b> to save the variable and value in the database put <b>false</b><br>
	 *                     When you set a variable with the same name, the old value will be replaced. <br>
	 *                     If you replace the variable with the new value <b>and do not save in the data base</b>, the old variable will be read from database when player log in game.
	 */
	public void setAccountVariable(String variable, String value, boolean saveInDB)
	{
		accountVariables.put(variable, value);
		
		if (saveInDB)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement pstDelete = con.prepareStatement(DELETE_ACCOUNT_VARIABLE);
				PreparedStatement pstInsert = con.prepareStatement(INSERT_ACCOUNT_VARIABLE))
			{
				pstDelete.setString(1, getAccountName());
				pstDelete.setString(2, variable);
				pstDelete.executeUpdate();
				
				pstInsert.setString(1, getAccountName());
				pstInsert.setString(2, variable.trim());
				pstInsert.setString(3, value);
				pstInsert.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("L2PcInstance.setAccountVariable : Problem when tried to save account variable into database for player " + getName() + "(" + getObjectId() + "), account name " + getAccountName(), e);
			}
		}
	}
	
	/**
	 * @param variable
	 * @param removeInDB (Optional) If you want to keep the variable and value in the database put <b>false</b>
	 */
	public void removeAccountVariable(String variable, boolean removeInDB)
	{
		accountVariables.remove(variable);
		
		if (removeInDB)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement pst = con.prepareStatement(DELETE_ACCOUNT_VARIABLE))
			{
				pst.setString(1, getAccountName());
				pst.setString(2, variable);
				pst.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("L2PcInstance.removeAccountVariable : Problem when tried to remove variable from database for player " + getName() + "(" + getObjectId() + "), account name " + getAccountName(), e);
			}
		}
	}
	
	/**
	 * Read account variables from database
	 */
	public void loadAccountVariables()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement pst = con.prepareStatement(SELECT_ACCOUNT_VARIABLES))
		{
			pst.setString(1, getAccountName());
			
			try (ResultSet rset = pst.executeQuery())
			{
				while (rset.next())
				{
					accountVariables.put(rset.getString("variable"), rset.getString("value"));
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("L2PcInstance.loadAccountVariables : Problem when tried to get variables for player " + getName() + "(" + getObjectId() + ")", e);
		}
	}
}