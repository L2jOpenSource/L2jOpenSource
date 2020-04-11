package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;

import net.sf.l2j.gameserver.model.clanhall.SiegableHall;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.pledge.Clan;

public class SiegeAttackerList extends L2GameServerPacket
{
	private final int _id;
	private final List<Clan> _attackers;
	
	public SiegeAttackerList(Castle castle)
	{
		_id = castle.getCastleId();
		_attackers = castle.getSiege().getAttackerClans();
	}
	
	public SiegeAttackerList(SiegableHall hall)
	{
		_id = hall.getId();
		_attackers = hall.getSiege().getAttackerClans();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xca);
		writeD(_id);
		writeD(0x00);
		writeD(0x01);
		writeD(0x00);
		
		final int size = _attackers.size();
		
		if (size > 0)
		{
			writeD(size);
			writeD(size);
			
			for (Clan clan : _attackers)
			{
				writeD(clan.getClanId());
				writeS(clan.getName());
				writeS(clan.getLeaderName());
				writeD(clan.getCrestId());
				writeD(0x00);
				writeD(clan.getAllyId());
				writeS(clan.getAllyName());
				writeS(""); // TODO AllyLeaderName
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