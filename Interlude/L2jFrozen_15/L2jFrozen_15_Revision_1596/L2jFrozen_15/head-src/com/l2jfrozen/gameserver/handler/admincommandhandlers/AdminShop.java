package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.controllers.TradeController;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2TradeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.BuyList;

/**
 * This class handles following admin commands: - gmshop = shows menu - buy id = shows shop with respective id
 * @version $Revision: 1.2.4.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminShop implements IAdminCommandHandler
{
	private static Logger LOGGER = Logger.getLogger(AdminShop.class);
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_buy",
		"admin_gmshop"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.startsWith("admin_buy"))
		{
			try
			{
				handleBuyRequest(activeChar, command.substring(10));
			}
			catch (final IndexOutOfBoundsException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				activeChar.sendMessage("Please specify buylist.");
			}
		}
		else if (command.equals("admin_gmshop"))
		{
			AdminHelpPage.showHelpPage(activeChar, "gmshops.htm");
		}
		
		return true;
	}
	
	private void handleBuyRequest(final L2PcInstance activeChar, final String command)
	{
		int val = -1;
		
		try
		{
			val = Integer.parseInt(command);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("admin buylist failed:" + command);
		}
		
		L2TradeList list = TradeController.getInstance().getBuyList(val);
		
		if (list != null)
		{
			activeChar.sendMessage("GM BUYLIST SHOP ID: " + val);
			activeChar.sendPacket(new BuyList(list, activeChar.getAdena()));
			
			if (Config.DEBUG)
			{
				LOGGER.debug("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") opened GM shop id " + val);
			}
		}
		else
		{
			LOGGER.warn("no buylist with id:" + val);
		}
		
		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
