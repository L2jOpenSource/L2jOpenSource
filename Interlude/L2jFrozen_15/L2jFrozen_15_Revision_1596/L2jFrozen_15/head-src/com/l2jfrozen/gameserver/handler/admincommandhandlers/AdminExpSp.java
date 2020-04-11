package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands:
 * <li>add_exp_sp_to_character <i>shows menu for add or remove</i>
 * <li>add_exp_sp exp sp <i>Adds exp & sp to target, displays menu if a parameter is missing</i>
 * <li>remove_exp_sp exp sp <i>Removes exp & sp from target, displays menu if a parameter is missing</i>
 * @version $Revision: 1.2.4.6 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminExpSp implements IAdminCommandHandler
{
	private static Logger LOGGER = Logger.getLogger(AdminExpSp.class);
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_add_exp_sp_to_character",
		"admin_add_exp_sp",
		"admin_remove_exp_sp"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.startsWith("admin_add_exp_sp"))
		{
			try
			{
				String val = command.substring(16);
				
				if (!adminAddExpSp(activeChar, val))
				{
					activeChar.sendMessage("Usage: //add_exp_sp exp sp");
				}
				
				val = null;
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				// Case of missing parameter
				activeChar.sendMessage("Usage: //add_exp_sp exp sp");
			}
		}
		else if (command.startsWith("admin_remove_exp_sp"))
		{
			try
			{
				String val = command.substring(19);
				
				if (!adminRemoveExpSP(activeChar, val))
				{
					activeChar.sendMessage("Usage: //remove_exp_sp exp sp");
				}
				
				val = null;
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				// Case of missing parameter
				activeChar.sendMessage("Usage: //remove_exp_sp exp sp");
			}
		}
		
		addExpSp(activeChar);
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void addExpSp(final L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/expsp.htm");
		adminReply.replace("%name%", player.getName());
		adminReply.replace("%level%", String.valueOf(player.getLevel()));
		adminReply.replace("%xp%", String.valueOf(player.getExp()));
		adminReply.replace("%sp%", String.valueOf(player.getSp()));
		adminReply.replace("%class%", player.getTemplate().className);
		activeChar.sendPacket(adminReply);
		
		adminReply = null;
		player = null;
		target = null;
	}
	
	private boolean adminAddExpSp(final L2PcInstance activeChar, final String ExpSp)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return false;
		}
		
		target = null;
		
		StringTokenizer st = new StringTokenizer(ExpSp);
		
		if (st.countTokens() != 2)
		{
			return false;
		}
		
		String exp = st.nextToken();
		String sp = st.nextToken();
		
		long expval = 0;
		int spval = 0;
		
		try
		{
			expval = Long.parseLong(exp);
			spval = Integer.parseInt(sp);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return false;
		}
		
		if (expval != 0 || spval != 0)
		{
			// Common character information
			player.sendMessage("Admin is adding you " + expval + " xp and " + spval + " sp.");
			player.addExpAndSp(expval, spval);
			// Admin information
			activeChar.sendMessage("Added " + expval + " xp and " + spval + " sp to " + player.getName() + ".");
			
			if (Config.DEBUG)
			{
				LOGGER.debug("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") added " + expval + " xp and " + spval + " sp to " + player.getObjectId() + ".");
			}
		}
		
		exp = null;
		sp = null;
		
		st = null;
		player = null;
		
		return true;
	}
	
	private boolean adminRemoveExpSP(final L2PcInstance activeChar, final String ExpSp)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return false;
		}
		
		target = null;
		
		StringTokenizer st = new StringTokenizer(ExpSp);
		
		if (st.countTokens() != 2)
		{
			return false;
		}
		
		String exp = st.nextToken();
		String sp = st.nextToken();
		
		long expval = 0;
		int spval = 0;
		
		try
		{
			expval = Long.parseLong(exp);
			spval = Integer.parseInt(sp);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return false;
		}
		
		if (expval != 0 || spval != 0)
		{
			// Common character information
			player.sendMessage("Admin is removing you " + expval + " xp and " + spval + " sp.");
			player.removeExpAndSp(expval, spval);
			// Admin information
			activeChar.sendMessage("Removed " + expval + " xp and " + spval + " sp from " + player.getName() + ".");
			
			if (Config.DEBUG)
			{
				LOGGER.debug("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") removed " + expval + " xp and " + spval + " sp from " + player.getObjectId() + ".");
			}
		}
		
		exp = null;
		sp = null;
		
		player = null;
		st = null;
		
		return true;
	}
}
