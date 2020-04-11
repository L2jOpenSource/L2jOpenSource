package com.l2jfrozen.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public class L2TradeList
{
	private static Logger LOGGER = Logger.getLogger(L2TradeList.class);
	
	private final List<L2ItemInstance> items;
	private final int listId;
	private boolean confirmed;
	private boolean gm;
	private String buyStoreName, sellStoreName;
	
	private String npcId;
	
	public L2TradeList(final int listId)
	{
		items = new ArrayList<>();
		this.listId = listId;
		confirmed = false;
	}
	
	public void setNpcId(final String id)
	{
		npcId = id;
		
		if (id.equals("gm"))
		{
			gm = true;
		}
		else
		{
			gm = false;
		}
		
	}
	
	public String getNpcId()
	{
		return npcId;
	}
	
	public void addItem(final L2ItemInstance item)
	{
		items.add(item);
	}
	
	public void replaceItem(final int itemID, int price)
	{
		for (int i = 0; i < items.size(); i++)
		{
			L2ItemInstance item = items.get(i);
			
			if (item.getItemId() == itemID)
			{
				
				if (price < (item.getReferencePrice() / 2))
				{
					
					LOGGER.warn("L2TradeList " + getListId() + " itemId  " + itemID + " has an ADENA sell price lower then reference price.. Automatically Updating it..");
					price = item.getReferencePrice();
				}
				item.setPriceToSell(price);
			}
			
			item = null;
		}
	}
	
	public synchronized boolean decreaseCount(final int itemID, final int count)
	{
		
		for (int i = 0; i < items.size(); i++)
		{
			L2ItemInstance item = items.get(i);
			
			if (item.getItemId() == itemID)
			{
				if (item.getCount() < count)
				{
					return false;
				}
				item.setCount(item.getCount() - count);
			}
			
			item = null;
		}
		return true;
	}
	
	public void restoreCount(final int time)
	{
		for (int i = 0; i < items.size(); i++)
		{
			L2ItemInstance item = items.get(i);
			
			if (item.getCountDecrease() && item.getTime() == time)
			{
				item.restoreInitCount();
			}
			
			item = null;
		}
	}
	
	public void removeItem(final int itemID)
	{
		for (int i = 0; i < items.size(); i++)
		{
			L2ItemInstance item = items.get(i);
			
			if (item.getItemId() == itemID)
			{
				items.remove(i);
			}
			
			item = null;
		}
	}
	
	/**
	 * @return Returns the listId.
	 */
	public int getListId()
	{
		return listId;
	}
	
	public void setSellStoreName(final String name)
	{
		sellStoreName = name;
	}
	
	public String getSellStoreName()
	{
		return sellStoreName;
	}
	
	public void setBuyStoreName(final String name)
	{
		buyStoreName = name;
	}
	
	public String getBuyStoreName()
	{
		return buyStoreName;
	}
	
	/**
	 * @return Returns the items.
	 */
	public List<L2ItemInstance> getItems()
	{
		return items;
	}
	
	public List<L2ItemInstance> getItems(final int start, final int end)
	{
		return items.subList(start, end);
	}
	
	public int getPriceForItemId(final int itemId)
	{
		for (int i = 0; i < items.size(); i++)
		{
			L2ItemInstance item = items.get(i);
			
			if (item.getItemId() == itemId)
			{
				return item.getPriceToSell();
			}
			
			item = null;
		}
		return -1;
	}
	
	public boolean countDecrease(final int itemId)
	{
		for (int i = 0; i < items.size(); i++)
		{
			L2ItemInstance item = items.get(i);
			
			if (item.getItemId() == itemId)
			{
				return item.getCountDecrease();
			}
			
			item = null;
		}
		return false;
	}
	
	public boolean containsItemId(final int itemId)
	{
		for (L2ItemInstance item : items)
		{
			if (item.getItemId() == itemId)
			{
				return true;
			}
			
			item = null;
		}
		
		return false;
	}
	
	public L2ItemInstance getItem(final int ObjectId)
	{
		for (int i = 0; i < items.size(); i++)
		{
			L2ItemInstance item = items.get(i);
			
			if (item.getObjectId() == ObjectId)
			{
				return item;
			}
			
			item = null;
		}
		return null;
	}
	
	public synchronized void setConfirmedTrade(final boolean x)
	{
		confirmed = x;
	}
	
	public synchronized boolean hasConfirmed()
	{
		return confirmed;
	}
	
	public void removeItem(final int objId, final int count)
	{
		L2ItemInstance temp;
		
		for (int y = 0; y < items.size(); y++)
		{
			temp = items.get(y);
			
			if (temp.getObjectId() == objId)
			{
				if (count == temp.getCount())
				{
					items.remove(temp);
				}
				
				break;
			}
		}
		
		temp = null;
	}
	
	public boolean contains(final int objId)
	{
		boolean bool = false;
		
		L2ItemInstance temp;
		
		for (int y = 0; y < items.size(); y++)
		{
			temp = items.get(y);
			
			if (temp.getObjectId() == objId)
			{
				bool = true;
				break;
			}
		}
		
		temp = null;
		
		return bool;
	}
	
	public void updateBuyList(final L2PcInstance player, final List<TradeItem> list)
	{
		TradeItem temp;
		int count;
		Inventory playersInv = player.getInventory();
		L2ItemInstance temp2;
		count = 0;
		
		while (count != list.size())
		{
			temp = list.get(count);
			temp2 = playersInv.getItemByItemId(temp.getItemId());
			
			if (temp2 == null)
			{
				list.remove(count);
				count = count - 1;
			}
			else
			{
				if (temp.getCount() == 0)
				{
					list.remove(count);
					count = count - 1;
				}
			}
			count++;
		}
		
		temp = null;
		playersInv = null;
		temp2 = null;
		
	}
	
	public void updateSellList(final L2PcInstance player, final List<TradeItem> list)
	{
		Inventory playersInv = player.getInventory();
		TradeItem temp;
		L2ItemInstance temp2;
		
		int count = 0;
		while (count != list.size())
		{
			temp = list.get(count);
			temp2 = playersInv.getItemByObjectId(temp.getObjectId());
			
			if (temp2 == null)
			{
				list.remove(count);
				count = count - 1;
			}
			else
			{
				if (temp2.getCount() < temp.getCount())
				{
					temp.setCount(temp2.getCount());
				}
				
			}
			count++;
		}
		
		playersInv = null;
		temp = null;
		temp2 = null;
		
	}
	
	public boolean isGm()
	{
		return gm;
	}
	
}
