package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Object;

/**
 * sample 0000: 1e 9b da 12 40 ....@ format d
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class DeleteObject extends L2GameServerPacket
{
	private final int objectId;
	
	public DeleteObject(final L2Object obj)
	{
		objectId = obj.getObjectId();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x12);
		writeD(objectId);
		writeD(0x00); // c2
	}
	
	@Override
	public String getType()
	{
		return "[S] 12 DeleteObject";
	}
}