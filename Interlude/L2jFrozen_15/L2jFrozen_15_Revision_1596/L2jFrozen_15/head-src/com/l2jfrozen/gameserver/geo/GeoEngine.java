package com.l2jfrozen.gameserver.geo;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.StringTokenizer;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.csv.DoorTable;
import com.l2jfrozen.gameserver.geo.pathfinding.Node;
import com.l2jfrozen.gameserver.geo.pathfinding.cellnodes.CellPathFinding;
import com.l2jfrozen.gameserver.geo.util.L2Arrays;
import com.l2jfrozen.gameserver.geo.util.LookupTable;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SiegeGuardInstance;
import com.l2jfrozen.util.Point3D;

import main.data.memory.ObjectData;
import main.holders.objects.ObjectHolder;

public final class GeoEngine extends GeoData
{
	private final static byte e = 1;
	private final static byte w = 2;
	private final static byte s = 4;
	private final static byte n = 8;
	
	private static final class SingletonHolder
	{
		protected static final GeoEngine INSTANCE = new GeoEngine();
	}
	
	public static GeoEngine getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private final LookupTable<MappedByteBuffer> geodata = new LookupTable<>();
	private final LookupTable<IntBuffer> geodataIndex = new LookupTable<>();
	private BufferedOutputStream geoBugsOut;
	
	protected GeoEngine()
	{
		nInitGeodata();
	}
	
	@Override
	public short getType(final int x, final int y)
	{
		return nGetType(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4);
	}
	
	@Override
	public short getHeight(final int x, final int y, final int z)
	{
		return nGetHeight(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, z);
	}
	
	@Override
	public short getSpawnHeight(final int x, final int y, final int zmin, final int zmax, final int spawnid)
	{
		return nGetSpawnHeight(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, zmin, zmax, spawnid);
	}
	
	@Override
	public String geoPosition(final int x, final int y)
	{
		final int gx = x - L2World.MAP_MIN_X >> 4;
		final int gy = y - L2World.MAP_MIN_Y >> 4;
		return "bx: " + getBlock(gx) + " by: " + getBlock(gy) + " cx: " + getCell(gx) + " cy: " + getCell(gy) + "  region offset: " + getRegionOffset(gx, gy);
	}
	
	@Override
	public boolean canSeeTarget(final L2Object cha, final Point3D target)
	{
		if (DoorTable.getInstance().checkIfDoorsBetween(cha.getX(), cha.getY(), cha.getZ(), target.getX(), target.getY(), target.getZ()))
		{
			return false;
		}
		if (cha.getZ() >= target.getZ())
		{
			return canSeeTarget(cha.getX(), cha.getY(), cha.getZ(), target.getX(), target.getY(), target.getZ());
		}
		return canSeeTarget(target.getX(), target.getY(), target.getZ(), cha.getX(), cha.getY(), cha.getZ());
	}
	
	@Override
	public boolean canSeeTarget(final L2Object cha, final L2Object target)
	{
		ObjectHolder oh = ObjectData.get(ObjectHolder.class, cha);
		if (oh != null && oh.isDifferentWorld(target))
		{
			return false;
		}
		
		// To be able to see over fences and give the player the viewpoint
		// game client has, all coordinates are lifted 45 from ground.
		// Because of layer selection in LOS algorithm (it selects -45 there
		// and some layers can be very close...) do not change this without
		// changing the LOS code.
		// Basically the +45 is character height. Raid bosses are naturally higher,
		// dwarves shorter, but this should work relatively well.
		// If this is going to be improved, use e.g.
		// ((L2Character)cha).getTemplate().collisionHeight
		int z = cha.getZ() + 45;
		if (cha instanceof L2SiegeGuardInstance)
		{
			z += 30; // well they don't move closer to balcony fence at the moment :(
		}
		int z2 = target.getZ() + 45;
		if (target instanceof L2DoorInstance)
		{
			return true; // door coordinates are hinge coords..
		}
		if (DoorTable.getInstance().checkIfDoorsBetween(cha.getX(), cha.getY(), z, target.getX(), target.getY(), z2))
		{
			return false;
		}
		if (target instanceof L2SiegeGuardInstance)
		{
			z2 += 30; // well they don't move closer to balcony fence at the moment :(
		}
		if (z >= z2)
		{
			return canSeeTarget(cha.getX(), cha.getY(), z, target.getX(), target.getY(), z2);
		}
		return canSeeTarget(target.getX(), target.getY(), z2, cha.getX(), cha.getY(), z);
	}
	
	@Override
	public boolean canSeeTargetDebug(final L2PcInstance gm, final L2Object target)
	{
		// comments: see above
		final int z = gm.getZ() + 45;
		final int z2 = target.getZ() + 45;
		if (target instanceof L2DoorInstance)
		{
			gm.sendMessage("door always true");
			return true; // door coordinates are hinge coords..
		}
		
		if (z >= z2)
		{
			return canSeeDebug(gm, gm.getX() - L2World.MAP_MIN_X >> 4, gm.getY() - L2World.MAP_MIN_Y >> 4, z, target.getX() - L2World.MAP_MIN_X >> 4, target.getY() - L2World.MAP_MIN_Y >> 4, z2);
		}
		return canSeeDebug(gm, target.getX() - L2World.MAP_MIN_X >> 4, target.getY() - L2World.MAP_MIN_Y >> 4, z2, gm.getX() - L2World.MAP_MIN_X >> 4, gm.getY() - L2World.MAP_MIN_Y >> 4, z);
	}
	
	@Override
	public short getNSWE(final int x, final int y, final int z)
	{
		return nGetNSWE(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, z);
	}
	
	@Override
	public boolean canMoveFromToTarget(final int x, final int y, final int z, final int tx, final int ty, final int tz)
	{
		final Location destiny = moveCheck(x, y, z, tx, ty, tz);
		return destiny.getX() == tx && destiny.getY() == ty && destiny.getZ() == tz;
	}
	
	@Override
	public Location moveCheck(final int x, final int y, final int z, final int tx, final int ty, final int tz)
	{
		final Location startpoint = new Location(x, y, z);
		if (DoorTable.getInstance().checkIfDoorsBetween(x, y, z, tx, ty, tz))
		{
			return startpoint;
		}
		
		final Location destiny = new Location(tx, ty, tz);
		return moveCheck(startpoint, destiny, x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, z, tx - L2World.MAP_MIN_X >> 4, ty - L2World.MAP_MIN_Y >> 4, tz);
	}
	
