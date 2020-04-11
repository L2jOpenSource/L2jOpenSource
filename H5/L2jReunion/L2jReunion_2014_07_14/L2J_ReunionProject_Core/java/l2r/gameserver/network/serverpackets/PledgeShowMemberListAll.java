/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2Clan.SubPledge;
import l2r.gameserver.model.L2ClanMember;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class PledgeShowMemberListAll extends L2GameServerPacket
{
	private final L2Clan _clan;
	private final L2PcInstance _activeChar;
	private final L2ClanMember[] _members;
	private int _pledgeType;
	
	public PledgeShowMemberListAll(L2Clan clan, L2PcInstance activeChar)
	{
		_clan = clan;
		_activeChar = activeChar;
		_members = _clan.getMembers();
	}
	
	@Override
	protected final void writeImpl()
	{
		_pledgeType = 0;
		// FIXME: That's wrong on retail sends this whole packet few times (depending how much sub pledges it has)
		writePledge(0);
		
		for (SubPledge subPledge : _clan.getAllSubPledges())
		{
			_activeChar.sendPacket(new PledgeReceiveSubPledgeCreated(subPledge, _clan));
		}
		
		for (L2ClanMember m : _members)
		{
			if (m.getPledgeType() == 0)
			{
				continue;
			}
			_activeChar.sendPacket(new PledgeShowMemberListAdd(m));
		}
		
		// unless this is sent sometimes, the client doesn't recognise the player as the leader
		_activeChar.sendPacket(new UserInfo(_activeChar));
		_activeChar.sendPacket(new ExBrExtraUserInfo(_activeChar));
		
	}
	
	private void writePledge(int mainOrSubpledge)
	{
		writeC(0x5a);
		
		writeD(mainOrSubpledge);
		writeD(_clan.getId());
		writeD(_pledgeType);
		writeS(_clan.getName());
		writeS(_clan.getLeaderName());
		
		writeD(_clan.getCrestId()); // crest id .. is used again
		writeD(_clan.getLevel());
		writeD(_clan.getCastleId());
		writeD(_clan.getHideoutId());
		writeD(_clan.getFortId());
		writeD(_clan.getRank());
		writeD(_clan.getReputationScore());
		writeD(0x00); // 0
		writeD(0x00); // 0
		writeD(_clan.getAllyId());
		writeS(_clan.getAllyName());
		writeD(_clan.getAllyCrestId());
		writeD(_clan.isAtWar() ? 1 : 0);// new c3
		writeD(0x00); // Territory castle ID
		writeD(_clan.getSubPledgeMembersCount(_pledgeType));
		
		for (L2ClanMember m : _members)
		{
			if (m.getPledgeType() != _pledgeType)
			{
				continue;
			}
			writeS(m.getName());
			writeD(m.getLevel());
			writeD(m.getClassId());
			L2PcInstance player;
			if ((player = m.getPlayerInstance()) != null)
			{
				writeD(player.getAppearance().getSex() ? 1 : 0); // no visible effect
				writeD(player.getRace().ordinal());// writeD(1);
			}
			else
			{
				writeD(0x01); // no visible effect
				writeD(0x01); // writeD(1);
			}
			writeD(m.isOnline() ? m.getObjectId() : 0); // objectId = online 0 = offline
			writeD(m.getSponsor() != 0 ? 1 : 0);
		}
	}
}
