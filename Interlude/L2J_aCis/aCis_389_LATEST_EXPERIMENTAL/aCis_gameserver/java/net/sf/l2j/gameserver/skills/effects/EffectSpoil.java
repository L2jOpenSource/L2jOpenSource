package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.EffectType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.AbstractEffect;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.L2Skill;

public class EffectSpoil extends AbstractEffect
{
	public EffectSpoil(EffectTemplate template, L2Skill skill, Creature effected, Creature effector)
	{
		super(template, skill, effected, effector);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.SPOIL;
	}
	
	@Override
	public boolean onStart()
	{
		if (!(getEffector() instanceof Player))
			return false;
		
		if (!(getEffected() instanceof Monster))
			return false;
		
		final Monster target = (Monster) getEffected();
		if (target.isDead())
			return false;
		
		if (target.getSpoilState().isSpoiled())
		{
			getEffector().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_SPOILED));
			return false;
		}
		
		if (Formulas.calcMagicSuccess(getEffector(), target, getSkill()))
		{
			target.getSpoilState().setSpoilerId(getEffector().getObjectId());
			getEffector().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SPOIL_SUCCESS));
		}
		
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}