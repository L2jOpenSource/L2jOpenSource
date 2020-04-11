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
package l2r.gameserver.pathfinding.cellnodes;

import l2r.gameserver.GeoData;
import l2r.gameserver.pathfinding.AbstractNodeLoc;

import com.l2jserver.gameserver.geoengine.Direction;

/**
 * @author -Nemesiss-, FBIagent
 */
public class NodeLoc extends AbstractNodeLoc
{
	private int _x;
	private int _y;
	private boolean _goNorth;
	private boolean _goEast;
	private boolean _goSouth;
	private boolean _goWest;
	private int _geoHeight;
	
	public NodeLoc(int x, int y, int z)
	{
		set(x, y, z);
	}
	
	public void set(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_goNorth = GeoData.getInstance().canEnterNeighbors(x, y, z, Direction.NORTH);
		_goEast = GeoData.getInstance().canEnterNeighbors(x, y, z, Direction.EAST);
		_goSouth = GeoData.getInstance().canEnterNeighbors(x, y, z, Direction.SOUTH);
		_goWest = GeoData.getInstance().canEnterNeighbors(x, y, z, Direction.WEST);
		_geoHeight = GeoData.getInstance().getNearestZ(x, y, z);
	}
	
	public boolean canGoNorth()
	{
		return _goNorth;
	}
	
	public boolean canGoEast()
	{
		return _goEast;
	}
	
	public boolean canGoSouth()
	{
		return _goSouth;
	}
	
	public boolean canGoWest()
	{
		return _goWest;
	}
	
	public boolean canGoNone()
	{
		return !canGoNorth() && !canGoEast() && !canGoSouth() && !canGoWest();
	}
	
	public boolean canGoAll()
	{
		return canGoNorth() && canGoEast() && canGoSouth() && canGoWest();
	}
	
	@Override
	public int getX()
	{
		return GeoData.getInstance().getWorldX(_x);
	}
	
	@Override
	public int getY()
	{
		return GeoData.getInstance().getWorldY(_y);
	}
	
	@Override
	public int getZ()
	{
		return _geoHeight;
	}
	
	@Override
	public void setZ(short z)
	{
		//
	}
	
	@Override
	public int getNodeX()
	{
		return _x;
	}
	
	@Override
	public int getNodeY()
	{
		return _y;
	}
	
	@Override
	public int hashCode()
	{
		
		final int prime = 31;
		int result = 1;
		result = (prime * result) + _x;
		result = (prime * result) + _y;
		
		byte nswe = 0;
		if (canGoNorth())
		{
			nswe |= 1;
		}
		if (canGoEast())
		{
			nswe |= 1 << 1;
		}
		if (canGoSouth())
		{
			nswe |= 1 << 2;
		}
		if (canGoEast())
		{
			nswe |= 1 << 3;
		}
		
		result = (prime * result) + (((_geoHeight & 0xFFFF) << 1) | nswe);
		return result;
		// return super.hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof NodeLoc))
		{
			return false;
		}
		final NodeLoc other = (NodeLoc) obj;
		if (_x != other._x)
		{
			return false;
		}
		if (_y != other._y)
		{
			return false;
		}
		if (_goNorth != other._goNorth)
		{
			return false;
		}
		if (_goEast != other._goEast)
		{
			return false;
		}
		if (_goSouth != other._goSouth)
		{
			return false;
		}
		if (_goWest != other._goWest)
		{
			return false;
		}
		if (_geoHeight != other._geoHeight)
		{
			return false;
		}
		return true;
	}
}
