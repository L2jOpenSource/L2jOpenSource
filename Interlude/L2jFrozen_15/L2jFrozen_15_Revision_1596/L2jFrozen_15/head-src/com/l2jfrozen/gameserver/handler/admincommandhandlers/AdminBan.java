package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ServerClose;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.thread.LoginServerThread;
import com.l2jfrozen.gameserver.util.GMAudit;
import com.l2jfrozen.logs.Log;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class handles following admin commands: - ban_acc <account_name> = changes account access level to -100 and logs him off. If no account is specified target's account is used. - ban_char <char_name> = changes a characters access level to -100 and logs him off. If no character is specified
 * target is used. - ban_chat <char_name> <duration> = chat bans a character for the specified duration. If no name is specified the target is chat banned indefinitely. - unban_acc <account_name> = changes account access level to 0. - unban_char <char_name> = changes specified characters access
 * level to 0. - unban_chat <char_name> = lifts chat ban from specified player. If no player name is specified current target is used. - jail charname [penalty_time] = jails character. Time specified in minutes. For ever if no time is specified. - unjail charname = Unjails player, teleport him to
 * Floran.
 * @version $Revision: 1.1.6.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminBan implements IAdminCommandHandler
{
	private static final Logger LOGGER = Logger.getLogger(AdminBan.class);
	
	private static final String UPDATE_CHARACTER_PUNISH = "UPDATE characters SET punish_level=?, punish_timer=? WHERE car_name=?";
	private static final String UPDATE_JAIL_OFFLINE_PLAYER = "UPDATE characters SET x=?, y=?, z=?, punish_level=?, punish_timer=? WHERE char_name=?";
	private static final String UPDATE_ACCESS_LEVEL = "UPDATE characters SET accesslevel=? WHERE char_name=?";
	private static final String INSERT_IP_BANNED = "INSERT IGNORE INTO `" + Config.LOGINSERVER_DB + "`.`ip_banned` (ip_address) VALUES(?)";
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_ban", // returns ban commands
		"admin_ban_acc",
		"admin_ban_char",
		"admin_banchat",
		"admin_ban_ip", // Consider use the method "addBanForAddress" in LoginController class
		"admin_unban", // returns unban commands
		"admin_unban_acc",
		"admin_unban_char",
		"admin_unbanchat",
		"admin_jail",
		"admin_unjail"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		String player = "";
		int duration = -1;
		L2PcInstance targetPlayer = null;
		
		if (st.hasMoreTokens())
		{
			player = st.nextToken();
			targetPlayer = L2World.getInstance().getPlayer(player);
			
			if (st.hasMoreTokens())
			{
				try
				{
					duration = Integer.parseInt(st.nextToken());
				}
				catch (final NumberFormatException nfe)
				{
					activeChar.sendMessage("Invalid number format used: " + nfe);
					return false;
				}
			}
		}
		else
		{
			if (activeChar.getTarget() != null && activeChar.getTarget() instanceof L2PcInstance)
			{
				targetPlayer = (L2PcInstance) activeChar.getTarget();
			}
		}
		
		if (targetPlayer != null && targetPlayer.equals(activeChar))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_ON_YOURSELF));
			return false;
		}
		
		if (command.startsWith("admin_ban ") || command.equalsIgnoreCase("admin_ban"))
		{
			activeChar.sendMessage("Available ban commands: //ban_acc, //ban_char, //ban_chat, //ban_ip");
			return false;
		}
		else if (command.startsWith("admin_ban_acc"))
		{
			// May need to check usage in admin_ban_menu as well.
			
			if (targetPlayer == null && player.equals(""))
			{
				activeChar.sendMessage("Usage: //ban_acc <account_name> (if none, target char's account gets banned)");
				return false;
			}
			else if (targetPlayer == null)
			{
				LoginServerThread.getInstance().sendAccessLevel(player, -100);
				activeChar.sendMessage("Ban request sent for account " + player);
				auditAction(command, activeChar, player);
			}
			else
			{
				targetPlayer.setPunishLevel(L2PcInstance.PunishLevel.ACC, 0);
				activeChar.sendMessage("Account " + targetPlayer.getAccountName() + " banned.");
				auditAction(command, activeChar, targetPlayer.getAccountName());
			}
		}
		else if (command.startsWith("admin_ban_char"))
		{
			if (targetPlayer == null && player.equals(""))
			{
				activeChar.sendMessage("Usage: //ban_char <char_name> (if none, target char is banned)");
				return false;
			}
			auditAction(command, activeChar, (targetPlayer == null ? player : targetPlayer.getName()));
			return changeCharAccessLevel(targetPlayer, player, activeChar, -100);
		}
		else if (command.startsWith("admin_banchat"))
		{
			if (targetPlayer == null && player.equals(""))
			{
				activeChar.sendMessage("Usage: //banchat <char_name> [penalty_minutes]");
				return false;
			}
			if (targetPlayer != null)
			{
				if (targetPlayer.getPunishLevel().value() > 0)
				{
					activeChar.sendMessage(targetPlayer.getName() + " is already jailed or banned.");
					return false;
				}
				String banLengthStr = "";
				
				targetPlayer.setPunishLevel(L2PcInstance.PunishLevel.CHAT, duration);
				if (duration > 0)
				{
					banLengthStr = " for " + duration + " minutes";
				}
				activeChar.sendMessage(targetPlayer.getName() + " is now chat banned" + banLengthStr + ".");
				auditAction(command, activeChar, targetPlayer.getName());
			}
			else
			{
				banChatOfflinePlayer(activeChar, player, duration, true);
				auditAction(command, activeChar, player);
			}
		}
		else if (command.startsWith("admin_ban_ip"))
		{
			if (targetPlayer == null || player.equals(""))
			{
				activeChar.sendMessage("Usage: //admin_ban_ip <char_name>");
				return false;
			}
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement pst = con.prepareStatement(INSERT_IP_BANNED))
			{
				pst.setString(1, targetPlayer.getIpAddress());
				pst.executeUpdate();
				
				String text = targetPlayer.getName() + " with IP " + targetPlayer.getIpAddress() + " has been banned. Just the IP ADDRESS was banned, player still can join the game.";
				activeChar.sendMessage(text);
				targetPlayer.kick();
				
				Log.add(text, "ip_banned");
			}
			catch (Exception e)
			{
				LOGGER.error("AdminBan: Command admin_ban_ip, could not insert IP in database", e);
				return false;
			}
		}
		else if (command.startsWith("admin_unbanchat"))
		{
			if (targetPlayer == null && player.equals(""))
			{
				activeChar.sendMessage("Usage: //unbanchat <char_name>");
				return false;
			}
			if (targetPlayer != null)
			{
				if (targetPlayer.isChatBanned())
				{
					targetPlayer.setPunishLevel(L2PcInstance.PunishLevel.NONE, 0);
					activeChar.sendMessage(targetPlayer.getName() + "'s chat ban has now been lifted.");
					auditAction(command, activeChar, targetPlayer.getName());
				}
				else
				{
					activeChar.sendMessage(targetPlayer.getName() + " is not currently chat banned.");
				}
			}
			else
			{
				banChatOfflinePlayer(activeChar, player, 0, false);
				auditAction(command, activeChar, player);
			}
		}
		else if (command.startsWith("admin_unban ") || command.equalsIgnoreCase("admin_unban"))
		{
			activeChar.sendMessage("Available unban commands: //unban_acc, //unban_char, //unban_chat");
			return false;
		}
		else if (command.startsWith("admin_unban_acc"))
		{
			// Need to check admin_unban_menu command as well in AdminMenu.java handler.
			
			if (targetPlayer != null)
			{
				activeChar.sendMessage(targetPlayer.getName() + " is currently online so must not be banned.");
				return false;
			}
			else if (!player.equals(""))
			{
				LoginServerThread.getInstance().sendAccessLevel(player, 0);
				activeChar.sendMessage("Unban request sent for account " + player);
				auditAction(command, activeChar, player);
			}
			else
			{
				activeChar.sendMessage("Usage: //unban_acc <account_name>");
				return false;
			}
		}
		else if (command.startsWith("admin_unban_char"))
		{
			if (targetPlayer == null && player.equals(""))
			{
				activeChar.sendMessage("Usage: //unban_char <char_name>");
				return false;
			}
			else if (targetPlayer != null)
			{
				activeChar.sendMessage(targetPlayer.getName() + " is currently online so must not be banned.");
				return false;
			}
			else
			{
				auditAction(command, activeChar, player);
				return changeCharAccessLevel(null, player, activeChar, 0);
			}
		}
		else if (command.startsWith("admin_jail"))
		{
			if (targetPlayer == null && player.equals(""))
			{
				activeChar.sendMessage("Usage: //jail <charname> [penalty_minutes] (if no name is given, selected target is jailed indefinitely)");
				return false;
			}
			if (targetPlayer != null)
			{
				targetPlayer.setPunishLevel(L2PcInstance.PunishLevel.JAIL, duration);
				activeChar.sendMessage("Character " + targetPlayer.getName() + " jailed for " + (duration > 0 ? duration + " minutes." : "ever!"));
				auditAction(command, activeChar, targetPlayer.getName());
				
				if (targetPlayer.getParty() != null)
				{
					targetPlayer.getParty().removePartyMember(targetPlayer);
				}
			}
			else
			{
				jailOfflinePlayer(activeChar, player, duration);
				auditAction(command, activeChar, player);
			}
		}
		else if (command.startsWith("admin_unjail"))
		{
			if (targetPlayer == null && player.equals(""))
			{
				activeChar.sendMessage("Usage: //unjail <charname> (If no name is given target is used)");
				return false;
			}
			else if (targetPlayer != null)
			{
				targetPlayer.setPunishLevel(L2PcInstance.PunishLevel.NONE, 0);
				activeChar.sendMessage("Character " + targetPlayer.getName() + " removed from jail");
				auditAction(command, activeChar, targetPlayer.getName());
			}
			else
			{
				unjailOfflinePlayer(activeChar, player);
				auditAction(command, activeChar, player);
			}
		}
		return true;
	}
	
	private void auditAction(final String fullCommand, final L2PcInstance activeChar, final String target)
	{
		if (!Config.GMAUDIT)
		{
			return;
		}
		
		final String[] command = fullCommand.split(" ");
		
		GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", command[0], (target.equals("") ? "no-target" : target), (command.length > 2 ? command[2] : ""));
	}
	
	private void banChatOfflinePlayer(final L2PcInstance activeChar, final String name, final int delay, final boolean ban)
	{
		int level = 0;
		long value = 0;
		if (ban)
		{
			level = L2PcInstance.PunishLevel.CHAT.value();
			value = (delay > 0 ? delay * 60000L : 60000);
		}
		else
		{
			level = L2PcInstance.PunishLevel.NONE.value();
			value = 0;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER_PUNISH))
		{
			statement.setInt(1, level);
			statement.setLong(2, value);
			statement.setString(3, name);
			statement.executeUpdate();
			
			final int count = statement.getUpdateCount();
			
			if (count == 0)
			{
				activeChar.sendMessage("Character not found!");
			}
			else if (ban)
			{
				activeChar.sendMessage("Character " + name + " chat-banned for " + (delay > 0 ? delay + " minutes." : "ever!"));
			}
			else
			{
				activeChar.sendMessage("Character " + name + "'s chat-banned lifted");
			}
		}
		catch (final SQLException se)
		{
			activeChar.sendMessage("SQLException while chat-banning player");
			se.printStackTrace();
		}
	}
	
	private void jailOfflinePlayer(final L2PcInstance activeChar, final String name, final int delay)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_JAIL_OFFLINE_PLAYER))
		{
			statement.setInt(1, -114356);
			statement.setInt(2, -249645);
			statement.setInt(3, -2984);
			statement.setInt(4, L2PcInstance.PunishLevel.JAIL.value());
			statement.setLong(5, (delay > 0 ? delay * 60000L : 0));
			statement.setString(6, name);
			statement.executeUpdate();
			
			final int count = statement.getUpdateCount();
			
			if (count == 0)
			{
				activeChar.sendMessage("Character not found!");
			}
			else
			{
				activeChar.sendMessage("Character " + name + " jailed for " + (delay > 0 ? delay + " minutes." : "ever!"));
			}
		}
		catch (final SQLException se)
		{
			activeChar.sendMessage("SQLException while jailing player");
			if (Config.DEBUG)
			{
				se.printStackTrace();
			}
		}
	}
	
	private void unjailOfflinePlayer(final L2PcInstance activeChar, final String name)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement(UPDATE_JAIL_OFFLINE_PLAYER))
		{
			statement.setInt(1, 17836);
			statement.setInt(2, 170178);
			statement.setInt(3, -3507);
			statement.setInt(4, 0);
			statement.setLong(5, 0);
			statement.setString(6, name);
			statement.executeUpdate();
			
			final int count = statement.getUpdateCount();
			
			if (count == 0)
			{
				activeChar.sendMessage("Character not found!");
			}
			else
			{
				activeChar.sendMessage("Character " + name + " removed from jail");
			}
		}
		catch (final SQLException se)
		{
			activeChar.sendMessage("SQLException while jailing player");
			if (Config.DEBUG)
			{
				se.printStackTrace();
			}
		}
	}
	
	private boolean changeCharAccessLevel(final L2PcInstance targetPlayer, final String player, final L2PcInstance activeChar, final int lvl)
	{
		boolean output = false;
		
		if (targetPlayer != null)
		{
			targetPlayer.setAccessLevel(lvl);
			targetPlayer.sendMessage("Your character has been banned. Contact the administrator for more informations.");
			
			try
			{
				// Save player status
				targetPlayer.store();
				
				// Player Disconnect like L2OFF, no client crash.
				if (targetPlayer.getClient() != null)
				{
					targetPlayer.getClient().sendPacket(ServerClose.STATIC_PACKET);
					targetPlayer.getClient().setActiveChar(null);
					targetPlayer.setClient(null);
				}
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					t.printStackTrace();
				}
			}
			
			targetPlayer.deleteMe();
			
			RegionBBSManager.getInstance().changeCommunityBoard();
			activeChar.sendMessage("The character " + targetPlayer.getName() + " has now been banned.");
			
			output = true;
		}
		else
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(UPDATE_ACCESS_LEVEL))
			{
				statement.setInt(1, lvl);
				statement.setString(2, player);
				statement.executeUpdate();
				
				final int count = statement.getUpdateCount();
				
				if (count == 0)
				{
					activeChar.sendMessage("Character not found or access level unaltered.");
				}
				else
				{
					activeChar.sendMessage(player + " now has an access level of " + lvl);
					output = true;
				}
			}
			catch (final SQLException se)
			{
				activeChar.sendMessage("SQLException while changing character's access level");
				if (Config.DEBUG)
				{
					se.printStackTrace();
				}
			}
		}
		return output;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}