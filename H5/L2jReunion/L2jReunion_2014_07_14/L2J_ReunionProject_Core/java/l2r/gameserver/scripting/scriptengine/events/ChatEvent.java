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
package l2r.gameserver.scripting.scriptengine.events;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.scripting.scriptengine.events.impl.L2Event;
import l2r.gameserver.scripting.scriptengine.listeners.talk.ChatListener.ChatTargetType;

/**
 * @author TheOne
 */
public class ChatEvent implements L2Event
{
	private String text;
	private L2PcInstance origin;
	private String target;
	private ChatTargetType targetType;
	
	/**
	 * @return the text
	 */
	public String getText()
	{
		return text;
	}
	
	/**
	 * @param text the text to set
	 */
	public void setText(String text)
	{
		this.text = text;
	}
	
	/**
	 * @return the origin
	 */
	public L2PcInstance getOrigin()
	{
		return origin;
	}
	
	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(L2PcInstance origin)
	{
		this.origin = origin;
	}
	
	/**
	 * @return the target
	 */
	public String getTarget()
	{
		return target;
	}
	
	/**
	 * @param target the target to set
	 */
	public void setTarget(String target)
	{
		this.target = target;
	}
	
	/**
	 * @return the targetType
	 */
	public ChatTargetType getTargetType()
	{
		return targetType;
	}
	
	/**
	 * @param targetType the targetType to set
	 */
	public void setTargetType(ChatTargetType targetType)
	{
		this.targetType = targetType;
	}
}
