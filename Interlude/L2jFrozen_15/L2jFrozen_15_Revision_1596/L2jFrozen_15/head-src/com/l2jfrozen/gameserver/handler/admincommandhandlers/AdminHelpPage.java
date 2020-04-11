package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * This class handles following admin commands: - help path = shows /data/html/admin/path file to char, should not be used by GM's directly
 * @version $Revision: 1.2.4.3 $ $Date: 2005/04/11 10:06:02 $
 */
public class AdminHelpPage implements IAdminCommandHandler
{
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_help"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.startsWith("admin_help"))
		{
			try
			{
				String val = command.substring(11);
				showHelpPage(activeChar, val);
				val = null;
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				// case of empty filename
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
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
	
	public static void showHelpPage(L2PcInstance targetChar, String filename)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/" + filename);
		targetChar.sendPacket(adminReply);
	}
	
	public static void showSubMenuPage(L2PcInstance targetChar, String filename)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/" + filename);
		targetChar.sendPacket(adminReply);
	}
}
