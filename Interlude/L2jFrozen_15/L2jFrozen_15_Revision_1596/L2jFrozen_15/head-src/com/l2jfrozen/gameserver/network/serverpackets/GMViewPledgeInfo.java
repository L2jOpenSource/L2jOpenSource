package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2ClanMember;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * format SdSS dddddddd d (Sddddd)
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class GMViewPledgeInfo extends L2GameServerPacket
{
	private final L2Clan clan;
	private final L2PcInstance activeChar;
	
	public GMViewPledgeInfo(final L2Clan clan, final L2PcInstance activeChar)
	{
		this.clan = clan;
		this.activeChar = activeChar;
	}
	
	@Override
	protected final void writeImpl()
	{
		final int TOP = ClanTable.getInstance().getTopRate(clan.getClanId());
		writeC(0x90);
		writeS(activeChar.getName());
		writeD(clan.getClanId());
		writeD(0x00);
		writeS(clan.getName());
		writeS(clan.getLeaderName());
		writeD(clan.getCrestId()); // -> no, it's no longer used (nuocnam) fix by game
		writeD(clan.getLevel());
		writeD(clan.getCastleId());
		writeD(clan.getHasHideout());
		writeD(TOP);
		writeD(clan.getReputationScore());
		writeD(0);
		writeD(0);
		
		writeD(clan.getAllyId()); // c2
		writeS(clan.getAllyName()); // c2
		writeD(clan.getAllyCrestId()); // c2
		writeD(clan.isAtWar()); // c3
		
		final L2ClanMember[] members = clan.getMembers();
		writeD(members.length);
		
		for (final L2ClanMember member : members)
		{
			writeS(member.getName());
			writeD(member.getLevel());
			writeD(member.getClassId());
			writeD(0);
			writeD(1);
			writeD(member.isOnline() ? member.getObjectId() : 0);
			writeD(0);
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 90 GMViewPledgeInfo";
	}
	
}
