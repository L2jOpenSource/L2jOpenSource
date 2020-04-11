package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * format ch c: (id) 0xD0 h: (subid) 0x12
 * @author -Wooden-
 */
public final class RequestOlympiadObserverEnd extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.inObserverMode())
		{
			activeChar.leaveOlympiadObserverMode();
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:12 RequestOlympiadObserverEnd";
	}
}
