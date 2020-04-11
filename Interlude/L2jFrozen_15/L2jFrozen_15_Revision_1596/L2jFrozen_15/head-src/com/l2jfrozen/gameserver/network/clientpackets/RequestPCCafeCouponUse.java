package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

/**
 * Format: (ch) S
 * @author -Wooden-
 */
public final class RequestPCCafeCouponUse extends L2GameClientPacket
{
	private final Logger LOGGER = Logger.getLogger(RequestPCCafeCouponUse.class);
	private String str;
	
	@Override
	protected void readImpl()
	{
		str = readS();
	}
	
	@Override
	protected void runImpl()
	{
		// TODO
		LOGGER.info("C5: RequestPCCafeCouponUse: S: " + str);
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:20 RequestPCCafeCouponUse";
	}
	
}
