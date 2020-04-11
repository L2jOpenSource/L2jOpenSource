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
package l2r.gameserver.model.actor.transform;

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.stats.Stats;

/**
 * @author UnAfraid
 */
public final class TransformLevelData
{
	private final int _level;
	private final double _levelMod;
	private Map<Integer, Double> _stats;
	
	public TransformLevelData(StatsSet set)
	{
		_level = set.getInteger("val");
		_levelMod = set.getDouble("levelMod");
		addStats(Stats.MAX_HP, set.getDouble("hp"));
		addStats(Stats.MAX_MP, set.getDouble("mp"));
		addStats(Stats.MAX_CP, set.getDouble("cp"));
		addStats(Stats.REGENERATE_HP_RATE, set.getDouble("hpRegen"));
		addStats(Stats.REGENERATE_MP_RATE, set.getDouble("mpRegen"));
		addStats(Stats.REGENERATE_CP_RATE, set.getDouble("cpRegen"));
	}
	
	private void addStats(Stats stat, double val)
	{
		if (_stats == null)
		{
			_stats = new HashMap<>();
		}
		_stats.put(stat.ordinal(), val);
	}
	
	public double getStats(Stats stats)
	{
		if ((_stats == null) || !_stats.containsKey(stats.ordinal()))
		{
			return 0;
		}
		return _stats.get(stats.ordinal());
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public double getLevelMod()
	{
		return _levelMod;
	}
}
