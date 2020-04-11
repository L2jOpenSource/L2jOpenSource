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
import java.util.Set;

import com.l2jserver.gameserver.instancemanager.CastleManorManager;
import com.l2jserver.gameserver.model.CropProcure;
import com.l2jserver.gameserver.model.L2Seed;

/**
 * @author l3x
 */
public class ExShowCropSetting extends L2GameServerPacket
{
	private final int _manorId;
	private final Set<L2Seed> _seeds;
	private final Map<Integer, CropProcure> _current = new HashMap<>();
	private final Map<Integer, CropProcure> _next = new HashMap<>();
	
	public ExShowCropSetting(int manorId)
	{
		final CastleManorManager manor = CastleManorManager.getInstance();
		_manorId = manorId;
		_seeds = manor.getSeedsForCastle(_manorId);
		for (L2Seed s : _seeds)
		{
			// Current period
			CropProcure cp = manor.getCropProcure(manorId, s.getCropId(), false);
			if (cp != null)
			{
				_current.put(s.getCropId(), cp);
			}
			// Next period
			cp = manor.getCropProcure(manorId, s.getCropId(), true);
			if (cp != null)
			{
				_next.put(s.getCropId(), cp);
			}
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE); // Id
		writeH(0x2b); // SubId
		
		writeD(_manorId); // manor id
		writeD(_seeds.size()); // size
		
		CropProcure cp;
		for (L2Seed s : _seeds)
		{
			writeD(s.getCropId()); // crop id
			writeD(s.getLevel()); // seed level
			writeC(1);
			writeD(s.getReward(1)); // reward 1 id
			writeC(1);
			writeD(s.getReward(2)); // reward 2 id
			writeD(s.getCropLimit()); // next sale limit
			writeD(0); // ???
			writeD(s.getCropMinPrice()); // min crop price
			writeD(s.getCropMaxPrice()); // max crop price
			// Current period
			if (_current.containsKey(s.getCropId()))
			{
				cp = _current.get(s.getCropId());
				writeQ(cp.getStartAmount()); // buy
				writeQ(cp.getPrice()); // price
				writeC(cp.getReward()); // reward
			}
			else
			{
				writeQ(0);
				writeQ(0);
				writeC(0);
			}
			// Next period
			if (_next.containsKey(s.getCropId()))
			{
				cp = _next.get(s.getCropId());
				writeQ(cp.getStartAmount()); // buy
				writeQ(cp.getPrice()); // price
				writeC(cp.getReward()); // reward
			}
			else
			{
				writeQ(0);
				writeQ(0);
				writeC(0);
			}
		}
		_next.clear();
		_current.clear();
	}
}