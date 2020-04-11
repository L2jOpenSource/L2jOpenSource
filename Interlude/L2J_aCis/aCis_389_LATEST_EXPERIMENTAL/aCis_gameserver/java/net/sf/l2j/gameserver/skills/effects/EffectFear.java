package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.EffectFlag;
import net.sf.l2j.gameserver.enums.skills.EffectType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Folk;
import net.sf.l2j.gameserver.model.actor.instance.SiegeFlag;
import net.sf.l2j.gameserver.model.actor.instance.SiegeSummon;
import net.sf.l2j.gameserver.skills.AbstractEffect;
import net.sf.l2j.gameserver.skills.L2Skill;

public class EffectFear extends AbstractEffect
{
	public EffectFear(EffectTemplate template, L2Skill skill, Creature effected, Creature effector)
	{
		super(template, skill, effected, effector);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.FEAR;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof Player && getEffector() instanceof Player)
		{
			switch (getSkill().getId())
			{
				case 1376:
				case 1169:
				case 65:
				case 1092:
				case 98:
				case 1272:
				case 1381:
				case 763:
					break;
				
				default:
					return false;
			}
		}
		
		if (getEffected() instanceof Folk || getEffected() instanceof SiegeFlag || getEffected() instanceof SiegeSummon)
			return false;
		
		if (getEffected().isAfraid())
			return false;
		
		// Abort attack, cast and move.
		getEffected().abortAll(false);
		
		// Refresh abnormal effects.
		getEffected().updateAbnormalEffect();
		
		onActionTime();
		
		return true;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopEffects(EffectType.FEAR);
		
		// Refresh abnormal effects.
		getEffected().updateAbnormalEffect();
	}
	
	@Override
	public boolean onActionTime()
	{
		getEffected().fleeFrom(getEffector(), 500);
		return true;
	}
	
	@Override
	public boolean onSameEffect(AbstractEffect effect)
	{
		return false;
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.FEAR.getMask();
	}
}