package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;

public class GMViewPledgeInfo extends L2GameServerPacket
{
	private final Clan _clan;
	private final Player _player;
	
	public GMViewPledgeInfo(Clan clan, Player player)
	{
		_clan = clan;
		_player = player;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x90);
		writeS(_player.getName());
		writeD(_clan.getClanId());
		writeD(0x00);
		writeS(_clan.getName());
		writeS(_clan.getLeaderName());
		writeD(_clan.getCrestId()); // -> no, it's no longer used (nuocnam) fix by game
		writeD(_clan.getLevel());
		writeD(_clan.getCastleId());
		writeD(_clan.getClanHallId());
		writeD(_clan.getRank());
		writeD(_clan.getReputationScore());
		writeD(0);
		writeD(0);
		
		writeD(_clan.getAllyId()); // c2
		writeS(_clan.getAllyName()); // c2
		writeD(_clan.getAllyCrestId()); // c2
		writeD(_clan.isAtWar() ? 1 : 0); // c3
		writeD(_clan.getMembersCount());
		
		for (ClanMember member : _clan.getMembers())
		{
			if (member != null)
			{
				writeS(member.getName());
				writeD(member.getLevel());
				writeD(member.getClassId());
				writeD(member.getSex().ordinal());
				writeD(member.getRaceOrdinal());
				writeD(member.isOnline() ? member.getObjectId() : 0);
				writeD(member.getSponsor() != 0 ? 1 : 0);
			}
		}
	}
}