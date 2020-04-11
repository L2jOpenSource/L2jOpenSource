package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.network.serverpackets.KeyPacket;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;

public final class ProtocolVersion extends L2GameClientPacket
{
	static Logger LOGGER = Logger.getLogger(ProtocolVersion.class);
	private int version;
	
	@Override
	protected void readImpl()
	{
		version = readH();
	}
	
	@Override
	protected void runImpl()
	{
		if (version < Config.MIN_PROTOCOL_REVISION || version > Config.MAX_PROTOCOL_REVISION)
		{
			LOGGER.info("Client: " + getClient().toString() + " -> Protocol Revision: " + version + " is invalid. Minimum is " + Config.MIN_PROTOCOL_REVISION + " and Maximum is " + Config.MAX_PROTOCOL_REVISION + " are supported. Closing connection.");
			LOGGER.warn("Wrong Client Protocol Version " + version);
			getClient().close((L2GameServerPacket) null);
		}
		else
		{
			if (Config.DEBUG)
			{
				LOGGER.debug(getType() + ": Client Protocol Revision is ok: " + version);
			}
			
			final KeyPacket pk = new KeyPacket(getClient().enableCrypt());
			getClient().sendPacket(pk);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 00 ProtocolVersion";
	}
}