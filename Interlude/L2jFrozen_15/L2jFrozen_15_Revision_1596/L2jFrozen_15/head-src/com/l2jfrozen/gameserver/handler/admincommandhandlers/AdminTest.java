package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author luisantonioa
 */
public class AdminTest implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_stats",
		"admin_mcrit",
		"admin_addbufftest",
		"admin_skill_test",
		"admin_st",
		"admin_mp",
		"admin_known",
		"admin_oly_obs_mode",
		"admin_obs_mode"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.equals("admin_stats"))
		{
			for (final String line : ThreadPoolManager.getInstance().getStats())
			{
				activeChar.sendMessage(line);
			}
		}
		if (command.equals("admin_mcrit"))
		{
			final L2Character target = (L2Character) activeChar.getTarget();
			
			activeChar.sendMessage("Activechar Mcrit " + activeChar.getMCriticalHit(null, null));
			activeChar.sendMessage("Activechar baseMCritRate " + activeChar.getTemplate().baseMCritRate);
			
			if (target != null)
			{
				activeChar.sendMessage("Target Mcrit " + target.getMCriticalHit(null, null));
				activeChar.sendMessage("Target baseMCritRate " + target.getTemplate().baseMCritRate);
			}
		}
		if (command.equals("admin_addbufftest"))
		{
			final L2Character target = (L2Character) activeChar.getTarget();
			activeChar.sendMessage("cast");
			
			final L2Skill skill = SkillTable.getInstance().getInfo(1085, 3);
			
			if (target != null)
			{
				activeChar.sendMessage("target locked");
				
				for (int i = 0; i < 100;)
				{
					if (activeChar.isCastingNow())
					{
						continue;
					}
					
					activeChar.sendMessage("Casting " + i);
					activeChar.useMagic(skill, false, false);
					i++;
				}
			}
		}
		else if (command.startsWith("admin_skill_test") || command.startsWith("admin_st"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				
				final int id = Integer.parseInt(st.nextToken());
				
				adminTestSkill(activeChar, id);
				
				st = null;
			}
			catch (NumberFormatException | NoSuchElementException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				activeChar.sendMessage("Command format is //skill_test <ID>");
			}
		}
		else if (command.equals("admin_mp on"))
		{
			// .startPacketMonitor();
			activeChar.sendMessage("command not working");
		}
		else if (command.equals("admin_mp off"))
		{
			// .stopPacketMonitor();
			activeChar.sendMessage("command not working");
		}
		else if (command.equals("admin_mp dump"))
		{
			// .dumpPacketHistory();
			activeChar.sendMessage("command not working");
		}
		else if (command.equals("admin_known on"))
		{
			Config.CHECK_KNOWN = true;
		}
		else if (command.equals("admin_known off"))
		{
			Config.CHECK_KNOWN = false;
		}
		else if (command.startsWith("admin_oly_obs_mode"))
		{
			if (!activeChar.inObserverMode())
			{
				activeChar.enterOlympiadObserverMode(activeChar.getX(), activeChar.getY(), activeChar.getZ(), -1);
			}
			else
			{
				activeChar.leaveOlympiadObserverMode();
			}
		}
		else if (command.startsWith("admin_obs_mode"))
		{
			if (!activeChar.inObserverMode())
			{
				activeChar.enterObserverMode(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			}
			else
			{
				activeChar.leaveObserverMode();
			}
		}
		return true;
	}
	
	/**
	 * @param activeChar
	 * @param id
	 */
	private void adminTestSkill(final L2PcInstance activeChar, final int id)
	{
		L2Character player;
		L2Object target = activeChar.getTarget();
		
		if (target == null || !(target instanceof L2Character))
		{
			player = activeChar;
		}
		else
		{
			player = (L2Character) target;
		}
		
		player.broadcastPacket(new MagicSkillUser(activeChar, player, id, 1, 1, 1));
		
		target = null;
		player = null;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}