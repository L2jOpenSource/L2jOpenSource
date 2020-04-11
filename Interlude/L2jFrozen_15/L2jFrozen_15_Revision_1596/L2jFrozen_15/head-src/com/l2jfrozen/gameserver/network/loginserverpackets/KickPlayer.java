package com.l2jfrozen.gameserver.network.loginserverpackets;

public class KickPlayer extends LoginServerBasePacket
{
	
	private final String account;
	
	/**
	 * @param decrypt
	 */
	public KickPlayer(final byte[] decrypt)
	{
		super(decrypt);
		account = readS();
	}
	
	/**
	 * @return Returns the account.
	 */
	public String getAccount()
	{
		return account;
	}
	
}
