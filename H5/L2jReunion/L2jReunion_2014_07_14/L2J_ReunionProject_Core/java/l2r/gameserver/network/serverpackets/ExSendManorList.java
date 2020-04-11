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

import java.util.List;

import javolution.util.FastList;

/**
 * @author l3x
 */
public class ExSendManorList extends L2GameServerPacket
{
	private final List<String> _manors;
	
	public ExSendManorList(FastList<String> manors)
	{
		_manors = manors;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x22);
		writeD(_manors.size());
		int i = 1;
		for (String manor : _manors)
		{
			writeD(i++);
			writeS(manor);
		}
	}
}