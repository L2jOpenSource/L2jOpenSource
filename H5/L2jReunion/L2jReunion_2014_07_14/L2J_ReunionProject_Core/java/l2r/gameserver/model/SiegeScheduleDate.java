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
package l2r.gameserver.model;

import java.util.Calendar;

/**
 * @author UnAfraid
 */
public class SiegeScheduleDate
{
	private final int _day;
	private final int _hour;
	private final int _maxConcurrent;
	
	public SiegeScheduleDate(StatsSet set)
	{
		_day = set.getInteger("day", Calendar.SUNDAY);
		_hour = set.getInteger("hour", 16);
		_maxConcurrent = set.getInteger("maxConcurrent", 5);
	}
	
	public int getDay()
	{
		return _day;
	}
	
	public int getHour()
	{
		return _hour;
	}
	
	public int getMaxConcurrent()
	{
		return _maxConcurrent;
	}
}
