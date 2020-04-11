package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.managers.CursedWeaponsManager;
import com.l2jfrozen.gameserver.model.CursedWeapon;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

import javolution.text.TextBuilder;

/**
 * This class handles following admin commands:<br>
 * - cw_info = displays cursed weapon status.<br>
 * - cw_remove = removes a cursed weapon from the world, item id or name must be provided.<br>
 * - cw_add = adds a cursed weapon into the world, item id or name must be provided, the target will be the wielder.<br>
 * - cw_goto = teleports GM to the specified cursed weapon.<br>
 * - cw_reload = reloads instance manager.
 * @author ProGramMoS, Zoey76
 */
public class AdminCursedWeapons implements IAdminCommandHandler
{
	private static final CursedWeaponsManager cursedWeaponsManager = CursedWeaponsManager.getInstance();
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_cw_info",
		"admin_cw_remove",
		"admin_cw_goto",
		"admin_cw_reload",
		"admin_cw_add",
		"admin_cw_info_menu"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		if (command.equalsIgnoreCase("admin_cw_info"))
		{
			activeChar.sendMessage("====== Cursed Weapons: ======");
			
			for (final CursedWeapon cw : cursedWeaponsManager.getCursedWeapons())
			{
				activeChar.sendMessage("> " + cw.getName() + " (" + cw.getItemId() + ")");
				
				if (cw.isActivated())
				{
					L2PcInstance pl = cw.getPlayer();
					activeChar.sendMessage("  Player holding: " + (pl == null ? "null" : pl.getName()));
					activeChar.sendMessage("    Player karma: " + cw.getPlayerKarma());
					activeChar.sendMessage("    Time Remaining: " + (cw.getTimeLeft() / 60000) + " min.");
					activeChar.sendMessage("    Kills : " + cw.getNbKills());
					pl = null;
				}
				else if (cw.isDropped())
				{
					activeChar.sendMessage("  Lying on the ground.");
					activeChar.sendMessage("    Time Remaining: " + (cw.getTimeLeft() / 60000) + " min.");
					activeChar.sendMessage("    Kills : " + cw.getNbKills());
				}
				else
				{
					activeChar.sendMessage("  Don't exist in the world.");
				}
				
				activeChar.sendPacket(new SystemMessage(SystemMessageId.FRIEND_LIST_FOOT));
			}
		}
		else if (command.equalsIgnoreCase("admin_cw_info_menu"))
		{
			final TextBuilder replyMSG = new TextBuilder();
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile("data/html/admin/cwinfo.htm");
			
			for (final CursedWeapon cw : cursedWeaponsManager.getCursedWeapons())
			{
				final int itemId = cw.getItemId();
				replyMSG.append("<table width=270><tr><td>Name:</td><td>" + cw.getName() + "</td></tr>");
				
				if (cw.isActivated())
				{
					final L2PcInstance pl = cw.getPlayer();
					replyMSG.append("<tr><td>Weilder:</td><td>" + (pl == null ? "null" : pl.getName()) + "</td></tr>");
					replyMSG.append("<tr><td>Karma:</td><td>" + String.valueOf(cw.getPlayerKarma()) + "</td></tr>");
					replyMSG.append("<tr><td>Kills:</td><td>" + String.valueOf(cw.getPlayerPkKills()) + "/" + String.valueOf(cw.getNbKills()) + "</td></tr>");
					replyMSG.append("<tr><td>Time remaining:</td><td>" + String.valueOf(cw.getTimeLeft() / 60000) + " min.</td></tr>");
					replyMSG.append("<tr><td><button value=\"Remove\" action=\"bypass -h admin_cw_remove " + String.valueOf(itemId) + "\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
					replyMSG.append("<td><button value=\"Go\" action=\"bypass -h admin_cw_goto " + String.valueOf(itemId) + "\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
				}
				else if (cw.isDropped())
				{
					replyMSG.append("<tr><td>Position:</td><td>Lying on the ground</td></tr>");
					replyMSG.append("<tr><td>Time remaining:</td><td>" + String.valueOf(cw.getTimeLeft() / 60000) + " min.</td></tr>");
					replyMSG.append("<tr><td>Kills:</td><td>" + String.valueOf(cw.getNbKills()) + "</td></tr>");
					replyMSG.append("<tr><td><button value=\"Remove\" action=\"bypass -h admin_cw_remove " + String.valueOf(itemId) + "\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
					replyMSG.append("<td><button value=\"Go\" action=\"bypass -h admin_cw_goto " + String.valueOf(itemId) + "\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
				}
				else
				{
					replyMSG.append("<tr><td>Position:</td><td>Doesn't exist.</td></tr>");
					replyMSG.append("<tr><td><button value=\"Give to Target\" action=\"bypass -h admin_cw_add " + String.valueOf(itemId) + "\" width=99 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td></td></tr>");
				}
				
				replyMSG.append("</table>");
				replyMSG.append("<br>");
			}
			
			adminReply.replace("%cwinfo%", replyMSG.toString());
			activeChar.sendPacket(adminReply);
		}
		else if (command.equalsIgnoreCase("admin_cw_reload"))
		{
			cursedWeaponsManager.reload();
		}
		else if (command.startsWith("admin_cw_remove"))
		{
			if (!st.hasMoreElements())
			{
				activeChar.sendMessage("Not enough parameters!");
				return false;
			}
			
			String parameter = st.nextToken();
			int id = 0;
			if (parameter.matches("[0-9]*"))
			{
				id = Integer.parseInt(parameter);
			}
			else
			{
				parameter = parameter.replace('_', ' ');
				for (final CursedWeapon cwp : cursedWeaponsManager.getCursedWeapons())
				{
					if (cwp.getName().toLowerCase().contains(parameter.toLowerCase()))
					{
						id = cwp.getItemId();
						break;
					}
				}
			}
			
			if (cursedWeaponsManager.isCursed(id))
			{
				cursedWeaponsManager.getCursedWeapon(id).endOfLife();
			}
			else
			{
				activeChar.sendMessage("Wrong Cursed Weapon Id!");
			}
		}
		else if (command.startsWith("admin_cw_goto"))
		{
			if (!st.hasMoreElements())
			{
				activeChar.sendMessage("Not enough parameters!");
				return false;
			}
			
			String parameter = st.nextToken();
			int id = 0;
			if (parameter.matches("[0-9]*"))
			{
				id = Integer.parseInt(parameter);
			}
			else
			{
				parameter = parameter.replace('_', ' ');
				for (final CursedWeapon cwp : cursedWeaponsManager.getCursedWeapons())
				{
					if (cwp.getName().toLowerCase().contains(parameter.toLowerCase()))
					{
						id = cwp.getItemId();
						break;
					}
				}
			}
			
			if (cursedWeaponsManager.isCursed(id))
			{
				cursedWeaponsManager.getCursedWeapon(id).goTo(activeChar);
			}
			else
			{
				activeChar.sendMessage("Wrong Cursed Weapon Id!");
			}
		}
		else if (command.startsWith("admin_cw_add"))
		{
			if (!st.hasMoreElements())
			{
				activeChar.sendMessage("Not enough parameters!");
				return false;
			}
			
			String parameter = st.nextToken();
			int id = 0;
			if (parameter.matches("[0-9]*"))
			{
				id = Integer.parseInt(parameter);
			}
			else
			{
				parameter = parameter.replace('_', ' ');
				
				for (final CursedWeapon cwp : cursedWeaponsManager.getCursedWeapons())
				{
					if (cwp.getName().toLowerCase().contains(parameter.toLowerCase()))
					{
						id = cwp.getItemId();
						break;
					}
				}
			}
			
			if (cursedWeaponsManager.isCursed(id))
			{
				final CursedWeapon cursedWeapon = cursedWeaponsManager.getCursedWeapon(id);
				if (cursedWeapon.isActive())
				{
					activeChar.sendMessage("This Cursed Weapon is already active!");
				}
				else
				{
					// end time is equal to dropped one
					final long endTime = System.currentTimeMillis() + cursedWeapon.getDuration() * 60000L;
					cursedWeapon.setEndTime(endTime);
					
					final L2Object target = activeChar.getTarget();
					if ((target != null) && (target instanceof L2PcInstance))
					{
						((L2PcInstance) target).addItem("AdminCursedWeaponAdd", id, 1, target, true);
					}
					else
					{
						activeChar.addItem("AdminCursedWeaponAdd", id, 1, activeChar, true);
					}
				}
			}
			else
			{
				activeChar.sendMessage("Wrong Cursed Weapon Id!");
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
