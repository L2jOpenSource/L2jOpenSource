package com.l2jfrozen.gameserver.handler.itemhandlers;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.datatables.csv.ExtractableItemsData;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.model.L2ExtractableItem;
import com.l2jfrozen.gameserver.model.L2ExtractableProductItem;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author FBIagent 11/12/2006
 */
public class ExtractableItems implements IItemHandler
{
	private static Logger LOGGER = Logger.getLogger(ItemTable.class);
	
	public void doExtract(final L2PlayableInstance playable, final L2ItemInstance item, int count)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance activeChar = (L2PcInstance) playable;
		final int itemID = item.getItemId();
		
		if (count > item.getCount())
		{
			return;
		}
		
		while (count-- > 0)
		{
			L2ExtractableItem exitem = ExtractableItemsData.getInstance().getExtractableItem(itemID);
			
			if (exitem == null)
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			int createItemID = 0;
			int createAmount = 0;
			final int rndNum = Rnd.get(100);
			int chanceFrom = 0;
			
			for (L2ExtractableProductItem expi : exitem.getProductItems())
			{
				int chance = expi.getChance();
				
				if (rndNum >= chanceFrom && rndNum <= (chance + chanceFrom))
				{
					createItemID = expi.getId();
					createAmount = expi.getAmmount();
					break;
				}
				
				chanceFrom += chance;
			}
			
			if (createItemID == 0)
			{
				activeChar.sendMessage("Nothing happened.");
				return;
			}
			
			if (createItemID > 0)
			{
				if (ItemTable.getInstance().createDummyItem(createItemID) == null)
				{
					LOGGER.warn("createItemID " + createItemID + " doesn't have template!");
					activeChar.sendMessage("Nothing happened.");
					return;
				}
				
				if (ItemTable.getInstance().createDummyItem(createItemID).isStackable())
				{
					activeChar.addItem("Extract", createItemID, createAmount, item, false);
				}
				else
				{
					for (int i = 0; i < createAmount; i++)
					{
						activeChar.addItem("Extract", createItemID, 1, item, false);
					}
				}
				
				SystemMessage sm;
				
				if (createAmount > 1)
				{
					sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
					sm.addItemName(createItemID);
					sm.addNumber(createAmount);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
					sm.addItemName(createItemID);
				}
				activeChar.sendPacket(sm);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
			}
			
			activeChar.destroyItemByItemId("Extract", itemID, 1, activeChar.getTarget(), true);
		}
	}
	
	// by Azagthtot
	@Override
	public void useItem(final L2PlayableInstance playable, final L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		if (item.getCount() > 1)
		{
			String message = HtmCache.getInstance().getHtm("data/html/others/extractable.htm");
			if (message == null)
			{
				doExtract(playable, item, 1);
			}
			else
			{
				message = message.replace("%objectId%", String.valueOf(item.getObjectId()));
				message = message.replace("%itemname%", item.getItemName());
				message = message.replace("%count%", String.valueOf(item.getCount()));
				playable.sendPacket(new NpcHtmlMessage(5, message));
			}
		}
		else
		{
			doExtract(playable, item, 1);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ExtractableItemsData.getInstance().itemIDs();
	}
}