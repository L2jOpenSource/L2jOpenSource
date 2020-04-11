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

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author zabbix
 */
public class PartyMemberPosition extends L2GameServerPacket
{
	private final Map<Integer, Location> locations = new HashMap<>();
	
	public PartyMemberPosition(L2Party party)
	{
		reuse(party);
	}
	
	public void reuse(L2Party party)
	{
		locations.clear();
		for (L2PcInstance member : party.getMembers())
		{
			if (member == null)
			{
				continue;
			}
			locations.put(member.getObjectId(), member.getLocation());
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xba);
		writeD(locations.size());
		for (Map.Entry<Integer, Location> entry : locations.entrySet())
		{
			Location loc = entry.getValue();
			writeD(entry.getKey());
			writeD(loc.getX());
			writeD(loc.getY());
			writeD(loc.getZ());
		}
	}
}
