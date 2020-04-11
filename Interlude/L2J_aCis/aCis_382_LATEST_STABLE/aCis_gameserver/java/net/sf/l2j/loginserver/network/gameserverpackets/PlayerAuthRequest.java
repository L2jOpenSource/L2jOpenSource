package net.sf.l2j.loginserver.network.gameserverpackets;

import net.sf.l2j.loginserver.network.SessionKey;
import net.sf.l2j.loginserver.network.clientpackets.ClientBasePacket;

public class PlayerAuthRequest extends ClientBasePacket
{
	private final String _account;
	private final SessionKey _sessionKey;
	
	public PlayerAuthRequest(byte[] decrypt)
	{
		super(decrypt);
		
		_account = readS();
		
		int playKey1 = readD();
		int playKey2 = readD();
		int loginKey1 = readD();
		int loginKey2 = readD();
		
		_sessionKey = new SessionKey(loginKey1, loginKey2, playKey1, playKey2);
	}
	
	public String getAccount()
	{
		return _account;
	}
	
	public SessionKey getKey()
	{
		return _sessionKey;
	}
}