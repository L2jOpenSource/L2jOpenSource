package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.base.Race;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.EnchantResult;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2WeaponType;
import com.l2jfrozen.gameserver.util.IllegalPlayerAction;
import com.l2jfrozen.gameserver.util.Util;
import com.l2jfrozen.logs.Log;
import com.l2jfrozen.util.random.Rnd;

import main.EngineModsManager;

public final class RequestEnchantItem extends L2GameClientPacket
{
	protected static final Logger LOGGER = Logger.getLogger(Inventory.class);
	private static final int[] CRYSTAL_SCROLLS =
	{
		731,
		732,
		949,
		950,
		953,
		954,
		957,
		958,
		961,
		962
	};
	
	private static final int[] NORMAL_WEAPON_SCROLLS =
	{
		729,
		947,
		951,
		955,
		959
	};
	
	private static final int[] BLESSED_WEAPON_SCROLLS =
	{
		6569,
		6571,
		6573,
		6575,
		6577
	};
	
	private static final int[] CRYSTAL_WEAPON_SCROLLS =
	{
		731,
		949,
		953,
		957,
		961
	};
	
	private static final int[] NORMAL_ARMOR_SCROLLS =
	{
		730,
		948,
		952,
		956,
		960
	};
	
	private static final int[] BLESSED_ARMOR_SCROLLS =
	{
		6570,
		6572,
		6574,
		6576,
		6578
	};
	
	private static final int[] CRYSTAL_ARMOR_SCROLLS =
	{
		732,
		950,
		954,
		958,
		962
	};
	
	private int objectId;
	
	@Override
	protected void readImpl()
	{
		objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null || objectId == 0)
		{
			return;
		}
		
