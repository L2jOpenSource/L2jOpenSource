package com.l2jfrozen.gameserver.geo.pathfinding;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.gameserver.geo.pathfinding.cellnodes.CellPathFinding;
import com.l2jfrozen.gameserver.geo.pathfinding.geonodes.GeoPathFinding;
import com.l2jfrozen.gameserver.geo.pathfinding.utils.BinaryNodeHeap;
import com.l2jfrozen.gameserver.geo.pathfinding.utils.CellNodeMap;
import com.l2jfrozen.gameserver.geo.util.L2Arrays;
import com.l2jfrozen.gameserver.geo.util.L2Collections;
import com.l2jfrozen.gameserver.geo.util.L2FastSet;
import com.l2jfrozen.gameserver.model.L2World;

/**
 * @author -Nemesiss-
 */
public abstract class PathFinding
{
	public static PathFinding getInstance()
	{
		if (!Config.GEODATA_CELLFINDING)
		{
			return GeoPathFinding.getInstance(); // Higher Memory Usage, Smaller Cpu Usage
		}
		return CellPathFinding.getInstance(); // Cell pathfinding, calculated directly from geodata files
	}
	
	public abstract Node[] findPath(int x, int y, int z, int tx, int ty, int tz);
	
	public abstract Node[] readNeighbors(Node n, int idx);
	
	public final Node[] search(final Node start, final Node end)
	{
		// The simplest grid-based pathfinding.
		// Drawback is not having higher cost for diagonal movement (means funny routes)
		// Could be optimized e.g. not to calculate backwards as far as forwards.
		
		// List of Visited Nodes
		final L2FastSet<Node> visited = L2Collections.newL2FastSet();
		
		// List of Nodes to Visit
		final L2FastSet<Node> to_visit = L2Collections.newL2FastSet();
		to_visit.add(start);
		try
		{
			int i = 0;
			while (i < 800)
			{
				if (to_visit.isEmpty())
				{
					// No Path found
					return null;
				}
				
				final Node node = to_visit.removeFirst();
				
				if (node.equals(end))
				{
					return constructPath(node);
				}
				i++;
				visited.add(node);
				node.attachNeighbors();
				final Node[] neighbors = node.getNeighbors();
				if (neighbors == null)
				{
					continue;
				}
				for (final Node n : neighbors)
				{
					if (!visited.contains(n) && !to_visit.contains(n))
					{
						n.setParent(node);
						to_visit.add(n);
					}
				}
			}
			// No Path found
			return null;
		}
		finally
		{
			L2Collections.recycle(visited);
			L2Collections.recycle(to_visit);
		}
	}
	
	public final Node[] searchByClosest(final Node start, final Node end)
	{
		// Note: This is the version for cell-based calculation, harder
		// on cpu than from block-based pathnode files. However produces better routes.
		
		// Always continues checking from the closest to target non-blocked
		// node from to_visit list. There's extra length in path if needed
		// to go backwards/sideways but when moving generally forwards, this is extra fast
		// and accurate. And can reach insane distances (try it with 8000 nodes..).
		// Minimum required node count would be around 300-400.
		// Generally returns a bit (only a bit) more intelligent looking routes than
		// the basic version. Not a true distance image (which would increase CPU
		// load) level of intelligence though.
		
		// List of Visited Nodes
		final CellNodeMap known = CellNodeMap.newInstance();
		// List of Nodes to Visit
		final ArrayList<Node> to_visit = L2Collections.newArrayList();
		to_visit.add(start);
		known.add(start);
		try
		{
			final int targetx = end.getNodeX();
			final int targety = end.getNodeY();
			final int targetz = end.getZ();
			int dx, dy, dz;
			boolean added;
			int i = 0;
			while (i < 3500)
			{
				if (to_visit.isEmpty())
				{
					// No Path found
					return null;
				}
				
				final Node node = to_visit.remove(0);
				
				i++;
				
				node.attachNeighbors();
				if (node.equals(end))
				{
					// path found! note that node z coordinate is updated only in attach
					// to improve performance (alternative: much more checks)
					// LOGGER.info("path found, i:"+i);
					return constructPath(node);
				}
				
				final Node[] neighbors = node.getNeighbors();
				if (neighbors == null)
				{
					continue;
				}
				for (final Node n : neighbors)
				{
					if (!known.contains(n))
					{
						added = false;
						n.setParent(node);
						dx = targetx - n.getNodeX();
						dy = targety - n.getNodeY();
						dz = targetz - n.getZ();
						n.setCost(dx * dx + dy * dy + dz / 2 * dz/* +n.getCost() */);
						for (int index = 0; index < to_visit.size(); index++)
						{
							// supposed to find it quite early..
							if (to_visit.get(index).getCost() > n.getCost())
							{
								to_visit.add(index, n);
								added = true;
								break;
							}
						}
						if (!added)
						{
							to_visit.add(n);
						}
						known.add(n);
					}
				}
			}
			// No Path found
			// LOGGER.info("no path found");
			return null;
		}
		finally
		{
			CellNodeMap.recycle(known);
			L2Collections.recycle(to_visit);
		}
	}
	
