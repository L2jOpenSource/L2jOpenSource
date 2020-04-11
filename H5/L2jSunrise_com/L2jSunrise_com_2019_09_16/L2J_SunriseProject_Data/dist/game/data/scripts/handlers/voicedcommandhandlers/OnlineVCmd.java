package handlers.voicedcommandhandlers;

import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author -=DoctorNo=-
 */
public class OnlineVCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"online"
	};
	
	private int onlineplayers = 0;
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith("online"))
		{
			showPlayers(activeChar, target);
		}
		return true;
	}
	
	public void showPlayers(L2PcInstance player, String target)
	{
		player.sendMessage("======<Online Players>======");
		onlineplayers = L2World.getInstance().getAllPlayersCount();
		player.sendMessage("Number of player(s): " + String.valueOf(onlineplayers));
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}