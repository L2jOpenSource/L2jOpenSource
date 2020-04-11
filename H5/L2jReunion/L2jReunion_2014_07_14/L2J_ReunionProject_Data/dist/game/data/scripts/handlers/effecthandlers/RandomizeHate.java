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

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;
import l2r.util.Rnd;

public class RandomizeHate extends L2Effect
{
	public RandomizeHate(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffected() == null) || (getEffected() == getEffector()) || !getEffected().isAttackable())
		{
			return false;
		}
		
		L2Attackable effectedMob = (L2Attackable) getEffected();
		final List<L2Character> targetList = new ArrayList<>();
		for (L2Character cha : getEffected().getKnownList().getKnownCharacters())
		{
			if ((cha != null) && (cha != effectedMob) && (cha != getEffector()))
			{
				// Aggro cannot be transfered to a mob of the same faction.
				if (cha.isAttackable() && (((L2Attackable) cha).getFactionId() != null) && ((L2Attackable) cha).getFactionId().equals(effectedMob.getFactionId()))
				{
					continue;
				}
				
				targetList.add(cha);
			}
		}
		// if there is no target, exit function
		if (targetList.isEmpty())
		{
			return true;
		}
		
		// Choosing randomly a new target
		final L2Character target = targetList.get(Rnd.get(targetList.size()));
		final int hate = effectedMob.getHating(getEffector());
		effectedMob.stopHating(getEffector());
		effectedMob.addDamageHate(target, 0, hate);
		
		return true;
	}
}