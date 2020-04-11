package com.l2jfrozen.gameserver.model.zone.shape;

import com.l2jfrozen.gameserver.model.zone.L2ZoneShape;

/**
 * A primitive rectangular zone
 * @author durgus
 */
public class ZoneCuboid extends L2ZoneShape
{
	private int zoneX1, zoneX2, zoneY1, zoneY2, zoneZ1, zoneZ2;
	
	public ZoneCuboid(final int x1, final int x2, final int y1, final int y2, final int z1, final int z2)
	{
		zoneX1 = x1;
		zoneX2 = x2;
		if (zoneX1 > zoneX2) // switch them if alignment is wrong
		{
			zoneX1 = x2;
			zoneX2 = x1;
		}
		
		zoneY1 = y1;
		zoneY2 = y2;
		if (zoneY1 > zoneY2) // switch them if alignment is wrong
		{
			zoneY1 = y2;
			zoneY2 = y1;
		}
		
		zoneZ1 = z1;
		zoneZ2 = z2;
		if (zoneZ1 > zoneZ2) // switch them if alignment is wrong
		{
			zoneZ1 = z2;
			zoneZ2 = z1;
		}
	}
	
	@Override
	public boolean isInsideZone(final int x, final int y, final int z)
	{
		if (x < zoneX1 || x > zoneX2 || y < zoneY1 || y > zoneY2 || z < zoneZ1 || z > zoneZ2)
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean intersectsRectangle(final int ax1, final int ax2, final int ay1, final int ay2)
	{
		// Check if any point inside this rectangle
		if (isInsideZone(ax1, ay1, (zoneZ2 - 1)))
		{
			return true;
		}
		
		if (isInsideZone(ax1, ay2, (zoneZ2 - 1)))
		{
			return true;
		}
		
		if (isInsideZone(ax2, ay1, (zoneZ2 - 1)))
		{
			return true;
		}
		
		if (isInsideZone(ax2, ay2, (zoneZ2 - 1)))
		{
			return true;
		}
		
		// Check if any point from this rectangle is inside the other one
		if (zoneX1 > ax1 && zoneX1 < ax2 && zoneY1 > ay1 && zoneY1 < ay2)
		{
			return true;
		}
		
		if (zoneX1 > ax1 && zoneX1 < ax2 && zoneY2 > ay1 && zoneY2 < ay2)
		{
			return true;
		}
		
		if (zoneX2 > ax1 && zoneX2 < ax2 && zoneY1 > ay1 && zoneY1 < ay2)
		{
			return true;
		}
		
		if (zoneX2 > ax1 && zoneX2 < ax2 && zoneY2 > ay1 && zoneY2 < ay2)
		{
			return true;
		}
		
		// Horizontal lines may intersect vertical lines
		if (lineIntersectsLine(zoneX1, zoneY1, zoneX2, zoneY1, ax1, ay1, ax1, ay2))
		{
			return true;
		}
		
		if (lineIntersectsLine(zoneX1, zoneY1, zoneX2, zoneY1, ax2, ay1, ax2, ay2))
		{
			return true;
		}
		
		if (lineIntersectsLine(zoneX1, zoneY2, zoneX2, zoneY2, ax1, ay1, ax1, ay2))
		{
			return true;
		}
		
		if (lineIntersectsLine(zoneX1, zoneY2, zoneX2, zoneY2, ax2, ay1, ax2, ay2))
		{
			return true;
		}
		
		// Vertical lines may intersect horizontal lines
		if (lineIntersectsLine(zoneX1, zoneY1, zoneX1, zoneY2, ax1, ay1, ax2, ay1))
		{
			return true;
		}
		
		if (lineIntersectsLine(zoneX1, zoneY1, zoneX1, zoneY2, ax1, ay2, ax2, ay2))
		{
			return true;
		}
		
		if (lineIntersectsLine(zoneX2, zoneY1, zoneX2, zoneY2, ax1, ay1, ax2, ay1))
		{
			return true;
		}
		
		if (lineIntersectsLine(zoneX2, zoneY1, zoneX2, zoneY2, ax1, ay2, ax2, ay2))
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public double getDistanceToZone(final int x, final int y)
	{
		// Since we aren't given a z coordinate to test against
		// we just use the minimum z coordinate to prevent the
		// function from saying we aren't in the zone because
		// of a bad z coordinate.
		if (isInsideZone(x, y, zoneZ1))
		{
			return 0; // If you are inside the zone distance to zone is 0.
		}
		
		double test, shortestDist = Math.pow(zoneX1 - x, 2) + Math.pow(zoneY1 - y, 2);
		
		test = Math.pow(zoneX1 - x, 2) + Math.pow(zoneY2 - y, 2);
		if (test < shortestDist)
		{
			shortestDist = test;
		}
		
		test = Math.pow(zoneX2 - x, 2) + Math.pow(zoneY1 - y, 2);
		if (test < shortestDist)
		{
			shortestDist = test;
		}
		
		test = Math.pow(zoneX2 - x, 2) + Math.pow(zoneY2 - y, 2);
		if (test < shortestDist)
		{
			shortestDist = test;
		}
		
		return Math.sqrt(shortestDist);
	}
	
	/*
	 * getLowZ() / getHighZ() - These two functions were added to cope with the demand of the new fishing algorithms, wich are now able to correctly place the hook in the water, thanks to getHighZ(). getLowZ() was added, considering potential future modifications.
	 */
	@Override
	public int getLowZ()
	{
		return zoneZ1;
	}
	
	@Override
	public int getHighZ()
	{
		return zoneZ2;
	}
}
