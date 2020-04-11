package net.sf.l2j.loginserver.enums;

public enum AccountKickedReason
{
	DATA_STEALER(0x01),
	GENERIC_VIOLATION(0x08),
	SEVEN_DAYS_SUSPENDED(0x10),
	PERMANENTLY_BANNED(0x20);
	
	private final int _code;
	
	AccountKickedReason(int code)
	{
		_code = code;
	}
	
	public final int getCode()
	{
		return _code;
	}
}