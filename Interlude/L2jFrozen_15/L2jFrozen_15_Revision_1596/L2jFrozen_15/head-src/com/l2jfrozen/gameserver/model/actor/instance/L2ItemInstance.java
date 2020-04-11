package com.l2jfrozen.gameserver.model.actor.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.gameserver.managers.ItemsOnGroundManager;
import com.l2jfrozen.gameserver.model.DropProtection;
import com.l2jfrozen.gameserver.model.L2Augmentation;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.actor.knownlist.NullKnownList;
import com.l2jfrozen.gameserver.model.extender.BaseExtender.EventType;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.funcs.Func;
import com.l2jfrozen.gameserver.templates.L2Armor;
import com.l2jfrozen.gameserver.templates.L2EtcItem;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2Weapon;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.IllegalPlayerAction;
import com.l2jfrozen.gameserver.util.Util;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class manages items.
 * @version $Revision: 1.4.2.1.2.11 $ $Date: 2005/03/31 16:07:50 $
 */
public final class L2ItemInstance extends L2Object
{
	private static final Logger LOGGER = Logger.getLogger(L2ItemInstance.class);
	
	private static final String SELECT_AUGMENTATIONS_BY_ITEM_OBJECT_ID = "SELECT attributes,skill,level FROM augmentations WHERE item_object_id=?";
	private static final String DELETE_ITEM_BY_OBJ_ID = "DELETE FROM items WHERE object_id=?";
	private static final String INSERT_ITEM = "INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,price_sell,price_buy,object_id,custom_type1,custom_type2,mana_left) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String UPDATE_ITEM_BY_ITEM_OBJ_ID = "UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,price_sell=?,price_buy=?,custom_type1=?,custom_type2=?,mana_left=? WHERE object_id=?";
	
	private static final java.util.logging.Logger logItems = java.util.logging.Logger.getLogger("item");
	
	private final DropProtection dropProtection = new DropProtection();
	
	public static enum ItemLocation
	{
		VOID,
		INVENTORY,
		PAPERDOLL,
		WAREHOUSE,
		CLANWH,
		PET,
		PET_EQUIP,
		LEASE,
		FREIGHT
	}
	
	private int ownerId;
	
	/** Quantity of the item. */
	private int itemCount;
	
	/** Initial Quantity of the item. */
	private int initCount;
	
	/** Time after restore Item count (in Hours). */
	private int itemTime;
	
	/** Quantity of the item can decrease. */
	private boolean decrease = false;
	
	/** ID of the item. */
	private final int itemId;
	
	/** Object L2Item associated to the item. */
	private final L2Item itemInstance;
	
	/** Location of the item : Inventory, PaperDoll, WareHouse. */
	private ItemLocation itemLoc;
	
	/** Slot where item is stored. */
	private int locData;
	
	/** Level of enchantment of the item. */
	private int itemEnchantLevel;
	
	/** Price of the item for selling. */
	private int priceSell;
	
	/** Price of the item for buying. */
	private int priceBuy;
	
	/** Wear Item. */
	private boolean wear;
	
	/** Augmented Item. */
	private L2Augmentation itemAugmentation = null;
	
	/** Shadow item. */
	private int mana = -1;
	
	private boolean consumingMana = false;
	
	/** The Constant MANA_CONSUMPTION_RATE. */
	private static final int MANA_CONSUMPTION_RATE = 60000;
	
	/** Custom item types (used loto, race tickets). */
	private int type1;
	
	private int type2;
	
	private long dropTime;
	
	public static final int CHARGED_NONE = 0;
	public static final int CHARGED_SOULSHOT = 1;
	public static final int CHARGED_SPIRITSHOT = 1;
	public static final int CHARGED_BLESSED_SOULSHOT = 2; // It's a realy exists? ;-)
	public static final int CHARGED_BLESSED_SPIRITSHOT = 2;
	
	/** Item charged with SoulShot (type of SoulShot). */
	private int chargedSoulshot = CHARGED_NONE;
	
	/** Item charged with SpiritShot (type of SpiritShot). */
	private int chargedSpiritshot = CHARGED_NONE;
	
	private boolean chargedFishtshot = false;
	
	private boolean protectedItem;
	
	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int REMOVED = 3;
	public static final int MODIFIED = 2;
	
	private int lastChange = 2; // 1 ??, 2 modified, 3 removed
	
	private boolean existsInDb; // if a record exists in DB.
	
	private boolean storedInDb; // if DB data is up-to-date.
	
