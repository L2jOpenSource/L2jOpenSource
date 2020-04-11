package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.skills.Env;

public class EffectDebuff extends L2Effect
{
	
	public EffectDebuff(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.DEBUFF;
	}
	
	@Override
	public boolean onActionTime()
	{
		// stop effect
		return false;
	}
}