		if (activeChar.getActiveTradeList() != null)
		{
			activeChar.cancelActiveTrade();
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THE_ATTEMP_TO_TRADE_HAS_FAILED));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Fix enchant transactions
		if (activeChar.isProcessingTransaction())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
			activeChar.setActiveEnchantItem(null);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!activeChar.isOnline())
		{
			activeChar.setActiveEnchantItem(null);
			return;
		}
		
		if (activeChar.getPrivateStoreType() != 0 || activeChar.isInStoreMode())
		{
			activeChar.setActiveEnchantItem(null);
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(objectId);
		L2ItemInstance scroll = activeChar.getActiveEnchantItem();
		activeChar.setActiveEnchantItem(null);
		
		if (item == null || scroll == null)
		{
			activeChar.setActiveEnchantItem(null);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// can't enchant rods and shadow items
		if (item.getItem().getItemType() == L2WeaponType.ROD || item.isShadowItem())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
			activeChar.setActiveEnchantItem(null);
			return;
		}
		
		if (!Config.ENCHANT_HERO_WEAPON && item.getItemId() >= 6611 && item.getItemId() <= 6621)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
			activeChar.setActiveEnchantItem(null);
			return;
		}
		
		/*
		 * if(!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), FloodProtector.PROTECTED_ENCHANT)) { activeChar.setActiveEnchantItem(null); activeChar.sendMessage("Enchant failed"); return; }
		 */
		
		if (item.isWear())
		{
			activeChar.setActiveEnchantItem(null);
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to enchant a weared Item", IllegalPlayerAction.PUNISH_KICK);
			return;
		}
		
		final int itemType2 = item.getItem().getType2();
		boolean enchantItem = false;
		boolean blessedScroll = false;
		boolean crystalScroll = false;
		int crystalId = 0;
		
		/** pretty code ;D */
		switch (item.getItem().getCrystalType())
		{
			case L2Item.CRYSTAL_A:
				crystalId = 1461;
				switch (scroll.getItemId())
				{
					case 729:
					case 731:
					case 6569:
						if (itemType2 == L2Item.TYPE2_WEAPON)
						{
							enchantItem = true;
						}
						break;
					case 730:
					case 732:
					case 6570:
						if (itemType2 == L2Item.TYPE2_SHIELD_ARMOR || itemType2 == L2Item.TYPE2_ACCESSORY)
						{
							enchantItem = true;
						}
						break;
				}
				break;
			case L2Item.CRYSTAL_B:
				crystalId = 1460;
				switch (scroll.getItemId())
				{
					case 947:
					case 949:
					case 6571:
						if (itemType2 == L2Item.TYPE2_WEAPON)
						{
							enchantItem = true;
						}
						break;
					case 948:
					case 950:
					case 6572:
						if (itemType2 == L2Item.TYPE2_SHIELD_ARMOR || itemType2 == L2Item.TYPE2_ACCESSORY)
						{
							enchantItem = true;
						}
						break;
				}
				break;
			case L2Item.CRYSTAL_C:
				crystalId = 1459;
				switch (scroll.getItemId())
				{
					case 951:
					case 953:
					case 6573:
						if (itemType2 == L2Item.TYPE2_WEAPON)
						{
							enchantItem = true;
						}
						break;
					case 952:
					case 954:
					case 6574:
						if (itemType2 == L2Item.TYPE2_SHIELD_ARMOR || itemType2 == L2Item.TYPE2_ACCESSORY)
						{
							enchantItem = true;
						}
						break;
				}
				break;
			case L2Item.CRYSTAL_D:
				crystalId = 1458;
				switch (scroll.getItemId())
				{
					case 955:
					case 957:
					case 6575:
						if (itemType2 == L2Item.TYPE2_WEAPON)
						{
							enchantItem = true;
						}
						break;
					case 956:
					case 958:
					case 6576:
						if (itemType2 == L2Item.TYPE2_SHIELD_ARMOR || itemType2 == L2Item.TYPE2_ACCESSORY)
						{
							enchantItem = true;
						}
						break;
				}
				break;
			case L2Item.CRYSTAL_S:
				crystalId = 1462;
				switch (scroll.getItemId())
				{
					case 959:
					case 961:
					case 6577:
						if (itemType2 == L2Item.TYPE2_WEAPON)
						{
							enchantItem = true;
						}
						break;
					case 960:
					case 962:
					case 6578:
						if (itemType2 == L2Item.TYPE2_SHIELD_ARMOR || itemType2 == L2Item.TYPE2_ACCESSORY)
						{
							enchantItem = true;
						}
						break;
				}
				break;
		}
		
		if (!enchantItem)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
			return;
		}
		
		// Get the scroll type - Yesod
		if (scroll.getItemId() >= 6569 && scroll.getItemId() <= 6578)
		{
			blessedScroll = true;
		}
		else
		{
			for (final int crystalscroll : CRYSTAL_SCROLLS)
			{
				if (scroll.getItemId() == crystalscroll)
				{
					crystalScroll = true;
					break;
				}
			}
		}
		
		// SystemMessage sm = new SystemMessage(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
		// activeChar.sendPacket(sm);
		
		SystemMessage sm;
		
		int chance = 0;
		int maxEnchantLevel = 0;
		int minEnchantLevel = 0;
		
		if (item.getItem().getType2() == L2Item.TYPE2_WEAPON)
		{
			if (blessedScroll)
			{
				
				for (final int blessedweaponscroll : BLESSED_WEAPON_SCROLLS)
				{
					if (scroll.getItemId() == blessedweaponscroll)
					{
						if (item.getEnchantLevel() >= Config.BLESS_WEAPON_ENCHANT_LEVEL.size()) // the hash has size equals to
																								// max enchant, so if the actual
																								// enchant level is equal or more then max
																								// then the enchant rate is equal to last
																								// enchant rate
						{
							chance = Config.BLESS_WEAPON_ENCHANT_LEVEL.get(Config.BLESS_WEAPON_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.BLESS_WEAPON_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_WEAPON_MAX;
						
						break;
					}
				}
				
			}
			else if (crystalScroll)
			{
				
				for (final int crystalweaponscroll : CRYSTAL_WEAPON_SCROLLS)
				{
					if (scroll.getItemId() == crystalweaponscroll)
					{
						if (item.getEnchantLevel() >= Config.CRYSTAL_WEAPON_ENCHANT_LEVEL.size())
						{
							chance = Config.CRYSTAL_WEAPON_ENCHANT_LEVEL.get(Config.CRYSTAL_WEAPON_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.CRYSTAL_WEAPON_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						minEnchantLevel = Config.CRYSTAL_ENCHANT_MIN;
						maxEnchantLevel = Config.CRYSTAL_ENCHANT_MAX;
						
						break;
						
					}
				}
				
			}
			else
			{ // normal scrolls
				
				for (final int normalweaponscroll : NORMAL_WEAPON_SCROLLS)
				{
					if (scroll.getItemId() == normalweaponscroll)
					{
						if (item.getEnchantLevel() >= Config.NORMAL_WEAPON_ENCHANT_LEVEL.size())
						{
							chance = Config.NORMAL_WEAPON_ENCHANT_LEVEL.get(Config.NORMAL_WEAPON_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.NORMAL_WEAPON_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_WEAPON_MAX;
						
						break;
					}
				}
				
			}
			
		}
		else if (item.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR)
		{
			if (blessedScroll)
			{
				
				for (final int blessedarmorscroll : BLESSED_ARMOR_SCROLLS)
				{
					if (scroll.getItemId() == blessedarmorscroll)
					{
						if (item.getEnchantLevel() >= Config.BLESS_ARMOR_ENCHANT_LEVEL.size())
						{
							chance = Config.BLESS_ARMOR_ENCHANT_LEVEL.get(Config.BLESS_ARMOR_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.BLESS_ARMOR_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_ARMOR_MAX;
						
						break;
					}
				}
				
			}
			else if (crystalScroll)
			{
				
				for (final int crystalarmorscroll : CRYSTAL_ARMOR_SCROLLS)
				{
					if (scroll.getItemId() == crystalarmorscroll)
					{
						if (item.getEnchantLevel() >= Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.size())
						{
							chance = Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.get(Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						minEnchantLevel = Config.CRYSTAL_ENCHANT_MIN;
						maxEnchantLevel = Config.CRYSTAL_ENCHANT_MAX;
						
						break;
					}
				}
				
			}
			else
			{ // normal scrolls
				
				for (final int normalarmorscroll : NORMAL_ARMOR_SCROLLS)
				{
					if (scroll.getItemId() == normalarmorscroll)
					{
						if (item.getEnchantLevel() >= Config.NORMAL_ARMOR_ENCHANT_LEVEL.size())
						{
							chance = Config.NORMAL_ARMOR_ENCHANT_LEVEL.get(Config.NORMAL_ARMOR_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.NORMAL_ARMOR_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_ARMOR_MAX;
						
						break;
					}
				}
				
			}
			
		}
		else if (item.getItem().getType2() == L2Item.TYPE2_ACCESSORY)
		{
			if (blessedScroll)
			{
				
				for (final int blessedjewelscroll : BLESSED_ARMOR_SCROLLS)
				{
					if (scroll.getItemId() == blessedjewelscroll)
					{
						if (item.getEnchantLevel() >= Config.BLESS_JEWELRY_ENCHANT_LEVEL.size())
						{
							chance = Config.BLESS_JEWELRY_ENCHANT_LEVEL.get(Config.BLESS_JEWELRY_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.BLESS_JEWELRY_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_JEWELRY_MAX;
						
						break;
					}
				}
				
			}
			else if (crystalScroll)
			{
				
				for (final int crystaljewelscroll : CRYSTAL_ARMOR_SCROLLS)
				{
					if (scroll.getItemId() == crystaljewelscroll)
					{
						if (item.getEnchantLevel() >= Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.size())
						{
							chance = Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.get(Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						minEnchantLevel = Config.CRYSTAL_ENCHANT_MIN;
						maxEnchantLevel = Config.CRYSTAL_ENCHANT_MAX;
						
						break;
					}
				}
				
			}
			else
			{
				
				for (final int normaljewelscroll : NORMAL_ARMOR_SCROLLS)
				{
					if (scroll.getItemId() == normaljewelscroll)
					{
						if (item.getEnchantLevel() >= Config.NORMAL_JEWELRY_ENCHANT_LEVEL.size())
						{
							chance = Config.NORMAL_JEWELRY_ENCHANT_LEVEL.get(Config.NORMAL_JEWELRY_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.NORMAL_JEWELRY_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_JEWELRY_MAX;
						
						break;
					}
				}
				
			}
			
		}
		
		if (maxEnchantLevel != 0 && item.getEnchantLevel() >= maxEnchantLevel || item.getEnchantLevel() < minEnchantLevel)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
			return;
		}
		
		if (Config.SCROLL_STACKABLE)
		{
			scroll = activeChar.getInventory().destroyItem("Enchant", scroll.getObjectId(), 1, activeChar, item);
		}
		else
		{
			scroll = activeChar.getInventory().destroyItem("Enchant", scroll, activeChar, item);
		}
		
		if (scroll == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to enchant with a scroll he doesnt have", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (item.getEnchantLevel() < Config.ENCHANT_SAFE_MAX || item.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR && item.getEnchantLevel() < Config.ENCHANT_SAFE_MAX_FULL)
		{
			chance = 100;
		}
		
		int rndValue = Rnd.get(100);
		
		if (Config.ENABLE_DWARF_ENCHANT_BONUS && activeChar.getRace() == Race.dwarf)
		{
			if (activeChar.getLevel() >= Config.DWARF_ENCHANT_MIN_LEVEL)
			{
				rndValue -= Config.DWARF_ENCHANT_BONUS;
			}
		}
		
		final Object aChance = item.fireEvent("calcEnchantChance", new Object[chance]);
		if (aChance != null)
		{
			chance = (Integer) aChance;
		}
		synchronized (item)
		{
			if (rndValue < chance)
			{
				if (item.getOwnerId() != activeChar.getObjectId())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
					return;
				}
				
				if (item.getLocation() != L2ItemInstance.ItemLocation.INVENTORY && item.getLocation() != L2ItemInstance.ItemLocation.PAPERDOLL)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
					return;
				}
				
				if (item.getEnchantLevel() == 0)
				{
					sm = new SystemMessage(SystemMessageId.S1_SUCCESSFULLY_ENCHANTED);
					sm.addItemName(item.getItemId());
					activeChar.sendPacket(sm);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_S2_SUCCESSFULLY_ENCHANTED);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(item.getItemId());
					activeChar.sendPacket(sm);
				}
				
				item.setEnchantLevel(item.getEnchantLevel() + Config.CUSTOM_ENCHANT_VALUE);
				item.updateDatabase();
				
				String message = "Enchant item successful for player " + activeChar.getName() + ", item " + ItemTable.getInstance().getTemplate(item.getItemId()) + "(" + item.getItemId() + ") to +" + item.getEnchantLevel();
				Log.add(message, "enchant_item");
			}
			else
			{
				if (crystalScroll)
				{
					sm = SystemMessage.sendString("Failed in Crystal Enchant. The enchant value of the item become " + Config.CRYSTAL_ENCHANT_MIN);
					activeChar.sendPacket(sm);
				}
				else if (blessedScroll)
				{
					sm = new SystemMessage(SystemMessageId.BLESSED_ENCHANT_FAILED);
					activeChar.sendPacket(sm);
				}
				else
				{
					if (item.getEnchantLevel() > 0)
					{
						sm = new SystemMessage(SystemMessageId.ENCHANTMENT_FAILED_S1_S2_EVAPORATED);
						sm.addNumber(item.getEnchantLevel());
						sm.addItemName(item.getItemId());
						activeChar.sendPacket(sm);
					}
					else
					{
						sm = new SystemMessage(SystemMessageId.ENCHANTMENT_FAILED_S1_EVAPORATED);
						sm.addItemName(item.getItemId());
						activeChar.sendPacket(sm);
					}
				}
				
				if (!blessedScroll && !crystalScroll)
				{
					if (item.getEnchantLevel() > 0)
					{
						sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
						sm.addNumber(item.getEnchantLevel());
						sm.addItemName(item.getItemId());
						activeChar.sendPacket(sm);
					}
					else
					{
						sm = new SystemMessage(SystemMessageId.S1_DISARMED);
						sm.addItemName(item.getItemId());
						activeChar.sendPacket(sm);
					}
					
					if (item.isEquipped())
					{
						if (item.isAugmented())
						{
							item.getAugmentation().removeBoni(activeChar);
						}
						
						final L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getEquipSlot());
						
						final InventoryUpdate iu = new InventoryUpdate();
						for (final L2ItemInstance element : unequiped)
						{
							iu.addModifiedItem(element);
						}
						activeChar.sendPacket(iu);
						
						activeChar.broadcastUserInfo();
					}
					
					int count = item.getCrystalCount() - (item.getItem().getCrystalCount() + 1) / 2;
					if (count < 1)
					{
						count = 1;
					}
					
					if (item.fireEvent("enchantFail", new Object[] {}) != null)
					{
						return;
					}
					final L2ItemInstance destroyItem = activeChar.getInventory().destroyItem("Enchant", item, activeChar, null);
					if (destroyItem == null)
					{
						return;
					}
					
					final L2ItemInstance crystals = activeChar.getInventory().addItem("Enchant", crystalId, count, activeChar, destroyItem);
					
					sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
					sm.addItemName(crystals.getItemId());
					sm.addNumber(count);
					activeChar.sendPacket(sm);
					
					if (!Config.FORCE_INVENTORY_UPDATE)
					{
						final InventoryUpdate iu = new InventoryUpdate();
						if (destroyItem.getCount() == 0)
						{
							iu.addRemovedItem(destroyItem);
						}
						else
						{
							iu.addModifiedItem(destroyItem);
						}
						iu.addItem(crystals);
						
						activeChar.sendPacket(iu);
					}
					else
					{
						activeChar.sendPacket(new ItemList(activeChar, true));
					}
					
					final StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
					su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
					activeChar.sendPacket(su);
					
					activeChar.broadcastUserInfo();
					
					final L2World world = L2World.getInstance();
					world.removeObject(destroyItem);
				}
				else
				{
					if (blessedScroll)
					{
						item.setEnchantLevel(Config.BREAK_ENCHANT);
						item.updateDatabase();
					}
					else if (crystalScroll)
					{
						item.setEnchantLevel(Config.CRYSTAL_ENCHANT_MIN);
						item.updateDatabase();
					}
					
				}
				
				String message = "Enchant item failed for player " + activeChar.getName() + ", item " + ItemTable.getInstance().getTemplate(item.getItemId()) + "(" + item.getItemId() + ") to +" + (item.getEnchantLevel() + 1);
				Log.add(message, "enchant_item");
			}
		}
		sm = null;
		
		StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
		su = null;
		
		EngineModsManager.onEnchant(activeChar);
		
		activeChar.sendPacket(new EnchantResult(item.getEnchantLevel())); // FIXME i'm really not sure about this...
		activeChar.sendPacket(new ItemList(activeChar, false)); // TODO update only the enchanted item
		activeChar.broadcastUserInfo();
	}
	
	@Override
	public String getType()
	{
		return "[C] 58 RequestEnchantItem";
	}
}
