package com.l2jfrozen.gameserver.network.loginserverpackets;

public class InitLS extends LoginServerBasePacket
{
	private final int rev;
	private final byte[] key;
	
	public int getRevision()
	{
		return rev;
	}
	
	public byte[] getRSAKey()
	{
		return key;
	}
	
	/**
	 * @param decrypt
	 */
	public InitLS(final byte[] decrypt)
	{
		super(decrypt);
		rev = readD();
		final int size = readD();
		key = readB(size);
	}
	
}
