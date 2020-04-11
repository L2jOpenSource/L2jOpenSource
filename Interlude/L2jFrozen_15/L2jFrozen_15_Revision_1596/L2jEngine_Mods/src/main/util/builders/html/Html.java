package main.util.builders.html;

import java.util.Map;
import java.util.Map.Entry;

import main.util.builders.html.HtmlBuilder.HtmlType;

/**
 * @author fissban
 */
public class Html
{
	// Tags
	public static final String START = "<html><body>";
	public static final String END = "</body></html>";
	
	public static String fontColor(String color, int text)
	{
		return fontColor(color, text + "");
	}
	
	public static String fontColor(String color, String text)
	{
		return "<font color=\"" + color + "\">" + text + "</font>";
	}
	
	public static String image(String image, int width, int height)
	{
		return "<img src=" + image + " width=" + width + " height=" + height + ">";
	}
	
	public static String head(String name)
	{
		HtmlBuilder hb = new HtmlBuilder();
		hb.append("<center>");
		hb.append("<br>");
		hb.append(image(L2UI_CH3.br_bar2_mp, 264, 1));
		hb.append("<table width=264 height=32 bgcolor=\"000000\" cellspacing=0 cellpadding=0 border=0>");// 3A5B87
		hb.append("<tr>");
		hb.append("<td align=center>", name, "</td>");
		hb.append("</tr>");
		hb.append("</table>");
		hb.append(image(L2UI_CH3.br_bar2_mp, 264, 1));
		hb.append("</center>");
		
		return hb.toString();
	}
	
	public static String headCommunity(String name)
	{
		HtmlBuilder hb = new HtmlBuilder();
		hb.append("<center>");
		hb.append("<br>");
		hb.append(image(L2UI_CH3.br_bar2_mp, 600, 1));
		hb.append("<table width=600 height=32 bgcolor=\"000000\" cellspacing=0 cellpadding=0 border=0>");// 3A5B87
		hb.append("<tr>");
		hb.append("<td align=center>", name, "</td>");
		hb.append("</tr>");
		hb.append("</table>");
		hb.append(image(L2UI_CH3.br_bar2_mp, 600, 1));
		hb.append("</center>");
		return hb.toString();
	}
	
	private static final String[] POS =
	{
		L2UI_CH3.shortcut_f01,
		L2UI_CH3.shortcut_f02,
		L2UI_CH3.shortcut_f03,
		L2UI_CH3.shortcut_f04,
		L2UI_CH3.shortcut_f05,
		L2UI_CH3.shortcut_f06,
		L2UI_CH3.shortcut_f07,
		L2UI_CH3.shortcut_f08,
		L2UI_CH3.shortcut_f09,
		L2UI_CH3.shortcut_f10,
	};
	
	/**
	 * Html del ranking, usado en:<br>
	 * -> AllVsAll<br>
	 * -> TeamVsTeam<br>
	 * -> Survive<br>
	 * -> SearchChest<br>
	 * -> <br>
	 * @param  pointsOrdered
	 * @return
	 */
	public static HtmlBuilder eventRanking(Map<String, Integer> pointsOrdered)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append(START);
		hb.append(head("TOP 10"));
		hb.append("<center>");
		hb.append("<br>");
		
		hb.append(image(L2UI_CH3.br_bar2_mp, 280, 1));
		hb.append("<table width=280 bgcolor=\"000000\">");
		hb.append("<tr>");
		hb.append("<td width=16 align=center></td>");
		hb.append("<td width=214 align=center>", fontColor("LEVEL", "Player"), "</td>");
		hb.append("<td width=50 align=center>", fontColor("LEVEL", "Kills"), "</td>");
		hb.append("</tr>");
		hb.append("</table>");
		hb.append(image(L2UI_CH3.br_bar2_mp, 280, 1));
		
		int cont = 0;
		for (Entry<String, Integer> entry : pointsOrdered.entrySet())
		{
			cont++;
			hb.append("<table width=280", (cont % 2) == 0 ? " bgcolor=\"000000\">" : ">");
			hb.append("<tr>");
			hb.append("<td width=16 height=22 align=center>", image(POS[cont - 1], 16, 16), "</td>");
			hb.append("<td width=214 align=center>", entry.getKey(), "</td>");
			hb.append("<td width=50 align=center>", entry.getValue(), "</td>");
			hb.append("</tr>");
			hb.append("</table>");
			hb.append(image(L2UI_CH3.br_bar2_mp, 280, 1));
		}
		hb.append("</center>");
		hb.append(END);
		
		return hb;
	}
	
	/**
	 * A color is assigned according to the character's lvl
	 * @param  lvl
	 * @return
	 */
	public static String getClanColorLevel(int lvl)
	{
		HtmlBuilder hb = new HtmlBuilder();
		
		if ((lvl >= 2) && (lvl < 4))
		{
			hb.append(Html.fontColor(BgColor.YELLOW, lvl)); // yellow
		}
		else if ((lvl >= 4) && (lvl < 7))
		{
			hb.append(Html.fontColor("9A5C00", lvl)); // orange
		}
		else if (lvl >= 7)
		{
			hb.append(Html.fontColor(BgColor.RED, lvl)); // red
		}
		else
		{
			hb.append(lvl);
		}
		
		return hb.toString();
	}
	
	/**
	 * Format Addena
	 */
	public static String formatAdena(int amount)
	{
		String s = "";
		int rem = amount % 1000;
		s = Integer.toString(rem);
		amount = (amount - rem) / 1000;
		while (amount > 0)
		{
			if (rem < 99)
			{
				s = '0' + s;
			}
			if (rem < 9)
			{
				s = '0' + s;
			}
			rem = amount % 1000;
			s = Integer.toString(rem) + "," + s;
			amount = (amount - rem) / 1000;
		}
		return s;
	}
	
	public static String convertInUTF8(String text)
	{
		String out = null;
		try
		{
			out = new String(text.getBytes("UTF-8"), "ISO-8859-1");
		}
		catch (Exception e)
		{
			return null;
		}
		
		return out;
	}
}
