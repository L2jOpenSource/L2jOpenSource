/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import l2r.Config;
import l2r.gameserver.network.L2GameClient;
import l2r.gameserver.network.serverpackets.KeyPacket;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import gr.reunion.protection.ConfigProtection;
import gr.reunion.protection.Protection;

/**
 * This class ...
 * @version $Revision: 1.5.2.8.2.8 $ $Date: 2005/04/02 10:43:04 $
 */
public final class ProtocolVersion extends L2GameClientPacket
{
	private static final String _C__0E_PROTOCOLVERSION = "[C] 0E ProtocolVersion";
	private static final Logger _logAccounting = Logger.getLogger("accounting");
	
	private int _version;
	
	// Protection
	private String _hwidHdd = "";
	private String _hwidMac = "";
	private String _hwidCPU = "";
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
		}
		else if (!Config.PROTOCOL_LIST.contains(_version))
		{
			LogRecord record = new LogRecord(Level.WARNING, "Wrong protocol");
			record.setParameters(new Object[]
			{
				_version,
				getClient()
			});
			_logAccounting.log(record);
			KeyPacket pk = new KeyPacket(getClient().enableCrypt(), 0);
			getClient().setProtocolOk(false);
			getClient().close(pk);
		}
		getClient().setRevision(_version);
		if (Protection.isProtectionOn())
		{
			switch (ConfigProtection.GET_CLIENT_HWID)
			{
				case 1:
					if (_hwidHdd == "")
					{
						_log.info("Status HWID HDD : NoPatch!!!");
						getClient().close(new KeyPacket(getClient().enableCrypt(), 0));
					}
					else
					{
						getClient().setHWID(_hwidHdd);
					}
					break;
				case 2:
					if (_hwidMac == "")
					{
						_log.info("Status HWID MAC : NoPatch!!!");
						getClient().close(new KeyPacket(getClient().enableCrypt(), 0));
					}
					else
					{
						getClient().setHWID(_hwidMac);
					}
					break;
				case 3:
					if (_hwidCPU == "")
					{
						_log.info("Status HWID : NoPatch!!!");
						getClient().close(new KeyPacket(getClient().enableCrypt(), 0));
					}
					else
					{
						getClient().setHWID(_hwidCPU);
					}
					break;
			}
		}
		else
		{
			getClient().setHWID("NoGuard");
		}
		
		if (Config.DEBUG)
		{
			_log.info("Client Protocol Revision is ok: " + _version);
		}
		
		KeyPacket pk = new KeyPacket(getClient().enableCrypt(), 1);
		getClient().sendPacket(pk);
		getClient().setProtocolOk(true);
	}
	
	@Override
	public String getType()
	{
		return _C__0E_PROTOCOLVERSION;
	}
}
