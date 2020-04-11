package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * sample 63 01 00 00 00 count c1 b2 e0 4a object id 54 00 75 00 65 00 73 00 64 00 61 00 79 00 00 00 name 5a 01 00 00 hp 5a 01 00 00 hp max 89 00 00 00 mp 89 00 00 00 mp max 0e 00 00 00 level 12 00 00 00 class 00 00 00 00 01 00 00 00 format d (dSdddddddd)
 * @version $Revision: 1.6.2.1.2.5 $ $Date: 2005/03/27 15:29:57 $
 */
public final class PartySmallWindowAll extends L2GameServerPacket
{
	private final L2Party party;
	private final L2PcInstance exclude;
	private final int dist, leaderOID;
	
	public PartySmallWindowAll(final L2PcInstance exclude, final L2Party party)
	{
		this.exclude = exclude;
		this.party = party;
		leaderOID = this.party.getPartyLeaderOID();
		dist = this.party.getLootDistribution();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4e);
		writeD(leaderOID);
		writeD(dist);
		writeD(party.getMemberCount() - 1);
		
		for (final L2PcInstance member : party.getPartyMembers())
		{
			if ((member != null) && (member != exclude))
			{
				writeD(member.getObjectId());
				writeS(member.getName());
				
				writeD((int) member.getCurrentCp()); // c4
				writeD(member.getMaxCp()); // c4
				
				writeD((int) member.getCurrentHp());
				writeD(member.getMaxHp());
				writeD((int) member.getCurrentMp());
				writeD(member.getMaxMp());
				writeD(member.getLevel());
				writeD(member.getClassId().getId());
				writeD(0);// writeD(0x01); ??
				writeD(member.getRace().ordinal());
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 4e PartySmallWindowAll";
	}
}
