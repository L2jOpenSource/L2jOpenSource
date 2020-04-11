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

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.stats.Env;

/**
 * The Class ConditionTargetAggro.
 * @author mkizub
 */
public class ConditionTargetAggro extends Condition
{
	
	private final boolean _isAggro;
	
	/**
	 * Instantiates a new condition target aggro.
	 * @param isAggro the is aggro
	 */
	public ConditionTargetAggro(boolean isAggro)
	{
		_isAggro = isAggro;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		final L2Character target = env.getTarget();
		if (target instanceof L2MonsterInstance)
		{
			return ((L2MonsterInstance) target).isAggressive() == _isAggro;
		}
		if (target instanceof L2PcInstance)
		{
			return ((L2PcInstance) target).getKarma() > 0;
		}
		return false;
	}
}
