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

/**
 * Macro Cmd data transfer object.
 * @author Zoey76
 */
public class MacroCmd
{
	private final int _entry;
	private final int _type;
	private final int _d1; // skill_id or page for shortcuts
	private final int _d2; // shortcut
	private final String _cmd;
	
	public MacroCmd(int entry, int type, int d1, int d2, String cmd)
	{
		_entry = entry;
		_type = type;
		_d1 = d1;
		_d2 = d2;
		_cmd = cmd;
	}
	
	public int getEntry()
	{
		return _entry;
	}
	
	public int getType()
	{
		return _type;
	}
	
	public int getD1()
	{
		return _d1;
	}
	
	public int getD2()
	{
		return _d2;
	}
	
	public String getCmd()
	{
		return _cmd;
	}
}
