package net.sf.l2j.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.FriendList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendDel extends L2GameClientPacket
{
	private static final String DELETE_FRIEND = "DELETE FROM character_friends WHERE (char_id = ? AND friend_id = ?) OR (char_id = ? AND friend_id = ?)";
	
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
		
		final int targetId = PlayerInfoTable.getInstance().getPlayerObjectId(_targetName);
		if (targetId == -1 || !player.getFriendList().contains(targetId))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_NOT_ON_YOUR_FRIENDS_LIST).addString(_targetName));
			return;
		}
		
		// Player deleted from your friendlist
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(_targetName));
		
		player.getFriendList().remove(Integer.valueOf(targetId));
		player.sendPacket(new FriendList(player)); // update friendList *heavy method*
		
		final Player target = World.getInstance().getPlayer(_targetName);
		if (target != null)
		{
			target.getFriendList().remove(Integer.valueOf(player.getObjectId()));
			target.sendPacket(new FriendList(target)); // update friendList *heavy method*
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_FRIEND))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, targetId);
			ps.setInt(3, targetId);
			ps.setInt(4, player.getObjectId());
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't delete friendId {} for {}.", e, targetId, player.toString());
		}
	}
}