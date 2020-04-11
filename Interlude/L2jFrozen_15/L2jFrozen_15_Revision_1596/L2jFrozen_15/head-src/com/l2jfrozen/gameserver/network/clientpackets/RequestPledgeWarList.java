package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeReceiveWarList;

/**
 * Format: (ch) dd
 * @author -Wooden-
 */
public final class RequestPledgeWarList extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int unk1;
	private int tab;
	
	@Override
	protected void readImpl()
	{
		unk1 = readD();
		tab = readD();
	}
	
	@Override
	protected void runImpl()
	{
		// LOGGER.info("C5: RequestPledgeWarList d:"+_unk1);
		// LOGGER.info("C5: RequestPledgeWarList d:"+_tab);
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.getClan() == null)
		{
			return;
		}
		
		// do we need powers to do that??
		activeChar.sendPacket(new PledgeReceiveWarList(activeChar.getClan(), tab));
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:1E RequestPledgeWarList";
	}
	
}
