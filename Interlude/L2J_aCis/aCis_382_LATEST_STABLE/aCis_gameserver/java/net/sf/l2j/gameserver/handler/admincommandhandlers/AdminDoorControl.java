package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.data.xml.DoorData;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.network.SystemMessageId;

/**
 * This class handles following admin commands
 * <ul>
 * <li>open = open a door using a doorId, or a targeted door if not found.</li>
 * <li>close = close a door using a doorId, or a targeted door if not found.</li>
 * <li>openall = open all doors registered on doors.xml.</li>
 * <li>closeall = close all doors registered on doors.xml.</li>
 * </ul>
 */
public class AdminDoorControl implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_open",
		"admin_close",
		"admin_openall",
		"admin_closeall"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_open"))
		{
			if (command.equals("admin_openall"))
			{
				for (Door door : DoorData.getInstance().getDoors())
					door.openMe();
			}
			else
			{
				try
				{
					final Door door = DoorData.getInstance().getDoor(Integer.parseInt(command.substring(11)));
					if (door != null)
						door.openMe();
					else
						activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
				catch (Exception e)
				{
					final WorldObject target = activeChar.getTarget();
					
					if (target instanceof Door)
						((Door) target).openMe();
					else
						activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
			}
		}
		else if (command.startsWith("admin_close"))
		{
			if (command.equals("admin_closeall"))
			{
				for (Door door : DoorData.getInstance().getDoors())
					door.closeMe();
			}
			else
			{
				try
				{
					final Door door = DoorData.getInstance().getDoor(Integer.parseInt(command.substring(12)));
					if (door != null)
						door.closeMe();
					else
						activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
				catch (Exception e)
				{
					final WorldObject target = activeChar.getTarget();
					
					if (target instanceof Door)
						((Door) target).closeMe();
					else
						activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
			}
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}