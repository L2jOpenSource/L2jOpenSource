/*
 * Copyright (C) 2004-2019 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 package custom.BugReport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import com.l2jserver.gameserver.cache.HtmCache;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.network.L2GameClient;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author -=DoctorNo=- Version 2.1
 */
public class BugReport extends Quest
{
	private static final int NpcId = 36630; // npc id here
	private static String qn = "BugReport";
	private static String _type;
	
	public BugReport(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(NpcId);
		addTalkId(NpcId);
		addStartNpc(NpcId);
	}
	
	/**
	 * Method to manage all player bypasses
	 * @param event
	 * @param npc
	 * @param player
	 * @return
	 */
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.startsWith("report"))
		{
			sendReport(event, npc, player, event);
		}
		return "";
	}
	
	private void sendReport(String event, L2Npc npc, L2PcInstance player, String command)
	{
		String message = "";
		String[] type = command.split(" "); // General, Fatal, Misuse, Balance, Other
		L2GameClient info = player.getClient().getConnection().getClient();
		
		if (type[1].equals("General"))
		{
			_type = "General";
		}
		if (type[1].equals("Fatal"))
		{
			_type = "Fatal";
		}
		if (type[1].equals("Misuse"))
		{
			_type = "Misuse";
		}
		if (type[1].equals("Balance"))
		{
			_type = "Balance";
		}
		if (type[1].equals("Other"))
		{
			_type = "Other";
		}
		
		try
		{
			for (String s : event.split(" "))
			{
				message = message + " " + s;
			}
			message = message.replaceFirst("report", "");
			message = message.replaceFirst("General", "");
			message = message.replaceFirst("Fatal", "");
			message = message.replaceFirst("Misuse", "");
			message = message.replaceFirst("Balance", "");
			message = message.replaceFirst("Other", "");
			message = message.replaceFirst("  ", "");
			
			String fname = "data/BugReports/" + player.getName() + ".txt";
			File file = new File(fname);
			boolean exist = file.createNewFile();
			if (!exist)
			{
				player.sendMessage("You have already sent a bug report, GMs must check it first.");
				return;
			}
			FileWriter fstream = new FileWriter(fname);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("Character Info: " + info + "\r\nBug Type: " + _type + "\r\nMessage: " + message);
			player.sendMessage("Report sent. GMs will check it soon. Thanks...");
			
			for (L2PcInstance allgms : L2World.getInstance().getAllGMs())
			{
				allgms.sendPacket(new CreatureSay(0, Say2.SHOUT, "Bug Report Manager", player.getName() + " sent a bug report."));
			}
			
			System.out.println("Character: " + player.getName() + " sent a bug report.");
			out.close();
		}
		catch (Exception e)
		{
			player.sendMessage("Something went wrong try again.");
		}
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final int npcId = npc.getId();
		if (player.getQuestState(qn) == null)
		{
			newQuestState(player);
		}
		
		if (npcId == NpcId)
		{
			String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/scripts/custom/BugReport/1.htm");
			html = html.replaceAll("%player%", player.getName());
			
			NpcHtmlMessage npcHtml = new NpcHtmlMessage(0);
			npcHtml.setHtml(html);
			player.sendPacket(npcHtml);
		}
		return "";
	}
	
	public static void main(final String[] args)
	{
		new BugReport(-1, "BugReport", "custom");
		_log.info("------------------------------------------=[ Master Scripts ]");
		_log.info("BugReport Manager: Enabled.");
	}
}