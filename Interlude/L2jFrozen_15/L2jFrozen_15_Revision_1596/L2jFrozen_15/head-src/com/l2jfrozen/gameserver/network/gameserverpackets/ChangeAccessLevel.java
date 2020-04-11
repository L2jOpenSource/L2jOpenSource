package com.l2jfrozen.gameserver.network.gameserverpackets;

/**
 * @author -Wooden-
 */
public class ChangeAccessLevel extends GameServerBasePacket
{
	public ChangeAccessLevel(final String player, final int access)
	{
		writeC(0x04);
		writeD(access);
		writeS(player);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
