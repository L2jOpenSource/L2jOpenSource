/*
 * Copyright (C) 2004-2014 L2J Server
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
package l2r.gameserver.datatables.xml;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import l2r.Config;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.primeshop.L2ProductItem;
import l2r.gameserver.model.primeshop.L2ProductItemComponent;
import l2r.gameserver.network.serverpackets.ExBrBuyProduct;
import l2r.gameserver.network.serverpackets.ExBrGamePoint;
import l2r.gameserver.network.serverpackets.ExBrRecentProductList;
import l2r.gameserver.network.serverpackets.StatusUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Created by GodFather
 */
public class ProductItemData
{
	protected static final Logger _log = LoggerFactory.getLogger(ProductItemData.class);
	
	private final Map<Integer, L2ProductItem> _itemsList = new TreeMap<>();
	private final ConcurrentHashMap<Integer, List<L2ProductItem>> recentList;
	
	private static ProductItemData _instance = new ProductItemData();
	
	public static ProductItemData getInstance()
	{
		if (_instance == null)
		{
			_instance = new ProductItemData();
		}
		return _instance;
	}
	
	public void reload()
	{
		_instance = new ProductItemData();
	}
	
	protected ProductItemData()
	{
		recentList = new ConcurrentHashMap<>();
		try
		{
			DocumentBuilderFactory factory1 = DocumentBuilderFactory.newInstance();
			factory1.setValidating(false);
			factory1.setIgnoringComments(true);
			
			File file = new File(Config.DATAPACK_ROOT, "data/xml/item-mall.xml");
			if (!file.exists())
			{
				_log.warn(getClass().getSimpleName() + ": Couldn't find data/" + file.getName());
				return;
			}
			
			Document doc1 = factory1.newDocumentBuilder().parse(file);
			
			for (Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n1.getNodeName()))
				{
					for (Node d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling())
					{
						if ("product".equalsIgnoreCase(d1.getNodeName()))
						{
							int productId = Integer.parseInt(d1.getAttributes().getNamedItem("id").getNodeValue());
							
							Node categoryNode = d1.getAttributes().getNamedItem("category");
							int category = categoryNode != null ? Integer.parseInt(categoryNode.getNodeValue()) : 5;
							
							Node priceNode = d1.getAttributes().getNamedItem("price");
							int price = priceNode != null ? Integer.parseInt(priceNode.getNodeValue()) : 0;
							
							Node isEventNode = d1.getAttributes().getNamedItem("is_event");
							Boolean isEvent = (isEventNode != null) && Boolean.parseBoolean(isEventNode.getNodeValue());
							
							Node isBestNode = d1.getAttributes().getNamedItem("is_best");
							Boolean isBest = (isBestNode != null) && Boolean.parseBoolean(isBestNode.getNodeValue());
							
							Node isNewNode = d1.getAttributes().getNamedItem("is_new");
							Boolean isNew = (isNewNode != null) && Boolean.parseBoolean(isNewNode.getNodeValue());
							
							int tabId = getProductTabId(isEvent, isBest, isNew);
							
							Node startTimeNode = d1.getAttributes().getNamedItem("sale_start_date");
							long startTimeSale = startTimeNode != null ? getMillisecondsFromString(startTimeNode.getNodeValue()) : 0;
							
							Node endTimeNode = d1.getAttributes().getNamedItem("sale_end_date");
							long endTimeSale = endTimeNode != null ? getMillisecondsFromString(endTimeNode.getNodeValue()) : 0;
							
							ArrayList<L2ProductItemComponent> components = new ArrayList<>();
							L2ProductItem pr = new L2ProductItem(productId, category, price, tabId, startTimeSale, endTimeSale);
							for (Node t1 = d1.getFirstChild(); t1 != null; t1 = t1.getNextSibling())
							{
								if ("component".equalsIgnoreCase(t1.getNodeName()))
								{
									int item_id = Integer.parseInt(t1.getAttributes().getNamedItem("item_id").getNodeValue());
									int count = Integer.parseInt(t1.getAttributes().getNamedItem("count").getNodeValue());
									L2ProductItemComponent component = new L2ProductItemComponent(item_id, count);
									components.add(component);
								}
							}
							pr.setComponents(components);
							_itemsList.put(productId, pr);
						}
					}
				}
			}
			_log.info(String.format(getClass().getSimpleName() + ": Loaded %d items for Item Mall.", _itemsList.size()));
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Lists could not be initialized.");
			e.printStackTrace();
		}
	}
	
	public void requestBuyItem(L2PcInstance player, int _productId, int _count)
	{
		if ((_count > 99) || (_count < 0))
		{
			return;
		}
		
		L2ProductItem product = ProductItemData.getInstance().getProduct(_productId);
		if (product == null)
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_WRONG_PRODUCT));
			return;
		}
		
		if ((System.currentTimeMillis() < product.getStartTimeSale()) || (System.currentTimeMillis() > product.getEndTimeSale()))
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_SALE_PERIOD_ENDED));
			return;
		}
		
		long totalPoints = product.getPoints() * _count;
		
		if (totalPoints < 0)
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_WRONG_PRODUCT));
			return;
		}
		
		final long gamePointSize = Config.GAME_POINT_ITEM_ID == -1 ? player.getGamePoints() : player.getInventory().getInventoryItemCount(Config.GAME_POINT_ITEM_ID, -1);
		
		if (totalPoints > gamePointSize)
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_NOT_ENOUGH_POINTS));
			return;
		}
		
		int totalWeight = 0;
		for (L2ProductItemComponent com : product.getComponents())
		{
			totalWeight += com.getWeight();
		}
		totalWeight *= _count;
		
		int totalCount = 0;
		
		for (L2ProductItemComponent com : product.getComponents())
		{
			L2Item item = ItemData.getInstance().getTemplate(com.getId());
			if (item == null)
			{
				player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_WRONG_PRODUCT));
				return;
			}
			totalCount += item.isStackable() ? 1 : com.getCount() * _count;
		}
		
		if (!player.getInventory().validateCapacity(totalCount) || !player.getInventory().validateWeight(totalWeight))
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_INVENTORY_FULL));
			return;
		}
		
		for (L2ProductItemComponent $comp : product.getComponents())
		{
			player.getInventory().addItem("Buy Product" + _productId, $comp.getId(), $comp.getCount() * _count, player, null);
		}
		
		if (Config.GAME_POINT_ITEM_ID == -1)
		{
			player.setGamePoints(player.getGamePoints() - totalPoints);
		}
		else
		{
			player.getInventory().destroyItemByItemId("Buy Product" + _productId, Config.GAME_POINT_ITEM_ID, totalPoints, player, null);
		}
		
		if (recentList.get(player.getObjectId()) == null)
		{
			List<L2ProductItem> charList = new ArrayList<>();
			charList.add(product);
			recentList.put(player.getObjectId(), charList);
		}
		else
		{
			recentList.get(player.getObjectId()).add(product);
		}
		
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		
		player.sendPacket(new ExBrGamePoint(player));
		player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_OK));
	}
	
	private static int getProductTabId(boolean isEvent, boolean isBest, boolean isNew)
	{
		if (isEvent && isBest)
		{
			return 3;
		}
		
		if (isEvent)
		{
			return 1;
		}
		
		if (isBest)
		{
			return 2;
		}
		return 4;
	}
	
	private static long getMillisecondsFromString(String datetime)
	{
		DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		try
		{
			Date time = df.parse(datetime);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(time);
			
			return calendar.getTimeInMillis();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return 0;
	}
	
	public Collection<L2ProductItem> getAllItems()
	{
		return _itemsList.values();
	}
	
	public L2ProductItem getProduct(int id)
	{
		return _itemsList.get(id);
	}
	
	public void recentProductList(L2PcInstance player)
	{
		player.sendPacket(new ExBrRecentProductList(player.getObjectId()));
	}
	
	public List<L2ProductItem> getRecentListByOID(int objId)
	{
		return recentList.get(objId) == null ? new ArrayList<>() : recentList.get(objId);
	}
}