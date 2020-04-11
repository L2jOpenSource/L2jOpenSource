package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * Format: (ch) Sd
 * @author -Wooden-
 */
public final class RequestPledgeSetMemberPowerGrade extends L2GameClientPacket
{
	private int _powerGrade;
	private String _member;
	
	@Override
	protected void readImpl()
	{
		_member = readS();
		_powerGrade = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getPlayer();
		if (activeChar == null)
			return;
		
		final Clan clan = activeChar.getClan();
		if (clan == null)
			return;
		
		final ClanMember member = clan.getClanMember(_member);
		if (member == null)
			return;
		
		if (member.getPledgeType() == Clan.SUBUNIT_ACADEMY)
			return;
		
		member.setPowerGrade(_powerGrade);
		clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(member), SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_PRIVILEGE_CHANGED_TO_S2).addString(member.getName()).addNumber(_powerGrade));
	}
}