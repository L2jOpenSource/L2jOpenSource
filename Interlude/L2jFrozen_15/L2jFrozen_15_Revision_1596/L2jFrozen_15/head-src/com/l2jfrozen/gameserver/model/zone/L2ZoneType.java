package com.l2jfrozen.gameserver.model.zone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;

import main.EngineModsManager;

/**
 * Abstract base class for any zone type Handles basic operations
 * @author durgus
 */
public abstract class L2ZoneType
{
	private static final Logger LOGGER = Logger.getLogger(L2ZoneType.class);
	
	private int zoneId;
	private String zoneName;
	protected L2ZoneShape zoneShape;
	public Map<Integer, L2Character> characterList;
	
	protected L2ZoneType(int id)
	{
		zoneId = id;
		characterList = new ConcurrentHashMap<>();
	}
	
	public void setParameter(String name, String value)
	{
		if (name.equalsIgnoreCase("name"))
		{
			zoneName = value;
		}
		else
		{
			LOGGER.warn("Unknow parameter name " + name + " for zone with ID " + zoneId);
		}
	}
	
	public int getZoneId()
	{
		return zoneId;
	}
	
	public void setZoneName(String zoneName)
	{
		this.zoneName = zoneName;
	}
	
	public String getZoneName()
	{
		if (zoneName == null)
		{
			return "zone unamed";
		}
		
		return zoneName;
	}
	
	public void setSpawnLocs(Node node1)
	{
	}
	
	/**
	 * @param zone : The shape for this L2ZoneType Instance<BR>
	 *                 <B>Values:</B><BR>
	 *                 - ZoneCuboid<BR>
	 *                 - ZoneCylinder<BR>
	 *                 - ZoneNPoly<BR>
	 */
	public void setZoneShape(L2ZoneShape zone)
	{
		zoneShape = zone;
	}
	
	/**
	 * @return the zone <B>shape</B><BR>
	 *         <B>Values:</B><BR>
	 *         - ZoneCuboid<BR>
	 *         - ZoneCylinder<BR>
	 *         - ZoneNPoly<BR>
	 */
	public L2ZoneShape getZoneShape()
	{
		return zoneShape;
	}
	
	public boolean isInsideZone(int x, int y, int z)
	{
		return zoneShape.isInsideZone(x, y, z);
	}
	
	public boolean isInsideZone(L2Object object)
	{
		return zoneShape.isInsideZone(object.getX(), object.getY(), object.getZ());
	}
	
	public double getDistanceToZone(int x, int y)
	{
		return zoneShape.getDistanceToZone(x, y);
	}
	
	public double getDistanceToZone(L2Object object)
	{
		return zoneShape.getDistanceToZone(object.getX(), object.getY());
	}
	
	public void revalidateInZone(L2Character character)
	{
		if (Config.ZONE_DEBUG && character.isPlayer() && ((L2PcInstance) character).isGM())
		{
			
			LOGGER.debug("ZONE: Character " + character.getName() + " has coords: ");
			LOGGER.debug("ZONE: 	X: " + character.getX());
			LOGGER.debug("ZONE: 	Y: " + character.getY());
			LOGGER.debug("ZONE: 	Z: " + character.getZ());
			LOGGER.debug("ZONE:  -  is inside zone " + zoneId + "?: " + zoneShape.isInsideZone(character.getX(), character.getY(), character.getZ()));
			
		}
		
		// If the object is inside the zone...
		if (zoneShape.isInsideZone(character.getX(), character.getY(), character.getZ()))
		{
			// Was the character not yet inside this zone?
			if (!characterList.containsKey(character.getObjectId()))
			{
				characterList.put(character.getObjectId(), character);
				onEnter(character);
				EngineModsManager.onEnterZone(character, this);
			}
		}
		else
		{
			// Was the character inside this zone?
			if (characterList.containsKey(character.getObjectId()))
			{
				if (Config.ZONE_DEBUG && character.isPlayer() && character.getName() != null)
				{
					LOGGER.debug("ZONE: " + "Character " + character.getName() + " removed from zone.");
				}
				characterList.remove(character.getObjectId());
				onExit(character);
				EngineModsManager.onExitZone(character, this);
			}
		}
		
		if (Config.ZONE_DEBUG)
		{
			for (final L2Character actual : characterList.values())
			{
				if (actual.isPlayer())
				{
					LOGGER.debug("ZONE:	 -  " + actual.getName() + " is inside zone " + zoneId);
				}
			}
		}
		
	}
	
	/**
	 * Force fully removes a character from the zone Should use during teleport / logoff
	 * @param character
	 */
	public void removeCharacter(L2Character character)
	{
		if (characterList.containsKey(character.getObjectId()))
		{
			characterList.remove(character.getObjectId());
			onExit(character);
		}
	}
	
	public boolean isCharacterInZone(L2Character character)
	{
		// re validate zone is not always performed, so better both checks
		if (character != null)
		{
			return characterList.containsKey(character.getObjectId()) || isInsideZone(character.getX(), character.getY(), character.getZ());
		}
		
		return false;
		
	}
	
	/**
	 * @param packet Broadcasts packet to all players inside the zone
	 */
	public void broadcastPacket(L2GameServerPacket packet)
	{
		if (characterList.isEmpty())
		{
			return;
		}
		
		for (L2Character character : characterList.values())
		{
			if (character.isPlayer())
			{
				character.sendPacket(packet);
			}
		}
	}
	
	public Map<Integer, L2Character> getCharactersInside()
	{
		return characterList;
	}
	
	public List<L2PcInstance> getPlayersInside()
	{
		List<L2PcInstance> playerList = new ArrayList<>();
		
		for (L2Character character : characterList.values())
		{
			if (character.isPlayer())
			{
				playerList.add((L2PcInstance) character);
			}
		}
		
		return playerList;
	}
	
	protected abstract void onEnter(L2Character character);
	
	protected abstract void onExit(L2Character character);
	
	protected abstract void onDieInside(L2Character character);
	
	protected abstract void onReviveInside(L2Character character);
	
}
