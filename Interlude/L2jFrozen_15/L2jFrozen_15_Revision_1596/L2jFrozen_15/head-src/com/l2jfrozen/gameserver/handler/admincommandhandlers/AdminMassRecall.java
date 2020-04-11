package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class handles following admin commands: - recallparty - recallclan - recallally
 * @author Yamaneko
 */
public class AdminMassRecall implements IAdminCommandHandler
{
	private static String[] adminCommands =
	{
		"admin_recallclan",
		"admin_recallparty",
		"admin_recallally"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.startsWith("admin_recallclan"))
		{
			try
			{
				String val = command.substring(17).trim();
				
				L2Clan clan = ClanTable.getInstance().getClanByName(val);
				
				if (clan == null)
				{
					activeChar.sendMessage("This clan doesn't exists.");
					return true;
				}
				
				val = null;
				L2PcInstance[] m = clan.getOnlineMembers("");
				
				for (final L2PcInstance element : m)
				{
					Teleport(element, activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
				}
				
				clan = null;
				m = null;
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				activeChar.sendMessage("Error in recallclan command.");
			}
		}
		else if (command.startsWith("admin_recallally"))
		{
			try
			{
				String val = command.substring(17).trim();
				L2Clan clan = ClanTable.getInstance().getClanByName(val);
				
				if (clan == null)
				{
					activeChar.sendMessage("This clan doesn't exists.");
					return true;
				}
				
				final int ally = clan.getAllyId();
				
				if (ally == 0)
				{
					
					L2PcInstance[] m = clan.getOnlineMembers("");
					
					for (final L2PcInstance element : m)
					{
						Teleport(element, activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
					}
					
					m = null;
				}
				else
				{
					for (final L2Clan aclan : ClanTable.getInstance().getClans())
					{
						if (aclan.getAllyId() == ally)
						{
							L2PcInstance[] m = aclan.getOnlineMembers("");
							
							for (final L2PcInstance element : m)
							{
								Teleport(element, activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
							}
							
							m = null;
						}
					}
				}
				
				clan = null;
				val = null;
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				activeChar.sendMessage("Error in recallally command.");
			}
		}
		else if (command.startsWith("admin_recallparty"))
		{
			try
			{
				String val = command.substring(18).trim();
				L2PcInstance player = L2World.getInstance().getPlayer(val);
				
				if (player == null)
				{
					activeChar.sendMessage("Target error.");
					return true;
				}
				
				if (!player.isInParty())
				{
					activeChar.sendMessage("Player is not in party.");
					return true;
				}
				
				L2Party p = player.getParty();
				
				for (final L2PcInstance ppl : p.getPartyMembers())
				{
					Teleport(ppl, activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
				}
				
				p = null;
				val = null;
				player = null;
				
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				activeChar.sendMessage("Error in recallparty command.");
			}
		}
		return true;
	}
	
	private void Teleport(final L2PcInstance player, final int X, final int Y, final int Z, final String Message)
	{
		player.sendMessage(Message);
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.teleToLocation(X, Y, Z, true);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return adminCommands;
	}
}
