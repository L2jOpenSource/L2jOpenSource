package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.network.serverpackets.PledgeCrest;

public final class RequestPledgeCrest extends L2GameClientPacket
{
	private int crestId;
	
	@Override
	protected void readImpl()
	{
		crestId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		sendPacket(new PledgeCrest(crestId));
	}
	
	@Override
	public String getType()
	{
		return "[C] 68 RequestPledgeCrest";
	}
	
}
