package l2r.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import l2r.Config;
import l2r.gameserver.network.L2GameClient;
import l2r.gameserver.network.serverpackets.KeyPacket;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;

import gr.sr.network.handler.types.ServerType;
import gr.sr.protection.ConfigProtection;
import gr.sr.protection.Protection;

public final class ProtocolVersion extends L2GameClientPacket
{
	private static final Logger _logAccounting = Logger.getLogger("accounting");
	
	private int _version;
	
	// Protection
	private String _hwidHdd = "NoGuard";
	private String _hwidMac = "NoGuard";
	private String _hwidCPU = "NoGuard";
	private byte[] _data;
	
	@Override
	protected void readImpl()
	{
		L2GameClient client = getClient();
		
		_version = readD();
		
		if (_buf.remaining() > 260)
		{
			_data = new byte[260];
			readB(_data);
			if (Protection.isProtectionOn())
			{
				_hwidHdd = readS();
				_hwidMac = readS();
				_hwidCPU = readS();
			}
		}
		else if (Protection.isProtectionOn())
		{
			client.close(new KeyPacket(getClient().enableCrypt(), 0));
		}
	}
	
	@Override
	protected void runImpl()
	{
		// this packet is never encrypted
		if (_version == -2)
		{
			if (Config.DEBUG)
			{
				_log.info("Ping received");
			}
			// this is just a ping attempt from the new C2 client
			getClient().close((L2GameServerPacket) null);
			return;
		}
		
		if (!ServerType.isValidProtocol(_version))
		{
			LogRecord record = new LogRecord(Level.WARNING, "Wrong protocol");
			record.setParameters(new Object[]
			{
				_version,
				getClient()
			});
			_logAccounting.log(record);
			
			getClient().setProtocolOk(false);
			getClient().close(new KeyPacket(getClient().enableCrypt(), 0));
			return;
		}
		
		getClient().setRevision(_version);
		if (Protection.isProtectionOn())
		{
			if (_hwidHdd.equals("NoGuard") && _hwidMac.equals("NoGuard") && _hwidCPU.equals("NoGuard"))
			{
				_log.info("HWID Status: NoPatch!!!");
				getClient().close(new KeyPacket(getClient().enableCrypt(), 0));
			}
			
			switch (ConfigProtection.GET_CLIENT_HWID)
			{
				case 1:
					getClient().setHWID(_hwidHdd);
					break;
				case 2:
					getClient().setHWID(_hwidMac);
					break;
				case 3:
					getClient().setHWID(_hwidCPU);
					break;
			}
		}
		
		if (Config.DEBUG)
		{
			_log.info("Client Protocol Revision is ok: " + _version);
		}
		
		getClient().sendPacket(new KeyPacket(getClient().enableCrypt(), 1));
		getClient().setProtocolOk(true);
	}
}