	/** The item loot shedule. */
	private ScheduledFuture<?> itemLootShedule = null;
	
	/**
	 * Constructor of the L2ItemInstance from the objectId and the itemId.
	 * @param  objectId                 : int designating the ID of the object in the world
	 * @param  itemId                   : int designating the ID of the item
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public L2ItemInstance(final int objectId, final int itemId) throws IllegalArgumentException
	{
		this(objectId, ItemTable.getInstance().getTemplate(itemId));
	}
	
	/**
	 * Constructor of the L2ItemInstance from the objetId and the description of the item given by the L2Item.
	 * @param  objectId                 : int designating the ID of the object in the world
	 * @param  item                     : L2Item containing informations of the item
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public L2ItemInstance(final int objectId, final L2Item item) throws IllegalArgumentException
	{
		super(objectId);
		
		if (item == null)
		{
			throw new IllegalArgumentException();
		}
		
		super.setKnownList(new NullKnownList(this));
		
		itemId = item.getItemId();
		itemInstance = item;
		itemCount = 1;
		itemLoc = ItemLocation.VOID;
		mana = itemInstance.getDuration();
	}
	
	/**
	 * Sets the ownerID of the item.
	 * @param process   : String Identifier of process triggering this action
	 * @param owner_id  : int designating the ID of the owner
	 * @param creator   : L2PcInstance Player requesting the item creation
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void setOwnerId(final String process, final int owner_id, final L2PcInstance creator, final L2Object reference)
	{
		final int oldOwner = ownerId;
		setOwnerId(owner_id);
		
		fireEvent(EventType.SETOWNER.name, new Object[]
		{
			process,
			oldOwner
		});
	}
	
	/**
	 * Sets the ownerID of the item.
	 * @param owner_id : int designating the ID of the owner
	 */
	public void setOwnerId(final int owner_id)
	{
		if (owner_id == ownerId)
		{
			return;
		}
		
		ownerId = owner_id;
		storedInDb = false;
	}
	
	/**
	 * Returns the ownerID of the item.
	 * @return int : ownerID of the item
	 */
	public int getOwnerId()
	{
		return ownerId;
	}
	
	/**
	 * Sets the location of the item.
	 * @param loc : ItemLocation (enumeration)
	 */
	public void setLocation(final ItemLocation loc)
	{
		setLocation(loc, 0);
	}
	
	/**
	 * Sets the location of the item.<BR>
	 * <BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * @param loc      : ItemLocation (enumeration)
	 * @param loc_data : int designating the slot where the item is stored or the village for freights
	 */
	public void setLocation(final ItemLocation loc, final int loc_data)
	{
		if (loc == itemLoc && loc_data == locData)
		{
			return;
		}
		itemLoc = loc;
		locData = loc_data;
		storedInDb = false;
	}
	
	public ItemLocation getLocation()
	{
		return itemLoc;
	}
	
	public boolean isPotion()
	{
		return itemInstance.isPotion();
	}
	
	/**
	 * @return the quantity of item.
	 */
	public int getCount()
	{
		return itemCount;
	}
	
	/**
	 * Sets the quantity of the item.<BR>
	 * <BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * @param process   : String Identifier of process triggering this action
	 * @param count     : int
	 * @param creator   : L2PcInstance Player requesting the item creation
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void changeCount(final String process, final int count, final L2PcInstance creator, final L2Object reference)
	{
		if (count == 0)
		{
			return;
		}
		
		if (count > 0 && itemCount > Integer.MAX_VALUE - count)
		{
			itemCount = Integer.MAX_VALUE;
		}
		else
		{
			itemCount += count;
		}
		
		if (itemCount < 0)
		{
			itemCount = 0;
		}
		
		storedInDb = false;
		
		if (Config.LOG_ITEMS)
		{
			LogRecord record = new LogRecord(Level.INFO, "CHANGE:" + process);
			record.setLoggerName("item");
			record.setParameters(new Object[]
			{
				this,
				creator,
				reference
			});
			logItems.log(record);
			record = null;
		}
	}
	
	// No logging (function designed for shots only)
	/**
	 * Change count without trace.
	 * @param process   the process
	 * @param count     the count
	 * @param creator   the creator
	 * @param reference the reference
	 */
	public void changeCountWithoutTrace(final String process, final int count, final L2PcInstance creator, final L2Object reference)
	{
		if (count == 0)
		{
			return;
		}
		if (count > 0 && itemCount > Integer.MAX_VALUE - count)
		{
			itemCount = Integer.MAX_VALUE;
		}
		else
		{
			itemCount += count;
		}
		if (itemCount < 0)
		{
			itemCount = 0;
		}
		
		storedInDb = false;
	}
	
