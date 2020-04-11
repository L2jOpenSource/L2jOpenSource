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
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.L2Skill;

public class TargetFrontArea implements ITargetHandler
{
	@Override
	public WorldObject[] getTargetList(L2Skill skill, Creature caster, Creature target, boolean onlyFirst)
	{
		if (((target == null || target == caster || target.isAlikeDead()) && skill.getCastRange() >= 0) || (!(target instanceof Attackable || target instanceof Playable)))
		{
			caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return EMPTY_TARGET_ARRAY;
		}
		
		final Creature origin;
		final boolean srcInArena = caster.isInArena();
		
		final List<Creature> list = new ArrayList<>();
		
		if (skill.getCastRange() >= 0)
		{
			if (!L2Skill.checkForAreaOffensiveSkills(caster, target, skill, srcInArena))
				return EMPTY_TARGET_ARRAY;
			
			if (onlyFirst)
				return new Creature[]
				{
					target
				};
			
			origin = target;
			
			list.add(origin);
		}
		else
			origin = caster;
		
		for (Creature creature : caster.getKnownType(Creature.class))
		{
			if (!(creature instanceof Attackable || creature instanceof Playable))
				continue;
			
			if (creature == origin)
				continue;
			
			if (MathUtil.checkIfInRange(skill.getSkillRadius(), origin, creature, true))
			{
				if (!creature.isInFrontOf(caster))
					continue;
				
				if (!L2Skill.checkForAreaOffensiveSkills(caster, creature, skill, srcInArena))
					continue;
				
				list.add(creature);
			}
		}
		
		if (list.isEmpty())
			return EMPTY_TARGET_ARRAY;
		
		return list.toArray(new Creature[list.size()]);
	}
	
	@Override
	public SkillTargetType getTargetType()
	{
		return SkillTargetType.FRONT_AREA;
	}
}