	public final Node[] searchByClosest2(final Node start, final Node end)
	{
		// Always continues checking from the closest to target non-blocked
		// node from to_visit list. There's extra length in path if needed
		// to go backwards/sideways but when moving generally forwards, this is extra fast
		// and accurate. And can reach insane distances (try it with 800 nodes..).
		// Minimum required node count would be around 300-400.
		// Generally returns a bit (only a bit) more intelligent looking routes than
		// the basic version. Not a true distance image (which would increase CPU
		// load) level of intelligence though.
		
		// List of Visited Nodes
		final L2FastSet<Node> visited = L2Collections.newL2FastSet();
		// List of Nodes to Visit
		final ArrayList<Node> to_visit = L2Collections.newArrayList();
		to_visit.add(start);
		try
		{
			final int targetx = end.getNodeX();
			final int targety = end.getNodeY();
			int dx, dy;
			boolean added;
			int i = 0;
			while (i < 550)
			{
				if (to_visit.isEmpty())
				{
					// No Path found
					return null;
				}
				
				final Node node = to_visit.remove(0);
				
				if (node.equals(end)) // path found!
				{
					return constructPath2(node);
				}
				i++;
				visited.add(node);
				node.attachNeighbors();
				final Node[] neighbors = node.getNeighbors();
				if (neighbors == null)
				{
					continue;
				}
				for (final Node n : neighbors)
				{
					if (!visited.contains(n) && !to_visit.contains(n))
					{
						added = false;
						n.setParent(node);
						dx = targetx - n.getNodeX();
						dy = targety - n.getNodeY();
						n.setCost(dx * dx + dy * dy);
						for (int index = 0; index < to_visit.size(); index++)
						{
							// supposed to find it quite early..
							if (to_visit.get(index).getCost() > n.getCost())
							{
								to_visit.add(index, n);
								added = true;
								break;
							}
						}
						if (!added)
						{
							to_visit.add(n);
						}
					}
				}
			}
			// No Path found
			return null;
		}
		finally
		{
			L2Collections.recycle(visited);
			L2Collections.recycle(to_visit);
		}
	}
	
	public final Node[] searchAStar(final Node start, final Node end)
	{
		// Not operational yet?
		final int start_x = start.getX();
		final int start_y = start.getY();
		final int end_x = end.getX();
		final int end_y = end.getY();
		// List of Visited Nodes
		final L2FastSet<Node> visited = L2Collections.newL2FastSet();// TODO! Add limit to cfg
		
		// List of Nodes to Visit
		final BinaryNodeHeap to_visit = BinaryNodeHeap.newInstance();
		to_visit.add(start);
		try
		{
			int i = 0;
			while (i < 800)// TODO! Add limit to cfg
			{
				if (to_visit.isEmpty())
				{
					// No Path found
					return null;
				}
				
				Node node;
				try
				{
					node = to_visit.removeFirst();
				}
				catch (final Exception e)
				{
					// No Path found
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
					return null;
				}
				if (node.equals(end))
				{
					return constructPath(node);
				}
				visited.add(node);
				node.attachNeighbors();
				for (final Node n : node.getNeighbors())
				{
					if (!visited.contains(n) && !to_visit.contains(n))
					{
						i++;
						n.setParent(node);
						n.setCost(Math.abs(start_x - n.getNodeX()) + Math.abs(start_y - n.getNodeY()) + Math.abs(end_x - n.getNodeX()) + Math.abs(end_y - n.getNodeY()));
						to_visit.add(n);
					}
				}
			}
			// No Path found
			return null;
		}
		finally
		{
			L2Collections.recycle(visited);
			BinaryNodeHeap.recycle(to_visit);
		}
	}
	
