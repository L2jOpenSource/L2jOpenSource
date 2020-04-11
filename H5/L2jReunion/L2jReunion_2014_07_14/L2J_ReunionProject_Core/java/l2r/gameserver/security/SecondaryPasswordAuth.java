/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

import l2r.L2DatabaseFactory;
import l2r.gameserver.LoginServerThread;
import l2r.gameserver.datatables.xml.SecondaryAuthData;
import l2r.gameserver.network.L2GameClient;
import l2r.gameserver.network.serverpackets.Ex2ndPasswordAck;
import l2r.gameserver.network.serverpackets.Ex2ndPasswordCheck;
import l2r.gameserver.network.serverpackets.Ex2ndPasswordVerify;
import l2r.gameserver.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mrTJO
 */
public class SecondaryPasswordAuth
{
	private final Logger _log = LoggerFactory.getLogger(SecondaryPasswordAuth.class);
	private final L2GameClient _activeClient;
	
	private String _password;
	private int _wrongAttempts;
	private boolean _authed;
	
	private static final String VAR_PWD = "secauth_pwd";
	private static final String VAR_WTE = "secauth_wte";
	
	private static final String SELECT_PASSWORD = "SELECT var, value FROM account_gsdata WHERE account_name=? AND var LIKE 'secauth_%'";
	private static final String INSERT_PASSWORD = "INSERT INTO account_gsdata VALUES (?, ?, ?)";
	private static final String UPDATE_PASSWORD = "UPDATE account_gsdata SET value=? WHERE account_name=? AND var=?";
	
	private static final String INSERT_ATTEMPT = "INSERT INTO account_gsdata VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value=?";
	
	/**
	 * @param activeClient
	 */
	public SecondaryPasswordAuth(L2GameClient activeClient)
	{
		_activeClient = activeClient;
		_password = null;
		_wrongAttempts = 0;
		_authed = false;
		loadPassword();
	}
	
