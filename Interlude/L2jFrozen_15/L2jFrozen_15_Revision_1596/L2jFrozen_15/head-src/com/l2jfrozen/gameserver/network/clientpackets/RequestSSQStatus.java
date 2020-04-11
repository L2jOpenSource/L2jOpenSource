package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jfrozen.gameserver.network.serverpackets.SSQStatus;

/**
 * Seven Signs Record Update Request packet type id 0xc7 format: cc
 * @author Tempy
 */
public final class RequestSSQStatus extends L2GameClientPacket
{
	private int page;
	
	@Override
	protected void readImpl()
	{
		page = readC();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if ((SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod()) && page == 4)
		{
			return;
		}
		
		final SSQStatus ssqs = new SSQStatus(activeChar, page);
		activeChar.sendPacket(ssqs);
	}
	
	@Override
	public String getType()
	{
		return "[C] C7 RequestSSQStatus";
	}
}
