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
import l2r.gameserver.model.L2ClanMember;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class GMViewPledgeInfo extends L2GameServerPacket
{
	private final L2Clan _clan;
	private final L2PcInstance _activeChar;
	
	public GMViewPledgeInfo(L2Clan clan, L2PcInstance activeChar)
	{
		_clan = clan;
		_activeChar = activeChar;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x96);
		writeS(_activeChar.getName());
		writeD(_clan.getId());
		writeD(0x00);
		writeS(_clan.getName());
		writeS(_clan.getLeaderName());
		writeD(_clan.getCrestId()); // -> no, it's no longer used (nuocnam) fix by game
		writeD(_clan.getLevel());
		writeD(_clan.getCastleId());
		writeD(_clan.getHideoutId());
		writeD(_clan.getFortId());
		writeD(_clan.getRank());
		writeD(_clan.getReputationScore());
		writeD(0x00);
		writeD(0x00);
		
		writeD(_clan.getAllyId()); // c2
		writeS(_clan.getAllyName()); // c2
		writeD(_clan.getAllyCrestId()); // c2
		writeD(_clan.isAtWar() ? 1 : 0); // c3
		writeD(0x00); // T3 Unknown
		writeD(_clan.getMembers().length);
		
		for (L2ClanMember member : _clan.getMembers())
		{
			if (member != null)
			{
				writeS(member.getName());
				writeD(member.getLevel());
				writeD(member.getClassId());
				writeD(member.getSex() ? 1 : 0);
				writeD(member.getRaceOrdinal());
				writeD(member.isOnline() ? member.getObjectId() : 0);
				writeD(member.getSponsor() != 0 ? 1 : 0);
			}
		}
	}
}
