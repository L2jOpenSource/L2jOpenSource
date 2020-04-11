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
package l2r.loginserver.network.gameserverpackets;

import l2r.loginserver.GameServerThread;
import l2r.loginserver.LoginController;
import l2r.util.network.BaseRecievePacket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author -Wooden-
 */
public class ChangeAccessLevel extends BaseRecievePacket
{
	protected static Logger _log = LoggerFactory.getLogger(ChangeAccessLevel.class);
	
	/**
	 * @param decrypt
	 * @param server
	 */
	public ChangeAccessLevel(byte[] decrypt, GameServerThread server)
	{
		super(decrypt);
		int level = readD();
		String account = readS();
		
		LoginController.getInstance().setAccountAccessLevel(account, level);
		_log.info("Changed " + account + " access level to " + level);
	}
}
