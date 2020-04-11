package com.l2jfrozen.gameserver.network.serverpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.netcore.SendablePacket;

/**
 * The Class L2GameServerPacket.
 * @author ProGramMoS
 */
public abstract class L2GameServerPacket extends SendablePacket<L2GameClient>
{
	private static final Logger LOGGER = Logger.getLogger(L2GameServerPacket.class);
	
	@Override
	protected void write()
	{
		try
		{
			writeImpl();
		}
		catch (final Throwable t)
		{
			LOGGER.error("Client: " + getClient().toString() + " - Failed writing: " + getType(), t);
			t.printStackTrace();
		}
	}
	
	/**
	 * Run impl.
	 */
	public void runImpl()
	{
		
	}
	
	/**
	 * Write impl.
	 */
	protected abstract void writeImpl();
	
	/**
	 * Gets the type.
	 * @return A String with this packet name for debuging purposes
	 */
	public abstract String getType();
}