	public final Node[] constructPath(Node node)
	{
		final ArrayList<Node> tmp = L2Collections.newArrayList();
		
		while (node.getParent() != null)
		{
			tmp.add(node);
			
			node = node.getParent();
		}
		
		final Node[] path = tmp.toArray(new Node[tmp.size()]);
		
		L2Collections.recycle(tmp);
		
		ArrayUtils.reverse(path);
		
		for (int lastValid = 0; lastValid < path.length - 1;)
		{
			final Node lastValidNode = path[lastValid];
			
			int low = lastValid;
			int high = path.length - 1;
			
			while (low < high)
			{
				final int mid = ((low + high) >> 1) + 1;
				final Node midNode = path[mid];
				final int delta = mid - lastValid;
				final int deltaNodeX = Math.abs(midNode.getNodeX() - lastValidNode.getNodeX());
				final int deltaNodeY = Math.abs(midNode.getNodeY() - lastValidNode.getNodeY());
				
				if (delta <= 1)
				{
					low = mid;
				}
				else if (delta % 2 == 0 && deltaNodeX == delta / 2 && deltaNodeY == delta / 2)
				{
					low = mid;
				}
				else if (deltaNodeX == delta || deltaNodeY == delta)
				{
					low = mid;
				}
				else if (GeoData.getInstance().canMoveFromToTarget(lastValidNode.getX(), lastValidNode.getY(), lastValidNode.getZ(), midNode.getX(), midNode.getY(), midNode.getZ()))
				{
					low = mid;
				}
				else
				{
					high = mid - 1;
				}
			}
			
			final int nextValid = low;
			
			for (int i = lastValid + 1; i < nextValid; i++)
			{
				path[i] = null;
			}
			
			lastValid = nextValid;
		}
		
		return L2Arrays.compact(path);
	}
	
	public final Node[] constructPath2(Node node)
	{
		final ArrayList<Node> tmp = L2Collections.newArrayList();
		int previousdirectionx = -1000;
		int previousdirectiony = -1000;
		int directionx;
		int directiony;
		while (node.getParent() != null)
		{
			// only add a new route point if moving direction changes
			directionx = node.getNodeX() - node.getParent().getNodeX();
			directiony = node.getNodeY() - node.getParent().getNodeY();
			if (directionx != previousdirectionx || directiony != previousdirectiony)
			{
				previousdirectionx = directionx;
				previousdirectiony = directiony;
				tmp.add(node);
			}
			node = node.getParent();
		}
		
		final Node[] path = tmp.toArray(new Node[tmp.size()]);
		
		L2Collections.recycle(tmp);
		
		ArrayUtils.reverse(path);
		
		return path;
	}
	
	/**
	 * Convert geodata position to pathnode position
	 * @param  geo_pos
	 * @return         pathnode position
	 */
	public final short getNodePos(final int geo_pos)
	{
		return (short) (geo_pos >> 3); // OK?
	}
	
	/**
	 * Convert node position to pathnode block position
	 * @param  node_pos
	 * @return          pathnode block position (0...255)
	 */
	public final short getNodeBlock(final int node_pos)
	{
		return (short) (node_pos % 256);
	}
	
	public final byte getRegionX(final int node_pos)
	{
		return (byte) ((node_pos >> 8) + 16);
	}
	
	public final byte getRegionY(final int node_pos)
	{
		return (byte) ((node_pos >> 8) + 10);
	}
	
	public final short getRegionOffset(final byte rx, final byte ry)
	{
		return (short) ((rx << 5) + ry);
	}
	
	/**
	 * Convert pathnode x to World x position
	 * @param  node_x
	 * @return
	 */
	public final int calculateWorldX(final short node_x)
	{
		return L2World.MAP_MIN_X + node_x * 128 + 48;
	}
	
	/**
	 * Convert pathnode y to World y position
	 * @param  node_y
	 * @return
	 */
	public final int calculateWorldY(final short node_y)
	{
		return L2World.MAP_MIN_Y + node_y * 128 + 48;
	}
}