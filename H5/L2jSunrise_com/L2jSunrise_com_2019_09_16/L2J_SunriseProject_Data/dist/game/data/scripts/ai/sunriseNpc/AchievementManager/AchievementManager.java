package ai.sunriseNpc.AchievementManager;

import java.util.StringTokenizer;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

import gr.sr.achievementEngine.AchievementsHandler;
import gr.sr.achievementEngine.AchievementsManager;
import gr.sr.achievementEngine.base.Achievement;
import gr.sr.achievementEngine.base.Condition;
import gr.sr.configsEngine.configs.impl.CustomNpcsConfigs;

import ai.npc.AbstractNpcAI;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class AchievementManager extends AbstractNpcAI
{
	private final static int NPC = CustomNpcsConfigs.ACHIEVEMENT_NPC_ID;
	
	public AchievementManager()
	{
		super(AchievementManager.class.getSimpleName(), "ai/sunriseNpc");
		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (!CustomNpcsConfigs.ENABLE_ACHIEVEMENT_MANAGER)
		{
			player.sendMessage("Achievement manager npc is disabled by admin.");
			return "main.htm";
		}
		
		if (player.getLevel() < CustomNpcsConfigs.ACHIEVEMENT_REQUIRED_LEVEL)
		{
			player.sendMessage("You need to be " + CustomNpcsConfigs.ACHIEVEMENT_REQUIRED_LEVEL + " level or higher to use my services.");
			return "main.htm";
		}
		
		if (event.startsWith("showMyAchievements"))
		{
			AchievementsHandler.getAchievemntData(player);
			showMyAchievements(player, npc);
		}
		else if (event.startsWith("achievementInfo"))
		{
			StringTokenizer st = new StringTokenizer(event, " ");
			st.nextToken();
			int id = Integer.parseInt(st.nextToken());
			showAchievementInfo(id, player, npc);
		}
		else if (event.startsWith("getReward"))
		{
			StringTokenizer st = new StringTokenizer(event, " ");
			st.nextToken();
			int id = Integer.parseInt(st.nextToken());
			
			if (!AchievementsManager.checkConditions(id, player))
			{
				return "";
			}
			
			AchievementsManager.getInstance().rewardForAchievement(id, player);
			AchievementsHandler.saveAchievementData(player, id);
			showMyAchievements(player, npc);
		}
		else if (event.startsWith("showMyStats"))
		{
			showMyStats(player);
		}
		else if (event.startsWith("showMainWindow"))
		{
			return "main.htm";
		}
		
		return "";
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "main.htm";
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
			
			tb.append("<table width=280 border=0 bgcolor=\"33FF33\">");
			tb.append("<tr><td width=115 align=\"left\">Name:</td><td width=50 align=\"center\">Info:</td><td width=115 align=\"center\">Status:</td></tr></table>");
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"280\" height=\"1\"><br>");
			
			for (Achievement a : AchievementsManager.getInstance().getAchievementList().values())
			{
				tb.append(getTableColor(i));
				tb.append("<tr><td width=115 align=\"left\">" + a.getName() + "</td><td width=50 align=\"center\"><a action=\"bypass -h Quest AchievementManager achievementInfo " + a.getId() + "\">info</a></td><td width=115 align=\"center\">" + getStatusString(a.getId(), player) + "</td></tr></table>");
				i++;
			}
			
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"280\" height=\"1s\"><br>");
			tb.append("<center><button value=\"Back\" action=\"bypass -h Quest AchievementManager showMainWindow\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		}
		
		NpcHtmlMessage msg = new NpcHtmlMessage(npc.getObjectId());
		msg.setHtml(tb.toString());
		player.sendPacket(msg);
	}
	
	private void showAchievementInfo(int achievementID, L2PcInstance player, L2Npc npc)
	{
		Achievement a = AchievementsManager.getInstance().getAchievementList().get(achievementID);
		
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><br>");
		
		tb.append("<center><table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"center\">" + a.getName() + "</td></tr></table><br>");
		tb.append("Status: " + getStatusString(achievementID, player));
		
		if (a.meetAchievementRequirements(player) && !player.getCompletedAchievements().contains(achievementID))
		{
			tb.append("<button value=\"Receive Reward!\" action=\"bypass -h Quest AchievementManager getReward " + a.getId() + "\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
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
		tb.append("<center><button value=\"Back\" action=\"bypass -h Quest AchievementManager showMyAchievements\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(npc.getObjectId());
		msg.setHtml(tb.toString());
		player.sendPacket(msg);
	}
	
	private void showMyStats(L2PcInstance player)
	{
		AchievementsHandler.getAchievemntData(player);
		int completedCount = player.getCompletedAchievements().size();
		player.sendMessage("You have completed " + completedCount + " from " + AchievementsManager.getInstance().getAchievementList().size() + " achievements");
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