package com.l2jfrozen.loginserver.network.serverpackets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.datatables.GameServerTable;
import com.l2jfrozen.gameserver.datatables.GameServerTable.GameServerInfo;
import com.l2jfrozen.loginserver.L2LoginClient;
import com.l2jfrozen.loginserver.network.gameserverpackets.ServerStatus;

/**
 * ServerList Format: cc [cddcchhcdc] c: server list size (number of servers) c: ? [ (repeat for each servers) c: server id (ignored by client?) d: server ip d: server port c: age limit (used by client?) c: pvp or not (used by client?) h: current number of players h: max number of players c: 0 if
 * server is down d: 2nd bit: clock 3rd bit: wont dsiplay server name 4th bit: test server (used by client?) c: 0 if you dont want to display brackets in front of sever name ] Server will be considered as Good when the number of online players is less than half the maximum. as Normal between half
 * and 4/5 and Full when there's more than 4/5 of the maximum number of players
 */
public final class ServerList extends L2LoginServerPacket
{
	private final List<ServerData> servers;
	private final int lastServer;
	
	class ServerData
	{
		protected String ip;
		protected int port;
		protected boolean pvp;
		protected int currentPlayers;
		protected int maxPlayers;
		protected boolean testServer;
		protected boolean brackets;
		protected boolean clock;
		protected int status;
		protected int serverId;
		
		ServerData(final String pIp, final int pPort, final boolean pPvp, final boolean pTestServer, final int pCurrentPlayers, final int pMaxPlayers, final boolean pBrackets, final boolean pClock, final int pStatus, final int pServer_id)
		{
			ip = pIp;
			port = pPort;
			pvp = pPvp;
			testServer = pTestServer;
			currentPlayers = pCurrentPlayers;
			maxPlayers = pMaxPlayers;
			brackets = pBrackets;
			clock = pClock;
			status = pStatus;
			serverId = pServer_id;
		}
	}
	
	public ServerList(final L2LoginClient client)
	{
		servers = new ArrayList<>();
		lastServer = client.getLastServer();
		
		for (final GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values())
		{
			if (gsi.getStatus() == ServerStatus.STATUS_GM_ONLY && client.getAccessLevel() >= 100)
			{
				// Server is GM-Only but you've got GM Status
				addServer(client.usesInternalIP() ? gsi.getInternalHost() : gsi.getExternalHost(), gsi.getPort(), gsi.isPvp(), gsi.isTestServer(), gsi.getCurrentPlayerCount(), gsi.getMaxPlayers(), gsi.isShowingBrackets(), gsi.isShowingClock(), gsi.getStatus(), gsi.getId());
			}
			else if (gsi.getStatus() != ServerStatus.STATUS_GM_ONLY)
			{
				// Server is not GM-Only
				addServer(client.usesInternalIP() ? gsi.getInternalHost() : gsi.getExternalHost(), gsi.getPort(), gsi.isPvp(), gsi.isTestServer(), gsi.getCurrentPlayerCount(), gsi.getMaxPlayers(), gsi.isShowingBrackets(), gsi.isShowingClock(), gsi.getStatus(), gsi.getId());
			}
			else
			{
				// Server's GM-Only and you've got no GM-Status
				addServer(client.usesInternalIP() ? gsi.getInternalHost() : gsi.getExternalHost(), gsi.getPort(), gsi.isPvp(), gsi.isTestServer(), gsi.getCurrentPlayerCount(), gsi.getMaxPlayers(), gsi.isShowingBrackets(), gsi.isShowingClock(), ServerStatus.STATUS_DOWN, gsi.getId());
			}
		}
	}
	
	public void addServer(final String ip, final int port, final boolean pvp, final boolean testServer, final int currentPlayer, final int maxPlayer, final boolean brackets, final boolean clock, final int status, final int server_id)
	{
		servers.add(new ServerData(ip, port, pvp, testServer, currentPlayer, maxPlayer, brackets, clock, status, server_id));
	}
	
	@Override
	public void write()
	{
		writeC(0x04);
		writeC(servers.size());
		writeC(lastServer);
		
		for (final ServerData server : servers)
		{
			writeC(server.serverId); // server id
			
			try
			{
				final InetAddress i4 = InetAddress.getByName(server.ip);
				
				byte[] raw = i4.getAddress();
				
				writeC(raw[0] & 0xff);
				writeC(raw[1] & 0xff);
				writeC(raw[2] & 0xff);
				writeC(raw[3] & 0xff);
				raw = null;
			}
			catch (final UnknownHostException e)
			{
				e.printStackTrace();
				writeC(127);
				writeC(0);
				writeC(0);
				writeC(1);
			}
			
			writeD(server.port);
			writeC(0x00); // age limit
			writeC(server.pvp ? 0x01 : 0x00);
			writeH(server.currentPlayers);
			writeH(server.maxPlayers);
			writeC(server.status == ServerStatus.STATUS_DOWN ? 0x00 : 0x01);
			
			int bits = 0;
			
			if (server.testServer)
			{
				bits |= 0x04;
			}
			
			if (server.clock)
			{
				bits |= 0x02;
			}
			
			writeD(bits);
			writeC(server.brackets ? 0x01 : 0x00);
		}
	}
	
	@Override
	public String getType()
	{
		return "ServerList";
	}
}
