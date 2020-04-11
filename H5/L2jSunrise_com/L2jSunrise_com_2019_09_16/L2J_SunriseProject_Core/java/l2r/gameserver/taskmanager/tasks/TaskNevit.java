/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.gameserver.taskmanager.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;

import l2r.L2DatabaseFactory;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.ExNevitAdventTimeChange;
import l2r.gameserver.taskmanager.Task;
import l2r.gameserver.taskmanager.TaskManager;
import l2r.gameserver.taskmanager.TaskManager.ExecutedTask;
import l2r.gameserver.taskmanager.TaskTypes;

/**
 * @author Janiko
 */
public class TaskNevit extends Task
{
	private static final String NAME = "nevit_system";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("UPDATE character_variables SET val=? WHERE var='hunting_time'"))
			{
				ps.setInt(1, 0); // Refuel-reset hunting bonus time
				ps.execute();
			}
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Could not reset Nevit System: " + e);
		}
		
		for (L2PcInstance player : L2World.getInstance().getPlayers())
		{
			if ((player != null) && player.isOnline() && !player.isInOfflineMode())
			{
				player.getNevitSystem().setAdventTime(0); // Refuel-reset hunting bonus time
				player.sendPacket(new ExNevitAdventTimeChange(player.getNevitSystem().getAdventTime(), true));
			}
		}
		
		_log.info("Nevit system reseted.");
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
	}
}