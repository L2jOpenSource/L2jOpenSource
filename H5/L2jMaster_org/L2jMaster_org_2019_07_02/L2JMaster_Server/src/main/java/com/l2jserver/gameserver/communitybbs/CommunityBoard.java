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
package com.l2jserver.gameserver.communitybbs;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jserver.Config;
import com.l2jserver.gameserver.communitybbs.Manager.BuffBBSManager;
import com.l2jserver.gameserver.communitybbs.Manager.ClanBBSManager;
import com.l2jserver.gameserver.communitybbs.Manager.FavoriteBBSManager;
import com.l2jserver.gameserver.communitybbs.Manager.FriendsBBSManager;
import com.l2jserver.gameserver.communitybbs.Manager.HomePageBBSManager;
import com.l2jserver.gameserver.communitybbs.Manager.MailBBSManager;
import com.l2jserver.gameserver.communitybbs.Manager.MemoBBSManager;
import com.l2jserver.gameserver.communitybbs.Manager.PostBBSManager;
import com.l2jserver.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jserver.gameserver.communitybbs.Manager.ServiceBBSManager;
import com.l2jserver.gameserver.communitybbs.Manager.StateBBSManager;
import com.l2jserver.gameserver.communitybbs.Manager.TeleportBBSManager;
import com.l2jserver.gameserver.communitybbs.Manager.TopBBSManager;
import com.l2jserver.gameserver.data.xml.impl.MultisellData;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.L2GameClient;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import com.l2jserver.gameserver.network.serverpackets.ExShowVariationMakeWindow;
import com.l2jserver.gameserver.network.serverpackets.ShowBoard;

import ZeuS.ZeuS;
import main.EngineModsManager;
import main.data.ConfigData;

public class CommunityBoard
{
	/** The bypasses used by the players. */
	private final Map<Integer, String> _bypasses = new ConcurrentHashMap<>();
	
	public void handleCommands(L2GameClient client, String command)
	{
		L2PcInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!Config.ENABLE_COMMUNITY_BOARD)
		{
			activeChar.sendPacket(SystemMessageId.CB_OFFLINE);
			return;
		}
		
		if (!activeChar.isInsideZone(ZoneId.PEACE) && Config.COMMUNITY_BOARD_ON_PEACE_ZONE)
		{
			activeChar.sendMessage("The Community Board only can use in peace zone!");
			return;
		}
		
		if (activeChar.isInDuel() || activeChar.isFlying() || activeChar.isJailed() || activeChar.isAttackingNow() || activeChar.isCastingNow() || activeChar.isDead() || (activeChar.isInCombat() && !Config.COMMUNITY_BOARD_ON_COMBAT) || activeChar.isInsideZone(ZoneId.PVP)
			|| activeChar.isInOlympiadMode() || activeChar.inObserverMode() || activeChar.isAlikeDead() || activeChar.isInSiege())
		{
			activeChar.sendMessage("You cant use Community Board for now.");
			return;
		}
		
		if (ZeuS.cbByPass(activeChar, command))
		{
			return;
		}
		
		if (EngineModsManager.onCommunityBoard(activeChar, command))
		{
			return;
		}
		
