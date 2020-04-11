package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.manager.PartyMatchRoomManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.group.PartyMatchRoom;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExPartyRoomMember;
import net.sf.l2j.gameserver.network.serverpackets.PartyMatchDetail;

public class RequestManagePartyRoom extends L2GameClientPacket
{
	private int _roomid;
	private int _membersmax;
	private int _lvlmin;
	private int _lvlmax;
	private int _loot;
	private String _roomtitle;
	
	@Override
	protected void readImpl()
	{
		_roomid = readD();
		_membersmax = readD();
		_lvlmin = readD();
		_lvlmax = readD();
		_loot = readD();
		_roomtitle = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		if (_roomid > 0)
		{
			final PartyMatchRoom room = PartyMatchRoomManager.getInstance().getRoom(_roomid);
			if (room != null)
			{
				room.setMaxMembers(_membersmax);
				room.setMinLvl(_lvlmin);
				room.setMaxLvl(_lvlmax);
				room.setLootType(_loot);
				room.setTitle(_roomtitle);
				
				for (Player member : room.getMembers())
				{
					member.sendPacket(new PartyMatchDetail(room));
					member.sendPacket(SystemMessageId.PARTY_ROOM_REVISED);
				}
			}
		}
		else
		{
			final int newId = PartyMatchRoomManager.getInstance().getNewRoomId();
			final PartyMatchRoom room = new PartyMatchRoom(newId, _roomtitle, _loot, _lvlmin, _lvlmax, _membersmax, player);
			
			// Remove Player from waiting list, and add newly created PartyMatchRoom.
			PartyMatchRoomManager.getInstance().removeWaitingPlayer(player);
			PartyMatchRoomManager.getInstance().addRoom(newId, room);
			
			final Party party = player.getParty();
			if (party != null)
			{
				for (Player member : party.getMembers())
				{
					if (member == player)
						continue;
					
					room.addMember(member, newId);
				}
			}
			
			player.sendPacket(new PartyMatchDetail(room));
			player.sendPacket(new ExPartyRoomMember(room, 1));
			
			player.sendPacket(SystemMessageId.PARTY_ROOM_CREATED);
			
			player.setPartyRoom(newId);
			player.broadcastUserInfo();
		}
	}
}