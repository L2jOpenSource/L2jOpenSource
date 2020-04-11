package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.SubPledge;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.JoinPledge;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListAdd;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestAnswerJoinPledge extends L2GameClientPacket
{
	private int _answer;
	
	@Override
	protected void readImpl()
	{
		_answer = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		final Player requestor = player.getRequest().getPartner();
		if (requestor == null)
			return;
		
		if (_answer == 0)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_NOT_RESPOND_TO_S1_CLAN_INVITATION).addCharName(requestor));
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DID_NOT_RESPOND_TO_CLAN_INVITATION).addCharName(player));
		}
		else
		{
			if (!(requestor.getRequest().getRequestPacket() instanceof RequestJoinPledge))
				return; // hax
				
			final RequestJoinPledge requestPacket = (RequestJoinPledge) requestor.getRequest().getRequestPacket();
			final Clan clan = requestor.getClan();
			
			// we must double check this cause during response time conditions can be changed, i.e. another player could join clan
			if (clan.checkClanJoinCondition(requestor, player, requestPacket.getPledgeType()))
			{
				player.sendPacket(new JoinPledge(requestor.getClanId()));
				
				player.setPledgeType(requestPacket.getPledgeType());
				
				switch (requestPacket.getPledgeType())
				{
					case Clan.SUBUNIT_ACADEMY:
						player.setPowerGrade(9);
						player.setLvlJoinedAcademy(player.getLevel());
						break;
					
					case Clan.SUBUNIT_ROYAL1:
					case Clan.SUBUNIT_ROYAL2:
						player.setPowerGrade(7);
						break;
					
					case Clan.SUBUNIT_KNIGHT1:
					case Clan.SUBUNIT_KNIGHT2:
					case Clan.SUBUNIT_KNIGHT3:
					case Clan.SUBUNIT_KNIGHT4:
						player.setPowerGrade(8);
						break;
					
					default:
						player.setPowerGrade(6);
				}
				
				clan.addClanMember(player);
				player.setClanPrivileges(clan.getPriviledgesByRank(player.getPowerGrade()));
				
				player.sendPacket(SystemMessageId.ENTERED_THE_CLAN);
				
				clan.broadcastToOtherOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_JOINED_CLAN).addCharName(player), player);
				clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(player), player);
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				
				// this activates the clan tab on the new member
				player.sendPacket(new PledgeShowMemberListAll(clan, 0));
				for (SubPledge sp : player.getClan().getAllSubPledges())
					player.sendPacket(new PledgeShowMemberListAll(clan, sp.getId()));
				
				player.setClanJoinExpiryTime(0);
				player.broadcastUserInfo();
			}
		}
		player.getRequest().onRequestResponse();
	}
}