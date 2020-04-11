package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.skills.Env;

public class EffectStunSelf extends L2Effect
{
	public EffectStunSelf(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.STUN_SELF;
	}
	
	@Override
	public void onStart()
	{
		if (getEffector() != null)
		{
			getEffector().startStunning();
		}
	}
	
	@Override
	public void onExit()
	{
		if (getEffector() != null)
		{
			getEffector().stopStunning(this);
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}
