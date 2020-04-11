/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.gameserver.data.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.model.actor.L2Summon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SummonSkillsTable
{
	private static Logger _log = LoggerFactory.getLogger(SummonSkillsTable.class);
	private final Map<Integer, Map<Integer, L2PetSkillLearn>> _skillTrees = new HashMap<>();
	
	protected SummonSkillsTable()
	{
		load();
	}
	
	public void load()
	{
		_skillTrees.clear();
		int count = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT templateId, minLvl, skillId, skillLvl FROM pets_skills"))
		{
			while (rs.next())
			{
				final int npcId = rs.getInt("templateId");
				Map<Integer, L2PetSkillLearn> skillTree = _skillTrees.get(npcId);
				if (skillTree == null)
				{
					skillTree = new HashMap<>();
					_skillTrees.put(npcId, skillTree);
				}
				
				int id = rs.getInt("skillId");
				int lvl = rs.getInt("skillLvl");
				skillTree.put(SkillData.getSkillHashCode(id, lvl + 1), new L2PetSkillLearn(id, lvl, rs.getInt("minLvl")));
				count++;
			}
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Error while loading pet skill tree:", e);
		}
		_log.info(getClass().getSimpleName() + ": Loaded " + count + " skills.");
	}
	
	public int getAvailableLevel(L2Summon cha, int skillId)
	{
		int lvl = 0;
		if (!_skillTrees.containsKey(cha.getId()))
		{
			if (Config.DEBUG)
			{
				_log.warn(getClass().getSimpleName() + ": Pet id " + cha.getId() + " does not have any skills assigned.");
			}
			return lvl;
		}
		Collection<L2PetSkillLearn> skills = _skillTrees.get(cha.getId()).values();
		for (L2PetSkillLearn temp : skills)
		{
			if (temp.getId() != skillId)
			{
				continue;
			}
			if (temp.getLevel() == 0)
			{
				if (cha.getLevel() < 70)
				{
					lvl = (cha.getLevel() / 10);
					if (lvl <= 0)
					{
						lvl = 1;
					}
				}
				else
				{
					lvl = (7 + ((cha.getLevel() - 70) / 5));
				}
				
				// formula usable for skill that have 10 or more skill levels
				int maxLvl = SkillData.getInstance().getMaxLevel(temp.getId());
				if (lvl > maxLvl)
				{
					lvl = maxLvl;
				}
				break;
			}
			else if (temp.getMinLevel() <= cha.getLevel())
			{
				if (temp.getLevel() > lvl)
				{
					lvl = temp.getLevel();
				}
			}
		}
		return lvl;
	}
	
	public static final class L2PetSkillLearn
	{
		private final int _id;
		private final int _level;
		private final int _minLevel;
		
		public L2PetSkillLearn(int id, int lvl, int minLvl)
		{
			_id = id;
			_level = lvl;
			_minLevel = minLvl;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public int getLevel()
		{
			return _level;
		}
		
		public int getMinLevel()
		{
			return _minLevel;
		}
	}
	
	public static SummonSkillsTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SummonSkillsTable _instance = new SummonSkillsTable();
	}
}