package net.sf.l2j.loginserver.network.serverpackets;

import net.sf.l2j.loginserver.network.LoginClient;

public final class Init extends L2LoginServerPacket
{
	private final int _sessionId;
	
	private final byte[] _publicKey;
	private final byte[] _blowfishKey;
	
	private static final int PROTOCOL_VERSION = 0x0000c621;
	
	private static final byte[] UNKNOWN_GG = new byte[16];
	
	public Init(LoginClient client)
	{
		_sessionId = client.getSessionId();
		
		_publicKey = client.getScrambledModulus();
		_blowfishKey = client.getBlowfishKey();
	}
	
	@Override
	protected void write()
	{
		writeC(0x00);
		writeD(_sessionId);
		writeD(PROTOCOL_VERSION);
		writeB(_publicKey);
		writeB(UNKNOWN_GG);
		writeB(_blowfishKey);
		writeC(0x00);
	}
}