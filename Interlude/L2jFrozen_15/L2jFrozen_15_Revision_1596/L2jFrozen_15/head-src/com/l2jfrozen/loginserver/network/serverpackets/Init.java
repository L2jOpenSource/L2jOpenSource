package com.l2jfrozen.loginserver.network.serverpackets;

import com.l2jfrozen.loginserver.L2LoginClient;

/**
 * Format: dd b dddd s d: session id d: protocol revision b: 0x90 bytes : 0x80 bytes for the scrambled RSA public key 0x10 bytes at 0x00 d: unknow d: unknow d: unknow d: unknow s: blowfish key
 */
public final class Init extends L2LoginServerPacket
{
	private final int sessionId;
	
	private final byte[] publicKey;
	private final byte[] blowfishKey;
	
	public Init(final L2LoginClient client)
	{
		this(client.getScrambledModulus(), client.getBlowfishKey(), client.getSessionId());
	}
	
	public Init(final byte[] publickey, final byte[] blowfishkey, final int sessionId)
	{
		this.sessionId = sessionId;
		publicKey = publickey;
		blowfishKey = blowfishkey;
	}
	
	@Override
	protected void write()
	{
		writeC(0x00); // init packet id
		
		writeD(sessionId); // session id
		writeD(0x0000c621); // protocol revision
		
		writeB(publicKey); // RSA Public Key
		
		// unk GG related?
		writeD(0x29DD954E);
		writeD(0x77C39CFC);
		writeD(0x97ADB620);
		writeD(0x07BDE0F7);
		
		writeB(blowfishKey); // BlowFish key
		writeC(0x00); // null termination ;)
	}
	
	@Override
	public String getType()
	{
		return "Init";
	}
}
