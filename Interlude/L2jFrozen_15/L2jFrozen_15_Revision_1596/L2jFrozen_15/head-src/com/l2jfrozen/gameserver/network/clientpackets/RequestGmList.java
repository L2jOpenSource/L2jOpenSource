package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.datatables.GmListTable;

/**
 * This class handles RequestGmLista packet triggered by /gmlist command
 */
public final class RequestGmList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		
	}
	
	@Override
	protected void runImpl()
	{
		if (getClient().getActiveChar() == null)
		{
			return;
		}
		
		GmListTable.getInstance().sendListToPlayer(getClient().getActiveChar());
	}
	
	@Override
	public String getType()
	{
		return "[C] 81 RequestGmList";
	}
}
