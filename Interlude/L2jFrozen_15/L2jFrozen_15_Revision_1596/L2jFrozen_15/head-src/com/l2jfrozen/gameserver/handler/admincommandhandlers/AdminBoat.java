package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2BoatInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public class AdminBoat implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_boat"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		L2BoatInstance boat = activeChar.getBoat();
		
		if (boat == null)
		{
			activeChar.sendMessage("Usage only possible while riding a boat.");
			return false;
		}
		
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		if (st.hasMoreTokens())
		{
			final String cmd = st.nextToken();
			if (cmd.equals("cycle"))
			{
				if (boat.isInCycle())
				{
					boat.stopCycle();
					activeChar.sendMessage("Boat cycle stopped.");
				}
				else
				{
					boat.startCycle();
					activeChar.sendMessage("Boat cycle started.");
				}
			}
			else if (cmd.equals("reload"))
			{
				boat.reloadPath();
				activeChar.sendMessage("Boat path reloaded.");
			}
			else
			{
				showUsage(activeChar);
			}
		}
		else
		{
			activeChar.sendMessage("====== Boat Information ======");
			activeChar.sendMessage("Name: " + boat.getBoatName() + " (" + boat.getId() + ") ObjId: " + boat.getObjectId());
			activeChar.sendMessage("Cycle: " + boat.isInCycle() + " (" + boat.getCycle() + ")");
			activeChar.sendMessage("Players inside: " + boat.getSizeInside());
			activeChar.sendMessage("Position: " + boat.getX() + " " + boat.getY() + " " + boat.getZ() + " " + boat.getPosition().getHeading());
			activeChar.sendMessage("==============================");
		}
		
		st = null;
		boat = null;
		
		return true;
	}
	
	private void showUsage(final L2PcInstance cha)
	{
		cha.sendMessage("Usage: //boat [cycle|reload]");
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
