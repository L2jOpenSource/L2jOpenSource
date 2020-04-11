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
package l2r.gameserver.network.serverpackets;

import java.util.List;

import l2r.gameserver.datatables.xml.ManorData;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.instancemanager.CastleManorManager;
import l2r.gameserver.model.SeedProduction;
import l2r.gameserver.model.entity.Castle;

/**
 * @author l3x
 */
public class ExShowSeedSetting extends L2GameServerPacket
{
	private final int _manorId;
	private final int _count;
	private final long[] _seedData; // data to send, size:_count*12
	
	public ExShowSeedSetting(int manorId)
	{
		_manorId = manorId;
		Castle c = CastleManager.getInstance().getCastleById(_manorId);
		List<Integer> seeds = ManorData.getInstance().getSeedsForCastle(_manorId);
		_count = seeds.size();
		_seedData = new long[_count * 12];
		int i = 0;
		for (int s : seeds)
		{
			_seedData[(i * 12) + 0] = s;
			_seedData[(i * 12) + 1] = ManorData.getInstance().getSeedLevel(s);
			_seedData[(i * 12) + 2] = ManorData.getInstance().getRewardItemBySeed(s, 1);
			_seedData[(i * 12) + 3] = ManorData.getInstance().getRewardItemBySeed(s, 2);
			_seedData[(i * 12) + 4] = ManorData.getInstance().getSeedSaleLimit(s);
			_seedData[(i * 12) + 5] = ManorData.getInstance().getSeedBuyPrice(s);
			_seedData[(i * 12) + 6] = (ManorData.getInstance().getSeedBasicPrice(s) * 60) / 100;
			_seedData[(i * 12) + 7] = ManorData.getInstance().getSeedBasicPrice(s) * 10;
			SeedProduction seedPr = c.getSeed(s, CastleManorManager.PERIOD_CURRENT);
			if (seedPr != null)
			{
				_seedData[(i * 12) + 8] = seedPr.getStartProduce();
				_seedData[(i * 12) + 9] = seedPr.getPrice();
			}
			else
			{
				_seedData[(i * 12) + 8] = 0;
				_seedData[(i * 12) + 9] = 0;
			}
			seedPr = c.getSeed(s, CastleManorManager.PERIOD_NEXT);
			if (seedPr != null)
			{
				_seedData[(i * 12) + 10] = seedPr.getStartProduce();
				_seedData[(i * 12) + 11] = seedPr.getPrice();
			}
			else
			{
				_seedData[(i * 12) + 10] = 0;
				_seedData[(i * 12) + 11] = 0;
			}
			i++;
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE); // Id
		writeH(0x26); // SubId
		
		writeD(_manorId); // manor id
		writeD(_count); // size
		
		for (int i = 0; i < _count; i++)
		{
			writeD((int) _seedData[(i * 12) + 0]); // seed id
			writeD((int) _seedData[(i * 12) + 1]); // level
			writeC(1);
			writeD((int) _seedData[(i * 12) + 2]); // reward 1 id
			writeC(1);
			writeD((int) _seedData[(i * 12) + 3]); // reward 2 id
			
			writeD((int) _seedData[(i * 12) + 4]); // next sale limit
			writeD((int) _seedData[(i * 12) + 5]); // price for castle to produce 1
			writeD((int) _seedData[(i * 12) + 6]); // min seed price
			writeD((int) _seedData[(i * 12) + 7]); // max seed price
			
			writeQ(_seedData[(i * 12) + 8]); // today sales
			writeQ(_seedData[(i * 12) + 9]); // today price
			writeQ(_seedData[(i * 12) + 10]); // next sales
			writeQ(_seedData[(i * 12) + 11]); // next price
		}
	}
}
