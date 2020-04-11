package com.l2jfrozen.gameserver.datatables.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.geo.pathfinding.Node;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.managers.ClanHallManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.entity.ClanHall;
import com.l2jfrozen.gameserver.templates.L2CharTemplate;
import com.l2jfrozen.gameserver.templates.StatsSet;

public class DoorTable
{
	private static Logger LOGGER = Logger.getLogger(DoorTable.class);
	
	private Map<Integer, L2DoorInstance> staticItems;
	
	private static DoorTable instance;
	
	public static DoorTable getInstance()
	{
		if (instance == null)
		{
			instance = new DoorTable();
		}
		
		return instance;
	}
	
	public DoorTable()
	{
		staticItems = new HashMap<>();
	}
	
	public void reloadAll()
	{
		respawn();
	}
	
	public void respawn()
	{
		staticItems = null;
		instance = new DoorTable();
	}
	
	public void parseData()
	{
		File doorData = new File(Config.DATAPACK_ROOT, "data/csv/door.csv");
		
		try (FileReader reader = new FileReader(doorData);
			BufferedReader buff = new BufferedReader(reader);
			LineNumberReader lnr = new LineNumberReader(buff))
		{
			String line = null;
			LOGGER.info("Searching clan halls doors:");
			
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().length() == 0 || line.startsWith("#"))
				{
					continue;
				}
				
				final L2DoorInstance door = parseList(line);
				staticItems.put(door.getDoorId(), door);
				door.spawnMe(door.getX(), door.getY(), door.getZ());
				final ClanHall clanhall = ClanHallManager.getInstance().getNearbyClanHall(door.getX(), door.getY(), 500);
				
				if (clanhall != null)
				{
					clanhall.getDoors().add(door);
					door.setClanHall(clanhall);
					
					if (Config.DEBUG)
					{
						LOGGER.warn("door " + door.getDoorName() + " attached to ch " + clanhall.getName());
					}
				}
				
			}
			
