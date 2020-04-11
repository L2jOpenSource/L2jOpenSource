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

import l2r.gameserver.model.L2ShortCut;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public final class ShortCutInit extends L2GameServerPacket
{
	private L2ShortCut[] _shortCuts;
	private L2PcInstance _activeChar;
	
	public ShortCutInit(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
		
		if (_activeChar == null)
		{
			return;
		}
		
		_shortCuts = _activeChar.getAllShortCuts();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x45);
		writeD(_shortCuts.length);
		
		for (L2ShortCut sc : _shortCuts)
		{
			writeD(sc.getType());
			writeD(sc.getSlot() + (sc.getPage() * 12));
			
			switch (sc.getType())
			{
				case L2ShortCut.TYPE_ITEM: // 1
					writeD(sc.getId());
					writeD(0x01);
					writeD(sc.getSharedReuseGroup());
					writeD(0x00);
					writeD(0x00);
					writeH(0x00);
					writeH(0x00);
					break;
				case L2ShortCut.TYPE_SKILL: // 2
					writeD(sc.getId());
					writeD(sc.getLevel());
					writeC(0x00); // C5
					writeD(0x01); // C6
					break;
				case L2ShortCut.TYPE_ACTION: // 3
					writeD(sc.getId());
					writeD(0x01); // C6
					break;
				case L2ShortCut.TYPE_MACRO: // 4
					writeD(sc.getId());
					writeD(0x01); // C6
					break;
				case L2ShortCut.TYPE_RECIPE: // 5
					writeD(sc.getId());
					writeD(0x01); // C6
					break;
				default:
					writeD(sc.getId());
					writeD(0x01); // C6
			}
		}
	}
}
