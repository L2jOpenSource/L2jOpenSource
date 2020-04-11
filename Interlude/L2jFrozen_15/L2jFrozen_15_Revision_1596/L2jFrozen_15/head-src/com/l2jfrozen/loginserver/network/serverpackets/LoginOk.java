package com.l2jfrozen.loginserver.network.serverpackets;

import com.l2jfrozen.loginserver.SessionKey;

/**
 * Format: dddddddd f: the session key d: ? d: ? d: ? d: ? d: ? d: ? b: 16 bytes - unknown
 */
public final class LoginOk extends L2LoginServerPacket
{
	private final int loginOk1, loginOk2;
	
	public LoginOk(final SessionKey sessionKey)
	{
		loginOk1 = sessionKey.loginOkID1;
		loginOk2 = sessionKey.loginOkID2;
	}
	
	@Override
	protected void write()
	{
		writeC(0x03);
		writeD(loginOk1);
		writeD(loginOk2);
		writeD(0x00);
		writeD(0x00);
		writeD(0x000003ea);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeB(new byte[16]);
	}
	
	@Override
	public String getType()
	{
		return "LoginOk";
	}
	
}
