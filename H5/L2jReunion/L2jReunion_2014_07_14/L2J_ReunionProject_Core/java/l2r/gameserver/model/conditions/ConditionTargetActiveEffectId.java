/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.conditions;

import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;

/**
 * The Class ConditionTargetActiveEffectId.
 */
public class ConditionTargetActiveEffectId extends Condition
{
	private final int _effectId;
	private final int _effectLvl;
	
	/**
	 * Instantiates a new condition target active effect id.
	 * @param effectId the effect id
	 */
	public ConditionTargetActiveEffectId(int effectId)
	{
		_effectId = effectId;
		_effectLvl = -1;
	}
	
	/**
	 * Instantiates a new condition target active effect id.
	 * @param effectId the effect id
	 * @param effectLevel the effect level
	 */
	public ConditionTargetActiveEffectId(int effectId, int effectLevel)
	{
		_effectId = effectId;
		_effectLvl = effectLevel;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		final L2Effect e = env.getTarget().getFirstEffect(_effectId);
		if ((e != null) && ((_effectLvl == -1) || (_effectLvl <= e.getSkill().getLevel())))
		{
			return true;
		}
		return false;
	}
}
