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

import l2r.gameserver.model.L2ClanMember;

/**
 * @author -Wooden-
 */
public class PledgeReceiveMemberInfo extends L2GameServerPacket
{
	private final L2ClanMember _member;
	
	/**
	 * @param member
	 */
	public PledgeReceiveMemberInfo(L2ClanMember member)
	{
		_member = member;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x3e);
		
		writeD(_member.getPledgeType());
		writeS(_member.getName());
		writeS(_member.getTitle()); // title
		writeD(_member.getPowerGrade()); // power
		
		// clan or subpledge name
		if (_member.getPledgeType() != 0)
		{
			writeS((_member.getClan().getSubPledge(_member.getPledgeType())).getName());
		}
		else
		{
			writeS(_member.getClan().getName());
		}
		
		writeS(_member.getApprenticeOrSponsorName()); // name of this member's apprentice/sponsor
	}
}
