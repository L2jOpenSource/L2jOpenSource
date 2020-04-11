package ai.sunriseNpc.ReportManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.StringTokenizer;

import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.L2GameClient;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.CreatureSay;

import gr.sr.configsEngine.configs.impl.CustomNpcsConfigs;

import ai.npc.AbstractNpcAI;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class ReportManager extends AbstractNpcAI
{
	private final static int NPC = CustomNpcsConfigs.REPORT_MANAGER_NPC_ID;
	
	public ReportManager()
	{
		super(ReportManager.class.getSimpleName(), "ai/sunriseNpc");
		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (!CustomNpcsConfigs.ENABLE_REPORT_MANAGER)
		{
			player.sendMessage("Report manager is disabled by admin.");
			return "main.htm";
		}
		
		if (player.getLevel() < CustomNpcsConfigs.REPORT_REQUIRED_LEVEL)
		{
			player.sendMessage("Your level is too low to use this function, you must be at least " + String.valueOf(CustomNpcsConfigs.REPORT_REQUIRED_LEVEL + 1) + " level.");
			return "main.htm";
		}
		
		if (event.startsWith("report"))
		{
			StringTokenizer st = new StringTokenizer(event);
			st.nextToken();
			
			String message = "";
			String _type = null; // General, Fatal, Misuse, Balance, Other
			L2GameClient info = player.getClient().getConnection().getClient();
			
			try
			{
				_type = st.nextToken();
				
				while (st.hasMoreTokens())
				{
					message = message + st.nextToken() + " ";
				}
				
				if (message.equals(""))
				{
					player.sendMessage("Message box cannot be empty.");
					return "main.htm";
				}
				
				String fname = CustomNpcsConfigs.REPORT_PATH + player.getName() + ".txt";
				File file = new File(fname);
				boolean exist = file.createNewFile();
				
				if (!exist)
				{
					player.sendMessage("You have already sent a bug report, GMs must check it first.");
					return "main.htm";
				}
				
				try (FileWriter fstream = new FileWriter(fname);
					BufferedWriter out = new BufferedWriter(fstream))
				{
					out.write("Character Info: " + info + "\r\nBug Type: " + _type + "\r\nMessage: " + message);
				}
				catch (Exception e)
				{
				
				}
				
				player.sendMessage("Report sent. GMs will check it soon. Thanks...");
				
				for (L2PcInstance allgms : L2World.getInstance().getAllGMs())
				{
					allgms.sendPacket(new CreatureSay(0, Say2.SHOUT, "Bug Report Manager", player.getName() + " sent a bug report."));
					allgms.sendPacket(new CreatureSay(0, Say2.SHOUT, "Report Type", _type + "."));
				}
				
				_log.info("Character: " + player.getName() + " sent a bug report.");
			}
			catch (Exception e)
			{
				player.sendMessage("Something went wrong try again.");
				return "main.htm";
			}
		}
		
		return "main.htm";
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "main.htm";
	}
}