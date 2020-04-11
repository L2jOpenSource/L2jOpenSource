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

import l2r.gameserver.scripts.handlers.chathandlers.ChatAll;
import l2r.gameserver.scripts.handlers.chathandlers.ChatAlliance;
import l2r.gameserver.scripts.handlers.chathandlers.ChatBattlefield;
import l2r.gameserver.scripts.handlers.chathandlers.ChatClan;
import l2r.gameserver.scripts.handlers.chathandlers.ChatHeroVoice;
import l2r.gameserver.scripts.handlers.chathandlers.ChatParty;
import l2r.gameserver.scripts.handlers.chathandlers.ChatPartyMatchRoom;
import l2r.gameserver.scripts.handlers.chathandlers.ChatPartyRoomAll;
import l2r.gameserver.scripts.handlers.chathandlers.ChatPartyRoomCommander;
import l2r.gameserver.scripts.handlers.chathandlers.ChatPetition;
import l2r.gameserver.scripts.handlers.chathandlers.ChatShout;
import l2r.gameserver.scripts.handlers.chathandlers.ChatTell;
import l2r.gameserver.scripts.handlers.chathandlers.ChatTrade;

/**
 * This class handles all chat handlers
 * @author durgus, UnAfraid
 */
public class ChatHandler implements IHandler<IChatHandler, Integer>
{
	private final Map<Integer, IChatHandler> _datatable;
	
	/**
	 * Singleton constructor
	 */
	protected ChatHandler()
	{
		_datatable = new HashMap<>();
		
		registerHandler(new ChatAll());
		registerHandler(new ChatAlliance());
		registerHandler(new ChatBattlefield());
		registerHandler(new ChatClan());
		registerHandler(new ChatHeroVoice());
		registerHandler(new ChatParty());
		registerHandler(new ChatPartyMatchRoom());
		registerHandler(new ChatPartyRoomAll());
		registerHandler(new ChatPartyRoomCommander());
		registerHandler(new ChatPetition());
		registerHandler(new ChatShout());
		registerHandler(new ChatTell());
		registerHandler(new ChatTrade());
	}
	
	/**
	 * Register a new chat handler
	 * @param handler
	 */
	@Override
	public void registerHandler(IChatHandler handler)
	{
		int[] ids = handler.getChatTypeList();
		for (int id : ids)
		{
			_datatable.put(id, handler);
		}
	}
	
	@Override
	public synchronized void removeHandler(IChatHandler handler)
	{
		int[] ids = handler.getChatTypeList();
		for (int id : ids)
		{
			_datatable.remove(id);
		}
	}
	
	/**
	 * Get the chat handler for the given chat type
	 * @param chatType
	 * @return
	 */
	@Override
	public IChatHandler getHandler(Integer chatType)
	{
		return _datatable.get(chatType);
	}
	
	/**
	 * Returns the size
	 * @return
	 */
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	public static ChatHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ChatHandler _instance = new ChatHandler();
	}
}