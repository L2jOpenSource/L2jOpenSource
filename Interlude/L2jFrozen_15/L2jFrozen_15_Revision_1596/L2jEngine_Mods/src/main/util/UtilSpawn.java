package main.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.gameserver.datatables.csv.DoorTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.templates.L2CharTemplate;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.util.random.Rnd;

import main.concurrent.ThreadPool;
import main.data.memory.ObjectData;
import main.data.memory.WorldData;
import main.enums.TeamType;
import main.holders.objects.NpcHolder;
import main.holders.objects.ObjectHolder;

/**
 * @author fissban
 */
public class UtilSpawn
{
	public static final Logger LOG = Logger.getLogger(UtilSpawn.class.getName());
	
	public static ObjectHolder door(int id, boolean close, int worldId)
	{
		L2DoorInstance oriDoor = DoorTable.getInstance().getDoor(id);
		L2CharTemplate template = oriDoor.getTemplate();
		
		// create door instance
		L2DoorInstance newDoor = new L2DoorInstance(IdFactory.getInstance().getNextId(), template, id, "engine", true);
		newDoor.setCurrentHpMp(newDoor.getStat().getMaxHp(), newDoor.getStat().getMaxMp());
		newDoor.setIsOpen(!close);
		// newDoor.getWorldPosition().set(oriDoor.getX(), oriDoor.getY(), oriDoor.getZ());
		// if (close)
		// {
		// GeoEngine.getInstance().addGeoObject(newDoor);
		// }
		// else
		// {
		// GeoEngine.getInstance().removeGeoObject(newDoor);
		// }
		
		newDoor.broadcastStatusUpdate();
		
		ObjectHolder oh = ObjectData.get(ObjectHolder.class, newDoor);
		
		addObjectInWorld(oh, worldId);
		
		newDoor.spawnMe();
		
		return oh;
	}
	
	public static NpcHolder npc(int npcId, Location loc, int randomOffset, long despawnDelay, TeamType teamType, int worldId)
	{
		return npc(npcId, loc.getX(), loc.getY(), loc.getZ(), 0, randomOffset, despawnDelay, teamType, worldId);
	}
	
	public static NpcHolder npc(int npcId, int x, int y, int z, int heading, int randomOffset, long despawnDelay, TeamType teamType, int worldId)
	{
		NpcHolder nh = null;
		
		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			
			if ((x == 0) && (y == 0))
			{
				LOG.log(Level.SEVERE, "UtilSpawn: Failed to adjust bad locks for mod spawn! Spawn aborted!");
				return null;
			}
			
			if (randomOffset > 0)
			{
				x += Rnd.get(-randomOffset, randomOffset);
				y += Rnd.get(-randomOffset, randomOffset);
			}
			
			final L2Spawn spawn = new L2Spawn(template);
			spawn.setAmount(1);
			spawn.setLocx(x);
			spawn.setLocy(y);
			spawn.setLocz(z + 20);
			spawn.setHeading(heading);
			
			L2NpcInstance npcInstance = doSpawn(spawn, worldId, teamType, true);
			// Npc npcInstance = spawn.doSpawn(true);
			nh = ObjectData.get(NpcHolder.class, npcInstance);
			
			if (despawnDelay > 0)
			{
				ThreadPool.schedule(() -> npcInstance.deleteMe(), despawnDelay);
			}
		}
		catch (Exception e1)
		{
			LOG.warning("Could not spawn Npc " + npcId);
			e1.printStackTrace();
		}
		
		return nh;
	}
	
	private static L2NpcInstance doSpawn(L2Spawn spawn, int worldId, TeamType team, boolean isSummonSpawn)
	{
		try
		{
			// Check if the L2Spawn is not a Pet.
			if (spawn.getTemplate().getType().equals("L2Pet"))
			{
				return null;
			}
			
			// Get L2Npc Init parameters and its generate an Identifier
			Object[] parameters =
			{
				IdFactory.getInstance().getNextId(),
				spawn.getTemplate()
			};
			
			Object tmp = spawn.getConstrcutor().newInstance(parameters);
			
			// Check if the Instance is a L2Npc
			if (!(tmp instanceof L2NpcInstance))
			{
				return null;
			}
			
			spawn.setLastSpawn((L2NpcInstance) tmp);
			
			// initialize Npc and spawn it
			spawn.intializeNpcInstance(spawn.getLastSpawn());
			
			NpcHolder nh = ObjectData.get(NpcHolder.class, spawn.getLastSpawn());
			
			if (nh == null)
			{
				System.out.println("WTF no se creo el NPC " + spawn.getLastSpawn().getNpcId());
				return null;
			}
			
			// check if world exist
			addObjectInWorld(nh, worldId);
			
			nh.setTeam(team);
			
			return spawn.getLastSpawn();
		}
		catch (Exception e)
		{
			LOG.log(Level.SEVERE, "UtilSpawn: Error during spawn, NPC id=" + spawn.getTemplate().getNpcId());
			e.printStackTrace();
			return null;
		}
	}
	
	private static void addObjectInWorld(ObjectHolder o, int worldId)
	{
		if (worldId > 0)
		{
			if (!WorldData.existWorld(worldId))
			{
				LOG.log(Level.SEVERE, "UtilSpawn: Cant spawn object " + o.getInstance().getName() + " in world " + worldId + " since that world does not exist.");
			}
			else
			{
				WorldData.get(worldId).add(o);
			}
		}
	}
}
