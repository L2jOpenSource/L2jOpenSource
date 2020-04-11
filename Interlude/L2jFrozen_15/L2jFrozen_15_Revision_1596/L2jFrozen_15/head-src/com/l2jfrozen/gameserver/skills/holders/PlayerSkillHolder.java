package com.l2jfrozen.gameserver.skills.holders;

import java.util.HashMap;
import java.util.Map;

import com.l2jfrozen.gameserver.model.L2Skill;

/**
 * @author UnAfraid
 */
public class PlayerSkillHolder implements ISkillsHolder
{
	private final Map<Integer, L2Skill> skills = new HashMap<>();
	
	public PlayerSkillHolder(final Map<Integer, L2Skill> map)
	{
		skills.putAll(map);
	}
	
	/**
	 * @return the map containing this character skills.
	 */
	@Override
	public Map<Integer, L2Skill> getSkills()
	{
		return skills;
	}
	
	/**
	 * Add a skill to the skills map.<br>
	 * @param skill
	 */
	@Override
	public L2Skill addSkill(final L2Skill skill)
	{
		return skills.put(skill.getId(), skill);
	}
	
	/**
	 * Return the level of a skill owned by the L2Character.
	 * @param  skillId The identifier of the L2Skill whose level must be returned
	 * @return         The level of the L2Skill identified by skillId
	 */
	@Override
	public int getSkillLevel(final int skillId)
	{
		final L2Skill skill = getKnownSkill(skillId);
		return (skill == null) ? -1 : skill.getLevel();
	}
	
	/**
	 * @param  skillId The identifier of the L2Skill to check the knowledge
	 * @return         the skill from the known skill.
	 */
	@Override
	public L2Skill getKnownSkill(final int skillId)
	{
		return skills.get(skillId);
	}
}