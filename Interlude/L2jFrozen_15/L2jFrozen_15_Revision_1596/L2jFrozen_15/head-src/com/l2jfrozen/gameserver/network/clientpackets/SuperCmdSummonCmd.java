package com.l2jfrozen.gameserver.network.clientpackets;

/**
 * Format chS c: (id) 0x39 h: (subid) 0x01 S: the summon name (or maybe cmd string ?)
 * @author -Wooden-
 */
public class SuperCmdSummonCmd extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private String summonName;
	
	@Override
	protected void readImpl()
	{
		summonName = readS();
	}
	
	@Override
	protected void runImpl()
	{
		
	}
	
	@Override
	public String getType()
	{
		return "[C] 39:01 SuperCmdSummonCmd";
	}
}
