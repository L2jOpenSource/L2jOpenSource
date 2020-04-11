
/**
 coded by Balancer
 balancer@balancer.ru
 http://balancer.ru

 version 0.1, 2005-03-12
 */
package com.l2jfrozen.gameserver.model;

import java.awt.Polygon;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.util.random.Rnd;

public class L2Territory
{
	private static Logger LOGGER = Logger.getLogger(L2Territory.class);
	
	protected class Point
	{
		protected int x, y, zMin, zMax, proc;
		
		Point(final int x, final int y, final int zmin, final int zmax, final int proc)
		{
			this.x = x;
			this.y = y;
			zMin = zmin;
			zMax = zmax;
			this.proc = proc;
		}
	}
	
	private Point[] points;
	// private String terr;
	private int xMin;
	private int xMax;
	private int yMin;
	private int yMax;
	private int territoryZMin;
	private int territoryZMax;
	private int procMax;
	private final Polygon poly;
	
	public L2Territory(/* String string */)
	{
		poly = new Polygon();
		points = new Point[0];
		// terr = string;
		xMin = 999999;
		xMax = -999999;
		yMin = 999999;
		yMax = -999999;
		territoryZMin = 999999;
		territoryZMax = -999999;
		procMax = 0;
	}
	
	public void add(final int x, final int y, final int zmin, final int zmax, final int proc)
	{
		Point[] newPoints = new Point[points.length + 1];
		System.arraycopy(points, 0, newPoints, 0, points.length);
		newPoints[points.length] = new Point(x, y, zmin, zmax, proc);
		points = newPoints;
		
		poly.addPoint(x, y);
		
		if (x < xMin)
		{
			xMin = x;
		}
		
		if (y < yMin)
		{
			yMin = y;
		}
		
		if (x > xMax)
		{
			xMax = x;
		}
		
		if (y > yMax)
		{
			yMax = y;
		}
		
		if (zmin < territoryZMin)
		{
			territoryZMin = zmin;
		}
		
		if (zmax > territoryZMax)
		{
			territoryZMax = zmax;
		}
		
		procMax += proc;
		
		newPoints = null;
	}
	
	public void print()
	{
		for (final Point p : points)
		{
			LOGGER.info("(" + p.x + "," + p.y + ")");
		}
	}
	
	public boolean isIntersect(final int x, final int y, final Point p1, final Point p2)
	{
		final double dy1 = p1.y - y;
		final double dy2 = p2.y - y;
		
		if (Math.signum(dy1) == Math.signum(dy2))
		{
			return false;
		}
		
		final double dx1 = p1.x - x;
		final double dx2 = p2.x - x;
		
		if (dx1 >= 0 && dx2 >= 0)
		{
			return true;
		}
		
		if (dx1 < 0 && dx2 < 0)
		{
			return false;
		}
		
		final double dx0 = dy1 * (p1.x - p2.x) / (p1.y - p2.y);
		
		return dx0 <= dx1;
	}
	
	public boolean isInside(final int x, final int y)
	{
		return poly.contains(x, y);
	}
	
	public int[] getRandomPoint()
	{
		int i;
		final int[] p = new int[3];
		
		for (i = 0; i < 100; i++)
		{
			p[0] = Rnd.get(xMin, xMax);
			p[1] = Rnd.get(yMin, yMax);
			
			if (i == 40)
			{
				LOGGER.warn("Heavy territory: " + this + ", need manual correction");
			}
			
			if (poly.contains(p[0], p[1]))
			{
				if (Config.GEODATA > 0)
				{
					final int tempz = GeoData.getInstance().getHeight(p[0], p[1], territoryZMin + (territoryZMax - territoryZMin) / 2);
					
					if (territoryZMin != territoryZMax)
					{
						if (tempz < territoryZMin || tempz > territoryZMax || territoryZMin > territoryZMax)
						{
							continue;
						}
					}
					else if (tempz < territoryZMin - 200 || tempz > territoryZMin + 200)
					{
						continue;
					}
					
					p[2] = tempz;
					
					if (GeoData.getInstance().getNSWE(p[0], p[1], p[2]) != 15)
					{
						continue;
					}
					
					return p;
				}
				
				double curdistance = -1;
				p[2] = territoryZMin;
				
				for (i = 0; i < points.length; i++)
				{
					Point p1 = points[i];
					
					final long dx = p1.x - p[0];
					final long dy = p1.y - p[1];
					final double sqdistance = dx * dx + dy * dy;
					
					if (curdistance == -1 || sqdistance < curdistance)
					{
						curdistance = sqdistance;
						p[2] = p1.zMin;
					}
					
					p1 = null;
				}
				return p;
			}
		}
		LOGGER.warn("Can't make point for " + this);
		return p;
	}
	
	public int getProcMax()
	{
		return procMax;
	}
	
	public int getYmin()
	{
		return yMin;
	}
	
	public int getXmax()
	{
		return xMax;
	}
	
	public int getXmin()
	{
		return xMin;
	}
	
	public int getYmax()
	{
		return yMax;
	}
	
	public int getZmin()
	{
		return territoryZMin;
	}
	
	public int getZmax()
	{
		return territoryZMax;
	}
}
