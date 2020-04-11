package com.l2jfrozen.gameserver.geo;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.geo.pathfinding.Node;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.Point3D;

import main.data.memory.ObjectData;
import main.holders.objects.ObjectHolder;

public class GeoData
{
	protected static final Logger LOGGER = Logger.getLogger(GeoData.class);
	
	private static final class SingletonHolder
	{
		static
		{
			LOGGER.info("Geodata Engine: Disabled.");
		}
		
		protected static final GeoData INSTANCE = new GeoData();
	}
	
	protected GeoData()
	{
	}
	
	public static GeoData getInstance()
	{
		if (Config.GEODATA > 0)
		{
			return GeoEngine.getInstance();
		}
		return SingletonHolder.INSTANCE;
	}
	
	// Public Methods
	/**
	 * @param  x
	 * @param  y
	 * @return   Geo Block Type
	 */
	public short getType(final int x, final int y)
	{
		return 0;
	}
	
	/**
	 * @param  x
	 * @param  y
	 * @param  z
	 * @return   Nearles Z
	 */
	public short getHeight(final int x, final int y, final int z)
	{
		return (short) z;
	}
	
	/**
	 * @param  x
	 * @param  y
	 * @param  zmin
	 * @param  zmax
	 * @param  spawnid
	 * @return
	 */
	public short getSpawnHeight(final int x, final int y, final int zmin, final int zmax, final int spawnid)
	{
		return (short) zmin;
	}
	
	/**
	 * @param  x
	 * @param  y
	 * @return
	 */
	public String geoPosition(final int x, final int y)
	{
		return "";
	}
	
	/**
	 * @param  cha
	 * @param  target
	 * @return        True if cha can see target (LOS)
	 */
	public boolean canSeeTarget(final L2Object cha, final L2Object target)
	{
		ObjectHolder oh = ObjectData.get(ObjectHolder.class, cha);
		if (oh != null && oh.isDifferentWorld(target))
		{
			return false;
		}
		// If geo is off do simple check :]
		// Don't allow casting on players on different dungeon lvls etc
		return Math.abs(target.getZ() - cha.getZ()) < 1000;
	}
	
	public boolean canSeeTarget(final L2Object cha, final Point3D worldPosition)
	{
		// If geo is off do simple check :]
		// Don't allow casting on players on different dungeon lvls etc
		return Math.abs(worldPosition.getZ() - cha.getZ()) < 1000;
	}
	
	public boolean canSeeTarget(final int x, final int y, final int z, final int tx, final int ty, final int tz)
	{
		// If geo is off do simple check :]
		// Don't allow casting on players on different dungeon lvls etc
		return Math.abs(z - tz) < 1000;
	}
	
	/**
	 * @param  gm
	 * @param  target
	 * @return        True if cha can see target (LOS) and send usful info to PC
	 */
	public boolean canSeeTargetDebug(final L2PcInstance gm, final L2Object target)
	{
		return true;
	}
	
	/**
	 * @param  x
	 * @param  y
	 * @param  z
	 * @return   Geo NSWE (0-15)
	 */
	public short getNSWE(final int x, final int y, final int z)
	{
		return 15;
	}
	
	/**
	 * @param  x
	 * @param  y
	 * @param  z
	 * @param  tx
	 * @param  ty
	 * @param  tz
	 * @return    Last Location (x,y,z) where player can walk - just before wall
	 */
	public Location moveCheck(final int x, final int y, final int z, final int tx, final int ty, final int tz)
	{
		return new Location(tx, ty, tz);
	}
	
	public boolean canMoveFromToTarget(final int x, final int y, final int z, final int tx, final int ty, final int tz)
	{
		return true;
	}
	
	/**
	 * @param gm
	 * @param comment
	 */
	public void addGeoDataBug(final L2PcInstance gm, final String comment)
	{
		// Do Nothing
	}
	
	public void unloadGeodata(final byte rx, final byte ry)
	{
	}
	
	public boolean loadGeodataFile(final byte rx, final byte ry)
	{
		return false;
	}
	
	public boolean hasGeo(final int x, final int y)
	{
		return false;
	}
	
	public Node[] getNeighbors(final Node n)
	{
		return null;
	}
}
