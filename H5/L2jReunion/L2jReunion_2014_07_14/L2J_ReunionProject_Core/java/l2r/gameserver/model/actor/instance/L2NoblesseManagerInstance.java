package l2r.gameserver.model.actor.instance;

import javolution.text.TextBuilder;
import l2r.gameserver.model.actor.FakePc;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.UserInfo;
import gr.reunion.main.Conditions;

/**
 * @author NeverMore
 */
public final class L2NoblesseManagerInstance extends L2Npc
{
	// Temp. Configs for NoblesseManager
	private static final int ItemId = 3470; // <---- Item will be needed for Noblesse.
	private static final int ItemAmount = 1000; // <---- Amount of the item that will be needed for Noblesse.
	private static final String Name = "Festival Adena"; // <---- Name of the item that will be needed for Noblesse. (eg.Gold bar)
	private static final int Level = 75; // <---- Level will be needed in order to complete the service. (restriction)
	
	/**
	 * @param objectId
	 * @param template
	 */
	public L2NoblesseManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		FakePc fpc = getFakePc();
		if (fpc != null)
		{
			setTitle(fpc.title);
		}
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		NpcHtmlMessage nhm = new NpcHtmlMessage(2);
		TextBuilder tb = new TextBuilder("");
		
		tb.append("<html><body><title>Noblesse Manager</title><center><br>");
		tb.append("<img src=\"l2ui.SquareGray\" width=270 height=1><br1>");
		tb.append("<table width=\"262\" cellpadding=\"5\" bgcolor=\"151515\">");
		tb.append("<tr>");
		tb.append("<td valign=\"top\"><center><font color=\"EBDF6C\">L2 Reunion</font> Noblesse manager<br>Use this npc to become noblesse and take a chance to be the next hero. </center></td>");
		tb.append("</tr>");
		tb.append("</table><br1>");
		tb.append("<img src=\"l2ui.SquareGray\" width=270 height=1><br>");
		tb.append("</center><br><center>");
		tb.append("<img src=\"l2ui.SquareGray\" width=270 height=1><br1>");
		tb.append("<table width=\"280\" bgcolor=\"151515\">");
		tb.append("<tr>");
		tb.append("<td><center><font color=\"EBDF6C\">Requirements</font></center></td>");
		tb.append("</tr>");
		tb.append("</table><br1>");
		tb.append("<img src=\"l2ui.SquareGray\" width=270 height=1><br><table width=\"280\"><tr><td>In order to become noblesse you will need <br> <font color=\"EBDF6C\">1) </font>" + ItemAmount + " <font color=\"EBDF6C\"> " + Name + "</font>. <br1><font color=\"EBDF6C\">2)</font> To be " + Level + " level or higher.</td></tr></table><br>");
		tb.append("<img src=\"l2ui.SquareGray\" width=270 height=1><br><br>");
		tb.append("<button action=\"bypass -h npc_" + getObjectId() + "_noblesse\" value=\"Make me noblesse\" width=140 height=24 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br><center>");
		tb.append("<table border=\"0\" cellspacing=\"0\"><tr>");
		tb.append("	<td valign=top><img src=icon.skill0325 width=32 height=32 align=left></td>");
		tb.append("	<td valign=top><img src=icon.skill0326 width=32 height=32 align=left></td>");
		tb.append("	<td valign=top><img src=icon.skill0327 width=32 height=32 align=left></td>");
		tb.append("	<td valign=top><img src=icon.skill1323 width=32 height=32 align=left></td>");
		tb.append("	<td valign=top><img src=icon.skill1324 width=32 height=32 align=left></td>");
		tb.append("	<td valign=top><img src=icon.skill1325 width=32 height=32 align=left></td>");
		tb.append("	<td valign=top><img src=icon.skill1326 width=32 height=32 align=left></td>");
		tb.append("	<td valign=top><img src=icon.skill1327 width=32 height=32 align=left></td></tr>");
		tb.append("</table><br><br>");
		tb.append("</center></center></body></html>");
		nhm.setHtml(tb.toString());
		player.sendPacket(nhm);
	}
	
	@Override
	public void onBypassFeedback(final L2PcInstance player, String command)
	{
		if (player == null)
		{
			return;
		}
		
		if (player.getKarma() > 0)
		{
			player.sendMessage("Cannot use while hava karma.");
			return;
		}
		
		if (player.isEnchanting())
		{
			player.sendMessage("Cannot use while Enchanting.");
			return;
		}
		
		if (player.isAlikeDead())
		{
			player.sendMessage("Cannot use while Dead or Fake Death.");
			return;
		}
		
		if (command.startsWith("noblesse"))
		{
			if (!Conditions.checkPlayerItemCount(player, ItemId, ItemAmount))
			{
				return;
			}
			
			if (player.getLevel() < Level)
			{
				player.sendMessage("You need to be " + Level + " level or higher to become noblesse.");
				return;
			}
			
			if (player.isNoble())
			{
				player.sendMessage("You are already noblesse.");
				return;
			}
			
			player.destroyItemByItemId("noblesse", ItemId, ItemAmount, player, true);
			player.addItem("Tiara", 7694, 1, null, true);
			player.setNoble(!player.isNoble());
			player.sendPacket(new UserInfo(player));
			player.sendMessage("Congratulations! You are now Noblesse!");
			return;
		}
	}
}