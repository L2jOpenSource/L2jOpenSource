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
package l2r.gameserver.network.clientpackets;

import static l2r.gameserver.model.itemcontainer.Inventory.ADENA_ID;
import l2r.Config;
import l2r.gameserver.datatables.xml.ItemData;
import l2r.gameserver.enums.ItemLocation;
import l2r.gameserver.enums.PrivateStoreType;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.MailManager;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Message;
import l2r.gameserver.model.itemcontainer.ItemContainer;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExChangePostState;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.ItemList;
import l2r.gameserver.network.serverpackets.StatusUpdate;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;
import gr.reunion.configsEngine.CustomServerConfigs;

/**
 * @author Migi, DS
 */
public final class RequestPostAttachment extends L2GameClientPacket
{
	private static final String _C__D0_6A_REQUESTPOSTATTACHMENT = "[C] D0:6A RequestPostAttachment";
	
	private int _msgId;
	
	@Override
	protected void readImpl()
	{
		_msgId = readD();
	}
	
	@Override
	public void runImpl()
	{
		if (!Config.ALLOW_MAIL || !Config.ALLOW_ATTACHMENTS)
		{
			return;
		}
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("getattach"))
		{
			return;
		}
		
		if (!activeChar.getAccessLevel().allowTransaction())
		{
			activeChar.sendMessage("Transactions are disabled for your Access Level");
			return;
		}
		
		if (!activeChar.isInsideZone(ZoneIdType.PEACE))
		{
			activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_NOT_IN_PEACE_ZONE);
			return;
		}
		
		if (activeChar.getActiveTradeList() != null)
		{
			activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_DURING_EXCHANGE);
			return;
		}
		
		if (activeChar.isEnchanting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_DURING_ENCHANT);
			return;
		}
		
		if (activeChar.getPrivateStoreType() != PrivateStoreType.NONE)
		{
			activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_PRIVATE_STORE);
			return;
		}
		
		final Message msg = MailManager.getInstance().getMessage(_msgId);
		if (msg == null)
		{
			return;
		}
		
		if (msg.getReceiverId() != activeChar.getObjectId())
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get not own attachment!", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (!msg.hasAttachments())
		{
			return;
		}
		
		final ItemContainer attachments = msg.getAttachments();
		if (attachments == null)
		{
			return;
		}
		
		int weight = 0;
		int slots = 0;
		
		for (L2ItemInstance item : attachments.getItems())
		{
			if (item == null)
			{
				continue;
			}
			
			// Calculate needed slots
			if (item.getOwnerId() != msg.getSenderId())
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get wrong item (ownerId != senderId) from attachment!", Config.DEFAULT_PUNISH);
				return;
			}
			
			if (item.getItemLocation() != ItemLocation.MAIL)
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get wrong item (Location != MAIL) from attachment!", Config.DEFAULT_PUNISH);
				return;
			}
			
			if (item.getLocationSlot() != msg.getId())
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get items from different attachment!", Config.DEFAULT_PUNISH);
				return;
			}
			
			weight += item.getCount() * item.getItem().getWeight();
			if (!item.isStackable())
			{
				slots += item.getCount();
			}
			else if (activeChar.getInventory().getItemByItemId(item.getId()) == null)
			{
				slots++;
			}
		}
		
		// Item Max Limit Check
		if (!activeChar.getInventory().validateCapacity(slots))
		{
			activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_INVENTORY_FULL);
			return;
		}
		
		// Weight limit Check
		if (!activeChar.getInventory().validateWeight(weight))
		{
			activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_INVENTORY_FULL);
			return;
		}
		
		long adena = msg.getReqAdena();
		if (CustomServerConfigs.ALTERNATE_PAYMODE_MAILS)
		{
			if ((adena > 0) && !activeChar.reduceFAdena("PayMail", adena, null, true))
			{
				activeChar.sendMessage("You cannot receive because you don't have enough " + ItemData.getInstance().getTemplate(CustomServerConfigs.ALTERNATE_PAYMENT_ID).getName() + ".");
				return;
			}
		}
		else
		{
			if ((adena > 0) && !activeChar.reduceAdena("PayMail", adena, null, true))
			{
				activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_NO_ADENA);
				return;
			}
		}
		
		// Proceed to the transfer
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (L2ItemInstance item : attachments.getItems())
		{
			if (item == null)
			{
				continue;
			}
			
			if (item.getOwnerId() != msg.getSenderId())
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get items with owner != sender !", Config.DEFAULT_PUNISH);
				return;
			}
			
			long count = item.getCount();
			final L2ItemInstance newItem = attachments.transferItem(attachments.getName(), item.getObjectId(), item.getCount(), activeChar.getInventory(), activeChar, null);
			if (newItem == null)
			{
				return;
			}
			
			if (playerIU != null)
			{
				if (newItem.getCount() > count)
				{
					playerIU.addModifiedItem(newItem);
				}
				else
				{
					playerIU.addNewItem(newItem);
				}
			}
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_ACQUIRED_S2_S1);
			sm.addItemName(item.getId());
			sm.addLong(count);
			activeChar.sendPacket(sm);
		}
		
		// Send updated item list to the player
		if (playerIU != null)
		{
			activeChar.sendPacket(playerIU);
		}
		else
		{
			activeChar.sendPacket(new ItemList(activeChar, false));
		}
		
		msg.removeAttachments();
		
		// Update current load status on player
		StatusUpdate su = new StatusUpdate(activeChar);
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
		
		SystemMessage sm;
		final L2PcInstance sender = L2World.getInstance().getPlayer(msg.getSenderId());
		if (adena > 0)
		{
			if (sender != null)
			{
				if (CustomServerConfigs.ALTERNATE_PAYMODE_MAILS)
				{
					sender.addFAdena("PayMail", adena, activeChar, false);
				}
				else
				{
					sender.addAdena("PayMail", adena, activeChar, false);
				}
				sm = SystemMessage.getSystemMessage(SystemMessageId.PAYMENT_OF_S1_ADENA_COMPLETED_BY_S2);
				sm.addLong(adena);
				sm.addCharName(activeChar);
				sender.sendPacket(sm);
			}
			else
			{
				if (CustomServerConfigs.ALTERNATE_PAYMODE_MAILS)
				{
					L2ItemInstance paidFAdena = ItemData.getInstance().createItem("PayMail", CustomServerConfigs.ALTERNATE_PAYMENT_ID, adena, activeChar, null);
					paidFAdena.setOwnerId(msg.getSenderId());
					paidFAdena.setItemLocation(ItemLocation.INVENTORY);
					paidFAdena.updateDatabase(true);
					L2World.getInstance().removeObject(paidFAdena);
				}
				else
				{
					L2ItemInstance paidAdena = ItemData.getInstance().createItem("PayMail", ADENA_ID, adena, activeChar, null);
					paidAdena.setOwnerId(msg.getSenderId());
					paidAdena.setItemLocation(ItemLocation.INVENTORY);
					paidAdena.updateDatabase(true);
					L2World.getInstance().removeObject(paidAdena);
				}
			}
		}
		else if (sender != null)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ACQUIRED_ATTACHED_ITEM);
			sm.addCharName(activeChar);
			sender.sendPacket(sm);
		}
		
		activeChar.sendPacket(new ExChangePostState(true, _msgId, Message.READED));
		activeChar.sendPacket(SystemMessageId.MAIL_SUCCESSFULLY_RECEIVED);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_6A_REQUESTPOSTATTACHMENT;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
