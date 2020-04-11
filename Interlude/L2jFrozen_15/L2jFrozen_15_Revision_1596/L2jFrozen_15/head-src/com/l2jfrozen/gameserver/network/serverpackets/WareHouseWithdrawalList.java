package com.l2jfrozen.gameserver.network.serverpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * 0x42 WarehouseWithdrawalList dh (h dddhh dhhh d)
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/29 23:15:10 $
 */
public class WareHouseWithdrawalList extends L2GameServerPacket
{
	private static final Logger LOGGER = Logger.getLogger(WareHouseWithdrawalList.class);
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3; // not sure
	public static final int FREIGHT = 4; // not sure
	private L2PcInstance activeChar;
	private int playerAdena;
	private L2ItemInstance[] items;
	private int whType;
	
	public WareHouseWithdrawalList(final L2PcInstance player, final int type)
	{
		activeChar = player;
		whType = type;
		
		playerAdena = activeChar.getAdena();
		if (activeChar.getActiveWarehouse() == null)
		{
			// Something went wrong!
			LOGGER.warn("error while sending withdraw request to: " + activeChar.getName());
			return;
		}
		items = activeChar.getActiveWarehouse().getItems();
		
		if (Config.DEBUG)
		{
			for (final L2ItemInstance item : items)
			{
				LOGGER.debug("item:" + item.getItem().getName() + " type1:" + item.getItem().getType1() + " type2:" + item.getItem().getType2());
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x42);
		/*
		 * 0x01-Private Warehouse 0x02-Clan Warehouse 0x03-Castle Warehouse 0x04-Warehouse
		 */
		writeH(whType);
		writeD(playerAdena);
		writeH(items.length);
		
		for (final L2ItemInstance item : items)
		{
			writeH(item.getItem().getType1()); // item type1 //unconfirmed, works
			writeD(0x00); // unconfirmed, works
			writeD(item.getItemId()); // unconfirmed, works
			writeD(item.getCount()); // unconfirmed, works
			writeH(item.getItem().getType2()); // item type2 //unconfirmed, works
			writeH(0x00); // ?
			writeD(item.getItem().getBodyPart()); // ?
			writeH(item.getEnchantLevel()); // enchant level -confirmed
			writeH(0x00); // ?
			writeH(0x00); // ?
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
		return "[S] 42 WareHouseWithdrawalList";
	}
}
