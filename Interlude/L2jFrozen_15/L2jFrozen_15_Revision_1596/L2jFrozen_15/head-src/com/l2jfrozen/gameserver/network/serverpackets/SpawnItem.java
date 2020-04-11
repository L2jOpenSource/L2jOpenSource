package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;

/**
 * 15 ee cc 11 43 object id 39 00 00 00 item id 8f 14 00 00 x b7 f1 00 00 y 60 f2 ff ff z 01 00 00 00 show item count 7a 00 00 00 count . format dddddddd
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class SpawnItem extends L2GameServerPacket
{
	private final int objectId;
	private final int itemId;
	private final int x, y, z;
	private final int stackable, count;
	
	public SpawnItem(final L2ItemInstance item)
	{
		objectId = item.getObjectId();
		itemId = item.getItemId();
		x = item.getX();
		y = item.getY();
		z = item.getZ();
		stackable = item.isStackable() ? 0x01 : 0x00;
		count = item.getCount();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x0b);
		writeD(objectId);
		writeD(itemId);
		
		writeD(x);
		writeD(y);
		writeD(z);
		// only show item count if it is a stackable item
		writeD(stackable);
		writeD(count);
		writeD(0x00); // c2
	}
	
	@Override
	public String getType()
	{
		return "[S] 15 SpawnItem";
	}
}
