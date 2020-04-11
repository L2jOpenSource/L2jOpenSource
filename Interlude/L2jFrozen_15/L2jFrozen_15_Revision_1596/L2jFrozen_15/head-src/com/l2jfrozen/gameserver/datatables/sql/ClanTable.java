package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.managers.FortManager;
import com.l2jfrozen.gameserver.managers.FortSiegeManager;
import com.l2jfrozen.gameserver.managers.SiegeManager;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2ClanMember;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Fort;
import com.l2jfrozen.gameserver.model.entity.siege.FortSiege;
import com.l2jfrozen.gameserver.model.entity.siege.Siege;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListAll;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.UserInfo;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.Util;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * @version $Revision: 1.11.2.5.2.5 $ $Date: 2005/03/27 15:29:18 $
 */
public class ClanTable
{
	private static Logger LOGGER = Logger.getLogger(ClanTable.class);
	private static final String SELECT_CLAN_ID = "SELECT clan_id FROM clan_data";
	private static final String SELECT_CLAN_WARS = "SELECT clan1, clan2, wantspeace1, wantspeace2 FROM clan_wars";
	private static final String DELETE_CLAN_WAR = "DELETE FROM clan_wars WHERE clan1=? AND clan2=? ";
	private static final String REPLACE_CLAN_WAR = "REPLACE INTO clan_wars (clan1, clan2, wantspeace1, wantspeace2) VALUES(?,?,?,?)";
	
	private static final String DELETE_CLAN_DATA_BY_CLAN_ID = "DELETE FROM clan_data WHERE clan_id=?";
	private static final String DELETE_CLAN_PRIVIS_BY_CLAN_ID = "DELETE FROM clan_privs WHERE clan_id=?";
	private static final String DELETE_CLAN_SKILLS_BY_CLAN_ID = "DELETE FROM clan_skills WHERE clan_id=?";
	private static final String DELETE_CLAN_SUBPLEDGES_BY_CLAN_ID = "DELETE FROM clan_subpledges WHERE clan_id=?";
	
	private static final String UPDATE_TAX_PERCENT_BY_CASTLE_ID = "UPDATE castle SET taxPercent=0 WHERE id=?";
	
	private static ClanTable instance;
	
	private final Map<Integer, L2Clan> clans;
	
	public static ClanTable getInstance()
	{
		if (instance == null)
		{
			instance = new ClanTable();
		}
		
		return instance;
	}
	
	public static void reload()
	{
		instance = null;
		getInstance();
	}
	
	public L2Clan[] getClans()
	{
		return clans.values().toArray(new L2Clan[clans.size()]);
	}
	
	public int getTopRate(int clan_id)
	{
		L2Clan clan = getClan(clan_id);
		
		if (clan.getLevel() < 3)
		{
			return 0;
		}
		
		int i = 1;
		for (L2Clan clans : getClans())
		{
			if (clan != clans)
			{
				if (clan.getLevel() < clans.getLevel())
				{
					i++;
				}
				else if (clan.getLevel() == clans.getLevel())
				{
					if (clan.getReputationScore() <= clans.getReputationScore())
					{
						i++;
					}
				}
			}
		}
		clan = null;
		return i;
	}
	
