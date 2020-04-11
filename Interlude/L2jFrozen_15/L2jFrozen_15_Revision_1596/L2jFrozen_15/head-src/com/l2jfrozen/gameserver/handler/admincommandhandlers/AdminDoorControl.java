package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import com.l2jfrozen.gameserver.datatables.csv.DoorTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;

/**
 * This class handles following admin commands:<br>
 * - open1 = open coloseum door 24190001<br>
 * - open2 = open coloseum door 24190002<br>
 * - open3 = open coloseum door 24190003<br>
 * - open4 = open coloseum door 24190004<br>
 * - openall = open all coloseum door<br>
 * - close1 = close coloseum door 24190001<br>
 * - close2 = close coloseum door 24190002<br>
 * - close3 = close coloseum door 24190003<br>
 * - close4 = close coloseum door 24190004<br>
 * - closeall = close all coloseum door<br>
 * <br>
 * - open = open selected door<br>
 * - close = close selected door<br>
 * @version $Revision: 1.3 $
 * @author  ProGramMoS
 */
public class AdminDoorControl implements IAdminCommandHandler
{
	// private static Logger LOGGER = Logger.getLogger(AdminDoorControl.class);
	private static DoorTable doorTable;
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_open",
		"admin_close",
		"admin_openall",
		"admin_closeall"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		doorTable = DoorTable.getInstance();
		
		L2Object target2 = null;
		
		if (command.startsWith("admin_close ")) // id
		{
			try
			{
				final int doorId = Integer.parseInt(command.substring(12));
				
				if (doorTable.getDoor(doorId) != null)
				{
					doorTable.getDoor(doorId).closeMe();
				}
				else
				{
					for (final Castle castle : CastleManager.getInstance().getCastles())
					{
						if (castle.getDoor(doorId) != null)
						{
							castle.getDoor(doorId).closeMe();
						}
					}
				}
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Wrong ID door.");
				e.printStackTrace();
				return false;
			}
		}
		else if (command.equals("admin_close")) // target
		{
			target2 = activeChar.getTarget();
			
			if (target2 instanceof L2DoorInstance)
			{
				((L2DoorInstance) target2).closeMe();
			}
			else
			{
				activeChar.sendMessage("Incorrect target.");
			}
			
			target2 = null;
		}
		else if (command.startsWith("admin_open ")) // id
		{
			try
			{
				final int doorId = Integer.parseInt(command.substring(11));
				
				if (doorTable.getDoor(doorId) != null)
				{
					doorTable.getDoor(doorId).openMe();
				}
				else
				{
					for (final Castle castle : CastleManager.getInstance().getCastles())
					{
						if (castle.getDoor(doorId) != null)
						{
							castle.getDoor(doorId).openMe();
						}
					}
				}
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Wrong ID door.");
				e.printStackTrace();
				return false;
			}
		}
		else if (command.equals("admin_open")) // target
		{
			target2 = activeChar.getTarget();
			
			if (target2 instanceof L2DoorInstance)
			{
				((L2DoorInstance) target2).openMe();
			}
			else
			{
				activeChar.sendMessage("Incorrect target.");
			}
			
			target2 = null;
		}
		
		// need optimize cycle
		// set limits on the ID doors that do not cycle to close doors
		else if (command.equals("admin_closeall"))
		{
			try
			{
				for (final L2DoorInstance door : doorTable.getDoors())
				{
					door.closeMe();
				}
				
				for (final Castle castle : CastleManager.getInstance().getCastles())
				{
					for (final L2DoorInstance door : castle.getDoors())
					{
						door.closeMe();
					}
				}
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
		else if (command.equals("admin_openall"))
		{
			// need optimize cycle
			// set limits on the PH door to do a cycle of opening doors.
			try
			{
				for (final L2DoorInstance door : doorTable.getDoors())
				{
					door.openMe();
				}
				
				for (final Castle castle : CastleManager.getInstance().getCastles())
				{
					for (final L2DoorInstance door : castle.getDoors())
					{
						door.openMe();
					}
				}
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				return false;
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
