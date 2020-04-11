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

/**
 * @author TheOne
 */
public class ItemCreateEvent implements L2Event
{
	private L2PcInstance player;
	private int itemId;
	private String process;
	private Object reference;
	private long count;
	
	/**
	 * @return the player
	 */
	public L2PcInstance getPlayer()
	{
		return player;
	}
	
	/**
	 * @param player the player to set
	 */
	public void setPlayer(L2PcInstance player)
	{
		this.player = player;
	}
	
	/**
	 * @return the itemId
	 */
	public int getId()
	{
		return itemId;
	}
	
	/**
	 * @param itemId the itemId to set
	 */
	public void setItemId(int itemId)
	{
		this.itemId = itemId;
	}
	
	/**
	 * @return the process
	 */
	public String getProcess()
	{
		return process;
	}
	
	/**
	 * @param process the process to set
	 */
	public void setProcess(String process)
	{
		this.process = process;
	}
	
	/**
	 * @return the reference
	 */
	public Object getReference()
	{
		return reference;
	}
	
	/**
	 * @param reference the reference to set
	 */
	public void setReference(Object reference)
	{
		this.reference = reference;
	}
	
	/**
	 * @return the count
	 */
	public long getCount()
	{
		return count;
	}
	
	/**
	 * @param count the count to set
	 */
	public void setCount(long count)
	{
		this.count = count;
	}
}
