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

import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2SiegeSummonInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;

/**
 * @author -Nemesiss-
 */
public class TargetMe extends L2Effect
{
	public TargetMe(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected().isPlayable())
		{
			if (getEffected() instanceof L2SiegeSummonInstance)
			{
				return false;
			}
			
			L2PcInstance effector = getEffector().getActingPlayer();
			L2PcInstance effected = getEffected().getActingPlayer();
			if ((effector != null) && (effected != null) && effector.isFriend(effected))
			{
				return false;
			}
			
			if (getEffected().getTarget() != getEffector())
			{
				// If effector is null, then its not a player, but NPC. If its not null, then it should check if the skill is pvp skill.
				if ((effector == null) || effector.checkPvpSkill(getEffected(), getSkill()))
				{
					// Target is different
					getEffected().setTarget(getEffector());
				}
			}
			((L2Playable) getEffected()).setLockedTarget(getEffector());
			return true;
		}
		else if (getEffected().isAttackable() && !getEffected().isRaid())
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onExit()
	{
		if (getEffected().isPlayable())
		{
			((L2Playable) getEffected()).setLockedTarget(null);
		}
	}
}
