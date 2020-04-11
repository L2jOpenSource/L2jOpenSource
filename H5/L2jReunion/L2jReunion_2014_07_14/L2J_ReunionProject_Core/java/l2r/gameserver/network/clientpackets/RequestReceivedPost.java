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
import l2r.gameserver.instancemanager.MailManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Message;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExChangePostState;
import l2r.gameserver.network.serverpackets.ExReplyReceivedPost;
import l2r.gameserver.util.Util;

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
		
		if (!activeChar.isInsideZone(ZoneIdType.PEACE) && msg.hasAttachments())
		{
			activeChar.sendPacket(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE);
			return;
		}
		
		if (activeChar.isInsideZone(ZoneIdType.JAIL))
		{
			activeChar.sendMessage("You cannot receive or send mail with attached items in jail.");
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
