package com.l2jfrozen.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.FriendList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * sample 5F 01 00 00 00 format cdd
 */
public final class RequestAnswerFriendInvite extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestAnswerFriendInvite.class);
	private static final String INSERT_CHARACTER_FRIEND = "INSERT INTO character_friends (char_id, friend_id, friend_name, not_blocked) VALUES (?, ?, ?, ?), (?, ?, ?,?)";
	private static final String SELECT_CHARACTER_FRIENDS = "SELECT friend_name FROM character_friends WHERE char_id=? AND not_blocked=1";
	
	private int response;
	
	@Override
	protected void readImpl()
	{
		response = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player != null)
		{
			final L2PcInstance requestor = player.getActiveRequester();
			if (requestor == null)
			{
				return;
			}
			
			if (response == 1)
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement(INSERT_CHARACTER_FRIEND))
				{
					statement.setInt(1, requestor.getObjectId());
					statement.setInt(2, player.getObjectId());
					statement.setString(3, player.getName());
					statement.setInt(4, 1);
					statement.setInt(5, player.getObjectId());
					statement.setInt(6, requestor.getObjectId());
					statement.setString(7, requestor.getName());
					statement.setInt(8, 1);
					statement.executeUpdate();
					DatabaseUtils.close(statement);
					SystemMessage msg = new SystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_INVITING_FRIEND);
					requestor.sendPacket(msg);
					
					// Player added to your friendlist
					msg = new SystemMessage(SystemMessageId.S1_ADDED_TO_FRIENDS);
					msg.addString(player.getName());
					requestor.sendPacket(msg);
					requestor.getFriendList().add(player.getName());
					
					// has joined as friend.
					msg = new SystemMessage(SystemMessageId.S1_JOINED_AS_FRIEND);
					msg.addString(requestor.getName());
					player.sendPacket(msg);
					player.getFriendList().add(requestor.getName());
					
					msg = null;
					
					// friend list rework ;)
					notifyFriends(player);
					notifyFriends(requestor);
					player.sendPacket(new FriendList(player));
					requestor.sendPacket(new FriendList(requestor));
				}
				catch (Exception e)
				{
					LOGGER.error("RequestAnswerFriendInvite.runImpl : Could not add friend objectid", e);
				}
			}
			else
			{
				final SystemMessage msg = new SystemMessage(SystemMessageId.FAILED_TO_INVITE_A_FRIEND);
				requestor.sendPacket(msg);
			}
			
			player.setActiveRequester(null);
			requestor.onTransactionResponse();
		}
	}
	
	private void notifyFriends(final L2PcInstance cha)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHARACTER_FRIENDS))
		{
			statement.setInt(1, cha.getObjectId());
			
			try (ResultSet rset = statement.executeQuery())
			{
				L2PcInstance friend;
				String friendName;
				
				while (rset.next())
				{
					friendName = rset.getString("friend_name");
					friend = L2World.getInstance().getPlayer(friendName);
					
					if (friend != null)
					{
						friend.sendPacket(new FriendList(friend));
					}
				}
			}
		}
		
		catch (Exception e)
		{
			LOGGER.error("RequestAnswerFriendInvite.notifyFriends : Could not restore friend data", e);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 5F RequestAnswerFriendInvite";
	}
}