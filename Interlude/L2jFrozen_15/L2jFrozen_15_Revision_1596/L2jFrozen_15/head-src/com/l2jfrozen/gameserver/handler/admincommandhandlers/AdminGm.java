package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class handles following admin commands: - gm = turns gm mode on/off
 * @version $Revision: 1.2.4.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminGm implements IAdminCommandHandler
{
	private static Logger LOGGER = Logger.getLogger(AdminGm.class);
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_gm"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.equals("admin_gm"))
		{
			handleGm(activeChar);
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void handleGm(final L2PcInstance activeChar)
	{
		if (activeChar.isGM())
		{
			GmListTable.getInstance().deleteGm(activeChar);
			// activeChar.setIsGM(false);
			
			activeChar.sendMessage("You no longer have GM status.");
			
			if (Config.DEBUG)
			{
				LOGGER.debug("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") turned his GM status off");
			}
		}
		else
		{
			GmListTable.getInstance().addGm(activeChar, false);
			// activeChar.setIsGM(true);
			
			activeChar.sendMessage("You now have GM status.");
			
			if (Config.DEBUG)
			{
				LOGGER.debug("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") turned his GM status on");
			}
		}
	}
}
