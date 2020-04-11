package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * <p>
 * sample bf 73 5d 30 49 01 00
 * <p>
 * format dh (objectid, color)
 * <p>
 * color -xx -> -9 red
 * <p>
 * -8 -> -6 light-red
 * <p>
 * -5 -> -3 yellow
 * <p>
 * -2 -> 2 white
 * <p>
 * 3 -> 5 green
 * <p>
 * 6 -> 8 light-blue
 * <p>
 * 9 -> xx blue
 * <p>
 * <p>
 * usually the color equals the level difference to the selected target.
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class MyTargetSelected extends L2GameServerPacket
{
	private final int objectId;
	private final int color;
	
	public MyTargetSelected(final int objectId, final int color)
	{
		this.objectId = objectId;
		this.color = color;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xa6);
		writeD(objectId);
		writeH(color);
	}
	
	@Override
	public String getType()
	{
		return "[S] a6 MyTargetSelected";
	}
	
}
