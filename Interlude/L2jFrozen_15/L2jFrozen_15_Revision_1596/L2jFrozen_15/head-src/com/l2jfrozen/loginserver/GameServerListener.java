package com.l2jfrozen.loginserver;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;

/**
 * @author KenM
 */
public class GameServerListener extends FloodProtectedListener
{
	private static Logger LOGGER = Logger.getLogger(GameServerListener.class);
	private static List<GameServerThread> gameServers = new ArrayList<>();
	
	public GameServerListener() throws IOException
	{
		super(Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT);
	}
	
	/**
	 * @see com.l2jfrozen.loginserver.FloodProtectedListener#addClient(java.net.Socket)
	 */
	@Override
	public void addClient(final Socket s)
	{
		if (Config.DEBUG)
		{
			LOGGER.info("Received gameserver connection from: " + s.getInetAddress().getHostAddress());
		}
		
		final GameServerThread gst = new GameServerThread(s);
		gst.start();
		gameServers.add(gst);
		
	}
	
	public void removeGameServer(final GameServerThread gst)
	{
		gameServers.remove(gst);
	}
}
