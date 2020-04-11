package main.engine.npc;

import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;

import main.data.properties.ConfigData;
import main.engine.AbstractMod;
import main.engine.mods.VoteReward;
import main.holders.objects.CharacterHolder;
import main.holders.objects.NpcHolder;
import main.holders.objects.PlayerHolder;
import main.util.Util;
import main.util.UtilInventory;
import main.util.UtilMessage;
import main.util.builders.html.Html;
import main.util.builders.html.HtmlBuilder;
import main.util.builders.html.HtmlBuilder.HtmlType;
import main.util.builders.html.L2UI;
import main.util.builders.html.L2UI_CH3;

/**
 * @author fissban
 */
public class NpcVoteRewardTopzone extends AbstractMod
{
	private static final int NPC = 60014;
	// player q esta votando
	private static PlayerHolder player = null;
	private static int votes = 0;
	
	public NpcVoteRewardTopzone()
	{
		registerMod(ConfigData.ENABLE_VoteRewardIndivualTopzone);
	}
	
	@Override
	public void onModState()
	{
		//
	}
	
	@Override
	public boolean onInteract(PlayerHolder ph, CharacterHolder character)
	{
		if (!Util.areObjectType(L2NpcInstance.class, character))
		{
			return false;
		}
		
		if (((L2NpcInstance) character.getInstance()).getNpcId() != NPC)
		{
			return false;
		}
		
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append(Html.START);
		hb.append(Html.head("VOTE REWARD TOPZONE"));
		hb.append("<br>");
		hb.append("<center>");
		hb.append("Bienvenido ", Html.fontColor("LEVEL", ph.getName()), "<br>");
		
		if (!checkLastVote(ph))
		{
			hb.append("Lo siento, aun no pasaron las ", Html.fontColor("LEVEL", "12hs"), "<br>");
			hb.append("desde tu ultimo voto, intenta mas tarde.<br>");
		}
		else
		{
			hb.append("Aqui podras votar por nuestro server<br>");
			hb.append("y obtener una buena recompenza por ello.<br>");
			if (player == null)
			{
				hb.append("Actualmente nadie esta votando<br>");
				hb.append("No esperes mas, ", Html.fontColor("LEVEL", "vota por nosotros!"), "<br>");
				
				hb.append("<table width=280>");
				hb.append("<tr>");
				hb.append("<td align=center>", Html.image(L2UI.bbs_folder, 32, 32), "</td>");
				hb.append("<td><button value=\"Vote\" action=\"bypass -h Engine ", NpcVoteRewardTopzone.class.getSimpleName(), " vote\" width=216 height=32 back=", L2UI_CH3.refinegrade3_21, " fore=", L2UI_CH3.refinegrade3_21, "></td>");
				hb.append("<td align=center>", Html.image(L2UI.bbs_folder, 32, 32), "</td>");
				hb.append("</tr>");
				hb.append("</table>");
			}
			else
			{
				hb.append("Actualmente se encuentra ", Html.fontColor("LEVEL", player.getName()), " votando<br>");
				hb.append("Solo espera un momento y ya sera tu turno<br>");
			}
		}
		
		hb.append("</center>");
		hb.append(Html.END);
		
		sendHtml((NpcHolder) character, ph, hb);
		
		return true;
	}
	
	private boolean checkLastVote(PlayerHolder ph)
	{
		// get last vote
		String vote = getValueDB(ph, "lastVote").getString();
		if (vote != null)
		{
			long lastVote = Long.parseLong(vote);
			
			// check time
			if ((lastVote + (ConfigData.INDIVIDUAL_VOTE_TIME_CAN_VOTE * 3600000)) > System.currentTimeMillis())
			{
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void onEvent(PlayerHolder ph, CharacterHolder character, String command)
	{
		if (!Util.areObjectType(L2NpcInstance.class, character))
		{
			return;
		}
		
		if (((L2NpcInstance) character.getInstance()).getNpcId() != NPC)
		{
			return;
		}
		
		// prevent bypass
		if (player != null)
		{
			return;
		}
		
		// prevent bypass
		if (!checkLastVote(ph))
		{
			return;
		}
		
		// init votes.
		votes = -1;
		
		if (command.equals("vote"))
		{
			try
			{
				votes = VoteReward.getVotesTopzone();
				HtmlBuilder hb = new HtmlBuilder();
				hb.append(Html.START);
				hb.append(Html.head("VOTE REWARD TOPZONE"));
				hb.append("<br>");
				hb.append("<br>");
				hb.append("<center>");
				
				if (votes == -1)
				{
					hb.append("The votes could not be obtained,<br> but try later");
				}
				else
				{
					player = ph;
					
					hb.append("Have ", Html.fontColor("LEVEL", ConfigData.INDIVIDUAL_VOTE_TIME_VOTE), " seconds to vote");
					
					startTimer("waitVote", ConfigData.INDIVIDUAL_VOTE_TIME_VOTE * 1000, null, player, false);
				}
				
				hb.append("</center>");
				hb.append(Html.END);
				sendHtml((NpcHolder) character, ph, hb);
			}
			catch (Exception e)
			{
				votes = 0;
				player = null;
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onTimer(String timerName, NpcHolder npc, PlayerHolder ph)
	{
		switch (timerName)
		{
			case "waitVote":
				if (VoteReward.getVotesTopzone() > votes)
				{
					UtilMessage.sendCreatureMsg(player, Say2.TELL, "", "Your vote could not be verified, try later!");
					UtilInventory.giveItems(player, ConfigData.INDIVIDUAL_VOTE_REWARD.getRewardId(), ConfigData.INDIVIDUAL_VOTE_REWARD.getRewardCount(), 0);
					setValueDB(player, "lastVote", System.currentTimeMillis() + "");
				}
				else
				{
					UtilMessage.sendCreatureMsg(player, Say2.TELL, "", "Your vote could not be verified, try later");
				}
				
				player = null;
				break;
		}
	}
}
