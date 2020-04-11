package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Clan;

/**
 * sample 0000: 9c c10c0000 48 00 61 00 6d 00 62 00 75 00 72 .....H.a.m.b.u.r 0010: 00 67 00 00 00 00000000 00000000 00000000 00000000 00000000 00000000 00 00 00000000 ... format dSddddddSd
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class PledgeInfo extends L2GameServerPacket
{
	private final L2Clan clan;
	
	public PledgeInfo(final L2Clan clan)
	{
		this.clan = clan;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x83);
		writeD(clan.getClanId());
		writeS(clan.getName());
		writeS(clan.getAllyName());
	}
	
	@Override
	public String getType()
	{
		return "[S] 9C PledgeInfo";
	}
	
}
