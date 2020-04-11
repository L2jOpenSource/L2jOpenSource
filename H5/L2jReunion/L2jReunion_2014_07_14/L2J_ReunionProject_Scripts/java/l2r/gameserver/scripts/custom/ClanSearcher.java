/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.scripts.custom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2r.L2DatabaseFactory;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.datatables.sql.ClanTable;
import l2r.gameserver.model.ClanPrivilege;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2ClanMember;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.ShowBoard;

public class ClanSearcher extends Quest
{
	private static final int NpcId = 559;
	private static final int paymentId = 57; // Item Id to get
	// "time left" update time in minutes
	private static final int UPDATE_TIME = 60;
	private static final int CLAN_PRESENTATION_DURATION = 604800000; // 604800000 = 1 week
	
	public ClanSearcher()
	{
		super(-1, "ClanSearcher", "custom");
		
		addFirstTalkId(NpcId);
		addTalkId(NpcId);
		addStartNpc(NpcId);
		
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> updateClans(), UPDATE_TIME * 60000, UPDATE_TIME * 60000);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.startsWith("show_list"))
		{
			loadClansAndShowWindow(npc, player, 1);
		}
		else if (event.startsWith("add"))
		{
			saveClanPresentation(event, npc, player);
			showMoreClanInfo("moreinfo_" + player.getClan().getId(), npc, player);
			
		}
		else if (event.startsWith("moreinfo_"))
		{
			showMoreClanInfo(event, npc, player);
		}
		else if (event.startsWith("requestjoin_"))
		{
			sendClanJoinRequest(event, npc, player);
		}
		else if (event.startsWith("remove"))
		{
			removeClanPresentation(npc, player);
		}
		else if (event.startsWith("newclan.htm"))
		{
			String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/scripts/custom/ClanSearcher/newclan.htm");
			html = html.replaceAll("%clanname%", player.getClan().getName());
			html = html.replaceAll("%clanlevel%", "" + player.getClan().getLevel());
			html = html.replaceAll("%clanleader%", player.getClan().getLeaderName());
			
			sendCBHtml(player, html, loadClansAndShowWindow(npc, player, 2));
		}
		else if (event.startsWith("deleteclan.htm"))
		{
			sendCBHtml(player, HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/scripts/custom/ClanSearcher/deleteclan.htm"));
		}
		return "";
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final int npcId = npc.getId();
		if (player.getQuestState(getName()) == null)
		{
			newQuestState(player);
		}
		
		if (npcId == NpcId)
		{
			String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/scripts/custom/ClanSearcher/1.htm");
			html = html.replaceAll("%player%", player.getName());
			
			if (player.getClan() == null)
			{
				html = html.replaceAll("%text%", "Its not very safe for people to walk alone around in these dark days... Perhaps you would like to join some clan? <br> Leaders from all over the world come to me to advertise their clans. Maybe you could be a nice addition to their clans...");
			}
			else if (player.isClanLeader())
			{
				html = html.replaceAll("%text%", "Another clan leader comes to me... what would you like me to offer you? <br> Do you want to advertise your clan, be it for new members, or just pissing off other clans. <br> Or maybe you just want to check out hows the concurency going on huh?");
			}
			else
			{
				html = html.replaceAll("%text%", "So... you are already in a clan huh? Good to see that people do not walk alone in those dark days...<br> Are you curious to see your concurence?");
			}
			
			if (player.isClanLeader())
			{
				html = html.replaceAll("%add%", "<button value=\"Edit your clan's presentation\" action=\"bypass -h Quest ClanSearcher newclan.htm\" width=255 height=27 back=\"L2UI_CT1.Button_DF.Gauge_DF_Attribute_Divine\" fore=\"L2UI_CT1.Button_DF.Gauge_DF_Attribute_Divine\">");
				html = html.replaceAll("%remove%", "<button value=\"Remove your clan's presentation\" action=\"bypass -h Quest ClanSearcher deleteclan.htm\" width=255 height=27 back=\"L2UI_CT1.Button_DF.Gauge_DF_Attribute_Divine\" fore=\"L2UI_CT1.Button_DF.Gauge_DF_Attribute_Divine\">");
			}
			else
			{
				html = html.replaceAll("%add%", "");
				html = html.replaceAll("%remove%", "");
			}
			
			sendCBHtml(player, html);
			/**
			 * NpcHtmlMessage npcHtml = new NpcHtmlMessage(0); npcHtml.setHtml(html); player.sendPacket(npcHtml);
			 */
		}
		return "";
	}
	
	private void sendClanJoinRequest(String event, L2Npc npc, L2PcInstance requester)
	{
		L2Clan clan = ClanTable.getInstance().getClan(Integer.parseInt(event.split("_")[1]));
		String text = "Player " + requester.getName() + " requested to join your clan.";
		boolean success = false;
		for (L2ClanMember member : clan.getMembers())
		{
			if ((member.getPlayerInstance() == null) && !member.isOnline())
			{
				continue;
			}
			L2PcInstance player = member.getPlayerInstance();
			
			if (player.hasClanPrivilege(ClanPrivilege.CL_JOIN_CLAN))
			{
				player.sendPacket(new CreatureSay(player.getObjectId(), 2, player.getName(), text));
				success = true;
			}
		}
		if (success)
		{
			NpcHtmlMessage npcHtml = new NpcHtmlMessage(0);
			npcHtml.setHtml("<html><body><br>&nbsp;Your request was successfully send.</body></html>");
			requester.sendPacket(npcHtml);
			loadClansAndShowWindow(npc, requester, 1);
		}
		else
		{
			NpcHtmlMessage npcHtml = new NpcHtmlMessage(0);
			npcHtml.setHtml("<html><body><br>&nbsp;Nobody was able to see your request, try again later.</body></html>");
			requester.sendPacket(npcHtml);
			loadClansAndShowWindow(npc, requester, 1);
		}
	}
	
	private String loadClansAndShowWindow(L2Npc npc, L2PcInstance player, int mode)
	{
		StringBuilder sb = new StringBuilder();
		L2Clan clan = null;
		Map<L2Clan, String> clans = new FastMap<>();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			final ResultSet set = con.prepareStatement("SELECT * FROM clan_search ORDER BY adenas DESC").executeQuery();
			while (set.next())
			{
				clan = ClanTable.getInstance().getClan(set.getInt("clanId"));
				if (clan == null)
				{
					continue;
				}
				if ((set.getInt("visible") > 0) || (mode == 2))
				{
					clans.put(clan, set.getString("message"));
				}
			}
			if (mode == 2)
			{
				return clans.get(player.getClan());
			}
			sb.append("<html><body><br><br><center><table border=\"1\" background=000000 width=\"750\">");
			sb.append("<tr><td align=center height=30 width=\"140\"><font color=LEVEL>Clan Name</font></td><td align=center height=30 width=\"100\"><font color=LEVEL>Clan Level</font></td><td align=center height=30 width=\"140\"><font color=LEVEL>Clan Leader</font></td><td align=center height=30 width=\"370\"><font color=LEVEL>Info</font></td></tr>");
			int i = 0;
			for (Map.Entry<L2Clan, String> entry : clans.entrySet())
			{
				if (entry.getKey() == null)
				{
					continue;
				}
				
				String fullMsg = clans.get(entry.getKey());
				String smallClanInfo = fullMsg.substring(0, Math.min(fullMsg.length(), 96));
				smallClanInfo = smallClanInfo.replaceAll("<br>", " ");
				smallClanInfo = smallClanInfo.replaceAll("<", "");
				smallClanInfo = smallClanInfo.replaceAll(">", "");
				smallClanInfo = smallClanInfo.replaceAll("bypass", "");
				sb.append("<tr><td align=center fixwidth=\"140\"><font color=00FFFF>" + entry.getKey().getName() + "</font></td><td align=center fixwidth=\"100\"><font color=00FFFF>" + entry.getKey().getLevel() + "</font></td><td align=center fixwidth=\"140\"><font color=00FFFF>" + entry.getKey().getLeaderName() + "</font></td><td align=center fixwidth=\"370\"><font color=00FFFF>" + smallClanInfo + "...</font><a action=\"bypass -h Quest ClanSearcher moreinfo_" + entry.getKey().getId() + "\">(more info)</a></td></tr>");
				i++;
			}
			while (i < 15) // Create empty clan spots for better look
			{
				sb.append("<tr><td align=center height=70 fixwidth=\"140\"> </td><td align=center height=70 fixwidth=\"100\"> </td><td align=center height=70 fixwidth=\"140\"> </td><td align=center height=70 fixwidth=\"370\"> </td></tr>");
				i++;
			}
			sb.append("</table><br><br></center></body></html>");
			sendCBHtml(player, sb.toString());
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return "";
	}
	
	private void showMoreClanInfo(String event, L2Npc npc, L2PcInstance player)
	{
		StringBuilder sb = new StringBuilder();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM clan_search WHERE clanId=?");
			statement.setInt(1, Integer.parseInt(event.split("_")[1]));
			final ResultSet set = statement.executeQuery();
			
			if (set.next())
			{
				L2Clan clan = ClanTable.getInstance().getClan(set.getInt("clanId"));
				String message = set.getString("message");
				sb.append("<html noscrollbar><body><center><br><table border=\"0\" background=000000 width=\"790\">");
				sb.append("<tr><td align=center width=\"140\"><font color=LEVEL>Clan Name</font></td><td align=center width=\"100\"><font color=LEVEL>Clan Level</font></td><td align=center width=\"140\"><font color=LEVEL>Clan Leader</font></td></tr>");
				sb.append("<tr><td align=center fixwidth=\"140\"><font color=00FFFF>" + clan.getName() + "</font></td><td align=center fixwidth=\"100\"><font color=00FFFF>" + clan.getLevel() + "</font></td><td align=center fixwidth=\"140\"><font color=00FFFF>" + clan.getLeaderName() + "</font></td></tr></table>");
				sb.append("<table border=\"0\" height=400 background=000000 width=\"790\">");
				sb.append("<tr><td height=20></td></tr>");
				sb.append("<tr><td align=center><font color=LEVEL>Clan presentation info:</font></td></tr>");
				sb.append("<tr><td width=780 height=380><font color=00FFFF>" + message + "</font></td></tr>");
				if (player.isClanLeader() && player.getClan().equals(clan))
				{
					sb.append("<tr><td align=center height=20><a action=\"bypass -h Quest ClanSearcher newclan.htm\">Edit clan presentation info</a></td></tr>");
				}
				else if (player.getClan() != null)
				{
					sb.append("<tr><td align=center height=20> </td></tr>");
				}
				else
				{
					// Player isn't in a clan, give him invitation request button
					sb.append("<tr><td align=center height=20><a action=\"bypass -h Quest ClanSearcher requestjoin_" + clan.getId() + "\">Send invitation request</a></td></tr>");
				}
				sb.append("<tr><td align=center height=30><a action=\"bypass -h Quest ClanSearcher show_list\">Back</a></td></tr>");
				sb.append("</table>");
				sb.append("</center></body></html>");
				sendCBHtml(player, sb.toString());
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private void saveClanPresentation(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.length() < 5)
		{
			player.sendMessage("Failed to submit, description is too short.");
			return;
		}
		else if (event.length() > 7800)
		{
			player.sendMessage("Failed to submit, description cannot exceed 7800 chars.");
			return;
		}
		String message = "";
		int money = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("INSERT INTO clan_search (visible,clanId,message,timeleft,adenas) values (?,?,?,?,?) ON DUPLICATE KEY UPDATE visible=?,clanId=?,message=?,timeleft=?,adenas=?");
			
			for (String s : event.split(" "))
			{
				message = message + " " + s;
			}
			message = message.replaceFirst("add", "");
			message = message.replaceFirst("  ", "");
			try
			{
				money = Integer.parseInt(message.split(" ")[0]);
			}
			catch (NumberFormatException e)
			{
				player.sendMessage("Bid price box can only contain numbers.");
				// player.sendPacket(new NpcHtmlMessage(0, "Bad amount of adenas inputed."));
				return;
			}
			
			if ((player.getInventory().getItemByItemId(paymentId) != null) && (player.getInventory().getItemByItemId(paymentId).getCount() > money))
			{
				player.destroyItemByItemId("clanner", paymentId, money, player, true);
			}
			else
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_FOR_THIS_BID);
				return;
			}
			
			message = message.replaceFirst("" + money, "");
			message = message.replaceAll("\n", "<br>");
			statement.setInt(1, 1);
			statement.setInt(2, player.getClanId());
			statement.setString(3, message);
			statement.setInt(4, CLAN_PRESENTATION_DURATION);
			statement.setInt(5, money);
			statement.setInt(6, 1);
			statement.setInt(7, player.getClanId());
			statement.setString(8, message);
			statement.setInt(9, CLAN_PRESENTATION_DURATION);
			statement.setInt(10, money);
			statement.execute();
			
			/**
			 * NpcHtmlMessage npcHtml = new NpcHtmlMessage(0); npcHtml.setHtml("<html><body><br>
			 * &nbsp;Your presentation has been saved.</body></html>"); player.sendPacket(npcHtml);
			 */
			player.sendMessage("Your presentation has been saved.");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private void removeClanPresentation(L2Npc npc, L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM clan_search WHERE clanId=?");
			statement.setInt(1, player.getClanId());
			statement.execute();
			
			/**
			 * NpcHtmlMessage npcHtml = new NpcHtmlMessage(0); npcHtml.setHtml("<html><body><br>
			 * &nbsp;Your presentation has been deleted.</body></html>"); player.sendPacket(npcHtml); sendCBHtml(player, "<html><body><br>
			 * &nbsp;Your presentation has been deleted.</body></html>");
			 */
			player.sendMessage("Your presentation has been deleted.");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private final void sendCBHtml(L2PcInstance activeChar, String html)
	{
		sendCBHtml(activeChar, html, "");
	}
	
	private final void sendCBHtml(L2PcInstance activeChar, String html, String fillMultiEdit)
	{
		if (activeChar == null)
		{
			return;
		}
		
		if (html != null)
		{
			activeChar.clearBypass();
			int len = html.length();
			for (int i = 0; i < len; i++)
			{
				int start = html.indexOf("\"bypass ", i);
				int finish = html.indexOf("\"", start + 1);
				if ((start < 0) || (finish < 0))
				{
					break;
				}
				
				if (html.substring(start + 8, start + 10).equals("-h"))
				{
					start += 11;
				}
				else
				{
					start += 8;
				}
				
				i = finish;
				int finish2 = html.indexOf("$", start);
				if ((finish2 < finish) && (finish2 > 0))
				{
					activeChar.addBypass2(html.substring(start, finish2).trim());
				}
				else
				{
					activeChar.addBypass(html.substring(start, finish).trim());
				}
			}
		}
		
		if (fillMultiEdit != null)
		{
			activeChar.sendPacket(new ShowBoard(html, "1001"));
			fillMultiEditContent(activeChar, fillMultiEdit);
		}
		else
		{
			activeChar.sendPacket(new ShowBoard(null, "101"));
			activeChar.sendPacket(new ShowBoard(html, "101"));
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}
	
	/**
	 * Must send after sendCBHtml
	 * @param activeChar
	 * @param text
	 */
	private void fillMultiEditContent(L2PcInstance activeChar, String text)
	{
		text = text.replaceAll("<br>", "\n");
		List<String> _arg = new FastList<>();
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add(activeChar.getName());
		_arg.add(Integer.toString(activeChar.getObjectId()));
		_arg.add(activeChar.getAccountName());
		_arg.add("9");
		_arg.add(" ");
		_arg.add(" ");
		_arg.add(text);
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		activeChar.sendPacket(new ShowBoard(_arg));
	}
	
	protected static void updateClans()
	{
		Map<L2Clan, Integer> clans = new FastMap<>();
		try (Connection c = L2DatabaseFactory.getInstance().getConnection())
		{
			ResultSet set = c.prepareStatement("SELECT * FROM clan_search").executeQuery();
			while (set.next())
			{
				L2Clan clan = ClanTable.getInstance().getClan(set.getInt("clanId"));
				if (clan != null)
				{
					clans.put(clan, set.getInt("timeleft"));
				}
			}
			for (Map.Entry<L2Clan, Integer> entry : clans.entrySet())
			{
				if ((entry.getValue() - (UPDATE_TIME * 60000)) < 1)
				{
					PreparedStatement statement = c.prepareStatement("UPDATE clan_search SET timeleft=0,visible=0 WHERE clanId=?");
					statement.setInt(1, entry.getKey().getId());
					statement.execute();
				}
				else
				{
					PreparedStatement statement = c.prepareStatement("UPDATE clan_search SET timeleft=? WHERE clanId=?");
					statement.setInt(1, entry.getValue() - (UPDATE_TIME * 60000));
					statement.setInt(2, entry.getKey().getId());
					statement.execute();
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		new ClanSearcher();
	}
}