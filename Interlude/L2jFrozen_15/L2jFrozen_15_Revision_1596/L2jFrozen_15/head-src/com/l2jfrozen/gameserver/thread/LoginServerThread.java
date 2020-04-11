package com.l2jfrozen.gameserver.thread;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.crypt.NewCrypt;
import com.l2jfrozen.gameserver.GameServer;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.network.L2GameClient.GameClientState;
import com.l2jfrozen.gameserver.network.gameserverpackets.AuthRequest;
import com.l2jfrozen.gameserver.network.gameserverpackets.BlowFishKey;
import com.l2jfrozen.gameserver.network.gameserverpackets.ChangeAccessLevel;
import com.l2jfrozen.gameserver.network.gameserverpackets.GameServerBasePacket;
import com.l2jfrozen.gameserver.network.gameserverpackets.PlayerAuthRequest;
import com.l2jfrozen.gameserver.network.gameserverpackets.PlayerInGame;
import com.l2jfrozen.gameserver.network.gameserverpackets.PlayerLogout;
import com.l2jfrozen.gameserver.network.gameserverpackets.ServerStatus;
import com.l2jfrozen.gameserver.network.loginserverpackets.AuthResponse;
import com.l2jfrozen.gameserver.network.loginserverpackets.InitLS;
import com.l2jfrozen.gameserver.network.loginserverpackets.KickPlayer;
import com.l2jfrozen.gameserver.network.loginserverpackets.LoginServerFail;
import com.l2jfrozen.gameserver.network.loginserverpackets.PlayerAuthResponse;
import com.l2jfrozen.gameserver.network.serverpackets.AuthLoginFail;
import com.l2jfrozen.gameserver.network.serverpackets.CharSelectInfo;
import com.l2jfrozen.util.Util;
import com.l2jfrozen.util.random.Rnd;

public class LoginServerThread extends Thread
{
	protected static final Logger LOGGER = Logger.getLogger(LoginServerThread.class);
	
	/** The LoginServerThread singleton */
	private static LoginServerThread instance;
	
	private static final int REVISION = 0x0102;
	private RSAPublicKey publicKey;
	private final String hostname;
	private final int port;
	private final int gamePort;
	private Socket loginSocket;
	private InputStream in;
	private OutputStream out;
	
	/**
	 * The BlowFish engine used to encrypt packets<br>
	 * It is first initialized with a unified key:<br>
	 * "_;v.]05-31!|+-%xT!^[$\00"<br>
	 * <br>
	 * and then after handshake, with a new key sent by<br>
	 * loginserver during the handshake. This new key is stored<br>
	 * in {@link #blowfishKey}
	 */
	private NewCrypt blowfish;
	private byte[] blowfishKey;
	private byte[] hexID;
	private final boolean acceptAlternate;
	private int requestID;
	private int serverID;
	private final boolean reserveHost;
	private int maxPlayer;
	private final List<WaitingClient> waitingClients;
	private final Map<String, L2GameClient> accountsInGameServer;
	private int statusNumber;
	private String serverName;
	private final String gameExternalHost;
	private final String gameInternalHost;
	
	public LoginServerThread()
	{
		super("LoginServerThread");
		port = Config.GAME_SERVER_LOGIN_PORT;
		gamePort = Config.PORT_GAME;
		hostname = Config.GAME_SERVER_LOGIN_HOST;
		hexID = Config.HEX_ID;
		if (hexID == null)
		{
			requestID = Config.REQUEST_ID;
			hexID = generateHex(16);
		}
		else
		{
			requestID = Config.SERVER_ID;
		}
		acceptAlternate = Config.ACCEPT_ALTERNATE_ID;
		reserveHost = Config.RESERVE_HOST_ON_LOGIN;
		gameExternalHost = Config.EXTERNAL_HOSTNAME;
		gameInternalHost = Config.INTERNAL_HOSTNAME;
		waitingClients = new ArrayList<>();
		accountsInGameServer = new ConcurrentHashMap<>();
		maxPlayer = Config.MAXIMUM_ONLINE_USERS;
	}
	
