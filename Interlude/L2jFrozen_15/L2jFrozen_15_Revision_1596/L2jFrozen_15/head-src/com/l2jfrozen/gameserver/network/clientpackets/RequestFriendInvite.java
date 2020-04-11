package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.AskJoinFriend;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendInvite extends L2GameClientPacket
{
	private String name;
	
	@Override
	protected void readImpl()
	{
		name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		SystemMessage sm;
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		final L2PcInstance friend = L2World.getInstance().getPlayer(name);
		
		if (friend == null)
		{
			// Target is not found in the game.
			sm = new SystemMessage(SystemMessageId.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if (friend == activeChar)
		{
			// You cannot add yourself to your own friend list.
			sm = new SystemMessage(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if (activeChar.getBlockList().isInBlockList(name))
		{
			sm = new SystemMessage(SystemMessageId.FAILED_TO_INVITE_A_FRIEND);
			activeChar.sendPacket(sm);
			return;
		}
		
		if (friend.getBlockList().isInBlockList(activeChar.getName()))
		{
			sm = new SystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
			sm.addString(friend.getName());
			activeChar.sendPacket(sm);
			sm = new SystemMessage(SystemMessageId.FAILED_TO_INVITE_A_FRIEND);
			activeChar.sendPacket(sm);
			return;
		}
		
		if (activeChar.isInCombat() || friend.isInCombat())
		{
			sm = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if (activeChar.getFriendList().contains(friend.getName()))
		{
			// Player already is in your friendlist
			sm = new SystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST);
			sm.addString(name);
			activeChar.sendPacket(sm);
			return;
		}
		
		if (!friend.isProcessingRequest())
		{
			// requets to become friend
			activeChar.onTransactionRequest(friend);
			sm = new SystemMessage(SystemMessageId.S1_REQUESTED_TO_BECOME_FRIENDS);
			sm.addString(name);
			final AskJoinFriend ajf = new AskJoinFriend(activeChar.getName());
			friend.sendPacket(ajf);
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
		}
		
		friend.sendPacket(sm);
		
	}
	
	@Override
	public String getType()
	{
		return "[C] 5E RequestFriendInvite";
	}
}