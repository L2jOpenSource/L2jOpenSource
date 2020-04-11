package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

import net.sf.l2j.commons.lang.StringUtil;

import net.sf.l2j.gameserver.data.manager.ClanHallManager;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.enums.SpawnType;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.clanhall.Auction;
import net.sf.l2j.gameserver.model.clanhall.ClanHall;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * This class handles //ch admincommand.
 */
public class AdminClanHall implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_ch"
	};
	
	private static final String[] LOCS =
	{
		"Aden",
		"Dion",
		"Giran",
		"Gludin",
		"Gludio",
		"Goddard",
		"Oren",
		"Rune",
		"Schuttgart"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		try
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			
			int chId = 0;
			
			if (command.startsWith("admin_ch "))
			{
				if (st.hasMoreTokens())
					chId = Integer.parseInt(st.nextToken());
			}
			
			// Hold ClanHall reference, if not existing or chId is set to 0, we return the whole list of existing ClanHalls.
			final ClanHall ch = ClanHallManager.getInstance().getClanHall(chId);
			if (chId == 0 || ch == null)
			{
				showClanHallSelectPage(activeChar);
				return true;
			}
			
			// No command token, we show the individual ClanHall informations.
			if (!st.hasMoreTokens())
			{
				showClanHallSelectPage(activeChar, ch);
				return true;
			}
			
			command = st.nextToken();
			
			if (command.equalsIgnoreCase("set"))
			{
				// Return current Player target.
				final WorldObject target = activeChar.getTarget();
				Player playerTarget = null;
				if (target instanceof Player)
					playerTarget = (Player) target;
				
				if (playerTarget == null || playerTarget.getClan() == null)
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				else if (!ch.isFree())
					activeChar.sendMessage("This ClanHall isn't free.");
				else if (playerTarget.getClan().hasClanHall())
					activeChar.sendMessage("Your target already owns a ClanHall.");
				else
					ch.setOwner(playerTarget.getClan());
			}
			else if (command.equalsIgnoreCase("del"))
			{
				if (ch.isFree())
					activeChar.sendMessage("This ClanHall is already free.");
				else
					ch.free();
			}
			else if (command.equalsIgnoreCase("open"))
				ch.openCloseDoors(true);
			else if (command.equalsIgnoreCase("close"))
				ch.openCloseDoors(false);
			else if (command.equalsIgnoreCase("goto"))
				activeChar.teleportTo(ch.getRndSpawn(SpawnType.OWNER), 0);
			else if (command.equalsIgnoreCase("end"))
			{
				final Auction auction = ch.getAuction();
				if (auction == null)
					activeChar.sendMessage("This ClanHall doesn't hold an auction.");
				else
					auction.endAuction();
			}
			
			showClanHallSelectPage(activeChar, ch);
		}
		catch (Exception e)
		{
			activeChar.sendMessage("Usage: //ch chId <set|del|open|close|goto|end>.");
		}
		return true;
	}
	
	/**
	 * Show detailed informations of a {@link ClanHall} to a {@link Player}.
	 * @param player : The Player who requested the action.
	 * @param ch : The ClanHall to show informations.
	 */
	private static void showClanHallSelectPage(Player player, ClanHall ch)
	{
		String clanName = "None";
		if (!ch.isFree())
		{
			final Clan clan = ClanTable.getInstance().getClan(ch.getOwnerId());
			if (clan != null)
				clanName = clan.getName();
		}
		
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		final StringBuilder sb = new StringBuilder();
		
		final Auction auction = ch.getAuction();
		if (auction == null)
			sb.append("<tr><td>This clanhall doesn't have Auction.</td></tr>");
		else if (auction.getHighestBidder() == null)
			StringUtil.append(sb, "<tr><td width=150>Bid: 0</td><td width=120>Bidders: ", auction.getBidders().size(), "</td></tr><tr><td>End date: ", sdf.format(auction.getEndDate()), "</td><td>Winner: none</td></tr>");
		else
			StringUtil.append(sb, "<tr><td width=150>Bid: ", StringUtil.formatNumber(auction.getHighestBidder().getBid()), "</td><td width=120>Bidders: ", auction.getBidders().size(), "</td></tr><tr><td>End date: ", sdf.format(auction.getEndDate()), "</td><td>Winner: ", auction.getHighestBidder().getName(), "</td></tr>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/clanhall.htm");
		html.replace("%name%", ch.getName());
		html.replace("%id%", ch.getId());
		html.replace("%grade%", ch.getGrade());
		html.replace("%lease%", StringUtil.formatNumber(ch.getLease()));
		html.replace("%loc%", ch.getLocation());
		html.replace("%desc%", ch.getDesc());
		html.replace("%defaultbid%", StringUtil.formatNumber(ch.getDefaultBid()));
		html.replace("%owner%", clanName);
		html.replace("%paid%", String.valueOf(ch.getPaid()));
		html.replace("%paiduntil%", sdf.format(ch.getPaidUntil()));
		html.replace("%auction", sb.toString());
		player.sendPacket(html);
	}
	
	/**
	 * Show the complete list of {@link ClanHall}s (listed, unlisted, owned, conquerable or auctionable).
	 * @param player : The Player who requested the action.
	 */
	private static void showClanHallSelectPage(Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/clanhalls.htm");
		
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		final StringBuilder sb = new StringBuilder();
		
		for (String loc : LOCS)
		{
			StringUtil.append(sb, "<font color=\"LEVEL\">", loc, "</font><br><table width=270><tr><td width=130>Clan Hall Name</td><td width=70>End Date</td><td width=70>Min Bid</td></tr>");
			
			for (ClanHall ch : ClanHallManager.getInstance().getClanHallsByLocation(loc))
			{
				final Auction auction = ch.getAuction();
				if (auction == null)
					StringUtil.append(sb, "<tr><td><a action=\"bypass -h admin_ch ", ch.getId(), "\">", ch.getName(), ((ch.isFree()) ? "" : "*"), "</a></td><td>-</td><td>-</td></tr>");
				else
					StringUtil.append(sb, "<tr><td><a action=\"bypass -h admin_ch ", ch.getId(), "\">", ch.getName(), ((ch.isFree()) ? "" : "*"), " [", auction.getBidders().size(), "]</a></td><td>", sdf.format(auction.getEndDate()), "</td><td>", auction.getMinimumBid(), "</td></tr>");
			}
			
			sb.append("</table><br>");
		}
		
		html.replace("%AGIT_LIST%", sb.toString());
		player.sendPacket(html);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}