package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestWithdrawalPledge extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.getClan() == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER));
			return;
		}
		
		if (activeChar.isClanLeader())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_LEADER_CANNOT_WITHDRAW));
			return;
		}
		
		if (activeChar.isInCombat())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_LEAVE_DURING_COMBAT));
			return;
		}
		
		final L2Clan clan = activeChar.getClan();
		
		clan.removeClanMember(activeChar.getName(), System.currentTimeMillis() + Config.ALT_CLAN_JOIN_DAYS * 86400000L); // 24*60*60*1000 = 86400000
		
		SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_WITHDRAWN_FROM_THE_CLAN);
		sm.addString(activeChar.getName());
		clan.broadcastToOnlineMembers(sm);
		sm = null;
		
		// Remove the Player From the Member list
		clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(activeChar.getName()));
		
		activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_WITHDRAWN_FROM_CLAN));
		activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN));
		activeChar.setActiveWarehouse(null);
	}
	
	@Override
	public String getType()
	{
		return "[C] 26 RequestWithdrawalPledge";
	}
}