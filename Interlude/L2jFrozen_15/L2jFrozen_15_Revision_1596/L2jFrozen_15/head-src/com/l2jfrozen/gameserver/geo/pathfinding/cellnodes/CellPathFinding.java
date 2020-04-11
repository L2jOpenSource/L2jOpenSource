package com.l2jfrozen.gameserver.geo.pathfinding.cellnodes;

import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.gameserver.geo.pathfinding.Node;
import com.l2jfrozen.gameserver.geo.pathfinding.PathFinding;
import com.l2jfrozen.gameserver.model.L2World;

public final class CellPathFinding extends PathFinding
{
	private static final class SingletonHolder
	{
		protected static final CellPathFinding INSTANCE = new CellPathFinding();
	}
	
	public static CellPathFinding getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	@Override
	public Node[] findPath(final int x, final int y, final int z, final int tx, final int ty, final int tz)
	{
		final int gx = x - L2World.MAP_MIN_X >> 4;
		final int gy = y - L2World.MAP_MIN_Y >> 4;
		if (!GeoData.getInstance().hasGeo(x, y))
		{
			return null;
		}
		final short gz = GeoData.getInstance().getHeight(x, y, z);
		final int gtx = tx - L2World.MAP_MIN_X >> 4;
		final int gty = ty - L2World.MAP_MIN_Y >> 4;
		if (!GeoData.getInstance().hasGeo(tx, ty))
		{
			return null;
		}
		final short gtz = GeoData.getInstance().getHeight(tx, ty, tz);
		final Node start = readNode(gx, gy, gz);
		final Node end = readNode(gtx, gty, gtz);
		return searchByClosest(start, end);
	}
	
	@Override
	public Node[] readNeighbors(final Node n, final int idx)
	{
		return GeoData.getInstance().getNeighbors(n);
	}
	
	// Private
	
	public Node readNode(final int gx, final int gy, final short z)
	{
		return new CellNode(gx, gy, z, 0);
	}
	
	protected CellPathFinding()
	{
		//
	}
}
