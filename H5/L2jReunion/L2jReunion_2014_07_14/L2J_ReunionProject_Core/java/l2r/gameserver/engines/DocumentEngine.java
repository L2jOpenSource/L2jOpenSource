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
package l2r.gameserver.engines;

import java.io.File;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import l2r.Config;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.engines.items.DocumentItem;
import l2r.gameserver.engines.skills.DocumentSkill;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.skills.L2Skill;
import l2r.util.file.filter.XMLFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mkizub
 */
public class DocumentEngine
{
	private static final Logger _log = LoggerFactory.getLogger(DocumentEngine.class);
	
	private final List<File> _itemFiles = new FastList<>();
	private final List<File> _skillFiles = new FastList<>();
	
	public static DocumentEngine getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected DocumentEngine()
	{
		hashFiles("data/xml/stats/items", _itemFiles);
		if (Config.CUSTOM_ITEMS_LOAD)
		{
			hashFiles("data/xml/stats/items/custom", _itemFiles);
		}
		hashFiles("data/xml/stats/skills", _skillFiles);
		if (Config.CUSTOM_SKILLS_LOAD)
		{
			hashFiles("data/xml/stats/skills/custom", _skillFiles);
		}
	}
	
	private void hashFiles(String dirname, List<File> hash)
	{
		File dir = new File(Config.DATAPACK_ROOT, dirname);
		if (!dir.exists())
		{
			_log.warn("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		File[] files = dir.listFiles(new XMLFilter());
		for (File f : files)
		{
			hash.add(f);
		}
	}
	
	public List<L2Skill> loadSkills(File file)
	{
		if (file == null)
		{
			_log.warn("Skill file not found.");
			return null;
		}
		DocumentSkill doc = new DocumentSkill(file);
		doc.parse();
		return doc.getSkills();
	}
	
	public void loadAllSkills(final Map<Integer, L2Skill> allSkills)
	{
		int count = 0;
		for (File file : _skillFiles)
		{
			List<L2Skill> s = loadSkills(file);
			if (s == null)
			{
				continue;
			}
			for (L2Skill skill : s)
			{
				allSkills.put(SkillData.getSkillHashCode(skill), skill);
				count++;
			}
		}
		_log.info(getClass().getSimpleName() + ": Loaded " + count + " Skill templates from XML files.");
	}
	
	/**
	 * Return created items
	 * @return List of {@link L2Item}
	 */
	public List<L2Item> loadItems()
	{
		List<L2Item> list = new FastList<>();
		for (File f : _itemFiles)
		{
			DocumentItem document = new DocumentItem(f);
			document.parse();
			list.addAll(document.getItemList());
		}
		return list;
	}
	
	private static class SingletonHolder
	{
		protected static final DocumentEngine _instance = new DocumentEngine();
	}
}
