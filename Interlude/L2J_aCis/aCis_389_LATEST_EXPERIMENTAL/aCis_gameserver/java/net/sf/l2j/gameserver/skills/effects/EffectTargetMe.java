package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.skills.EffectType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.skills.AbstractEffect;
import net.sf.l2j.gameserver.skills.L2Skill;

public class EffectTargetMe extends AbstractEffect
{
	public EffectTargetMe(EffectTemplate template, L2Skill skill, Creature effected, Creature effector)
	{
		super(template, skill, effected, effector);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.TARGET_ME;
	}
	
	@Override
	public boolean onStart()
	{
		// work only on players, cause mobs got their own aggro system (AGGDAMAGE, AGGREMOVE, etc)
		if (getEffected() instanceof Player)
		{
			// add an INTENTION_ATTACK, but only if victim got attacker as target
			if (getEffected().getTarget() == getEffector())
				getEffected().getAI().tryTo(IntentionType.ATTACK, getEffector(), null);
			// target the agressor
			else
				getEffected().setTarget(getEffector());
			
			return true;
		}
		return false;
	}
	
	@Override
	public void onExit()
	{
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}