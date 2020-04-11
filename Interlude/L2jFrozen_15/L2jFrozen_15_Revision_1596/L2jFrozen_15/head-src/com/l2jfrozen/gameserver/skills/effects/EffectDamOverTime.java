package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Skill.SkillTargetType;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author L2JFrozen dev
 */

class EffectDamOverTime extends L2Effect
{
	public EffectDamOverTime(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.DMG_OVER_TIME;
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
		{
			return false;
		}
		
		double damage = calc();
		
		// Like L2OFF you can't die with DamOverTime
		if (damage >= getEffected().getCurrentHp() - 1)
		{
			if (getSkill().isToggle())
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_HP);
				getEffected().sendPacket(sm);
				getEffected().removeEffect(this);
				this.exit(false);
				return false;
			}
			
			// ** This is just hotfix, needs better solution **
			// 1947: "DOT skills shouldn't kill"
			// Well, some of them should ;-)
			if (getSkill().getId() != 4082)
			{
				damage = getEffected().getCurrentHp() - 1;
			}
		}
		
		final boolean awake = !(getEffected() instanceof L2Attackable) && !(getSkill().getTargetType() == SkillTargetType.TARGET_SELF && getSkill().isToggle());
		
		// getEffected().reduceCurrentHp(damage, getEffector(), awake);
		getEffected().reduceCurrentHpByDamOverTime(damage, getEffector(), awake, getPeriod());
		
		return true;
	}
}