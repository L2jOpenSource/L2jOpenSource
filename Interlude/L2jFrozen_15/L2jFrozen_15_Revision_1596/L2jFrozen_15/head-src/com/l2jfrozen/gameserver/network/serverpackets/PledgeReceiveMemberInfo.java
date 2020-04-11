package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2ClanMember;

/**
 * @author -Wooden-
 */
public class PledgeReceiveMemberInfo extends L2GameServerPacket
{
	private final L2ClanMember member;
	
	/**
	 * @param member
	 */
	public PledgeReceiveMemberInfo(final L2ClanMember member)
	{
		this.member = member;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x3d);
		
		writeD(member.getPledgeType());
		writeS(member.getName());
		writeS(member.getTitle()); // title
		writeD(member.getPowerGrade()); // power
		
		// clan or subpledge name
		if (member.getPledgeType() != 0)
		{
			writeS(member.getClan().getSubPledge(member.getPledgeType()).getName());
		}
		else
		{
			writeS(member.getClan().getName());
		}
		
		writeS(member.getApprenticeOrSponsorName()); // name of this member's apprentice/sponsor
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:3D PledgeReceiveMemberInfo";
	}
}
