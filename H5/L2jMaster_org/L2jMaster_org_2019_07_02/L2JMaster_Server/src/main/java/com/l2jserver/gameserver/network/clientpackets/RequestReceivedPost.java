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
package com.l2jserver.gameserver.network.clientpackets;

import com.l2jserver.Config;
import com.l2jserver.gameserver.instancemanager.MailManager;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.Message;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExChangePostState;
import com.l2jserver.gameserver.network.serverpackets.ExReplyReceivedPost;
import com.l2jserver.gameserver.util.Util;

/**
 * @author Migi, DS
 */
public final class RequestReceivedPost extends L2GameClientPacket
{
	private static final String _C__D0_69_REQUESTRECEIVEDPOST = "[C] D0:69 RequestReceivedPost";
	
	private int _msgId;
	
	@Override
	protected void readImpl()
	{
		_msgId = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if ((activeChar == null) || !Config.ALLOW_MAIL)
		{
			return;
		}
		
		final Message msg = MailManager.getInstance().getMessage(_msgId);
		if (msg == null)
		{
			return;
		}
		
		if (!activeChar.isInsideZone(ZoneId.PEACE) && msg.hasAttachments())
		{
			activeChar.sendPacket(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE);
			return;
		}
		
		if (msg.getReceiverId() != activeChar.getObjectId())
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to receive not own post!", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (msg.isDeletedByReceiver())
		{
			return;
		}
		
		activeChar.sendPacket(new ExReplyReceivedPost(msg));
		activeChar.sendPacket(new ExChangePostState(true, _msgId, Message.READED));
		msg.markAsRead();
	}
	
	@Override
	public String getType()
	{
		return _C__D0_69_REQUESTRECEIVEDPOST;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
