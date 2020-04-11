package com.l2jfrozen.loginserver.network.gameserverpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.GameServerTable;
import com.l2jfrozen.gameserver.datatables.GameServerTable.GameServerInfo;
import com.l2jfrozen.loginserver.network.clientpackets.ClientBasePacket;

/**
 * @author -Wooden-
 */
public class ServerStatus extends ClientBasePacket
{
	protected static Logger LOGGER = Logger.getLogger(ServerStatus.class);
	
	public static final String[] STATUS_STRING =
	{
		"Auto",
		"Good",
		"Normal",
		"Full",
		"Down",
		"Gm Only"
	};
	
	public static final int SERVER_LIST_STATUS = 0x01;
	public static final int SERVER_LIST_CLOCK = 0x02;
	public static final int SERVER_LIST_SQUARE_BRACKET = 0x03;
	public static final int MAX_PLAYERS = 0x04;
	public static final int TEST_SERVER = 0x05;
	
	public static final int STATUS_AUTO = 0x00;
	public static final int STATUS_GOOD = 0x01;
	public static final int STATUS_NORMAL = 0x02;
	public static final int STATUS_FULL = 0x03;
	public static final int STATUS_DOWN = 0x04;
	public static final int STATUS_GM_ONLY = 0x05;
	
	public static final int ON = 0x01;
	public static final int OFF = 0x00;
	
	/**
	 * @param decrypt
	 * @param serverId
	 */
	public ServerStatus(final byte[] decrypt, final int serverId)
	{
		super(decrypt);
		
		GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(serverId);
		if (gsi != null)
		{
			final int size = readD();
			
			for (int i = 0; i < size; i++)
			{
				final int type = readD();
				final int value = readD();
				
				switch (type)
				{
					case SERVER_LIST_STATUS:
						gsi.setStatus(value);
						break;
					case SERVER_LIST_CLOCK:
						gsi.setShowingClock(value == ON);
						break;
					case SERVER_LIST_SQUARE_BRACKET:
						gsi.setShowingBrackets(value == ON);
						break;
					case TEST_SERVER:
						gsi.setTestServer(value == ON);
						break;
					case MAX_PLAYERS:
						gsi.setMaxPlayers(value);
						break;
				}
			}
		}
		
		gsi = null;
	}
}
