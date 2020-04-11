package com.l2jfrozen.gameserver.skills;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Skill;

/**
 * @author BiggBoss Simple class for storing skill id/level
 */
public final class SkillHolder
{
	private final int skillId;
	private final int skillLvl;
	
	public SkillHolder(final int skillId, final int skillLvl)
	{
		this.skillId = skillId;
		this.skillLvl = skillLvl;
	}
	
	public SkillHolder(final L2Skill skill)
	{
		skillId = skill.getId();
		skillLvl = skill.getLevel();
	}
	
	public final int getSkillId()
	{
		return skillId;
	}
	
	public final int getSkillLvl()
	{
		return skillLvl;
	}
	
	public final L2Skill getSkill()
	{
		return SkillTable.getInstance().getInfo(skillId, skillLvl);
	}
}