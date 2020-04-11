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
package handlers.admincommandhandlers;

import l2r.gameserver.data.xml.impl.BuyListData;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.buylist.L2BuyList;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.ExBuyList;
import l2r.gameserver.network.serverpackets.ExBuySellList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles following admin commands:
 * <ul>
 * <li>gmshop = shows menu</li>
 * <li>buy id = shows shop with respective id</li>
 * </ul>
 */
public class AdminShop implements IAdminCommandHandler
{
	private static final Logger _log = LoggerFactory.getLogger(AdminShop.class);
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_buy",
		"admin_gmshop"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_buy"))
		{
			try
			{
				handleBuyRequest(activeChar, command.substring(10));
			}
			catch (IndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Please specify buylist.");
			}
		}
		else if (command.equals("admin_gmshop"))
		{
			AdminHtml.showAdminHtml(activeChar, "gmshops.htm");
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void handleBuyRequest(L2PcInstance activeChar, String command)
	{
		int val = -1;
		try
		{
			val = Integer.parseInt(command);
		}
		catch (Exception e)
		{
			_log.warn("admin buylist failed:" + command);
		}
		
		L2BuyList buyList = BuyListData.getInstance().getBuyList(val);
		
		if (buyList != null)
		{
			activeChar.sendPacket(new ExBuyList(buyList, activeChar, 0));
			activeChar.sendPacket(new ExBuySellList(activeChar, 0, false));
		}
		else
		{
			_log.warn("no buylist with id:" + val);
		}
		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
