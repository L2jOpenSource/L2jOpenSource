package net.sf.l2j.gameserver.handler.targethandlers;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.handler.ITargetHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.L2Skill;

public class TargetAreaCorpseMob implements ITargetHandler
{
	@Override
	public WorldObject[] getTargetList(L2Skill skill, Creature caster, Creature target, boolean onlyFirst)
	{
		if ((!(target instanceof Attackable)) || !target.isDead())
		{
			caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return EMPTY_TARGET_ARRAY;
		}
		
		if (onlyFirst)
			return new Creature[]
			{
				target
			};
		
		final List<Creature> list = new ArrayList<>();
		list.add(target);
		
		final boolean srcInArena = caster.isInArena();
		
		for (Creature creature : caster.getKnownTypeInRadius(Creature.class, skill.getSkillRadius()))
		{
			if (!(creature instanceof Attackable || creature instanceof Playable))
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
		return SkillTargetType.AREA_CORPSE_MOB;
	}
}