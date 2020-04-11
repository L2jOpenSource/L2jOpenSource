package com.l2jfrozen.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.L2PetDataTable;
import com.l2jfrozen.gameserver.managers.CursedWeaponsManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.util.Util;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public final class RequestDestroyItem extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestDestroyItem.class);
	private static final String DELETE_PET = "DELETE FROM pets WHERE item_obj_id=?";
	
	private int objectId;
	private int destroyCount;
	
	@Override
	protected void readImpl()
	{
		objectId = readD();
		destroyCount = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (destroyCount <= 0)
		{
			if (destroyCount < 0)
			{
				Util.handleIllegalPlayerAction(activeChar, "[RequestDestroyItem] count < 0! ban! oid: " + objectId + " owner: " + activeChar.getName(), Config.DEFAULT_PUNISH);
			}
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("destroy"))
		{
			activeChar.sendMessage("You destroying items too fast.");
			return;
		}
		
		int count = destroyCount;
		
		if (activeChar.isInStoreMode())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			return;
		}
		
		final L2ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(objectId);
		
		// if we cant find requested item, its actualy a cheat!
		if (itemToRemove == null)
		{
			return;
		}
		if (itemToRemove.fireEvent("DESTROY", (Object[]) null) != null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
			return;
		}
		
		// Cannot discard item that the skill is consumming
		if (activeChar.isCastingNow())
		{
			if (activeChar.getCurrentSkill() != null && activeChar.getCurrentSkill().getSkill().getItemConsumeId() == itemToRemove.getItemId())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
				return;
			}
		}
		
		final int itemId = itemToRemove.getItemId();
		
		if (itemToRemove.isWear() || !itemToRemove.isDestroyable() || CursedWeaponsManager.getInstance().isCursed(itemId))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
			return;
		}
		
		if (!itemToRemove.isStackable() && count > 1)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestDestroyItem] count > 1 but item is not stackable! oid: " + objectId + " owner: " + activeChar.getName(), Config.DEFAULT_PUNISH);
			return;
		}
		
		if (destroyCount > itemToRemove.getCount())
		{
			count = itemToRemove.getCount();
		}
		
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
				activeChar.checkSSMatch(null, element);
				iu.addModifiedItem(element);
			}
			
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();
		}
		
		if (L2PetDataTable.isPetItem(itemId))
		{
			if (activeChar.getPet() != null && activeChar.getPet().getControlItemId() == objectId)
			{
				activeChar.getPet().unSummon(activeChar);
			}
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(DELETE_PET))
			{
				statement.setInt(1, objectId);
				statement.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("RequestDestroyItem.runImpl : Could not delete pet with object id " + objectId, e);
			}
		}
		
		final L2ItemInstance removedItem = activeChar.getInventory().destroyItem("Destroy", objectId, count, activeChar, null);
		
		if (removedItem == null)
		{
			return;
		}
		
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
			
			// client.getConnection().sendPacket(iu);
			activeChar.sendPacket(iu);
		}
		else
		{
			sendPacket(new ItemList(activeChar, true));
		}
		
		final StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
		
		// L2World world = L2World.getInstance();
		// world.removeObject(removedItem);
	}
	
	@Override
	public String getType()
	{
		return "[C] 59 RequestDestroyItem";
	}
}