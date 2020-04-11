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
package com.l2jserver.gameserver.handler;

import java.util.logging.Logger;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public interface IUserCommandHandler
{
	public static Logger _log = Logger.getLogger(IUserCommandHandler.class.getName());
	
	/**
	 * this is the worker method that is called when someone uses an admin command.
	 * @param id
	 * @param activeChar
	 * @return command success
	 */
	public boolean useUserCommand(int id, L2PcInstance activeChar);
	
	/**
	 * this method is called at initialization to register all the item ids automatically
	 * @return all known itemIds
	 */
	public int[] getUserCommandList();
}
