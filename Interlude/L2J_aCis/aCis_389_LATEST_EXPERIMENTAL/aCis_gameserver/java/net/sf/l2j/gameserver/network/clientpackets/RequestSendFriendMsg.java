package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.L2FriendSay;

public final class RequestSendFriendMsg extends L2GameClientPacket
{
	private static final Logger CHAT_LOG = Logger.getLogger("chat");
	
	private String _message;
	private String _targetName;
	
	@Override
	protected void readImpl()
	{
		_message = readS();
		_targetName = readS();
	}
	
	@Override
	protected void runImpl()
	{
		if (_message == null || _message.isEmpty() || _message.length() > 300)
			return;
		
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		final Player targetPlayer = World.getInstance().getPlayer(_targetName);
		if (targetPlayer == null || !targetPlayer.getFriendList().contains(player.getObjectId()))
		{
			player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			return;
		}
		
		if (Config.LOG_CHAT)
		{
			LogRecord record = new LogRecord(Level.INFO, _message);
			record.setLoggerName("chat");
			record.setParameters(new Object[]
			{
				"PRIV_MSG",
				"[" + player.getName() + " to " + _targetName + "]"
			});
			
			CHAT_LOG.log(record);
		}
		
		targetPlayer.sendPacket(new L2FriendSay(player.getName(), _targetName, _message));
	}
}