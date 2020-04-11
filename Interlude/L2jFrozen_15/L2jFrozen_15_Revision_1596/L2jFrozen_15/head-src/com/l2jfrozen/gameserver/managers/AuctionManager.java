package com.l2jfrozen.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.entity.Auction;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class AuctionManager
{
	protected static final Logger LOGGER = Logger.getLogger(AuctionManager.class);
	private static final String SELECT_AUCTION_ID = "SELECT id FROM auction ORDER BY id";
	private final List<Auction> auctions = new ArrayList<>();
	private static final String[] ITEM_INIT_DATA =
	{
		"(23, 0, 'NPC', 'NPC Clan', 'ClanHall', 23, 0, 'Onyx Hall', 1, 20000000, 0, 1164841200000)",
		"(24, 0, 'NPC', 'NPC Clan', 'ClanHall', 24, 0, 'Topaz Hall', 1, 20000000, 0, 1164841200000)",
		"(25, 0, 'NPC', 'NPC Clan', 'ClanHall', 25, 0, 'Ruby Hall', 1, 20000000, 0, 1164841200000)",
		"(26, 0, 'NPC', 'NPC Clan', 'ClanHall', 26, 0, 'Crystal Hall', 1, 20000000, 0, 1164841200000)",
		"(27, 0, 'NPC', 'NPC Clan', 'ClanHall', 27, 0, 'Onyx Hall', 1, 20000000, 0, 1164841200000)",
		"(28, 0, 'NPC', 'NPC Clan', 'ClanHall', 28, 0, 'Sapphire Hall', 1, 20000000, 0, 1164841200000)",
		"(29, 0, 'NPC', 'NPC Clan', 'ClanHall', 29, 0, 'Moonstone Hall', 1, 20000000, 0, 1164841200000)",
		"(30, 0, 'NPC', 'NPC Clan', 'ClanHall', 30, 0, 'Emerald Hall', 1, 20000000, 0, 1164841200000)",
		"(31, 0, 'NPC', 'NPC Clan', 'ClanHall', 31, 0, 'The Atramental Barracks', 1, 8000000, 0, 1164841200000)",
		"(32, 0, 'NPC', 'NPC Clan', 'ClanHall', 32, 0, 'The Scarlet Barracks', 1, 8000000, 0, 1164841200000)",
		"(33, 0, 'NPC', 'NPC Clan', 'ClanHall', 33, 0, 'The Viridian Barracks', 1, 8000000, 0, 1164841200000)",
		"(36, 0, 'NPC', 'NPC Clan', 'ClanHall', 36, 0, 'The Golden Chamber', 1, 50000000, 0, 1164841200000)",
		"(37, 0, 'NPC', 'NPC Clan', 'ClanHall', 37, 0, 'The Silver Chamber', 1, 50000000, 0, 1164841200000)",
		"(38, 0, 'NPC', 'NPC Clan', 'ClanHall', 38, 0, 'The Mithril Chamber', 1, 50000000, 0, 1164841200000)",
		"(39, 0, 'NPC', 'NPC Clan', 'ClanHall', 39, 0, 'Silver Manor', 1, 50000000, 0, 1164841200000)",
		"(40, 0, 'NPC', 'NPC Clan', 'ClanHall', 40, 0, 'Gold Manor', 1, 50000000, 0, 1164841200000)",
		"(41, 0, 'NPC', 'NPC Clan', 'ClanHall', 41, 0, 'The Bronze Chamber', 1, 50000000, 0, 1164841200000)",
		"(42, 0, 'NPC', 'NPC Clan', 'ClanHall', 42, 0, 'The Golden Chamber', 1, 50000000, 0, 1164841200000)",
		"(43, 0, 'NPC', 'NPC Clan', 'ClanHall', 43, 0, 'The Silver Chamber', 1, 50000000, 0, 1164841200000)",
		"(44, 0, 'NPC', 'NPC Clan', 'ClanHall', 44, 0, 'The Mithril Chamber', 1, 50000000, 0, 1164841200000)",
		"(45, 0, 'NPC', 'NPC Clan', 'ClanHall', 45, 0, 'The Bronze Chamber', 1, 50000000, 0, 1164841200000)",
		"(46, 0, 'NPC', 'NPC Clan', 'ClanHall', 46, 0, 'Silver Manor', 1, 50000000, 0, 1164841200000)",
		"(47, 0, 'NPC', 'NPC Clan', 'ClanHall', 47, 0, 'Moonstone Hall', 1, 50000000, 0, 1164841200000)",
		"(48, 0, 'NPC', 'NPC Clan', 'ClanHall', 48, 0, 'Onyx Hall', 1, 50000000, 0, 1164841200000)",
		"(49, 0, 'NPC', 'NPC Clan', 'ClanHall', 49, 0, 'Emerald Hall', 1, 50000000, 0, 1164841200000)",
		"(50, 0, 'NPC', 'NPC Clan', 'ClanHall', 50, 0, 'Sapphire Hall', 1, 50000000, 0, 1164841200000)",
		"(51, 0, 'NPC', 'NPC Clan', 'ClanHall', 51, 0, 'Mont Chamber', 1, 50000000, 0, 1164841200000)",
		"(52, 0, 'NPC', 'NPC Clan', 'ClanHall', 52, 0, 'Astaire Chamber', 1, 50000000, 0, 1164841200000)",
		"(53, 0, 'NPC', 'NPC Clan', 'ClanHall', 53, 0, 'Aria Chamber', 1, 50000000, 0, 1164841200000)",
		"(54, 0, 'NPC', 'NPC Clan', 'ClanHall', 54, 0, 'Yiana Chamber', 1, 50000000, 0, 1164841200000)",
		"(55, 0, 'NPC', 'NPC Clan', 'ClanHall', 55, 0, 'Roien Chamber', 1, 50000000, 0, 1164841200000)",
		"(56, 0, 'NPC', 'NPC Clan', 'ClanHall', 56, 0, 'Luna Chamber', 1, 50000000, 0, 1164841200000)",
		"(57, 0, 'NPC', 'NPC Clan', 'ClanHall', 57, 0, 'Traban Chamber', 1, 50000000, 0, 1164841200000)",
		"(58, 0, 'NPC', 'NPC Clan', 'ClanHall', 58, 0, 'Eisen Hall', 1, 50000000, 0, 1164841200000)",
		"(59, 0, 'NPC', 'NPC Clan', 'ClanHall', 59, 0, 'Heavy Metal Hall', 1, 50000000, 0, 1164841200000)",
		"(60, 0, 'NPC', 'NPC Clan', 'ClanHall', 60, 0, 'Molten Ore Hall', 1, 50000000, 0, 1164841200000)",
		"(61, 0, 'NPC', 'NPC Clan', 'ClanHall', 61, 0, 'Titan Hall', 1, 50000000, 0, 1164841200000)"
	};
	private static final Integer[] ItemInitDataId =
	{
		23,
		24,
		25,
		26,
		27,
		28,
		29,
		30,
		31,
		32,
		33,
		36,
		37,
		38,
		39,
		40,
		41,
		42,
		43,
		44,
		45,
		46,
		47,
		48,
		49,
		50,
		51,
		52,
		53,
		54,
		55,
		56,
		57,
		58,
		59,
		60,
		61
	};
	
	public static final AuctionManager getInstance()
	{
		return SingletonHolder.instance;
		
	}
	
	public AuctionManager()
	{
		auctions.clear();
		load();
	}
	
	public void reload()
	{
		auctions.clear();
		load();
	}
	
	private void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_AUCTION_ID);
			ResultSet rs = statement.executeQuery())
		{
			while (rs.next())
			{
				auctions.add(new Auction(rs.getInt("id")));
			}
			
			LOGGER.info("Loaded: " + getAuctions().size() + " auction(s)");
		}
		catch (Exception e)
		{
			LOGGER.error("AuctionManager.load : Could not select from auction table", e);
		}
	}
	
	public final Auction getAuction(final int auctionId)
	{
		final int index = getAuctionIndex(auctionId);
		if (index >= 0)
		{
			return getAuctions().get(index);
		}
		
		return null;
	}
	
	public final int getAuctionIndex(final int auctionId)
	{
		Auction auction;
		
		for (int i = 0; i < getAuctions().size(); i++)
		{
			auction = getAuctions().get(i);
			if (auction != null && auction.getId() == auctionId)
			{
				return i;
			}
		}
		
		auction = null;
		return -1;
	}
	
	public final List<Auction> getAuctions()
	{
		return auctions;
	}
	
	/**
	 * Init Clan NPC aution
	 * @param id
	 */
	public void initNPC(final int id)
	{
		int i = 0;
		
		for (i = 0; i < ItemInitDataId.length; i++)
		{
			if (ItemInitDataId[i] == id)
			{
				break;
			}
		}
		
		if (i >= ItemInitDataId.length)
		{
			LOGGER.warn("Clan Hall auction not found for Id :" + id);
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO `auction` VALUES " + ITEM_INIT_DATA[i]))
		{
			statement.execute();
			
			auctions.add(new Auction(id));
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.error("Exception: Auction.initNPC(): " + e.getMessage(), e);
		}
	}
	
	private static class SingletonHolder
	{
		protected static final AuctionManager instance = new AuctionManager();
	}
}
