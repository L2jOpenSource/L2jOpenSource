package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2ClanMember;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * Format: (ch) Sd
 * @author -Wooden-
 */
public final class RequestPledgeSetMemberPowerGrade extends L2GameClientPacket
{
	private int powerGrade;
	private String clanMember;
	
	@Override
	protected void readImpl()
	{
		clanMember = readS();
		powerGrade = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			return;
		}
		
		final L2ClanMember member = clan.getClanMember(clanMember);
		if (member == null)
		{
			return;
		}
		
		if (member.getPledgeType() == L2Clan.SUBUNIT_ACADEMY)
		{
			// also checked from client side
			activeChar.sendMessage("You cannot change academy member grade");
			return;
		}
		
		member.setPowerGrade(powerGrade);
		clan.broadcastClanStatus();
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:1C RequestPledgeSetMemberPowerGrade";
	}
	
}
