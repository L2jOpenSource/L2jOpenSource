package net.sf.l2j.gameserver.model.zone.type.subtype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.SpawnType;
import net.sf.l2j.gameserver.model.location.Location;

/**
 * An abstract zone with spawn {@link Location}s, inheriting {@link ZoneType} behavior.
 */
public abstract class SpawnZoneType extends ZoneType
{
	private Map<SpawnType, List<Location>> _spawns = new HashMap<>();
	
	public SpawnZoneType(int id)
	{
		super(id);
	}
	
	/**
	 * Add a {@link Location} into the dedicated {@link SpawnType} {@link List}.<br>
	 * <br>
	 * If the key doesn't exist, generate a new {@link ArrayList}.
	 * @param type : The SpawnType to test.
	 * @param loc : The Location to add.
	 */
	public final void addSpawn(SpawnType type, Location loc)
	{
		_spawns.computeIfAbsent(type, k -> new ArrayList<>()).add(loc);
	}
	
	/**
	 * @param type : The SpawnType to test.
	 * @return the {@link List} of {@link Location}s based on {@link SpawnType} parameter. If that SpawnType doesn't exist, return the NORMAL List of Locations.
	 */
	public final List<Location> getSpawns(SpawnType type)
	{
		return _spawns.getOrDefault(type, _spawns.get(SpawnType.NORMAL));
	}
	
	/**
	 * @param type : The SpawnType to test.
	 * @return a random {@link Location} based on {@link SpawnType} parameter. If that SpawnType doesn't exist, return a NORMAL random Location.
	 */
	public final Location getRndSpawn(SpawnType type)
	{
		return Rnd.get(getSpawns(type));
	}
}