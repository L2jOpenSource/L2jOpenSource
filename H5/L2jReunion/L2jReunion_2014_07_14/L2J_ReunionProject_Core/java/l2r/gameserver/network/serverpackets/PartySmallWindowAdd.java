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

import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public final class PartySmallWindowAdd extends L2GameServerPacket
{
	
	private final L2PcInstance _member;
	private final int _leaderId;
	private final int _distribution;
	
	public PartySmallWindowAdd(L2PcInstance member, L2Party party)
	{
		_member = member;
		_leaderId = party.getLeaderObjectId();
		_distribution = party.getLootDistribution();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4F);
		writeD(_leaderId); // c3
		writeD(_distribution);// writeD(0x04); ?? //c3
		writeD(_member.getObjectId());
		writeS(_member.getName());
		writeD((int) _member.getCurrentCp()); // c4
		writeD(_member.getMaxCp()); // c4
		writeD((int) _member.getCurrentHp());
		writeD(_member.getMaxHp());
		writeD((int) _member.getCurrentMp());
		writeD(_member.getMaxMp());
		writeD(_member.getLevel());
		writeD(_member.getClassId().getId());
		writeD(0x00); // ?
		writeD(0x00); // ?
	}
}
