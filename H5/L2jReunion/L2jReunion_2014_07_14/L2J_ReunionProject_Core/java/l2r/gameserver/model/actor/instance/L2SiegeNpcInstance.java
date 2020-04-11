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

import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.entity.clanhall.SiegableHall;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * This class ...
 * @version $Revision$ $Date$
 */
public class L2SiegeNpcInstance extends L2NpcInstance
{
	public L2SiegeNpcInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
		setInstanceType(InstanceType.L2SiegeNpcInstance);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		showSiegeInfoWindow(player);
	}
	
	/**
	 * If siege is in progress shows the Busy HTML<BR>
	 * else Shows the SiegeInfo window
	 * @param player
	 */
	public void showSiegeInfoWindow(L2PcInstance player)
	{
		if (validateCondition(player))
		{
			SiegableHall hall = getConquerableHall();
			if (hall != null)
			{
				hall.showSiegeInfo(player);
			}
			else
			{
				getCastle().getSiege().listRegisterClan(player);
			}
		}
		else
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player.getHtmlPrefix(), "data/html/siege/" + getId() + "-busy.htm");
			html.replace("%castlename%", getConquerableHall() != null ? getConquerableHall().getName() : getCastle().getName());
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	private boolean validateCondition(L2PcInstance player)
	{
		if ((getConquerableHall() != null) && getConquerableHall().isInSiege())
		{
			return false;
		}
		if (getCastle().getSiege().isInProgress())
		{
			return false; // Busy because of siege
		}
		
		return true;
	}
}