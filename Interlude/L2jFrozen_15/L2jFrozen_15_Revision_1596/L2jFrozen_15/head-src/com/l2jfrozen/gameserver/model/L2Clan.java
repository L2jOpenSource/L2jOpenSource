package com.l2jfrozen.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.communitybbs.BB.Forum;
import com.l2jfrozen.gameserver.communitybbs.Manager.ForumsBBSManager;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.CrownManager;
import com.l2jfrozen.gameserver.managers.SiegeManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeReceiveSubPledgeCreated;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListAll;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListDeleteAll;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeSkillListAdd;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.UserInfo;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @version $Revision: 1.7.3 $ $Date: 2009/04/29 00:07:09 $
 * @author  programmos
 */
public class L2Clan
{
	private static final Logger LOGGER = Logger.getLogger(L2Clan.class);
	private static final String INSERT_CLAN_DATA = "INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id) VALUES (?,?,?,?,?,?,?,?,?,?)";
	private static final String UPDATE_CLAN_DATA = "UPDATE clan_data SET leader_id=?,ally_id=?,ally_name=?,reputation_score=?,ally_penalty_expiry_time=?,ally_penalty_type=?,char_penalty_expiry_time=?,dissolving_expiry_time=? WHERE clan_id=?";
	
	private static final String UPDATE_CHARACTER_WITHOT_CLAN = "UPDATE characters SET clanid=0, title=?, clan_join_expiry_time=?, clan_create_expiry_time=?, clan_privs=0, wantspeace=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0 WHERE obj_Id=?";
	private static final String UPDATE_CHARACTER_APPRENTICE = "UPDATE characters SET apprentice=0 WHERE apprentice=?";
	private static final String UPDATE_CHARACTER_SPONSOR = "UPDATE characters SET sponsor=0 WHERE sponsor=?";
	
	private static final String SELECT_CLAN_NOTICE = "SELECT enabled,notice FROM clan_notices WHERE clan_id=?";
	
	private static final String UPDATE_CLAN_LEVEL = "UPDATE clan_data SET clan_level=? WHERE clan_id=?";
	private static final String UPDATE_ALLY_CREST_ID = "UPDATE clan_data SET ally_crest_id=? WHERE clan_id=?";
	
	private static final String INSERT_CLAN_NOTICE = "INSERT INTO clan_notices (clan_id,notice,enabled) VALUES (?,?,?) ON DUPLICATE KEY UPDATE notice=?,enabled=?";
	
	private static final String SELECT_CLAN_SKILLS_BY_CLAN_ID = "SELECT skill_id,skill_level FROM clan_skills WHERE clan_id=?";
	
	private static final String SELECT_CLAN_SUBPLEDGES_BY_CLAN_ID = "SELECT sub_pledge_id,name,leader_name FROM clan_subpledges WHERE clan_id=?";
	private static final String INSERT_CLAN_SUBPLEDGE = "INSERT INTO clan_subpledges (clan_id,sub_pledge_id,name,leader_name) VALUES (?,?,?,?)";
	private static final String UPDATE_CLAN_SUBPLEDGE = "UPDATE clan_subpledges SET leader_name=?, name=? WHERE clan_id=? AND sub_pledge_id=?";
	
	private static final String SELECT_CLAN_PRIVIS_BY_CLAN_ID = "SELECT privs,'rank',party FROM clan_privs WHERE clan_id=?";
	private static final String INSERT_CLAN_PRIVIS_ON_DUPLICATE_KEY = "INSERT INTO clan_privs (clan_id,'rank',party,privs) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE privs= ?";
	private static final String INSERT_CLAN_PRIVIS = "INSERT INTO clan_privs (clan_id,'rank',party,privs) VALUES (?,?,?,?)";
	
	private static final String UPDATE_CLAN_SKILL = "UPDATE clan_skills SET skill_level=? WHERE skill_id=? AND clan_id=?";
	private static final String INSERT_CLAN_SKILL = "INSERT INTO clan_skills (clan_id,skill_id,skill_level,skill_name) VALUES (?,?,?,?)";
	private static final String UPDATE_DUPLICATE_CLAN_SKILL = "UPDATE clan_skills SET skill_level=? WHERE skill_id=? AND clan_id=?";
	
	private static final String UPDATE_CLAN_ACUTION_BID_BY_CLAN_ID = "UPDATE clan_data SET auction_bid_at=? WHERE clan_id=?";
	
	private static final String SELECT_CLAN_DATA_BY_CLAN_ID = "SELECT clan_name,clan_level,hasCastle,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id,reputation_score,auction_bid_at,ally_penalty_expiry_time,ally_penalty_type,char_penalty_expiry_time,dissolving_expiry_time FROM clan_data WHERE clan_id=?";
	private static final String SELECT_CHAR_DATA_BY_CLAN_ID = "SELECT char_name,level,classid,obj_Id,title,power_grade,subpledge,apprentice,sponsor FROM characters WHERE clanid=?";
	
	private String name;
	private int clanId;
	private L2ClanMember leader;
	private final Map<String, L2ClanMember> members = new HashMap<>();
	
	private String allyName;
	private int allyId = 0;
	private int clanLevel;
	private int castleId = 0;
	private int hasFort;
	private int hasHideout;
	private boolean hasCrest;
	private int hiredGuards;
	private int crestId;
	private int crestLargeId;
	private int allyCrestId;
	private int auctionBiddedAt = 0;
	private long allyPenaltyExpiryTime;
	private int allyPenaltyType;
	private long charPenaltyExpiryTime;
	private long dissolvingExpiryTime;
	// Ally Penalty Types
	/** Clan leaved ally */
	public static final int PENALTY_TYPE_CLAN_LEAVED = 1;
	/** Clan was dismissed from ally */
	public static final int PENALTY_TYPE_CLAN_DISMISSED = 2;
	/** Leader clan dismiss clan from ally */
	public static final int PENALTY_TYPE_DISMISS_CLAN = 3;
	/** Leader clan dissolve ally */
	public static final int PENALTY_TYPE_DISSOLVE_ALLY = 4;
	
	private final ItemContainer warehouse = new ClanWarehouse(this);
	private final List<Integer> atWarWith = new ArrayList<>();
	private final List<Integer> atWarAttackers = new ArrayList<>();
	
	private boolean hasCrestLarge;
	
	private Forum forum;
	
	private final List<L2Skill> skillList = new ArrayList<>();
	
	// Clan Notice
	private String notice;
	private boolean noticeEnabled = false;
	private static final int MAX_NOTICE_LENGTH = 512;
	
	// Clan Privileges
	/** No privilege to manage any clan activity */
	public static final int CP_NOTHING = 0;
	/** Privilege to join clan */
	public static final int CP_CL_JOIN_CLAN = 2;
	/** Privilege to give a title */
	public static final int CP_CL_GIVE_TITLE = 4;
	/** Privilege to view warehouse content */
	public static final int CP_CL_VIEW_WAREHOUSE = 8;
	/** Privilege to manage clan ranks */
	public static final int CP_CL_MANAGE_RANKS = 16;
	public static final int CP_CL_PLEDGE_WAR = 32;
	public static final int CP_CL_DISMISS = 64;
	/** Privilege to register clan crest */
	public static final int CP_CL_REGISTER_CREST = 128;
	public static final int CP_CL_MASTER_RIGHTS = 256;
	public static final int CP_CL_MANAGE_LEVELS = 512;
	/** Privilege to open a door */
	public static final int CP_CH_OPEN_DOOR = 1024;
	public static final int CP_CH_OTHER_RIGHTS = 2048;
	public static final int CP_CH_AUCTION = 4096;
	public static final int CP_CH_DISMISS = 8192;
	public static final int CP_CH_SET_FUNCTIONS = 16384;
	public static final int CP_CS_OPEN_DOOR = 32768;
	public static final int CP_CS_MANOR_ADMIN = 65536;
	public static final int CP_CS_MANAGE_SIEGE = 131072;
	public static final int CP_CS_USE_FUNCTIONS = 262144;
	public static final int CP_CS_DISMISS = 524288;
	public static final int CP_CS_TAXES = 1048576;
	public static final int CP_CS_MERCENARIES = 2097152;
	public static final int CP_CS_SET_FUNCTIONS = 4194304;
	/** Privilege to manage all clan activity */
	public static final int CP_ALL = 8388606;
	
