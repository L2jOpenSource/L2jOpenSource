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

import l2r.Config;
import l2r.gameserver.enums.ElementalItems;
import l2r.gameserver.enums.PrivateStoreType;
import l2r.gameserver.model.Elementals;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExAttributeEnchantResult;
import l2r.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.UserInfo;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

public class RequestExEnchantItemAttribute extends L2GameClientPacket
{
	private static final String _C__D0_35_REQUESTEXENCHANTITEMATTRIBUTE = "[C] D0:35 RequestExEnchantItemAttribute";
	
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (_objectId == 0xFFFFFFFF)
		{
			// Player canceled enchant
			player.setActiveEnchantAttrItemId(L2PcInstance.ID_NONE);
			player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_CANCELED);
			return;
		}
		
		if (!player.isOnline())
		{
			player.setActiveEnchantAttrItemId(L2PcInstance.ID_NONE);
			return;
		}
		
		if (player.getPrivateStoreType() != PrivateStoreType.NONE)
		{
			player.sendPacket(SystemMessageId.CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_PRIVATE_STORE_OR_WORKSHOP);
			player.setActiveEnchantAttrItemId(L2PcInstance.ID_NONE);
			return;
		}
		
		// Restrict enchant during a trade (bug if enchant fails)
		if (player.getActiveRequester() != null)
		{
			// Cancel trade
			player.cancelActiveTrade();
			player.setActiveEnchantAttrItemId(L2PcInstance.ID_NONE);
			player.sendMessage("Enchanting items is not allowed during a trade.");
			return;
		}
		
		final L2ItemInstance item = player.getInventory().getItemByObjectId(_objectId);
		final L2ItemInstance stone = player.getInventory().getItemByObjectId(player.getActiveEnchantAttrItemId());
		if ((item == null) || (stone == null))
		{
			player.setActiveEnchantAttrItemId(L2PcInstance.ID_NONE);
			return;
		}
		
		if (!item.isElementable())
		{
			player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_REQUIREMENT_NOT_SUFFICIENT);
			player.setActiveEnchantAttrItemId(L2PcInstance.ID_NONE);
			return;
		}
		
		switch (item.getItemLocation())
		{
			case INVENTORY:
			case PAPERDOLL:
			{
				if (item.getOwnerId() != player.getObjectId())
				{
					player.setActiveEnchantAttrItemId(L2PcInstance.ID_NONE);
					return;
				}
				break;
			}
			default:
			{
				player.setActiveEnchantAttrItemId(L2PcInstance.ID_NONE);
				Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to use enchant Exploit!", Config.DEFAULT_PUNISH);
				return;
			}
		}
		
		int stoneId = stone.getId();
		byte elementToAdd = Elementals.getItemElement(stoneId);
		// Armors have the opposite element
		if (item.isArmor())
		{
			elementToAdd = Elementals.getOppositeElement(elementToAdd);
		}
		byte opositeElement = Elementals.getOppositeElement(elementToAdd);
		
		Elementals oldElement = item.getElemental(elementToAdd);
		int elementValue = oldElement == null ? 0 : oldElement.getValue();
		int limit = getLimit(item, stoneId);
		int powerToAdd = getPowerToAdd(stoneId, elementValue, item);
		
		if ((item.isWeapon() && (oldElement != null) && (oldElement.getElement() != elementToAdd) && (oldElement.getElement() != -2)) || (item.isArmor() && (item.getElemental(elementToAdd) == null) && (item.getElementals() != null) && (item.getElementals().length >= 3)))
		{
			player.sendPacket(SystemMessageId.ANOTHER_ELEMENTAL_POWER_ALREADY_ADDED);
			player.setActiveEnchantAttrItemId(L2PcInstance.ID_NONE);
			return;
		}
		
		if (item.isArmor() && (item.getElementals() != null))
		{
			// cant add opposite element
			for (Elementals elm : item.getElementals())
			{
				if (elm.getElement() == opositeElement)
				{
					player.setActiveEnchantAttrItemId(L2PcInstance.ID_NONE);
					Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to add oposite attribute to item!", Config.DEFAULT_PUNISH);
					return;
				}
			}
		}
		
		int newPower = elementValue + powerToAdd;
		if (newPower > limit)
		{
			newPower = limit;
			powerToAdd = limit - elementValue;
		}
		
		if (powerToAdd <= 0)
		{
			player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_CANCELED);
			player.setActiveEnchantAttrItemId(L2PcInstance.ID_NONE);
			return;
		}
		
		if (!player.destroyItem("AttrEnchant", stone, 1, player, true))
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to attribute enchant with a stone he doesn't have", Config.DEFAULT_PUNISH);
			player.setActiveEnchantAttrItemId(L2PcInstance.ID_NONE);
			return;
		}
		boolean success = false;
		switch (Elementals.getItemElemental(stoneId)._type)
		{
			case Stone:
			case Roughore:
				success = Rnd.get(100) < Config.ENCHANT_CHANCE_ELEMENT_STONE;
				break;
			case Crystal:
				success = Rnd.get(100) < Config.ENCHANT_CHANCE_ELEMENT_CRYSTAL;
				break;
			case Jewel:
				success = Rnd.get(100) < Config.ENCHANT_CHANCE_ELEMENT_JEWEL;
				break;
			case Energy:
				success = Rnd.get(100) < Config.ENCHANT_CHANCE_ELEMENT_ENERGY;
				break;
		}
		if (success)
		{
			byte realElement = item.isArmor() ? opositeElement : elementToAdd;
			
			SystemMessage sm;
			if (item.getEnchantLevel() == 0)
			{
				if (item.isArmor())
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.THE_S2_ATTRIBUTE_WAS_SUCCESSFULLY_BESTOWED_ON_S1_RES_TO_S3_INCREASED);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.ELEMENTAL_POWER_S2_SUCCESSFULLY_ADDED_TO_S1);
				}
				sm.addItemName(item);
				sm.addElemental(realElement);
				if (item.isArmor())
				{
					sm.addElemental(Elementals.getOppositeElement(realElement));
				}
			}
			else
			{
				if (item.isArmor())
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.THE_S3_ATTRIBUTE_BESTOWED_ON_S1_S2_RESISTANCE_TO_S4_INCREASED);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.ELEMENTAL_POWER_S3_SUCCESSFULLY_ADDED_TO_S1_S2);
				}
				sm.addInt(item.getEnchantLevel());
				sm.addItemName(item);
				sm.addElemental(realElement);
				if (item.isArmor())
				{
					sm.addElemental(Elementals.getOppositeElement(realElement));
				}
			}
			player.sendPacket(sm);
			item.setElementAttr(elementToAdd, newPower);
			if (item.isEquipped())
			{
				item.updateElementAttrBonus(player);
			}
			
			// send packets
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(item);
			player.sendPacket(iu);
		}
		else
		{
			player.sendPacket(SystemMessageId.FAILED_ADDING_ELEMENTAL_POWER);
		}
		
		player.sendPacket(new ExAttributeEnchantResult(powerToAdd));
		player.sendPacket(new UserInfo(player));
		player.sendPacket(new ExBrExtraUserInfo(player));
		player.setActiveEnchantAttrItemId(L2PcInstance.ID_NONE);
	}
	
	public int getLimit(L2ItemInstance item, int sotneId)
	{
		ElementalItems elementItem = Elementals.getItemElemental(sotneId);
		if (elementItem == null)
		{
			return 0;
		}
		
		if (item.isWeapon())
		{
			return Elementals.WEAPON_VALUES[elementItem._type._maxLevel];
		}
		return Elementals.ARMOR_VALUES[elementItem._type._maxLevel];
	}
	
	public int getPowerToAdd(int stoneId, int oldValue, L2ItemInstance item)
	{
		if (Elementals.getItemElement(stoneId) != Elementals.NONE)
		{
			if (item.isWeapon())
			{
				if (oldValue == 0)
				{
					return Elementals.FIRST_WEAPON_BONUS;
				}
				return Elementals.NEXT_WEAPON_BONUS;
			}
			else if (item.isArmor())
			{
				return Elementals.ARMOR_BONUS;
			}
		}
		return 0;
	}
	
	@Override
	public String getType()
	{
		return _C__D0_35_REQUESTEXENCHANTITEMATTRIBUTE;
	}
}
