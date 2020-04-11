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
package com.l2jserver.gameserver.model.announce;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author UnAfraid
 */
public enum AnnouncementType
{
	NORMAL,
	CRITICAL,
	EVENT,
	AUTO_NORMAL,
	AUTO_CRITICAL;
	private static final Logger _log = Logger.getLogger(AnnouncementType.class.getName());
	
	public static AnnouncementType findById(int id)
	{
		for (AnnouncementType type : values())
		{
			if (type.ordinal() == id)
			{
				return type;
			}
		}
		_log.log(Level.WARNING, AnnouncementType.class.getSimpleName() + ": Unexistent id specified: " + id + "!", new IllegalStateException());
		return NORMAL;
	}
	
	public static AnnouncementType findByName(String name)
	{
		for (AnnouncementType type : values())
		{
			if (type.name().equalsIgnoreCase(name))
			{
				return type;
			}
		}
		_log.log(Level.WARNING, AnnouncementType.class.getSimpleName() + ": Unexistent name specified: " + name + "!", new IllegalStateException());
		return NORMAL;
	}
}
