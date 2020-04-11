package com.l2jfrozen.util;

import java.io.Serializable;

/**
 * @author luisantonioa
 */
public class Point3D implements Serializable
{
	private static final long serialVersionUID = 4638345252031872576L;
	
	private volatile int x, y, z;
	
	public Point3D(final int pX, final int pY, final int pZ)
	{
		x = pX;
		y = pY;
		z = pZ;
	}
	
	public Point3D(final int pX, final int pY)
	{
		x = pX;
		y = pY;
		z = 0;
	}
	
	/**
	 * @param worldPosition
	 */
	public Point3D(final Point3D worldPosition)
	{
		synchronized (worldPosition)
		{
			x = worldPosition.x;
			y = worldPosition.y;
			z = worldPosition.z;
		}
	}
	
	public synchronized void setTo(final Point3D point)
	{
		synchronized (point)
		{
			x = point.x;
			y = point.y;
			z = point.z;
		}
	}
	
	@Override
	public String toString()
	{
		return "(" + x + ", " + y + ", " + z + ")";
	}
	
	@Override
	public int hashCode()
	{
		return x ^ y ^ z;
	}
	
	@Override
	public synchronized boolean equals(final Object o)
	{
		if (o instanceof Point3D)
		{
			final Point3D point3D = (Point3D) o;
			boolean ret;
			synchronized (point3D)
			{
				ret = point3D.x == x && point3D.y == y && point3D.z == z;
			}
			return ret;
		}
		return false;
	}
	
	public synchronized boolean equals(final int pX, final int pY, final int pZ)
	{
		return x == pX && y == pY && z == pZ;
	}
	
	public synchronized long distanceSquaredTo(final Point3D point)
	{
		long dx, dy;
		synchronized (point)
		{
			dx = x - point.x;
			dy = y - point.y;
		}
		return dx * dx + dy * dy;
	}
	
	public static long distanceSquared(final Point3D point1, final Point3D point2)
	{
		long dx, dy;
		synchronized (point1)
		{
			synchronized (point2)
			{
				dx = point1.x - point2.x;
				dy = point1.y - point2.y;
			}
		}
		return dx * dx + dy * dy;
	}
	
	public static boolean distanceLessThan(final Point3D point1, final Point3D point2, final double distance)
	{
		return distanceSquared(point1, point2) < distance * distance;
	}
	
	public synchronized int getX()
	{
		return x;
	}
	
	public synchronized void setX(final int pX)
	{
		x = pX;
	}
	
	public synchronized int getY()
	{
		return y;
	}
	
	public synchronized void setY(final int pY)
	{
		y = pY;
	}
	
	public synchronized int getZ()
	{
		return z;
	}
	
	public synchronized void setZ(final int pZ)
	{
		z = pZ;
	}
	
	public synchronized void setXYZ(final int pX, final int pY, final int pZ)
	{
		x = pX;
		y = pY;
		z = pZ;
	}
}
