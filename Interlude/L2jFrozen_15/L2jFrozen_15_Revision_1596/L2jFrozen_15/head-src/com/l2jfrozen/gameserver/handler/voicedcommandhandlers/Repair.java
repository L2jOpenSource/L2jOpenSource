package com.l2jfrozen.gameserver.handler.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.handler.IVoicedCommandHandler;
import com.l2jfrozen.gameserver.handler.custom.ICustomByPassHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * <B><U>User Character .repair voicecommand - SL2 L2JEmu</U></B><BR>
 * <BR>
 * <U>NOTICE:</U> Voice command .repair that when used, allows player to try to repair any of characters on his account, by setting spawn to Floran, removing all shortcuts and moving everything equipped to that char warehouse.<BR>
 * <BR>
 * (solving client crashes on character entering world)<BR>
 * <BR>
 * @author szponiasty
 */
public class Repair implements IVoicedCommandHandler, ICustomByPassHandler
{
	private static final Logger LOGGER = Logger.getLogger(Repair.class);
	private static final String SELECT_CHAR_NAME_BY_ACCOUNT_NAME = "SELECT char_name FROM characters WHERE account_name=?";
	private static final String SELECT_ACCOUNT_NAME_BY_CHAR_NAME = "SELECT account_name FROM characters WHERE char_name=?";
	private static final String SELECT_CHAR_PUNISH_BY_CHAR_NAME = "SELECT accesslevel,punish_level FROM characters WHERE char_name=?";
	private static final String SELECT_CHAR_KARMA_BY_CHAR_NAME = "SELECT karma FROM characters WHERE char_name=?";
	private static final String SELECT_CHAR_OBJ_ID_BY_CHAR_NAME = "SELECT obj_Id FROM characters WHERE char_name=?";
	private static final String UPDATE_CHARACTER_LOCATION = "UPDATE characters SET x=17867, y=170259, z=-3503 WHERE obj_Id=?";
	private static final String DELETE_CHARACTER_SHORTCUTS = "DELETE FROM character_shortcuts WHERE char_obj_id=?";
	private static final String UPDATE_CHARACTER_ITEM_LOCATION = "UPDATE items SET loc=\"INVENTORY\" WHERE owner_id=? AND loc=\"PAPERDOLL\"";
	
	private static final String[] VOICED_COMMANDS =
	{
		"repair",
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (command.startsWith("repair"))
		{
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
			npcHtmlMessage.setFile("data/html/mods/repair/repair.htm");
			npcHtmlMessage.replace("%acc_chars%", getCharList(activeChar));
			activeChar.sendPacket(npcHtmlMessage);
			return true;
		}
		
		return false;
	}
	
