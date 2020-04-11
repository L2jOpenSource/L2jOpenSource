package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.L2TradeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class manages buylists from database
 * @version $Revision: 1.5.4.13 $ $Date: 2005/04/06 16:13:38 $
 */
public class TradeListTable
{
	private static final Logger LOGGER = Logger.getLogger(TradeListTable.class);
	private static TradeListTable instance;
	
	private static final String UPDATE_MERCHANT_BUY_LIST_BY_TIME = "UPDATE merchant_buylists SET savetimer=? WHERE time=?";
	private static final String UPDATE_MERCHANT_BUY_LIST_BY_ITEM_ID_AND_SHOP_ID = "UPDATE merchant_buylists SET currentCount=? WHERE item_id=? AND shop_id=?";
	
	private int nextListId;
	private final Map<Integer, L2TradeList> tradeLists;
	
	/** Task launching the function for restore count of Item (Clan Hall) */
	private class RestoreCount implements Runnable
	{
		private final int timer;
		
		public RestoreCount(final int time)
		{
			timer = time;
		}
		
		@Override
		public void run()
		{
			restoreCount(timer);
			dataTimerSave(timer);
			ThreadPoolManager.getInstance().scheduleGeneral(new RestoreCount(timer), (long) timer * 60 * 60 * 1000);
		}
	}
	
	public static TradeListTable getInstance()
	{
		if (instance == null)
		{
			instance = new TradeListTable();
		}
		
		return instance;
	}
	
	private TradeListTable()
	{
		tradeLists = new HashMap<>();
		load();
	}
	
	private void load(final boolean custom)
	{
		Connection con = null;
		/*
		 * Initialize Shop buylist
		 */
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement1 = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
			{
				"shop_id",
				"npc_id"
			}) + " FROM " + (custom ? "custom_merchant_shopids" : "merchant_shopids"));
			final ResultSet rset1 = statement1.executeQuery();
			
			while (rset1.next())
			{
				final PreparedStatement statement = con.prepareStatement("SELECT item_id, price, shop_id, order, count, time, currentCount FROM " + (custom ? "custom_merchant_buylists" : "merchant_buylists") + " WHERE shop_id=? ORDER BY order ASC");
				statement.setString(1, String.valueOf(rset1.getInt("shop_id")));
				final ResultSet rset = statement.executeQuery();
				
				final L2TradeList buylist = new L2TradeList(rset1.getInt("shop_id"));
				
				buylist.setNpcId(rset1.getString("npc_id"));
				int itemId = 0;
				int itemCount = 0;
				int price = 0;
				
				if (!buylist.isGm() && NpcTable.getInstance().getTemplate(rset1.getInt("npc_id")) == null)
				{
					LOGGER.warn("TradeListTable: Merchant id {} with {} buylist {} not exist. " + rset1.getString("npc_id") + " " + buylist.getListId());
				}
				
				try
				{
					while (rset.next())
					{
						itemId = rset.getInt("item_id");
						price = rset.getInt("price");
						final int count = rset.getInt("count");
						final int currentCount = rset.getInt("currentCount");
						final int time = rset.getInt("time");
						
						final L2ItemInstance buyItem = ItemTable.getInstance().createDummyItem(itemId);
						
						if (buyItem == null)
						{
							continue;
						}
						
						itemCount++;
						
						if (count > -1)
						{
							buyItem.setCountDecrease(true);
						}
						buyItem.setPriceToSell(price);
						buyItem.setTime(time);
						buyItem.setInitCount(count);
						
						if (currentCount > -1)
						{
							buyItem.setCount(currentCount);
						}
						else
						{
							buyItem.setCount(count);
						}
						
						buylist.addItem(buyItem);
						
						if (!buylist.isGm() && buyItem.getReferencePrice() > price)
						{
							LOGGER.warn("TradeListTable: Reference price of item {} in buylist {} higher then sell price. " + itemId + " " + buylist.getListId());
						}
					}
				}
				catch (final Exception e)
				{
					LOGGER.error("TradeListTable: Problem with buylist {}. " + buylist.getListId(), e);
				}
				
				if (itemCount > 0)
				{
					tradeLists.put(buylist.getListId(), buylist);
					nextListId = Math.max(nextListId, buylist.getListId() + 1);
				}
				else
				{
					LOGGER.warn("TradeListTable: Empty buylist {}." + buylist.getListId());
				}
				
				DatabaseUtils.close(statement);
				DatabaseUtils.close(rset);
			}
			rset1.close();
			statement1.close();
			
			LOGGER.info("TradeListTable: Loaded {} Buylists. " + tradeLists.size());
			/*
			 * Restore Task for reinitialize count of buy item
			 */
			try
			{
				int time = 0;
				long savetimer = 0;
				final long currentMillis = System.currentTimeMillis();
				final PreparedStatement statement2 = con.prepareStatement("SELECT DISTINCT time, savetimer FROM " + (custom ? "merchant_buylists" : "merchant_buylists") + " WHERE time <> 0 ORDER BY time");
				final ResultSet rset2 = statement2.executeQuery();
				
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
			}
			catch (final Exception e)
			{
				LOGGER.error("TradeController: Could not restore Timer for Item count. ", e);
			}
		}
		catch (final Exception e)
		{
			// problem with initializing buylists, go to next one
			LOGGER.error("TradeListTable: Buylists could not be initialized. ", e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	public void load()
	{
		load(false); // not custom
		load(true); // custom
	}
	
	public void reloadAll()
	{
		tradeLists.clear();
		
		load();
	}
	
	public L2TradeList getBuyList(final int listId)
	{
		if (tradeLists.containsKey(listId))
		{
			return tradeLists.get(listId);
		}
		
		return null;
	}
	
	public List<L2TradeList> getBuyListByNpcId(int npcId)
	{
		List<L2TradeList> lists = new ArrayList<>();
		
		for (L2TradeList list : tradeLists.values())
		{
			if (list.isGm())
			{
				continue;
			}
			
			/** if (npcId == list.getNpcId()) **/
			lists.add(list);
		}
		
		return lists;
	}
	
	protected void restoreCount(final int time)
	{
		if (tradeLists == null)
		{
			return;
		}
		
		for (final L2TradeList list : tradeLists.values())
		{
			list.restoreCount(time);
		}
	}
	
	protected void dataTimerSave(int time)
	{
		long timerSave = System.currentTimeMillis() + (long) time * 3600000; // 60*60*1000
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_MERCHANT_BUY_LIST_BY_TIME);)
		{
			statement.setLong(1, timerSave);
			statement.setInt(2, time);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("TradeListTable.dataTimerSave : Could not update Timer save in Buylist ", e);
		}
	}
	
	public void dataCountStore()
	{
		if (tradeLists == null)
		{
			return;
		}
		
		int listId;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_MERCHANT_BUY_LIST_BY_ITEM_ID_AND_SHOP_ID))
		{
			for (final L2TradeList list : tradeLists.values())
			{
				if (list == null)
				{
					continue;
				}
				
				listId = list.getListId();
				
				for (L2ItemInstance Item : list.getItems())
				{
					if (Item.getCount() < Item.getInitCount()) // needed?
					{
						statement.setInt(1, Item.getCount());
						statement.setInt(2, Item.getItemId());
						statement.setInt(3, listId);
						statement.executeUpdate();
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("TradeController: Could not store Count Item ", e);
		}
	}
}
