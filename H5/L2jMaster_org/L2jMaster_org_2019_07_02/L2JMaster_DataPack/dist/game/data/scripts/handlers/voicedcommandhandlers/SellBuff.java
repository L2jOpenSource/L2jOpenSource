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
package handlers.voicedcommandhandlers;

import com.l2jserver.Config;
import com.l2jserver.gameserver.handler.IVoicedCommandHandler;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Voice Command - SellBuff (.sellbuff)
 * @author Aegnor Staff
 */

public class SellBuff implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"sellbuff"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (Config.COMMAND_SELL_BUFF)
		{
			if (command.equals("sellbuff"))
			{
				if (activeChar == null)
				{
					return false;
				}
				
				if (activeChar.isSellBuff())
				{
					activeChar.setSellBuff(false);
					activeChar.standUp();
					activeChar.getAppearance().setNameColor(activeChar.getOldNameColor());
					activeChar.setTitle(activeChar.getOldTitle());
					activeChar.getAppearance().setTitleColor(activeChar.getOldTitleColor());
					activeChar.broadcastUserInfo();
					activeChar.broadcastTitleInfo();
				}
				else
				{
					if (activeChar.isDead() || activeChar.isAlikeDead())
					{
						activeChar.sendMessage("You are dead, you can't sell in this mode.!");
						return false;
					}
					else if (!activeChar.isInsideZone(ZoneId.PEACE))
					{
						activeChar.sendMessage("You are not in peace zone. You can sell only in peace zone!");
						return false;
					}
					else if (activeChar.isInOlympiadMode())
					{
						activeChar.sendMessage("You are in olys. You can't sell in this momment.");
						return false;
					}
					else if ((activeChar.getPvpFlag() > 0) || activeChar.isInCombat() || (activeChar.getKarma() > 0))
					{
						activeChar.sendMessage("You are in combat. Try later!");
						return false;
					}
					else if ((activeChar.getClassId().getId() != 98) && (activeChar.getClassId().getId() != 17 // buffer humano
					) && (activeChar.getClassId().getId() != 105) && (activeChar.getClassId().getId() != 30 // buffer elf
					) && (activeChar.getClassId().getId() != 112) && (activeChar.getClassId().getId() != 43 // buffer dark elf
					) && (activeChar.getClassId().getId() != 100) && (activeChar.getClassId().getId() != 21 // sword singer
					) && (activeChar.getClassId().getId() != 107) && (activeChar.getClassId().getId() != 34 // blade dancer
					) && (activeChar.getClassId().getId() != 115) && (activeChar.getClassId().getId() != 116 // buffer orc
					) && (activeChar.getClassId().getId() != 51) && (activeChar.getClassId().getId() != 52 // buffer orc
					) && (activeChar.getClassId().getId() != 94) && (activeChar.getClassId().getId() != 103 // buffer Mage
					))
					{
						activeChar.sendMessage("Only buffers can sell Buff!");
						return false;
					}
					
					StringBuilder tb = new StringBuilder(0);
					tb.append("<html><title>..:: Sell Buff Panel ::..</title><body><center>");
					tb.append("<table><tr>");
					tb.append("<td><img src=\"icon.etc_alphabet_b_i00\" width=32 height=32 align=left></td><td><img src=\"icon.etc_alphabet_u_i00\" width=32 height=32 align=left></td>");
					tb.append("<td><img src=\"icon.etc_alphabet_f_i00\" width=32 height=32 align=left></td><td><img src=\"icon.etc_alphabet_f_i00\" width=32 height=32 align=left></td>");
					tb.append("</tr></table><br>");
					tb.append("<br><font color=0080ff>Hello</font>, complete to sell your buffs!<br>");
					tb.append("<p><font color=0080ff>Buffs Price:</font></p>");
					tb.append("<p><edit var=\"pri\" width=120 height=15></p>");
					tb.append("<button value=\"Sell Buffs Now\" action=\"bypass -h actr $pri\" width=200 height=32 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
					tb.append("</center><br>");
					tb.append("<center><font color=0080ff>..:: L2JMaster ::..</center>");
					tb.append("</body></html>");
					NpcHtmlMessage n = new NpcHtmlMessage(0);
					n.setHtml(tb.toString());
					activeChar.sendPacket(n);
				}
			}
		}
		else
		{
			activeChar.sendMessage("Command disabled!");
			return false;
		}
		
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