	private String getCharList(L2PcInstance activeChar)
	{
		String result = "";
		final String repCharAcc = activeChar.getAccountName();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHAR_NAME_BY_ACCOUNT_NAME))
		{
			statement.setString(1, repCharAcc);
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					if (activeChar.getName().compareTo(rset.getString(1)) != 0)
					{
						result += rset.getString(1) + ";";
					}
				}
			}
			
		}
		catch (SQLException e)
		{
			LOGGER.error("Repair.getChatList : Something went wrong while getting data from characters table. ", e);
		}
		return result;
	}
	
	private boolean checkAcc(L2PcInstance activeChar, String repairChar)
	{
		boolean result = false;
		String repCharAcc = "";
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_ACCOUNT_NAME_BY_CHAR_NAME))
		{
			statement.setString(1, repairChar);
			
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					repCharAcc = rset.getString(1);
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.error("Repair.checkAcc : Something went wrong while checking account data from characters table. ", e);
		}
		
		if (activeChar.getAccountName().compareTo(repCharAcc) == 0)
		{
			result = true;
		}
		
		return result;
	}
	
	private boolean checkPunish(L2PcInstance activeChar, String repairChar)
	{
		boolean result = false;
		int accessLevel = 0;
		int repCharJail = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHAR_PUNISH_BY_CHAR_NAME))
		{
			statement.setString(1, repairChar);
			
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					accessLevel = rset.getInt(1);
					repCharJail = rset.getInt(2);
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.error("Repair.checkPunish : Something went wrong while checking punish data from characters table. ", e);
		}
		
		if (repCharJail == 1 || accessLevel < 0)
		{
			result = true;
		}
		
		return result;
	}
	
	private boolean checkKarma(L2PcInstance activeChar, String repairChar)
	{
		boolean result = false;
		int repCharKarma = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHAR_KARMA_BY_CHAR_NAME))
		{
			statement.setString(1, repairChar);
			
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					repCharKarma = rset.getInt(1);
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.error("Repair.checkKarma : Something went wrong while checking karma data from characters table. ", e);
		}
		
		if (repCharKarma > 0)
		{
			result = true;
		}
		
		return result;
	}
	
	private boolean checkChar(L2PcInstance activeChar, String repairChar)
	{
		boolean result = false;
		
		if (activeChar.getName().compareTo(repairChar) == 0)
		{
			result = true;
		}
		
		return result;
	}
	
	private void repairCharacter(String charName)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHAR_OBJ_ID_BY_CHAR_NAME))
		{
			statement.setString(1, charName);
			
			int objId = 0;
			
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					objId = rset.getInt(1);
				}
			}
			
			if (objId == 0)
			{
				return;
			}
			
			try (PreparedStatement statementRepair = con.prepareStatement(UPDATE_CHARACTER_LOCATION))
			{
				statementRepair.setInt(1, objId);
				statementRepair.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("Repair.repairCharacter : Something went wrong while updating loc x,y and z data into characters table. ", e);
			}
			
			try (PreparedStatement statementRepair = con.prepareStatement(DELETE_CHARACTER_SHORTCUTS))
			{
				statementRepair.setInt(1, objId);
				statementRepair.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("Repair.repairCharacter : Something went wrong while deleting shortcuts data into character_shortcuts table. ", e);
			}
			
			try (PreparedStatement statementRepair = con.prepareStatement(UPDATE_CHARACTER_ITEM_LOCATION))
			{
				statementRepair.setInt(1, objId);
				statementRepair.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("Repair.repairCharacter : Something went wrong while deleting shortcuts data into character_shortcuts table. ", e);
			}
		}
		catch (Exception e)
		{
			LOGGER.warn("GameServer: could not repair character:" + e);
		}
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
	private static final String[] BYPASSCMD =
	{
		"repair",
		"repair_close_win"
	};
	
	private enum CommandEnum
	{
		repair,
		repair_close_win
	}
	
	@Override
	public String[] getByPassCommands()
	{
		return BYPASSCMD;
	}
	
	@Override
	public void handleCommand(String command, L2PcInstance activeChar, String repairChar)
	{
		final CommandEnum comm = CommandEnum.valueOf(command);
		
		if (comm == null)
		{
			return;
		}
		
		switch (comm)
		{
			case repair:
			{
				if (repairChar == null || repairChar.equals(""))
				{
					return;
				}
				
				if (checkAcc(activeChar, repairChar))
				{
					if (checkChar(activeChar, repairChar))
					{
						NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
						npcHtmlMessage.setFile("data/html/mods/repair/repair-self.htm");
						activeChar.sendPacket(npcHtmlMessage);
						return;
					}
					else if (checkPunish(activeChar, repairChar))
					{
						NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
						npcHtmlMessage.setFile("data/html/mods/repair/repair-jail.htm");
						activeChar.sendPacket(npcHtmlMessage);
						return;
					}
					else if (checkKarma(activeChar, repairChar))
					{
						activeChar.sendMessage("Selected Char has Karma,Cannot be repaired!");
						return;
					}
					else
					{
						repairCharacter(repairChar);
						NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
						npcHtmlMessage.setFile("data/html/mods/repair/repair-done.htm");
						activeChar.sendPacket(npcHtmlMessage);
						return;
					}
				}
				
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
				npcHtmlMessage.setFile("data/html/mods/repair/repair-error.htm");
				npcHtmlMessage.replace("%acc_chars%", getCharList(activeChar));
				activeChar.sendPacket(npcHtmlMessage);
				return;
			}
			case repair_close_win:
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
	}
}
