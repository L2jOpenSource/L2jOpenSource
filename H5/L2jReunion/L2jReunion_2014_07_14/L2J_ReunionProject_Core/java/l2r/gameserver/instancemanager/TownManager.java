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
package l2r.gameserver.instancemanager;

import l2r.gameserver.model.entity.Castle;
import l2r.gameserver.model.zone.L2ZoneType;
import l2r.gameserver.model.zone.type.L2TownZone;

public class TownManager
{
	public static final int getTownCastle(int townId)
	{
		switch (townId)
		{
			case 912:
				return 1;
			case 916:
				return 2;
			case 918:
				return 3;
			case 922:
				return 4;
			case 924:
				return 5;
			case 926:
				return 6;
			case 1538:
				return 7;
			case 1537:
				return 8;
			case 1714:
				return 9;
			default:
				return 0;
		}
	}
	
	public static final boolean townHasCastleInSiege(int townId)
	{
		int castleIndex = getTownCastle(townId);
		
		if (castleIndex > 0)
		{
			Castle castle = CastleManager.getInstance().getCastles().get(CastleManager.getInstance().getCastleIndex(castleIndex));
			if (castle != null)
			{
				return castle.getSiege().isInProgress();
			}
		}
		return false;
	}
	
	public static final boolean townHasCastleInSiege(int x, int y)
	{
		return townHasCastleInSiege(MapRegionManager.getInstance().getMapRegionLocId(x, y));
	}
	
	public static final L2TownZone getTown(int townId)
	{
		for (L2TownZone temp : ZoneManager.getInstance().getAllZones(L2TownZone.class))
		{
			if (temp.getTownId() == townId)
			{
				return temp;
			}
		}
		return null;
	}
	
	/**
	 * Returns the town at that position (if any)
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static final L2TownZone getTown(int x, int y, int z)
	{
		for (L2ZoneType temp : ZoneManager.getInstance().getZones(x, y, z))
		{
			if (temp instanceof L2TownZone)
			{
				return (L2TownZone) temp;
			}
		}
		return null;
	}
}
