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
package l2r.gameserver.geoeditorcon;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dezmond
 */
public class GeoEditorListener extends Thread
{
	private static GeoEditorListener _instance;
	private static final int PORT = 9011;
	private static Logger _log = LoggerFactory.getLogger(GeoEditorListener.class);
	private final ServerSocket _serverSocket;
	private GeoEditorThread _geoEditor;
	
	public static GeoEditorListener getInstance()
	{
		synchronized (GeoEditorListener.class)
		{
			if (_instance == null)
			{
				try
				{
					_instance = new GeoEditorListener();
					_instance.start();
					_log.info("GeoEditorListener Initialized.");
				}
				catch (IOException e)
				{
					_log.error("Error creating geoeditor listener! " + e.getMessage(), e);
					System.exit(1);
				}
			}
		}
		return _instance;
	}
	
	private GeoEditorListener() throws IOException
	{
		_serverSocket = new ServerSocket(PORT);
	}
	
	public GeoEditorThread getThread()
	{
		return _geoEditor;
	}
	
	public String getStatus()
	{
		if ((_geoEditor != null) && _geoEditor.isWorking())
		{
			return "Geoeditor connected.";
		}
		return "Geoeditor not connected.";
	}
	
	@Override
	public void run()
	{
		try (Socket connection = _serverSocket.accept())
		{
			while (!isInterrupted())
			{
				if ((_geoEditor != null) && _geoEditor.isWorking())
				{
					_log.warn("Geoeditor already connected!");
					connection.close();
					continue;
				}
				_log.info("Received geoeditor connection from: " + connection.getInetAddress().getHostAddress());
				_geoEditor = new GeoEditorThread(connection);
				_geoEditor.start();
			}
		}
		catch (Exception e)
		{
			_log.warn("GeoEditorListener: " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				_serverSocket.close();
			}
			catch (Exception e)
			{
				_log.warn("GeoEditorListener: " + e.getMessage(), e);
			}
			_log.warn("GeoEditorListener Closed!");
		}
	}
}
