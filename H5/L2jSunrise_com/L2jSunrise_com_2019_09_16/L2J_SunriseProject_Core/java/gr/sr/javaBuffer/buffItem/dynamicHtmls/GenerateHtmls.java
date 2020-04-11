package gr.sr.javaBuffer.buffItem.dynamicHtmls;

import java.util.List;

import l2r.gameserver.data.xml.impl.SkillIconData;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import gr.sr.javaBuffer.BufferMenuCategories;
import gr.sr.javaBuffer.BufferPacketCategories;
import gr.sr.javaBuffer.BufferPacketSender;
import gr.sr.javaBuffer.BuffsInstance;
import gr.sr.javaBuffer.PlayerMethods;
import gr.sr.javaBuffer.xml.dataHolder.BuffsHolder;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class GenerateHtmls
{
	/* Constants for dynamic htmls */
	private static final String _headHtml = "<html><title>Item Buffer</title><body><center>";
	private static final String _endHtml = "</center></body></html>";
	
	/**
	 * Shows the available profiles to edit
	 * @param player
	 * @param action
	 */
	public static void showSchemeToEdit(L2PcInstance player, String action)
	{
		List<String> profileNames = PlayerMethods.getProfiles(player);
		
		StringBuilder tb = new StringBuilder();
		tb.append(_headHtml);
		tb.append("Choose the profile<br></center><font color=00FFFF>Scheme Profiles:</font><center><img src=\"L2UI.SquareGray\" width=280 height=1><table bgcolor=131210>");
		for (String profile : profileNames)
		{
			tb.append("<tr>");
			tb.append("<td align=center><button value=\"" + profile + "\" action=\"bypass -h Aiobuff_" + action + "_" + profile + "\" width=135 height=28 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
			tb.append("</tr>");
		}
		tb.append("</table><img src=\"L2UI.SquareGray\" width=280 height=1>");
		tb.append(_endHtml);
		
		BufferPacketSender.sendPacket(player, tb.toString(), BufferPacketCategories.DYNAMIC, 0);
	}
	
	/**
	 * Shows the available buffs to add to a profile
	 * @param player
	 * @param profile
	 * @param category
	 * @param bypass
	 */
	public static void showBuffsToAdd(L2PcInstance player, String profile, BufferMenuCategories category, String bypass)
	{
		List<Integer> ownedBuffs = PlayerMethods.getProfileBuffs(profile, player);
		int i = 0;
		
		StringBuilder tb = new StringBuilder();
		tb.append(_headHtml);
		tb.append("Choose the buffs to be added<br></center>");
		switch (category)
		{
			case CHANT:
				tb.append("<font color=00FFFF>Scheme Buffer: Chant</font>");
				break;
			case DANCE:
				tb.append("<font color=00FFFF>Scheme Buffer: Dance</font>");
				break;
			case SONG:
				tb.append("<font color=00FFFF>Scheme Buffer: Song</font>");
				break;
			case OVERLORD:
				tb.append("<font color=00FFFF>Scheme Buffer: Overlord</font>");
				break;
			case PROPHET:
				tb.append("<font color=00FFFF>Scheme Buffer: Prophet</font>");
				break;
			case ELDER:
				tb.append("<font color=00FFFF>Scheme Buffer: Elder</font>");
				break;
			case DWARF:
				tb.append("<font color=00FFFF>Scheme Buffer: Dwarf</font>");
				break;
			case MISC:
				tb.append("<font color=00FFFF>Scheme Buffer: Misc</font>");
				break;
			default:
				break;
		}
		tb.append("<center><img src=\"L2UI.SquareGray\" width=280 height=1>");
		for (BuffsInstance buffInst : BuffsHolder.getInstance().getBuffs().values())
		{
			// Just a check to know if this buff
			// Is in the category we wish to be
			if (buffInst.getCategory() != category)
			{
				continue;
			}
			
			final int id = buffInst.getId();
			final int level = buffInst.getLevel();
			final String description = buffInst.getDescription();
			final String name = buffInst.getName();
			final String icon = SkillIconData.getInstance().getIcon(id);
			
			// Check if the buff id exists in the owned buffs list
			// Remember the l2pc has only the buffId contained
			if ((ownedBuffs != null) && ownedBuffs.contains(id))
			{
				continue;
			}
			
			tb.append((i % 2) == 0 ? "<table bgcolor=131210>" : "<table>");
			tb.append("<tr>");
			tb.append("<td width=40><button value=. action=\"bypass -h Aiobuff_" + bypass + "_" + profile + "_" + id + "\" width=32 height=32 back=\"" + icon + "\" fore=\"" + icon + "\"></td>");
			tb.append("<td><table>");
			tb.append("<tr><td width=220 height=15>" + name + "<font color=a1a1a1> Lv</font> <font color=ae9977>" + level + "</font></td></tr>");
			tb.append("<tr><td width=220 height=15><font color=b0bccc>" + description + "</font></td></tr></table></td></tr>");
			tb.append("</table>");
			i++;
		}
		tb.append("<br><br><img src=L2UI.SquareWhite width=280 height=1><button value=\"Back\" action=\"bypass -h Aiobuff_Chat_scheme.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append(_endHtml);
		
		BufferPacketSender.sendPacket(player, tb.toString(), BufferPacketCategories.DYNAMIC, 0);
	}
	
	/**
	 * Shows the available buffs to delete from a profile
	 * @param player
	 * @param profile
	 * @param bypass
	 */
	public static void showBuffsToDelete(L2PcInstance player, String profile, String bypass)
	{
		List<Integer> ownedBuffs = PlayerMethods.getProfileBuffs(profile, player);
		// int i = 0;
		
		if (ownedBuffs == null)
		{
			player.sendMessage("There are no buffs in that profile.");
			return;
		}
		
		StringBuilder tb = new StringBuilder();
		tb.append(_headHtml);
		tb.append("Choose the buffs to be deleted<br></center>");
		tb.append("<font color=00FFFF>Scheme Buffer: Remove</font>");
		// Temporary fix for long html
		tb.append("<center><img src=\"L2UI.SquareGray\" width=280 height=1></center><table width=280>");
		// tb.append("<center><img src=\"L2UI.SquareGray\" width=280 height=1>");
		for (BuffsInstance buffInst : BuffsHolder.getInstance().getBuffs().values())
		{
			int id = buffInst.getId();
			// Integer level = buffInst.getLevel();
			// String description = buffInst.getDescription();
			String name = buffInst.getName();
			
			// Check if the buff id exists in the owned buffs list
			// Remember the l2pc has only the buffId contained
			if (!ownedBuffs.contains(id))
			{
				continue;
			}
			
			// Temporary fix for long html
			tb.append("<tr>");
			tb.append("<td><a action=\"bypass -h Aiobuff_" + bypass + "_" + profile + "_" + id + "\">" + name + "</a></td>");
			tb.append("</tr>");
		}
		
		// Temporary fix for long html
		tb.append("</table>");
		tb.append("<center>");
		tb.append("<br><br><img src=L2UI.SquareWhite width=280 height=1><button value=\"Back\" action=\"bypass -h Aiobuff_Chat_main.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append(_endHtml);
		
		BufferPacketSender.sendPacket(player, tb.toString(), BufferPacketCategories.DYNAMIC, 0);
	}
}