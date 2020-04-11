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
package l2r.gameserver.datatables.xml;

import static l2r.gameserver.model.itemcontainer.Inventory.ADENA_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.engines.DocumentEngine;
import l2r.gameserver.enums.ItemLocation;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.L2Armor;
import l2r.gameserver.model.items.L2EtcItem;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.items.L2Weapon;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.scripting.scriptengine.events.ItemCreateEvent;
import l2r.gameserver.scripting.scriptengine.listeners.player.NewItemListener;
import l2r.gameserver.util.GMAudit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class serves as a container for all item templates in the game.
 */
public class ItemData
{
	private static Logger _log = LoggerFactory.getLogger(ItemData.class);
	private static java.util.logging.Logger _logItems = java.util.logging.Logger.getLogger("item");
	
	private static FastList<NewItemListener> newItemListeners = new FastList<NewItemListener>().shared();
	
	public static final Map<String, Integer> _slots = new FastMap<>();
	
	private L2Item[] _allTemplates;
	private final Map<Integer, L2EtcItem> _etcItems;
	private final Map<Integer, L2Armor> _armors;
	private final Map<Integer, L2Weapon> _weapons;
	
	static
	{
		_slots.put("shirt", L2Item.SLOT_UNDERWEAR);
		_slots.put("lbracelet", L2Item.SLOT_L_BRACELET);
		_slots.put("rbracelet", L2Item.SLOT_R_BRACELET);
		_slots.put("talisman", L2Item.SLOT_DECO);
		_slots.put("chest", L2Item.SLOT_CHEST);
		_slots.put("fullarmor", L2Item.SLOT_FULL_ARMOR);
		_slots.put("head", L2Item.SLOT_HEAD);
		_slots.put("hair", L2Item.SLOT_HAIR);
		_slots.put("hairall", L2Item.SLOT_HAIRALL);
		_slots.put("underwear", L2Item.SLOT_UNDERWEAR);
		_slots.put("back", L2Item.SLOT_BACK);
		_slots.put("neck", L2Item.SLOT_NECK);
		_slots.put("legs", L2Item.SLOT_LEGS);
		_slots.put("feet", L2Item.SLOT_FEET);
		_slots.put("gloves", L2Item.SLOT_GLOVES);
		_slots.put("chest,legs", L2Item.SLOT_CHEST | L2Item.SLOT_LEGS);
		_slots.put("belt", L2Item.SLOT_BELT);
		_slots.put("rhand", L2Item.SLOT_R_HAND);
		_slots.put("lhand", L2Item.SLOT_L_HAND);
		_slots.put("lrhand", L2Item.SLOT_LR_HAND);
		_slots.put("rear;lear", L2Item.SLOT_R_EAR | L2Item.SLOT_L_EAR);
		_slots.put("rfinger;lfinger", L2Item.SLOT_R_FINGER | L2Item.SLOT_L_FINGER);
		_slots.put("wolf", L2Item.SLOT_WOLF);
		_slots.put("greatwolf", L2Item.SLOT_GREATWOLF);
		_slots.put("hatchling", L2Item.SLOT_HATCHLING);
		_slots.put("strider", L2Item.SLOT_STRIDER);
		_slots.put("babypet", L2Item.SLOT_BABYPET);
		_slots.put("none", L2Item.SLOT_NONE);
		
		// retail compatibility
		_slots.put("onepiece", L2Item.SLOT_FULL_ARMOR);
		_slots.put("hair2", L2Item.SLOT_HAIR2);
		_slots.put("dhair", L2Item.SLOT_HAIRALL);
		_slots.put("alldress", L2Item.SLOT_ALLDRESS);
		_slots.put("deco1", L2Item.SLOT_DECO);
		_slots.put("waist", L2Item.SLOT_BELT);
	}
	
	/**
	 * @return a reference to this ItemTable object
	 */
	public static ItemData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected ItemData()
	{
		_etcItems = new FastMap<>();
		_armors = new FastMap<>();
		_weapons = new FastMap<>();
		load();
	}
	
	private void load()
	{
		int highest = 0;
		_armors.clear();
		_etcItems.clear();
		_weapons.clear();
		for (L2Item item : DocumentEngine.getInstance().loadItems())
		{
			if (highest < item.getId())
			{
				highest = item.getId();
			}
			if (item instanceof L2EtcItem)
			{
				_etcItems.put(item.getId(), (L2EtcItem) item);
			}
			else if (item instanceof L2Armor)
			{
				_armors.put(item.getId(), (L2Armor) item);
			}
			else
			{
				_weapons.put(item.getId(), (L2Weapon) item);
			}
		}
		buildFastLookupTable(highest);
		_log.info(getClass().getSimpleName() + ": Loaded: " + _etcItems.size() + " Etc Items");
		_log.info(getClass().getSimpleName() + ": Loaded: " + _armors.size() + " Armor Items");
		_log.info(getClass().getSimpleName() + ": Loaded: " + _weapons.size() + " Weapon Items");
		_log.info(getClass().getSimpleName() + ": Loaded: " + (_etcItems.size() + _armors.size() + _weapons.size()) + " Items in total.");
	}
	
