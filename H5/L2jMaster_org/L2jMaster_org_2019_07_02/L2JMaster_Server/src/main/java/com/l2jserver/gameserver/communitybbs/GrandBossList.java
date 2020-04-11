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

public class GrandBossList
{
	private final StringBuilder _GrandBossList = new StringBuilder();
	
	public GrandBossList()
	{
		loadFromDB();
	}
	
	@SuppressWarnings(
	{
		"deprecation",
	})
	private void loadFromDB()
	{
		Connection con = null;
		int pos = 0;
		
		try
		{
			con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT boss_id, status FROM grandboss_data");
			ResultSet result = statement.executeQuery();
			
			nextnpc:
			while (result.next())
			{
				int npcid = result.getInt("boss_id");
				int status = result.getInt("status");
				if ((npcid == 29066) || (npcid == 29067))
				{
					continue nextnpc;
				}
				
				PreparedStatement statement2 = con.prepareStatement("SELECT name FROM npc WHERE id=" + npcid);
				ResultSet result2 = statement2.executeQuery();
				
				while (result2.next())
				{
					pos++;
					boolean rstatus = false;
					if (status == 0)
					{
						rstatus = true;
					}
					String npcname = result2.getString("name");
					addGrandBossToList(pos, npcname, rstatus);
				}
				result2.close();
				statement2.close();
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
	
	private void addGrandBossToList(int pos, String npcname, boolean rstatus)
	{
		_GrandBossList.append("<table border=0 cellspacing=0 cellpadding=2>");
		_GrandBossList.append("<tr>");
		_GrandBossList.append("<td FIXWIDTH=5></td>");
		_GrandBossList.append("<td FIXWIDTH=50>" + pos + "</td>");
		_GrandBossList.append("<td FIXWIDTH=130>" + npcname + "</td>");
		_GrandBossList.append("<td FIXWIDTH=60 align=center>" + ((rstatus) ? "<font color=99FF00>Alive</font>" : "<font color=CC0000>Dead</font>") + "</td>");
		_GrandBossList.append("<td FIXWIDTH=5></td>");
		_GrandBossList.append("</tr>");
		_GrandBossList.append("</table>");
		_GrandBossList.append("<img src=\"L2UI.Squaregray\" width=\"250\" height=\"1\">");
	}
	
	public String loadGrandBossList()
	{
		return _GrandBossList.toString();
	}
}