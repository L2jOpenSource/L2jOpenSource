package com.l2jfrozen.gameserver.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.model.L2TradeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * @version $Revision: 1.5.4.13 $ $Date: 2005/04/06 16:13:38 $
 */
public class TradeController
{
	private static Logger LOGGER = Logger.getLogger(TradeController.class);
	private static final String SELECT_MERCHANT_SHOP_IDS = "SELECT shop_id, npc_id FROM merchant_shopids";
	private static final String SELECT_CUSTOM_MERCHANT_SHOP_IDS = "SELECT shop_id,npc_id FROM custom_merchant_shopids";
	
	private static final String SELECT_MERCHANT_BUY_LIST = "SELECT item_id,price,shop_id,`order`,count,`time`,currentCount FROM merchant_buylists WHERE shop_id=? ORDER BY `order` ASC";
	private static final String SELECT_CUSTOM_MERCHANT_BUY_LIST = "SELECT item_id,price,shop_id,`order`,count,`time`,currentCount FROM custom_merchant_buylists WHERE shop_id=? ORDER BY `order` ASC";
	
	private static final String UPDATE_MERCHANT_BUY_LIST_BY_TIMER = "UPDATE merchant_buylists SET savetimer =? WHERE time=? ";
	private static final String UPDATE_MERCHANT_BUY_LIST_BY_SHOP_ID = "UPDATE merchant_buylists SET currentCount=? WHERE item_id=? AND shop_id=? ";
	
	private static TradeController instance;
	
	private int nextListId;
	private final Map<Integer, L2TradeList> tradeLists;
	private final Map<Integer, L2TradeList> listsTaskItem;
	
	/** Task launching the function for restore count of Item (Clan Hall) */
	public class RestoreCount implements Runnable
	{
		private final int timer;
		
		public RestoreCount(final int time)
		{
			timer = time;
		}
		
