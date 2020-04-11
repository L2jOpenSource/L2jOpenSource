package main.engine.admin;

import java.util.StringTokenizer;

import com.l2jfrozen.gameserver.network.clientpackets.Say2;

import main.data.memory.WorldData;
import main.data.properties.ConfigData;
import main.engine.AbstractMod;
import main.holders.WorldHolder;
import main.holders.objects.PlayerHolder;
import main.util.UtilMessage;
import main.util.builders.html.Html;
import main.util.builders.html.HtmlBuilder;
import main.util.builders.html.HtmlBuilder.HtmlType;
import main.util.builders.html.L2UI_CH3;

/**
 * @author fissban
 */
public class PanelAdmin extends AbstractMod
{
	public PanelAdmin()
	{
		registerMod(true);
	}
	
	@Override
	public void onModState()
	{
		//
	}
	
	@Override
	public boolean onAdminCommand(PlayerHolder ph, String chat)
	{
		StringTokenizer st = new StringTokenizer(chat, " ");
		
		String command = st.nextToken();
		if (!command.equals("engine"))
		{
			return false;
		}
		
		String panel = st.hasMoreTokens() ? st.nextToken() : "general";
		
		switch (panel)
		{
			case "general":
			{
				HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
				hb.append("<html imgsrc=L2UI_CH3.credit_ch3_07><body><center>");
				hb.append(panelMain());
				hb.append(panelGeneral());
				hb.append("</center></body></html>");
				sendHtml(null, hb, ph);
				break;
			}
			case "fissban":
			{
				HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
				hb.append("<html imgsrc=L2UI_CH3.credit_ch3_07><body><center>");
				if (!ph.isSuperAdmin())
				{
					hb.append("<br><br>No tienes privilegios para acceder a esta zona!");
				}
				else
				{
					hb.append(panelMain());
					hb.append(panelSuperAdmin());
				}
				
				hb.append("</center></body></html>");
				sendHtml(null, hb, ph);
				// missing
				break;
			}
			case "VIP":
			case "AIO":
			{
				panelVipAndAio(ph, panel);
				break;
			}
			case "world":
			{
				panelWorld(ph);
				break;
			}
			case "teleport": // teletransporta al player a xyz
			{
				if (st.hasMoreTokens())
				{
					int x = Integer.parseInt(st.nextToken());
					int y = Integer.parseInt(st.nextToken());
					int z = Integer.parseInt(st.nextToken());
					
					ph.getInstance().teleToLocation(x, y, z, false);
				}
				sendHtmlFile(ph, null, "data/html/engine/panel/teleport/index.htm");
				break;
			}
			case "html":// muestra cualquier html dentro del engine
			{
				sendHtmlFile(ph, null, "data/html/engine/" + st.nextToken());
				break;
			}
			case "enterWorld":
			{
				int id = Integer.valueOf(st.nextToken());
				
				// hardcode
				ph.setWorldId(id);
				
				panelWorld(ph);
				break;
			}
			case "reloadConfigs":
			{
				// Reload configs
				ConfigData.load();
				// Send msg
				UtilMessage.sendCreatureMsg(ph, Say2.ANNOUNCEMENT, "[System]", "All configurations were reloaded");
				// Generate html
				HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
				hb.append("<html imgsrc=L2UI_CH3.credit_ch3_07><body><center>");
				hb.append(panelMain());
				hb.append(panelSuperAdmin());
				hb.append("</center></body></html>");
				// Send html
				sendHtml(null, hb, ph);
				break;
			}
		}
		
		return true;
	}
	
	// -----------------------------------------------------------------------------------------------------------------
	private static void panelVipAndAio(PlayerHolder ph, String option)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		
		hb.append("<html imgsrc=L2UI_CH3.credit_ch3_07><body><center>");
		hb.append(Html.head("ENGINE PANEL > AIO and VIP"));
		hb.append("<br>");
		hb.append(Html.fontColor("LEVEL", option));
		hb.append("<edit var=\"box\" width=200>");
		hb.append("* Requieren target");
		hb.append("<table>");
		hb.append("<tr>");
		hb.append("<td width=75><button value=\"SET ", option, "\" action=\"bypass -h admin_set", option, " ", "$box", "\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("<td width=75><button value=\"REMOVE ", option, "\" action=\"bypass -h admin_remove", option, "\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("<td width=75><button value=\"ALL ", option, "\" action=\"bypass -h admin_all", option, "\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("</tr>");
		hb.append("</table>");
		hb.append("</center></body></html>");
		
		sendHtml(null, hb, ph);
	}
	// -----------------------------------------------------------------------------------------------------------------
	
