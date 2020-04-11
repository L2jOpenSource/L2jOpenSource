package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.manager.PartyMatchRoomManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.PartyMatchRoom;

public class PartyMatchList extends L2GameServerPacket
{
	private final PartyMatchRoom[] _rooms;
	
	public PartyMatchList(Player player)
	{
		_rooms = PartyMatchRoomManager.getInstance().getRooms();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x96);
		writeD((_rooms.length == 0) ? 0 : 1);
		writeD(_rooms.length);
		
		for (PartyMatchRoom room : _rooms)
		{
			writeD(room.getId());
			writeS(room.getTitle());
			writeD(room.getLocation());
			writeD(room.getMinLvl());
			writeD(room.getMaxLvl());
			writeD(room.getMembersCount());
			writeD(room.getMaxMembers());
			writeS(room.getLeader().getName());
		}
	}
}