	/**
	 * Builds a variable in which all items are putting in in function of their ID.
	 * @param size
	 */
	private void buildFastLookupTable(int size)
	{
		// Create a FastLookUp Table called _allTemplates of size : value of the highest item ID
		_log.info(getClass().getSimpleName() + ": Highest item id used:" + size);
		_allTemplates = new L2Item[size + 1];
		
		// Insert armor item in Fast Look Up Table
		for (L2Armor item : _armors.values())
		{
			_allTemplates[item.getId()] = item;
		}
		
		// Insert weapon item in Fast Look Up Table
		for (L2Weapon item : _weapons.values())
		{
			_allTemplates[item.getId()] = item;
		}
		
		// Insert etcItem item in Fast Look Up Table
		for (L2EtcItem item : _etcItems.values())
		{
			_allTemplates[item.getId()] = item;
		}
	}
	
	/**
	 * Returns the item corresponding to the item ID
	 * @param id : int designating the item
	 * @return L2Item
	 */
	public L2Item getTemplate(int id)
	{
		if ((id >= _allTemplates.length) || (id < 0))
		{
			return null;
		}
		
		return _allTemplates[id];
	}
	
	/**
	 * Create the L2ItemInstance corresponding to the Item Identifier and quantitiy add logs the activity. <B><U> Actions</U> :</B> <li>Create and Init the L2ItemInstance corresponding to the Item Identifier and quantity</li> <li>Add the L2ItemInstance object to _allObjects of L2world</li> <li>Logs
	 * Item creation according to log settings</li>
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item Identifier of the item to be created
	 * @param count : int Quantity of items to be created for stackable items
	 * @param actor : L2PcInstance Player requesting the item creation
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the new item
	 */
	public L2ItemInstance createItem(String process, int itemId, long count, L2PcInstance actor, Object reference)
	{
		if (!fireNewItemListeners(process, itemId, count, actor, reference))
		{
			return null;
		}
		
		// Create and Init the L2ItemInstance corresponding to the Item Identifier
		L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		
		if (process.equalsIgnoreCase("loot"))
		{
			ScheduledFuture<?> itemLootShedule;
			if ((reference instanceof L2Attackable) && ((L2Attackable) reference).isRaid()) // loot privilege for raids
			{
				L2Attackable raid = (L2Attackable) reference;
				// if in CommandChannel and was killing a World/RaidBoss
				if ((raid.getFirstCommandChannelAttacked() != null) && !Config.AUTO_LOOT_RAIDS)
				{
					item.setOwnerId(raid.getFirstCommandChannelAttacked().getLeaderObjectId());
					itemLootShedule = ThreadPoolManager.getInstance().scheduleGeneral(new ResetOwner(item), Config.LOOT_RAIDS_PRIVILEGE_INTERVAL);
					item.setItemLootShedule(itemLootShedule);
				}
			}
			else if (!Config.AUTO_LOOT)
			{
				item.setOwnerId(actor.getObjectId());
				itemLootShedule = ThreadPoolManager.getInstance().scheduleGeneral(new ResetOwner(item), 15000);
				item.setItemLootShedule(itemLootShedule);
			}
		}
		
		if (Config.DEBUG)
		{
			_log.info(getClass().getSimpleName() + ": Item created  oid:" + item.getObjectId() + " itemid:" + itemId);
		}
		
		// Add the L2ItemInstance object to _allObjects of L2world
		L2World.getInstance().storeObject(item);
		
		// Set Item parameters
		if (item.isStackable() && (count > 1))
		{
			item.setCount(count);
		}
		
		if (Config.LOG_ITEMS && !process.equals("Reset"))
		{
			if (!Config.LOG_ITEMS_SMALL_LOG || (Config.LOG_ITEMS_SMALL_LOG && (item.isEquipable() || (item.getId() == ADENA_ID))))
			{
				LogRecord record = new LogRecord(Level.INFO, "CREATE:" + process);
				record.setLoggerName("item");
				record.setParameters(new Object[]
				{
					item,
					actor,
					reference
				});
				_logItems.log(record);
				
			}
		}
		
		if (actor != null)
		{
			if (actor.isGM())
			{
				String referenceName = "no-reference";
				if (reference instanceof L2Object)
				{
					referenceName = (((L2Object) reference).getName() != null ? ((L2Object) reference).getName() : "no-name");
				}
				else if (reference instanceof String)
				{
					referenceName = (String) reference;
				}
				String targetName = (actor.getTarget() != null ? actor.getTarget().getName() : "no-target");
				if (Config.GMAUDIT)
				{
					GMAudit.auditGMAction(actor.getName() + " [" + actor.getObjectId() + "]", process + "(id: " + itemId + " count: " + count + " name: " + item.getItemName() + " objId: " + item.getObjectId() + ")", targetName, "L2Object referencing this action is: " + referenceName);
				}
			}
		}
		
		return item;
	}
	
	public L2ItemInstance createItem(String process, int itemId, int count, L2PcInstance actor)
	{
		return createItem(process, itemId, count, actor, null);
	}
	
