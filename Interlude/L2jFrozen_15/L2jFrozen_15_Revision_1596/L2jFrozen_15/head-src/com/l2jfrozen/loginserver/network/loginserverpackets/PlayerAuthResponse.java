package com.l2jfrozen.loginserver.network.loginserverpackets;

import com.l2jfrozen.loginserver.network.serverpackets.ServerBasePacket;

/**
 * @author -Wooden-
 */
public class PlayerAuthResponse extends ServerBasePacket
{
	public PlayerAuthResponse(final String account, final boolean response)
	{
		writeC(0x03);
		writeS(account);
		writeC(response ? 1 : 0);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
