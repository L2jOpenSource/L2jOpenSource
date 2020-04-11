package main.engine.community;

import java.util.StringTokenizer;

import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

import main.data.memory.ObjectData;
import main.data.properties.ConfigData;
import main.engine.AbstractMod;
import main.holders.objects.PlayerHolder;
import main.util.builders.html.Html;
import main.util.builders.html.HtmlBuilder;
import main.util.builders.html.HtmlBuilder.HtmlType;
import main.util.builders.html.L2UI;
import main.util.builders.html.L2UI_CH3;

/**
 * @author fissban
 */
public class RegionComunityBoard extends AbstractMod
{
	public RegionComunityBoard()
	{
		registerMod(ConfigData.ENABLE_BBS_REGION);
	}
	
	@Override
	public void onModState()
	{
		//
	}
	
	@Override
	public boolean onCommunityBoard(PlayerHolder ph, String command)
	{
		if (command.startsWith("_bbsloc"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			// bbsloc
			st.nextToken();
			// page
			int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
			
			HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
			hb.append(Html.START);
			hb.append("<br>");
			// head
			hb.append(Html.headCommunity("TOTAL ONLINE: " + L2World.getInstance().getAllPlayers().size()));
			hb.append("<br>");
			// body
			hb.append("<center>");
			hb.append("<table border=0 cellspacing=0 cellpadding=0>");
			hb.append("<tr>");
			hb.append("<td width=16 valign=top align=center height=22>", Html.image(L2UI_CH3.FrameBackMid, 16, 22), "</td>");
			// hb.append(topMenuList("", 16));
			hb.append(topMenuList("Name", 100));
			hb.append(topMenuList("Lvl", 30));
			
			hb.append(topMenuList("aio", 30));
			hb.append(topMenuList("vip", 30));
			
			hb.append(topMenuList("Class", 100));
			hb.append(topMenuList("Clan (Lvl.)", 100));
			hb.append(topMenuList("Town Region", 84));
			hb.append("<td width=16 valign=top align=center height=22>", Html.image(L2UI_CH3.FrameBackMid, 16, 22), "</td>");
			
			hb.append("</tr>");
			hb.append("</table>");
			
			// 582 MAX! de largo del html
			int MAX_PER_PAGE = 15;
			int searchPage = MAX_PER_PAGE * (page - 1);
			int count = 0;
			
			for (L2PcInstance pc : L2World.getInstance().getAllPlayers())
			{
				if (pc == null)
				{
					continue;
				}
				// min
				if (count < searchPage)
				{
					count++;
					continue;
				}
				// max
				if (count >= (searchPage + MAX_PER_PAGE))
				{
					continue;
				}
				
				hb.append("<table height=22 border=0 cellspacing=0 cellpadding=0>");
				hb.append("<tr>");
				hb.append("<td fixwidth=16 height=22 align=center>", getIconSex(pc.getAppearance().getSex()), "</td>");
				hb.append("<td fixwidth=100 align=center>", pc.getName(), pc.getName().equals("fissban") ? Html.fontColor("FF0000", "   ADMIN") : pc.getAccessLevel().getLevel() > 0 ? Html.fontColor("LEVEL", "   GM") : "", "</td>");
				hb.append("<td fixwidth=30 align=center>", getColorLevel(pc.getLevel()), "</td>");
				hb.append("<td fixwidth=30 align=center>", getIconStatus(ObjectData.get(PlayerHolder.class, pc).isAio()), "</td>");
				hb.append("<td fixwidth=30 align=center> ", getIconStatus(ObjectData.get(PlayerHolder.class, pc).isVip()), "</td>");
				hb.append("<td fixwidth=100 align=center> ", pc.getClassId().toString(), "</td>");
				hb.append("<td fixwidth=100 align=center> ", pc.getClan() != null ? pc.getClan().getName() + Html.fontColor("LEVEL", "  (" + pc.getClan().getLevel() + ")") : "No Clan", "</td>");
				hb.append("<td fixwidth=100 align=center> ", MapRegionTable.getInstance().getClosestTownName(pc), "</td>");
				hb.append("</tr>");
				hb.append("</table>");
				hb.append(Html.image(L2UI.SquareGray, 506, 1));
				count++;
			}
			
			hb.append("<br>");
			hb.append("<table border=0 cellspacing=0 cellpadding=0>");
			hb.append("<tr>");
			
			int currentPage = 1;
			int size = L2World.getInstance().getAllPlayers().size();
			for (int i = 0; i < size; i++)
			{
				if ((i % MAX_PER_PAGE) == 0)
				{
					if (currentPage == page)
					{
						hb.append("<td width=20>", Html.fontColor("LEVEL", currentPage), "</td>");
					}
					else
					{
						hb.append("<td width=20><a action=\"bypass _bbsloc ", currentPage, "\">", currentPage, "</a></td>");
					}
					currentPage++;
				}
			}
			hb.append("</tr>");
			hb.append("</table>");
			
			hb.append("</center>");
			hb.append(Html.END);
			sendCommunity(ph, hb.toString());
			
			return true;
		}
		return false;
		
	}
	
	// XXX MISC ----------------------------------------------------------------------------------------------------------------------
	
	private static String getIconStatus(boolean status)
	{
		if (status)
		{
			return Html.image(L2UI_CH3.QuestWndInfoIcon_5, 16, 16);
		}
		return "X";
	}
	
	/**
	 * 
	 */
	private static String getIconSex(boolean sex)
	{
		// positivo = male
		// negativo = female
		return sex ? Html.image(L2UI_CH3.msnicon4, 16, 16) : Html.image(L2UI_CH3.msnicon1, 16, 16);
	}
	
	/**
	 * Asignamos un color segun el nievel del personaje
	 * @param  lvl
	 * @return
	 */
	private static String getColorLevel(int lvl)
	{
		HtmlBuilder hb = new HtmlBuilder();
		
		if ((lvl >= 20) && (lvl < 40))
		{
			hb.append(Html.fontColor("LEVEL", lvl)); // amarillo
		}
		else if ((lvl >= 40) && (lvl < 76))
		{
			hb.append(Html.fontColor("9A5C00", lvl)); // naranja oscuro
		}
		else if (lvl >= 76)
		{
			hb.append(Html.fontColor("FF0000", lvl));// rojo
		}
		else
		{
			hb.append(lvl);
		}
		
		return hb.toString();
	}
	
	private static String topMenuList(String text, int widthMid)
	{
		return "<td fixwidth=" + widthMid + " align=center><button value=\"" + text + "\" width=" + widthMid + " height=22 back=" + L2UI_CH3.FrameBackMid + " fore=" + L2UI_CH3.FrameBackMid + "></td>";
	}
}
