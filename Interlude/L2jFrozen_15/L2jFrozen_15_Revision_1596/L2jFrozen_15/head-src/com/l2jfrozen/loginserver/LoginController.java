package com.l2jfrozen.loginserver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;

import javax.crypto.Cipher;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.crypt.Base64;
import com.l2jfrozen.crypt.ScrambledKeyPair;
import com.l2jfrozen.gameserver.datatables.GameServerTable;
import com.l2jfrozen.gameserver.datatables.GameServerTable.GameServerInfo;
import com.l2jfrozen.loginserver.network.gameserverpackets.ServerStatus;
import com.l2jfrozen.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import com.l2jfrozen.logs.Log;
import com.l2jfrozen.util.Util;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.random.Rnd;

import javolution.util.FastCollection.Record;
import javolution.util.FastList;
import javolution.util.FastMap;

public class LoginController
{
	private static final String UPDATE_ACCOUNT_LAST_SERVER_BY_LOGIN = "UPDATE accounts SET lastServer = ? WHERE login=?";
	private static final String UPDATE_ACCOUNT_ACCESS_LEVEL_BY_LOGIN = "UPDATE accounts SET access_level=? WHERE login=?";
	private static final String SELECT_ACCOUNT_ACCESS_LEVEL_BY_LOGIN = "SELECT access_level FROM accounts WHERE login=?";
	private static final String SELECT_ACCOUNT_BY_LOGIN = "SELECT password, access_level, lastServer FROM accounts WHERE login=?";
	private static final String INSERT_ACCOUNT = "INSERT INTO accounts (login,password,lastactive,access_level,lastIP) VALUES (?,?,?,?,?)";
	private static final String UPDATE_ACCOUNT = "UPDATE accounts SET lastactive=?, lastIP=? WHERE login=?";
	
	private static final String SELECT_IP_BANNED = "SELECT ip_address FROM ip_banned WHERE ip_address=?";
	
	private static final String LOGIN_LOG_PATH = "log/login/";
	
