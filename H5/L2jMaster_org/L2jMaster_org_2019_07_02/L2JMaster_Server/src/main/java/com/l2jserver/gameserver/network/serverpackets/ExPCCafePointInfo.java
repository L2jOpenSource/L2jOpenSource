/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.network.serverpackets;

/**
 * @author KenM
 * @author UnAfraid
 */
public class ExPCCafePointInfo extends L2GameServerPacket
{
	private final int _points;
	private final int _mAddPoint;
	private final int _mPeriodType;
	private final int _remainTime;
	private final int _pointType;
	private final int _time;
	
	public ExPCCafePointInfo()
	{
		_points = 0;
		_mAddPoint = 0;
		_remainTime = 0;
		_mPeriodType = 0;
		_pointType = 0;
		_time = 0;
	}
	
	public ExPCCafePointInfo(int points, int pointsToAdd, int time)
	{
		_points = points;
		_mAddPoint = pointsToAdd;
		_mPeriodType = 1;
		_remainTime = 42; // No idea why but retail sends 42..
		_pointType = pointsToAdd < 0 ? 3 : 0; // When using points is 3
		_time = time;
		
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x32);
		writeD(_points); // num points
		writeD(_mAddPoint); // points inc display
		writeC(_mPeriodType); // period(0=don't show window,1=acquisition,2=use points)
		writeD(_remainTime); // period hours left
		writeC(_pointType); // points inc display color(0=yellow, 1=cyan-blue, 2=red, all other black)
		writeD(_time * 3); // value is in seconds * 3
	}
}
