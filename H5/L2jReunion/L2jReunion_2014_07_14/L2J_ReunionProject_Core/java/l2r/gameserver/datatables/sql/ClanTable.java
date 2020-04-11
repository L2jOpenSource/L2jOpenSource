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
package l2r.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.communitybbs.Managers.ForumsBBSManager;
import l2r.gameserver.enums.EventStage;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.instancemanager.AuctionManager;
import l2r.gameserver.instancemanager.CHSiegeManager;
import l2r.gameserver.instancemanager.FortManager;
import l2r.gameserver.instancemanager.FortSiegeManager;
import l2r.gameserver.instancemanager.SiegeManager;
import l2r.gameserver.model.ClanPrivilege;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2ClanMember;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Auction;
import l2r.gameserver.model.entity.Fort;
import l2r.gameserver.model.entity.FortSiege;
import l2r.gameserver.model.entity.Siege;
import l2r.gameserver.model.entity.clanhall.SiegableHall;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2r.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2r.gameserver.network.serverpackets.PledgeShowMemberListAll;
import l2r.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.UserInfo;
import l2r.gameserver.scripting.scriptengine.events.ClanWarEvent;
import l2r.gameserver.scripting.scriptengine.listeners.clan.ClanWarListener;
import l2r.gameserver.util.Util;
import l2r.util.EnumIntBitmask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class loads the clan related data.
 */
public class ClanTable
{
	private static final Logger _log = LoggerFactory.getLogger(ClanTable.class);
	
	private static List<ClanWarListener> clanWarListeners = new FastList<ClanWarListener>().shared();
	
	private final Map<Integer, L2Clan> _clans = new HashMap<>();
	
	public L2Clan[] getClans()
	{
		return _clans.values().toArray(new L2Clan[_clans.size()]);
	}
	
	protected ClanTable()
	{
		// forums has to be loaded before clan data, because of last forum id used should have also memo included
		if (Config.ENABLE_COMMUNITY)
		{
			ForumsBBSManager.getInstance().initRoot();
		}
		
		L2Clan clan;
		// Count the clans
		int clanCount = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT clan_id FROM clan_data"))
		{
			while (rs.next())
			{
				int clanId = rs.getInt("clan_id");
				_clans.put(clanId, new L2Clan(clanId));
				clan = getClan(clanId);
				if (clan.getDissolvingExpiryTime() != 0)
				{
					scheduleRemoveClan(clan.getId());
				}
				clanCount++;
			}
		}
		catch (Exception e)
		{
			_log.error("Error restoring ClanTable.", e);
		}
		_log.info(getClass().getSimpleName() + ": Restored " + clanCount + " clans from the database.");
		allianceCheck();
		restorewars();
	}
	
