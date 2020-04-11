package net.sf.l2j.gameserver.model;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.commons.logging.CLogger;

import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.data.sql.SpawnTable;
import net.sf.l2j.gameserver.enums.SayType;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.spawn.Spawn;
import net.sf.l2j.gameserver.model.zone.type.subtype.ZoneType;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;

public final class World
{
	private static final CLogger LOGGER = new CLogger(World.class.getName());
	
	// Geodata min/max tiles
	public static final int TILE_X_MIN = 16;
	public static final int TILE_X_MAX = 26;
	public static final int TILE_Y_MIN = 10;
	public static final int TILE_Y_MAX = 25;
	
	// Map dimensions
	public static final int TILE_SIZE = 32768;
	public static final int WORLD_X_MIN = (TILE_X_MIN - 20) * TILE_SIZE;
	public static final int WORLD_X_MAX = (TILE_X_MAX - 19) * TILE_SIZE;
	public static final int WORLD_Y_MIN = (TILE_Y_MIN - 18) * TILE_SIZE;
	public static final int WORLD_Y_MAX = (TILE_Y_MAX - 17) * TILE_SIZE;
	public static final int WORLD_Z_MAX = 16410;
	
	// Regions and offsets
	private static final int REGION_SIZE = 2048;
	private static final int REGIONS_X = (WORLD_X_MAX - WORLD_X_MIN) / REGION_SIZE;
	private static final int REGIONS_Y = (WORLD_Y_MAX - WORLD_Y_MIN) / REGION_SIZE;
	private static final int REGION_X_OFFSET = Math.abs(WORLD_X_MIN / REGION_SIZE);
	private static final int REGION_Y_OFFSET = Math.abs(WORLD_Y_MIN / REGION_SIZE);
	
	private final Map<Integer, WorldObject> _objects = new ConcurrentHashMap<>();
	private final Map<Integer, Pet> _pets = new ConcurrentHashMap<>();
	private final Map<Integer, Player> _players = new ConcurrentHashMap<>();
	
	private final WorldRegion[][] _worldRegions = new WorldRegion[REGIONS_X + 1][REGIONS_Y + 1];
	
	protected World()
	{
		for (int i = 0; i <= REGIONS_X; i++)
		{
			for (int j = 0; j <= REGIONS_Y; j++)
				_worldRegions[i][j] = new WorldRegion(i, j);
		}
		
		for (int x = 0; x <= REGIONS_X; x++)
		{
			for (int y = 0; y <= REGIONS_Y; y++)
			{
				for (int a = -1; a <= 1; a++)
				{
					for (int b = -1; b <= 1; b++)
					{
						if (validRegion(x + a, y + b))
							_worldRegions[x + a][y + b].addSurroundingRegion(_worldRegions[x][y]);
					}
				}
			}
		}
		LOGGER.info("World grid ({} by {}) is now set up.", REGIONS_X, REGIONS_Y);
	}
	
	public void addObject(WorldObject object)
	{
		_objects.putIfAbsent(object.getObjectId(), object);
	}
	
	public void removeObject(WorldObject object)
	{
		_objects.remove(object.getObjectId());
	}
	
	public Collection<WorldObject> getObjects()
	{
		return _objects.values();
	}
	
	public WorldObject getObject(int objectId)
	{
		return _objects.get(objectId);
	}
	
	public void addPlayer(Player cha)
	{
		_players.putIfAbsent(cha.getObjectId(), cha);
	}
	
	public void removePlayer(Player cha)
	{
		_players.remove(cha.getObjectId());
	}
	
	public Collection<Player> getPlayers()
	{
		return _players.values();
	}
	
	public Player getPlayer(String name)
	{
		return _players.get(PlayerInfoTable.getInstance().getPlayerObjectId(name));
	}
	
	public Player getPlayer(int objectId)
	{
		return _players.get(objectId);
	}
	
