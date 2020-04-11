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
package l2r.gameserver.util;

import java.awt.Color;

import l2r.gameserver.GeoData;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.ExServerPrimitive;

import com.l2jserver.gameserver.geoengine.Direction;

/**
 * @author FBIagent
 */
public final class GeoUtils
{
	public static void debug2DLine(L2PcInstance player, int x, int y, int tx, int ty, int z)
	{
		int gx = GeoData.getInstance().getGeoX(x);
		int gy = GeoData.getInstance().getGeoY(y);
		
		int tgx = GeoData.getInstance().getGeoX(tx);
		int tgy = GeoData.getInstance().getGeoY(ty);
		
		ExServerPrimitive prim = new ExServerPrimitive("Debug2DLine", x, y, z);
		prim.addLine(Color.BLUE, GeoData.getInstance().getWorldX(gx), GeoData.getInstance().getWorldY(gy), z, GeoData.getInstance().getWorldX(tgx), GeoData.getInstance().getWorldY(tgy), z);
		
		LinePointIterator iter = new LinePointIterator(gx, gy, tgx, tgy);
		
		while (iter.next())
		{
			int wx = GeoData.getInstance().getWorldX(iter.x());
			int wy = GeoData.getInstance().getWorldY(iter.y());
			
			prim.addPoint(Color.RED, wx, wy, z);
		}
		player.sendPacket(prim);
	}
	
	public static void debug3DLine(L2PcInstance player, int x, int y, int z, int tx, int ty, int tz)
	{
		int gx = GeoData.getInstance().getGeoX(x);
		int gy = GeoData.getInstance().getGeoY(y);
		
		int tgx = GeoData.getInstance().getGeoX(tx);
		int tgy = GeoData.getInstance().getGeoY(ty);
		
		ExServerPrimitive prim = new ExServerPrimitive("Debug3DLine", x, y, z);
		prim.addLine(Color.BLUE, GeoData.getInstance().getWorldX(gx), GeoData.getInstance().getWorldY(gy), z, GeoData.getInstance().getWorldX(tgx), GeoData.getInstance().getWorldY(tgy), tz);
		
		LinePointIterator3D iter = new LinePointIterator3D(gx, gy, z, tgx, tgy, tz);
		iter.next();
		int prevX = iter.x();
		int prevY = iter.y();
		int wx = GeoData.getInstance().getWorldX(prevX);
		int wy = GeoData.getInstance().getWorldY(prevY);
		int wz = iter.z();
		prim.addPoint(Color.RED, wx, wy, wz);
		
		while (iter.next())
		{
			int curX = iter.x();
			int curY = iter.y();
			
			if ((curX != prevX) || (curY != prevY))
			{
				wx = GeoData.getInstance().getWorldX(curX);
				wy = GeoData.getInstance().getWorldY(curY);
				wz = iter.z();
				
				prim.addPoint(Color.RED, wx, wy, wz);
				
				prevX = curX;
				prevY = curY;
			}
		}
		player.sendPacket(prim);
	}
	
	/**
	 * difference between x values: never abover 1<br>
	 * difference between y values: never above 1
	 * @param lastX
	 * @param lastY
	 * @param x
	 * @param y
	 * @return
	 */
	public static Direction computeDirection(int lastX, int lastY, int x, int y)
	{
		if (x > lastX) // east
		{
			if (y > lastY)
			{
				return Direction.SOUTH_EAST;
			}
			else if (y < lastY)
			{
				return Direction.NORTH_EAST;
			}
			else
			{
				return Direction.EAST;
			}
		}
		else if (x < lastX) // west
		{
			if (y > lastY)
			{
				return Direction.SOUTH_WEST;
			}
			else if (y < lastY)
			{
				return Direction.NORTH_WEST;
			}
			else
			{
				return Direction.WEST;
			}
		}
		else
		// unchanged x
		{
			if (y > lastY)
			{
				return Direction.SOUTH;
			}
			else if (y < lastY)
			{
				return Direction.NORTH;
			}
			else
			{
				return null;// error, should never happen, TODO: Logging
			}
		}
	}
}