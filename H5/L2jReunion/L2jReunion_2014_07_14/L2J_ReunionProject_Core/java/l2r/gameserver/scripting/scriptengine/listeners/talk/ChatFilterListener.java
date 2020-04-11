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
 * Could be useful to improve or customize the integrated chat filters (i.e.: make it dependent on who's sending the message and the chat type... for example GMs could be unfiltered?).<br>
 * See Say2.java
 * @author TheOne
 */
public abstract class ChatFilterListener extends L2JListener
{
	public ChatFilterListener()
	{
		register();
	}
	
	/**
	 * Allows for filtering the text
	 * @param event
	 * @return
	 */
	public abstract String onTalk(ChatEvent event);
	
	@Override
	public void register()
	{
		Say2.addChatFilterListener(this);
	}
	
	@Override
	public void unregister()
	{
		Say2.removeChatFilterListener(this);
	}
}
