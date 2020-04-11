package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2HennaInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public final class HennaInfo extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	private final L2HennaInstance[] hennas = new L2HennaInstance[3];
	private int count;
	
	public HennaInfo(final L2PcInstance player)
	{
		activeChar = player;
		count = 0;
		
		for (int i = 0; i < 3; i++)
		{
			L2HennaInstance henna = activeChar.getHennas(i + 1);
			if (henna != null)
			{
				hennas[count++] = henna;
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe4);
		
		writeC(activeChar.getHennaStatINT()); // equip INT
		writeC(activeChar.getHennaStatSTR()); // equip STR
		writeC(activeChar.getHennaStatCON()); // equip CON
		writeC(activeChar.getHennaStatMEN()); // equip MEM
		writeC(activeChar.getHennaStatDEX()); // equip DEX
		writeC(activeChar.getHennaStatWIT()); // equip WIT
		
		// Henna slots
		int classId = activeChar.getClassId().level();
		if (classId == 1)
		{
			writeD(2);
		}
		else if (classId > 1)
		{
			writeD(3);
		}
		else
		{
			writeD(0);
		}
		
		writeD(count); // size
		for (int i = 0; i < count; i++)
		{
			writeD(hennas[i].getSymbolId());
			writeD(hennas[i].getSymbolId());
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] E4 HennaInfo";
	}
}
