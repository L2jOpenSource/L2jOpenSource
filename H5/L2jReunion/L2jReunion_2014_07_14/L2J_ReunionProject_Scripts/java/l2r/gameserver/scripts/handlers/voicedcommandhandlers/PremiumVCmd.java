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

package l2r.gameserver.scripts.handlers.voicedcommandhandlers;

import java.text.SimpleDateFormat;

import javolution.text.TextBuilder;
import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import gr.reunion.configsEngine.PremiumServiceConfigs;
import gr.reunion.premiumEngine.PremiumHandler;

public class PremiumVCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"premium"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith("premium"))
		{
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
			if (!activeChar.isPremium())
			{
				NpcHtmlMessage preReply = new NpcHtmlMessage(5);
				TextBuilder html3 = new TextBuilder("<html><body><title>Normal Account</title><center>");
				html3.append("<table>");
				html3.append("<tr><td><center>Account Type: <font color=\"LEVEL\">Normal<br></font></td></tr>");
				html3.append("<tr><td><center>Details<br1></td></tr>");
				html3.append("<tr><td>Rate EXP: <font color=\"LEVEL\">Your Rate<br1></font></td></tr>");
				html3.append("<tr><td>Rate SP: <font color=\"LEVEL\">Your Rate<br1></font></td></tr>");
				html3.append("<tr><td>Rate Spoil: <font color=\"LEVEL\">Your Rate <br1></font></td></tr><br>");
				html3.append("<tr><td>Expires: <font color=\"00A5FF\">Never (Normal Account)<br1></font></td></tr>");
				html3.append("<tr><td>Current Date: <font color=\"70FFCA\">" + String.valueOf(format.format(System.currentTimeMillis())) + " <br><br></font></td></tr><br><br1><br1>");
				html3.append("<tr><td><font color=\"LEVEL\"><center>Premium Info & Rules<br1></font></td></tr>");
				html3.append("<tr><td>Upgrade to Premium Account: <font color=\"70FFCA\"> http://www.l2reunion.eu</font></td></tr>");
				html3.append("<tr><td>Premium Account: <font color=\"70FFCA\">Benefits<br1></font></td></tr>");
				html3.append("<tr><td>Rate EXP: <font color=\"LEVEL\">" + PremiumServiceConfigs.PREMIUM_RATE_XP + " (Account Premium)<br1></font></td></tr>");
				html3.append("<tr><td>Rate SP: <font color=\"LEVEL\">" + PremiumServiceConfigs.PREMIUM_RATE_SP + " (Account Premium)<br1></font></td></tr>");
				html3.append("<tr><td>Drop Spoil Rate: <font color=\"LEVEL\">" + PremiumServiceConfigs.PREMIUM_RATE_DROP_SPOIL + " (Account Premium)<br1></font></td></tr>");
				html3.append("<tr><td>Item Buffer: <font color=\"LEVEL\">Unique .buffer command (Account Premium)<br1></font></td></tr>");
				html3.append("<tr><td>Vip Buffer: <font color=\"LEVEL\">Enchanted buffs from npc buffer (Account Premium)<br1></font></td></tr>");
				html3.append("<tr><td> <font color=\"70FFCA\">1.Premium  benefits CAN NOT BE TRANSFERED.<br1></font></td></tr><br>");
				html3.append("<tr><td> <font color=\"70FFCA\">2.Premium benefits effect ALL characters in same account.<br1></font></td></tr><br>");
				html3.append("<tr><td> <font color=\"70FFCA\">3.Does not effect Party members.</font></td></tr>");
				html3.append("</table>");
				html3.append("</center></body></html>");
				
				preReply.setHtml(html3.toString());
				activeChar.sendPacket(preReply);
			}
			else
			{
				long _end_prem_date;
				_end_prem_date = PremiumHandler.getPremServiceData(activeChar.getAccountName());
				NpcHtmlMessage preReply = new NpcHtmlMessage(5);
				
				TextBuilder html3 = new TextBuilder("<html><body><title>Premium Account Details</title><center>");
				html3.append("<table>");
				html3.append("<tr><td><center>Thank you for supporting <font color=\"00FF00\">YOUR</font> server.<br><br></td></tr>");
				html3.append("<tr><td><center>Your account: <font color=\"LEVEL\">Premium<br></font></td></tr>");
				html3.append("<tr><td><center>Details<br1></center></td></tr>");
				html3.append("<tr><td>Rate EXP: <font color=\"LEVEL\"> x" + PremiumServiceConfigs.PREMIUM_RATE_XP + " <br1></font></td></tr>");
				html3.append("<tr><td>Rate SP: <font color=\"LEVEL\"> x" + PremiumServiceConfigs.PREMIUM_RATE_SP + "  <br1></font></td></tr>");
				html3.append("<tr><td>Rate Spoil: <font color=\"LEVEL\"> x" + PremiumServiceConfigs.PREMIUM_RATE_DROP_SPOIL + " <br1></font></td></tr>");
				html3.append("<tr><td>Item Buffer: <font color=\"LEVEL\">Unique .buffer command<br1></font></td></tr>");
				html3.append("<tr><td>Vip Buffer: <font color=\"LEVEL\">Enchanted buffs from npc buffer<br1></font></td></tr>");
				html3.append("<tr><td>Expires: <font color=\"00A5FF\">" + String.valueOf(format.format(_end_prem_date)) + " (Premium added)</font></td></tr>");
				html3.append("<tr><td>Current Date: <font color=\"70FFCA\">" + String.valueOf(format.format(System.currentTimeMillis())) + "<br><br></font></td></tr>");
				html3.append("<tr><td><font color=\"LEVEL\"><center>Premium Info & Rules<br1></font></center></td></tr>");
				html3.append("<tr><td><font color=\"70FFCA\">1.Premium Account CAN NOT BE TRANSFERED.<br1></font></td></tr>");
				html3.append("<tr><td><font color=\"70FFCA\">2.Premium Account effects ALL characters in same account.<br1></font></td></tr>");
				html3.append("<tr><td><font color=\"70FFCA\">3.Does not effect Party members.<br><br></font></td></tr>");
				html3.append("</table>");
				html3.append("</center></body></html>");
				
				preReply.setHtml(html3.toString());
				activeChar.sendPacket(preReply);
			}
		}
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