		if (command.startsWith("_bbsgetfav") || (command.startsWith("bbs_add_fav") || (command.startsWith("_bbsdelfav_"))))
		{
			FavoriteBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else if (command.startsWith("_bbsclan"))
		{
			ClanBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else if (command.startsWith("_bbslink"))
		{
			HomePageBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else if (command.startsWith("_bbsloc"))
		{
			RegionBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else if (command.startsWith("_bbsmemo"))
		{
			MemoBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else if (command.startsWith("_bbstopics"))
		{
			MemoBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else if (command.startsWith("_bbsposts"))
		{
			PostBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else if (command.startsWith("_maillist") && (!ConfigData.ENABLE_BBS_FAVORITE))
		{
			MailBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else if (command.startsWith("_friend") || command.startsWith("_block"))
		{
			FriendsBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else if (command.startsWith("_bbstop"))
		{
			TopBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else if (command.startsWith("_bbstoprank"))
		{
			TopBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else if (command.startsWith("_bbshome"))
		{
			TopBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else if (command.startsWith("_bbsstat;"))
		{
			if (Config.ALLOW_COMMUNITY_STATS)
			{
				StateBBSManager.getInstance().parsecmd(command, activeChar);
			}
			else
			{
				activeChar.sendMessage("You cant see stats!");
				return;
			}
		}
		else if (command.startsWith("_bbsteleport;"))
		{
			if (Config.ALLOW_COMMUNITY_TELEPORT)
			{
				TeleportBBSManager.getInstance().parsecmd(command, activeChar);
			}
			else
			{
				activeChar.sendMessage("You cant use this service!");
				return;
			}
		}
		else if (command.startsWith("_bbsservice"))
		{
			if (Config.ALLOW_COMMUNITY_SERVICES)
			{
				ServiceBBSManager.getInstance().parsecmd(command, activeChar);
			}
			else
			{
				activeChar.sendMessage("You cant use this service!");
				return;
			}
		}
		else if (command.startsWith("_bbsmultisell;"))
		{
			if (Config.ALLOW_COMMUNITY_MULTISELL)
			{
				if (activeChar.isDead() || activeChar.isAlikeDead() || activeChar.isInSiege() || activeChar.isCastingNow() || activeChar.isInCombat() || activeChar.isAttackingNow() || activeChar.isInOlympiadMode() || activeChar.isJailed() || activeChar.isFlying() || (activeChar.getKarma() > 0)
					|| activeChar.isInDuel())
				{
					activeChar.sendMessage("You cant use this service!");
					return;
				}
				StringTokenizer st = new StringTokenizer(command, ";");
				st.nextToken(); // skip _bbsmultisell
				TopBBSManager.getInstance().parsecmd("_bbstop;" + st.nextToken(), activeChar);
				String multisellId = st.nextToken();
				int multisell = Integer.parseInt(multisellId);
				activeChar.setIsUsingAioMultisell(true);
				MultisellData.getInstance().separateAndSend(multisell, activeChar, null, false);
			}
			else
			{
				activeChar.sendMessage("You cant use this service");
				return;
			}
		}
		else if (command.startsWith("_bbs_buff"))
		{
			if (Config.ALLOW_COMMUNITY_BUFFER)
			{
				StringTokenizer st = new StringTokenizer(command, ";");
				st.nextToken(); // _bbs_buff
				String commandB = st.nextToken();
				
				if (commandB.startsWith("bufferCB"))
				{
					BuffBBSManager.getInstance().parsecmd(command, activeChar);
				}
			}
			else
			{
				activeChar.sendMessage("You cant use this service!");
				return;
			}
		}
		else
		{
			if (command.startsWith("_bbsAugment;add"))
			{
				if (Config.ALLOW_COMMUNITY_MULTISELL)
				{
					TopBBSManager.getInstance().parsecmd(command, activeChar);
					activeChar.sendPacket(SystemMessageId.SELECT_THE_ITEM_TO_BE_AUGMENTED);
					activeChar.sendPacket(new ExShowVariationMakeWindow());
					activeChar.cancelActiveTrade();
					TopBBSManager.getInstance().parsecmd(command, activeChar);
					return;
				}
				activeChar.sendMessage("You cant use this service!");
				return;
			}
			if (command.startsWith("_bbsAugment;remove"))
			{
				if (Config.ALLOW_COMMUNITY_MULTISELL)
				{
					TopBBSManager.getInstance().parsecmd(command, activeChar);
					activeChar.sendPacket(SystemMessageId.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION);
					activeChar.sendPacket(new ExShowVariationCancelWindow());
					activeChar.cancelActiveTrade();
					TopBBSManager.getInstance().parsecmd(command, activeChar);
					return;
				}
				activeChar.sendMessage("You cant use this service!");
				return;
			}
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
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
	public void handleWriteCommands(L2GameClient client, String url, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		L2PcInstance activeChar = client.getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		if (ZeuS.cbByPassWrite(activeChar, url, arg1, arg2, arg3, arg4, arg5))
		{
			return;
		}
		
		if (url.equals("Topic"))
		{
			MemoBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
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
		else if (url.equals("Mail"))
		{
			MailBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		}
		else if (url.equals("_friend") || (url.equals("_block")))
		{
			FriendsBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + url + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
		
	}
	
	public static CommunityBoard getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CommunityBoard _instance = new CommunityBoard();
	}
	
	/**
	 * Sets the last bypass used by the player.
	 * @param player the player
	 * @param title the title
	 * @param bypass the bypass
	 */
	public void addBypass(L2PcInstance player, String title, String bypass)
	{
		_bypasses.put(player.getObjectId(), title + "&" + bypass);
	}
	
	/**
	 * Removes the last bypass used by the player.
	 * @param player the player
	 * @return the last bypass used
	 */
	public String removeBypass(L2PcInstance player)
	{
		return _bypasses.remove(player.getObjectId());
	}
	
	public static void separateAndSend(String html, L2PcInstance acha)
	{
		if (html == null)
		{
			return;
		}
		if (html.length() < 4090)
		{
			acha.sendPacket(new ShowBoard(html, "101"));
			acha.sendPacket(new ShowBoard(null, "102"));
			acha.sendPacket(new ShowBoard(null, "103"));
			
		}
		else if (html.length() < 8180)
		{
			acha.sendPacket(new ShowBoard(html.substring(0, 4090), "101"));
			acha.sendPacket(new ShowBoard(html.substring(4090, html.length()), "102"));
			acha.sendPacket(new ShowBoard(null, "103"));
			
		}
		else if (html.length() < 12270)
		{
			acha.sendPacket(new ShowBoard(html.substring(0, 4090), "101"));
			acha.sendPacket(new ShowBoard(html.substring(4090, 8180), "102"));
			acha.sendPacket(new ShowBoard(html.substring(8180, html.length()), "103"));
			
		}
	}
}