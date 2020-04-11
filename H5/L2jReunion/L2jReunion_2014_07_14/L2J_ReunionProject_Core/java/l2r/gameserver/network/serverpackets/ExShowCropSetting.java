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
import l2r.gameserver.model.CropProcure;
import l2r.gameserver.model.entity.Castle;

/**
 * @author l3x
 */
public class ExShowCropSetting extends L2GameServerPacket
{
	private final int _manorId;
	private final int _count;
	private final long[] _cropData; // data to send, size:_count*14
	
	public ExShowCropSetting(int manorId)
	{
		_manorId = manorId;
		Castle c = CastleManager.getInstance().getCastleById(_manorId);
		List<Integer> crops = ManorData.getInstance().getCropsForCastle(_manorId);
		_count = crops.size();
		_cropData = new long[_count * 14];
		int i = 0;
		for (int cr : crops)
		{
			_cropData[(i * 14) + 0] = cr;
			_cropData[(i * 14) + 1] = ManorData.getInstance().getSeedLevelByCrop(cr);
			_cropData[(i * 14) + 2] = ManorData.getInstance().getRewardItem(cr, 1);
			_cropData[(i * 14) + 3] = ManorData.getInstance().getRewardItem(cr, 2);
			_cropData[(i * 14) + 4] = ManorData.getInstance().getCropPuchaseLimit(cr);
			_cropData[(i * 14) + 5] = 0; // Looks like not used
			_cropData[(i * 14) + 6] = (ManorData.getInstance().getCropBasicPrice(cr) * 60) / 100;
			_cropData[(i * 14) + 7] = ManorData.getInstance().getCropBasicPrice(cr) * 10;
			CropProcure cropPr = c.getCrop(cr, CastleManorManager.PERIOD_CURRENT);
			if (cropPr != null)
			{
				_cropData[(i * 14) + 8] = cropPr.getStartAmount();
				_cropData[(i * 14) + 9] = cropPr.getPrice();
				_cropData[(i * 14) + 10] = cropPr.getReward();
			}
			else
			{
				_cropData[(i * 14) + 8] = 0;
				_cropData[(i * 14) + 9] = 0;
				_cropData[(i * 14) + 10] = 0;
			}
			cropPr = c.getCrop(cr, CastleManorManager.PERIOD_NEXT);
			if (cropPr != null)
			{
				_cropData[(i * 14) + 11] = cropPr.getStartAmount();
				_cropData[(i * 14) + 12] = cropPr.getPrice();
				_cropData[(i * 14) + 13] = cropPr.getReward();
			}
			else
			{
				_cropData[(i * 14) + 11] = 0;
				_cropData[(i * 14) + 12] = 0;
				_cropData[(i * 14) + 13] = 0;
			}
			i++;
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE); // Id
		writeH(0x2b); // SubId
		
		writeD(_manorId); // manor id
		writeD(_count); // size
		
		for (int i = 0; i < _count; i++)
		{
			writeD((int) _cropData[(i * 14) + 0]); // crop id
			writeD((int) _cropData[(i * 14) + 1]); // seed level
			writeC(1);
			writeD((int) _cropData[(i * 14) + 2]); // reward 1 id
			writeC(1);
			writeD((int) _cropData[(i * 14) + 3]); // reward 2 id
			
			writeD((int) _cropData[(i * 14) + 4]); // next sale limit
			writeD((int) _cropData[(i * 14) + 5]); // ???
			writeD((int) _cropData[(i * 14) + 6]); // min crop price
			writeD((int) _cropData[(i * 14) + 7]); // max crop price
			
			writeQ(_cropData[(i * 14) + 8]); // today buy
			writeQ(_cropData[(i * 14) + 9]); // today price
			writeC((int) _cropData[(i * 14) + 10]); // today reward
			
			writeQ(_cropData[(i * 14) + 11]); // next buy
			writeQ(_cropData[(i * 14) + 12]); // next price
			writeC((int) _cropData[(i * 14) + 13]); // next reward
		}
	}
}
