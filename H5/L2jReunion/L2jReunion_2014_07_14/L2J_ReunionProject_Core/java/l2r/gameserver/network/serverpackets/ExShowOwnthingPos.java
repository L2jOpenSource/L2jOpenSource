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

import l2r.gameserver.instancemanager.TerritoryWarManager;
import l2r.gameserver.model.TerritoryWard;

/**
 * @author -Gigiikun-
 */
public class ExShowOwnthingPos extends L2GameServerPacket
{
	public static final ExShowOwnthingPos STATIC_PACKET = new ExShowOwnthingPos();
	
	private ExShowOwnthingPos()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x93);
		
		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			List<TerritoryWard> territoryWardList = TerritoryWarManager.getInstance().getAllTerritoryWards();
			writeD(territoryWardList.size());
			for (TerritoryWard ward : territoryWardList)
			{
				writeD(ward.getTerritoryId());
				
				if (ward.getNpc() != null)
				{
					writeD(ward.getNpc().getX());
					writeD(ward.getNpc().getY());
					writeD(ward.getNpc().getZ());
				}
				else if (ward.getPlayer() != null)
				{
					writeD(ward.getPlayer().getX());
					writeD(ward.getPlayer().getY());
					writeD(ward.getPlayer().getZ());
				}
				else
				{
					writeD(0x00);
					writeD(0x00);
					writeD(0x00);
				}
			}
		}
		else
		{
			writeD(0x00);
		}
	}
}
