package gr.sr.javaBuffer.buffCommunity.dynamicHtmls;

import java.util.List;

import l2r.gameserver.communitybbs.Managers.ServicesBBSManager;
import l2r.gameserver.data.xml.impl.SkillIconData;
import l2r.gameserver.model.ClanPrivilege;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.SortedWareHouseWithdrawalList;
import l2r.gameserver.network.serverpackets.SortedWareHouseWithdrawalList.WarehouseListType;
import l2r.gameserver.network.serverpackets.WareHouseWithdrawalList;

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
	private static final String _headHtml = "<html noscrollbar><title>L2jSunrise Community Board</title><body><br><table width=755><tr><td><center><table border=0 cellpadding=0 cellspacing=0 width=769 height=492 background=\"l2ui_ct1.Windows_DF_TooltipBG\"><tr><td valign=\"top\" align=\"center\"><table  width=450><tr><td align=center><br><font name=\"hs12\">Community Buffer</font></td></tr><tr><td align=center><img src=\"L2UI.SquareGray\" width=160 height=1><br><br><br><br></td></tr></table><br>";
	private static final String _endHtml = "</td></tr></table></center></td></tr></table></body></html>";
	
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
		tb.append("<table width=450><tr><td align=center><br><br>Choose the profile<br></td></tr></table>");
		for (String profile : profileNames)
		{
			tb.append("<table width=450><tr>");
			tb.append("<td align=center><button value=\"" + profile + "\" action=\"bypass " + ServicesBBSManager.getInstance()._servicesBBSCommand + "_functions_buffer_" + action + "_" + profile + "\" width=200 height=31 back=\"L2UI_CT1.OlympiadWnd_DF_HeroConfirm_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_HeroConfirm\"></td>");
			tb.append("</tr></table>");
		}
		tb.append("<table width=755 border=0 height=40>");
		tb.append("<tr>");
		tb.append("<td width=600 align=center valign=top><br><br><br><br>");
		tb.append("<button action=\"bypass " + ServicesBBSManager.getInstance()._servicesBBSCommand + "_functions_buffer_page_main.htm\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red_Down\" fore=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red\" />");
		tb.append("</td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append(_endHtml);
		
		BufferPacketSender.sendPacket(player, tb.toString(), BufferPacketCategories.DYNAMIC, 1);
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
		tb.append("<table width=450><tr><td align=center>Choose the buffs to be added<br></td></tr></table>");
		switch (category)
		{
			case CHANT:
				tb.append("<table width=450><tr><td align=center><font color=ae9977>Scheme Buffer: Chant</font></td></tr></table>");
				break;
			case DANCE:
				tb.append("<table width=450><tr><td align=center><font color=ae9977>Scheme Buffer: Dance</font></td></tr></table>");
				break;
			case SONG:
				tb.append("<table width=450><tr><td align=center><font color=ae9977>Scheme Buffer: Song</font></td></tr></table>");
				break;
			case OVERLORD:
				tb.append("<table width=450><tr><td align=center><font color=ae9977>Scheme Buffer: Overlord</font></td></tr></table>");
				break;
			case PROPHET:
				tb.append("<table width=450><tr><td align=center><font color=ae9977>Scheme Buffer: Prophet</font></td></tr></table>");
				break;
			case ELDER:
				tb.append("<table width=450><tr><td align=center><font color=ae9977>Scheme Buffer: Elder</font></td></tr></table>");
				break;
			case DWARF:
				tb.append("<table width=450><tr><td align=center><font color=ae9977>Scheme Buffer: Dwarf</font></td></tr></table>");
				break;
			case MISC:
				tb.append("<table width=450><tr><td align=center><font color=ae9977>Scheme Buffer: Misc</font></td></tr></table>");
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
			
			// Check if the buff id exists in the owned buffs fastlist
			// Remember the l2pc has only the buffId contained
			if ((ownedBuffs != null) && ownedBuffs.contains(id))
			{
				continue;
			}
			
			tb.append((i % 2) == 0 ? "<table bgcolor=131210>" : "<table>");
			tb.append("<tr>");
			tb.append("<td width=40><button action=\"bypass " + ServicesBBSManager.getInstance()._servicesBBSCommand + "_functions_buffer_" + bypass + "_" + profile + "_" + id + "\" width=32 height=32 back=\"" + icon + "\" fore=\"" + icon + "\"></td>");
			tb.append("<td><table>");
			tb.append("<tr><td width=220>" + name + "<font color=a1a1a1> Lv</font> <font color=ae9977>" + level + "</font></td></tr>");
			tb.append("<tr><td width=220><font color=b0bccc>" + description + "</font></td></tr></table></td></tr>");
			tb.append("</table>");
			i++;
		}
		tb.append("<table width=755 border=0 height=40>");
		tb.append("<tr>");
		tb.append("<td width=600 align=center valign=top><br><br><br><br>");
		tb.append("<button action=\"bypass " + ServicesBBSManager.getInstance()._servicesBBSCommand + "_functions_buffer_page_scheme.htm\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red_Down\" fore=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red\" />");
		tb.append("<br><br><br></td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append(_endHtml);
		
		BufferPacketSender.sendPacket(player, tb.toString(), BufferPacketCategories.DYNAMIC, 1);
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
		tb.append("<font color=ae9977>Scheme Buffer: Remove</font>");
		// Temporary fix for long html
		tb.append("<center><img src=\"L2UI.SquareGray\" width=280 height=1></center><table width=280>");
		// tb.append("<center><img src=\"L2UI.SquareGray\" width=280 height=1>");
		for (BuffsInstance buffInst : BuffsHolder.getInstance().getBuffs().values())
		{
			int id = buffInst.getId();
			// Integer level = buffInst.getLevel();
			// String description = buffInst.getDescription();
			String name = buffInst.getName();
			
			// Check if the buff id exists in the owned buffs fastlist
			// Remember the l2pc has only the buffId contained
			if (!ownedBuffs.contains(id))
			{
				continue;
			}
			
			// Temporary fix for long html
			tb.append("<tr>");
			tb.append("<td align=center><a action=\"bypass " + ServicesBBSManager.getInstance()._servicesBBSCommand + "_functions_buffer_" + bypass + "_" + profile + "_" + id + "\">" + name + "</a></td>");
			tb.append("</tr>");
		}
		
		// Temporary fix for long html
		tb.append("</table>");
		tb.append("<center>");
		tb.append("<table width=755 border=0 height=40>");
		tb.append("<tr>");
		tb.append("<td width=600 align=center valign=top><br><br><br><br>");
		tb.append("<button action=\"bypass " + ServicesBBSManager.getInstance()._servicesBBSCommand + "_functions_buffer_page_main.htm\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red_Down\" fore=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red\" />");
		tb.append("<br><br><br></td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append(_endHtml);
		
		BufferPacketSender.sendPacket(player, tb.toString(), BufferPacketCategories.DYNAMIC, 1);
	}
	
	public static final void showCWithdrawWindow(L2PcInstance player, WarehouseListType itemtype, byte sortorder)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		if (!player.hasClanPrivilege(ClanPrivilege.CL_VIEW_WAREHOUSE))
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
			return;
		}
		
		player.setActiveWarehouse(player.getClan().getWarehouse());
		
		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
			return;
		}
		
		if (itemtype != null)
		{
			player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN, itemtype, sortorder));
		}
		else
		{
			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN));
		}
	}
	
	public static final void showPWithdrawWindow(L2PcInstance player, WarehouseListType itemtype, byte sortorder)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());
		
		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
			return;
		}
		
		if (itemtype != null)
		{
			player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE, itemtype, sortorder));
		}
		else
		{
			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE));
		}
	}
}