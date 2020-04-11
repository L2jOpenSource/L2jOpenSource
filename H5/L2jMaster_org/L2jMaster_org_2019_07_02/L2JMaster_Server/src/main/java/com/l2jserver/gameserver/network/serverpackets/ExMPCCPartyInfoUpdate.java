/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.network.serverpackets;

import com.l2jserver.gameserver.model.L2Party;

/**
 * @author chris_00
 */
public class ExMPCCPartyInfoUpdate extends L2GameServerPacket
{
	private final int _mode, _LeaderOID, _memberCount;
	private final String _name;
	
	/**
	 * @param party
	 * @param mode 0 = Remove, 1 = Add
	 */
	public ExMPCCPartyInfoUpdate(L2Party party, int mode)
	{
		_name = party.getLeader().getName();
		_LeaderOID = party.getLeaderObjectId();
		_memberCount = party.getMemberCount();
		_mode = mode;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x5B);
		writeS(_name);
		writeD(_LeaderOID);
		writeD(_memberCount);
		writeD(_mode); // mode 0 = Remove Party, 1 = AddParty, maybe more...
	}
}
