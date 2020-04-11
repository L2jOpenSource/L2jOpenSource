package net.sf.l2j.loginserver.network.serverpackets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.commons.network.StatusType;

import net.sf.l2j.loginserver.data.manager.GameServerManager;
import net.sf.l2j.loginserver.model.Account;
import net.sf.l2j.loginserver.model.GameServerInfo;
import net.sf.l2j.loginserver.model.ServerData;

public final class ServerList extends L2LoginServerPacket
{
	private final List<ServerData> _servers = new ArrayList<>();
	
	private final int _lastServer;
	
	public ServerList(Account account)
	{
		_lastServer = account.getLastServer();
		
		for (GameServerInfo gsi : GameServerManager.getInstance().getRegisteredGameServers().values())
		{
			final StatusType status = (account.getAccessLevel() < 0 || (gsi.getStatus() == StatusType.GM_ONLY && account.getAccessLevel() <= 0)) ? StatusType.DOWN : gsi.getStatus();
			final String hostName = gsi.getHostName();
			
			_servers.add(new ServerData(status, hostName, gsi));
		}
	}
	
	@Override
	public void write()
	{
		writeC(0x04);
		writeC(_servers.size());
		writeC(_lastServer);
		
		for (ServerData server : _servers)
		{
			writeC(server.getServerId());
			
			try
			{
				final byte[] raw = InetAddress.getByName(server.getHostName()).getAddress();
				writeC(raw[0] & 0xff);
				writeC(raw[1] & 0xff);
				writeC(raw[2] & 0xff);
				writeC(raw[3] & 0xff);
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
				writeC(127);
				writeC(0);
				writeC(0);
				writeC(1);
			}
			
			writeD(server.getPort());
			writeC(server.getAgeLimit());
			writeC(server.isPvp() ? 0x01 : 0x00);
			writeH(server.getCurrentPlayers());
			writeH(server.getMaxPlayers());
			writeC(server.getStatus() == StatusType.DOWN ? 0x00 : 0x01);
			
			int bits = 0;
			if (server.isTestServer())
				bits |= 0x04;
			
			if (server.isShowingClock())
				bits |= 0x02;
			
			writeD(bits);
			writeC(server.isShowingBrackets() ? 0x01 : 0x00);
		}
	}
}