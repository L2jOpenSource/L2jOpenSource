package com.l2jfrozen.gameserver.geo.pathfinding.geonodes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.gameserver.geo.pathfinding.Node;
import com.l2jfrozen.gameserver.geo.pathfinding.PathFinding;
import com.l2jfrozen.gameserver.geo.util.L2Arrays;
import com.l2jfrozen.gameserver.geo.util.LookupTable;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.Location;

public final class GeoPathFinding extends PathFinding
{
	protected static final Logger LOGGER = Logger.getLogger(GeoPathFinding.class);
	
	private static final class SingletonHolder
	{
		protected static final GeoPathFinding INSTANCE = new GeoPathFinding();
	}
	
	public static GeoPathFinding getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private final LookupTable<ByteBuffer> pathNodes = new LookupTable<>();
	private final LookupTable<IntBuffer> pathNodesIndex = new LookupTable<>();
	
	private boolean pathNodesExist(final short regionoffset)
	{
		return pathNodesIndex.get(regionoffset) != null;
	}
	
	@Override
	public Node[] findPath(final int x, final int y, final int z, final int tx, final int ty, final int tz)
	{
		final int gx = x - L2World.MAP_MIN_X >> 4;
		final int gy = y - L2World.MAP_MIN_Y >> 4;
		final short gz = (short) z;
		final int gtx = tx - L2World.MAP_MIN_X >> 4;
		final int gty = ty - L2World.MAP_MIN_Y >> 4;
		final short gtz = (short) tz;
		
		final Node start = readNode(gx, gy, gz);
		final Node end = readNode(gtx, gty, gtz);
		if (start == null || end == null)
		{
			return null;
		}
		if (Math.abs(start.getZ() - z) > 55)
		{
			return null; // not correct layer
		}
		if (Math.abs(end.getZ() - tz) > 55)
		{
			return null; // not correct layer
		}
		if (start.equals(end))
		{
			return null;
		}
		// TODO: Find closest path node we CAN access. Now only checks if we can not reach the closest
		Location temp = GeoData.getInstance().moveCheck(x, y, z, start.getX(), start.getY(), start.getZ());
		if (temp.getX() != start.getX() || temp.getY() != start.getY())
		{
			return null; // cannot reach closest...
		}
		
		// TODO: Find closest path node around target, now only checks if final location can be reached
		temp = GeoData.getInstance().moveCheck(tx, ty, tz, end.getX(), end.getY(), end.getZ());
		if (temp.getX() != end.getX() || temp.getY() != end.getY())
		{
			return null; // cannot reach closest...
		}
		
		// return searchAStar(start, end);
		return searchByClosest2(start, end);
	}
	
