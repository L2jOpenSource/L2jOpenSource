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

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.stats.Env;
import l2r.util.Rnd;

/**
 * @author UnAfraid
 */
public class CancelDebuff extends L2Effect
{
	public CancelDebuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		return cancel(getEffector(), getEffected(), getSkill(), getEffectPower());
	}
	
	private static boolean cancel(L2Character caster, L2Character target, L2Skill skill, double baseRate)
	{
		if (target.isDead())
		{
			return false;
		}
		
		int count = 0;
		for (L2Effect e : target.getAllEffects())
		{
			if ((e == null) || !e.getSkill().isOffensive() || !e.getSkill().canBeDispeled())
			{
				continue;
			}
			
			// TODO: Unhardcode Poison of Death skill
			if (e.getSkill().getId() == 4082)
			{
				continue;
			}
			
			if (Rnd.get(100) > baseRate)
			{
				continue;
			}
			
			e.exit();
			
			count++;
			if (count >= skill.getMaxNegatedEffects())
			{
				break;
			}
		}
		return true;
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DISPEL;
	}
}