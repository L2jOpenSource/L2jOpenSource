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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.engines.DocumentParser;
import l2r.gameserver.model.buylist.L2BuyList;
import l2r.gameserver.model.buylist.Product;
import l2r.gameserver.model.items.L2Item;
import l2r.util.file.filter.NumericNameFilter;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Nos
 */
public final class BuyListData extends DocumentParser
{
	private final Map<Integer, L2BuyList> _buyLists = new HashMap<>();
	
	protected BuyListData()
	{
		setCurrentFileFilter(new NumericNameFilter());
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_buyLists.clear();
		parseDirectory("data/xml/buylists", false);
		if (Config.CUSTOM_BUYLIST_LOAD)
		{
			parseDirectory("data/xml/buylists/custom", false);
		}
		
		_log.info(getClass().getSimpleName() + ": Loaded " + _buyLists.size() + " BuyLists.");
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement statement = con.createStatement();
			ResultSet rs = statement.executeQuery("SELECT * FROM `buylists`"))
		{
			while (rs.next())
			{
				int buyListId = rs.getInt("buylist_id");
				int itemId = rs.getInt("item_id");
				long count = rs.getLong("count");
				long nextRestockTime = rs.getLong("next_restock_time");
				final L2BuyList buyList = getBuyList(buyListId);
				if (buyList == null)
				{
					_log.warn("BuyList found in database but not loaded from xml! BuyListId: " + buyListId);
					continue;
				}
				final Product product = buyList.getProductByItemId(itemId);
				if (product == null)
				{
					_log.warn("ItemId found in database but not loaded from xml! BuyListId: " + buyListId + " ItemId: " + itemId);
					continue;
				}
				if (count < product.getMaxCount())
				{
					product.setCount(count);
					product.restartRestockTask(nextRestockTime);
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Failed to load buyList data from database.", e);
		}
	}
	
	@Override
	protected void parseDocument()
	{
		try
		{
			final int buyListId = Integer.parseInt(getCurrentFile().getName().replaceAll(".xml", ""));
			
			for (Node node = getCurrentDocument().getFirstChild(); node != null; node = node.getNextSibling())
			{
				if ("list".equalsIgnoreCase(node.getNodeName()))
				{
					final L2BuyList buyList = new L2BuyList(buyListId);
					for (Node list_node = node.getFirstChild(); list_node != null; list_node = list_node.getNextSibling())
					{
						if ("item".equalsIgnoreCase(list_node.getNodeName()))
						{
							int itemId = -1;
							long price = -1;
							long restockDelay = -1;
							long count = -1;
							NamedNodeMap attrs = list_node.getAttributes();
							Node attr = attrs.getNamedItem("id");
							itemId = Integer.parseInt(attr.getNodeValue());
							attr = attrs.getNamedItem("price");
							if (attr != null)
							{
								price = Long.parseLong(attr.getNodeValue());
							}
							attr = attrs.getNamedItem("restock_delay");
							if (attr != null)
							{
								restockDelay = Long.parseLong(attr.getNodeValue());
							}
							attr = attrs.getNamedItem("count");
							if (attr != null)
							{
								count = Long.parseLong(attr.getNodeValue());
							}
							final L2Item item = ItemData.getInstance().getTemplate(itemId);
							if (item != null)
							{
								buyList.addProduct(new Product(buyList.getListId(), item, price, restockDelay, count));
							}
							else
							{
								_log.warn("Item not found. BuyList:" + buyList.getListId() + " ItemID:" + itemId + " File:" + getCurrentFile().getName());
							}
						}
						else if ("npcs".equalsIgnoreCase(list_node.getNodeName()))
						{
							for (Node npcs_node = list_node.getFirstChild(); npcs_node != null; npcs_node = npcs_node.getNextSibling())
							{
								if ("npc".equalsIgnoreCase(npcs_node.getNodeName()))
								{
									int npcId = Integer.parseInt(npcs_node.getTextContent());
									buyList.addAllowedNpc(npcId);
								}
							}
						}
					}
					_buyLists.put(buyList.getListId(), buyList);
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Failed to load buyList data from xml File:" + getCurrentFile().getName(), e);
		}
	}
	
	public L2BuyList getBuyList(int listId)
	{
		return _buyLists.get(listId);
	}
	
	public static BuyListData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected final static BuyListData _instance = new BuyListData();
	}
}
