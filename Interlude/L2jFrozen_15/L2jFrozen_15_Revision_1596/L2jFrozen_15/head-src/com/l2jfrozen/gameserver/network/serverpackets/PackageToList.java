package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.Map;

/**
 * Format: (c) d[dS] d: list size [ d: char ID S: char Name ]
 * @author -Wooden-
 */
public class PackageToList extends L2GameServerPacket
{
	private final Map<Integer, String> players;
	
	// Lecter : i put a char list here, but i'm unsure these really are Pc. I duno how freight work tho...
	public PackageToList(final Map<Integer, String> players)
	{
		this.players = players;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xC2);
		writeD(players.size());
		for (final int objId : players.keySet())
		{
			writeD(objId); // you told me char id, i guess this was object id?
			writeS(players.get(objId));
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] C2 PackageToList";
	}
}
