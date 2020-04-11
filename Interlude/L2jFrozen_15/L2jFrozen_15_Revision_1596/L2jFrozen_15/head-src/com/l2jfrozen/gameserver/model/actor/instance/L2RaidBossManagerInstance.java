package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

import javolution.text.TextBuilder;

/**
 * @author xAddytzu moded by Bobi
 */
public class L2RaidBossManagerInstance extends L2NpcInstance
{
	public L2RaidBossManagerInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(final L2PcInstance player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		// Check if the L2PcInstance already target the L2Npc
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			final MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			
			// Send a Server->Client packet ValidateLocation to correct the L2Npc position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2Npc
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showChatWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(new ActionFailed());
	}
	
	@Override
	public void showChatWindow(final L2PcInstance player, final int val)
	{
		final NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(rbWindow(player));
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(msg);
	}
	
	private String rbWindow(final L2PcInstance player)
	{
		final TextBuilder tb = new TextBuilder();
		tb.append("<html><title>L2 Raidboss Manager</title><body>");
		tb.append("<center>");
		tb.append("<br>");
		tb.append("<font color=\"999999\">Raidboss Manager</font><br>");
		tb.append("<img src=\"L2UI.SquareGray\" width=\"200\" height=\"1\"><br>");
		tb.append("Welcome " + player.getName() + "<br>");
		tb.append("<table width=\"85%\"><tr><td>We gatekeepers use the will of the gods to open the doors of time and space and teleport others. Which door would you like to open?</td></tr></table><br>");
		
		tb.append("<img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\"></center><br>");
		tb.append("<table width=180>");
		tb.append("<tr>");
		tb.append("<td><center><a action=\"bypass -h npc_%objectId%_RaidbossLvl_40\">Raidboss Level (40-45)</a></center></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td><center><a action=\"bypass -h npc_%objectId%_RaidbossLvl_45\">Raidboss Level (45-50)</a></center></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td><center><a action=\"bypass -h npc_%objectId%_RaidbossLvl_50\">Raidboss Level (50-55)</a></center></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td><center><a action=\"bypass -h npc_%objectId%_RaidbossLvl_55\">Raidboss Level (55-60)</a></center></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td><center><a action=\"bypass -h npc_%objectId%_RaidbossLvl_60\">Raidboss Level (60-65)</a></center></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td><center><a action=\"bypass -h npc_%objectId%_RaidbossLvl_65\">Raidboss Level (65-70)</a></center></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td><center><a action=\"bypass -h npc_%objectId%_RaidbossLvl_70\">Raidboss Level (70-75)</a></center></td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("<center><img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\">");
		tb.append("<font color=\"999999\">Gates of Fire</font></center>");
		tb.append("</body></html>");
		return tb.toString();
	}
}