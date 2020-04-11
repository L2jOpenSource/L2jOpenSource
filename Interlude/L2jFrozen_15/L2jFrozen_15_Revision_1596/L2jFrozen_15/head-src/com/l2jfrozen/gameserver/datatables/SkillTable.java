package com.l2jfrozen.gameserver.datatables;

import java.util.HashMap;
import java.util.Map;

import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.skills.SkillsEngine;
import com.l2jfrozen.gameserver.templates.L2WeaponType;

/**
 * This class ...
 * @author  ProGramMoS, scoria dev
 * @version $Revision: 1.8.2.6.2.18 $ $Date: 2009/04/09 12:06 $
 */
public class SkillTable
{
	// private static Logger LOGGER = Logger.getLogger(SkillTable.class);
	private static SkillTable instance;
	
	private final Map<Integer, L2Skill> skills;
	private final boolean initialized = true;
	
	public static SkillTable getInstance()
	{
		if (instance == null)
		{
			instance = new SkillTable();
		}
		
		return instance;
	}
	
	private SkillTable()
	{
		skills = new HashMap<>();
		SkillsEngine.getInstance().loadAllSkills(skills);
	}
	
	public void reload()
	{
		instance = new SkillTable();
	}
	
	public boolean isInitialized()
	{
		return initialized;
	}
	
	/**
	 * Provides the skill hash
	 * @param  skill The L2Skill to be hashed
	 * @return       SkillTable.getSkillHashCode(skill.getId(), skill.getLevel())
	 */
	public static int getSkillHashCode(final L2Skill skill)
	{
		return SkillTable.getSkillHashCode(skill.getId(), skill.getLevel());
	}
	
	/**
	 * Centralized method for easier change of the hashing sys
	 * @param  skillId    The Skill Id
	 * @param  skillLevel The Skill Level
	 * @return            The Skill hash number
	 */
	public static int getSkillHashCode(final int skillId, final int skillLevel)
	{
		return skillId * 256 + skillLevel;
	}
	
	public L2Skill getInfo(final int skillId, final int level)
	{
		return skills.get(SkillTable.getSkillHashCode(skillId, level));
	}
	
	public int getMaxLevel(final int magicId, int level)
	{
		L2Skill temp;
		
		while (level < 100)
		{
			level++;
			temp = skills.get(SkillTable.getSkillHashCode(magicId, level));
			
			if (temp == null)
			{
				return level - 1;
			}
		}
		
		temp = null;
		
		return level;
	}
	
	private static final L2WeaponType[] weaponDbMasks =
	{
		L2WeaponType.ETC,
		L2WeaponType.BOW,
		L2WeaponType.POLE,
		L2WeaponType.DUALFIST,
		L2WeaponType.DUAL,
		L2WeaponType.BLUNT,
		L2WeaponType.SWORD,
		L2WeaponType.DAGGER,
		L2WeaponType.BIGSWORD,
		L2WeaponType.ROD,
		L2WeaponType.BIGBLUNT
	};
	
	public int calcWeaponsAllowed(final int mask)
	{
		if (mask == 0)
		{
			return 0;
		}
		
		int weaponsAllowed = 0;
		
		for (int i = 0; i < weaponDbMasks.length; i++)
		{
			if ((mask & 1 << i) != 0)
			{
				weaponsAllowed |= weaponDbMasks[i].mask();
			}
		}
		
		return weaponsAllowed;
	}
}
