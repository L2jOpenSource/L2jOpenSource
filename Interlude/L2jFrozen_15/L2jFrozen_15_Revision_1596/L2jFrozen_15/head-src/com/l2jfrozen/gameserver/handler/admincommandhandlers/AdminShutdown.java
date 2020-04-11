package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.Shutdown;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * This class handles following admin commands: - server_shutdown [sec] = shows menu or shuts down server in sec seconds
 * @version $Revision: 1.5.2.1.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminShutdown implements IAdminCommandHandler
{
	// private static Logger LOGGER = Logger.getLogger(AdminShutdown.class);
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_server_shutdown",
		"admin_server_restart",
		"admin_server_abort"
	};
	
	private enum CommandEnum
	{
		admin_server_shutdown,
		admin_server_restart,
		admin_server_abort
	}
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		
		final CommandEnum comm = CommandEnum.valueOf(st.nextToken());
		
		if (comm == null)
		{
			return false;
		}
		
		switch (comm)
		{
			case admin_server_shutdown:
			{
				
				if (st.hasMoreTokens())
				{
					
					final String secs = st.nextToken();
					
					try
					{
						final int val = Integer.parseInt(secs);
						
						if (val >= 0)
						{
							serverShutdown(activeChar, val, false);
							return true;
						}
						activeChar.sendMessage("Negative Value is not allowed");
						return false;
					}
					catch (final StringIndexOutOfBoundsException e)
					{
						sendHtmlForm(activeChar);
						return false;
					}
					
				}
				sendHtmlForm(activeChar);
				return false;
				
			}
			case admin_server_restart:
			{
				if (st.hasMoreTokens())
				{
					String secs = st.nextToken();
					
					try
					{
						final int val = Integer.parseInt(secs);
						
						if (val >= 0)
						{
							serverShutdown(activeChar, val, true);
							return true;
						}
						activeChar.sendMessage("Negative Value is not allowed");
						return false;
					}
					catch (final StringIndexOutOfBoundsException e)
					{
						sendHtmlForm(activeChar);
						return false;
					}
				}
				sendHtmlForm(activeChar);
				return false;
			}
			case admin_server_abort:
			{
				
				serverAbort(activeChar);
				return true;
			}
		}
		return false;
		
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void sendHtmlForm(final L2PcInstance activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		final int t = GameTimeController.getInstance().getGameTime();
		final int h = t / 60;
		final int m = t % 60;
		
		SimpleDateFormat format = new SimpleDateFormat("h:mm a");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, m);
		adminReply.setFile("data/html/admin/shutdown.htm");
		adminReply.replace("%count%", String.valueOf(L2World.getAllPlayersCount()));
		adminReply.replace("%used%", String.valueOf(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		adminReply.replace("%xp%", String.valueOf(Config.RATE_XP));
		adminReply.replace("%sp%", String.valueOf(Config.RATE_SP));
		adminReply.replace("%adena%", String.valueOf(Config.RATE_DROP_ADENA));
		adminReply.replace("%drop%", String.valueOf(Config.RATE_DROP_ITEMS));
		adminReply.replace("%time%", String.valueOf(format.format(cal.getTime())));
		activeChar.sendPacket(adminReply);
		
		adminReply = null;
		format = null;
		cal = null;
	}
	
	private void serverShutdown(final L2PcInstance activeChar, final int seconds, final boolean restart)
	{
		Shutdown.getInstance().startShutdown(activeChar, seconds, restart);
	}
	
	private void serverAbort(final L2PcInstance activeChar)
	{
		Shutdown.getInstance().abort(activeChar);
	}
	
}
