package l2r.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import gr.reunion.achievementEngine.AchievementsHandler;
import gr.reunion.achievementEngine.AchievementsManager;
import gr.reunion.achievementEngine.base.Achievement;
import gr.reunion.achievementEngine.base.Condition;
import gr.reunion.main.Conditions;

public class L2AchievementManagerInstance extends L2Npc
{
	public L2AchievementManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		// No null pointers
		if ((player == null))
		{
			return;
		}
		
		// Restrictions Section
		if (!Conditions.checkPlayerBasicConditions(player))
		{
			return;
		}
		
		if (command.startsWith("showMyAchievements"))
		{
			AchievementsHandler.getAchievemntData(player);
			showMyAchievements(player);
		}
		else if (command.startsWith("achievementInfo"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int id = Integer.parseInt(st.nextToken());
			
			showAchievementInfo(id, player);
		}
		else if (command.startsWith("getReward"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int id = Integer.parseInt(st.nextToken());
			
			if (!AchievementsManager.checkConditions(id, player))
			{
				return;
			}
			
			AchievementsManager.getInstance().rewardForAchievement(id, player);
			AchievementsHandler.saveAchievementData(player, id);
			showMyAchievements(player);
		}
		else if (command.startsWith("showMyStats"))
		{
			showMyStatsWindow(player);
		}
		else if (command.startsWith("showHelpWindow"))
		{
			showHelpWindow(player);
		}
		else if (command.startsWith("showMainWindow"))
		{
			showChatWindow(player, 0);
		}
	}
	
	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Hello <font color=\"LEVEL\">" + player.getName() + "</font><br>Are you looking for challenge?");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		tb.append("<button value=\"My Achievements\" action=\"bypass -h npc_%objectId%_showMyAchievements\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append("<button value=\"Statistics\" action=\"bypass -h npc_%objectId%_showMyStats\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append("<button value=\"Help\" action=\"bypass -h npc_%objectId%_showHelpWindow\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showMyAchievements(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Achievements Manager</title><body><br>");
		
		tb.append("<center><font color=\"LEVEL\">My achievements</font>:</center><br>");
		
		if (AchievementsManager.getInstance().getAchievementList().isEmpty())
		{
			tb.append("There are no Achievements created yet!");
		}
		else
		{
			int i = 0;
			
			tb.append("<table width=280 border=0 bgcolor=\"33FF33\">");
			tb.append("<tr><td width=115 align=\"left\">Name:</td><td width=50 align=\"center\">Info:</td><td width=115 align=\"center\">Status:</td></tr></table>");
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"280\" height=\"1\"><br>");
			
			for (Achievement a : AchievementsManager.getInstance().getAchievementList().values())
			{
				tb.append(getTableColor(i));
				tb.append("<tr><td width=115 align=\"left\">" + a.getName() + "</td><td width=50 align=\"center\"><a action=\"bypass -h npc_%objectId%_achievementInfo " + a.getId() + "\">info</a></td><td width=115 align=\"center\">" + getStatusString(a.getId(), player) + "</td></tr></table>");
				i++;
			}
			
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"280\" height=\"1s\"><br>");
			tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showMainWindow\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		}
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showAchievementInfo(int achievementID, L2PcInstance player)
	{
		Achievement a = AchievementsManager.getInstance().getAchievementList().get(achievementID);
		
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Achievements Manager</title><body><br>");
		
		tb.append("<center><table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"center\">" + a.getName() + "</td></tr></table><br>");
		tb.append("Status: " + getStatusString(achievementID, player));
		
		if (a.meetAchievementRequirements(player) && !player.getCompletedAchievements().contains(achievementID))
		{
			tb.append("<button value=\"Receive Reward!\" action=\"bypass -h npc_%objectId%_getReward " + a.getId() + "\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		}
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"center\">Description</td></tr></table><br>");
		tb.append(a.getDescription());
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		
		tb.append("<table width=280 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=120 align=\"left\">Condition To Meet:</td><td width=55 align=\"center\">Value:</td><td width=95 align=\"center\">Status:</td></tr></table>");
		tb.append(getConditionsStatus(achievementID, player));
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showMyAchievements\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showMyStatsWindow(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Check your <font color=\"LEVEL\">Achievements </font>statistics:");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		AchievementsHandler.getAchievemntData(player);
		int completedCount = player.getCompletedAchievements().size();
		
		tb.append("You have completed: " + completedCount + "/<font color=\"LEVEL\">" + AchievementsManager.getInstance().getAchievementList().size() + "</font>");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showMainWindow\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showHelpWindow(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Achievements  <font color=\"LEVEL\">Help </font>page:");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		tb.append("You can check status of your achievements, receive reward if every condition of achievement is meet, if not you can check which condition is still not meet, by using info button");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<font color=\"FF0000\">Not Completed</font> - you did not meet the achivement requirements.<br>");
		tb.append("<font color=\"LEVEL\">Get Reward</font> - you may receive reward, click info.<br>");
		tb.append("<font color=\"5EA82E\">Completed</font> - achievement completed, reward received.<br>");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showMainWindow\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private String getStatusString(int achievementID, L2PcInstance player)
	{
		if (player.getCompletedAchievements().contains(achievementID))
		{
			return "<font color=\"5EA82E\">Completed</font>";
		}
		if (AchievementsManager.getInstance().getAchievementList().get(achievementID).meetAchievementRequirements(player))
		{
			return "<font color=\"LEVEL\">Get Reward</font>";
		}
		return "<font color=\"FF0000\">Not Completed</font>";
	}
	
	private String getTableColor(int i)
	{
		if ((i % 2) == 0)
		{
			return "<table width=280 border=0 bgcolor=\"444444\">";
		}
		return "<table width=280 border=0>";
	}
	
	private String getConditionsStatus(int achievementID, L2PcInstance player)
	{
		int i = 0;
		String s = "</center>";
		Achievement a = AchievementsManager.getInstance().getAchievementList().get(achievementID);
		String completed = "<font color=\"5EA82E\">Completed</font></td></tr></table>";
		String notcompleted = "<font color=\"FF0000\">Not Completed</font></td></tr></table>";
		
		for (Condition c : a.getConditions())
		{
			s += getTableColor(i);
			s += "<tr><td width=120 align=\"left\">" + c.getType().getText() + "</td><td width=55 align=\"center\">" + c.getValue() + "</td><td width=95 align=\"center\">";
			i++;
			
			if (c.meetConditionRequirements(player))
			{
				s += completed;
			}
			else
			{
				s += notcompleted;
			}
		}
		return s;
	}
}