package com.l2jfrozen.gameserver.skills.l2skills;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.skills.effects.EffectSeed;
import com.l2jfrozen.gameserver.templates.StatsSet;

public class L2SkillSeed extends L2Skill
{
	
	public L2SkillSeed(final StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(final L2Character caster, final L2Object[] targets)
	{
		if (caster.isAlikeDead())
		{
			return;
		}
		
		// Update Seeds Effects
		for (final L2Object target2 : targets)
		{
			final L2Character target = (L2Character) target2;
			if (target.isAlikeDead() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
			{
				continue;
			}
			
			final EffectSeed oldEffect = (EffectSeed) target.getFirstEffect(getId());
			if (oldEffect == null)
			{
				getEffects(caster, target, false, false, false);
			}
			else
			{
				oldEffect.increasePower();
			}
			
			final L2Effect[] effects = target.getAllEffects();
			for (final L2Effect effect : effects)
			{
				if (effect.getEffectType() == L2Effect.EffectType.SEED)
				{
					effect.rescheduleEffect();
					/*
					 * for (int j=0;j<effects.length;j++ { if (effects[j].getEffectType()==L2Effect.EffectType.SEED) { EffectSeed e = (EffectSeed)effects[j]; if (e.getInUse() || e.getSkill().getId()==this.getId()) { e.rescheduleEffect(); } } }
					 */
				}
			}
		}
	}
}
