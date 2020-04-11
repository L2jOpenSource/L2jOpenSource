package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * <b>This class handles Access Level Management commands:</b><br>
 * <br>
 */
public class AdminChangeAccessLevel implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_changelvl"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		handleChangeLevel(command, activeChar);
		
		return true;
	}
	
	/**
	 * @param command
	 * @param activeChar
	 */
	private void handleChangeLevel(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return;
		}
		
		String[] parts = command.split(" ");
		
		if (parts.length == 2)
		{
			final int lvl = Integer.parseInt(parts[1]);
			
			if (activeChar.getTarget() instanceof L2PcInstance)
			{
				((L2PcInstance) activeChar.getTarget()).setAccessLevel(lvl);
				activeChar.sendMessage("You have changed the access level of player " + activeChar.getTarget().getName() + " to " + lvl + " .");
			}
		}
		else if (parts.length == 3)
		{
			final int lvl = Integer.parseInt(parts[2]);
			
			final L2PcInstance player = L2World.getInstance().getPlayer(parts[1]);
			
			if (player != null)
			{
				player.setAccessLevel(lvl);
				activeChar.sendMessage("You have changed the access level of player " + activeChar.getTarget().getName() + " to " + lvl + " .");
			}
		}
		
		parts = null;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
