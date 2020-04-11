package com.l2jfrozen.loginserver.network.gameserverpackets;

import com.l2jfrozen.loginserver.network.clientpackets.ClientBasePacket;

/**
 * @author -Wooden-
 */
public class ChangeAccessLevel extends ClientBasePacket
{
	
	private final int level;
	private final String account;
	
	/**
	 * @param decrypt
	 */
	public ChangeAccessLevel(final byte[] decrypt)
	{
		super(decrypt);
		level = readD();
		account = readS();
	}
	
	/**
	 * @return Returns the account.
	 */
	public String getAccount()
	{
		return account;
	}
	
	/**
	 * @return Returns the level.
	 */
	public int getLevel()
	{
		return level;
	}
	
}
