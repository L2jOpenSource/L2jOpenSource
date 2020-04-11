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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2r.gameserver.engines.DocumentParser;
import l2r.gameserver.model.L2Seed;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.items.L2Item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Service class for manor
 * @author l3x, UnAfraid
 */
public class ManorData extends DocumentParser
{
	private static Logger _log = LoggerFactory.getLogger(ManorData.class);
	
	private static Map<Integer, L2Seed> _seeds = new HashMap<>();
	
	protected ManorData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_seeds.clear();
		parseDatapackFile("data/xml/seeds.xml");
		_log.info(getClass().getSimpleName() + ": Loaded: " + _seeds.size() + " Seeds");
	}
	
	@Override
	protected void parseDocument()
	{
		StatsSet set;
		NamedNodeMap attrs;
		Node att;
		int castleId;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("castle".equalsIgnoreCase(d.getNodeName()))
					{
						castleId = parseInteger(d.getAttributes(), "id");
						for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
						{
							if ("crop".equalsIgnoreCase(c.getNodeName()))
							{
								set = new StatsSet();
								set.set("castleId", castleId);
								
								attrs = c.getAttributes();
								for (int i = 0; i < attrs.getLength(); i++)
								{
									att = attrs.item(i);
									set.set(att.getNodeName(), att.getNodeValue());
								}
								
								L2Seed seed = new L2Seed(set);
								_seeds.put(seed.getSeedId(), seed);
							}
						}
					}
				}
			}
		}
	}
	
	public List<Integer> getAllCrops()
	{
		List<Integer> crops = new ArrayList<>();
		
		for (L2Seed seed : _seeds.values())
		{
			if (!crops.contains(seed.getCropId()) && (seed.getCropId() != 0) && !crops.contains(seed.getCropId()))
			{
				crops.add(seed.getCropId());
			}
		}
		
		return crops;
	}
	
	public int getSeedBasicPrice(int seedId)
	{
		final L2Item seedItem = ItemData.getInstance().getTemplate(seedId);
		if (seedItem != null)
		{
			return seedItem.getReferencePrice();
		}
		return 0;
	}
	
	public int getSeedBasicPriceByCrop(int cropId)
	{
		for (L2Seed seed : _seeds.values())
		{
			if (seed.getCropId() == cropId)
			{
				return getSeedBasicPrice(seed.getSeedId());
			}
		}
		return 0;
	}
	
	public int getCropBasicPrice(int cropId)
	{
		final L2Item cropItem = ItemData.getInstance().getTemplate(cropId);
		if (cropItem != null)
		{
			return cropItem.getReferencePrice();
		}
		return 0;
	}
	
	public int getMatureCrop(int cropId)
	{
		for (L2Seed seed : _seeds.values())
		{
			if (seed.getCropId() == cropId)
			{
				return seed.getMatureId();
			}
		}
		return 0;
	}
	
	/**
	 * Returns price which lord pays to buy one seed
	 * @param seedId
	 * @return seed price
	 */
	public long getSeedBuyPrice(int seedId)
	{
		long buyPrice = getSeedBasicPrice(seedId);
		return (buyPrice > 0 ? buyPrice : 1);
	}
	
	public int getSeedMinLevel(int seedId)
	{
		L2Seed seed = _seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getLevel() - 5;
		}
		return -1;
	}
	
	public int getSeedMaxLevel(int seedId)
	{
		L2Seed seed = _seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getLevel() + 5;
		}
		return -1;
	}
	
	public int getSeedLevelByCrop(int cropId)
	{
		for (L2Seed seed : _seeds.values())
		{
			if (seed.getCropId() == cropId)
			{
				return seed.getLevel();
			}
		}
		return 0;
	}
	
	public int getSeedLevel(int seedId)
	{
		L2Seed seed = _seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getLevel();
		}
		return -1;
	}
	
	public boolean isAlternative(int seedId)
	{
		L2Seed seed = _seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.isAlternative();
		}
		return false;
	}
	
	public int getCropType(int seedId)
	{
		L2Seed seed = _seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getCropId();
		}
		return -1;
	}
	
	public int getRewardItem(int cropId, int type)
	{
		for (L2Seed seed : _seeds.values())
		{
			if (seed.getCropId() == cropId)
			{
				return seed.getReward(type); // there can be several seeds with same crop, but reward should be the same for all.
			}
		}
		return -1;
	}
	
	public int getRewardItemBySeed(int seedId, int type)
	{
		L2Seed seed = _seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getReward(type);
		}
		return 0;
	}
	
	/**
	 * Return all crops which can be purchased by given castle
	 * @param castleId
	 * @return
	 */
	public List<Integer> getCropsForCastle(int castleId)
	{
		List<Integer> crops = new ArrayList<>();
		
		for (L2Seed seed : _seeds.values())
		{
			if ((seed.getCastleId() == castleId) && !crops.contains(seed.getCropId()))
			{
				crops.add(seed.getCropId());
			}
		}
		
		return crops;
	}
	
	/**
	 * Return list of seed ids, which belongs to castle with given id
	 * @param castleId - id of the castle
	 * @return seedIds - list of seed ids
	 */
	public List<Integer> getSeedsForCastle(int castleId)
	{
		List<Integer> seedsID = new ArrayList<>();
		
		for (L2Seed seed : _seeds.values())
		{
			if ((seed.getCastleId() == castleId) && !seedsID.contains(seed.getSeedId()))
			{
				seedsID.add(seed.getSeedId());
			}
		}
		
		return seedsID;
	}
	
	/**
	 * Returns castle id where seed can be sowned<br>
	 * @param seedId
	 * @return castleId
	 */
	public int getCastleIdForSeed(int seedId)
	{
		L2Seed seed = _seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getCastleId();
		}
		return 0;
	}
	
	public int getSeedSaleLimit(int seedId)
	{
		L2Seed seed = _seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getSeedLimit();
		}
		return 0;
	}
	
	public int getCropPuchaseLimit(int cropId)
	{
		for (L2Seed seed : _seeds.values())
		{
			if (seed.getCropId() == cropId)
			{
				return seed.getCropLimit();
			}
		}
		return 0;
	}
	
	public static ManorData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ManorData _instance = new ManorData();
	}
}
