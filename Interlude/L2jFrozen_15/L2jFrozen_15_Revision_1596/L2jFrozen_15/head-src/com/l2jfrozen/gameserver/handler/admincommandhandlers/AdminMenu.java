package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.thread.LoginServerThread;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class handles following admin commands: - handles every admin menu command
 * @version $Revision: 1.3.2.6.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminMenu implements IAdminCommandHandler
{
	private static final Logger LOGGER = Logger.getLogger(AdminMenu.class);
	
	private static final String SELECT_ACCOUNT_BY_CHARACTER = "SELECT account_name FROM characters WHERE char_name=?";
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_char_manage",
		"admin_teleport_character_to_menu",
		"admin_recall_char_menu",
		"admin_recall_party_menu",
		"admin_recall_clan_menu",
		"admin_goto_char_menu",
		"admin_kick_menu",
		"admin_kill_menu",
		"admin_ban_menu",
		"admin_unban_menu"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.equals("admin_char_manage"))
		{
			showMainPage(activeChar);
		}
		else if (command.startsWith("admin_teleport_character_to_menu"))
		{
			String[] data = command.split(" ");
			
			if (data.length == 5)
			{
				String playerName = data[1];
				L2PcInstance player = L2World.getInstance().getPlayer(playerName);
				
				if (player != null)
				{
					teleportCharacter(player, Integer.parseInt(data[2]), Integer.parseInt(data[3]), Integer.parseInt(data[4]), activeChar, "Admin is teleporting you.");
				}
				
				playerName = null;
				player = null;
			}
			
			showMainPage(activeChar);
			
			data = null;
		}
		else if (command.startsWith("admin_recall_char_menu"))
		{
			try
			{
				String targetName = command.substring(23);
				L2PcInstance player = L2World.getInstance().getPlayer(targetName);
				teleportCharacter(player, activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar, "Admin is teleporting you.");
				targetName = null;
				player = null;
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
		else if (command.startsWith("admin_recall_party_menu"))
		{
			final int x = activeChar.getX(), y = activeChar.getY(), z = activeChar.getZ();
			
			try
			{
				String targetName = command.substring(24);
				L2PcInstance player = L2World.getInstance().getPlayer(targetName);
				
				if (player == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
					return true;
				}
				
				if (!player.isInParty())
				{
					activeChar.sendMessage("Player is not in party.");
					teleportCharacter(player, x, y, z, activeChar, "Admin is teleporting you.");
					return true;
				}
				
				for (final L2PcInstance pm : player.getParty().getPartyMembers())
				{
					teleportCharacter(pm, x, y, z, activeChar, "Your party is being teleported by an Admin.");
				}
				
				targetName = null;
				player = null;
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
		else if (command.startsWith("admin_recall_clan_menu"))
		{
			final int x = activeChar.getX(), y = activeChar.getY(), z = activeChar.getZ();
			try
			{
				String targetName = command.substring(23);
				L2PcInstance player = L2World.getInstance().getPlayer(targetName);
				
				if (player == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
					return true;
				}
				
				L2Clan clan = player.getClan();
				if (clan == null)
				{
					activeChar.sendMessage("Player is not in a clan.");
					teleportCharacter(player, x, y, z, activeChar, "Admin is teleporting you.");
					
					return true;
				}
				
				L2PcInstance[] members = clan.getOnlineMembers("");
				
				for (final L2PcInstance member : members)
				{
					teleportCharacter(member, x, y, z, activeChar, "Your clan is being teleported by an Admin.");
				}
				
				targetName = null;
				player = null;
				clan = null;
				members = null;
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
		else if (command.startsWith("admin_goto_char_menu"))
		{
			try
			{
				String targetName = command.substring(21);
				L2PcInstance player = L2World.getInstance().getPlayer(targetName);
				teleportToCharacter(activeChar, player);
				targetName = null;
				player = null;
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
		else if (command.equals("admin_kill_menu"))
		{
			handleKill(activeChar);
		}
		else if (command.startsWith("admin_kick_menu"))
		{
			StringTokenizer st = new StringTokenizer(command);
			
			if (st.countTokens() > 1)
			{
				st.nextToken();
				String player = st.nextToken();
				L2PcInstance plyr = L2World.getInstance().getPlayer(player);
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				
				if (plyr != null)
				{
					plyr.logout();
					sm.addString("You kicked " + plyr.getName() + " from the game.");
				}
				else
				{
					sm.addString("Player " + player + " was not found in the game.");
				}
				
				activeChar.sendPacket(sm);
				
				sm = null;
				player = null;
				plyr = null;
			}
			
			showMainPage(activeChar);
			
			st = null;
		}
		else if (command.startsWith("admin_ban_menu"))
		{
			StringTokenizer st = new StringTokenizer(command);
			
			if (st.countTokens() > 1)
			{
				st.nextToken();
				String player = st.nextToken();
				L2PcInstance plyr = L2World.getInstance().getPlayer(player);
				
				if (plyr != null)
				{
					plyr.logout();
				}
				
				setAccountAccessLevel(player, activeChar, -100);
				
				plyr = null;
				player = null;
			}
			
			showMainPage(activeChar);
			
			st = null;
		}
		else if (command.startsWith("admin_unban_menu"))
		{
			StringTokenizer st = new StringTokenizer(command);
			
			if (st.countTokens() > 1)
			{
				st.nextToken();
				final String player = st.nextToken();
				setAccountAccessLevel(player, activeChar, 0);
			}
			
			showMainPage(activeChar);
			
			st = null;
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void handleKill(final L2PcInstance activeChar)
	{
		handleKill(activeChar, null);
	}
	
	private void handleKill(final L2PcInstance activeChar, final String player)
	{
		L2Object obj = activeChar.getTarget();
		L2Character target = (L2Character) obj;
		String filename = "main_menu.htm";
		
		if (player != null)
		{
			final L2PcInstance plyr = L2World.getInstance().getPlayer(player);
			if (plyr != null)
			{
				target = plyr;
				activeChar.sendMessage("You killed " + player);
			}
		}
		
		if (target != null)
		{
			if (target instanceof L2PcInstance)
			{
				target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar);
				filename = "charmanage.htm";
			}
			else if (Config.L2JMOD_CHAMPION_ENABLE && target.isChampion())
			{
				target.reduceCurrentHp(target.getMaxHp() * Config.L2JMOD_CHAMPION_HP + 1, activeChar);
			}
			else
			{
				target.reduceCurrentHp(target.getMaxHp() + 1, activeChar);
			}
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
		}
		
		AdminHelpPage.showHelpPage(activeChar, filename);
		
		filename = null;
		target = null;
		obj = null;
	}
	
	private void teleportCharacter(final L2PcInstance player, final int x, final int y, final int z, final L2PcInstance activeChar, final String message)
	{
		if (player != null)
		{
			player.sendMessage(message);
			player.teleToLocation(x, y, z, true);
		}
		
		showMainPage(activeChar);
	}
	
	private void teleportToCharacter(final L2PcInstance activeChar, final L2Object target)
	{
		L2PcInstance player = null;
		
		if (target != null && target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		
		if (player.getObjectId() == activeChar.getObjectId())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_ON_YOURSELF));
		}
		else
		{
			activeChar.teleToLocation(player.getX(), player.getY(), player.getZ(), true);
			activeChar.sendMessage("You're teleporting yourself to character " + player.getName());
		}
		
		showMainPage(activeChar);
		
		player = null;
	}
	
	private void showMainPage(final L2PcInstance activeChar)
	{
		AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
	}
	
	private void setAccountAccessLevel(String player, L2PcInstance activeChar, int banLevel)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_ACCOUNT_BY_CHARACTER))
		{
			statement.setString(1, player);
			try (ResultSet result = statement.executeQuery())
			{
				if (result.next())
				{
					String acc_name = result.getString(1);
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
					
					if (acc_name.length() > 0)
					{
						LoginServerThread.getInstance().sendAccessLevel(acc_name, banLevel);
						sm.addString("Account Access Level for " + player + " set to " + banLevel + ".");
					}
					else
					{
						sm.addString("Couldn't find player: " + player + ".");
					}
					
					activeChar.sendPacket(sm);
					sm = null;
					acc_name = null;
				}
				else
				{
					activeChar.sendMessage("Specified player name didn't lead to a valid account.");
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("AdminMenu.setAccountAccessLevel : Could not set accessLevel", e);
		}
	}
}
