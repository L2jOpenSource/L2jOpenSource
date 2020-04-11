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
package com.l2jserver.tools.dbinstaller.frontend.swing;

import java.text.MessageFormat;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.l2jserver.tools.util.app.IApplicationFrontend;

/**
 * @author HorridoJoho
 */
public abstract class AbstractGUI extends JFrame implements IApplicationFrontend
{
	private static final long serialVersionUID = 1L;
	
	protected AbstractGUI(String title)
	{
		super(title);
	}
	
	@Override
	public void reportInfo(boolean drawAttention, String message)
	{
		if (drawAttention)
		{
			JOptionPane.showMessageDialog(this, message, "Database Installer", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	@Override
	public final void reportInfo(boolean drawAttention, String message, Object... args)
	{
		reportInfo(drawAttention, MessageFormat.format(message, args));
	}
	
	@Override
	public void reportWarn(boolean drawAttention, String message)
	{
		if (drawAttention)
		{
			JOptionPane.showMessageDialog(this, message, "Database Installer", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	@Override
	public final void reportWarn(boolean drawAttention, String message, Object... args)
	{
		reportWarn(drawAttention, MessageFormat.format(message, args));
	}
	
	@Override
	public void reportError(boolean drawAttention, String message)
	{
		if (drawAttention)
		{
			JOptionPane.showMessageDialog(this, message, "Database Installer", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	@Override
	public final void reportError(boolean drawAttention, Throwable t, String message)
	{
		if (t != null)
		{
			message += MessageFormat.format("\n\nReason:\n{0}", t.getMessage());
		}
		
		reportError(drawAttention, message);
	}
	
	@Override
	public final void reportError(boolean drawAttention, Throwable t, String message, Object... args)
	{
		reportError(drawAttention, t, MessageFormat.format(message, args));
	}
	
	@Override
	public final String requestUserInput(String message, Object... args)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public final boolean requestUserConfirm(String message, Object... args)
	{
		return JOptionPane.showConfirmDialog(this, MessageFormat.format(message, args), "Database Installer", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == 0;
	}
	
	@Override
	public void close()
	{
		setVisible(false);
		dispose();
	}
}