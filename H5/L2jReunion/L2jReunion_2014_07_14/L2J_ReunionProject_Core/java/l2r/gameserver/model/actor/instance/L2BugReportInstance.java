/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.actor.instance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.FakePc;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.network.L2GameClient;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author -=DoctorNo=- Version 2.4
 */
public class L2BugReportInstance extends L2Npc
{
	/**
	 * @param objectId
	 * @param template
	 */
	public L2BugReportInstance(int objectId, L2NpcTemplate template)
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
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		TextBuilder tb = new TextBuilder();
		
		tb.append("<html noscrollbar><body>");
		tb.append("<title>Bug Report Manager</title>");
		tb.append("<center><br>");
		tb.append("<img src=\"l2ui.SquareGray\" width=290 height=1><br1>");
		tb.append("<table width=\"290\" cellpadding=\"5\" bgcolor=\"151515\">");
		tb.append("<tr>");
		tb.append("<td valign=\"top\"><center><font color=\"EBDF6C\">L2 Reunion</font> bug report manager<br>Use this npc to report us any bug when there are not online gms</center></td>");
		tb.append(" </tr>");
		tb.append(" </table><br1>");
		tb.append("<img src=\"l2ui.SquareGray\" width=290 height=1><br>");
		tb.append("</center><center>");
		tb.append("<img src=\"L2UI.SquareGray\" width=290 height=1>");
		tb.append("<br><br>");
		tb.append("<table width=250>");
		tb.append("<tr>");
		tb.append("<td><font color=\"EBDF6C\">Select Report Type:</font></td>");
		tb.append("<td><combobox width=105 var=type list=General;Fatal;Misuse;Balance;Other></td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("<br><br>");
		tb.append("<multiedit var=\"msg\" width=250 height=50><br>");
		tb.append("<button value=\"Sent Report\" action=\"bypass -h npc_%objectId%_report $type $msg\" width=128 height=26 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		tb.append("<br><br><font color=\"FF0000\">Warning:</font>Fake or nonsense reports will cause permanent ban.<br><br>");
		tb.append("<img src=\"L2UI.SquareGray\" width=290 height=1>");
		tb.append("<br><br><br><br><br>");
		tb.append("<font color=EBDF6C>L][Reunion Team</font><br>");
		tb.append("</center></body></html>");
		
		msg.setHtml(tb.toString());
		msg.replace("%player%", player.getName());
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(msg);
	}
	
	@Override
	public void onBypassFeedback(final L2PcInstance player, String command)
	{
		// No null pointers
		if (player == null)
		{
			return;
		}
		
		if (command.startsWith("report"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			
			String message = "";
			String _type = null; // General, Fatal, Misuse, Balance, Other
			L2GameClient info = player.getClient().getConnection().getClient();
			
			try
			{
				_type = st.nextToken();
				
				while (st.hasMoreTokens())
				{
					message = message + st.nextToken() + " ";
				}
				
				if (message.equals(""))
				{
					player.sendMessage("Message box cannot be empty.");
					return;
				}
				
				String fname = "data/BugReports/" + player.getName() + ".txt";
				File file = new File(fname);
				boolean exist = file.createNewFile();
				
				if (!exist)
				{
					player.sendMessage("You have already sent a bug report, GMs must check it first.");
					return;
				}
				
				FileWriter fstream = new FileWriter(fname);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write("Character Info: " + info + "\r\nBug Type: " + _type + "\r\nMessage: " + message);
				player.sendMessage("Report sent. GMs will check it soon. Thanks...");
				
				for (L2PcInstance allgms : L2World.getInstance().getAllGMs())
				{
					allgms.sendPacket(new CreatureSay(0, Say2.SHOUT, "Bug Report Manager", player.getName() + " sent a bug report."));
					allgms.sendPacket(new CreatureSay(0, Say2.SHOUT, "Report Type", _type + "."));
				}
				
				_log.info("Character: " + player.getName() + " sent a bug report.");
				out.close();
			}
			catch (Exception e)
			{
				player.sendMessage("Something went wrong try again.");
			}
		}
	}
}