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
package com.l2jserver.gameserver.taskmanager.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.l2jserver.Config;
import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.instancemanager.MailManager;
import com.l2jserver.gameserver.model.entity.Message;
import com.l2jserver.gameserver.taskmanager.Task;
import com.l2jserver.gameserver.taskmanager.TaskManager;
import com.l2jserver.gameserver.taskmanager.TaskManager.ExecutedTask;
import com.l2jserver.gameserver.taskmanager.TaskTypes;

/**
 * Birthday Gift task.
 * @author Zoey76
 */
public class TaskBirthday extends Task
{
	private static final String NAME = "birthday";
	/** Get all players that have had a birthday since last check. */
	private static final String SELECT_PENDING_BIRTHDAY_GIFTS = "SELECT charId, char_name, createDate, (YEAR(NOW()) - YEAR(createDate)) AS age FROM characters WHERE (YEAR(NOW()) - YEAR(createDate) > 0) AND ((DATE_ADD(createDate, INTERVAL (YEAR(NOW()) - YEAR(createDate)) YEAR)) BETWEEN FROM_UNIXTIME(?) AND NOW())";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		// TODO(Zoey76): Fix first run.
		final int birthdayGiftCount = giveBirthdayGifts(task.getLastActivation());
		
		_log.info("BirthdayManager: " + birthdayGiftCount + " gifts sent.");
	}
	
	private int giveBirthdayGifts(long lastActivation)
	{
		int birthdayGiftCount = 0;
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SELECT_PENDING_BIRTHDAY_GIFTS))
		{
			ps.setLong(1, TimeUnit.SECONDS.convert(lastActivation, TimeUnit.MILLISECONDS));
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					String text = Config.ALT_BIRTHDAY_MAIL_TEXT;
					text = text.replaceAll("$c1", rs.getString("char_name"));
					text = text.replaceAll("$s1", Integer.toString(rs.getInt("age")));
					
					final Message msg = new Message(rs.getInt("charId"), Config.ALT_BIRTHDAY_MAIL_SUBJECT, text, Message.SendBySystem.ALEGRIA);
					msg.createAttachments().addItem("Birthday", Config.ALT_BIRTHDAY_GIFT, 1, null, null);
					MailManager.getInstance().sendMessage(msg);
					birthdayGiftCount++;
				}
			}
		}
		catch (SQLException e)
		{
			_log.warning("Error checking birthdays: " + e.getMessage());
		}
		return birthdayGiftCount;
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
	}
}
