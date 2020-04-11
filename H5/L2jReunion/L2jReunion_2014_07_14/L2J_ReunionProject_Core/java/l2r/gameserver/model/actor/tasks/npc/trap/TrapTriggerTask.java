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
package l2r.gameserver.model.actor.tasks.npc.trap;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.actor.instance.L2TrapInstance;

/**
 * Trap trigger task.
 * @author Zoey76
 */
public class TrapTriggerTask implements Runnable
{
	private final L2TrapInstance _trap;
	
	public TrapTriggerTask(L2TrapInstance trap)
	{
		_trap = trap;
	}
	
	@Override
	public void run()
	{
		try
		{
			_trap.doCast(_trap.getSkill());
			ThreadPoolManager.getInstance().scheduleGeneral(new TrapUnsummonTask(_trap), _trap.getSkill().getHitTime() + 300);
		}
		catch (Exception e)
		{
			_trap.unSummon();
		}
	}
}