	// Sub-unit types
	/** Clan subunit type of Academy */
	public static final int SUBUNIT_ACADEMY = -1;
	/** Clan subunit type of Royal Guard A */
	public static final int SUBUNIT_ROYAL1 = 100;
	/** Clan subunit type of Royal Guard B */
	public static final int SUBUNIT_ROYAL2 = 200;
	/** Clan subunit type of Order of Knights A-1 */
	public static final int SUBUNIT_KNIGHT1 = 1001;
	/** Clan subunit type of Order of Knights A-2 */
	public static final int SUBUNIT_KNIGHT2 = 1002;
	/** Clan subunit type of Order of Knights B-1 */
	public static final int SUBUNIT_KNIGHT3 = 2001;
	/** Clan subunit type of Order of Knights B-2 */
	public static final int SUBUNIT_KNIGHT4 = 2002;
	
	/** HashMap(Integer, L2Skill) containing all skills of the L2Clan */
	protected final Map<Integer, L2Skill> skills = new HashMap<>();
	protected final Map<Integer, RankPrivs> clanPrivs = new HashMap<>();
	protected final Map<Integer, SubPledge> subPledges = new HashMap<>();
	
	private int reputationScore = 0;
	private int rank = 0;
	
	/**
	 * Called if a clan is referenced only by id. In this case all other data needs to be fetched from db
	 * @param clanId A valid clan Id to create and restore
	 */
	public L2Clan(final int clanId)
	{
		this.clanId = clanId;
		initializePrivs();
		
		try
		{
			restore();
			getWarehouse().restore();
		}
		catch (final Exception e)
		{
			LOGGER.error("Error restoring clan \n\t" + this, e);
		}
	}
	
	/**
	 * Called only if a new clan is created
	 * @param clanId   A valid clan Id to create
	 * @param clanName A valid clan name
	 */
	public L2Clan(final int clanId, final String clanName)
	{
		this.clanId = clanId;
		name = clanName;
		initializePrivs();
	}
	
	public int getClanId()
	{
		return clanId;
	}
	
	public void setClanId(final int clanId)
	{
		this.clanId = clanId;
	}
	
	/**
	 * @return Returns the leaderId.
	 */
	public int getLeaderId()
	{
		return leader != null ? leader.getObjectId() : 0;
	}
	
	public L2ClanMember getLeader()
	{
		return leader;
	}
	
	public boolean setLeader(final L2ClanMember member)
	{
		if (member == null)
		{
			return false;
		}
		
		final L2ClanMember old_leader = leader;
		leader = member;
		members.put(member.getName(), member);
		
		// refresh oldleader and new leader info
		if (old_leader != null)
		{
			
			final L2PcInstance exLeader = old_leader.getPlayerInstance();
			exLeader.setClan(this);
			exLeader.setPledgeClass(exLeader.getClan().getClanMember(exLeader.getObjectId()).calculatePledgeClass(exLeader));
			exLeader.setClanPrivileges(L2Clan.CP_NOTHING);
			
			exLeader.broadcastUserInfo();
			
			CrownManager.getInstance().checkCrowns(exLeader);
			
		}
		
		updateClanInDB();
		
		if (member.getPlayerInstance() != null)
		{
			
			final L2PcInstance newLeader = member.getPlayerInstance();
			newLeader.setClan(this);
			newLeader.setPledgeClass(member.calculatePledgeClass(newLeader));
			newLeader.setClanPrivileges(L2Clan.CP_ALL);
			
			newLeader.broadcastUserInfo();
		}
		
		broadcastClanStatus();
		
		CrownManager.getInstance().checkCrowns(member.getPlayerInstance());
		
		return true;
	}
	
	// public void setNewLeader(L2ClanMember member)
	public void setNewLeader(final L2ClanMember member, final L2PcInstance activeChar)
	{
		if (activeChar.isRiding() || activeChar.isFlying())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!getLeader().isOnline())
		{
			return;
		}
		
		if (member == null)
		{
			return;
		}
		
		if (!member.isOnline())
		{
			return;
		}
		
