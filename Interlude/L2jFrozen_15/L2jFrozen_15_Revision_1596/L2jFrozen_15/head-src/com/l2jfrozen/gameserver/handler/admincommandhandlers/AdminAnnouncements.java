package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.AutoAnnouncementHandler;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.util.Broadcast;

/**
 * This class handles following admin commands: - announce text = announces text to all players - list_announcements = show menu - reload_announcements = reloads announcements from txt file - announce_announcements = announce all stored announcements to all players - add_announcement text = adds
 * text to startup announcements - del_announcement id = deletes announcement with respective id
 * @version $Revision: 1.5 $
 * @author  ProGramMoS
 */
public class AdminAnnouncements implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_list_announcements",
		"admin_reload_announcements",
		"admin_announce_announcements",
		"admin_add_announcement",
		"admin_del_announcement",
		"admin_announce",
		"admin_critannounce",
		"admin_announce_menu",
		"admin_list_autoannouncements",
		"admin_add_autoannouncement",
		"admin_del_autoannouncement",
		"admin_autoannounce"
	};
	
	private enum CommandEnum
	{
		admin_list_announcements,
		admin_reload_announcements,
		admin_announce_announcements,
		admin_add_announcement,
		admin_del_announcement,
		admin_announce,
		admin_critannounce,
		admin_announce_menu,
		admin_list_autoannouncements,
		admin_add_autoannouncement,
		admin_del_autoannouncement,
		admin_autoannounce
	}
	
	@Override
	public boolean useAdminCommand(String command, final L2PcInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		
		final String comm_s = st.nextToken();
		String text = "";
		int index = 0;
		
		CommandEnum comm = CommandEnum.valueOf(comm_s);
		
		if (comm == null)
		{
			return false;
		}
		
		switch (comm)
		{
			case admin_list_announcements:
				Announcements.getInstance().listAnnouncements(activeChar);
				return true;
			case admin_reload_announcements:
				Announcements.getInstance().loadAnnouncements();
				Announcements.getInstance().listAnnouncements(activeChar);
				return true;
			case admin_announce_menu:
				
				if (st.hasMoreTokens())
				{
					text = command.replace(comm_s + " ", "");
				}
				
				if (!text.equals(""))
				{
					Announcements.getInstance().announceToAll(text);
				}
				
				Announcements.getInstance().listAnnouncements(activeChar);
				return true;
			
			case admin_announce_announcements:
				
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					Announcements.getInstance().showAnnouncements(player);
				}
				
				Announcements.getInstance().listAnnouncements(activeChar);
				return true;
			case admin_add_announcement:
				
				if (st.hasMoreTokens())
				{
					text = command.replace(comm_s + " ", "");
				}
				
				if (!text.equals(""))
				{
					Announcements.getInstance().addAnnouncement(text);
					Announcements.getInstance().listAnnouncements(activeChar);
					return true;
				}
				
				activeChar.sendMessage("You cannot announce Empty message");
				return false;
			
			case admin_del_announcement:
				
				if (st.hasMoreTokens())
				{
					final String index_s = st.nextToken();
					
					try
					{
						index = Integer.parseInt(index_s);
					}
					catch (final NumberFormatException e)
					{
						activeChar.sendMessage("Usage: //del_announcement <index> (number >=0)");
					}
				}
				
				if (index >= 0)
				{
					Announcements.getInstance().delAnnouncement(index);
					Announcements.getInstance().listAnnouncements(activeChar);
					return true;
				}
				
				activeChar.sendMessage("Usage: //del_announcement <index> (number >=0)");
				return false;
			case admin_announce:
				// Call method from another class
				if (Config.GM_ANNOUNCER_NAME)
				{
					command = command + " [ " + activeChar.getName() + " ]";
				}
				
				Announcements.getInstance().handleAnnounce(command, 15);
				return true;
			
			case admin_critannounce:
				
				String text1 = command.substring(19);
				if (Config.GM_CRITANNOUNCER_NAME && text1.length() > 0)
				{
					text1 = activeChar.getName() + ": " + text1;
				}
				
				final CreatureSay cs = new CreatureSay(activeChar.getObjectId(), Say2.CRITICAL_ANNOUNCE, "", text1);
				Broadcast.toAllOnlinePlayers(cs);
				return true;
			
			case admin_list_autoannouncements:
				AutoAnnouncementHandler.getInstance().listAutoAnnouncements(activeChar);
				return true;
			
			case admin_add_autoannouncement:
				
				if (st.hasMoreTokens())
				{
					
					int delay = 0;
					
					try
					{
						delay = Integer.parseInt(st.nextToken().trim());
						
					}
					catch (final NumberFormatException e)
					{
						
						activeChar.sendMessage("Usage: //add_autoannouncement <delay> (Seconds > 30) <Announcements>");
						return false;
						
					}
					
					if (st.hasMoreTokens())
					{
						
						String autoAnnounce = st.nextToken();
						
						if (delay > 30)
						{
							while (st.hasMoreTokens())
							{
								autoAnnounce = autoAnnounce + " " + st.nextToken();
							}
							
							AutoAnnouncementHandler.getInstance().registerAnnouncment(autoAnnounce, delay);
							AutoAnnouncementHandler.getInstance().listAutoAnnouncements(activeChar);
							
							return true;
							
						}
						activeChar.sendMessage("Usage: //add_autoannouncement <delay> (Seconds > 30) <Announcements>");
						return false;
					}
					activeChar.sendMessage("Usage: //add_autoannouncement <delay> (Seconds > 30) <Announcements>");
					return false;
				}
				activeChar.sendMessage("Usage: //add_autoannouncement <delay> (Seconds > 30) <Announcements>");
				return false;
			case admin_del_autoannouncement:
				if (st.hasMoreTokens())
				{
					
					try
					{
						index = Integer.parseInt(st.nextToken());
						
					}
					catch (final NumberFormatException e)
					{
						
						activeChar.sendMessage("Usage: //del_autoannouncement <index> (number >= 0)");
						return false;
						
					}
					
					if (index >= 0)
					{
						
						AutoAnnouncementHandler.getInstance().removeAnnouncement(index);
						AutoAnnouncementHandler.getInstance().listAutoAnnouncements(activeChar);
						
					}
					else
					{
						activeChar.sendMessage("Usage: //del_autoannouncement <index> (number >= 0)");
						return false;
						
					}
				}
				else
				{
					activeChar.sendMessage("Usage: //del_autoannouncement <index> (number >= 0)");
					return false;
				}
			case admin_autoannounce:
				AutoAnnouncementHandler.getInstance().listAutoAnnouncements(activeChar);
				return true;
		}
		
		comm = null;
		return false;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}