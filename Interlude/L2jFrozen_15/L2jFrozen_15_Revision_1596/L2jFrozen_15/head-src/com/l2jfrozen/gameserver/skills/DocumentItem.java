package com.l2jfrozen.gameserver.skills;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jfrozen.gameserver.model.Item;
import com.l2jfrozen.gameserver.templates.L2Armor;
import com.l2jfrozen.gameserver.templates.L2ArmorType;
import com.l2jfrozen.gameserver.templates.L2EtcItem;
import com.l2jfrozen.gameserver.templates.L2EtcItemType;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2Weapon;
import com.l2jfrozen.gameserver.templates.L2WeaponType;
import com.l2jfrozen.gameserver.templates.StatsSet;

/**
 * @author mkizub
 */
final class DocumentItem extends DocumentBase
{
	private Item currentItem = null;
	private final List<L2Item> itemsInFile = new ArrayList<>();
	private Map<Integer, Item> itemData = new HashMap<>();
	
	/**
	 * @param pItemData
	 * @param file
	 */
	public DocumentItem(final Map<Integer, Item> pItemData, final File file)
	{
		super(file);
		itemData = pItemData;
	}
	
	/**
	 * @param item
	 */
	private void setCurrentItem(final Item item)
	{
		currentItem = item;
	}
	
	@Override
	protected StatsSet getStatsSet()
	{
		return currentItem.set;
	}
	
	@Override
	protected String getTableValue(final String name)
	{
		return tables.get(name)[currentItem.currentLevel];
	}
	
	@Override
	protected String getTableValue(final String name, final int idx)
	{
		return tables.get(name)[idx - 1];
	}
	
	@Override
	protected void parseDocument(final Document doc)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						setCurrentItem(new Item());
						parseItem(d);
						itemsInFile.add(currentItem.item);
						resetTable();
					}
				}
			}
			else if ("item".equalsIgnoreCase(n.getNodeName()))
			{
				setCurrentItem(new Item());
				parseItem(n);
				itemsInFile.add(currentItem.item);
			}
		}
	}
	
	protected void parseItem(Node n)
	{
		final int itemId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
		final String itemName = n.getAttributes().getNamedItem("name").getNodeValue();
		
		currentItem.id = itemId;
		currentItem.name = itemName;
		
		Item item;
		
		if ((item = itemData.get(currentItem.id)) == null)
		{
			throw new IllegalStateException("No SQL data for Item ID: " + itemId + " - name: " + itemName);
		}
		
		currentItem.set = item.set;
		currentItem.type = item.type;
		
		final Node first = n.getFirstChild();
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("table".equalsIgnoreCase(n.getNodeName()))
			{
				parseTable(n);
			}
		}
		
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("set".equalsIgnoreCase(n.getNodeName()))
			{
				parseBeanSet(n, itemData.get(currentItem.id).set, 1);
			}
		}
		
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("for".equalsIgnoreCase(n.getNodeName()))
			{
				makeItem();
				parseTemplate(n, currentItem.item);
			}
		}
	}
	
	private void makeItem()
	{
		if (currentItem.item != null)
		{
			return;
		}
		
		if (currentItem.type instanceof L2ArmorType)
		{
			currentItem.item = new L2Armor((L2ArmorType) currentItem.type, currentItem.set);
		}
		else if (currentItem.type instanceof L2WeaponType)
		{
			currentItem.item = new L2Weapon((L2WeaponType) currentItem.type, currentItem.set);
		}
		else if (currentItem.type instanceof L2EtcItemType)
		{
			currentItem.item = new L2EtcItem((L2EtcItemType) currentItem.type, currentItem.set);
		}
		else
		{
			throw new Error("Unknown item type " + currentItem.type);
		}
	}
	
	/**
	 * @return
	 */
	public List<L2Item> getItemList()
	{
		return itemsInFile;
	}
}
