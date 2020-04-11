package com.l2jfrozen.gameserver.network.clientpackets;

/**
 * Format ch c: (id) 0x39 h: (subid) 0x02
 * @author -Wooden-
 */
public final class SuperCmdServerStatus extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger packet
	}
	
	@Override
	protected void runImpl()
	{
		
	}
	
	@Override
	public String getType()
	{
		return "[C] 39:02 SuperCmdServerStatus";
	}
	
}
