package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestReplyStartPledgeWar extends L2GameClientPacket
{
	private int answer;
	
	@Override
	protected void readImpl()
	{
		readS();
		answer = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final L2PcInstance requestor = activeChar.getActiveRequester();
		if (requestor == null)
		{
			return;
		}
		
		if (answer == 1)
		{
			ClanTable.getInstance().storeClanWars(requestor.getClanId(), activeChar.getClanId());
		}
		else
		{
			requestor.sendPacket(new SystemMessage(SystemMessageId.WAR_PROCLAMATION_HAS_BEEN_REFUSED));
		}
		
		activeChar.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
	
	@Override
	public String getType()
	{
		return "[C] 4e RequestReplyStartPledgeWar";
	}
}
