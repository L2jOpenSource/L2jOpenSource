package gr.sr.aioItem.dymanicHtmls;

import l2r.Config;
import l2r.gameserver.data.sql.NpcTable;
import l2r.gameserver.instancemanager.GrandBossManager;
import l2r.gameserver.model.ClanPrivilege;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SortedWareHouseWithdrawalList;
import l2r.gameserver.network.serverpackets.SortedWareHouseWithdrawalList.WarehouseListType;
import l2r.gameserver.network.serverpackets.WareHouseWithdrawalList;

import gr.sr.achievementEngine.AchievementsHandler;
import gr.sr.achievementEngine.AchievementsManager;
import gr.sr.achievementEngine.base.Achievement;
import gr.sr.achievementEngine.base.Condition;
import gr.sr.dataHolder.PlayersTopData;
import gr.sr.main.TopListsLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class GenerateHtmls
{
	private static Logger _log = LoggerFactory.getLogger(GenerateHtmls.class);
	private static final int[] BOSSES =
	{
		29001,
		29006,
		29014,
		29019,
		29020,
		29022,
		29028,
		29118
	};
	
	/**
	 * Method to send the html to char
	 * @param player
	 * @param html
	 */
	public static void sendPacket(L2PcInstance player, String html)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage();
		msg.setFile(player, player.getHtmlPrefix(), "/data/html/sunrise/AioItemNpcs/" + html);
		player.sendPacket(msg);
	}
	
	/**
	 * Method to show grand boss info
	 * @param player
	 */
	public static final void showRbInfo(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage();
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Rb Info</title><body>");
		tb.append("<br><br>");
		tb.append("<font color=00FFFF>Grand Boss Info</font>");
		tb.append("<center>");
		tb.append("<img src=L2UI.SquareGray width=280 height=1>");
		tb.append("<br><br>");
		tb.append("<table width = 280>");
		for (int boss : BOSSES)
		{
			String name = NpcTable.getInstance().getTemplate(boss).getName();
			long delay = GrandBossManager.getInstance().getStatsSet(boss).getLong("respawn_time");
			if (delay <= System.currentTimeMillis())
			{
				tb.append("<tr>");
				tb.append("<td><font color=\"00C3FF\">" + name + "</color>:</td> " + "<td><font color=\"9CC300\">Is Alive</color></td>" + "<br1>");
				tb.append("</tr>");
			}
			else
			{
				int hours = (int) ((delay - System.currentTimeMillis()) / 1000 / 60 / 60);
				int mins = (int) (((delay - (hours * 60 * 60 * 1000)) - System.currentTimeMillis()) / 1000 / 60);
				int seconts = (int) (((delay - ((hours * 60 * 60 * 1000) + (mins * 60 * 1000))) - System.currentTimeMillis()) / 1000);
				tb.append("<tr>");
				tb.append("<td><font color=\"00C3FF\">" + name + "</color></td>" + "<td><font color=\"FFFFFF\">" + " " + "Respawn in :</color></td>" + " " + "<td><font color=\"32C332\">" + hours + " : " + mins + " : " + seconts + "</color></td><br1>");
				tb.append("</tr>");
			}
		}
		tb.append("</table>");
		tb.append("<br><br>");
		tb.append("<img src=L2UI.SquareWhite width=280 height=1>");
		tb.append("<td><button value=\"Back\" action=\"bypass -h Aioitem_Chat_service/services.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		tb.append("</center>");
		tb.append("</body></html>");
		html.setHtml(tb.toString());
		player.sendPacket(html);
	}
	
	/**
	 * Method to show achievement main menu
	 * @param player
	 * @param val
	 */
	public static void showAchievementMain(L2PcInstance player, int val)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Hello <font color=\"LEVEL\">" + player.getName() + "</font><br>Are you looking for challenge?");
		tb.append("<br><img src=\"L2UI.SquareWhite\" width=\"280\" height=\"1\"><br><br>");
		tb.append("<button value=\"My Achievements\" action=\"bypass -h Aioitem_showMyAchievements\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append("<button value=\"Statistics\" action=\"bypass -h Aioitem_showAchievementStats\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append("<button value=\"Help\" action=\"bypass -h Aioitem_showAchievementHelp\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append("<br><br><img src=\"L2UI.SquareWhite\" width=\"280\" height=\"1\">");
		tb.append("<td><button value=\"Back\" action=\"bypass -h Aioitem_Chat_service/services.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		NpcHtmlMessage msg = new NpcHtmlMessage();
		msg.setHtml(tb.toString());
		
		player.sendPacket(msg);
	}
	
	/**
	 * Method to show player's achievements
	 * @param player
	 */
	public static void showMyAchievements(L2PcInstance player)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><br>");
		
		tb.append("<center><font color=\"LEVEL\">My achievements</font>:</center><br>");
		
		if (AchievementsManager.getInstance().getAchievementList().isEmpty())
		{
			tb.append("There are no Achievements created yet!");
		}
		else
		{
			int i = 0;
			
			tb.append("<table width=280 border=0 bgcolor=\"33FF33\">");
			tb.append("<tr><td width=115 align=\"left\">Name:</td><td width=50 align=\"center\">Info:</td><td width=115 align=\"center\">Status:</td></tr></table>");
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"280\" height=\"1\"><br>");
			
			for (Achievement a : AchievementsManager.getInstance().getAchievementList().values())
			{
				tb.append(getTableColor(i));
				tb.append("<tr><td width=115 align=\"left\">" + a.getName() + "</td><td width=50 align=\"center\"><a action=\"bypass -h Aioitem_showAchievementInfo " + a.getId() + "\">info</a></td><td width=115 align=\"center\">" + getStatusString(a.getId(), player) + "</td></tr></table>");
				i++;
			}
			
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"280\" height=\"1s\"><br>");
			tb.append("<center><button value=\"Back\" action=\"bypass -h Aioitem_showAchievementMain\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		}
		
		NpcHtmlMessage msg = new NpcHtmlMessage();
		msg.setHtml(tb.toString());
		
		player.sendPacket(msg);
	}
	
	/**
	 * Method to show achievement info
	 * @param achievementID
	 * @param player
	 */
	public static void showAchievementInfo(int achievementID, L2PcInstance player)
	{
		Achievement a = AchievementsManager.getInstance().getAchievementList().get(achievementID);
		
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><br>");
		
		tb.append("<center><table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"center\">" + a.getName() + "</td></tr></table><br>");
		tb.append("Status: " + getStatusString(achievementID, player));
		
		if (a.meetAchievementRequirements(player) && !player.getCompletedAchievements().contains(achievementID))
		{
			tb.append("<button value=\"Receive Reward!\" action=\"bypass -h Aioitem_achievementGetReward " + a.getId() + "\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		}
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"center\">Description</td></tr></table><br>");
		tb.append(a.getDescription());
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		
		tb.append("<table width=280 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=120 align=\"left\">Condition To Meet:</td><td width=55 align=\"center\">Value:</td><td width=95 align=\"center\">Status:</td></tr></table>");
		tb.append(getConditionsStatus(achievementID, player));
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h Aioitem_showMyAchievements\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage();
		msg.setHtml(tb.toString());
		
		player.sendPacket(msg);
	}
	
	/**
	 * Method to achievement statistics
	 * @param player
	 */
	public static void showAchievementStats(L2PcInstance player)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Check your <font color=\"LEVEL\">Achievements </font>statistics:");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		AchievementsHandler.getAchievemntData(player);
		int completedCount = player.getCompletedAchievements().size();
		
		tb.append("You have completed: " + completedCount + "/<font color=\"LEVEL\">" + AchievementsManager.getInstance().getAchievementList().size() + "</font>");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h Aioitem_showAchievementMain\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage();
		msg.setHtml(tb.toString());
		
		player.sendPacket(msg);
	}
	
	/**
	 * Method to show achievement help menu
	 * @param player
	 */
	public static void showAchievementHelp(L2PcInstance player)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Achievements  <font color=\"LEVEL\">Help </font>page:");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		tb.append("You can check status of your achievements, receive reward if every condition of achievement is meet, if not you can check which condition is still not meet, by using info button");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<font color=\"FF0000\">Not Completed</font> - you did not meet the achivement requirements.<br>");
		tb.append("<font color=\"LEVEL\">Get Reward</font> - you may receive reward, click info.<br>");
		tb.append("<font color=\"5EA82E\">Completed</font> - achievement completed, reward received.<br>");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h Aioitem_showAchievementMain\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage();
		msg.setHtml(tb.toString());
		
		player.sendPacket(msg);
	}
	
	public static final void showCWithdrawWindow(L2PcInstance player, WarehouseListType itemtype, byte sortorder)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		if (!player.hasClanPrivilege(ClanPrivilege.CL_VIEW_WAREHOUSE))
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
			return;
		}
		
		player.setActiveWarehouse(player.getClan().getWarehouse());
		
		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
			return;
		}
		
		if (itemtype != null)
		{
			player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN, itemtype, sortorder));
		}
		else
		{
			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN));
		}
		
		if (Config.DEBUG)
		{
			_log.info("Source: L2WarehouseInstance.java; Player: " + player.getName() + "; Command: showRetrieveWindowClan; Message: Showing stored items.");
		}
	}
	
	public static final void showPWithdrawWindow(L2PcInstance player, WarehouseListType itemtype, byte sortorder)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());
		
		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
			return;
		}
		
		if (itemtype != null)
		{
			player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE, itemtype, sortorder));
		}
		else
		{
			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE));
		}
		
		if (Config.DEBUG)
		{
			_log.info("Source: L2WarehouseInstance.java; Player: " + player.getName() + "; Command: showRetrieveWindow; Message: Showing stored items.");
		}
	}
	
	/**
	 * Method to show top festival adena player
	 * @param player
	 */
	public static void showTopFa(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage();
		StringBuilder sb = new StringBuilder();
		sb.append("<html><title>Top Fa</title><body><center><br>");
		sb.append("<table border=1 width = 280>");
		sb.append("<tr>");
		sb.append("<td><font color=FFD700>No</font></td><td><font color=FFD700>Character Name:</font></td><td><font color=FFD700>Fa Count:</font></td>");
		sb.append("</tr>");
		int count = 1;
		for (PlayersTopData playerData : TopListsLoader.getInstance().getTopCurrency())
		{
			String name = playerData.getCharName();
			long countFa = playerData.getCurrencyCount();
			
			sb.append("<tr>");
			sb.append("<td align=center>" + count + "</td><td>" + name + "</td><td align=center>" + countFa + "</td>");
			sb.append("</tr>");
			sb.append("<br>");
			count = count + 1;
		}
		sb.append("</table>");
		sb.append("<br><center>");
		sb.append("<br><img src=L2UI.SquareWhite width=280 height=1>");
		sb.append("<td><button value=\"Back\" action=\"bypass -h Aioitem_Chat_service/toplists.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("</center>");
		sb.append("</body></html>");
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	/**
	 * Method to show top PvP player
	 * @param player
	 */
	public static void showTopPvp(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage();
		StringBuilder sb = new StringBuilder();
		sb.append("<html><title>Top PvP</title><body><center><br>");
		sb.append("<table border=1 width = 280>");
		sb.append("<tr>");
		sb.append("<td><font color=FFD700>No</font></td><td><font color=FFD700>Character Name:</font></td><td><font color=FFD700>Clan Name:</font></td><td><font color=FFD700>PvP Kills:</font></td>");
		sb.append("</tr>");
		int count = 1;
		for (PlayersTopData playerData : TopListsLoader.getInstance().getTopPvp())
		{
			String name = playerData.getCharName();
			String cName = playerData.getClanName();
			int pvp = playerData.getPvp();
			
			sb.append("<tr>");
			sb.append("<td align=center>" + count + "</td><td>" + name + "</td><td align=center>" + cName + "</td><td align=center>" + pvp + "</td>");
			sb.append("</tr>");
			sb.append("<br>");
			count = count + 1;
		}
		sb.append("</table>");
		sb.append("<br><center>");
		sb.append("<br><img src=L2UI.SquareWhite width=280 height=1>");
		sb.append("<td><button value=\"Back\" action=\"bypass -h Aioitem_Chat_service/toplists.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("</center>");
		sb.append("</body></html>");
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	/**
	 * Method to show top Pk player
	 * @param player
	 */
	public static void showTopPk(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage();
		StringBuilder sb = new StringBuilder();
		sb.append("<html><title>Top Pk</title><body><center><br>");
		sb.append("<table border=1 width = 280>");
		sb.append("<tr>");
		sb.append("<td><font color=FFD700>No</font></td><td><font color=FFD700>Character Name:</font></td><td><font color=FFD700>Clan Name:</font></td><td><font color=FFD700>Pk Kills:</font></td>");
		sb.append("</tr>");
		int count = 1;
		for (PlayersTopData playerData : TopListsLoader.getInstance().getTopPk())
		{
			String name = playerData.getCharName();
			String cName = playerData.getClanName();
			int pk = playerData.getPk();
			
			sb.append("<tr>");
			sb.append("<td align=center>" + count + "</td><td>" + name + "</td><td align=center>" + cName + "</td><td align=center>" + pk + "</td>");
			sb.append("</tr>");
			sb.append("<br>");
			count = count + 1;
		}
		sb.append("</table>");
		sb.append("<br><center>");
		sb.append("<br><img src=L2UI.SquareWhite width=280 height=1>");
		sb.append("<td><button value=\"Back\" action=\"bypass -h Aioitem_Chat_service/toplists.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("</center>");
		sb.append("</body></html>");
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	/**
	 * Method to show top clan
	 * @param player
	 */
	public static void showTopClan(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage();
		StringBuilder sb = new StringBuilder();
		sb.append("<html><title>Top Clan</title><body><center><br>");
		sb.append("<table border=1 width = 280>");
		sb.append("<tr>");
		sb.append("<td><font color=FFD700>No</font></td><td><font color=FFD700>Leader Name:</font></td><td><font color=FFD700>Clan Name:</font></td><td><font color=FFD700>Clan Level:</font></td>");
		sb.append("</tr>");
		int count = 1;
		for (PlayersTopData playerData : TopListsLoader.getInstance().getTopClan())
		{
			String name = playerData.getCharName();
			String cName = playerData.getClanName();
			int cLevel = playerData.getClanLevel();
			
			sb.append("<tr>");
			sb.append("<td align=center>" + count + "</td><td>" + name + "</td><td align=center>" + cName + "</td><td align=center>" + cLevel + "</td>");
			sb.append("</tr>");
			sb.append("<br>");
			count = count + 1;
		}
		sb.append("</table>");
		sb.append("<br><center>");
		sb.append("<br><img src=L2UI.SquareWhite width=280 height=1>");
		sb.append("<td><button value=\"Back\" action=\"bypass -h Aioitem_Chat_service/toplists.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("</center>");
		sb.append("</body></html>");
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	private static String getStatusString(int achievementID, L2PcInstance player)
	{
		if (player.getCompletedAchievements().contains(achievementID))
		{
			return "<font color=\"5EA82E\">Completed</font>";
		}
		if (AchievementsManager.getInstance().getAchievementList().get(achievementID).meetAchievementRequirements(player))
		{
			return "<font color=\"LEVEL\">Get Reward</font>";
		}
		return "<font color=\"FF0000\">Not Completed</font>";
	}
	
	private static String getTableColor(int i)
	{
		if ((i % 2) == 0)
		{
			return "<table width=280 border=0 bgcolor=\"444444\">";
		}
		return "<table width=280 border=0>";
	}
	
	private static String getConditionsStatus(int achievementID, L2PcInstance player)
	{
		int i = 0;
		String s = "</center>";
		Achievement a = AchievementsManager.getInstance().getAchievementList().get(achievementID);
		String completed = "<font color=\"5EA82E\">Completed</font></td></tr></table>";
		String notcompleted = "<font color=\"FF0000\">Not Completed</font></td></tr></table>";
		
		for (Condition c : a.getConditions())
		{
			s += getTableColor(i);
			s += "<tr><td width=120 align=\"left\">" + c.getType().getText() + "</td><td width=55 align=\"center\">" + c.getValue() + "</td><td width=95 align=\"center\">";
			i++;
			
			if (c.meetConditionRequirements(player))
			{
				s += completed;
			}
			else
			{
				s += notcompleted;
			}
		}
		return s;
	}
}