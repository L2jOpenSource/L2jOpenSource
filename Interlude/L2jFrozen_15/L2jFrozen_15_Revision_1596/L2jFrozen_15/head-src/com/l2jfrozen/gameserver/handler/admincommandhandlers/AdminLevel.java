package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.xml.ExperienceData;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public class AdminLevel implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_add_level",
		"admin_set_level"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		final L2Object targetChar = activeChar.getTarget();
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken(); // Get actual command
		
		String val = "";
		if (st.countTokens() >= 1)
		{
			val = st.nextToken();
		}
		
		if (actualCommand.equalsIgnoreCase("admin_add_level"))
		{
			try
			{
				if (targetChar instanceof L2PlayableInstance)
				{
					((L2PlayableInstance) targetChar).getStat().addLevel(Byte.parseByte(val));
				}
			}
			catch (final NumberFormatException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				activeChar.sendMessage("Wrong Number Format");
			}
		}
		else if (actualCommand.equalsIgnoreCase("admin_set_level"))
		{
			try
			{
				if (targetChar == null || !(targetChar instanceof L2PlayableInstance))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT)); // incorrect
					return false;
				}
				
				final L2PlayableInstance targetPlayer = (L2PlayableInstance) targetChar;
				
				byte lvl = Byte.parseByte(val);
				byte min_level = 1;
				byte max_level = ExperienceData.getInstance().getMaxLevel();
				
				if (targetChar.isPlayer())
				{
					L2PcInstance player = (L2PcInstance) targetPlayer;
					if (player.isSubClassActive())
					{
						min_level = 40;
						max_level = Config.MAX_SUBCLASS_LEVEL;
						
						if (lvl < 40)
						{
							lvl = 40;
						}
					}
				}
				
				if (lvl >= min_level && lvl <= max_level)
				{
					targetPlayer.setLevel(lvl);
				}
				else
				{
					String text = String.format("You must specify level between %s and %s.", min_level, max_level);
					activeChar.sendMessage(text);
					return false;
				}
			}
			catch (final NumberFormatException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				activeChar.sendMessage("You must specify level between 1 and " + ExperienceData.getInstance().getMaxLevel() + ".");
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
