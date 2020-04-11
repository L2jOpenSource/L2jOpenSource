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

import java.util.ArrayList;
import java.util.List;

import l2r.Config;
import l2r.gameserver.model.Macro;
import l2r.gameserver.model.MacroCmd;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;

/**
 * packet type id 0xc1 sample c1 d // id S // macro name S // unknown desc S // unknown acronym c // icon c // count c // entry c // type d // skill id c // shortcut id S // command name format: cdSSScc (ccdcS)
 */
public final class RequestMakeMacro extends L2GameClientPacket
{
	private static final String _C__CD_REQUESTMAKEMACRO = "[C] CD RequestMakeMacro";
	
	private Macro _macro;
	private int _commandsLenght = 0;
	
	private static final int MAX_MACRO_LENGTH = 12;
	
	@Override
	protected void readImpl()
	{
		int _id = readD();
		String _name = readS();
		String _desc = readS();
		String _acronym = readS();
		int _icon = readC();
		int _count = readC();
		if (_count > MAX_MACRO_LENGTH)
		{
			_count = MAX_MACRO_LENGTH;
		}
		
		if (Config.DEBUG)
		{
			_log.info("Make macro id:" + _id + "\tname:" + _name + "\tdesc:" + _desc + "\tacronym:" + _acronym + "\ticon:" + _icon + "\tcount:" + _count);
		}
		
		final List<MacroCmd> commands = new ArrayList<>(_count);
		for (int i = 0; i < _count; i++)
		{
			int entry = readC();
			int type = readC(); // 1 = skill, 3 = action, 4 = shortcut
			int d1 = readD(); // skill or page number for shortcuts
			int d2 = readC();
			String command = readS();
			_commandsLenght += command.length();
			commands.add(new MacroCmd(entry, type, d1, d2, command));
			if (Config.DEBUG)
			{
				_log.info("entry:" + entry + "\ttype:" + type + "\td1:" + d1 + "\td2:" + d2 + "\tcommand:" + command);
			}
		}
		_macro = new Macro(_id, _icon, _name, _desc, _acronym, commands);
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		if (_commandsLenght > 255)
		{
			// Invalid macro. Refer to the Help file for instructions.
			player.sendPacket(SystemMessageId.INVALID_MACRO);
			return;
		}
		if (player.getMacros().getAllMacroses().size() > 48)
		{
			// You may create up to 48 macros.
			player.sendPacket(SystemMessageId.YOU_MAY_CREATE_UP_TO_48_MACROS);
			return;
		}
		if (_macro.getName().isEmpty())
		{
			// Enter the name of the macro.
			player.sendPacket(SystemMessageId.ENTER_THE_MACRO_NAME);
			return;
		}
		if (_macro.getDescr().length() > 32)
		{
			// Macro descriptions may contain up to 32 characters.
			player.sendPacket(SystemMessageId.MACRO_DESCRIPTION_MAX_32_CHARS);
			return;
		}
		player.registerMacro(_macro);
	}
	
	@Override
	public String getType()
	{
		return _C__CD_REQUESTMAKEMACRO;
	}
}
