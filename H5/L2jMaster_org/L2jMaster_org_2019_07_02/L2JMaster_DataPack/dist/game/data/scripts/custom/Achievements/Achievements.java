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

package custom.Achievements;

import java.util.StringTokenizer;

import com.l2jserver.gameserver.instancemanager.achievements.AchievementsManager;
import com.l2jserver.gameserver.instancemanager.achievements.base.Achievement;
import com.l2jserver.gameserver.instancemanager.achievements.base.Condition;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

import ai.npc.AbstractNpcAI;

public class Achievements extends AbstractNpcAI
{
	// NPCs
	private static final int NPC = 1012;
	private boolean first = true;
	
	private Achievements()
	{
		super(Achievements.class.getSimpleName(), "custom");
		addStartNpc(NPC);
		addTalkId(NPC);
		addFirstTalkId(NPC);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		
		if (event.contains("achievementsInfo"))
		{
			StringTokenizer st = new StringTokenizer(event, " ");
			st.nextToken();
			int id = Integer.parseInt(st.nextToken());
			showAchievementInfo(id, player, npc);
			return htmltext;
		}
		else if (event.contains("achievementsReward"))
		{
			StringTokenizer st = new StringTokenizer(event, " ");
			st.nextToken();
			int id = Integer.parseInt(st.nextToken());
			if (id == 10)
			{
				player.destroyItemByItemId("", 8787, 200, player, true);
				AchievementsManager.getInstance().rewardForAchievement(id, player);
			}
			else if ((id == 4) || (id == 19))
			{
				L2ItemInstance weapon = player.getInventory().getPaperdollItem(5);
				if (weapon != null)
				{
					int objid = weapon.getObjectId();
					if (AchievementsManager.getInstance().getAchievementList().get(Integer.valueOf(id)).meetAchievementRequirements(player))
					{
						if (!AchievementsManager.getInstance().isBinded(objid, id))
						{
							AchievementsManager.getInstance().getBinded().add(objid + "@" + id);
							player.saveAchievementData(id, objid);
							AchievementsManager.getInstance().rewardForAchievement(id, player);
						}
						else
						{
							player.sendMessage("This item was already used to earn this achievement.");
						}
					}
					else
					{
						player.sendMessage("Seems you don't meet the achievements requirements now.");
					}
				}
				else
				{
					player.sendMessage("You must equip your weapon in order to get rewarded.");
				}
			}
			else if ((id == 6) || (id == 18))
			{
				int clid = player.getClan().getId();
				
				if (!AchievementsManager.getInstance().isBinded(clid, id))
				{
					AchievementsManager.getInstance().getBinded().add(clid + "@" + id);
					player.saveAchievementData(id, clid);
					AchievementsManager.getInstance().rewardForAchievement(id, player);
				}
				else
				{
					player.sendMessage("Current clan was already rewarded for this achievement.");
				}
			}
			else
			{
				player.saveAchievementData(id, 0);
				AchievementsManager.getInstance().rewardForAchievement(id, player);
			}
			
			showMyAchievements(player, npc);
			return htmltext;
		}
		
		switch (event)
		{
			case "achievementsList":
			{
				player.getAchievemntData();
				showMyAchievements(player, npc);
				break;
			}
			case "achievementsRank":
			{
				showTopListWindow(player, npc);
				break;
			}
			case "achievementsMain":
			{
				showChatWindow(player, npc, 0);
				break;
			}
			case "achievementsStats":
			{
				showMyStatsWindow(player, npc);
				break;
			}
			case "achievementsHelp":
			{
				showHelpWindow(player, npc);
				break;
			}
		}
		
		return htmltext;
	}
	
	public void showChatWindow(L2PcInstance player, L2Npc npc, int val)
	{
		if (this.first)
		{
			AchievementsManager.getInstance().loadUsed();
			this.first = false;
		}
		
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Hello <font color=\"LEVEL\">" + player.getName() + "</font><br>Are you looking for challenge?");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		tb.append("<button value=\"My Achievems\" action=\"bypass -h npc_%objectId%_Quest Achievements achievementsList\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=95 height=21>");
		tb.append("<button value=\"Statistics\" action=\"bypass -h npc_%objectId%_Quest Achievements achievementsStats\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=95 height=21>");
		tb.append("<button value=\"Help\" action=\"bypass -h npc_%objectId%_Quest Achievements achievementsHelp\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=95 height=21>");
		
