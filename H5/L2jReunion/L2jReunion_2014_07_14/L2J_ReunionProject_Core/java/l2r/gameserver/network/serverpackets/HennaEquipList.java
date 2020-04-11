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

import l2r.gameserver.datatables.xml.HennaData;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.L2Henna;

/**
 * @author Zoey76
 */
public class HennaEquipList extends L2GameServerPacket
{
	private final L2PcInstance _player;
	private final List<L2Henna> _hennaEquipList;
	
	public HennaEquipList(L2PcInstance player)
	{
		_player = player;
		_hennaEquipList = HennaData.getInstance().getHennaList(player.getClassId());
	}
	
	public HennaEquipList(L2PcInstance player, List<L2Henna> list)
	{
		_player = player;
		_hennaEquipList = list;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xEE);
		writeQ(_player.getAdena()); // activeChar current amount of Adena
		writeD(3); // available equip slot
		writeD(_hennaEquipList.size());
		
		for (L2Henna henna : _hennaEquipList)
		{
			// Player must have at least one dye in inventory
			// to be able to see the Henna that can be applied with it.
			if ((_player.getInventory().getItemByItemId(henna.getDyeItemId())) != null)
			{
				writeD(henna.getDyeId()); // dye Id
				writeD(henna.getDyeItemId()); // item Id of the dye
				writeQ(henna.getWearCount()); // amount of dyes required
				writeQ(henna.getWearFee()); // amount of Adena required
				writeD(henna.isAllowedClass(_player.getClassId()) ? 0x01 : 0x00); // meet the requirement or not
			}
		}
	}
}
