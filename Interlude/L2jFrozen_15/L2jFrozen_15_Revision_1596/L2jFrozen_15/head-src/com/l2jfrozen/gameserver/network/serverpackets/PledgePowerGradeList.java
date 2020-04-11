package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Clan.RankPrivs;

/**
 * sample 0000: 9c c10c0000 48 00 61 00 6d 00 62 00 75 00 72 .....H.a.m.b.u.r 0010: 00 67 00 00 00 00000000 00000000 00000000 00000000 00000000 00000000 00 00 00000000 ... format dd ??
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class PledgePowerGradeList extends L2GameServerPacket
{
	private final RankPrivs[] privs;
	
	public PledgePowerGradeList(final RankPrivs[] privs)
	{
		this.privs = privs;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x3b);
		writeD(privs.length);
		for (final RankPrivs priv : privs)
		{
			writeD(priv.getRank());
			writeD(priv.getParty());
			// LOGGER.warn("rank: "+_privs[i].getRank()+" party: "+_privs[i].getParty());
		}
		
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:3B PledgePowerGradeList";
	}
	
}
