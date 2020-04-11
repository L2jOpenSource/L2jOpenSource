package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.managers.QuestManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author luisantonioa
 */
public class AdminQuest implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_quest_reload"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		// syntax will either be:
		// quest_reload <id>
		// quest_reload <questName>
		// The questName MUST start with a non-numeric character for this to work,
		// regardless which of the two formats is used.
		// Example: //quest_reload orc_occupation_change_1
		// Example: //quest_reload chests
		// Example: //quest_reload SagasSuperclass
		// Example: //quest_reload 12
		if (command.startsWith("admin_quest_reload"))
		{
			String[] parts = command.split(" ");
			
			if (parts.length < 2)
			{
				activeChar.sendMessage("Syntax: //quest_reload <questFolder>.<questSubFolders...>.questName> or //quest_reload <id>");
			}
			else
			{
				// try the first param as id
				try
				{
					final int questId = Integer.parseInt(parts[1]);
					
					if (QuestManager.getInstance().reload(questId))
					{
						activeChar.sendMessage("Quest Reloaded Successfully.");
					}
					else
					{
						activeChar.sendMessage("Quest Reloaded Failed");
					}
				}
				catch (final NumberFormatException e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
					if (QuestManager.getInstance().reload(parts[1]))
					{
						activeChar.sendMessage("Quest Reloaded Successfully.");
					}
					else
					{
						activeChar.sendMessage("Quest Reloaded Failed");
					}
				}
			}
			
			parts = null;
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
}
