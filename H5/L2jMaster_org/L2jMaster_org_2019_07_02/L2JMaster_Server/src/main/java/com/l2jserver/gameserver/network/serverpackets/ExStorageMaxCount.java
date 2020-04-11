/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.network.serverpackets;

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.stats.Stats;

/**
 * @author -Wooden-, KenM
 */
public class ExStorageMaxCount extends L2GameServerPacket
{
	private final int _inventory;
	private final int _warehouse;
	private final int _clan;
	private final int _privateSell;
	private final int _privateBuy;
	private final int _receipeD;
	private final int _recipe;
	private final int _inventoryExtraSlots;
	private final int _inventoryQuestItems;
	
	public ExStorageMaxCount(L2PcInstance activeChar)
	{
		_inventory = activeChar.getInventoryLimit();
		_warehouse = activeChar.getWareHouseLimit();
		_privateSell = activeChar.getPrivateSellStoreLimit();
		_privateBuy = activeChar.getPrivateBuyStoreLimit();
		_clan = Config.WAREHOUSE_SLOTS_CLAN;
		_receipeD = activeChar.getDwarfRecipeLimit();
		_recipe = activeChar.getCommonRecipeLimit();
		_inventoryExtraSlots = (int) activeChar.getStat().calcStat(Stats.INV_LIM, 0, null, null);
		_inventoryQuestItems = Config.INVENTORY_MAXIMUM_QUEST_ITEMS;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x2F);
		
		writeD(_inventory);
		writeD(_warehouse);
		writeD(_clan);
		writeD(_privateSell);
		writeD(_privateBuy);
		writeD(_receipeD);
		writeD(_recipe);
		writeD(_inventoryExtraSlots); // Belt inventory slots increase count
		writeD(_inventoryQuestItems);
	}
}