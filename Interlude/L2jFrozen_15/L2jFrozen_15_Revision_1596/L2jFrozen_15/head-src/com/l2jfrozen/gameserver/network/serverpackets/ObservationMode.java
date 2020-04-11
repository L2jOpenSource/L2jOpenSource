package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class ObservationMode extends L2GameServerPacket
{
	// ddSS
	private final int x, y, z;
	
	public ObservationMode(final int x, final int y, final int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xdf);
		writeD(x);
		writeD(y);
		writeD(z);
		writeC(0x00);
		writeC(0xc0);
		writeC(0x00);
	}
	
	@Override
	public String getType()
	{
		return "[S] DF ObservationMode";
	}
}
