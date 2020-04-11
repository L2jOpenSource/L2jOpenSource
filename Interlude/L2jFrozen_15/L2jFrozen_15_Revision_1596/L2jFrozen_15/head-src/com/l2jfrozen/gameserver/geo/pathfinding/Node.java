package com.l2jfrozen.gameserver.geo.pathfinding;

public abstract class Node
{
	private final int neighborsIdx;
	private Node[] neighbors;
	private Node parent;
	private short cost;
	
	protected Node(final int neighborsIdx)
	{
		this.neighborsIdx = neighborsIdx;
	}
	
	public final void setParent(final Node p)
	{
		parent = p;
	}
	
	public final void setCost(final int cost)
	{
		this.cost = (short) cost;
	}
	
	public final void attachNeighbors()
	{
		neighbors = PathFinding.getInstance().readNeighbors(this, neighborsIdx);
	}
	
	public final Node[] getNeighbors()
	{
		return neighbors;
	}
	
	public final Node getParent()
	{
		return parent;
	}
	
	public final short getCost()
	{
		return cost;
	}
	
	public abstract int getX();
	
	public abstract int getY();
	
	public abstract short getZ();
	
	public abstract void setZ(short z);
	
	public abstract int getNodeX();
	
	public abstract int getNodeY();
	
	@Override
	public final int hashCode()
	{
		return hash((getNodeX() << 20) + (getNodeY() << 8) + getZ());
	}
	
	@Override
	public final boolean equals(final Object obj)
	{
		if (!(obj instanceof Node))
		{
			return false;
		}
		
		final Node n = (Node) obj;
		
		return getNodeX() == n.getNodeX() && getNodeY() == n.getNodeY() && getZ() == n.getZ();
	}
	
	public final int hash(int h)
	{
		h ^= (h >>> 20) ^ (h >>> 12);
		return h ^ (h >>> 7) ^ (h >>> 4);
	}
}
