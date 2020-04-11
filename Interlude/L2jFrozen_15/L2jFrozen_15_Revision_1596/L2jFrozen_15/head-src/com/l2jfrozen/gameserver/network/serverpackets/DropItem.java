package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;

/**
 * 16 d6 6d c0 4b player id who dropped it ee cc 11 43 object id 39 00 00 00 item id 8f 14 00 00 x b7 f1 00 00 y 60 f2 ff ff z 01 00 00 00 show item-count 1=yes 7a 00 00 00 count . format dddddddd rev 377 ddddddddd rev 417
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class DropItem extends L2GameServerPacket
{
	private final L2ItemInstance item;
	private final int charObjId;
	
	/**
	 * Constructor of the DropItem server packet
	 * @param item        : L2ItemInstance designating the item
	 * @param playerObjId : int designating the player ID who dropped the item
	 */
	public DropItem(final L2ItemInstance item, final int playerObjId)
	{
		this.item = item;
		charObjId = playerObjId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x0c);
		writeD(charObjId);
		writeD(item.getObjectId());
		writeD(item.getItemId());
		
		writeD(item.getX());
		writeD(item.getY());
		writeD(item.getZ());
		// only show item count if it is a stackable item
		if (item.isStackable())
		{
			writeD(0x01);
		}
		else
		{
			writeD(0x00);
		}
		writeD(item.getCount());
		
		writeD(1); // unknown
	}
	
	@Override
	public String getType()
	{
		return "[S] 0c DropItem";
	}
	
}
