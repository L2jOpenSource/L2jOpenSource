package main.engine.stats;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.base.ClassId;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.skills.funcs.LambdaStats.StatsType;

import main.engine.AbstractMod;
import main.holders.objects.CharacterHolder;
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
public class StatsPlayer extends AbstractMod
{
	private enum BonusType
	{
		NORMAL,
		HERO,
		NOBLE,
		OLY
	}
	
	private static class StatsHolder
	{
		private final Map<BonusType, LinkedHashMap<Stats, Integer>> stats = new LinkedHashMap<>();
		
		public StatsHolder()
		{
			initBonus();
		}
		
		private void initBonus()
		{
			// inicializamos todos los stats
			for (BonusType bt : BonusType.values())
			{
				for (Stats sts : Stats.values())
				{
					if (!stats.containsKey(bt))
					{
						stats.put(bt, new LinkedHashMap<>());
					}
					
					stats.get(bt).put(sts, 0);
				}
			}
		}
		
		public void setBonus(BonusType type, Stats stat, int bonus)
		{
			stats.get(type).put(stat, bonus);
		}
		
		public int getBonus(BonusType type, Stats stat)
		{
			return stats.get(type).get(stat);
		}
		
		public LinkedHashMap<Stats, Integer> getAllBonus(BonusType type)
		{
			return stats.get(type);
		}
		
		public void increaseBonus(BonusType type, Stats stat)
		{
			int oldBonus = stats.get(type).get(stat);
			stats.get(type).put(stat, oldBonus + 1);
		}
		
		public void decreaseBonus(BonusType type, Stats stat)
		{
			int oldBonus = stats.get(type).get(stat);
			stats.get(type).put(stat, oldBonus - 1);
		}
	}
	
	private static final Map<String, StatsHolder> classStats = new HashMap<>();
	
	public StatsPlayer()
	{
		registerMod(true);
	}
	
	@Override
	public void onModState()
	{
		switch (getState())
		{
			case START:
				loadValuesFromDb();
				initStats();
				break;
			case END:
				//
				break;
		}
	}
	
	private void initStats()
	{
		// for all ---------------------------------------------------------------------------------------
		
		classStats.put("all", new StatsHolder());
		
		for (BonusType bt : BonusType.values())
		{
			String values = getValueDB(99999, bt.name()).getString(); // 99999 for all
			
			if (values != null)
			{
				for (String split : values.split(";"))
				{
					String[] parse = split.split(",");
					
					Stats stat = Stats.valueOf(parse[0]);
					int bonus = Integer.parseInt(parse[1]);
					
					classStats.get("all").setBonus(bt, stat, bonus);
				}
			}
		}
		
		// for class ---------------------------------------------------------------------------------------
		for (ClassId cs : ClassId.values())
		{
			// solo los de 3ra clase vamos a balancear
			if (cs.level() < 3)
			{
				continue;
			}
			
			classStats.put(cs.name(), new StatsHolder());
			
			for (BonusType bt : BonusType.values())
			{
				// en lugar de usar el objectId usaremos el id de la clase para almacenar en la DB.
				String values = getValueDB(cs.getId(), bt.name()).getString();
				
				if (values != null)
				{
					for (String split : values.split(";"))
					{
						String[] parse = split.split(",");
						
						Stats stat = Stats.valueOf(parse[0]);
						int bonus = Integer.parseInt(parse[1]);
						
						classStats.get(cs.name()).setBonus(bt, stat, bonus);
					}
				}
			}
		}
	}
	
