package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.L2Clan;

/**
 * This class ...
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PledgeShowInfoUpdate extends L2GameServerPacket
{
	private final L2Clan clan;
	
	public PledgeShowInfoUpdate(final L2Clan clan)
	{
		this.clan = clan;
	}
	
	@Override
	protected final void writeImpl()
	{
		final int TOP = ClanTable.getInstance().getTopRate(clan.getClanId());
		// ddddddddddSdd
		writeC(0x88);
		// sending empty data so client will ask all the info in response ;)
		writeD(clan.getClanId());
		writeD(clan.getCrestId());
		writeD(clan.getLevel()); // clan level
		writeD(clan.getHasFort() != 0 ? clan.getHasFort() : clan.getCastleId());
		writeD(clan.getHasHideout());
		writeD(TOP);
		writeD(clan.getReputationScore()); // clan reputation score
		writeD(0);
		writeD(0);
		writeD(clan.getAllyId());
		writeS(clan.getAllyName());
		writeD(clan.getAllyCrestId());
		writeD(clan.isAtWar());
	}
	
	@Override
	public String getType()
	{
		return "[S] 88 PledgeShowInfoUpdate";
	}
	
}
