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
import com.l2jserver.gameserver.model.L2Seed;
import com.l2jserver.gameserver.model.SeedProduction;

/**
 * @author l3x
 */
public class ExShowSeedSetting extends L2GameServerPacket
{
	private final int _manorId;
	private final Set<L2Seed> _seeds;
	private final Map<Integer, SeedProduction> _current = new HashMap<>();
	private final Map<Integer, SeedProduction> _next = new HashMap<>();
	
	public ExShowSeedSetting(int manorId)
	{
		final CastleManorManager manor = CastleManorManager.getInstance();
		_manorId = manorId;
		_seeds = manor.getSeedsForCastle(_manorId);
		for (L2Seed s : _seeds)
		{
			// Current period
			SeedProduction sp = manor.getSeedProduct(manorId, s.getSeedId(), false);
			if (sp != null)
			{
				_current.put(s.getSeedId(), sp);
			}
			// Next period
			sp = manor.getSeedProduct(manorId, s.getSeedId(), true);
			if (sp != null)
			{
				_next.put(s.getSeedId(), sp);
			}
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE); // Id
		writeH(0x26); // SubId
		
		writeD(_manorId); // manor id
		writeD(_seeds.size()); // size
		
		SeedProduction sp;
		for (L2Seed s : _seeds)
		{
			writeD(s.getSeedId()); // seed id
			writeD(s.getLevel()); // level
			writeC(1);
			writeD(s.getReward(1)); // reward 1 id
			writeC(1);
			writeD(s.getReward(2)); // reward 2 id
			writeD(s.getSeedLimit()); // next sale limit
			writeD(s.getSeedReferencePrice()); // price for castle to produce 1
			writeD(s.getSeedMinPrice()); // min seed price
			writeD(s.getSeedMaxPrice()); // max seed price
			// Current period
			if (_current.containsKey(s.getSeedId()))
			{
				sp = _current.get(s.getSeedId());
				writeQ(sp.getStartAmount()); // sales
				writeQ(sp.getPrice()); // price
			}
			else
			{
				writeQ(0);
				writeQ(0);
			}
			// Next period
			if (_next.containsKey(s.getSeedId()))
			{
				sp = _next.get(s.getSeedId());
				writeQ(sp.getStartAmount()); // sales
				writeQ(sp.getPrice()); // price
			}
			else
			{
				writeQ(0);
				writeQ(0);
			}
		}
		_current.clear();
		_next.clear();
	}
}