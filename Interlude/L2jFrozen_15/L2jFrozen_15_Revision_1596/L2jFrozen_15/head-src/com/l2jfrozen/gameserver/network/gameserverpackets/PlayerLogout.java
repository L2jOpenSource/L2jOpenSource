package com.l2jfrozen.gameserver.network.gameserverpackets;

/**
 * @author -Wooden-
 */
public class PlayerLogout extends GameServerBasePacket
{
	public PlayerLogout(final String player)
	{
		writeC(0x03);
		writeS(player);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