	/**
	 * Returns a dummy item.<br>
	 * <U><I>Concept :</I></U><BR>
	 * Dummy item is created by setting the ID of the object in the world at null value
	 * @param itemId : int designating the item
	 * @return L2ItemInstance designating the dummy item created
	 */
	public L2ItemInstance createDummyItem(int itemId)
	{
		L2Item item = getTemplate(itemId);
		if (item == null)
		{
			return null;
		}
		L2ItemInstance temp = new L2ItemInstance(0, item);
		return temp;
	}
	
	/**
	 * Destroys the L2ItemInstance.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Sets L2ItemInstance parameters to be unusable</li>
	 * <li>Removes the L2ItemInstance object to _allObjects of L2world</li>
	 * <li>Logs Item deletion according to log settings</li>
	 * </ul>
	 * @param process a string identifier of process triggering this action.
	 * @param item the item instance to be destroyed.
	 * @param actor the player requesting the item destroy.
	 * @param reference the object referencing current action like NPC selling item or previous item in transformation.
	 */
	public void destroyItem(String process, L2ItemInstance item, L2PcInstance actor, Object reference)
	{
		synchronized (item)
		{
			long old = item.getCount();
			item.setCount(0);
			item.setOwnerId(0);
			item.setItemLocation(ItemLocation.VOID);
			item.setLastChange(L2ItemInstance.REMOVED);
			
			L2World.getInstance().removeObject(item);
			IdFactory.getInstance().releaseId(item.getObjectId());
			
			if (Config.LOG_ITEMS)
			{
				if (!Config.LOG_ITEMS_SMALL_LOG || (Config.LOG_ITEMS_SMALL_LOG && (item.isEquipable() || (item.getId() == ADENA_ID))))
				{
					LogRecord record = new LogRecord(Level.INFO, "DELETE:" + process);
					record.setLoggerName("item");
					record.setParameters(new Object[]
					{
						item,
						"PrevCount(" + old + ")",
						actor,
						reference
					});
					_logItems.log(record);
				}
			}
			
			if (actor != null)
			{
				if (actor.isGM())
				{
					String referenceName = "no-reference";
					if (reference instanceof L2Object)
					{
						referenceName = (((L2Object) reference).getName() != null ? ((L2Object) reference).getName() : "no-name");
					}
					else if (reference instanceof String)
					{
						referenceName = (String) reference;
					}
					String targetName = (actor.getTarget() != null ? actor.getTarget().getName() : "no-target");
					if (Config.GMAUDIT)
					{
						GMAudit.auditGMAction(actor.getName() + " [" + actor.getObjectId() + "]", process + "(id: " + item.getId() + " count: " + item.getCount() + " itemObjId: " + item.getObjectId() + ")", targetName, "L2Object referencing this action is: " + referenceName);
					}
				}
			}
			
			// if it's a pet control item, delete the pet as well
			if (item.getItem().isPetItem())
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?"))
				{
					// Delete the pet in db
					statement.setInt(1, item.getObjectId());
					statement.execute();
				}
				catch (Exception e)
				{
					_log.warn("could not delete pet objectid:", e);
				}
			}
		}
	}
	
	public void reload()
	{
		load();
		EnchantItemHPBonusData.getInstance().load();
	}
	
	protected static class ResetOwner implements Runnable
	{
		L2ItemInstance _item;
		
		public ResetOwner(L2ItemInstance item)
		{
			_item = item;
		}
		
		@Override
		public void run()
		{
			_item.setOwnerId(0);
			_item.setItemLootShedule(null);
		}
	}
	
	public Set<Integer> getAllArmorsId()
	{
		return _armors.keySet();
	}
	
	public Set<Integer> getAllWeaponsId()
	{
		return _weapons.keySet();
	}
	
	public int getArraySize()
	{
		return _allTemplates.length;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemData _instance = new ItemData();
	}
	
	// Listeners
	
	/**
	 * Fires all the new item listeners, if any
	 * @param process
	 * @param itemId
	 * @param count
	 * @param actor
	 * @param reference
	 * @return
	 */
	private boolean fireNewItemListeners(String process, int itemId, long count, L2PcInstance actor, Object reference)
	{
		if (!newItemListeners.isEmpty() && (actor != null))
		{
			ItemCreateEvent event = new ItemCreateEvent();
			event.setItemId(itemId);
			event.setPlayer(actor);
			event.setCount(count);
			event.setProcess(process);
			event.setReference(reference);
			for (NewItemListener listener : newItemListeners)
			{
				if (listener.containsItemId(itemId))
				{
					if (!listener.onCreate(event))
					{
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Adds a new item listener
	 * @param listener
	 */
	public static void addNewItemListener(NewItemListener listener)
	{
		if (!newItemListeners.contains(listener))
		{
			newItemListeners.add(listener);
		}
	}
	
	/**
	 * Removes a new item listener
	 * @param listener
	 */
	public static void removeNewItemListener(NewItemListener listener)
	{
		newItemListeners.remove(listener);
	}
}
