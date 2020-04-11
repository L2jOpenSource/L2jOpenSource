package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - target name = sets player with respective name as target
 * @version $Revision: 1.2.4.3 $ $Date: 2005/04/11 10:05:56 $
 */
public class AdminTarget implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_target"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.startsWith("admin_target"))
		{
			handleTarget(command, activeChar);
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void handleTarget(final String command, final L2PcInstance activeChar)
	{
		try
		{
			String targetName = command.substring(13);
			L2Object obj = L2World.getInstance().getPlayer(targetName);
			
			if (obj != null && obj instanceof L2PcInstance)
			{
				obj.onAction(activeChar);
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Player " + targetName + " not found");
				activeChar.sendPacket(sm);
			}
			
			obj = null;
			targetName = null;
		}
		catch (final IndexOutOfBoundsException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Please specify correct name.");
			activeChar.sendPacket(sm);
			sm = null;
		}
	}
}
