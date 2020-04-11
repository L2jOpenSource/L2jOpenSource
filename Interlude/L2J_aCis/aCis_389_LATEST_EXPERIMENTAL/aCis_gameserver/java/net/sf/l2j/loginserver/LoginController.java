package net.sf.l2j.loginserver;

import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;

import net.sf.l2j.commons.crypt.BCrypt;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.loginserver.crypt.ScrambledKeyPair;
import net.sf.l2j.loginserver.data.manager.GameServerManager;
import net.sf.l2j.loginserver.data.manager.IpBanManager;
import net.sf.l2j.loginserver.data.sql.AccountTable;
import net.sf.l2j.loginserver.enums.AccountKickedReason;
import net.sf.l2j.loginserver.enums.LoginClientState;
import net.sf.l2j.loginserver.model.Account;
import net.sf.l2j.loginserver.model.GameServerInfo;
import net.sf.l2j.loginserver.network.LoginClient;
import net.sf.l2j.loginserver.network.SessionKey;
import net.sf.l2j.loginserver.network.serverpackets.AccountKicked;
import net.sf.l2j.loginserver.network.serverpackets.LoginFail;
import net.sf.l2j.loginserver.network.serverpackets.LoginOk;
import net.sf.l2j.loginserver.network.serverpackets.ServerList;

public class LoginController
{
	protected static final CLogger LOGGER = new CLogger(LoginController.class.getName());
	
	public static final int LOGIN_TIMEOUT = 60 * 1000;
	
	private final Map<String, LoginClient> _clients = new ConcurrentHashMap<>();
	private final Map<InetAddress, Integer> _failedAttempts = new ConcurrentHashMap<>();
	
	protected ScrambledKeyPair[] _keyPairs;
	
	protected byte[][] _blowfishKeys;
	private static final int BLOWFISH_KEYS = 20;
	
	protected LoginController()
	{
		_keyPairs = new ScrambledKeyPair[10];
		
		// Generate keys.
		try
		{
			final KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
			final RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
			
			// Initialize the key pair generator.
			keygen.initialize(spec);
			
			// Generate the initial set of keys.
			for (int i = 0; i < 10; i++)
				_keyPairs[i] = new ScrambledKeyPair(keygen.generateKeyPair());
			
			LOGGER.info("Cached 10 KeyPairs for RSA communication.");
			
			// Test the cipher.
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, _keyPairs[0].getKeyPair().getPrivate());
			
			// Store keys for blowfish communication.
			_blowfishKeys = new byte[BLOWFISH_KEYS][16];
			
			for (int i = 0; i < BLOWFISH_KEYS; i++)
			{
				for (int j = 0; j < _blowfishKeys[i].length; j++)
					_blowfishKeys[i][j] = (byte) (Rnd.get(255) + 1);
			}
			LOGGER.info("Stored {} keys for Blowfish communication.", _blowfishKeys.length);
		}
		catch (GeneralSecurityException gse)
		{
			LOGGER.error("Failed generating keys.", gse);
		}
		