	/**
	 * Sets the quantity of the item.<BR>
	 * <BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * @param count : int
	 */
	public void setCount(final int count)
	{
		if (itemCount == count)
		{
			return;
		}
		
		itemCount = count >= -1 ? count : 0;
		storedInDb = false;
	}
	
	public boolean isEquipable()
	{
		return !(itemInstance.getBodyPart() == 0 || itemInstance instanceof L2EtcItem);
	}
	
	public boolean isEquipped()
	{
		return itemLoc == ItemLocation.PAPERDOLL || itemLoc == ItemLocation.PET_EQUIP;
	}
	
	/**
	 * @return the slot where the item is stored.
	 */
	public int getEquipSlot()
	{
		if (Config.ASSERT)
		{
			assert itemLoc == ItemLocation.PAPERDOLL || itemLoc == ItemLocation.PET_EQUIP || itemLoc == ItemLocation.FREIGHT;
		}
		
		return locData;
	}
	
	/**
	 * Returns the characteristics of the item.
	 * @return L2Item
	 */
	public L2Item getItem()
	{
		return itemInstance;
	}
	
	public int getCustomType1()
	{
		return type1;
	}
	
	public int getCustomType2()
	{
		return type2;
	}
	
	public void setCustomType1(final int newtype)
	{
		type1 = newtype;
	}
	
	public void setCustomType2(final int newtype)
	{
		type2 = newtype;
	}
	
	/**
	 * Sets the drop time.
	 * @param time the new drop time
	 */
	public void setDropTime(final long time)
	{
		dropTime = time;
	}
	
	/**
	 * Gets the drop time.
	 * @return the drop time
	 */
	public long getDropTime()
	{
		return dropTime;
	}
	
	public boolean isCupidBow()
	{
		if (getItemId() == 9140 || getItemId() == 9141)
		{
			return true;
		}
		return false;
	}
	
	public boolean isWear()
	{
		return wear;
	}
	
	/**
	 * Sets the wear.
	 * @param newwear the new wear
	 */
	public void setWear(final boolean newwear)
	{
		wear = newwear;
	}
	
	/**
	 * Returns the type of item.
	 * @return Enum
	 */
	public Enum<?> getItemType()
	{
		return itemInstance.getItemType();
	}
	
	/**
	 * Returns the ID of the item.
	 * @return int
	 */
	public int getItemId()
	{
		return itemId;
	}
	
	/**
	 * Returns the quantity of crystals for crystallization.
	 * @return int
	 */
	public final int getCrystalCount()
	{
		return itemInstance.getCrystalCount(itemEnchantLevel);
	}
	
	/**
	 * Returns the reference price of the item.
	 * @return int
	 */
	public int getReferencePrice()
	{
		return itemInstance.getReferencePrice();
	}
	
	/**
	 * Returns the name of the item.
	 * @return String
	 */
	public String getItemName()
	{
		return itemInstance.getName();
	}
	
	/**
	 * Returns the price of the item for selling.
	 * @return int
	 */
	public int getPriceToSell()
	{
		return isConsumable() ? (int) (priceSell * Config.RATE_CONSUMABLE_COST) : priceSell;
	}
	
	/**
	 * Sets the price of the item for selling <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date.
	 * @param price : int designating the price
	 */
	public void setPriceToSell(final int price)
	{
		priceSell = price;
		storedInDb = false;
	}
	
	public int getLastChange()
	{
		return lastChange;
	}
	
	/**
	 * Sets the last change of the item.
	 * @param lastChange : int
	 */
	public void setLastChange(final int lastChange)
	{
		this.lastChange = lastChange;
	}
	
	public boolean isStackable()
	{
		return itemInstance.isStackable();
	}
	
	public boolean isDropable()
	{
		return isAugmented() ? false : itemInstance.isDropable();
	}
	
	public boolean isDestroyable()
	{
		return itemInstance.isDestroyable();
	}
	
	public boolean isTradeable()
	{
		return isAugmented() ? false : itemInstance.isTradeable();
	}
	
	public boolean isConsumable()
	{
		return itemInstance.isConsumable();
	}
	
