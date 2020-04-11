package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * Give / Take Status Aio to Player Changes name color and title color if enabled Uses: - setaio [<time_duration in days>] - removeaio
 * @author KhayrusS
 */
public class AdminAio implements IAdminCommandHandler
{
	private final static Logger LOGGER = Logger.getLogger(AdminAio.class);
	private static final SimpleDateFormat SDT = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_setaio",
		"admin_removeaio"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		if (command.startsWith("admin_setaio"))
		{
			// Player MUST BE ONLINE to give AIO
			L2Object target = activeChar.getTarget();
			
			if (target == null)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
				AdminHelpPage.showHelpPage(activeChar, "mods_menu.htm");
				return false;
			}
			else if (!target.isPlayer())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
				AdminHelpPage.showHelpPage(activeChar, "mods_menu.htm");
				return false;
			}
			
			if (!st.hasMoreTokens())
			{
				activeChar.sendMessage("Usage: //setaio [time (in days)]");
				AdminHelpPage.showHelpPage(activeChar, "mods_menu.htm");
				return false;
			}
			
			int days = 0;
			
			try
			{
				days = Integer.parseInt(st.nextToken());
			}
			catch (NumberFormatException e)
			{
				activeChar.sendMessage("Usage: //setaio [NUMBER]");
				AdminHelpPage.showHelpPage(activeChar, "mods_menu.htm");
				return false;
			}
			
			if (target.isPlayer())
			{
				L2PcInstance player = (L2PcInstance) target;
				
				if (player.isOnline())
				{
					giveAio(activeChar, player, days);
				}
			}
		}
		else if (command.startsWith("admin_removeaio"))
		{
			// Player MUST BE ONLINE to remove AIO
			L2Object target = activeChar.getTarget();
			
			if (target == null)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
				AdminHelpPage.showHelpPage(activeChar, "mods_menu.htm");
				return false;
			}
			else if (!target.isPlayer())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
				AdminHelpPage.showHelpPage(activeChar, "mods_menu.htm");
				return false;
			}
			
			if (target.isPlayer())
			{
				L2PcInstance player = (L2PcInstance) target;
				
				if (player.isOnline())
				{
					removeAio(activeChar, player);
				}
			}
		}
		
		return true;
		
	}
	
	public void giveAio(L2PcInstance activeChar, L2PcInstance player, int days)
	{
		if (player == null)
		{
			return;
		}
		
		if (player.isAio())
		{
			activeChar.sendMessage(player.getName() + " is AIO already.");
			AdminHelpPage.showHelpPage(activeChar, "mods_menu.htm");
			return;
		}
		
		if (days > 0)
		{
			long miliseconds = 86400000 * days;
			long endDay = System.currentTimeMillis() + miliseconds;
			player.setAio(true);
			player.setAioEndDate(endDay);
			player.getStat().addExp(player.getStat().getExpForLevel(81));
			player.updateAIOColor();
			player.giveAioSkills();
			player.broadcastUserInfo();
			player.sendPacket(new EtcStatusUpdate(player));
			player.sendSkillList();
			GmListTable.broadcastMessageToGMs("GM " + activeChar.getName() + " set AIO stat for player " + player.getName() + " for " + days + " day(s)");
			LOGGER.info("GM " + activeChar.getName() + " gave to player " + player.getName() + " AIO status that will end at " + SDT.format(new Date(endDay)));
			player.sendMessage("Congratulations you are AIO now.");
			player.broadcastUserInfo();
			
			player.setVariable(L2PcInstance.AIO_END, endDay, true);
		}
		
		AdminHelpPage.showHelpPage(activeChar, "mods_menu.htm");
	}
	
	public void removeAio(L2PcInstance activeChar, L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		if (!player.isAio())
		{
			activeChar.sendMessage(player.getName() + " is not AIO.");
			AdminHelpPage.showHelpPage(activeChar, "mods_menu.htm");
			return;
		}
		
		player.setAio(false);
		player.removeAioSkills();
		player.getAppearance().setNameColor(0xFFFFFF);
		player.getAppearance().setTitleColor(0xFFFFFF);
		player.broadcastUserInfo();
		player.sendPacket(new EtcStatusUpdate(player));
		player.sendSkillList();
		GmListTable.broadcastMessageToGMs("GM " + activeChar.getName() + " remove Aio stat of player " + player.getName());
		player.sendMessage("You lost AIO status.");
		player.broadcastUserInfo();
		
		player.removeVariable(L2PcInstance.AIO_END, true);
		
		AdminHelpPage.showHelpPage(activeChar, "mods_menu.htm");
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}