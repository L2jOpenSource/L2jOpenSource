package com.l2jfrozen.gameserver.communitybbs;

import java.util.HashMap;
import java.util.Map;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.communitybbs.Manager.BaseBBSManager;
import com.l2jfrozen.gameserver.communitybbs.Manager.ClanBBSManager;
import com.l2jfrozen.gameserver.communitybbs.Manager.PostBBSManager;
import com.l2jfrozen.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jfrozen.gameserver.communitybbs.Manager.TopBBSManager;
import com.l2jfrozen.gameserver.communitybbs.Manager.TopicBBSManager;
import com.l2jfrozen.gameserver.handler.IBBSHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ShowBoard;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

import main.EngineModsManager;

public class CommunityBoard
{
	private static CommunityBoard instance;
	private final Map<String, IBBSHandler> handlers;
	
	public CommunityBoard()
	{
		handlers = new HashMap<>();
	}
	
	public static CommunityBoard getInstance()
	{
		if (instance == null)
		{
			instance = new CommunityBoard();
		}
		
		return instance;
	}
	
	/**
	 * by Azagthtot
	 * @param handler as IBBSHandler
	 */
	public void registerBBSHandler(final IBBSHandler handler)
	{
		for (final String s : handler.getBBSCommands())
		{
			handlers.put(s, handler);
		}
	}
	
	/**
	 * by Azagthtot
	 * @param client
	 * @param command
	 */
	public void handleCommands(final L2GameClient client, final String command)
	{
		L2PcInstance activeChar = client.getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		if (EngineModsManager.onCommunityBoard(activeChar, command))
		{
			return;
		}
		
		if (Config.COMMUNITY_TYPE.equals("full"))
		{
			String cmd = command.substring(4);
			String params = "";
			final int iPos = cmd.indexOf(" ");
			if (iPos != -1)
			{
				params = cmd.substring(iPos + 1);
				cmd = cmd.substring(0, iPos);
				
			}
			final IBBSHandler bbsh = handlers.get(cmd);
			if (bbsh != null)
			{
				bbsh.handleCommand(cmd, activeChar, params);
			}
			else
			{
				if (command.startsWith("_bbsclan"))
				{
					ClanBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbsmemo"))
				{
					TopicBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbsgetfav"))
				{
					String text = HtmCache.getInstance().getHtm("data/html/CommunityBoard/favorites.htm");
					if (text != null)
					{
						text = text.replace("%username%", activeChar.getName());
						text = text.replace("%charId%", String.valueOf(activeChar.getObjectId()));
						BaseBBSManager.separateAndSend(text, activeChar);
					}
					else
					{
						ShowBoard sb = new ShowBoard("<html><body><br><br><br><br></body></html>", "101");
						activeChar.sendPacket(sb);
						sb = null;
						activeChar.sendPacket(new ShowBoard(null, "102"));
						activeChar.sendPacket(new ShowBoard(null, "103"));
					}
					
				}
				else if (command.startsWith("_bbstopics"))
				{
					TopicBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbsposts"))
				{
					PostBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbstop"))
				{
					TopBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbshome"))
				{
					TopBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbsloc"))
				{
					RegionBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else
				{
					ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101");
					activeChar.sendPacket(sb);
					sb = null;
					activeChar.sendPacket(new ShowBoard(null, "102"));
					activeChar.sendPacket(new ShowBoard(null, "103"));
					
				}
			}
			
		}
		else if (Config.COMMUNITY_TYPE.equals("old"))
		{
			RegionBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CB_OFFLINE));
		}
		
		activeChar = null;
	}
	
	/**
	 * @param client
	 * @param url
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 */
	public void handleWriteCommands(final L2GameClient client, final String url, final String arg1, final String arg2, final String arg3, final String arg4, final String arg5)
	{
		L2PcInstance activeChar = client.getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		if (Config.COMMUNITY_TYPE.equals("full"))
		{
			if (url.equals("Topic"))
			{
				TopicBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
			}
			else if (url.equals("Post"))
			{
				PostBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
			}
			else if (url.equals("Region"))
			{
				RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
			}
			else if (url.equals("Notice"))
			{
				ClanBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
			}
			else
			{
				ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + url + " is not implemented yet</center><br><br></body></html>", "101");
				activeChar.sendPacket(sb);
				sb = null;
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
			}
		}
		else if (Config.COMMUNITY_TYPE.equals("old"))
		{
			RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>The Community board is currently disable</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			sb = null;
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
		
		activeChar = null;
	}
}