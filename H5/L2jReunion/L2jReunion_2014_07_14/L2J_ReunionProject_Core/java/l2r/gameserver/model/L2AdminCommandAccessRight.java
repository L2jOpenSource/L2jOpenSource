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
package l2r.gameserver.model;

import l2r.gameserver.datatables.xml.AdminData;

/**
 * @author FBIagent
 */
public class L2AdminCommandAccessRight
{
	/** The admin command */
	private String _adminCommand = null;
	/** The access levels which can use the admin command. */
	private final int _accessLevel;
	private final boolean _requireConfirm;
	
	public L2AdminCommandAccessRight(StatsSet set)
	{
		_adminCommand = set.getString("command");
		_requireConfirm = set.getBool("confirmDlg", false);
		_accessLevel = set.getInteger("accessLevel", 7);
	}
	
	public L2AdminCommandAccessRight(String command, boolean confirm, int level)
	{
		_adminCommand = command;
		_requireConfirm = confirm;
		_accessLevel = level;
	}
	
	/**
	 * Returns the admin command the access right belongs to
	 * @return the admin command the access right belongs to
	 */
	public String getAdminCommand()
	{
		return _adminCommand;
	}
	
	/**
	 * Checks if the given characterAccessLevel is allowed to use the admin command which belongs to this access right.
	 * @param characterAccessLevel
	 * @return true if characterAccessLevel is allowed to use the admin command which belongs to this access right, otherwise false<br>
	 */
	public boolean hasAccess(L2AccessLevel characterAccessLevel)
	{
		L2AccessLevel accessLevel = AdminData.getInstance().getAccessLevel(_accessLevel);
		return ((accessLevel.getLevel() == characterAccessLevel.getLevel()) || characterAccessLevel.hasChildAccess(accessLevel));
	}
	
	public boolean getRequireConfirm()
	{
		return _requireConfirm;
	}
}