package com.l2jfrozen.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.managers.AuctionManager;
import com.l2jfrozen.gameserver.managers.ClanHallManager;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * The Class Auction.
 */
public class Auction
{
	protected static final Logger LOGGER = Logger.getLogger(Auction.class);
	private static final String SELECT_AUCTIONS = "SELECT id,sellerId,sellerName,sellerClanName,itemType,itemId,itemObjectId,itemName,itemQuantity,startingBid,currentBid,endDate FROM auction WHERE id=?";
	private static final String SELECT_AUCTION_BID_BY_AUCTION_ID = "SELECT bidderId, bidderName, maxBid, clan_name, time_bid FROM auction_bid WHERE auctionId = ? ORDER BY maxBid DESC";
	private static final String UPDATE_AUCTION_BY_ID = "UPDATE auction SET endDate = ? WHERE id = ?";
	private static final String UPDATE_AUCTION_BID_BY_ID_AND_BIDDER_ID = "UPDATE auction_bid SET bidderId=?, bidderName=?, maxBid=?, time_bid=? WHERE auctionId=? AND bidderId=?";
	private static final String INSERT_AUCTION_BID = "INSERT INTO auction_bid (id, auctionId, bidderId, bidderName, maxBid, clan_name, time_bid) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String DELETE_AUCTION_BID_BY_AUCTION_ID = "DELETE FROM auction_bid WHERE auctionId=?";
	private static final String DELETE_AUCTION_BY_ITEM_ID = "DELETE FROM auction WHERE itemId=?";
	private static final String DELETE_AUCTION_BID_BY_ID_AND_BIDDER_ID = "DELETE FROM auction_bid WHERE auctionId=? AND bidderId=?";
	private static final String INSERT_AUCTION = "INSERT INTO auction (id, sellerId, sellerName, sellerClanName, itemType, itemId, itemObjectId, itemName, itemQuantity, startingBid, currentBid, endDate) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
	
	private int id = 0;
	private final int adenaId = 57;
	private long endDate;
	private int highestBidderId = 0;
	private String highestBidderName = "";
	private int highestBidderMaxBid = 0;
	private int itemId = 0;
	private String itemName = "";
	private int itemObjectId = 0;
	private final int itemQuantity = 0;
	private String itemType = "";
	private int sellerId = 0;
	private String sellerClanName = "";
	private String sellerName = "";
	private int currentBid = 0;
	private int startingBid = 0;
	public static final long MAX_ADENA = 99900000000L;
	private final Map<Integer, Bidder> bidders = new HashMap<>();
	private static final String[] ItemTypeName =
	{
		"ClanHall"
	};
	
	/**
	 * The Enum ItemTypeEnum.
	 */
	public static enum ItemTypeEnum
	{
		
		/** The Clan hall. */
		ClanHall
	}
	
	/**
	 * The Class Bidder.
	 */
	public class Bidder
	{
		private final String name;
		private final String clanName;
		private int bid;
		private final Calendar timeBid;
		
		/**
		 * Instantiates a new bidder.
		 * @param name     the name
		 * @param clanName the clan name
		 * @param bid      the bid
		 * @param timeBid  the time bid
		 */
		public Bidder(final String name, final String clanName, final int bid, final long timeBid)
		{
			this.name = name;
			this.clanName = clanName;
			this.bid = bid;
			this.timeBid = Calendar.getInstance();
			this.timeBid.setTimeInMillis(timeBid);
		}
		
		/**
		 * Gets the name.
		 * @return the name
		 */
		public String getName()
		{
			return name;
		}
		
		/**
		 * Gets the clan name.
		 * @return the clan name
		 */
		public String getClanName()
		{
			return clanName;
		}
		
		/**
		 * Gets the bid.
		 * @return the bid
		 */
		public int getBid()
		{
			return bid;
		}
		
		/**
		 * Gets the time bid.
		 * @return the time bid
		 */
		public Calendar getTimeBid()
		{
			return timeBid;
		}
		
		/**
		 * Sets the time bid.
		 * @param timeBid the new time bid
		 */
		public void setTimeBid(final long timeBid)
		{
			this.timeBid.setTimeInMillis(timeBid);
		}
		
		/**
		 * Sets the bid.
		 * @param bid the new bid
		 */
		public void setBid(final int bid)
		{
			this.bid = bid;
		}
	}
	
	/**
	 * Task Sheduler for endAuction.
	 */
	public class AutoEndTask implements Runnable
	{
		
		public AutoEndTask()
		{
		}
		
