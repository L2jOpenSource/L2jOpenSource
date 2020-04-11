package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.SiegeManager;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

import javolution.text.TextBuilder;

public class AdminSiege implements IAdminCommandHandler
{
	// private static Logger LOGGER = Logger.getLogger(AdminSiege.class);
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_siege",
		"admin_add_attacker",
		"admin_add_defender",
		"admin_add_guard",
		"admin_list_siege_clans",
		"admin_clear_siege_list",
		"admin_move_defenders",
		"admin_spawn_doors",
		"admin_startsiege",
		"admin_endsiege",
		"admin_setcastle",
		"admin_removecastle",
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		command = st.nextToken(); // Get actual command
		
		// Get castle
		Castle castle = null;
		
		if (st.hasMoreTokens())
		{
			castle = CastleManager.getInstance().getCastle(st.nextToken());
		}
		
		// Get castle
		String val = "";
		
		if (st.hasMoreTokens())
		{
			val = st.nextToken();
		}
		
		if (castle == null)
		{
			// No castle specified
			showCastleList(activeChar);
		}
		else if (castle.getCastleId() < 0)
		{
			// No castle specified
			showCastleList(activeChar);
		}
		else
		{
			L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			
			if (target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			
			if (command.equalsIgnoreCase("admin_add_attacker"))
			{
				if (player == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				}
				else if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), castle.getCastleId()))
				{
					activeChar.sendMessage("Clan is already registered!");
				}
				else
				{
					castle.getSiege().registerAttacker(player, true);
				}
			}
			else if (command.equalsIgnoreCase("admin_add_defender"))
			{
				if (player == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				}
				else
				{
					castle.getSiege().registerDefender(player, true);
				}
			}
			else if (command.equalsIgnoreCase("admin_add_guard"))
			{
				try
				{
					final int npcId = Integer.parseInt(val);
					castle.getSiege().getSiegeGuardManager().addSiegeGuard(activeChar, npcId);
				}
				catch (final Exception e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
					activeChar.sendMessage("Usage: //add_guard npcId");
				}
			}
			else if (command.equalsIgnoreCase("admin_clear_siege_list"))
			{
				castle.getSiege().clearSiegeClan();
			}
			else if (command.equalsIgnoreCase("admin_spawn_doors"))
			{
				castle.spawnDoor();
			}
			else if (command.equalsIgnoreCase("admin_startsiege"))
			{
				castle.getSiege().startSiege();
			}
			else if (command.equalsIgnoreCase("admin_endsiege"))
			{
				castle.getSiege().endSiege();
			}
			else if (command.equalsIgnoreCase("admin_list_siege_clans"))
			{
				castle.getSiege().listRegisterClan(activeChar);
				
				return true;
			}
			else if (command.equalsIgnoreCase("admin_move_defenders"))
			{
				activeChar.sendPacket(SystemMessage.sendString("Not implemented yet."));
			}
			else if (command.equalsIgnoreCase("admin_setcastle"))
			{
				if (player == null || player.getClan() == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				}
				else
				{
					castle.setOwner(player.getClan());
				}
			}
			else if (command.equalsIgnoreCase("admin_removecastle"))
			{
				L2Clan clan = ClanTable.getInstance().getClan(castle.getOwnerId());
				
				if (clan != null)
				{
					castle.removeOwner(clan);
				}
				else
				{
					activeChar.sendMessage("Unable to remove castle");
				}
			}
			else
			{
				showCastleInfo(activeChar, castle.getName());
			}
		}
		return true;
	}
	
	private void showCastleList(L2PcInstance activeChar)
	{
		int i = 0;
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/castles.htm");
		TextBuilder cList = new TextBuilder();
		
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle != null)
			{
				String name = castle.getName();
				cList.append("<td fixwidth=90><a action=\"bypass -h admin_siege " + name + "\">" + name + "</a></td>");
				i++;
				name = null;
			}
			
			if (i > 2)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}
		
		adminReply.replace("%castles%", cList.toString());
		cList.clear();
		i = 0;
		
		activeChar.sendPacket(adminReply);
		
	}
	
	private void showCastleInfo(L2PcInstance activeChar, String castleName)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/castle.htm");
		adminReply.replace("%castleName%", castleName);
		activeChar.sendPacket(adminReply);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
}
