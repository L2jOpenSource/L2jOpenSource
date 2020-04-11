package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Macro;
import com.l2jfrozen.gameserver.model.L2Macro.L2MacroCmd;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestMakeMacro extends L2GameClientPacket
{
	private final Logger LOGGER = Logger.getLogger(RequestMakeMacro.class);
	private L2Macro macro;
	private int commandsLenght = 0;
	private static final int MAX_MACRO_LENGTH = 12;
	
	/**
	 * packet type id 0xc1 sample c1 d // id S // macro name S // unknown desc S // unknown acronym c // icon c // count c // entry c // type d // skill id c // shortcut id S // command name format: cdSSScc (ccdcS)
	 */
	@Override
	protected void readImpl()
	{
		final int id = readD();
		final String name = readS();
		final String desc = readS();
		final String acronym = readS();
		final int icon = readC();
		int count = readC();
		if (count < 0)
		{
			count = 0;
			return;
		}
		if (count > MAX_MACRO_LENGTH)
		{
			count = MAX_MACRO_LENGTH;
		}
		
		final L2MacroCmd[] commands = new L2MacroCmd[count];
		if (Config.DEBUG)
		{
			LOGGER.info("Make macro id:" + id + "\tname:" + name + "\tdesc:" + desc + "\tacronym:" + acronym + "\ticon:" + icon + "\tcount:" + count);
		}
		for (int i = 0; i < count; i++)
		{
			final int entry = readC();
			final int type = readC(); // 1 = skill, 3 = action, 4 = shortcut
			final int d1 = readD(); // skill or page number for shortcuts
			final int d2 = readC();
			final String command = readS();
			commandsLenght += command.length() + 1;
			commands[i] = new L2MacroCmd(entry, type, d1, d2, command);
			if (Config.DEBUG)
			{
				LOGGER.info("entry:" + entry + "\ttype:" + type + "\td1:" + d1 + "\td2:" + d2 + "\tcommand:" + command);
			}
		}
		macro = new L2Macro(id, icon, name, desc, acronym, commands);
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		// Macro exploit fix
		if (!getClient().getFloodProtectors().getMacro().tryPerformAction("make macro"))
		{
			return;
		}
		
		if (commandsLenght > 255)
		{
			// Invalid macro. Refer to the Help file for instructions.
			player.sendPacket(new SystemMessage(SystemMessageId.INVALID_MACRO));
			return;
		}
		
		if (player.getMacroses().getAllMacroses().length > 24)
		{
			// You may create up to 24 macros.
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_CREATE_UP_TO_24_MACROS));
			return;
		}
		
		if (macro.name.length() == 0)
		{
			// Enter the name of the macro.
			player.sendPacket(new SystemMessage(SystemMessageId.ENTER_THE_MACRO_NAME));
			return;
		}
		
		if (macro.descr.length() > 32)
		{
			// Macro descriptions may contain up to 32 characters.
			player.sendPacket(new SystemMessage(SystemMessageId.MACRO_DESCRIPTION_MAX_32_CHARS));
			return;
		}
		
		// Security Check
		for (final L2MacroCmd command : macro.commands)
		{
			
			if (!checkSecurityOnCommand(command))
			{
				
				// Invalid macro. Refer to the Help file for instructions.
				player.sendPacket(new SystemMessage(SystemMessageId.INVALID_MACRO));
				player.sendMessage("SecurityCheck: not more then 2x ',' or 2x ';' in the same command");
				return;
				
			}
			
		}
		
		player.registerMacro(macro);
	}
	
	private boolean checkSecurityOnCommand(final L2MacroCmd cmd)
	{
		
		// not more then 2x ;
		if (cmd.cmd != null && cmd.cmd.split(";").length > 2)
		{
			return false;
		}
		
		// not more then 2x ,
		if (cmd.cmd != null && cmd.cmd.split(",").length > 2)
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public String getType()
	{
		return "[C] C1 RequestMakeMacro";
	}
}
