package com.l2jfrozen.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.templates.L2EtcItemType;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.thread.daemons.ItemsAutoDestroy;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class manage all items on ground
 * @version $Revision: $ $Date: $
 * @author  DiezelMax - original ideea
 * @author  Enforcer - actual build
 */
public class ItemsOnGroundManager
{
	static final Logger LOGGER = Logger.getLogger(ItemsOnGroundManager.class);
	private static final String DELETE_ITEMS_ON_GROUND = "DELETE FROM itemsonground";
	private static final String INSERT_ITEMS_ON_GROUND = "INSERT INTO itemsonground(object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable) VALUES(?,?,?,?,?,?,?,?,?)";
	private static final String SELECT_ITEMS_ON_GROUND = "SELECT object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable FROM itemsonground";
	protected List<L2ItemInstance> items = new ArrayList<>();
	
	public ItemsOnGroundManager()
	{
		// If SaveDroppedItem is false, may want to delete all items previously stored to avoid add old items on reactivate
		if (!Config.SAVE_DROPPED_ITEM)
		{
			if (Config.CLEAR_DROPPED_ITEM_TABLE)
			{
				emptyTable();
			}
			
			return;
		}
		
		LOGGER.info("Initializing ItemsOnGroundManager");
		
		items.clear();
		load();
		
		if (!Config.SAVE_DROPPED_ITEM)
		{
			return;
		}
		
		if (Config.SAVE_DROPPED_ITEM_INTERVAL > 0)
		{
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new StoreInDb(), Config.SAVE_DROPPED_ITEM_INTERVAL, Config.SAVE_DROPPED_ITEM_INTERVAL);
		}
	}
	
	public static final ItemsOnGroundManager getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private void load()
	{
		// if DestroyPlayerDroppedItem was previously false, items curently protected will be added to ItemsAutoDestroy
		if (Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			String sql = null;
			if (!Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
			{
				sql = "UPDATE itemsonground SET drop_time=? WHERE drop_time=-1 AND equipable=0";
			}
			else if (Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
			{
				sql = "UPDATE itemsonground SET drop_time=? WHERE drop_time=-1";
			}
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(sql))
			{
				statement.setLong(1, System.currentTimeMillis());
				statement.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("ItemsOnGroundManager.load : Error while updating table itemsonground", e);
			}
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement s = con.prepareStatement(SELECT_ITEMS_ON_GROUND);
			ResultSet result = s.executeQuery())
		{
			int count = 0;
			while (result.next())
			{
				L2ItemInstance item = new L2ItemInstance(result.getInt(1), result.getInt(2));
				L2World.getInstance().storeObject(item);
				
				if (item.isStackable() && result.getInt(3) > 1)
				{
					item.setCount(result.getInt(3));
				}
				
				if (result.getInt(4) > 0)
				{
					item.setEnchantLevel(result.getInt(4));
				}
				
				item.getPosition().setWorldPosition(result.getInt(5), result.getInt(6), result.getInt(7));
				item.getPosition().setWorldRegion(L2World.getInstance().getRegion(item.getPosition().getWorldPosition()));
				item.getPosition().getWorldRegion().addVisibleObject(item);
				item.setDropTime(result.getLong(8));
				
				if (result.getLong(8) == -1)
				{
					item.setProtected(true);
				}
				else
				{
					item.setProtected(false);
				}
				
				item.setIsVisible(true);
				L2World.getInstance().addVisibleObject(item, item.getPosition().getWorldRegion(), null);
				items.add(item);
				count++;
				// add to ItemsAutoDestroy only items not protected
				if (!Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
				{
					if (result.getLong(8) > -1)
					{
						if (Config.AUTODESTROY_ITEM_AFTER > 0 && item.getItemType() != L2EtcItemType.HERB || Config.HERB_AUTO_DESTROY_TIME > 0 && item.getItemType() == L2EtcItemType.HERB)
						{
							ItemsAutoDestroy.getInstance().addItem(item);
						}
					}
				}
				item = null;
			}
			
			if (count > 0)
			{
				LOGGER.info("ItemsOnGroundManager: restored " + count + " items.");
			}
		}
		catch (Exception e)
		{
			LOGGER.error("error while loading ItemsOnGround", e);
		}
		
		if (Config.EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD)
		{
			emptyTable();
		}
	}
	
	public void save(final L2ItemInstance item)
	{
		if (!Config.SAVE_DROPPED_ITEM)
		{
			return;
		}
		
		items.add(item);
	}
	
	public void removeObject(final L2Object item)
	{
		if (!Config.SAVE_DROPPED_ITEM)
		{
			return;
		}
		
		items.remove(item);
	}
	
	public void saveInDb()
	{
		if (!Config.SAVE_DROPPED_ITEM)
		{
			return;
		}
		
		ThreadPoolManager.getInstance().executeTask(new StoreInDb());
	}
	
	public void cleanUp()
	{
		items.clear();
	}
	
	public void emptyTable()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement del = con.prepareStatement(DELETE_ITEMS_ON_GROUND))
		{
			del.executeUpdate();
		}
		catch (final Exception e)
		{
			LOGGER.error("ItemsOnGroundManager.emptyTable : Could not delete items in database", e);
		}
	}
	
	protected class StoreInDb extends Thread
	{
		@Override
		public void run()
		{
			emptyTable();
			
			if (items.isEmpty())
			{
				if (Config.DEBUG)
				{
					LOGGER.warn("ItemsOnGroundManager: nothing to save...");
				}
				return;
			}
			
			int counter = 0;
			int batchSize = 500; // Prevents OutOfMemoryError
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(INSERT_ITEMS_ON_GROUND))
			{
				for (final L2ItemInstance item : items)
				{
					if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
					{
						continue; // Cursed Items not saved to ground, prevent double save
					}
					
					statement.setInt(1, item.getObjectId());
					statement.setInt(2, item.getItemId());
					statement.setInt(3, item.getCount());
					statement.setInt(4, item.getEnchantLevel());
					statement.setInt(5, item.getX());
					statement.setInt(6, item.getY());
					statement.setInt(7, item.getZ());
					
					if (item.isProtected())
					{
						statement.setLong(8, -1); // item will be protected
					}
					else
					{
						statement.setLong(8, item.getDropTime()); // item will be added to ItemsAutoDestroy
					}
					
					if (item.isEquipable())
					{
						statement.setLong(9, 1); // set equipable
					}
					else
					{
						statement.setLong(9, 0);
					}
					
					statement.addBatch();
					counter++;
					
					if (counter % batchSize == 0)
					{
						statement.executeBatch();
					}
				}
				
				if (counter > 0)
				{
					statement.executeBatch();
				}
			}
			catch (Exception e)
			{
				LOGGER.error("StoreInDb.run : Error while inserting into table ItemsOnGround " + e);
			}
			
			if (Config.DEBUG)
			{
				LOGGER.info("ItemsOnGroundManager: " + items.size() + " items on ground saved");
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final ItemsOnGroundManager instance = new ItemsOnGroundManager();
	}
}
