package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.PartyMatchRoom;
import com.l2jfrozen.gameserver.model.PartyMatchRoomList;
import com.l2jfrozen.gameserver.model.PartyMatchWaitingList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExPartyRoomMember;
import com.l2jfrozen.gameserver.network.serverpackets.PartyMatchDetail;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * author: Gnacik Packetformat Rev650 cdddddS
 */
public class RequestPartyMatchList extends L2GameClientPacket
{
	
	private static final Logger LOGGER = Logger.getLogger(RequestPartyMatchList.class);
	
	private int roomid;
	private int membersmax;
	private int lvlMin;
	private int lvlMax;
	private int loot;
	private String roomtitle;
	
	@Override
	protected void readImpl()
	{
		roomid = readD();
		membersmax = readD();
		lvlMin = readD();
		lvlMax = readD();
		loot = readD();
		roomtitle = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (roomid > 0)
		{
			final PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(roomid);
			if (room != null)
			{
				LOGGER.debug("PartyMatchRoom #" + room.getId() + " changed by " + activeChar.getName());
				room.setMaxMembers(membersmax);
				room.setMinLvl(lvlMin);
				room.setMaxLvl(lvlMax);
				room.setLootType(loot);
				room.setTitle(roomtitle);
				
				for (final L2PcInstance member : room.getPartyMembers())
				{
					if (member == null)
					{
						continue;
					}
					
					member.sendPacket(new PartyMatchDetail(activeChar, room));
					member.sendPacket(new SystemMessage(SystemMessageId.PARTY_ROOM_REVISED));
				}
			}
		}
		else
		{
			final int maxid = PartyMatchRoomList.getInstance().getMaxId();
			
			final PartyMatchRoom room = new PartyMatchRoom(maxid, roomtitle, loot, lvlMin, lvlMax, membersmax, activeChar);
			
			LOGGER.debug("PartyMatchRoom #" + maxid + " created by " + activeChar.getName());
			
			// Remove from waiting list, and add to current room
			PartyMatchWaitingList.getInstance().removePlayer(activeChar);
			PartyMatchRoomList.getInstance().addPartyMatchRoom(maxid, room);
			
			if (activeChar.isInParty())
			{
				for (final L2PcInstance ptmember : activeChar.getParty().getPartyMembers())
				{
					if (ptmember == null)
					{
						continue;
					}
					if (ptmember == activeChar)
					{
						continue;
					}
					
					ptmember.setPartyRoom(maxid);
					
					room.addMember(ptmember);
				}
			}
			
			activeChar.sendPacket(new PartyMatchDetail(activeChar, room));
			activeChar.sendPacket(new ExPartyRoomMember(activeChar, room, 1));
			
			activeChar.sendPacket(new SystemMessage(SystemMessageId.PARTY_ROOM_CREATED));
			
			activeChar.setPartyRoom(maxid);
			activeChar.broadcastUserInfo();
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 70 RequestPartyMatchList";
	}
}