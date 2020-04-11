package net.sf.l2j.gameserver.handler.targethandlers;

import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.handler.ITargetHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.L2Skill;

public class TargetPartyMember implements ITargetHandler
{
	@Override
	public WorldObject[] getTargetList(L2Skill skill, Creature caster, Creature target, boolean onlyFirst)
	{
		if (target != null && (target == caster || (caster.isInParty() && target.isInParty() && caster.getParty().getLeaderObjectId() == target.getParty().getLeaderObjectId()) || (caster instanceof Player && target instanceof Summon && caster.getSummon() == target) || (caster instanceof Summon && target instanceof Player && caster == target.getSummon())))
		{
			if (!target.isDead())
			{
				return new Creature[]
				{
					target
				};
			}
			return EMPTY_TARGET_ARRAY;
		}
		
		caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
		return EMPTY_TARGET_ARRAY;
	}
	
	@Override
	public SkillTargetType getTargetType()
	{
		return SkillTargetType.PARTY_MEMBER;
	}
}