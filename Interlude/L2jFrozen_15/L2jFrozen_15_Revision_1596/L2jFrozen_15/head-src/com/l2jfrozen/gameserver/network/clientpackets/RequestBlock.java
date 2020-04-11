package com.l2jfrozen.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public final class RequestBlock extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestBlock.class);
	private static final String SELECT_CHARACTER_FRIEND = "SELECT char_id, friend_id, friend_name, not_blocked FROM character_friends WHERE char_id=? AND friend_name=?";
	private static final String UPDATE_CHARACTER_FRIEND = "UPDATE character_friends SET not_blocked = ? WHERE char_id=?  AND friend_name=?";
	private static final String INSERT_CHARACTER_FRIEND = "INSERT INTO character_friends (char_id, friend_id, friend_name, not_blocked) VALUES (?, ?, ?, ?)";
	private static final String DELETE_CHARACTER_FRIEND = "DELETE FROM character_friends WHERE char_id=? AND friend_name=?";
	
	private final static int BLOCK = 0;
	private final static int UNBLOCK = 1;
	private final static int BLOCKLIST = 2;
	private final static int ALLBLOCK = 3;
	private final static int ALLUNBLOCK = 4;
	
	private String name;
	private Integer type;
	
	// private L2PcInstance target;
	
	@Override
	protected void readImpl()
	{
		type = readD(); // 0x00 - block, 0x01 - unblock, 0x03 - allblock, 0x04 - allunblock
		
		if (type == BLOCK || type == UNBLOCK)
		{
			name = readS();
			// target = L2World.getInstance().getPlayer(name);
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		switch (type)
		{
			case BLOCK:
			case UNBLOCK:
				
				final L2PcInstance target = L2World.getInstance().getPlayer(name);
				
				if (target == null)
				{
					// Incorrect player name.
					activeChar.sendPacket(new SystemMessage(SystemMessageId.FAILED_TO_REGISTER_TO_IGNORE_LIST));
					return;
				}
				
				if (target.isGM())
				{
					// Cannot block a GM character.
					activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_IMPOSE_A_BLOCK_AN_A_GM));
					return;
				}
				
				if (type == BLOCK)
				{
					
					if (activeChar.getBlockList().isInBlockList(name))
					{
						// Player is already in your blocklist
						activeChar.sendPacket(new SystemMessage(SystemMessageId.FAILED_TO_REGISTER_TO_IGNORE_LIST));
						return;
					}
					
					activeChar.getBlockList().addToBlockList(name);
					
					try (Connection con = L2DatabaseFactory.getInstance().getConnection();
						PreparedStatement statement = con.prepareStatement(SELECT_CHARACTER_FRIEND))
					{
						statement.setInt(1, activeChar.getObjectId());
						statement.setString(2, name);
						final ResultSet rset = statement.executeQuery();
						
						if (rset.next())
						{
							try (PreparedStatement updatePst = con.prepareStatement(UPDATE_CHARACTER_FRIEND))
							{
								updatePst.setInt(1, type);
								updatePst.setInt(2, activeChar.getObjectId());
								updatePst.setString(3, name);
								updatePst.executeUpdate();
							}
						}
						else
						{
							try (PreparedStatement insertPst = con.prepareStatement(INSERT_CHARACTER_FRIEND))
							{
								insertPst.setInt(1, activeChar.getObjectId());
								insertPst.setInt(2, target.getObjectId());
								insertPst.setString(3, target.getName());
								insertPst.setInt(4, type);
								insertPst.executeUpdate();
							}
						}
					}
					catch (final Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e.printStackTrace();
						}
						
						LOGGER.warn("could not add blocked objectid: ");
						e.printStackTrace();
					}
				}
				else
				{
					activeChar.getBlockList().removeFromBlockList(name);
					
					try (Connection con = L2DatabaseFactory.getInstance().getConnection();
						PreparedStatement statement = con.prepareStatement(DELETE_CHARACTER_FRIEND))
					{
						statement.setInt(1, activeChar.getObjectId());
						statement.setString(2, name);
						statement.executeUpdate();
					}
					catch (final Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e.printStackTrace();
						}
						
						LOGGER.warn("could not add blocked objectid: ");
						e.printStackTrace();
					}
				}
				break;
			case BLOCKLIST:
				activeChar.sendBlockList();
				break;
			case ALLBLOCK:
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MESSAGE_REFUSAL_MODE));// Update by rocknow
				activeChar.getBlockList().setBlockAll(true);
				break;
			case ALLUNBLOCK:
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MESSAGE_REFUSAL_MODE));// Update by rocknow
				activeChar.getBlockList().setBlockAll(false);
				break;
			default:
				LOGGER.info("Unknown 0x0a block type: " + type);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] A0 RequestBlock";
	}
}