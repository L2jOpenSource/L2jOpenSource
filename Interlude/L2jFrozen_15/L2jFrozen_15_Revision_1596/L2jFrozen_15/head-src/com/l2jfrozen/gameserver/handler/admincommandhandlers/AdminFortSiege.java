package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.managers.FortManager;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Fort;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

import javolution.text.TextBuilder;

/**
 * This class handles all siege commands: Todo: change the class name, and neaten it up
 * @author programmos
 */
public class AdminFortSiege implements IAdminCommandHandler
{
	// private static Logger LOGGER = Logger.getLogger(AdminFortSiege.class);
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_fortsiege",
		"admin_add_fortattacker",
		"admin_add_fortdefender",
		"admin_add_fortguard",
		"admin_list_fortsiege_clans",
		"admin_clear_fortsiege_list",
		"admin_move_fortdefenders",
		"admin_spawn_fortdoors",
		"admin_endfortsiege",
		"admin_startfortsiege",
		"admin_setfort",
		"admin_removefort"
	};
	
	@Override
	public boolean useAdminCommand(String command, final L2PcInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		command = st.nextToken(); // Get actual command
		
		// Get fort
		Fort fort = null;
		
		if (st.hasMoreTokens())
		{
			fort = FortManager.getInstance().getFort(st.nextToken());
		}
		
		// No fort specified
		if (fort == null || fort.getFortId() < 0)
		{
			showFortSelectPage(activeChar);
		}
		else
		{
			L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			
			if (target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			
			if (command.equalsIgnoreCase("admin_add_fortattacker"))
			{
				if (player == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				}
				else
				{
					fort.getSiege().registerAttacker(player, true);
				}
			}
			else if (command.equalsIgnoreCase("admin_add_fortdefender"))
			{
				if (player == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				}
				else
				{
					fort.getSiege().registerDefender(player, true);
				}
			}
			else if (command.equalsIgnoreCase("admin_clear_fortsiege_list"))
			{
				fort.getSiege().clearSiegeClan();
			}
			else if (command.equalsIgnoreCase("admin_endfortsiege"))
			{
				fort.getSiege().endSiege();
			}
			else if (command.equalsIgnoreCase("admin_list_fortsiege_clans"))
			{
				fort.getSiege().listRegisterClan(activeChar);
				return true;
			}
			else if (command.equalsIgnoreCase("admin_move_fortdefenders"))
			{
				activeChar.sendMessage("Not implemented yet.");
			}
			else if (command.equalsIgnoreCase("admin_setfort"))
			{
				if (player == null || player.getClan() == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				}
				else
				{
					fort.setOwner(player.getClan());
				}
			}
			else if (command.equalsIgnoreCase("admin_removefort"))
			{
				L2Clan clan = ClanTable.getInstance().getClan(fort.getOwnerId());
				
				if (clan != null)
				{
					fort.removeOwner(clan);
				}
				else
				{
					activeChar.sendMessage("Unable to remove fort");
				}
				
				clan = null;
			}
			else if (command.equalsIgnoreCase("admin_spawn_fortdoors"))
			{
				fort.spawnDoor();
			}
			else if (command.equalsIgnoreCase("admin_startfortsiege"))
			{
				fort.getSiege().startSiege();
			}
			
			showFortSiegePage(activeChar, fort.getName());
		}
		return true;
	}
	
	private void showFortSelectPage(final L2PcInstance activeChar)
	{
		int i = 0;
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/forts.htm");
		TextBuilder cList = new TextBuilder();
		
		for (final Fort fort : FortManager.getInstance().getForts())
		{
			if (fort != null)
			{
				String name = fort.getName();
				cList.append("<td fixwidth=90><a action=\"bypass -h admin_fortsiege " + name + "\">" + name + "</a></td>");
				name = null;
				i++;
			}
			
			if (i > 2)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}
		
		adminReply.replace("%forts%", cList.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showFortSiegePage(final L2PcInstance activeChar, final String fortName)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/fort.htm");
		adminReply.replace("%fortName%", fortName);
		activeChar.sendPacket(adminReply);
		
		adminReply = null;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
}
