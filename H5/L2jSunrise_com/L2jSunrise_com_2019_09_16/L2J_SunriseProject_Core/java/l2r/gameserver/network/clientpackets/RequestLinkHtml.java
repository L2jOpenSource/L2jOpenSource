/*
 * Copyright (C) 2004-2015 L2J Server
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

package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.util.Util;

/**
 * Lets drink to code!
 * @author zabbix
 */
public final class RequestLinkHtml extends L2GameClientPacket
{
	private static final String _C__22_REQUESTLINKHTML = "[C] 22 RequestLinkHtml";
	private String _link;
	
	@Override
	protected void readImpl()
	{
		_link = readS();
	}
	
	@Override
	public void runImpl()
	{
		L2PcInstance actor = getClient().getActiveChar();
		if (actor == null)
		{
			return;
		}
		
		if (_link.isEmpty())
		{
			_log.warn("Player " + actor.getName() + " sent empty html link!");
			return;
		}
		
		if (_link.contains("..") || !_link.contains(".htm"))
		{
			_log.warn("[RequestLinkHtml] hack? link contains prohibited characters: '" + _link + "', skipped");
			return;
		}
		
		int htmlObjectId = actor.validateHtmlAction("link " + _link);
		if (htmlObjectId == -1)
		{
			_log.warn("Player " + actor.getName() + " sent non cached  html link: link " + _link);
			return;
		}
		
		if ((htmlObjectId > 0) && !Util.isInsideRangeOfObjectId(actor, htmlObjectId, L2Npc.INTERACTION_DISTANCE))
		{
			// No logging here, this could be a common case
			return;
		}
		
		String filename = "data/html/" + _link;
		final NpcHtmlMessage msg = new NpcHtmlMessage(htmlObjectId);
		msg.setFile(actor, actor.getHtmlPrefix(), filename);
		sendPacket(msg);
	}
	
	@Override
	public String getType()
	{
		return _C__22_REQUESTLINKHTML;
	}
}
