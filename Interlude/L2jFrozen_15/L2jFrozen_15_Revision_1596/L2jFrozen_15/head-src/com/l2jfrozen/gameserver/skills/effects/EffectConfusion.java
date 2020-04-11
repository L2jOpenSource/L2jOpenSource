package com.l2jfrozen.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author littlecrow Implementation of the Confusion Effect
 */
final class EffectConfusion extends L2Effect
{
	
	public EffectConfusion(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.CONFUSION;
	}
	
	/** Notify started */
	@Override
	public void onStart()
	{
		getEffected().startConfused();
		onActionTime();
	}
	
	/** Notify exited */
	@Override
	public void onExit()
	{
		getEffected().stopConfused(this);
	}
	
	@Override
	public boolean onActionTime()
	{
		List<L2Character> targetList = new ArrayList<>();
		
		// Getting the possible targets
		
		for (final L2Object obj : getEffected().getKnownList().getKnownObjects().values())
		{
			if (obj == null)
			{
				continue;
			}
			
			if (obj instanceof L2Character && obj != getEffected())
			{
				targetList.add((L2Character) obj);
			}
		}
		// if there is no target, exit function
		if (targetList.size() == 0)
		{
			return true;
		}
		
		// Choosing randomly a new target
		final int nextTargetIdx = Rnd.nextInt(targetList.size());
		final L2Object target = targetList.get(nextTargetIdx);
		
		// Attacking the target
		getEffected().setTarget(target);
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		
		return true;
	}
}
