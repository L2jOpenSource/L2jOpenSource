package com.l2jfrozen.loginserver.network.clientpackets;

import java.io.IOException;
import java.net.InetAddress;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.GameServerTable.GameServerInfo;
import com.l2jfrozen.loginserver.L2LoginClient;
import com.l2jfrozen.loginserver.L2LoginClient.LoginClientState;
import com.l2jfrozen.loginserver.LoginController;
import com.l2jfrozen.loginserver.LoginController.AuthLoginResult;
import com.l2jfrozen.loginserver.network.serverpackets.AccountKicked;
import com.l2jfrozen.loginserver.network.serverpackets.AccountKicked.AccountKickedReason;
import com.l2jfrozen.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import com.l2jfrozen.loginserver.network.serverpackets.LoginOk;
import com.l2jfrozen.loginserver.network.serverpackets.ServerList;

/**
 * Format: x 0 (a leading null) x: the rsa encrypted block with the login an password
 */
public class RequestAuthLogin extends L2LoginClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestAuthLogin.class);
	
	private final byte[] raw = new byte[128];
	
	private String user;
	private String password;
	private int ncotp;
	
	/**
	 * @return
	 */
	public String getPassword()
	{
		return password;
	}
	
	/**
	 * @return
	 */
	public String getUser()
	{
		return user;
	}
	
	public int getOneTimePassword()
	{
		return ncotp;
	}
	
	@Override
	public boolean readImpl()
	{
		if (super.buf.remaining() >= 128)
		{
			readB(raw);
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		byte[] decrypted = null;
		try
		{
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, getClient().getRSAPrivateKey());
			decrypted = rsaCipher.doFinal(raw, 0x00, 0x80);
			rsaCipher = null;
		}
		catch (final GeneralSecurityException e)
		{
			e.printStackTrace();
			return;
		}
		
		user = new String(decrypted, 0x5E, 14).trim();
		user = user.toLowerCase();
		password = new String(decrypted, 0x6C, 16).trim();
		ncotp = decrypted[0x7c];
		ncotp |= decrypted[0x7d] << 8;
		ncotp |= decrypted[0x7e] << 16;
		ncotp |= decrypted[0x7f] << 24;
		
		LoginController lc = LoginController.getInstance();
		L2LoginClient client = getClient();
		final InetAddress address = getClient().getConnection().getInetAddress();
		if (address == null)
		{
			LOGGER.warn("Socket is not connected: " + client.getAccount());
			client.close(LoginFailReason.REASON_SYSTEM_ERROR);
			return;
		}
		final String addhost = address.getHostAddress();
		AuthLoginResult result = lc.tryAuthLogin(user, password, getClient());
		
		switch (result)
		{
			case AUTH_SUCCESS:
				client.setAccount(user);
				client.setState(LoginClientState.AUTHED_LOGIN);
				client.setSessionKey(lc.assignSessionKeyToClient(user, client));
				if (Config.SHOW_LICENCE)
				{
					client.sendPacket(new LoginOk(getClient().getSessionKey()));
				}
				else
				{
					getClient().sendPacket(new ServerList(getClient()));
				}
				if (Config.ENABLE_DDOS_PROTECTION_SYSTEM)
				{
					String deny_comms = Config.DDOS_COMMAND_BLOCK;
					deny_comms = deny_comms.replace("$IP", addhost);
					
					try
					{
						Runtime.getRuntime().exec(deny_comms);
						if (Config.ENABLE_DEBUG_DDOS_PROTECTION_SYSTEM)
						{
							LOGGER.info("Accepted IP access GS by " + addhost);
							LOGGER.info("Command is" + deny_comms);
						}
						
					}
					catch (final IOException e1)
					{
						LOGGER.info("Accepts by ip " + addhost + " no allowed");
						LOGGER.info("Command is" + deny_comms);
					}
					
				}
				
				break;
			case INVALID_PASSWORD:
				client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
				break;
			case ACCOUNT_BANNED:
				client.close(new AccountKicked(AccountKickedReason.REASON_PERMANENTLY_BANNED));
				break;
			case ALREADY_ON_LS:
				L2LoginClient oldClient;
				if ((oldClient = lc.getAuthedClient(user)) != null)
				{
					// kick the other client
					oldClient.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
					lc.removeAuthedLoginClient(user);
				}
				oldClient = null;
				break;
			case ALREADY_ON_GS:
				GameServerInfo gsi;
				if ((gsi = lc.getAccountOnGameServer(user)) != null)
				{
					client.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
					
					// kick from there
					if (gsi.isAuthed())
					{
						gsi.getGameServerThread().kickPlayer(user);
					}
				}
				gsi = null;
				break;
		}
		
		result = null;
		
		decrypted = null;
		lc = null;
		client = null;
	}
}
