package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.skills.L2Skill;

public interface ITargetHandler
{
	static final WorldObject[] EMPTY_TARGET_ARRAY = new WorldObject[0];
	
	/**
	 * The worker method called by a {@link Creature} when using a {@link L2Skill}.
	 * @param skill : The used {@link L2Skill}.
	 * @param caster : The {@link Creature} caster.
	 * @param onlyFirst : If true, return instantly the first target.
	 * @param target : The {@link Creature} used as initial target.
	 * @return The array of valid {@link WorldObject} targets, based on the {@link Creature} caster, {@link Creature} target and {@link L2Skill} set as parameters.
	 */
	public WorldObject[] getTargetList(L2Skill skill, Creature caster, Creature target, boolean onlyFirst);
	
	/**
	 * @return The associated {@link SkillTargetType}.
	 */
	public SkillTargetType getTargetType();
}