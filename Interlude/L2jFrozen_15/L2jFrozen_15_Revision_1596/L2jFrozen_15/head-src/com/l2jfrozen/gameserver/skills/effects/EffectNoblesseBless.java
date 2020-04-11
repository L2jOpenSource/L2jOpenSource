package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author earendil
 */
final class EffectNoblesseBless extends L2Effect
{
	
	public EffectNoblesseBless(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.NOBLESSE_BLESSING;
	}
	
	/** Notify started */
	@Override
	public void onStart()
	{
		if (getEffected() instanceof L2PlayableInstance)
		{
			((L2PlayableInstance) getEffected()).startNoblesseBlessing(this);
		}
	}
	
	/** Notify exited */
	@Override
	public void onExit()
	{
		if (getEffected() instanceof L2PlayableInstance)
		{
			((L2PlayableInstance) getEffected()).stopNoblesseBlessing(this);
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}