	@Override
	public void addGeoDataBug(final L2PcInstance gm, final String comment)
	{
		final int gx = gm.getX() - L2World.MAP_MIN_X >> 4;
		final int gy = gm.getY() - L2World.MAP_MIN_Y >> 4;
		final int bx = getBlock(gx);
		final int by = getBlock(gy);
		final int cx = getCell(gx);
		final int cy = getCell(gy);
		final int rx = (gx >> 11) + 10;
		final int ry = (gy >> 11) + 10;
		final String out = rx + ";" + ry + ";" + bx + ";" + by + ";" + cx + ";" + cy + ";" + gm.getZ() + ";" + comment + "\n";
		try
		{
			geoBugsOut.write(out.getBytes());
			geoBugsOut.flush();
			gm.sendMessage("GeoData bug saved!");
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			gm.sendMessage("GeoData bug save Failed!");
		}
	}
	
	@Override
	public boolean canSeeTarget(final int x, final int y, final int z, final int tx, final int ty, final int tz)
	{
		return canSee(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, z, tx - L2World.MAP_MIN_X >> 4, ty - L2World.MAP_MIN_Y >> 4, tz);
	}
	
	@Override
	public boolean hasGeo(final int x, final int y)
	{
		final int gx = x - L2World.MAP_MIN_X >> 4;
		final int gy = y - L2World.MAP_MIN_Y >> 4;
		final short region = getRegionOffset(gx, gy);
		
		return geodata.get(region) != null;
	}
	
