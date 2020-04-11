package com.l2jfrozen.gameserver.network.gameserverpackets;

import com.l2jfrozen.gameserver.thread.LoginServerThread.SessionKey;

/**
 * @author -Wooden-
 */
public class PlayerAuthRequest extends GameServerBasePacket
{
	public PlayerAuthRequest(final String account, final SessionKey key)
	{
		writeC(0x05);
		writeS(account);
		writeD(key.playOkID1);
		writeD(key.playOkID2);
		writeD(key.loginOkID1);
		writeD(key.loginOkID2);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
	
}
