package com.l2jfrozen.gameserver.network.loginserverpackets;

/**
 * @author -Wooden-
 */
public class PlayerAuthResponse extends LoginServerBasePacket
{
	
	private final String account;
	private final boolean authed;
	
	/**
	 * @param decrypt
	 */
	public PlayerAuthResponse(final byte[] decrypt)
	{
		super(decrypt);
		
		account = readS();
		authed = readC() == 0 ? false : true;
	}
	
	/**
	 * @return Returns the account.
	 */
	public String getAccount()
	{
		return account;
	}
	
	/**
	 * @return Returns the authed state.
	 */
	public boolean isAuthed()
	{
		return authed;
	}
	
}
