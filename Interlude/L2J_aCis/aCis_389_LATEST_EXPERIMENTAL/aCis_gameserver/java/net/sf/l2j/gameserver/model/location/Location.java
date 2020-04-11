package net.sf.l2j.gameserver.model.location;

import java.util.Objects;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.StatsSet;

/**
 * A datatype used to retain a 3D (x/y/z) point. It got the capability to be set and cleaned.
 */
public class Location extends Point2D
{
	public static final Location DUMMY_LOC = new Location(0, 0, 0);
	
	protected volatile int _z;
	
	public Location(int x, int y, int z)
	{
		super(x, y);
		
		_z = z;
	}
	
	public Location(Location loc)
	{
		this(loc.getX(), loc.getY(), loc.getZ());
	}
	
	public Location(StatsSet loc)
	{
		this(loc.getInteger("x"), loc.getInteger("y"), loc.getInteger("z"));
	}
	
	@Override
	public Location clone()
	{
		return new Location(_x, _y, _z);
	}
	
	@Override
	public String toString()
	{
		return super.toString() + ", " + _z;
	}
	
	@Override
	public int hashCode()
	{
		return 31 * super.hashCode() + Objects.hash(_z);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		
		if (!super.equals(obj))
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		final Location other = (Location) obj;
		return _z == other._z;
	}
	
	@Override
	public void clean()
	{
		super.clean();
		
		_z = 0;
	}
	
	/**
	 * @param x : The X coord to test.
	 * @param y : The Y coord to test.
	 * @param z : The Z coord to test.
	 * @return True if all coordinates equals this {@link Location} coordinates.
	 */
	public boolean equals(int x, int y, int z)
	{
		return super.equals(x, y) && _z == z;
	}
	
	public int getZ()
	{
		return _z;
	}
	
	public void setZ(int z)
	{
		_z = z;
	}
	
	public void set(int x, int y, int z)
	{
		super.set(x, y);
		
		_z = z;
	}
	
	public void set(Location loc)
	{
		set(loc.getX(), loc.getY(), loc.getZ());
	}
	
	/**
	 * Set the current {@link Location} using another X/Y {@link Location} and a positive offset.<br>
	 * <br>
	 * This method is mostly used for servitor spawns.
	 * @param loc : The default {@link Location} used as reference.
	 * @param offset : The offset used to impact X and Y.
	 */
	public void setUsingPositiveOffset(Location loc, int offset)
	{
		_x = loc.getX() + Rnd.get(offset);
		_y = loc.getY() + Rnd.get(offset);
		_z = loc.getZ();
	}
	
	/**
	 * Set the current {@link Location} using another X/Y {@link Location} and a random offset.
	 * @param loc : The {@link Location} used as reference.
	 * @param offset : The offset used to impact X and Y.
	 */
	public void setUsingRandomOffset(Location loc, int offset)
	{
		_x = loc.getX() + ((Rnd.nextBoolean()) ? -Rnd.get(offset) : Rnd.get(offset));
		_y = loc.getY() + ((Rnd.nextBoolean()) ? -Rnd.get(offset) : Rnd.get(offset));
		_z = loc.getZ();
	}
	
	/**
	 * @param x : The X position to test.
	 * @param y : The Y position to test.
	 * @param z : The Z position to test.
	 * @return The distance between this {@link Location} and some given coordinates.
	 */
	public double distance3D(int x, int y, int z)
	{
		double dx = (double) _x - x;
		double dy = (double) _y - y;
		double dz = (double) _z - z;
		
		return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
	}
	
	/**
	 * @param loc : The {@link Location} to test.
	 * @return The distance between this {@Location} and the {@link Location} set as parameter.
	 */
	public double distance3D(Location loc)
	{
		return distance3D(loc.getX(), loc.getY(), loc.getZ());
	}
	
	/**
	 * @param x : The X position to test.
	 * @param y : The Y position to test.
	 * @param z : The Z position to test.
	 * @param radius : The radius to check.
	 * @return True if this {@link Location} is in the radius of some given coordinates.
	 */
	public boolean isIn3DRadius(int x, int y, int z, int radius)
	{
		return distance3D(x, y, z) < radius;
	}
	
	/**
	 * @param point : The {@link Location} to test.
	 * @param radius : The radius to check.
	 * @return True if this {@link Location} is in the radius of the {@link Location} set as parameter.
	 */
	public boolean isIn3DRadius(Location point, int radius)
	{
		return distance3D(point) < radius;
	}
}