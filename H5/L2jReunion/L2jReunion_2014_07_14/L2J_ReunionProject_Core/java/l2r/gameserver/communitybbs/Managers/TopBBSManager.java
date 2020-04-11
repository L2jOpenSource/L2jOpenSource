/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.communitybbs.Managers;

import java.io.File;
import java.util.StringTokenizer;

import l2r.Config;
import l2r.gameserver.GameTimeController;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.communitybbs.ReunionBoards.CastleStatus;
import l2r.gameserver.communitybbs.ReunionBoards.GrandBossList;
import l2r.gameserver.communitybbs.ReunionBoards.HeroeList;
import l2r.gameserver.communitybbs.ReunionBoards.RaidList;
import l2r.gameserver.communitybbs.ReunionBoards.TopClan;
import l2r.gameserver.communitybbs.ReunionBoards.TopOnlinePlayers;
import l2r.gameserver.communitybbs.ReunionBoards.TopPkPlayers;
import l2r.gameserver.communitybbs.ReunionBoards.TopPvpPlayers;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.ShowBoard;
import gr.reunion.configsEngine.SmartCommunityConfigs;

public class TopBBSManager extends BaseBBSManager
{
	@Override
	public void cbByPass(String command, L2PcInstance activeChar)
	{
		String path = "data/html/CommunityBoard/";
		String filepath = "";
		String content = "";
		
		if (command.equals("_bbstop") | command.equals("_bbshome"))
		{
			filepath = path + "index.htm";
			content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), filepath);
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith("_bbstop;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			String file = st.nextToken();
			filepath = path + file + ".htm";
			File filecom = new File(filepath);
			
			if (!(filecom.exists()))
			{
				content = "<html><body><br><br><center>The command " + command + " points to file(" + filepath + ") that NOT exists.</center></body></html>";
				separateAndSend(content, activeChar);
				return;
			}
			content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), filepath);
			
			if (content.isEmpty())
			{
				content = "<html><body><br><br><center>Content Empty: The command " + command + " points to an invalid or empty html file(" + filepath + ").</center></body></html>";
			}
			
			switch (file)
			{
				case "toppvp":
					TopPvpPlayers pvp = new TopPvpPlayers(file);
					content = content.replaceAll("%toppvp%", pvp.loadTopList());
					break;
				case "toppk":
					TopPkPlayers pk = new TopPkPlayers(file);
					content = content.replaceAll("%toppk%", pk.loadTopList());
					break;
				case "topadena":
					TopPkPlayers adena = new TopPkPlayers(file);
					content = content.replaceAll("%topadena%", adena.loadTopList());
					break;
				case "toponline":
					TopOnlinePlayers online = new TopOnlinePlayers(file);
					content = content.replaceAll("%toponline%", online.loadTopList());
					break;
				case "topclan":
					TopClan clan = new TopClan(file);
					content = content.replaceAll("%topclan%", clan.loadClanList());
					break;
				case "heroes":
					HeroeList hr = new HeroeList();
					content = content.replaceAll("%heroelist%", hr.loadHeroeList());
					break;
				case "castle":
					CastleStatus status = new CastleStatus();
					content = content.replaceAll("%castle%", status.loadCastleList());
					break;
				case "boss":
					GrandBossList gb = new GrandBossList();
					content = content.replaceAll("%gboss%", gb.loadGrandBossList());
					break;
				case "stats":
					content = content.replace("%online%", Integer.toString(L2World.getInstance().getAllPlayersCount() + SmartCommunityConfigs.EXTRA_PLAYERS_COUNT));
					content = content.replace("%servercapacity%", Integer.toString(Config.MAXIMUM_ONLINE_USERS));
					content = content.replace("%serverruntime%", getServerRunTime());
					if (SmartCommunityConfigs.ALLOW_REAL_ONLINE_STATS)
					{
						content = content.replace("%serveronline%", getRealOnline());
					}
					else
					{
						content = content.replace("%serveronline%", "");
					}
					break;
				default:
					break;
			}
			if (file.startsWith("raid"))
			{
				String rfid = file.substring(4);
				RaidList rd = new RaidList(rfid);
				content = content.replaceAll("%raidlist%", rd.loadRaidList());
			}
			if (content.isEmpty())
			{
				content = "<html><body><br><br><center>404 :File not found or empty: " + filepath + " your command is " + command + "</center></body></html>";
			}
			separateAndSend(content, activeChar);
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}
	
	public String getServerRunTime()
	{
		int timeSeconds = GameTimeController.getInstance().getServerRunTime();
		String timeResult = "";
		if (timeSeconds >= 86400)
		{
			timeResult = Integer.toString(timeSeconds / 86400) + " Days " + Integer.toString((timeSeconds % 86400) / 3600) + " hours";
		}
		else
		{
			timeResult = Integer.toString(timeSeconds / 3600) + " Hours " + Integer.toString((timeSeconds % 3600) / 60) + " mins";
		}
		return timeResult;
	}
	
	public String getRealOnline()
	{
		int counter = 0;
		for (L2PcInstance onlinePlayer : L2World.getInstance().getPlayers())
		{
			if (onlinePlayer.isOnline() && ((onlinePlayer.getClient() != null) && !onlinePlayer.getClient().isDetached()))
			{
				counter++;
			}
		}
		
		int allPlayers = L2World.getInstance().getAllPlayersCount();
		
		if (SmartCommunityConfigs.EXTRA_PLAYERS_COUNT > 0)
		{
			counter += SmartCommunityConfigs.EXTRA_PLAYERS_COUNT;
			allPlayers += SmartCommunityConfigs.EXTRA_PLAYERS_COUNT;
		}
		
		String realOnline = "<table border=0 cellspacing=0 width=\"740\" cellpadding=2 bgcolor=111111><tr><td fixwidth=11></td><td FIXWIDTH=280>Players Active</td><td FIXWIDTH=470><font color=26e600>" + counter + "</font></td></tr></table><img src=\"l2ui.squaregray\" width=\"740\" height=\"1\"><table border=0 cellspacing=0 width=\"740\" cellpadding=2 bgcolor=111111><tr><td fixwidth=11></td><td FIXWIDTH=280>Players Shops</td><td FIXWIDTH=470><font color=26e600>" + (allPlayers - counter) + "</font></td></tr></table>";
		return realOnline;
	}
	
	@Override
	public void parsewrite(String url, String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		
	}
	
	public static TopBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TopBBSManager _instance = new TopBBSManager();
	}
}