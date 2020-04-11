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
package custom.events.Casino;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.util.Rnd;

import ai.npc.AbstractNpcAI;

/**
 * @reworked MaGa
 */

public class SlotMachine1 extends AbstractNpcAI
{
	// NPCs
	private static final int NPC = 37629;
	
	// Items
	private static final int itemId = 57;
	private static final int itemCount = 1000;
	
	// Mist
	private boolean win = false;
	
	private SlotMachine1()
	{
		super(SlotMachine1.class.getSimpleName(), "custom/events/Casino");
		addStartNpc(NPC);
		addTalkId(NPC);
		addFirstTalkId(NPC);
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "play":
			{
				if (checkstatus(player))
				{
					if (getQuestItemsCount(player, itemId) < itemCount)
					{
						player.sendMessage("You do not have that many adenas, get adenas!");
						break;
					}
					
					// Pay items
					player.destroyItemByItemId("bet", itemId, itemCount, player, true);
					
					try
					{
						run(player, npc);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		showmain(player, npc);
		return "";
	}
	
	void checkresult(L2PcInstance player, L2Npc npc)
	{
		if (win)
		{
			// Rewards
			player.getInventory().addItem("SlotMachine", itemId, itemCount * 2, player, player);
			
			// Message
			player.sendMessage("You have won " + (itemCount * 2) + " adenas! Congratulations! " + player.getName() + "!");
		}
		else
		{
			// Message
			player.sendMessage("You lost the bet! Try again, you may have better luck!");
		}
		
		showresult(player, npc);
		win = false;
	}
	
	boolean checkstatus(L2PcInstance player)
	{
		if (player == null)
		{
			return false;
		}
		return true;
	}
	
	void run(L2PcInstance player, L2Npc npc) throws InterruptedException
	{
		int a = Rnd.get(10, 20);
		int ar = a % 2;
		int b = Rnd.get(10, 20);
		int i = 1;
		
		while (i <= a)
		{
			if (!checkstatus(player))
			{
				return;
			}
			
			if ((i % 2) == 0)
			{
				showpage(player, npc, "a", "b");
			}
			else if ((i % 2) == 1)
			{
				showpage(player, npc, "b", "a");
			}
			Thread.sleep(150);
			i++;
		}
		
		Thread.sleep(500);
		i = 1;
		while (i <= b)
		{
			if (!checkstatus(player))
			{
				return;
			}
			
			if (ar == 0)
			{
				if ((i % 2) == 0)
				{
					showpage(player, npc, "a", "b");
				}
				else if ((i % 2) == 1)
				{
					showpage(player, npc, "b", "a");
				}
			}
			
			else if (ar == 1)
			{
				if ((i % 2) == 0)
				{
					showpage(player, npc, "a", "a");
					win = true;
				}
				else if ((i % 2) == 1)
				{
					showpage(player, npc, "b", "b");
					win = true;
				}
			}
			
			Thread.sleep(250);
			i++;
		}
		
		Thread.sleep(2000);
		
		if (checkstatus(player))
		{
			checkresult(player, npc);
		}
	}
	
	void showpage(L2PcInstance player, L2Npc npc, String a, String b)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>..:: * Casino * ::..</title><body>");
		tb.append("<center>");
		tb.append("<table border=0 cellpadding=0 cellspacing=0 width=292 height=350 background=L2UI_CH3.refinewnd_back_Pattern>");
		tb.append("<tr>");
		tb.append("<td valign=top align=center>");
		tb.append("<table border=0 cellpadding=0 cellspacing=0>");
		tb.append("<tr>");
		tb.append("<td width=256 height=185 background=\"L2UI_CT1.OlympiadWnd_DF_GrandTexture\"></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td align=center fixwidth=200>");
		tb.append("<font name=\"hs15\" color=LEVEL>Slot Machine</font>");
		tb.append("</td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("<br>");
		tb.append("Good Luck, loading...");
		tb.append("<br>");
		tb.append("<center><img src=\"l2ui.squaregray\" width=200 height=1></center><br>" + "<table width=180 height=30 cellpadding=0 cellspacing=0 valign=top>" + "<tr>" + "<td>" + "<table width=180 height=40 bgcolor=090908 cellspacing=0 cellpadding=7>" + "<tr>" + "<td valign=top>"
			+ "<table width=180 cellspacing=0 cellpadding=0>" + "<tr>");
		tb.append("<td height=24 valign=top>" + "<center>" + "<table cellspacing=-1>" + "<tr>" + "<td><img src=\"icon.etc_dice_" + a + "_i00\" " + "width=32 height=32>" + "</td>" + "<td>" + "<img src=\"icon.etc_dice_" + b + "_i00\" width=32 height=32>" + "</td>" + "<td>"
			+ "<img src=\"icon.etc_dice_" + b + "_i00\" width=32 height=32>" + "</td>" + "<td>" + "<img src=\"icon.etc_dice_" + a + "_i00\" width=32 height=32>" + "</td>" + "</tr>" + "</table>" + "</center><br><br>");
		tb.append("</td><br>" + "<br></tr>" + "</table>" + "</td>" + "</tr>" + "</table>" + "</td>" + "</tr>" + "</table>" + "<br><br><center><img src=\"l2ui.squaregray\" width=200 height=1></center><br>");
		tb.append("</td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("</center>");
		tb.append("</body>");
		tb.append("</html>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(npc.getObjectId());
		msg.setHtml(tb.toString());
		player.sendPacket(msg);
	}
	
	void showmain(L2PcInstance player, L2Npc npc)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>..:: * Casino * ::..</title><body>");
		tb.append("<center>");
		tb.append("<table border=0 cellpadding=0 cellspacing=0 width=292 height=350 background=L2UI_CH3.refinewnd_back_Pattern>");
		tb.append("<tr>");
		tb.append("<td valign=top align=center>");
		tb.append("<table border=0 cellpadding=0 cellspacing=0>");
		tb.append("<tr>");
		tb.append("<td width=256 height=185 background=\"L2UI_CT1.OlympiadWnd_DF_GrandTexture\"></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td align=center fixwidth=200>");
		tb.append("<font name=\"hs15\" color=LEVEL>Slot Machine</font>");
		tb.append("</td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("<br>");
		tb.append("Hello, You want to play?<br1>");
		tb.append("Get the same color and you will win<br1>");
		tb.append("the double, good luck!");
		tb.append("<br>");
		tb.append("Match Price: <font color=LEVEL>" + itemCount + "</font> adena(s).</font>");
		tb.append("<br1>");
		tb.append("<center>");
		tb.append("<table width=260>");
		tb.append("<tr>");
		tb.append("<td align=center><button value=\"Play Match\" action=\"bypass -h Quest SlotMachine1 play\" width=200 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"/></td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("</center>");
		tb.append("</td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("</center>");
		tb.append("</body>");
		tb.append("</html>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(npc.getObjectId());
		msg.setHtml(tb.toString());
		player.sendPacket(msg);
	}
	
	void showresult(L2PcInstance player, L2Npc npc)
	{
		String result = "";
		if (win)
		{
			result = "Congratulations, you won!";
		}
		else
		{
			result = "Sorry, you lose.";
		}
		
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>..:: * Casino * ::..</title><body>");
		tb.append("<center>");
		tb.append("<table border=0 cellpadding=0 cellspacing=0 width=292 height=350 background=L2UI_CH3.refinewnd_back_Pattern>");
		tb.append("<tr>");
		tb.append("<td valign=top align=center>");
		tb.append("<table border=0 cellpadding=0 cellspacing=0>");
		tb.append("<tr>");
		tb.append("<td width=256 height=185 background=\"L2UI_CT1.OlympiadWnd_DF_GrandTexture\"></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td align=center fixwidth=200>");
		tb.append("<font name=\"hs15\" color=LEVEL>Slot Machine</font>");
		tb.append("</td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("<br>");
		tb.append(result);
		tb.append("<br1>");
		tb.append("Play again? Come on!");
		tb.append("<br1>");
		tb.append("<center>");
		tb.append("<table width=260>");
		tb.append("<tr>");
		tb.append("<td align=center><button value=\"New Match\" action=\"bypass -h Quest SlotMachine1 play\" width=150 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"/></td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("</center>");
		tb.append("</td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("</center>");
		tb.append("</body>");
		tb.append("</html>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(npc.getObjectId());
		msg.setHtml(tb.toString());
		player.sendPacket(msg);
	}
	
	public static void main(String[] args)
	{
		new SlotMachine1();
		_log.info("Casino Event: Enabled.");
	}
}