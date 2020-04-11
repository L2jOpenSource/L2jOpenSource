package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.network.serverpackets.AllyCrest;

public final class RequestAllyCrest extends L2GameClientPacket
{
	private int crestId;
	
	/**
	 * packet type id 0x88 format: cd
	 */
	@Override
	protected void readImpl()
	{
		crestId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		sendPacket(new AllyCrest(crestId));
	}
	
	@Override
	public String getType()
	{
		return "[C] 88 RequestAllyCrest";
	}
}
