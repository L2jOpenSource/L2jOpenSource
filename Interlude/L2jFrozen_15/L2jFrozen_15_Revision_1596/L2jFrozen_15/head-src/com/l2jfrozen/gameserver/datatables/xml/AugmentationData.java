package com.l2jfrozen.gameserver.datatables.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Augmentation;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.util.random.Rnd;

/**
 * This class manages the augmentation data and can also create new augmentations.
 * @author programmos & Incognito, sword dev
 */
public class AugmentationData
{
	private static final Logger LOGGER = Logger.getLogger(AugmentationData.class);
	
	private static AugmentationData instance;
	
	public static final AugmentationData getInstance()
	{
		if (instance == null)
		{
			instance = new AugmentationData();
		}
		
		return instance;
	}
	
	private static final int STAT_START = 1;
	private static final int STAT_END = 14560;
	private static final int STAT_BLOCKSIZE = 3640;
	private static final int STAT_SUBBLOCKSIZE = 91;
	
	// skills
	private static final int BLUE_START = 14561;
	private static final int SKILLS_BLOCKSIZE = 178;
	
	// basestats
	private static final int BASESTAT_STR = 16341;
	private static final int BASESTAT_CON = 16342;
	private static final int BASESTAT_INT = 16343;
	private static final int BASESTAT_MEN = 16344;
	
	private final List<AugmentationStat> augmentationStats[];
	private final Map<Integer, List<AugmentationSkill>> blueSkills;
	private final Map<Integer, List<AugmentationSkill>> purpleSkills;
	private final Map<Integer, List<AugmentationSkill>> redSkills;
	
	@SuppressWarnings("unchecked")
	private AugmentationData()
	{
		LOGGER.info("Initializing AugmentationData.");
		
		augmentationStats = new ArrayList[4];
		augmentationStats[0] = new ArrayList<>();
		augmentationStats[1] = new ArrayList<>();
		augmentationStats[2] = new ArrayList<>();
		augmentationStats[3] = new ArrayList<>();
		
		blueSkills = new HashMap<>();
		purpleSkills = new HashMap<>();
		redSkills = new HashMap<>();
		for (int i = 1; i <= 10; i++)
		{
			blueSkills.put(i, new ArrayList<AugmentationSkill>());
			purpleSkills.put(i, new ArrayList<AugmentationSkill>());
			redSkills.put(i, new ArrayList<AugmentationSkill>());
		}
		
		load();
		
		// Use size*4: since theres 4 blocks of stat-data with equivalent size
		LOGGER.info("AugmentationData: Loaded: " + augmentationStats[0].size() * 4 + " augmentation stats.");
		
		if (Config.DEBUG)
		{
			for (int i = 1; i <= 10; i++)
			{
				LOGGER.info("AugmentationData: Loaded: " + blueSkills.get(i).size() + " blue, " + purpleSkills.get(i).size() + " purple and " + redSkills.get(i).size() + " red skills for lifeStoneLevel " + i);
			}
		}
		
	}
	
	public static void reload()
	{
		instance = null;
		getInstance();
	}
	
	public class AugmentationSkill
	{
		private final int skillId;
		private final int maxSkillLevel;
		private final int augmentationSkillId;
		
		public AugmentationSkill(final int skillId, final int maxSkillLevel, final int augmentationSkillId)
		{
			this.skillId = skillId;
			this.maxSkillLevel = maxSkillLevel;
			this.augmentationSkillId = augmentationSkillId;
		}
		
		public L2Skill getSkill(final int level)
		{
			if (level > maxSkillLevel)
			{
				return SkillTable.getInstance().getInfo(skillId, maxSkillLevel);
			}
			
			return SkillTable.getInstance().getInfo(skillId, level);
		}
		
		public int getAugmentationSkillId()
		{
			return augmentationSkillId;
		}
	}
	
	public class AugmentationStat
	{
		private final Stats stat;
		private final int singleSize;
		private final int combinedSize;
		private final float singleValues[];
		private final float combinedValues[];
		
