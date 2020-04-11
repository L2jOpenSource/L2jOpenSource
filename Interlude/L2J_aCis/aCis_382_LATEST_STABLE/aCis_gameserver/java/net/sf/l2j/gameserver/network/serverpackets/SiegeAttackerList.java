package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;

import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.pledge.Clan;

public class SiegeAttackerList extends L2GameServerPacket
{
	private final Castle _castle;
	
	public SiegeAttackerList(Castle castle)
	{
		_castle = castle;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xca);
		writeD(_castle.getCastleId());
		writeD(0x00); // 0
		writeD(0x01); // 1
		writeD(0x00); // 0
		
		final List<Clan> attackers = _castle.getSiege().getAttackerClans();
		final int size = attackers.size();
		
		if (size > 0)
		{
			writeD(size);
			writeD(size);
			
			for (Clan clan : attackers)
			{
				writeD(clan.getClanId());
				writeS(clan.getName());
				writeS(clan.getLeaderName());
				writeD(clan.getCrestId());
				writeD(0x00); // signed time (seconds) (not storated by L2J)
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
}