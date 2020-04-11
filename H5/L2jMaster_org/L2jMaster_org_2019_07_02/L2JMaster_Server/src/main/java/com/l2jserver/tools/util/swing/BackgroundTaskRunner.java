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
package com.l2jserver.tools.util.swing;

import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Objects;

/**
 * @author HorridoJoho
 */
public class BackgroundTaskRunner<T> implements Runnable, ComponentListener
{
	private final Dialog _dialog;
	private final BackgroundTask<T> _task;
	private final Thread _thread;
	
	private Throwable _thrown;
	private T _result;
	
	public BackgroundTaskRunner(Dialog dialog, BackgroundTask<T> task)
	{
		Objects.requireNonNull(dialog);
		Objects.requireNonNull(task);
		_dialog = dialog;
		_task = task;
		_thread = new Thread(this, "L2J-TOOLS-BackgroundTaskRunner");
		
		_dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		_dialog.addComponentListener(this);
		_dialog.setVisible(true);
	}
	
	@Override
	public void run()
	{
		try
		{
			_result = _task.get();
		}
		catch (Throwable t)
		{
			_thrown = t;
		}
		finally
		{
			finishDialog();
		}
	}
	
	private void finishDialog()
	{
		if (_dialog != null)
		{
			_dialog.removeComponentListener(this);
			_dialog.setVisible(false);
		}
	}
	
	public Throwable getThrown()
	{
		return _thrown;
	}
	
	public T getResult()
	{
		return _result;
	}
	
	@Override
	public void componentResized(ComponentEvent e)
	{
	}
	
	@Override
	public void componentMoved(ComponentEvent e)
	{
	}
	
	@Override
	public void componentShown(ComponentEvent e)
	{
		try
		{
			_thread.start();
		}
		catch (Throwable t)
		{
			_thrown = t;
			finishDialog();
		}
	}
	
	@Override
	public void componentHidden(ComponentEvent e)
	{
	}
}