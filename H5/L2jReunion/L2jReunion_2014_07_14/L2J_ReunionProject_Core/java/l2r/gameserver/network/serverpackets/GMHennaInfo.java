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

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.L2Henna;

/**
 * This server packet sends the player's henna information using the Game Master's UI.
 * @author KenM, Zoey76
 */
public final class GMHennaInfo extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final List<L2Henna> _hennas = new ArrayList<>();
	
	public GMHennaInfo(L2PcInstance player)
	{
		_activeChar = player;
		for (L2Henna henna : _activeChar.getHennaList())
		{
			if (henna != null)
			{
				_hennas.add(henna);
			}
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xF0);
		writeC(_activeChar.getHennaStatINT()); // equip INT
		writeC(_activeChar.getHennaStatSTR()); // equip STR
		writeC(_activeChar.getHennaStatCON()); // equip CON
		writeC(_activeChar.getHennaStatMEN()); // equip MEN
		writeC(_activeChar.getHennaStatDEX()); // equip DEX
		writeC(_activeChar.getHennaStatWIT()); // equip WIT
		writeD(3); // Slots
		writeD(_hennas.size()); // Size
		for (L2Henna henna : _hennas)
		{
			writeD(henna.getDyeId());
			writeD(0x01);
		}
	}
}
