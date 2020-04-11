/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.network.clientpackets;

import l2r.Config;
import l2r.gameserver.datatables.xml.AdminData;
import l2r.gameserver.handler.AdminCommandHandler;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.util.GMAudit;

/**
 * This class handles all GM commands triggered by //command
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:29 $
 */
public final class SendBypassBuildCmd extends L2GameClientPacket
{
	private static final String _C__74_SENDBYPASSBUILDCMD = "[C] 74 SendBypassBuildCmd";
	
	public static final int GM_MESSAGE = 9;
	public static final int ANNOUNCEMENT = 10;
	
	private String _command;
	
	@Override
	protected void readImpl()
	{
		_command = readS();
		if (_command != null)
		{
			_command = _command.trim();
		}
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		String command = "admin_" + _command.split(" ")[0];
		
		IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(command);
		
		if (ach == null)
		{
			if (activeChar.isGM())
			{
				activeChar.sendMessage("The command " + command.substring(6) + " does not exists!");
			}
			
			_log.warn("No handler registered for admin command '" + command + "'");
			return;
		}
		
		if (!AdminData.getInstance().hasAccess(command, activeChar.getAccessLevel()))
		{
			activeChar.sendMessage("You don't have the access right to use this command!");
			_log.warn("Character " + activeChar.getName() + " tryed to use admin command " + command + ", but have no access to it!");
			return;
		}
		
		if (Config.GMAUDIT)
		{
			GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", _command, (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target"));
		}
		
		ach.useAdminCommand("admin_" + _command, activeChar);
	}
	
	@Override
	public String getType()
	{
		return _C__74_SENDBYPASSBUILDCMD;
	}
}
