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
 * The Class ConditionForceBuff.
 * @author kombat, Forsaiken
 */
public final class ConditionForceBuff extends Condition
{
	private static final short BATTLE_FORCE = 5104;
	private static final short SPELL_FORCE = 5105;
	
	private final byte[] _forces;
	
	/**
	 * Instantiates a new condition force buff.
	 * @param forces the forces
	 */
	public ConditionForceBuff(byte[] forces)
	{
		_forces = forces;
	}
	
	/**
	 * Test impl.
	 * @param env the env
	 * @return true, if successful
	 */
	@Override
	public boolean testImpl(Env env)
	{
		if (_forces[0] > 0)
		{
			L2Effect force = env.getCharacter().getFirstEffect(BATTLE_FORCE);
			if ((force == null) || (force.getForceEffect() < _forces[0]))
			{
				return false;
			}
		}
		
		if (_forces[1] > 0)
		{
			L2Effect force = env.getCharacter().getFirstEffect(SPELL_FORCE);
			if ((force == null) || (force.getForceEffect() < _forces[1]))
			{
				return false;
			}
		}
		return true;
	}
}