	private void loadPassword()
	{
		String var, value = null;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_PASSWORD))
		{
			statement.setString(1, _activeClient.getAccountName());
			try (ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					var = rs.getString("var");
					value = rs.getString("value");
					
					if (var.equals(VAR_PWD))
					{
						_password = value;
					}
					else if (var.equals(VAR_WTE))
					{
						_wrongAttempts = Integer.parseInt(value);
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.error("Error while reading password.", e);
		}
	}
	
	public boolean savePassword(String password)
	{
		if (passwordExist())
		{
			_log.warn("[SecondaryPasswordAuth]" + _activeClient.getAccountName() + " forced savePassword");
			_activeClient.closeNow();
			return false;
		}
		
		if (!validatePassword(password))
		{
			_activeClient.sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.WRONG_PATTERN));
			return false;
		}
		
		password = cryptPassword(password);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_PASSWORD))
		{
			statement.setString(1, _activeClient.getAccountName());
			statement.setString(2, VAR_PWD);
			statement.setString(3, password);
			statement.execute();
		}
		catch (Exception e)
		{
			_log.error("Error while writing password.", e);
			return false;
		}
		_password = password;
		return true;
	}
	
	public boolean insertWrongAttempt(int attempts)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_ATTEMPT))
		{
			statement.setString(1, _activeClient.getAccountName());
			statement.setString(2, VAR_WTE);
			statement.setString(3, Integer.toString(attempts));
			statement.setString(4, Integer.toString(attempts));
			statement.execute();
		}
		catch (Exception e)
		{
			_log.error("Error while writing wrong attempts.", e);
			return false;
		}
		return true;
	}
	
	public boolean changePassword(String oldPassword, String newPassword)
	{
		if (!passwordExist())
		{
			_log.warn("[SecondaryPasswordAuth]" + _activeClient.getAccountName() + " forced changePassword");
			_activeClient.closeNow();
			return false;
		}
		
		if (!checkPassword(oldPassword, true))
		{
			return false;
		}
		
		if (!validatePassword(newPassword))
		{
			_activeClient.sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.WRONG_PATTERN));
			return false;
		}
		
		newPassword = cryptPassword(newPassword);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_PASSWORD))
		{
			statement.setString(1, newPassword);
			statement.setString(2, _activeClient.getAccountName());
			statement.setString(3, VAR_PWD);
			statement.execute();
		}
		catch (Exception e)
		{
			_log.error("Error while reading password.", e);
			return false;
		}
		
		_password = newPassword;
		_authed = false;
		return true;
	}
	
	public boolean checkPassword(String password, boolean skipAuth)
	{
		password = cryptPassword(password);
		
		if (!password.equals(_password))
		{
			_wrongAttempts++;
			if (_wrongAttempts < SecondaryAuthData.getInstance().getMaxAttempts())
			{
				_activeClient.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.PASSWORD_WRONG, _wrongAttempts));
				insertWrongAttempt(_wrongAttempts);
			}
			else
			{
				LoginServerThread.getInstance().sendTempBan(_activeClient.getAccountName(), _activeClient.getConnectionAddress().getHostAddress(), SecondaryAuthData.getInstance().getBanTime());
				LoginServerThread.getInstance().sendMail(_activeClient.getAccountName(), "SATempBan", _activeClient.getConnectionAddress().getHostAddress(), Integer.toString(SecondaryAuthData.getInstance().getMaxAttempts()), Long.toString(SecondaryAuthData.getInstance().getBanTime()), SecondaryAuthData.getInstance().getRecoveryLink());
				_log.warn(_activeClient.getAccountName() + " - (" + _activeClient.getConnectionAddress().getHostAddress() + ") has inputted the wrong password " + _wrongAttempts + " times in row.");
				insertWrongAttempt(0);
				_activeClient.close(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.PASSWORD_BAN, SecondaryAuthData.getInstance().getMaxAttempts()));
			}
			return false;
		}
		if (!skipAuth)
		{
			_authed = true;
			_activeClient.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.PASSWORD_OK, _wrongAttempts));
		}
		insertWrongAttempt(0);
		return true;
	}
	
	public boolean passwordExist()
	{
		return _password == null ? false : true;
	}
	
	public void openDialog()
	{
		if (passwordExist())
		{
			_activeClient.sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.PASSWORD_PROMPT));
		}
		else
		{
			_activeClient.sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.PASSWORD_NEW));
		}
	}
	
	public boolean isAuthed()
	{
		return _authed;
	}
	
	private String cryptPassword(String password)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] raw = password.getBytes("UTF-8");
			byte[] hash = md.digest(raw);
			return Base64.getEncoder().encodeToString(hash);
		}
		catch (NoSuchAlgorithmException e)
		{
			_log.error("[SecondaryPasswordAuth]Unsupported Algorythm");
		}
		catch (UnsupportedEncodingException e)
		{
			_log.error("[SecondaryPasswordAuth]Unsupported Encoding");
		}
		return null;
	}
	
	private boolean validatePassword(String password)
	{
		if (!Util.isDigit(password))
		{
			return false;
		}
		
		if ((password.length() < 6) || (password.length() > 8))
		{
			return false;
		}
		
		for (int i = 0; i < (password.length() - 1); i++)
		{
			char curCh = password.charAt(i);
			char nxtCh = password.charAt(i + 1);
			
			if ((curCh + 1) == nxtCh)
			{
				return false;
			}
			else if ((curCh - 1) == nxtCh)
			{
				return false;
			}
			else if (curCh == nxtCh)
			{
				return false;
			}
		}
		
		for (int i = 0; i < (password.length() - 2); i++)
		{
			String toChk = password.substring(i + 1);
			StringBuffer chkEr = new StringBuffer(password.substring(i, i + 2));
			
			if (toChk.contains(chkEr))
			{
				return false;
			}
			else if (toChk.contains(chkEr.reverse()))
			{
				return false;
			}
		}
		_wrongAttempts = 0;
		return true;
	}
}