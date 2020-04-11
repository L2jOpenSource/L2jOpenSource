package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2SiegeClan;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;

/**
 * Populates the Siege Defender List in the SiegeInfo Window<BR>
 * <BR>
 * packet type id 0xcb<BR>
 * format: cddddddd + dSSdddSSd<BR>
 * <BR>
 * c = 0xcb<BR>
 * d = CastleID<BR>
 * d = unknow (0x00)<BR>
 * d = unknow (0x01)<BR>
 * d = unknow (0x00)<BR>
 * d = Number of Defending Clans?<BR>
 * d = Number of Defending Clans<BR>
 * { //repeats<BR>
 * d = ClanID<BR>
 * S = ClanName<BR>
 * S = ClanLeaderName<BR>
 * d = ClanCrestID<BR>
 * d = signed time (seconds)<BR>
 * d = Type -> Owner = 0x01 || Waiting = 0x02 || Accepted = 0x03<BR>
 * d = AllyID<BR>
 * S = AllyName<BR>
 * S = AllyLeaderName<BR>
 * d = AllyCrestID<BR>
 * @author KenM
 */
public class SiegeDefenderList extends L2GameServerPacket
{
	private final Castle castle;
	
	public SiegeDefenderList(final Castle castle)
	{
		this.castle = castle;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xcb);
		writeD(castle.getCastleId());
		writeD(0x00); // 0
		writeD(0x01); // 1
		writeD(0x00); // 0
		final int size = castle.getSiege().getDefenderClans().size() + castle.getSiege().getDefenderWaitingClans().size();
		if (size > 0)
		{
			L2Clan clan;
			
			writeD(size);
			writeD(size);
			// Listing the Lord and the approved clans
			for (final L2SiegeClan siegeclan : castle.getSiege().getDefenderClans())
			{
				clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
				if (clan == null)
				{
					continue;
				}
				
				writeD(clan.getClanId());
				writeS(clan.getName());
				writeS(clan.getLeaderName());
				writeD(clan.getCrestId());
				writeD(0x00); // signed time (seconds) (not storated by L2J)
				switch (siegeclan.getType())
				{
					case OWNER:
						writeD(0x01); // owner
						break;
					case DEFENDER_PENDING:
						writeD(0x02); // approved
						break;
					case DEFENDER:
						writeD(0x03); // waiting approved
						break;
					default:
						writeD(0x00);
						break;
				}
				writeD(clan.getAllyId());
				writeS(clan.getAllyName());
				writeS(""); // AllyLeaderName
				writeD(clan.getAllyCrestId());
			}
			for (final L2SiegeClan siegeclan : castle.getSiege().getDefenderWaitingClans())
			{
				clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
				writeD(clan.getClanId());
				writeS(clan.getName());
				writeS(clan.getLeaderName());
				writeD(clan.getCrestId());
				writeD(0x00); // signed time (seconds) (not storated by L2J)
				writeD(0x02); // waiting approval
				writeD(clan.getAllyId());
				writeS(clan.getAllyName());
				writeS(""); // AllyLeaderName
				writeD(clan.getAllyCrestId());
			}
		}
		else
		{
			writeD(0x00);
			writeD(0x00);
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] cb SiegeDefenderList";
	}
	
}
