package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: ch (trigger).
 * @author KenM
 */
public class ExShowAdventurerGuideBook extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x37);
	}
	
	/**
	 * Gets the type.
	 * @return the type
	 */
	@Override
	public String getType()
	{
		return "[S] FE:37 ExShowAdventurerGuideBook";
	}
}
