/*
 * Copyright (C) 2004-2015 L2J Server
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
import l2r.gameserver.communitybbs.BoardsManager;
import l2r.gameserver.communitybbs.SunriseBoards.CastleStatus;
import l2r.gameserver.communitybbs.SunriseBoards.GrandBossList;
import l2r.gameserver.communitybbs.SunriseBoards.HeroeList;
import l2r.gameserver.communitybbs.SunriseBoards.RaidList;
import l2r.gameserver.communitybbs.SunriseBoards.TopClan;
import l2r.gameserver.communitybbs.SunriseBoards.TopOnlinePlayers;
import l2r.gameserver.communitybbs.SunriseBoards.TopPkPlayers;
import l2r.gameserver.communitybbs.SunriseBoards.TopPvpPlayers;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.ShowBoard;

import gr.sr.configsEngine.configs.impl.CustomServerConfigs;
import gr.sr.configsEngine.configs.impl.SmartCommunityConfigs;
import gr.sr.main.ClassNamesHolder;
import gr.sr.utils.Tools;

public class TopBBSManager extends BaseBBSManager
{
	@Override
	public void cbByPass(String command, L2PcInstance activeChar)
	{
		BoardsManager.getInstance().addBypass(activeChar, "Home", command);
		
		String path = "data/html/CommunityBoard/";
		String filepath = "";
		String content = "";
		
		if (command.equals("_bbstop") | command.equals("_bbshome"))
		{
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), path + "index.htm");
			content = replaceVars(activeChar, content);
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith("_bbstop_settings"))
		{
			String subCommand = command.split(" ")[1];
			switch (subCommand)
			{
				case "tradeprot":
					if (activeChar.getVarB("noTrade"))
					{
						activeChar.setVar("noTrade", "false");
						activeChar.sendMessage("Trade refusal mode disabled.");
					}
					else
					{
						activeChar.setVar("noTrade", "true");
						activeChar.sendMessage("Trade refusal mode enabled.");
					}
					break;
				case "changeexp":
					if (CustomServerConfigs.ALLOW_EXP_GAIN_COMMAND)
					{
						if (!activeChar.getVarB("noExp"))
						{
							activeChar.setVar("noExp", "true");
							activeChar.sendMessage("Experience gain enabled.");
						}
						else
						{
							activeChar.setVar("noExp", "false");
							activeChar.sendMessage("Experience gain disabled.");
						}
					}
					else
					{
						activeChar.sendMessage("Experience command disabled by a gm.");
					}
					break;
				case "nobuff":
					if (activeChar.getVarB("noBuff"))
					{
						activeChar.setVar("noBuff", "false");
						activeChar.sendMessage("Bad-buff protection disabled.");
					}
					else
					{
						activeChar.setVar("noBuff", "true");
						activeChar.sendMessage("Bad-buff protection enabled.");
					}
					break;
				case "enchantanime":
					if (activeChar.getVarB("showEnchantAnime"))
					{
						activeChar.setVar("showEnchantAnime", "false");
						activeChar.sendMessage("Enchant animation disabled.");
					}
					else
					{
						activeChar.setVar("showEnchantAnime", "true");
						activeChar.sendMessage("Enchant animation enabled.");
					}
					break;
				case "hidestores":
					if (activeChar.getVarB("hideStores"))
					{
						activeChar.setVar("hideStores", "false");
						activeChar.sendMessage("All stores are visible, please restart.");
					}
					else
					{
						activeChar.setVar("hideStores", "true");
						activeChar.sendMessage("All stores are invisible, please restart.");
					}
					break;
				case "shotsonenter":
					if (activeChar.getVarB("onEnterLoadSS"))
					{
						activeChar.setVar("onEnterLoadSS", "false");
						activeChar.sendMessage("On enter auto load shots disabled.");
					}
					else
					{
						activeChar.setVar("onEnterLoadSS", "true");
						activeChar.sendMessage("On enter auto load shots enabled.");
					}
					break;
				case "blockshotsanime":
					if (!activeChar.getVarB("hideSSAnime"))
					{
						activeChar.setVar("hideSSAnime", "true");
						activeChar.sendMessage("Broadcast shots animation enabled.");
					}
					else
					{
						activeChar.setVar("hideSSAnime", "false");
						activeChar.sendMessage("Broadcast shots animation disabled.");
					}
					break;
			}
			
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), path + "index.htm");
			content = replaceVars(activeChar, content);
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
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), filepath);
			
			if (content.isEmpty())
			{
				content = "<html><body><br><br><center>Content Empty: The command " + command + " points to an invalid or empty html file(" + filepath + ").</center></body></html>";
			}
			
			switch (file)
			{
				case "toppvp":
					content = content.replaceAll("%toppvp%", TopPvpPlayers.getInstance().getList());
					break;
				case "toppk":
					content = content.replaceAll("%toppk%", TopPkPlayers.getInstance().getList());
					break;
				case "topadena":
					content = content.replaceAll("%topadena%", TopPkPlayers.getInstance().getList());
					break;
				case "toponline":
					content = content.replaceAll("%toponline%", TopOnlinePlayers.getInstance().getList());
					break;
				case "topclan":
					content = content.replaceAll("%topclan%", TopClan.getInstance().getList());
					break;
				case "heroes":
					content = content.replaceAll("%heroelist%", HeroeList.getInstance().getList());
					break;
				case "castle":
					content = content.replaceAll("%castle%", CastleStatus.getInstance().getList());
					break;
				case "boss":
					content = content.replaceAll("%gboss%", GrandBossList.getInstance().getList());
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
				case "instances":
					// Queen Ant
					content = content.replace("%QASTATIC%", String.valueOf(Config.QUEEN_ANT_SPAWN_INTERVAL));
					content = content.replace("%QARANDOM%", String.valueOf(Config.QUEEN_ANT_SPAWN_RANDOM));
					// Antharas
					content = content.replace("%ANTHARASSTATIC%", String.valueOf(Config.ANTHARAS_SPAWN_INTERVAL));
					content = content.replace("%ANTHARASRANDOM%", String.valueOf(Config.ANTHARAS_SPAWN_RANDOM));
					// Valakas
					content = content.replace("%VALAKASSTATIC%", String.valueOf(Config.VALAKAS_SPAWN_INTERVAL));
					content = content.replace("%VALAKASRANDOM%", String.valueOf(Config.VALAKAS_SPAWN_RANDOM));
					// Baium
					content = content.replace("%BAIUMSTATIC%", String.valueOf(Config.BAIUM_SPAWN_INTERVAL));
					content = content.replace("%BAIUMRANDOM%", String.valueOf(Config.BAIUM_SPAWN_RANDOM));
					// Beleth
					content = content.replace("%BELETHSTATIC%", String.valueOf(Config.BELETH_SPAWN_INTERVAL));
					content = content.replace("%BELETHRANDOM%", String.valueOf(Config.BELETH_SPAWN_RANDOM));
					content = content.replace("%BELETHPLAYERS%", String.valueOf(Config.BELETH_MIN_PLAYERS) + "-" + String.valueOf(Config.BELETH_MIN_PLAYERS));
					// Epidos
					content = content.replace("%EPIDOSSTATIC%", String.valueOf(2));
					// Freya Normal
					content = content.replace("%FREYANORMALPLAYERS%", String.valueOf(Config.MIN_PLAYERS_TO_EASY) + "-" + String.valueOf(Config.MAX_PLAYERS_TO_EASY));
					// Freya Hard
					content = content.replace("%FREYAHARDPLAYERS%", String.valueOf(Config.MIN_PLAYERS_TO_HARD) + "-" + String.valueOf(Config.MAX_PLAYERS_TO_HARD));
					// Frintezza
					content = content.replace("%FRINTEZZAPLAYERS%", String.valueOf(Config.MIN_PLAYER_TO_FE) + "-" + String.valueOf(Config.MAX_PLAYER_TO_FE));
					// Zaken Day 83
					content = content.replace("%ZAKENDAY83PLAYERS%", String.valueOf(Config.ZAKEN_MIN_MEMBERS_DAYTIME_83) + "-" + String.valueOf(Config.ZAKEN_MAX_MEMBERS_DAYTIME_83));
					// Zaken Day 60
					content = content.replace("%ZAKENDAY60PLAYERS%", String.valueOf(Config.ZAKEN_MIN_MEMBERS_DAYTIME_60) + "-" + String.valueOf(Config.ZAKEN_MAX_MEMBERS_DAYTIME_60));
					// Zaken Night
					content = content.replace("%ZAKENNIGHTPLAYERS%", String.valueOf(Config.ZAKEN_MINMEMBERS_NIGHTTIME) + "-" + String.valueOf(Config.ZAKEN_MAXMEMBERS_NIGHTTIME));
					// Tiat
					content = content.replace("%TIATPLAYERS%", String.valueOf(Config.MIN_PLAYER_TO_TIAT) + "-" + String.valueOf(Config.MAX_PLAYER_TO_TIAT));
					break;
				default:
					break;
			}
			if (file.startsWith("raid"))
			{
				String rfid = file.substring(4);
				RaidList.getInstance().load(rfid);
				content = content.replaceAll("%raidlist%", RaidList.getInstance().getList());
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
	
	private String replaceVars(L2PcInstance activeChar, String content)
	{
		content = content.replace("%name%", activeChar.getName());
		content = content.replace("%class%", ClassNamesHolder.getClassName(activeChar.getActiveClass()));
		content = content.replace("%level%", String.valueOf(activeChar.getLevel()));
		content = content.replace("%clan%", String.valueOf(activeChar.getClan() != null ? "Yes" : "No"));
		content = content.replace("%noble%", String.valueOf(activeChar.isNoble() ? "Yes" : "No"));
		content = content.replace("%online_time%", Tools.convertMinuteToString(activeChar.getCurrentOnlineTime()));
		content = content.replace("%ip%", activeChar.getIPAddress());
		
		content = content.replace("%online%", Integer.toString(L2World.getInstance().getAllPlayersCount() + SmartCommunityConfigs.EXTRA_PLAYERS_COUNT));
		
		content = content.replace("%trade_status%", activeChar.getVarB("noTrade") ? "MP" : "HP");
		content = content.replace("%exp_status%", activeChar.getVarB("noExp") ? "MP" : "HP");
		content = content.replace("%buff_status%", activeChar.getVarB("noBuff") ? "MP" : "HP");
		content = content.replace("%enchant_anim_status%", activeChar.getVarB("showEnchantAnime") ? "MP" : "HP");
		content = content.replace("%shots_status%", activeChar.getVarB("onEnterLoadSS") ? "MP" : "HP");
		content = content.replace("%shots_anim_status%", activeChar.getVarB("hideSSAnime") ? "MP" : "HP");
		content = content.replace("%hide_stores_status%", activeChar.getVarB("hideStores") ? "MP" : "HP");
		return content;
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
		
		String realOnline = "<table border=0 cellspacing=0 width=\"680\" cellpadding=2 bgcolor=111111><tr><td fixwidth=11></td><td FIXWIDTH=280>Players Active</td><td FIXWIDTH=470><font color=26e600>" + counter + "</font></td></tr></table><img src=\"l2ui.squaregray\" width=\"680\" height=\"1\"><table border=0 cellspacing=0 width=\"680\" cellpadding=2 bgcolor=111111><tr><td fixwidth=11></td><td FIXWIDTH=280>Players Shops</td><td FIXWIDTH=470><font color=26e600>" + (allPlayers - counter) + "</font></td></tr></table>";
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