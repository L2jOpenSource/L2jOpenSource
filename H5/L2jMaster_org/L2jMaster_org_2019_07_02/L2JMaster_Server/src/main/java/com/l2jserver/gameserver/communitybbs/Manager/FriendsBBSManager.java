/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.communitybbs.Manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.cache.HtmCache;
import com.l2jserver.gameserver.data.sql.impl.CharNameTable;
import com.l2jserver.gameserver.model.BlockList;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.FriendList;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class FriendsBBSManager extends BaseBBSManager
{
	private static final Logger LOG = LoggerFactory.getLogger(FriendsBBSManager.class);
	
	private static final String FRIENDLIST_DELETE_BUTTON = "<br>\n<table><tr><td width=10></td><td>Are you sure you want to delete all friends from the friends list?&nbsp;</td><td width=20></td><td><button value=\"OK\" action=\"bypass _friend;delall\" back=\"l2ui_ct1.button.button_df_small_down\" width=70 height=25 fore=\"l2ui_ct1.button.button_df_small\"></td></tr></table>";
	private static final String BLOCKLIST_DELETE_BUTTON = "<br>\n<table><tr><td width=10></td><td>Do you want to delete all characters from the block list?&nbsp;</td><td width=20></td><td><button value=\"OK\" action=\"bypass _block;delall\" back=\"l2ui_ct1.button.button_df_small_down\" width=70 height=25 fore=\"l2ui_ct1.button.button_df_small\"></td></tr></table>";
	
	protected FriendsBBSManager()
	{
	}
	
	public static FriendsBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("_friendlist"))
		{
			showFriendsList(activeChar, false);
		}
		
		else if (command.startsWith("_blocklist"))
		{
			showBlockList(activeChar, false);
		}
		else if (command.startsWith("_friend"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();// _friend
			String action = st.nextToken();
			
			if (action.equals("select"))
			{
				activeChar.selectFriend((st.hasMoreTokens()) ? Integer.valueOf(st.nextToken()) : 0);
				showFriendsList(activeChar, false);
			}
			else if (action.equals("deselect"))
			{
				activeChar.deselectFriend((st.hasMoreTokens()) ? Integer.valueOf(st.nextToken()) : 0);
				showFriendsList(activeChar, false);
			}
			else if (action.equals("delall"))
			{
				try (Connection con = ConnectionFactory.getInstance().getConnection())
				{
					PreparedStatement statement = con.prepareStatement("DELETE FROM character_friends WHERE charId = ? OR friendId = ?");
					statement.setInt(1, activeChar.getObjectId());
					statement.setInt(2, activeChar.getObjectId());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					LOG.warn("could not delete friends objectid: ", e);
				}
				
				for (int friendId : activeChar.getFriendList())
				{
					L2PcInstance player = L2World.getInstance().getPlayer(friendId);
					if (player != null)
					{
						player.getFriendList().remove(Integer.valueOf(activeChar.getObjectId()));
						player.getSelectedFriendList().remove(Integer.valueOf(activeChar.getObjectId()));
						
						player.sendPacket(new FriendList(player)); // update friendList *heavy method*
					}
				}
				
				activeChar.getFriendList().clear();
				activeChar.getSelectedFriendList().clear();
				showFriendsList(activeChar, false);
				
				activeChar.sendMessage("You have cleared your friend list.");
				activeChar.sendPacket(new FriendList(activeChar));
			}
			else if (action.equals("delconfirm"))
			{
				showFriendsList(activeChar, true);
			}
			else if (action.equals("del"))
			{
				try (Connection con = ConnectionFactory.getInstance().getConnection())
				{
					for (int friendId : activeChar.getSelectedFriendList())
					{
						PreparedStatement statement = con.prepareStatement("DELETE FROM character_friends WHERE (charId = ? AND friendId = ?) OR (charId = ? AND friendId = ?)");
						statement.setInt(1, activeChar.getObjectId());
						statement.setInt(2, friendId);
						statement.setInt(3, friendId);
						statement.setInt(4, activeChar.getObjectId());
						statement.execute();
						statement.close();
						
						String name = CharNameTable.getInstance().getNameById(friendId);
						
						L2PcInstance player = L2World.getInstance().getPlayer(friendId);
						if (player != null)
						{
							player.getFriendList().remove(Integer.valueOf(activeChar.getObjectId()));
							player.sendPacket(new FriendList(player)); // update friendList *heavy method*
						}
						
						// Player deleted from your friendlist
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(name));
						
						activeChar.getFriendList().remove(Integer.valueOf(friendId));
					}
				}
				catch (Exception e)
				{
					LOG.warn("could not delete friend objectid: ", e);
				}
				
				activeChar.getSelectedFriendList().clear();
				showFriendsList(activeChar, false);
				
				activeChar.sendPacket(new FriendList(activeChar)); // update friendList *heavy method*
			}
		}
		else if (command.startsWith("_block"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			if (st.countTokens() < 2)
			{
				showBlockList(activeChar, false);
				return;
			}
			st.nextToken();
			String action = st.nextToken();
			
			if (action.equals("select"))
			{
				activeChar.selectBlock((st.hasMoreTokens()) ? Integer.valueOf(st.nextToken()) : 0);
				showBlockList(activeChar, false);
			}
			else if (action.equals("deselect"))
			{
				activeChar.deselectBlock((st.hasMoreTokens()) ? Integer.valueOf(st.nextToken()) : 0);
				showBlockList(activeChar, false);
			}
			else if (action.equals("delall"))
			{
				List<Integer> list = new ArrayList<>();
				list.addAll(activeChar.getBlockList().getBlockList());
				
				for (Integer blockId : list)
				{
					BlockList.removeFromBlockList(activeChar, blockId);
				}
				
				activeChar.getSelectedBlocksList().clear();
				showBlockList(activeChar, false);
			}
			else if (action.equals("delconfirm"))
			{
				showBlockList(activeChar, true);
			}
			else if (action.equals("del"))
			{
				for (Integer blockId : activeChar.getSelectedBlocksList())
				{
					BlockList.removeFromBlockList(activeChar, blockId);
				}
				
				activeChar.getSelectedBlocksList().clear();
				showBlockList(activeChar, false);
			}
			else if (action.equals("CBblock"))
			{
				if (st.hasMoreTokens())
				{
					String nameToBlock = st.nextToken().trim();
					if (nameToBlock.length() > 3)
					{
						final int targetId = CharNameTable.getInstance().getIdByName(nameToBlock);
						if (targetId == -1)
						{
							activeChar.sendMessage("Player not found");
							showBlockList(activeChar, false);
							return;
						}
						BlockList.addToBlockList(activeChar, targetId);
					}
				}
				showBlockList(activeChar, false);
			}
		}
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		
	}
	
	private static void showFriendsList(L2PcInstance activeChar, boolean delMsg)
	{
		String content = HtmCache.getInstance().getHtm(CB_PATH + "friends_list.html");
		if (content == null)
		{
			return;
		}
		
		// Retrieve activeChar's friendlist and selected
		final List<Integer> list = activeChar.getFriendList();
		final List<Integer> slist = activeChar.getSelectedFriendList();
		
		// Friendlist
		if (list.isEmpty())
		{
			content = content.replaceAll("%friendslist%", "");
		}
		else
		{
			String friends = "";
			
			for (Integer id : list)
			{
				if (slist.contains(id))
				{
					continue;
				}
				
				String friendName = CharNameTable.getInstance().getNameById(id);
				if (friendName == null)
				{
					continue;
				}
				
				L2PcInstance friend = L2World.getInstance().getPlayer(friendName);
				friends += "<a action=\"bypass _friend;select;" + id + "\">[Select]</a>&nbsp;" + friendName + " " + (((friend != null) && friend.isOnline()) ? "(on)" : "(off)") + "<br1>";
			}
			
			content = content.replaceAll("%friendslist%", friends);
		}
		
		// Selected friendlist
		if (slist.isEmpty())
		{
			content = content.replaceAll("%selectedFriendsList%", "");
		}
		else
		{
			String selectedFriends = "";
			
			for (Integer id : slist)
			{
				String friendName = CharNameTable.getInstance().getNameById(id);
				if (friendName == null)
				{
					continue;
				}
				
				L2PcInstance friend = L2World.getInstance().getPlayer(friendName);
				selectedFriends += "<a action=\"bypass _friend;deselect;" + id + "\">[Deselect]</a>&nbsp;" + friendName + " " + (((friend != null) && friend.isOnline()) ? "(on)" : "(off)") + "<br1>";
			}
			
			content = content.replaceAll("%selectedFriendsList%", selectedFriends);
		}
		
		// Delete button.
		content = content.replaceAll("%deleteMSG%", (delMsg) ? FRIENDLIST_DELETE_BUTTON : "");
		
		separateAndSend(content, activeChar);
	}
	
	private static void showBlockList(L2PcInstance activeChar, boolean delMsg)
	{
		String content = HtmCache.getInstance().getHtm(CB_PATH + "friends_block_list.html");
		if (content == null)
		{
			return;
		}
		
		// Retrieve activeChar's blocklist and selected
		final List<Integer> list = activeChar.getBlockList().getBlockList();
		final List<Integer> slist = activeChar.getSelectedBlocksList();
		
		// Blocklist
		if (list.isEmpty())
		{
			content = content.replaceAll("%blocklist%", "");
		}
		else
		{
			String selectedBlocks = "";
			
			for (Integer id : list)
			{
				if (slist.contains(id))
				{
					continue;
				}
				
				String blockName = CharNameTable.getInstance().getNameById(id);
				if (blockName == null)
				{
					continue;
				}
				
				L2PcInstance block = L2World.getInstance().getPlayer(blockName);
				selectedBlocks += "<a action=\"bypass _block;select;" + id + "\">[Select]</a>&nbsp;" + blockName + " " + (((block != null) && block.isOnline()) ? "(on)" : "(off)") + "<br1>";
			}
			
			content = content.replaceAll("%blocklist%", selectedBlocks);
		}
		
		// Selected Blocklist
		if (slist.isEmpty())
		{
			content = content.replaceAll("%selectedBlocksList%", "");
		}
		else
		{
			String selectedBlocks = "";
			
			for (Integer id : slist)
			{
				String blockName = CharNameTable.getInstance().getNameById(id);
				if (blockName == null)
				{
					continue;
				}
				
				L2PcInstance block = L2World.getInstance().getPlayer(blockName);
				selectedBlocks += "<a action=\"bypass _block;deselect;" + id + "\">[Deselect]</a>&nbsp;" + blockName + " " + (((block != null) && block.isOnline()) ? "(on)" : "(off)") + "<br1>";
			}
			
			content = content.replaceAll("%selectedBlocksList%", selectedBlocks);
		}
		
		// Delete button.
		content = content.replaceAll("%deleteMSG%", (delMsg) ? BLOCKLIST_DELETE_BUTTON : "");
		
		separateAndSend(content, activeChar);
	}
	
	private static class SingletonHolder
	{
		protected static final FriendsBBSManager _instance = new FriendsBBSManager();
	}
}