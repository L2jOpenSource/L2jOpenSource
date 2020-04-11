package l2r.gameserver.model.actor.instance;

import javolution.text.TextBuilder;
import l2r.gameserver.model.actor.FakePc;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SocialAction;
import l2r.util.Rnd;
import gr.reunion.main.Conditions;

/**
 * @author NeverMore
 */

public final class L2CasinoManagerInstance extends L2Npc
{
	// Temp. Misc Configs for CasinoManager
	private static final String Name = "Festival Adena"; // <---- Name of the item that will be needed for Bet. (eg.Gold bar)
	private static final int Level = 75; // <---- Level will be needed in order to use this npc. (restriction)
	private static final int Chance = 45; // <---- Chance to win
	// Temp. Reward/Bet item configs for CasinoManager
	private static final int ItemId = 6673; // <---- Item will be needed for bet (ID).
	private static final int Bet1 = 10; // <---- Bet amount 1
	private static final int Bet2 = 25; // <---- Bet amount 2
	private static final int Bet3 = 50; // <---- Bet amount 3
	
	/**
	 * @param objectId
	 * @param template
	 */
	public L2CasinoManagerInstance(int objectId, L2NpcTemplate template)
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
		
		if (player.isInCombat())
		{
			player.sendMessage("Cannot use while in combat.");
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
		
		if (player.getLevel() < Level)
		{
			player.sendMessage("You need to be " + Level + " level or higher to use my services.");
			return;
		}
		
		NpcHtmlMessage nhm = new NpcHtmlMessage(2);
		TextBuilder tb = new TextBuilder("");
		
		tb.append("<html><body><title>Casino Manager</title><center><br>");
		tb.append("<img src=\"l2ui.SquareGray\" width=270 height=1><br1>");
		tb.append("<table width=\"262\" cellpadding=\"5\" bgcolor=\"151515\">");
		tb.append("<tr>");
		tb.append("<td valign=\"top\"><center><font color=\"EBDF6C\">L2 Reunion</font> Casino manager<br>Use this npc and take the risk to double or lose your bets. Good luck.</center></td>");
		tb.append("</tr>");
		tb.append("</table><br1>");
		tb.append("<img src=\"l2ui.SquareGray\" width=270 height=1><br>");
		tb.append("</center><br><center>");
		tb.append("<img src=\"l2ui.SquareGray\" width=270 height=1><br>");
		tb.append("<table width=\"280\"><tr>");
		tb.append("<td>Choose the bet amount:</td>");
		tb.append("<td><combobox width=62 var=amount list=" + Bet1 + ";" + Bet2 + ";" + Bet3 + "></td>");
		tb.append("</tr></table><br1>");
		tb.append("<table width=\"280\"><tr><td>Chance to win <font color=\"EBDF6C\">" + Chance + "%</font>.<br1>You can bet only <font color=\"EBDF6C\">" + Name + ".</font></td></tr></table><br>");
		tb.append("<img src=\"l2ui.SquareGray\" width=270 height=1><br><br><br>");
		tb.append("<button action=\"bypass -h npc_" + getObjectId() + "_bet $amount\" value=\"Place my bet\" width=140 height=24 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br><center>");
		tb.append("<br><table border=\"0\" cellspacing=\"0\"><tr>");
		tb.append("	<td valign=top><img src=icon.etc_dice_b_i00 width=32 height=32 align=left></td>");
		tb.append("	<td valign=top><img src=icon.etc_dice_a_i00 width=32 height=32 align=left></td>");
		tb.append("	<td valign=top><img src=icon.etc_dice_d_i00 width=32 height=32 align=left></td>");
		tb.append("	<td valign=top><img src=icon.etc_dice_c_i00 width=32 height=32 align=left></td>");
		tb.append("	<td valign=top><img src=icon.etc_dice_b_i00 width=32 height=32 align=left></td>");
		tb.append("	<td valign=top><img src=icon.etc_dice_a_i00 width=32 height=32 align=left></td></tr>");
		tb.append("</table>");
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
		
		if (command.startsWith("bet"))
		{
			String val = command.substring(4);
			int bet = Integer.parseInt(val);
			
			if ((bet == 0) || ((bet != Bet1) && (bet != Bet2) && (bet != Bet3)))
			{
				player.sendMessage("Somthing went wrong.Try again later.");
				return;
			}
			
			if (!Conditions.checkPlayerItemCount(player, ItemId, bet))
			{
				return;
			}
			
			if ((Rnd.get(100) <= Chance))
			{
				player.getInventory().addItem("bet", ItemId, bet, player, true);
				player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
				player.broadcastPacket(new MagicSkillUse(player, player, 2024, 1, 1, 0));
				player.sendMessage("Congratulations you won! ");
			}
			else
			{
				player.destroyItemByItemId("bet", ItemId, bet, player, true);
				player.sendMessage("I am sorry you lost your bet. Try again you might be luckier");
			}
		}
	}
}