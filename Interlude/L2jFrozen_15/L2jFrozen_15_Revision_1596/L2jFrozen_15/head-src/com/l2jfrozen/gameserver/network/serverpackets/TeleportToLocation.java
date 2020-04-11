package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Object;

/**
 * format dddd sample 0000: 3a 69 08 10 48 02 c1 00 00 f7 56 00 00 89 ea ff :i..H.....V..... 0010: ff 0c b2 d8 61 ....a
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class TeleportToLocation extends L2GameServerPacket
{
	private final int targetObjId;
	private final int x;
	private final int y;
	private final int z;
	private final int heading;
	
	public TeleportToLocation(final L2Object obj, final int x, final int y, final int z)
	{
		targetObjId = obj.getObjectId();
		this.x = x;
		this.y = y;
		this.z = z;
		heading = obj.getPosition().getHeading();
	}
	
	public TeleportToLocation(final L2Object obj, final int x, final int y, final int z, final int heading)
	{
		targetObjId = obj.getObjectId();
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = heading;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x28);
		writeD(targetObjId);
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(0x00); // 0 - with black screen, 1 - fast teleport (for correcting position)
		writeD(heading); // nYaw
	}
	
	@Override
	public String getType()
	{
		return "[S] 28 TeleportToLocation";
	}
}
