package net.sf.l2j.gameserver.handler.targethandlers;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.enums.skills.SkillType;
import net.sf.l2j.gameserver.handler.ITargetHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.skills.L2Skill;

public class TargetAura implements ITargetHandler
{
	@Override
	public WorldObject[] getTargetList(L2Skill skill, Creature caster, Creature target, boolean onlyFirst)
	{
		final List<Creature> list = new ArrayList<>();
		
		if (skill.getSkillType() == SkillType.DUMMY)
		{
			if (onlyFirst)
				return new Creature[]
				{
					caster
				};
			
			final Player sourcePlayer = caster.getActingPlayer();
			
			list.add(caster);
			
			for (Creature creature : caster.getKnownTypeInRadius(Creature.class, skill.getSkillRadius()))
			{
				if (!(creature == caster || creature == sourcePlayer || creature instanceof Npc || creature instanceof Attackable))
					continue;
				
				list.add(creature);
			}
		}
		else
		{
			final boolean srcInArena = caster.isInArena();
			
			for (Creature creature : caster.getKnownTypeInRadius(Creature.class, skill.getSkillRadius()))
			{
				if (creature instanceof Attackable || creature instanceof Playable)
				{
					if (!L2Skill.checkForAreaOffensiveSkills(caster, creature, skill, srcInArena))
						continue;
					
					if (onlyFirst)
						return new Creature[]
						{
							creature
						};
					
					list.add(creature);
				}
			}
		}
		return list.toArray(new Creature[list.size()]);
	}
	
	@Override
	public SkillTargetType getTargetType()
	{
		return SkillTargetType.AURA;
	}
}