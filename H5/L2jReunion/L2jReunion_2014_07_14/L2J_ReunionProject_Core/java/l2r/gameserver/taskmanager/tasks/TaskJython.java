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
package l2r.gameserver.taskmanager.tasks;

import l2r.gameserver.taskmanager.Task;
import l2r.gameserver.taskmanager.TaskManager.ExecutedTask;

import org.python.util.PythonInterpreter;

/**
 * @author Layane
 */
public class TaskJython extends Task
{
	public static final String NAME = "jython";
	private final PythonInterpreter _python = new PythonInterpreter();
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		_python.cleanup();
		_python.exec("import sys");
		_python.execfile("data/scripts/cron/" + task.getParams()[2]);
	}
}
