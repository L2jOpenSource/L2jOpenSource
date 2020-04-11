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

import java.util.HashMap;
import java.util.Map;

import com.l2jserver.gameserver.instancemanager.CastleManorManager;
import com.l2jserver.gameserver.model.CropProcure;
import com.l2jserver.gameserver.model.L2Seed;
import com.l2jserver.gameserver.model.itemcontainer.PcInventory;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author l3x
 */
public final class ExShowSellCropList extends L2GameServerPacket
{
	private final int _manorId;
	private final Map<Integer, L2ItemInstance> _cropsItems = new HashMap<>();
	private final Map<Integer, CropProcure> _castleCrops = new HashMap<>();
	
	public ExShowSellCropList(PcInventory inventory, int manorId)
	{
		_manorId = manorId;
		for (int cropId : CastleManorManager.getInstance().getCropIds())
		{
			final L2ItemInstance item = inventory.getItemByItemId(cropId);
			if (item != null)
			{
				_cropsItems.put(cropId, item);
			}
		}
		
		for (CropProcure crop : CastleManorManager.getInstance().getCropProcure(_manorId, false))
		{
			if (_cropsItems.containsKey(crop.getId()) && (crop.getAmount() > 0))
			{
				_castleCrops.put(crop.getId(), crop);
			}
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x2C);
		
		writeD(_manorId); // manor id
		writeD(_cropsItems.size()); // size
		for (L2ItemInstance item : _cropsItems.values())
		{
			final L2Seed seed = CastleManorManager.getInstance().getSeedByCrop(item.getId());
			writeD(item.getObjectId()); // Object id
			writeD(item.getId()); // crop id
			writeD(seed.getLevel()); // seed level
			writeC(0x01);
			writeD(seed.getReward(1)); // reward 1 id
			writeC(0x01);
			writeD(seed.getReward(2)); // reward 2 id
			if (_castleCrops.containsKey(item.getId()))
			{
				final CropProcure crop = _castleCrops.get(item.getId());
				writeD(_manorId); // manor
				writeQ(crop.getAmount()); // buy residual
				writeQ(crop.getPrice()); // buy price
				writeC(crop.getReward()); // reward
			}
			else
			{
				writeD(0xFFFFFFFF); // manor
				writeQ(0x00); // buy residual
				writeQ(0x00); // buy price
				writeC(0x00); // reward
			}
			writeQ(item.getCount()); // my crops
		}
	}
}