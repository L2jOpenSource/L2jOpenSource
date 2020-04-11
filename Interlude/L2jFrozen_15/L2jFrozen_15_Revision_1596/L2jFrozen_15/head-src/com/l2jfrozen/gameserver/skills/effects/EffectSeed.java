package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.skills.Env;

public final class EffectSeed extends L2Effect
{
	
	private int power = 1;
	
	public EffectSeed(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.SEED;
	}
	
	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
	
	public int getPower()
	{
		return power;
	}
	
	public void increasePower()
	{
		power++;
	}
}