	public static LoginServerThread getInstance()
	{
		if (instance == null)
		{
			instance = new LoginServerThread();
		}
		return instance;
	}
	
	@Override
	public void run()
	{
		while (!interrupted)
		{
			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			try
			{
				// Connection
				LOGGER.info("Connecting to login on " + hostname + ":" + port);
				loginSocket = new Socket(hostname, port);
				in = loginSocket.getInputStream();
				
				if (out != null)
				{
					synchronized (out) // avoids tow threads writing in the mean time
					{
						out = new BufferedOutputStream(loginSocket.getOutputStream());
					}
				}
				else
				{
					out = new BufferedOutputStream(loginSocket.getOutputStream());
				}
				
				// init Blowfish
				blowfishKey = generateHex(40);
				blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
				while (!interrupted)
				{
					lengthLo = in.read();
					lengthHi = in.read();
					length = lengthHi * 256 + lengthLo;
					
					if (lengthHi < 0)
					{
						LOGGER.info("LoginServerThread: Login terminated the connection.");
						break;
					}
					
					final byte[] incoming = new byte[length];
					incoming[0] = (byte) lengthLo;
					incoming[1] = (byte) lengthHi;
					
					int receivedBytes = 0;
					int newBytes = 0;
					while (newBytes != -1 && receivedBytes < length - 2)
					{
						newBytes = in.read(incoming, 2, length - 2);
						receivedBytes = receivedBytes + newBytes;
					}
					
					if (receivedBytes != length - 2)
					{
						LOGGER.warn("Incomplete Packet is sent to the server, closing connection.(LS)");
						break;
					}
					
					byte[] decrypt = new byte[length - 2];
					System.arraycopy(incoming, 2, decrypt, 0, decrypt.length);
					// decrypt if we have a key
					decrypt = blowfish.decrypt(decrypt);
					checksumOk = NewCrypt.verifyChecksum(decrypt);
					
					if (!checksumOk)
					{
						LOGGER.warn("Incorrect packet checksum, ignoring packet (LS)");
						break;
					}
					
					if (Config.DEBUG)
					{
						LOGGER.warn("[C]\n" + Util.printData(decrypt));
					}
					
					final int packetType = decrypt[0] & 0xff;
					switch (packetType)
					{
						case 00:
							final InitLS init = new InitLS(decrypt);
							if (Config.DEBUG)
							{
								LOGGER.info("Init received");
							}
							if (init.getRevision() != REVISION)
							{
								// TODO: revision mismatch
								LOGGER.warn("/!\\ Revision mismatch between LS and GS /!\\");
								break;
							}
							try
							{
								final KeyFactory kfac = KeyFactory.getInstance("RSA");
								final BigInteger modulus = new BigInteger(init.getRSAKey());
								final RSAPublicKeySpec kspec1 = new RSAPublicKeySpec(modulus, RSAKeyGenParameterSpec.F4);
								publicKey = (RSAPublicKey) kfac.generatePublic(kspec1);
								if (Config.DEBUG)
								{
									LOGGER.info("RSA key set up");
								}
							}
							
							catch (final GeneralSecurityException e)
							{
								if (Config.ENABLE_ALL_EXCEPTIONS)
								{
									e.printStackTrace();
								}
								
								LOGGER.warn("Troubles while init the public key send by login");
								break;
							}
							// send the blowfish key through the rsa encryption
							final BlowFishKey bfk = new BlowFishKey(blowfishKey, publicKey);
							sendPacket(bfk);
							if (Config.DEBUG)
							{
								LOGGER.info("Sent new blowfish key");
							}
							// now, only accept paket with the new encryption
							blowfish = new NewCrypt(blowfishKey);
							if (Config.DEBUG)
							{
								LOGGER.info("Changed blowfish key");
							}
							final AuthRequest ar = new AuthRequest(requestID, acceptAlternate, hexID, gameExternalHost, gameInternalHost, gamePort, reserveHost, maxPlayer);
							sendPacket(ar);
							if (Config.DEBUG)
							{
								LOGGER.info("Sent AuthRequest to login");
							}
							break;
						case 01:
							final LoginServerFail lsf = new LoginServerFail(decrypt);
							LOGGER.info("Damn! Registeration Failed: " + lsf.getReasonString());
							// login will close the connection here
							break;
						case 02:
							final AuthResponse aresp = new AuthResponse(decrypt);
							serverID = aresp.getServerId();
							serverName = aresp.getServerName();
							Config.saveHexid(serverID, hexToString(hexID));
							LOGGER.info("Registered on login as Server " + serverID + " : " + serverName);
							final ServerStatus st = new ServerStatus();
							if (Config.SERVER_LIST_BRACKET)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.OFF);
							}
							if (Config.SERVER_LIST_CLOCK)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_CLOCK, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_CLOCK, ServerStatus.OFF);
							}
							if (Config.SERVER_LIST_TESTSERVER)
							{
								st.addAttribute(ServerStatus.TEST_SERVER, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.TEST_SERVER, ServerStatus.OFF);
							}
							if (Config.SERVER_GMONLY)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
							}
							sendPacket(st);
							if (L2World.getAllPlayersCount() > 0)
							{
								final ArrayList<String> playerList = new ArrayList<>();
								
								for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
								{
									playerList.add(player.getAccountName());
								}
								
								final PlayerInGame pig = new PlayerInGame(playerList);
								sendPacket(pig);
							}
							break;
						case 03:
							final PlayerAuthResponse par = new PlayerAuthResponse(decrypt);
							final String account = par.getAccount();
							WaitingClient wcToRemove = null;
							synchronized (waitingClients)
							{
								for (final WaitingClient wc : waitingClients)
								{
									if (wc.account.equals(account))
									{
										wcToRemove = wc;
									}
								}
							}
							if (wcToRemove != null)
							{
								if (par.isAuthed())
								{
									if (Config.DEBUG)
									{
										LOGGER.info("Login accepted player " + wcToRemove.account + " waited(" + (GameTimeController.getGameTicks() - wcToRemove.timestamp) + "ms)");
									}
									PlayerInGame pig = new PlayerInGame(par.getAccount());
									sendPacket(pig);
									pig = null;
									
									wcToRemove.gameClient.setState(GameClientState.AUTHED);
									wcToRemove.gameClient.setSessionId(wcToRemove.session);
									
									// before the char selection, check shutdown status
									if (GameServer.getSelectorThread().isShutdown())
									{
										wcToRemove.gameClient.getConnection().sendPacket(new AuthLoginFail(1));
										wcToRemove.gameClient.closeNow();
									}
									else
									{
										CharSelectInfo cl = new CharSelectInfo(wcToRemove.account, wcToRemove.gameClient.getSessionId().playOkID1);
										wcToRemove.gameClient.getConnection().sendPacket(cl);
										wcToRemove.gameClient.setCharSelection(cl.getCharInfo());
										cl = null;
									}
									
								}
								else
								{
									LOGGER.warn("Session key is not correct. Closing connection for account " + wcToRemove.account + ".");
									wcToRemove.gameClient.getConnection().sendPacket(new AuthLoginFail(1));
									wcToRemove.gameClient.closeNow();
								}
								waitingClients.remove(wcToRemove);
							}
							break;
						case 04:
							KickPlayer kp = new KickPlayer(decrypt);
							doKickPlayer(kp.getAccount());
							kp = null;
							break;
					}
				}
			}
			catch (final UnknownHostException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				LOGGER.info("Deconnected from Login, Trying to reconnect:");
				LOGGER.info(e.toString());
			}
			catch (final IOException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				LOGGER.info("Deconnected from Login, Trying to reconnect..");
				// LOGGER.info(e.toString());
			}
			finally
			{
				if (out != null)
				{
					synchronized (out) // avoids tow threads writing in the mean time
					{
						try
						{
							loginSocket.close();
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
				else
				{
					try
					{
						loginSocket.close();
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
			
			try
			{
				Thread.sleep(5000); // 5 seconds
			}
			catch (final InterruptedException e)
			{
				// no need exception LOGGER
			}
		}
	}
	
	public void addWaitingClientAndSendRequest(final String acc, final L2GameClient client, final SessionKey key)
	{
		if (Config.DEBUG)
		{
			LOGGER.info(key);
		}
		
		final WaitingClient wc = new WaitingClient(acc, client, key);
		
		synchronized (waitingClients)
		{
			waitingClients.add(wc);
		}
		
		PlayerAuthRequest par = new PlayerAuthRequest(acc, key);
		
		try
		{
			sendPacket(par);
		}
		catch (final IOException e)
		{
			LOGGER.warn("Error while sending player auth request");
			
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
		
		par = null;
	}
	
	public void removeWaitingClient(final L2GameClient client)
	{
		WaitingClient toRemove = null;
		
		synchronized (waitingClients)
		{
			for (final WaitingClient c : waitingClients)
			{
				if (c.gameClient == client)
				{
					toRemove = c;
				}
			}
			
			if (toRemove != null)
			{
				waitingClients.remove(toRemove);
			}
		}
		
		toRemove = null;
	}
	
	public void sendLogout(final String account)
	{
		if (account == null)
		{
			return;
		}
		PlayerLogout pl = new PlayerLogout(account);
		try
		{
			sendPacket(pl);
		}
		catch (final IOException e)
		{
			LOGGER.warn("Error while sending logout packet to login");
			
			e.printStackTrace();
		}
		
		pl = null;
	}
	
	/*
	 * public void addGameServerLogin(String account, L2GameClient client) { accountsInGameServer.put(account, client); }
	 */
	
	public boolean addGameServerLogin(final String account, final L2GameClient client)
	{
		
		final L2GameClient savedClient = accountsInGameServer.get(account);
		
		if (savedClient != null)
		{
			if (savedClient.isDetached())
			{
				if (Config.DEBUG)
				{
					LOGGER.info("Old Client was disconnected: Offline or OfflineMode --> Login Again");
				}
				accountsInGameServer.put(account, client);
				
				return true;
			}
			if (Config.DEBUG)
			{
				LOGGER.info("Old Client was online --> Close Old Client Connection");
			}
			
			savedClient.closeNow();
			accountsInGameServer.remove(account);
			return false;
		}
		
		if (Config.DEBUG)
		{
			LOGGER.info("Client was not online --> New Client Connection");
		}
		
		accountsInGameServer.put(account, client);
		
		return true;
	}
	
	public void sendAccessLevel(final String account, final int level)
	{
		ChangeAccessLevel cal = new ChangeAccessLevel(account, level);
		try
		{
			sendPacket(cal);
		}
		catch (final IOException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
		
		cal = null;
	}
	
	private String hexToString(final byte[] hex)
	{
		return new BigInteger(hex).toString(16);
	}
	
	public void doKickPlayer(final String account)
	{
		if (accountsInGameServer.get(account) != null)
		{
			accountsInGameServer.get(account).closeNow();
			LoginServerThread.getInstance().sendLogout(account);
			
			if (Config.DEBUG)
			{
				LOGGER.debug("called [doKickPlayer], closing connection");
			}
			
		}
	}
	
	public static byte[] generateHex(final int size)
	{
		final byte[] array = new byte[size];
		Rnd.nextBytes(array);
		if (Config.DEBUG)
		{
			LOGGER.debug("Generated random String:  \"" + array + "\"");
		}
		return array;
	}
	
	/**
	 * @param  sl
	 * @throws IOException
	 */
	private void sendPacket(final GameServerBasePacket sl) throws IOException
	{
		if (interrupted)
		{
			return;
		}
		
		byte[] data = sl.getContent();
		NewCrypt.appendChecksum(data);
		if (Config.DEBUG)
		{
			LOGGER.debug("[S]\n" + Util.printData(data));
		}
		data = blowfish.crypt(data);
		
		final int len = data.length + 2;
		
		if (out != null && !loginSocket.isClosed() && loginSocket.isConnected())
		{
			synchronized (out) // avoids tow threads writing in the mean time
			{
				out.write(len & 0xff);
				out.write(len >> 8 & 0xff);
				out.write(data);
				out.flush();
			}
		}
	}
	
	/**
	 * @param maxPlayer The maxPlayer to set.
	 */
	public void setMaxPlayer(final int maxPlayer)
	{
		sendServerStatus(ServerStatus.MAX_PLAYERS, maxPlayer);
		this.maxPlayer = maxPlayer;
	}
	
	/**
	 * @return Returns the maxPlayer.
	 */
	public int getMaxPlayer()
	{
		return maxPlayer;
	}
	
	/**
	 * @param id
	 * @param value
	 */
	public void sendServerStatus(final int id, final int value)
	{
		ServerStatus ss = new ServerStatus();
		ss.addAttribute(id, value);
		try
		{
			sendPacket(ss);
		}
		catch (final IOException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
		
		ss = null;
	}
	
	/**
	 * @return
	 */
	public String getStatusString()
	{
		return ServerStatus.STATUS_STRING[statusNumber];
	}
	
	/**
	 * @return
	 */
	public boolean isClockShown()
	{
		return Config.SERVER_LIST_CLOCK;
	}
	
	/**
	 * @return
	 */
	public boolean isBracketShown()
	{
		return Config.SERVER_LIST_BRACKET;
	}
	
	/**
	 * @return Returns the serverName.
	 */
	public String getServerName()
	{
		return serverName;
	}
	
	public void setServerStatus(final int status)
	{
		switch (status)
		{
			case ServerStatus.STATUS_AUTO:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
				statusNumber = status;
				break;
			case ServerStatus.STATUS_DOWN:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_DOWN);
				statusNumber = status;
				break;
			case ServerStatus.STATUS_FULL:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_FULL);
				statusNumber = status;
				break;
			case ServerStatus.STATUS_GM_ONLY:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
				statusNumber = status;
				break;
			case ServerStatus.STATUS_GOOD:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GOOD);
				statusNumber = status;
				break;
			case ServerStatus.STATUS_NORMAL:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_NORMAL);
				statusNumber = status;
				break;
			default:
				throw new IllegalArgumentException("Status does not exists:" + status);
		}
	}
	
	public static class SessionKey
	{
		public int playOkID1;
		public int playOkID2;
		public int loginOkID1;
		public int loginOkID2;
		public int clientKey = -1; // by Azagthtot
		
		public SessionKey(final int loginOK1, final int loginOK2, final int playOK1, final int playOK2)
		{
			playOkID1 = playOK1;
			playOkID2 = playOK2;
			loginOkID1 = loginOK1;
			loginOkID2 = loginOK2;
		}
		
		@Override
		public String toString()
		{
			return "PlayOk: " + playOkID1 + " " + playOkID2 + " LoginOk:" + loginOkID1 + " " + loginOkID2;
		}
	}
	
	private class WaitingClient
	{
		public int timestamp;
		public String account;
		public L2GameClient gameClient;
		public SessionKey session;
		
		public WaitingClient(final String acc, final L2GameClient client, final SessionKey key)
		{
			account = acc;
			timestamp = GameTimeController.getGameTicks();
			gameClient = client;
			session = key;
		}
	}
	
	private boolean interrupted = false;
	
	@Override
	public void interrupt()
	{
		interrupted = true;
		super.interrupt();
	}
	
	@Override
	public boolean isInterrupted()
	{
		return interrupted;
	}
}
