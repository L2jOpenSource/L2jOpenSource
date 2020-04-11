/*
 * Copyright (C) 2004-2019 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.gameserver.data.sql.impl.TeleportLocationTable;
import com.l2jserver.gameserver.data.xml.impl.DoorData;
import com.l2jserver.gameserver.enums.InstanceType;
import com.l2jserver.gameserver.model.L2TeleportLocation;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

public class L2DoormenInstance extends L2NpcInstance
{
	private static final Logger LOG = LoggerFactory.getLogger(L2DoormenInstance.class);
	
	/**
	 * Creates a doorman.
	 * @param template the doorman NPC template
	 */
	public L2DoormenInstance(L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2DoormenInstance);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("Chat"))
		{
			showChatWindow(player);
			return;
		}
		else if (command.startsWith("open_doors"))
		{
			if (isOwnerClan(player))
			{
				if (isUnderSiege())
				{
					cannotManageDoors(player);
				}
				else
				{
					openDoors(player, command);
				}
			}
			return;
		}
		else if (command.startsWith("close_doors"))
		{
			if (isOwnerClan(player))
			{
				if (isUnderSiege())
				{
					cannotManageDoors(player);
				}
				else
				{
					closeDoors(player, command);
				}
			}
			return;
		}
		else if (command.startsWith("tele"))
		{
			if (isOwnerClan(player))
			{
				doTeleport(player, command);
			}
			return;
		}
		super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
		if (!isOwnerClan(player))
		{
			html.setFile(player.getHtmlPrefix(), "data/html/doormen/" + getTemplate().getId() + "-no.htm");
		}
		else
		{
			html.setFile(player.getHtmlPrefix(), "data/html/doormen/" + getTemplate().getId() + ".htm");
		}
		
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	protected void openDoors(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
		st.nextToken();
		
		while (st.hasMoreTokens())
		{
			DoorData.getInstance().getDoor(Integer.parseInt(st.nextToken())).openMe();
		}
	}
	
	protected void closeDoors(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
		st.nextToken();
		
		while (st.hasMoreTokens())
		{
			DoorData.getInstance().getDoor(Integer.parseInt(st.nextToken())).closeMe();
		}
	}
	
	protected void cannotManageDoors(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), "data/html/doormen/" + getTemplate().getId() + "-busy.htm");
		player.sendPacket(html);
	}
	
	protected void doTeleport(L2PcInstance player, String command)
	{
		final int whereTo = Integer.parseInt(command.substring(5).trim());
		L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(whereTo);
		if (list != null)
		{
			if (!player.isAlikeDead())
			{
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), false);
			}
		}
		else
		{
			LOG.warn("No teleport destination with id: {}", whereTo);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	protected boolean isOwnerClan(L2PcInstance player)
	{
		return true;
	}
	
	protected boolean isUnderSiege()
	{
		return false;
	}
}