		// L2PcInstance exLeader = getLeader().getPlayerInstance();
		if (setLeader(member))
		{
			
			SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_LEADER_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_S1);
			sm.addString(member.getName());
			broadcastToOnlineMembers(sm);
			sm = null;
			
		}
		
		// SiegeManager.getInstance().removeSiegeSkills(exLeader);
		
		/*
		 * if(getLevel() >= 4) { SiegeManager.getInstance().addSiegeSkills(newLeader); }
		 */
		
	}
	
	public String getLeaderName()
	{
		return leader != null ? leader.getName() : "";
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(final String name)
	{
		this.name = name;
	}
	
	private void addClanMember(final L2ClanMember member)
	{
		members.put(member.getName(), member);
	}
	
	public void addClanMember(final L2PcInstance player)
	{
		L2ClanMember member = new L2ClanMember(this, player.getName(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), player.getPledgeType(), player.getPowerGrade(), player.getTitle());
		
		// store in memory
		addClanMember(member);
		member.setPlayerInstance(player);
		player.setClan(this);
		player.setPledgeClass(member.calculatePledgeClass(player));
		player.sendPacket(new PledgeShowMemberListUpdate(player));
		player.sendPacket(new UserInfo(player));
		player.rewardSkills();
		
		member = null;
	}
	
	public void updateClanMember(final L2PcInstance player)
	{
		L2ClanMember member = new L2ClanMember(player);
		addClanMember(member);
		
		member = null;
	}
	
	public L2ClanMember getClanMember(final String name)
	{
		return members.get(name);
	}
	
	public L2ClanMember getClanMember(final int objectID)
	{
		for (final L2ClanMember temp : members.values())
		{
			if (temp.getObjectId() == objectID)
			{
				return temp;
			}
		}
		
		return null;
	}
	
	public void removeClanMember(final String name, final long clanJoinExpiryTime)
	{
		L2ClanMember exMember = members.remove(name);
		
		if (exMember == null)
		{
			LOGGER.warn("Member " + name + " not found in clan while trying to remove");
			return;
		}
		
		final int leadssubpledge = getLeaderSubPledge(name);
		
		if (leadssubpledge != 0)
		{
			// Sub-unit leader withdraws, position becomes vacant and leader
			// should appoint new via NPC
			getSubPledge(leadssubpledge).setLeaderName("");
			updateSubPledgeInDB(leadssubpledge);
		}
		
		if (exMember.getApprentice() != 0)
		{
			L2ClanMember apprentice = getClanMember(exMember.getApprentice());
			
			if (apprentice != null)
			{
				if (apprentice.getPlayerInstance() != null)
				{
					apprentice.getPlayerInstance().setSponsor(0);
				}
				else
				{
					apprentice.initApprenticeAndSponsor(0, 0);
				}
				
				apprentice.saveApprenticeAndSponsor(0, 0);
			}
			
			apprentice = null;
		}
		
		if (exMember.getSponsor() != 0)
		{
			L2ClanMember sponsor = getClanMember(exMember.getSponsor());
			
			if (sponsor != null)
			{
				if (sponsor.getPlayerInstance() != null)
				{
					sponsor.getPlayerInstance().setApprentice(0);
				}
				else
				{
					sponsor.initApprenticeAndSponsor(0, 0);
				}
				
				sponsor.saveApprenticeAndSponsor(0, 0);
			}
			
			sponsor = null;
		}
		
		exMember.saveApprenticeAndSponsor(0, 0);
		
		if (Config.REMOVE_CASTLE_CIRCLETS)
		{
			CastleManager.getInstance().removeCirclet(exMember, getCastleId());
		}
		
		if (exMember.isOnline())
		{
			L2PcInstance player = exMember.getPlayerInstance();
			
			player.setTitle("");
			player.setApprentice(0);
			player.setSponsor(0);
			
			if (player.isClanLeader())
			{
				SiegeManager.getInstance().removeSiegeSkills(player);
				player.setClanCreateExpiryTime(System.currentTimeMillis() + Config.ALT_CLAN_CREATE_DAYS * 86400000L); // 24*60*60*1000 = 86400000
			}
			
			// remove Clan skills from Player
			for (final L2Skill skill : player.getClan().getAllSkills())
			{
				player.removeSkill(skill, false);
			}
			
			player.setClan(null);
			player.setClanJoinExpiryTime(clanJoinExpiryTime);
			player.setPledgeClass(exMember.calculatePledgeClass(player));
			player.broadcastUserInfo();
			// disable clan tab
			player.sendPacket(new PledgeShowMemberListDeleteAll());
			
			player = null;
		}
		else
		{
			removeMemberInDatabase(exMember, clanJoinExpiryTime, getLeaderName().equalsIgnoreCase(name) ? System.currentTimeMillis() + Config.ALT_CLAN_CREATE_DAYS * 86400000L : 0);
		}
		
		exMember = null;
	}
	
	public L2ClanMember[] getMembers()
	{
		return members.values().toArray(new L2ClanMember[members.size()]);
	}
	
	public int getMembersCount()
	{
		return members.size();
	}
	
	public int getSubPledgeMembersCount(final int subpl)
	{
		int result = 0;
		
		for (final L2ClanMember temp : members.values())
		{
			if (temp.getPledgeType() == subpl)
			{
				result++;
			}
		}
		
		return result;
	}
	
	public int getMaxNrOfMembers(final int pledgetype)
	{
		int limit = 0;
		
		switch (pledgetype)
		{
			case 0:
				switch (getLevel())
				{
					case 4:
						limit = 40;
						break;
					case 3:
						limit = 30;
						break;
					case 2:
						limit = 20;
						break;
					case 1:
						limit = 15;
						break;
					case 0:
						limit = 10;
						break;
					default:
						limit = 40;
						break;
				}
				break;
			case -1:
			case 100:
			case 200:
				limit = 20;
				break;
			case 1001:
			case 1002:
			case 2001:
			case 2002:
				limit = 10;
				break;
			default:
				break;
		}
		
		return limit;
	}
	
	public L2PcInstance[] getOnlineMembers(final String exclude)
	{
		List<L2PcInstance> result = new ArrayList<>();
		
		for (L2ClanMember temp : members.values())
		{
			try
			{
				if (temp.isOnline() && !temp.getName().equals(exclude))
				{
					result.add(temp.getPlayerInstance());
				}
			}
			catch (NullPointerException e)
			{
				LOGGER.error("L2Clan.getOnlineMembers : NPE error", e);
			}
		}
		
		return result.toArray(new L2PcInstance[result.size()]);
		
	}
	
	public int getAllyId()
	{
		return allyId;
	}
	
	public String getAllyName()
	{
		return allyName;
	}
	
	public void setAllyCrestId(final int allyCrestId)
	{
		this.allyCrestId = allyCrestId;
	}
	
	public int getAllyCrestId()
	{
		return allyCrestId;
	}
	
	public int getLevel()
	{
		return clanLevel;
	}
	
	/**
	 * @return 0 --> If clan does not have a castle<br>
	 *         If the clan has a castle, return the castle ID
	 */
	public int getCastleId()
	{
		return castleId;
	}
	
	public boolean hasCastle()
	{
		return castleId > 0;
	}
	
	public int getHasFort()
	{
		return hasFort;
	}
	
	public int getHasHideout()
	{
		return hasHideout;
	}
	
	public void setCrestId(final int crestId)
	{
		this.crestId = crestId;
	}
	
	public int getCrestId()
	{
		return crestId;
	}
	
	public void setCrestLargeId(final int crestLargeId)
	{
		this.crestLargeId = crestLargeId;
	}
	
	public int getCrestLargeId()
	{
		return crestLargeId;
	}
	
	public void setAllyId(final int allyId)
	{
		this.allyId = allyId;
	}
	
	public void setAllyName(final String allyName)
	{
		this.allyName = allyName;
	}
	
	public void setHasCastle(final int hasCastle)
	{
		castleId = hasCastle;
	}
	
	public void setHasFort(final int hasFort)
	{
		this.hasFort = hasFort;
	}
	
	public void setHasHideout(final int hasHideout)
	{
		this.hasHideout = hasHideout;
	}
	
	public void setLevel(final int level)
	{
		clanLevel = level;
		
		if (forum == null)
		{
			if (clanLevel >= 2)
			{
				final ForumsBBSManager fbbsm = ForumsBBSManager.getInstance();
				final Forum clanRootForum = fbbsm.getForumByName("ClanRoot");
				if (clanRootForum != null)
				{
					forum = clanRootForum.getChildByName(name);
					if (forum == null)
					{
						forum = fbbsm.createNewForum(name, clanRootForum, Forum.CLAN, Forum.CLANMEMBERONLY, getClanId());
					}
				}
			}
		}
	}
	
	public boolean isMember(final String name)
	{
		return name == null ? false : members.containsKey(name);
	}
	
	public void updateClanInDB()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CLAN_DATA))
		{
			statement.setInt(1, getLeaderId());
			statement.setInt(2, getAllyId());
			statement.setString(3, getAllyName());
			statement.setInt(4, getReputationScore());
			statement.setLong(5, getAllyPenaltyExpiryTime());
			statement.setInt(6, getAllyPenaltyType());
			statement.setLong(7, getCharPenaltyExpiryTime());
			statement.setLong(8, getDissolvingExpiryTime());
			statement.setInt(9, getClanId());
			statement.executeUpdate();
			
			if (Config.DEBUG)
			{
				LOGGER.debug("New clan leader saved in db: " + getClanId());
			}
		}
		catch (Exception e)
		{
			LOGGER.error("L2Clan.updateClanInDB : Error while updating clan data in clan_data table", e);
		}
	}
	
	public void store()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_CLAN_DATA))
		{
			statement.setInt(1, getClanId());
			statement.setString(2, getName());
			statement.setInt(3, getLevel());
			statement.setInt(4, getCastleId());
			statement.setInt(5, getAllyId());
			statement.setString(6, getAllyName());
			statement.setInt(7, getLeaderId());
			statement.setInt(8, getCrestId());
			statement.setInt(9, getCrestLargeId());
			statement.setInt(10, getAllyCrestId());
			statement.executeUpdate();
			
			if (Config.DEBUG)
			{
				LOGGER.debug("New clan saved in db: " + getClanId());
			}
		}
		catch (Exception e)
		{
			LOGGER.error("L2Clan.store : Error while saving new clan to db", e);
		}
	}
	
	private void removeMemberInDatabase(final L2ClanMember member, final long clanJoinExpiryTime, final long clanCreateExpiryTime)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement pst = con.prepareStatement(UPDATE_CHARACTER_WITHOT_CLAN))
		{
			pst.setString(1, "");
			pst.setLong(2, clanJoinExpiryTime);
			pst.setLong(3, clanCreateExpiryTime);
			pst.setInt(4, member.getObjectId());
			pst.executeUpdate();
			
			if (Config.DEBUG)
			{
				LOGGER.debug("clan member removed in db: " + getClanId());
			}
			
			try (PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER_APPRENTICE))
			{
				statement.setInt(1, member.getObjectId());
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER_SPONSOR))
			{
				statement.setInt(1, member.getObjectId());
				statement.executeUpdate();
			}
		}
		catch (Exception e)
		{
			LOGGER.error("L2Clan.removeMemberInDatabase : Error while removing clan member in db ", e);
		}
	}
	
	private void restore()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CLAN_DATA_BY_CLAN_ID))
		{
			statement.setInt(1, getClanId());
			
			try (ResultSet clanData = statement.executeQuery())
			{
				if (clanData.next())
				{
					setName(clanData.getString("clan_name"));
					setLevel(clanData.getInt("clan_level"));
					setHasCastle(clanData.getInt("hasCastle"));
					setAllyId(clanData.getInt("ally_id"));
					setAllyName(clanData.getString("ally_name"));
					setAllyPenaltyExpiryTime(clanData.getLong("ally_penalty_expiry_time"), clanData.getInt("ally_penalty_type"));
					
					if (getAllyPenaltyExpiryTime() < System.currentTimeMillis())
					{
						setAllyPenaltyExpiryTime(0, 0);
					}
					
					setCharPenaltyExpiryTime(clanData.getLong("char_penalty_expiry_time"));
					
					if (getCharPenaltyExpiryTime() + Config.ALT_CLAN_JOIN_DAYS * 86400000L < System.currentTimeMillis())
					{
						setCharPenaltyExpiryTime(0);
					}
					
					setDissolvingExpiryTime(clanData.getLong("dissolving_expiry_time"));
					
					setCrestId(clanData.getInt("crest_id"));
					
					if (getCrestId() != 0)
					{
						setHasCrest(true);
					}
					
					setCrestLargeId(clanData.getInt("crest_large_id"));
					
					if (getCrestLargeId() != 0)
					{
						setHasCrestLarge(true);
					}
					
					setAllyCrestId(clanData.getInt("ally_crest_id"));
					setReputationScore(clanData.getInt("reputation_score"), false);
					setAuctionBiddedAt(clanData.getInt("auction_bid_at"), false);
					
					int leaderId = clanData.getInt("leader_id");
					
					try (PreparedStatement statement2 = con.prepareStatement(SELECT_CHAR_DATA_BY_CLAN_ID))
					{
						statement2.setInt(1, getClanId());
						
						try (ResultSet clanMembers = statement2.executeQuery())
						{
							while (clanMembers.next())
							{
								L2ClanMember member = new L2ClanMember(this, clanMembers.getString("char_name"), clanMembers.getInt("level"), clanMembers.getInt("classid"), clanMembers.getInt("obj_id"), clanMembers.getInt("subpledge"), clanMembers.getInt("power_grade"), clanMembers.getString("title"));
								
								if (member.getObjectId() == leaderId)
								{
									setLeader(member);
								}
								else
								{
									addClanMember(member);
								}
								member.initApprenticeAndSponsor(clanMembers.getInt("apprentice"), clanMembers.getInt("sponsor"));
							}
						}
					}
				}
			}
			
			if (Config.DEBUG && getName() != null)
			{
				LOGGER.debug("Restored clan data for \"" + getName() + "\" from database.");
			}
			
			restoreSubPledges();
			restoreRankPrivs();
			restoreSkills();
			restoreNotice();
		}
		catch (Exception e)
		{
			LOGGER.error("L2Clan.restore : Could not restore clan", e);
		}
	}
	
	private void restoreNotice()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CLAN_NOTICE))
		{
			statement.setInt(1, getClanId());
			
			try (ResultSet noticeData = statement.executeQuery())
			{
				while (noticeData.next())
				{
					noticeEnabled = noticeData.getBoolean("enabled");
					notice = noticeData.getString("notice");
				}
				
			}
		}
		catch (Exception e)
		{
			LOGGER.error("L2Clan.restoreNotice : Error restoring clan notice", e);
		}
	}
	
	private void storeNotice(String notice, final boolean enabled)
	{
		if (notice == null)
		{
			notice = "";
		}
		
		if (notice.length() > MAX_NOTICE_LENGTH)
		{
			notice = notice.substring(0, MAX_NOTICE_LENGTH - 1);
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_CLAN_NOTICE))
		{
			statement.setInt(1, getClanId());
			statement.setString(2, notice);
			if (enabled)
			{
				statement.setString(3, "true");
			}
			else
			{
				statement.setString(3, "false");
			}
			statement.setString(4, notice);
			if (enabled)
			{
				statement.setString(5, "true");
			}
			else
			{
				statement.setString(5, "false");
			}
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("L2Clan.storeNotice : Error could not store clan notice", e);
		}
		
		this.notice = notice;
		noticeEnabled = enabled;
	}
	
	public void setNoticeEnabled(final boolean enabled)
	{
		storeNotice(notice, enabled);
	}
	
	public void setNotice(final String notice)
	{
		storeNotice(notice, noticeEnabled);
	}
	
	public boolean isNoticeEnabled()
	{
		return noticeEnabled;
	}
	
	public String getNotice()
	{
		if (notice == null)
		{
			return "";
		}
		return notice;
	}
	
	private void restoreSkills()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CLAN_SKILLS_BY_CLAN_ID))
		{
			statement.setInt(1, getClanId());
			
			try (ResultSet rset = statement.executeQuery())
			{
				// Go though the recordset of this SQL query
				while (rset.next())
				{
					int id = rset.getInt("skill_id");
					int level = rset.getInt("skill_level");
					
					// Create a L2Skill object for each record
					L2Skill skill = SkillTable.getInstance().getInfo(id, level);
					
					// Add the L2Skill object to the L2Clan skills
					skills.put(skill.getId(), skill);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("L2Clan.restoreSkills : Could not select clan skills from clan_skills table", e);
		}
	}
	
	/**
	 * used to retrieve all skills
	 * @return
	 */
	public final L2Skill[] getAllSkills()
	{
		if (skills == null)
		{
			return new L2Skill[0];
		}
		
		return skills.values().toArray(new L2Skill[skills.values().size()]);
	}
	
	/**
	 * used to add a skill to skill list of this L2Clan
	 * @param  newSkill
	 * @return
	 */
	public L2Skill addSkill(final L2Skill newSkill)
	{
		L2Skill oldSkill = null;
		
		if (newSkill != null)
		{
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = skills.put(newSkill.getId(), newSkill);
		}
		
		return oldSkill;
	}
	
	/**
	 * used to add a new skill to the list, send a packet to all online clan members, update their stats and store it in db
	 * @param  newSkill
	 * @return
	 */
	public L2Skill addNewSkill(final L2Skill newSkill)
	{
		L2Skill oldSkill = null;
		
		if (newSkill != null)
		{
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = skills.put(newSkill.getId(), newSkill);
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
			{
				if (oldSkill != null)
				{
					try (PreparedStatement statement = con.prepareStatement(UPDATE_CLAN_SKILL))
					{
						statement.setInt(1, newSkill.getLevel());
						statement.setInt(2, oldSkill.getId());
						statement.setInt(3, getClanId());
						statement.executeUpdate();
					}
				}
				else
				{
					try (PreparedStatement statement = con.prepareStatement(INSERT_CLAN_SKILL))
					{
						statement.setInt(1, getClanId());
						statement.setInt(2, newSkill.getId());
						statement.setInt(3, newSkill.getLevel());
						statement.setString(4, newSkill.getName());
						statement.executeUpdate();
					}
					catch (SQLException e) // update to avoid miss information
					{
						// Duplicate entry
						if (e.getErrorCode() == 1062)
						{
							try (PreparedStatement statement = con.prepareStatement(UPDATE_DUPLICATE_CLAN_SKILL))
							{
								statement.setInt(1, newSkill.getLevel());
								statement.setInt(2, newSkill.getId());
								statement.setInt(3, getClanId());
								statement.executeUpdate();
							}
						}
						else
						{
							LOGGER.error("L2Clan.addNewSkill : Duplicate entry", e);
						}
					}
				}
			}
			catch (final Exception e2)
			{
				LOGGER.warn("Error could not store char skills: ");
				e2.printStackTrace();
			}
			
			for (final L2ClanMember temp : members.values())
			{
				if (temp == null)
				{
					continue;
				}
				
				if (temp.isOnline())
				{
					if (newSkill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass())
					{
						temp.getPlayerInstance().addSkill(newSkill, false); // Skill is not saved to player DB
						temp.getPlayerInstance().sendPacket(new PledgeSkillListAdd(newSkill.getId(), newSkill.getLevel()));
					}
				}
			}
		}
		
		return oldSkill;
	}
	
	public void addSkillEffects()
	{
		for (final L2Skill skill : skills.values())
		{
			for (final L2ClanMember temp : members.values())
			{
				if (temp == null)
				{
					continue;
				}
				
				if (temp.isOnline())
				{
					if (skill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass())
					{
						temp.getPlayerInstance().addSkill(skill, false); // Skill is not saved to player DB
					}
				}
			}
		}
	}
	
	public void addSkillEffects(final L2PcInstance cm)
	{
		if (cm == null)
		{
			return;
		}
		
		for (final L2Skill skill : skills.values())
		{
			if (skill.getMinPledgeClass() <= cm.getPledgeClass())
			{
				cm.addSkill(skill, false); // Skill is not saved to player DB
			}
		}
	}
	
	public void broadcastToOnlineAllyMembers(final L2GameServerPacket packet)
	{
		if (getAllyId() == 0)
		{
			return;
		}
		
		for (final L2Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getAllyId() == getAllyId())
			{
				clan.broadcastToOnlineMembers(packet);
			}
		}
	}
	
	public void broadcastToOnlineMembers(final L2GameServerPacket packet)
	{
		for (final L2ClanMember member : members.values())
		{
			if (member == null)
			{
				continue;
			}
			
			if (member.isOnline())
			{
				member.getPlayerInstance().sendPacket(packet);
			}
		}
	}
	
	public void broadcastToOtherOnlineMembers(final L2GameServerPacket packet, final L2PcInstance player)
	{
		for (final L2ClanMember member : members.values())
		{
			if (member == null)
			{
				continue;
			}
			
			if (member.isOnline() && member.getPlayerInstance() != player)
			{
				member.getPlayerInstance().sendPacket(packet);
			}
		}
	}
	
	public boolean hasCrest()
	{
		return hasCrest;
	}
	
	public boolean hasCrestLarge()
	{
		return hasCrestLarge;
	}
	
	public void setHasCrest(final boolean flag)
	{
		hasCrest = flag;
	}
	
	public void setHasCrestLarge(final boolean flag)
	{
		hasCrestLarge = flag;
	}
	
	public ItemContainer getWarehouse()
	{
		return warehouse;
	}
	
	public boolean isAtWarWith(final Integer id)
	{
		if (atWarWith != null && atWarWith.size() > 0)
		{
			if (atWarWith.contains(id))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isAtWarAttacker(final Integer id)
	{
		if (atWarAttackers != null && atWarAttackers.size() > 0)
		{
			if (atWarAttackers.contains(id))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public void setEnemyClan(final L2Clan clan)
	{
		Integer id = clan.getClanId();
		atWarWith.add(id);
		
		id = null;
	}
	
	public void setEnemyClan(final Integer clan)
	{
		atWarWith.add(clan);
	}
	
	public void setAttackerClan(final L2Clan clan)
	{
		Integer id = clan.getClanId();
		atWarAttackers.add(id);
		
		id = null;
	}
	
	public void setAttackerClan(final Integer clan)
	{
		atWarAttackers.add(clan);
	}
	
	public void deleteEnemyClan(final L2Clan clan)
	{
		Integer id = clan.getClanId();
		atWarWith.remove(id);
		
		id = null;
	}
	
	public void deleteAttackerClan(final L2Clan clan)
	{
		Integer id = clan.getClanId();
		atWarAttackers.remove(id);
		
		id = null;
	}
	
	public int getHiredGuards()
	{
		return hiredGuards;
	}
	
	public void incrementHiredGuards()
	{
		hiredGuards++;
	}
	
	public int isAtWar()
	{
		if (atWarWith != null && atWarWith.size() > 0)
		{
			return 1;
		}
		
		return 0;
	}
	
	public List<Integer> getWarList()
	{
		return atWarWith;
	}
	
	public List<Integer> getAttackerList()
	{
		return atWarAttackers;
	}
	
	public void broadcastClanStatus()
	{
		for (final L2PcInstance member : getOnlineMembers(""))
		{
			member.sendPacket(new PledgeShowMemberListDeleteAll());
			member.sendPacket(new PledgeShowMemberListAll(this, member));
		}
	}
	
	public void removeSkill(final int id)
	{
		L2Skill deleteSkill = null;
		for (final L2Skill sk : skillList)
		{
			if (sk.getId() == id)
			{
				deleteSkill = sk;
				return;
			}
		}
		skillList.remove(deleteSkill);
	}
	
	public void removeSkill(final L2Skill deleteSkill)
	{
		skillList.remove(deleteSkill);
	}
	
	/**
	 * @return
	 */
	public List<L2Skill> getSkills()
	{
		return skillList;
	}
	
	public class SubPledge
	{
		private final int id;
		private String subPledgeName;
		private String leaderName;
		
		public SubPledge(final int id, final String name, final String leaderName)
		{
			this.id = id;
			subPledgeName = name;
			this.leaderName = leaderName;
		}
		
		public int getId()
		{
			return id;
		}
		
		public String getName()
		{
			return subPledgeName;
		}
		
		public String getLeaderName()
		{
			return leaderName;
		}
		
		public void setLeaderName(final String leaderName)
		{
			this.leaderName = leaderName;
		}
		
		/**
		 * @param pledgeName
		 */
		public void setName(final String pledgeName)
		{
			subPledgeName = pledgeName;
		}
	}
	
	public class RankPrivs
	{
		private final int rankId;
		private final int party;
		private int rankPrivs;
		
		public RankPrivs(final int rank, final int party, final int privs)
		{
			rankId = rank;
			this.party = party;
			rankPrivs = privs;
		}
		
		public int getRank()
		{
			return rankId;
		}
		
		public int getParty()
		{
			return party;
		}
		
		public int getPrivs()
		{
			return rankPrivs;
		}
		
		public void setPrivs(final int privs)
		{
			rankPrivs = privs;
		}
	}
	
	private void restoreSubPledges()
	{
		// Retrieve all subpledges of this clan from the database
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CLAN_SUBPLEDGES_BY_CLAN_ID))
		{
			statement.setInt(1, getClanId());
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int id = rset.getInt("sub_pledge_id");
					String name = rset.getString("name");
					String leaderName = rset.getString("leader_name");
					
					subPledges.put(id, new SubPledge(id, name, leaderName));
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("L2Clan.restoreSubPledges : Could not select clan sub-units from clan_subpledges table", e);
		}
	}
	
	/**
	 * used to retrieve subPledge by type
	 * @param  pledgeType
	 * @return
	 */
	public final SubPledge getSubPledge(final int pledgeType)
	{
		if (subPledges == null)
		{
			return null;
		}
		
		return subPledges.get(pledgeType);
	}
	
	/**
	 * used to retrieve subPledge by type
	 * @param  pledgeName
	 * @return
	 */
	public final SubPledge getSubPledge(final String pledgeName)
	{
		if (subPledges == null)
		{
			return null;
		}
		
		for (final SubPledge sp : subPledges.values())
		{
			if (sp.getName().equalsIgnoreCase(pledgeName))
			{
				return sp;
			}
		}
		return null;
	}
	
	/**
	 * used to retrieve all subPledges
	 * @return
	 */
	public final SubPledge[] getAllSubPledges()
	{
		if (subPledges == null)
		{
			return new SubPledge[0];
		}
		
		return subPledges.values().toArray(new SubPledge[subPledges.values().size()]);
	}
	
	public SubPledge createSubPledge(final L2PcInstance player, int pledgeType, final String leaderName, final String subPledgeName)
	{
		SubPledge subPledge = null;
		pledgeType = getAvailablePledgeTypes(pledgeType);
		
		if (pledgeType == 0)
		{
			if (pledgeType == L2Clan.SUBUNIT_ACADEMY)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY));
			}
			else
			{
				player.sendMessage("You can't create any more sub-units of this type");
			}
			return null;
		}
		
		if (leader.getName().equals(leaderName))
		{
			player.sendMessage("Leader is not correct");
			return null;
		}
		
		// Royal Guard 5000 points per each
		// Order of Knights 10000 points per each
		if (pledgeType != -1 && (getReputationScore() < 5000 && pledgeType < L2Clan.SUBUNIT_KNIGHT1 || getReputationScore() < 10000 && pledgeType > L2Clan.SUBUNIT_ROYAL2))
		{
			SystemMessage sp = new SystemMessage(SystemMessageId.CLAN_REPUTATION_SCORE_IS_TOO_LOW);
			player.sendPacket(sp);
			sp = null;
			
			return null;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_CLAN_SUBPLEDGE))
		{
			statement.setInt(1, getClanId());
			statement.setInt(2, pledgeType);
			statement.setString(3, subPledgeName);
			
			if (pledgeType != -1)
			{
				statement.setString(4, leaderName);
			}
			else
			{
				statement.setString(4, "");
			}
			
			statement.executeUpdate();
			
			if (Config.DEBUG)
			{
				LOGGER.debug("New sub_clan saved in db: " + getClanId() + "; " + pledgeType);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("L2Clan.createSubPledge : Error while saving new sub_clan to db", e);
		}
		
		subPledge = new SubPledge(pledgeType, subPledgeName, leaderName);
		subPledges.put(pledgeType, subPledge);
		
		if (pledgeType != -1)
		{
			setReputationScore(getReputationScore() - 2500, true);
		}
		
		broadcastToOnlineMembers(new PledgeShowInfoUpdate(leader.getClan()));
		broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(subPledge));
		
		return subPledge;
	}
	
	public int getAvailablePledgeTypes(int pledgeType)
	{
		if (subPledges.get(pledgeType) != null)
		{
			// LOGGER.warn("found sub-unit with id: "+pledgeType);
			switch (pledgeType)
			{
				case SUBUNIT_ACADEMY:
					return 0;
				case SUBUNIT_ROYAL1:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_ROYAL2);
					break;
				case SUBUNIT_ROYAL2:
					return 0;
				case SUBUNIT_KNIGHT1:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT2);
					break;
				case SUBUNIT_KNIGHT2:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT3);
					break;
				case SUBUNIT_KNIGHT3:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT4);
					break;
				case SUBUNIT_KNIGHT4:
					return 0;
			}
		}
		return pledgeType;
	}
	
	public void updateSubPledgeInDB(final int pledgeType)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CLAN_SUBPLEDGE))
		{
			statement.setString(1, getSubPledge(pledgeType).getLeaderName());
			statement.setString(2, getSubPledge(pledgeType).getName());
			statement.setInt(3, getClanId());
			statement.setInt(4, pledgeType);
			statement.executeUpdate();
			
			if (Config.DEBUG)
			{
				LOGGER.debug("New subpledge leader saved in db: " + getClanId());
			}
		}
		catch (Exception e)
		{
			LOGGER.error("L2Clan.updateSubPledge : Error while saving new clan leader to db", e);
		}
	}
	
	private void restoreRankPrivs()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CLAN_PRIVIS_BY_CLAN_ID))
		{
			statement.setInt(1, getClanId());
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int rank = rset.getInt("rank");
					int privileges = rset.getInt("privs");
					
					clanPrivs.get(rank).setPrivs(privileges);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("L2Clan.restoreRankPrivs : Could not restore clan privs by rank", e);
		}
	}
	
	public void initializePrivs()
	{
		RankPrivs privs;
		
		for (int i = 1; i < 10; i++)
		{
			privs = new RankPrivs(i, 0, CP_NOTHING);
			clanPrivs.put(i, privs);
		}
		
		privs = null;
	}
	
	public int getRankPrivs(final int rank)
	{
		if (clanPrivs.get(rank) != null)
		{
			return clanPrivs.get(rank).getPrivs();
		}
		return CP_NOTHING;
	}
	
	public void setRankPrivs(final int rank, final int privs)
	{
		if (clanPrivs.get(rank) != null)
		{
			clanPrivs.get(rank).setPrivs(privs);
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(INSERT_CLAN_PRIVIS_ON_DUPLICATE_KEY))
			{
				statement.setInt(1, getClanId());
				statement.setInt(2, rank);
				statement.setInt(3, 0);
				statement.setInt(4, privs);
				statement.setInt(5, privs);
				statement.executeUpdate();
			}
			catch (final Exception e)
			{
				LOGGER.warn("Could not store clan privs for rank: " + e);
			}
			
			for (final L2ClanMember cm : getMembers())
			{
				if (cm.isOnline())
				{
					if (cm.getPowerGrade() == rank)
					{
						if (cm.getPlayerInstance() != null)
						{
							cm.getPlayerInstance().setClanPrivileges(privs);
							cm.getPlayerInstance().sendPacket(new UserInfo(cm.getPlayerInstance()));
						}
					}
				}
			}
			broadcastClanStatus();
		}
		else
		{
			clanPrivs.put(rank, new RankPrivs(rank, 0, privs));
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(INSERT_CLAN_PRIVIS))
			{
				statement.setInt(1, getClanId());
				statement.setInt(2, rank);
				statement.setInt(3, 0);
				statement.setInt(4, privs);
				statement.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("L2Clan.setRankPrivis : Could not create new rank and store clan privs for rank", e);
			}
		}
	}
	
	public final RankPrivs[] getAllRankPrivs()
	{
		if (clanPrivs == null)
		{
			return new RankPrivs[0];
		}
		
		return clanPrivs.values().toArray(new RankPrivs[clanPrivs.values().size()]);
	}
	
	public int getLeaderSubPledge(final String name)
	{
		int id = 0;
		
		for (final SubPledge sp : subPledges.values())
		{
			if (sp.getLeaderName() == null)
			{
				continue;
			}
			
			if (sp.getLeaderName().equals(name))
			{
				id = sp.getId();
			}
		}
		return id;
	}
	
	public void setReputationScore(final int value, final boolean save)
	{
		if (reputationScore >= 0 && value < 0)
		{
			broadcastToOnlineMembers(new SystemMessage(SystemMessageId.REPUTATION_POINTS_0_OR_LOWER_CLAN_SKILLS_DEACTIVATED));
			L2Skill[] skills = getAllSkills();
			
			for (final L2ClanMember member : members.values())
			{
				if (member.isOnline() && member.getPlayerInstance() != null)
				{
					for (final L2Skill sk : skills)
					{
						member.getPlayerInstance().removeSkill(sk, false);
					}
				}
			}
			
			skills = null;
		}
		else if (reputationScore < 0 && value >= 0)
		{
			broadcastToOnlineMembers(new SystemMessage(SystemMessageId.CLAN_SKILLS_WILL_BE_ACTIVATED_SINCE_REPUTATION_IS_0_OR_HIGHER));
			L2Skill[] skills = getAllSkills();
			
			for (final L2ClanMember member : members.values())
			{
				if (member.isOnline() && member.getPlayerInstance() != null)
				{
					for (final L2Skill sk : skills)
					{
						if (sk.getMinPledgeClass() <= member.getPlayerInstance().getPledgeClass())
						{
							member.getPlayerInstance().addSkill(sk, false);
						}
					}
				}
			}
			
			skills = null;
		}
		
		reputationScore = value;
		
		if (reputationScore > 100000000)
		{
			reputationScore = 100000000;
		}
		if (reputationScore < -100000000)
		{
			reputationScore = -100000000;
		}
		
		if (save)
		{
			updateClanInDB();
		}
	}
	
	public int getReputationScore()
	{
		return reputationScore;
	}
	
	public void setRank(final int rank)
	{
		this.rank = rank;
	}
	
	public int getRank()
	{
		return rank;
	}
	
	public int getAuctionBiddedAt()
	{
		return auctionBiddedAt;
	}
	
	public void setAuctionBiddedAt(final int id, final boolean storeInDb)
	{
		auctionBiddedAt = id;
		
		if (storeInDb)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(UPDATE_CLAN_ACUTION_BID_BY_CLAN_ID))
			{
				statement.setInt(1, id);
				statement.setInt(2, getClanId());
				statement.executeUpdate();
			}
			catch (final Exception e)
			{
				LOGGER.warn("Could not store auction for clan: " + e);
			}
		}
	}
	
	/**
	 * Checks if activeChar and target meet various conditions to join a clan
	 * @param  activeChar
	 * @param  target
	 * @param  pledgeType
	 * @return
	 */
	public boolean checkClanJoinCondition(final L2PcInstance activeChar, final L2PcInstance target, final int pledgeType)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_JOIN_CLAN) != L2Clan.CP_CL_JOIN_CLAN)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return false;
		}
		
		if (target == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET));
			return false;
		}
		
		if (activeChar.getObjectId() == target.getObjectId())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_INVITE_YOURSELF));
			return false;
		}
		
		if (getCharPenaltyExpiryTime() > System.currentTimeMillis())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			sm = null;
			return false;
		}
		
		if (target.getClanId() != 0)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_WORKING_WITH_ANOTHER_CLAN);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			sm = null;
			return false;
		}
		
		if (target.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			sm = null;
			return false;
		}
		
		if ((target.getLevel() > 40 || target.getClassId().level() >= 2) && pledgeType == -1)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_DOESNOT_MEET_REQUIREMENTS_TO_JOIN_ACADEMY);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			sm = null;
			activeChar.sendPacket(new SystemMessage(SystemMessageId.ACADEMY_REQUIREMENTS));
			return false;
		}
		
		if (getSubPledgeMembersCount(pledgeType) >= getMaxNrOfMembers(pledgeType))
		{
			if (pledgeType == 0)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_CLAN_IS_FULL);
				sm.addString(getName());
				activeChar.sendPacket(sm);
				sm = null;
			}
			else
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.SUBCLAN_IS_FULL));
			}
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if activeChar and target meet various conditions to join a clan
	 * @param  activeChar
	 * @param  target
	 * @return
	 */
	public boolean checkAllyJoinCondition(final L2PcInstance activeChar, final L2PcInstance target)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (activeChar.getAllyId() == 0 || !activeChar.isClanLeader() || activeChar.getClanId() != activeChar.getAllyId())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER));
			return false;
		}
		
		L2Clan leaderClan = activeChar.getClan();
		
		if (leaderClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis())
		{
			if (leaderClan.getAllyPenaltyType() == PENALTY_TYPE_DISMISS_CLAN)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_INVITE_CLAN_WITHIN_1_DAY));
				return false;
			}
		}
		
		if (target == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET));
			return false;
		}
		
		if (activeChar.getObjectId() == target.getObjectId())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_INVITE_YOURSELF));
			return false;
		}
		
		if (target.getClan() == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_MUST_BE_IN_CLAN));
			return false;
		}
		
		if (!target.isClanLeader())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			sm = null;
			return false;
		}
		
		L2Clan targetClan = target.getClan();
		
		if (target.getAllyId() != 0)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CLAN_ALREADY_MEMBER_OF_S2_ALLIANCE);
			sm.addString(targetClan.getName());
			sm.addString(targetClan.getAllyName());
			activeChar.sendPacket(sm);
			sm = null;
			return false;
		}
		
		if (targetClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis())
		{
			if (targetClan.getAllyPenaltyType() == PENALTY_TYPE_CLAN_LEAVED)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANT_ENTER_ALLIANCE_WITHIN_1_DAY);
				sm.addString(target.getClan().getName());
				sm.addString(target.getClan().getAllyName());
				activeChar.sendPacket(sm);
				sm = null;
				return false;
			}
			if (targetClan.getAllyPenaltyType() == PENALTY_TYPE_CLAN_DISMISSED)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_ENTER_ALLIANCE_WITHIN_1_DAY));
				return false;
			}
		}
		
		if (activeChar.isInsideZone(L2Character.ZONE_SIEGE) && target.isInsideZone(L2Character.ZONE_SIEGE))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.OPPOSING_CLAN_IS_PARTICIPATING_IN_SIEGE));
			return false;
		}
		
		if (leaderClan.isAtWarWith(targetClan.getClanId()))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.MAY_NOT_ALLY_CLAN_BATTLE));
			return false;
		}
		
		int numOfClansInAlly = 0;
		
		for (final L2Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getAllyId() == activeChar.getAllyId())
			{
				++numOfClansInAlly;
			}
		}
		
		if (numOfClansInAlly >= Config.ALT_MAX_NUM_OF_CLANS_IN_ALLY)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_THE_LIMIT));
			return false;
		}
		
		targetClan = null;
		leaderClan = null;
		
		return true;
	}
	
	public long getAllyPenaltyExpiryTime()
	{
		return allyPenaltyExpiryTime;
	}
	
	public int getAllyPenaltyType()
	{
		return allyPenaltyType;
	}
	
	public void setAllyPenaltyExpiryTime(final long expiryTime, final int penaltyType)
	{
		allyPenaltyExpiryTime = expiryTime;
		allyPenaltyType = penaltyType;
	}
	
	public long getCharPenaltyExpiryTime()
	{
		return charPenaltyExpiryTime;
	}
	
	public void setCharPenaltyExpiryTime(final long time)
	{
		charPenaltyExpiryTime = time;
	}
	
	public long getDissolvingExpiryTime()
	{
		return dissolvingExpiryTime;
	}
	
	public void setDissolvingExpiryTime(final long time)
	{
		dissolvingExpiryTime = time;
	}
	
	public void createAlly(final L2PcInstance player, final String allyName)
	{
		if (null == player)
		{
			return;
		}
		
		if (Config.DEBUG)
		{
			LOGGER.debug(player.getObjectId() + "(" + player.getName() + ") requested ally creation from ");
		}
		
		if (!player.isClanLeader())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.ONLY_CLAN_LEADER_CREATE_ALLIANCE));
			return;
		}
		
		if (getAllyId() != 0)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.ALREADY_JOINED_ALLIANCE));
			return;
		}
		
		if (getLevel() < 5)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.TO_CREATE_AN_ALLY_YOU_CLAN_MUST_BE_LEVEL_5_OR_HIGHER));
			return;
		}
		
		if (getAllyPenaltyExpiryTime() > System.currentTimeMillis())
		{
			if (getAllyPenaltyType() == L2Clan.PENALTY_TYPE_DISSOLVE_ALLY)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.CANT_CREATE_ALLIANCE_10_DAYS_DISOLUTION));
				return;
			}
		}
		
		if (getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_CREATE_ALLY_WHILE_DISSOLVING));
			return;
		}
		
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.ALLY_NAME_TEMPLATE);
		}
		catch (final PatternSyntaxException e) // case of illegal pattern
		{
			LOGGER.info("ERROR: Ally name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		
		final Matcher match = pattern.matcher(allyName);
		
		if (!match.matches())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_ALLIANCE_NAME));
			return;
		}
		
		if (allyName.length() > 16 || allyName.length() < 2)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_ALLIANCE_NAME_LENGTH));
			return;
		}
		
		if (ClanTable.getInstance().isAllyExists(allyName))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.ALLIANCE_ALREADY_EXISTS));
			return;
		}
		
		setAllyId(getClanId());
		setAllyName(allyName.trim());
		setAllyPenaltyExpiryTime(0, 0);
		updateClanInDB();
		
		player.sendPacket(new UserInfo(player));
		
		player.sendMessage("Alliance " + allyName + " has been created.");
	}
	
	public void dissolveAlly(final L2PcInstance player)
	{
		if (getAllyId() == 0)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.NO_CURRENT_ALLIANCES));
			return;
		}
		
		if (!player.isClanLeader() || getClanId() != getAllyId())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER));
			return;
		}
		
		if (player.isInsideZone(L2Character.ZONE_SIEGE))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISSOLVE_ALLY_WHILE_IN_SIEGE));
			return;
		}
		
		broadcastToOnlineAllyMembers(new SystemMessage(SystemMessageId.ALLIANCE_DISOLVED));
		
		final long currentTime = System.currentTimeMillis();
		
		for (final L2Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getAllyId() == getAllyId() && clan.getClanId() != getClanId())
			{
				clan.setAllyId(0);
				clan.setAllyName(null);
				clan.setAllyPenaltyExpiryTime(0, 0);
				clan.updateClanInDB();
			}
		}
		
		setAllyId(0);
		setAllyName(null);
		// 24*60*60*1000 = 86400000
		setAllyPenaltyExpiryTime(currentTime + Config.ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED * 86400000L, L2Clan.PENALTY_TYPE_DISSOLVE_ALLY);
		updateClanInDB();
		
		// The clan leader should take the XP penalty of a full death.
		player.deathPenalty(false);
	}
	
	public void levelUpClan(final L2PcInstance player)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return;
		}
		
		if (System.currentTimeMillis() < getDissolvingExpiryTime())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_RISE_LEVEL_WHILE_DISSOLUTION_IN_PROGRESS));
			return;
		}
		
		boolean increaseClanLevel = false;
		
		switch (getLevel())
		{
			case 0:
			{
				// upgrade to 1
				if (player.getSp() >= 30000 && player.getAdena() >= 650000)
				{
					if (player.reduceAdena("ClanLvl", 650000, player.getTarget(), true))
					{
						player.setSp(player.getSp() - 30000);
						SystemMessage sp = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(30000);
						player.sendPacket(sp);
						sp = null;
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 1:
			{
				// upgrade to 2
				if (player.getSp() >= 150000 && player.getAdena() >= 2500000)
				{
					if (player.reduceAdena("ClanLvl", 2500000, player.getTarget(), true))
					{
						player.setSp(player.getSp() - 150000);
						SystemMessage sp = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(150000);
						player.sendPacket(sp);
						sp = null;
						
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 2:
			{
				// upgrade to 3
				if (player.getSp() >= 500000 && player.getInventory().getItemByItemId(1419) != null)
				{
					// itemid 1419 == proof of blood
					if (player.destroyItemByItemId("ClanLvl", 1419, 1, player.getTarget(), false))
					{
						player.setSp(player.getSp() - 500000);
						SystemMessage sp = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(500000);
						player.sendPacket(sp);
						sp = null;
						
						SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
						sm.addItemName(1419);
						sm.addNumber(1);
						player.sendPacket(sm);
						sm = null;
						
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 3:
			{
				// upgrade to 4
				if (player.getSp() >= 1400000 && player.getInventory().getItemByItemId(3874) != null)
				{
					// itemid 3874 == proof of alliance
					if (player.destroyItemByItemId("ClanLvl", 3874, 1, player.getTarget(), false))
					{
						player.setSp(player.getSp() - 1400000);
						SystemMessage sp = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(1400000);
						player.sendPacket(sp);
						sp = null;
						
						SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
						sm.addItemName(3874);
						sm.addNumber(1);
						player.sendPacket(sm);
						sm = null;
						
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 4:
			{
				// upgrade to 5
				if (player.getSp() >= 3500000 && player.getInventory().getItemByItemId(3870) != null)
				{
					// itemid 3870 == proof of aspiration
					if (player.destroyItemByItemId("ClanLvl", 3870, 1, player.getTarget(), false))
					{
						player.setSp(player.getSp() - 3500000);
						SystemMessage sp = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(3500000);
						player.sendPacket(sp);
						sp = null;
						
						SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
						sm.addItemName(3870);
						sm.addNumber(1);
						player.sendPacket(sm);
						sm = null;
						
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 5:
				if (getReputationScore() >= 10000 && getMembersCount() >= 30)
				{
					setReputationScore(getReputationScore() - 10000, true);
					SystemMessage cr = new SystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(10000);
					player.sendPacket(cr);
					cr = null;
					
					increaseClanLevel = true;
				}
				break;
			
			case 6:
				if (getReputationScore() >= 20000 && getMembersCount() >= 80)
				{
					setReputationScore(getReputationScore() - 20000, true);
					SystemMessage cr = new SystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(20000);
					player.sendPacket(cr);
					cr = null;
					
					increaseClanLevel = true;
				}
				break;
			case 7:
				if (getReputationScore() >= 40000 && getMembersCount() >= 120)
				{
					setReputationScore(getReputationScore() - 40000, true);
					SystemMessage cr = new SystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(40000);
					player.sendPacket(cr);
					cr = null;
					
					increaseClanLevel = true;
				}
				break;
			default:
				return;
		}
		
		if (!increaseClanLevel)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.FAILED_TO_INCREASE_CLAN_LEVEL);
			player.sendPacket(sm);
			sm = null;
			return;
		}
		
		// the player should know that he has less sp now :p
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.SP, player.getSp());
		player.sendPacket(su);
		su = null;
		
		ItemList il = new ItemList(player, false);
		player.sendPacket(il);
		il = null;
		
		changeLevel(getLevel() + 1);
	}
	
	public void changeLevel(final int level)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CLAN_LEVEL))
		{
			statement.setInt(1, level);
			statement.setInt(2, getClanId());
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			LOGGER.error("L2Clan.changeLevel : Could not update clan level in clan_data table", e);
		}
		
		setLevel(level);
		
		if (getLeader().isOnline())
		{
			L2PcInstance leader = getLeader().getPlayerInstance();
			
			if (3 < level)
			{
				SiegeManager.getInstance().addSiegeSkills(leader);
			}
			else if (4 > level)
			{
				SiegeManager.getInstance().removeSiegeSkills(leader);
			}
			
			if (4 < level)
			{
				leader.sendPacket(new SystemMessage(SystemMessageId.CLAN_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS));
			}
			
			leader = null;
		}
		
		// notify all the members about it
		broadcastToOnlineMembers(new SystemMessage(SystemMessageId.CLAN_LEVEL_INCREASED));
		broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
	}
	
	public void setAllyCrest(final int crestId)
	{
		setAllyCrestId(crestId);
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_ALLY_CREST_ID))
		{
			statement.setInt(1, crestId);
			statement.setInt(2, getClanId());
			statement.executeUpdate();
		}
		catch (final SQLException e)
		{
			LOGGER.error("L2Clan.setAllyCrest : Could not update the ally crest id in clan_data table", e);
		}
	}
	
	@Override
	public String toString()
	{
		return "L2Clan [name=" + name + ", clanId=" + clanId + ", leader=" + leader + ", members=" + members + ", allyName=" + allyName + ", allyId=" + allyId + ", level=" + clanLevel + ", hasCastle=" + castleId + ", hasFort=" + hasFort + ", hasHideout=" + hasHideout + ", hasCrest=" + hasCrest + ", hiredGuards=" + hiredGuards + ", crestId=" + crestId + ", crestLargeId=" + crestLargeId
			+ ", allyCrestId=" + allyCrestId + ", auctionBiddedAt=" + auctionBiddedAt + ", allyPenaltyExpiryTime=" + allyPenaltyExpiryTime + ", allyPenaltyType=" + allyPenaltyType + ", charPenaltyExpiryTime=" + charPenaltyExpiryTime + ", dissolvingExpiryTime=" + dissolvingExpiryTime + ", warehouse=" + warehouse + ", atWarWith=" + atWarWith + ", atWarAttackers=" + atWarAttackers
			+ ", hasCrestLarge=" + hasCrestLarge + ", forum=" + forum + ", skillList=" + skillList + ", notice=" + notice + ", noticeEnabled=" + noticeEnabled + ", skills=" + skills + ", privs=" + clanPrivs + ", subPledges=" + subPledges + ", reputationScore=" + reputationScore + ", rank=" + rank + "]";
	}
}