	/**
	 * @param clanId
	 * @return
	 */
	public L2Clan getClan(int clanId)
	{
		L2Clan clan = _clans.get(Integer.valueOf(clanId));
		
		return clan;
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
	 * @param player
	 * @param clanName
	 * @return NULL if clan with same name already exists
	 */
	public L2Clan createClan(L2PcInstance player, String clanName)
	{
		if (null == player)
		{
			return null;
		}
		
		if (Config.DEBUG)
		{
			_log.info(getClass().getSimpleName() + ": " + player.getObjectId() + "(" + player.getName() + ") requested a clan creation.");
		}
		
		if (10 > player.getLevel())
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN);
			return null;
		}
		if (0 != player.getClanId())
		{
			player.sendPacket(SystemMessageId.FAILED_TO_CREATE_CLAN);
			return null;
		}
		if (System.currentTimeMillis() < player.getClanCreateExpiryTime())
		{
			player.sendPacket(SystemMessageId.YOU_MUST_WAIT_XX_DAYS_BEFORE_CREATING_A_NEW_CLAN);
			return null;
		}
		if (!Util.isAlphaNumeric(clanName) || (2 > clanName.length()))
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
			return null;
		}
		if (16 < clanName.length())
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_TOO_LONG);
			return null;
		}
		
		if (null != getClanByName(clanName))
		{
			// clan name is already taken
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_EXISTS);
			sm.addString(clanName);
			player.sendPacket(sm);
			sm = null;
			return null;
		}
		
		L2Clan clan = new L2Clan(IdFactory.getInstance().getNextId(), clanName);
		L2ClanMember leader = new L2ClanMember(clan, player);
		clan.setLeader(leader);
		leader.setPlayerInstance(player);
		clan.store();
		player.setClan(clan);
		player.setPledgeClass(L2ClanMember.calculatePledgeClass(player));
		player.setClanPrivileges(new EnumIntBitmask<>(ClanPrivilege.class, true));
		
		_clans.put(Integer.valueOf(clan.getId()), clan);
		
		// should be update packet only
		player.sendPacket(new PledgeShowInfoUpdate(clan));
		player.sendPacket(new PledgeShowMemberListAll(clan, player));
		player.sendPacket(new UserInfo(player));
		player.sendPacket(new ExBrExtraUserInfo(player));
		player.sendPacket(new PledgeShowMemberListUpdate(player));
		player.sendPacket(SystemMessageId.CLAN_CREATED);
		return clan;
	}
	
	public synchronized void destroyClan(int clanId)
	{
		L2Clan clan = getClan(clanId);
		if (clan == null)
		{
			return;
		}
		
		clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_HAS_DISPERSED));
		int castleId = clan.getCastleId();
		if (castleId == 0)
		{
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				siege.removeSiegeClan(clan);
			}
		}
		
		int fortId = clan.getFortId();
		if (fortId == 0)
		{
			for (FortSiege siege : FortSiegeManager.getInstance().getSieges())
			{
				siege.removeAttacker(clan);
			}
		}
		
		int hallId = clan.getHideoutId();
		if (hallId == 0)
		{
			for (SiegableHall hall : CHSiegeManager.getInstance().getConquerableHalls().values())
			{
				hall.removeAttacker(clan);
			}
		}
		
		Auction auction = AuctionManager.getInstance().getAuction(clan.getAuctionBiddedAt());
		if (auction != null)
		{
			auction.cancelBid(clan.getId());
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
			clan.removeClanMember(member.getObjectId(), 0);
		}
		
		_clans.remove(clanId);
		IdFactory.getInstance().releaseId(clanId);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?"))
			{
				ps.setInt(1, clanId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?"))
			{
				ps.setInt(1, clanId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?"))
			{
				ps.setInt(1, clanId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?"))
			{
				ps.setInt(1, clanId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? OR clan2=?"))
			{
				ps.setInt(1, clanId);
				ps.setInt(2, clanId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM clan_notices WHERE clan_id=?"))
			{
				ps.setInt(1, clanId);
				ps.execute();
			}
			
			if (castleId != 0)
			{
				try (PreparedStatement ps = con.prepareStatement("UPDATE castle SET taxPercent = 0 WHERE id = ?"))
				{
					ps.setInt(1, castleId);
					ps.execute();
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
						fort.removeOwner(true);
					}
				}
			}
			
			if (hallId != 0)
			{
				SiegableHall hall = CHSiegeManager.getInstance().getSiegableHall(hallId);
				if ((hall != null) && (hall.getOwnerId() == clanId))
				{
					hall.free();
				}
			}
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Error removing clan from DB.", e);
		}
	}
	
	public void scheduleRemoveClan(final int clanId)
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
		}, Math.max(getClan(clanId).getDissolvingExpiryTime() - System.currentTimeMillis(), 300000));
	}
	
	public boolean isAllyExists(String allyName)
	{
		for (L2Clan clan : getClans())
		{
			if ((clan.getAllyName() != null) && clan.getAllyName().equalsIgnoreCase(allyName))
			{
				return true;
			}
		}
		return false;
	}
	
	public void storeclanswars(int clanId1, int clanId2)
	{
		L2Clan clan1 = ClanTable.getInstance().getClan(clanId1);
		L2Clan clan2 = ClanTable.getInstance().getClan(clanId2);
		
		if (!fireClanWarStartListeners(clan1, clan2))
		{
			return;
		}
		
		clan1.setEnemyClan(clan2);
		clan2.setAttackerClan(clan1);
		clan1.broadcastClanStatus();
		clan2.broadcastClanStatus();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("REPLACE INTO clan_wars (clan1, clan2, wantspeace1, wantspeace2) VALUES(?,?,?,?)"))
		{
			ps.setInt(1, clanId1);
			ps.setInt(2, clanId2);
			ps.setInt(3, 0);
			ps.setInt(4, 0);
			ps.execute();
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Error storing clan wars data.", e);
		}
		
		// SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.WAR_WITH_THE_S1_CLAN_HAS_BEGUN);
		//
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_DECLARED_AGAINST_S1_IF_KILLED_LOSE_LOW_EXP);
		msg.addString(clan2.getName());
		clan1.broadcastToOnlineMembers(msg);
		// msg = SystemMessage.getSystemMessage(SystemMessageId.WAR_WITH_THE_S1_CLAN_HAS_BEGUN);
		// msg.addString(clan1.getName());
		// clan2.broadcastToOnlineMembers(msg);
		// clan1 declared clan war.
		msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_DECLARED_WAR);
		msg.addString(clan1.getName());
		clan2.broadcastToOnlineMembers(msg);
	}
	
	public void deleteclanswars(int clanId1, int clanId2)
	{
		L2Clan clan1 = ClanTable.getInstance().getClan(clanId1);
		L2Clan clan2 = ClanTable.getInstance().getClan(clanId2);
		
		if (!fireClanWarEndListeners(clan1, clan2))
		{
			return;
		}
		
		clan1.deleteEnemyClan(clan2);
		clan2.deleteAttackerClan(clan1);
		clan1.broadcastClanStatus();
		clan2.broadcastClanStatus();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?"))
		{
			ps.setInt(1, clanId1);
			ps.setInt(2, clanId2);
			ps.execute();
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Error removing clan wars data.", e);
		}
		
		// SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.WAR_WITH_THE_S1_CLAN_HAS_ENDED);
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.WAR_AGAINST_S1_HAS_STOPPED);
		msg.addString(clan2.getName());
		clan1.broadcastToOnlineMembers(msg);
		msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_HAS_DECIDED_TO_STOP);
		msg.addString(clan1.getName());
		clan2.broadcastToOnlineMembers(msg);
	}
	
	public void checkSurrender(L2Clan clan1, L2Clan clan2)
	{
		int count = 0;
		for (L2ClanMember player : clan1.getMembers())
		{
			if ((player != null) && (player.getPlayerInstance().getWantsPeace() == 1))
			{
				count++;
			}
		}
		if (count == (clan1.getMembers().length - 1))
		{
			if (!fireClanWarEndListeners(clan1, clan2))
			{
				return;
			}
			clan1.deleteEnemyClan(clan2);
			clan2.deleteEnemyClan(clan1);
			deleteclanswars(clan1.getId(), clan2.getId());
		}
	}
	
	private void restorewars()
	{
		L2Clan clan1, clan2;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement statement = con.createStatement();
			ResultSet rset = statement.executeQuery("SELECT clan1, clan2 FROM clan_wars"))
		{
			while (rset.next())
			{
				clan1 = getClan(rset.getInt("clan1"));
				clan2 = getClan(rset.getInt("clan2"));
				if ((clan1 != null) && (clan2 != null))
				{
					clan1.setEnemyClan(rset.getInt("clan2"));
					clan2.setAttackerClan(rset.getInt("clan1"));
				}
				else
				{
					_log.warn(getClass().getSimpleName() + ": restorewars one of clans is null clan1:" + clan1 + " clan2:" + clan2);
				}
			}
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Error restoring clan wars data.", e);
		}
	}
	
	/**
	 * Check for nonexistent alliances
	 */
	private void allianceCheck()
	{
		for (L2Clan clan : _clans.values())
		{
			int allyId = clan.getAllyId();
			if ((allyId != 0) && (clan.getId() != allyId))
			{
				if (!_clans.containsKey(allyId))
				{
					clan.setAllyId(0);
					clan.setAllyName(null);
					clan.changeAllyCrest(0, true);
					clan.updateClanInDB();
					_log.info(getClass().getSimpleName() + ": Removed alliance from clan: " + clan);
				}
			}
		}
	}
	
	public List<L2Clan> getClanAllies(int allianceId)
	{
		final List<L2Clan> clanAllies = new ArrayList<>();
		if (allianceId != 0)
		{
			for (L2Clan clan : _clans.values())
			{
				if ((clan != null) && (clan.getAllyId() == allianceId))
				{
					clanAllies.add(clan);
				}
			}
		}
		return clanAllies;
	}
	
	public void storeClanScore()
	{
		for (L2Clan clan : _clans.values())
		{
			clan.updateClanScoreInDB();
		}
	}
	
	/**
	 * Fires all the ClanWarListener.onWarStart() methods<br>
	 * Returns true if the clan war is allowed
	 * @param clan1
	 * @param clan2
	 * @return
	 */
	private boolean fireClanWarStartListeners(L2Clan clan1, L2Clan clan2)
	{
		if (!clanWarListeners.isEmpty() && (clan1 != null) && (clan2 != null))
		{
			ClanWarEvent event = new ClanWarEvent();
			event.setClan1(clan1);
			event.setClan2(clan2);
			event.setStage(EventStage.START);
			for (ClanWarListener listener : clanWarListeners)
			{
				if (!listener.onWarStart(event))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Fires all the ClanWarListener.onWarEnd() methods<br>
	 * Returns true if the clan war end is allowed
	 * @param clan1
	 * @param clan2
	 * @return
	 */
	private boolean fireClanWarEndListeners(L2Clan clan1, L2Clan clan2)
	{
		if (!clanWarListeners.isEmpty() && (clan1 != null) && (clan2 != null))
		{
			ClanWarEvent event = new ClanWarEvent();
			event.setClan1(clan1);
			event.setClan2(clan2);
			event.setStage(EventStage.END);
			for (ClanWarListener listener : clanWarListeners)
			{
				if (!listener.onWarEnd(event))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Adds a clan war listener
	 * @param listener
	 */
	public static void addClanWarListener(ClanWarListener listener)
	{
		if (!clanWarListeners.contains(listener))
		{
			clanWarListeners.add(listener);
		}
	}
	
	/**
	 * Removes a clan war listener
	 * @param listener
	 */
	public static void removeClanWarListener(ClanWarListener listener)
	{
		clanWarListeners.remove(listener);
	}
	
	public static ClanTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ClanTable _instance = new ClanTable();
	}
}
