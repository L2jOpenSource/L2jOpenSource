package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * sample 5F 01 00 00 00 format cdd
 */
public final class RequestAnswerJoinAlly extends L2GameClientPacket
{
	private int response;
	
	@Override
	protected void readImpl()
	{
		response = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		final L2PcInstance requestor = activeChar.getRequest().getPartner();
		
		if (requestor == null)
		{
			return;
		}
		
		if (response == 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_DID_NOT_RESPOND_TO_ALLY_INVITATION));
			requestor.sendPacket(new SystemMessage(SystemMessageId.NO_RESPONSE_TO_ALLY_INVITATION));
		}
		else
		{
			if (!(requestor.getRequest().getRequestPacket() instanceof RequestJoinAlly))
			{
				return; // hax
			}
			
			final L2Clan clan = requestor.getClan();
			// we must double check this cause of hack
			if (clan.checkAllyJoinCondition(requestor, activeChar))
			{
				// TODO: Need correct message id
				requestor.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_INVITING_FRIEND));
				activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ACCEPTED_ALLIANCE));
				activeChar.getClan().setAllyId(clan.getAllyId());
				activeChar.getClan().setAllyName(clan.getAllyName());
				activeChar.getClan().setAllyPenaltyExpiryTime(0, 0);
				activeChar.getClan().setAllyCrest(clan.getAllyCrestId());
				activeChar.getClan().updateClanInDB();
			}
		}
		
		activeChar.getRequest().onRequestResponse();
	}
	
	@Override
	public String getType()
	{
		return "[C] 83 RequestAnswerJoinAlly";
	}
}