	private ClanTable()
	{
		clans = new HashMap<>();
		L2Clan clan;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CLAN_ID))
		{
			ResultSet result = statement.executeQuery();
			
			// Count the clans
			int clanCount = 0;
			
			while (result.next())
			{
				clans.put(Integer.parseInt(result.getString("clan_id")), new L2Clan(Integer.parseInt(result.getString("clan_id"))));
				clan = getClan(Integer.parseInt(result.getString("clan_id")));
				if (clan.getDissolvingExpiryTime() != 0)
				{
					if (clan.getDissolvingExpiryTime() < System.currentTimeMillis())
					{
						destroyClan(clan.getClanId());
					}
					else
					{
						scheduleRemoveClan(clan.getClanId());
					}
				}
				clanCount++;
			}
			result.close();
			
			LOGGER.info("Restored " + clanCount + " clans from the database.");
		}
		catch (final Exception e)
		{
			LOGGER.error("ClanTable.ClanTable : Could not select ClanData from clan_data table ", e);
		}
		
		restoreWars();
	}
	
	public L2Clan getClan(int clanId)
	{
		return clans.get(clanId);
	}
	
	public L2Clan getClanByName(String clanName)
	{
		for (L2Clan clan : getClans())
		{
			if (clan.getName().equalsIgnoreCase(clanName))
			{
				return clan;
			}
		}
		return null;
	}
	
	/**
	 * Creates a new clan and store clan info to database
	 * @param  player
	 * @param  clanName
	 * @return          NULL if clan with same name already exists
	 */
	public L2Clan createClan(final L2PcInstance player, final String clanName)
	{
		if (null == player)
		{
			return null;
		}
		
		LOGGER.debug("{" + player.getObjectId() + "}({" + player.getName() + "}) requested a clan creation.");
		
		if (10 > player.getLevel())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN));
			return null;
		}
		
		if (0 != player.getClanId())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.FAILED_TO_CREATE_CLAN));
			return null;
		}
		
		if (System.currentTimeMillis() < player.getClanCreateExpiryTime())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_MUST_WAIT_XX_DAYS_BEFORE_CREATING_A_NEW_CLAN));
			return null;
		}
		
		if (!isValidCalnName(player, clanName))
		{
			return null;
		}
		
		final L2Clan clan = new L2Clan(IdFactory.getInstance().getNextId(), clanName);
		final L2ClanMember leader = new L2ClanMember(clan, player.getName(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), player.getPledgeType(), player.getPowerGrade(), player.getTitle());
		
		clan.setLeader(leader);
		leader.setPlayerInstance(player);
		clan.store();
		player.setClan(clan);
		player.setPledgeClass(leader.calculatePledgeClass(player));
		player.setClanPrivileges(L2Clan.CP_ALL);
		
		LOGGER.debug("New clan created: {" + clan.getClanId() + "} {" + clan.getName() + "}");
		
		clans.put(clan.getClanId(), clan);
		
		// should be update packet only
		player.sendPacket(new PledgeShowInfoUpdate(clan));
		player.sendPacket(new PledgeShowMemberListAll(clan, player));
		player.sendPacket(new UserInfo(player));
		player.sendPacket(new PledgeShowMemberListUpdate(player));
		player.sendPacket(new SystemMessage(SystemMessageId.CLAN_CREATED));
		
		return clan;
	}
	
	public boolean isValidCalnName(final L2PcInstance player, final String clanName)
	{
		if (!Util.isAlphaNumeric(clanName) || clanName.length() < 2)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CLAN_NAME_INCORRECT));
			return false;
		}
		
		if (clanName.length() > 16)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CLAN_NAME_TOO_LONG));
			return false;
		}
		
		if (getClanByName(clanName) != null)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_ALREADY_EXISTS);
			sm.addString(clanName);
			player.sendPacket(sm);
			return false;
		}
		
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.CLAN_NAME_TEMPLATE);
		}
		catch (final PatternSyntaxException e) // case of illegal pattern
		{
			LOGGER.warn("ERROR: Clan name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		
		final Matcher match = pattern.matcher(clanName);
		
		if (!match.matches())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CLAN_NAME_INCORRECT));
			return false;
		}
		
		return true;
	}
	
	public synchronized void destroyClan(int clanId)
	{
		L2Clan clan = getClan(clanId);
		
		if (clan == null)
		{
			return;
		}
		
		L2PcInstance leader = null;
		if (clan.getLeader() != null && (leader = clan.getLeader().getPlayerInstance()) != null)
		{
			leader.getAppearance().setNameColor(0x000000);
			leader.getAppearance().setTitleColor(0xFFFF77);
			
			// remove clan leader skills
			leader.addClanLeaderSkills(false);
		}
		
		clan.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.CLAN_HAS_DISPERSED));
		
		int castleId = clan.getCastleId();
		
		if (castleId == 0)
		{
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				siege.removeSiegeClan(clanId);
			}
		}
		
		int fortId = clan.getHasFort();
		
		if (fortId == 0)
		{
			for (FortSiege siege : FortSiegeManager.getInstance().getSieges())
			{
				siege.removeSiegeClan(clanId);
			}
		}
		
		L2ClanMember leaderMember = clan.getLeader();
		
		if (leaderMember == null)
		{
			clan.getWarehouse().destroyAllItems("ClanRemove", null, null);
		}
		else
		{
			clan.getWarehouse().destroyAllItems("ClanRemove", clan.getLeader().getPlayerInstance(), null);
		}
		
		for (L2ClanMember member : clan.getMembers())
		{
			clan.removeClanMember(member.getName(), 0);
		}
		
		int leaderId = clan.getLeaderId();
		int clanLvl = clan.getLevel();
		
		clans.remove(clanId);
		IdFactory.getInstance().releaseId(clanId);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement statement = con.prepareStatement(DELETE_CLAN_DATA_BY_CLAN_ID))
			{
				statement.setInt(1, clanId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement(DELETE_CLAN_PRIVIS_BY_CLAN_ID))
			{
				statement.setInt(1, clanId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement(DELETE_CLAN_SKILLS_BY_CLAN_ID))
			{
				statement.setInt(1, clanId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement(DELETE_CLAN_SUBPLEDGES_BY_CLAN_ID))
			{
				statement.setInt(1, clanId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? OR clan2=?"))
			{
				statement.setInt(1, clanId);
				statement.setInt(2, clanId);
				statement.executeUpdate();
			}
			
			if (leader == null && leaderId != 0 && Config.CLAN_LEADER_COLOR_ENABLED && clanLvl >= Config.CLAN_LEADER_COLOR_CLAN_LEVEL)
			{
				String query;
				if (Config.CLAN_LEADER_COLORED == 1)
				{
					query = "UPDATE characters SET name_color = '000000' WHERE obj_Id = ?";
				}
				else
				{
					query = "UPDATE characters SET title_color = 'FFFF77' WHERE obj_Id = ?";
				}
				
				try (PreparedStatement statement = con.prepareStatement(query))
				{
					statement.setInt(1, leaderId);
					statement.executeUpdate();
				}
			}
			
			if (castleId != 0)
			{
				try (PreparedStatement statement = con.prepareStatement(UPDATE_TAX_PERCENT_BY_CASTLE_ID))
				{
					statement.setInt(1, castleId);
					statement.executeUpdate();
				}
			}
			
			if (fortId != 0)
			{
				Fort fort = FortManager.getInstance().getFortById(fortId);
				if (fort != null)
				{
					L2Clan owner = fort.getOwnerClan();
					if (clan == owner)
					{
						fort.removeOwner(clan);
					}
				}
			}
			
			LOGGER.debug("Clan removed in db: {}" + " " + clanId);
			
		}
		catch (Exception e)
		{
			LOGGER.error("ClanTable.destroyClan : Error while removing clan in db", e);
		}
	}
	
	public void scheduleRemoveClan(int clanId)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			if (getClan(clanId) == null)
			{
				return;
			}
			
			if (getClan(clanId).getDissolvingExpiryTime() != 0)
			{
				destroyClan(clanId);
			}
		}, getClan(clanId).getDissolvingExpiryTime() - System.currentTimeMillis());
	}
	
	public boolean isAllyExists(String allyName)
	{
		for (L2Clan clan : getClans())
		{
			if (clan.getAllyName() != null && clan.getAllyName().equalsIgnoreCase(allyName))
			{
				return true;
			}
		}
		return false;
	}
	
	public void storeClanWars(int clanId1, int clanId2)
	{
		L2Clan clan1 = ClanTable.getInstance().getClan(clanId1);
		L2Clan clan2 = ClanTable.getInstance().getClan(clanId2);
		
		clan1.setEnemyClan(clan2);
		clan2.setAttackerClan(clan1);
		clan1.broadcastClanStatus();
		clan2.broadcastClanStatus();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(REPLACE_CLAN_WAR))
		{
			statement.setInt(1, clanId1);
			statement.setInt(2, clanId2);
			statement.setInt(3, 0);
			statement.setInt(4, 0);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("ClanTable.storeClanWars : Could not replace clans wars data", e);
		}
		
		SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_WAR_DECLARED_AGAINST_S1_IF_KILLED_LOSE_LOW_EXP);
		msg.addString(clan2.getName());
		clan1.broadcastToOnlineMembers(msg);
		
		msg = new SystemMessage(SystemMessageId.CLAN_S1_DECLARED_WAR);
		msg.addString(clan1.getName());
		clan2.broadcastToOnlineMembers(msg);
	}
	
	public void deleteClanWars(int clanId1, int clanId2)
	{
		L2Clan clan1 = ClanTable.getInstance().getClan(clanId1);
		L2Clan clan2 = ClanTable.getInstance().getClan(clanId2);
		
		clan1.deleteEnemyClan(clan2);
		clan2.deleteAttackerClan(clan1);
		clan1.broadcastClanStatus();
		clan2.broadcastClanStatus();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_CLAN_WAR))
		{
			statement.setInt(1, clanId1);
			statement.setInt(2, clanId2);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("ClanTable.deleteClanWars : Could not delete clans wars data", e);
		}
		
		SystemMessage msg = new SystemMessage(SystemMessageId.WAR_AGAINST_S1_HAS_STOPPED);
		msg.addString(clan2.getName());
		clan1.broadcastToOnlineMembers(msg);
		
		msg = new SystemMessage(SystemMessageId.CLAN_S1_HAS_DECIDED_TO_STOP);
		msg.addString(clan1.getName());
		clan2.broadcastToOnlineMembers(msg);
	}
	
	public void checkSurrender(final L2Clan clan1, final L2Clan clan2)
	{
		int count = 0;
		
		for (final L2ClanMember player : clan1.getMembers())
		{
			if (player != null && player.getPlayerInstance().getWantsPeace() == 1)
			{
				count++;
			}
		}
		
		if (count == clan1.getMembers().length - 1)
		{
			clan1.deleteEnemyClan(clan2);
			clan2.deleteEnemyClan(clan1);
			deleteClanWars(clan1.getClanId(), clan2.getClanId());
		}
	}
	
	private void restoreWars()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CLAN_WARS))
		{
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				getClan(rset.getInt("clan1")).setEnemyClan(rset.getInt("clan2"));
				getClan(rset.getInt("clan2")).setAttackerClan(rset.getInt("clan1"));
			}
			DatabaseUtils.close(rset);
		}
		catch (Exception e)
		{
			LOGGER.error("ClanTable.restoreWars : Could not restore clan wars data", e);
		}
	}
}
