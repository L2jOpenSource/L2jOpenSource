package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2CommandChannel;
import com.l2jfrozen.gameserver.model.L2Party;

/**
 * @author chris_00 ch sdd d[sdd]
 */
public class ExMultiPartyCommandChannelInfo extends L2GameServerPacket
{
	private final L2CommandChannel channel;
	
	public ExMultiPartyCommandChannelInfo(final L2CommandChannel channel)
	{
		this.channel = channel;
	}
	
	@Override
	protected void writeImpl()
	{
		if (channel == null)
		{
			return;
		}
		
		writeC(0xfe);
		writeH(0x30);
		
		writeS(channel.getChannelLeader().getName());
		writeD(0); // Channel loot
		writeD(channel.getMemberCount());
		
		writeD(channel.getPartys().size());
		for (final L2Party p : channel.getPartys())
		{
			writeS(p.getLeader().getName());
			writeD(p.getPartyLeaderOID());
			writeD(p.getMemberCount());
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:30 ExMultiPartyCommandChannelInfo";
	}
}