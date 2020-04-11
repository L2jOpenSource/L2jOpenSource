/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.bypasshandlers;

import java.util.StringTokenizer;

import l2r.gameserver.handler.IBypassHandler;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2MerchantInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.ExBuyList;
import l2r.gameserver.network.serverpackets.ExBuySellList;

public class Buy implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"MerchantSell",
		"Buy"
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!(target instanceof L2MerchantInstance))
		{
			return false;
		}
		
		try
		{
			if (command.startsWith(COMMANDS[0])) // MerchantSell
			{
				activeChar.sendPacket(new ExBuyList(activeChar));
				activeChar.sendPacket(new ExBuySellList(activeChar, 0, true));
				return true;
			}
			else if (command.startsWith(COMMANDS[1])) // Buy
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				
				if (st.countTokens() < 1)
				{
					return false;
				}
				
				((L2MerchantInstance) target).showBuyWindow(activeChar, Integer.parseInt(st.nextToken()));
				return true;
			}
		}
		catch (Exception e)
		{
			_log.warn("Exception in " + getClass().getSimpleName(), e);
		}
		return false;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
