package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author ProGramMoS, L2JFrozen
 */
final class EffectBuff extends L2Effect
{
	
	public EffectBuff(final Env envbuff, final EffectTemplate template)
	{
		super(envbuff, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}
	
	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}