		@Override
		public void run()
		{
			try
			{
				endAuction();
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					t.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Constructor.
	 * @param auctionId the auction id
	 */
	
	public Auction(final int auctionId)
	{
		id = auctionId;
		load();
		startAutoTask();
	}
	
	/**
	 * Instantiates a new auction.
	 * @param itemId the item id
	 * @param Clan   the clan
	 * @param delay  the delay
	 * @param bid    the bid
	 * @param name   the name
	 */
	public Auction(final int itemId, final L2Clan Clan, final long delay, final int bid, final String name)
	{
		id = itemId;
		endDate = System.currentTimeMillis() + delay;
		this.itemId = itemId;
		itemName = name;
		itemType = "ClanHall";
		sellerId = Clan.getLeaderId();
		sellerName = Clan.getLeaderName();
		sellerClanName = Clan.getName();
		startingBid = bid;
	}
	
	private void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_AUCTIONS))
		{
			statement.setInt(1, getId());
			
			try (ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					currentBid = rs.getInt("currentBid");
					endDate = rs.getLong("endDate");
					itemId = rs.getInt("itemId");
					itemName = rs.getString("itemName");
					itemObjectId = rs.getInt("itemObjectId");
					itemType = rs.getString("itemType");
					sellerId = rs.getInt("sellerId");
					sellerClanName = rs.getString("sellerClanName");
					sellerName = rs.getString("sellerName");
					startingBid = rs.getInt("startingBid");
				}
			}
			loadBid();
		}
		catch (Exception e)
		{
			LOGGER.error("Action.load : Could not select data from auction table", e);
		}
	}
	
	private void loadBid()
	{
		highestBidderId = 0;
		highestBidderName = "";
		highestBidderMaxBid = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_AUCTION_BID_BY_AUCTION_ID))
		{
			statement.setInt(1, getId());
			
			try (ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					if (rs.isFirst())
					{
						highestBidderId = rs.getInt("bidderId");
						highestBidderName = rs.getString("bidderName");
						highestBidderMaxBid = rs.getInt("maxBid");
					}
					bidders.put(rs.getInt("bidderId"), new Bidder(rs.getString("bidderName"), rs.getString("clan_name"), rs.getInt("maxBid"), rs.getLong("time_bid")));
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Auction.loadBid : Could not select auction_bid by auction_id", e);
		}
	}
	
	/**
	 * Task Manage.
	 */
	private void startAutoTask()
	{
		final long currentTime = System.currentTimeMillis();
		long taskDelay = 0;
		
		if (endDate <= currentTime)
		{
			endDate = currentTime + 7 * 24 * 60 * 60 * 1000;
			saveAuctionDate();
		}
		else
		{
			taskDelay = endDate - currentTime;
		}
		
		ThreadPoolManager.getInstance().scheduleGeneral(new AutoEndTask(), taskDelay);
	}
	
	/**
	 * Gets the item type name.
	 * @param  value the value
	 * @return       the item type name
	 */
	public static String getItemTypeName(final ItemTypeEnum value)
	{
		return ItemTypeName[value.ordinal()];
	}
	
	private void saveAuctionDate()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_AUCTION_BY_ID))
		{
			statement.setLong(1, endDate);
			statement.setInt(2, id);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Auction.saveAuctionDate: Could not update auction in db", e);
		}
	}
	
	/**
	 * Set a bid.
	 * @param bidder the bidder
	 * @param bid    the bid
	 */
	public synchronized void setBid(final L2PcInstance bidder, final int bid)
	{
		int requiredAdena = bid;
		
		if (getHighestBidderName().equals(bidder.getClan().getLeaderName()))
		{
			requiredAdena = bid - getHighestBidderMaxBid();
		}
		
		if ((getHighestBidderId() > 0 && bid > getHighestBidderMaxBid()) || (getHighestBidderId() == 0 && bid >= getStartingBid()))
		{
			if (takeItem(bidder, requiredAdena))
			{
				updateInDB(bidder, bid);
				bidder.getClan().setAuctionBiddedAt(id, true);
				return;
			}
		}
		if ((bid < getStartingBid()) || (bid <= getHighestBidderMaxBid()))
		{
			bidder.sendMessage("Bid Price must be higher");
		}
	}
	
	/**
	 * Return Item in WHC.
	 * @param Clan     the clan
	 * @param quantity the quantity
	 * @param penalty  the penalty
	 */
	private void returnItem(final String Clan, int quantity, final boolean penalty)
	{
		if (penalty)
		{
			quantity *= 0.9; // take 10% tax fee if needed
		}
		
		// avoid overflow on return
		final long limit = MAX_ADENA - ClanTable.getInstance().getClanByName(Clan).getWarehouse().getAdena();
		quantity = (int) Math.min(quantity, limit);
		
		ClanTable.getInstance().getClanByName(Clan).getWarehouse().addItem("Outbidded", adenaId, quantity, null, null);
	}
	
	/**
	 * Take Item in WHC.
	 * @param  bidder   the bidder
	 * @param  quantity the quantity
	 * @return          true, if successful
	 */
	private boolean takeItem(final L2PcInstance bidder, final int quantity)
	{
		if (bidder.getClan() != null && bidder.getClan().getWarehouse().getAdena() >= quantity)
		{
			bidder.getClan().getWarehouse().destroyItemByItemId("Buy", adenaId, quantity, bidder, bidder);
			return true;
		}
		bidder.sendMessage("You do not have enough adena");
		return false;
	}
	
	/**
	 * Update auction in DB.
	 * @param bidder the bidder
	 * @param bid    the bid
	 */
	private void updateInDB(final L2PcInstance bidder, final int bid)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
		{
			if (getBidders().get(bidder.getClanId()) != null)
			{
				try (PreparedStatement statement = con.prepareStatement(UPDATE_AUCTION_BID_BY_ID_AND_BIDDER_ID))
				{
					statement.setInt(1, bidder.getClanId());
					statement.setString(2, bidder.getClan().getLeaderName());
					statement.setInt(3, bid);
					statement.setLong(4, System.currentTimeMillis());
					statement.setInt(5, getId());
					statement.setInt(6, bidder.getClanId());
					statement.executeUpdate();
				}
			}
			else
			{
				try (PreparedStatement statement = con.prepareStatement(INSERT_AUCTION_BID);)
				{
					statement.setInt(1, IdFactory.getInstance().getNextId());
					statement.setInt(2, getId());
					statement.setInt(3, bidder.getClanId());
					statement.setString(4, bidder.getName());
					statement.setInt(5, bid);
					statement.setString(6, bidder.getClan().getName());
					statement.setLong(7, System.currentTimeMillis());
					statement.executeUpdate();
				}
				
				if (L2World.getInstance().getPlayer(highestBidderName) != null)
				{
					L2World.getInstance().getPlayer(highestBidderName).sendMessage("You have been out bidded");
				}
			}
			highestBidderId = bidder.getClanId();
			highestBidderMaxBid = bid;
			highestBidderName = bidder.getClan().getLeaderName();
			
			if (bidders.get(highestBidderId) == null)
			{
				bidders.put(highestBidderId, new Bidder(highestBidderName, bidder.getClan().getName(), bid, Calendar.getInstance().getTimeInMillis()));
			}
			else
			{
				bidders.get(highestBidderId).setBid(bid);
				bidders.get(highestBidderId).setTimeBid(Calendar.getInstance().getTimeInMillis());
			}
			
			bidder.sendMessage("You have bidded successfully");
		}
		catch (Exception e)
		{
			LOGGER.error("Auction.updateInDB : Could not update or insert in auction_bid table", e);
		}
	}
	
	private void removeBids()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_AUCTION_BID_BY_AUCTION_ID))
		{
			statement.setInt(1, getId());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Auction.deleteFromDB : Could not delete from auction_bid table", e);
		}
		
		for (final Bidder b : bidders.values())
		{
			if (ClanTable.getInstance().getClanByName(b.getClanName()).getHasHideout() == 0)
			{
				returnItem(b.getClanName(), b.getBid(), true); // 10 % tax
			}
			else
			{
				if (L2World.getInstance().getPlayer(b.getName()) != null)
				{
					L2World.getInstance().getPlayer(b.getName()).sendMessage("Congratulation you have won ClanHall!");
				}
			}
			ClanTable.getInstance().getClanByName(b.getClanName()).setAuctionBiddedAt(0, true);
		}
		bidders.clear();
	}
	
	public void deleteAuctionFromDB()
	{
		AuctionManager.getInstance().getAuctions().remove(this);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_AUCTION_BY_ITEM_ID);)
		{
			statement.setInt(1, itemId);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Auction.deleteFromDB : Could not delete from auction table", e);
		}
	}
	
	public void endAuction()
	{
		ClanHallManager.getInstance();
		if (ClanHallManager.loaded())
		{
			if (highestBidderId == 0 && sellerId == 0)
			{
				startAutoTask();
				return;
			}
			
			if (highestBidderId == 0 && sellerId > 0)
			{
				/**
				 * If seller haven't sell ClanHall, auction removed, THIS MUST BE CONFIRMED
				 */
				final int aucId = AuctionManager.getInstance().getAuctionIndex(id);
				AuctionManager.getInstance().getAuctions().remove(aucId);
				
				return;
			}
			
			if (sellerId > 0)
			{
				returnItem(sellerClanName, highestBidderMaxBid, true);
				returnItem(sellerClanName, ClanHallManager.getInstance().getClanHallById(itemId).getLease(), false);
			}
			
			deleteAuctionFromDB();
			L2Clan Clan = ClanTable.getInstance().getClanByName(bidders.get(highestBidderId).getClanName());
			bidders.remove(highestBidderId);
			Clan.setAuctionBiddedAt(0, true);
			removeBids();
			ClanHallManager.getInstance().setOwner(itemId, Clan);
			Clan = null;
		}
		else
		{
			/** Task waiting ClanHallManager is loaded every 3s */
			ThreadPoolManager.getInstance().scheduleGeneral(new AutoEndTask(), 3000);
		}
	}
	
	public synchronized void cancelBid(final int bidder)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_AUCTION_BID_BY_ID_AND_BIDDER_ID))
		{
			statement.setInt(1, getId());
			statement.setInt(2, bidder);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Auction.cancelBid : Could not delete from auction_bid table", e);
		}
		
		returnItem(bidders.get(bidder).getClanName(), bidders.get(bidder).getBid(), true);
		ClanTable.getInstance().getClanByName(bidders.get(bidder).getClanName()).setAuctionBiddedAt(0, true);
		bidders.clear();
		loadBid();
	}
	
	/**
	 * Cancel auction.
	 */
	public void cancelAuction()
	{
		deleteAuctionFromDB();
		removeBids();
	}
	
	public void confirmAuction()
	{
		AuctionManager.getInstance().getAuctions().add(this);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_AUCTION))
		{
			statement.setInt(1, getId());
			statement.setInt(2, sellerId);
			statement.setString(3, sellerName);
			statement.setString(4, sellerClanName);
			statement.setString(5, itemType);
			statement.setInt(6, itemId);
			statement.setInt(7, itemObjectId);
			statement.setString(8, itemName);
			statement.setInt(9, itemQuantity);
			statement.setInt(10, startingBid);
			statement.setInt(11, currentBid);
			statement.setLong(12, endDate);
			statement.executeUpdate();
			loadBid();
		}
		catch (Exception e)
		{
			LOGGER.error("Auction.load : Could not insert into auction table", e);
		}
	}
	
	/**
	 * Get var auction.
	 * @return the id
	 */
	public final int getId()
	{
		return id;
	}
	
	/**
	 * Gets the current bid.
	 * @return the current bid
	 */
	public final int getCurrentBid()
	{
		return currentBid;
	}
	
	/**
	 * Gets the end date.
	 * @return the end date
	 */
	public final long getEndDate()
	{
		return endDate;
	}
	
	/**
	 * Gets the highest bidder id.
	 * @return the highest bidder id
	 */
	public final int getHighestBidderId()
	{
		return highestBidderId;
	}
	
	/**
	 * Gets the highest bidder name.
	 * @return the highest bidder name
	 */
	public final String getHighestBidderName()
	{
		return highestBidderName;
	}
	
	/**
	 * Gets the highest bidder max bid.
	 * @return the highest bidder max bid
	 */
	public final int getHighestBidderMaxBid()
	{
		return highestBidderMaxBid;
	}
	
	/**
	 * Gets the item id.
	 * @return the item id
	 */
	public final int getItemId()
	{
		return itemId;
	}
	
	/**
	 * Gets the item name.
	 * @return the item name
	 */
	public final String getItemName()
	{
		return itemName;
	}
	
	/**
	 * Gets the item object id.
	 * @return the item object id
	 */
	public final int getItemObjectId()
	{
		return itemObjectId;
	}
	
	/**
	 * Gets the item quantity.
	 * @return the item quantity
	 */
	public final int getItemQuantity()
	{
		return itemQuantity;
	}
	
	/**
	 * Gets the item type.
	 * @return the item type
	 */
	public final String getItemType()
	{
		return itemType;
	}
	
	/**
	 * Gets the seller id.
	 * @return the seller id
	 */
	public final int getSellerId()
	{
		return sellerId;
	}
	
	/**
	 * Gets the seller name.
	 * @return the seller name
	 */
	public final String getSellerName()
	{
		return sellerName;
	}
	
	/**
	 * Gets the seller clan name.
	 * @return the seller clan name
	 */
	public final String getSellerClanName()
	{
		return sellerClanName;
	}
	
	/**
	 * Gets the starting bid.
	 * @return the starting bid
	 */
	public final int getStartingBid()
	{
		return startingBid;
	}
	
	/**
	 * Gets the bidders.
	 * @return the bidders
	 */
	public final Map<Integer, Bidder> getBidders()
	{
		return bidders;
	}
}
