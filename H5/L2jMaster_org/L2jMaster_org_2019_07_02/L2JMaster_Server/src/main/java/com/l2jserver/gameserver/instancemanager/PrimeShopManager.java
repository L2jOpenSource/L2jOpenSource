/*
 * Copyright (C) 2004-2019 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.instancemanager;

import com.l2jserver.Config;
import com.l2jserver.gameserver.data.sql.impl.PrimeShopTable;
import com.l2jserver.gameserver.data.xml.impl.PrimeShopData;
import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.model.PrimeShop;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExBrBuyProduct;
import com.l2jserver.gameserver.network.serverpackets.ExBrGamePoint;
import com.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

public final class PrimeShopManager
{
	public PrimeShopManager()
	{
		// empty
	}
	
	public void giveProduct(L2PcInstance player, int productId, int count)
	{
		if (!Config.PRIME_SHOP_POINTS_ENABLED)
		{
			player.sendMessage("Feature disabled.");
			return;
		}
		
		if (!player.isInsideZone(ZoneId.PEACE) || (player.isOnlineInt() == 0) || player.isJailed())
		{
			player.sendMessage("The Prime Shop can only be used in peace zones!");
			return;
		}
		
		if (player.getPrimeShopPoints() > Config.PRIME_SHOP_POINTS_MAX)
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EXCEEDED_MAXIMUM_AMOUNT);
			player.setPrimeShopPoints(Config.PRIME_SHOP_POINTS_MAX);
			player.sendPacket(sm);
			return;
		}
		
		final PrimeShop product = PrimeShopData.getInstance().getProduct(productId);
		if (product == null)
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_WRONG_PRODUCT));
			return;
		}
		
		final long totalPoints = product.getPrice() * count;
		if (totalPoints < 0)
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_WRONG_PRODUCT));
			return;
		}
		
		final long gamePointSize = Config.PRIME_SHOP_POINTS_ID == -1 ? player.getPrimeShopPoints() : player.getInventory().getInventoryItemCount(Config.PRIME_SHOP_POINTS_ID, -1);
		if (totalPoints > gamePointSize)
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_NOT_ENOUGH_POINTS));
			return;
		}
		
		final L2Item item = ItemTable.getInstance().getTemplate(product.getItemId());
		if (item == null)
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_SERVER_ERROR));
			return;
		}
		
		// TODO: No stock available (need new value in xml data file).
		// player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_SOLD_OUT));
		
		final int totalWeight = product.getItemWeight() * product.getItemCount() * count;
		int totalCount = 0;
		totalCount += item.isStackable() ? 1 : product.getItemCount() * count;
		if (!player.getInventory().validateCapacity(totalCount) || !player.getInventory().validateWeight(totalWeight))
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_INVENTORY_FULL));
			return;
		}
		
		// Pay for Item
		if (Config.PRIME_SHOP_POINTS_ID == -1)
		{
			player.setPrimeShopPoints(player.getPrimeShopPoints() - totalPoints);
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
			sm.addString("Points");
			sm.addInt((int) totalPoints);
			player.sendPacket(sm);
		}
		else
		{
			player.getInventory().destroyItemByItemId("Buy Product" + productId, Config.PRIME_SHOP_POINTS_ID, totalPoints, player, null);
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
			sm.addItemName(Config.PRIME_SHOP_POINTS_ID);
			sm.addInt((int) totalPoints);
			player.sendPacket(sm);
		}
		
		// Buy Item
		player.getInventory().addItem("Buy Product" + productId, product.getItemId(), product.getItemCount() * count, player, null);
		
		// Info message for add item
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
		sm.addItemName(product.getItemId());
		sm.addInt(product.getItemCount() * count);
		player.sendPacket(sm);
		
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		
		player.sendPacket(new ExBrGamePoint(player));
		player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_OK));
		player.broadcastUserInfo();
		
		// Save transaction info at SQL table
		PrimeShopTable.getInstance().addPoduct(player.getObjectId(), product.getProductId(), count, count);
	}
	
	public static PrimeShopManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PrimeShopManager _instance = new PrimeShopManager();
	}
}