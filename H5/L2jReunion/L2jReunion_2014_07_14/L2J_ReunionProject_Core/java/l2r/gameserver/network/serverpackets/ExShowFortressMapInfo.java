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

import javolution.util.FastList;
import l2r.gameserver.instancemanager.FortSiegeManager;
import l2r.gameserver.model.FortSiegeSpawn;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.entity.Fort;

/**
 * @author KenM
 */
public class ExShowFortressMapInfo extends L2GameServerPacket
{
	private final Fort _fortress;
	
	public ExShowFortressMapInfo(Fort fortress)
	{
		_fortress = fortress;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x7D);
		
		writeD(_fortress.getResidenceId());
		writeD(_fortress.getSiege().isInProgress() ? 1 : 0); // fortress siege status
		writeD(_fortress.getFortSize()); // barracks count
		
		FastList<FortSiegeSpawn> commanders = FortSiegeManager.getInstance().getCommanderSpawnList(_fortress.getResidenceId());
		if ((commanders != null) && (commanders.size() != 0) && _fortress.getSiege().isInProgress())
		{
			switch (commanders.size())
			{
				case 3:
				{
					for (FortSiegeSpawn spawn : commanders)
					{
						if (isSpawned(spawn.getId()))
						{
							writeD(0);
						}
						else
						{
							writeD(1);
						}
					}
					break;
				}
				case 4: // TODO: change 4 to 5 once control room supported
				{
					int count = 0;
					for (FortSiegeSpawn spawn : commanders)
					{
						count++;
						if (count == 4)
						{
							writeD(1); // TODO: control room emulated
						}
						if (isSpawned(spawn.getId()))
						{
							writeD(0);
						}
						else
						{
							writeD(1);
						}
					}
					break;
				}
			}
		}
		else
		{
			for (int i = 0; i < _fortress.getFortSize(); i++)
			{
				writeD(0);
			}
		}
	}
	
	/**
	 * @param npcId
	 * @return
	 */
	private boolean isSpawned(int npcId)
	{
		boolean ret = false;
		for (L2Spawn spawn : _fortress.getSiege().getCommanders())
		{
			if (spawn.getId() == npcId)
			{
				ret = true;
				break;
			}
		}
		return ret;
	}
}
