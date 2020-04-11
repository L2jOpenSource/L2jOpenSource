package com.l2jfrozen.loginserver.network.gameserverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.loginserver.network.clientpackets.ClientBasePacket;

/**
 * @author -Wooden-
 */
public class PlayerInGame extends ClientBasePacket
{
	private final List<String> accounts;
	
	/**
	 * @param decrypt
	 */
	public PlayerInGame(final byte[] decrypt)
	{
		super(decrypt);
		
		accounts = new ArrayList<>();
		
		final int size = readH();
		
		for (int i = 0; i < size; i++)
		{
			accounts.add(readS());
		}
	}
	
	/**
	 * @return Returns the accounts.
	 */
	public List<String> getAccounts()
	{
		return accounts;
	}
	
}
