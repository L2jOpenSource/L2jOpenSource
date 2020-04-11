package com.l2jfrozen.loginserver;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.interfaces.RSAPrivateKey;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.crypt.LoginCrypt;
import com.l2jfrozen.crypt.ScrambledKeyPair;
import com.l2jfrozen.loginserver.network.serverpackets.L2LoginServerPacket;
import com.l2jfrozen.loginserver.network.serverpackets.LoginFail;
import com.l2jfrozen.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import com.l2jfrozen.loginserver.network.serverpackets.PlayFail;
import com.l2jfrozen.loginserver.network.serverpackets.PlayFail.PlayFailReason;
import com.l2jfrozen.logs.Log;
import com.l2jfrozen.netcore.MMOClient;
import com.l2jfrozen.netcore.MMOConnection;
import com.l2jfrozen.netcore.SendablePacket;
import com.l2jfrozen.util.random.Rnd;

/**
 * Represents a client connected into the LoginServer
 * @author ProGramMoS
 */
public final class L2LoginClient extends MMOClient<MMOConnection<L2LoginClient>>
{
	private static Logger LOGGER = Logger.getLogger(L2LoginClient.class);
	
	public static enum LoginClientState
	{
		CONNECTED,
		AUTHED_GG,
		AUTHED_LOGIN
	}
	
	private LoginClientState state;
	
	// Crypt
	private final LoginCrypt loginCrypt;
	private final ScrambledKeyPair scrambledPair;
	private final byte[] blowfishKey;
	
	private String account = "";
	private int accessLevel;
	private int lastServer;
	private boolean usesInternalIP;
	private SessionKey sessionKey;
	private final int sessionId;
	private boolean joinedGS;
	private final String ip;
	private long connectionStartTime;
	
	/**
	 * @param con
	 */
	public L2LoginClient(final MMOConnection<L2LoginClient> con)
	{
		super(con);
		state = LoginClientState.CONNECTED;
		final String ip = getConnection().getInetAddress().getHostAddress();
		this.ip = ip;
		final String[] localip = Config.NETWORK_IP_LIST.split(";");
		for (final String oneIp : localip)
		{
			if (ip.startsWith(oneIp) || ip.startsWith("127.0"))
			{
				usesInternalIP = true;
			}
		}
		
		scrambledPair = LoginController.getInstance().getScrambledRSAKeyPair();
		blowfishKey = LoginController.getInstance().getBlowfishKey();
		sessionId = Rnd.nextInt(Integer.MAX_VALUE);
		connectionStartTime = System.currentTimeMillis();
		loginCrypt = new LoginCrypt();
		loginCrypt.setKey(blowfishKey);
		LoginController.getInstance().addLoginClient(this);
		// This checkup must go next to BAN because it can cause decrease ban account time
		if (!BruteProtector.canLogin(ip))
		{
			LoginController.getInstance().addBanForAddress(getConnection().getInetAddress(), Config.BRUT_BAN_IP_TIME * 1000);
			LOGGER.warn("Drop connection from IP " + ip + " because of BruteForce.");
		}
		// Closer.getInstance().add(this);
	}
	
	public String getIntetAddress()
	{
		return ip;
	}
	
	public boolean usesInternalIP()
	{
		return usesInternalIP;
	}
	
	@Override
	public boolean decrypt(final ByteBuffer buf, final int size)
	{
		boolean ret = false;
		try
		{
			ret = loginCrypt.decrypt(buf.array(), buf.position(), size);
			connectionStartTime = System.currentTimeMillis();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
			super.getConnection().close((SendablePacket<L2LoginClient>) null);
			return false;
		}
		
		if (!ret)
		{
			byte[] dump = new byte[size];
			System.arraycopy(buf.array(), buf.position(), dump, 0, size);
			LOGGER.warn("Wrong checksum from client: " + toString());
			super.getConnection().close((SendablePacket<L2LoginClient>) null);
			dump = null;
		}
		
		return ret;
	}
	
	@Override
	public boolean encrypt(final ByteBuffer buf, int size)
	{
		final int offset = buf.position();
		try
		{
			size = loginCrypt.encrypt(buf.array(), offset, size);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
			return false;
		}
		
		buf.position(offset + size);
		return true;
	}
	
	public LoginClientState getState()
	{
		return state;
	}
	
	public void setState(final LoginClientState state)
	{
		this.state = state;
	}
	
	public byte[] getBlowfishKey()
	{
		return blowfishKey;
	}
	
	public byte[] getScrambledModulus()
	{
		return scrambledPair.scrambledModulus;
	}
	
	public RSAPrivateKey getRSAPrivateKey()
	{
		return (RSAPrivateKey) scrambledPair.pair.getPrivate();
	}
	
	public String getAccount()
	{
		return account;
	}
	
	public void setAccount(final String account)
	{
		this.account = account;
	}
	
	public void setAccessLevel(final int accessLevel)
	{
		this.accessLevel = accessLevel;
	}
	
	public int getAccessLevel()
	{
		return accessLevel;
	}
	
	public void setLastServer(final int lastServer)
	{
		this.lastServer = lastServer;
	}
	
	public int getLastServer()
	{
		return lastServer;
	}
	
	public int getSessionId()
	{
		return sessionId;
	}
	
	public boolean hasJoinedGS()
	{
		return joinedGS;
	}
	
	public void setJoinedGS(final boolean val)
	{
		joinedGS = val;
	}
	
	public void setSessionKey(final SessionKey sessionKey)
	{
		this.sessionKey = sessionKey;
	}
	
	public SessionKey getSessionKey()
	{
		return sessionKey;
	}
	
	public long getConnectionStartTime()
	{
		return connectionStartTime;
	}
	
	public void sendPacket(final L2LoginServerPacket lsp)
	{
		if (Config.DEBUG_PACKETS)
		{
			Log.add("[ServerPacket] SendingLoginServerPacket, Client: " + toString() + " Packet:" + lsp.getType(), "log/packets/", "LoginServerPacketsLog");
		}
		
		getConnection().sendPacket(lsp);
	}
	
	public void close(final LoginFailReason reason)
	{
		getConnection().close(new LoginFail(reason));
	}
	
	public void close(final PlayFailReason reason)
	{
		getConnection().close(new PlayFail(reason));
	}
	
	public void close(final L2LoginServerPacket lsp)
	{
		getConnection().close(lsp);
	}
	
	@Override
	public void onDisconnection()
	{
		// Closer.getInstance().close(this);
		if (Config.DEBUG)
		{
			LOGGER.info("DISCONNECTED: " + toString());
		}
		
		LoginController.getInstance().removeLoginClient(this);
		if (!hasJoinedGS() && getAccount() != null)
		{
			LoginController.getInstance().removeAuthedLoginClient(getAccount());
		}
	}
	
	@Override
	public String toString()
	{
		final InetAddress address = getConnection().getInetAddress();
		if (getState() == LoginClientState.AUTHED_LOGIN)
		{
			return "[" + getAccount() + " (" + (address == null ? "disconnected" : address.getHostAddress()) + ")]";
		}
		return "[" + (address == null ? "disconnected" : address.getHostAddress()) + "]";
	}
	
	@Override
	protected void onForcedDisconnection(final boolean critical)
	{
		// Empty
	}
}
