package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * format ddddd sample 0000: 39 0b 07 10 48 3e 31 10 48 3a f6 00 00 91 5b 00 9...H>1.H:....[. 0010: 00 4c f1 ff ff .L...
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class TargetSelected extends L2GameServerPacket
{
	private final int objectId;
	private final int targetObjId;
	private final int x;
	private final int y;
	private final int z;
	
	public TargetSelected(final int objectId, final int targetId, final int x, final int y, final int z)
	{
		this.objectId = objectId;
		targetObjId = targetId;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x29);
		writeD(objectId);
		writeD(targetObjId);
		writeD(x);
		writeD(y);
		writeD(z);
	}
	
	@Override
	public String getType()
	{
		return "[S] 29 TargetSelected";
	}
}
