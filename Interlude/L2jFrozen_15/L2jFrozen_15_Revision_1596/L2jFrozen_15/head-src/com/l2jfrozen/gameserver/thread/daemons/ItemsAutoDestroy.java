package com.l2jfrozen.gameserver.thread.daemons;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.managers.ItemsOnGroundManager;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.templates.L2EtcItemType;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

public class ItemsAutoDestroy
{
	protected static final Logger LOGGER = Logger.getLogger("ItemsAutoDestroy");
	private static ItemsAutoDestroy instance;
	protected List<L2ItemInstance> items = null;
	protected static long sleep;
	
	private ItemsAutoDestroy()
	{
		items = new ArrayList<>();
		sleep = Config.AUTODESTROY_ITEM_AFTER * 1000;
		
		if (sleep == 0)
		{
			sleep = 3600000;
		}
		
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckItemsForDestroy(), 5000, 5000);
	}
	
	public static ItemsAutoDestroy getInstance()
	{
		if (instance == null)
		{
			LOGGER.info("Initializing ItemsAutoDestroy.");
			instance = new ItemsAutoDestroy();
		}
		return instance;
	}
	
	public synchronized void addItem(final L2ItemInstance item)
	{
		item.setDropTime(System.currentTimeMillis());
		items.add(item);
	}
	
	public synchronized void removeItems()
	{
		if (Config.DEBUG)
		{
			LOGGER.info("[ItemsAutoDestroy] : " + items.size() + " items to check.");
		}
		
		if (items.isEmpty())
		{
			return;
		}
		
		final long curtime = System.currentTimeMillis();
		
		for (int i = 0; i < items.size(); i++)
		{
			L2ItemInstance item = items.get(i);
			if (item == null || item.getDropTime() == 0 || item.getLocation() != L2ItemInstance.ItemLocation.VOID)
			{
				items.remove(item);
			}
			else
			{
				if (item.getItemType() == L2EtcItemType.HERB)
				{
					if (curtime - item.getDropTime() > Config.HERB_AUTO_DESTROY_TIME)
					{
						L2World.getInstance().removeVisibleObject(item, item.getWorldRegion());
						L2World.getInstance().removeObject(item);
						items.remove(item);
						
						if (Config.SAVE_DROPPED_ITEM)
						{
							ItemsOnGroundManager.getInstance().removeObject(item);
						}
					}
				}
				else if (curtime - item.getDropTime() > sleep)
				{
					L2World.getInstance().removeVisibleObject(item, item.getWorldRegion());
					L2World.getInstance().removeObject(item);
					items.remove(item);
					
					if (Config.SAVE_DROPPED_ITEM)
					{
						ItemsOnGroundManager.getInstance().removeObject(item);
					}
				}
			}
		}
		
		if (Config.DEBUG)
		{
			LOGGER.info("[ItemsAutoDestroy] : " + items.size() + " items remaining.");
		}
	}
	
	protected class CheckItemsForDestroy extends Thread
	{
		@Override
		public void run()
		{
			removeItems();
		}
	}
}
