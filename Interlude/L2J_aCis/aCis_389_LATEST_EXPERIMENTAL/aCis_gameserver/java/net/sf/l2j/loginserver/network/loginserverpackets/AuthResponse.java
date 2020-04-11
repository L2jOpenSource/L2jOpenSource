package net.sf.l2j.loginserver.network.loginserverpackets;

import net.sf.l2j.loginserver.data.manager.GameServerManager;
import net.sf.l2j.loginserver.network.serverpackets.ServerBasePacket;

public class AuthResponse extends ServerBasePacket
{
	public AuthResponse(int serverId)
	{
		writeC(0x02);
		writeC(serverId);
		writeS(GameServerManager.getInstance().getServerNames().get(serverId));
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}