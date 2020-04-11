package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.datatables.sql.HennaTreeTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2HennaInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.HennaEquipList;

/**
 * RequestHennaList - 0xba
 * @author Tempy
 */
public final class RequestHennaList extends L2GameClientPacket
{
	// This is just a trigger packet...
	@SuppressWarnings("unused")
	private int unknown;
	
	@Override
	protected void readImpl()
	{
		unknown = readD(); // ??
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		final L2HennaInstance[] henna = HennaTreeTable.getInstance().getAvailableHenna(activeChar.getClassId());
		final HennaEquipList he = new HennaEquipList(activeChar, henna);
		activeChar.sendPacket(he);
	}
	
	@Override
	public String getType()
	{
		return "[C] ba RequestHennaList";
	}
}
