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
package l2r.gameserver.util;

import java.util.Comparator;

import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * Static comparators.
 * @author Zoey76
 */
public class Comparators
{
	/** Compares its two arguments for order by it's name. */
	public static final Comparator<L2PcInstance> PLAYER_NAME_COMPARATOR = new Comparator<L2PcInstance>()
	{
		@Override
		public int compare(L2PcInstance p1, L2PcInstance p2)
		{
			return p1.getName().compareToIgnoreCase(p2.getName());
		}
	};
	/** Compares its two arguments for order by it's up-time, the one that logged first. */
	public static final Comparator<L2PcInstance> PLAYER_UPTIME_COMPARATOR = new Comparator<L2PcInstance>()
	{
		@Override
		public int compare(L2PcInstance p1, L2PcInstance p2)
		{
			return Long.compare(p1.getUptime(), p2.getUptime());
		}
	};
	/** Compares its two arguments for order by it's PVP kills. */
	public static final Comparator<L2PcInstance> PLAYER_PVP_COMPARATOR = new Comparator<L2PcInstance>()
	{
		@Override
		public int compare(L2PcInstance p1, L2PcInstance p2)
		{
			return Integer.compare(p1.getPvpKills(), p2.getPvpKills());
		}
	};
	/** Compares its two arguments for order by it's PK kills. */
	public static final Comparator<L2PcInstance> PLAYER_PK_COMPARATOR = new Comparator<L2PcInstance>()
	{
		@Override
		public int compare(L2PcInstance p1, L2PcInstance p2)
		{
			return Integer.compare(p1.getPkKills(), p2.getPkKills());
		}
	};
}
