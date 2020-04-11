package com.l2jfrozen.gameserver.network.serverpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.templates.L2Item;

/**
 * 5e 01 00 00 00 01 - added ? 02 - modified 7b 86 73 42 object id 08 00 00 00 body slot body slot 0000 ?? underwear 0001 ear 0002 ear 0003 neck 0004 finger (magic ring) 0005 finger (magic ring) 0006 head (l.cap) 0007 r.hand (dagger) 0008 l.hand (arrows) 0009 hands (short gloves) 000a chest (squire
 * shirt) 000b legs (squire pants) 000c feet 000d ?? back 000e lr.hand (bow) format ddd
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:40 $
 */
public class EquipUpdate extends L2GameServerPacket
{
	private static Logger LOGGER = Logger.getLogger(EquipUpdate.class);
	
	private final L2ItemInstance item;
	private final int change;
	
	public EquipUpdate(final L2ItemInstance item, final int change)
	{
		this.item = item;
		this.change = change;
	}
	
	@Override
	protected final void writeImpl()
	{
		int bodypart = 0;
		writeC(0x4b);
		writeD(change);
		writeD(item.getObjectId());
		switch (item.getItem().getBodyPart())
		{
			case L2Item.SLOT_L_EAR:
				bodypart = 0x01;
				break;
			case L2Item.SLOT_R_EAR:
				bodypart = 0x02;
				break;
			case L2Item.SLOT_NECK:
				bodypart = 0x03;
				break;
			case L2Item.SLOT_R_FINGER:
				bodypart = 0x04;
				break;
			case L2Item.SLOT_L_FINGER:
				bodypart = 0x05;
				break;
			case L2Item.SLOT_HEAD:
				bodypart = 0x06;
				break;
			case L2Item.SLOT_R_HAND:
				bodypart = 0x07;
				break;
			case L2Item.SLOT_L_HAND:
				bodypart = 0x08;
				break;
			case L2Item.SLOT_GLOVES:
				bodypart = 0x09;
				break;
			case L2Item.SLOT_CHEST:
				bodypart = 0x0a;
				break;
			case L2Item.SLOT_LEGS:
				bodypart = 0x0b;
				break;
			case L2Item.SLOT_FEET:
				bodypart = 0x0c;
				break;
			case L2Item.SLOT_BACK:
				bodypart = 0x0d;
				break;
			case L2Item.SLOT_LR_HAND:
				bodypart = 0x0e;
				break;
			case L2Item.SLOT_HAIR:
				bodypart = 0x0f;
				break;
		}
		
		if (Config.DEBUG)
		{
			LOGGER.debug("body:" + bodypart);
		}
		writeD(bodypart);
	}
	
	@Override
	public String getType()
	{
		return "[S] 4b EquipUpdate";
	}
}
