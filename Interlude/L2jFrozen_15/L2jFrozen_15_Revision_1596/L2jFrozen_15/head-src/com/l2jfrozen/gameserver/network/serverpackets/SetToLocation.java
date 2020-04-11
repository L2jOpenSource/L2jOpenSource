package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Character;

/**
 * 0000: 76 7a 07 80 49 ea 01 00 00 c1 37 fe uz..Ic'.J.....7.
 * <p>
 * 0010: ff 9e c3 03 00 8f f3 ff ff .........
 * <p>
 * <p>
 * format dddddd (player id, target id, distance, startx, starty, startz)
 * <p>
 * @version $Revision: 1.3.4.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class SetToLocation extends L2GameServerPacket
{
	private final int charObjId;
	private final int x, y, z, heading;
	
	public SetToLocation(final L2Character character)
	{
		charObjId = character.getObjectId();
		x = character.getX();
		y = character.getY();
		z = character.getZ();
		heading = character.getHeading();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x76);
		
		writeD(charObjId);
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(heading);
	}
	
	@Override
	public String getType()
	{
		return "[S] 76 SetToLocation";
	}
}
