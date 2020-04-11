package com.l2jfrozen.loginserver.network.gameserverpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.loginserver.network.clientpackets.ClientBasePacket;

/**
 * Format: cccddb c desired ID c accept alternative ID c reserve Host s ExternalHostName s InetranlHostName d max players d hexid size b hexid
 * @author -Wooden-
 */
public class GameServerAuth extends ClientBasePacket
{
	protected static Logger LOGGER = Logger.getLogger(GameServerAuth.class);
	private final byte[] hexId;
	private final int desiredId;
	private final boolean hostReserved;
	private final boolean acceptAlternativeId;
	private final int maxPlayers;
	private final int port;
	private final String externalHost;
	private final String internalHost;
	
	/**
	 * @param decrypt
	 */
	public GameServerAuth(final byte[] decrypt)
	{
		super(decrypt);
		
		desiredId = readC();
		acceptAlternativeId = readC() == 0 ? false : true;
		hostReserved = readC() == 0 ? false : true;
		externalHost = readS();
		internalHost = readS();
		port = readH();
		maxPlayers = readD();
		
		final int size = readD();
		
		hexId = readB(size);
	}
	
	/**
	 * @return
	 */
	public byte[] getHexID()
	{
		return hexId;
	}
	
	public boolean getHostReserved()
	{
		return hostReserved;
	}
	
	public int getDesiredID()
	{
		return desiredId;
	}
	
	public boolean acceptAlternateID()
	{
		return acceptAlternativeId;
	}
	
	/**
	 * @return Returns the max players.
	 */
	public int getMaxPlayers()
	{
		return maxPlayers;
	}
	
	/**
	 * @return Returns the externalHost.
	 */
	public String getExternalHost()
	{
		return externalHost;
	}
	
	/**
	 * @return Returns the internalHost.
	 */
	public String getInternalHost()
	{
		return internalHost;
	}
	
	/**
	 * @return Returns the port.
	 */
	public int getPort()
	{
		return port;
	}
}
