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

import java.util.List;

import com.l2jserver.gameserver.instancemanager.CastleManorManager;
import com.l2jserver.gameserver.model.L2Seed;

/**
 * @author l3x
 */
public final class ExShowManorDefaultInfo extends L2GameServerPacket
{
	private final List<L2Seed> _crops;
	private final boolean _hideButtons;
	
	public ExShowManorDefaultInfo(boolean hideButtons)
	{
		_crops = CastleManorManager.getInstance().getCrops();
		_hideButtons = hideButtons;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x25);
		writeC(_hideButtons ? 0x01 : 0x00); // Hide "Seed Purchase" and "Crop Sales" buttons
		writeD(_crops.size());
		for (L2Seed crop : _crops)
		{
			writeD(crop.getCropId()); // crop Id
			writeD(crop.getLevel()); // level
			writeD(crop.getSeedReferencePrice()); // seed price
			writeD(crop.getCropReferencePrice()); // crop price
			writeC(1); // Reward 1 type
			writeD(crop.getReward(1)); // Reward 1 itemId
			writeC(1); // Reward 2 type
			writeD(crop.getReward(2)); // Reward 2 itemId
		}
	}
}