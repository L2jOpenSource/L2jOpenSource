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
package l2r.gameserver.model.items.instance;

import static l2r.gameserver.model.itemcontainer.Inventory.ADENA_ID;
import static l2r.gameserver.model.itemcontainer.Inventory.MAX_ADENA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javolution.util.FastList;
import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.GeoData;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.datatables.xml.EnchantItemOptionsData;
import l2r.gameserver.datatables.xml.ItemData;
import l2r.gameserver.datatables.xml.OptionsData;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.enums.ItemLocation;
import l2r.gameserver.enums.ShotType;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.instancemanager.ItemsOnGroundManager;
import l2r.gameserver.instancemanager.MercTicketManager;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.model.DropProtection;
import l2r.gameserver.model.Elementals;
import l2r.gameserver.model.L2Augmentation;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.L2WorldRegion;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.knownlist.NullKnownList;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.model.items.L2Armor;
import l2r.gameserver.model.items.L2EtcItem;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.items.L2Weapon;
import l2r.gameserver.model.items.type.EtcItemType;
import l2r.gameserver.model.items.type.ItemType;
import l2r.gameserver.model.options.EnchantOptions;
import l2r.gameserver.model.options.Options;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.model.skills.funcs.Func;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.DropItem;
import l2r.gameserver.network.serverpackets.GetItem;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SpawnItem;
import l2r.gameserver.network.serverpackets.StatusUpdate;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.scripting.scriptengine.events.AugmentEvent;
import l2r.gameserver.scripting.scriptengine.events.ItemDropEvent;
import l2r.gameserver.scripting.scriptengine.events.ItemPickupEvent;
import l2r.gameserver.scripting.scriptengine.listeners.player.AugmentListener;
import l2r.gameserver.scripting.scriptengine.listeners.player.DropListener;
import l2r.gameserver.util.GMAudit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages items.
 * @version $Revision: 1.4.2.1.2.11 $ $Date: 2005/03/31 16:07:50 $
 */
public final class L2ItemInstance extends L2Object
{
	private static final Logger _log = LoggerFactory.getLogger(L2ItemInstance.class);
	private static final java.util.logging.Logger _logItems = java.util.logging.Logger.getLogger("item");
	
	private static FastList<AugmentListener> augmentListeners = new FastList<AugmentListener>().shared();
	private static FastList<DropListener> dropListeners = new FastList<DropListener>().shared();
	
	/** ID of the owner */
	private int _ownerId;
	
	/** ID of who dropped the item last, used for knownlist */
	private int _dropperObjectId = 0;
	
	/** Quantity of the item */
	private long _count;
	/** Initial Quantity of the item */
	private long _initCount;
	/** Remaining time (in miliseconds) */
	private long _time;
	/** Quantity of the item can decrease */
	private boolean _decrease = false;
	
	/** ID of the item */
	private final int _itemId;
	
	/** Object L2Item associated to the item */
	private final L2Item _item;
	
	/** Location of the item : Inventory, PaperDoll, WareHouse */
	private ItemLocation _loc;
	
	/** Slot where item is stored : Paperdoll slot, inventory order ... */
	private int _locData;
	
	/** Level of enchantment of the item */
	private int _enchantLevel;
	
	/** Wear Item */
	private boolean _wear;
	
	/** Augmented Item */
	private L2Augmentation _augmentation = null;
	
	/** Shadow item */
	private int _mana = -1;
	private boolean _consumingMana = false;
	private static final int MANA_CONSUMPTION_RATE = 60000;
	
	/** Custom item types (used loto, race tickets) */
	private int _type1;
	private int _type2;
	
	private long _dropTime;
	
	private boolean _published = false;
	
	private boolean _protected;
	
	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int REMOVED = 3;
	public static final int MODIFIED = 2;
	
	//@formatter:off
	public static final int[] DEFAULT_ENCHANT_OPTIONS = new int[] { 0, 0, 0 };
	//@formatter:on
	
	private int _lastChange = 2; // 1 ??, 2 modified, 3 removed
	private boolean _existsInDb; // if a record exists in DB.
	private boolean _storedInDb; // if DB data is up-to-date.
	
	private final ReentrantLock _dbLock = new ReentrantLock();
	
	private Elementals[] _elementals = null;
	
	private ScheduledFuture<?> itemLootShedule = null;
	private ScheduledFuture<?> _lifeTimeTask;
	
	private final DropProtection _dropProtection = new DropProtection();
	
	private int _shotsMask = 0;
	
	private final List<Options> _enchantOptions = new ArrayList<>();
	
	/**
	 * Constructor of the L2ItemInstance from the objectId and the itemId.
	 * @param objectId : int designating the ID of the object in the world
	 * @param itemId : int designating the ID of the item
	 */
	public L2ItemInstance(int objectId, int itemId)
	{
		super(objectId);
		setInstanceType(InstanceType.L2ItemInstance);
		_itemId = itemId;
		_item = ItemData.getInstance().getTemplate(itemId);
		if ((_itemId == 0) || (_item == null))
		{
			throw new IllegalArgumentException();
		}
		super.setName(_item.getName());
		setCount(1);
		_loc = ItemLocation.VOID;
		_type1 = 0;
		_type2 = 0;
		_dropTime = 0;
		_mana = _item.getDuration();
		_time = _item.getTime() == -1 ? -1 : System.currentTimeMillis() + ((long) _item.getTime() * 60 * 1000);
		scheduleLifeTimeTask();
	}
	
	/**
	 * Constructor of the L2ItemInstance from the objetId and the description of the item given by the L2Item.
	 * @param objectId : int designating the ID of the object in the world
	 * @param item : L2Item containing informations of the item
	 */
	public L2ItemInstance(int objectId, L2Item item)
	{
		super(objectId);
		setInstanceType(InstanceType.L2ItemInstance);
		_itemId = item.getId();
		_item = item;
		if (_itemId == 0)
		{
			throw new IllegalArgumentException();
		}
		super.setName(_item.getName());
		setCount(1);
		_loc = ItemLocation.VOID;
		_mana = _item.getDuration();
		_time = _item.getTime() == -1 ? -1 : System.currentTimeMillis() + ((long) _item.getTime() * 60 * 1000);
		scheduleLifeTimeTask();
	}
	
