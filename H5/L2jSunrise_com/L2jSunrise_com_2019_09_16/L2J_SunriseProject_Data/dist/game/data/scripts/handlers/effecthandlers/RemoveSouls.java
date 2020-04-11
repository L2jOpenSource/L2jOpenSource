/*
 * Copyright (C) L2J Sunrise
 * This file is part of L2J Sunrise.
 */
package handlers.effecthandlers;

import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;

/**
 * @author vGodFather
 */
public class RemoveSouls extends L2Effect
{
	private final int _count;
	
	public RemoveSouls(Env env, EffectTemplate template)
	{
		super(env, template);
		_count = template.getParameters().getInt("count", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffected() == null) || !getEffected().isPlayer())
		{
			return false;
		}
		
		getEffected().getActingPlayer().decreaseSouls(_count, getSkill());
		return true;
	}
}