			LOGGER.info("DoorTable: Loaded " + staticItems.size() + " Door Templates.");
		}
		catch (FileNotFoundException e)
		{
			initialized = false;
			LOGGER.warn("DootTable.parseData: door.csv file is missing in gameserver/data/csv/ folder");
		}
		catch (IOException e)
		{
			initialized = false;
			LOGGER.error("DootTable.parseData : Error while creating door table. ", e);
		}
	}
	
	public static L2DoorInstance parseList(final String line)
	{
		StringTokenizer st = new StringTokenizer(line, ";");
		
		String name = st.nextToken();
		final int id = Integer.parseInt(st.nextToken());
		final int x = Integer.parseInt(st.nextToken());
		final int y = Integer.parseInt(st.nextToken());
		final int z = Integer.parseInt(st.nextToken());
		final int rangeXMin = Integer.parseInt(st.nextToken());
		final int rangeYMin = Integer.parseInt(st.nextToken());
		final int rangeZMin = Integer.parseInt(st.nextToken());
		final int rangeXMax = Integer.parseInt(st.nextToken());
		final int rangeYMax = Integer.parseInt(st.nextToken());
		final int rangeZMax = Integer.parseInt(st.nextToken());
		final int hp = Integer.parseInt(st.nextToken());
		final int pdef = Integer.parseInt(st.nextToken());
		final int mdef = Integer.parseInt(st.nextToken());
		
		boolean unlockable = false;
		
		if (st.hasMoreTokens())
		{
			unlockable = Boolean.parseBoolean(st.nextToken());
		}
		boolean autoOpen = false;
		
		if (st.hasMoreTokens())
		{
			autoOpen = Boolean.parseBoolean(st.nextToken());
		}
		
		st = null;
		
		if (rangeXMin > rangeXMax)
		{
			LOGGER.error("Error in door data, ID:" + id);
		}
		
		if (rangeYMin > rangeYMax)
		{
			LOGGER.error("Error in door data, ID:" + id);
		}
		
		if (rangeZMin > rangeZMax)
		{
			LOGGER.error("Error in door data, ID:" + id);
		}
		
		int collisionRadius; // (max) radius for movement checks
		
		if (rangeXMax - rangeXMin > rangeYMax - rangeYMin)
		{
			collisionRadius = rangeYMax - rangeYMin;
		}
		else
		{
			collisionRadius = rangeXMax - rangeXMin;
		}
		
		StatsSet npcDat = new StatsSet();
		npcDat.set("npcId", id);
		npcDat.set("level", 0);
		npcDat.set("jClass", "door");
		
		npcDat.set("baseSTR", 0);
		npcDat.set("baseCON", 0);
		npcDat.set("baseDEX", 0);
		npcDat.set("baseINT", 0);
		npcDat.set("baseWIT", 0);
		npcDat.set("baseMEN", 0);
		
		npcDat.set("baseShldDef", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("baseAccCombat", 38);
		npcDat.set("baseEvasRate", 38);
		npcDat.set("baseCritRate", 38);
		
		// npcDat.set("name", "");
		npcDat.set("collision_radius", collisionRadius);
		npcDat.set("collision_height", rangeZMax - rangeZMin);
		npcDat.set("sex", "male");
		npcDat.set("type", "");
		npcDat.set("baseAtkRange", 0);
		npcDat.set("baseMpMax", 0);
		npcDat.set("baseCpMax", 0);
		npcDat.set("rewardExp", 0);
		npcDat.set("rewardSp", 0);
		npcDat.set("basePAtk", 0);
		npcDat.set("baseMAtk", 0);
		npcDat.set("basePAtkSpd", 0);
		npcDat.set("aggroRange", 0);
		npcDat.set("baseMAtkSpd", 0);
		npcDat.set("rhand", 0);
		npcDat.set("lhand", 0);
		npcDat.set("armor", 0);
		npcDat.set("baseWalkSpd", 0);
		npcDat.set("baseRunSpd", 0);
		npcDat.set("name", name);
		npcDat.set("baseHpMax", hp);
		npcDat.set("baseHpReg", 3.e-3f);
		npcDat.set("baseMpReg", 3.e-3f);
		npcDat.set("basePDef", pdef);
		npcDat.set("baseMDef", mdef);
		
		L2CharTemplate template = new L2CharTemplate(npcDat);
		final L2DoorInstance door = new L2DoorInstance(IdFactory.getInstance().getNextId(), template, id, name, unlockable);
		door.setRange(rangeXMin, rangeYMin, rangeZMin, rangeXMax, rangeYMax, rangeZMax);
		name = null;
		npcDat = null;
		template = null;
		try
		{
			door.setMapRegion(MapRegionTable.getInstance().getMapRegion(x, y));
		}
		catch (final Exception e)
		{
			LOGGER.error("Error in door data, ID:" + id, e);
		}
		door.setCurrentHpMp(door.getMaxHp(), door.getMaxMp());
		door.setIsOpen(autoOpen);
		door.setXYZInvisible(x, y, z);
		
		return door;
	}
	
	public boolean isInitialized()
	{
		return initialized;
	}
	
	private boolean initialized = true;
	
	public L2DoorInstance getDoor(final Integer id)
	{
		return staticItems.get(id);
	}
	
	public void putDoor(final L2DoorInstance door)
	{
		staticItems.put(door.getDoorId(), door);
	}
	
	public L2DoorInstance[] getDoors()
	{
		final L2DoorInstance[] allTemplates = staticItems.values().toArray(new L2DoorInstance[staticItems.size()]);
		return allTemplates;
	}
	
	/**
	 * Performs a check and sets up a scheduled task for those doors that require auto opening/closing.
	 */
	public void checkAutoOpen()
	{
		for (final L2DoorInstance doorInst : getDoors())
		{
			// Garden of Eva (every 7 minutes)
			if (doorInst.getDoorName().startsWith("goe"))
			{
				doorInst.setAutoActionDelay(420000);
			}
			// Tower of Insolence (every 5 minutes)
			else if (doorInst.getDoorName().startsWith("aden_tower"))
			{
				doorInst.setAutoActionDelay(300000);
			}
			// Cruma Tower (every 20 minutes)
			else if (doorInst.getDoorName().startsWith("cruma"))
			{
				doorInst.setAutoActionDelay(1200000);
			}
		}
	}
	
	public boolean checkIfDoorsBetween(final Node start, final Node end)
	{
		return checkIfDoorsBetween(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ());
	}
	
	public boolean checkIfDoorsBetween(final int x, final int y, final int z, final int tx, final int ty, final int tz)
	{
		int region;
		try
		{
			region = MapRegionTable.getInstance().getMapRegion(x, y);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return false;
		}
		
		for (final L2DoorInstance doorInst : getDoors())
		{
			if (doorInst.getMapRegion() != region)
			{
				continue;
			}
			if (doorInst.getXMax() == 0)
			{
				continue;
			}
			
			// line segment goes through box
			// heavy approximation disabling some shooting angles especially near 2-piece doors
			// but most calculations should stop short
			// phase 1, x
			if (x <= doorInst.getXMax() && tx >= doorInst.getXMin() || tx <= doorInst.getXMax() && x >= doorInst.getXMin())
			{
				// phase 2, y
				if (y <= doorInst.getYMax() && ty >= doorInst.getYMin() || ty <= doorInst.getYMax() && y >= doorInst.getYMin())
				{
					// phase 3, basically only z remains but now we calculate it with another formula (by rage)
					// in some cases the direct line check (only) in the beginning isn't sufficient,
					// when char z changes a lot along the path
					if (doorInst.getStatus().getCurrentHp() > 0 && !doorInst.isOpen())
					{
						final int px1 = doorInst.getXMin();
						final int py1 = doorInst.getYMin();
						final int pz1 = doorInst.getZMin();
						final int px2 = doorInst.getXMax();
						final int py2 = doorInst.getYMax();
						final int pz2 = doorInst.getZMax();
						
						final int l = tx - x;
						final int m = ty - y;
						final int n = tz - z;
						
						int dk;
						
						if ((dk = (doorInst.getA() * l + doorInst.getB() * m + doorInst.getC() * n)) == 0)
						{
							continue; // Parallel
						}
						
						final float p = (float) (doorInst.getA() * x + doorInst.getB() * y + doorInst.getC() * z + doorInst.getD()) / (float) dk;
						
						final int fx = (int) (x - l * p);
						final int fy = (int) (y - m * p);
						final int fz = (int) (z - n * p);
						
						if ((Math.min(x, tx) <= fx && fx <= Math.max(x, tx)) && (Math.min(y, ty) <= fy && fy <= Math.max(y, ty)) && (Math.min(z, tz) <= fz && fz <= Math.max(z, tz)))
						{
							
							if (((fx >= px1 && fx <= px2) || (fx >= px2 && fx <= px1)) && ((fy >= py1 && fy <= py2) || (fy >= py2 && fy <= py1)) && ((fz >= pz1 && fz <= pz2) || (fz >= pz2 && fz <= pz1)))
							{
								return true; // Door between
							}
						}
					}
				}
			}
		}
		return false;
	}
}
