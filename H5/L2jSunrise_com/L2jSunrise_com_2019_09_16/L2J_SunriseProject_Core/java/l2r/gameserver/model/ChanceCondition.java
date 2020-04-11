/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.gameserver.model;

import l2r.gameserver.enums.TriggerType;
import l2r.gameserver.model.skills.L2Skill;
import l2r.util.Rnd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vGodFather
 */
public final class ChanceCondition
{
	protected static final Logger _log = LoggerFactory.getLogger(ChanceCondition.class);
	public static final int EVT_HIT = 1;
	public static final int EVT_CRIT = 2;
	public static final int EVT_CAST = 4;
	public static final int EVT_PHYSICAL = 8;
	public static final int EVT_MAGIC = 16;
	public static final int EVT_MAGIC_GOOD = 32;
	public static final int EVT_MAGIC_OFFENSIVE = 64;
	public static final int EVT_ATTACKED = 128;
	public static final int EVT_ATTACKED_HIT = 256;
	public static final int EVT_ATTACKED_CRIT = 512;
	public static final int EVT_HIT_BY_SKILL = 1024;
	public static final int EVT_HIT_BY_OFFENSIVE_SKILL = 2048;
	public static final int EVT_HIT_BY_GOOD_MAGIC = 4096;
	public static final int EVT_EVADED_HIT = 8192;
	public static final int EVT_ON_START = 16384;
	public static final int EVT_ON_ACTION_TIME = 32768;
	public static final int EVT_ON_EXIT = 65536;
	
	private final TriggerType _triggerType;
	private final int _chance;
	
	private ChanceCondition(TriggerType trigger, int chance)
	{
		_triggerType = trigger;
		_chance = chance;
	}
	
	public static ChanceCondition parse(StatsSet set)
	{
		try
		{
			TriggerType trigger = set.getEnum("chanceType", TriggerType.class, null);
			int chance = set.getInt("activationChance", -1);
			
			if (trigger != null)
			{
				return new ChanceCondition(trigger, chance);
			}
		}
		catch (Exception e)
		{
			_log.warn(String.valueOf(e));
		}
		return null;
	}
	
	public static ChanceCondition parse(String chanceType, int chance)
	{
		try
		{
			if (chanceType == null)
			{
				return null;
			}
			
			TriggerType trigger = Enum.valueOf(TriggerType.class, chanceType);
			
			if (trigger != null)
			{
				return new ChanceCondition(trigger, chance);
			}
		}
		catch (Exception e)
		{
			_log.warn(String.valueOf(e));
		}
		
		return null;
	}
	
	public boolean trigger(int event, double damage, byte element, boolean playable, L2Skill skill)
	{
		return _triggerType.check(event) && ((_chance < 0) || (Rnd.get(100) < _chance));
	}
	
	@Override
	public String toString()
	{
		return "Trigger[" + _chance + ";" + _triggerType.toString() + "]";
	}
}