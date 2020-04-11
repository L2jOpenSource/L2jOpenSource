package com.l2jfrozen.loginserver.network.clientpackets;

import com.l2jfrozen.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import com.l2jfrozen.loginserver.network.serverpackets.ServerList;

/**
 * Format: ddc d: fist part of session id d: second part of session id c: ?
 */
public class RequestServerList extends L2LoginClientPacket
{
	private int skey1;
	private int skey2;
	private int data3;
	
	/**
	 * @return
	 */
	public int getSessionKey1()
	{
		return skey1;
	}
	
	/**
	 * @return
	 */
	public int getSessionKey2()
	{
		return skey2;
	}
	
	/**
	 * @return
	 */
	public int getData3()
	{
		return data3;
	}
	
	@Override
	public boolean readImpl()
	{
		if (super.buf.remaining() >= 8)
		{
			skey1 = readD(); // loginOk 1
			skey2 = readD(); // loginOk 2
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		if (getClient().getSessionKey().checkLoginPair(skey1, skey2))
		{
			getClient().sendPacket(new ServerList(getClient()));
		}
		else
		{
			getClient().close(LoginFailReason.REASON_ACCESS_FAILED);
		}
	}
}
