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
package l2r.gameserver.scripting.scriptengine.listeners.talk;

import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.scripting.scriptengine.events.ChatEvent;
import l2r.gameserver.scripting.scriptengine.impl.L2JListener;

/**
 * Listener to intercept player chat.<br>
 * Useful to create customized chat log or any other use you can find for it.<br>
 * See network.Say2.java
 * @author TheOne
 */
public abstract class ChatListener extends L2JListener
{
	public ChatListener()
	{
		register();
	}
	
	/**
	 * Notifies that the given text was sent from player1(origin) to a given target.<br>
	 * @param event
	 */
	public abstract void onTalk(ChatEvent event);
	
	@Override
	public void register()
	{
		Say2.addChatListener(this);
	}
	
	@Override
	public void unregister()
	{
		Say2.removeChatListener(this);
	}
	
	/**
	 * Defines the type of talk/chat taking place
	 * @author TheOne
	 */
	public enum ChatTargetType
	{
		ALL,
		SHOUT,
		TELL,
		PARTY,
		CLAN,
		GM,
		PETITION_PLAYER,
		PETITION_GM,
		TRADE,
		ALLIANCE,
		ANNOUNCEMENT,
		BOAT,
		L2FRIEND,
		MSNCHAT,
		PARTYMATCH_ROOM,
		PARTYROOM_COMMANDER,
		PARTYROOM_ALL,
		HERO_VOICE,
		CRITICAL_ANNOUNCE,
		SCREEN_ANNOUNCE,
		BATTLEFIELD,
		MPCC_ROOM
	}
	
	/**
	 * Returns the ChatTargetType based on the type (String) given.<br>
	 * Default = ChatTargetType.ALL
	 * @param type
	 * @return
	 */
	public static ChatTargetType getTargetType(String type)
	{
		ChatTargetType targetType = ChatTargetType.ALL;
		try
		{
			targetType = ChatTargetType.valueOf(type);
		}
		catch (Exception e)
		{
			log.info("Invalid ChatTargetType:" + type);
			e.getMessage();
		}
		return targetType;
	}
}
