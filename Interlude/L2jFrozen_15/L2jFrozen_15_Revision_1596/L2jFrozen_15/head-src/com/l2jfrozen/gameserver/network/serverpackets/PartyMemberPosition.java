package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.Map;

import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author zabbix
 */
public class PartyMemberPosition extends L2GameServerPacket
{
	Map<Integer, Location> locations = new HashMap<>();
	
	public PartyMemberPosition(final L2Party party)
	{
		reuse(party);
	}
	
	public void reuse(final L2Party party)
	{
		locations.clear();
		for (final L2PcInstance member : party.getPartyMembers())
		{
			if (member == null)
			{
				continue;
			}
			locations.put(member.getObjectId(), new Location(member));
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xa7);
		writeD(locations.size());
		
		for (final Map.Entry<Integer, Location> entry : locations.entrySet())
		{
			final Location loc = entry.getValue();
			writeD(entry.getKey());
			writeD(loc.getX());
			writeD(loc.getY());
			writeD(loc.getZ());
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] a7 PartyMemberPosition";
	}
	
}
