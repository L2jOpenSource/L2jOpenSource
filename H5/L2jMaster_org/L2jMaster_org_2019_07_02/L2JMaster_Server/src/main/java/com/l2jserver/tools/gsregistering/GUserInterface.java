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
package com.l2jserver.tools.gsregistering;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.l2jserver.Config;
import com.l2jserver.loginserver.GameServerTable;
import com.l2jserver.tools.images.ImagesTable;

/**
 * @author KenM
 */
public class GUserInterface extends BaseGameServerRegister implements ActionListener
{
	private final JFrame _frame;
	private final JTableModel _dtm;
	protected final JProgressBar _progressBar;
	
	public JTable _gsTable;
	
	public GUserInterface(ResourceBundle bundle)
	{
		super(bundle);
		
		_frame = new JFrame();
		getFrame().setTitle(getBundle().getString("toolName"));
		getFrame().setSize(600, 400);
		getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getFrame().setLayout(new GridBagLayout());
		GridBagConstraints cons = new GridBagConstraints();
		
		JFrame.setDefaultLookAndFeelDecorated(true);
		getFrame().setIconImage(ImagesTable.getImage("l2j.png").getImage());
		
		JMenuBar menubar = new JMenuBar();
		getFrame().setJMenuBar(menubar);
		
		JMenu fileMenu = new JMenu(getBundle().getString("fileMenu"));
		
		JMenuItem exitItem = new JMenuItem(getBundle().getString("exitItem"));
		exitItem.addActionListener(this);
		exitItem.setActionCommand("exit");
		
		fileMenu.add(exitItem);
		
		JMenu helpMenu = new JMenu(getBundle().getString("helpMenu"));
		
		JMenuItem aboutItem = new JMenuItem(getBundle().getString("aboutItem"));
		aboutItem.addActionListener(this);
		aboutItem.setActionCommand("about");
		
		helpMenu.add(aboutItem);
		
		menubar.add(fileMenu);
		menubar.add(helpMenu);
		
		JButton btnRegister = new JButton(getBundle().getString("btnRegister"), ImagesTable.getImage("add.png"));
		btnRegister.addActionListener(this);
		btnRegister.setActionCommand("register");
		getFrame().add(btnRegister, cons);
		
		cons.gridx = 1;
		cons.anchor = GridBagConstraints.LINE_END;
		JButton btnRemoveAll = new JButton(getBundle().getString("btnRemoveAll"), ImagesTable.getImage("cross.png"));
		btnRemoveAll.addActionListener(this);
		btnRemoveAll.setActionCommand("removeAll");
		getFrame().add(btnRemoveAll, cons);
		
		String name = getBundle().getString("gsName");
		String action = getBundle().getString("gsAction");
		
		_dtm = new JTableModel(new Object[]
		{
			"ID",
			name,
			action
		});
		_gsTable = new JTable(_dtm);
		_gsTable.addMouseListener(new JTableButtonMouseListener(_gsTable));
		
		_gsTable.getColumnModel().getColumn(0).setMaxWidth(30);
		
		TableColumn actionCollumn = _gsTable.getColumnModel().getColumn(2);
		actionCollumn.setCellRenderer(new ButtonCellRenderer());
		
		cons.fill = GridBagConstraints.BOTH;
		cons.gridx = 0;
		cons.gridy = 1;
		cons.weighty = 1.0;
		cons.weightx = 1.0;
		cons.gridwidth = 2;
		JLayeredPane layer = new JLayeredPane();
		layer.setLayout(new BoxLayout(layer, BoxLayout.PAGE_AXIS));
		layer.add(new JScrollPane(_gsTable), 0);
		_progressBar = new JProgressBar();
		_progressBar.setIndeterminate(true);
		_progressBar.setVisible(false);
		layer.add(_progressBar, BorderLayout.CENTER, 1);
		// layer.setV
		getFrame().add(layer, cons);
		
		refreshAsync();
	}
	
	public void refreshAsync()
	{
		Thread t = new Thread(() -> GUserInterface.this.refreshServers(), "LoaderThread");
		t.start();
	}
	
	@Override
	public void load()
	{
		SwingUtilities.invokeLater(() -> _progressBar.setVisible(true));
		
		super.load();
		
		SwingUtilities.invokeLater(() -> _progressBar.setVisible(false));
	}
	
	@Override
	public void showError(String msg, Throwable t)
	{
		String title;
		if (getBundle() != null)
		{
			title = getBundle().getString("error");
			msg += Config.EOL + getBundle().getString("reason") + ' ' + t.getLocalizedMessage();
		}
		else
		{
			title = "Error";
			msg += Config.EOL + "Cause: " + t.getLocalizedMessage();
		}
		JOptionPane.showMessageDialog(getFrame(), msg, title, JOptionPane.ERROR_MESSAGE);
	}
	
