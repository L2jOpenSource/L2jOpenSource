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
package l2r.gameserver.communitybbs;

import l2r.Config;
import l2r.gameserver.communitybbs.Managers.ClanBBSManager;
import l2r.gameserver.communitybbs.Managers.MailBBSManager;
import l2r.gameserver.communitybbs.Managers.PostBBSManager;
import l2r.gameserver.communitybbs.Managers.TopBBSManager;
import l2r.gameserver.communitybbs.Managers.TopicBBSManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.L2GameClient;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ShowBoard;
import gr.reunion.interf.ReunionEvents;

public class BoardsManager
{
	public void handleCommands(L2GameClient client, String command)
	{
		L2PcInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (ReunionEvents.cbBypass(activeChar, command))
		{
			return;
		}
		
		if (!Config.ENABLE_COMMUNITY)
		{
			activeChar.sendPacket(SystemMessageId.CB_OFFLINE);
			return;
		}
		
		if (command.startsWith("_bbsclan"))
		{
			ClanBBSManager.getInstance().cbByPass(command, activeChar);
		}
		else if (command.startsWith("_bbsmemo"))
		{
			TopicBBSManager.getInstance().cbByPass(command, activeChar);
		}
		else if (command.startsWith("_bbstopics"))
		{
			TopicBBSManager.getInstance().cbByPass(command, activeChar);
		}
		else if (command.startsWith("_bbsposts"))
		{
			PostBBSManager.getInstance().cbByPass(command, activeChar);
		}
		else if (command.startsWith("_bbstop"))
		{
			TopBBSManager.getInstance().cbByPass(command, activeChar);
		}
		else if (command.startsWith("_bbshome"))
		{
			TopBBSManager.getInstance().cbByPass(command, activeChar);
		}
		else if (command.startsWith("_maillist"))
		{
			MailBBSManager.getInstance().cbByPass(command, activeChar);
		}
		else if (command.startsWith("_friendlist_0_") || command.startsWith("_bbs_friends") || command.startsWith("_bbsfriends"))
		{
			
		}
		else if (command.startsWith("_bbsloc"))
		{
			// RegionBBSManager.getInstance().cbByPass(command, activeChar);
		}
		else if (command.startsWith("_bbsgetfav"))
		{
			// RegionBBSManager.getInstance().cbByPass(command, activeChar);
		}
		else if (command.startsWith("_bbslink"))
		{
			// RegionBBSManager.getInstance().cbByPass(command, activeChar);
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}
	
	public void handleWriteCommands(L2GameClient client, String url, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		L2PcInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (url.equals("Topic"))
		{
			TopicBBSManager.getInstance().parsewrite(url, arg1, arg2, arg3, arg4, arg5, activeChar);
		}
		else if (url.equals("Post"))
		{
			PostBBSManager.getInstance().parsewrite(url, arg1, arg2, arg3, arg4, arg5, activeChar);
		}
		else if (url.equals("Region"))
		{
			// Future usage
			// RegionBBSManager.getInstance().parsewrite(url, arg1, arg2, arg3, arg4, arg5, activeChar);
		}
		else if (url.equals("Friends"))
		{
			// Future usage
			// FriendsBBSManager.getInstance().parsewrite(url, arg1, arg2, arg3, arg4, arg5, activeChar);
		}
		else if (url.equals("Notice"))
		{
			ClanBBSManager.getInstance().parsewrite(url, arg1, arg2, arg3, arg4, arg5, activeChar);
		}
		else if (url.equals("Mail"))
		{
			MailBBSManager.getInstance().parsewrite(url, arg1, arg2, arg3, arg4, arg5, activeChar);
		}
		else
		{
			// no nothing
		}
		return;
	}
	
	public static BoardsManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final BoardsManager _instance = new BoardsManager();
	}
}
