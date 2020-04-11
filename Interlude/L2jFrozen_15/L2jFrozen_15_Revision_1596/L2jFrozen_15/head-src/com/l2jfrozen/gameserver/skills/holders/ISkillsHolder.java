package com.l2jfrozen.gameserver.skills.holders;

import java.util.Map;

import com.l2jfrozen.gameserver.model.L2Skill;

/**
 * @author UnAfraid
 */
public interface ISkillsHolder
{
	public Map<Integer, L2Skill> getSkills();
	
	public L2Skill addSkill(L2Skill skill);
	
	public L2Skill getKnownSkill(int skillId);
	
	public int getSkillLevel(int skillId);
}