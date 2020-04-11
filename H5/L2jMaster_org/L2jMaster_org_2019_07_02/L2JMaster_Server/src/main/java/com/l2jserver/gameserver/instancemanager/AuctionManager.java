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
package com.l2jserver.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.model.entity.Auction;

/**
 * Zoey76: TODO: Rewrite it and unharcode it.
 */
public final class AuctionManager
{
	protected static final Logger _log = Logger.getLogger(AuctionManager.class.getName());
	private final List<Auction> _auctions = new ArrayList<>();
	
	private static final String[] ITEM_INIT_DATA =
	{
		"(22, 0, 'NPC', 'NPC Clan', 'ClanHall', 22, 0, 'Moonstone Hall', 1, 20000000, 0, 1073037600000)",
		"(23, 0, 'NPC', 'NPC Clan', 'ClanHall', 23, 0, 'Onyx Hall', 1, 20000000, 0, 1073037600000)",
		"(24, 0, 'NPC', 'NPC Clan', 'ClanHall', 24, 0, 'Topaz Hall', 1, 20000000, 0, 1073037600000)",
		"(25, 0, 'NPC', 'NPC Clan', 'ClanHall', 25, 0, 'Ruby Hall', 1, 20000000, 0, 1073037600000)",
		"(26, 0, 'NPC', 'NPC Clan', 'ClanHall', 26, 0, 'Crystal Hall', 1, 20000000, 0, 1073037600000)",
		"(27, 0, 'NPC', 'NPC Clan', 'ClanHall', 27, 0, 'Onyx Hall', 1, 20000000, 0, 1073037600000)",
		"(28, 0, 'NPC', 'NPC Clan', 'ClanHall', 28, 0, 'Sapphire Hall', 1, 20000000, 0, 1073037600000)",
		"(29, 0, 'NPC', 'NPC Clan', 'ClanHall', 29, 0, 'Moonstone Hall', 1, 20000000, 0, 1073037600000)",
		"(30, 0, 'NPC', 'NPC Clan', 'ClanHall', 30, 0, 'Emerald Hall', 1, 20000000, 0, 1073037600000)",
		"(31, 0, 'NPC', 'NPC Clan', 'ClanHall', 31, 0, 'The Atramental Barracks', 1, 8000000, 0, 1073037600000)",
		"(32, 0, 'NPC', 'NPC Clan', 'ClanHall', 32, 0, 'The Scarlet Barracks', 1, 8000000, 0, 1073037600000)",
		"(33, 0, 'NPC', 'NPC Clan', 'ClanHall', 33, 0, 'The Viridian Barracks', 1, 8000000, 0, 1073037600000)",
		"(36, 0, 'NPC', 'NPC Clan', 'ClanHall', 36, 0, 'The Golden Chamber', 1, 50000000, 0, 1106827200000)",
		"(37, 0, 'NPC', 'NPC Clan', 'ClanHall', 37, 0, 'The Silver Chamber', 1, 50000000, 0, 1106827200000)",
		"(38, 0, 'NPC', 'NPC Clan', 'ClanHall', 38, 0, 'The Mithril Chamber', 1, 50000000, 0, 1106827200000)",
		"(39, 0, 'NPC', 'NPC Clan', 'ClanHall', 39, 0, 'Silver Manor', 1, 50000000, 0, 1106827200000)",
		"(40, 0, 'NPC', 'NPC Clan', 'ClanHall', 40, 0, 'Gold Manor', 1, 50000000, 0, 1106827200000)",
		"(41, 0, 'NPC', 'NPC Clan', 'ClanHall', 41, 0, 'The Bronze Chamber', 1, 50000000, 0, 1106827200000)",
		"(42, 0, 'NPC', 'NPC Clan', 'ClanHall', 42, 0, 'The Golden Chamber', 1, 50000000, 0, 1106827200000)",
		"(43, 0, 'NPC', 'NPC Clan', 'ClanHall', 43, 0, 'The Silver Chamber', 1, 50000000, 0, 1106827200000)",
		"(44, 0, 'NPC', 'NPC Clan', 'ClanHall', 44, 0, 'The Mithril Chamber', 1, 50000000, 0, 1106827200000)",
		"(45, 0, 'NPC', 'NPC Clan', 'ClanHall', 45, 0, 'The Bronze Chamber', 1, 50000000, 0, 1106827200000)",
		"(46, 0, 'NPC', 'NPC Clan', 'ClanHall', 46, 0, 'Silver Manor', 1, 50000000, 0, 1106827200000)",
		"(47, 0, 'NPC', 'NPC Clan', 'ClanHall', 47, 0, 'Moonstone Hall', 1, 50000000, 0, 1106827200000)",
		"(48, 0, 'NPC', 'NPC Clan', 'ClanHall', 48, 0, 'Onyx Hall', 1, 50000000, 0, 1106827200000)",
		"(49, 0, 'NPC', 'NPC Clan', 'ClanHall', 49, 0, 'Emerald Hall', 1, 50000000, 0, 1106827200000)",
		"(50, 0, 'NPC', 'NPC Clan', 'ClanHall', 50, 0, 'Sapphire Hall', 1, 50000000, 0, 1106827200000)",
		"(51, 0, 'NPC', 'NPC Clan', 'ClanHall', 51, 0, 'Mont Chamber', 1, 50000000, 0, 1106827200000)",
		"(52, 0, 'NPC', 'NPC Clan', 'ClanHall', 52, 0, 'Astaire Chamber', 1, 50000000, 0, 1106827200000)",
		"(53, 0, 'NPC', 'NPC Clan', 'ClanHall', 53, 0, 'Aria Chamber', 1, 50000000, 0, 1106827200000)",
		"(54, 0, 'NPC', 'NPC Clan', 'ClanHall', 54, 0, 'Yiana Chamber', 1, 50000000, 0, 1106827200000)",
		"(55, 0, 'NPC', 'NPC Clan', 'ClanHall', 55, 0, 'Roien Chamber', 1, 50000000, 0, 1106827200000)",
		"(56, 0, 'NPC', 'NPC Clan', 'ClanHall', 56, 0, 'Luna Chamber', 1, 50000000, 0, 1106827200000)",
		"(57, 0, 'NPC', 'NPC Clan', 'ClanHall', 57, 0, 'Traban Chamber', 1, 50000000, 0, 1106827200000)",
		"(58, 0, 'NPC', 'NPC Clan', 'ClanHall', 58, 0, 'Eisen Hall', 1, 20000000, 0, 1106827200000)",
		"(59, 0, 'NPC', 'NPC Clan', 'ClanHall', 59, 0, 'Heavy Metal Hall', 1, 20000000, 0, 1106827200000)",
		"(60, 0, 'NPC', 'NPC Clan', 'ClanHall', 60, 0, 'Molten Ore Hall', 1, 20000000, 0, 1106827200000)",
		"(61, 0, 'NPC', 'NPC Clan', 'ClanHall', 61, 0, 'Titan Hall', 1, 50000000, 0, 1164841200000)",
		"(201,0, 'NPC', 'NPC Clan', 'ClanHall',201, 0, 'Titanum Chamber', 1, 50000000, 0, 1164841200000)",
		"(202,0, 'NPC', 'NPC Clan', 'ClanHall',202, 0, 'Knights Chamber', 1, 50000000, 0, 1164841200000)",
		"(203,0, 'NPC', 'NPC Clan', 'ClanHall',203, 0, 'Phoenix Chamber', 1, 50000000, 0, 1164841200000)",
		"(205,0, 'NPC', 'NPC Clan', 'ClanHall',205, 0, 'Waterfall Hall', 1, 50000000, 0, 1164841200000)",
		"(206,0, 'NPC', 'NPC Clan', 'ClanHall',206, 0, 'Giants Hall', 1, 50000000, 0, 1164841200000)",
		"(207,0, 'NPC', 'NPC Clan', 'ClanHall',207, 0, 'Earth Hall', 1, 50000000, 0, 1164841200000)",
		"(208,0, 'NPC', 'NPC Clan', 'ClanHall',208, 0, 'Wenus Chamber', 1, 50000000, 0, 1164841200000)",
		"(209,0, 'NPC', 'NPC Clan', 'ClanHall',209, 0, 'Saturn Chamber', 1, 50000000, 0, 1164841200000)",
		"(210,0, 'NPC', 'NPC Clan', 'ClanHall',210, 0, 'Hunters Hall', 1, 50000000, 0, 1164841200000)",
		"(211,0, 'NPC', 'NPC Clan', 'ClanHall',211, 0, 'Forbidden Hall', 1, 50000000, 0, 1164841200000)",
		"(212,0, 'NPC', 'NPC Clan', 'ClanHall',212, 0, 'Enchanted Hall', 1, 50000000, 0, 1164841200000)",
		"(215,0, 'NPC', 'NPC Clan', 'ClanHall',215, 0, 'Lion Hall', 1, 50000000, 0, 1164841200000)",
		"(216,0, 'NPC', 'NPC Clan', 'ClanHall',216, 0, 'Puma Hall', 1, 50000000, 0, 1164841200000)"
	};
	
