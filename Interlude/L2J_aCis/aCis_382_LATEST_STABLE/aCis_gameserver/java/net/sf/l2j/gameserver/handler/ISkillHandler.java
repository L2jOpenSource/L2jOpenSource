package net.sf.l2j.gameserver.handler;

import net.sf.l2j.commons.logging.CLogger;

import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;

public interface ISkillHandler
{
	public static final CLogger LOGGER = new CLogger(ISkillHandler.class.getName());
	
	/**
	 * The worker method called by a {@link Creature} when using a {@link L2Skill}.
	 * @param creature : The Creature who uses that L2Skill.
	 * @param skill : The L2Skill object itself.
	 * @param targets : The eventual targets.
	 */
	public void useSkill(Creature creature, L2Skill skill, WorldObject[] targets);
	
	/**
	 * @return all known {@link L2SkillType}s.
	 */
	public L2SkillType[] getSkillIds();
}