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

import java.util.Set;

import l2r.gameserver.enums.NpcRace;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.stats.Env;

/**
 * Condition that checks target NPC race.
 * @author Nos
 */
public class ConditionTargetNpcRace extends Condition
{
	private final Set<NpcRace> _races;
	
	/**
	 * Instantiates a new condition target NPC race
	 * @param races the races
	 */
	public ConditionTargetNpcRace(Set<NpcRace> races)
	{
		_races = races;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		final L2Npc npc = env.getTarget() instanceof L2Npc ? (L2Npc) env.getTarget() : null;
		return (npc != null) && _races.contains(npc.getTemplate().getRace());
	}
}
