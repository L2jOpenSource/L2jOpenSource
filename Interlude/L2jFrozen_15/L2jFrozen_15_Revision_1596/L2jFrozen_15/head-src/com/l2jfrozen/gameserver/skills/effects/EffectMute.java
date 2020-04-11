package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.skills.Env;

public class EffectMute extends L2Effect
{
	public EffectMute(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return L2Effect.EffectType.MUTE;
	}
	
	@Override
	public void onStart()
	{
		getEffected().startMuted();
	}
	
	@Override
	public boolean onActionTime()
	{
		// Simply stop the effect
		getEffected().stopMuted(this);
		return false;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopMuted(this);
	}
}
