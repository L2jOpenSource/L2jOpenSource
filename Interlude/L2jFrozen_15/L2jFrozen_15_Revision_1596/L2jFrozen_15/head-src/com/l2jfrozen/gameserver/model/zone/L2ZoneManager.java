package com.l2jfrozen.gameserver.model.zone;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class manages all zones for a given world region
 * @author durgus
 */
public class L2ZoneManager
{
	private final Logger LOGGER = Logger.getLogger(L2ZoneManager.class);
	private final List<L2ZoneType> zones;
	
	/**
	 * The Constructor creates an initial zone list use registerNewZone() / unregisterZone() to change the zone list
	 */
	public L2ZoneManager()
	{
		zones = new ArrayList<>();
	}
	
	/**
	 * Register a new zone object into the manager
	 * @param zone
	 */
	public void registerNewZone(final L2ZoneType zone)
	{
		zones.add(zone);
	}
	
	/**
	 * Unregister a given zone from the manager (e.g. dynamic zones)
	 * @param zone
	 */
	public void unregisterZone(L2ZoneType zone)
	{
		zones.remove(zone);
	}
	
	public void revalidateZones(L2Character character)
	{
		if (Config.ZONE_DEBUG && character != null && character instanceof L2PcInstance && character.getName() != null)
		{
			LOGGER.debug("ZONE: Revalidating Zone for character: " + character.getName());
		}
		
		for (L2ZoneType e : zones)
		{
			if (e != null)
			{
				e.revalidateInZone(character);
			}
		}
	}
	
	public void removeCharacter(final L2Character character)
	{
		for (L2ZoneType e : zones)
		{
			if (e != null)
			{
				e.removeCharacter(character);
			}
		}
	}
	
	public void onDeath(final L2Character character)
	{
		for (L2ZoneType e : zones)
		{
			if (e != null)
			{
				e.onDieInside(character);
			}
		}
	}
	
	public void onRevive(final L2Character character)
	{
		for (L2ZoneType e : zones)
		{
			if (e != null)
			{
				e.onReviveInside(character);
			}
		}
	}
	
	public List<L2ZoneType> getZones()
	{
		return zones;
	}
}
