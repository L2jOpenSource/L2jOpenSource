package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.managers.AuctionManager;
import com.l2jfrozen.gameserver.managers.ClanHallManager;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.ClanHall;
import com.l2jfrozen.gameserver.model.zone.type.L2ClanHallZone;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

import javolution.text.TextBuilder;

public class AdminClanHall implements IAdminCommandHandler
{
	// private static Logger LOGGER = Logger.getLogger(AdminSiege.class);
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_clanhall",
		"admin_clanhallset",
		"admin_clanhalldel",
		"admin_clanhallopendoors",
		"admin_clanhallclosedoors",
		"admin_clanhallteleportself"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		command = st.nextToken(); // Get actual command
		
		ClanHall clanhall = null;
		
		if (command.startsWith("admin_clanhall"))
		{
			showClanHallList(activeChar);
		}
		
		if (st.hasMoreTokens())
		{
			clanhall = ClanHallManager.getInstance().getClanHallById(Integer.parseInt(st.nextToken()));
		}
		
		if (clanhall == null)
		{
			// No clanhall specified
			showClanHallList(activeChar);
		}
		else
		{
			L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			
			if (target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			
			if (command.equalsIgnoreCase("admin_clanhallset"))
			{
				if (player == null || player.getClan() == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				}
				else if (!ClanHallManager.getInstance().isFree(clanhall.getId()))
				{
					activeChar.sendMessage("This ClanHall isn't free!");
				}
				else if (player.getClan().getHasHideout() == 0)
				{
					ClanHallManager.getInstance().setOwner(clanhall.getId(), player.getClan());
					
					if (AuctionManager.getInstance().getAuction(clanhall.getId()) != null)
					{
						AuctionManager.getInstance().getAuction(clanhall.getId()).deleteAuctionFromDB();
					}
				}
				else
				{
					activeChar.sendMessage("You have already a ClanHall!");
				}
			}
			else if (command.equalsIgnoreCase("admin_clanhalldel"))
			{
				if (!ClanHallManager.getInstance().isFree(clanhall.getId()))
				{
					ClanHallManager.getInstance().setFree(clanhall.getId());
					AuctionManager.getInstance().initNPC(clanhall.getId());
				}
				else
				{
					activeChar.sendMessage("This ClanHall is already Free!");
				}
			}
			else if (command.equalsIgnoreCase("admin_clanhallopendoors"))
			{
				clanhall.openCloseDoors(true);
			}
			else if (command.equalsIgnoreCase("admin_clanhallclosedoors"))
			{
				clanhall.openCloseDoors(false);
			}
			else if (command.equalsIgnoreCase("admin_clanhallteleportself"))
			{
				L2ClanHallZone zone = clanhall.getZone();
				
				if (zone != null)
				{
					activeChar.teleToLocation(zone.getSpawn(), true);
				}
			}
			
			showClanHallInfo(activeChar, clanhall);
		}
		return true;
	}
	
	private void showClanHallList(L2PcInstance activeChar)
	{
		int i = 0;
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/clanhall_list.htm");
		TextBuilder cList = new TextBuilder();
		
		i = 0;
		
		for (ClanHall clanhall : ClanHallManager.getInstance().getClanHalls().values())
		{
			if (clanhall != null)
			{
				cList.append("<td fixwidth=134><a action=\"bypass -h admin_clanhall " + clanhall.getId() + "\">");
				cList.append(clanhall.getName() + "</a></td>");
				i++;
			}
			
			if (i > 1)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}
		
		adminReply.replace("%clanhalls%", cList.toString());
		cList.clear();
		i = 0;
		
		for (ClanHall clanhall : ClanHallManager.getInstance().getFreeClanHalls().values())
		{
			if (clanhall != null)
			{
				cList.append("<td fixwidth=134><a action=\"bypass -h admin_clanhall " + clanhall.getId() + "\">");
				cList.append(clanhall.getName() + "</a></td>");
				i++;
			}
			
			if (i > 1)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}
		adminReply.replace("%freeclanhalls%", cList.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showClanHallInfo(L2PcInstance activeChar, ClanHall clanhall)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/clanhall.htm");
		adminReply.replace("%clanhallName%", clanhall.getName());
		adminReply.replace("%clanhallId%", String.valueOf(clanhall.getId()));
		L2Clan owner = ClanTable.getInstance().getClan(clanhall.getOwnerId());
		
		if (owner == null)
		{
			adminReply.replace("%clanhallOwner%", "None");
		}
		else
		{
			adminReply.replace("%clanhallOwner%", owner.getName());
		}
		
		activeChar.sendPacket(adminReply);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
}
