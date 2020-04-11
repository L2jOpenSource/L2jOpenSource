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
package l2r.gameserver.model.actor.knownlist;

import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.instance.L2DefenderInstance;
import l2r.gameserver.model.actor.instance.L2StaticObjectInstance;

public class StaticObjectKnownList extends CharKnownList
{
	public StaticObjectKnownList(L2StaticObjectInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public final L2StaticObjectInstance getActiveChar()
	{
		return (L2StaticObjectInstance) super.getActiveChar();
	}
	
	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		if (object instanceof L2DefenderInstance)
		{
			return 800;
		}
		if (!object.isPlayer())
		{
			return 0;
		}
		
		return 4000;
	}
	
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		if (object instanceof L2DefenderInstance)
		{
			return 600;
		}
		if (!object.isPlayer())
		{
			return 0;
		}
		return 2000;
	}
}
