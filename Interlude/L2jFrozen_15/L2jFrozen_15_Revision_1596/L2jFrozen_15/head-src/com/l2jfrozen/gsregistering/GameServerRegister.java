package com.l2jfrozen.gsregistering;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.l2jfrozen.Config;
import com.l2jfrozen.FService;
import com.l2jfrozen.ServerType;
import com.l2jfrozen.gameserver.datatables.GameServerTable;
import com.l2jfrozen.gameserver.thread.LoginServerThread;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class GameServerRegister
{
	private static final Logger LOGGER = Logger.getLogger(GameServerRegister.class);
	private static final String DELETE_ALL_GAMESERVERS = "DELETE FROM gameservers";
	private static String choice;
	private static boolean choiceOk;
	
	public static void main(final String[] args) throws IOException
	{
		PropertyConfigurator.configure(FService.LOG_CONF_FILE);
		ServerType.serverMode = ServerType.MODE_LOGINSERVER;
		Config.load();
		final LineNumberReader in = new LineNumberReader(new InputStreamReader(System.in));
		try
		{
			GameServerTable.load();
		}
		catch (final Exception e)
		{
			LOGGER.info("FATAL: Failed loading gameservers table. Reason: ", e);
			System.exit(1);
		}
		final GameServerTable gameServerTable = GameServerTable.getInstance();
		LOGGER.info("Welcome to L2JFrozen GameServer Regitering");
		LOGGER.info("Enter The id of the server you want to register");
		LOGGER.info("Type 'help' to get a list of ids.");
		LOGGER.info("Type 'clean' to unregister all currently registered gameservers on this LoginServer.");
		LOGGER.info("Type 'exit' to unregister all currently registered gameservers on this LoginServer.");
		while (!choiceOk)
		{
			System.out.print("Your choice: ");
			choice = in.readLine();
			if (choice.equalsIgnoreCase("help"))
			{
				for (final Map.Entry<Integer, String> entry : gameServerTable.getServerNames().entrySet())
				{
					LOGGER.info("Server: ID: " + entry.getKey() + "\t- " + entry.getValue() + " - In Use: " + (gameServerTable.hasRegisteredGameServerOnId(entry.getKey()) ? "YES" : "NO"));
				}
				LOGGER.info("You can also see servername.xml");
			}
			else if (choice.equalsIgnoreCase("clean"))
			{
				System.out.print("This is going to UNREGISTER ALL servers from this LoginServer. Are you sure? (y/n) ");
				choice = in.readLine();
				if (choice.equals("y"))
				{
					GameServerRegister.cleanRegisteredGameServersFromDB();
					gameServerTable.getRegisteredGameServers().clear();
				}
				else
				{
					LOGGER.info("ABORTED");
				}
			}
			else if (choice.equalsIgnoreCase("exit"))
			{
				System.out.println("Bye...");
				return;
			}
			else
			{
				try
				{
					final int id = Integer.parseInt(choice);
					final int size = gameServerTable.getServerNames().size();
					if (size == 0)
					{
						LOGGER.info("No server names avalible, please make sure that servername.xml is in the LoginServer directory.");
						System.exit(1);
					}
					
					choice = "";
					
					while (!choice.equalsIgnoreCase(""))
					{
						LOGGER.info("External Server Ip:");
						choice = in.readLine();
					}
					
					final String name = gameServerTable.getServerNameById(id);
					if (name == null)
					{
						LOGGER.info("No name for id: " + id);
						continue;
					}
					
					if (gameServerTable.hasRegisteredGameServerOnId(id))
					{
						LOGGER.info("This id is not free");
					}
					else
					{
						final byte[] hexId = LoginServerThread.generateHex(16);
						gameServerTable.registerServerOnDB(hexId, id);
						Config.saveHexid(id, new BigInteger(hexId).toString(16), "hexid.txt");
						LOGGER.info("Server Registered hexid saved to 'hexid.txt'");
						LOGGER.info("Put this file in the /config folder of your gameserver.");
						return;
					}
				}
				catch (final NumberFormatException nfe)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						nfe.printStackTrace();
					}
					
					LOGGER.info("Please, type a number or 'help'");
				}
			}
		}
	}
	
	public static void cleanRegisteredGameServersFromDB()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_ALL_GAMESERVERS))
		{
			statement.executeUpdate();
		}
		catch (final SQLException e)
		{
			LOGGER.error("GameServerRegister.cleanRegisteredGameServersFromDB : Could not delete from gameservers table", e);
		}
	}
}