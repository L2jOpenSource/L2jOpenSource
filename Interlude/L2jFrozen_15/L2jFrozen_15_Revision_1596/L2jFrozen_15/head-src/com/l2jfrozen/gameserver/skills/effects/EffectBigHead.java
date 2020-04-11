package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author L2JFrozen dev
 */
public class EffectBigHead extends L2Effect
{
	
	public EffectBigHead(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect(0x02000);
	}
	
	@Override
	public void onStart()
	{
		getEffected().startAbnormalEffect(0x02000);
	}
}
