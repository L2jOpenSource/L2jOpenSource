package handlers.voicedcommandhandlers;

import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author -=DoctorNo=-
 */
public class ItemBufferVCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"buffer"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (!activeChar.isPremium())
		{
			activeChar.sendMessage("Voiced buffer is only available for premium accounts.");
			return false;
		}
		
		if (command.startsWith("buffer"))
		{
			sendHtml(activeChar);
		}
		
		return true;
	}
	
	private void sendHtml(L2PcInstance activeChar)
	{
		String htmFile = "data/html/sunrise/ItemBuffer/main.htm";
		
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