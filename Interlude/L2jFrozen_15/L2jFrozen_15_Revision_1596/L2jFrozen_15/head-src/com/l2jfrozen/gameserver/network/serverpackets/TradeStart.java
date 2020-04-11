package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class TradeStart extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	private final L2ItemInstance[] itemList;
	
	public TradeStart(final L2PcInstance player)
	{
		activeChar = player;
		itemList = activeChar.getInventory().getAvailableItems(true);
	}
	
	@Override
	protected final void writeImpl()
	{
		// 0x2e TradeStart d h (h dddhh dhhh)
		if (activeChar.getActiveTradeList() == null || activeChar.getActiveTradeList().getPartner() == null)
		{
			return;
		}
		
		writeC(0x1E);
		writeD(activeChar.getActiveTradeList().getPartner().getObjectId());
		// writeD((_activeChar != null || activeChar.getTransactionRequester() != null)? activeChar.getTransactionRequester().getObjectId() : 0);
		
		writeH(itemList.length);
		for (final L2ItemInstance item : itemList)// int i = 0; i < count; i++)
		{
			writeH(item.getItem().getType1()); // item type1
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(item.getCount());
			writeH(item.getItem().getType2()); // item type2
			writeH(0x00); // ?
			
			writeD(item.getItem().getBodyPart()); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
			writeH(item.getEnchantLevel()); // enchant level
			writeH(0x00); // ?
			writeH(0x00);
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 1E TradeStart";
	}
}
