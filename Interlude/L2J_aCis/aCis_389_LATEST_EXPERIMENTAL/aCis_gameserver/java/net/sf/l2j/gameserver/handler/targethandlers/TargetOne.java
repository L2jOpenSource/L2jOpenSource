package net.sf.l2j.gameserver.handler.targethandlers;

import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.handler.ITargetHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.L2Skill;

public class TargetOne implements ITargetHandler
{
	@Override
	public WorldObject[] getTargetList(L2Skill skill, Creature caster, Creature target, boolean onlyFirst)
	{
		boolean canTargetSelf = false;
		
		switch (skill.getSkillType())
		{
			case BUFF:
			case HEAL:
			case HOT:
			case HEAL_PERCENT:
			case MANARECHARGE:
			case MANAHEAL:
			case NEGATE:
			case CANCEL_DEBUFF:
			case REFLECT:
			case COMBATPOINTHEAL:
			case SEED:
			case BALANCE_LIFE:
				canTargetSelf = true;
				break;
		}
		
		if (target == null || target.isDead() || (target == caster && !canTargetSelf))
		{
			caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return EMPTY_TARGET_ARRAY;
		}
		
		return new Creature[]
		{
			target
		};
	}
	
	@Override
	public SkillTargetType getTargetType()
	{
		return SkillTargetType.ONE;
	}
}