package com.l2jfrozen.gameserver.network.clientpackets;

/**
 * Format chS c (id) 0xD0 h (subid) 0x0C S the hero's words :)
 * @author -Wooden-
 */
public final class RequestWriteHeroWords extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private String heroWords;
	
	@Override
	protected void readImpl()
	{
		heroWords = readS();
	}
	
	@Override
	protected void runImpl()
	{
		
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:0C RequestWriteHeroWords";
	}
}
