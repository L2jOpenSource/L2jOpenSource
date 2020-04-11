package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.datatables.csv.HennaTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2HennaInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.HennaItemInfo;
import com.l2jfrozen.gameserver.templates.L2Henna;

public final class RequestHennaItemInfo extends L2GameClientPacket
{
	private int symbolId;
	
	@Override
	protected void readImpl()
	{
		symbolId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		final L2Henna template = HennaTable.getInstance().getTemplate(symbolId);
		
		if (template == null)
		{
			return;
		}
		
		final L2HennaInstance temp = new L2HennaInstance(template);
		
		final HennaItemInfo hii = new HennaItemInfo(temp, activeChar);
		activeChar.sendPacket(hii);
	}
	
	@Override
	public String getType()
	{
		return "[C] bb RequestHennaItemInfo";
	}
}
