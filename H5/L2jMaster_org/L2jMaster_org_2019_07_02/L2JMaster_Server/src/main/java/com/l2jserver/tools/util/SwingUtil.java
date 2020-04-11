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
package com.l2jserver.tools.util;

import java.awt.Container;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

import com.l2jserver.tools.util.swing.BackgroundTask;
import com.l2jserver.tools.util.swing.BackgroundTaskRunner;

/**
 * @author HorridoJoho
 */
public final class SwingUtil
{
	public static <T> T runBackgroundTaskWithDialog(Window owner, String title, Container contentPane, BackgroundTask<T> task) throws Throwable
	{
		final JDialog dialog = new JDialog(owner, title);
		dialog.setContentPane(contentPane);
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.pack();
		dialog.setLocationByPlatform(true);
		dialog.setLocationRelativeTo(owner);
		
		BackgroundTaskRunner<T> runner = new BackgroundTaskRunner<>(dialog, task);
		dialog.dispose();
		if (runner.getThrown() != null)
		{
			throw runner.getThrown();
		}
		return runner.getResult();
	}
}