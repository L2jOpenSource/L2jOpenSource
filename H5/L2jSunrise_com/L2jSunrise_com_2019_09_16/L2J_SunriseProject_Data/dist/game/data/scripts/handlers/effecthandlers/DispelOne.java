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
import l2r.gameserver.model.stats.Env;

/**
 * @author vGodFather
 */
public class DispelOne extends L2Effect
{
	private final boolean _ordered;
	
	public DispelOne(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_ordered = template.getParameters().getBoolean("ordered", false);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DISPEL;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		L2Character target = getEffected();
		if ((target == null) || target.isDead())
		{
			return false;
		}
		
		if (_ordered)
		{
			L2Effect buff = null;
			for (L2Effect e : target.getAllEffects())
			{
				if ((e != null) && !e.getSkill().canBeDispeled() && e.getSkill().isDance())
				{
					continue;
				}
				buff = e;
				break;
			}
			
			if (buff != null)
			{
				buff.exit();
				return true;
			}
			
			for (L2Effect e : target.getAllEffects())
			{
				if ((e != null) && !e.getSkill().canBeDispeled())
				{
					continue;
				}
				buff = e;
				break;
			}
			
			if (buff != null)
			{
				buff.exit();
				return true;
			}
		}
		else
		{
			for (L2Effect e : target.getAllEffects())
			{
				if (!e.getSkill().canBeDispeled())
				{
					continue;
				}
				e.exit();
				break;
			}
		}
		return true;
	}
}
