package com.l2jfrozen.gameserver.network.loginserverpackets;

/**
 * @author -Wooden-
 */
public class AuthResponse extends LoginServerBasePacket
{
	
	private final int serverId;
	private final String serverName;
	
	/**
	 * @param decrypt
	 */
	public AuthResponse(final byte[] decrypt)
	{
		super(decrypt);
		serverId = readC();
		serverName = readS();
	}
	
	/**
	 * @return Returns the serverId.
	 */
	public int getServerId()
	{
		return serverId;
	}
	
	/**
	 * @return Returns the serverName.
	 */
	public String getServerName()
	{
		return serverName;
	}
	
}
