package net.sf.l2j.loginserver.network.loginserverpackets;

import net.sf.l2j.loginserver.network.serverpackets.ServerBasePacket;

public class KickPlayer extends ServerBasePacket
{
	public KickPlayer(String account)
	{
		writeC(0x04);
		writeS(account);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}