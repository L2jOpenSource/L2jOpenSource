package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ControllableMobInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - kill = kills target L2Character - kill_monster = kills target non-player - kill <radius> = If radius is specified, then ALL players only in that radius will be killed. - kill_monster <radius> = If radius is specified, then ALL non-players only in
 * that radius will be killed.
 * @version $Revision: 1.2.4.5 $ $Date: 2007/07/31 10:06:06 $
 */
public class AdminKill implements IAdminCommandHandler
{
	private static Logger LOGGER = Logger.getLogger(AdminKill.class);
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_kill",
		"admin_kill_monster"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.startsWith("admin_kill"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // skip command
			
			if (st.hasMoreTokens())
			{
				String firstParam = st.nextToken();
				L2PcInstance plyr = L2World.getInstance().getPlayer(firstParam);
				
				if (plyr != null)
				{
					if (st.hasMoreTokens())
					{
						try
						{
							final int radius = Integer.parseInt(st.nextToken());
							
							for (final L2Character knownChar : plyr.getKnownList().getKnownCharactersInRadius(radius))
							{
								if (knownChar == null || knownChar instanceof L2ControllableMobInstance || knownChar.equals(activeChar))
								{
									continue;
								}
								
								kill(activeChar, knownChar);
							}
							
							activeChar.sendMessage("Killed all characters within a " + radius + " unit radius.");
							
							return true;
						}
						catch (final NumberFormatException e)
						{
							if (Config.ENABLE_ALL_EXCEPTIONS)
							{
								e.printStackTrace();
							}
							
							activeChar.sendMessage("Invalid radius.");
							return false;
						}
					}
					kill(activeChar, plyr);
				}
				else
				{
					try
					{
						final int radius = Integer.parseInt(firstParam);
						
						for (final L2Character knownChar : activeChar.getKnownList().getKnownCharactersInRadius(radius))
						{
							if (knownChar == null || knownChar instanceof L2ControllableMobInstance || knownChar.equals(activeChar))
							{
								continue;
							}
							
							kill(activeChar, knownChar);
						}
						
						activeChar.sendMessage("Killed all characters within a " + radius + " unit radius.");
						
						return true;
					}
					catch (final NumberFormatException e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e.printStackTrace();
						}
						
						activeChar.sendMessage("Usage: //kill <player_name | radius>");
						return false;
					}
				}
				
				firstParam = null;
				plyr = null;
			}
			else
			{
				L2Object obj = activeChar.getTarget();
				
				if (obj == null || obj instanceof L2ControllableMobInstance || !(obj instanceof L2Character))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
				}
				else
				{
					kill(activeChar, (L2Character) obj);
				}
				
				obj = null;
			}
			
			st = null;
		}
		
		return true;
	}
	
	private void kill(final L2PcInstance activeChar, final L2Character target)
	{
		if (target instanceof L2PcInstance)
		{
			// e.g. invincibility effect
			if (!((L2PcInstance) target).isGM())
			{
				target.stopAllEffects();
			}
			
			target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar);
		}
		else if (Config.L2JMOD_CHAMPION_ENABLE && target.isChampion())
		{
			target.reduceCurrentHp(target.getMaxHp() * Config.L2JMOD_CHAMPION_HP + 1, activeChar);
		}
		else
		{
			target.reduceCurrentHp(target.getMaxHp() + 1, activeChar);
		}
		
		if (Config.DEBUG)
		{
			LOGGER.debug("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ")" + " killed character " + target.getObjectId());
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