	@Override
	public boolean onAdminCommand(PlayerHolder player, String chat)
	{
		if (chat.equals("balance"))
		{
			htmlIndexClass(player);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean onCommunityBoard(PlayerHolder player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, ",");
		// bbshome
		String event = st.nextToken();
		
		if (!event.equals("_bbshome"))
		{
			return false;
		}
		if (!st.hasMoreTokens())
		{
			return false;
		}
		
		event = st.nextToken();
		if (event.equals("balance"))
		{
			htmlIndexClass(player);
			return true;
		}
		if (event.equals("class"))
		{
			String className = st.nextToken();
			BonusType bonusType = st.hasMoreTokens() ? BonusType.valueOf(st.nextToken()) : BonusType.NORMAL;
			int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
			
			htmlIndex(player, className, bonusType, page);
			
			return true;
		}
		if (event.equals("modified"))
		{
			String className = st.nextToken();
			BonusType bonusType = BonusType.valueOf(st.nextToken());
			Stats stat = Stats.valueOf(st.nextToken());
			String type = st.nextToken(); // add - sub
			
			// page
			int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
			
			switch (type)
			{
				case "add":
					classStats.get(className).increaseBonus(bonusType, stat);
					break;
				case "sub":
					classStats.get(className).decreaseBonus(bonusType, stat);
					break;
			}
			
			String parse = "";
			for (Entry<Stats, Integer> map : classStats.get(className).getAllBonus(bonusType).entrySet())
			{
				parse += map.getKey().name() + "," + map.getValue() + ";";
			}
			
			setValueDB(className.equals("all") ? 99999 : ClassId.valueOf(className).getId(), bonusType.name(), parse);
			
			htmlIndex(player, className, bonusType, page);
			
			return true;
		}
		return false;
	}
	
	private static void htmlIndexClass(PlayerHolder player)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		hb.append(Html.START);
		hb.append("<br>");
		hb.append("<center>");
		
		hb.append("Selecciona la clase a la que quieres ajustar su balance<br>");
		hb.append("<br>");
		
		hb.append("<td><button value=ALL action=\"bypass _bbshome,class,all\" width=93 height=22 back=", L2UI_CH3.bigbutton_down, " fore=", L2UI_CH3.bigbutton, "></td>");
		hb.append("<br>");
		hb.append(Html.fontColor("LEVEL", "HUMAN"));
		hb.append("<table bgcolor=000000>");
		hb.append("<tr>");
		hb.append(buttonClassId(ClassId.Duelist));
		hb.append(buttonClassId(ClassId.Dreadnought));
		hb.append(buttonClassId(ClassId.Phoenix_Knight));
		hb.append(buttonClassId(ClassId.Hell_Knight));
		hb.append(buttonClassId(ClassId.Sagittarius));
		hb.append(buttonClassId(ClassId.Adventurer));
		hb.append("</tr>");
		hb.append("<tr>");
		hb.append(buttonClassId(ClassId.Archmage));
		hb.append(buttonClassId(ClassId.Soultaker));
		hb.append(buttonClassId(ClassId.Arcana_Lord));
		hb.append(buttonClassId(ClassId.Cardinal));
		hb.append(buttonClassId(ClassId.Hierophant));
		hb.append("<td></td>");
		hb.append("</tr>");
		hb.append("</table>");
		// --------------------------------------------------------------------------------
		hb.append(Html.fontColor("LEVEL", "ELF"));
		hb.append("<table bgcolor=000000>");
		hb.append("<tr>");
		hb.append(buttonClassId(ClassId.Evas_Templar));
		hb.append(buttonClassId(ClassId.Sword_Muse));
		hb.append(buttonClassId(ClassId.Wind_Rider));
		hb.append(buttonClassId(ClassId.Moonlight_Sentinel));
		hb.append(buttonClassId(ClassId.Mystic_Muse));
		hb.append(buttonClassId(ClassId.Elemental_Master));
		hb.append("</tr>");
		hb.append("<tr>");
		hb.append(buttonClassId(ClassId.Evas_Saint));
		hb.append("<td></td>");
		hb.append("<td></td>");
		hb.append("<td></td>");
		hb.append("<td></td>");
		hb.append("</tr>");
		hb.append("</table>");
		// --------------------------------------------------------------------------------
		hb.append(Html.fontColor("LEVEL", "DARK ELF"));
		hb.append("<table bgcolor=000000>");
		hb.append("<tr>");
		hb.append(buttonClassId(ClassId.Shillien_Templar));
		hb.append(buttonClassId(ClassId.Spectral_Dancer));
		hb.append(buttonClassId(ClassId.Ghost_Hunter));
		hb.append(buttonClassId(ClassId.Ghost_Sentinel));
		hb.append(buttonClassId(ClassId.Storm_Screamer));
		hb.append(buttonClassId(ClassId.Spectral_Master));
		hb.append("</tr>");
		hb.append("<tr>");
		hb.append(buttonClassId(ClassId.Shillien_Saint));
		hb.append("<td></td>");
		hb.append("<td></td>");
		hb.append("<td></td>");
		hb.append("<td></td>");
		hb.append("</tr>");
		hb.append("</table>");
		// --------------------------------------------------------------------------------
		hb.append(Html.fontColor("LEVEL", "ORC"));
		hb.append("<table bgcolor=000000>");
		hb.append("<tr>");
		hb.append(buttonClassId(ClassId.Titan));
		hb.append(buttonClassId(ClassId.GrandKhauatari));
		hb.append(buttonClassId(ClassId.Dominator));
		hb.append(buttonClassId(ClassId.Doomcryer));
		hb.append("</tr>");
		hb.append("</table>");
		// --------------------------------------------------------------------------------
		hb.append(Html.fontColor("LEVEL", "DWARF"));
		hb.append("<table bgcolor=000000>");
		hb.append("<tr>");
		hb.append(buttonClassId(ClassId.Fortune_Seeker));
		hb.append(buttonClassId(ClassId.Maestro));
		hb.append("</tr>");
		hb.append("</table>");
		
		hb.append("<br>");
		hb.append("</center>");
		hb.append(Html.END);
		sendCommunity(player, hb.toString());
	}
	
