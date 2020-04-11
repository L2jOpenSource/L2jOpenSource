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

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.effects.EffectFlag;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.skills.L2SkillType;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.SystemMessageId;

public class ChameleonRest extends L2Effect
{
	public ChameleonRest(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.RELAXING;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected().isPlayer())
		{
			getEffected().getActingPlayer().sitDown(false);
		}
		else
		{
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		}
		return super.onStart();
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
		{
			return false;
		}
		
		// Only cont skills shouldn't end
		if (getSkill().getSkillType() != L2SkillType.CONT)
		{
			return false;
		}
		
		if (getEffected().isPlayer())
		{
			if (!getEffected().getActingPlayer().isSitting())
			{
				return false;
			}
		}
		
		double manaDam = getValue();
		if (manaDam > getEffected().getCurrentMp())
		{
			getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
			return false;
		}
		
		getEffected().reduceCurrentMp(manaDam);
		return true;
	}
	
	@Override
	public int getEffectFlags()
	{
		return (EffectFlag.SILENT_MOVE.getMask() | EffectFlag.RELAXING.getMask());
	}
}