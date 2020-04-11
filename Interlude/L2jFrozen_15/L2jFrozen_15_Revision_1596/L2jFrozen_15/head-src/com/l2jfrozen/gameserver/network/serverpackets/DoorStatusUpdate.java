package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;

/**
 * 61 d6 6d c0 4b door id 8f 14 00 00 x b7 f1 00 00 y 60 f2 ff ff z 00 00 00 00 ?? format dddd rev 377 ID:%d X:%d Y:%d Z:%d ddddd rev 419
 * @version $Revision: 1.3.2.2.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class DoorStatusUpdate extends L2GameServerPacket
{
	private final L2DoorInstance door;
	
	public DoorStatusUpdate(final L2DoorInstance door)
	{
		this.door = door;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4d);
		writeD(door.getObjectId());
		writeD(door.isOpen() ? 0 : 1);
		writeD(door.getDamage());
		writeD(door.isEnemyOf(getClient().getActiveChar()) ? 1 : 0);
		writeD(door.getDoorId());
		writeD(door.getMaxHp());
		writeD((int) door.getCurrentHp());
	}
	
	@Override
	public String getType()
	{
		return "[S] 4d DoorStatusUpdate";
	}
	
}
