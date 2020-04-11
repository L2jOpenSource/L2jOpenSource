package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.TradeList;

/**
 * This class ...
 * @author  Yme
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class TradeOtherAdd extends L2GameServerPacket
{
	private final TradeList.TradeItem item;
	
	public TradeOtherAdd(final TradeList.TradeItem item)
	{
		this.item = item;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x21);
		
		writeH(1); // item count
		
		writeH(item.getItem().getType1()); // item type1
		writeD(item.getObjectId());
		writeD(item.getItem().getItemId());
		writeD(item.getCount());
		writeH(item.getItem().getType2()); // item type2
		writeH(0x00); // ?
		
		writeD(item.getItem().getBodyPart()); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
		writeH(item.getEnchant()); // enchant level
		writeH(0x00); // ?
		writeH(0x00);
	}
	
	@Override
	public String getType()
	{
		return "[S] 21 TradeOtherAdd";
	}
}