		@Override
		public void run()
		{
			try
			{
				restoreCount(timer);
				dataTimerSave(timer);
				ThreadPoolManager.getInstance().scheduleGeneral(new RestoreCount(timer), (long) timer * 60 * 60 * 1000);
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
	
	public static TradeController getInstance()
	{
		if (instance == null)
		{
			instance = new TradeController();
		}
		
		return instance;
	}
	
	private TradeController()
	{
		tradeLists = new HashMap<>();
		listsTaskItem = new HashMap<>();
		
		int dummyItemCount = 0;
		boolean LimitedItem = false;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement pstSelect = con.prepareStatement(SELECT_MERCHANT_SHOP_IDS))
		{
			ResultSet rset1 = pstSelect.executeQuery();
			
			while (rset1.next())
			{
				PreparedStatement statement = con.prepareStatement(SELECT_MERCHANT_BUY_LIST);
				statement.setString(1, String.valueOf(rset1.getInt("shop_id")));
				ResultSet rset = statement.executeQuery();
				if (rset.next())
				{
					LimitedItem = false;
					dummyItemCount++;
					L2TradeList buy1 = new L2TradeList(rset1.getInt("shop_id"));
					
					int itemId = rset.getInt("item_id");
					int price = rset.getInt("price");
					int count = rset.getInt("count");
					int currentCount = rset.getInt("currentCount");
					int time = rset.getInt("time");
					
					L2ItemInstance item = ItemTable.getInstance().createDummyItem(itemId);
					
					if (item == null)
					{
						DatabaseUtils.close(rset);
						DatabaseUtils.close(statement);
						
						rset = null;
						statement = null;
						continue;
					}
					
					if (count > -1)
					{
						item.setCountDecrease(true);
						LimitedItem = true;
					}
					
					if (!rset1.getString("npc_id").equals("gm") && price < (item.getReferencePrice() / 2))
					{
						
						LOGGER.warn("L2TradeList " + buy1.getListId() + " itemId  " + itemId + " has an ADENA sell price lower then reference price.. Automatically Updating it..");
						price = item.getReferencePrice();
					}
					
					item.setPriceToSell(price);
					item.setTime(time);
					item.setInitCount(count);
					
					if (currentCount > -1)
					{
						item.setCount(currentCount);
					}
					else
					{
						item.setCount(count);
					}
					
					buy1.addItem(item);
					item = null;
					buy1.setNpcId(rset1.getString("npc_id"));
					
					try
					{
						while (rset.next()) // TODO aici
						{
							dummyItemCount++;
							itemId = rset.getInt("item_id");
							price = rset.getInt("price");
							count = rset.getInt("count");
							time = rset.getInt("time");
							currentCount = rset.getInt("currentCount");
							final L2ItemInstance item2 = ItemTable.getInstance().createDummyItem(itemId);
							
							if (item2 == null)
							{
								continue;
							}
							
							if (count > -1)
							{
								item2.setCountDecrease(true);
								LimitedItem = true;
							}
							
							if (!rset1.getString("npc_id").equals("gm") && price < item2.getReferencePrice() / 2)
							{
								
								LOGGER.warn("L2TradeList " + buy1.getListId() + " itemId  " + itemId + " has an ADENA sell price lower then reference price.. Automatically Updating it..");
								price = item2.getReferencePrice();
							}
							
							item2.setPriceToSell(price);
							
							item2.setTime(time);
							item2.setInitCount(count);
							if (currentCount > -1)
							{
								item2.setCount(currentCount);
							}
							else
							{
								item2.setCount(count);
							}
							buy1.addItem(item2);
						}
					}
					catch (final Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e.printStackTrace();
						}
						
						LOGGER.warn("TradeController: Problem with buylist " + buy1.getListId() + " item " + itemId);
					}
					if (LimitedItem)
					{
						listsTaskItem.put(buy1.getListId(), buy1);
					}
					else
					{
						tradeLists.put(buy1.getListId(), buy1);
					}
					
					nextListId = Math.max(nextListId, buy1.getListId() + 1);
					buy1 = null;
				}
				
				DatabaseUtils.close(rset);
				DatabaseUtils.close(statement);
				
				rset = null;
				statement = null;
			}
			rset1.close();
			
			rset1 = null;
			
			if (Config.DEBUG)
			{
				LOGGER.debug("created " + dummyItemCount + " Dummy-Items for buylists");
			}
			
			LOGGER.info("TradeController: Loaded " + tradeLists.size() + " Buylists.");
			LOGGER.info("TradeController: Loaded " + listsTaskItem.size() + " Limited Buylists.");
			/*
			 * Restore Task for reinitialyze count of buy item
			 */
			try
			{
				int time = 0;
				long savetimer = 0;
				final long currentMillis = System.currentTimeMillis();
				
				PreparedStatement statement2 = con.prepareStatement("SELECT DISTINCT time, savetimer FROM merchant_buylists WHERE time <> 0 ORDER BY time");
				ResultSet rset2 = statement2.executeQuery();
				
				while (rset2.next())
				{
					time = rset2.getInt("time");
					savetimer = rset2.getLong("savetimer");
					if (savetimer - currentMillis > 0)
					{
						ThreadPoolManager.getInstance().scheduleGeneral(new RestoreCount(time), savetimer - System.currentTimeMillis());
					}
					else
					{
						ThreadPoolManager.getInstance().scheduleGeneral(new RestoreCount(time), 0);
					}
				}
				rset2.close();
				statement2.close();
				
				rset2 = null;
				statement2 = null;
			}
			catch (final Exception e)
			{
				LOGGER.warn("TradeController: Could not restore Timer for Item count.");
				e.printStackTrace();
			}
		}
		catch (final Exception e)
		{
			// problem with initializing spawn, go to next one
			LOGGER.warn("TradeController: Buylists could not be initialized.");
			e.printStackTrace();
		}
		
		/*
		 * If enabled, initialize the custom buylist
		 */
		if (Config.CUSTOM_MERCHANT_TABLES)// Custom merchat Tabels
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement pstSelect = con.prepareStatement(SELECT_CUSTOM_MERCHANT_SHOP_IDS))
			{
				final int initialSize = tradeLists.size();
				
				ResultSet rset1 = pstSelect.executeQuery();
				
				while (rset1.next())
				{
					PreparedStatement statement = con.prepareStatement(SELECT_CUSTOM_MERCHANT_BUY_LIST);
					
					statement.setString(1, String.valueOf(rset1.getInt("shop_id")));
					ResultSet rset = statement.executeQuery();
					
					if (rset.next())
					{
						LimitedItem = false;
						dummyItemCount++;
						L2TradeList buy1 = new L2TradeList(rset1.getInt("shop_id"));
						int itemId = rset.getInt("item_id");
						int price = rset.getInt("price");
						int count = rset.getInt("count");
						int currentCount = rset.getInt("currentCount");
						int time = rset.getInt("time");
						L2ItemInstance item = ItemTable.getInstance().createDummyItem(itemId);
						if (item == null)
						{
							DatabaseUtils.close(rset);
							DatabaseUtils.close(statement);
							
							rset = null;
							statement = null;
							continue;
						}
						
						if (count > -1)
						{
							item.setCountDecrease(true);
							LimitedItem = true;
						}
						
						if (!rset1.getString("npc_id").equals("gm") && price < (item.getReferencePrice() / 2))
						{
							
							LOGGER.warn("L2TradeList " + buy1.getListId() + " itemId  " + itemId + " has an ADENA sell price lower then reference price.. Automatically Updating it..");
							price = item.getReferencePrice();
						}
						
						item.setPriceToSell(price);
						item.setTime(time);
						item.setInitCount(count);
						
						if (currentCount > -1)
						{
							item.setCount(currentCount);
						}
						else
						{
							item.setCount(count);
						}
						
						buy1.addItem(item);
						item = null;
						buy1.setNpcId(rset1.getString("npc_id"));
						
						try
						{
							while (rset.next())
							{
								dummyItemCount++;
								itemId = rset.getInt("item_id");
								price = rset.getInt("price");
								count = rset.getInt("count");
								time = rset.getInt("time");
								currentCount = rset.getInt("currentCount");
								L2ItemInstance item2 = ItemTable.getInstance().createDummyItem(itemId);
								if (item2 == null)
								{
									continue;
								}
								if (count > -1)
								{
									item2.setCountDecrease(true);
									LimitedItem = true;
								}
								
								if (!rset1.getString("npc_id").equals("gm") && price < item2.getReferencePrice() / 2)
								{
									
									LOGGER.warn("L2TradeList " + buy1.getListId() + " itemId  " + itemId + " has an ADENA sell price lower then reference price.. Automatically Updating it..");
									price = item2.getReferencePrice();
								}
								
								item2.setPriceToSell(price);
								item2.setTime(time);
								item2.setInitCount(count);
								if (currentCount > -1)
								{
									item2.setCount(currentCount);
								}
								else
								{
									item2.setCount(count);
								}
								buy1.addItem(item2);
								
								item2 = null;
							}
						}
						catch (final Exception e)
						{
							if (Config.ENABLE_ALL_EXCEPTIONS)
							{
								e.printStackTrace();
							}
							
							LOGGER.warn("TradeController: Problem with buylist " + buy1.getListId() + " item " + itemId);
						}
						if (LimitedItem)
						{
							listsTaskItem.put(buy1.getListId(), buy1);
						}
						else
						{
							tradeLists.put(buy1.getListId(), buy1);
						}
						nextListId = Math.max(nextListId, buy1.getListId() + 1);
						
						buy1 = null;
					}
					
					DatabaseUtils.close(rset);
					DatabaseUtils.close(statement);
					
					rset = null;
					statement = null;
				}
				rset1.close();
				
				rset1 = null;
				
				if (Config.DEBUG)
				{
					LOGGER.debug("created " + dummyItemCount + " Dummy-Items for buylists");
				}
				
				int customBuyListCount = tradeLists.size() - initialSize;
				
				if (customBuyListCount > 0)
				{
					LOGGER.info("TradeController: Loaded " + customBuyListCount + " Custom Buylists.");
				}
				
				/**
				 * Restore Task for reinitialyze count of buy item
				 */
				try
				{
					int time = 0;
					long savetimer = 0;
					final long currentMillis = System.currentTimeMillis();
					
					PreparedStatement statement2 = con.prepareStatement("SELECT DISTINCT time, savetimer FROM custom_merchant_buylists WHERE time <> 0 ORDER BY time");
					ResultSet rset2 = statement2.executeQuery();
					
					while (rset2.next())
					{
						time = rset2.getInt("time");
						savetimer = rset2.getLong("savetimer");
						if (savetimer - currentMillis > 0)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new RestoreCount(time), savetimer - System.currentTimeMillis());
						}
						else
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new RestoreCount(time), 0);
						}
					}
					rset2.close();
					statement2.close();
					
					rset2 = null;
					statement2 = null;
					
				}
				catch (final Exception e)
				{
					LOGGER.warn("TradeController: Could not restore Timer for Item count.");
					e.printStackTrace();
				}
			}
			catch (final Exception e)
			{
				// problem with initializing spawn, go to next one
				LOGGER.error("TradeController: Buylists could not be initialized", e);
			}
		}
	}
	
	public L2TradeList getBuyList(final int listId)
	{
		if (tradeLists.get(listId) != null)
		{
			return tradeLists.get(listId);
		}
		
		return listsTaskItem.get(listId);
	}
	
	public List<L2TradeList> getBuyListByNpcId(final int npcId)
	{
		final List<L2TradeList> lists = new ArrayList<>();
		
		for (final L2TradeList list : tradeLists.values())
		{
			if (list.getNpcId().startsWith("gm"))
			{
				continue;
			}
			
			if (npcId == Integer.parseInt(list.getNpcId()))
			{
				lists.add(list);
			}
		}
		for (final L2TradeList list : listsTaskItem.values())
		{
			if (list.getNpcId().startsWith("gm"))
			{
				continue;
			}
			
			if (npcId == Integer.parseInt(list.getNpcId()))
			{
				lists.add(list);
			}
		}
		return lists;
	}
	
	protected void restoreCount(final int time)
	{
		if (listsTaskItem == null)
		{
			return;
		}
		
		for (final L2TradeList list : listsTaskItem.values())
		{
			list.restoreCount(time);
		}
	}
	
	protected void dataTimerSave(long time)
	{
		final long timerSave = System.currentTimeMillis() + (time * 60L * 60L * 1000);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_MERCHANT_BUY_LIST_BY_TIMER))
		{
			statement.setLong(1, timerSave);
			statement.setLong(2, time);
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			LOGGER.error("TradeController: Could not update Timer save in Buylist", e);
		}
	}
	
	public void dataCountStore()
	{
		if (listsTaskItem == null)
		{
			return;
		}
		
		int listId;
		int counter = 0;
		int batchSize = 500; // Prevent out of memory
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_MERCHANT_BUY_LIST_BY_SHOP_ID))
		{
			for (L2TradeList list : listsTaskItem.values())
			{
				if (list == null)
				{
					continue;
				}
				
				listId = list.getListId();
				
				for (L2ItemInstance item : list.getItems())
				{
					if (item.getCount() < item.getInitCount()) // needed?
					{
						statement.setInt(1, item.getCount());
						statement.setInt(2, item.getItemId());
						statement.setInt(3, listId);
						statement.addBatch();
						counter++;
					}
					
					if (counter % batchSize == 0)
					{
						statement.executeBatch();
					}
				}
				statement.executeBatch();
			}
		}
		catch (Exception e)
		{
			LOGGER.error("TradeController.dataCountStore: Could not store Count Item", e);
		}
	}
	
	public synchronized int getNextId()
	{
		return nextListId++;
	}
	
	/**
	 * This will reload buylists info from DataBase
	 */
	public static void reload()
	{
		instance = new TradeController();
	}
}
