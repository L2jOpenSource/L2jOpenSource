package com.l2jfrozen.loginserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;

/**
 * @author -Wooden-
 */
public abstract class FloodProtectedListener extends Thread
{
	private final Logger LOGGER = Logger.getLogger(FloodProtectedListener.class);
	private final Map<String, ForeignConnection> floodProtection = new HashMap<>();
	private final String listenIp;
	private final int port;
	private ServerSocket serverSocket;
	
	public FloodProtectedListener(final String listenIp, final int port) throws IOException
	{
		this.port = port;
		this.listenIp = listenIp;
		
		if (this.listenIp.equals("*"))
		{
			serverSocket = new ServerSocket(this.port);
		}
		else
		{
			serverSocket = new ServerSocket(this.port, 50, InetAddress.getByName(this.listenIp));
		}
	}
	
	@Override
	public void run()
	{
		Socket connection = null;
		
		while (true)
		{
			try
			{
				connection = serverSocket.accept();
				if (Config.FLOOD_PROTECTION)
				{
					ForeignConnection fConnection = floodProtection.get(connection.getInetAddress().getHostAddress());
					
					if (fConnection != null)
					{
						fConnection.connectionNumber += 1;
						if (fConnection.connectionNumber > Config.FAST_CONNECTION_LIMIT && System.currentTimeMillis() - fConnection.lastConnection < Config.NORMAL_CONNECTION_TIME || System.currentTimeMillis() - fConnection.lastConnection < Config.FAST_CONNECTION_TIME || fConnection.connectionNumber > Config.MAX_CONNECTION_PER_IP)
						{
							fConnection.lastConnection = System.currentTimeMillis();
							connection.close();
							
							fConnection.connectionNumber -= 1;
							
							if (!fConnection.isFlooding)
							{
								LOGGER.warn("Potential Flood from " + connection.getInetAddress().getHostAddress());
							}
							
							fConnection.isFlooding = true;
							continue;
						}
						
						if (fConnection.isFlooding) // if connection was flooding server but now passed the check
						{
							fConnection.isFlooding = false;
							LOGGER.info(connection.getInetAddress().getHostAddress() + " is not considered as flooding anymore.");
						}
						
						fConnection.lastConnection = System.currentTimeMillis();
						fConnection = null;
					}
					else
					{
						fConnection = new ForeignConnection(System.currentTimeMillis());
						floodProtection.put(connection.getInetAddress().getHostAddress(), fConnection);
						fConnection = null;
					}
				}
				addClient(connection);
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				try
				{
					if (connection != null)
					{
						connection.close();
					}
				}
				catch (final Exception e2)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e2.printStackTrace();
					}
					
				}
				if (isInterrupted())
				{
					// shutdown?
					try
					{
						serverSocket.close();
					}
					catch (final IOException io)
					{
						LOGGER.error("fixme:unhandled exception", io);
					}
					break;
				}
			}
		}
	}
	
	protected static class ForeignConnection
	{
		public int connectionNumber;
		public long lastConnection;
		public boolean isFlooding = false;
		
		/**
		 * @param time
		 */
		public ForeignConnection(final long time)
		{
			lastConnection = time;
			connectionNumber = 1;
		}
	}
	
	public abstract void addClient(Socket s);
	
	public void removeFloodProtection(final String ip)
	{
		if (!Config.FLOOD_PROTECTION)
		{
			return;
		}
		
		ForeignConnection fConnection = floodProtection.get(ip);
		
		if (fConnection != null)
		{
			fConnection.connectionNumber -= 1;
			
			if (fConnection.connectionNumber == 0)
			{
				floodProtection.remove(ip);
			}
		}
		else
		{
			LOGGER.warn("Removing a flood protection for a GameServer that was not in the connection map??? :" + ip);
		}
		
		fConnection = null;
	}
	
	public void close()
	{
		try
		{
			serverSocket.close();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}
}
