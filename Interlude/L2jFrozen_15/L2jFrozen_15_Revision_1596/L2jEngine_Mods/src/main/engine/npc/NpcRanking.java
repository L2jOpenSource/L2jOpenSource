package main.engine.npc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.util.database.L2DatabaseFactory;

import main.engine.AbstractMod;
import main.holders.objects.CharacterHolder;
import main.holders.objects.NpcHolder;
import main.holders.objects.PlayerHolder;
import main.util.Util;
import main.util.builders.html.Html;
import main.util.builders.html.HtmlBuilder;
import main.util.builders.html.HtmlBuilder.HtmlType;
import main.util.builders.html.L2UI;
import main.util.builders.html.L2UI_CH3;

/**
 * @author fissban
 */
public class NpcRanking extends AbstractMod
{
	public class RankingHolder
	{
		String name;
		int kills;
	}
	
	private static final int NPC = 60008;
	// SQL
	private static final String SQL_PVP = "SELECT char_name,pvpkills FROM characters WHERE pvpkills>0 AND accesslevel=0 ORDER BY pvpkills DESC LIMIT 20";
	private static final String SQL_PK = "SELECT char_name,pkkills FROM characters WHERE pkkills>0 AND accesslevel=0 ORDER BY pkkills DESC LIMIT 20";
	// Rank
	public static final List<RankingHolder> rankingPvP = new ArrayList<>();
	public static final List<RankingHolder> rankingPk = new ArrayList<>();
	
	public NpcRanking()
	{
		registerMod(true);// TODO missing config
	}
	
	@Override
	public void onModState()
	{
		switch (getState())
		{
			case START:
				// pvp
				startTimer("loadRankingPvP", 0, null, null, false); // 0secs :P
				startTimer("loadRankingPvP", 60000, null, null, true); // 1min
				// pk
				startTimer("loadRankingPk", 0, null, null, false); // 0secs :P
				startTimer("loadRankingPk", 60000, null, null, true); // 1min
				break;
			case END:
				rankingPvP.clear();
				rankingPk.clear();
				cancelTimers("loadRankingPvP");
				cancelTimers("loadRankingPk");
				break;
		}
	}
	
	@Override
	public void onTimer(String timerName, NpcHolder npc, PlayerHolder player)
	{
		switch (timerName)
		{
			case "loadRankingPvP":
			{
				rankingPvP.clear();
				
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement(SQL_PVP);
					ResultSet rset = statement.executeQuery())
				{
					while (rset.next())
					{
						RankingHolder rh = new RankingHolder();
						rh.name = rset.getString("char_name");
						rh.kills = rset.getInt("pvpkills");
						
						rankingPvP.add(rh);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case "loadRankingPk":
			{
				rankingPk.clear();
				
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement(SQL_PK);
					ResultSet rset = statement.executeQuery())
				{
					while (rset.next())
					{
						RankingHolder rh = new RankingHolder();
						rh.name = rset.getString("char_name");
						rh.kills = rset.getInt("pkkills");
						
						rankingPk.add(rh);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
	@Override
	public boolean onInteract(PlayerHolder player, CharacterHolder npc)
	{
		if (!Util.areObjectType(L2NpcInstance.class, npc))
		{
			return false;
		}
		
		if (((NpcHolder) npc).getId() != NPC)
		{
			return false;
		}
		
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append(Html.START);
		hb.append(Html.head("RANKING"));
		hb.append("<br>");
		hb.append("Welcome my name is ", npc.getInstance().getName(), " and take care to meet the most famous players in the world.<br>");
		hb.append("You probably want to know who it is!<br>");
		hb.append("I actually have a list, I can show it to you if you want.<br>");
		hb.append("What would you like to see?<br>");
		hb.append("<center>");
		hb.append("<table width=280>");
		hb.append("<tr>");
		hb.append("<td>", Html.image(L2UI.bbs_folder, 32, 32), "</td>");
		hb.append("<td><button value=\"Top PvP\" action=\"bypass -h Engine NpcRanking pvp\" width=216 height=32 back=L2UI_CH5.UI_metro_orange2 fore=L2UI_CH5.UI_metro_orange1></td>");
		hb.append("<td>", Html.image(L2UI.bbs_folder, 32, 32), "</td>");
		hb.append("</tr>");
		hb.append("<tr>");
		hb.append("<td>", Html.image(L2UI.bbs_folder, 32, 32), "</td>");
		hb.append("<td><button value=\"Top PK\" action=\"bypass -h Engine NpcRanking pk\" width=216 height=32 back=L2UI_CH5.UI_metro_orange2 fore=L2UI_CH5.UI_metro_orange1></td>");
		hb.append("<td>", Html.image(L2UI.bbs_folder, 32, 32), "</td>");
		hb.append("</tr>");
		hb.append("</table>");
		hb.append("</center>");
		hb.append(Html.END);
		
		sendHtml((NpcHolder) npc, hb, player);
		return true;
	}
	
	@Override
	public void onEvent(PlayerHolder ph, CharacterHolder npc, String command)
	{
		if (!Util.areObjectType(L2NpcInstance.class, npc) || (((NpcHolder) npc).getId() != NPC))
		{
			return;
		}
		
		switch (command)
		{
			case "pvp":
				sendHtml(null, getRanking(rankingPvP, "PVP"), ph);
				break;
			
			case "pk":
				sendHtml(null, getRanking(rankingPk, "PK"), ph);
				break;
		}
	}
	
	private static HtmlBuilder getRanking(List<RankingHolder> ranking, String rankingName)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append(Html.START);
		hb.append(Html.head("RANKING " + rankingName));
		hb.append("<br>");
		hb.append("<table width=280>");
		hb.append("<tr>");
		hb.append("<td fixwidth=40><button value=Pos width=40 height=19 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("<td fixwidth=120><button value=Player width=120 height=19 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("<td fixwidth=120><button value=Kills width=120 height=19 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("</tr>");
		hb.append("</table>");
		
		int pos = 1;
		for (RankingHolder rh : ranking)
		{
			hb.append(Html.image(L2UI.SquareGray, 280, 1));
			hb.append("<table width=280>");
			hb.append("<tr>");
			hb.append("<td fixwidth=40 align=center><font color=F7D358>", pos, "</font></td>");
			hb.append("<td fixwidth=120 align=center>", rh.name, "</td>");
			hb.append("<td fixwidth=120 align=center>", rh.kills, "</td>");
			hb.append("</tr>");
			hb.append("</table>");
			
			pos++;
		}
		
		hb.append(Html.image(L2UI.SquareGray, 280, 1));
		hb.append("</center>");
		hb.append(Html.END);
		
		return hb;
	}
}
