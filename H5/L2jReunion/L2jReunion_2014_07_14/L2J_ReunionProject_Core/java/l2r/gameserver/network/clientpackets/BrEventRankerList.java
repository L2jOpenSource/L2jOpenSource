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
package l2r.gameserver.network.clientpackets;

import l2r.gameserver.network.serverpackets.ExBrLoadEventTopRankers;

/**
 * Halloween rank list client packet. Format: (ch)ddd
 */
public class BrEventRankerList extends L2GameClientPacket
{
	private static final String _C__D0_7B_BREVENTRANKERLIST = "[C] D0:7B BrEventRankerList";
	
	private int _eventId;
	private int _day;
	@SuppressWarnings("unused")
	private int _ranking;
	
	@Override
	protected void readImpl()
	{
		_eventId = readD();
		_day = readD(); // 0 - current, 1 - previous
		_ranking = readD();
	}
	
	@Override
	protected void runImpl()
	{
		// TODO count, bestScore, myScore
		int count = 0;
		int bestScore = 0;
		int myScore = 0;
		getClient().sendPacket(new ExBrLoadEventTopRankers(_eventId, _day, count, bestScore, myScore));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_7B_BREVENTRANKERLIST;
	}
}
