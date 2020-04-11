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
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Formulas;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * Steal Abnormal effect implementation.
 * @author Adry_85, Zoey76, vGodFather
 */
public class StealAbnormal extends L2Effect
{
	private final String _slot;
	private final int _rate;
	private final int _max;
	
	public StealAbnormal(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_slot = template.getParameters().getString("slot", null);
		_rate = template.getParameters().getInt("rate", 0);
		_max = template.getParameters().getInt("max", 0);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.STEAL_ABNORMAL;
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffected() != null) && getEffected().isPlayer() && (getEffector() != getEffected()))
		{
			final List<L2Effect> toSteal = Formulas.calcCancelStealEffects(getEffector(), getEffected(), getSkill(), _slot, _rate, _max);
			if (toSteal.isEmpty())
			{
				return false;
			}
			
			Map<L2Skill, L2Effect> skillIds = new ConcurrentHashMap<>();
			for (L2Effect eff : toSteal)
			{
				L2Skill skill = eff.getSkill();
				if (!skillIds.containsKey(skill))
				{
					skillIds.put(skill, eff);
				}
			}
			
			Env env = new Env();
			env.setCharacter(getEffected());
			env.setTarget(getEffector());
			
			for (Entry<L2Skill, L2Effect> stats : skillIds.entrySet())
			{
				L2Skill skill = stats.getKey();
				L2Effect effect = stats.getValue();
				if (skill.hasEffects())
				{
					env.setSkill(skill);
					
					L2Effect ef;
					for (EffectTemplate et : skill.getEffectTemplates())
					{
						ef = et.getEffect(env);
						if (ef != null)
						{
							ef.setCount(effect.getCount());
							ef.setFirstTime(effect.getTime());
							ef.scheduleEffect();
							
							if (ef.getShowIcon() && getEffector().isPlayer())
							{
								final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
								sm.addSkillName(effect);
								getEffector().sendPacket(sm);
							}
						}
					}
				}
				getEffected().stopSkillEffects(skill.getId());
			}
			return true;
		}
		return false;
	}
}
