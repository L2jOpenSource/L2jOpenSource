package ai.sunriseNpc.GrandBossManager;

import java.util.List;

import l2r.gameserver.data.sql.NpcTable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

import gr.sr.configsEngine.configs.impl.CustomNpcsConfigs;

import ai.npc.AbstractNpcAI;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class GrandBossManager extends AbstractNpcAI
{
	private static final int NpcId = CustomNpcsConfigs.GRANDBOSS_NPC_ID;
	private static final List<Integer> BOSSES = CustomNpcsConfigs.GRANDBOSS_LIST;
	
	public GrandBossManager()
	{
		super(GrandBossManager.class.getSimpleName(), "ai/sunriseNpc");
		addFirstTalkId(NpcId);
		addTalkId(NpcId);
		addStartNpc(NpcId);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (!CustomNpcsConfigs.ENABLE_GRANDBOSS_MANAGER)
		{
			player.sendMessage("GrandBoss manager npc is disabled by admin");
			return "";
		}
		
		showRbInfo(player);
		return "";
	}
	
	private final void showRbInfo(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage();
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Chat</title><body>");
		tb.append("<br><br>");
		tb.append("<font color=00FFFF>Grand Boss Info:</font>");
		tb.append("<center>");
		tb.append("<img src=L2UI.SquareWhite width=280 height=1>");
		tb.append("<br><br>");
		tb.append("<table width = 280>");
		for (int boss : BOSSES)
		{
			try
			{
				String name = NpcTable.getInstance().getTemplate(boss).getName();
				long delay = l2r.gameserver.instancemanager.GrandBossManager.getInstance().getStatsSet(boss).getLong("respawn_time");
				if (delay <= System.currentTimeMillis())
				{
					tb.append("<tr>");
					tb.append("<td><font color=\"00C3FF\">" + name + "</color>:</td> " + "<td><font color=\"00FF00\">Is Alive</color></td>" + "<br1>");
					tb.append("</tr>");
				}
				else
				{
					int hours = (int) ((delay - System.currentTimeMillis()) / 1000 / 60 / 60);
					int mins = (int) (((delay - (hours * 60 * 60 * 1000)) - System.currentTimeMillis()) / 1000 / 60);
					int seconts = (int) (((delay - ((hours * 60 * 60 * 1000) + (mins * 60 * 1000))) - System.currentTimeMillis()) / 1000);
					tb.append("<tr>");
					tb.append("<td><font color=\"00C3FF\">" + name + "</color></td>" + "<td><font color=\"00BFFF\">" + " " + "Respawn in :</color></td>" + " " + "<td><font color=\"00BFFF\">" + hours + " : " + mins + " : " + seconts + "</color></td>");
					tb.append("</tr>");
				}
			}
			catch (Exception e)
			{
				continue;
			}
		}
		tb.append("</table>");
		tb.append("<br><br>");
		tb.append("<img src=L2UI.SquareWhite width=280 height=1>");
		tb.append("<br><br><br><br>");
		tb.append("<font color=\"3293F3\">L2JSunrise</font><br>");
		tb.append("<img src=L2UI.SquareWhite width=280 height=1>");
		tb.append("</center>");
		tb.append("</body></html>");
		html.setHtml(tb.toString());
		player.sendPacket(html);
	}
	
	public static void main(final String[] args)
	{
		new GrandBossManager();
	}
}