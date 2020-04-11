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

public final class ShortCutRegister extends L2GameServerPacket
{
	private final L2ShortCut _shortcut;
	
	/**
	 * Register new skill shortcut
	 * @param shortcut
	 */
	public ShortCutRegister(L2ShortCut shortcut)
	{
		_shortcut = shortcut;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x44);
		
		writeD(_shortcut.getType());
		writeD(_shortcut.getSlot() + (_shortcut.getPage() * 12)); // C4 Client
		switch (_shortcut.getType())
		{
			case L2ShortCut.TYPE_ITEM: // 1
				writeD(_shortcut.getId());
				writeD(_shortcut.getCharacterType());
				writeD(_shortcut.getSharedReuseGroup());
				writeD(0x00); // unknown
				writeD(0x00); // unknown
				writeD(0x00); // item augment id
				break;
			case L2ShortCut.TYPE_SKILL: // 2
				writeD(_shortcut.getId());
				writeD(_shortcut.getLevel());
				writeC(0x00); // C5
				writeD(_shortcut.getCharacterType());
				break;
			//@formatter:off
				/** these are same as default case, no need to duplicate, enable if packet get changed
				 */
				/*	case L2ShortCut.TYPE_ACTION: //3
				 *		writeD(_shortcut.getId());
				 *		writeD(_shortcut.getUserCommand());
				 *		break;
				 *	case L2ShortCut.TYPE_MACRO: //4
				 *		writeD(_shortcut.getId());
				 *		writeD(_shortcut.getUserCommand());
				 *		break;
				 *	case L2ShortCut.TYPE_RECIPE: //5
				 *		writeD(_shortcut.getId());
				 *		writeD(_shortcut.getUserCommand());
				 *		break;
				 */
				//@formatter:on
			default:
			{
				writeD(_shortcut.getId());
				writeD(_shortcut.getCharacterType());
			}
		}
	}
}
