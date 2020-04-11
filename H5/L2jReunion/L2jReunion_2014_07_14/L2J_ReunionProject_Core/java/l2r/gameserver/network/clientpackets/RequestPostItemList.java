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
package l2r.gameserver.network.clientpackets;

import l2r.Config;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExReplyPostItemList;

/**
 * @author Migi, DS
 */
public final class RequestPostItemList extends L2GameClientPacket
{
	private static final String _C__D0_65_REQUESTPOSTITEMLIST = "[C] D0:65 RequestPostItemList";
	
	@Override
	protected void readImpl()
	{
		// trigger packet
	}
	
	@Override
	public void runImpl()
	{
		if (!Config.ALLOW_MAIL || !Config.ALLOW_ATTACHMENTS)
		{
			return;
		}
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!activeChar.isInsideZone(ZoneIdType.PEACE))
		{
			activeChar.sendPacket(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE);
			return;
		}
		
		if (activeChar.isInsideZone(ZoneIdType.JAIL))
		{
			activeChar.sendMessage("You cannot receive or send mail with attached items in jail.");
			return;
		}
		
		activeChar.sendPacket(new ExReplyPostItemList(activeChar));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_65_REQUESTPOSTITEMLIST;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
