package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands:<br>
 * admin|admin1/admin2/admin3/admin4/admin5 = slots for the 5 starting admin menus <br>
 * gmliston/gmlistoff = includes/excludes active character from /gmlist results <br>
 * silence = toggles private messages acceptance mode <br>
 * diet = toggles weight penalty mode <br>
 * tradeoff = toggles trade acceptance mode <br>
 * set/set_menu/set_mod = alters specified server setting <br>
 * saveolymp = saves olympiad state manually <br>
 * manualhero = cycles olympiad and calculate new heroes.
 * @author ProGramMoS
 */
public class AdminAdmin implements IAdminCommandHandler
{
	private static Logger LOGGER = Logger.getLogger(AdminAdmin.class);
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_admin",
		"admin_admin1",
		"admin_admin2",
		"admin_admin3",
		"admin_admin4",
		"admin_admin5",
		"admin_gmliston",
		"admin_gmlistoff",
		"admin_silence",
		"admin_diet",
		"admin_saveolymp",
		"admin_manualhero"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command == null)
		{
			return false;
		}
		
		if (activeChar == null)
		{
			return false;
		}
		
		String comm = new StringTokenizer(command).nextToken();
		
		switch (comm)
		{
			case "admin_admin":
			case "admin_admin1":
			case "admin_admin2":
			case "admin_admin3":
			case "admin_admin4":
			case "admin_admin5":
				showMainPage(activeChar, command);
				return true;
			
			case "admin_gmliston":
				GmListTable.getInstance().showGm(activeChar);
				activeChar.sendMessage("Registerd into gm list");
				return true;
			
			case "admin_gmlistoff":
				GmListTable.getInstance().hideGm(activeChar);
				activeChar.sendMessage("Removed from gm list");
				return true;
			
			case "admin_silence":
				if (activeChar.getMessageRefusal()) // already in message refusal mode
				{
					activeChar.setMessageRefusal(false);
					activeChar.sendPacket(new SystemMessage(SystemMessageId.MESSAGE_ACCEPTANCE_MODE));
				}
				else
				{
					activeChar.setMessageRefusal(true);
					activeChar.sendPacket(new SystemMessage(SystemMessageId.MESSAGE_REFUSAL_MODE));
				}
				return true;
			
			case "admin_saveolymp":
				
				Olympiad.getInstance().saveOlympiadStatus();
				activeChar.sendMessage("Olympiad stuff saved!");
				
				return true;
			
			case "admin_manualhero":
				try
				{
					Olympiad.getInstance().manualSelectHeroes();
				}
				catch (Exception e)
				{
					LOGGER.error("AdminAdmin.useAdminCommand case 'admin_manualhero', something went wrong", e);
				}
				
				activeChar.sendMessage("Heroes formed!");
				return true;
			
			case "admin_diet":
				boolean diet = !activeChar.getDietMode();
				activeChar.setDietMode(diet);
				activeChar.sendMessage("Diet mode " + (diet ? "on" : "off"));
				activeChar.refreshOverloaded();
				AdminHelpPage.showHelpPage(activeChar, "main_menu.htm");
				return true;
			
			default:
				return false;
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void showMainPage(L2PcInstance activeChar, String command)
	{
		if (activeChar == null)
		{
			return;
		}
		
		int mode = 0;
		String filename = null;
		
		if (command != null && command.length() > 11)
		{
			String mode_s = command.substring(11);
			
			try
			{
				mode = Integer.parseInt(mode_s);
			}
			catch (NumberFormatException e)
			{
				LOGGER.error("AdminAdmin.showMainPage : Impossible to parse to integer the string " + mode_s, e);
			}
		}
		
		switch (mode)
		{
			case 1:
				filename = "main";
				break;
			case 2:
				filename = "game";
				break;
			case 3:
				filename = "effects";
				break;
			case 4:
				filename = "server";
				break;
			case 5:
				filename = "mods";
				break;
			default:
				if (Config.GM_ADMIN_MENU_STYLE.equals("modern"))
				{
					filename = "main";
				}
				else
				{
					filename = "classic";
				}
				break;
		}
		
		AdminHelpPage.showHelpPage(activeChar, filename + "_menu.htm");
	}
}
