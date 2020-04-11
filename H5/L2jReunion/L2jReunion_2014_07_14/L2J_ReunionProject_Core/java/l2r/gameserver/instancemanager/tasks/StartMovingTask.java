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
package l2r.gameserver.instancemanager.tasks;

import l2r.gameserver.instancemanager.WalkingManager;
import l2r.gameserver.model.actor.L2Npc;

/**
 * Task which starts npc movement.
 * @author xban1x
 */
public final class StartMovingTask implements Runnable
{
	final L2Npc _npc;
	final String _routeName;
	
	public StartMovingTask(L2Npc npc, String routeName)
	{
		_npc = npc;
		_routeName = routeName;
	}
	
	@Override
	public void run()
	{
		if (_npc != null)
		{
			WalkingManager.getInstance().startMoving(_npc, _routeName);
		}
	}
}
