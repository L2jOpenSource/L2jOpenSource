package handlers.voicedcommandhandlers;

import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author -=DoctorNo=-
 */
public class AioItemVCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"aioitem"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith("aioitem"))
		{
			sendHtml(activeChar);
		}
		
		return true;
	}
	
	private void sendHtml(L2PcInstance activeChar)
	{
		String htmFile = "data/html/sunrise/AioItemNpcs/main.htm";
		
		NpcHtmlMessage msg = new NpcHtmlMessage();
		msg.setFile(activeChar, activeChar.getHtmlPrefix(), htmFile);
		activeChar.sendPacket(msg);
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}