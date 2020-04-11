package l2r.gameserver.scripts.handlers.voicedcommandhandlers;

import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import gr.reunion.voteEngine.old.dynamicHtmls.GenerateHtmls;

public class VotePanelVCmd implements IVoicedCommandHandler
{
	private static String[] VOICED_COMMANDS =
	{
		"votepanel"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equalsIgnoreCase("votepanel"))
		{
			GenerateHtmls.getInstance().VotePanelHtml(activeChar);
		}
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}