	private static String buttonClassId(ClassId classId)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append("<td><button value=", classId.toString().replace("_", " ").toLowerCase(), " action=\"bypass _bbshome,class,", classId.name(), "\" width=93 height=22 back=", L2UI_CH3.bigbutton_down, " fore=", L2UI_CH3.bigbutton, "></td>");
		return hb.toString();
	}
	
	private static void htmlIndex(PlayerHolder player, String className, BonusType bonusType, int page)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		hb.append(Html.START);
		hb.append("<br>");
		hb.append("<center>");
		
		hb.append("<button value=INDEX action=\"bypass _bbshome,balance\" width=93 height=22 back=", L2UI_CH3.bigbutton_down, " fore=", L2UI_CH3.bigbutton, ">");
		hb.append("<br>");
		hb.append(Html.headCommunity(className));
		hb.append("<br>");
		hb.append("<table width=460 height=22>");
		hb.append("<tr>");
		for (BonusType bt : BonusType.values())
		{
			hb.append("<td><button value=", bt.name(), " action=\"bypass _bbshome,class,", className, ",", bt.name(), "\" width=93 height=22 back=", L2UI_CH3.bigbutton_down, " fore=", L2UI_CH3.bigbutton, "></td>");
		}
		hb.append("</tr>");
		hb.append("</table>");
		
		hb.append("<br>");
		int MAX_PER_PAGE = 13;
		int searchPage = MAX_PER_PAGE * (page - 1);
		int count = 0;
		int color = 0;
		
		for (Stats stat : Stats.values())
		{
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
			
			double value = classStats.get(className).getBonus(bonusType, stat);
			hb.append("<table width=460 height=22 ", (color % 2) == 0 ? "bgcolor=000000 " : "", "cellspacing=0 cellpadding=0>");
			hb.append("<tr>");
			hb.append("<td fixwidth=16 height=22 align=center>", Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16), "</td>");
			hb.append("<td width=100 height=22 align=center>", Html.fontColor("LEVEL", stat.toString().replace("_", " ").toLowerCase()), " </td>");
			hb.append("<td width=62 align=center>", value, "%</td>");
			hb.append("<td width=32><button action=\"bypass _bbshome,modified,", className, ",", bonusType.name(), ",", stat, ",add\" width=16 height=16 back=sek.cbui343 fore=sek.cbui343></td>");
			hb.append("<td width=32><button action=\"bypass _bbshome,modified,", className, ",", bonusType.name(), ",", stat, ",sub\" width=16 height=16 back=sek.cbui347 fore=sek.cbui347></td>");
			hb.append("</tr>");
			hb.append("</table>");
			hb.append(Html.image(L2UI.SquareGray, 460, 1));
			
			color++;
			count++;
		}
		
		int currentPage = 1;
		int size = StatsType.values().length;
		
		hb.append("<br>");
		hb.append("<table>");
		hb.append("<tr>");
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
					hb.append("<td width=20><a action=\"bypass _bbshome,class,", className, ",", bonusType.name(), ",", currentPage, "\">", currentPage, "</a></td>");
				}
				
				currentPage++;
			}
		}
		hb.append("</tr>");
		hb.append("</table>");
		
		hb.append("</center>");
		hb.append(Html.END);
		sendCommunity(player, hb.toString());
	}
	
	@Override
	public double onStats(Stats stat, CharacterHolder ch, double value)
	{
		if (!Util.areObjectType(L2PlayableInstance.class, ch))
		{
			return value;
		}
		
		L2PcInstance player = ch.getInstance().getActingPlayer();
		
		BonusType bonusType = BonusType.NORMAL;
		
		if (player.isInOlympiadMode())
		{
			bonusType = BonusType.OLY;
		}
		if (player.isNoble())
		{
			bonusType = BonusType.NOBLE;
		}
		if (player.isHero())
		{
			bonusType = BonusType.HERO;
		}
		
		if (classStats.containsKey(player.getClassId().name()))
		{
			value *= (classStats.get(player.getClassId().name()).getBonus(bonusType, stat) / 10.0) + 1.0;
		}
		
		return value * ((classStats.get("all").getBonus(bonusType, stat) / 100.0) + 1.0);
	}
}
