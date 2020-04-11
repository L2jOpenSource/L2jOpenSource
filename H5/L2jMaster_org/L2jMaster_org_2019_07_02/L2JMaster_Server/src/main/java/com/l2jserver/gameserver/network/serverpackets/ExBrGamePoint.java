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

public final class ExBrGamePoint extends L2GameServerPacket
{
	private final int _playerObj;
	private long _points;
	
	public ExBrGamePoint(L2PcInstance player)
	{
		_playerObj = player.getObjectId();
		if (Config.PRIME_SHOP_POINTS_ID == -1)
		{
			_points = player.getPrimeShopPoints();
		}
		else
		{
			_points = player.getInventory().getInventoryItemCount(Config.PRIME_SHOP_POINTS_ID, -1);
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0xD5);
		writeD(_playerObj);
		writeQ(_points);
		writeD(0x00);
	}
}