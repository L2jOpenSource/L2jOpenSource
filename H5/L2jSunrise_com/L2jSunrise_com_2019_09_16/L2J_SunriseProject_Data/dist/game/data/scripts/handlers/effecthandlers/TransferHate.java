/*
 * Copyright (C) 2004-2015 L2J DataPack
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

import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Formulas;

/**
 * Transfer Hate effect implementation.
 * @author Adry_85
 */
public final class TransferHate extends L2Effect
{
	private final int _chance;
	
	public TransferHate(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_chance = template.getParameters().getInt("chance", 100);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if (!Formulas.calcProbability(_chance, getEffector(), getEffected(), getSkill()))
		{
			return false;
		}
		
		for (L2Character obj : getEffector().getKnownList().getKnownCharactersInRadius(getSkill().getAffectRange()))
		{
			if ((obj == null) || !obj.isAttackable() || obj.isDead())
			{
				continue;
			}
			
			final L2Attackable hater = ((L2Attackable) obj);
			final long hate = hater.getHating(getEffector());
			if (hate <= 0)
			{
				continue;
			}
			
			hater.reduceHate(getEffector(), -hate);
			hater.addDamageHate(getEffected(), 0, hate);
		}
		return true;
	}
}
