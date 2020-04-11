package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Character;

/**
 * 0000: 75 7a 07 80 49 63 27 00 4a ea 01 00 00 c1 37 fe uz..Ic'.J.....7.
 * <p>
 * 0010: ff 9e c3 03 00 8f f3 ff ff .........
 * <p>
 * <p>
 * format dddddd (player id, target id, distance, startx, starty, startz)
 * <p>
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/04/06 16:13:46 $
 */
public class MoveToPawn extends L2GameServerPacket
{
	private final int charObjId;
	private final int targetId;
	private final int distance;
	private final int x, y, z;
	
	public MoveToPawn(final L2Character cha, final L2Character target, final int distance)
	{
		charObjId = cha.getObjectId();
		targetId = target.getObjectId();
		this.distance = distance;
		x = cha.getX();
		y = cha.getY();
		z = cha.getZ();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x60);
		
		writeD(charObjId);
		writeD(targetId);
		writeD(distance);
		
		writeD(x);
		writeD(y);
		writeD(z);
	}
	
	@Override
	public String getType()
	{
		return "[S] 60 MoveToPawn";
	}
}
