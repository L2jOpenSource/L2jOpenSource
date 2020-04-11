/*
 * Copyright (C) 2004-2016 L2J Server
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
import l2r.gameserver.network.serverpackets.ExVoteSystemInfo;
import l2r.gameserver.taskmanager.Task;
import l2r.gameserver.taskmanager.TaskManager;
import l2r.gameserver.taskmanager.TaskManager.ExecutedTask;
import l2r.gameserver.taskmanager.TaskTypes;

/**
 * @author Layane, vGodFather
 */
public class TaskRecom extends Task
{
	private static final String NAME = "recommendations";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		// final String RESTART_SYSTEM = "UPDATE character_reco_bonus SET rec_left = ?, time_left = ?, rec_have=GREATEST(rec_have-20,0) WHERE charId IN (SELECT charId FROM characters)";
		final String UPDATE_CHARACTERS_RECO = "UPDATE character_reco_bonus cr, characters c SET cr.time_left = 3600, cr.rec_left = 20, rec_have = IF(rec_have > 20, rec_have - 20, 0) WHERE c.online = 0 AND c.charId = cr.charId";
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTERS_RECO);
			statement.execute();
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Could not reset Recommendations System: " + e);
		}
		
		for (L2PcInstance player : L2World.getInstance().getPlayers())
		{
			if ((player != null))
			{
				player.stopRecoBonusTask();
				player.setRecomBonusTime(3600);
				player.setRecomLeft(20);
				player.setRecomHave(player.getRecomHave() - 20);
				if (!player.isInOfflineMode())
				{
					player.sendUserInfo(true);
					player.sendPacket(new ExVoteSystemInfo(player));
				}
			}
		}
		
		_log.info("Recommendations System reseted.");
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
	}
}
