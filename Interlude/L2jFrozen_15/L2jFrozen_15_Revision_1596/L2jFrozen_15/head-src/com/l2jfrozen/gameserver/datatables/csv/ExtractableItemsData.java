package com.l2jfrozen.gameserver.datatables.csv;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2ExtractableItem;
import com.l2jfrozen.gameserver.model.L2ExtractableProductItem;

/**
 * @author FBIagent
 */
public class ExtractableItemsData
{
	private static Logger LOGGER = Logger.getLogger(ExtractableItemsData.class);
	
	// Map<itemid, L2ExtractableItem>
	private Map<Integer, L2ExtractableItem> items;
	
	private static ExtractableItemsData instance = null;
	
	public static ExtractableItemsData getInstance()
	{
		if (instance == null)
		{
			instance = new ExtractableItemsData();
		}
		
		return instance;
	}
	
	public ExtractableItemsData()
	{
		items = new HashMap<>();
		
		try (Scanner s = new Scanner(new File(Config.DATAPACK_ROOT + "/data/csv/extractable_items.csv"));)
		{
			int lineCount = 0;
			while (s.hasNextLine())
			{
				lineCount++;
				
				final String line = s.nextLine();
				
				if (line.startsWith("#"))
				{
					continue;
				}
				else if (line.equals(""))
				{
					continue;
				}
				
				final String[] lineSplit = line.split(";");
				int itemID = 0;
				
				try
				{
					itemID = Integer.parseInt(lineSplit[0]);
				}
				catch (Exception e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
					LOGGER.info("Extractable items data: Error in line " + lineCount + " -> invalid item id or wrong seperator after item id!");
					LOGGER.info("		" + line);
					return;
				}
				
				final List<L2ExtractableProductItem> product_temp = new ArrayList<>(lineSplit.length);
				for (int i = 0; i < lineSplit.length - 1; i++)
				{
					String[] lineSplit2 = lineSplit[i + 1].split(",");
					if (lineSplit2.length != 3)
					{
						LOGGER.info("Extractable items data: Error in line " + lineCount + " -> wrong seperator!");
						LOGGER.info("		" + line);
						continue;
					}
					
					int production = 0, amount = 0, chance = 0;
					
					try
					{
						production = Integer.parseInt(lineSplit2[0]);
						amount = Integer.parseInt(lineSplit2[1]);
						chance = Integer.parseInt(lineSplit2[2]);
						lineSplit2 = null;
					}
					catch (final Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e.printStackTrace();
						}
						
						LOGGER.info("Extractable items data: Error in line " + lineCount + " -> incomplete/invalid production data or wrong seperator!");
						LOGGER.info("		" + line);
						continue;
					}
					
					product_temp.add(new L2ExtractableProductItem(production, amount, chance));
				}
				
				int fullChances = 0;
				for (final L2ExtractableProductItem Pi : product_temp)
				{
					fullChances += Pi.getChance();
				}
				
				if (fullChances > 100)
				{
					LOGGER.info("Extractable items data: Error in line " + lineCount + " -> all chances together are more then 100!");
					LOGGER.info("		" + line);
					continue;
				}
				
				items.put(itemID, new L2ExtractableItem(itemID, product_temp));
			}
			
			LOGGER.info("Extractable items data: Loaded " + items.size() + " extractable items!");
		}
		catch (Exception e)
		{
			LOGGER.error("ExtractableItemsData.ExtractableItemsData : Can not find data in gameserver/data/csv/extractable_items.csv", e);
		}
	}
	
	public L2ExtractableItem getExtractableItem(final int itemID)
	{
		return items.get(itemID);
	}
	
	public int[] itemIDs()
	{
		final int size = items.size();
		final int[] result = new int[size];
		int i = 0;
		for (final L2ExtractableItem ei : items.values())
		{
			result[i] = ei.getItemId();
			i++;
		}
		return result;
	}
}
