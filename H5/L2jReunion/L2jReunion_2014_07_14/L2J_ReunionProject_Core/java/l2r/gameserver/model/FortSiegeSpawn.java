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
package l2r.gameserver.model;

import l2r.gameserver.model.interfaces.IIdentifiable;

/**
 * Fort Siege Spawn.
 * @author xban1x
 */
public final class FortSiegeSpawn extends Location implements IIdentifiable
{
	private final int _npcId;
	private final int _fortId;
	private final int _id;
	
	public FortSiegeSpawn(int fort_id, int x, int y, int z, int heading, int npc_id, int id)
	{
		super(x, y, z, heading);
		_fortId = fort_id;
		_npcId = npc_id;
		_id = id;
	}
	
	public int getFortId()
	{
		return _fortId;
	}
	
	/**
	 * Gets the NPC ID.
	 * @return the NPC ID
	 */
	@Override
	public int getId()
	{
		return _npcId;
	}
	
	public int getMessageId()
	{
		return _id;
	}
}
