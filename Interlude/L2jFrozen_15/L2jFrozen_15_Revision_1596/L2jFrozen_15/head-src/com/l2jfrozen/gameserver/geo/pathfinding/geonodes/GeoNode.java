package com.l2jfrozen.gameserver.geo.pathfinding.geonodes;

import com.l2jfrozen.gameserver.geo.pathfinding.Node;
import com.l2jfrozen.gameserver.model.L2World;

public final class GeoNode extends Node
{
	private final short x;
	private final short y;
	private final short z;
	
	public GeoNode(final short x, final short y, final short z, final int neighborsIdx)
	{
		super(neighborsIdx);
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public int getX()
	{
		return L2World.MAP_MIN_X + x * 128 + 48;
	}
	
	@Override
	public int getY()
	{
		return L2World.MAP_MIN_Y + y * 128 + 48;
	}
	
	@Override
	public short getZ()
	{
		return z;
	}
	
	@Override
	public void setZ(final short z)
	{
		//
	}
	
	@Override
	public int getNodeX()
	{
		return x;
	}
	
	@Override
	public int getNodeY()
	{
		return y;
	}
}
