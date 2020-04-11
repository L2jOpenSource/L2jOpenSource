package com.l2jfrozen.loginserver.network.serverpackets;

/**
 * @author KenM
 */
public final class AccountKicked extends L2LoginServerPacket
{
	public static enum AccountKickedReason
	{
		REASON_DATA_STEALER(0x01),
		REASON_GENERIC_VIOLATION(0x08),
		REASON_7_DAYS_SUSPENDED(0x10),
		REASON_PERMANENTLY_BANNED(0x20);
		
		private final int code;
		
		AccountKickedReason(final int code)
		{
			this.code = code;
		}
		
		public final int getCode()
		{
			return code;
		}
	}
	
	private final int reason;
	
	public AccountKicked(final AccountKickedReason reason)
	{
		this.reason = reason.getCode();
	}
	
	@Override
	protected void write()
	{
		writeC(0x02);
		writeD(reason);
	}
	
	@Override
	public String getType()
	{
		return "AccountKicked";
	}
	
}
