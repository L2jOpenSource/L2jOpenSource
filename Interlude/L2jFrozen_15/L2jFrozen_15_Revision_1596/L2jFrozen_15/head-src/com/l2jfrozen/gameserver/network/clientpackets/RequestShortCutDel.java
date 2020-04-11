package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public final class RequestShortCutDel extends L2GameClientPacket
{
	private int slot;
	private int page;
	
	@Override
	protected void readImpl()
	{
		final int id = readD();
		slot = id % 12;
		page = id / 12;
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		activeChar.deleteShortCut(slot, page);
		// client needs no confirmation. this packet is just to inform the server
	}
	
	@Override
	public String getType()
	{
		return "[C] 35 RequestShortCutDel";
	}
}
