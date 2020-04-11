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
package l2r.gameserver.network.clientpackets;

import l2r.Config;
import l2r.gameserver.LoginServerThread;
import l2r.gameserver.LoginServerThread.SessionKey;
import l2r.gameserver.network.L2GameClient;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import gr.reunion.protection.Protection;

/**
 * This class ...
 * @version $Revision: 1.9.2.3.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class AuthLogin extends L2GameClientPacket
{
	private static final String _C__2B_AUTHLOGIN = "[C] 2B AuthLogin";
	
	// loginName + keys must match what the loginserver used.
	private String _loginName;
	/*
	 * private final long _key1; private final long _key2; private final long _key3; private final long _key4;
	 */
	private int _playKey1;
	private int _playKey2;
	private int _loginKey1;
	private int _loginKey2;
	
	private final byte[] _data = new byte[48];
	
	@Override
	protected void readImpl()
	{
		_loginName = readS().toLowerCase();
		_playKey2 = readD();
		_playKey1 = readD();
		_loginKey1 = readD();
		_loginKey2 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (Protection.isProtectionOn())
		{
			if (!Protection.doAuthLogin(getClient(), _data, _loginName))
			{
				return;
			}
		}
		
		final L2GameClient client = getClient();
		if (_loginName.isEmpty() || !client.isProtocolOk())
		{
			client.close((L2GameServerPacket) null);
			return;
		}
		SessionKey key = new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);
		if (Config.DEBUG)
		{
			_log.info("user:" + _loginName);
			_log.info("key:" + key);
		}
		
		// avoid potential exploits
		if (client.getAccountName() == null)
		{
			// Preventing duplicate login in case client login server socket was disconnected or this packet was not sent yet
			if (LoginServerThread.getInstance().addGameServerLogin(_loginName, client))
			{
				client.setAccountName(_loginName);
				LoginServerThread.getInstance().addWaitingClientAndSendRequest(_loginName, client, key);
			}
			else
			{
				client.close((L2GameServerPacket) null);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__2B_AUTHLOGIN;
	}
}
