package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.PcInventory;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.util.IllegalPlayerAction;
import com.l2jfrozen.gameserver.util.Util;

public final class RequestCrystallizeItem extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestCrystallizeItem.class);
	
	private int objectId;
	private int count;
	
	@Override
	protected void readImpl()
	{
		objectId = readD();
		count = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			LOGGER.warn("RequestCrystalizeItem: activeChar was null");
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("crystallize"))
		{
			activeChar.sendMessage("You crystallizing too fast.");
			return;
		}
		
		if (count <= 0)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestCrystallizeItem] count <= 0! ban! oid: " + objectId + " owner: " + activeChar.getName(), IllegalPlayerAction.PUNISH_KICK);
			return;
		}
		
		if (activeChar.getPrivateStoreType() != 0 || activeChar.isInCrystallize())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			return;
		}
		
		final int skillLevel = activeChar.getSkillLevel(L2Skill.SKILL_CRYSTALLIZE);
		if (skillLevel <= 0)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
			activeChar.sendPacket(sm);
			sm = null;
			final ActionFailed af = ActionFailed.STATIC_PACKET;
			activeChar.sendPacket(af);
			return;
		}
		
		final PcInventory inventory = activeChar.getInventory();
		if (inventory != null)
		{
			final L2ItemInstance item = inventory.getItemByObjectId(objectId);
			if (item == null || item.isWear())
			{
				final ActionFailed af = ActionFailed.STATIC_PACKET;
				activeChar.sendPacket(af);
				return;
			}
			
			final int itemId = item.getItemId();
			
			if (itemId >= 6611 && itemId <= 6621 || itemId == 6842)
			{
				return;
			}
			
			if (count > item.getCount())
			{
				count = activeChar.getInventory().getItemByObjectId(objectId).getCount();
			}
		}
		
		final L2ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(objectId);
		
		if (itemToRemove == null || itemToRemove.isWear())
		{
			return;
		}
		if (itemToRemove.fireEvent("CRYSTALLIZE", (Object[]) null) != null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
			return;
		}
		
		if (!itemToRemove.getItem().isCrystallizable() || itemToRemove.getItem().getCrystalCount() <= 0 || itemToRemove.getItem().getCrystalType() == L2Item.CRYSTAL_NONE)
		{
			LOGGER.warn("" + activeChar.getObjectId() + " tried to crystallize " + itemToRemove.getItem().getItemId());
			return;
		}
		
		// Check if the char can crystallize C items and return if false;
		if (itemToRemove.getItem().getCrystalType() == L2Item.CRYSTAL_C && skillLevel <= 1)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
			activeChar.sendPacket(sm);
			sm = null;
			final ActionFailed af = ActionFailed.STATIC_PACKET;
			activeChar.sendPacket(af);
			return;
		}
		
		// Check if the user can crystallize B items and return if false;
		if (itemToRemove.getItem().getCrystalType() == L2Item.CRYSTAL_B && skillLevel <= 2)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
			activeChar.sendPacket(sm);
			sm = null;
			final ActionFailed af = ActionFailed.STATIC_PACKET;
			activeChar.sendPacket(af);
			return;
		}
		
		// Check if the user can crystallize A items and return if false;
		if (itemToRemove.getItem().getCrystalType() == L2Item.CRYSTAL_A && skillLevel <= 3)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
			activeChar.sendPacket(sm);
			sm = null;
			final ActionFailed af = ActionFailed.STATIC_PACKET;
			activeChar.sendPacket(af);
			return;
		}
		
		// Check if the user can crystallize S items and return if false;
		if (itemToRemove.getItem().getCrystalType() == L2Item.CRYSTAL_S && skillLevel <= 4)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
			activeChar.sendPacket(sm);
			sm = null;
			final ActionFailed af = ActionFailed.STATIC_PACKET;
			activeChar.sendPacket(af);
			return;
		}
		
		activeChar.setInCrystallize(true);
		
		// unequip if needed
		if (itemToRemove.isEquipped())
		{
			if (itemToRemove.isAugmented())
			{
				itemToRemove.getAugmentation().removeBoni(activeChar);
			}
			
			final L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getEquipSlot());
			final InventoryUpdate iu = new InventoryUpdate();
			
			for (final L2ItemInstance element : unequiped)
			{
				iu.addModifiedItem(element);
			}
			activeChar.sendPacket(iu);
			// activeChar.updatePDef();
			// activeChar.updatePAtk();
			// activeChar.updateMDef();
			// activeChar.updateMAtk();
			// activeChar.updateAccuracy();
			// activeChar.updateCriticalChance();
		}
		
		// remove from inventory
		final L2ItemInstance removedItem = activeChar.getInventory().destroyItem("Crystalize", objectId, count, activeChar, null);
		
		// add crystals
		final int crystalId = itemToRemove.getItem().getCrystalItemId();
		final int crystalAmount = itemToRemove.getCrystalCount();
		final L2ItemInstance createditem = activeChar.getInventory().addItem("Crystalize", crystalId, crystalAmount, activeChar, itemToRemove);
		
		SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
		sm.addItemName(crystalId);
		sm.addNumber(crystalAmount);
		activeChar.sendPacket(sm);
		sm = null;
		
		// send inventory update
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			final InventoryUpdate iu = new InventoryUpdate();
			if (removedItem.getCount() == 0)
			{
				iu.addRemovedItem(removedItem);
			}
			else
			{
				iu.addModifiedItem(removedItem);
			}
			
			if (createditem.getCount() != crystalAmount)
			{
				iu.addModifiedItem(createditem);
			}
			else
			{
				iu.addNewItem(createditem);
			}
			
			activeChar.sendPacket(iu);
		}
		else
		{
			activeChar.sendPacket(new ItemList(activeChar, false));
		}
		
		// status & user info
		final StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
		
		activeChar.broadcastUserInfo();
		
		final L2World world = L2World.getInstance();
		world.removeObject(removedItem);
		
		activeChar.setInCrystallize(false);
	}
	
	@Override
	public String getType()
	{
		return "[C] 72 RequestCrystallizeItem";
	}
}
