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
package com.l2jserver.gameserver.communitybbs.Manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.gameserver.network.serverpackets.ShowBoard;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * Created by LordWinter Fixed by RobikBobik
 */
public class TeleportBBSManager extends BaseBBSManager
{
	
	public class CBteleport
	{
		public int TpId = 0;
		public String TpName = "";
		public int PlayerId = 0;
		public int xC = 0;
		public int yC = 0;
		public int zC = 0;
	}
	
	private static TeleportBBSManager _Instance = null;
	
	public static TeleportBBSManager getInstance()
	{
		if (_Instance == null)
		{
			_Instance = new TeleportBBSManager();
		}
		return _Instance;
	}
	
	public String points[][];
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.equals("_bbsteleport;"))
		{
			showTp(activeChar);
		}
		else if (command.startsWith("_bbsteleport;delete;"))
		{
			StringTokenizer stDell = new StringTokenizer(command, ";");
			stDell.nextToken();
			stDell.nextToken();
			int TpNameDell = Integer.parseInt(stDell.nextToken());
			delTp(activeChar, TpNameDell);
			showTp(activeChar);
		}
		else if (command.startsWith("_bbsteleport;save;"))
		{
			StringTokenizer stAdd = new StringTokenizer(command, ";");
			stAdd.nextToken();
			stAdd.nextToken();
			String TpNameAdd = stAdd.nextToken();
			AddTp(activeChar, TpNameAdd);
			showTp(activeChar);
		}
		else if (command.startsWith("_bbsteleport;teleport;"))
		{
			StringTokenizer stGoTp = new StringTokenizer(command, " ");
			stGoTp.nextToken();
			int xTp = Integer.parseInt(stGoTp.nextToken());
			int yTp = Integer.parseInt(stGoTp.nextToken());
			int zTp = Integer.parseInt(stGoTp.nextToken());
			int priceTp = Integer.parseInt(stGoTp.nextToken());
			goTp(activeChar, xTp, yTp, zTp, priceTp);
			showTp(activeChar);
			separateAndSend(null, activeChar);
			activeChar.sendPacket(new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "00"));
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}
	
	private void goTp(L2PcInstance activeChar, int xTp, int yTp, int zTp, int priceTp)
	{
		if (activeChar.isDead() || activeChar.isAlikeDead() || activeChar.isCastingNow() || activeChar.isInCombat() || activeChar.isAttackingNow() || activeChar.isInOlympiadMode() || activeChar.isJailed() || activeChar.isFlying() || (activeChar.getKarma() > 0) || activeChar.isInDuel())
		{
			activeChar.sendMessage("Teleportation is not possible now.");
			return;
		}
		
		if ((priceTp > 0) && (activeChar.getAdena() < priceTp))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}
		else if (priceTp > 0)
		{
			activeChar.reduceAdena("Teleport", priceTp, activeChar, true);
		}
		activeChar.teleToLocation(xTp, yTp, zTp);
	}
	
	private void showTp(L2PcInstance activeChar)
	{
		CBteleport tp;
		try (Connection con = ConnectionFactory.getInstance().getConnection())
		{
			PreparedStatement st = con.prepareStatement("SELECT * FROM comteleport WHERE charId=?;");
			st.setLong(1, activeChar.getObjectId());
			ResultSet rs = st.executeQuery();
			StringBuilder html = new StringBuilder();
			html.append("<table width=220>");
			while (rs.next())
			{
				
				tp = new CBteleport();
				tp.TpId = rs.getInt("TpId");
				tp.TpName = rs.getString("name");
				tp.PlayerId = rs.getInt("charId");
				tp.xC = rs.getInt("xPos");
				tp.yC = rs.getInt("yPos");
				tp.zC = rs.getInt("zPos");
				html.append("<tr>");
				html.append("<td>");
				html.append("<button value=\"" + tp.TpName + "\" action=\"bypass -h _bbsteleport;teleport; " + tp.xC + " " + tp.yC + " " + tp.zC + " " + 100000 + "\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				html.append("</td>");
				html.append("<td>");
				html.append("<button value=\"Delete\" action=\"bypass -h _bbsteleport;delete;" + tp.TpId + "\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				html.append("</td>");
				html.append("</tr>");
			}
			html.append("</table>");
			
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile(activeChar.getLang(), "data/html/CommunityBoard/8.htm");
			adminReply.replace("%tp%", html.toString());
			separateAndSend(adminReply.getHtml(), activeChar);
			return;
		}
		catch (Exception e)
		{
		}
		
	}
	
	private void delTp(L2PcInstance activeChar, int TpNameDell)
	{
		try (Connection conDel = ConnectionFactory.getInstance().getConnection())
		{
			PreparedStatement stDel = conDel.prepareStatement("DELETE FROM comteleport WHERE charId=? AND TpId=?;");
			stDel.setInt(1, activeChar.getObjectId());
			stDel.setInt(2, TpNameDell);
			stDel.execute();
		}
		catch (Exception e)
		{
		}
	}
	
	private void AddTp(L2PcInstance activeChar, String TpNameAdd)
	{
		if (TpNameAdd.equals("") || TpNameAdd.equals(null))
		{
			activeChar.sendMessage("You didn't enter a tab name.");
			return;
		}
		
		if (activeChar.isDead() || activeChar.isAlikeDead() || activeChar.isCastingNow() || activeChar.isAttackingNow())
		{
			activeChar.sendMessage("You can't to save a tab in your status.");
			return;
		}
		
		if (activeChar.isInCombat())
		{
			activeChar.sendMessage("You can't to save a tab in combat.");
			return;
		}
		
		if (activeChar.isInsideZone(ZoneId.SWAMP) || activeChar.isInsideZone(ZoneId.LANDING) || activeChar.isInsideZone(ZoneId.MONSTER_TRACK) || activeChar.isInsideZone(ZoneId.CASTLE) || activeChar.isInsideZone(ZoneId.MOTHER_TREE) || activeChar.isInsideZone(ZoneId.SCRIPT)
			|| activeChar.isInsideZone(ZoneId.JAIL) || activeChar.isFlying())
		{
			activeChar.sendMessage("You can't to save this location.");
			return;
		}
		
		try (Connection con = ConnectionFactory.getInstance().getConnection())
		{
			PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM comteleport WHERE charId=?;");
			st.setLong(1, activeChar.getObjectId());
			ResultSet rs = st.executeQuery();
			rs.next();
			if (rs.getInt(1) <= 9)
			{
				PreparedStatement st1 = con.prepareStatement("SELECT COUNT(*) FROM comteleport WHERE charId=? AND name=?;");
				st1.setLong(1, activeChar.getObjectId());
				st1.setString(2, TpNameAdd);
				ResultSet rs1 = st1.executeQuery();
				rs1.next();
				if (rs1.getInt(1) == 0)
				{
					PreparedStatement stAdd = con.prepareStatement("INSERT INTO comteleport (charId,xPos,yPos,zPos,name) VALUES(?,?,?,?,?)");
					stAdd.setInt(1, activeChar.getObjectId());
					stAdd.setInt(2, activeChar.getX());
					stAdd.setInt(3, activeChar.getY());
					stAdd.setInt(4, activeChar.getZ());
					stAdd.setString(5, TpNameAdd);
					stAdd.execute();
				}
				else
				{
					PreparedStatement stAdd = con.prepareStatement("UPDATE comteleport SET xPos=?, yPos=?, zPos=? WHERE charId=? AND name=?;");
					stAdd.setInt(1, activeChar.getObjectId());
					stAdd.setInt(2, activeChar.getX());
					stAdd.setInt(3, activeChar.getY());
					stAdd.setInt(4, activeChar.getZ());
					stAdd.setString(5, TpNameAdd);
					stAdd.execute();
				}
			}
			else
			{
				activeChar.sendMessage("You can't save more than ten tabs.");
			}
			
		}
		catch (Exception e)
		{
		}
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}
}