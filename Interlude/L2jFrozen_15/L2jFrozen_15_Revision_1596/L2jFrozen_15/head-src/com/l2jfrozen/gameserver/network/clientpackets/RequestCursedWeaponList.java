package com.l2jfrozen.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.managers.CursedWeaponsManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.network.serverpackets.ExCursedWeaponList;

/**
 * Format: (ch)
 * @author -Wooden-
 */
public class RequestCursedWeaponList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// nothing to read it's just a trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2Character activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		// send a ExCursedWeaponList :p
		final List<Integer> list = new ArrayList<>();
		for (final int id : CursedWeaponsManager.getInstance().getCursedWeaponsIds())
		{
			list.add(id);
		}
		
		activeChar.sendPacket(new ExCursedWeaponList(list));
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:22 RequestCursedWeaponList";
	}
	
}
