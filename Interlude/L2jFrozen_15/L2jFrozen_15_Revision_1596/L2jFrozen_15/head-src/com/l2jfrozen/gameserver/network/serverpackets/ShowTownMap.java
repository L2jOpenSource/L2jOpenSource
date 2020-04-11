package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * sample format d
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class ShowTownMap extends L2GameServerPacket
{
	private final String texture;
	private final int x;
	private final int y;
	
	public ShowTownMap(final String texture, final int x, final int y)
	{
		this.texture = texture;
		this.x = x;
		this.y = y;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xde);
		writeS(texture);
		writeD(x);
		writeD(y);
	}
	
	@Override
	public String getType()
	{
		return "[S] DE ShowTownMap";
	}
}
