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

import com.l2jserver.commons.database.pool.impl.ConnectionFactory;

public class ClanList
{
	private final StringBuilder _clanList = new StringBuilder();
	
	public ClanList(int type)
	{
		loadFromDB(type);
	}
	
	@SuppressWarnings(
	{
		"deprecation"
	})
	private void loadFromDB(int type)
	{
		Connection con = null;
		int stpoint = 0;
		int results = 20;
		String castlename = "";
		String allystatus = "";
		String leadername = "";
		for (int count = 1; count != type; count++)
		{
			stpoint += 20;
		}
		
		try
		{
			con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT clan_id, clan_name, ally_name, leader_id, clan_level, reputation_score, hasCastle, ally_id FROM clan_data ORDER BY `clan_level` desc Limit " + stpoint + ", " + results);
			ResultSet result = statement.executeQuery();
			int pos = 0;
			
			while (result.next())
			{
				int clanid = result.getInt("leader_id");
				String clan = result.getString("clan_name");
				String ally = result.getString("ally_name");
				int clanleader = result.getInt("leader_id");
				int clanlevel = result.getInt("clan_level");
				int reputation = result.getInt("reputation_score");
				int hascastle = result.getInt("hasCastle");
				int allyid = result.getInt("ally_id");
				if (allyid != 0)
				{
					if (allyid == clanid)
					{
						allystatus = "Alliance Leader";
					}
					allystatus = "Affiliated Clan";
				}
				else
				{
					allystatus = "-";
					ally = "[no-ally]";
				}
				if (hascastle != 0)
				{
					PreparedStatement statement2 = con.prepareStatement("SELECT name FROM castle WHERE id=" + hascastle);
					ResultSet result2 = statement2.executeQuery();
					if (result2.next())
					{
						castlename = result2.getString("name");
					}
					result2.close();
					statement2.close();
				}
				else
				{
					castlename = "[none]";
				}
				PreparedStatement statement3 = con.prepareStatement("SELECT char_name FROM characters WHERE charId=" + clanleader);
				ResultSet result3 = statement3.executeQuery();
				
				if (result3.next())
				{
					leadername = result3.getString("char_name");
				}
				result3.close();
				statement3.close();
				pos++;
				addClanToList(pos, clan, ally, leadername, clanlevel, reputation, castlename, allystatus);
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
			ConnectionFactory.close(con);
		}
	}
	
	private void addClanToList(int pos, String clan, String ally, String leadername, int clanlevel, int reputation, String castlename, String allystatus)
	{
		_clanList.append("<table border=0 cellspacing=0 cellpadding=2 width=760>");
		_clanList.append("<tr>");
		_clanList.append("<td FIXWIDTH=5></td>");
		_clanList.append("<td FIXWIDTH=20>" + pos + "</td>");
		_clanList.append("<td FIXWIDTH=90>" + clan + "</td>");
		_clanList.append("<td FIXWIDTH=90>" + ally + "</td>");
		_clanList.append("<td FIXWIDTH=85>" + leadername + "</td>");
		_clanList.append("<td FIXWIDTH=45 align=center>" + clanlevel + "</td>");
		_clanList.append("<td FIXWIDTH=70 align=center>" + reputation + "</td>");
		_clanList.append("<td FIXWIDTH=50 align=center>" + castlename + "</td>");
		_clanList.append("<td FIXWIDTH=70 align=center>" + allystatus + "</td>");
		_clanList.append("<td FIXWIDTH=5></td>");
		_clanList.append("</tr>");
		_clanList.append("</table>");
		_clanList.append("<img src=\"L2UI.Squaregray\" width=\"740\" height=\"1\">");
	}
	
	public String loadClanList()
	{
		return _clanList.toString();
	}
}