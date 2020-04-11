/*
 * Copyright (C) 2004-2014 L2J Server
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
package l2r.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import l2r.gameserver.datatables.sql.TeleportLocationTable;
import l2r.gameserver.datatables.xml.DoorData;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.model.L2TeleportLocation;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * This class ...
 * @version $Revision$ $Date$
 */
public class L2DoormenInstance extends L2NpcInstance
{
	/**
	 * @param objectID
	 * @param template
	 */
	public L2DoormenInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
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
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
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
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
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
				player.teleToLocation(list.getX(), list.getY(), list.getZ(), false);
			}
		}
		else
		{
			_log.warn("No teleport destination with id:" + whereTo);
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
