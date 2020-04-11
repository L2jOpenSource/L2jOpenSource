package net.sf.l2j.loginserver.network.clientpackets;

import net.sf.l2j.loginserver.model.Account;
import net.sf.l2j.loginserver.network.serverpackets.LoginFail;
import net.sf.l2j.loginserver.network.serverpackets.ServerList;

public class RequestServerList extends L2LoginClientPacket
{
	private int _skey1;
	private int _skey2;
	private int _data3;
	
	public int getSessionKey1()
	{
		return _skey1;
	}
	
	public int getSessionKey2()
	{
		return _skey2;
	}
	
	public int getData3()
	{
		return _data3;
	}
	
	@Override
	public boolean readImpl()
	{
		if (super._buf.remaining() >= 8)
		{
			_skey1 = readD(); // loginOk 1
			_skey2 = readD(); // loginOk 2
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		final Account account = getClient().getAccount();
		if (account == null)
		{
			getClient().close(LoginFail.REASON_ACCESS_FAILED);
			return;
		}
		
		if (!getClient().getSessionKey().checkLoginPair(_skey1, _skey2))
		{
			getClient().close(LoginFail.REASON_ACCESS_FAILED);
			return;
		}
		
		getClient().sendPacket(new ServerList(account));
	}
}