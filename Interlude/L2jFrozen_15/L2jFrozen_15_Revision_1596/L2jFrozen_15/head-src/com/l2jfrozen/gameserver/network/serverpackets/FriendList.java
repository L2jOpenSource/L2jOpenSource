package com.l2jfrozen.gameserver.network.serverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * Support for "Chat with Friends" dialog. Format: ch (hdSdh) h: Total Friend Count h: Unknown d: Player Object ID S: Friend Name d: Online/Offline h: Unknown
 * @author Tempy
 */
public class FriendList extends L2GameServerPacket
{
	private static Logger LOGGER = Logger.getLogger(FriendList.class);
	private static final String SELECT_CHARACTER_FRIEND = "SELECT friend_id, friend_name FROM character_friends WHERE char_id=? AND not_blocked = 1 ORDER BY friend_name ASC";
	
	private final L2PcInstance activeChar;
	
	public FriendList(final L2PcInstance character)
	{
		activeChar = character;
	}
	
	@Override
	protected final void writeImpl()
	{
		if (activeChar == null)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHARACTER_FRIEND))
		{
			statement.setInt(1, activeChar.getObjectId());
			
			try (ResultSet rset = statement.executeQuery())
			{
				// Obtain the total number of friend entries for this player.
				rset.last();
				
				writeC(0xfa);
				writeD(rset.getRow());
				
				if (rset.getRow() > 0)
				{
					rset.beforeFirst();
					
					while (rset.next())
					{
						final int friendId = rset.getInt("friend_id");
						final String friendName = rset.getString("friend_name");
						
						if (friendId == activeChar.getObjectId())
						{
							continue;
						}
						
						final L2PcInstance friend = L2World.getInstance().getPlayer(friendName);
						
						// writeH(0); // ??
						writeD(friendId);
						writeS(friendName);
						
						if (friend == null)
						{
							writeD(0); // offline
							writeD(0x00);
						}
						else
						{
							writeD(1); // online
							writeD(friendId);
						}
						
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("FriendList.writeImpl Error found in " + activeChar.getName() + "'s FriendList", e);
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] FA FriendList";
	}
}