	/**
	 * Constructor overload.<br>
	 * Sets the next free object ID in the ID factory.
	 * @param itemId the item template ID
	 */
	public L2ItemInstance(int itemId)
	{
		this(IdFactory.getInstance().getNextId(), itemId);
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new NullKnownList(this));
	}
	
	/**
	 * Remove a L2ItemInstance from the world and send server->client GetItem packets.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client Packet GetItem to player that pick up and its _knowPlayers member</li> <li>Remove the L2Object from the world</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR>
	 * <BR>
	 * <B><U> Assert </U> :</B><BR>
	 * <BR>
	 * <li>this instanceof L2ItemInstance</li> <li>_worldRegion != null <I>(L2Object is visible at the beginning)</I></li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Do Pickup Item : PCInstance and Pet</li><BR>
	 * <BR>
	 * @param player Player that pick up the item
	 */
	public final void pickupMe(L2Character player)
	{
		if (!firePickupListeners(player.getActingPlayer()))
		{
			return;
		}
		assert getWorldRegion() != null;
		
		L2WorldRegion oldregion = getWorldRegion();
		
		// Create a server->client GetItem packet to pick up the L2ItemInstance
		player.broadcastPacket(new GetItem(this, player.getObjectId()));
		
		synchronized (this)
		{
			setIsVisible(false);
			setWorldRegion(null);
		}
		
		// if this item is a mercenary ticket, remove the spawns!
		int itemId = getId();
		
		if (MercTicketManager.getInstance().getTicketCastleId(itemId) > 0)
		{
			MercTicketManager.getInstance().removeTicket(this);
			ItemsOnGroundManager.getInstance().removeObject(this);
		}
		
		if (!Config.DISABLE_TUTORIAL && ((itemId == Inventory.ADENA_ID) || (itemId == 6353)))
		{
			// Note from UnAfraid:
			// Unhardcode this?
			L2PcInstance actor = player.getActingPlayer();
			if (actor != null)
			{
				QuestState qs = actor.getQuestState("255_Tutorial");
				if ((qs != null) && (qs.getQuest() != null))
				{
					qs.getQuest().notifyEvent("CE" + itemId, null, actor);
				}
			}
		}
		// outside of synchronized to avoid deadlocks
		// Remove the L2ItemInstance from the world
		L2World.getInstance().removeVisibleObject(this, oldregion);
	}
	
	/**
	 * Sets the ownerID of the item
	 * @param process : String Identifier of process triggering this action
	 * @param owner_id : int designating the ID of the owner
	 * @param creator : L2PcInstance Player requesting the item creation
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void setOwnerId(String process, int owner_id, L2PcInstance creator, Object reference)
	{
		setOwnerId(owner_id);
		
		if (Config.LOG_ITEMS)
		{
			if (!Config.LOG_ITEMS_SMALL_LOG || (Config.LOG_ITEMS_SMALL_LOG && (getItem().isEquipable() || (getItem().getId() == ADENA_ID))))
			{
				LogRecord record = new LogRecord(Level.INFO, "SETOWNER:" + process);
				record.setLoggerName("item");
				record.setParameters(new Object[]
				{
					this,
					creator,
					reference
				});
				_logItems.log(record);
			}
		}
		
		if (creator != null)
		{
			if (creator.isGM())
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
				String targetName = (creator.getTarget() != null ? creator.getTarget().getName() : "no-target");
				if (Config.GMAUDIT)
				{
					GMAudit.auditGMAction(creator.getName() + " [" + creator.getObjectId() + "]", process + "(id: " + getId() + " name: " + getName() + ")", targetName, "L2Object referencing this action is: " + referenceName);
				}
			}
		}
	}
	
	/**
	 * Sets the ownerID of the item
	 * @param owner_id : int designating the ID of the owner
	 */
	public void setOwnerId(int owner_id)
	{
		if (owner_id == _ownerId)
		{
			return;
		}
		
		// Remove any inventory skills from the old owner.
		removeSkillsFromOwner();
		
		_ownerId = owner_id;
		_storedInDb = false;
		
		// Give any inventory skills to the new owner only if the item is in inventory
		// else the skills will be given when location is set to inventory.
		giveSkillsToOwner();
	}
	
	/**
	 * Returns the ownerID of the item
	 * @return int : ownerID of the item
	 */
	public int getOwnerId()
	{
		return _ownerId;
	}
	
	/**
	 * Sets the location of the item
	 * @param loc : ItemLocation (enumeration)
	 */
	public void setItemLocation(ItemLocation loc)
	{
		setItemLocation(loc, 0);
	}
	
	/**
	 * Sets the location of the item.<BR>
	 * <BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * @param loc : ItemLocation (enumeration)
	 * @param loc_data : int designating the slot where the item is stored or the village for freights
	 */
	public void setItemLocation(ItemLocation loc, int loc_data)
	{
		if ((loc == _loc) && (loc_data == _locData))
		{
			return;
		}
		
		// Remove any inventory skills from the old owner.
		removeSkillsFromOwner();
		
		_loc = loc;
		_locData = loc_data;
		_storedInDb = false;
		
		// Give any inventory skills to the new owner only if the item is in inventory
		// else the skills will be given when location is set to inventory.
		giveSkillsToOwner();
	}
	
	public ItemLocation getItemLocation()
	{
		return _loc;
	}
	
	/**
	 * Sets the quantity of the item.<BR>
	 * <BR>
	 * @param count the new count to set
	 */
	public void setCount(long count)
	{
		if (getCount() == count)
		{
			return;
		}
		
		_count = count >= -1 ? count : 0;
		_storedInDb = false;
	}
	
	/**
	 * @return Returns the count.
	 */
	public long getCount()
	{
		return _count;
	}
	
	/**
	 * Sets the quantity of the item.<BR>
	 * <BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * @param process : String Identifier of process triggering this action
	 * @param count : int
	 * @param creator : L2PcInstance Player requesting the item creation
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void changeCount(String process, long count, L2PcInstance creator, Object reference)
	{
		if (count == 0)
		{
			return;
		}
		long old = getCount();
		long max = getId() == ADENA_ID ? MAX_ADENA : Integer.MAX_VALUE;
		
		if ((count > 0) && (getCount() > (max - count)))
		{
			setCount(max);
		}
		else
		{
			setCount(getCount() + count);
		}
		
		if (getCount() < 0)
		{
			setCount(0);
		}
		
		_storedInDb = false;
		
		if (Config.LOG_ITEMS && (process != null))
		{
			if (!Config.LOG_ITEMS_SMALL_LOG || (Config.LOG_ITEMS_SMALL_LOG && (_item.isEquipable() || (_item.getId() == ADENA_ID))))
			{
				LogRecord record = new LogRecord(Level.INFO, "CHANGE:" + process);
				record.setLoggerName("item");
				record.setParameters(new Object[]
				{
					this,
					"PrevCount(" + old + ")",
					creator,
					reference
				});
				_logItems.log(record);
			}
		}
		
		if (creator != null)
		{
			if (creator.isGM())
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
				String targetName = (creator.getTarget() != null ? creator.getTarget().getName() : "no-target");
				if (Config.GMAUDIT)
				{
					GMAudit.auditGMAction(creator.getName() + " [" + creator.getObjectId() + "]", process + "(id: " + getId() + " objId: " + getObjectId() + " name: " + getName() + " count: " + count + ")", targetName, "L2Object referencing this action is: " + referenceName);
				}
			}
		}
	}
	
	// No logging (function designed for shots only)
	public void changeCountWithoutTrace(int count, L2PcInstance creator, Object reference)
	{
		changeCount(null, count, creator, reference);
	}
	
	/**
	 * Return true if item can be enchanted
	 * @return boolean
	 */
	public int isEnchantable()
	{
		if ((getItemLocation() == ItemLocation.INVENTORY) || (getItemLocation() == ItemLocation.PAPERDOLL))
		{
			return getItem().isEnchantable();
		}
		return 0;
	}
	
	/**
	 * Returns if item is equipable
	 * @return boolean
	 */
	public boolean isEquipable()
	{
		return !((_item.getBodyPart() == 0) || (_item.getItemType() == EtcItemType.ARROW) || (_item.getItemType() == EtcItemType.BOLT) || (_item.getItemType() == EtcItemType.LURE));
	}
	
	/**
	 * Returns if item is equipped
	 * @return boolean
	 */
	public boolean isEquipped()
	{
		return (_loc == ItemLocation.PAPERDOLL) || (_loc == ItemLocation.PET_EQUIP);
	}
	
	/**
	 * Returns the slot where the item is stored
	 * @return int
	 */
	public int getLocationSlot()
	{
		assert (_loc == ItemLocation.PAPERDOLL) || (_loc == ItemLocation.PET_EQUIP) || (_loc == ItemLocation.INVENTORY) || (_loc == ItemLocation.MAIL) || (_loc == ItemLocation.FREIGHT);
		return _locData;
	}
	
	/**
	 * Returns the characteristics of the item
	 * @return L2Item
	 */
	public L2Item getItem()
	{
		return _item;
	}
	
	public int getCustomType1()
	{
		return _type1;
	}
	
	public int getCustomType2()
	{
		return _type2;
	}
	
	public void setCustomType1(int newtype)
	{
		_type1 = newtype;
	}
	
	public void setCustomType2(int newtype)
	{
		_type2 = newtype;
	}
	
	public void setDropTime(long time)
	{
		_dropTime = time;
	}
	
	public long getDropTime()
	{
		return _dropTime;
	}
	
	/**
	 * @return the type of item.
	 */
	public ItemType getItemType()
	{
		return _item.getItemType();
	}
	
	/**
	 * Gets the item ID.
	 * @return the item ID
	 */
	@Override
	public int getId()
	{
		return _itemId;
	}
	
	/**
	 * @return the display Id of the item.
	 */
	public int getDisplayId()
	{
		return getItem().getDisplayId();
	}
	
	/**
	 * @return {@code true} if item is an EtcItem, {@code false} otherwise.
	 */
	public boolean isEtcItem()
	{
		return (_item instanceof L2EtcItem);
	}
	
	/**
	 * @return {@code true} if item is a Weapon/Shield, {@code false} otherwise.
	 */
	public boolean isWeapon()
	{
		return (_item instanceof L2Weapon);
	}
	
	/**
	 * @return {@code true} if item is an Armor, {@code false} otherwise.
	 */
	public boolean isArmor()
	{
		return (_item instanceof L2Armor);
	}
	
	/**
	 * @return the characteristics of the L2EtcItem, {@code false} otherwise.
	 */
	public L2EtcItem getEtcItem()
	{
		if (_item instanceof L2EtcItem)
		{
			return (L2EtcItem) _item;
		}
		return null;
	}
	
	/**
	 * @return the characteristics of the L2Weapon.
	 */
	public L2Weapon getWeaponItem()
	{
		if (_item instanceof L2Weapon)
		{
			return (L2Weapon) _item;
		}
		return null;
	}
	
	/**
	 * @return the characteristics of the L2Armor.
	 */
	public L2Armor getArmorItem()
	{
		if (_item instanceof L2Armor)
		{
			return (L2Armor) _item;
		}
		return null;
	}
	
	/**
	 * @return the quantity of crystals for crystallization.
	 */
	public final int getCrystalCount()
	{
		return _item.getCrystalCount(_enchantLevel);
	}
	
	/**
	 * @return the reference price of the item.
	 */
	public int getReferencePrice()
	{
		return _item.getReferencePrice();
	}
	
	/**
	 * @return the name of the item.
	 */
	public String getItemName()
	{
		return _item.getName();
	}
	
	/**
	 * @return the reuse delay of this item.
	 */
	public int getReuseDelay()
	{
		return _item.getReuseDelay();
	}
	
	/**
	 * @return the shared reuse item group.
	 */
	public int getSharedReuseGroup()
	{
		return _item.getSharedReuseGroup();
	}
	
	/**
	 * @return the last change of the item
	 */
	public int getLastChange()
	{
		return _lastChange;
	}
	
	/**
	 * Sets the last change of the item
	 * @param lastChange : int
	 */
	public void setLastChange(int lastChange)
	{
		_lastChange = lastChange;
	}
	
	/**
	 * Returns if item is stackable
	 * @return boolean
	 */
	public boolean isStackable()
	{
		return _item.isStackable();
	}
	
	/**
	 * Returns if item is dropable
	 * @return boolean
	 */
	public boolean isDropable()
	{
		return isAugmented() ? false : _item.isDropable();
	}
	
	/**
	 * Returns if item is destroyable
	 * @return boolean
	 */
	public boolean isDestroyable()
	{
		return _item.isDestroyable();
	}
	
	/**
	 * Returns if item is tradeable
	 * @return boolean
	 */
	public boolean isTradeable()
	{
		return isAugmented() ? false : _item.isTradeable();
	}
	
	/**
	 * Returns if item is sellable
	 * @return boolean
	 */
	public boolean isSellable()
	{
		return isAugmented() ? false : _item.isSellable();
	}
	
	/**
	 * @param isPrivateWareHouse
	 * @return if item can be deposited in warehouse or freight
	 */
	public boolean isDepositable(boolean isPrivateWareHouse)
	{
		// equipped, hero and quest items
		if (isEquipped() || !_item.isDepositable())
		{
			return false;
		}
		if (!isPrivateWareHouse)
		{
			// augmented not tradeable
			if (!isTradeable() || isShadowItem())
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Returns if item is consumable
	 * @return boolean
	 */
	public boolean isConsumable()
	{
		return _item.isConsumable();
	}
	
	public boolean isPotion()
	{
		return _item.isPotion();
	}
	
	public boolean isElixir()
	{
		return _item.isElixir();
	}
	
	public boolean isHeroItem()
	{
		return _item.isHeroItem();
	}
	
	public boolean isCommonItem()
	{
		return _item.isCommon();
	}
	
	/**
	 * Returns whether this item is pvp or not
	 * @return boolean
	 */
	public boolean isPvp()
	{
		return _item.isPvpItem();
	}
	
	public boolean isOlyRestrictedItem()
	{
		return getItem().isOlyRestrictedItem();
	}
	
	/**
	 * @param player
	 * @param allowAdena
	 * @param allowNonTradeable
	 * @return if item is available for manipulation
	 */
	public boolean isAvailable(L2PcInstance player, boolean allowAdena, boolean allowNonTradeable)
	{
		return ((!isEquipped()) // Not equipped
			&& (getItem().getType2() != L2Item.TYPE2_QUEST) // Not Quest Item
			&& ((getItem().getType2() != L2Item.TYPE2_MONEY) || (getItem().getType1() != L2Item.TYPE1_SHIELD_ARMOR)) // not money, not shield
			&& (!player.hasSummon() || (getObjectId() != player.getSummon().getControlObjectId())) // Not Control item of currently summoned pet
			&& (player.getActiveEnchantItemId() != getObjectId()) // Not momentarily used enchant scroll
			&& (player.getActiveEnchantSupportItemId() != getObjectId()) // Not momentarily used enchant support item
			&& (player.getActiveEnchantAttrItemId() != getObjectId()) // Not momentarily used enchant attribute item
			&& (allowAdena || (getId() != Inventory.ADENA_ID)) // Not Adena
			&& ((player.getCurrentSkill() == null) || (player.getCurrentSkill().getSkill().getItemConsumeId() != getId())) && (!player.isCastingSimultaneouslyNow() || (player.getLastSimultaneousSkillCast() == null) || (player.getLastSimultaneousSkillCast().getItemConsumeId() != getId())) && (allowNonTradeable || (isTradeable() && (!((getItem().getItemType() == EtcItemType.PET_COLLAR) && player.havePetInvItems())))));
	}
	
	/**
	 * Returns the level of enchantment of the item
	 * @return int
	 */
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}
	
	/**
	 * @param enchantLevel the enchant value to set
	 */
	public void setEnchantLevel(int enchantLevel)
	{
		if (_enchantLevel == enchantLevel)
		{
			return;
		}
		clearEnchantStats();
		_enchantLevel = enchantLevel;
		applyEnchantStats();
		_storedInDb = false;
	}
	
	/**
	 * Returns whether this item is augmented or not
	 * @return true if augmented
	 */
	public boolean isAugmented()
	{
		return _augmentation != null;
	}
	
	/**
	 * Returns the augmentation object for this item
	 * @return augmentation
	 */
	public L2Augmentation getAugmentation()
	{
		return _augmentation;
	}
	
	/**
	 * Sets a new augmentation
	 * @param augmentation
	 * @return return true if sucessfull
	 */
	public boolean setAugmentation(L2Augmentation augmentation)
	{
		// there shall be no previous augmentation..
		if (_augmentation != null)
		{
			_log.info("Warning: Augment set for (" + getObjectId() + ") " + getName() + " owner: " + getOwnerId());
			return false;
		}
		if (!fireAugmentListeners(true, augmentation))
		{
			return false;
		}
		_augmentation = augmentation;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			updateItemAttributes(con);
		}
		catch (SQLException e)
		{
			_log.error("Could not update atributes for item: " + this + " from DB:", e);
		}
		return true;
	}
	
	/**
	 * Remove the augmentation
	 */
	public void removeAugmentation()
	{
		if (_augmentation == null)
		{
			return;
		}
		if (!fireAugmentListeners(true, _augmentation))
		{
			return;
		}
		_augmentation = null;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM item_attributes WHERE itemId = ?"))
		{
			ps.setInt(1, getObjectId());
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			_log.error("Could not remove augmentation for item: " + this + " from DB:", e);
		}
	}
	
	public void restoreAttributes()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps1 = con.prepareStatement("SELECT augAttributes FROM item_attributes WHERE itemId=?");
			PreparedStatement ps2 = con.prepareStatement("SELECT elemType,elemValue FROM item_elementals WHERE itemId=?"))
		{
			ps1.setInt(1, getObjectId());
			try (ResultSet rs = ps1.executeQuery())
			{
				if (rs.next())
				{
					int aug_attributes = rs.getInt(1);
					if (aug_attributes != -1)
					{
						_augmentation = new L2Augmentation(rs.getInt("augAttributes"));
					}
				}
			}
			
			ps2.setInt(1, getObjectId());
			try (ResultSet rs = ps2.executeQuery())
			{
				while (rs.next())
				{
					byte elem_type = rs.getByte(1);
					int elem_value = rs.getInt(2);
					if ((elem_type != -1) && (elem_value != -1))
					{
						applyAttribute(elem_type, elem_value);
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.error("Could not restore augmentation and elemental data for item " + this + " from DB: " + e.getMessage(), e);
		}
	}
	
	private void updateItemAttributes(Connection con)
	{
		try (PreparedStatement ps = con.prepareStatement("REPLACE INTO item_attributes VALUES(?,?)"))
		{
			ps.setInt(1, getObjectId());
			ps.setInt(2, _augmentation != null ? _augmentation.getAttributes() : -1);
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			_log.error("Could not update atributes for item: " + this + " from DB:", e);
		}
	}
	
	private void updateItemElements(Connection con)
	{
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ?"))
		{
			ps.setInt(1, getObjectId());
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			_log.error("Could not update elementals for item: " + this + " from DB:", e);
		}
		
		if (_elementals == null)
		{
			return;
		}
		
		try (PreparedStatement ps = con.prepareStatement("INSERT INTO item_elementals VALUES(?,?,?)"))
		{
			for (Elementals elm : _elementals)
			{
				ps.setInt(1, getObjectId());
				ps.setByte(2, elm.getElement());
				ps.setInt(3, elm.getValue());
				ps.executeUpdate();
				ps.clearParameters();
			}
		}
		catch (SQLException e)
		{
			_log.error("Could not update elementals for item: " + this + " from DB:", e);
		}
	}
	
	public Elementals[] getElementals()
	{
		return _elementals;
	}
	
	public Elementals getElemental(byte attribute)
	{
		if (_elementals == null)
		{
			return null;
		}
		for (Elementals elm : _elementals)
		{
			if (elm.getElement() == attribute)
			{
				return elm;
			}
		}
		return null;
	}
	
	public byte getAttackElementType()
	{
		if (!isWeapon())
		{
			return -2;
		}
		else if (getItem().getElementals() != null)
		{
			return getItem().getElementals()[0].getElement();
		}
		else if (_elementals != null)
		{
			return _elementals[0].getElement();
		}
		return -2;
	}
	
	public int getAttackElementPower()
	{
		if (!isWeapon())
		{
			return 0;
		}
		else if (getItem().getElementals() != null)
		{
			return getItem().getElementals()[0].getValue();
		}
		else if (_elementals != null)
		{
			return _elementals[0].getValue();
		}
		return 0;
	}
	
	public int getElementDefAttr(byte element)
	{
		if (!isArmor())
		{
			return 0;
		}
		else if (getItem().getElementals() != null)
		{
			Elementals elm = getItem().getElemental(element);
			if (elm != null)
			{
				return elm.getValue();
			}
		}
		else if (_elementals != null)
		{
			Elementals elm = getElemental(element);
			if (elm != null)
			{
				return elm.getValue();
			}
		}
		return 0;
	}
	
	private void applyAttribute(byte element, int value)
	{
		if (_elementals == null)
		{
			_elementals = new Elementals[1];
			_elementals[0] = new Elementals(element, value);
		}
		else
		{
			Elementals elm = getElemental(element);
			if (elm != null)
			{
				elm.setValue(value);
			}
			else
			{
				elm = new Elementals(element, value);
				Elementals[] array = new Elementals[_elementals.length + 1];
				System.arraycopy(_elementals, 0, array, 0, _elementals.length);
				array[_elementals.length] = elm;
				_elementals = array;
			}
		}
	}
	
	/**
	 * Add elemental attribute to item and save to db
	 * @param element
	 * @param value
	 */
	public void setElementAttr(byte element, int value)
	{
		applyAttribute(element, value);
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			updateItemElements(con);
		}
		catch (SQLException e)
		{
			_log.error("Could not update elementals for item: " + this + " from DB:", e);
		}
	}
	
	/**
	 * Remove elemental from item
	 * @param element byte element to remove, -1 for all elementals remove
	 */
	public void clearElementAttr(byte element)
	{
		if ((getElemental(element) == null) && (element != -1))
		{
			return;
		}
		
		Elementals[] array = null;
		if ((element != -1) && (_elementals != null) && (_elementals.length > 1))
		{
			array = new Elementals[_elementals.length - 1];
			int i = 0;
			for (Elementals elm : _elementals)
			{
				if (elm.getElement() != element)
				{
					array[i++] = elm;
				}
			}
		}
		_elementals = array;
		
		String query = (element != -1) ? "DELETE FROM item_elementals WHERE itemId = ? AND elemType = ?" : "DELETE FROM item_elementals WHERE itemId = ?";
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			if (element != -1)
			{
				// Item can have still others
				ps.setInt(2, element);
			}
			
			ps.setInt(1, getObjectId());
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			_log.error("Could not remove elemental enchant for item: " + this + " from DB:", e);
		}
	}
	
	/**
	 * Used to decrease mana (mana means life time for shadow items)
	 */
	public static class ScheduleConsumeManaTask implements Runnable
	{
		private static final Logger _log = LoggerFactory.getLogger(ScheduleConsumeManaTask.class);
		private final L2ItemInstance _shadowItem;
		
		public ScheduleConsumeManaTask(L2ItemInstance item)
		{
			_shadowItem = item;
		}
		
		@Override
		public void run()
		{
			try
			{
				// decrease mana
				if (_shadowItem != null)
				{
					_shadowItem.decreaseMana(true);
				}
			}
			catch (Exception e)
			{
				_log.error("", e);
			}
		}
	}
	
	/**
	 * Returns true if this item is a shadow item Shadow items have a limited life-time
	 * @return
	 */
	public boolean isShadowItem()
	{
		return (_mana >= 0);
	}
	
	/**
	 * Returns the remaining mana of this shadow item
	 * @return lifeTime
	 */
	public int getMana()
	{
		return _mana;
	}
	
	/**
	 * Decreases the mana of this shadow item, sends a inventory update schedules a new consumption task if non is running optionally one could force a new task
	 * @param resetConsumingMana if true forces a new consumption task if item is equipped
	 */
	public void decreaseMana(boolean resetConsumingMana)
	{
		decreaseMana(resetConsumingMana, 1);
	}
	
	/**
	 * Decreases the mana of this shadow item, sends a inventory update schedules a new consumption task if non is running optionally one could force a new task
	 * @param resetConsumingMana if forces a new consumption task if item is equipped
	 * @param count how much mana decrease
	 */
	public void decreaseMana(boolean resetConsumingMana, int count)
	{
		if (!isShadowItem())
		{
			return;
		}
		
		if ((_mana - count) >= 0)
		{
			_mana -= count;
		}
		else
		{
			_mana = 0;
		}
		
		if (_storedInDb)
		{
			_storedInDb = false;
		}
		if (resetConsumingMana)
		{
			_consumingMana = false;
		}
		
		final L2PcInstance player = getActingPlayer();
		if (player != null)
		{
			SystemMessage sm;
			switch (_mana)
			{
				case 10:
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_10);
					sm.addItemName(_item);
					player.sendPacket(sm);
					break;
				case 5:
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_5);
					sm.addItemName(_item);
					player.sendPacket(sm);
					break;
				case 1:
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_1);
					sm.addItemName(_item);
					player.sendPacket(sm);
					break;
			}
			
			if (_mana == 0) // The life time has expired
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_0);
				sm.addItemName(_item);
				player.sendPacket(sm);
				
				// unequip
				if (isEquipped())
				{
					L2ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(getLocationSlot());
					InventoryUpdate iu = new InventoryUpdate();
					for (L2ItemInstance item : unequiped)
					{
						item.unChargeAllShots();
						iu.addModifiedItem(item);
					}
					player.sendPacket(iu);
					player.broadcastUserInfo();
				}
				
				if (getItemLocation() != ItemLocation.WAREHOUSE)
				{
					// destroy
					player.getInventory().destroyItem("L2ItemInstance", this, player, null);
					
					// send update
					InventoryUpdate iu = new InventoryUpdate();
					iu.addRemovedItem(this);
					player.sendPacket(iu);
					
					StatusUpdate su = new StatusUpdate(player);
					su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
					player.sendPacket(su);
					
				}
				else
				{
					player.getWarehouse().destroyItem("L2ItemInstance", this, player, null);
				}
				
				// delete from world
				L2World.getInstance().removeObject(this);
			}
			else
			{
				// Reschedule if still equipped
				if (!_consumingMana && isEquipped())
				{
					scheduleConsumeManaTask();
				}
				if (getItemLocation() != ItemLocation.WAREHOUSE)
				{
					InventoryUpdate iu = new InventoryUpdate();
					iu.addModifiedItem(this);
					player.sendPacket(iu);
				}
			}
		}
	}
	
	public void scheduleConsumeManaTask()
	{
		if (_consumingMana)
		{
			return;
		}
		_consumingMana = true;
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleConsumeManaTask(this), MANA_CONSUMPTION_RATE);
	}
	
	/**
	 * Returns false cause item can't be attacked
	 * @return boolean false
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
	
	/**
	 * This function basically returns a set of functions from L2Item/L2Armor/L2Weapon, but may add additional functions, if this particular item instance is enhanched for a particular player.
	 * @param player : L2Character designating the player
	 * @return Func[]
	 */
	public Func[] getStatFuncs(L2Character player)
	{
		return getItem().getStatFuncs(this, player);
	}
	
	/**
	 * Updates the database.<BR>
	 */
	public void updateDatabase()
	{
		this.updateDatabase(false);
	}
	
	/**
	 * Updates the database.<BR>
	 * @param force if the update should necessarilly be done.
	 */
	public void updateDatabase(boolean force)
	{
		_dbLock.lock();
		
		try
		{
			if (_existsInDb)
			{
				if ((_ownerId == 0) || (_loc == ItemLocation.VOID) || (_loc == ItemLocation.REFUND) || ((getCount() == 0) && (_loc != ItemLocation.LEASE)))
				{
					removeFromDb();
				}
				else if (!Config.LAZY_ITEMS_UPDATE || force)
				{
					updateInDb();
				}
			}
			else
			{
				if ((_ownerId == 0) || (_loc == ItemLocation.VOID) || (_loc == ItemLocation.REFUND) || ((getCount() == 0) && (_loc != ItemLocation.LEASE)))
				{
					return;
				}
				insertIntoDb();
			}
		}
		finally
		{
			_dbLock.unlock();
		}
	}
	
	/**
	 * Returns a L2ItemInstance stored in database from its objectID
	 * @param ownerId
	 * @param rs
	 * @return L2ItemInstance
	 */
	public static L2ItemInstance restoreFromDb(int ownerId, ResultSet rs)
	{
		L2ItemInstance inst = null;
		int objectId, item_id, loc_data, enchant_level, custom_type1, custom_type2, manaLeft;
		long time, count;
		ItemLocation loc;
		try
		{
			objectId = rs.getInt(1);
			item_id = rs.getInt("item_id");
			count = rs.getLong("count");
			loc = ItemLocation.valueOf(rs.getString("loc"));
			loc_data = rs.getInt("loc_data");
			enchant_level = rs.getInt("enchant_level");
			custom_type1 = rs.getInt("custom_type1");
			custom_type2 = rs.getInt("custom_type2");
			manaLeft = rs.getInt("mana_left");
			time = rs.getLong("time");
		}
		catch (Exception e)
		{
			_log.error("Could not restore an item owned by " + ownerId + " from DB:", e);
			return null;
		}
		L2Item item = ItemData.getInstance().getTemplate(item_id);
		if (item == null)
		{
			_log.error("Item item_id=" + item_id + " not known, object_id=" + objectId);
			return null;
		}
		inst = new L2ItemInstance(objectId, item);
		inst._ownerId = ownerId;
		inst.setCount(count);
		inst._enchantLevel = enchant_level;
		inst._type1 = custom_type1;
		inst._type2 = custom_type2;
		inst._loc = loc;
		inst._locData = loc_data;
		inst._existsInDb = true;
		inst._storedInDb = true;
		
		// Setup life time for shadow weapons
		inst._mana = manaLeft;
		inst._time = time;
		
		// load augmentation and elemental enchant
		if (inst.isEquipable())
		{
			inst.restoreAttributes();
		}
		
		return inst;
	}
	
	/**
	 * Init a dropped L2ItemInstance and add it in the world as a visible object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the x,y,z position of the L2ItemInstance dropped and update its _worldregion</li> <li>Add the L2ItemInstance dropped to _visibleObjects of its L2WorldRegion</li> <li>Add the L2ItemInstance dropped in the world as a <B>visible</B> object</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to _allObjects of L2World </B></FONT><BR>
	 * <BR>
	 * <B><U> Assert </U> :</B><BR>
	 * <BR>
	 * <li>_worldRegion == null <I>(L2Object is invisible at the beginning)</I></li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Drop item</li> <li>Call Pet</li><BR>
	 */
	public class ItemDropTask implements Runnable
	{
		private int _x, _y, _z;
		private final L2Character _dropper;
		private final L2ItemInstance _itm;
		
		public ItemDropTask(L2ItemInstance item, L2Character dropper, int x, int y, int z)
		{
			_x = x;
			_y = y;
			_z = z;
			_dropper = dropper;
			_itm = item;
		}
		
		@Override
		public final void run()
		{
			assert _itm.getWorldRegion() == null;
			
			if ((Config.GEODATA > 0) && (_dropper != null))
			{
				Location dropDest = GeoData.getInstance().moveCheck(_dropper.getX(), _dropper.getY(), _dropper.getZ(), _x, _y, _z, _dropper.getInstanceId());
				_x = dropDest.getX();
				_y = dropDest.getY();
				_z = dropDest.getZ();
			}
			
			if (_dropper != null)
			{
				setInstanceId(_dropper.getInstanceId()); // Inherit instancezone when dropped in visible world
			}
			else
			{
				setInstanceId(0); // No dropper? Make it a global item...
			}
			
			synchronized (_itm)
			{
				// Set the x,y,z position of the L2ItemInstance dropped and update its _worldregion
				_itm.setIsVisible(true);
				_itm.setXYZ(_x, _y, _z);
				_itm.setWorldRegion(L2World.getInstance().getRegion(getLocation()));
				
				// Add the L2ItemInstance dropped to _visibleObjects of its L2WorldRegion
			}
			
			_itm.getWorldRegion().addVisibleObject(_itm);
			_itm.setDropTime(System.currentTimeMillis());
			_itm.setDropperObjectId(_dropper != null ? _dropper.getObjectId() : 0); // Set the dropper Id for the knownlist packets in sendInfo
			
			// this can synchronize on others instances, so it's out of
			// synchronized, to avoid deadlocks
			// Add the L2ItemInstance dropped in the world as a visible object
			L2World.getInstance().addVisibleObject(_itm, _itm.getWorldRegion());
			if (Config.SAVE_DROPPED_ITEM)
			{
				ItemsOnGroundManager.getInstance().save(_itm);
			}
			_itm.setDropperObjectId(0); // Set the dropper Id back to 0 so it no longer shows the drop packet
		}
	}
	
	public final void dropMe(L2Character dropper, int x, int y, int z)
	{
		if (!fireDropListeners(dropper, new Location(x, y, z)))
		{
			return;
		}
		ThreadPoolManager.getInstance().executeGeneral(new ItemDropTask(this, dropper, x, y, z));
	}
	
	/**
	 * Update the database with values of the item
	 */
	private void updateInDb()
	{
		assert _existsInDb;
		
		if (_wear)
		{
			return;
		}
		
		if (_storedInDb)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,custom_type1=?,custom_type2=?,mana_left=?,time=? " + "WHERE object_id = ?"))
		{
			ps.setInt(1, _ownerId);
			ps.setLong(2, getCount());
			ps.setString(3, _loc.name());
			ps.setInt(4, _locData);
			ps.setInt(5, getEnchantLevel());
			ps.setInt(6, getCustomType1());
			ps.setInt(7, getCustomType2());
			ps.setInt(8, getMana());
			ps.setLong(9, getTime());
			ps.setInt(10, getObjectId());
			ps.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
		}
		catch (Exception e)
		{
			_log.error("Could not update item " + this + " in DB: Reason: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Insert the item in database
	 */
	private void insertIntoDb()
	{
		assert !_existsInDb && (getObjectId() != 0);
		
		if (_wear)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,mana_left,time) " + "VALUES (?,?,?,?,?,?,?,?,?,?,?)"))
		{
			ps.setInt(1, _ownerId);
			ps.setInt(2, _itemId);
			ps.setLong(3, getCount());
			ps.setString(4, _loc.name());
			ps.setInt(5, _locData);
			ps.setInt(6, getEnchantLevel());
			ps.setInt(7, getObjectId());
			ps.setInt(8, _type1);
			ps.setInt(9, _type2);
			ps.setInt(10, getMana());
			ps.setLong(11, getTime());
			
			ps.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
			
			if (_augmentation != null)
			{
				updateItemAttributes(con);
			}
			if (_elementals != null)
			{
				updateItemElements(con);
			}
		}
		catch (Exception e)
		{
			_log.error("Could not insert item " + this + " into DB: Reason: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Delete item from database
	 */
	private void removeFromDb()
	{
		assert _existsInDb;
		
		if (_wear)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM items WHERE object_id = ?"))
			{
				ps.setInt(1, getObjectId());
				ps.executeUpdate();
				_existsInDb = false;
				_storedInDb = false;
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM item_attributes WHERE itemId = ?"))
			{
				ps.setInt(1, getObjectId());
				ps.executeUpdate();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ?"))
			{
				ps.setInt(1, getObjectId());
				ps.executeUpdate();
			}
		}
		catch (Exception e)
		{
			_log.error("Could not delete item " + this + " in DB: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Returns the item in String format
	 * @return String
	 */
	@Override
	public String toString()
	{
		return _item + "[" + getObjectId() + "]";
	}
	
	public void resetOwnerTimer()
	{
		if (itemLootShedule != null)
		{
			itemLootShedule.cancel(true);
		}
		itemLootShedule = null;
	}
	
	public void setItemLootShedule(ScheduledFuture<?> sf)
	{
		itemLootShedule = sf;
	}
	
	public ScheduledFuture<?> getItemLootShedule()
	{
		return itemLootShedule;
	}
	
	public void setProtected(boolean isProtected)
	{
		_protected = isProtected;
	}
	
	public boolean isProtected()
	{
		return _protected;
	}
	
	public boolean isNightLure()
	{
		return (((_itemId >= 8505) && (_itemId <= 8513)) || (_itemId == 8485));
	}
	
	public void setCountDecrease(boolean decrease)
	{
		_decrease = decrease;
	}
	
	public boolean getCountDecrease()
	{
		return _decrease;
	}
	
	public void setInitCount(int InitCount)
	{
		_initCount = InitCount;
	}
	
	public long getInitCount()
	{
		return _initCount;
	}
	
	public void restoreInitCount()
	{
		if (_decrease)
		{
			setCount(_initCount);
		}
	}
	
	public boolean isTimeLimitedItem()
	{
		return (_time > 0);
	}
	
	/**
	 * Returns (current system time + time) of this time limited item
	 * @return Time
	 */
	public long getTime()
	{
		return _time;
	}
	
	public long getRemainingTime()
	{
		return _time - System.currentTimeMillis();
	}
	
	public void setLifeTime(long lifetime)
	{
		_time = lifetime;
	}
	
	public void endOfLife()
	{
		L2PcInstance player = getActingPlayer();
		if (player != null)
		{
			if (isEquipped())
			{
				L2ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(getLocationSlot());
				InventoryUpdate iu = new InventoryUpdate();
				for (L2ItemInstance item : unequiped)
				{
					item.unChargeAllShots();
					iu.addModifiedItem(item);
				}
				player.sendPacket(iu);
				player.broadcastUserInfo();
			}
			
			if (getItemLocation() != ItemLocation.WAREHOUSE)
			{
				// destroy
				player.getInventory().destroyItem("L2ItemInstance", this, player, null);
				
				// send update
				InventoryUpdate iu = new InventoryUpdate();
				iu.addRemovedItem(this);
				player.sendPacket(iu);
				
				StatusUpdate su = new StatusUpdate(player);
				su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
				player.sendPacket(su);
				
			}
			else
			{
				player.getWarehouse().destroyItem("L2ItemInstance", this, player, null);
			}
			player.sendPacket(SystemMessageId.TIME_LIMITED_ITEM_DELETED);
			// delete from world
			L2World.getInstance().removeObject(this);
		}
	}
	
	public void scheduleLifeTimeTask()
	{
		if (!isTimeLimitedItem())
		{
			return;
		}
		if (getRemainingTime() <= 0)
		{
			endOfLife();
		}
		else
		{
			if (_lifeTimeTask != null)
			{
				_lifeTimeTask.cancel(false);
			}
			_lifeTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleLifeTimeTask(this), getRemainingTime());
		}
	}
	
	public static class ScheduleLifeTimeTask implements Runnable
	{
		private static final Logger _log = LoggerFactory.getLogger(ScheduleLifeTimeTask.class);
		private final L2ItemInstance _limitedItem;
		
		public ScheduleLifeTimeTask(L2ItemInstance item)
		{
			_limitedItem = item;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_limitedItem != null)
				{
					_limitedItem.endOfLife();
				}
			}
			catch (Exception e)
			{
				_log.error("", e);
			}
		}
	}
	
	public void updateElementAttrBonus(L2PcInstance player)
	{
		if (_elementals == null)
		{
			return;
		}
		for (Elementals elm : _elementals)
		{
			elm.updateBonus(player, isArmor());
		}
	}
	
	public void removeElementAttrBonus(L2PcInstance player)
	{
		if (_elementals == null)
		{
			return;
		}
		for (Elementals elm : _elementals)
		{
			elm.removeBonus(player);
		}
	}
	
	public void setDropperObjectId(int id)
	{
		_dropperObjectId = id;
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if (_dropperObjectId != 0)
		{
			activeChar.sendPacket(new DropItem(this, _dropperObjectId));
		}
		else
		{
			activeChar.sendPacket(new SpawnItem(this));
		}
	}
	
	public final DropProtection getDropProtection()
	{
		return _dropProtection;
	}
	
	public boolean isPublished()
	{
		return _published;
	}
	
	public void publish()
	{
		_published = true;
	}
	
	@Override
	public void decayMe()
	{
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().removeObject(this);
		}
		super.decayMe();
	}
	
	public boolean isQuestItem()
	{
		return getItem().isQuestItem();
	}
	
	public boolean isElementable()
	{
		if ((getItemLocation() == ItemLocation.INVENTORY) || (getItemLocation() == ItemLocation.PAPERDOLL))
		{
			return getItem().isElementable();
		}
		return false;
	}
	
	public boolean isFreightable()
	{
		return getItem().isFreightable();
	}
	
	public int useSkillDisTime()
	{
		return getItem().useSkillDisTime();
	}
	
	public int getOlyEnchantLevel()
	{
		L2PcInstance player = getActingPlayer();
		int enchant = getEnchantLevel();
		
		if (player == null)
		{
			return enchant;
		}
		
		if (player.isInOlympiadMode() && (Config.ALT_OLY_ENCHANT_LIMIT >= 0) && (enchant > Config.ALT_OLY_ENCHANT_LIMIT))
		{
			enchant = Config.ALT_OLY_ENCHANT_LIMIT;
		}
		
		return enchant;
	}
	
	public int getDefaultEnchantLevel()
	{
		return _item.getDefaultEnchantLevel();
	}
	
	public boolean hasPassiveSkills()
	{
		return (getItemType() == EtcItemType.RUNE) && (getItemLocation() == ItemLocation.INVENTORY) && (getOwnerId() > 0) && getItem().hasSkills();
	}
	
	public void giveSkillsToOwner()
	{
		if (!hasPassiveSkills())
		{
			return;
		}
		
		final L2PcInstance player = getActingPlayer();
		
		if (player != null)
		{
			for (SkillHolder sh : getItem().getSkills())
			{
				if (sh.getSkill().isPassive())
				{
					player.addSkill(sh.getSkill(), false);
				}
			}
		}
	}
	
	public void removeSkillsFromOwner()
	{
		if (!hasPassiveSkills())
		{
			return;
		}
		
		final L2PcInstance player = getActingPlayer();
		
		if (player != null)
		{
			for (SkillHolder sh : getItem().getSkills())
			{
				if (sh.getSkill().isPassive())
				{
					player.removeSkill(sh.getSkill(), false, true);
				}
			}
		}
	}
	
	@Override
	public boolean isItem()
	{
		return true;
	}
	
	@Override
	public L2PcInstance getActingPlayer()
	{
		return L2World.getInstance().getPlayer(getOwnerId());
	}
	
	public int getEquipReuseDelay()
	{
		return _item.getEquipReuseDelay();
	}
	
	/**
	 * @param activeChar
	 * @param command
	 */
	public void onBypassFeedback(L2PcInstance activeChar, String command)
	{
		if (command.startsWith("Quest"))
		{
			String questName = command.substring(6);
			String content = null;
			
			String event = null;
			int idx = questName.indexOf(' ');
			if (idx > 0)
			{
				event = questName.substring(idx).trim();
				questName = questName.substring(0, idx);
			}
			
			Quest q = QuestManager.getInstance().getQuest(questName);
			QuestState qs = activeChar.getQuestState(questName);
			
			if (q != null)
			{
				if (((q.getId() >= 1) && (q.getId() < 20000)) && ((activeChar.getWeightPenalty() >= 3) || !activeChar.isInventoryUnder90(true)))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT));
					return;
				}
				
				if (qs == null)
				{
					if ((q.getId() >= 1) && (q.getId() < 20000))
					{
						if (activeChar.getAllActiveQuests().length > 40)
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TOO_MANY_QUESTS));
							return;
						}
					}
					qs = q.newQuestState(activeChar);
				}
			}
			else
			{
				content = Quest.getNoQuestMsg(activeChar);
			}
			
			if (qs != null)
			{
				if ((event != null) && !qs.getQuest().notifyItemEvent(this, activeChar, event))
				{
					return;
				}
				else if (!qs.getQuest().notifyItemTalk(this, activeChar))
				{
					return;
				}
				
				questName = qs.getQuest().getName();
				String stateId = State.getStateName(qs.getState());
				String path = "data/scripts/quests/" + questName + "/" + stateId + ".htm";
				content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), path);
			}
			
			if (content != null)
			{
				showChatWindow(activeChar, content);
			}
			
			// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	/**
	 * @param activeChar
	 * @param content
	 */
	public void showChatWindow(L2PcInstance activeChar, String content)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0, getId());
		html.setHtml(content);
		html.replace("%itemId%", String.valueOf(getObjectId()));
		activeChar.sendPacket(html);
	}
	
	@Override
	public boolean isChargedShot(ShotType type)
	{
		return (_shotsMask & type.getMask()) == type.getMask();
	}
	
	@Override
	public void setChargedShot(ShotType type, boolean charged)
	{
		if (charged)
		{
			_shotsMask |= type.getMask();
		}
		else
		{
			_shotsMask &= ~type.getMask();
		}
	}
	
	public void unChargeAllShots()
	{
		_shotsMask = 0;
	}
	
	/**
	 * Returns enchant effect object for this item
	 * @return enchanteffect
	 */
	public int[] getEnchantOptions()
	{
		EnchantOptions op = EnchantItemOptionsData.getInstance().getOptions(this);
		if (op != null)
		{
			return op.getOptions();
		}
		return DEFAULT_ENCHANT_OPTIONS;
	}
	
	/**
	 * Clears all the enchant bonuses if item is enchanted and containing bonuses for enchant value.
	 */
	public void clearEnchantStats()
	{
		final L2PcInstance player = getActingPlayer();
		if (player == null)
		{
			_enchantOptions.clear();
			return;
		}
		
		for (Options op : _enchantOptions)
		{
			op.remove(player);
		}
		_enchantOptions.clear();
	}
	
	/**
	 * Clears and applies all the enchant bonuses if item is enchanted and containing bonuses for enchant value.
	 */
	public void applyEnchantStats()
	{
		final L2PcInstance player = getActingPlayer();
		if (!isEquipped() || (player == null) || (getEnchantOptions() == DEFAULT_ENCHANT_OPTIONS))
		{
			return;
		}
		
		for (int id : getEnchantOptions())
		{
			final Options options = OptionsData.getInstance().getOptions(id);
			if (options != null)
			{
				options.apply(player);
				_enchantOptions.add(options);
			}
			else if (id != 0)
			{
				_log.info("applyEnchantStats: Couldn't find option: " + id);
			}
		}
	}
	
	// LISTENERS
	/**
	 * Fires all the DropListener.onPickup() methods, if any
	 * @param actor
	 * @return false if the item cannot be picked up by the given player
	 */
	private boolean firePickupListeners(L2PcInstance actor)
	{
		if (!dropListeners.isEmpty() && (actor != null))
		{
			ItemPickupEvent event = new ItemPickupEvent();
			event.setItem(this);
			event.setPicker(actor);
			event.setLocation(new Location(getX(), getY(), getZ()));
			for (DropListener listener : dropListeners)
			{
				if (!listener.onPickup(event))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Fires all the AugmentListener.onAugment() methods, if any
	 * @param isAugment
	 * @param augmentation
	 * @return false if the operation is not allowed
	 */
	private boolean fireAugmentListeners(boolean isAugment, L2Augmentation augmentation)
	{
		if (!augmentListeners.isEmpty() && (augmentation != null))
		{
			AugmentEvent event = new AugmentEvent();
			event.setAugmentation(augmentation);
			event.setIsAugment(isAugment);
			event.setItem(this);
			for (AugmentListener listener : augmentListeners)
			{
				if (isAugment)
				{
					if (!listener.onAugment(event))
					{
						return false;
					}
				}
				else
				{
					if (!listener.onRemoveAugment(event))
					{
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private boolean fireDropListeners(L2Character dropper, Location loc)
	{
		if (!dropListeners.isEmpty() && (dropper != null))
		{
			ItemDropEvent event = new ItemDropEvent();
			event.setDropper(dropper);
			event.setItem(this);
			event.setLocation(loc);
			for (DropListener listener : dropListeners)
			{
				if (!listener.onDrop(event))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Adds an augmentation listener
	 * @param listener
	 */
	public static void addAugmentListener(AugmentListener listener)
	{
		if (!augmentListeners.contains(listener))
		{
			augmentListeners.add(listener);
		}
	}
	
	/**
	 * Removes an augmentation listener
	 * @param listener
	 */
	public static void removeAugmentListener(AugmentListener listener)
	{
		augmentListeners.remove(listener);
	}
	
	/**
	 * Adds a drop / pickup listener
	 * @param listener
	 */
	public static void addDropListener(DropListener listener)
	{
		if (!dropListeners.contains(listener))
		{
			dropListeners.add(listener);
		}
	}
	
	/**
	 * Removes a drop / pickup listener
	 * @param listener
	 */
	public static void removeDropListener(DropListener listener)
	{
		dropListeners.remove(listener);
	}
	
	public void deleteMe()
	{
		if ((_lifeTimeTask != null) && !_lifeTimeTask.isDone())
		{
			_lifeTimeTask.cancel(false);
			_lifeTimeTask = null;
		}
	}
}
