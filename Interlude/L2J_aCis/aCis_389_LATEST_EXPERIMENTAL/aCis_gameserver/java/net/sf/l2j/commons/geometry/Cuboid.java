package net.sf.l2j.commons.geometry;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.model.location.Location;

/**
 * @author Hasha
 */
public class Cuboid extends Rectangle
{
	// min and max Z coorinates
	private final int _minZ;
	private final int _maxZ;
	
	/**
	 * Cuboid constructor.
	 * @param x : Bottom left lower X coordinate.
	 * @param y : Bottom left lower Y coordinate.
	 * @param minZ : Minimum Z coordinate.
	 * @param maxZ : Maximum Z coordinate.
	 * @param w : Cuboid width.
	 * @param h : Cuboid height.
	 */
	public Cuboid(int x, int y, int minZ, int maxZ, int w, int h)
	{
		super(x, y, w, h);
		
		_minZ = minZ;
		_maxZ = maxZ;
	}
	
	@Override
	public final double getArea()
	{
		return 2 * (_w * _h + (_w + _h) * (_maxZ - _minZ));
	}
	
	@Override
	public final double getVolume()
	{
		return _w * _h * (_maxZ - _minZ);
	}
	
	@Override
	public boolean isInside(int x, int y, int z)
	{
		if (z < _minZ || z > _maxZ)
			return false;
		
		int d = x - _x;
		if (d < 0 || d > _w)
			return false;
		
		d = y - _y;
		if (d < 0 || d > _h)
			return false;
		
		return true;
	}
	
	@Override
	public Location getRandomLocation()
	{
		// calculate coordinates and return
		return new Location(_x + Rnd.get(_w), _y + Rnd.get(_h), Rnd.get(_minZ, _maxZ));
	}
}