	// @formatter:off
	private static final int[] ItemInitDataId =
	{
		22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 36, 37, 38, 39, 40, 41, 42,
		43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61,
		201, 202, 203, 205, 206, 207, 208, 209, 210, 211, 212, 215, 216
	};
	// @formatter:on
	
	protected AuctionManager()
	{
		load();
	}
	
	public void reload()
	{
		_auctions.clear();
		load();
	}
	
	private final void load()
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT id FROM auction ORDER BY id"))
		{
			while (rs.next())
			{
				_auctions.add(new Auction(rs.getInt("id")));
			}
			_log.info(getClass().getSimpleName() + ": Loaded: " + _auctions.size() + " auction(s)");
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception: AuctionManager.load(): " + e.getMessage(), e);
		}
	}
	
	public final Auction getAuction(int auctionId)
	{
		int index = getAuctionIndex(auctionId);
		if (index >= 0)
		{
			return _auctions.get(index);
		}
		return null;
	}
	
	public final int getAuctionIndex(int auctionId)
	{
		Auction auction;
		for (int i = 0; i < _auctions.size(); i++)
		{
			auction = _auctions.get(i);
			if ((auction != null) && (auction.getId() == auctionId))
			{
				return i;
			}
		}
		return -1;
	}
	
	public final List<Auction> getAuctions()
	{
		return _auctions;
	}
	
	/**
	 * Init Clan NPC aution
	 * @param id
	 */
	public void initNPC(int id)
	{
		int i;
		for (i = 0; i < ItemInitDataId.length; i++)
		{
			if (ItemInitDataId[i] == id)
			{
				break;
			}
		}
		if ((i >= ItemInitDataId.length) || (ItemInitDataId[i] != id))
		{
			_log.warning(getClass().getSimpleName() + ": Clan Hall auction not found for Id :" + id);
			return;
		}
		
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			Statement s = con.createStatement())
		{
			s.executeUpdate("INSERT INTO `auction` VALUES " + ITEM_INIT_DATA[i]);
			_auctions.add(new Auction(id));
			_log.info(getClass().getSimpleName() + ": Created auction for ClanHall: " + id);
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Exception: Auction.initNPC(): " + e.getMessage(), e);
		}
	}
	
	public static final AuctionManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AuctionManager _instance = new AuctionManager();
	}
}
