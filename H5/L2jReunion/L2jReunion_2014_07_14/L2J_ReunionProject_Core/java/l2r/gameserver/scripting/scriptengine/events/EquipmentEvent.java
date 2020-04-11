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

import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.scripting.scriptengine.events.impl.L2Event;

/**
 * @author TheOne
 */
public class EquipmentEvent implements L2Event
{
	private L2ItemInstance item;
	private boolean isEquipped;
	
	/**
	 * @return the item
	 */
	public L2ItemInstance getItem()
	{
		return item;
	}
	
	/**
	 * @param item the item to set
	 */
	public void setItem(L2ItemInstance item)
	{
		this.item = item;
	}
	
	/**
	 * @return the isEquipped
	 */
	public boolean isEquipped()
	{
		return isEquipped;
	}
	
	/**
	 * @param isEquipped the isEquipped to set
	 */
	public void setEquipped(boolean isEquipped)
	{
		this.isEquipped = isEquipped;
	}
}
