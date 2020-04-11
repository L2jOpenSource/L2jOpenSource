package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;

/**
 * 15 ee cc 11 43 object id 39 00 00 00 item id 8f 14 00 00 x b7 f1 00 00 y 60 f2 ff ff z 01 00 00 00 show item count 7a 00 00 00 count . format dddddddd
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class SpawnItemPoly extends L2GameServerPacket
{
	private int objectId;
	private int itemId;
	private int x, y, z;
	private int stackable, count;
	
	public SpawnItemPoly(final L2Object object)
	{
		if (object instanceof L2ItemInstance)
		{
			final L2ItemInstance item = (L2ItemInstance) object;
			objectId = object.getObjectId();
			itemId = object.getPoly().getPolyId();
			x = item.getX();
			y = item.getY();
			z = item.getZ();
			stackable = item.isStackable() ? 0x01 : 0x00;
			count = item.getCount();
		}
		else
		{
			objectId = object.getObjectId();
			itemId = object.getPoly().getPolyId();
			x = object.getX();
			y = object.getY();
			z = object.getZ();
			stackable = 0x00;
			count = 1;
		}
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
