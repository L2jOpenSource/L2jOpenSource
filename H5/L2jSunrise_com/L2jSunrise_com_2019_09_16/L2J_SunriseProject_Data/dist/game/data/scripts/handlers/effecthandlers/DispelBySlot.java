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

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;

/**
 * @author vGodFather
 */
public class DispelBySlot extends L2Effect
{
	private final String _dispel;
	private final Map<String, Short> _dispelAbnormals;
	
	public DispelBySlot(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_dispel = template.getParameters().getString("dispel", null);
		if ((_dispel != null) && !_dispel.isEmpty())
		{
			_dispelAbnormals = new ConcurrentHashMap<>();
			for (String ngtStack : _dispel.split(";"))
			{
				String[] ngt = ngtStack.split(",");
				_dispelAbnormals.put(ngt[0].toLowerCase(), Short.parseShort(ngt[1]));
			}
		}
		else
		{
			_dispelAbnormals = Collections.<String, Short> emptyMap();
		}
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DISPEL_BY_SLOT;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if (_dispelAbnormals.isEmpty())
		{
			return false;
		}
		
		L2Character target = getEffected();
		if ((target == null) || target.isDead())
		{
			return false;
		}
		
		for (Entry<String, Short> value : _dispelAbnormals.entrySet())
		{
			String stackType = value.getKey();
			float stackOrder = value.getValue();
			int skillCast = getSkill().getId();
			
			for (L2Effect e : target.getAllEffects())
			{
				if (!e.getSkill().canBeDispeled())
				{
					continue;
				}
				
				// Fist check for stacktype
				if (stackType.equalsIgnoreCase(e.getAbnormalType()) && (e.getSkill().getId() != skillCast))
				{
					if (e.getSkill() != null)
					{
						if (stackOrder == -1)
						{
							target.stopSkillEffects(e.getSkill().getId());
						}
						else if (stackOrder >= e.getAbnormalLvl())
						{
							target.stopSkillEffects(e.getSkill().getId());
						}
					}
				}
			}
		}
		return true;
	}
}
