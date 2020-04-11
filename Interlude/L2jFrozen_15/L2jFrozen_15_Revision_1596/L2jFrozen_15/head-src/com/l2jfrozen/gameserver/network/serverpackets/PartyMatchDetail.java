package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.PartyMatchRoom;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Gnacik
 */
public class PartyMatchDetail extends L2GameServerPacket
{
	private final PartyMatchRoom room;
	
	public PartyMatchDetail(final L2PcInstance player, final PartyMatchRoom room)
	{
		this.room = room;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x97);
		writeD(room.getId()); // Room ID
		writeD(room.getMaxMembers()); // Max Members
		writeD(room.getMinLvl()); // Level Min
		writeD(room.getMaxLvl()); // Level Max
		writeD(room.getLootType()); // Loot Type
		writeD(room.getLocation()); // Room Location
		writeS(room.getTitle()); // Room title
	}
	
	@Override
	public String getType()
	{
		return "[S] 97 PartyMatchDetail";
	}
}