package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		SystemMessage sm;
		
		// ======<Friend List>======
		activeChar.sendPacket(new SystemMessage(SystemMessageId.FRIEND_LIST_HEAD));
		
		L2PcInstance friend = null;
		for (final String friendName : activeChar.getFriendList())
		{
			friend = L2World.getInstance().getPlayer(friendName);
			
			if (friend == null || !friend.isOnline())
			{
				// (Currently: Offline)
				sm = new SystemMessage(SystemMessageId.S1_OFFLINE);
				sm.addString(friendName);
			}
			else
			{
				// (Currently: Online)
				sm = new SystemMessage(SystemMessageId.S1_ONLINE);
				sm.addString(friendName);
			}
			
			activeChar.sendPacket(sm);
		}
		
		// =========================
		activeChar.sendPacket(new SystemMessage(SystemMessageId.FRIEND_LIST_FOOT));
	}
	
	@Override
	public String getType()
	{
		return "[C] 60 RequestFriendList";
	}
}
