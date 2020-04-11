package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2jfrozen.gameserver.handler.AdminCommandHandler;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.util.GMAudit;

import main.EngineModsManager;

/**
 * This class handles all GM commands triggered by //command
 */
public final class SendBypassBuildCmd extends L2GameClientPacket
{
	protected static final Logger LOGGER = Logger.getLogger(SendBypassBuildCmd.class);
	public final static int GM_MESSAGE = 9;
	public final static int ANNOUNCEMENT = 10;
	
	private String command;
	
	@Override
	protected void readImpl()
	{
		command = "admin_" + readS().trim();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		else if (!activeChar.isGM())
		{
			LOGGER.warn("Player " + activeChar.getName() + " tried to send builder command (ADM command) without being GM!");
			return;
		}
		
		if (EngineModsManager.onVoiced(activeChar, "admin_" + command))
		{
			return;
		}
		
		// Checks The Access and notify requester if requester access it not allowed for that command
		if (!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel()))
		{
			activeChar.sendMessage("You don't have the access right to use this command!");
			LOGGER.warn("Character " + activeChar.getName() + " tried to use admin command " + command + ", but doesn't have access to it!");
			return;
		}
		
		// gets the Handler of That Commmand
		final IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(command);
		
		// if handler is valid we Audit and use else we notify in console.
		if (ach != null)
		{
			if (Config.GMAUDIT)
			{
				GMAudit.auditGMAction(activeChar.getName() + "_" + activeChar.getObjectId(), command, activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target");
			}
			
			ach.useAdminCommand(command, activeChar);
		}
		else
		{
			activeChar.sendMessage("The command " + command + " doesn't exists!");
			LOGGER.warn("No handler registered for admin command '" + command + "'");
			return;
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 5b SendBypassBuildCmd";
	}
}
