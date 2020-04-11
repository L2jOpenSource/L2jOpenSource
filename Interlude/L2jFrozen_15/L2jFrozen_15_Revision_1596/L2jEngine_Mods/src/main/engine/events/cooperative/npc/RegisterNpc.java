package main.engine.events.cooperative.npc;

import java.util.StringTokenizer;

import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;

import main.data.properties.ConfigData;
import main.engine.AbstractMod;
import main.engine.events.cooperative.AbstractCooperative;
import main.engine.events.cooperative.EventCooperativeManager;
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
 * Class responsible for managing the actions of the NPC responsible for the votes and registrations to cooperative type events.
 * @author fissban
 */
public class RegisterNpc extends AbstractMod
{
	private static final int NPC = 60017;
	
	public RegisterNpc()
	{
		registerMod(true);
	}
	
	@Override
	public void onModState()
	{
		//
	}
	
	@Override
	public boolean onInteract(PlayerHolder player, CharacterHolder character)
	{
		if (!Util.areObjectType(L2NpcInstance.class, character))
		{
			return false;
		}
		
		if (((NpcHolder) character).getId() != NPC)
		{
			return false;
		}
		
		sendHtml((NpcHolder) character, htmlIndex(player), player);
		return true;
	}
	
	@Override
	public void onEvent(PlayerHolder ph, CharacterHolder npc, String command)
	{
		if (((NpcHolder) npc).getId() != NPC)
		{
			return;
		}
		
		StringTokenizer st = new StringTokenizer(command, " ");
		
		String event = st.nextToken();
		switch (event)
		{
			case "main":
			{
				sendHtml((NpcHolder) npc, htmlIndex(ph), ph);
				break;
			}
			case "unregister":
			{
				HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
				hb.append(Html.START);
				hb.append(Html.head("COOPERATIVE EVENTS"));
				hb.append("<center>");
				hb.append("<br><br><br><br>");
				if (EventCooperativeManager.unRegister(ph))
				{
					hb.append("Your registration cancel successfully!<br>");
				}
				else
				{
					hb.append("You are not already registered to the event!<br>");
				}
				
				hb.append("</center>");
				hb.append(Html.END);
				sendHtml((NpcHolder) npc, hb, ph);
				break;
			}
			case "register":
			{
				HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
				hb.append(Html.START);
				hb.append(Html.head("COOPERATIVE EVENTS"));
				hb.append("<center>");
				hb.append("<br><br><br><br>");
				
				if (!EventCooperativeManager.canRegisterOrVote())
				{
					hb.append("There is an ongoing event.<br>");
					hb.append("Wait for it to finish before you can sign up.<br>");
				}
				else if (EventCooperativeManager.isMaxPlayerRegisters())
				{
					hb.append("Already registered the maximum number of users allowed<br>");
				}
				else if (EventCooperativeManager.isRegisterPlayerIp(ph, true))
				{
					hb.append("Already registered with your IP!<br>");
				}
				else if (EventCooperativeManager.register(ph))
				{
					hb.append("Your registration completed successfully!<br>");
				}
				else
				{
					hb.append("You are already registered to the event!<br>");
				}
				
				hb.append("<button value=Back action=\"bypass -h Engine RegisterNpc main\" width=95 height=22 back=", L2UI_CH3.bigbutton_down, " fore=", L2UI_CH3.bigbutton, ">");
				hb.append("</center>");
				hb.append(Html.END);
				
				sendHtml((NpcHolder) npc, hb, ph);
				break;
			}
			case "voteFor":
			{
				HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
				hb.append(Html.START);
				hb.append(Html.head("COOPERATIVE EVENTS"));
				hb.append("<center>");
				hb.append("<br><br><br><br>");
				
				// Check whether the character voted or not
				if (!EventCooperativeManager.hasVote(ph))
				{
					hb.append("Your vote was successfully recorded..");
					
					// Add the character to the list of players that vote
					EventCooperativeManager.vote(ph);
					
					if (st.hasMoreTokens())
					{
						// Increased the number of votes of the event that was voted.
						EventCooperativeManager.increaseVote(st.nextToken());
					}
				}
				else
				{
					hb.append("You have already voted.");
				}
				
				hb.append("<br>");
				hb.append("<button value=Back action=\"bypass -h Engine RegisterNpc main\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
				hb.append("</center>");
				hb.append(Html.END);
				sendHtml((NpcHolder) npc, hb, ph);
				break;
			}
		}
	}
	
	private static HtmlBuilder htmlIndex(PlayerHolder ph)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append(Html.START);
		hb.append(Html.head("COOPERATIVE EVENTS"));
		hb.append("<center>");
		hb.append("<br>");
		
		if (EventCooperativeManager.canRegisterOrVote())
		{
			hb.append("Participate in our events<br1>");
			hb.append("and wins important prizes.<br>");
			hb.append("Registered Players: ", Html.fontColor("LEVEL", EventCooperativeManager.getRegisterPlayersCount() + " / " + ConfigData.COOPERATIVE_MAX_PLAYERS), "<br>");
			
			if (EventCooperativeManager.isRegisterPlayerIp(ph, true))
			{
				hb.append(Html.fontColor("9933FF", "I already registered a player with your IP"), "<br>");
			}
			else
			{
				hb.append("Registered: ", Html.fontColor("9933FF", EventCooperativeManager.isRegisterPlayer(ph) ? "YES" : "NO"), "<br>");
			}
			
			hb.append(Html.fontColor("LEVEL", "Register for the next event!"), "<br>");
			hb.append("<table width=280>");
			hb.append("<tr>");
			hb.append("<td>", Html.image(L2UI.bbs_folder, 32, 32), "</td>");
			if (EventCooperativeManager.isRegisterPlayer(ph))
			{
				hb.append("<td width=216 align=center><button value=\"UnRegister\" action=\"bypass -h Engine RegisterNpc unregister\" width=95 height=22 back=", L2UI_CH3.bigbutton_down, " fore=", L2UI_CH3.bigbutton, "></td>");
			}
			else
			{
				hb.append("<td width=216 align=center><button value=\"Register\" action=\"bypass -h Engine RegisterNpc register\" width=95 height=22 back=", L2UI_CH3.bigbutton_down, " fore=", L2UI_CH3.bigbutton, "></td>");
			}
			hb.append("<td>", Html.image(L2UI.bbs_folder, 32, 32), "</td>");
			hb.append("</tr>");
			hb.append("</table>");
			
			hb.append(Html.image(L2UI.SquareGray, 280, 1));
			
			int cont = 0;
			for (AbstractCooperative e : EventCooperativeManager.getAllEvents())
			{
				String eventName = e.getClass().getSimpleName();
				
				hb.append("<table width=280", (cont % 2) == 0 ? " bgcolor=000000>" : ">");
				hb.append("<tr>");
				hb.append("<td width=130 align=center>", Html.fontColor("LEVEL", eventName), "</td>");
				hb.append("<td width=75 align=center>", Html.fontColor("LEVEL", "Votes : "), e.getVotes(), "</td>");
				hb.append("<td width=75 align=center><button value=Vote action=\"bypass -h Engine RegisterNpc voteFor ", eventName, "\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
				hb.append("</tr>");
				hb.append("</table>");
				hb.append(Html.image(L2UI.SquareGray, 280, 1));
				cont++;
			}
		}
		else
		{
			// Wait for the event to end
			hb.append("We have the event ", EventCooperativeManager.getCurrentEvent().getName(), " in progress.<br1>");
		}
		
		hb.append("</center>");
		hb.append(Html.END);
		
		return hb;
	}
}
