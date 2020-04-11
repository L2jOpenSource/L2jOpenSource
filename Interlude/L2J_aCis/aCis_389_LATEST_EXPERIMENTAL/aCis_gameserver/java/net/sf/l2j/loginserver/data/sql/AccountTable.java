package net.sf.l2j.loginserver.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.l2j.commons.logging.CLogger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.loginserver.model.Account;

/**
 * This class controls all generated {@link Account}s.
 */
public class AccountTable
{
	private static final CLogger LOGGER = new CLogger(AccountTable.class.getName());
	
	private static final String SELECT_ACCOUNT = "SELECT password, access_level, last_server FROM accounts WHERE login = ?";
	private static final String INSERT_ACCOUNT = "INSERT INTO accounts (login, password, last_active) VALUES (?, ?, ?)";
	private static final String UPDATE_ACCOUNT_LAST_TIME = "UPDATE accounts SET last_active = ? WHERE login = ?";
	private static final String UPDATE_ACCOUNT_LAST_SERVER = "UPDATE accounts SET last_server = ? WHERE login = ?";
	private static final String UPDATE_ACCOUNT_ACCESS_LEVEL = "UPDATE accounts SET access_level = ? WHERE login = ?";
	
	protected AccountTable()
	{
	}
	
	/**
	 * @param login : The {@link String} used as login.
	 * @return A new {@link Account} whom informations are already registered into the database, or null if not existing.
	 */
	public Account getAccount(String login)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SELECT_ACCOUNT))
		{
			ps.setString(1, login);
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
					return new Account(login, rs.getString("password"), rs.getInt("access_level"), rs.getInt("last_server"));
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Exception retrieving account infos.", e);
		}
		return null;
	}
	
	/**
	 * @param login : The {@link String} used as login.
	 * @param hashed : The {@link String} used as hashed password.
	 * @param currentTime : The creation timestamp of the newly generated {@link Account}.
	 * @return A new {@link Account} whom informations are saved into the database, or null if a problem occured.
	 */
	public Account createAccount(String login, String hashed, long currentTime)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT_ACCOUNT))
		{
			ps.setString(1, login);
			ps.setString(2, hashed);
			ps.setLong(3, currentTime);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Exception auto creating account for {}.", e, login);
			return null;
		}
		
		// Generate a new Account.
		return new Account(login, hashed, 0, 1);
	}
	
	/**
	 * @param login : The {@link String} used as login.
	 * @param currentTime : The timestamp to refresh this {@link Account} with.
	 * @return True if the given {@link Account} last_active timestamp has been correctly refreshed on the database, false otherwise.
	 */
	public boolean setAccountLastTime(String login, long currentTime)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_ACCOUNT_LAST_TIME))
		{
			ps.setLong(1, currentTime);
			ps.setString(2, login);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Exception auto creating account for {}.", e, login);
			return false;
		}
		return true;
	}
	
	/**
	 * Refresh access_level value of an {@link Account} on the database.
	 * @param login : The {@link String} used as login.
	 * @param level : The new level to set.
	 */
	public void setAccountAccessLevel(String login, int level)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_ACCOUNT_ACCESS_LEVEL))
		{
			ps.setInt(1, level);
			ps.setString(2, login);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't set access level {} for {}.", e, level, login);
		}
	}
	
	/**
	 * Refresh last_server value of an {@link Account} on the database.
	 * @param login : The {@link String} used as login.
	 * @param serverId : The serverId to set.
	 */
	public void setAccountLastServer(String login, int serverId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_ACCOUNT_LAST_SERVER))
		{
			ps.setInt(1, serverId);
			ps.setString(2, login);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't set last server.", e);
		}
	}
	
	public static AccountTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AccountTable INSTANCE = new AccountTable();
	}
}