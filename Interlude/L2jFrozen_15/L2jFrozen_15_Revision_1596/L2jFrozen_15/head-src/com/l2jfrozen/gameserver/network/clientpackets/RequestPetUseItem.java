package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.L2PetDataTable;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.handler.ItemHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.PetInfo;
import com.l2jfrozen.gameserver.network.serverpackets.PetItemList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestPetUseItem extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestPetUseItem.class);
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
		
		if (activeChar == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getUseItem().tryPerformAction("pet use item"))
		{
			return;
		}
		
		final L2PetInstance pet = (L2PetInstance) activeChar.getPet();
		
		if (pet == null)
		{
			return;
		}
		
		final L2ItemInstance item = pet.getInventory().getItemByObjectId(objectId);
		
		if (item == null)
		{
			return;
		}
		
		if (item.isWear())
		{
			return;
		}
		
		final int itemId = item.getItemId();
		
		if (activeChar.isAlikeDead() || pet.isDead())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addItemName(item.getItemId());
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if (Config.DEBUG)
		{
			LOGGER.debug(activeChar.getObjectId() + ": pet use item " + objectId);
		}
		
		// check if the item matches the pet
		if (item.isEquipable())
		{
			if (L2PetDataTable.isWolf(pet.getNpcId()) && // wolf
				item.getItem().isForWolf())
			{
				useItem(pet, item, activeChar);
				return;
			}
			else if (L2PetDataTable.isHatchling(pet.getNpcId()) && // hatchlings
				item.getItem().isForHatchling())
			{
				useItem(pet, item, activeChar);
				return;
			}
			else if (L2PetDataTable.isStrider(pet.getNpcId()) && // striders
				item.getItem().isForStrider())
			{
				useItem(pet, item, activeChar);
				return;
			}
			else if (L2PetDataTable.isBaby(pet.getNpcId()) && // baby pets (buffalo, cougar, kookaboora)
				item.getItem().isForBabyPet())
			{
				useItem(pet, item, activeChar);
				return;
			}
			else
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.ITEM_NOT_FOR_PETS));
				return;
			}
		}
		else if (L2PetDataTable.isPetFood(itemId))
		{
			if (L2PetDataTable.isWolf(pet.getNpcId()) && L2PetDataTable.isWolfFood(itemId))
			{
				feed(activeChar, pet, item);
				return;
			}
			
			if (L2PetDataTable.isSinEater(pet.getNpcId()) && L2PetDataTable.isSinEaterFood(itemId))
			{
				feed(activeChar, pet, item);
				return;
			}
			else if (L2PetDataTable.isHatchling(pet.getNpcId()) && L2PetDataTable.isHatchlingFood(itemId))
			{
				feed(activeChar, pet, item);
				return;
			}
			else if (L2PetDataTable.isStrider(pet.getNpcId()) && L2PetDataTable.isStriderFood(itemId))
			{
				feed(activeChar, pet, item);
				return;
			}
			else if (L2PetDataTable.isWyvern(pet.getNpcId()) && L2PetDataTable.isWyvernFood(itemId))
			{
				feed(activeChar, pet, item);
				return;
			}
			else if (L2PetDataTable.isBaby(pet.getNpcId()) && L2PetDataTable.isBabyFood(itemId))
			{
				feed(activeChar, pet, item);
				return;
			}
		}
		
		final IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
		
		if (handler != null)
		{
			useItem(pet, item, activeChar);
		}
		else
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.ITEM_NOT_FOR_PETS);
			activeChar.sendPacket(sm);
		}
		
		return;
	}
	
	private synchronized void useItem(final L2PetInstance pet, final L2ItemInstance item, final L2PcInstance activeChar)
	{
		if (item.isEquipable())
		{
			if (item.isEquipped())
			{
				pet.getInventory().unEquipItemInSlot(item.getEquipSlot());
			}
			else
			{
				pet.getInventory().equipItem(item);
			}
			
			final PetItemList pil = new PetItemList(pet);
			activeChar.sendPacket(pil);
			
			final PetInfo pi = new PetInfo(pet);
			activeChar.sendPacket(pi);
			// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
			pet.updateEffectIcons(true);
		}
		else
		{
			// LOGGER.finest("item not equipable id:"+ item.getItemId());
			final IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
			
			if (handler == null)
			{
				LOGGER.warn("no itemhandler registered for itemId:" + item.getItemId());
			}
			else
			{
				handler.useItem(pet, item);
			}
		}
	}
	
	/**
	 * When fed by owner double click on food from pet inventory. <BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : 1 food = 100 points of currentFed</B></FONT><BR>
	 * <BR>
	 * @param player
	 * @param pet
	 * @param item
	 */
	private void feed(final L2PcInstance player, final L2PetInstance pet, final L2ItemInstance item)
	{
		// if pet has food in inventory
		if (pet.destroyItem("Feed", item.getObjectId(), 1, pet, false))
		{
			pet.setCurrentFed(pet.getCurrentFed() + 100);
		}
		
		pet.broadcastStatusUpdate();
	}
	
	@Override
	public String getType()
	{
		return "[C] 8a RequestPetUseItem";
	}
}
