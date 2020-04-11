package com.l2jfrozen.gameserver.skills;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.Item;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.templates.L2Armor;
import com.l2jfrozen.gameserver.templates.L2EtcItem;
import com.l2jfrozen.gameserver.templates.L2EtcItemType;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2Weapon;

/**
 * @author PrioGramMoS, L2JFrozen
 */
public class SkillsEngine
{
	
	protected static final Logger LOGGER = Logger.getLogger(SkillsEngine.class);
	
	private static final SkillsEngine instance = new SkillsEngine();
	
	private final List<File> armorFiles = new ArrayList<>();
	private final List<File> weaponFiles = new ArrayList<>();
	private final List<File> etcitemFiles = new ArrayList<>();
	private final List<File> skillFiles = new ArrayList<>();
	
	public static SkillsEngine getInstance()
	{
		return instance;
	}
	
	private SkillsEngine()
	{
		// hashFiles("data/xml/etcitem", etcitemFiles);
		hashFiles("data/xml/armor", armorFiles);
		hashFiles("data/xml/weapon", weaponFiles);
		hashFiles("data/xml/skills", skillFiles);
	}
	
	private void hashFiles(final String dirname, final List<File> hash)
	{
		final File dir = new File(Config.DATAPACK_ROOT, dirname);
		if (!dir.exists())
		{
			LOGGER.info("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		final File[] files = dir.listFiles();
		for (final File f : files)
		{
			if (f.getName().endsWith(".xml"))
			{
				if (!f.getName().startsWith("custom"))
				{
					hash.add(f);
				}
			}
		}
		final File customfile = new File(Config.DATAPACK_ROOT, dirname + "/custom.xml");
		if (customfile.exists())
		{
			hash.add(customfile);
		}
	}
	
	public List<L2Skill> loadSkills(final File file)
	{
		if (file == null)
		{
			LOGGER.warn("Skill file not found.");
			return null;
		}
		final DocumentSkill doc = new DocumentSkill(file);
		doc.parse();
		return doc.getSkills();
	}
	
	public void loadAllSkills(final Map<Integer, L2Skill> allSkills)
	{
		int count = 0;
		for (final File file : skillFiles)
		{
			final List<L2Skill> s = loadSkills(file);
			if (s == null)
			{
				continue;
			}
			for (final L2Skill skill : s)
			{
				allSkills.put(SkillTable.getSkillHashCode(skill), skill);
				count++;
			}
		}
		LOGGER.info("SkillsEngine: Loaded " + count + " Skill templates from XML files.");
	}
	
	public List<L2Armor> loadArmors(Map<Integer, Item> armorData)
	{
		List<L2Armor> list = new ArrayList<>();
		
		for (L2Item item : loadData(armorData, armorFiles))
		{
			list.add((L2Armor) item);
		}
		
		return list;
	}
	
	public List<L2Weapon> loadWeapons(Map<Integer, Item> weaponData)
	{
		List<L2Weapon> list = new ArrayList<>();
		
		for (final L2Item item : loadData(weaponData, weaponFiles))
		{
			list.add((L2Weapon) item);
		}
		
		return list;
	}
	
	public List<L2EtcItem> loadItems(Map<Integer, Item> itemData)
	{
		List<L2EtcItem> list = new ArrayList<>();
		
		for (L2Item item : loadData(itemData, etcitemFiles))
		{
			list.add((L2EtcItem) item);
		}
		
		if (list.size() == 0)
		{
			for (Item item : itemData.values())
			{
				list.add(new L2EtcItem((L2EtcItemType) item.type, item.set));
			}
		}
		
		return list;
	}
	
	public List<L2Item> loadData(final Map<Integer, Item> itemData, final List<File> files)
	{
		List<L2Item> list = new ArrayList<>();
		
		for (File f : files)
		{
			final DocumentItem document = new DocumentItem(itemData, f);
			document.parse();
			list.addAll(document.getItemList());
		}
		
		return list;
	}
}
