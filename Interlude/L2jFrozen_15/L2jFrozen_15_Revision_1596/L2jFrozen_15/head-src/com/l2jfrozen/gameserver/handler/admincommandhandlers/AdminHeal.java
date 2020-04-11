package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - heal = restores HP/MP/CP on target, name or radius
 * @version $Revision: 1.2.4.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminHeal implements IAdminCommandHandler
{
	private static Logger LOGGER = Logger.getLogger(AdminRes.class);
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_heal"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.equals("admin_heal"))
		{
			handleRes(activeChar);
		}
		else if (command.startsWith("admin_heal"))
		{
			try
			{
				String healTarget = command.substring(11);
				handleRes(activeChar, healTarget);
				healTarget = null;
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Incorrect target/radius specified.");
				activeChar.sendPacket(sm);
				sm = null;
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void handleRes(final L2PcInstance activeChar)
	{
		handleRes(activeChar, null);
	}
	
	private void handleRes(final L2PcInstance activeChar, final String player)
	{
		L2Object obj = activeChar.getTarget();
		
		if (player != null)
		{
			L2PcInstance plyr = L2World.getInstance().getPlayer(player);
			
			if (plyr != null)
			{
				obj = plyr;
			}
			else
			{
				try
				{
					final int radius = Integer.parseInt(player);
					for (final L2Object object : activeChar.getKnownList().getKnownObjects().values())
					{
						if (object instanceof L2Character)
						{
							L2Character character = (L2Character) object;
							character.setCurrentHpMp(character.getMaxHp(), character.getMaxMp());
							
							if (object instanceof L2PcInstance)
							{
								character.setCurrentCp(character.getMaxCp());
							}
							
							character = null;
						}
					}
					activeChar.sendMessage("Healed within " + radius + " unit radius.");
					return;
				}
				catch (final NumberFormatException nbe)
				{
					// ignore
				}
			}
			
			plyr = null;
		}
		
		if (obj == null)
		{
			obj = activeChar;
		}
		
		if (obj instanceof L2Character)
		{
			final L2Character target = (L2Character) obj;
			target.setCurrentHpMp(target.getMaxHp(), target.getMaxMp());
			
			if (target instanceof L2PcInstance)
			{
				target.setCurrentCp(target.getMaxCp());
			}
			
			if (Config.DEBUG)
			{
				LOGGER.debug("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") healed character " + target.getName());
			}
			
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
		}
	}
}
