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
package l2r.gameserver.network.serverpackets;

/**
 * @author mochitto
 */
public class ExNevitAdventTimeChange extends L2GameServerPacket
{
	private final boolean _paused;
	private final int _time;
	
	public ExNevitAdventTimeChange(int time)
	{
		_time = time > 240000 ? 240000 : time;
		_paused = _time < 1;
	}
	
	// Add NevitAdvent by pmq Start
	public ExNevitAdventTimeChange(int time, boolean paused)
	{
		_time = time;
		_paused = paused;
	}
	
	// Add NevitAdvent by pmq End
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xE1);
		// state 0 - pause 1 - started
		writeC(_paused ? 0x01 : 0x00);
		// left time in ms max is 16000 its 4m and state is automatically changed to quit
		writeD(_time);
	}
}
