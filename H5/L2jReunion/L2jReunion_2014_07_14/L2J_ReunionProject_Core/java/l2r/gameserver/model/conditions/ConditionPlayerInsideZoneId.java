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
package l2r.gameserver.model.conditions;

import java.util.ArrayList;

import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.zone.L2ZoneType;

/**
 * @author UnAfraid
 */
public class ConditionPlayerInsideZoneId extends Condition
{
	private final ArrayList<Integer> _zones;
	
	public ConditionPlayerInsideZoneId(ArrayList<Integer> zones)
	{
		_zones = zones;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (env.getPlayer() == null)
		{
			return false;
		}
		
		for (L2ZoneType zone : ZoneManager.getInstance().getZones(env.getCharacter()))
		{
			if (_zones.contains(zone.getId()))
			{
				return true;
			}
		}
		return false;
	}
}
