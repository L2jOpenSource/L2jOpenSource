package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public final class KeyPacket extends L2GameServerPacket
{
	private final byte[] key;
	
	public KeyPacket(final byte[] key)
	{
		this.key = key;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0x00);
		writeC(0x01);
		writeB(key);
		writeD(0x01);
		writeD(0x01);
	}
	
	@Override
	public String getType()
	{
		return "[S] 01 KeyPacket";
	}
	
}
