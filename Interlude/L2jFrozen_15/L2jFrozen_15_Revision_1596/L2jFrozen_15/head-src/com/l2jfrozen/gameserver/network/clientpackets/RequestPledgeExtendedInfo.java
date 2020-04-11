package com.l2jfrozen.gameserver.network.clientpackets;

/**
 * @author programmos
 */
public class RequestPledgeExtendedInfo extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private String name;
	
	@Override
	protected void readImpl()
	{
		name = readS();
	}
	
	@Override
	protected void runImpl()
	{
	}
	
	@Override
	public String getType()
	{
		return "[C] 0x67 RequestPledgeExtendedInfo";
	}
}
