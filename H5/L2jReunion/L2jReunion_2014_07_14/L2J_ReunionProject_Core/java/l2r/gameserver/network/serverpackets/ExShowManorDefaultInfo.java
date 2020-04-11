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

/**
 * @author l3x
 */
public class ExShowManorDefaultInfo extends L2GameServerPacket
{
	private List<Integer> _crops = null;
	
	public ExShowManorDefaultInfo()
	{
		_crops = ManorData.getInstance().getAllCrops();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x25);
		writeC(0);
		writeD(_crops.size());
		for (int cropId : _crops)
		{
			writeD(cropId); // crop Id
			writeD(ManorData.getInstance().getSeedLevelByCrop(cropId)); // level
			writeD(ManorData.getInstance().getSeedBasicPriceByCrop(cropId)); // seed
			// price
			writeD(ManorData.getInstance().getCropBasicPrice(cropId)); // crop
			// price
			writeC(1); // rewrad 1 Type
			writeD(ManorData.getInstance().getRewardItem(cropId, 1)); // Rewrad 1
			// Type Item
			// Id
			writeC(1); // rewrad 2 Type
			writeD(ManorData.getInstance().getRewardItem(cropId, 2)); // Rewrad 2
			// Type Item
			// Id
		}
	}
}
