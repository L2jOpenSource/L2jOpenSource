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

import l2r.Config;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.handler.IChatHandler;
import l2r.gameserver.instancemanager.MapRegionManager;
import l2r.gameserver.model.BlockList;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.util.Util;

/**
 * Shout chat handler.
 * @author durgus
 */
public class ChatShout implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		1
	};
	
	/**
	 * Handle chat type 'shout'
	 */
	@Override
	public void handleChat(int type, L2PcInstance activeChar, String target, String text)
	{
		if (activeChar.isInOlympiadMode() && !activeChar.isGM() && Config.ENABLE_OLY_ANTIFEED)
		{
			return;
		}
		
		if (activeChar.isChatBanned() && Util.contains(Config.BAN_CHAT_CHANNELS, type))
		{
			activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
			return;
		}
		
		if (!activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS) && !activeChar.getFloodProtectors().getGlobalChat().tryPerformAction("global chat"))
		{
			activeChar.sendMessage("Do not spam shout channel.");
			return;
		}
		
		CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
		CreatureSay cs2 = new CreatureSay(activeChar.getObjectId(), type, activeChar.getVar("namePrefix", "") + activeChar.getName(), text);
		
		Collection<L2PcInstance> pls = L2World.getInstance().getPlayers();
		
		if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("on") || (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("gm") && activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS)))
		{
			int region = MapRegionManager.getInstance().getMapRegionLocId(activeChar);
			for (L2PcInstance player : pls)
			{
				if ((region == MapRegionManager.getInstance().getMapRegionLocId(player)) && !BlockList.isBlocked(player, activeChar) && (player.getInstanceId() == activeChar.getInstanceId()))
				{
					player.sendPacket(cs);
				}
			}
		}
		else if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("custom"))
		{
			if (activeChar.isGM())
			{
				for (L2PcInstance player : pls)
				{
					player.sendPacket(cs2);
				}
			}
			
			else if (!activeChar.isGM())
			{
				int region = MapRegionManager.getInstance().getMapRegionLocId(activeChar);
				for (L2PcInstance player : pls)
				{
					if (player.isGM())
					{
						player.sendPacket(cs);
					}
					else if (!player.isGM())
					{
						if ((region == MapRegionManager.getInstance().getMapRegionLocId(player)) && !BlockList.isBlocked(player, activeChar) && (player.getInstanceId() == activeChar.getInstanceId()))
						{
							player.sendPacket(cs);
						}
					}
				}
			}
		}
		else if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("global"))
		{
			for (L2PcInstance player : pls)
			{
				if (!BlockList.isBlocked(player, activeChar))
				{
					player.sendPacket(cs);
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
