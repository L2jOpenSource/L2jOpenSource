package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.skills.Env;

public class EffectBlockBuff extends L2Effect
{
	public EffectBlockBuff(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return L2Effect.EffectType.BLOCK_BUFF;
	}
	
	@Override
	public void onStart()
	{
		getEffected().setBlockBuff(true);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public void onExit()
	{
		getEffected().setBlockBuff(false);
	}
}