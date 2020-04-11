
package com.l2jfrozen.gameserver.handler.usercommandhandlers;

import java.text.SimpleDateFormat;

import com.l2jfrozen.gameserver.handler.IUserCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;

import javolution.text.TextBuilder;

/**
 * Support for clan penalty user command.
 * @author Tempy
 */
public class ClanPenalty implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		100
	};
	
	@Override
	public boolean useUserCommand(final int id, final L2PcInstance activeChar)
	{
		if (id != COMMAND_IDS[0])
		{
			return false;
		}
		
		boolean penalty = false;
		
		final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		TextBuilder htmlContent = new TextBuilder("<html><body>");
		
		htmlContent.append("<center><table width=270 border=0 bgcolor=111111>");
		htmlContent.append("<tr><td width=170>Penalty</td>");
		htmlContent.append("<td width=100 align=center>Expiration Date</td></tr>");
		htmlContent.append("</table><table width=270 border=0><tr>");
		
		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			htmlContent.append("<td width=170>Unable to join a clan.</td>");
			htmlContent.append("<td width=100 align=center>" + format.format(activeChar.getClanJoinExpiryTime()) + "</td>");
			penalty = true;
		}
		if (activeChar.getClanCreateExpiryTime() > System.currentTimeMillis())
		{
			htmlContent.append("<td width=170>Unable to create a clan.</td>");
			htmlContent.append("<td width=100 align=center>" + format.format(activeChar.getClanCreateExpiryTime()) + "</td>");
			penalty = true;
		}
		if (activeChar.getClan() != null && activeChar.getClan().getCharPenaltyExpiryTime() > System.currentTimeMillis())
		{
			htmlContent.append("<td width=170>Unable to invite a clan member.</td>");
			htmlContent.append("<td width=100 align=center>");
			htmlContent.append(format.format(activeChar.getClan().getCharPenaltyExpiryTime()));
			htmlContent.append("</td>");
			penalty = true;
		}
		if (!penalty)
		{
			htmlContent.append("<td width=170>No penalty is imposed.</td>");
			htmlContent.append("<td width=100 align=center> </td>");
		}
		
		htmlContent.append("</tr></table><img src=\"L2UI.SquareWhite\" width=270 height=1>");
		htmlContent.append("</center></body></html>");
		
		NpcHtmlMessage penaltyHtml = new NpcHtmlMessage(0);
		penaltyHtml.setHtml(htmlContent.toString());
		activeChar.sendPacket(penaltyHtml);
		
		penaltyHtml = null;
		htmlContent = null;
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
