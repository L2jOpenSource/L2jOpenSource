/*
 * Copyright (C) 2004-2013 L2J DataPack
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
package handlers.chathandlers;

import java.util.Collection;
import java.util.StringTokenizer;

import l2r.Config;
import l2r.gameserver.handler.IChatHandler;
import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.handler.VoicedCommandHandler;
import l2r.gameserver.model.BlockList;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.util.Util;

import gr.sr.interf.SunriseEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A chat handler
 * @author durgus
 */
public class ChatAll implements IChatHandler
{
	private static Logger _log = LoggerFactory.getLogger(ChatAll.class);
	
	private static final int[] COMMAND_IDS =
	{
		0
	};
	
	/**
	 * Handle chat type 'all'
	 */
	@Override
	public void handleChat(int type, L2PcInstance activeChar, String params, String text)
	{
		if (activeChar.isInOlympiadMode() && !activeChar.isGM() && Config.ENABLE_OLY_ANTIFEED)
		{
			return;
		}
		
		boolean vcd_used = false;
		if (text.startsWith("."))
		{
			StringTokenizer st = new StringTokenizer(text);
			IVoicedCommandHandler vch;
			String command = "";
			
			if (st.countTokens() > 1)
			{
				command = st.nextToken().substring(1);
				params = text.substring(command.length() + 2);
				vch = VoicedCommandHandler.getInstance().getHandler(command);
			}
			else
			{
				command = text.substring(1);
				if (Config.DEBUG)
				{
					_log.info("Command: " + command);
				}
				vch = VoicedCommandHandler.getInstance().getHandler(command);
			}
			if (vch != null)
			{
				vch.useVoicedCommand(command, activeChar, params);
				vcd_used = true;
			}
			else
			{
				if (Config.DEBUG)
				{
					_log.warn("No handler registered for bypass '" + command + "'");
				}
				vcd_used = false;
			}
		}
		if (!vcd_used)
		{
			if (activeChar.isChatBanned() && Util.contains(Config.BAN_CHAT_CHANNELS, type))
			{
				activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
				return;
			}
			
			/**
			 * Match the character "." literally (Exactly 1 time) Match any character that is NOT a . character. Between one and unlimited times as possible, giving back as needed (greedy)
			 */
			if (text.matches("\\.{1}[^\\.]+") && SunriseEvents.isInEvent(activeChar))
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_SYNTAX);
			}
			else
			{
				CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getAppearance().getVisibleName(), text);
				CreatureSay cs2 = new CreatureSay(activeChar.getObjectId(), type, activeChar.getVar("namePrefix", "") + activeChar.getAppearance().getVisibleName(), text);
				Collection<L2PcInstance> plrs = activeChar.getKnownList().getKnownPlayers().values();
				if (activeChar.isGM())
				{
					for (L2PcInstance player : plrs)
					{
						if ((player != null) && activeChar.isInsideRadius(player, 1250, false, true) && !BlockList.isBlocked(player, activeChar))
						{
							player.sendPacket(cs2);
						}
					}
				}
				else if (!activeChar.isGM())
				{
					for (L2PcInstance player : plrs)
					{
						if ((player != null) && activeChar.isInsideRadius(player, 1250, false, true) && !BlockList.isBlocked(player, activeChar))
						{
							player.sendPacket(cs);
						}
					}
				}
				if (activeChar.isGM())
				{
					activeChar.sendPacket(cs2);
				}
				else if (!activeChar.isGM())
				{
					activeChar.sendPacket(cs);
				}
			}
		}
	}
	
	/**
	 * Returns the chat types registered to this handler.
	 */
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}