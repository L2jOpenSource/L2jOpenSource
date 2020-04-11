package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2ClanMember;

/**
 * Format : (ch) dSd
 * @author -Wooden-
 */
public class PledgeReceivePowerInfo extends L2GameServerPacket
{
	private final L2ClanMember member;
	
	/**
	 * @param member
	 */
	public PledgeReceivePowerInfo(final L2ClanMember member)
	{
		this.member = member;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x3c);
		
		writeD(member.getPowerGrade()); // power grade
		writeS(member.getName());
		writeD(member.getClan().getRankPrivs(member.getPowerGrade())); // privileges
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:3D PledgeReceivePowerInfo";
	}
}
