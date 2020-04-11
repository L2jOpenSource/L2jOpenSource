package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author mkizub
 */
final class EffectSleep extends L2Effect
{
	
	public EffectSleep(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.SLEEP;
	}
	
	/** Notify started */
	@Override
	public void onStart()
	{
		getEffected().startSleeping();
	}
	
	/** Notify exited */
	@Override
	public void onExit()
	{
		getEffected().stopSleeping(this);
	}
	
	@Override
	public boolean onActionTime()
	{
		getEffected().stopSleeping(this);
		// just stop this effect
		return false;
	}
}
