package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author mkizub
 */
final class EffectImobileBuff extends L2Effect
{
	public EffectImobileBuff(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}
	
	/** Notify started */
	@Override
	public void onStart()
	{
		getEffector().setIsImobilised(true);
	}
	
	/** Notify exited */
	@Override
	public void onExit()
	{
		getEffector().setIsImobilised(false);
	}
	
	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}