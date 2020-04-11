package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;

/**
 * 60 d6 6d c0 4b door id 8f 14 00 00 x b7 f1 00 00 y 60 f2 ff ff z 00 00 00 00 ?? format dddd rev 377 ID:%d X:%d Y:%d Z:%d ddddd rev 419
 * @version $Revision: 1.3.2.2.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class DoorInfo extends L2GameServerPacket
{
	private L2DoorInstance door;
	private boolean showHP;
	
	public DoorInfo(L2DoorInstance door)
	{
		this.door = door;
		showHP = door.getCastle() != null && door.getCastle().getSiege().getIsInProgress();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4c);
		writeD(door.getObjectId());
		writeD(door.getDoorId());
		writeD(showHP ? 1 : 0);
		writeD(1); // ??? (can target)
		writeD(door.isOpen() ? 0 : 1);
		writeD((int) door.getCurrentHp());
		writeD(0); // ??? (show HP)
		writeD(0); // ??? (Damage)
	}
	
	@Override
	public String getType()
	{
		return "[S] 4c DoorInfo";
	}
	
}