package com.l2jfrozen.gameserver.network.clientpackets;

/**
 * Format chS c: (id) 0x39 h: (subid) 0x00 S: the character name (or maybe cmd string ?)
 * @author -Wooden-
 */
public final class SuperCmdCharacterInfo extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private String characterName;
	
	@Override
	protected void readImpl()
	{
		characterName = readS();
	}
	
	@Override
	protected void runImpl()
	{
		
	}
	
	@Override
	public String getType()
	{
		return "[C] 39:00 SuperCmdCharacterInfo";
	}
}
