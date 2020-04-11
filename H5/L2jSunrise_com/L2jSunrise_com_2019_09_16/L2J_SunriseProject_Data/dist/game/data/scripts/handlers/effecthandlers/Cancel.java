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

import java.util.List;

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Formulas;

/**
 * Cancel effect.
 * @author vGodFather
 */
public class Cancel extends L2Effect
{
	private final boolean _ordered;
	
	public Cancel(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_ordered = template.getParameters().getBoolean("ordered", false);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CANCEL;
	}
	
	@Override
	public boolean onStart()
	{
		return cancel(getEffector(), getEffected(), this);
	}
	
	private boolean cancel(L2Character activeChar, L2Character target, L2Effect effect)
	{
		if (target.isDead())
		{
			return false;
		}
		
		final List<L2Effect> canceled = Formulas.calcCancelStealEffects(activeChar, target, effect.getSkill(), effect.getEffectPower(), _ordered);
		for (L2Effect eff : canceled)
		{
			target.stopSkillEffects(eff.getSkill().getId());
		}
		return true;
	}
}