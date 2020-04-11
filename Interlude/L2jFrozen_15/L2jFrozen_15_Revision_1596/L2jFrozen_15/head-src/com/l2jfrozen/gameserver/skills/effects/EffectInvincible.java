package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.skills.Env;

public class EffectInvincible extends L2Effect
{
	public EffectInvincible(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return L2Effect.EffectType.INVINCIBLE;
	}
	
	@Override
	public void onStart()
	{
		getEffected().setIsInvul(true);
	}
	
	@Override
	public boolean onActionTime()
	{
		// Commented. But I'm not really sure about this, could cause some bugs.
		// getEffected().setIsInvul(false);
		return false;
	}
	
	@Override
	public void onExit()
	{
		getEffected().setIsInvul(false);
	}
}
