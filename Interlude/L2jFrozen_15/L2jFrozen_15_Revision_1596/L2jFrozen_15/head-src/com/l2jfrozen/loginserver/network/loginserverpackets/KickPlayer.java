package com.l2jfrozen.loginserver.network.loginserverpackets;

import com.l2jfrozen.loginserver.network.serverpackets.ServerBasePacket;

/**
 * @author -Wooden-
 */
public class KickPlayer extends ServerBasePacket
{
	public KickPlayer(final String account)
	{
		writeC(0x04);
		writeS(account);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
