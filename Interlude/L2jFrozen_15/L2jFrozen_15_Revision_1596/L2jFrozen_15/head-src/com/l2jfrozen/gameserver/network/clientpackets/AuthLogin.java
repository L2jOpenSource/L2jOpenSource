package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.thread.LoginServerThread;
import com.l2jfrozen.gameserver.thread.LoginServerThread.SessionKey;

public final class AuthLogin extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(AuthLogin.class);
	
	// loginName + keys must match what the loginserver used.
	private String loginName;
	private int playKey1;
	private int playKey2;
	private int loginKey1;
	private int loginKey2;
	
	@Override
	protected void readImpl()
	{
		loginName = readS().toLowerCase();
		playKey2 = readD();
		playKey1 = readD();
		loginKey1 = readD();
		loginKey2 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final SessionKey key = new SessionKey(loginKey1, loginKey2, playKey1, playKey2);
		
		if (Config.DEBUG)
		{
			LOGGER.info("DEBUG " + getType() + ": user: " + loginName + " key:" + key);
		}
		
		final L2GameClient client = getClient();
		
		// avoid potential exploits
		if (client.getAccountName() == null)
		{
			// Preventing duplicate login in case client login server socket was
			// disconnected or this packet was not sent yet
			if (LoginServerThread.getInstance().addGameServerLogin(loginName, client))
			{
				client.setAccountName(loginName);
				LoginServerThread.getInstance().addWaitingClientAndSendRequest(loginName, client, key);
			}
			else
			{
				client.closeNow();
			}
			
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 08 AuthLogin";
	}
}