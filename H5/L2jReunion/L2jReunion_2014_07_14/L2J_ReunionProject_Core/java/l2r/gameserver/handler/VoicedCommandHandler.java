/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import l2r.Config;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.AioItemVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.Antibot;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.Banking;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.CcpVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.ChangePassword;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.ChatAdmin;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.Debug;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.EvenlyDistributeItems;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.Hellbound;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.ItemBufferVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.Lang;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.OnlineVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.PremiumVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.PvpZoneVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.RepairVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.TeleportsVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.VotePanelVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.VoteVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.Wedding;
import gr.reunion.configsEngine.AioBufferConfigs;
import gr.reunion.configsEngine.AioItemsConfigs;
import gr.reunion.configsEngine.AntibotConfigs;
import gr.reunion.configsEngine.ChaoticZoneConfigs;
import gr.reunion.configsEngine.CustomServerConfigs;
import gr.reunion.configsEngine.GetRewardVoteSystemConfigs;
import gr.reunion.configsEngine.IndividualVoteSystemConfigs;
import gr.reunion.configsEngine.PremiumServiceConfigs;
import gr.reunion.voteEngine.RewardVote;

/**
 * @author UnAfraid
 */
public class VoicedCommandHandler implements IHandler<IVoicedCommandHandler, String>
{
	private final Map<String, IVoicedCommandHandler> _datatable;
	
	protected VoicedCommandHandler()
	{
		_datatable = new HashMap<>();
		
		registerHandler(new VoteVCmd());
		
		if (CustomServerConfigs.ENABLE_CHARACTER_CONTROL_PANEL)
		{
			registerHandler(new CcpVCmd());
		}
		
		if (PremiumServiceConfigs.USE_PREMIUM_SERVICE)
		{
			registerHandler(new PremiumVCmd());
		}
		
		if (AntibotConfigs.ENABLE_ANTIBOT_SYSTEMS)
		{
			registerHandler(new Antibot());
		}
		
		if (ChaoticZoneConfigs.ENABLE_CHAOTIC_ZONE)
		{
			registerHandler(new PvpZoneVCmd());
		}
		
		if (AioBufferConfigs.ENABLE_AIO_BUFFER && PremiumServiceConfigs.USE_PREMIUM_SERVICE)
		{
			registerHandler(new ItemBufferVCmd());
		}
		
		if (IndividualVoteSystemConfigs.ENABLE_VOTE_SYSTEM)
		{
			registerHandler(new VotePanelVCmd());
		}
		
		if (GetRewardVoteSystemConfigs.ENABLE_VOTE_SYSTEM)
		{
			registerHandler(new RewardVote());
		}
		
		if (CustomServerConfigs.ALLOW_ONLINE_COMMAND)
		{
			registerHandler(new OnlineVCmd());
		}
		
		if (CustomServerConfigs.ALLOW_REPAIR_COMMAND)
		{
			registerHandler(new RepairVCmd());
		}
		
		if (CustomServerConfigs.ALLOW_TELEPORTS_COMMAND)
		{
			registerHandler(new TeleportsVCmd());
		}
		
		if (Config.BANKING_SYSTEM_ENABLED)
		{
			registerHandler(new Banking());
		}
		
		if (CustomServerConfigs.EVENLY_DISTRIBUTED_ITEMS)
		{
			registerHandler(new EvenlyDistributeItems());
		}
		
		if (AioItemsConfigs.ALLOW_AIO_ITEM_COMMAND && AioItemsConfigs.ENABLE_AIO_NPCS)
		{
			registerHandler(new AioItemVCmd());
		}
		
		if (Config.L2JMOD_ALLOW_CHANGE_PASSWORD)
		{
			registerHandler(new ChangePassword());
		}
		if (Config.L2JMOD_CHAT_ADMIN)
		{
			registerHandler(new ChatAdmin());
		}
		
		if (Config.L2JMOD_MULTILANG_ENABLE && Config.L2JMOD_MULTILANG_VOICED_ALLOW)
		{
			registerHandler(new Lang());
		}
		
		if (Config.L2JMOD_DEBUG_VOICE_COMMAND)
		{
			registerHandler(new Debug());
		}
		
		if (Config.L2JMOD_HELLBOUND_STATUS)
		{
			registerHandler(new Hellbound());
		}
		
		if ((Config.L2JMOD_MULTILANG_ENABLE) && (Config.L2JMOD_MULTILANG_VOICED_ALLOW))
		{
			registerHandler(new Lang());
		}
		
		if (Config.L2JMOD_ALLOW_WEDDING)
		{
			registerHandler(new Wedding());
		}
	}
	
	@Override
	public void registerHandler(IVoicedCommandHandler handler)
	{
		String[] ids = handler.getVoicedCommandList();
		for (String id : ids)
		{
			_datatable.put(id, handler);
		}
	}
	
	@Override
	public synchronized void removeHandler(IVoicedCommandHandler handler)
	{
		String[] ids = handler.getVoicedCommandList();
		for (String id : ids)
		{
			_datatable.remove(id);
		}
	}
	
	@Override
	public IVoicedCommandHandler getHandler(String voicedCommand)
	{
		String command = voicedCommand;
		if (voicedCommand.contains(" "))
		{
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
		}
		return _datatable.get(command);
	}
	
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	public static VoicedCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final VoicedCommandHandler _instance = new VoicedCommandHandler();
	}
}
