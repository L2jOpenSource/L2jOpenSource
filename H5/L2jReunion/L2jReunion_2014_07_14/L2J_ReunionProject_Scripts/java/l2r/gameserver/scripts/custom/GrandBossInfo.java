package l2r.gameserver.scripts.custom;

import javolution.text.TextBuilder;
import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.instancemanager.GrandBossManager;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author -=DoctorNo=-
 */
public class GrandBossInfo extends Quest
{
	private static final int NpcId = 543; // npc id here
	private static String qn = "GrandBossInfo";
	private static final int[] BOSSES =
	{
		29001,
		29006,
		29014,
		29019,
		29020,
		29022,
		29028
	};
	
	public GrandBossInfo()
	{
		super(-1, "GrandBossInfo", "custom");
		
		addFirstTalkId(NpcId);
		addTalkId(NpcId);
		addStartNpc(NpcId);
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
			showRbInfo(player);
		}
		return "";
	}
	
	private final void showRbInfo(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Chat</title><body>");
		tb.append("<br><br>");
		tb.append("<font color=00FFFF>Grand Boss Info:</font>");
		tb.append("<center>");
		tb.append("<img src=L2UI.SquareWhite width=280 height=1>");
		tb.append("<br><br>");
		tb.append("<table width = 280>");
		for (int boss : BOSSES)
		{
			String name = NpcTable.getInstance().getTemplate(boss).getName();
			long delay = GrandBossManager.getInstance().getStatsSet(boss).getLong("respawn_time");
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
		tb.append("</table>");
		tb.append("<br><br>");
		tb.append("<img src=L2UI.SquareWhite width=280 height=1>");
		tb.append("<br><br><br><br>");
		tb.append("<font color=\"3293F3\">L2JReunion</font><br>");
		tb.append("<img src=L2UI.SquareWhite width=280 height=1>");
		tb.append("</center>");
		tb.append("</body></html>");
		html.setHtml(tb.toString());
		player.sendPacket(html);
	}
	
	public static void main(final String[] args)
	{
		new GrandBossInfo();
	}
}