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
package l2r.gameserver.model.zone.type;

import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.zone.L2ZoneType;

/**
 * Zone where 'Build Headquarters' is allowed.
 * @author Gnacik
 */
public class L2HqZone extends L2ZoneType
{
	public L2HqZone(final int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if ("castleId".equals(name))
		{
			// TODO
		}
		else if ("fortId".equals(name))
		{
			// TODO
		}
		else if ("clanHallId".equals(name))
		{
			// TODO
		}
		else if ("territoryId".equals(name))
		{
			// TODO
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(final L2Character character)
	{
		if (character.isPlayer())
		{
			character.setInsideZone(ZoneIdType.HQ, true);
		}
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
		if (character.isPlayer())
		{
			character.setInsideZone(ZoneIdType.HQ, false);
		}
	}
	
	@Override
	public void onDieInside(final L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(final L2Character character)
	{
	}
}
