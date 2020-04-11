package com.l2jfrozen.gameserver.model.actor.instance;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.managers.RaidBossSpawnManager;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.templates.StatsSet;

public class L2RaidBossInfoInstance extends L2NpcInstance
{
	private final static Logger LOGGER = Logger.getLogger(L2RaidBossInfoInstance.class);
	
	public L2RaidBossInfoInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/raidbossinfo/" + pom + ".htm";
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("raidInfo"))
		{
			StringBuilder tb = new StringBuilder();
			tb.append("<html><title>Raid Boss Info</title><body><br><center>");
			tb.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><br>");
			
			for (int boss : Config.RAID_INFO_IDS_LIST)
			{
				String name = "";
				L2NpcTemplate template = NpcTable.getInstance().getTemplate(boss);
				
				if (template != null)
				{
					name = template.getName();
				}
				else
				{
					LOGGER.warn("[RaidInfoHandler][sendInfo] Raid Boss with ID " + boss + " is not defined into NpcTable");
					continue;
				}
				
				StatsSet actual_boss_stat = null;
				GrandBossManager.getInstance().getStatsSet(boss);
				long delay = 0;
				
				if (NpcTable.getInstance().getTemplate(boss).type.equals("L2RaidBoss"))
				{
					actual_boss_stat = RaidBossSpawnManager.getInstance().getStatsSet(boss);
					if (actual_boss_stat != null)
					{
						delay = actual_boss_stat.getLong("respawnTime");
					}
				}
				else if (NpcTable.getInstance().getTemplate(boss).type.equals("L2GrandBoss"))
				{
					actual_boss_stat = GrandBossManager.getInstance().getStatsSet(boss);
					if (actual_boss_stat != null)
					{
						delay = actual_boss_stat.getLong("respawn_time");
					}
				}
				else
				{
					continue;
				}
				
				if (delay <= System.currentTimeMillis())
				{
					tb.append("<font color=\"00C3FF\">" + name + "</color>: " + "<font color=\"9CC300\">Alive</color>" + "<br1>");
				}
				else
				{
					int hours = (int) ((delay - System.currentTimeMillis()) / 1000 / 60 / 60);
					int mins = (int) (((delay - (hours * 60 * 60 * 1000)) - System.currentTimeMillis()) / 1000 / 60);
					int seconts = (int) (((delay - ((hours * 60 * 60 * 1000) + (mins * 60 * 1000))) - System.currentTimeMillis()) / 1000);
					tb.append("<font color=\"00C3FF\">" + name + "</color>" + "<font color=\"FFFFFF\">" + " " + "Respawn in :</color>" + " " + " <font color=\"32C332\">" + hours + " : " + mins + " : " + seconts + "</color><br1>");
				}
			}
			
			tb.append("<br><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>");
			tb.append("</center></body></html>");
			
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setHtml(tb.toString());
			player.sendPacket(html);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}
