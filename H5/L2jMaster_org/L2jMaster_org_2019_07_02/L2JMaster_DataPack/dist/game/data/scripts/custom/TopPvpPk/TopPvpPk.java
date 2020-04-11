/*
 * Copyright (C) 2004-2019 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package custom.TopPvpPk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @reworked MaGa
 */

public class TopPvpPk extends Quest
{
	private static final int NPC_ID = 37632;
	private static final boolean DEBUG = false;
	static List<TopPVP> TOP_PVP = new ArrayList<>();
	static List<TopPK> TOP_PK = new ArrayList<>();
	private static final long refreshTime = 5 * 60 * 1000;
	ResultSet rset;
	
	public TopPvpPk(int questid, String name, String descr)
	{
		super(questid, name, descr);
		addFirstTalkId(NPC_ID);
		addTalkId(NPC_ID);
		addStartNpc(NPC_ID);
		
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new reloadTop(), 1000, refreshTime);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "main.htm";
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		return "main.htm";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("toppvp"))
		{
			sendTopPVP(player);
		}
		else if (event.equalsIgnoreCase("toppk"))
		{
			sendTopPK(player);
		}
		return null;
	}
	
	private void sendTopPK(L2PcInstance player)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>TOP 25 PK</title><body><br><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><table width=260>");
		
		for (TopPK pk : TOP_PK)
		{
			String name = pk.getName();
			int pk1 = pk.getpk();
			tb.append("<tr><td><font color=\"00C3FF\">" + name + "</color>:</td><td><font color=\"32C332\">" + pk1 + "</color></td></tr>");
		}
		
		tb.append("</table><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br></center></body></html>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(NPC_ID);
		msg.setHtml(tb.toString());
		player.sendPacket(msg);
	}
	
	private void sendTopPVP(L2PcInstance player)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>TOP 25 PVP</title><body><br><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><table width=260>");
		
		for (TopPVP pvp : TOP_PVP)
		{
			String name = pvp.getName();
			int pvp1 = pvp.getpvp();
			tb.append("<tr><td><font color=\"00C3FF\">" + name + "</color>:</td><td><font color=\"32C332\">" + pvp1 + "</color></td></tr>");
		}
		
		tb.append("</table><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br></center></body></html>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(NPC_ID);
		msg.setHtml(tb.toString());
		player.sendPacket(msg);
	}
	
	public class reloadTop implements Runnable
	{
		@Override
		public void run()
		{
			
			TOP_PK.clear();
			TOP_PVP.clear();
			
			try (Connection con = ConnectionFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT char_name,pvpkills FROM characters WHERE pvpkills>0 and accesslevel=0 order by pvpkills desc limit 25"))
			{
				rset = statement.executeQuery();
				while (rset.next())
				{
					TopPVP pvp = new TopPVP();
					pvp.setTopPvp(rset.getString("char_name"), rset.getInt("pvpkills"));
					TOP_PVP.add(pvp);
				}
				
				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not restore top pvp: " + e.getMessage(), e);
			}
			
			try (Connection con = ConnectionFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT char_name,pkkills FROM characters WHERE pkkills>0 and accesslevel=0 order by pkkills desc limit 25"))
			{
				rset = statement.executeQuery();
				while (rset.next())
				{
					TopPK pk = new TopPK();
					pk.setTopPk(rset.getString("char_name"), rset.getInt("pkkills"));
					TOP_PK.add(pk);
				}
				
				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not restore top pk: " + e.getMessage(), e);
			}
			
			if (DEBUG)
			{
				_log.info("Top Pvp/Pk Loaded: " + TOP_PVP.size() + " Pvps");
				_log.info("Top Pvp/Pk Loaded: " + TOP_PK.size() + " Pks");
			}
		}
	}
	
	public class TopPK
	{
		private String getcharName = null;
		private int gettopPk = 0;
		
		void setTopPk(String char_name, int pkkills)
		{
			getcharName = char_name;
			gettopPk = pkkills;
		}
		
		String getName()
		{
			return getcharName;
		}
		
		int getpk()
		{
			return gettopPk;
		}
	}
	
	public class TopPVP
	{
		private String getcharName = null;
		private int gettopPvp = 0;
		
		void setTopPvp(String char_name, int pvpkills)
		{
			getcharName = char_name;
			gettopPvp = pvpkills;
		}
		
		String getName()
		{
			return getcharName;
		}
		
		int getpvp()
		{
			return gettopPvp;
		}
	}
	
	public static void main(String[] args)
	{
		new TopPvpPk(-1, "TopPvpPk", "custom");
		_log.info("Top PvP/Pk Npc: Enabled.");
	}
}