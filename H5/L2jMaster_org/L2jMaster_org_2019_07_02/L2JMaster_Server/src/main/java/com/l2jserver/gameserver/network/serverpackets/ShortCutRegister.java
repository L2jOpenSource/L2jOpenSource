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

import com.l2jserver.gameserver.model.Shortcut;

public final class ShortCutRegister extends L2GameServerPacket
{
	private final Shortcut _shortcut;
	
	/**
	 * Register new skill shortcut
	 * @param shortcut
	 */
	public ShortCutRegister(Shortcut shortcut)
	{
		_shortcut = shortcut;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x44);
		writeD(_shortcut.getType().ordinal());
		writeD(_shortcut.getSlot() + (_shortcut.getPage() * 12)); // C4 Client
		switch (_shortcut.getType())
		{
			case ITEM:
			{
				writeD(_shortcut.getId());
				writeD(_shortcut.getCharacterType());
				writeD(_shortcut.getSharedReuseGroup());
				writeD(0x00); // unknown
				writeD(0x00); // unknown
				writeD(0x00); // item augment id
				break;
			}
			case SKILL:
			{
				writeD(_shortcut.getId());
				writeD(_shortcut.getLevel());
				writeC(0x00); // C5
				writeD(_shortcut.getCharacterType());
				break;
			}
			case ACTION:
			case MACRO:
			case RECIPE:
			case BOOKMARK:
			{
				writeD(_shortcut.getId());
				writeD(_shortcut.getCharacterType());
			}
		}
	}
}
