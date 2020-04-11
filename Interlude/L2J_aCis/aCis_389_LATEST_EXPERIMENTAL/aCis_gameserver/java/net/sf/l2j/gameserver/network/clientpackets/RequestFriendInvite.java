package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.container.player.BlockList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.FriendAddRequest;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendInvite extends L2GameClientPacket
{
	private String _targetName;
	
	@Override
	protected void readImpl()
	{
		_targetName = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		final Player target = World.getInstance().getPlayer(_targetName);
		
		// can't use friend invite for locating invisible characters
		if (target == null || !target.isOnline() || !target.getAppearance().isVisible())
		{
			// Target is not found in the game.
			player.sendPacket(SystemMessageId.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME);
			return;
		}
		
		if (target == player)
		{
			// You cannot add yourself to your own friend list.
			player.sendPacket(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
			return;
		}
		
		if (BlockList.isBlocked(player, target))
		{
			player.sendMessage("You have blocked " + _targetName + ".");
			return;
		}
		
		if (BlockList.isBlocked(target, player))
		{
			player.sendMessage("You are in " + _targetName + "'s block list.");
			return;
		}
		
		if (player.getFriendList().contains(target.getObjectId()))
		{
			// Player already is in your friendlist
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST).addString(_targetName));
			return;
		}
		
		if (!target.isProcessingRequest())
		{
			// request to become friend
			player.onTransactionRequest(target);
			target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_REQUESTED_TO_BECOME_FRIENDS).addCharName(player));
			target.sendPacket(new FriendAddRequest(player.getName()));
		}
		else
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(_targetName));
	}
}