	@Override
	public Node[] readNeighbors(final Node n, int idx)
	{
		final int node_x = n.getNodeX();
		final int node_y = n.getNodeY();
		// short node_z = n.getZ();
		
		final short regoffset = getRegionOffset(getRegionX(node_x), getRegionY(node_y));
		final ByteBuffer pn = pathNodes.get(regoffset);
		
		final Node[] Neighbors = new Node[8];
		int index = 0;
		Node newNode;
		short new_node_x, new_node_y;
		
		// Region for sure will change, we must read from correct file
		byte neighbor = pn.get(idx++); // N
		if (neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) node_x;
			new_node_y = (short) (node_y - 1);
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if (newNode != null)
			{
				Neighbors[index++] = newNode;
			}
		}
		neighbor = pn.get(idx++); // NE
		if (neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) (node_x + 1);
			new_node_y = (short) (node_y - 1);
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if (newNode != null)
			{
				Neighbors[index++] = newNode;
			}
		}
		neighbor = pn.get(idx++); // E
		if (neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) (node_x + 1);
			new_node_y = (short) node_y;
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if (newNode != null)
			{
				Neighbors[index++] = newNode;
			}
		}
		neighbor = pn.get(idx++); // SE
		if (neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) (node_x + 1);
			new_node_y = (short) (node_y + 1);
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if (newNode != null)
			{
				Neighbors[index++] = newNode;
			}
		}
		neighbor = pn.get(idx++); // S
		if (neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) node_x;
			new_node_y = (short) (node_y + 1);
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if (newNode != null)
			{
				Neighbors[index++] = newNode;
			}
		}
		neighbor = pn.get(idx++); // SW
		if (neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) (node_x - 1);
			new_node_y = (short) (node_y + 1);
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if (newNode != null)
			{
				Neighbors[index++] = newNode;
			}
		}
		neighbor = pn.get(idx++); // W
		if (neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) (node_x - 1);
			new_node_y = (short) node_y;
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if (newNode != null)
			{
				Neighbors[index++] = newNode;
			}
		}
		neighbor = pn.get(idx++); // NW
		if (neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) (node_x - 1);
			new_node_y = (short) (node_y - 1);
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if (newNode != null)
			{
				Neighbors[index++] = newNode;
			}
		}
		return L2Arrays.compact(Neighbors);
	}
	
	// Private
	
	private Node readNode(final short node_x, final short node_y, final byte layer)
	{
		final short regoffset = getRegionOffset(getRegionX(node_x), getRegionY(node_y));
		if (!pathNodesExist(regoffset))
		{
			return null;
		}
		final short nbx = getNodeBlock(node_x);
		final short nby = getNodeBlock(node_y);
		int idx = pathNodesIndex.get(regoffset).get((nby << 8) + nbx);
		final ByteBuffer pn = pathNodes.get(regoffset);
		// reading
		final byte nodes = pn.get(idx);
		idx += layer * 10 + 1;// byte + layer*10byte
		if (nodes < layer)
		{
			LOGGER.warn("SmthWrong!");
		}
		final short node_z = pn.getShort(idx);
		idx += 2;
		return new GeoNode(node_x, node_y, node_z, idx);
	}
	
	private Node readNode(final int gx, final int gy, final short z)
	{
		final short node_x = getNodePos(gx);
		final short node_y = getNodePos(gy);
		final short regoffset = getRegionOffset(getRegionX(node_x), getRegionY(node_y));
		if (!pathNodesExist(regoffset))
		{
			return null;
		}
		final short nbx = getNodeBlock(node_x);
		final short nby = getNodeBlock(node_y);
		int idx = pathNodesIndex.get(regoffset).get((nby << 8) + nbx);
		final ByteBuffer pn = pathNodes.get(regoffset);
		// reading
		byte nodes = pn.get(idx++);
		int idx2 = 0; // create index to nearlest node by z
		short last_z = Short.MIN_VALUE;
		while (nodes > 0)
		{
			final short node_z = pn.getShort(idx);
			if (Math.abs(last_z - z) > Math.abs(node_z - z))
			{
				last_z = node_z;
				idx2 = idx + 2;
			}
			idx += 10; // short + 8 byte
			nodes--;
		}
		return new GeoNode(node_x, node_y, last_z, idx2);
	}
	
	protected GeoPathFinding()
	{
		FileReader reader = null;
		BufferedReader buff = null;
		LineNumberReader lnr = null;
		
		try
		{
			LOGGER.info("PathFinding Engine: - Loading Path Nodes...");
			final File Data = new File(Config.DATAPACK_ROOT + "/data/pathnode/pn_index.txt");
			if (!Data.exists())
			{
				return;
			}
			
			reader = new FileReader(Data);
			buff = new BufferedReader(reader);
			lnr = new LineNumberReader(buff);
			
			String line;
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().length() == 0)
				{
					continue;
				}
				final StringTokenizer st = new StringTokenizer(line, "_");
				final byte rx = Byte.parseByte(st.nextToken());
				final byte ry = Byte.parseByte(st.nextToken());
				LoadPathNodeFile(rx, ry);
			}
			
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (lnr != null)
			{
				try
				{
					lnr.close();
				}
				catch (final Exception e1)
				{
					e1.printStackTrace();
				}
			}
			
			if (buff != null)
			{
				try
				{
					buff.close();
				}
				catch (final Exception e1)
				{
					e1.printStackTrace();
				}
			}
			
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (final Exception e1)
				{
					e1.printStackTrace();
				}
			}
			
		}
		
	}
	
	private void LoadPathNodeFile(final byte rx, final byte ry)
	{
		final String fname = Config.DATAPACK_ROOT + "/data/pathnode/" + rx + "_" + ry + ".pn";
		final short regionoffset = getRegionOffset(rx, ry);
		final File Pn = new File(fname);
		int node = 0, size, index = 0;
		RandomAccessFile raf = null;
		FileChannel roChannel = null;
		try
		{
			// Create a read-only memory-mapped file
			raf = new RandomAccessFile(Pn, "r");
			roChannel = raf.getChannel();
			size = (int) roChannel.size();
			MappedByteBuffer nodes;
			if (Config.FORCE_GEODATA)
			{
				// it is not guarantee, because the underlying operating system may have paged out some of the buffer's data
				nodes = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size).load();
			}
			else
			{
				nodes = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
			}
			
			// Indexing pathnode files, so we will know where each block starts
			final IntBuffer indexs = IntBuffer.allocate(65536);
			
			while (node < 65536)
			{
				final byte layer = nodes.get(index);
				indexs.put(node++, index);
				index += layer * 10 + 1;
			}
			pathNodesIndex.set(regionoffset, indexs);
			pathNodes.set(regionoffset, nodes);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("Failed to Load PathNode File: " + fname + "\n", e);
		}
		finally
		{
			if (roChannel != null)
			{
				try
				{
					roChannel.close();
				}
				catch (final Exception e1)
				{
					e1.printStackTrace();
				}
			}
			
			if (raf != null)
			{
				try
				{
					raf.close();
				}
				catch (final Exception e1)
				{
					e1.printStackTrace();
				}
			}
			
		}
		
	}
}
