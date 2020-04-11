package com.l2jfrozen.gameserver.managers;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.model.zone.type.L2FishingZone;
import com.l2jfrozen.gameserver.model.zone.type.L2WaterZone;

public class FishingZoneManager
{
	private static FishingZoneManager instance;
	
	private List<L2FishingZone> fishingZones;
	private List<L2WaterZone> waterZones;
	
	public static final FishingZoneManager getInstance()
	{
		if (instance == null)
		{
			instance = new FishingZoneManager();
		}
		return instance;
	}
	
	public void addFishingZone(final L2FishingZone fishingZone)
	{
		if (fishingZones == null)
		{
			fishingZones = new ArrayList<>();
		}
		
		fishingZones.add(fishingZone);
	}
	
	public void addWaterZone(final L2WaterZone waterZone)
	{
		if (waterZones == null)
		{
			waterZones = new ArrayList<>();
		}
		
		waterZones.add(waterZone);
	}
	
	/*
	 * isInsideFishingZone() - This function was modified to check the coordinates without caring for Z. This allows for the player to fish off bridges, into the water, or from other similar high places. One should be able to cast the line from up into the water, not only fishing whith one's feet wet.
	 * :) TODO: Consider in the future, limiting the maximum height one can be above water, if we start getting "orbital fishing" players... xD
	 */
	public final L2FishingZone isInsideFishingZone(final int x, final int y, final int z)
	{
		for (final L2FishingZone temp : fishingZones)
		{
			if (temp.isInsideZone(x, y, temp.getWaterZ() - 10))
			{
				return temp;
			}
		}
		return null;
	}
	
	public final L2WaterZone isInsideWaterZone(final int x, final int y, final int z)
	{
		for (final L2WaterZone temp : waterZones)
		{
			if (temp.isInsideZone(x, y, temp.getWaterZ()))
			{
				return temp;
			}
		}
		return null;
	}
}
