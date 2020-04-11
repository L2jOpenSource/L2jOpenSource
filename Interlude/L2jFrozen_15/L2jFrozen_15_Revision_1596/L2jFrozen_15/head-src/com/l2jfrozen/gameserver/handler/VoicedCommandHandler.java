package com.l2jfrozen.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.GameServer;
import com.l2jfrozen.gameserver.handler.voicedcommandhandlers.AwayCmd;
import com.l2jfrozen.gameserver.handler.voicedcommandhandlers.BankingCmd;
import com.l2jfrozen.gameserver.handler.voicedcommandhandlers.CTFCmd;
import com.l2jfrozen.gameserver.handler.voicedcommandhandlers.DMCmd;
import com.l2jfrozen.gameserver.handler.voicedcommandhandlers.FarmPvpCmd;
import com.l2jfrozen.gameserver.handler.voicedcommandhandlers.OfflineShop;
import com.l2jfrozen.gameserver.handler.voicedcommandhandlers.Online;
import com.l2jfrozen.gameserver.handler.voicedcommandhandlers.Repair;
import com.l2jfrozen.gameserver.handler.voicedcommandhandlers.ServerTimeCmd;
import com.l2jfrozen.gameserver.handler.voicedcommandhandlers.StatsCmd;
import com.l2jfrozen.gameserver.handler.voicedcommandhandlers.TvTCmd;
import com.l2jfrozen.gameserver.handler.voicedcommandhandlers.WeddingCmd;

public class VoicedCommandHandler
{
	private static Logger LOGGER = Logger.getLogger(GameServer.class);
	private static VoicedCommandHandler instance;
	private Map<String, IVoicedCommandHandler> voicedCommands = new HashMap<>();
	
	public static VoicedCommandHandler getInstance()
	{
		if (instance == null)
		{
			instance = new VoicedCommandHandler();
		}
		
		return instance;
	}
	
	private VoicedCommandHandler()
	{
		if (Config.BANKING_SYSTEM_ENABLED)
		{
			registerVoicedCommandHandler(new BankingCmd());
		}
		
		if (Config.CTF_COMMAND)
		{
			registerVoicedCommandHandler(new CTFCmd());
		}
		
		if (Config.TVT_COMMAND)
		{
			registerVoicedCommandHandler(new TvTCmd());
		}
		
		if (Config.DM_COMMAND)
		{
			registerVoicedCommandHandler(new DMCmd());
		}
		
		if (Config.L2JMOD_ALLOW_WEDDING)
		{
			registerVoicedCommandHandler(new WeddingCmd());
		}
		
		registerVoicedCommandHandler(new StatsCmd());
		
		if (Config.ALLOW_AWAY_STATUS)
		{
			registerVoicedCommandHandler(new AwayCmd());
		}
		
		if (Config.ALLOW_FARM1_COMMAND || Config.ALLOW_FARM2_COMMAND || Config.ALLOW_PVP1_COMMAND || Config.ALLOW_PVP2_COMMAND)
		{
			registerVoicedCommandHandler(new FarmPvpCmd());
		}
		
		if (Config.ALLOW_ONLINE_VIEW)
		{
			registerVoicedCommandHandler(new Online());
		}
		
		if (Config.OFFLINE_TRADE_ENABLE && Config.OFFLINE_COMMAND2)
		{
			registerVoicedCommandHandler(new OfflineShop());
		}
		
		if (Config.CHARACTER_REPAIR)
		{
			registerVoicedCommandHandler(new Repair());
		}
		
		registerVoicedCommandHandler(new ServerTimeCmd());
		
		LOGGER.info("VoicedCommandHandler: Loaded " + voicedCommands.size() + " handlers.");
		
	}
	
	public void registerVoicedCommandHandler(IVoicedCommandHandler handler)
	{
		String[] ids = handler.getVoicedCommandList();
		
		for (String id : ids)
		{
			if (Config.DEBUG)
			{
				LOGGER.debug("Adding handler for command " + id);
			}
			
			voicedCommands.put(id, handler);
		}
		
	}
	
	public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand)
	{
		String command = voicedCommand;
		
		if (voicedCommand.indexOf(" ") != -1)
		{
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
		}
		
		if (Config.DEBUG)
		{
			LOGGER.debug("getting handler for command: " + command + " -> " + (voicedCommands.get(command) != null));
		}
		
		return voicedCommands.get(command);
	}
	
	public int size()
	{
		return voicedCommands.size();
	}
}