package com.l2jfrozen.gameserver.network.loginserverpackets;

public class LoginServerFail extends LoginServerBasePacket
{
	
	private static final String[] REASONS =
	{
		"None",
		"Reason: ip banned",
		"Reason: ip reserved",
		"Reason: wrong hexid",
		"Reason: id reserved",
		"Reason: no free ID",
		"Not authed",
		"Reason: already logged in"
	};
	private final int reason;
	
	/**
	 * @param decrypt
	 */
	public LoginServerFail(final byte[] decrypt)
	{
		super(decrypt);
		reason = readC();
	}
	
	public String getReasonString()
	{
		return REASONS[reason];
	}
	
	public int getReason()
	{
		return reason;
	}
	
}