	protected void refreshServers()
	{
		if (!isLoaded())
		{
			load();
		}
		
		// load succeeded?
		if (isLoaded())
		{
			SwingUtilities.invokeLater(() ->
			{
				int size = GameServerTable.getInstance().getServerNames().size();
				if (size == 0)
				{
					String title = getBundle().getString("error");
					String msg = getBundle().getString("noServerNames");
					JOptionPane.showMessageDialog(getFrame(), msg, title, JOptionPane.ERROR_MESSAGE);
					System.exit(1);
				}
				// reset
				_dtm.setRowCount(0);
				
				for (final int id : GameServerTable.getInstance().getRegisteredGameServers().keySet())
				{
					String name = GameServerTable.getInstance().getServerNameById(id);
					JButton button = new JButton(getBundle().getString("btnRemove"), ImagesTable.getImage("cross.png"));
					button.addActionListener(e ->
					{
						String sid = String.valueOf(id);
						String sname = GameServerTable.getInstance().getServerNameById(id);
						
						int choice = JOptionPane.showConfirmDialog(getFrame(), getBundle().getString("confirmRemoveText").replace("%d", sid).replace("%s", sname), getBundle().getString("confirmRemoveTitle"), JOptionPane.YES_NO_OPTION);
						if (choice == JOptionPane.YES_OPTION)
						{
							try
							{
								BaseGameServerRegister.unregisterGameServer(id);
								GUserInterface.this.refreshAsync();
							}
							catch (SQLException e1)
							{
								GUserInterface.this.showError(getBundle().getString("errorUnregister"), e1);
							}
						}
					});
					_dtm.addRow(new Object[]
					{
						id,
						name,
						button
					});
				}
			});
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();
		
		if (cmd.equals("register"))
		{
			RegisterDialog rd = new RegisterDialog(this);
			rd.setVisible(true);
		}
		else if (cmd.equals("exit"))
		{
			System.exit(0);
		}
		else if (cmd.equals("about"))
		{
			JOptionPane.showMessageDialog(getFrame(), getBundle().getString("credits") + Config.EOL + "http://www.l2jserver.com" + Config.EOL + Config.EOL + getBundle().getString("icons") + Config.EOL + Config.EOL + getBundle().getString("langText") + Config.EOL + getBundle().getString("translation"), getBundle().getString("aboutItem"), JOptionPane.INFORMATION_MESSAGE, ImagesTable.getImage("l2jserverlogo.png"));
		}
		else if (cmd.equals("removeAll"))
		{
			int choice = JOptionPane.showConfirmDialog(getFrame(), getBundle().getString("confirmRemoveAllText"), getBundle().getString("confirmRemoveTitle"), JOptionPane.YES_NO_OPTION);
			if (choice == JOptionPane.YES_OPTION)
			{
				try
				{
					BaseGameServerRegister.unregisterAllGameServers();
					refreshAsync();
				}
				catch (SQLException e1)
				{
					GUserInterface.this.showError(getBundle().getString("errorUnregister"), e1);
				}
			}
		}
	}
	
	/**
	 * @return Returns the frame.
	 */
	public JFrame getFrame()
	{
		return _frame;
	}
	
	protected class ButtonCellRenderer implements TableCellRenderer
	{
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			return (Component) value;
		}
	}
	
	/**
	 * Forward mouse-events from table to buttons inside.<br>
	 * Buttons animate properly.
	 * @author KenM
	 */
	private class JTableButtonMouseListener implements MouseListener
	{
		private final JTable _table;
		
		public JTableButtonMouseListener(JTable table)
		{
			_table = table;
		}
		
		private void forwardEvent(MouseEvent e)
		{
			TableColumnModel columnModel = _table.getColumnModel();
			int column = columnModel.getColumnIndexAtX(e.getX());
			int row = e.getY() / _table.getRowHeight();
			Object value;
			
			if ((row >= _table.getRowCount()) || (row < 0) || (column >= _table.getColumnCount()) || (column < 0))
			{
				return;
			}
			
			value = _table.getValueAt(row, column);
			
			if (value instanceof JButton)
			{
				final JButton b = (JButton) value;
				if (e.getID() == MouseEvent.MOUSE_PRESSED)
				{
					b.getModel().setPressed(true);
					b.getModel().setArmed(true);
					_table.repaint();
				}
				else if (e.getID() == MouseEvent.MOUSE_RELEASED)
				{
					b.doClick();
				}
			}
		}
		
		@Override
		public void mouseEntered(MouseEvent e)
		{
			forwardEvent(e);
		}
		
		@Override
		public void mouseExited(MouseEvent e)
		{
			forwardEvent(e);
		}
		
		@Override
		public void mousePressed(MouseEvent e)
		{
			forwardEvent(e);
		}
		
		@Override
		public void mouseClicked(MouseEvent e)
		{
			forwardEvent(e);
		}
		
		@Override
		public void mouseReleased(MouseEvent e)
		{
			forwardEvent(e);
		}
	}
	
	private class JTableModel extends DefaultTableModel
	{
		private static final long serialVersionUID = -5907903982876753479L;
		
		public JTableModel(Object[] columnNames)
		{
			super(columnNames, 0);
		}
		
		@Override
		public boolean isCellEditable(int row, int column)
		{
			return false;
		}
		
		@Override
		public Class<?> getColumnClass(int column)
		{
			return getValueAt(0, column).getClass();
		}
	}
}
