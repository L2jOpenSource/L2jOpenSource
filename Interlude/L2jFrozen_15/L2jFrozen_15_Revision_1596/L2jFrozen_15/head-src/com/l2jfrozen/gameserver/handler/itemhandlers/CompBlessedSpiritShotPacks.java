package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:30:07 $
 */

public class CompBlessedSpiritShotPacks implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5146,
		5147,
		5148,
		5149,
		5150,
		5151,
		5262,
		5263,
		5264,
		5265,
		5266,
		5267
	};
	
	@Override
	public void useItem(final L2PlayableInstance playable, final L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		
		final int itemId = item.getItemId();
		int itemToCreateId;
		int amount;
		
		if (itemId < 5200)
		{ // Normal Compressed Package of SpiritShots
			itemToCreateId = itemId - 1199; // Gives id of matching item for this pack
			amount = 300;
		}
		else
		{ // Greater Compressed Package of Spirithots
			itemToCreateId = itemId - 1315; // Gives id of matching item for this pack
			amount = 1000;
		}
		
		activeChar.getInventory().destroyItem("Extract", item, activeChar, null);
		activeChar.getInventory().addItem("Extract", itemToCreateId, amount, activeChar, item);
		
		SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
		sm.addItemName(itemToCreateId);
		sm.addNumber(amount);
		activeChar.sendPacket(sm);
		sm = null;
		
		ItemList playerUI = new ItemList(activeChar, false);
		activeChar.sendPacket(playerUI);
		
		playerUI = null;
		activeChar = null;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
