package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author eX1steam, L2JFrozen
 */
public class EffectProtectionBlessing extends L2Effect
{
	public EffectProtectionBlessing(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.PROTECTION_BLESSING;
	}
	
	/** Notify started */
	@Override
	public void onStart()
	{
		if (getEffected() instanceof L2PlayableInstance)
		{
			((L2PlayableInstance) getEffected()).startProtectionBlessing(this);
		}
	}
	
	/** Notify exited */
	@Override
	public void onExit()
	{
		if (getEffected() instanceof L2PlayableInstance)
		{
			((L2PlayableInstance) getEffected()).stopProtectionBlessing(this);
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}
