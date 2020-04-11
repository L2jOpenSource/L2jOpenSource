package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2ClanMember;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestOustPledgeMember extends L2GameClientPacket
{
	static Logger LOGGER = Logger.getLogger(RequestOustPledgeMember.class);
	
	private String target;
	
	@Override
	protected void readImpl()
	{
		target = readS();
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
		
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_DISMISS) != L2Clan.CP_CL_DISMISS)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return;
		}
		
		if (activeChar.getName().equalsIgnoreCase(target))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_DISMISS_YOURSELF));
			return;
		}
		
		final L2Clan clan = activeChar.getClan();
		
		final L2ClanMember member = clan.getClanMember(target);
		
		if (member == null)
		{
			LOGGER.debug("Target (" + target + ") is not member of the clan");
			return;
		}
		
		if (member.isOnline() && member.getPlayerInstance().isInCombat())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_MEMBER_CANNOT_BE_DISMISSED_DURING_COMBAT));
			return;
		}
		
		// this also updates the database
		clan.removeClanMember(target, System.currentTimeMillis() + Config.ALT_CLAN_JOIN_DAYS * 86400000L); // Like L2OFF also player takes the penality
		clan.setCharPenaltyExpiryTime(System.currentTimeMillis() + Config.ALT_CLAN_JOIN_DAYS * 86400000L); // 24*60*60*1000 = 86400000
		clan.updateClanInDB();
		
		SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED);
		sm.addString(member.getName());
		clan.broadcastToOnlineMembers(sm);
		sm = null;
		activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_EXPELLING_CLAN_MEMBER));
		activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER));
		
		// Remove the Player From the Member list
		clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(target));
		if (member.isOnline())
		{
			final L2PcInstance player = member.getPlayerInstance();
			player.sendPacket(new SystemMessage(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED));
			player.setActiveWarehouse(null);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 27 RequestOustPledgeMember";
	}
}