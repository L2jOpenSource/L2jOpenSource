package com.l2jfrozen.gameserver.geo.pathfinding.cellnodes;

import com.l2jfrozen.gameserver.geo.pathfinding.Node;
import com.l2jfrozen.gameserver.model.L2World;

public final class CellNode extends Node
{
	private final int x;
	private final int y;
	private short z;
	
	public CellNode(final int x, final int y, final short z, final int neighborsIdx)
	{
		super(neighborsIdx);
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public int getX()
	{
		return (x << 4) + L2World.MAP_MIN_X;
	}
	
	@Override
	public int getY()
	{
		return (y << 4) + L2World.MAP_MIN_Y;
	}
	
	@Override
	public short getZ()
	{
		return z;
	}
	
	@Override
	public void setZ(final short z)
	{
		this.z = z;
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