	/**
	 * Returns if item is available for manipulation.
	 * @param  player        the player
	 * @param  allowAdena    the allow adena
	 * @param  allowEquipped
	 * @return               boolean
	 */
	public boolean isAvailable(final L2PcInstance player, final boolean allowAdena, final boolean allowEquipped)
	{
		return (!isEquipped() || allowEquipped) && getItem().getType2() != L2Item.TYPE2_QUEST && (getItem().getType2() != L2Item.TYPE2_MONEY || getItem().getType1() != L2Item.TYPE1_SHIELD_ARMOR) // TODO: what does this mean?
			&& (player.getPet() == null || getObjectId() != player.getPet().getControlItemId()) // Not Control item of currently summoned pet
			&& player.getActiveEnchantItem() != this && (allowAdena || getItemId() != 57) && (player.getCurrentSkill() == null || player.getCurrentSkill().getSkill().getItemConsumeId() != getItemId()) && isTradeable();
	}
	
	@Override
	public void onAction(final L2PcInstance player)
	{
		// this causes the validate position handler to do the pickup if the location is reached.
		// mercenary tickets can only be picked up by the castle owner and GMs.
		if ((!player.isGM()) && (itemId >= 3960 && itemId <= 4021 && player.isInParty() || itemId >= 3960 && itemId <= 3969 && !player.isCastleLord(1) || itemId >= 3973 && itemId <= 3982 && !player.isCastleLord(2) || itemId >= 3986 && itemId <= 3995 && !player.isCastleLord(3) || itemId >= 3999 && itemId <= 4008 && !player.isCastleLord(4)
			|| itemId >= 4012 && itemId <= 4021 && !player.isCastleLord(5) || itemId >= 5205 && itemId <= 5214 && !player.isCastleLord(6) || itemId >= 6779 && itemId <= 6788 && !player.isCastleLord(7) || itemId >= 7973 && itemId <= 7982 && !player.isCastleLord(8) || itemId >= 7918 && itemId <= 7927 && !player.isCastleLord(9)))
		{
			if (player.isInParty())
			{
				player.sendMessage("You cannot pickup mercenaries while in a party.");
			}
			else
			{
				player.sendMessage("Only the castle lord can pickup mercenaries.");
			}
			
			player.setTarget(this);
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			if (player.getFreight().getItemByObjectId(getObjectId()) != null)
			{
				player.setTarget(this);
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
				player.sendPacket(ActionFailed.STATIC_PACKET);
				
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to pickup Freight Items", IllegalPlayerAction.PUNISH_KICK);
			}
			else
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this);
			}
		}
	}
	
	/**
	 * @return the level of enchantment of the item. +0, +1, +2, +3
	 */
	public int getEnchantLevel()
	{
		return itemEnchantLevel;
	}
	
	/**
	 * @param enchantLevel the level of enchantment of the item. +0, +1, +2, +3
	 */
	public void setEnchantLevel(final int enchantLevel)
	{
		if (itemEnchantLevel == enchantLevel)
		{
			return;
		}
		itemEnchantLevel = enchantLevel;
		storedInDb = false;
	}
	
	/**
	 * Returns the physical defense of the item.
	 * @return int
	 */
	public int getPDef()
	{
		if (itemInstance instanceof L2Armor)
		{
			return ((L2Armor) itemInstance).getPDef();
		}
		return 0;
	}
	
	public boolean isAugmented()
	{
		return itemAugmentation == null ? false : true;
	}
	
	public L2Augmentation getAugmentation()
	{
		return itemAugmentation;
	}
	
	/**
	 * Sets a new augmentation.
	 * @param  augmentation the augmentation
	 * @return              return true if sucessfull
	 */
	public boolean setAugmentation(final L2Augmentation augmentation)
	{
		// there shall be no previous augmentation..
		if (itemAugmentation != null)
		{
			return false;
		}
		itemAugmentation = augmentation;
		return true;
	}
	
	public void removeAugmentation()
	{
		if (itemAugmentation == null)
		{
			return;
		}
		itemAugmentation.deleteAugmentationData();
		itemAugmentation = null;
	}
	
	/**
	 * Used to decrease mana (mana means life time for shadow items).
	 */
	public class ScheduleConsumeManaTask implements Runnable
	{
		private final L2ItemInstance shadowItem;
		
		public ScheduleConsumeManaTask(final L2ItemInstance item)
		{
			shadowItem = item;
		}
		
		@Override
		public void run()
		{
			try
			{
				// decrease mana
				if (shadowItem != null)
				{
					shadowItem.decreaseMana(true);
				}
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
	 * @return true if this item is a shadow item or have a limited life-time.
	 */
	public boolean isShadowItem()
	{
		return mana >= 0;
	}
	
	/**
	 * Sets the mana for this shadow item <b>NOTE</b>: does not send an inventory update packet.
	 * @param mana the new mana
	 */
	public void setMana(final int mana)
	{
		this.mana = mana;
	}
	
	/**
	 * Returns the remaining mana of this shadow item.
	 * @return lifeTime
	 */
	public int getMana()
	{
		return mana;
	}
	
	/**
	 * Decreases the mana of this shadow item, sends a inventory update schedules a new consumption task if non is running optionally one could force a new task.
	 * @param resetConsumingMana the reset consuming mana
	 */
	public void decreaseMana(final boolean resetConsumingMana)
	{
		if (!isShadowItem())
		{
			return;
		}
		
		if (mana > 0)
		{
			mana--;
		}
		
		if (storedInDb)
		{
			storedInDb = false;
		}
		if (resetConsumingMana)
		{
			consumingMana = false;
		}
		
		L2PcInstance player = (L2PcInstance) L2World.getInstance().findObject(getOwnerId());
		if (player != null)
		{
			SystemMessage sm;
			switch (mana)
			{
				case 10:
					sm = new SystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_10);
					sm.addString(getItemName());
					player.sendPacket(sm);
					break;
				case 5:
					sm = new SystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_5);
					sm.addString(getItemName());
					player.sendPacket(sm);
					break;
				case 1:
					sm = new SystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_1);
					sm.addString(getItemName());
					player.sendPacket(sm);
					break;
			}
			
			if (mana == 0) // The life time has expired
			{
				sm = new SystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_0);
				sm.addString(getItemName());
				player.sendPacket(sm);
				
				// unequip
				if (isEquipped())
				{
					L2ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(getEquipSlot());
					InventoryUpdate iu = new InventoryUpdate();
					
					for (final L2ItemInstance element : unequiped)
					{
						player.checkSSMatch(null, element);
						iu.addModifiedItem(element);
					}
					
					player.sendPacket(iu);
					
					unequiped = null;
					iu = null;
				}
				
				if (getLocation() != ItemLocation.WAREHOUSE)
				{
					// destroy
					player.getInventory().destroyItem("L2ItemInstance", this, player, null);
					
					// send update
					InventoryUpdate iu = new InventoryUpdate();
					iu.addRemovedItem(this);
					player.sendPacket(iu);
					iu = null;
					
					StatusUpdate su = new StatusUpdate(player.getObjectId());
					su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
					player.sendPacket(su);
					su = null;
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
				if (!consumingMana && isEquipped())
				{
					scheduleConsumeManaTask();
				}
				
				if (getLocation() != ItemLocation.WAREHOUSE)
				{
					InventoryUpdate iu = new InventoryUpdate();
					iu.addModifiedItem(this);
					player.sendPacket(iu);
					iu = null;
				}
			}
			
			sm = null;
		}
		
		player = null;
	}
	
	private void scheduleConsumeManaTask()
	{
		consumingMana = true;
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleConsumeManaTask(this), MANA_CONSUMPTION_RATE);
	}
	
	/**
	 * @param  attacker the attacker
	 * @return          Returns false cause item can't be attacked.
	 */
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		return false;
	}
	
	/**
	 * Returns the type of charge with SoulShot of the item.
	 * @return int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public int getChargedSoulshot()
	{
		return chargedSoulshot;
	}
	
	/**
	 * Returns the type of charge with SpiritShot of the item.
	 * @return int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public int getChargedSpiritshot()
	{
		return chargedSpiritshot;
	}
	
	/**
	 * Gets the charged fishshot.
	 * @return the charged fishshot
	 */
	public boolean getChargedFishshot()
	{
		return chargedFishtshot;
	}
	
	/**
	 * Sets the type of charge with SoulShot of the item.
	 * @param type : int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public void setChargedSoulshot(final int type)
	{
		chargedSoulshot = type;
	}
	
	/**
	 * Sets the type of charge with SpiritShot of the item.
	 * @param type : int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public void setChargedSpiritshot(final int type)
	{
		chargedSpiritshot = type;
	}
	
	/**
	 * Sets the charged fishshot.
	 * @param type the new charged fishshot
	 */
	public void setChargedFishshot(final boolean type)
	{
		chargedFishtshot = type;
	}
	
	/**
	 * This function basically returns a set of functions from L2Item/L2Armor/L2Weapon, but may add additional functions, if this particular item instance is enhanched for a particular player.
	 * @param  player : L2Character designating the player
	 * @return        Func[]
	 */
	public Func[] getStatFuncs(final L2Character player)
	{
		return getItem().getStatFuncs(this, player);
	}
	
	/**
	 * Updates database.<BR>
	 * <BR>
	 * <U><I>Concept : </I></U><BR>
	 * <B>IF</B> the item exists in database :
	 * <UL>
	 * <LI><B>IF</B> the item has no owner, or has no location, or has a null quantity : remove item from database</LI>
	 * <LI><B>ELSE</B> : update item in database</LI>
	 * </UL>
	 * <B> Otherwise</B> :
	 * <UL>
	 * <LI><B>IF</B> the item hasn't a null quantity, and has a correct location, and has a correct owner : insert item in database</LI>
	 * </UL>
	 */
	public void updateDatabase()
	{
		// LOGGER.info("Item: "+getItemId()+" Loc: "+_loc.name()+" ExistInDb: "+_existsInDb+" owner: "+_ownerId);
		
		if (isWear())
		{
			return;
		}
		
		if (existsInDb)
		{
			if (ownerId == 0 || itemLoc == ItemLocation.VOID || itemCount == 0 && itemLoc != ItemLocation.LEASE)
			{
				removeFromDb();
			}
			else
			{
				updateInDb();
			}
		}
		else
		{
			if (itemCount == 0 && itemLoc != ItemLocation.LEASE)
			{
				return;
			}
			
			if (itemLoc == ItemLocation.VOID || ownerId == 0)
			{
				return;
			}
			
			insertIntoDb();
		}
	}
	
	/**
	 * Returns a L2ItemInstance stored in database from its objectID.
	 * @param  rset ResultSet from "Inventory.java" or "ItemContainer.java"
	 * @return      L2ItemInstance
	 */
	public static L2ItemInstance restoreFromDb(ResultSet rset)
	{
		L2ItemInstance inst = null;
		int itemObjectId = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int owner_id = rset.getInt("owner_id");
			int objectId = rset.getInt("object_id");
			itemObjectId = objectId;
			int item_id = rset.getInt("item_id");
			int count = rset.getInt("count");
			
			ItemLocation loc = ItemLocation.valueOf(rset.getString("loc"));
			
			int loc_data = rset.getInt("loc_data");
			int enchant_level = rset.getInt("enchant_level");
			int custom_type1 = rset.getInt("custom_type1");
			int custom_type2 = rset.getInt("custom_type2");
			int price_sell = rset.getInt("price_sell");
			int price_buy = rset.getInt("price_buy");
			int manaLeft = rset.getInt("mana_left");
			
			L2Item item = ItemTable.getInstance().getTemplate(item_id);
			
			if (item == null)
			{
				LOGGER.warn("Item item_id=" + item_id + " not known, object_id=" + objectId);
				return null;
			}
			
			inst = new L2ItemInstance(objectId, item);
			inst.existsInDb = true;
			inst.storedInDb = true;
			inst.ownerId = owner_id;
			inst.itemCount = count;
			inst.itemEnchantLevel = enchant_level;
			inst.type1 = custom_type1;
			inst.type2 = custom_type2;
			inst.itemLoc = loc;
			inst.locData = loc_data;
			inst.priceSell = price_sell;
			inst.priceBuy = price_buy;
			
			// Setup life time for shadow weapons
			inst.mana = manaLeft;
			
			// consume 1 mana
			if (inst.mana > 0 && inst.getLocation() == ItemLocation.PAPERDOLL)
			{
				inst.decreaseMana(false);
			}
			
			// if mana left is 0 delete this item
			if (inst.mana == 0)
			{
				inst.removeFromDb();
				return null;
			}
			else if (inst.mana > 0 && inst.getLocation() == ItemLocation.PAPERDOLL)
			{
				inst.scheduleConsumeManaTask();
			}
			
			// load augmentation
			try (PreparedStatement pst = con.prepareStatement(SELECT_AUGMENTATIONS_BY_ITEM_OBJECT_ID))
			{
				pst.setInt(1, objectId);
				
				try (ResultSet rs = pst.executeQuery())
				{
					if (rs.next())
					{
						inst.itemAugmentation = new L2Augmentation(inst, rs.getInt("attributes"), rs.getInt("skill"), rs.getInt("level"), false);
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("L2ItemInstance.restoreFromDb : Could not restore item " + itemObjectId + " from DB", e);
		}
		
		if (inst != null)
		{
			inst.fireEvent(EventType.LOAD.name, new Object[] {});
		}
		
		return inst;
	}
	
	/**
	 * Init a dropped L2ItemInstance and add it in the world as a visible object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the x,y,z position of the L2ItemInstance dropped and update its worldregion</li>
	 * <li>Add the L2ItemInstance dropped to visibleObjects of its L2WorldRegion</li>
	 * <li>Add the L2ItemInstance dropped in the world as a <B>visible</B> object</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to allObjects of L2World </B></FONT><BR>
	 * <BR>
	 * <B><U> Assert </U> :</B><BR>
	 * <BR>
	 * <li>_worldRegion == null <I>(L2Object is invisible at the beginning)</I></li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Drop item</li>
	 * <li>Call Pet</li><BR>
	 * @param dropper the dropper
	 * @param x       the x
	 * @param y       the y
	 * @param z       the z
	 */
	public final void dropMe(final L2Character dropper, int x, int y, int z)
	{
		if (Config.ASSERT)
		{
			assert getPosition().getWorldRegion() == null;
		}
		
		if (Config.GEODATA > 0 && dropper != null)
		{
			Location dropDest = GeoData.getInstance().moveCheck(dropper.getX(), dropper.getY(), dropper.getZ(), x, y, z);
			
			if (dropDest != null && dropDest.getX() != 0 && dropDest.getY() != 0)
			{
				
				x = dropDest.getX();
				y = dropDest.getY();
				z = dropDest.getZ();
				
			}
			
			dropDest = null;
		}
		
		synchronized (this)
		{
			// Set the x,y,z position of the L2ItemInstance dropped and update its worldregion
			setIsVisible(true);
			getPosition().setWorldPosition(x, y, z);
			getPosition().setWorldRegion(L2World.getInstance().getRegion(getPosition().getWorldPosition()));
			
			// Add the L2ItemInstance dropped to visibleObjects of its L2WorldRegion
			getPosition().getWorldRegion().addVisibleObject(this);
		}
		
		setDropTime(System.currentTimeMillis());
		
		// this can synchronize on others instancies, so it's out of
		// synchronized, to avoid deadlocks
		// Add the L2ItemInstance dropped in the world as a visible object
		L2World.getInstance().addVisibleObject(this, getPosition().getWorldRegion(), dropper);
		
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().save(this);
		}
	}
	
	/**
	 * Update the database with values of the item.
	 */
	private void updateInDb()
	{
		if (Config.ASSERT)
		{
			assert existsInDb;
		}
		
		if (wear)
		{
			return;
		}
		
		if (storedInDb)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_ITEM_BY_ITEM_OBJ_ID))
		{
			statement.setInt(1, ownerId);
			statement.setInt(2, getCount());
			statement.setString(3, itemLoc.name());
			statement.setInt(4, locData);
			statement.setInt(5, getEnchantLevel());
			statement.setInt(6, priceSell);
			statement.setInt(7, priceBuy);
			statement.setInt(8, getCustomType1());
			statement.setInt(9, getCustomType2());
			statement.setInt(10, getMana());
			statement.setInt(11, getObjectId());
			statement.executeUpdate();
			existsInDb = true;
			storedInDb = true;
		}
		catch (final Exception e)
		{
			LOGGER.error("Could not update item " + getObjectId() + " in DB: Reason: ", e);
		}
		
		if (existsInDb)
		{
			fireEvent(EventType.STORE.name, (Object[]) null);
		}
	}
	
	/**
	 * Insert the item in database.
	 */
	private void insertIntoDb()
	{
		if (wear)
		{
			return;
		}
		
		if (Config.ASSERT)
		{
			assert !existsInDb && getObjectId() != 0;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_ITEM))
		{
			statement.setInt(1, ownerId);
			statement.setInt(2, itemId);
			statement.setInt(3, getCount());
			statement.setString(4, itemLoc.name());
			statement.setInt(5, locData);
			statement.setInt(6, getEnchantLevel());
			statement.setInt(7, priceSell);
			statement.setInt(8, priceBuy);
			statement.setInt(9, getObjectId());
			statement.setInt(10, type1);
			statement.setInt(11, type2);
			statement.setInt(12, getMana());
			
			statement.executeUpdate();
			existsInDb = true;
			storedInDb = true;
		}
		catch (SQLException e)
		{
			// Duplicate entry
			if (e.getErrorCode() == 1062)
			{
				LOGGER.warn("L2ItemInstance.insertIntoDb : Update item instead of insert one, check player with id " + getOwnerId() + " actions on item " + getObjectId());
				updateInDb();
			}
			else
			{
				LOGGER.error("L2ItemInstance.insertIntoDb: Possible duplicate entry", e);
			}
		}
	}
	
	/**
	 * Delete item from database.
	 */
	private void removeFromDb()
	{
		if (wear)
		{
			return;
		}
		
		if (Config.ASSERT)
		{
			assert existsInDb;
		}
		
		// delete augmentation data
		if (isAugmented())
		{
			itemAugmentation.deleteAugmentationData();
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_ITEM_BY_OBJ_ID))
		{
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			existsInDb = false;
			storedInDb = false;
		}
		catch (final Exception e)
		{
			LOGGER.error("Could not delete item " + getObjectId() + " in DB:", e);
		}
		
		if (!existsInDb)
		{
			fireEvent(EventType.DELETE.name, (Object[]) null);
		}
	}
	
	@Override
	public String toString()
	{
		return getItemName() + "(" + getItemId() + ")";
	}
	
	public void resetOwnerTimer()
	{
		if (itemLootShedule != null)
		{
			itemLootShedule.cancel(true);
		}
		
		itemLootShedule = null;
	}
	
	/**
	 * Sets the item loot shedule.
	 * @param sf the new item loot shedule
	 */
	public void setItemLootShedule(final ScheduledFuture<?> sf)
	{
		itemLootShedule = sf;
	}
	
	/**
	 * Gets the item loot shedule.
	 * @return the item loot shedule
	 */
	public ScheduledFuture<?> getItemLootShedule()
	{
		return itemLootShedule;
	}
	
	public void setProtected(final boolean isProtected)
	{
		protectedItem = isProtected;
	}
	
	public boolean isProtected()
	{
		return protectedItem;
	}
	
	/**
	 * Checks if is night lure.
	 * @return true, if is night lure
	 */
	public boolean isNightLure()
	{
		return itemId >= 8505 && itemId <= 8513 || itemId == 8485;
	}
	
	/**
	 * Sets the count decrease.
	 * @param decrease the new count decrease
	 */
	public void setCountDecrease(final boolean decrease)
	{
		this.decrease = decrease;
	}
	
	/**
	 * Gets the count decrease.
	 * @return the count decrease
	 */
	public boolean getCountDecrease()
	{
		return decrease;
	}
	
	/**
	 * Sets the inits the count.
	 * @param InitCount the new inits the count
	 */
	public void setInitCount(final int InitCount)
	{
		initCount = InitCount;
	}
	
	public int getInitCount()
	{
		return initCount;
	}
	
	public void restoreInitCount()
	{
		if (decrease)
		{
			itemCount = initCount;
		}
	}
	
	public void setTime(final int time)
	{
		if (time > 0)
		{
			itemTime = time;
		}
		else
		{
			itemTime = 0;
		}
	}
	
	public int getTime()
	{
		return itemTime;
	}
	
	/**
	 * Returns the slot where the item is stored.
	 * @return int
	 */
	public int getLocationSlot()
	{
		if (Config.ASSERT)
		{
			assert itemLoc == ItemLocation.PAPERDOLL || itemLoc == ItemLocation.PET_EQUIP || itemLoc == ItemLocation.FREIGHT || itemLoc == ItemLocation.INVENTORY;
		}
		
		return locData;
	}
	
	public final DropProtection getDropProtection()
	{
		return dropProtection;
	}
	
	/**
	 * Checks if is varka ketra ally quest item.
	 * @return true, if is varka ketra ally quest item
	 */
	public boolean isVarkaKetraAllyQuestItem()
	{
		if ((getItemId() >= 7211 && getItemId() <= 7215) || (getItemId() >= 7221 && getItemId() <= 7225))
		{
			return true;
		}
		
		return false;
	}
	
	public boolean isOlyRestrictedItem()
	{
		return (Config.LIST_OLY_RESTRICTED_ITEMS.contains(itemId));
	}
	
	public boolean isHeroItem()
	{
		return ((itemId >= 6611 && itemId <= 6621) || (itemId >= 9388 && itemId <= 9390) || itemId == 6842);
	}
	
	public boolean checkOlympCondition()
	{
		if (isHeroItem() || isOlyRestrictedItem() || isWear() || (!Config.ALT_OLY_AUGMENT_ALLOW && isAugmented()))
		{
			return false;
		}
		return true;
	}
	
	/**
	 * @return true if item is a Weapon/Shield
	 */
	public boolean isWeapon()
	{
		return (itemInstance instanceof L2Weapon);
	}
}
