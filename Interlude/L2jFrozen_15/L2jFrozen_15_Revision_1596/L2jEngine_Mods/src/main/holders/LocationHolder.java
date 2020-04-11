package main.holders;

import com.l2jfrozen.util.random.Rnd;

/**
 * Simple class for storing location x/z/y/heading/npcId
 */
public class LocationHolder
{
	protected int x;
	protected int y;
	protected int z;
	protected int heading;
	private int npcId;
	
	public LocationHolder(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		heading = 0;
		npcId = -1;
	}
	
	public LocationHolder(int x, int y, int z, int heading)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		// Generate a random heading if no positive one given.
		heading = (heading < 0) ? Rnd.nextInt(65536) : heading;
		npcId = -1;
	}
	
	public LocationHolder(int x, int y, int z, int heading, int npcId)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		// Generate a random heading if no positive one given.
		heading = (heading < 0) ? Rnd.nextInt(65536) : heading;
		this.npcId = npcId;
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
	
	public int getNpcId()
	{
		return npcId;
	}
	
	public void setX(int x)
	{
		this.x = x;
	}
	
	public void setY(int y)
	{
		this.y = y;
	}
	
	public void setZ(int z)
	{
		this.z = z;
	}
	
	public void setHeading(int heading)
	{
		this.heading = heading;
	}
	
	public void setNpcId(int npcId)
	{
		this.npcId = npcId;
	}
	
	public void setXYZ(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if (object instanceof LocationHolder)
		{
			LocationHolder loc = (LocationHolder) object;
			
			return (loc.getX() == x) && (loc.getY() == y) && (loc.getZ() == z);
		}
		return false;
	}
	
	public boolean equals(int x, int y, int z)
	{
		return (this.x == x) && (this.y == y) && (this.z == z);
	}
}
