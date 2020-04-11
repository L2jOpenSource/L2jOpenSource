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

import l2r.gameserver.model.L2Augmentation;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.scripting.scriptengine.events.impl.L2Event;

/**
 * @author TheOne
 */
public class AugmentEvent implements L2Event
{
	private L2ItemInstance item;
	private L2Augmentation augmentation;
	private boolean isAugment; // true = is being augmented // false = augment is being removed
	
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
	 * @return the augmentation
	 */
	public L2Augmentation getAugmentation()
	{
		return augmentation;
	}
	
	/**
	 * @param augmentation the augmentation to set
	 */
	public void setAugmentation(L2Augmentation augmentation)
	{
		this.augmentation = augmentation;
	}
	
	/**
	 * @return the isAugment
	 */
	public boolean isAugment()
	{
		return isAugment;
	}
	
	/**
	 * @param isAugment the isAugment to set
	 */
	public void setIsAugment(boolean isAugment)
	{
		this.isAugment = isAugment;
	}
}
