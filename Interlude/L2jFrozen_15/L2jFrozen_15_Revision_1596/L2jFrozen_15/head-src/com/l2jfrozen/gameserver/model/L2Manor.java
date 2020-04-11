package com.l2jfrozen.gameserver.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.templates.L2Item;

/**
 * Service class for manor
 * @author l3x
 */
public class L2Manor
{
	private static Logger LOGGER = Logger.getLogger(L2Manor.class);
	private static L2Manor instance;
	
	private static Map<Integer, SeedData> seeds;
	
	public L2Manor()
	{
		seeds = new ConcurrentHashMap<>();
		parseData();
	}
	
	public static L2Manor getInstance()
	{
		if (instance == null)
		{
			instance = new L2Manor();
		}
		
		return instance;
	}
	
	public List<Integer> getAllCrops()
	{
		List<Integer> crops = new ArrayList<>();
		
		for (SeedData seed : seeds.values())
		{
			if (!crops.contains(seed.getCrop()) && seed.getCrop() != 0 && !crops.contains(seed.getCrop()))
			{
				crops.add(seed.getCrop());
			}
		}
		
		return crops;
	}
	
	public int getSeedBasicPrice(final int seedId)
	{
		final L2Item seedItem = ItemTable.getInstance().getTemplate(seedId);
		
		if (seedItem != null)
		{
			return seedItem.getReferencePrice();
		}
		return 0;
	}
	
	public int getSeedBasicPriceByCrop(final int cropId)
	{
		for (final SeedData seed : seeds.values())
		{
			if (seed.getCrop() == cropId)
			{
				return getSeedBasicPrice(seed.getId());
			}
		}
		
		return 0;
	}
	
	public int getCropBasicPrice(final int cropId)
	{
		final L2Item cropItem = ItemTable.getInstance().getTemplate(cropId);
		
		if (cropItem != null)
		{
			return cropItem.getReferencePrice();
		}
		return 0;
	}
	
	public int getMatureCrop(final int cropId)
	{
		for (final SeedData seed : seeds.values())
		{
			if (seed.getCrop() == cropId)
			{
				return seed.getMature();
			}
		}
		return 0;
	}
	
	/**
	 * Returns price which lord pays to buy one seed
	 * @param  seedId
	 * @return        seed price
	 */
	public int getSeedBuyPrice(final int seedId)
	{
		final int buyPrice = getSeedBasicPrice(seedId) / 10;
		
		return buyPrice > 0 ? buyPrice : 1;
	}
	
