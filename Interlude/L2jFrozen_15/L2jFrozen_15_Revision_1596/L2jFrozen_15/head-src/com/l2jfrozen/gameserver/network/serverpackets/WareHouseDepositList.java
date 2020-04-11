package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * 0x53 WareHouseDepositList dh (h dddhh dhhh d)
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class WareHouseDepositList extends L2GameServerPacket
{
	private static Logger LOGGER = Logger.getLogger(WareHouseDepositList.class);
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3; // not sure
	public static final int FREIGHT = 4; // not sure
	private final L2PcInstance activeChar;
	private final int playerAdena;
	private final List<L2ItemInstance> items;
	private final int whType;
	
	public WareHouseDepositList(final L2PcInstance player, final int type)
	{
		activeChar = player;
		whType = type;
		playerAdena = activeChar.getAdena();
		items = new ArrayList<>();
		
		for (final L2ItemInstance temp : activeChar.getInventory().getAvailableItems(true))
		{
			items.add(temp);
		}
		
		// augmented and shadow items can be stored in private wh
		if (whType == PRIVATE)
		{
			for (final L2ItemInstance temp : player.getInventory().getItems())
			{
				if (temp != null && !temp.isEquipped() && (temp.isShadowItem() || temp.isAugmented()))
				{
					items.add(temp);
				}
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x41);
		/*
		 * 0x01-Private Warehouse 0x02-Clan Warehouse 0x03-Castle Warehouse 0x04-Warehouse
		 */
		writeH(whType);
		writeD(playerAdena);
		final int count = items.size();
		if (Config.DEBUG)
		{
			LOGGER.debug("count:" + count);
		}
		writeH(count);
		
		for (final L2ItemInstance item : items)
		{
			writeH(item.getItem().getType1()); // item type1 //unconfirmed, works
			writeD(item.getObjectId()); // unconfirmed, works
			writeD(item.getItemId()); // unconfirmed, works
			writeD(item.getCount()); // unconfirmed, works
			writeH(item.getItem().getType2()); // item type2 //unconfirmed, works
			writeH(0x00); // ? 100
			writeD(item.getItem().getBodyPart()); // ?
			writeH(item.getEnchantLevel()); // enchant level -confirmed
			writeH(0x00); // ? 300
			writeH(0x00); // ? 200
			writeD(item.getObjectId()); // item id - confimed
			if (item.isAugmented())
			{
				writeD(0x0000FFFF & item.getAugmentation().getAugmentationId());
				writeD(item.getAugmentation().getAugmentationId() >> 16);
			}
			else
			{
				writeQ(0x00);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 41 WareHouseDepositList";
	}
}
