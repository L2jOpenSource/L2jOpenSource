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
package l2r.gameserver.data.xml.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import l2r.Config;
import l2r.gameserver.engines.DocumentEngine;
import l2r.gameserver.model.skills.L2Skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkillData
{
	private static Logger LOGGER = LoggerFactory.getLogger(SkillData.class);
	
	private final Map<Integer, L2Skill> _skills = new HashMap<>();
	private final Map<Integer, Integer> _skillMaxLevel = new HashMap<>();
	private final Set<Integer> _enchantable = new HashSet<>();
	
	protected SkillData()
	{
		load();
	}
	
	public void reload()
	{
		load();
		// Reload Skill Tree as well.
		SkillTreesData.getInstance().load();
	}
	
	private void load()
	{
		final Map<Integer, L2Skill> _temp = new HashMap<>();
		DocumentEngine.getInstance().loadAllSkills(_temp);
		
		_skills.clear();
		_skills.putAll(_temp);
		
		_skillMaxLevel.clear();
		_enchantable.clear();
		for (final L2Skill skill : _skills.values())
		{
			final int skillId = skill.getId();
			final int skillLvl = skill.getLevel();
			if (skillLvl > 99)
			{
				if (!_enchantable.contains(skillId))
				{
					_enchantable.add(skillId);
				}
				continue;
			}
			
			// only non-enchanted skills
			final int maxLvl = getMaxLevel(skillId);
			if (skillLvl > maxLvl)
			{
				_skillMaxLevel.put(skillId, skillLvl);
			}
		}
	}
	
	/**
	 * Provides the skill hash
	 * @param skill The L2Skill to be hashed
	 * @return getSkillHashCode(skill.getId(), skill.getLevel())
	 */
	public static int getSkillHashCode(L2Skill skill)
	{
		return getSkillHashCode(skill.getId(), skill.getLevel());
	}
	
	/**
	 * Centralized method for easier change of the hashing sys
	 * @param skillId The Skill Id
	 * @param skillLevel The Skill Level
	 * @return The Skill hash number
	 */
	public static int getSkillHashCode(int skillId, int skillLevel)
	{
		return (skillId * 1021) + skillLevel;
	}
	
	public final L2Skill getInfo(final int skillId, final int level)
	{
		return getSkill(skillId, level);
	}
	
	public final L2Skill getSkill(final int skillId, final int level)
	{
		final L2Skill result = _skills.get(getSkillHashCode(skillId, level));
		if (result != null)
		{
			return result;
		}
		
		// skill/level not found, fix for transformation scripts
		final int maxLvl = _skillMaxLevel.get(skillId);
		// requested level too high
		if ((maxLvl > 0) && (level > maxLvl))
		{
			if (Config.DEBUG)
			{
				LOGGER.warn(getClass().getSimpleName() + ": call to unexisting skill level id: " + skillId + " requested level: " + level + " max level: " + maxLvl, new Throwable());
			}
			return _skills.get(getSkillHashCode(skillId, maxLvl));
		}
		
		if (Config.DEBUG)
		{
			LOGGER.warn(getClass().getSimpleName() + ": No skill info found for skill id " + skillId + " and skill level " + level + ".");
		}
		return null;
	}
	
	public final int getMaxLevel(int skillId)
	{
		final Integer maxLevel = _skillMaxLevel.get(skillId);
		return maxLevel != null ? maxLevel : 0;
	}
	
	/**
	 * Verifies if the given skill ID correspond to an enchantable skill.
	 * @param skillId the skill ID
	 * @return {@code true} if the skill is enchantable, {@code false} otherwise
	 */
	public final boolean isEnchantable(int skillId)
	{
		return _enchantable.contains(skillId);
	}
	
	/**
	 * @param addNoble
	 * @param hasCastle
	 * @return an array with siege skills. If addNoble == true, will add also Advanced headquarters.
	 */
	public L2Skill[] getSiegeSkills(boolean addNoble, boolean hasCastle)
	{
		L2Skill[] temp = new L2Skill[2 + (addNoble ? 1 : 0) + (hasCastle ? 2 : 0)];
		int i = 0;
		temp[i++] = _skills.get(SkillData.getSkillHashCode(246, 1));
		temp[i++] = _skills.get(SkillData.getSkillHashCode(247, 1));
		
		if (addNoble)
		{
			temp[i++] = _skills.get(SkillData.getSkillHashCode(326, 1));
		}
		if (hasCastle)
		{
			temp[i++] = _skills.get(SkillData.getSkillHashCode(844, 1));
			temp[i++] = _skills.get(SkillData.getSkillHashCode(845, 1));
		}
		return temp;
	}
	
	public static SkillData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillData _instance = new SkillData();
	}
}