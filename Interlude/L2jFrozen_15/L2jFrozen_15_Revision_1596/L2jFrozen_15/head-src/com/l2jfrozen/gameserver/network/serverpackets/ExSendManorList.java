package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.List;

/**
 * Format : (h) d [dS] h sub id d: number of manors [ d: id S: manor name ]
 * @author l3x
 */
public class ExSendManorList extends L2GameServerPacket
{
	private final List<String> manors;
	
	public ExSendManorList(final List<String> manors)
	{
		this.manors = manors;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x1B);
		writeD(manors.size());
		for (int i = 0; i < manors.size(); i++)
		{
			final int j = i + 1;
			writeD(j);
			writeS(manors.get(i));
		}
		
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:1B ExSendManorList";
	}
}
