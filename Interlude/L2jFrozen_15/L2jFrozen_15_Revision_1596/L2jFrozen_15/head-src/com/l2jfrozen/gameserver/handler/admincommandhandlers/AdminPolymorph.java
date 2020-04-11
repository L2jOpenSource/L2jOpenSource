package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.SetupGauge;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: polymorph
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2007/07/31 10:05:56 $
 */

public class AdminPolymorph implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_polymorph",
		"admin_unpolymorph",
		"admin_polymorph_menu",
		"admin_unpolymorph_menu"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.startsWith("admin_polymorph"))
		{
			StringTokenizer st = new StringTokenizer(command);
			L2Object target = activeChar.getTarget();
			
			try
			{
				st.nextToken();
				String p1 = st.nextToken();
				
				if (st.hasMoreTokens())
				{
					String p2 = st.nextToken();
					doPolymorph(activeChar, target, p2, p1);
					p2 = null;
				}
				else
				{
					doPolymorph(activeChar, target, p1, "npc");
				}
				
				p1 = null;
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				activeChar.sendMessage("Usage: //polymorph [type] <id>");
			}
			
			target = null;
			st = null;
		}
		else if (command.equals("admin_unpolymorph"))
		{
			doUnpoly(activeChar, activeChar.getTarget());
		}
		
		if (command.contains("menu"))
		{
			showMainPage(activeChar);
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	/**
	 * @param activeChar
	 * @param obj
	 * @param id
	 * @param type
	 */
	private void doPolymorph(final L2PcInstance activeChar, final L2Object obj, final String id, final String type)
	{
		if (obj != null)
		{
			obj.getPoly().setPolyInfo(type, id);
			
			// animation
			if (obj instanceof L2Character)
			{
				L2Character Char = (L2Character) obj;
				MagicSkillUser msk = new MagicSkillUser(Char, 1008, 1, 4000, 0);
				Char.broadcastPacket(msk);
				SetupGauge sg = new SetupGauge(0, 4000);
				Char.sendPacket(sg);
				Char = null;
				sg = null;
				msk = null;
			}
			
			// end of animation
			obj.decayMe();
			obj.spawnMe(obj.getX(), obj.getY(), obj.getZ());
			activeChar.sendMessage("Polymorph succeed");
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
		}
	}
	
	/**
	 * @param activeChar
	 * @param target
	 */
	private void doUnpoly(final L2PcInstance activeChar, final L2Object target)
	{
		if (target != null)
		{
			target.getPoly().setPolyInfo(null, "1");
			target.decayMe();
			target.spawnMe(target.getX(), target.getY(), target.getZ());
			activeChar.sendMessage("Unpolymorph succeed");
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
		}
	}
	
	private void showMainPage(final L2PcInstance activeChar)
	{
		AdminHelpPage.showHelpPage(activeChar, "effects_menu.htm");
	}
}
