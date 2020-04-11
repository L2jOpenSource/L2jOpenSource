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

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.util.Util;

/**
 * Exist NPC condition.
 * @author UnAfraid, Zoey76
 */
public class ConditionPlayerRangeFromNpc extends Condition
{
	/** NPC Ids. */
	private final int[] _npcIds;
	/** Radius to check. */
	private final int _radius;
	/** Expected value. */
	private final boolean _val;
	
	public ConditionPlayerRangeFromNpc(int[] npcIds, int radius, boolean val)
	{
		_npcIds = npcIds;
		_radius = radius;
		_val = val;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		boolean existNpc = false;
		if ((_npcIds != null) && (_npcIds.length > 0) && (_radius > 0))
		{
			for (L2Character target : env.getCharacter().getKnownList().getKnownCharactersInRadius(_radius))
			{
				if (target.isNpc() && Util.contains(_npcIds, target.getId()))
				{
					existNpc = true;
					break;
				}
			}
		}
		return existNpc == _val;
	}
}
