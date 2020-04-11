package com.l2jfrozen.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.FriendList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public final class RequestFriendDel extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestFriendDel.class);
	private static final String SELECT_FRIEND_ID = "SELECT friend_id FROM character_friends, characters WHERE char_id=? AND friend_id=obj_id AND char_name=? AND not_blocked=1";
	private static final String DELETE_CHARACTER_FRIEND = "DELETE FROM character_friends WHERE (char_id=? AND friend_id=?) OR (char_id=? AND friend_id=?)";
	
	private String name;
	
	@Override
	protected void readImpl()
	{
		try
		{
			name = readS();
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			name = null;
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (name == null)
		{
			return;
		}
		
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!activeChar.getFriendList().contains(name))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_NOT_ON_YOUR_FRIENDS_LIST);
			sm.addString(name);
			activeChar.sendPacket(sm);
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			L2PcInstance friend = L2World.getInstance().getPlayer(name);
			
			int objectId = -1;
			
			if (friend != null)
			{
				objectId = friend.getObjectId();
			}
			else
			{
				try (PreparedStatement statement = con.prepareStatement(SELECT_FRIEND_ID))
				{
					statement.setInt(1, activeChar.getObjectId());
					statement.setString(2, name);
					
					try (ResultSet rset = statement.executeQuery())
					{
						if (rset.next())
						{
							objectId = rset.getInt("friend_id");
							
							try (PreparedStatement delStatement = con.prepareStatement(DELETE_CHARACTER_FRIEND))
							{
								delStatement.setInt(1, activeChar.getObjectId());
								delStatement.setInt(2, objectId);
								delStatement.setInt(3, objectId);
								delStatement.setInt(4, activeChar.getObjectId());
								delStatement.executeUpdate();
							}
						}
						else
						{
							// Player is not in your friendlist
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_NOT_ON_YOUR_FRIENDS_LIST);
							sm.addString(name);
							activeChar.sendPacket(sm);
							return;
						}
					}
				}
			}
			
			// Player deleted from your friendlist
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST);
			sm.addString(name);
			activeChar.sendPacket(sm);
			
			activeChar.getFriendList().remove(name);
			activeChar.sendPacket(new FriendList(activeChar));
			
			if (friend != null)
			{
				friend.getFriendList().remove(activeChar.getName());
				friend.sendPacket(new FriendList(friend));
			}
		}
		catch (Exception e)
		{
			LOGGER.error("RequestFriendDel : Could not delete friend for player" + activeChar.getName(), e);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 61 RequestFriendDel";
	}
}