package com.l2jfrozen.gameserver.network.serverpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;

/**
 * This class ...
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class PetItemList extends L2GameServerPacket
{
	private static Logger LOGGER = Logger.getLogger(PetItemList.class);
	private final L2PetInstance activeChar;
	
	public PetItemList(final L2PetInstance character)
	{
		activeChar = character;
		if (Config.DEBUG)
		{
			final L2ItemInstance[] items = activeChar.getInventory().getItems();
			for (final L2ItemInstance temp : items)
			{
				LOGGER.debug("item:" + temp.getItem().getName() + " type1:" + temp.getItem().getType1() + " type2:" + temp.getItem().getType2());
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xB2);
		
		final L2ItemInstance[] items = activeChar.getInventory().getItems();
		final int count = items.length;
		writeH(count);
		
		for (final L2ItemInstance temp : items)
		{
			writeH(temp.getItem().getType1()); // item type1
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(temp.getItem().getType2()); // item type2
			writeH(0xff); // ?
			if (temp.isEquipped())
			{
				writeH(0x01);
			}
			else
			{
				writeH(0x00);
			}
			writeD(temp.getItem().getBodyPart()); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
			// writeH(temp.getItem().getBodyPart()); // rev 377 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
			writeH(temp.getEnchantLevel()); // enchant level
			writeH(0x00); // ?
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] b2  PetItemList";
	}
}
