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

import java.util.Map.Entry;

import com.l2jserver.gameserver.model.L2PremiumItem;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Gnacik
 */
public class ExGetPremiumItemList extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	
	public ExGetPremiumItemList(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x86);
		writeD(_activeChar.getPremiumItemList().size());
		for (Entry<Integer, L2PremiumItem> entry : _activeChar.getPremiumItemList().entrySet())
		{
			L2PremiumItem item = entry.getValue();
			writeD(entry.getKey());
			writeD(_activeChar.getObjectId());
			writeD(item.getItemId());
			writeQ(item.getCount());
			writeD(0x00); // ?
			writeS(item.getSender());
		}
	}
}
