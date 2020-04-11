/*
 * Copyright (C) 2004-2019 L2J Server
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
 package com.l2jserver.gameserver.communitybbs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.l2jserver.Config;
import com.l2jserver.commons.database.pool.impl.ConnectionFactory;

public class TopPlayers
{
	private int pos;
	private final StringBuilder _topList = new StringBuilder();
	String sort = "";
	
	public TopPlayers(String file)
	{
		loadDB(file);
	}
	
	private void loadDB(String file)
	{
		Connection con = null;
		
		switch (file)
		{
			case "toppvp":
				sort = "pvpkills";
				break;
			case "toppk":
				sort = "pkkills";
				break;
			case "topadena":
				sort = "SUM(it.count)";
				break;
			case "toprbrank":
				sort = "SUM(chr.points)";
				break;
			case "toponline":
				sort = "onlinetime";
				break;
			default:
				break;
			
		}
		
		try
		{
			pos = 0;
			con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT SUM(chr.points), SUM(it.count), ch.char_name, ch.pkkills, ch.pvpkills, ch.onlinetime, ch.base_class, ch.online FROM characters ch LEFT JOIN character_raid_points chr ON ch.charId=chr.charId LEFT OUTER JOIN items it ON ch.charId=it.owner_id WHERE item_id=57 GROUP BY ch.charId ORDER BY "
				+ sort + " DESC LIMIT " + Config.TOP_PLAYER_RESULTS);
			
			ResultSet result = statement.executeQuery();
			
			while (result.next())
			{
				boolean status = false;
				pos++;
				
				if (result.getInt("online") == 1)
				{
					status = true;
				}
				String timeon = getPlayerRunTime(result.getInt("ch.onlinetime"));
				String adenas = getAdenas(result.getInt("SUM(it.count)"));
				
				addChar(pos, result.getString("ch.char_name"), result.getInt("base_class"), result.getInt("ch.pvpkills"), result.getInt("ch.pkkills"), result.getInt("SUM(chr.points)"), adenas, timeon, status);
			}
			
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
				
			}
		}
	}
	
	public String loadTopList()
	{
		return _topList.toString();
	}
	
	private void addChar(int position, String name, int classid, int pvp, int pk, int raid, String adenas, String online, boolean isOnline)
	{
		_topList.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=050505 height=" + Config.TOP_PLAYER_ROW_HEIGHT + "><tr><td FIXWIDTH=5></td>");
		_topList.append("<td FIXWIDTH=20>" + position + ".</td>");
		_topList.append("<td FIXWIDTH=180>" + name + "</td>");
		_topList.append("<td FIXWIDTH=175>" + className(classid) + "</td>");
		_topList.append("<td FIXWIDTH=60>" + pvp + "</td>");
		_topList.append("<td FIXWIDTH=60>" + pk + "</td>");
		_topList.append("<td FIXWIDTH=65>" + raid + "</td>");
		_topList.append("<td FIXWIDTH=150>" + adenas + "</td>");
		_topList.append("<td FIXWIDTH=148>" + online + "</td>");
		_topList.append("<td FIXWIDTH=65>" + ((isOnline) ? "<font color=99FF00>Online</font>" : "<font color=CC0000>Offline</font>") + "</td>");
		_topList.append("</tr></table><img src=\"L2UI.Squaregray\" width=\"758\" height=\"1\">");
		
	}
	
	public final static String className(int classid)
	{
		Map<Integer, String> classList;
		classList = new HashMap<>();
		classList.put(0, "Fighter");
		classList.put(1, "Warrior");
		classList.put(2, "Gladiator");
		classList.put(3, "Warlord");
		classList.put(4, "Knight");
		classList.put(5, "Paladin");
		classList.put(6, "Dark Avenger");
		classList.put(7, "Rogue");
		classList.put(8, "Treasure Hunter");
		classList.put(9, "Hawkeye");
		classList.put(10, "Mage");
		classList.put(11, "Wizard");
		classList.put(12, "Sorcerer");
		classList.put(13, "Necromancer");
		classList.put(14, "Warlock");
		classList.put(15, "Cleric");
		classList.put(16, "Bishop");
		classList.put(17, "Prophet");
		classList.put(18, "Elven Fighter");
		classList.put(19, "Elven Knight");
		classList.put(20, "Temple Knight");
		classList.put(21, "Swordsinger");
		classList.put(22, "Elven Scout");
		classList.put(23, "Plains Walker");
		classList.put(24, "Silver Ranger");
		classList.put(25, "Elven Mage");
		classList.put(26, "Elven Wizard");
		classList.put(27, "Spellsinger");
		classList.put(28, "Elemental Summoner");
		classList.put(29, "Oracle");
		classList.put(30, "Elder");
		classList.put(31, "Dark Fighter");
		classList.put(32, "Palus Knightr");
		classList.put(33, "Shillien Knight");
		classList.put(34, "Bladedancer");
		classList.put(35, "Assasin");
		classList.put(36, "Abyss Walker");
		classList.put(37, "Phantom Ranger");
		classList.put(38, "Dark Mage");
		classList.put(39, "Dark Wizard");
		classList.put(40, "Spellhowler");
		classList.put(41, "Phantom Summoner");
		classList.put(42, "Shillien Oracle");
		classList.put(43, "Shilien Elder");
		classList.put(44, "Orc Fighter");
		classList.put(45, "Orc Raider");
		classList.put(46, "Destroyer");
		classList.put(47, "Orc Monk");
		classList.put(48, "Tyrant");
		classList.put(49, "Orc Mage");
		classList.put(50, "Orc Shaman");
		classList.put(51, "Overlord");
		classList.put(52, "Warcryer");
		classList.put(53, "Dwarven Fighter");
		classList.put(54, "Scavenger");
		classList.put(55, "Bounty Hunter");
		classList.put(56, "Artisan");
		classList.put(57, "Warsmith");
		classList.put(88, "Duelist");
		classList.put(89, "Dreadnought");
		classList.put(90, "Phoenix Knight");
		classList.put(91, "Hell Knight");
		classList.put(92, "Sagittarius");
		classList.put(93, "Adventurer");
		classList.put(94, "Archmage");
		classList.put(95, "Soultaker");
		classList.put(96, "Arcana Lord");
		classList.put(97, "Cardinal");
		classList.put(98, "Hierophant");
		classList.put(99, "Evas Templar");
		classList.put(100, "Sword Muse");
		classList.put(101, "Wind Rider");
		classList.put(102, "Moonlight Sentinel");
		classList.put(103, "Mystic Muse");
		classList.put(104, "Elemental Master");
		classList.put(105, "Evas Saint");
		classList.put(106, "Shillien Templar");
		classList.put(107, "Spectral Dancer");
		classList.put(108, "Ghost Hunter");
		classList.put(109, "Ghost Sentinel");
		classList.put(110, "Storm Screamer");
		classList.put(111, "Spectral Master");
		classList.put(112, "Shillien Saint");
		classList.put(113, "Titan");
		classList.put(114, "Grand Khavatari");
		classList.put(115, "Dominator");
		classList.put(116, "Doomcryer");
		classList.put(117, "Fortune Seeker");
		classList.put(118, "Maestro");
		classList.put(123, "Male Soldier");
		classList.put(124, "Female Soldier");
		classList.put(125, "Trooper");
		classList.put(126, "Warder");
		classList.put(127, "Berserker");
		classList.put(128, "Male Soulbreaker");
		classList.put(129, "Female Soulbreaker");
		classList.put(130, "Arbalester");
		classList.put(131, "Doombringer");
		classList.put(132, "Male Soulhound");
		classList.put(133, "Female Soulhound");
		classList.put(134, "Trickster");
		classList.put(135, "Inspector");
		classList.put(136, "Judicator");
		
		return classList.get(classid);
	}
	
	public String getPlayerRunTime(int secs)
	{
		String timeResult = "";
		if (secs >= 86400)
		{
			timeResult = Integer.toString(secs / 86400) + " Days " + Integer.toString((secs % 86400) / 3600) + " hours";
		}
		else
		{
			timeResult = Integer.toString(secs / 3600) + " Hours " + Integer.toString((secs % 3600) / 60) + " mins";
		}
		return timeResult;
	}
	
	public String getAdenas(int adena)
	{
		String adenas = "";
		if (adena >= 1000000000)
		{
			adenas = Integer.toString(adena / 1000000000) + " Billion " + Integer.toString((adena % 1000000000) / 1000000) + " million";
		}
		else
		{
			adenas = Integer.toString(adena / 1000000) + " Million " + Integer.toString((adena % 1000000) / 1000) + " k";
		}
		return adenas;
	}
}