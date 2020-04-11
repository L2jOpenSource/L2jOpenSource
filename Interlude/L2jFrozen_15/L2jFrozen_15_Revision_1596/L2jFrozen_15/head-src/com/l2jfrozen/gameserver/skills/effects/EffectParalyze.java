package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.skills.Env;

final class EffectParalyze extends L2Effect
{
	
	public EffectParalyze(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.PARALYZE;
	}
	
	@Override
	public void onStart()
	{
		getEffected().stopMove(null);
		getEffected().startAbnormalEffect(L2Character.ABNORMAL_EFFECT_HOLD_1);
		getEffected().setIsParalyzed(true);
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect(L2Character.ABNORMAL_EFFECT_HOLD_1);
		getEffected().setIsParalyzed(false);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
