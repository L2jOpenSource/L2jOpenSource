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
package l2r.gameserver.model.itemcontainer;

import l2r.Config;
import l2r.gameserver.enums.ItemLocation;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.stats.Stats;

/**
 * @author UnAfraid
 */
public class PcFreight extends ItemContainer
{
	private final L2PcInstance _owner;
	private final int _ownerId;
	
	public PcFreight(int object_id)
	{
		_owner = null;
		_ownerId = object_id;
		restore();
	}
	
	public PcFreight(L2PcInstance owner)
	{
		_owner = owner;
		_ownerId = owner.getObjectId();
	}
	
	@Override
	public int getOwnerId()
	{
		return _ownerId;
	}
	
	@Override
	public L2PcInstance getOwner()
	{
		return _owner;
	}
	
	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.FREIGHT;
	}
	
	@Override
	public String getName()
	{
		return "Freight";
	}
	
	@Override
	public boolean validateCapacity(long slots)
	{
		int curSlots = _owner == null ? Config.ALT_FREIGHT_SLOTS : Config.ALT_FREIGHT_SLOTS + (int) _owner.getStat().calcStat(Stats.FREIGHT_LIM, 0, null, null);
		return ((getSize() + slots) <= curSlots);
	}
	
	@Override
	public void refreshWeight()
	{
	}
}