	public int getSeedMinLevel(final int seedId)
	{
		final SeedData seed = seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getLevel() - 5;
		}
		return -1;
	}
	
	public int getSeedMaxLevel(final int seedId)
	{
		final SeedData seed = seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getLevel() + 5;
		}
		return -1;
	}
	
	public int getSeedLevelByCrop(final int cropId)
	{
		for (final SeedData seed : seeds.values())
		{
			if (seed.getCrop() == cropId)
			{
				return seed.getLevel();
			}
		}
		return 0;
	}
	
	public int getSeedLevel(final int seedId)
	{
		final SeedData seed = seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getLevel();
		}
		return -1;
	}
	
	public boolean isAlternative(final int seedId)
	{
		for (final SeedData seed : seeds.values())
		{
			if (seed.getId() == seedId)
			{
				return seed.isAlternative();
			}
		}
		return false;
	}
	
	public int getCropType(final int seedId)
	{
		final SeedData seed = seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getCrop();
		}
		return -1;
	}
	
	public synchronized int getRewardItem(final int cropId, final int type)
	{
		for (final SeedData seed : seeds.values())
		{
			if (seed.getCrop() == cropId)
			{
				return seed.getReward(type);
				// there can be several
				// seeds with same crop, but
				// reward should be the same for
				// all
			}
		}
		return -1;
	}
	
	public synchronized int getRewardItemBySeed(final int seedId, final int type)
	{
		final SeedData seed = seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getReward(type);
		}
		return 0;
	}
	
	/**
	 * Return all crops which can be purchased by given castle
	 * @param  castleId
	 * @return
	 */
	public List<Integer> getCropsForCastle(int castleId)
	{
		List<Integer> crops = new ArrayList<>();
		
		for (SeedData seed : seeds.values())
		{
			if (seed.getManorId() == castleId && !crops.contains(seed.getCrop()))
			{
				crops.add(seed.getCrop());
			}
		}
		
		return crops;
	}
	
	/**
	 * Return list of seed ids, which belongs to castle with given id
	 * @param  castleId - id of the castle
	 * @return          seedIds - list of seed ids
	 */
	public List<Integer> getSeedsForCastle(final int castleId)
	{
		List<Integer> seedsID = new ArrayList<>();
		
		for (final SeedData seed : seeds.values())
		{
			if (seed.getManorId() == castleId && !seedsID.contains(seed.getId()))
			{
				seedsID.add(seed.getId());
			}
		}
		
		return seedsID;
	}
	
	/**
	 * Returns castle id where seed can be sowned<br>
	 * @param  seedId
	 * @return        castleId
	 */
	public int getCastleIdForSeed(final int seedId)
	{
		final SeedData seed = seeds.get(seedId);
		if (seed != null)
		{
			return seed.getManorId();
		}
		return 0;
	}
	
	public int getSeedSaleLimit(final int seedId)
	{
		final SeedData seed = seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getSeedLimit();
		}
		return 0;
	}
	
	public int getCropPuchaseLimit(final int cropId)
	{
		for (final SeedData seed : seeds.values())
		{
			if (seed.getCrop() == cropId)
			{
				return seed.getCropLimit();
			}
		}
		return 0;
	}
	
	private class SeedData
	{
		private int id;
		private final int level; // seed level
		private final int crop; // crop type
		private final int mature; // mature crop type
		private int type1;
		private int type2;
		private int manorId; // id of manor (castle id) where seed can be farmed
		private int isAlternative;
		private int limitSeeds;
		private int limitCrops;
		
		public SeedData(final int level, final int crop, final int mature)
		{
			this.level = level;
			this.crop = crop;
			this.mature = mature;
		}
		
		public void setData(final int id, final int t1, final int t2, final int manorId, final int isAlt, final int lim1, final int lim2)
		{
			this.id = id;
			type1 = t1;
			type2 = t2;
			this.manorId = manorId;
			isAlternative = isAlt;
			limitSeeds = lim1;
			limitCrops = lim2;
		}
		
		public int getManorId()
		{
			return manorId;
		}
		
		public int getId()
		{
			return id;
		}
		
		public int getCrop()
		{
			return crop;
		}
		
		public int getMature()
		{
			return mature;
		}
		
		public int getReward(final int type)
		{
			return type == 1 ? type1 : type2;
		}
		
		public int getLevel()
		{
			return level;
		}
		
		public boolean isAlternative()
		{
			return isAlternative == 1;
		}
		
		public int getSeedLimit()
		{
			return limitSeeds * Config.RATE_DROP_MANOR;
		}
		
		public int getCropLimit()
		{
			return limitCrops * Config.RATE_DROP_MANOR;
		}
	}
	
	private void parseData()
	{
		File seedData = new File(Config.DATAPACK_ROOT, "data/csv/seeds.csv");
		
		try (FileReader reader = new FileReader(seedData);
			BufferedReader buff = new BufferedReader(reader);
			LineNumberReader lnr = new LineNumberReader(buff))
		{
			String line = null;
			
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().length() == 0 || line.startsWith("#"))
				{
					continue;
				}
				
				SeedData seed = parseList(line);
				seeds.put(seed.getId(), seed);
				seed = null;
			}
			
			LOGGER.info("ManorManager: Loaded " + seeds.size() + " seeds");
		}
		catch (FileNotFoundException e)
		{
			LOGGER.error("L2Manor.parseData : seeds.csv file is missing in gameserver/data/csv folder");
		}
		catch (Exception e)
		{
			LOGGER.error("L2Manor.parseData : Error while loading seeds. ", e);
		}
	}
	
	private SeedData parseList(final String line)
	{
		StringTokenizer st = new StringTokenizer(line, ";");
		
		final int seedId = Integer.parseInt(st.nextToken()); // seed id
		final int level = Integer.parseInt(st.nextToken()); // seed level
		final int cropId = Integer.parseInt(st.nextToken()); // crop id
		final int matureId = Integer.parseInt(st.nextToken()); // mature crop id
		final int type1R = Integer.parseInt(st.nextToken()); // type I reward
		final int type2R = Integer.parseInt(st.nextToken()); // type II reward
		final int manorId = Integer.parseInt(st.nextToken()); // id of manor, where seed can be farmed
		final int isAlt = Integer.parseInt(st.nextToken()); // alternative seed
		final int limitSeeds = Integer.parseInt(st.nextToken()); // limit for seeds
		final int limitCrops = Integer.parseInt(st.nextToken()); // limit for crops
		
		final SeedData seed = new SeedData(level, cropId, matureId);
		seed.setData(seedId, type1R, type2R, manorId, isAlt, limitSeeds, limitCrops);
		
		st = null;
		
		return seed;
	}
}