		final NpcHtmlMessage msg = new NpcHtmlMessage(npc.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(npc.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showMyAchievements(L2PcInstance player, L2Npc npc)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><br>");
		tb.append("<center><font color=\"LEVEL\">My achievements</font>:</center><br>");
		
		if (AchievementsManager.getInstance().getAchievementList().isEmpty())
		{
			tb.append("There are no Achievements created yet!");
		}
		else
		{
			int i = 0;
			
			tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
			tb.append("<tr><td width=270 align=\"left\">Name:</td><td width=60 align=\"right\">Info:</td><td width=200 align=\"center\">Status:</td></tr></table>");
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
			
			for (Achievement a : AchievementsManager.getInstance().getAchievementList().values())
			{
				tb.append(getTableColor(i));
				tb.append("<tr><td width=270 align=\"left\">" + a.getName() + "</td><td width=50 align=\"right\"><a action=\"bypass -h npc_%objectId%_Quest Achievements achievementsInfo " + a.getID() + "\">info</a></td><td width=200 align=\"center\">" + getStatusString(a.getID(), player)
					+ "</td></tr></table>");
				i++;
			}
			
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
			tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_Quest Achievements achievementsMain\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=95 height=21></center>");
		}
		
		NpcHtmlMessage msg = new NpcHtmlMessage(npc.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(npc.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showAchievementInfo(int achievementID, L2PcInstance player, L2Npc npc)
	{
		// System.out.println("INFO: " + achievementID);
		
		Achievement a = AchievementsManager.getInstance().getAchievementList().get(Integer.valueOf(achievementID));
		
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"center\">" + a.getName() + "</td></tr></table><br>");
		tb.append("<center>Status: " + getStatusString(achievementID, player));
		
		if ((a.meetAchievementRequirements(player)) && (!player.getCompletedAchievements().contains(Integer.valueOf(achievementID))))
		{
			tb.append("<button value=\"Receive Reward!\" action=\"bypass -h npc_%objectId%_Quest Achievements achievementsReward " + a.getID() + "\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=95 height=21>");
		}
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"center\">Description</td></tr></table><br>");
		tb.append(a.getDescription());
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"left\">Condition:</td><td width=100 align=\"left\">Value:</td><td width=200 align=\"center\">Status:</td></tr></table>");
		tb.append(getConditionsStatus(achievementID, player));
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_Quest Achievements achievementsList\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=95 height=21></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(npc.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(npc.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showMyStatsWindow(L2PcInstance player, L2Npc npc)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Check your <font color=\"LEVEL\">Achievements </font>statistics:");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		player.getAchievemntData();
		int completedCount = player.getCompletedAchievements().size();
		
		tb.append("You have completed: " + completedCount + "/<font color=\"LEVEL\">" + AchievementsManager.getInstance().getAchievementList().size() + "</font>");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_Quest Achievements achievementsMain\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=95 height=21></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(npc.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(npc.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showTopListWindow(L2PcInstance player, L2Npc npc)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Check your <font color=\"LEVEL\">Achievements </font>Top List:");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		tb.append("Not implemented yet!");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_Quest Achievements achievementsMain\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=95 height=21></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(npc.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(npc.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showHelpWindow(L2PcInstance player, L2Npc npc)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Achievements <font color=\"LEVEL\">Help </font>page:");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		tb.append("<table><tr><td>You can check the status of your achievements,</td></tr><tr><td>receive reward if every condition of the achievement is meet,</td></tr><tr><td>if not you can check which condition is still not met, by using info button</td></tr></table>");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<table><tr><td><font color=\"FF0000\">Not Completed</font> - you did not meet the achivement requirements.</td></tr>");
		tb.append("<tr><td><font color=\"LEVEL\">Get Reward</font> - you may receive reward, click info.</td></tr>");
		tb.append("<tr><td><font color=\"5EA82E\">Completed</font> - achievement completed, reward received.</td></tr></table>");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_Quest Achievements achievementsMain\" width=100 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(npc.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(npc.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private String getStatusString(int achievementID, L2PcInstance player)
	{
		if (player.getCompletedAchievements().contains(Integer.valueOf(achievementID)))
		{
			return "<font color=\"5EA82E\">Completed</font>";
		}
		
		if (AchievementsManager.getInstance().getAchievementList().get(Integer.valueOf(achievementID)).meetAchievementRequirements(player))
		{
			return "<font color=\"LEVEL\">Get Reward</font>";
		}
		
		return "<font color=\"FF0000\">Not Completed</font>";
	}
	
	private String getTableColor(int i)
	{
		if ((i % 2) == 0)
		{
			return "<table width=270 border=0 bgcolor=\"444444\">";
		}
		
		return "<table width=270 border=0>";
	}
	
	private String getConditionsStatus(int achievementID, L2PcInstance player)
	{
		int i = 0;
		String s = "</center>";
		Achievement a = AchievementsManager.getInstance().getAchievementList().get(Integer.valueOf(achievementID));
		String completed = "<font color=\"5EA82E\">Completed</font></td></tr></table>";
		String notcompleted = "<font color=\"FF0000\">Not Completed</font></td></tr></table>";
		
		for (Condition c : a.getConditions())
		{
			s = s + getTableColor(i);
			s = s + "<tr><td width=270 align=\"left\">" + c.getName() + "</td><td width=100 align=\"left\">" + c.getValue() + "</td><td width=200 align=\"center\">";
			i++;
			
			if (c.meetConditionRequirements(player))
			{
				s = s + completed;
			}
			else
			{
				s = s + notcompleted;
			}
		}
		
		return s;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if ((npc == null) || (player == null))
		{
			return null;
		}
		
		if (npc.getId() == NPC)
		{
			showChatWindow(player, npc, 0);
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Achievements();
	}
}