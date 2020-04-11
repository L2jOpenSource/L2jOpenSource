package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.skills.Env;

public class EffectSilenceMagicPhysical extends L2Effect
{
	
	public EffectSilenceMagicPhysical(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return L2Effect.EffectType.SILENCE_MAGIC_PHYSICAL;
	}
	
	@Override
	public void onStart()
	{
		getEffected().startMuted();
		getEffected().startPsychicalMuted();
	}
	
	@Override
	public boolean onActionTime()
	{
		getEffected().stopMuted(this);
		getEffected().stopPsychicalMuted(this);
		return false;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopMuted(this);
		getEffected().stopPsychicalMuted(this);
	}
}
