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
package l2r.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2r.L2DatabaseFactory;
import l2r.gameserver.datatables.sql.CharNameTable;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class ...
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class BlockList
{
	private static Logger _log = LoggerFactory.getLogger(BlockList.class);
	private static Map<Integer, List<Integer>> _offlineList = new FastMap<Integer, List<Integer>>().shared();
	
	private final L2PcInstance _owner;
	private List<Integer> _blockList;
	
	public BlockList(L2PcInstance owner)
	{
		_owner = owner;
		_blockList = _offlineList.get(owner.getObjectId());
		if (_blockList == null)
		{
			_blockList = loadList(_owner.getObjectId());
		}
	}
	
	private void addToBlockList(int target)
	{
		_blockList.add(target);
		updateInDB(target, true);
	}
	
	private void removeFromBlockList(int target)
	{
		_blockList.remove(Integer.valueOf(target));
		updateInDB(target, false);
	}
	
	public void playerLogout()
	{
		_offlineList.put(_owner.getObjectId(), _blockList);
	}
	
	private static List<Integer> loadList(int ObjId)
	{
		List<Integer> list = new FastList<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT friendId FROM character_friends WHERE charId=? AND relation=1");
			statement.setInt(1, ObjId);
			ResultSet rset = statement.executeQuery();
			
			int friendId;
			while (rset.next())
			{
				friendId = rset.getInt("friendId");
				if (friendId == ObjId)
				{
					continue;
				}
				list.add(friendId);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Error found in " + ObjId + " FriendList while loading BlockList: " + e.getMessage(), e);
		}
		return list;
	}
	
	private void updateInDB(int targetId, boolean state)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			if (state) // add
			{
				try (PreparedStatement statement = con.prepareStatement("INSERT INTO character_friends (charId, friendId, relation) VALUES (?, ?, 1)"))
				{
					statement.setInt(1, _owner.getObjectId());
					statement.setInt(2, targetId);
					statement.execute();
				}
			}
			else
			// remove
			{
				try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_friends WHERE charId=? AND friendId=? AND relation=1"))
				{
					statement.setInt(1, _owner.getObjectId());
					statement.setInt(2, targetId);
					statement.execute();
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Could not add block player: " + e.getMessage(), e);
		}
	}
	
	public boolean isInBlockList(L2PcInstance target)
	{
		return _blockList.contains(target.getObjectId());
	}
	
	public boolean isInBlockList(int targetId)
	{
		return _blockList.contains(targetId);
	}
	
	private boolean isBlockAll()
	{
		return _owner.getMessageRefusal();
	}
	
	public static boolean isBlocked(L2PcInstance listOwner, L2PcInstance target)
	{
		BlockList blockList = listOwner.getBlockList();
		return blockList.isBlockAll() || blockList.isInBlockList(target);
	}
	
	public static boolean isBlocked(L2PcInstance listOwner, int targetId)
	{
		BlockList blockList = listOwner.getBlockList();
		return blockList.isBlockAll() || blockList.isInBlockList(targetId);
	}
	
	private void setBlockAll(boolean state)
	{
		_owner.setMessageRefusal(state);
	}
	
	private List<Integer> getBlockList()
	{
		return _blockList;
	}
	
	public static void addToBlockList(L2PcInstance listOwner, int targetId)
	{
		if (listOwner == null)
		{
			return;
		}
		
		String charName = CharNameTable.getInstance().getNameById(targetId);
		
		if (listOwner.getFriendList().contains(targetId))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST);
			sm.addString(charName);
			listOwner.sendPacket(sm);
			return;
		}
		
		if (listOwner.getBlockList().getBlockList().contains(targetId))
		{
			listOwner.sendMessage("Already in ignore list.");
			return;
		}
		
		listOwner.getBlockList().addToBlockList(targetId);
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_ADDED_TO_YOUR_IGNORE_LIST);
		sm.addString(charName);
		listOwner.sendPacket(sm);
		
		L2PcInstance player = L2World.getInstance().getPlayer(targetId);
		
		if (player != null)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
			sm.addString(listOwner.getName());
			player.sendPacket(sm);
		}
	}
	
	public static void removeFromBlockList(L2PcInstance listOwner, int targetId)
	{
		if (listOwner == null)
		{
			return;
		}
		
		SystemMessage sm;
		
		String charName = CharNameTable.getInstance().getNameById(targetId);
		
		if (!listOwner.getBlockList().getBlockList().contains(targetId))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT);
			listOwner.sendPacket(sm);
			return;
		}
		
		listOwner.getBlockList().removeFromBlockList(targetId);
		
		sm = SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST);
		sm.addString(charName);
		listOwner.sendPacket(sm);
	}
	
	public static boolean isInBlockList(L2PcInstance listOwner, L2PcInstance target)
	{
		return listOwner.getBlockList().isInBlockList(target);
	}
	
	public boolean isBlockAll(L2PcInstance listOwner)
	{
		return listOwner.getBlockList().isBlockAll();
	}
	
	public static void setBlockAll(L2PcInstance listOwner, boolean newValue)
	{
		listOwner.getBlockList().setBlockAll(newValue);
	}
	
	public static void sendListToOwner(L2PcInstance listOwner)
	{
		int i = 1;
		listOwner.sendPacket(SystemMessageId.BLOCK_LIST_HEADER);
		for (int playerId : listOwner.getBlockList().getBlockList())
		{
			listOwner.sendMessage((i++) + ". " + CharNameTable.getInstance().getNameById(playerId));
		}
		listOwner.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
	}
	
	/**
	 * @param ownerId object id of owner block list
	 * @param targetId object id of potential blocked player
	 * @return true if blocked
	 */
	public static boolean isInBlockList(int ownerId, int targetId)
	{
		L2PcInstance player = L2World.getInstance().getPlayer(ownerId);
		if (player != null)
		{
			return BlockList.isBlocked(player, targetId);
		}
		if (!_offlineList.containsKey(ownerId))
		{
			_offlineList.put(ownerId, loadList(ownerId));
		}
		return _offlineList.get(ownerId).contains(targetId);
	}
}
