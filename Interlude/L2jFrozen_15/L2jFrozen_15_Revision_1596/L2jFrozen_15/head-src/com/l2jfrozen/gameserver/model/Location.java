package com.l2jfrozen.gameserver.model;

/**
 * This class ...
 * @version $Revision: 1.1.4.1 $ $Date: 2005/03/27 15:29:33 $
 */

public final class Location
{
	public int x;
	public int y;
	public int z;
	public int heading;
	
	public Location(final int x, final int y, final int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Location(final int x, final int y, final int z, final int heading)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = heading;
	}
	
	public Location(final L2Object obj)
	{
		x = obj.getX();
		y = obj.getY();
		z = obj.getZ();
	}
	
	public Location(final L2Character obj)
	{
		x = obj.getX();
		y = obj.getY();
		z = obj.getZ();
		heading = obj.getHeading();
	}
	
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
	
	public int getZ()
	{
		return z;
	}
	
	public int getHeading()
	{
		return heading;
	}
	
	public void setX(final int x)
	{
		this.x = x;
	}
	
	public void setY(final int y)
	{
		this.y = y;
	}
	
	public void setZ(final int z)
	{
		this.z = z;
	}
	
	public void setHeading(final int head)
	{
		heading = head;
	}
	
	public void setXYZ(final int x, final int y, final int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public boolean equals(final int x, final int y, final int z)
	{
		if (this.x == x && this.y == y && this.z == z)
		{
			return true;
		}
		return false;
	}
}
