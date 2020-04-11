package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.managers.TownManager;
import com.l2jfrozen.gameserver.model.PartyMatchRoom;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Gnacik
 */
public class ExPartyRoomMember extends L2GameServerPacket
{
	private final PartyMatchRoom room;
	private final int mode;
	
	public ExPartyRoomMember(final L2PcInstance player, final PartyMatchRoom room, final int mode)
	{
		this.room = room;
		this.mode = mode;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x0e);
		writeD(mode);
		writeD(room.getMembers());
		for (final L2PcInstance member : room.getPartyMembers())
		{
			writeD(member.getObjectId());
			writeS(member.getName());
			writeD(member.getActiveClass());
			writeD(member.getLevel());
			writeD(TownManager.getClosestLocation(member));
			if (room.getOwner().equals(member))
			{
				writeD(1);
			}
			else
			{
				if ((room.getOwner().isInParty() && member.isInParty()) && (room.getOwner().getParty().getPartyLeaderOID() == member.getParty().getPartyLeaderOID()))
				{
					writeD(2);
				}
				else
				{
					writeD(0);
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:0E ExPartyRoomMember";
	}
}