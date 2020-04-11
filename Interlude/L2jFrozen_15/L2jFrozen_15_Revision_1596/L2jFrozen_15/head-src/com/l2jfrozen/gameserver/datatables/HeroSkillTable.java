package com.l2jfrozen.gameserver.datatables;

import com.l2jfrozen.gameserver.model.L2Skill;

/**
 * @author BiTi
 */
public class HeroSkillTable
{
	private static HeroSkillTable instance;
	private static L2Skill[] heroSkills;
	
	private HeroSkillTable()
	{
		heroSkills = new L2Skill[5];
		heroSkills[0] = SkillTable.getInstance().getInfo(395, 1);
		heroSkills[1] = SkillTable.getInstance().getInfo(396, 1);
		heroSkills[2] = SkillTable.getInstance().getInfo(1374, 1);
		heroSkills[3] = SkillTable.getInstance().getInfo(1375, 1);
		heroSkills[4] = SkillTable.getInstance().getInfo(1376, 1);
	}
	
	public static HeroSkillTable getInstance()
	{
		if (instance == null)
		{
			instance = new HeroSkillTable();
		}
		
		return instance;
	}
	
	public static L2Skill[] getHeroSkills()
	{
		return heroSkills;
	}
	
	public static boolean isHeroSkill(final int skillid)
	{
		Integer[] heroSkillsId = new Integer[]
		{
			395,
			396,
			1374,
			1375,
			1376
		};
		
		for (final int id : heroSkillsId)
		{
			if (id == skillid)
			{
				return true;
			}
		}
		heroSkillsId = null;
		
		return false;
	}
}