	public void addPet(int ownerId, Pet pet)
	{
		_pets.putIfAbsent(ownerId, pet);
	}
	
	public void removePet(int ownerId)
	{
		_pets.remove(ownerId);
	}
	
	public Pet getPet(int ownerId)
	{
		return _pets.get(ownerId);
	}
	
	public static int getRegionX(int regionX)
	{
		return (regionX - REGION_X_OFFSET) * REGION_SIZE;
	}
	
	public static int getRegionY(int regionY)
	{
		return (regionY - REGION_Y_OFFSET) * REGION_SIZE;
	}
	
	/**
	 * @param loc : The Location to check.
	 * @return a {@link WorldRegion} based on a {@link Location}.
	 */
	public WorldRegion getRegion(Location loc)
	{
		return getRegion(loc.getX(), loc.getY());
	}
	
	/**
	 * @param x : The X point to check.
	 * @param y : The Y point to check.
	 * @return a {@link WorldRegion} based on X/Y coordinates.
	 */
	public WorldRegion getRegion(int x, int y)
	{
		return _worldRegions[(x - WORLD_X_MIN) / REGION_SIZE][(y - WORLD_Y_MIN) / REGION_SIZE];
	}
	
	/**
	 * BEWARE : This method is kinda expensive, avoid to use it.
	 * @param zone : The ZoneType to check.
	 * @return the first encountered {@link WorldRegion} based on a {@link ZoneType}.
	 */
	public WorldRegion getRegion(ZoneType zone)
	{
		for (int i = 0; i <= REGIONS_X; i++)
		{
			for (int j = 0; j <= REGIONS_Y; j++)
			{
				final WorldRegion region = _worldRegions[i][j];
				if (region.containsZone(zone.getId()))
					return region;
			}
		}
		return null;
	}
	
	/**
	 * @return the whole 2d array containing the world regions used by ZoneData.java to setup zones inside the world regions
	 */
	public WorldRegion[][] getWorldRegions()
	{
		return _worldRegions;
	}
	
	/**
	 * @param x X position of the object
	 * @param y Y position of the object
	 * @return True if the given coordinates are valid WorldRegion coordinates.
	 */
	private static boolean validRegion(int x, int y)
	{
		return (x >= 0 && x <= REGIONS_X && y >= 0 && y <= REGIONS_Y);
	}
	
	/**
	 * Delete all spawns in the world.
	 */
	public void deleteVisibleNpcSpawns()
	{
		LOGGER.info("Deleting all visible NPCs.");
		for (int i = 0; i <= REGIONS_X; i++)
		{
			for (int j = 0; j <= REGIONS_Y; j++)
			{
				for (WorldObject obj : _worldRegions[i][j].getObjects())
				{
					if (obj instanceof Npc)
					{
						((Npc) obj).deleteMe();
						
						final Spawn spawn = ((Npc) obj).getSpawn();
						if (spawn != null)
						{
							spawn.setRespawnState(false);
							SpawnTable.getInstance().deleteSpawn(spawn, false);
						}
					}
				}
			}
		}
		LOGGER.info("All visibles NPCs are now deleted.");
	}
	
	/**
	 * Send a packet to all online {@link Player}s present in the {@link World}.
	 * @param packet : The packet to send.
	 */
	public static void toAllOnlinePlayers(L2GameServerPacket packet)
	{
		for (Player player : World.getInstance().getPlayers())
		{
			if (player.isOnline())
				player.sendPacket(packet);
		}
	}
	
	public static void announceToOnlinePlayers(String text)
	{
		toAllOnlinePlayers(new CreatureSay(SayType.ANNOUNCEMENT, null, text));
	}
	
	public static void announceToOnlinePlayers(String text, boolean critical)
	{
		toAllOnlinePlayers(new CreatureSay((critical) ? SayType.CRITICAL_ANNOUNCE : SayType.ANNOUNCEMENT, null, text));
	}
	
	public static World getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final World INSTANCE = new World();
	}
}