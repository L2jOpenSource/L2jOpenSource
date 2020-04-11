package com.l2jfrozen.gameserver.model.zone.shape;

import com.l2jfrozen.gameserver.model.zone.L2ZoneShape;

/**
 * A not so primitive npoly zone
 * @author durgus
 */
public class ZoneNPoly extends L2ZoneShape
{
	private final int[] zoneX;
	private final int[] zoneY;
	private final int zoneZ1;
	private final int zoneZ2;
	
	public ZoneNPoly(final int[] x, final int[] y, final int z1, final int z2)
	{
		zoneX = x;
		zoneY = y;
		zoneZ1 = z1;
		zoneZ2 = z2;
	}
	
	@Override
	public boolean isInsideZone(final int x, final int y, final int z)
	{
		if (z < zoneZ1 || z > zoneZ2)
		{
			return false;
		}
		
		boolean inside = false;
		for (int i = 0, j = zoneX.length - 1; i < zoneX.length; j = i++)
		{
			if ((((zoneY[i] <= y) && (y < zoneY[j])) || ((zoneY[j] <= y) && (y < zoneY[i]))) && (x < (zoneX[j] - zoneX[i]) * (y - zoneY[i]) / (zoneY[j] - zoneY[i]) + zoneX[i]))
			{
				inside = !inside;
			}
		}
		return inside;
	}
	
	@Override
	public boolean intersectsRectangle(final int ax1, final int ax2, final int ay1, final int ay2)
	{
		int tX, tY, uX, uY;
		
		// First check if a point of the polygon lies inside the rectangle
		if (zoneX[0] > ax1 && zoneX[0] < ax2 && zoneY[0] > ay1 && zoneY[0] < ay2)
		{
			return true;
		}
		
		// Or a point of the rectangle inside the polygon
		if (isInsideZone(ax1, ay1, (zoneZ2 - 1)))
		{
			return true;
		}
		
		// If the first point wasn't inside the rectangle it might still have any line crossing any side
		// of the rectangle
		
		// Check every possible line of the polygon for a collision with any of the rectangles side
		for (int i = 0; i < zoneY.length; i++)
		{
			tX = zoneX[i];
			tY = zoneY[i];
			uX = zoneX[(i + 1) % zoneX.length];
			uY = zoneY[(i + 1) % zoneX.length];
			
			// Check if this line intersects any of the four sites of the rectangle
			if (lineSegmentsIntersect(tX, tY, uX, uY, ax1, ay1, ax1, ay2))
			{
				return true;
			}
			if (lineSegmentsIntersect(tX, tY, uX, uY, ax1, ay1, ax2, ay1))
			{
				return true;
			}
			if (lineSegmentsIntersect(tX, tY, uX, uY, ax2, ay2, ax1, ay2))
			{
				return true;
			}
			if (lineSegmentsIntersect(tX, tY, uX, uY, ax2, ay2, ax2, ay1))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public double getDistanceToZone(final int x, final int y)
	{
		double test, shortestDist = Math.pow(zoneX[0] - x, 2) + Math.pow(zoneY[0] - y, 2);
		
		for (int i = 1; i < zoneY.length; i++)
		{
			test = Math.pow(zoneX[i] - x, 2) + Math.pow(zoneY[i] - y, 2);
			if (test < shortestDist)
			{
				shortestDist = test;
			}
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