package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

/**
 * Format: (ch) just a trigger
 * @author -Wooden-
 */
public final class RequestExFishRanking extends L2GameClientPacket
{
	private final Logger LOGGER = Logger.getLogger(RequestExFishRanking.class);
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		LOGGER.info("C5: RequestExFishRanking");
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:1F RequestExFishRanking";
	}
	
}
