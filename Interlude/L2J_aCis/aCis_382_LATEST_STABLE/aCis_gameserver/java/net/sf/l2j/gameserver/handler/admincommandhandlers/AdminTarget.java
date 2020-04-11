package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

/**
 * This class handles following admin commands: - target name = sets player with respective name as target
 */
public class AdminTarget implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_target"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_target"))
			handleTarget(command, activeChar);
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void handleTarget(String command, Player activeChar)
	{
		try
		{
			String targetName = command.substring(13);
			Player obj = World.getInstance().getPlayer(targetName);
			
			if (obj != null)
				obj.onAction(activeChar);
			else
				activeChar.sendPacket(SystemMessageId.CONTACT_CURRENTLY_OFFLINE);
		}
		catch (IndexOutOfBoundsException e)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_CHARACTER_NAME_TRY_AGAIN);
		}
	}
}