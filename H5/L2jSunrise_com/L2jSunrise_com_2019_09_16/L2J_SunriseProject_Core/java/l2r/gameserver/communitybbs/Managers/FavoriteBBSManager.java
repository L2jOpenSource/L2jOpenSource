/*
 * Copyright (C) 2004-2016 L2J Server
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

import l2r.L2DatabaseFactory;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.communitybbs.BoardsManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FavoriteBBSManager extends BaseBBSManager
{
	private static Logger _log = LoggerFactory.getLogger(FavoriteBBSManager.class);
	
	// SQL Queries
	private static final String SELECT_FAVORITES = "SELECT * FROM `bbs_favorites` WHERE `playerId`=? ORDER BY `favAddDate` DESC";
	private static final String DELETE_FAVORITE = "DELETE FROM `bbs_favorites` WHERE `playerId`=? AND `favId`=?";
	private static final String ADD_FAVORITE = "REPLACE INTO `bbs_favorites`(`playerId`, `favTitle`, `favBypass`) VALUES(?, ?, ?)";
	
	@Override
	public void cbByPass(String command, L2PcInstance activeChar)
	{
		// None of this commands can be added to favorites.
		if (command.startsWith("_bbsgetfav"))
		{
			// Load Favorite links
			final String list = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/favorite/favorite_list.html");
			final StringBuilder sb = new StringBuilder();
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(SELECT_FAVORITES))
			{
				ps.setInt(1, activeChar.getObjectId());
				try (ResultSet rs = ps.executeQuery())
				{
					while (rs.next())
					{
						String link = list.replaceAll("%fav_bypass%", String.valueOf(rs.getString("favBypass")));
						link = link.replaceAll("%fav_title%", rs.getString("favTitle"));
						final SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						link = link.replaceAll("%fav_add_date%", date.format(rs.getTimestamp("favAddDate")));
						link = link.replaceAll("%fav_id%", String.valueOf(rs.getInt("favId")));
						sb.append(link);
					}
				}
				String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/favorite/favorite.html");
				html = html.replaceAll("%fav_list%", sb.toString());
				separateAndSend(html, activeChar);
			}
			catch (Exception e)
			{
				_log.warn(FavoriteBBSManager.class.getSimpleName() + ": Couldn't load favorite links for player " + activeChar.getName());
			}
		}
		else if (command.startsWith("bbs_add_fav"))
		{
			final String bypass = BoardsManager.getInstance().removeBypass(activeChar);
			if (bypass != null)
			{
				final String[] parts = bypass.split("&", 2);
				if (parts.length != 2)
				{
					_log.warn(FavoriteBBSManager.class.getSimpleName() + ": Couldn't add favorite link, " + bypass + " it's not a valid bypass!");
					return;
				}
				
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement ps = con.prepareStatement(ADD_FAVORITE))
				{
					ps.setInt(1, activeChar.getObjectId());
					ps.setString(2, parts[0].trim());
					ps.setString(3, parts[1].trim());
					ps.execute();
					// Callback
					cbByPass("_bbsgetfav", activeChar);
				}
				catch (Exception e)
				{
					_log.warn(FavoriteBBSManager.class.getSimpleName() + ": Couldn't add favorite link " + bypass + " for player " + activeChar.getName());
				}
			}
		}
		else if (command.startsWith("_bbsdelfav_"))
		{
			final String favId = command.replaceAll("_bbsdelfav_", "");
			if (!Util.isDigit(favId))
			{
				_log.warn(FavoriteBBSManager.class.getSimpleName() + ": Couldn't delete favorite link, " + favId + " it's not a valid ID!");
				return;
			}
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(DELETE_FAVORITE))
			{
				ps.setInt(1, activeChar.getObjectId());
				ps.setInt(2, Integer.parseInt(favId));
				ps.execute();
				// Callback
				cbByPass("_bbsgetfav", activeChar);
			}
			catch (Exception e)
			{
				_log.warn(FavoriteBBSManager.class.getSimpleName() + ": Couldn't delete favorite link ID " + favId + " for player " + activeChar.getName());
			}
		}
	}
	
	@Override
	public void parsewrite(String url, String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	
	}
	
	public static FavoriteBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final FavoriteBBSManager _instance = new FavoriteBBSManager();
	}
}