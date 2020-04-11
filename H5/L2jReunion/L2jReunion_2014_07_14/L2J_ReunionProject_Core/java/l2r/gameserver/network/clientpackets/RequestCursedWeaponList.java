/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.network.clientpackets;

import java.util.List;

import javolution.util.FastList;
import l2r.gameserver.instancemanager.CursedWeaponsManager;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.network.serverpackets.ExCursedWeaponList;

/**
 * Format: (ch)
 * @author -Wooden-
 */
public class RequestCursedWeaponList extends L2GameClientPacket
{
	private static final String _C__D0_2A_REQUESTCURSEDWEAPONLIST = "[C] D0:2A RequestCursedWeaponList";
	
	@Override
	protected void readImpl()
	{
		// nothing to read it's just a trigger
	}
	
	@Override
	protected void runImpl()
	{
		L2Character activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		// send a ExCursedWeaponList :p
		List<Integer> list = new FastList<>();
		for (int id : CursedWeaponsManager.getInstance().getCursedWeaponsIds())
		{
			list.add(id);
		}
		
		activeChar.sendPacket(new ExCursedWeaponList(list));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_2A_REQUESTCURSEDWEAPONLIST;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
