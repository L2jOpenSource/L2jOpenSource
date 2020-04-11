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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultCaret;

import com.l2jserver.tools.images.ImagesTable;

/**
 * @author mrTJO, HorridoJoho
 */
public class DBInstallerGUI extends AbstractGUI
{
	private static final long serialVersionUID = -1005504757826370170L;
	
	private final JTextArea _progArea;
	
	public DBInstallerGUI()
	{
		super("L2J Database Installer");
		setLayout(new BorderLayout());
		setDefaultLookAndFeelDecorated(true);
		setIconImage(ImagesTable.getImage("l2j.png").getImage());
		
		int width = 480;
		int height = 360;
		Dimension resolution = Toolkit.getDefaultToolkit().getScreenSize();
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setBounds((resolution.width - width) / 2, (resolution.height - height) / 2, width, height);
		setResizable(false);
		
		_progArea = new JTextArea();
		_progArea.setEditable(false);
		DefaultCaret caret = (DefaultCaret) _progArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		add(new JScrollPane(_progArea), BorderLayout.CENTER);
		setVisible(true);
	}
	
	private void appendToTextArea(String message)
	{
		_progArea.append(message);
		_progArea.append("\n");
	}
	
	@Override
	public void reportInfo(boolean drawAttention, String message)
	{
		appendToTextArea(message);
		super.reportInfo(drawAttention, message);
	}
	
	@Override
	public void reportWarn(boolean drawAttention, String message)
	{
		appendToTextArea(message);
		super.reportWarn(drawAttention, message);
	}
	
	@Override
	public void reportError(boolean drawAttention, String message)
	{
		appendToTextArea(message);
		super.reportError(drawAttention, message);
	}
}