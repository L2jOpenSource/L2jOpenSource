package com.l2jfrozen.gameserver.network.clientpackets;

import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.controllers.TradeController;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2TradeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2CastleChamberlainInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ClanHallManagerInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2FishermanInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MercManagerInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MerchantInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.util.Util;

/**
 * This class ...
 * @version $Revision: 1.12.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestBuyItem extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestBuyItem.class);
	
	private int listId;
	private int count;
	private int[] items; // count*2
	
	@Override
	protected void readImpl()
	{
		listId = readD();
		count = readD();
		// count*8 is the size of a for iteration of each item
		if (count * 2 < 0 || count > Config.MAX_ITEM_IN_PACKET || count * 8 > buf.remaining())
		{
			count = 0;
		}
		
		items = new int[count * 2];
		for (int i = 0; i < count; i++)
		{
			final int itemId = readD();
			items[i * 2 + 0] = itemId;
			final long cnt = readD();
			
			if (cnt > Integer.MAX_VALUE || cnt < 0)
			{
				count = 0;
				items = null;
				return;
			}
			
			items[i * 2 + 1] = (int) cnt;
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
			|| !(target instanceof L2MerchantInstance || target instanceof L2FishermanInstance || target instanceof L2MercManagerInstance || target instanceof L2ClanHallManagerInstance || target instanceof L2CastleChamberlainInstance) // Target not a merchant, fisherman or mercmanager
			|| !player.isInsideRadius(target, L2NpcInstance.INTERACTION_DISTANCE, false, false) // Distance is too far
		))
		{
			return;
		}
		
		boolean ok = true;
		String htmlFolder = "";
		
		if (target != null)
		{
			if (target instanceof L2MerchantInstance)
			{
				htmlFolder = "merchant";
			}
			else if (target instanceof L2FishermanInstance)
			{
				htmlFolder = "fisherman";
			}
			else if (target instanceof L2MercManagerInstance)
			{
				ok = true;
			}
			else if (target instanceof L2ClanHallManagerInstance)
			{
				ok = true;
			}
			else if (target instanceof L2CastleChamberlainInstance)
			{
				ok = true;
			}
			else
			{
				ok = false;
			}
		}
		else
		{
			ok = false;
		}
		
		L2NpcInstance merchant = null;
		
		if (ok)
		{
			merchant = (L2NpcInstance) target;
		}
		else if (!ok && !player.isGM())
		{
			player.sendMessage("Invalid Target: Seller must be targetted");
			return;
		}
		
		L2TradeList list = null;
		
		if (merchant != null)
		{
			final List<L2TradeList> lists = TradeController.getInstance().getBuyListByNpcId(merchant.getNpcId());
			
			if (!player.isGM())
			{
				if (lists == null)
				{
					Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
					return;
				}
				for (final L2TradeList tradeList : lists)
				{
					if (tradeList.getListId() == listId)
					{
						list = tradeList;
					}
				}
			}
			else
			{
				list = TradeController.getInstance().getBuyList(listId);
			}
		}
		else
		{
			list = TradeController.getInstance().getBuyList(listId);
		}
		
		if (list == null)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
			return;
		}
		
		listId = list.getListId();
		
		if (listId > 1000000) // lease
		{
			if (merchant != null && merchant.getTemplate().npcId != listId - 1000000)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		if (count < 1)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		double taxRate = 0;
		
		if (merchant != null && merchant.getIsInTown())
		{
			taxRate = merchant.getCastle().getTaxRate();
		}
		
		long subTotal = 0;
		int tax = 0;
		
		// Check for buylist validity and calculates summary values
		long slots = 0;
		long weight = 0;
		for (int i = 0; i < count; i++)
		{
			final int itemId = items[i * 2 + 0];
			final int count = items[i * 2 + 1];
			int price = -1;
			
			if (!list.containsItemId(itemId))
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
				return;
			}
			
			final L2Item template = ItemTable.getInstance().getTemplate(itemId);
			
			if (template == null)
			{
				continue;
			}
			
			// Check count
			if (count > Integer.MAX_VALUE || !template.isStackable() && count > 1)
			{
				// Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase invalid quantity of items at the same time.", Config.DEFAULT_PUNISH);
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				sendPacket(sm);
				sm = null;
				return;
			}
			
			if (listId < 1000000)
			{
				// list = TradeController.getInstance().getBuyList(_listId);
				price = list.getPriceForItemId(itemId);
				
				if (itemId >= 3960 && itemId <= 4026)
				{
					price *= Config.RATE_SIEGE_GUARDS_PRICE;
				}
				
			}
			/*
			 * TODO: Disabled until Leaseholders are rewritten ;-) } else { L2ItemInstance li = merchant.findLeaseItem(itemId, 0); if (li == null || li.getCount() < cnt) { cnt = li.getCount(); if (cnt <= 0) { items.remove(i); continue; } items.get(i).setCount((int)cnt); } price = li.getPriceToSell(); // lease
			 * holder sells the item weight = li.getItem().getWeight(); }
			 */
			if (price < 0)
			{
				LOGGER.warn("ERROR, no price found .. wrong buylist ??");
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (price == 0 && !player.isGM() && Config.ONLY_GM_ITEMS_FREE)
			{
				player.sendMessage("Ohh Cheat dont work? You have a problem now!");
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried buy item for 0 adena.", Config.DEFAULT_PUNISH);
				return;
			}
			
			subTotal += (long) count * price; // Before tax
			tax = (int) (subTotal * taxRate);
			
			// Check subTotal + tax
			if (subTotal + tax > Integer.MAX_VALUE)
			{
				// Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " adena worth of goods.", Config.DEFAULT_PUNISH);
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				sendPacket(sm);
				sm = null;
				return;
			}
			
			weight += (long) count * template.getWeight();
			if (!template.isStackable())
			{
				slots += count;
			}
			else if (player.getInventory().getItemByItemId(itemId) == null)
			{
				slots++;
			}
		}
		
		if (weight > Integer.MAX_VALUE || weight < 0 || !player.getInventory().validateWeight((int) weight))
		{
			sendPacket(new SystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			return;
		}
		
		if (slots > Integer.MAX_VALUE || slots < 0 || !player.getInventory().validateCapacity((int) slots))
		{
			sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
			return;
		}
		
		// Charge buyer and add tax to castle treasury if not owned by npc clan
		if (subTotal < 0 || !player.reduceAdena("Buy", (int) (subTotal + tax), player.getLastFolkNPC(), false))
		{
			sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}
		
		if (merchant != null && merchant.getIsInTown() && merchant.getCastle().getOwnerId() > 0)
		{
			merchant.getCastle().addToTreasury(tax);
		}
		
		// Proceed the purchase
		for (int i = 0; i < count; i++)
		{
			final int itemId = items[i * 2 + 0];
			int count = items[i * 2 + 1];
			
			if (count < 0)
			{
				count = 0;
			}
			
			if (!list.containsItemId(itemId))
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
				return;
			}
			
			if (list.countDecrease(itemId))
			{
				if (!list.decreaseCount(itemId, count))
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
					sendPacket(sm);
					sm = null;
					return;
				}
				
			}
			// Add item to Inventory and adjust update packet
			player.getInventory().addItem("Buy", itemId, count, player, merchant);
			/*
			 * TODO: Disabled until Leaseholders are rewritten ;-) // Update Leaseholder list if (_listId >= 1000000) { L2ItemInstance li = merchant.findLeaseItem(item.getItemId(), 0); if (li == null) continue; if (li.getCount() < item.getCount()) item.setCount(li.getCount()); li.setCount(li.getCount() -
			 * item.getCount()); li.updateDatabase(); price = item.getCount() + li.getPriceToSell(); L2ItemInstance la = merchant.getLeaseAdena(); la.setCount(la.getCount() + price); la.updateDatabase(); player.getInventory().addItem(item); item.updateDatabase(); }
			 */
		}
		
		if (merchant != null)
		{
			final String html = HtmCache.getInstance().getHtm("data/html/" + htmlFolder + "/" + merchant.getNpcId() + "-bought.htm");
			
			if (html != null)
			{
				final NpcHtmlMessage boughtMsg = new NpcHtmlMessage(merchant.getObjectId());
				boughtMsg.setHtml(html.replaceAll("%objectId%", String.valueOf(merchant.getObjectId())));
				player.sendPacket(boughtMsg);
			}
		}
		
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		player.sendPacket(new ItemList(player, true));
	}
	
	@Override
	public String getType()
	{
		return "[C] 1F RequestBuyItem";
	}
}
