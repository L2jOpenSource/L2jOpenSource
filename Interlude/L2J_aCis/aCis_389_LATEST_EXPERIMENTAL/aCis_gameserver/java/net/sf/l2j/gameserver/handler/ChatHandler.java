package net.sf.l2j.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.gameserver.enums.SayType;
import net.sf.l2j.gameserver.handler.chathandlers.ChatAll;
import net.sf.l2j.gameserver.handler.chathandlers.ChatAlliance;
import net.sf.l2j.gameserver.handler.chathandlers.ChatClan;
import net.sf.l2j.gameserver.handler.chathandlers.ChatHeroVoice;
import net.sf.l2j.gameserver.handler.chathandlers.ChatParty;
import net.sf.l2j.gameserver.handler.chathandlers.ChatPartyMatchRoom;
import net.sf.l2j.gameserver.handler.chathandlers.ChatPartyRoomAll;
import net.sf.l2j.gameserver.handler.chathandlers.ChatPartyRoomCommander;
import net.sf.l2j.gameserver.handler.chathandlers.ChatPetition;
import net.sf.l2j.gameserver.handler.chathandlers.ChatShout;
import net.sf.l2j.gameserver.handler.chathandlers.ChatTell;
import net.sf.l2j.gameserver.handler.chathandlers.ChatTrade;

public class ChatHandler
{
	private final Map<SayType, IChatHandler> _entries = new HashMap<>();
	
	protected ChatHandler()
	{
		registerHandler(new ChatAll());
		registerHandler(new ChatAlliance());
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
	
	private void registerHandler(IChatHandler handler)
	{
		for (SayType type : handler.getChatTypeList())
			_entries.put(type, handler);
	}
	
	public IChatHandler getHandler(SayType chatType)
	{
		return _entries.get(chatType);
	}
	
	public int size()
	{
		return _entries.size();
	}
	
	public static ChatHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ChatHandler INSTANCE = new ChatHandler();
	}
}