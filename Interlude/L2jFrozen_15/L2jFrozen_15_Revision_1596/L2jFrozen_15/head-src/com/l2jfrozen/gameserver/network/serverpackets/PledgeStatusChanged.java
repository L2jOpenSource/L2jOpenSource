package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Clan;

/**
 * sample 0000: cd b0 98 a0 48 1e 01 00 00 00 00 00 00 00 00 00 ....H........... 0010: 00 00 00 00 00 ..... format ddddd
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PledgeStatusChanged extends L2GameServerPacket
{
	private final L2Clan clan;
	
	public PledgeStatusChanged(final L2Clan clan)
	{
		this.clan = clan;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xcd);
		writeD(clan.getLeaderId());
		writeD(clan.getClanId());
		writeD(0);
		writeD(clan.getLevel());
		writeD(0);
		writeD(0);
		writeD(0);
	}
	
	@Override
	public String getType()
	{
		return "[S] CD PledgeStatusChanged";
	}
}
