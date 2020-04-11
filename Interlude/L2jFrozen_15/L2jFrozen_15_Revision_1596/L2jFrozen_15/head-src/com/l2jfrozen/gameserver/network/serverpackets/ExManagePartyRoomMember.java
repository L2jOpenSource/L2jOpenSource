package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.managers.TownManager;
import com.l2jfrozen.gameserver.model.PartyMatchRoom;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Gnacik Mode : 0 - add 1 - modify 2 - quit
 */
public class ExManagePartyRoomMember extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	private final PartyMatchRoom room;
	private final int mode;
	
	public ExManagePartyRoomMember(final L2PcInstance player, final PartyMatchRoom room, final int mode)
	{
		activeChar = player;
		this.room = room;
		this.mode = mode;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x10);
		writeD(mode);
		writeD(activeChar.getObjectId());
		writeS(activeChar.getName());
		writeD(activeChar.getActiveClass());
		writeD(activeChar.getLevel());
		writeD(TownManager.getClosestLocation(activeChar));
		if (room.getOwner().equals(activeChar))
		{
			writeD(1);
		}
		else
		{
			if ((room.getOwner().isInParty() && activeChar.isInParty()) && (room.getOwner().getParty().getPartyLeaderOID() == activeChar.getParty().getPartyLeaderOID()))
			{
				writeD(2);
			}
			else
			{
				writeD(0);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:10 ExManagePartyRoomMember";
	}
}