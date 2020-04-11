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

import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.stats.Env;

/**
 * The Class ConditionPlayerInstanceId.
 */
public class ConditionPlayerInstanceId extends Condition
{
	private final ArrayList<Integer> _instanceIds;
	
	/**
	 * Instantiates a new condition player instance id.
	 * @param instanceIds the instance ids
	 */
	public ConditionPlayerInstanceId(ArrayList<Integer> instanceIds)
	{
		_instanceIds = instanceIds;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (env.getPlayer() == null)
		{
			return false;
		}
		
		final int instanceId = env.getCharacter().getInstanceId();
		if (instanceId <= 0)
		{
			return false; // player not in instance
		}
		
		final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(env.getPlayer());
		if ((world == null) || (world.getInstanceId() != instanceId))
		{
			return false; // player in the different instance
		}
		return _instanceIds.contains(world.getTemplateId());
	}
}