	private boolean canSee(int x, int y, double z, final int tx, final int ty, final int tz)
	{
		int dx = tx - x;
		int dy = ty - y;
		final double dz = tz - z;
		final int distance2 = dx * dx + dy * dy;
		
		if (distance2 > 90000) // (300*300) 300*16 = 4800 in world coord
		{
			// Avoid too long check
			return false;
		}
		// very short checks: 9 => 144 world distance
		// this ensures NLOS function has enough points to calculate,
		// it might not work when distance is small and path vertical
		else if (distance2 < 82)
		{
			// 150 should be too deep/high.
			if (dz * dz > 22500)
			{
				final short region = getRegionOffset(x, y);
				// geodata is loaded for region and mobs should have correct Z coordinate...
				// so there would likely be a floor in between the two
				if (geodata.get(region) != null)
				{
					return false;
				}
			}
			return true;
		}
		
		// Increment in Z coordinate when moving along X or Y axis
		// and not straight to the target. This is done because
		// calculation moves either in X or Y direction.
		final int inc_x = sign(dx);
		final int inc_y = sign(dy);
		dx = Math.abs(dx);
		dy = Math.abs(dy);
		final double inc_z_directionx = dz * dx / distance2;
		final double inc_z_directiony = dz * dy / distance2;
		
		// next_* are used in NLOS check from x,y
		int next_x = x;
		int next_y = y;
		
		// creates path to the target
		// calculation stops when next_* == target
		if (dx >= dy)// dy/dx <= 1
		{
			final int delta_A = 2 * dy;
			int d = delta_A - dx;
			final int delta_B = delta_A - 2 * dx;
			
			for (int i = 0; i < dx; i++)
			{
				x = next_x;
				y = next_y;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					z += inc_z_directionx;
					if (!nLOS(x, y, (int) z, inc_x, 0, inc_z_directionx, tz, false))
					{
						return false;
					}
					next_y += inc_y;
					z += inc_z_directiony;
					// LOGGER.warn("1: next_x:"+next_x+" next_y"+next_y);
					if (!nLOS(next_x, y, (int) z, 0, inc_y, inc_z_directiony, tz, false))
					{
						return false;
					}
				}
				else
				{
					d += delta_A;
					next_x += inc_x;
					// LOGGER.warn("2: next_x:"+next_x+" next_y"+next_y);
					z += inc_z_directionx;
					if (!nLOS(x, y, (int) z, inc_x, 0, inc_z_directionx, tz, false))
					{
						return false;
					}
				}
			}
		}
		else
		{
			final int delta_A = 2 * dx;
			int d = delta_A - dy;
			final int delta_B = delta_A - 2 * dy;
			for (int i = 0; i < dy; i++)
			{
				x = next_x;
				y = next_y;
				if (d > 0)
				{
					d += delta_B;
					next_y += inc_y;
					z += inc_z_directiony;
					if (!nLOS(x, y, (int) z, 0, inc_y, inc_z_directiony, tz, false))
					{
						return false;
					}
					next_x += inc_x;
					z += inc_z_directionx;
					// LOGGER.warn("3: next_x:"+next_x+" next_y"+next_y);
					if (!nLOS(x, next_y, (int) z, inc_x, 0, inc_z_directionx, tz, false))
					{
						return false;
					}
				}
				else
				{
					d += delta_A;
					next_y += inc_y;
					// LOGGER.warn("4: next_x:"+next_x+" next_y"+next_y);
					z += inc_z_directiony;
					if (!nLOS(x, y, (int) z, 0, inc_y, inc_z_directiony, tz, false))
					{
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/*
	 * Debug function for checking if there's a line of sight between two coordinates. Creates points for line of sight check (x,y,z towards target) and in each point, layer and movement checks are made with NLOS function. Coordinates here are geodata x,y but z coordinate is world coordinate
	 */
	private boolean canSeeDebug(final L2PcInstance gm, int x, int y, double z, final int tx, final int ty, final int tz)
	{
		int dx = tx - x;
		int dy = ty - y;
		final double dz = tz - z;
		final int distance2 = dx * dx + dy * dy;
		
		if (distance2 > 90000) // (300*300) 300*16 = 4800 in world coord
		{
			// Avoid too long check
			gm.sendMessage("dist > 300");
			return false;
		}
		// very short checks: 9 => 144 world distance
		// this ensures NLOS function has enough points to calculate,
		// it might not work when distance is small and path vertical
		else if (distance2 < 82)
		{
			// 150 should be too deep/high.
			if (dz * dz > 22500)
			{
				final short region = getRegionOffset(x, y);
				// geodata is loaded for region and mobs should have correct Z coordinate...
				// so there would likely be a floor in between the two
				if (geodata.get(region) != null)
				{
					return false;
				}
			}
			return true;
		}
		
		// Increment in Z coordinate when moving along X or Y axis
		// and not straight to the target. This is done because
		// calculation moves either in X or Y direction.
		final int inc_x = sign(dx);
		final int inc_y = sign(dy);
		dx = Math.abs(dx);
		dy = Math.abs(dy);
		final double inc_z_directionx = dz * dx / distance2;
		final double inc_z_directiony = dz * dy / distance2;
		
		gm.sendMessage("Los: from X: " + x + "Y: " + y + "--->> X: " + tx + " Y: " + ty);
		
		// next_* are used in NLOS check from x,y
		int next_x = x;
		int next_y = y;
		
		// creates path to the target
		// calculation stops when next_* == target
		if (dx >= dy)// dy/dx <= 1
		{
			final int delta_A = 2 * dy;
			int d = delta_A - dx;
			final int delta_B = delta_A - 2 * dx;
			
			for (int i = 0; i < dx; i++)
			{
				x = next_x;
				y = next_y;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					z += inc_z_directionx;
					if (!nLOS(x, y, (int) z, inc_x, 0, inc_z_directionx, tz, true))
					{
						return false;
					}
					next_y += inc_y;
					z += inc_z_directiony;
					// LOGGER.warn( "1: next_x:"+next_x+" next_y"+next_y);
					if (!nLOS(next_x, y, (int) z, 0, inc_y, inc_z_directiony, tz, true))
					{
						return false;
					}
				}
				else
				{
					d += delta_A;
					next_x += inc_x;
					// LOGGER.warn( "2: next_x:"+next_x+" next_y"+next_y);
					z += inc_z_directionx;
					if (!nLOS(x, y, (int) z, inc_x, 0, inc_z_directionx, tz, true))
					{
						return false;
					}
				}
			}
		}
		else
		{
			final int delta_A = 2 * dx;
			int d = delta_A - dy;
			final int delta_B = delta_A - 2 * dy;
			for (int i = 0; i < dy; i++)
			{
				x = next_x;
				y = next_y;
				if (d > 0)
				{
					d += delta_B;
					next_y += inc_y;
					z += inc_z_directiony;
					if (!nLOS(x, y, (int) z, 0, inc_y, inc_z_directiony, tz, true))
					{
						return false;
					}
					next_x += inc_x;
					z += inc_z_directionx;
					// LOGGER.warn( "3: next_x:"+next_x+" next_y"+next_y);
					if (!nLOS(x, next_y, (int) z, inc_x, 0, inc_z_directionx, tz, true))
					{
						return false;
					}
				}
				else
				{
					d += delta_A;
					next_y += inc_y;
					// LOGGER.warn( "4: next_x:"+next_x+" next_y"+next_y);
					z += inc_z_directiony;
					if (!nLOS(x, y, (int) z, 0, inc_y, inc_z_directiony, tz, true))
					{
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/*
	 * MoveCheck
	 */
	private Location moveCheck(final Location startpoint, final Location destiny, int x, int y, double z, int tx, int ty, int tz)
	{
		int dx = tx - x;
		int dy = ty - y;
		final int distance2 = dx * dx + dy * dy;
		
		if (distance2 == 0)
		{
			return destiny;
		}
		if (distance2 > 36100) // 190*190*16 = 3040 world coord
		{
			// Avoid too long check
			// Currently we calculate a middle point
			// for wyvern users and otherwise for comfort
			final double divider = Math.sqrt((double) 30000 / distance2);
			tx = x + (int) (divider * dx);
			ty = y + (int) (divider * dy);
			final int dz = tz - startpoint.getZ();
			tz = startpoint.getZ() + (int) (divider * dz);
			dx = tx - x;
			dy = ty - y;
			// return startpoint;
		}
		
		// Increment in Z coordinate when moving along X or Y axis
		// and not straight to the target. This is done because
		// calculation moves either in X or Y direction.
		final int inc_x = sign(dx);
		final int inc_y = sign(dy);
		dx = Math.abs(dx);
		dy = Math.abs(dy);
		
		// gm.sendMessage("MoveCheck: from X: "+x+ "Y: "+y+ "--->> X: "+tx+" Y: "+ty);
		
		// next_* are used in NcanMoveNext check from x,y
		int next_x = x;
		int next_y = y;
		double tempz = z;
		
		// creates path to the target, using only x or y direction
		// calculation stops when next_* == target
		if (dx >= dy)// dy/dx <= 1
		{
			final int delta_A = 2 * dy;
			int d = delta_A - dx;
			final int delta_B = delta_A - 2 * dx;
			
			for (int i = 0; i < dx; i++)
			{
				x = next_x;
				y = next_y;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					tempz = nCanMoveNext(x, y, (int) z, next_x, next_y, tz);
					if (tempz == Double.MIN_VALUE)
					{
						return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, (int) z);
					}
					z = tempz;
					next_y += inc_y;
					// LOGGER.warn("2: next_x:"+next_x+" next_y"+next_y);
					tempz = nCanMoveNext(next_x, y, (int) z, next_x, next_y, tz);
					if (tempz == Double.MIN_VALUE)
					{
						return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, (int) z);
					}
					z = tempz;
				}
				else
				{
					d += delta_A;
					next_x += inc_x;
					// LOGGER.warn("3: next_x:"+next_x+" next_y"+next_y);
					tempz = nCanMoveNext(x, y, (int) z, next_x, next_y, tz);
					if (tempz == Double.MIN_VALUE)
					{
						return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, (int) z);
					}
					z = tempz;
				}
			}
		}
		else
		{
			final int delta_A = 2 * dx;
			int d = delta_A - dy;
			final int delta_B = delta_A - 2 * dy;
			for (int i = 0; i < dy; i++)
			{
				x = next_x;
				y = next_y;
				if (d > 0)
				{
					d += delta_B;
					next_y += inc_y;
					tempz = nCanMoveNext(x, y, (int) z, next_x, next_y, tz);
					if (tempz == Double.MIN_VALUE)
					{
						return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, (int) z);
					}
					z = tempz;
					next_x += inc_x;
					// LOGGER.warn("5: next_x:"+next_x+" next_y"+next_y);
					tempz = nCanMoveNext(x, next_y, (int) z, next_x, next_y, tz);
					if (tempz == Double.MIN_VALUE)
					{
						return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, (int) z);
					}
					z = tempz;
				}
				else
				{
					d += delta_A;
					next_y += inc_y;
					// LOGGER.warn("6: next_x:"+next_x+" next_y"+next_y);
					tempz = nCanMoveNext(x, y, (int) z, next_x, next_y, tz);
					if (tempz == Double.MIN_VALUE)
					{
						return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, (int) z);
					}
					z = tempz;
				}
			}
		}
		if (z == startpoint.getZ())
		{
			return destiny;
		}
		return new Location(destiny.getX(), destiny.getY(), (int) z);
	}
	
	private byte sign(final int x)
	{
		if (x >= 0)
		{
			return +1;
		}
		return -1;
	}
	
	// GeoEngine
	private void nInitGeodata()
	{
		LOGGER.info("Geo Engine: - Loading Geodata...");
		final File Data = new File(Config.DATAPACK_ROOT, "data/geodata/geo_index.txt");
		if (!Data.exists())
		{
			return;
		}
		
		BufferedReader lnr = null;
		FileReader reader = null;
		try
		{
			reader = new FileReader(Data);
			lnr = new BufferedReader(reader);
			
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
				loadGeodataFile(rx, ry);
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
				catch (final Exception e)
				{
					e.printStackTrace();
				}
			}
			
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (final Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		final File geo_bugs = new File(Config.DATAPACK_ROOT, "data/geodata/geo_bugs.txt");
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(geo_bugs, true);
			geoBugsOut = new BufferedOutputStream(out);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			
		}
		
	}
	
	@Override
	public void unloadGeodata(final byte rx, final byte ry)
	{
		final short regionoffset = (short) ((rx << 5) + ry);
		geodataIndex.set(regionoffset, null);
		geodata.set(regionoffset, null);
	}
	
	@Override
	public boolean loadGeodataFile(final byte rx, final byte ry)
	{
		boolean output = false;
		final String fname = "data/geodata/" + rx + "_" + ry + ".l2j";
		final short regionoffset = (short) ((rx << 5) + ry);
		final File Geo = new File(Config.DATAPACK_ROOT, fname);
		int size, index = 0, block = 0, flor = 0;
		FileChannel roChannel = null;
		RandomAccessFile raf = null;
		try
		{
			// Create a read-only memory-mapped file
			raf = new RandomAccessFile(Geo, "r");
			roChannel = raf.getChannel();
			size = (int) roChannel.size();
			MappedByteBuffer geo;
			if (Config.FORCE_GEODATA)
			{
				// it is not guarantee, because the underlying operating system may have paged out some of the buffer's data
				geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size).load();
			}
			else
			{
				geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
			}
			geo.order(ByteOrder.LITTLE_ENDIAN);
			
			if (size > 196608)
			{
				// Indexing geo files, so we will know where each block starts
				final IntBuffer indexs = IntBuffer.allocate(65536);
				while (block < 65536)
				{
					final byte type = geo.get(index);
					indexs.put(block, index);
					block++;
					index++;
					if (type == 0)
					{
						index += 2; // 1x short
					}
					else if (type == 1)
					{
						index += 128; // 64 x short
					}
					else
					{
						for (int b = 0; b < 64; b++)
						{
							final byte layers = geo.get(index);
							index += (layers << 1) + 1;
							if (layers > flor)
							{
								flor = layers;
							}
						}
					}
				}
				geodataIndex.set(regionoffset, indexs);
			}
			geodata.set(regionoffset, geo);
			
			output = true;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			LOGGER.warn("Failed to Load GeoFile at block: " + block + "\n");
			
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
		return output;
	}
	
	// Geodata Methods
	/**
	 * @param  x
	 * @param  y
	 * @return   Region Offset
	 */
	private short getRegionOffset(final int x, final int y)
	{
		final int rx = x >> 11; // =/(256 * 8)
		final int ry = y >> 11;
		return (short) ((rx + 16 << 5) + ry + 10);
	}
	
	/**
	 * @param  geo_pos
	 * @return         Block Index: 0-255
	 */
	private int getBlock(final int geo_pos)
	{
		return (geo_pos >> 3) % 256;
	}
	
	/**
	 * @param  geo_pos
	 * @return         Cell Index: 0-7
	 */
	private int getCell(final int geo_pos)
	{
		return geo_pos % 8;
	}
	
	// Geodata Functions
	
	/**
	 * @param  x
	 * @param  y
	 * @return   Type of geo_block: 0-2
	 */
	private short nGetType(final int x, final int y)
	{
		final short region = getRegionOffset(x, y);
		final int blockX = getBlock(x);
		final int blockY = getBlock(y);
		int index = 0;
		// Geodata without index - it is just empty so index can be calculated on the fly
		if (geodataIndex.get(region) == null)
		{
			index = ((blockX << 8) + blockY) * 3;
			// Get Index for current block of current geodata region
		}
		else
		{
			index = geodataIndex.get(region).get((blockX << 8) + blockY);
		}
		// Buffer that Contains current Region GeoData
		final ByteBuffer geo = geodata.get(region);
		if (geo == null)
		{
			if (Config.DEBUG)
			{
				LOGGER.warn("Geo Region - Region Offset: " + region + " dosnt exist!!");
			}
			return 0;
		}
		return geo.get(index);
	}
	
	/**
	 * @param  geox
	 * @param  geoy
	 * @param  z
	 * @return      Nearest Z
	 */
	private short nGetHeight(final int geox, final int geoy, final int z)
	{
		final short region = getRegionOffset(geox, geoy);
		final int blockX = getBlock(geox);
		final int blockY = getBlock(geoy);
		int cellX, cellY, index;
		// Geodata without index - it is just empty so index can be calculated on the fly
		if (geodataIndex.get(region) == null)
		{
			index = ((blockX << 8) + blockY) * 3;
			// Get Index for current block of current region geodata
		}
		else
		{
			index = geodataIndex.get(region).get((blockX << 8) + blockY);
		}
		// Buffer that Contains current Region GeoData
		final ByteBuffer geo = geodata.get(region);
		if (geo == null)
		{
			if (Config.DEBUG)
			{
				LOGGER.warn("Geo Region - Region Offset: " + region + " dosnt exist!!");
			}
			return (short) z;
		}
		// Read current block type: 0-flat,1-complex,2-multilevel
		final byte type = geo.get(index);
		index++;
		if (type == 0)
		{
			return geo.getShort(index);
		}
		else if (type == 1)// complex
		{
			cellX = getCell(geox);
			cellY = getCell(geoy);
			index += (cellX << 3) + cellY << 1;
			short height = geo.getShort(index);
			height = (short) (height & 0x0fff0);
			height = (short) (height >> 1); // height / 2
			return height;
		}
		else
		// multilevel
		{
			cellX = getCell(geox);
			cellY = getCell(geoy);
			int offset = (cellX << 3) + cellY;
			while (offset > 0)
			{
				final byte lc = geo.get(index);
				index += (lc << 1) + 1;
				offset--;
			}
			byte layers = geo.get(index);
			index++;
			short height = -1;
			if (layers <= 0 || layers > 125)
			{
				if (Config.DEBUG)
				{
					LOGGER.warn("Broken geofile (case1), region: " + region + " - invalid layer count: " + layers + " at: " + geox + " " + geoy);
				}
				return (short) z;
			}
			short temph = Short.MIN_VALUE;
			while (layers > 0)
			{
				height = geo.getShort(index);
				height = (short) (height & 0x0fff0);
				height = (short) (height >> 1); // height / 2
				if ((z - temph) * (z - temph) > (z - height) * (z - height))
				{
					temph = height;
				}
				layers--;
				index += 2;
			}
			return temph;
		}
	}
	
	/**
	 * @param  geox
	 * @param  geoy
	 * @param  z
	 * @return      One layer higher Z than parameter Z
	 */
	private short nGetUpperHeight(final int geox, final int geoy, final int z)
	{
		final short region = getRegionOffset(geox, geoy);
		final int blockX = getBlock(geox);
		final int blockY = getBlock(geoy);
		int cellX, cellY, index;
		// Geodata without index - it is just empty so index can be calculated on the fly
		if (geodataIndex.get(region) == null)
		{
			index = ((blockX << 8) + blockY) * 3;
			// Get Index for current block of current region geodata
		}
		else
		{
			index = geodataIndex.get(region).get((blockX << 8) + blockY);
		}
		// Buffer that Contains current Region GeoData
		final ByteBuffer geo = geodata.get(region);
		if (geo == null)
		{
			if (Config.DEBUG)
			{
				LOGGER.warn("Geo Region - Region Offset: " + region + " dosnt exist!!");
			}
			return (short) z;
		}
		// Read current block type: 0-flat,1-complex,2-multilevel
		final byte type = geo.get(index);
		index++;
		if (type == 0)
		{
			return geo.getShort(index);
		}
		else if (type == 1)// complex
		{
			cellX = getCell(geox);
			cellY = getCell(geoy);
			index += (cellX << 3) + cellY << 1;
			short height = geo.getShort(index);
			height = (short) (height & 0x0fff0);
			height = (short) (height >> 1); // height / 2
			return height;
		}
		else
		// multilevel
		{
			cellX = getCell(geox);
			cellY = getCell(geoy);
			int offset = (cellX << 3) + cellY;
			while (offset > 0)
			{
				final byte lc = geo.get(index);
				index += (lc << 1) + 1;
				offset--;
			}
			byte layers = geo.get(index);
			index++;
			short height = -1;
			if (layers <= 0 || layers > 125)
			{
				if (Config.DEBUG)
				{
					LOGGER.warn("Broken geofile (case1), region: " + region + " - invalid layer count: " + layers + " at: " + geox + " " + geoy);
				}
				return (short) z;
			}
			short temph = Short.MAX_VALUE;
			while (layers > 0) // from higher to lower
			{
				height = geo.getShort(index);
				height = (short) (height & 0x0fff0);
				height = (short) (height >> 1); // height / 2
				if (height < z)
				{
					return temph;
				}
				temph = height;
				layers--;
				index += 2;
			}
			return temph;
		}
	}
	
	/**
	 * @param  geox
	 * @param  geoy
	 * @param  zmin
	 * @param  zmax
	 * @param  spawnid
	 * @return         Z betwen zmin and zmax
	 */
	private short nGetSpawnHeight(final int geox, final int geoy, final int zmin, final int zmax, final int spawnid)
	{
		final short region = getRegionOffset(geox, geoy);
		final int blockX = getBlock(geox);
		final int blockY = getBlock(geoy);
		int cellX, cellY, index;
		short temph = Short.MIN_VALUE;
		// Geodata without index - it is just empty so index can be calculated on the fly
		if (geodataIndex.get(region) == null)
		{
			index = ((blockX << 8) + blockY) * 3;
			// Get Index for current block of current region geodata
		}
		else
		{
			index = geodataIndex.get(region).get((blockX << 8) + blockY);
		}
		// Buffer that Contains current Region GeoData
		final ByteBuffer geo = geodata.get(region);
		if (geo == null)
		{
			if (Config.DEBUG)
			{
				LOGGER.warn("Geo Region - Region Offset: " + region + " dosnt exist!!");
			}
			return (short) zmin;
		}
		// Read current block type: 0-flat,1-complex,2-multilevel
		final byte type = geo.get(index);
		index++;
		if (type == 0)
		{
			temph = geo.getShort(index);
		}
		else if (type == 1)// complex
		{
			cellX = getCell(geox);
			cellY = getCell(geoy);
			index += (cellX << 3) + cellY << 1;
			short height = geo.getShort(index);
			height = (short) (height & 0x0fff0);
			height = (short) (height >> 1); // height / 2
			temph = height;
		}
		else
		// multilevel
		{
			cellX = getCell(geox);
			cellY = getCell(geoy);
			short height;
			int offset = (cellX << 3) + cellY;
			while (offset > 0)
			{
				final byte lc = geo.get(index);
				index += (lc << 1) + 1;
				offset--;
			}
			// Read current block type: 0-flat,1-complex,2-multilevel
			byte layers = geo.get(index);
			index++;
			if (layers <= 0 || layers > 125)
			{
				if (Config.DEBUG)
				{
					LOGGER.warn("Broken geofile (case2), region: " + region + " - invalid layer count: " + layers + " at: " + geox + " " + geoy);
				}
				return (short) zmin;
			}
			while (layers > 0)
			{
				height = geo.getShort(index);
				height = (short) (height & 0x0fff0);
				height = (short) (height >> 1); // height / 2
				if ((zmin - temph) * (zmin - temph) > (zmin - height) * (zmin - height))
				{
					temph = height;
				}
				layers--;
				index += 2;
			}
			if (temph > zmax + 200 || temph < zmin - 200)
			{
				if (Config.DEBUG)
				{
					LOGGER.warn("SpawnHeight Error - Couldnt find correct layer to spawn NPC - GeoData or Spawnlist Bug!: zmin: " + zmin + " zmax: " + zmax + " value: " + temph + " SpawnId: " + spawnid + " at: " + geox + " : " + geoy);
				}
				return (short) zmin;
			}
		}
		if (temph > zmax + 1000 || temph < zmin - 1000)
		{
			if (Config.DEBUG)
			{
				LOGGER.warn("SpawnHeight Error - Spawnlist z value is wrong or GeoData error: zmin: " + zmin + " zmax: " + zmax + " value: " + temph + " SpawnId: " + spawnid + " at: " + geox + " : " + geoy);
			}
			return (short) zmin;
		}
		return temph;
	}
	
	/**
	 * @param  x
	 * @param  y
	 * @param  z
	 * @param  tx
	 * @param  ty
	 * @param  tz
	 * @return    True if char can move to (tx,ty,tz)
	 */
	private double nCanMoveNext(final int x, final int y, final int z, final int tx, final int ty, final int tz)
	{
		final short region = getRegionOffset(x, y);
		final int blockX = getBlock(x);
		final int blockY = getBlock(y);
		int cellX, cellY;
		short NSWE = 0;
		
		int index = 0;
		// Geodata without index - it is just empty so index can be calculated on the fly
		if (geodataIndex.get(region) == null)
		{
			index = ((blockX << 8) + blockY) * 3;
			// Get Index for current block of current region geodata
		}
		else
		{
			index = geodataIndex.get(region).get((blockX << 8) + blockY);
		}
		// Buffer that Contains current Region GeoData
		final ByteBuffer geo = geodata.get(region);
		if (geo == null)
		{
			if (Config.DEBUG)
			{
				LOGGER.warn("Geo Region - Region Offset: " + region + " dosnt exist!!");
			}
			return z;
		}
		// Read current block type: 0-flat,1-complex,2-multilevel
		final byte type = geo.get(index);
		index++;
		if (type == 0)
		{
			return geo.getShort(index);
		}
		else if (type == 1) // complex
		{
			cellX = getCell(x);
			cellY = getCell(y);
			index += (cellX << 3) + cellY << 1;
			short height = geo.getShort(index);
			NSWE = (short) (height & 0x0F);
			height = (short) (height & 0x0fff0);
			height = (short) (height >> 1); // height / 2
			if (checkNSWE(NSWE, x, y, tx, ty))
			{
				return height;
			}
			return Double.MIN_VALUE;
		}
		else
		// multilevel, type == 2
		{
			cellX = getCell(x);
			cellY = getCell(y);
			int offset = (cellX << 3) + cellY;
			while (offset > 0) // iterates (too many times?) to get to layer count
			{
				final byte lc = geo.get(index);
				index += (lc << 1) + 1;
				offset--;
			}
			byte layers = geo.get(index);
			// LOGGER.warn( "layers"+layers);
			index++;
			short height = -1;
			if (layers <= 0 || layers > 125)
			{
				if (Config.DEBUG)
				{
					LOGGER.warn("Broken geofile (case3), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
				}
				return z;
			}
			short tempz = Short.MIN_VALUE;
			while (layers > 0)
			{
				height = geo.getShort(index);
				height = (short) (height & 0x0fff0);
				height = (short) (height >> 1); // height / 2
				
				// searches the closest layer to current z coordinate
				if ((z - tempz) * (z - tempz) > (z - height) * (z - height))
				{
					// layercurr = layers;
					tempz = height;
					NSWE = geo.getShort(index);
					NSWE = (short) (NSWE & 0x0F);
				}
				layers--;
				index += 2;
			}
			if (checkNSWE(NSWE, x, y, tx, ty))
			{
				return tempz;
			}
			return Double.MIN_VALUE;
		}
	}
	
	/**
	 * @param  x
	 * @param  y
	 * @param  z
	 * @param  inc_x
	 * @param  inc_y
	 * @param  inc_z
	 * @param  tz
	 * @param  debug
	 * @return       True if Char can see target
	 */
	private boolean nLOS(final int x, final int y, final int z, final int inc_x, final int inc_y, final double inc_z, final int tz, final boolean debug)
	{
		final short region = getRegionOffset(x, y);
		final int blockX = getBlock(x);
		final int blockY = getBlock(y);
		int cellX, cellY;
		short NSWE = 0;
		
		int index;
		// Geodata without index - it is just empty so index can be calculated on the fly
		if (geodataIndex.get(region) == null)
		{
			index = ((blockX << 8) + blockY) * 3;
			// Get Index for current block of current region geodata
		}
		else
		{
			index = geodataIndex.get(region).get((blockX << 8) + blockY);
		}
		// Buffer that Contains current Region GeoData
		final ByteBuffer geo = geodata.get(region);
		if (geo == null)
		{
			if (Config.DEBUG)
			{
				LOGGER.warn("Geo Region - Region Offset: " + region + " dosnt exist!!");
			}
			return true;
		}
		// Read current block type: 0-flat,1-complex,2-multilevel
		final byte type = geo.get(index);
		index++;
		if (type == 0) // flat, movement and sight always possible
		{
			final short height = geo.getShort(index);
			if (debug)
			{
				LOGGER.warn("flatheight:" + height);
			}
			if (z > height)
			{
				return inc_z > height;
			}
			return inc_z < height;
		}
		else if (type == 1) // complex
		{
			cellX = getCell(x);
			cellY = getCell(y);
			index += (cellX << 3) + cellY << 1;
			short height = geo.getShort(index);
			NSWE = (short) (height & 0x0F);
			height = (short) (height & 0x0fff0);
			height = (short) (height >> 1); // height / 2
			if (!checkNSWE(NSWE, x, y, x + inc_x, y + inc_y))
			{
				if (debug)
				{
					LOGGER.warn("height:" + height + " z" + z);
				}
				
				return z >= nGetUpperHeight(x + inc_x, y + inc_y, height);
			}
			return true;
		}
		else
		// multilevel, type == 2
		{
			cellX = getCell(x);
			cellY = getCell(y);
			
			int offset = (cellX << 3) + cellY;
			while (offset > 0) // iterates (too many times?) to get to layer count
			{
				final byte lc = geo.get(index);
				index += (lc << 1) + 1;
				offset--;
			}
			final byte layers = geo.get(index);
			
			index++;
			short tempZ = -1;
			if (layers <= 0 || layers > 125)
			{
				if (Config.DEBUG)
				{
					LOGGER.warn("Broken geofile (case4), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
				}
				return false;
			}
			short upperHeight = Short.MAX_VALUE; // big positive value
			short lowerHeight = Short.MIN_VALUE; // big negative value
			byte temp_layers = layers;
			boolean highestlayer = false;
			while (temp_layers > 0) // from higher to lower
			{
				// reads tempZ for current layer, result in world z coordinate
				tempZ = geo.getShort(index);
				tempZ = (short) (tempZ & 0x0fff0);
				tempZ = (short) (tempZ >> 1); // tempZ / 2
				
				if (z > tempZ)
				{
					lowerHeight = tempZ;
					NSWE = geo.getShort(index);
					NSWE = (short) (NSWE & 0x0F);
					break;
				}
				highestlayer = false;
				upperHeight = tempZ;
				
				temp_layers--;
				index += 2;
			}
			if (debug)
			{
				LOGGER.warn("z:" + z + " x: " + cellX + " y:" + cellY + " la " + layers + " lo:" + lowerHeight + " up:" + upperHeight);
			}
			// Check if LOS goes under a layer/floor
			// clearly under layer but not too much under
			// lowerheight here only for geodata bug checking, layers very close? maybe could be removed
			if (z - upperHeight < -10 && z - upperHeight > inc_z - 10 && z - lowerHeight > 40)
			{
				if (debug)
				{
					LOGGER.warn("false, incz" + inc_z);
				}
				return false;
			}
			
			// or there's a fence/wall ahead when we're not on highest layer
			if (!highestlayer)
			{
				// a probable wall, there's movement block and layers above you
				if (!checkNSWE(NSWE, x, y, x + inc_x, y + inc_y)) // cannot move
				{
					if (debug)
					{
						LOGGER.warn("block and next in x" + inc_x + " y" + inc_y + " is:" + nGetUpperHeight(x + inc_x, y + inc_y, lowerHeight));
					}
					// check one inc_x inc_y further, for the height there
					return z >= nGetUpperHeight(x + inc_x, y + inc_y, lowerHeight);
				}
				return true;
			}
			if (!checkNSWE(NSWE, x, y, x + inc_x, y + inc_y))
			{
				// check one inc_x inc_y further, for the height there
				return z >= nGetUpperHeight(x + inc_x, y + inc_y, lowerHeight);
			}
			return true;
		}
	}
	
	/**
	 * @param  x
	 * @param  y
	 * @param  z
	 * @return   NSWE: 0-15
	 */
	private short nGetNSWE(final int x, final int y, final int z)
	{
		final short region = getRegionOffset(x, y);
		final int blockX = getBlock(x);
		final int blockY = getBlock(y);
		int cellX, cellY;
		short NSWE = 0;
		
		int index = 0;
		// Geodata without index - it is just empty so index can be calculated on the fly
		if (geodataIndex.get(region) == null)
		{
			index = ((blockX << 8) + blockY) * 3;
			// Get Index for current block of current region geodata
		}
		else
		{
			index = geodataIndex.get(region).get((blockX << 8) + blockY);
		}
		// Buffer that Contains current Region GeoData
		final ByteBuffer geo = geodata.get(region);
		if (geo == null)
		{
			if (Config.DEBUG)
			{
				LOGGER.warn("Geo Region - Region Offset: " + region + " dosnt exist!!");
			}
			return 15;
		}
		// Read current block type: 0-flat,1-complex,2-multilevel
		final byte type = geo.get(index);
		index++;
		if (type == 0)
		{
			return 15;
		}
		else if (type == 1)// complex
		{
			cellX = getCell(x);
			cellY = getCell(y);
			index += (cellX << 3) + cellY << 1;
			final short height = geo.getShort(index);
			NSWE = (short) (height & 0x0F);
		}
		else
		// multilevel
		{
			cellX = getCell(x);
			cellY = getCell(y);
			int offset = (cellX << 3) + cellY;
			while (offset > 0)
			{
				final byte lc = geo.get(index);
				index += (lc << 1) + 1;
				offset--;
			}
			byte layers = geo.get(index);
			index++;
			short height = -1;
			if (layers <= 0 || layers > 125)
			{
				if (Config.DEBUG)
				{
					LOGGER.warn("Broken geofile (case5), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
				}
				return 15;
			}
			short tempz = Short.MIN_VALUE;
			while (layers > 0)
			{
				height = geo.getShort(index);
				height = (short) (height & 0x0fff0);
				height = (short) (height >> 1); // height / 2
				
				if ((z - tempz) * (z - tempz) > (z - height) * (z - height))
				{
					tempz = height;
					NSWE = geo.get(index);
					NSWE = (short) (NSWE & 0x0F);
				}
				layers--;
				index += 2;
			}
		}
		return NSWE;
	}
	
	/**
	 * @param  n
	 * @return   NSWE: 0-15
	 */
	@Override
	public Node[] getNeighbors(final Node n)
	{
		Node newNode;
		final int x = n.getNodeX();
		final int y = n.getNodeY();
		int parentdirection = 0;
		if (n.getParent() != null) // check for not adding parent again
		{
			if (n.getParent().getNodeX() > x)
			{
				parentdirection = 1;
			}
			if (n.getParent().getNodeX() < x)
			{
				parentdirection = -1;
			}
			if (n.getParent().getNodeY() > y)
			{
				parentdirection = 2;
			}
			if (n.getParent().getNodeY() < y)
			{
				parentdirection = -2;
			}
		}
		final short z = n.getZ();
		final short region = getRegionOffset(x, y);
		final int blockX = getBlock(x);
		final int blockY = getBlock(y);
		int cellX, cellY;
		short NSWE = 0;
		int index = 0;
		// Geodata without index - it is just empty so index can be calculated on the fly
		if (geodataIndex.get(region) == null)
		{
			index = ((blockX << 8) + blockY) * 3;
			// Get Index for current block of current region geodata
		}
		else
		{
			index = geodataIndex.get(region).get((blockX << 8) + blockY);
		}
		// Buffer that Contains current Region GeoData
		final ByteBuffer geo = geodata.get(region);
		if (geo == null)
		{
			if (Config.DEBUG)
			{
				LOGGER.warn("Geo Region - Region Offset: " + region + " dosnt exist!!");
			}
			return null;
		}
		
		final Node[] Neighbors = new Node[4];
		int arrayIndex = 0;
		
		// Read current block type: 0-flat,1-complex,2-multilevel
		final byte type = geo.get(index);
		index++;
		if (type == 0)// flat
		{
			final short height = geo.getShort(index);
			n.setZ(height);
			if (parentdirection != 1)
			{
				newNode = CellPathFinding.getInstance().readNode(x + 1, y, height);
				// newNode.setCost(0);
				Neighbors[arrayIndex++] = newNode;
			}
			if (parentdirection != 2)
			{
				newNode = CellPathFinding.getInstance().readNode(x, y + 1, height);
				Neighbors[arrayIndex++] = newNode;
			}
			if (parentdirection != -2)
			{
				newNode = CellPathFinding.getInstance().readNode(x, y - 1, height);
				Neighbors[arrayIndex++] = newNode;
			}
			if (parentdirection != -1)
			{
				newNode = CellPathFinding.getInstance().readNode(x - 1, y, height);
				Neighbors[arrayIndex++] = newNode;
			}
		}
		else if (type == 1)// complex
		{
			cellX = getCell(x);
			cellY = getCell(y);
			index += (cellX << 3) + cellY << 1;
			short height = geo.getShort(index);
			NSWE = (short) (height & 0x0F);
			height = (short) (height & 0x0fff0);
			height = (short) (height >> 1); // height / 2
			n.setZ(height);
			if (NSWE != 15 && parentdirection != 0)
			{
				return null; // no node with a block will be used
			}
			if (parentdirection != 1 && checkNSWE(NSWE, x, y, x + 1, y))
			{
				newNode = CellPathFinding.getInstance().readNode(x + 1, y, height);
				// newNode.setCost(basecost+50);
				Neighbors[arrayIndex++] = newNode;
			}
			if (parentdirection != 2 && checkNSWE(NSWE, x, y, x, y + 1))
			{
				newNode = CellPathFinding.getInstance().readNode(x, y + 1, height);
				Neighbors[arrayIndex++] = newNode;
			}
			if (parentdirection != -2 && checkNSWE(NSWE, x, y, x, y - 1))
			{
				newNode = CellPathFinding.getInstance().readNode(x, y - 1, height);
				Neighbors[arrayIndex++] = newNode;
			}
			if (parentdirection != -1 && checkNSWE(NSWE, x, y, x - 1, y))
			{
				newNode = CellPathFinding.getInstance().readNode(x - 1, y, height);
				Neighbors[arrayIndex++] = newNode;
			}
		}
		else
		// multilevel
		{
			cellX = getCell(x);
			cellY = getCell(y);
			int offset = (cellX << 3) + cellY;
			while (offset > 0)
			{
				final byte lc = geo.get(index);
				index += (lc << 1) + 1;
				offset--;
			}
			byte layers = geo.get(index);
			index++;
			short height = -1;
			if (layers <= 0 || layers > 125)
			{
				if (Config.DEBUG)
				{
					LOGGER.warn("Broken geofile (case5), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
				}
				return null;
			}
			short tempz = Short.MIN_VALUE;
			while (layers > 0)
			{
				height = geo.getShort(index);
				height = (short) (height & 0x0fff0);
				height = (short) (height >> 1); // height / 2
				
				if ((z - tempz) * (z - tempz) > (z - height) * (z - height))
				{
					tempz = height;
					NSWE = geo.get(index);
					NSWE = (short) (NSWE & 0x0F);
				}
				layers--;
				index += 2;
			}
			n.setZ(tempz);
			if (NSWE != 15 && parentdirection != 0)
			{
				return null; // no node with a block will be used
			}
			if (parentdirection != 1 && checkNSWE(NSWE, x, y, x + 1, y))
			{
				newNode = CellPathFinding.getInstance().readNode(x + 1, y, tempz);
				// newNode.setCost(basecost+50);
				Neighbors[arrayIndex++] = newNode;
			}
			if (parentdirection != 2 && checkNSWE(NSWE, x, y, x, y + 1))
			{
				newNode = CellPathFinding.getInstance().readNode(x, y + 1, tempz);
				Neighbors[arrayIndex++] = newNode;
			}
			if (parentdirection != -2 && checkNSWE(NSWE, x, y, x, y - 1))
			{
				newNode = CellPathFinding.getInstance().readNode(x, y - 1, tempz);
				Neighbors[arrayIndex++] = newNode;
			}
			if (parentdirection != -1 && checkNSWE(NSWE, x, y, x - 1, y))
			{
				newNode = CellPathFinding.getInstance().readNode(x - 1, y, tempz);
				Neighbors[arrayIndex++] = newNode;
			}
		}
		
		return L2Arrays.compact(Neighbors);
	}
	
	/**
	 * @param  NSWE
	 * @param  x
	 * @param  y
	 * @param  tx
	 * @param  ty
	 * @return      True if NSWE dont block given direction
	 */
	private boolean checkNSWE(final short NSWE, final int x, final int y, final int tx, final int ty)
	{
		// Check NSWE
		if (NSWE == 15)
		{
			return true;
		}
		if (tx > x)// E
		{
			if ((NSWE & e) == 0)
			{
				return false;
			}
		}
		else if (tx < x)// W
		{
			if ((NSWE & w) == 0)
			{
				return false;
			}
		}
		if (ty > y)// S
		{
			if ((NSWE & s) == 0)
			{
				return false;
			}
		}
		else if (ty < y)// N
		{
			if ((NSWE & n) == 0)
			{
				return false;
			}
		}
		return true;
	}
}