	private static String panelSuperAdmin()
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append("<br>");
		
		hb.append("<table>");
		hb.append("<tr>");
		hb.append("<td width=75><button value=Reload Configs action=\"bypass -h reloadConfigs\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("<td width=75></td>");
		hb.append("<td width=75></td>");
		hb.append("</tr>");
		hb.append("</table>");
		
		return hb.toString();
	}
	
	private static String panelGeneral()
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		
		hb.append("<br>");
		// 264
		hb.append("* Need target");
		
		hb.append("<table>");
		hb.append("<tr>");
		hb.append("<td width=75><button value=NCoins action=\"bypass -h admin_ncoins ", "$box", "\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("<td width=189><edit var=\"box\" width=189></td>");
		hb.append("</tr>");
		hb.append("</table>");
		
		hb.append("<br>");
		
		hb.append("<table>");
		hb.append("<tr>");
		hb.append("<td width=75><button value=VIP action=\"bypass -h admin_engine VIP\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("<td width=75><button value=AIO action=\"bypass -h admin_engine AIO\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("</tr>");
		hb.append("</table>");
		
		hb.append("<br>");
		
		hb.append("<table>");
		hb.append("<tr>");
		hb.append("<td width=75><button value=AllWorlds action=\"bypass -h admin_engine world\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("<td width=75><button value=AllNpcTemplate action=\"bypass -h admin_ant allNpcTemplate\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("<td width=75><button value=Balance action=\"bypass -h admin_balance\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("</tr>");
		hb.append("<tr>");
		hb.append("<td width=75><button value=Teleport action=\"bypass -h admin_engine html panel/teleport/index.htm\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("<td width=75></td>");
		hb.append("<td width=75></td>");
		hb.append("</tr>");
		hb.append("</table>");
		
		return hb.toString();
	}
	
	// -----------------------------------------------------------------------------------------------------------------
	
	private static String panelMain()
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		
		hb.append(Html.head("ENGINE PANEL"));
		hb.append("<br>");
		hb.append("<table>");
		hb.append("<tr>");
		hb.append("<td width=75><button value=General action=\"bypass -h admin_engine general\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("<td width=75><button value=Fissban action=\"bypass -h admin_engine fissban\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("</tr>");
		hb.append("</table>");
		return hb.toString();
	}
	
	// -----------------------------------------------------------------------------------------------------------------
	
	private static void panelWorld(PlayerHolder ph)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		
		hb.append("<html imgsrc=L2UI_CH3.credit_ch3_07><body><center>");
		hb.append(Html.head("ENGINE PANEL > WORLD"));
		hb.append("<br>");
		
		hb.append("<button value=\"Go World 0\" action=\"bypass -h Engine admin_enterWorld -1000\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		
		if (WorldData.getAll().isEmpty())
		{
			hb.append(Html.fontColor("LEVEL", "No worlds have been created"));
		}
		else
		{
			hb.append(Html.fontColor("LEVEL", "All worlds are created"));
			hb.append("<table>");
			for (WorldHolder w : WorldData.getAll())
			{
				hb.append("<tr>");
				hb.append("<td width=20>ID:</td>");
				hb.append("<td width=50>", w.getId(), "</td>");
				hb.append("<td><button value=Go action=\"bypass -h admin_engine enterWorld ", w.getId(), "\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
				hb.append("</tr>");
			}
			hb.append("</table>");
		}
		
		hb.append("<br>");
		hb.append("<button value=Return action=\"bypass -h admin_engine\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		
		hb.append("</center></body></html>");
		sendHtml(null, hb, ph);
	}
}
