package com.l2jfrozen.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.FriendRecvMsg;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * Recieve Private (Friend) Message - 0xCC Format: c SS S: Message S: Receiving Player
 * @author L2JFrozen
 */
public final class RequestSendFriendMsg extends L2GameClientPacket
{
	private static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("chat");
	
	private String message;
	private String reciever;
	
	@Override
	protected void readImpl()
	{
		message = readS();
		reciever = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final L2PcInstance targetPlayer = L2World.getInstance().getPlayer(reciever);
		if (targetPlayer == null || !targetPlayer.getFriendList().contains(activeChar.getName()))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME));
			return;
		}
		
		if (Config.LOG_CHAT)
		{
			final LogRecord record = new LogRecord(Level.INFO, message);
			record.setLoggerName("chat");
			record.setParameters(new Object[]
			{
				"PRIV_MSG",
				"[" + activeChar.getName() + " to " + reciever + "]"
			});
			
			LOGGER.log(record);
		}
		
		final FriendRecvMsg frm = new FriendRecvMsg(activeChar.getName(), reciever, message);
		targetPlayer.sendPacket(frm);
	}
	
	@Override
	public String getType()
	{
		return "[C] CC RequestSendMsg";
	}
}
