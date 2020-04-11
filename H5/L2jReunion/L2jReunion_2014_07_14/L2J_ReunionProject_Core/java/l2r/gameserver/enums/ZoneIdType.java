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
package l2r.gameserver.enums;

/**
 * Zone Ids.
 * @author Zoey76
 */
public enum ZoneIdType
{
	PVP(0),
	PEACE(1),
	SIEGE(2),
	MOTHER_TREE(3),
	CLAN_HALL(4),
	LANDING(5),
	NO_LANDING(6),
	WATER(7),
	JAIL(8),
	MONSTER_TRACK(9),
	CASTLE(10),
	SWAMP(11),
	NO_SUMMON_FRIEND(12),
	FORT(13),
	NO_STORE(14),
	TOWN(15),
	SCRIPT(16),
	HQ(17),
	DANGER_AREA(18),
	ALTERED(19),
	NO_BOOKMARK(20),
	NO_ITEM_DROP(21),
	NO_RESTART(22),
	ZONE_CHAOTIC(23),
	QUEEN_ANT(24),
	FLAG(25);
	
	private final int _id;
	
	private ZoneIdType(int id)
	{
		_id = id;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public static int getZoneCount()
	{
		return values().length;
	}
}
