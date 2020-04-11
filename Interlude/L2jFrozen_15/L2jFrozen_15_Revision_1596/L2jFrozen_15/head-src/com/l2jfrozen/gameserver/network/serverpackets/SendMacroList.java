package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Macro;

/**
 * packet type id 0xe7 sample e7 d // unknown change of Macro edit,add,delete c // unknown c //count of Macros c // unknown d // id S // macro name S // desc S // acronym c // icon c // count c // entry c // type d // skill id c // shortcut id S // command name format: cdhcdSSScc (ccdcS)
 */
public class SendMacroList extends L2GameServerPacket
{
	private final int rev;
	private final int count;
	private final L2Macro macro;
	
	public SendMacroList(final int rev, final int count, final L2Macro macro)
	{
		this.rev = rev;
		this.count = count;
		this.macro = macro;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xE7);
		
		writeD(rev); // macro change revision (changes after each macro edition)
		writeC(0); // unknown
		writeC(count); // count of Macros
		writeC(macro != null ? 1 : 0); // unknown
		
		if (macro != null)
		{
			writeD(macro.id); // Macro ID
			writeS(macro.name); // Macro Name
			writeS(macro.descr); // Desc
			writeS(macro.acronym); // acronym
			writeC(macro.icon); // icon
			
			writeC(macro.commands.length); // count
			
			for (int i = 0; i < macro.commands.length; i++)
			{
				final L2Macro.L2MacroCmd cmd = macro.commands[i];
				writeC(i + 1); // i of count
				writeC(cmd.type); // type 1 = skill, 3 = action, 4 = shortcut
				writeD(cmd.d1); // skill id
				writeC(cmd.d2); // shortcut id
				writeS(cmd.cmd); // command name
			}
		}
		
		// writeD(1); //unknown change of Macro edit,add,delete
		// writeC(0); //unknown
		// writeC(1); //count of Macros
		// writeC(1); //unknown
		//
		// writeD(1430); //Macro ID
		// writeS("Admin"); //Macro Name
		// writeS("Admin Command"); //Desc
		// writeS("ADM"); //acronym
		// writeC(0); //icon
		// writeC(2); //count
		//
		// writeC(1); //i of count
		// writeC(3); //type 1 = skill, 3 = action, 4 = shortcut
		// writeD(0); // skill id
		// writeC(0); // shortcut id
		// writeS("/loc"); // command name
		//
		// writeC(2); //i of count
		// writeC(3); //type 1 = skill, 3 = action, 4 = shortcut
		// writeD(0); // skill id
		// writeC(0); // shortcut id
		// writeS("//admin"); // command name
		
	}
	
	@Override
	public String getType()
	{
		return "[S] E7 SendMacroList";
	}
	
}
