/*
 * Copyright (C) L2J Sunrise
 * This file is part of L2J Sunrise.
 */
package l2r;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author UnAfraid
 */
public class UPnPService
{
	private static final Logger _log = LoggerFactory.getLogger(UPnPService.class);
	private static final String PROTOCOL = "TCP";
	
	private final GatewayDiscover _gatewayDiscover = new GatewayDiscover();
	private GatewayDevice _activeGW;
	
	protected UPnPService()
	{
		try
		{
			load();
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": error while initializing: ", e);
		}
	}
	
	private void load() throws Exception
	{
		if (!Config.ENABLE_UPNP)
		{
			_log.warn("UPnP Service is disabled.");
			return;
		}
		
		_log.info("Looking for UPnP Gateway Devices...");
		
		final Map<InetAddress, GatewayDevice> gateways = _gatewayDiscover.discover();
		if (gateways.isEmpty())
		{
			_log.info("No UPnP gateways found");
			return;
		}
		
		// choose the first active gateway for the tests
		_activeGW = _gatewayDiscover.getValidGateway();
		if (_activeGW != null)
		{
			_log.info("Using UPnP gateway: " + _activeGW.getFriendlyName());
		}
		else
		{
			_log.info("No active UPnP gateway found");
			return;
		}
		
		_log.info("Using local address: " + _activeGW.getLocalAddress().getHostAddress() + " External address: " + _activeGW.getExternalIPAddress());
		
		if (Server.serverMode == Server.MODE_GAMESERVER)
		{
			addPortMapping(Config.PORT_GAME, "L2j Game Server");
		}
		else if (Server.serverMode == Server.MODE_LOGINSERVER)
		{
			addPortMapping(Config.PORT_LOGIN, "L2j Login Server");
		}
	}
	
	public void removeAllPorts() throws Exception
	{
		if (_activeGW != null)
		{
			if (Server.serverMode == Server.MODE_GAMESERVER)
			{
				deletePortMapping(Config.PORT_GAME);
			}
			else if (Server.serverMode == Server.MODE_LOGINSERVER)
			{
				deletePortMapping(Config.PORT_LOGIN);
			}
		}
	}
	
	private void addPortMapping(int port, String description) throws IOException, SAXException
	{
		final PortMappingEntry portMapping = new PortMappingEntry();
		final InetAddress localAddress = _activeGW.getLocalAddress();
		
		// Attempt to re-map
		if (_activeGW.getSpecificPortMappingEntry(port, PROTOCOL, portMapping))
		{
			_activeGW.deletePortMapping(port, PROTOCOL);
		}
		
		if (_activeGW.addPortMapping(port, port, localAddress.getHostAddress(), PROTOCOL, description))
		{
			_log.info("Mapping successfull on [" + localAddress.getHostAddress() + ":" + port + "]");
		}
		else
		{
			_log.info("Mapping failed on [" + localAddress.getHostAddress() + ":" + port + "] - Already mapped?");
		}
	}
	
	private void deletePortMapping(int port) throws IOException, SAXException
	{
		if (_activeGW.deletePortMapping(port, PROTOCOL))
		{
			_log.info("Mapping was deleted from [" + _activeGW.getLocalAddress().getHostAddress() + ":" + port + "]");
		}
	}
	
	public static UPnPService getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final UPnPService _instance = new UPnPService();
	}
}
