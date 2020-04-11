package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public final class RequestReplySurrenderPledgeWar extends L2GameClientPacket
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
			requestor.deathPenalty(false);
			ClanTable.getInstance().deleteClanWars(requestor.getClanId(), activeChar.getClanId());
		}
		else
		{
		}
		
		activeChar.onTransactionRequest(null);
	}
	
	@Override
	public String getType()
	{
		return "[C] 52 RequestReplySurrenderPledgeWar";
	}
}
