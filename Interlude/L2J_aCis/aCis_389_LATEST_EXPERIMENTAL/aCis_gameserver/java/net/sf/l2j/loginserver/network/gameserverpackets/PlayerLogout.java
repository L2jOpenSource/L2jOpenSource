package net.sf.l2j.loginserver.network.gameserverpackets;

import net.sf.l2j.loginserver.network.clientpackets.ClientBasePacket;

public class PlayerLogout extends ClientBasePacket
{
	private final String _account;
	
	public PlayerLogout(byte[] decrypt)
	{
		super(decrypt);
		_account = readS();
	}
	
	public String getAccount()
	{
		return _account;
	}
}