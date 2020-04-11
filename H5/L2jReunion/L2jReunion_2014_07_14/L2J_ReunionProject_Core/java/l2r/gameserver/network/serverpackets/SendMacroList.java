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

import l2r.gameserver.model.Macro;
import l2r.gameserver.model.MacroCmd;

public class SendMacroList extends L2GameServerPacket
{
	private final int _rev;
	private final int _count;
	private final Macro _macro;
	
	public SendMacroList(int rev, int count, Macro macro)
	{
		_rev = rev;
		_count = count;
		_macro = macro;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xE8);
		
		writeD(_rev); // macro change revision (changes after each macro edition)
		writeC(0x00); // unknown
		writeC(_count); // count of Macros
		writeC(_macro != null ? 1 : 0); // unknown
		
		if (_macro != null)
		{
			writeD(_macro.getId()); // Macro ID
			writeS(_macro.getName()); // Macro Name
			writeS(_macro.getDescr()); // Desc
			writeS(_macro.getAcronym()); // acronym
			writeC(_macro.getIcon()); // icon
			
			writeC(_macro.getCommands().size()); // count
			
			int i = 1;
			for (MacroCmd cmd : _macro.getCommands())
			{
				writeC(i++); // command count
				writeC(cmd.getType()); // type 1 = skill, 3 = action, 4 = shortcut
				writeD(cmd.getD1()); // skill id
				writeC(cmd.getD2()); // shortcut id
				writeS(cmd.getCmd()); // command name
			}
		}
	}
}