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

import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.util.Util;

/**
 * Rebalance HP effect.
 * @author Adry_85, earendil
 */
public class RebalanceHP extends L2Effect
{
	public RebalanceHP(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.REBALANCE_HP;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if (!getEffector().isPlayer() || !getEffector().isInParty())
		{
			return false;
		}
		
		double fullHP = 0;
		double currentHPs = 0;
		final L2Party party = getEffector().getParty();
		for (L2PcInstance member : party.getMembers())
		{
			if (member.isDead() || !Util.checkIfInRange(getSkill().getAffectRange(), getEffector(), member, true))
			{
				continue;
			}
			
			if (member.hasSummon() && !member.getSummon().isDead())
			{
				fullHP += member.getSummon().getMaxHp();
				currentHPs += member.getSummon().getCurrentHp();
			}
			
			fullHP += member.getMaxHp();
			currentHPs += member.getCurrentHp();
		}
		
		double percentHP = currentHPs / fullHP;
		for (L2PcInstance member : party.getMembers())
		{
			if (member.isDead() || !Util.checkIfInRange(getSkill().getAffectRange(), getEffector(), member, true))
			{
				continue;
			}
			
			if (member.hasSummon())
			{
				double newHP = member.getSummon().getMaxHp() * percentHP;
				if (newHP > member.getSummon().getCurrentHp()) // The target gets healed
				{
					// The heal will be blocked if the current hp passes the limit
					if (member.getSummon().getCurrentHp() > member.getSummon().getMaxRecoverableHp())
					{
						newHP = member.getSummon().getCurrentHp();
					}
					else if (newHP > member.getSummon().getMaxRecoverableHp())
					{
						newHP = member.getSummon().getMaxRecoverableHp();
					}
				}
				
				member.getSummon().setCurrentHp(newHP);
			}
			
			double newHP = member.getMaxHp() * percentHP;
			if (newHP > member.getCurrentHp()) // The target gets healed
			{
				// The heal will be blocked if the current hp passes the limit
				if (member.getCurrentHp() > member.getMaxRecoverableHp())
				{
					newHP = member.getCurrentHp();
				}
				else if (newHP > member.getMaxRecoverableHp())
				{
					newHP = member.getMaxRecoverableHp();
				}
			}
			
			member.setCurrentHp(newHP);
		}
		return true;
	}
}
