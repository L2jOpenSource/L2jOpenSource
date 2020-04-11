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
package l2r.loginserver;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import javolution.util.FastList;
import l2r.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author KenM
 */
public class GameServerListener extends FloodProtectedListener
{
	private static Logger _log = LoggerFactory.getLogger(GameServerListener.class);
	private static List<GameServerThread> _gameServers = new FastList<>();
	
	public GameServerListener() throws IOException
	{
		super(Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT);
		setName(getClass().getSimpleName());
	}
	
	@Override
	public void addClient(Socket s)
	{
		if (Config.DEBUG)
		{
			_log.info("Received gameserver connection from: " + s.getInetAddress().getHostAddress());
		}
		GameServerThread gst = new GameServerThread(s);
		_gameServers.add(gst);
	}
	
	public void removeGameServer(GameServerThread gst)
	{
		_gameServers.remove(gst);
	}
}
