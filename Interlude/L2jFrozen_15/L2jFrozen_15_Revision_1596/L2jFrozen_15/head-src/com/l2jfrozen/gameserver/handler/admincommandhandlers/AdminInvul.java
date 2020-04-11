package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class handles following admin commands:<br>
 * invul = turns invulnerability on/off
 */
public class AdminInvul implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_invul",
		"admin_setinvul"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command == null)
		{
			return false;
		}
		
		if (activeChar == null)
		{
			return false;
		}
		
		if (command.equals("admin_invul"))
		{
			handleInvul(activeChar);
			AdminHelpPage.showHelpPage(activeChar, "main_menu.htm");
		}
		else if (command.equals("admin_setinvul"))
		{
			L2Object target = activeChar.getTarget();
			
			if (target == null)
			{
				return false;
			}
			
			if (target instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) target;
				handleInvul(player);
			}
		}
		
		return true;
	}
	
	private void handleInvul(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		boolean invul = !player.isInvul();
		player.setIsInvul(invul);
		player.sendMessage(player.getName() + "is now " + (invul ? "inmmortal" : "mortal"));
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
