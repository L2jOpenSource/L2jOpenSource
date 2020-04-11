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
package com.l2jserver.gameserver.model.zone;

/**
 * Zone Ids.
 * @author Zoey76
 */
public enum ZoneId
{
	PVP,
	PEACE,
	SIEGE,
	MOTHER_TREE,
	CLAN_HALL,
	LANDING,
	NO_LANDING,
	WATER,
	JAIL,
	MONSTER_TRACK,
	CASTLE,
	SWAMP,
	NO_SUMMON_FRIEND,
	FORT,
	NO_STORE,
	TOWN,
	QUEEN_ANT,
	SCRIPT,
	HQ,
	DANGER_AREA,
	ALTERED,
	NO_BOOKMARK,
	NO_ITEM_DROP,
	NO_RESTART,
	NO_ZEUS;
	
	public static int getZoneCount()
	{
		return values().length;
	}
}
