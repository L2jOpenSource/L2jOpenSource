package com.l2jfrozen.gameserver.datatables;

import com.l2jfrozen.gameserver.model.L2Skill;

/**
 * @author -Nemesiss-
 */
public class NobleSkillTable
{
	private static NobleSkillTable instance;
	private static L2Skill[] nobleSkills;
	
	private NobleSkillTable()
	{
		nobleSkills = new L2Skill[8];
		nobleSkills[0] = SkillTable.getInstance().getInfo(1323, 1);
		nobleSkills[1] = SkillTable.getInstance().getInfo(325, 1);
		nobleSkills[2] = SkillTable.getInstance().getInfo(326, 1);
		nobleSkills[3] = SkillTable.getInstance().getInfo(327, 1);
		nobleSkills[4] = SkillTable.getInstance().getInfo(1324, 1);
		nobleSkills[5] = SkillTable.getInstance().getInfo(1325, 1);
		nobleSkills[6] = SkillTable.getInstance().getInfo(1326, 1);
		nobleSkills[7] = SkillTable.getInstance().getInfo(1327, 1);
	}
	
	public static NobleSkillTable getInstance()
	{
		if (instance == null)
		{
			instance = new NobleSkillTable();
		}
		
		return instance;
	}
	
	public L2Skill[] GetNobleSkills()
	{
		return nobleSkills;
	}
}
