package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * @author L2jFrozen
 */
public class AdminNoble implements IAdminCommandHandler
{
	protected static final Logger LOGGER = Logger.getLogger(AdminNoble.class);
	
	private static String[] ADMIN_COMMANDS =
	{
		"admin_setnoble"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (command.startsWith("admin_setnoble"))
		{
			L2Object target = activeChar.getTarget();
			
			if (target == null)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
				return false;
			}
			
			if (target instanceof L2PcInstance)
			{
				L2PcInstance targetPlayer = (L2PcInstance) target;
				
				final boolean newNoble = !targetPlayer.isNoble();
				
				if (newNoble)
				{
					targetPlayer.setNoble(true);
					targetPlayer.sendMessage("You are now a noblesse.");
				}
				else
				{
					targetPlayer.setNoble(false);
					targetPlayer.sendMessage("You are no longer a noblesse.");
				}
				
				targetPlayer = null;
			}
			else
			{
				activeChar.sendMessage("Impossible to set a non Player Target as noble.");
				LOGGER.info("GM: " + activeChar.getName() + " is trying to set a non Player Target as noble.");
				return false;
			}
			target = null;
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
