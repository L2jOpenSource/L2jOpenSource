/*
 * Copyright (C) 2004-2013 L2J DataPack
 *
 * This file is part of L2J DataPack.
 *
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.effects.AbnormalEffect;
import l2r.gameserver.model.effects.EffectFlag;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;

/**
 * Paralyze effect implementation.
 */
public class Paralyze extends L2Effect
{
	private boolean _mustCleanFreezingEffect;

	public Paralyze(Env env, EffectTemplate template)
	{
		super(env, template);
		_mustCleanFreezingEffect = false;
		if (template.hasParameters())
		{
			_mustCleanFreezingEffect = template.getParameters().getBool("mustCleanFreezingEffect", false);
		}
	}

	@Override
	public boolean canBeStolen()
	{
		return true;
	}

	@Override
	public int getEffectFlags()
	{
		return EffectFlag.PARALYZED.getMask();
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.PARALYZE;
	}

	@Override
	public void onExit()
	{
		if (!getEffected().isPlayer())
		{
			getEffected().getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
		
		if (_mustCleanFreezingEffect)
		{
			getEffected().stopSpecialEffect(AbnormalEffect.S_FREEZING);
		}
	}

	@Override
	public boolean onStart()
	{
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, getEffector());
		getEffected().startParalyze();
		return super.onStart();
	}
}
