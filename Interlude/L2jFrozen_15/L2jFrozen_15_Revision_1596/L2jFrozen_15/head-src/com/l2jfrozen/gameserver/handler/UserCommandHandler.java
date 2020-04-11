package com.l2jfrozen.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.GameServer;
import com.l2jfrozen.gameserver.handler.usercommandhandlers.ChannelDelete;
import com.l2jfrozen.gameserver.handler.usercommandhandlers.ChannelLeave;
import com.l2jfrozen.gameserver.handler.usercommandhandlers.ChannelListUpdate;
import com.l2jfrozen.gameserver.handler.usercommandhandlers.ClanPenalty;
import com.l2jfrozen.gameserver.handler.usercommandhandlers.ClanWarsList;
import com.l2jfrozen.gameserver.handler.usercommandhandlers.DisMount;
import com.l2jfrozen.gameserver.handler.usercommandhandlers.Escape;
import com.l2jfrozen.gameserver.handler.usercommandhandlers.Loc;
import com.l2jfrozen.gameserver.handler.usercommandhandlers.Mount;
import com.l2jfrozen.gameserver.handler.usercommandhandlers.OfflineShop;
import com.l2jfrozen.gameserver.handler.usercommandhandlers.OlympiadStat;
import com.l2jfrozen.gameserver.handler.usercommandhandlers.PartyInfo;
import com.l2jfrozen.gameserver.handler.usercommandhandlers.SiegeStatus;
import com.l2jfrozen.gameserver.handler.usercommandhandlers.Time;

public class UserCommandHandler
{
	private static final Logger LOGGER = Logger.getLogger(GameServer.class);
	private static UserCommandHandler instance;
	
	private Map<Integer, IUserCommandHandler> dataTable = new HashMap<>();
	
	public static UserCommandHandler getInstance()
	{
		if (instance == null)
		{
			instance = new UserCommandHandler();
		}
		
		return instance;
	}
	
	private UserCommandHandler()
	{
		registerUserCommandHandler(new Time());
		registerUserCommandHandler(new OlympiadStat());
		registerUserCommandHandler(new ChannelLeave());
		registerUserCommandHandler(new ChannelDelete());
		registerUserCommandHandler(new ChannelListUpdate());
		registerUserCommandHandler(new ClanPenalty());
		registerUserCommandHandler(new ClanWarsList());
		registerUserCommandHandler(new DisMount());
		registerUserCommandHandler(new Escape());
		registerUserCommandHandler(new Loc());
		registerUserCommandHandler(new Mount());
		registerUserCommandHandler(new PartyInfo());
		registerUserCommandHandler(new SiegeStatus());
		if (Config.OFFLINE_TRADE_ENABLE && Config.OFFLINE_COMMAND1)
		{
			registerUserCommandHandler(new OfflineShop());
		}
		LOGGER.info("UserCommandHandler: Loaded " + dataTable.size() + " handlers.");
	}
	
	public void registerUserCommandHandler(final IUserCommandHandler handler)
	{
		int[] ids = handler.getUserCommandList();
		
		for (final int id : ids)
		{
			if (Config.DEBUG)
			{
				LOGGER.debug("Adding handler for user command " + id);
			}
			dataTable.put(id, handler);
		}
	}
	
	public IUserCommandHandler getUserCommandHandler(final int userCommand)
	{
		if (Config.DEBUG)
		{
			LOGGER.debug("getting handler for user command: " + userCommand);
		}
		
		return dataTable.get(userCommand);
	}
	
	public int size()
	{
		return dataTable.size();
	}
}