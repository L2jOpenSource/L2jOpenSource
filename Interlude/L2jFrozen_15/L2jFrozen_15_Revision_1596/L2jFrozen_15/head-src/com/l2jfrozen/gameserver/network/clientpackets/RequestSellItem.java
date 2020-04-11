package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2FishermanInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MerchantInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestSellItem extends L2GameClientPacket
{
	private int listId;
	private int count;
	private int[] items; // count*3
	
	/**
	 * packet type id 0x1e sample 1e 00 00 00 00 // list id 02 00 00 00 // number of items 71 72 00 10 // object id ea 05 00 00 // item id 01 00 00 00 // item count 76 4b 00 10 // object id 2e 0a 00 00 // item id 01 00 00 00 // item count format: cdd (ddd)
	 */
	@Override
	protected void readImpl()
	{
		listId = readD();
		count = readD();
		
		if (count <= 0 || count * 12 > buf.remaining() || count > Config.MAX_ITEM_IN_PACKET)
		{
			count = 0;
			items = null;
			return;
		}
		
		items = new int[count * 3];
		
		for (int i = 0; i < count; i++)
		{
			final int objectId = readD();
			items[i * 3 + 0] = objectId;
			final int itemId = readD();
			items[i * 3 + 1] = itemId;
			final long cnt = readD();
			
			if (cnt > Integer.MAX_VALUE || cnt <= 0)
			{
				count = 0;
				items = null;
				return;
			}
			items[i * 3 + 2] = (int) cnt;
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("buy"))
		{
			player.sendMessage("You buying too fast.");
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0)
		{
			return;
		}
		
		final L2Object target = player.getTarget();
		if (!player.isGM() && (target == null // No target (ie GM Shop)
			|| !(target instanceof L2MerchantInstance) // Target not a merchant and not mercmanager
			|| !player.isInsideRadius(target, L2NpcInstance.INTERACTION_DISTANCE, false, false)))
		{
			return; // Distance is too far
		}
		
		String htmlFolder = "";
		L2NpcInstance merchant = null;
		if (target instanceof L2MerchantInstance)
		{
			htmlFolder = "merchant";
			merchant = (L2NpcInstance) target;
		}
		else if (target instanceof L2FishermanInstance)
		{
			htmlFolder = "fisherman";
			merchant = (L2NpcInstance) target;
		}
		else
		{
			return;
		}
		
		if (listId > 1000000) // lease
		{
			if (merchant.getTemplate().npcId != listId - 1000000)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		long totalPrice = 0;
		// Proceed the sell
		for (int i = 0; i < count; i++)
		{
			final int objectId = items[i * 3 + 0];
			final int count = items[i * 3 + 2];
			
			// Check count
			if (count <= 0 || count > Integer.MAX_VALUE)
			{
				// Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " items at the same time.", Config.DEFAULT_PUNISH);
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				sendPacket(sm);
				sm = null;
				return;
			}
			
			L2ItemInstance item = player.checkItemManipulation(objectId, count, "sell");
			
			// Check Item
			if (item == null || !item.getItem().isSellable())
			{
				continue;
			}
			
			final long price = item.getReferencePrice() / 2;
			totalPrice += price * count;
			
			// Fix exploit during Sell
			if ((Integer.MAX_VALUE / count) < price || totalPrice > Integer.MAX_VALUE)
			{
				// Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + MAX_ADENA + " adena worth of goods.", Config.DEFAULT_PUNISH);
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				sendPacket(sm);
				sm = null;
				return;
			}
			
			// Check totalPrice
			if (totalPrice <= 0)
			{
				// Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " adena worth of goods.", Config.DEFAULT_PUNISH);
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				sendPacket(sm);
				sm = null;
				return;
			}
			
			item = player.getInventory().destroyItem("Sell", objectId, count, player, null);
			
			/*
			 * TODO: Disabled until Leaseholders are rewritten ;-) int price = item.getReferencePrice()*(int)count/2; L2ItemInstance li = null; L2ItemInstance la = null; if (_listId > 1000000) { li = merchant.findLeaseItem(item.getItemId(),item.getEnchantLevel()); la = merchant.getLeaseAdena(); if (li == null
			 * || la == null) continue; price = li.getPriceToBuy()*(int)count; // player sells, thus merchant buys. if (price > la.getCount()) continue; }
			 */
			/*
			 * TODO: Disabled until Leaseholders are rewritten ;-) if (item != null && listId > 1000000) { li.setCount(li.getCount()+(int)count); li.updateDatabase(); la.setCount(la.getCount()-price); la.updateDatabase(); }
			 */
		}
		player.addAdena("Sell", (int) totalPrice, merchant, false);
		
		final String html = HtmCache.getInstance().getHtm("data/html/" + htmlFolder + "/" + merchant.getNpcId() + "-sold.htm");
		
		if (html != null)
		{
			final NpcHtmlMessage soldMsg = new NpcHtmlMessage(merchant.getObjectId());
			soldMsg.setHtml(html.replaceAll("%objectId%", String.valueOf(merchant.getObjectId())));
			player.sendPacket(soldMsg);
		}
		
		// Update current load as well
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		player.sendPacket(new ItemList(player, true));
	}
	
	@Override
	public String getType()
	{
		return "[C] 1E RequestSellItem";
	}
}
