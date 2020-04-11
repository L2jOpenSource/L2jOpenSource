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
import com.l2jserver.gameserver.model.SeedProduction;

/**
 * @author l3x
 */
public class ExShowSeedInfo extends L2GameServerPacket
{
	private final List<SeedProduction> _seeds;
	private final int _manorId;
	private final boolean _hideButtons;
	
	public ExShowSeedInfo(int manorId, boolean nextPeriod, boolean hideButtons)
	{
		_manorId = manorId;
		_hideButtons = hideButtons;
		
		final CastleManorManager manor = CastleManorManager.getInstance();
		_seeds = (nextPeriod && !manor.isManorApproved()) ? null : manor.getSeedProduction(manorId, nextPeriod);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE); // Id
		writeH(0x23); // SubId
		writeC(_hideButtons ? 0x01 : 0x00); // Hide "Seed Purchase" button
		writeD(_manorId); // Manor ID
		writeD(0x00); // Unknown
		if (_seeds == null)
		{
			writeD(0);
			return;
		}
		writeD(_seeds.size());
		for (SeedProduction seed : _seeds)
		{
			writeD(seed.getId()); // Seed id
			writeQ(seed.getAmount()); // Left to buy
			writeQ(seed.getStartAmount()); // Started amount
			writeQ(seed.getPrice()); // Sell Price
			final L2Seed s = CastleManorManager.getInstance().getSeed(seed.getId());
			if (s == null)
			{
				writeD(0); // Seed level
				writeC(0x01); // Reward 1
				writeD(0); // Reward 1 - item id
				writeC(0x01); // Reward 2
				writeD(0); // Reward 2 - item id
			}
			else
			{
				writeD(s.getLevel()); // Seed level
				writeC(0x01); // Reward 1
				writeD(s.getReward(1)); // Reward 1 - item id
				writeC(0x01); // Reward 2
				writeD(s.getReward(2)); // Reward 2 - item id
			}
		}
	}
}