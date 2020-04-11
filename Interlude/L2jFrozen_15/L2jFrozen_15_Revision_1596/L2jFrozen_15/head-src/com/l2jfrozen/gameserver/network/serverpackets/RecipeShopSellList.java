package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2ManufactureItem;
import com.l2jfrozen.gameserver.model.L2ManufactureList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ... dddd d(ddd)
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class RecipeShopSellList extends L2GameServerPacket
{
	private final L2PcInstance buyer, manufacturer;
	
	public RecipeShopSellList(final L2PcInstance buyer, final L2PcInstance manufacturer)
	{
		this.buyer = buyer;
		this.manufacturer = manufacturer;
	}
	
	@Override
	protected final void writeImpl()
	{
		final L2ManufactureList createList = manufacturer.getCreateList();
		
		if (createList != null)
		{
			// dddd d(ddd)
			writeC(0xd9);
			writeD(manufacturer.getObjectId());
			writeD((int) manufacturer.getCurrentMp());// Creator's MP
			writeD(manufacturer.getMaxMp());// Creator's MP
			writeD(buyer.getAdena());// Buyer Adena
			
			final int count = createList.size();
			writeD(count);
			L2ManufactureItem temp;
			
			for (int i = 0; i < count; i++)
			{
				temp = createList.getList().get(i);
				writeD(temp.getRecipeId());
				writeD(0x00); // unknown
				writeD(temp.getCost());
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] d9 RecipeShopSellList";
	}
	
}
