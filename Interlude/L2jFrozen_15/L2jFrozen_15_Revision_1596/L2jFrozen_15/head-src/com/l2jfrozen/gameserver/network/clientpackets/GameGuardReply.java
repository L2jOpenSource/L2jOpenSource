package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.crypt.nProtect;

/**
 * @author zabbix Lets drink to code! Unknown Packet: ca 0000: 45 00 01 00 1e 37 a2 f5 00 00 00 00 00 00 00 00 E....7..........
 */
public class GameGuardReply extends L2GameClientPacket
{
	private final int[] reply = new int[4];
	private static final Logger LOGGER = Logger.getLogger(GameGuardReply.class);
	
	@Override
	protected void readImpl()
	{
		reply[0] = readD();
		reply[1] = readD();
		reply[2] = readD();
		reply[3] = readD();
	}
	
	@Override
	protected void runImpl()
	{
		// TODO: clean nProtect System
		if (!nProtect.getInstance().checkGameGuardRepy(getClient(), reply))
		{
			return;
		}
		
		// L2jFrozen cannot be reached with GameGuard: L2Net notification --> Close Client connection
		if (Config.GAMEGUARD_L2NET_CHECK)
		{
			getClient().closeNow();
			LOGGER.warn("Player with account name " + getClient().accountName + " kicked to use L2Net ");
			return;
		}
		
		getClient().setGameGuardOk(true);
	}
	
	@Override
	public String getType()
	{
		return "[C] CA GameGuardReply";
	}
}