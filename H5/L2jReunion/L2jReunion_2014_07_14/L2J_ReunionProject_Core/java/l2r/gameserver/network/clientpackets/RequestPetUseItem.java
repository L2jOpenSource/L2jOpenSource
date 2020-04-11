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

import l2r.gameserver.handler.IItemHandler;
import l2r.gameserver.handler.ItemHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2PetInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.PetItemList;
import l2r.gameserver.network.serverpackets.SystemMessage;

public final class RequestPetUseItem extends L2GameClientPacket
{
	private static final String _C__8A_REQUESTPETUSEITEM = "[C] 8A RequestPetUseItem";
	
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		// TODO: implement me properly
		// readQ();
		// readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if ((activeChar == null) || !activeChar.hasPet())
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getUseItem().tryPerformAction("pet use item"))
		{
			return;
		}
		
		final L2PetInstance pet = (L2PetInstance) activeChar.getSummon();
		final L2ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);
		if (item == null)
		{
			return;
		}
		
		if (!item.getItem().isForNpc())
		{
			activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
			return;
		}
		
		if (activeChar.isAlikeDead() || pet.isDead())
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addItemName(item);
			activeChar.sendPacket(sm);
			return;
		}
		
		// If the item has reuse time and it has not passed.
		// Message from reuse delay must come from item.
		final int reuseDelay = item.getReuseDelay();
		if (reuseDelay > 0)
		{
			final long reuse = pet.getItemRemainingReuseTime(item.getObjectId());
			if (reuse > 0)
			{
				return;
			}
		}
		
		if (activeChar.isDebug())
		{
			activeChar.sendDebugMessage("Pet tries to use item: " + item);
		}
		
		if (!item.isEquipped() && !item.getItem().checkCondition(pet, pet, true))
		{
			return;
		}
		
		useItem(pet, item, activeChar);
	}
	
	private void useItem(L2PetInstance pet, L2ItemInstance item, L2PcInstance activeChar)
	{
		if (item.isEquipable())
		{
			if (!item.getItem().isConditionAttached())
			{
				activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
				return;
			}
			
			if (item.isEquipped())
			{
				pet.getInventory().unEquipItemInSlot(item.getLocationSlot());
			}
			else
			{
				pet.getInventory().equipItem(item);
			}
			
			activeChar.sendPacket(new PetItemList(pet.getInventory().getItems()));
			pet.updateAndBroadcastStatus(1);
		}
		else
		{
			final IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
			if (handler != null)
			{
				if (handler.useItem(pet, item, false))
				{
					final int reuseDelay = item.getReuseDelay();
					if (reuseDelay > 0)
					{
						activeChar.addTimeStampItem(item, reuseDelay);
					}
					pet.updateAndBroadcastStatus(1);
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
				_log.warn("No item handler registered for itemId: " + item.getId());
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__8A_REQUESTPETUSEITEM;
	}
}
