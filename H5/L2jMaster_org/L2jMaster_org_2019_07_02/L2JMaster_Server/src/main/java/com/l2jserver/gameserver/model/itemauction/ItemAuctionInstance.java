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
package com.l2jserver.gameserver.model.itemauction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jserver.Config;
import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.data.sql.impl.CharNameTable;
import com.l2jserver.gameserver.enums.ItemLocation;
import com.l2jserver.gameserver.instancemanager.ItemAuctionManager;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.util.Rnd;

public final class ItemAuctionInstance
{
	private static final Logger LOG = LoggerFactory.getLogger(ItemAuctionInstance.class);
	private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss dd.MM.yy");
	
	private static final long START_TIME_SPACE = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
	private static final long FINISH_TIME_SPACE = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);
	
	// SQL queries
	private static final String SELECT_AUCTION_ID_BY_INSTANCE_ID = "SELECT auctionId FROM item_auction WHERE instanceId = ?";
	private static final String SELECT_AUCTION_INFO = "SELECT auctionItemId, startingTime, endingTime, auctionStateId FROM item_auction WHERE auctionId = ? ";
	private static final String DELETE_AUCTION_INFO_BY_AUCTION_ID = "DELETE FROM item_auction WHERE auctionId = ?";
	private static final String DELETE_AUCTION_BID_INFO_BY_AUCTION_ID = "DELETE FROM item_auction_bid WHERE auctionId = ?";
	private static final String SELECT_PLAYERS_ID_BY_AUCTION_ID = "SELECT playerObjId, playerBid FROM item_auction_bid WHERE auctionId = ?";
	
	private final int _instanceId;
	private final AtomicInteger _auctionIds;
	private final Map<Integer, ItemAuction> _auctions;
	private final ArrayList<AuctionItem> _items;
	private final AuctionDateGenerator _dateGenerator;
	
	private ItemAuction _currentAuction;
	private ItemAuction _nextAuction;
	private ScheduledFuture<?> _stateTask;
	
	public ItemAuctionInstance(final int instanceId, final AtomicInteger auctionIds, final Node node) throws Exception
	{
		_instanceId = instanceId;
		_auctionIds = auctionIds;
		_auctions = new HashMap<>();
		_items = new ArrayList<>();
		
		final NamedNodeMap nanode = node.getAttributes();
		final StatsSet generatorConfig = new StatsSet();
		for (int i = nanode.getLength(); i-- > 0;)
		{
			final Node n = nanode.item(i);
			if (n != null)
			{
				generatorConfig.set(n.getNodeName(), n.getNodeValue());
			}
		}
		
		_dateGenerator = new AuctionDateGenerator(generatorConfig);
		
		for (Node na = node.getFirstChild(); na != null; na = na.getNextSibling())
		{
			try
			{
				if ("item".equalsIgnoreCase(na.getNodeName()))
				{
					final NamedNodeMap naa = na.getAttributes();
					final int auctionItemId = Integer.parseInt(naa.getNamedItem("auctionItemId").getNodeValue());
					final int auctionLenght = Integer.parseInt(naa.getNamedItem("auctionLenght").getNodeValue());
					final long auctionInitBid = Integer.parseInt(naa.getNamedItem("auctionInitBid").getNodeValue());
					
					final int itemId = Integer.parseInt(naa.getNamedItem("itemId").getNodeValue());
					final int itemCount = Integer.parseInt(naa.getNamedItem("itemCount").getNodeValue());
					
					if (auctionLenght < 1)
					{
						throw new IllegalArgumentException("auctionLenght < 1 for instanceId: " + _instanceId + ", itemId " + itemId);
					}
					
					final StatsSet itemExtra = new StatsSet();
					final AuctionItem item = new AuctionItem(auctionItemId, auctionLenght, auctionInitBid, itemId, itemCount, itemExtra);
					
					if (!item.checkItemExists())
					{
						throw new IllegalArgumentException("Item with id " + itemId + " not found");
					}
					
					for (final AuctionItem tmp : _items)
					{
						if (tmp.getAuctionItemId() == auctionItemId)
						{
							throw new IllegalArgumentException("Dublicated auction item id " + auctionItemId);
						}
					}
					
					_items.add(item);
					
					for (Node nb = na.getFirstChild(); nb != null; nb = nb.getNextSibling())
					{
						if ("extra".equalsIgnoreCase(nb.getNodeName()))
						{
							final NamedNodeMap nab = nb.getAttributes();
							for (int i = nab.getLength(); i-- > 0;)
							{
								final Node n = nab.item(i);
								if (n != null)
								{
									itemExtra.set(n.getNodeName(), n.getNodeValue());
								}
							}
						}
					}
				}
			}
			catch (IllegalArgumentException e)
			{
				LOG.warn("Failed loading auction item!", e);
			}
		}
		
		if (_items.isEmpty())
		{
			throw new IllegalArgumentException("No items defined");
		}
		
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SELECT_AUCTION_ID_BY_INSTANCE_ID))
		{
			ps.setInt(1, _instanceId);
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					final int auctionId = rset.getInt(1);
					try
					{
						final ItemAuction auction = loadAuction(auctionId);
						if (auction != null)
						{
							_auctions.put(auctionId, auction);
						}
						else
						{
							ItemAuctionManager.deleteAuction(auctionId);
						}
					}
					catch (SQLException e)
					{
						LOG.warn("Failed loading auction ID {}!", auctionId, e);
					}
				}
			}
		}
		catch (SQLException e)
		{
			LOG.error("Error loading auctions.", e);
			return;
		}
		
		LOG.info("Loaded {} item(s) and registered {} auction(s) for NPC ID {}.", _items.size(), _auctions.size(), _instanceId);
		checkAndSetCurrentAndNextAuction();
	}
	
	public final ItemAuction getCurrentAuction()
	{
		return _currentAuction;
	}
	
	public final ItemAuction getNextAuction()
	{
		return _nextAuction;
	}
	
	public final void shutdown()
	{
		final ScheduledFuture<?> stateTask = _stateTask;
		if (stateTask != null)
		{
			stateTask.cancel(false);
		}
	}
	
	private final AuctionItem getAuctionItem(final int auctionItemId)
	{
		for (int i = _items.size(); i-- > 0;)
		{
			final AuctionItem item = _items.get(i);
			if (item.getAuctionItemId() == auctionItemId)
			{
				return item;
			}
		}
		return null;
	}
	
	final void checkAndSetCurrentAndNextAuction()
	{
		final ItemAuction[] auctions = _auctions.values().toArray(new ItemAuction[_auctions.size()]);
		
		ItemAuction currentAuction = null;
		ItemAuction nextAuction = null;
		
		switch (auctions.length)
		{
			case 0:
			{
				nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE);
				break;
			}
			
			case 1:
			{
				switch (auctions[0].getAuctionState())
				{
					case CREATED:
					{
						if (auctions[0].getStartingTime() < (System.currentTimeMillis() + START_TIME_SPACE))
						{
							currentAuction = auctions[0];
							nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE);
						}
						else
						{
							nextAuction = auctions[0];
						}
						break;
					}
					
					case STARTED:
					{
						currentAuction = auctions[0];
						nextAuction = createAuction(Math.max(currentAuction.getEndingTime() + FINISH_TIME_SPACE, System.currentTimeMillis() + START_TIME_SPACE));
						break;
					}
					
					case FINISHED:
					{
						currentAuction = auctions[0];
						nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE);
						break;
					}
					
					default:
						throw new IllegalArgumentException();
				}
				break;
			}
			
			default:
			{
				Arrays.sort(auctions, Comparator.comparingLong(ItemAuction::getStartingTime).reversed());
				
				// just to make sure we won't skip any auction because of little different times
				final long currentTime = System.currentTimeMillis();
				
				for (final ItemAuction auction : auctions)
				{
					if (auction.getAuctionState() == ItemAuctionState.STARTED)
					{
						currentAuction = auction;
						break;
					}
					else if (auction.getStartingTime() <= currentTime)
					{
						currentAuction = auction;
						break; // only first
					}
				}
				
				for (final ItemAuction auction : auctions)
				{
					if ((auction.getStartingTime() > currentTime) && (currentAuction != auction))
					{
						nextAuction = auction;
						break;
					}
				}
				
				if (nextAuction == null)
				{
					nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE);
				}
				break;
			}
		}
		
		_auctions.put(nextAuction.getAuctionId(), nextAuction);
		
		_currentAuction = currentAuction;
		_nextAuction = nextAuction;
		
		if ((currentAuction != null) && (currentAuction.getAuctionState() != ItemAuctionState.FINISHED))
		{
			if (currentAuction.getAuctionState() == ItemAuctionState.STARTED)
			{
				setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleAuctionTask(currentAuction), Math.max(currentAuction.getEndingTime() - System.currentTimeMillis(), 0L)));
			}
			else
			{
				setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleAuctionTask(currentAuction), Math.max(currentAuction.getStartingTime() - System.currentTimeMillis(), 0L)));
			}
			LOG.info("Schedule current auction ID {} for NPC ID {}.", currentAuction.getAuctionId(), _instanceId);
		}
		else
		{
			setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleAuctionTask(nextAuction), Math.max(nextAuction.getStartingTime() - System.currentTimeMillis(), 0L)));
			LOG.info("Schedule next auction ID {} on {} for NPC ID {}.", nextAuction.getAuctionId(), DATE_FORMAT.format(new Date(nextAuction.getStartingTime())), _instanceId);
		}
	}
	
	public final ItemAuction getAuction(int auctionId)
	{
		return _auctions.get(auctionId);
	}
	
	public final ItemAuction[] getAuctionsByBidder(int bidderObjId)
	{
		final Collection<ItemAuction> auctions = getAuctions();
		final ArrayList<ItemAuction> stack = new ArrayList<>(auctions.size());
		for (final ItemAuction auction : getAuctions())
		{
			if (auction.getAuctionState() != ItemAuctionState.CREATED)
			{
				final ItemAuctionBid bid = auction.getBidFor(bidderObjId);
				if (bid != null)
				{
					stack.add(auction);
				}
			}
		}
		return stack.toArray(new ItemAuction[stack.size()]);
	}
	
	public final Collection<ItemAuction> getAuctions()
	{
		final Collection<ItemAuction> auctions;
		
		synchronized (_auctions)
		{
			auctions = _auctions.values();
		}
		
		return auctions;
	}
	
	private class ScheduleAuctionTask implements Runnable
	{
		private final Logger LOG = LoggerFactory.getLogger(ScheduleAuctionTask.class);
		
		private final ItemAuction _auction;
		
		public ScheduleAuctionTask(ItemAuction auction)
		{
			_auction = auction;
		}
		
		@Override
		public final void run()
		{
			try
			{
				runImpl();
			}
			catch (final Exception e)
			{
				LOG.error("Failed scheduling auction ID {}!", _auction.getAuctionId(), e);
			}
		}
		
		private void runImpl() throws Exception
		{
			final ItemAuctionState state = _auction.getAuctionState();
			switch (state)
			{
				case CREATED:
				{
					if (!_auction.setAuctionState(state, ItemAuctionState.STARTED))
					{
						throw new IllegalStateException("Could not set auction state: " + ItemAuctionState.STARTED.toString() + ", expected: " + state.toString());
					}
					
					LOG.info("Auction ID {} has started for NPC ID {}.", _auction.getAuctionId(), _auction.getInstanceId());
					checkAndSetCurrentAndNextAuction();
					break;
				}
				
				case STARTED:
				{
					switch (_auction.getAuctionEndingExtendState())
					{
						case EXTEND_BY_5_MIN:
						{
							if (_auction.getScheduledAuctionEndingExtendState() == ItemAuctionExtendState.INITIAL)
							{
								_auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_5_MIN);
								setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(this, Math.max(_auction.getEndingTime() - System.currentTimeMillis(), 0L)));
								return;
							}
							break;
						}
						
						case EXTEND_BY_3_MIN:
						{
							if (_auction.getScheduledAuctionEndingExtendState() != ItemAuctionExtendState.EXTEND_BY_3_MIN)
							{
								_auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_3_MIN);
								setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(this, Math.max(_auction.getEndingTime() - System.currentTimeMillis(), 0L)));
								return;
							}
							break;
						}
						
						case EXTEND_BY_CONFIG_PHASE_A:
						{
							if (_auction.getScheduledAuctionEndingExtendState() != ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_B)
							{
								_auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_B);
								setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(this, Math.max(_auction.getEndingTime() - System.currentTimeMillis(), 0L)));
								return;
							}
							break;
						}
						
						case EXTEND_BY_CONFIG_PHASE_B:
						{
							if (_auction.getScheduledAuctionEndingExtendState() != ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A)
							{
								_auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A);
								setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(this, Math.max(_auction.getEndingTime() - System.currentTimeMillis(), 0L)));
								return;
							}
						}
					}
					
					if (!_auction.setAuctionState(state, ItemAuctionState.FINISHED))
					{
						throw new IllegalStateException("Could not set auction state: " + ItemAuctionState.FINISHED.toString() + ", expected: " + state.toString());
					}
					
					onAuctionFinished(_auction);
					checkAndSetCurrentAndNextAuction();
					break;
				}
				
				default:
					throw new IllegalStateException("Invalid state: " + state);
			}
		}
	}
	
	final void onAuctionFinished(final ItemAuction auction)
	{
		auction.broadcastToAllBiddersInternal(SystemMessage.getSystemMessage(SystemMessageId.S1_AUCTION_ENDED).addInt(auction.getAuctionId()));
		
		final ItemAuctionBid bid = auction.getHighestBid();
		if (bid != null)
		{
			final L2ItemInstance item = auction.createNewItemInstance();
			final L2PcInstance player = bid.getPlayer();
			if (player != null)
			{
				player.getWarehouse().addItem("ItemAuction", item, null, null);
				player.sendPacket(SystemMessageId.WON_BID_ITEM_CAN_BE_FOUND_IN_WAREHOUSE);
				
				LOG.info("Auction ID {} has finished. Highest bid by {} for instance ID {}.", auction.getAuctionId(), player.getName(), _instanceId);
			}
			else
			{
				item.setOwnerId(bid.getPlayerObjId());
				item.setItemLocation(ItemLocation.WAREHOUSE);
				item.updateDatabase();
				L2World.getInstance().removeObject(item);
				
				final String playerName = CharNameTable.getInstance().getNameById(bid.getPlayerObjId());
				LOG.info("Auction ID {} has finished. Highest bid by {} for instance ID {}.", auction.getAuctionId(), playerName, _instanceId);
			}
			
			// Clean all canceled bids
			auction.clearCanceledBids();
		}
		else
		{
			LOG.info("Auction ID {} has finished. There have not been any bid for instance ID {}.", auction.getAuctionId(), _instanceId);
		}
	}
	
	final void setStateTask(final ScheduledFuture<?> future)
	{
		final ScheduledFuture<?> stateTask = _stateTask;
		if (stateTask != null)
		{
			stateTask.cancel(false);
		}
		
		_stateTask = future;
	}
	
	private final ItemAuction createAuction(final long after)
	{
		final AuctionItem auctionItem = _items.get(Rnd.get(_items.size()));
		final long startingTime = _dateGenerator.nextDate(after);
		final long endingTime = startingTime + TimeUnit.MILLISECONDS.convert(auctionItem.getAuctionLength(), TimeUnit.MINUTES);
		final ItemAuction auction = new ItemAuction(_auctionIds.getAndIncrement(), _instanceId, startingTime, endingTime, auctionItem);
		auction.storeMe();
		return auction;
	}
	
	private final ItemAuction loadAuction(final int auctionId) throws SQLException
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection())
		{
			int auctionItemId = 0;
			long startingTime = 0;
			long endingTime = 0;
			byte auctionStateId = 0;
			try (PreparedStatement ps = con.prepareStatement(SELECT_AUCTION_INFO))
			{
				ps.setInt(1, auctionId);
				try (ResultSet rset = ps.executeQuery())
				{
					if (!rset.next())
					{
						LOG.warn("Auction data not found for auction ID {}!", auctionId);
						return null;
					}
					auctionItemId = rset.getInt(1);
					startingTime = rset.getLong(2);
					endingTime = rset.getLong(3);
					auctionStateId = rset.getByte(4);
				}
			}
			
			if (startingTime >= endingTime)
			{
				LOG.warn("Invalid starting/ending paramaters for auction ID {}!", auctionId);
				return null;
			}
			
			final AuctionItem auctionItem = getAuctionItem(auctionItemId);
			if (auctionItem == null)
			{
				LOG.warn("Auction item ID {} not found for auction ID {}!", auctionItemId, auctionId);
				return null;
			}
			
			final ItemAuctionState auctionState = ItemAuctionState.stateForStateId(auctionStateId);
			if (auctionState == null)
			{
				LOG.warn("Invalid auctionStateId {} for auction ID {}!", auctionStateId, auctionId);
				return null;
			}
			
			if ((auctionState == ItemAuctionState.FINISHED) && (startingTime < (System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(Config.ALT_ITEM_AUCTION_EXPIRED_AFTER, TimeUnit.DAYS))))
			{
				LOG.info("Clearing expired auction ID {}.", auctionId);
				try (PreparedStatement ps = con.prepareStatement(DELETE_AUCTION_INFO_BY_AUCTION_ID))
				{
					ps.setInt(1, auctionId);
					ps.execute();
				}
				
				try (PreparedStatement ps = con.prepareStatement(DELETE_AUCTION_BID_INFO_BY_AUCTION_ID))
				{
					ps.setInt(1, auctionId);
					ps.execute();
				}
				return null;
			}
			
			final List<ItemAuctionBid> auctionBids = new ArrayList<>();
			try (PreparedStatement ps = con.prepareStatement(SELECT_PLAYERS_ID_BY_AUCTION_ID))
			{
				ps.setInt(1, auctionId);
				try (ResultSet rs = ps.executeQuery())
				{
					while (rs.next())
					{
						final int playerObjId = rs.getInt(1);
						final long playerBid = rs.getLong(2);
						final ItemAuctionBid bid = new ItemAuctionBid(playerObjId, playerBid);
						auctionBids.add(bid);
					}
				}
			}
			return new ItemAuction(auctionId, _instanceId, startingTime, endingTime, auctionItem, auctionBids, auctionState);
		}
	}
}