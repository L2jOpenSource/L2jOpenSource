package com.l2jfrozen.gameserver.model.zone.shape;

import com.l2jfrozen.gameserver.model.zone.L2ZoneShape;

/**
 * A primitive circular zone
 * @author durgus
 */
public class ZoneCylinder extends L2ZoneShape
{
	private final int zoneX, zoneY, zoneZ1, zoneZ2, zoneRad, zoneRadS;
	
	public ZoneCylinder(final int x, final int y, final int z1, final int z2, final int rad)
	{
		zoneX = x;
		zoneY = y;
		zoneZ1 = z1;
		zoneZ2 = z2;
		zoneRad = rad;
		zoneRadS = rad * rad;
	}
	
	@Override
	public boolean isInsideZone(final int x, final int y, final int z)
	{
		if (Math.pow(zoneX - x, 2) + Math.pow(zoneY - y, 2) > zoneRadS || z < zoneZ1 || z > zoneZ2)
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean intersectsRectangle(final int ax1, final int ax2, final int ay1, final int ay2)
	{
		// Circles point inside the rectangle?
		if (zoneX > ax1 && zoneX < ax2 && zoneY > ay1 && zoneY < ay2)
		{
			return true;
		}
		
		// Any point of the rectangle intersecting the Circle?
		if (Math.pow(ax1 - zoneX, 2) + Math.pow(ay1 - zoneY, 2) < zoneRadS)
		{
			return true;
		}
		
		if (Math.pow(ax1 - zoneX, 2) + Math.pow(ay2 - zoneY, 2) < zoneRadS)
		{
			return true;
		}
		
		if (Math.pow(ax2 - zoneX, 2) + Math.pow(ay1 - zoneY, 2) < zoneRadS)
		{
			return true;
		}
		
		if (Math.pow(ax2 - zoneX, 2) + Math.pow(ay2 - zoneY, 2) < zoneRadS)
		{
			return true;
		}
		
		// Collision on any side of the rectangle?
		if (zoneX > ax1 && zoneX < ax2)
		{
			if (Math.abs(zoneY - ay2) < zoneRad)
			{
				return true;
			}
			
			if (Math.abs(zoneY - ay1) < zoneRad)
			{
				return true;
			}
		}
		
		if (zoneY > ay1 && zoneY < ay2)
		{
			if (Math.abs(zoneX - ax2) < zoneRad)
			{
				return true;
			}
			
			if (Math.abs(zoneX - ax1) < zoneRad)
			{
				return true;
			}
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
		
		return Math.sqrt((Math.pow(zoneX - x, 2) + Math.pow(zoneY - y, 2))) - zoneRad;
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
