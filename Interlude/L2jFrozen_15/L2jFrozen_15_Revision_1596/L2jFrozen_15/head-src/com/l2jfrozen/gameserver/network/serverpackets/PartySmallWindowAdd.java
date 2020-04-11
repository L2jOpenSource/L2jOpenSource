package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.4.2.1.2.5 $ $Date: 2005/03/27 15:29:57 $
 */
public final class PartySmallWindowAdd extends L2GameServerPacket
{
	private final L2PcInstance member;
	private final int leaderId;
	private final int distribution;
	
	public PartySmallWindowAdd(final L2PcInstance member, final L2Party party)
	{
		this.member = member;
		leaderId = party.getPartyLeaderOID();
		distribution = party.getLootDistribution();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4f);
		writeD(leaderId); // c3
		writeD(distribution); // c3
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
		writeD(0);
	}
	
	@Override
	public String getType()
	{
		return "[S] 4f PartySmallWindowAdd";
	}
}
