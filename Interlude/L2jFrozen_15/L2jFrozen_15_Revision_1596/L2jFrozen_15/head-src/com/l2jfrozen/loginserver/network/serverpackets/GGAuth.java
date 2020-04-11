package com.l2jfrozen.loginserver.network.serverpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;

/**
 * Fromat: d d: response
 */
public final class GGAuth extends L2LoginServerPacket
{
	static final Logger LOGGER = Logger.getLogger(GGAuth.class);
	public static final int SKIP_GG_AUTH_REQUEST = 0x0b;
	
	private final int response;
	
	public GGAuth(final int response)
	{
		this.response = response;
		
		if (Config.DEBUG)
		{
			LOGGER.warn("Reason Hex: " + Integer.toHexString(response));
		}
	}
	
	@Override
	protected void write()
	{
		writeC(0x0b);
		writeD(response);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
	}
	
	@Override
	public String getType()
	{
		return "GGAuth";
	}
}