		// "Dropping AFK connections on login" task.
		final Thread purge = new PurgeThread();
		purge.setDaemon(true);
		purge.start();
	}
	
	public byte[] getBlowfishKey()
	{
		return _blowfishKeys[(int) (Math.random() * BLOWFISH_KEYS)];
	}
	
	public void removeAuthedLoginClient(String account)
	{
		if (account == null)
			return;
		
		_clients.remove(account);
	}
	
	public LoginClient getAuthedClient(String account)
	{
		return _clients.get(account);
	}
	
	/**
	 * Update attempts counter. If the maximum amount is reached, it will end with a client ban.
	 * @param address : The {@link InetAddress} to test.
	 */
	private void recordFailedAttempt(InetAddress address)
	{
		final int attempts = _failedAttempts.merge(address, 1, (k, v) -> k + v);
		if (attempts >= Config.LOGIN_TRY_BEFORE_BAN)
		{
			// Add a ban for the given InetAddress.
			IpBanManager.getInstance().addBanForAddress(address, Config.LOGIN_BLOCK_AFTER_BAN * 1000);
			
			// Clear all failed login attempts.
			_failedAttempts.remove(address);
			
			LOGGER.info("IP address: {} has been banned due to too many login attempts.", address.getHostAddress());
		}
	}
	
	/**
	 * If passwords don't match, register the failed attempt and eventually ban the {@link InetAddress} if AUTO_CREATE_ACCOUNTS is off.
	 * @param client : The {@link LoginClient} to eventually ban after multiple failed attempts.
	 * @param login : The {@link String} login to test.
	 * @param password : The {@link String} password to test.
	 */
	public void retrieveAccountInfo(LoginClient client, String login, String password)
	{
		final InetAddress addr = client.getConnection().getInetAddress();
		final long currentTime = System.currentTimeMillis();
		
		// Retrieve or create (if auto-create is on) an Account based on given login and password.
		Account account = AccountTable.getInstance().getAccount(login);
		
		if (account == null)
		{
			// Auto-create is off, add a failed attempt.
			if (!Config.AUTO_CREATE_ACCOUNTS)
			{
				recordFailedAttempt(addr);
				client.close(LoginFail.REASON_USER_OR_PASS_WRONG);
				return;
			}
			
			// Generate an Account and feed variable.
			account = AccountTable.getInstance().createAccount(login, BCrypt.hashpw(password, BCrypt.gensalt()), currentTime);
			if (account == null)
			{
				client.close(LoginFail.REASON_ACCESS_FAILED);
				return;
			}
			
			LOGGER.info("Auto created account '{}'.", login);
		}
		else
		{
			// Check if that an unencrypted password matches one that has previously been hashed.
			if (!BCrypt.checkpw(password, account.getPassword()))
			{
				recordFailedAttempt(addr);
				client.close(LoginFail.REASON_PASS_WRONG);
				return;
			}
			
			// Clear all failed login attempts.
			_failedAttempts.remove(addr);
			
			// Refresh current time of the account.
			if (!AccountTable.getInstance().setAccountLastTime(login, currentTime))
			{
				client.close(LoginFail.REASON_ACCESS_FAILED);
				return;
			}
		}
		
		// Account is banned, return.
		if (account.getAccessLevel() < 0)
		{
			client.close(new AccountKicked(AccountKickedReason.PERMANENTLY_BANNED));
			return;
		}
		
		// Account is already set on ls, return.
		if (isAccountInAnyGameServer(login))
		{
			final GameServerInfo gsi = LoginController.getInstance().getAccountOnGameServer(login);
			if (gsi != null)
			{
				client.close(LoginFail.REASON_ACCOUNT_IN_USE);
				
				if (gsi.isAuthed())
					gsi.getGameServerThread().kickPlayer(login);
			}
			return;
		}
		
		// Account is already set on gs, close the previous client.
		if (_clients.putIfAbsent(login, client) != null)
		{
			final LoginClient oldClient = LoginController.getInstance().getAuthedClient(login);
			if (oldClient != null)
			{
				oldClient.close(LoginFail.REASON_ACCOUNT_IN_USE);
				LoginController.getInstance().removeAuthedLoginClient(login);
			}
			client.close(LoginFail.REASON_ACCOUNT_IN_USE);
			return;
		}
		
		client.setAccount(account);
		client.setState(LoginClientState.AUTHED_LOGIN);
		client.setSessionKey(new SessionKey(Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt()));
		client.sendPacket((Config.SHOW_LICENCE) ? new LoginOk(client.getSessionKey()) : new ServerList(account));
	}
	
	public SessionKey getKeyForAccount(String account)
	{
		final LoginClient client = _clients.get(account);
		return (client == null) ? null : client.getSessionKey();
	}
	
	public boolean isAccountInAnyGameServer(String account)
	{
		for (GameServerInfo gsi : GameServerManager.getInstance().getRegisteredGameServers().values())
		{
			final GameServerThread gst = gsi.getGameServerThread();
			if (gst != null && gst.hasAccountOnGameServer(account))
				return true;
		}
		return false;
	}
	
	public GameServerInfo getAccountOnGameServer(String account)
	{
		for (GameServerInfo gsi : GameServerManager.getInstance().getRegisteredGameServers().values())
		{
			final GameServerThread gst = gsi.getGameServerThread();
			if (gst != null && gst.hasAccountOnGameServer(account))
				return gsi;
		}
		return null;
	}
	
	/**
	 * @return One of the cached {@link ScrambledKeyPair}s to communicate with Login Clients.
	 */
	public ScrambledKeyPair getScrambledRSAKeyPair()
	{
		return Rnd.get(_keyPairs);
	}
	
	private class PurgeThread extends Thread
	{
		public PurgeThread()
		{
			setName("PurgeThread");
		}
		
		@Override
		public void run()
		{
			while (!isInterrupted())
			{
				for (LoginClient client : _clients.values())
				{
					if ((client.getConnectionStartTime() + LOGIN_TIMEOUT) < System.currentTimeMillis())
						client.close(LoginFail.REASON_ACCESS_FAILED);
				}
				
				try
				{
					Thread.sleep(LOGIN_TIMEOUT / 2);
				}
				catch (InterruptedException e)
				{
					return;
				}
			}
		}
	}
	
	public static LoginController getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final LoginController INSTANCE = new LoginController();
	}
}