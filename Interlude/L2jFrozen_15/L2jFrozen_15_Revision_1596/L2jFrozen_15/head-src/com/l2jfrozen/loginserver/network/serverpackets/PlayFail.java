package com.l2jfrozen.loginserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: 1.2.4.1 $ $Date: 2005/03/27 15:30:11 $
 */
public final class PlayFail extends L2LoginServerPacket
{
	public static enum PlayFailReason
	{
		REASON_SYSTEM_ERROR(0x01),
		REASON_USER_OR_PASS_WRONG(0x02),
		REASON3(0x03),
		REASON4(0x04),
		REASON_TOO_MANY_PLAYERS(0x0f);
		
		private final int code;
		
		PlayFailReason(final int code)
		{
			this.code = code;
		}
		
		public final int getCode()
		{
			return code;
		}
	}
	
	private final PlayFailReason reason;
	
	public PlayFail(final PlayFailReason reason)
	{
		this.reason = reason;
	}
	
	@Override
	protected void write()
	{
		writeC(0x06);
		writeC(reason.getCode());
	}
	
	@Override
	public String getType()
	{
		return "PlayFail";
	}
}
