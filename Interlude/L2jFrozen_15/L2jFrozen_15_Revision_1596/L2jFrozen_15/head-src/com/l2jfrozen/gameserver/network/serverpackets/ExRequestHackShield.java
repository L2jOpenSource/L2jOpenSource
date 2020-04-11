package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: ch Trigger packet.
 * @author KenM
 */
public class ExRequestHackShield extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x48);
	}
	
	/**
	 * Gets the type.
	 * @return the type
	 */
	@Override
	public String getType()
	{
		return "[S] FE:48 ExRequestHackShield";
	}
}
