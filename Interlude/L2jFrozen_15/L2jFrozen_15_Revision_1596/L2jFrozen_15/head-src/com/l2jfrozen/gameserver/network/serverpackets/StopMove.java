package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Character;

/**
 * format ddddd sample 0000: 59 1a 95 20 48 44 17 02 00 03 f0 fc ff 98 f1 ff Y.. HD.......... 0010: ff c1 1a 00 00 .....
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class StopMove extends L2GameServerPacket
{
	private final int objectId;
	private final int x;
	private final int y;
	private final int z;
	private final int heading;
	
	public StopMove(final L2Character cha)
	{
		this(cha.getObjectId(), cha.getX(), cha.getY(), cha.getZ(), cha.getHeading());
	}
	
	public StopMove(final int objectId, final int x, final int y, final int z, final int heading)
	{
		this.objectId = objectId;
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = heading;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x47);
		writeD(objectId);
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(heading);
	}
	
	@Override
	public String getType()
	{
		return "[S] 47 StopMove";
	}
}