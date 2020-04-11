/*
 * Copyright (C) L2J Sunrise
 * This file is part of L2J Sunrise.
 */
package handlers.effecthandlers;

import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;

/**
 * Implement restoration effect
 * @author vGodFather
 */
public class Restoration extends L2Effect
{
	private final int _itemId;
	private final int _itemCount;
	
	public Restoration(Env env, EffectTemplate template)
	{
		super(env, template);
		_itemId = template.getParameters().getInt("itemId", 0);
		_itemCount = template.getParameters().getInt("itemCount", 0);
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
		
		getEffected().getActingPlayer().addItem("Restoration: skillId" + getSkill().getId(), _itemId, _itemCount, getEffected().getActingPlayer(), true);
		return true;
	}
}