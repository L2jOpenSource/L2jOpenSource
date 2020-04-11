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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import com.l2jserver.tools.dbinstaller.RunTasks;
import com.l2jserver.tools.images.ImagesTable;
import com.l2jserver.tools.util.SQLUtil;
import com.l2jserver.tools.util.SwingUtil;
import com.l2jserver.tools.util.swing.SpringUtilities;

/**
 * @author mrTJO, HorridoJoho
 */
public class DBConfigGUI extends AbstractGUI
{
	private static final long serialVersionUID = -8391792251140797076L;
	
	JTextField _dbHost;
	JTextField _dbPort;
	JTextField _dbUser;
	JPasswordField _dbPass;
	JTextField _dbDbse;
	
	String _db;
	String _dir;
	String _cleanUp;
	
	Preferences _prop;
	
	public DBConfigGUI(String db, String dir, String cleanUp)
	{
		super("L2J Database Installer");
		setLayout(new SpringLayout());
		setDefaultLookAndFeelDecorated(true);
		setIconImage(ImagesTable.getImage("l2j.png").getImage());
		
		_db = db;
		_dir = dir;
		_cleanUp = cleanUp;
		
		int width = 320;
		int height = 240;
		Dimension resolution = Toolkit.getDefaultToolkit().getScreenSize();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds((resolution.width - width) / 2, (resolution.height - height) / 2, width, height);
		setResizable(false);
		
		_prop = Preferences.userRoot();
		
		// Host
		JLabel labelDbHost = new JLabel("Host: ", SwingConstants.LEFT);
		add(labelDbHost);
		_dbHost = new JTextField(15);
		_dbHost.setText(_prop.get("dbHost_" + db, "localhost"));
		labelDbHost.setLabelFor(_dbHost);
		add(_dbHost);
		
		// Port
		JLabel labelDbPort = new JLabel("Port: ", SwingConstants.LEFT);
		add(labelDbPort);
		_dbPort = new JTextField(15);
		_dbPort.setText(_prop.get("dbPort_" + db, "3306"));
		labelDbPort.setLabelFor(_dbPort);
		add(_dbPort);
		
		// Username
		JLabel labelDbUser = new JLabel("Username: ", SwingConstants.LEFT);
		add(labelDbUser);
		_dbUser = new JTextField(15);
		_dbUser.setText(_prop.get("dbUser_" + db, "root"));
		labelDbUser.setLabelFor(_dbUser);
		add(_dbUser);
		
		// Password
		JLabel labelDbPass = new JLabel("Password: ", SwingConstants.LEFT);
		add(labelDbPass);
		_dbPass = new JPasswordField(15);
		_dbPass.setText(_prop.get("dbPass_" + db, ""));
		labelDbPass.setLabelFor(_dbPass);
		add(_dbPass);
		
		// Database
		JLabel labelDbDbse = new JLabel("Database: ", SwingConstants.LEFT);
		add(labelDbDbse);
		_dbDbse = new JTextField(15);
		_dbDbse.setText(_prop.get("dbDbse_" + db, db));
		labelDbDbse.setLabelFor(_dbDbse);
		add(_dbDbse);
		
		// Quit
		JButton btnCancel = new JButton("Quit");
		btnCancel.addActionListener(e -> close());
		add(btnCancel);
		
		ActionListener connectListener = e ->
		{
			Connection con = null;
			try
			{
				
				final JOptionPane optionPane = new JOptionPane(MessageFormat.format("Connecting to SQL server at {0}:{1} with user {2}...", _dbHost.getText(), _dbPort.getText(), _dbUser.getText()), JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {}, null);
				con = SwingUtil.runBackgroundTaskWithDialog(this, "Database Installer", optionPane, () -> SQLUtil.connect(_dbHost.getText(), _dbPort.getText(), _dbUser.getText(), new String(_dbPass.getPassword()), _dbDbse.getText()));
			}
			catch (Throwable t)
			{
				reportError(true, t, "Failed to establish connection with user {0} to SQL server at {1}:{2}!", _dbUser.getText(), _dbHost.getText(), _dbPort.getText());
				return;
			}
			
			try
			{
				final Connection con2 = con;
				final JOptionPane optionPane = new JOptionPane(MessageFormat.format("Ensuring that database {0} exists and is in use...", _dbDbse.getText()), JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {}, null);
				SwingUtil.runBackgroundTaskWithDialog(this, "Database Installer", optionPane, () ->
				{
					SQLUtil.ensureDatabaseUsage(con2, _dbDbse.getText());
					return null;
				});
			}
			catch (Throwable t)
			{
				SQLUtil.close(con);
				reportError(true, t, MessageFormat.format("Failed to ensure that the database {0} exists and is in use!", _dbDbse.getText()));
				return;
			}
			
			_prop.put("dbHost_" + _db, _dbHost.getText());
			_prop.put("dbPort_" + _db, _dbPort.getText());
			_prop.put("dbUser_" + _db, _dbUser.getText());
			_prop.put("dbDbse_" + _db, _dbDbse.getText());
			
			try
			{
				boolean cleanInstall = false;
				
				Object[] options =
				{
					"Full Install",
					"Upgrade",
					"Abbort"
				};
				int n = JOptionPane.showOptionDialog(null, "Select the installation type for your database:", "Database Installer", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
				if ((n == 2) || (n == -1))
				{
					SQLUtil.close(con);
					return;
				}
				
				if (n == 0)
				{
					if (!requestUserConfirm("A clean install will delete your current database! Do you want to continue?"))
					{
						SQLUtil.close(con);
						return;
					}
					
					cleanInstall = true;
				}
				
				DBInstallerGUI dbi = new DBInstallerGUI();
				RunTasks task = new RunTasks(dbi, con, _db, _dir, _cleanUp, cleanInstall);
				task.setPriority(Thread.MAX_PRIORITY);
				task.start();
			}
			catch (Throwable t)
			{
				SQLUtil.close(con);
				reportError(true, t, "Failed to launch database installation!");
			}
		};
		
		// Connect
		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(connectListener);
		add(btnConnect);
		
		SpringUtilities.makeCompactGrid(getContentPane(), 6, 2, 5, 5, 5, 5);
		
		setVisible(true);
	}
}