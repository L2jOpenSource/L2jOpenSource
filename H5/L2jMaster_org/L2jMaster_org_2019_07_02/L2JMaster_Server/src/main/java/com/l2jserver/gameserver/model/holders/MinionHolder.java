/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.model.holders;

import com.l2jserver.gameserver.model.interfaces.IIdentifiable;

/**
 * This class hold info needed for minions spawns<br>
 * @author Zealar
 */
public class MinionHolder implements IIdentifiable
{
	private final int _id;
	private final int _count;
	private final long _respawnTime;
	private final int _weightPoint;
	
	/**
	 * Constructs a minion holder.
	 * @param id the id
	 * @param count the count
	 * @param respawnTime the respawn time
	 * @param weightPoint the weight point
	 */
	public MinionHolder(final int id, final int count, final long respawnTime, final int weightPoint)
	{
		_id = id;
		_count = count;
		_respawnTime = respawnTime;
		_weightPoint = weightPoint;
	}
	
	/**
	 * @return the Identifier of the Minion to spawn.
	 */
	@Override
	public int getId()
	{
		return _id;
	}
	
	/**
	 * @return the count of the Minions to spawn.
	 */
	public int getCount()
	{
		return _count;
	}
	
	/**
	 * @return the respawn time of the Minions.
	 */
	public long getRespawnTime()
	{
		return _respawnTime;
	}
	
	/**
	 * @return the weight point of the Minion.
	 */
	public int getWeightPoint()
	{
		return _weightPoint;
	}
}
