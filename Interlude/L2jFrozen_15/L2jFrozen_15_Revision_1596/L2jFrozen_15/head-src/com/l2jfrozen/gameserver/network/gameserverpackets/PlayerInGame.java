package com.l2jfrozen.gameserver.network.gameserverpackets;

import java.util.ArrayList;

/**
 * @author -Wooden-
 */
public class PlayerInGame extends GameServerBasePacket
{
	public PlayerInGame(final String player)
	{
		writeC(0x02);
		writeH(1);
		writeS(player);
	}
	
	public PlayerInGame(final ArrayList<String> players)
	{
		writeC(0x02);
		writeH(players.size());
		for (final String pc : players)
		{
			writeS(pc);
		}
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
	
}
