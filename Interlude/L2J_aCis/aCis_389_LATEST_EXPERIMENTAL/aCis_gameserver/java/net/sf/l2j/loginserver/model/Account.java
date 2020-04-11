package net.sf.l2j.loginserver.model;

import net.sf.l2j.commons.network.StatusType;

import net.sf.l2j.loginserver.data.manager.GameServerManager;
import net.sf.l2j.loginserver.data.sql.AccountTable;

public final class Account
{
	private final String _login;
	private final String _password;
	private final int _accessLevel;
	private final int _lastServer;
	
	public Account(final String login, final String password, final int accessLevel, final int lastServer)
	{
		_login = login.toLowerCase();
		_password = password;
		_accessLevel = accessLevel;
		_lastServer = lastServer;
	}
	
	public String getLogin()
	{
		return _login;
	}
	
	public String getPassword()
	{
		return _password;
	}
	
	public int getAccessLevel()
	{
		return _accessLevel;
	}
	
	public int getLastServer()
	{
		return _lastServer;
	}
	
	public final boolean isLoginPossible(int serverId)
	{
		final GameServerInfo gsi = GameServerManager.getInstance().getRegisteredGameServers().get(serverId);
		if (gsi == null || !gsi.isAuthed())
			return false;
		
		final StatusType status = gsi.getStatus();
		
		// DOWN status doesn't allow anyone to logon.
		if (status == StatusType.DOWN)
			return false;
		
		// GM_ONLY status or full server only allows superior access levels accounts to login. Otherwise, any positive access level account can login.
		final boolean canLogin = (status == StatusType.GM_ONLY || gsi.getCurrentPlayerCount() >= gsi.getMaxPlayers()) ? _accessLevel > 0 : _accessLevel >= 0;
		if (canLogin && _lastServer != serverId)
			AccountTable.getInstance().setAccountLastServer(_login, serverId);
		
		return canLogin;
	}
}