		public AugmentationStat(final Stats stat, final float sValues[], final float cValues[])
		{
			this.stat = stat;
			singleSize = sValues.length;
			singleValues = sValues;
			combinedSize = cValues.length;
			combinedValues = cValues;
		}
		
		public int getSingleStatSize()
		{
			return singleSize;
		}
		
		public int getCombinedStatSize()
		{
			return combinedSize;
		}
		
		public float getSingleStatValue(final int i)
		{
			if (i >= singleSize || i < 0)
			{
				return singleValues[singleSize - 1];
			}
			
			return singleValues[i];
		}
		
		public float getCombinedStatValue(final int i)
		{
			if (i >= combinedSize || i < 0)
			{
				return combinedValues[combinedSize - 1];
			}
			
			return combinedValues[i];
		}
		
		public Stats getStat()
		{
			return stat;
		}
	}
	
	private final void load()
	{
		// Load the skillmap
		// Note: the skillmap data is only used when generating new augmentations
		// the client expects a different id in order to display the skill in the
		// items description...
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			int badAugmantData = 0;
			
			File file = new File(Config.DATAPACK_ROOT + "/data/xml/augmentation/augmentation_skillmap.xml");
			if (!file.exists())
			{
				if (Config.DEBUG)
				{
					LOGGER.info("The augmentation skillmap file is missing.");
				}
				return;
			}
			
			Document doc = factory.newDocumentBuilder().parse(file);
			
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("augmentation".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							int skillId = 0;
							final int augmentationId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							String type = "blue";
							int skillLvL = 0;
							
							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if ("skillId".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									skillId = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
								}
								else if ("skillLevel".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									skillLvL = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
								}
								else if ("type".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									type = attrs.getNamedItem("val").getNodeValue();
								}
							}
							
							if (skillId == 0)
							{
								if (Config.DEBUG)
								{
									LOGGER.error("Bad skillId in augmentation_skillmap.xml in the augmentationId:" + augmentationId);
								}
								badAugmantData++;
								continue;
							}
							else if (skillLvL == 0)
							{
								if (Config.DEBUG)
								{
									LOGGER.error("Bad skillLevel in augmentation_skillmap.xml in the augmentationId:" + augmentationId);
								}
								badAugmantData++;
								continue;
							}
							
							int k = 1;
							while (augmentationId - k * SKILLS_BLOCKSIZE >= BLUE_START)
							{
								k++;
							}
							
							if (type.equalsIgnoreCase("blue"))
							{
								blueSkills.get(k).add(new AugmentationSkill(skillId, skillLvL, augmentationId));
							}
							else if (type.equalsIgnoreCase("purple"))
							{
								purpleSkills.get(k).add(new AugmentationSkill(skillId, skillLvL, augmentationId));
							}
							else
							{
								redSkills.get(k).add(new AugmentationSkill(skillId, skillLvL, augmentationId));
							}
							
							attrs = null;
						}
					}
				}
			}
			
			if (badAugmantData != 0)
			{
				LOGGER.info("AugmentationData: " + badAugmantData + " bad skill(s) were skipped.");
			}
			
			doc = null;
			file = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.error("Error parsing augmentation_skillmap.xml.", e);
			
			return;
		}
		
		// Load the stats from xml
		for (int i = 1; i < 5; i++)
		{
			try
			{
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(false);
				factory.setIgnoringComments(true);
				
				File file = new File(Config.DATAPACK_ROOT + "/data/xml/augmentation/augmentation_stats" + i + ".xml");
				
				if (!file.exists())
				{
					if (Config.DEBUG)
					{
						LOGGER.info("The augmentation stat data file " + i + " is missing.");
					}
					
					return;
				}
				
				Document doc = factory.newDocumentBuilder().parse(file);
				
				for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if ("list".equalsIgnoreCase(n.getNodeName()))
					{
						for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							if ("stat".equalsIgnoreCase(d.getNodeName()))
							{
								NamedNodeMap attrs = d.getAttributes();
								String statName = attrs.getNamedItem("name").getNodeValue();
								
								float soloValues[] = null, combinedValues[] = null;
								
								for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
								{
									if ("table".equalsIgnoreCase(cd.getNodeName()))
									{
										attrs = cd.getAttributes();
										String tableName = attrs.getNamedItem("name").getNodeValue();
										
										final StringTokenizer data = new StringTokenizer(cd.getFirstChild().getNodeValue());
										final List<Float> array = new ArrayList<>();
										
										while (data.hasMoreTokens())
										{
											array.add(Float.parseFloat(data.nextToken()));
										}
										
										if (tableName.equalsIgnoreCase("#soloValues"))
										{
											soloValues = new float[array.size()];
											int x = 0;
											
											for (final float value : array)
											{
												soloValues[x++] = value;
											}
										}
										else
										{
											combinedValues = new float[array.size()];
											int x = 0;
											
											for (final float value : array)
											{
												combinedValues[x++] = value;
											}
										}
										
										tableName = null;
									}
								}
								
								// store this stat
								augmentationStats[(i - 1)].add(new AugmentationStat(Stats.valueOfXml(statName), soloValues, combinedValues));
								
								statName = null;
								attrs = null;
							}
						}
					}
				}
				
				doc = null;
				file = null;
				factory = null;
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				LOGGER.error("Error parsing augmentation_stats" + i + ".xml.", e);
				return;
			}
		}
	}
	
	/**
	 * Generate a new random augmentation
	 * @param  item
	 * @param  lifeStoneLevel
	 * @param  lifeStoneGrade
	 * @return                L2Augmentation
	 */
	public L2Augmentation generateRandomAugmentation(final L2ItemInstance item, final int lifeStoneLevel, final int lifeStoneGrade)
	{
		// Note that stat12 stands for stat 1 AND 2 (same for stat34 ;p )
		// this is because a value can contain up to 2 stat modifications
		// (there are two short values packed in one integer value, meaning 4 stat modifications at max)
		// for more info take a look at getAugStatsById(...)
		
		// Note: lifeStoneGrade: (0 means low grade, 3 top grade)
		// First: determine whether we will add a skill/baseStatModifier or not because this determine which color could be the result
		int skill_Chance = 0;
		int stat34 = 0;
		boolean generateSkill = false;
		int resultColor = 0;
		boolean generateGlow = false;
		
		switch (lifeStoneGrade)
		{
			case 0:
				skill_Chance = Config.AUGMENTATION_NG_SKILL_CHANCE;
				
				if (Rnd.get(1, 100) <= Config.AUGMENTATION_NG_GLOW_CHANCE)
				{
					generateGlow = true;
				}
				break;
			
			case 1:
				skill_Chance = Config.AUGMENTATION_MID_SKILL_CHANCE;
				
				if (Rnd.get(1, 100) <= Config.AUGMENTATION_MID_GLOW_CHANCE)
				{
					generateGlow = true;
				}
				break;
			
			case 2:
				skill_Chance = Config.AUGMENTATION_HIGH_SKILL_CHANCE;
				
				if (Rnd.get(1, 100) <= Config.AUGMENTATION_HIGH_GLOW_CHANCE)
				{
					generateGlow = true;
				}
				break;
			
			case 3:
				skill_Chance = Config.AUGMENTATION_TOP_SKILL_CHANCE;
				
				if (Rnd.get(1, 100) <= Config.AUGMENTATION_TOP_GLOW_CHANCE)
				{
					generateGlow = true;
				}
		}
		
		if (Rnd.get(1, 100) <= skill_Chance)
		{
			generateSkill = true;
		}
		else if (Rnd.get(1, 100) <= Config.AUGMENTATION_BASESTAT_CHANCE)
		{
			stat34 = Rnd.get(BASESTAT_STR, BASESTAT_MEN);
		}
		
		// Second: decide which grade the augmentation result is going to have:
		// 0:yellow, 1:blue, 2:purple, 3:red
		// The chances used here are most likely custom,
		// whats known is: u can also get a red result from a normal grade lifeStone
		// however I will make it so that a higher grade lifeStone will more likely result in a
		// higher grade augmentation... and the augmentation result will at least have the grade
		// of the life stone
		// Second: Calculate the subblock offset for the choosen color,
		// and the level of the lifeStone
		// whats known is: you cant have yellow with skill(or baseStatModifier)
		// noGrade stone can not have glow, mid only with skill, high has a chance(custom), top allways glow
		if (stat34 == 0 && !generateSkill)
		{
			resultColor = Rnd.get(0, 100);
			
			if (resultColor <= 15 * lifeStoneGrade + 40)
			{
				resultColor = 1;
			}
			else
			{
				resultColor = 0;
			}
		}
		else
		{
			resultColor = Rnd.get(0, 100);
			
			if (resultColor <= 10 * lifeStoneGrade + 5 || stat34 != 0)
			{
				resultColor = 3;
			}
			else if (resultColor <= 10 * lifeStoneGrade + 10)
			{
				resultColor = 1;
			}
			else
			{
				resultColor = 2;
			}
		}
		
		// is neither a skill nor basestat used for stat34? then generate a normal stat
		int stat12 = 0;
		
		if (stat34 == 0 && !generateSkill)
		{
			final int temp = Rnd.get(2, 3);
			final int colorOffset = resultColor * 10 * STAT_SUBBLOCKSIZE + temp * STAT_BLOCKSIZE + 1;
			int offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + colorOffset;
			
			stat34 = Rnd.get(offset, offset + STAT_SUBBLOCKSIZE - 1);
			
			if (generateGlow && lifeStoneGrade >= 2)
			{
				offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + (temp - 2) * STAT_BLOCKSIZE + lifeStoneGrade * 10 * STAT_SUBBLOCKSIZE + 1;
			}
			else
			{
				offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + (temp - 2) * STAT_BLOCKSIZE + Rnd.get(0, 1) * 10 * STAT_SUBBLOCKSIZE + 1;
			}
			stat12 = Rnd.get(offset, offset + STAT_SUBBLOCKSIZE - 1);
		}
		else
		{
			int offset;
			
			if (!generateGlow)
			{
				offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + Rnd.get(0, 1) * STAT_BLOCKSIZE + 1;
			}
			else
			{
				offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + Rnd.get(0, 1) * STAT_BLOCKSIZE + (lifeStoneGrade + resultColor) / 2 * 10 * STAT_SUBBLOCKSIZE + 1;
			}
			stat12 = Rnd.get(offset, offset + STAT_SUBBLOCKSIZE - 1);
		}
		
		// generate a skill if neccessary
		L2Skill skill = null;
		if (generateSkill)
		{
			AugmentationSkill temp = null;
			switch (resultColor)
			{
				case 1: // blue skill
					temp = blueSkills.get(lifeStoneLevel).get(Rnd.get(0, blueSkills.get(lifeStoneLevel).size() - 1));
					skill = temp.getSkill(lifeStoneLevel);
					stat34 = temp.getAugmentationSkillId();
					break;
				case 2: // purple skill
					temp = purpleSkills.get(lifeStoneLevel).get(Rnd.get(0, purpleSkills.get(lifeStoneLevel).size() - 1));
					skill = temp.getSkill(lifeStoneLevel);
					stat34 = temp.getAugmentationSkillId();
					break;
				case 3: // red skill
					temp = redSkills.get(lifeStoneLevel).get(Rnd.get(0, redSkills.get(lifeStoneLevel).size() - 1));
					skill = temp.getSkill(lifeStoneLevel);
					stat34 = temp.getAugmentationSkillId();
					break;
			}
		}
		if (Config.DEBUG)
		{
			LOGGER.info("Augmentation success: stat12=" + stat12 + "; stat34=" + stat34 + "; resultColor=" + resultColor + "; level=" + lifeStoneLevel + "; grade=" + lifeStoneGrade);
		}
		
		return new L2Augmentation(item, ((stat34 << 16) + stat12), skill, true);
	}
	
	public class AugStat
	{
		private final Stats stat;
		private final float value;
		
		public AugStat(final Stats stat, final float value)
		{
			this.stat = stat;
			this.value = value;
		}
		
		public Stats getStat()
		{
			return stat;
		}
		
		public float getValue()
		{
			return value;
		}
	}
	
	/**
	 * Returns the stat and basestat boni for a given augmentation id
	 * @param  augmentationId
	 * @return
	 */
	public List<AugStat> getAugStatsById(final int augmentationId)
	{
		final List<AugStat> temp = new ArrayList<>();
		// An augmentation id contains 2 short vaues so we gotta seperate them here
		// both values contain a number from 1-16380, the first 14560 values are stats
		// the 14560 stats are devided into 4 blocks each holding 3640 values
		// each block contains 40 subblocks holding 91 stat values
		// the first 13 values are so called Solo-stats and they have the highest stat increase possible
		// after the 13 Solo-stats come 78 combined stats (thats every possible combination of the 13 solo stats)
		// the first 12 combined stats (14-26) is the stat 1 combined with stat 2-13
		// the next 11 combined stats then are stat 2 combined with stat 3-13 and so on...
		// to get the idea have a look @ optiondata_client-e.dat - thats where the data came from :)
		final int stats[] = new int[2];
		stats[0] = 0x0000FFFF & augmentationId;
		stats[1] = augmentationId >> 16;
		
		for (int i = 0; i < 2; i++)
		{
			// its a stat
			if (stats[i] >= STAT_START && stats[i] <= STAT_END)
			{
				int block = 0;
				
				while (stats[i] > STAT_BLOCKSIZE)
				{
					stats[i] -= STAT_BLOCKSIZE;
					block++;
				}
				
				int subblock = 0;
				
				while (stats[i] > STAT_SUBBLOCKSIZE)
				{
					stats[i] -= STAT_SUBBLOCKSIZE;
					subblock++;
				}
				
				if (stats[i] < 14) // solo stat
				{
					final AugmentationStat as = augmentationStats[block].get((stats[i] - 1));
					temp.add(new AugStat(as.getStat(), as.getSingleStatValue(subblock)));
				}
				else
				// twin stat
				{
					stats[i] -= 13; // rescale to 0 (if first of first combined block)
					
					int x = 12; // next combi block has 12 stats
					int rescales = 0; // number of rescales done
					
					while (stats[i] > x)
					{
						stats[i] -= x;
						x--;
						rescales++;
					}
					
					// get first stat
					AugmentationStat as = augmentationStats[block].get(rescales);
					if (rescales == 0)
					{
						temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock)));
					}
					else
					{
						temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock * 2 + 1)));
					}
					
					// get 2nd stat
					as = augmentationStats[block].get(rescales + stats[i]);
					if (as.getStat() == Stats.CRITICAL_DAMAGE)
					{
						temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock)));
					}
					else
					{
						temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock * 2)));
					}
					
					as = null;
				}
			}
			// its a base stat
			else if (stats[i] >= BASESTAT_STR && stats[i] <= BASESTAT_MEN)
			{
				switch (stats[i])
				{
					case BASESTAT_STR:
						temp.add(new AugStat(Stats.STAT_STR, 1.0f));
						break;
					case BASESTAT_CON:
						temp.add(new AugStat(Stats.STAT_CON, 1.0f));
						break;
					case BASESTAT_INT:
						temp.add(new AugStat(Stats.STAT_INT, 1.0f));
						break;
					case BASESTAT_MEN:
						temp.add(new AugStat(Stats.STAT_MEN, 1.0f));
						break;
				}
			}
		}
		
		return temp;
	}
}
