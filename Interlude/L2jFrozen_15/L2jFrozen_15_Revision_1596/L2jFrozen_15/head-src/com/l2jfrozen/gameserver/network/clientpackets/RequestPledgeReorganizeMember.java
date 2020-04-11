package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2ClanMember;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListUpdate;

/**
 * Format: (ch) dSdS
 * @author -Wooden-
 */
public final class RequestPledgeReorganizeMember extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int unk1;
	private String memberName;
	private int newPledgeType;
	
	@SuppressWarnings("unused")
	private String unk2;
	
	@Override
	protected void readImpl()
	{
		unk1 = readD();
		memberName = readS();
		newPledgeType = readD();
		unk2 = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		// do we need powers to do that??
		final L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			return;
		}
		
		final L2ClanMember member = clan.getClanMember(memberName);
		if (member == null)
		{
			return;
		}
		
		member.setPledgeType(newPledgeType);
		clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(member));
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:24 RequestPledgeReorganizeMember";
	}
	
}