	protected class ConnectionChecker extends Thread
	{
		@Override
		public void run()
		{
			for (;;)
			{
				final long now = System.currentTimeMillis();
				if (stopNow)
				{
					break;
				}
				for (final L2LoginClient cl : clients)
				{
					try
					{
						
						if (cl != null && now - cl.getConnectionStartTime() > Config.SESSION_TTL)
						{
							// LOGGER.info("Closing "+cl.getIntetAddress()+" because idle time too long");
							cl.close(LoginFailReason.REASON_TEMP_PASS_EXPIRED);
						}
						else
						{
							clients.remove(cl);
						}
					}
					catch (final Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e.printStackTrace();
						}
						
					}
				}
				try
				{
					Thread.sleep(2500);
				}
				catch (final Exception e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
				}
			}
		}
	}
	
	protected static final Logger LOGGER = Logger.getLogger(LoginController.class);
	
	private static LoginController instance;
	
	/** Time before kicking the client if he didnt logged yet */
	private final static int LOGIN_TIMEOUT = 60 * 1000;
	
	/** Clients that are on the LS but arent assocated with a account yet */
	protected FastList<L2LoginClient> clients = new FastList<>();
	
	/** Authed Clients on LoginServer */
	protected FastMap<String, L2LoginClient> loginServerClients = new FastMap<String, L2LoginClient>().shared();
	
	private final Map<InetAddress, BanInfo> bannedIps = new FastMap<InetAddress, BanInfo>().shared();
	
	private final Map<InetAddress, FailedLoginAttempt> hackProtection;
	protected ScrambledKeyPair[] keyPairs;
	
	protected byte[][] blowfishKeys;
	private static final int BLOWFISH_KEYS = 20;
	
	public static void load() throws GeneralSecurityException
	{
		if (instance == null)
		{
			instance = new LoginController();
		}
		else
		{
			throw new IllegalStateException("LoginController can only be loaded a single time.");
		}
	}
	
	public static LoginController getInstance()
	{
		return instance;
	}
	
	private LoginController() throws GeneralSecurityException
	{
		Util.printSection("LoginController");
		
		hackProtection = new FastMap<>();
		
		keyPairs = new ScrambledKeyPair[10];
		KeyPairGenerator keygen = null;
		
		keygen = KeyPairGenerator.getInstance("RSA");
		RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
		keygen.initialize(spec);
		
		// generate the initial set of keys
		for (int i = 0; i < 10; i++)
		{
			keyPairs[i] = new ScrambledKeyPair(keygen.generateKeyPair());
		}
		
		LOGGER.info("Cached 10 KeyPairs for RSA communication");
		
		testCipher((RSAPrivateKey) keyPairs[0].pair.getPrivate());
		
		// Store keys for blowfish communication
		generateBlowFishKeys();
		
		spec = null;
		keygen = null;
		new ConnectionChecker().start();
	}
	
	/**
	 * This is mostly to force the initialization of the Crypto Implementation, avoiding it being done on runtime when its first needed.<BR>
	 * In short it avoids the worst-case execution time on runtime by doing it on loading.
	 * @param  key                      Any private RSA Key just for testing purposes.
	 * @throws GeneralSecurityException if a underlying exception was thrown by the Cipher
	 */
	private void testCipher(final RSAPrivateKey key) throws GeneralSecurityException
	{
		// avoid worst-case execution, KenM
		Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
		rsaCipher.init(Cipher.DECRYPT_MODE, key);
		rsaCipher = null;
	}
	
	protected boolean stopNow = false;
	
	public void shutdown()
	{
		stopNow = true;
		try
		{
			Thread.sleep(10000);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
		}
	}
	
	private void generateBlowFishKeys()
	{
		blowfishKeys = new byte[BLOWFISH_KEYS][16];
		
		for (int i = 0; i < BLOWFISH_KEYS; i++)
		{
			for (int j = 0; j < blowfishKeys[i].length; j++)
			{
				blowfishKeys[i][j] = (byte) (Rnd.nextInt(255) + 1);
			}
		}
		LOGGER.info("Stored " + blowfishKeys.length + " keys for Blowfish communication");
	}
	
	/**
	 * @return Returns a random key
	 */
	public byte[] getBlowfishKey()
	{
		return blowfishKeys[(int) (Math.random() * BLOWFISH_KEYS)];
	}
	
	public void addLoginClient(final L2LoginClient client)
	{
		if (clients.size() >= Config.MAX_LOGINSESSIONS)
		{
			for (final L2LoginClient cl : clients)
			{
				try
				{
					cl.close(LoginFailReason.REASON_DUAL_BOX);
				}
				catch (final Exception e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
				}
			}
		}
		synchronized (clients)
		{
			clients.add(client);
		}
	}
	
	public void removeLoginClient(final L2LoginClient client)
	{
		if (clients.contains(client))
		{
			synchronized (clients)
			{
				try
				{
					clients.remove(client);
				}
				catch (final Exception e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
				}
			}
		}
	}
	
	public SessionKey assignSessionKeyToClient(final String account, final L2LoginClient client)
	{
		SessionKey key;
		
		key = new SessionKey(Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt());
		loginServerClients.put(account, client);
		return key;
	}
	
	public void removeAuthedLoginClient(final String account)
	{
		try
		{
			loginServerClients.remove(account);
			
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
		}
	}
	
	public boolean isAccountInLoginServer(final String account)
	{
		return loginServerClients.containsKey(account);
	}
	
	public L2LoginClient getAuthedClient(final String account)
	{
		return loginServerClients.get(account);
	}
	
	public static enum AuthLoginResult
	{
		INVALID_PASSWORD,
		ACCOUNT_BANNED,
		ALREADY_ON_LS,
		ALREADY_ON_GS,
		AUTH_SUCCESS
	}
	
	public AuthLoginResult tryAuthLogin(final String account, final String password, final L2LoginClient client)
	{
		AuthLoginResult ret = AuthLoginResult.INVALID_PASSWORD;
		// check auth
		if (loginValid(account, password, client))
		{
			// login was successful, verify presence on Gameservers
			ret = AuthLoginResult.ALREADY_ON_GS;
			
			if (!isAccountInAnyGameServer(account))
			{
				// account isnt on any GS verify LS itself
				ret = AuthLoginResult.ALREADY_ON_LS;
				
				// dont allow 2 simultaneous login
				synchronized (loginServerClients)
				{
					if (!loginServerClients.containsKey(account))
					{
						loginServerClients.put(account, client);
						ret = AuthLoginResult.AUTH_SUCCESS;
						
						// remove him from the non-authed list
						removeLoginClient(client);
					}
				}
			}
		}
		else
		{
			if (client.getAccessLevel() < 0)
			{
				ret = AuthLoginResult.ACCOUNT_BANNED;
			}
		}
		return ret;
	}
	
	/**
	 * Adds the address to the ban list of the login server, with the given duration.
	 * @param  address              The Address to be banned.
	 * @param  expiration           Timestamp in miliseconds when this ban expires
	 * @throws UnknownHostException if the address is invalid.
	 */
	public void addBanForAddress(final String address, final long expiration) throws UnknownHostException
	{
		
		InetAddress netAddress = InetAddress.getByName(address);
		bannedIps.put(netAddress, new BanInfo(netAddress, expiration));
		netAddress = null;
	}
	
	/**
	 * Adds the address to the ban list of the login server, with the given duration.
	 * @param address  The Address to be banned.
	 * @param duration is miliseconds
	 */
	public void addBanForAddress(final InetAddress address, final long duration)
	{
		bannedIps.put(address, new BanInfo(address, System.currentTimeMillis() + duration));
	}
	
	public boolean isBannedAddress(final InetAddress address)
	{
		final BanInfo bi = bannedIps.get(address);
		if (bi != null)
		{
			if (bi.hasExpired())
			{
				bannedIps.remove(address);
				return false;
			}
			return true;
		}
		return false;
	}
	
	public Map<InetAddress, BanInfo> getBannedIps()
	{
		return bannedIps;
	}
	
	/**
	 * Remove the specified address from the ban list
	 * @param  address The address to be removed from the ban list
	 * @return         true if the ban was removed, false if there was no ban for this ip
	 */
	public boolean removeBanForAddress(final InetAddress address)
	{
		return bannedIps.remove(address) != null;
	}
	
	/**
	 * Remove the specified address from the ban list
	 * @param  address The address to be removed from the ban list
	 * @return         true if the ban was removed, false if there was no ban for this ip or the address was invalid.
	 */
	public boolean removeBanForAddress(final String address)
	{
		try
		{
			return this.removeBanForAddress(InetAddress.getByName(address));
		}
		catch (final UnknownHostException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return false;
		}
	}
	
	public SessionKey getKeyForAccount(final String account)
	{
		final L2LoginClient client = loginServerClients.get(account);
		
		if (client != null)
		{
			return client.getSessionKey();
		}
		
		return null;
	}
	
	public int getOnlinePlayerCount(final int serverId)
	{
		GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(serverId);
		
		if (gsi != null && gsi.isAuthed())
		{
			return gsi.getCurrentPlayerCount();
		}
		
		gsi = null;
		
		return 0;
	}
	
	public boolean isAccountInAnyGameServer(final String account)
	{
		Collection<GameServerInfo> serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
		
		for (final GameServerInfo gsi : serverList)
		{
			GameServerThread gst = gsi.getGameServerThread();
			
			if (gst != null && gst.hasAccountOnGameServer(account))
			{
				return true;
			}
			
			gst = null;
		}
		
		serverList = null;
		
		return false;
	}
	
	public GameServerInfo getAccountOnGameServer(final String account)
	{
		Collection<GameServerInfo> serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
		
		for (final GameServerInfo gsi : serverList)
		{
			GameServerThread gst = gsi.getGameServerThread();
			
			if (gst != null && gst.hasAccountOnGameServer(account))
			{
				return gsi;
			}
			
			gst = null;
		}
		
		serverList = null;
		
		return null;
	}
	
	public int getTotalOnlinePlayerCount()
	{
		int total = 0;
		Collection<GameServerInfo> serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
		
		for (final GameServerInfo gsi : serverList)
		{
			if (gsi.isAuthed())
			{
				total += gsi.getCurrentPlayerCount();
			}
		}
		
		serverList = null;
		
		return total;
	}
	
	public int getMaxAllowedOnlinePlayers(final int id)
	{
		final GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(id);
		
		if (gsi != null)
		{
			return gsi.getMaxPlayers();
		}
		
		return 0;
	}
	
	/**
	 * @param  client
	 * @param  serverId
	 * @return
	 */
	public boolean isLoginPossible(final L2LoginClient client, final int serverId)
	{
		final GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(serverId);
		final int access = client.getAccessLevel();
		if (gsi != null && gsi.isAuthed())
		{
			final boolean loginOk = gsi.getCurrentPlayerCount() < gsi.getMaxPlayers() && gsi.getStatus() != ServerStatus.STATUS_GM_ONLY || access >= 100;
			if (loginOk && client.getLastServer() != serverId)
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement(UPDATE_ACCOUNT_LAST_SERVER_BY_LOGIN);)
				{
					statement.setInt(1, serverId);
					statement.setString(2, client.getAccount());
					statement.executeUpdate();
				}
				catch (Exception e)
				{
					LOGGER.error("LoginController.isLoginPossible : Could not set lastServer for account " + client.getAccount(), e);
				}
			}
			return loginOk;
		}
		return false;
	}
	
	public void setAccountAccessLevel(final String account, final int banLevel)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
		{
			PreparedStatement statement = con.prepareStatement(UPDATE_ACCOUNT_ACCESS_LEVEL_BY_LOGIN);
			statement.setInt(1, banLevel);
			statement.setString(2, account);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("LoginController.setAccountAccessLevel : Could not set accessLevel to account " + account, e);
		}
	}
	
	public boolean isGM(final String user)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_ACCOUNT_ACCESS_LEVEL_BY_LOGIN))
		{
			statement.setString(1, user);
			
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					int accessLevel = rset.getInt("access_level");
					if (accessLevel >= 1)
					{
						return true;
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warn("LoginController.isGM : Could not check gm state", e);
		}
		return false;
	}
	
	/**
	 * <p>
	 * This method returns one of the cached {@link ScrambledKeyPair ScrambledKeyPairs} for communication with Login Clients.
	 * </p>
	 * @return a scrambled keypair
	 */
	public ScrambledKeyPair getScrambledRSAKeyPair()
	{
		return keyPairs[Rnd.nextInt(10)];
	}
	
	/**
	 * user name is not case sensitive any more
	 * @param  user
	 * @param  password
	 * @param  client
	 * @return
	 */
	public synchronized boolean loginValid(String user, String password, L2LoginClient client)
	{
		boolean ok = false;
		InetAddress address = client.getConnection().getInetAddress();
		
		// player disconnected meanwhile
		if (address == null)
		{
			return false;
		}
		
		String ipAddres = address.getHostAddress();
		
		if (!ipAddres.isEmpty())
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement pst = con.prepareStatement(SELECT_IP_BANNED))
			{
				pst.setString(1, ipAddres);
				
				try (ResultSet rs = pst.executeQuery())
				{
					if (rs.next())
					{
						LOGGER.warn("Account " + user + " with IP " + ipAddres + " banned is triying to log.");
						return false;
					}
				}
			}
			catch (Exception e)
			{
				LOGGER.error("LoginController.loginValid : Could no select ip " + ipAddres + " from ip_banned table", e);
				return false;
			}
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_ACCOUNT_BY_LOGIN))
		{
			MessageDigest md = MessageDigest.getInstance("SHA");
			final byte[] raw = password.getBytes("UTF-8");
			final byte[] hash = md.digest(raw);
			
			byte[] expected = null;
			int access = 0;
			int lastServer = 1;
			
			statement.setString(1, user);
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					expected = Base64.decode(rset.getString("password"));
					access = rset.getInt("access_level");
					lastServer = rset.getInt("lastServer");
					
					if (lastServer <= 0)
					{
						lastServer = 1; // minServerId is 1 in Interlude
					}
					
					if (Config.DEBUG)
					{
						LOGGER.debug("Account already exists.");
					}
				}
			}
			
			// if account doesnt exists
			if (expected == null)
			{
				if (Config.AUTO_CREATE_ACCOUNTS)
				{
					if ((user != null) && (user.length()) >= 2 && (user.length() <= 14))
					{
						try (PreparedStatement insertStatement = con.prepareStatement(INSERT_ACCOUNT))
						{
							insertStatement.setString(1, user);
							insertStatement.setString(2, Base64.encodeBytes(hash));
							insertStatement.setLong(3, System.currentTimeMillis());
							insertStatement.setInt(4, 0);
							insertStatement.setString(5, address.getHostAddress());
							insertStatement.executeUpdate();
						}
						
						LOGGER.info("Created new account : " + user + " on IP : " + address.getHostAddress());
						return true;
						
					}
					LOGGER.warn("Invalid username creation/use attempt: " + user);
					return false;
				}
				LOGGER.warn("Account missing for user " + user + " IP: " + address.getHostAddress());
				return false;
			}
			
			// is this account banned?
			if (access < 0)
			{
				client.setAccessLevel(access);
				return false;
			}
			
			// check password hash
			ok = true;
			for (int i = 0; i < expected.length; i++)
			{
				if (hash[i] != expected[i])
				{
					ok = false;
					break;
				}
			}
			
			if (ok)
			{
				client.setAccessLevel(access);
				client.setLastServer(lastServer);
				
				try (PreparedStatement updateStatement = con.prepareStatement(UPDATE_ACCOUNT))
				{
					updateStatement.setLong(1, System.currentTimeMillis());
					updateStatement.setString(2, address.getHostAddress());
					updateStatement.setString(3, user);
					updateStatement.executeUpdate();
				}
			}
			
			md = null;
		}
		catch (Exception e)
		{
			LOGGER.error("LoginController.loginValid : Could not check password", e);
			ok = false;
		}
		
		if (!ok)
		{
			Log.add("'" + user + "' " + address.getHostAddress(), LOGIN_LOG_PATH, "_logins_ip_fails");
			
			FailedLoginAttempt failedAttempt = hackProtection.get(address);
			
			int failedCount;
			if (failedAttempt == null)
			{
				hackProtection.put(address, new FailedLoginAttempt(address, password));
				failedCount = 1;
			}
			else
			{
				failedAttempt.increaseCounter(password);
				failedCount = failedAttempt.getCount();
			}
			
			if (failedCount >= Config.LOGIN_TRY_BEFORE_BAN)
			{
				LOGGER.info("Banning '" + address.getHostAddress() + "' for " + Config.LOGIN_BLOCK_AFTER_BAN + " seconds due to " + failedCount + " invalid user/pass attempts");
				this.addBanForAddress(address, Config.LOGIN_BLOCK_AFTER_BAN * 1000);
			}
			
			failedAttempt = null;
		}
		else
		{
			hackProtection.remove(address);
			// General log
			Log.add("'" + user + "' " + address.getHostAddress(), LOGIN_LOG_PATH, "_logins_ip");
			// Individual log
			Log.add("'" + user + "' " + address.getHostAddress(), LOGIN_LOG_PATH + "accounts/", user);
		}
		
		address = null;
		
		return ok;
	}
	
	public boolean loginBanned(final String user)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_ACCOUNT_ACCESS_LEVEL_BY_LOGIN);)
		{
			statement.setString(1, user);
			
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					int accessLevel = rset.getInt("access_level");
					if (accessLevel < 0)
					{
						return true;
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warn("LoginController.loginBanned : Could not check ban state", e);
		}
		
		return false;
	}
	
	class FailedLoginAttempt
	{
		// private InetAddress ipAddress;
		private int count;
		private long lastAttempTime;
		private String lastPassword;
		
		public FailedLoginAttempt(final InetAddress address, final String lastPassword)
		{
			// ipAddress = address;
			count = 1;
			lastAttempTime = System.currentTimeMillis();
			this.lastPassword = lastPassword;
		}
		
		public void increaseCounter(final String password)
		{
			if (!lastPassword.equals(password))
			{
				// check if theres a long time since last wrong try
				if (System.currentTimeMillis() - lastAttempTime < 300 * 1000)
				{
					count++;
				}
				else
				{
					// restart the status
					count = 1;
					
				}
				lastPassword = password;
				lastAttempTime = System.currentTimeMillis();
			}
			else
			// trying the same password is not brute force
			{
				lastAttempTime = System.currentTimeMillis();
			}
		}
		
		public int getCount()
		{
			return count;
		}
	}
	
	class BanInfo
	{
		private final InetAddress ipAddress;
		// Expiration
		private final long expiration;
		
		public BanInfo(final InetAddress ipAddress, final long expiration)
		{
			this.ipAddress = ipAddress;
			this.expiration = expiration;
		}
		
		public InetAddress getAddress()
		{
			return ipAddress;
		}
		
		public boolean hasExpired()
		{
			return System.currentTimeMillis() > expiration && expiration > 0;
		}
	}
	
	class PurgeThread extends Thread
	{
		@Override
		public void run()
		{
			for (;;)
			{
				synchronized (clients)
				{
					for (Record e = clients.head(), end = clients.tail(); (e = e.getNext()) != end;)
					{
						L2LoginClient client = clients.valueOf(e);
						if (client.getConnectionStartTime() + LOGIN_TIMEOUT >= System.currentTimeMillis())
						{
							client.close(LoginFailReason.REASON_ACCESS_FAILED);
						}
						
						client = null;
					}
				}
				
				synchronized (loginServerClients)
				{
					for (FastMap.Entry<String, L2LoginClient> e = loginServerClients.head(), end = loginServerClients.tail(); (e = e.getNext()) != end;)
					{
						L2LoginClient client = e.getValue();
						if (client.getConnectionStartTime() + LOGIN_TIMEOUT >= System.currentTimeMillis())
						{
							client.close(LoginFailReason.REASON_ACCESS_FAILED);
						}
						client = null;
					}
				}
				
				try
				{
					Thread.sleep(2 * LOGIN_TIMEOUT);
				}
				catch (final InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
