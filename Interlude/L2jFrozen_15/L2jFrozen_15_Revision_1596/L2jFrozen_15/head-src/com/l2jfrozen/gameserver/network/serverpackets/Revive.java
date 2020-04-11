package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Object;

/**
 * sample 0000: 0c 9b da 12 40 ....@ format d
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:40 $
 */
public class Revive extends L2GameServerPacket
{
	private final int objectId;
	
	public Revive(final L2Object obj)
	{
		objectId = obj.getObjectId();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x07);
		writeD(objectId);
	}
	
	@Override
	public String getType()
	{
		return "[S] 07 Revive";
	}
	
}
