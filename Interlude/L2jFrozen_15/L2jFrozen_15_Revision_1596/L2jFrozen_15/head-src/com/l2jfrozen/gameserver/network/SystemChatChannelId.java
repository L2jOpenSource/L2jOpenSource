package com.l2jfrozen.gameserver.network;

/**
 * @author Beetle
 */
public enum SystemChatChannelId
{
	CHAT_NORMAL("ALL"), // id = 0 , white
	CHAT_SHOUT("SHOUT"), // ! id = 1 , dark orange
	CHAT_TELL("WHISPER"), // " id = 2, purple
	CHAT_PARTY("PARTY"), // # id = 3, green
	CHAT_CLAN("CLAN"), // @ id = 4, blue/purple
	CHAT_SYSTEM("EMOTE"), // ( id = 5
	CHAT_USER_PET("USERPET"), // * id = 6
	CHAT_GM_PET("GMPET"), // * id = 7
	CHAT_MARKET("TRADE"), // + id = 8 pink
	CHAT_ALLIANCE("ALLIANCE"), // $ id = 9 light green
	CHAT_ANNOUNCE("ANNOUNCE"), // id = 10 light cyan
	CHAT_CUSTOM("CRASH"), // id = 11 --> Crashes client
	CHAT_L2FRIEND("L2FRIEND"), // id = 12
	CHAT_MSN("MSN"), // id = 13
	CHAT_PARTY_ROOM("PARTYROOM"), // id = 14
	CHAT_COMMANDER("COMMANDER"), // id = 15
	CHAT_INNER_PARTYMASTER("INNERPARTYMASTER"), // id = 16
	CHAT_HERO("HERO"), // % id = 17 blue
	CHAT_CRITICAL_ANNOUNCE("CRITANNOUNCE"), // id = 18 dark cyan
	CHAT_UNKNOWN("UNKNOWN"), // id = 19
	CHAT_BATTLEFIELD("BATTLEFIELD"), // ^ id = 20
	CHAT_NONE("NONE");
	private String channelName;
	
	private SystemChatChannelId(final String channelName)
	{
		this.channelName = channelName;
	}
	
	public int getId()
	{
		return ordinal();
	}
	
	public String getName()
	{
		return channelName;
	}
	
	public static SystemChatChannelId getChatType(final int channelId)
	{
		for (final SystemChatChannelId channel : SystemChatChannelId.values())
		{
			if (channel.getId() == channelId)
			{
				return channel;
			}
		}
		return SystemChatChannelId.CHAT_NONE;
	}
}
