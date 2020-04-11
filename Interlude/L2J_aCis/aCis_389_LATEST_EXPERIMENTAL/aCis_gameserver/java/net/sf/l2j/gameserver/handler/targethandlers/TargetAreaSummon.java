package net.sf.l2j.gameserver.handler.targethandlers;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.commons.math.MathUtil;

import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.handler.ITargetHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Servitor;
import net.sf.l2j.gameserver.skills.L2Skill;

public class TargetAreaSummon implements ITargetHandler
{
	@Override
	public WorldObject[] getTargetList(L2Skill skill, Creature caster, Creature target, boolean onlyFirst)
	{
		target = caster.getSummon();
		if (target == null || !(target instanceof Servitor) || target.isDead())
			return EMPTY_TARGET_ARRAY;
		
		if (onlyFirst)
			return new Creature[]
			{
				target
			};
		
		final boolean srcInArena = caster.isInArena();
		final List<Creature> list = new ArrayList<>();
		
		for (Creature creature : target.getKnownType(Creature.class))
		{
			if (creature == target || creature == caster)
				continue;
			
			if (!(creature instanceof Attackable || creature instanceof Playable))
				continue;
			
			if (!MathUtil.checkIfInRange(skill.getSkillRadius(), target, creature, true))
				continue;
			
			if (!L2Skill.checkForAreaOffensiveSkills(caster, creature, skill, srcInArena))
				continue;
			
			list.add(creature);
		}
		
		if (list.isEmpty())
			return EMPTY_TARGET_ARRAY;
		
		return list.toArray(new Creature[list.size()]);
	}
	
	@Override
	public SkillTargetType getTargetType()
	{
		return SkillTargetType.AREA_SUMMON;
	}
}