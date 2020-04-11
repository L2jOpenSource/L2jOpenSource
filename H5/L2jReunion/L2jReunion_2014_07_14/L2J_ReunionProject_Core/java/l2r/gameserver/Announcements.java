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
package l2r.gameserver;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javolution.util.FastList;
import l2r.Config;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.script.DateRange;
import l2r.gameserver.util.Broadcast;
import l2r.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Announcements
{
	private static Logger _log = LoggerFactory.getLogger(Announcements.class);
	
	private final List<String> _announcements = new FastList<>();
	private final List<String> _critAnnouncements = new FastList<>();
	private final List<List<Object>> _eventAnnouncements = new FastList<>();
	
	protected Announcements()
	{
		loadAnnouncements();
	}
	
	public static Announcements getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public void loadAnnouncements()
	{
		_announcements.clear();
		_critAnnouncements.clear();
		readFromDisk("data/announcements.txt", _announcements);
		readFromDisk("data/critannouncements.txt", _critAnnouncements);
		
		if (Config.DEBUG)
		{
			_log.info("Announcements: Loaded " + (_announcements.size() + _critAnnouncements.size()) + " announcements.");
		}
	}
	
	public void showAnnouncements(L2PcInstance activeChar)
	{
		for (String announce : _announcements)
		{
			CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, activeChar.getName(), announce);
			activeChar.sendPacket(cs);
		}
		
		for (String critAnnounce : _critAnnouncements)
		{
			CreatureSay cs = new CreatureSay(0, Say2.CRITICAL_ANNOUNCE, activeChar.getName(), critAnnounce);
			activeChar.sendPacket(cs);
		}
		
		for (List<Object> eventAnnounce : _eventAnnouncements)
		{
			List<Object> entry = eventAnnounce;
			
			DateRange validDateRange = (DateRange) entry.get(0);
			String[] msg = (String[]) entry.get(1);
			Date currentDate = new Date();
			
			if (!validDateRange.isValid() || validDateRange.isWithinRange(currentDate))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
				for (String element : msg)
				{
					sm.addString(element);
				}
				activeChar.sendPacket(sm);
			}
			
		}
	}
	
	public void addEventAnnouncement(DateRange validDateRange, String... msg)
	{
		List<Object> entry = new FastList<>();
		entry.add(validDateRange);
		entry.add(msg);
		_eventAnnouncements.add(entry);
	}
	
	public void listAnnouncements(L2PcInstance activeChar)
	{
		String content = HtmCache.getInstance().getHtmForce(activeChar.getHtmlPrefix(), "data/html/admin/announce.htm");
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(content);
		final StringBuilder replyMSG = StringUtil.startAppend(500, "<br>");
		for (int i = 0; i < _announcements.size(); i++)
		{
			StringUtil.append(replyMSG, "<table width=260><tr><td width=220>", _announcements.get(i), "</td><td width=40>" + "<button value=\"Delete\" action=\"bypass -h admin_del_announcement ", String.valueOf(i), "\" width=60 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>");
		}
		adminReply.replace("%announces%", replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	public void listCritAnnouncements(L2PcInstance activeChar)
	{
		String content = HtmCache.getInstance().getHtmForce(activeChar.getHtmlPrefix(), "data/html/admin/critannounce.htm");
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(content);
		final StringBuilder replyMSG = StringUtil.startAppend(500, "<br>");
		for (int i = 0; i < _critAnnouncements.size(); i++)
		{
			StringUtil.append(replyMSG, "<table width=260><tr><td width=220>", _critAnnouncements.get(i), "</td><td width=40>" + "<button value=\"Delete\" action=\"bypass -h admin_del_critannouncement ", String.valueOf(i), "\" width=60 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>");
		}
		adminReply.replace("%critannounces%", replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	public void addAnnouncement(String text)
	{
		_announcements.add(text);
		saveToDisk(false);
	}
	
	public void delAnnouncement(int line)
	{
		_announcements.remove(line);
		saveToDisk(false);
	}
	
	public void addCritAnnouncement(String text)
	{
		_critAnnouncements.add(text);
		saveToDisk(true);
	}
	
	public void delCritAnnouncement(int line)
	{
		_critAnnouncements.remove(line);
		saveToDisk(true);
	}
	
	private void readFromDisk(String path, List<String> list)
	{
		final File file = new File(Config.DATAPACK_ROOT, path);
		try (FileReader fr = new FileReader(file);
			LineNumberReader lnr = new LineNumberReader(fr))
		{
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line, Config.EOL);
				if (st.hasMoreTokens())
				{
					list.add(st.nextToken());
				}
			}
		}
		catch (IOException e1)
		{
			_log.error("Error reading announcements: ", e1);
		}
	}
	
	private void saveToDisk(boolean isCritical)
	{
		String path;
		List<String> list;
		
		if (isCritical)
		{
			path = "data/critannouncements.txt";
			list = _critAnnouncements;
		}
		else
		{
			path = "data/announcements.txt";
			list = _announcements;
		}
		
		final File file = new File(path);
		try (FileWriter save = new FileWriter(file))
		{
			for (String announce : list)
			{
				save.write(announce);
				save.write(Config.EOL);
			}
		}
		catch (IOException e)
		{
			_log.error("Saving to the announcements file has failed: ", e);
		}
	}
	
	public void announceToAll(String text)
	{
		announceToAll(text, false);
	}
	
	public void announceToAll(String text, boolean isCritical)
	{
		Broadcast.announceToOnlinePlayers(text, isCritical);
	}
	
	public void announceToAll(SystemMessage sm)
	{
		Broadcast.toAllOnlinePlayers(sm);
	}
	
	public void announceToInstance(SystemMessage sm, int instanceId)
	{
		Broadcast.toPlayersInInstance(sm, instanceId);
	}
	
	/**
	 * Method for handling announcements from admin
	 * @param command
	 * @param lengthToTrim
	 * @param isCritical
	 */
	public void handleAnnounce(String command, int lengthToTrim, boolean isCritical)
	{
		try
		{
			// Announce string to everyone on server
			String text = command.substring(lengthToTrim);
			SingletonHolder._instance.announceToAll(text, isCritical);
		}
		
		// No body cares!
		catch (StringIndexOutOfBoundsException e)
		{
			// empty message.. ignore
		}
	}
	
	private static class SingletonHolder
	{
		protected static final Announcements _instance = new Announcements();
	}
}
