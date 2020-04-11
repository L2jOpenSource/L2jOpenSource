package com.l2jfrozen.gameserver.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.L2AttackableAI;
import com.l2jfrozen.gameserver.ai.L2FortSiegeGuardAI;
import com.l2jfrozen.gameserver.ai.L2SiegeGuardAI;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.model.zone.L2ZoneManager;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.gameserver.model.zone.type.L2PeaceZone;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.object.L2ObjectSet;

/**
 * This class ...
 * @version $Revision: 1.3.4.4 $ $Date: 2005/03/27 15:29:33 $
 */
public final class L2WorldRegion
{
	private static Logger LOGGER = Logger.getLogger(L2WorldRegion.class);
	
	/**
	 * L2ObjectHashSet(L2PlayableInstance) containing L2PlayableInstance of all player & summon in game in this L2WorldRegion
	 */
	private final L2ObjectSet<L2PlayableInstance> allPlayable;
	
	/** L2ObjectHashSet(L2Object) containing L2Object visible in this L2WorldRegion */
	private final L2ObjectSet<L2Object> visibleObjects;
	
	private final List<L2WorldRegion> surroundingRegions;
	private final int tileX, tileY;
	private Boolean active = false;
	private ScheduledFuture<?> neighborsTask = null;
	
	private L2ZoneManager zoneManager;
	
	public L2WorldRegion(final int pTileX, final int pTileY)
	{
		allPlayable = L2ObjectSet.createL2PlayerSet(); // new L2ObjectHashSet<L2PcInstance>();
		visibleObjects = L2ObjectSet.createL2ObjectSet(); // new L2ObjectHashSet<L2Object>();
		surroundingRegions = new ArrayList<>();
		// surroundingRegions.add(this); //done in L2World.initRegions()
		
		tileX = pTileX;
		tileY = pTileY;
		
		// default a newly initialized region to inactive, unless always on is specified
		if (Config.GRIDS_ALWAYS_ON)
		{
			active = true;
		}
		else
		{
			active = false;
		}
	}
	
	public void addZone(final L2ZoneType zone)
	{
		if (zoneManager == null)
		{
			zoneManager = new L2ZoneManager();
		}
		zoneManager.registerNewZone(zone);
	}
	
	public void removeZone(final L2ZoneType zone)
	{
		if (zoneManager == null)
		{
			return;
		}
		
		zoneManager.unregisterZone(zone);
	}
	
	public void revalidateZones(final L2Character character)
	{
		if (zoneManager == null)
		{
			return;
		}
		
		if (zoneManager != null)
		{
			zoneManager.revalidateZones(character);
		}
	}
	
	public void removeFromZones(final L2Character character)
	{
		if (zoneManager == null)
		{
			return;
		}
		
		if (zoneManager != null)
		{
			zoneManager.removeCharacter(character);
		}
	}
	
	public void onDeath(final L2Character character)
	{
		if (zoneManager == null)
		{
			return;
		}
		
		if (zoneManager != null)
		{
			zoneManager.onDeath(character);
		}
	}
	
	public void onRevive(final L2Character character)
	{
		if (zoneManager == null)
		{
			return;
		}
		
		if (zoneManager != null)
		{
			zoneManager.onRevive(character);
		}
	}
	
	/** Task of AI notification */
	public class NeighborsTask implements Runnable
	{
		private final boolean isActivating;
		
		public NeighborsTask(final boolean isActivating)
		{
			this.isActivating = isActivating;
		}
		
		@Override
		public void run()
		{
			if (isActivating)
			{
				// for each neighbor, if it's not active, activate.
				for (final L2WorldRegion neighbor : getSurroundingRegions())
				{
					neighbor.setActive(true);
				}
			}
			else
			{
				if (areNeighborsEmpty())
				{
					setActive(false);
				}
				
				// check and deactivate
				for (final L2WorldRegion neighbor : getSurroundingRegions())
				{
					if (neighbor.areNeighborsEmpty())
					{
						neighbor.setActive(false);
					}
				}
			}
		}
	}
	
	private void switchAI(final Boolean isOn)
	{
		int c = 0;
		
		if (!isOn)
		{
			for (final L2Object o : visibleObjects)
			{
				if (o instanceof L2Attackable)
				{
					c++;
					final L2Attackable mob = (L2Attackable) o;
					
					// Set target to null and cancel Attack or Cast
					mob.setTarget(null);
					
					// Stop movement
					mob.stopMove(null);
					
					// Stop all active skills effects in progress on the L2Character
					mob.stopAllEffects();
					
					mob.clearAggroList();
					mob.getKnownList().removeAllKnownObjects();
					
					if (mob.getAI() != null)
					{
						
						mob.getAI().setIntention(com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE);
						
						// stop the ai tasks
						if (mob.getAI() instanceof L2AttackableAI)
						{
							((L2AttackableAI) mob.getAI()).stopAITask();
						}
						else if (mob.getAI() instanceof L2FortSiegeGuardAI)
						{
							((L2FortSiegeGuardAI) mob.getAI()).stopAITask();
						}
						else if (mob.getAI() instanceof L2SiegeGuardAI)
						{
							((L2SiegeGuardAI) mob.getAI()).stopAITask();
						}
						
					}
					
					// Stop HP/MP/CP Regeneration task
					// try this: allow regen, but only until mob is 100% full...then stop
					// it until the grid is made active.
					// mob.getStatus().stopHpMpRegeneration();
				}
			}
			if (Config.DEBUG)
			{
				LOGGER.info(c + " mobs were turned off");
			}
		}
		else
		{
			for (final L2Object o : visibleObjects)
			{
				if (o instanceof L2Attackable)
				{
					c++;
					// Start HP/MP/CP Regeneration task
					((L2Attackable) o).getStatus().startHpMpRegeneration();
					
					// start the ai
					// ((L2AttackableAI) mob.getAI()).startAITask();
				}
				else if (o instanceof L2NpcInstance)
				{
					// Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it
					// L2Monsterinstance/L2Attackable socials are handled by AI (TODO: check the instances)
					((L2NpcInstance) o).startRandomAnimationTimer();
				}
			}
			if (Config.DEBUG)
			{
				LOGGER.info(c + " mobs were turned on");
			}
		}
		
	}
	
	public Boolean isActive()
	{
		return active;
	}
	
	// check if all 9 neighbors (including self) are inactive or active but with no players.
	// returns true if the above condition is met.
	public Boolean areNeighborsEmpty()
	{
		// if this region is occupied, return false.
		if (isActive() && allPlayable.size() > 0)
		{
			return false;
		}
		
		// if any one of the neighbors is occupied, return false
		for (final L2WorldRegion neighbor : surroundingRegions)
		{
			if (neighbor.isActive() && neighbor.allPlayable.size() > 0)
			{
				return false;
			}
		}
		
		// in all other cases, return true.
		return true;
	}
	
	/**
	 * this function turns this region's AI and geodata on or off
	 * @param value
	 */
	public void setActive(final boolean value)
	{
		if (active == value)
		{
			return;
		}
		
		active = value;
		
		// turn the AI on or off to match the region's activation.
		switchAI(value);
		
		// TODO
		// turn the geodata on or off to match the region's activation.
		if (Config.DEBUG)
		{
			if (value)
			{
				LOGGER.info("Starting Grid " + tileX + "," + tileY);
			}
			else
			{
				LOGGER.info("Stoping Grid " + tileX + "," + tileY);
			}
		}
	}
	
	/**
	 * Immediately sets self as active and starts a timer to set neighbors as active this timer is to avoid turning on neighbors in the case when a person just teleported into a region and then teleported out immediately...there is no reason to activate all the neighbors in that case.
	 */
	private void startActivation()
	{
		// first set self to active and do self-tasks...
		setActive(true);
		
		// if the timer to deactivate neighbors is running, cancel it.
		if (neighborsTask != null)
		{
			neighborsTask.cancel(true);
			neighborsTask = null;
		}
		
		// then, set a timer to activate the neighbors
		neighborsTask = ThreadPoolManager.getInstance().scheduleGeneral(new NeighborsTask(true), 1000 * Config.GRID_NEIGHBOR_TURNON_TIME);
	}
	
	/**
	 * starts a timer to set neighbors (including self) as inactive this timer is to avoid turning off neighbors in the case when a person just moved out of a region that he may very soon return to. There is no reason to turn self & neighbors off in that case.
	 */
	private void startDeactivation()
	{
		// if the timer to activate neighbors is running, cancel it.
		if (neighborsTask != null)
		{
			neighborsTask.cancel(true);
			neighborsTask = null;
		}
		
		// start a timer to "suggest" a deactivate to self and neighbors.
		// suggest means: first check if a neighbor has L2PcInstances in it. If not, deactivate.
		neighborsTask = ThreadPoolManager.getInstance().scheduleGeneral(new NeighborsTask(false), 1000 * Config.GRID_NEIGHBOR_TURNOFF_TIME);
	}
	
	/**
	 * Add the L2Object in the L2ObjectHashSet(L2Object) visibleObjects containing L2Object visible in this L2WorldRegion <BR>
	 * If L2Object is a L2PcInstance, Add the L2PcInstance in the L2ObjectHashSet(L2PcInstance) allPlayable containing L2PcInstance of all player in game in this L2WorldRegion <BR>
	 * Assert : object.getCurrentWorldRegion() == this
	 * @param object
	 */
	public void addVisibleObject(final L2Object object)
	{
		if (Config.ASSERT)
		{
			assert object.getWorldRegion() == this;
		}
		
		if (object == null)
		{
			return;
		}
		
		visibleObjects.put(object);
		
		if (object instanceof L2PlayableInstance)
		{
			allPlayable.put((L2PlayableInstance) object);
			
			// if this is the first player to enter the region, activate self & neighbors
			if (allPlayable.size() == 1 && !Config.GRIDS_ALWAYS_ON)
			{
				startActivation();
			}
		}
	}
	
	/**
	 * Remove the L2Object from the L2ObjectHashSet(L2Object) visibleObjects in this L2WorldRegion <BR>
	 * <BR>
	 * If L2Object is a L2PcInstance, remove it from the L2ObjectHashSet(L2PcInstance) allPlayable of this L2WorldRegion <BR>
	 * Assert : object.getCurrentWorldRegion() == this || object.getCurrentWorldRegion() == null
	 * @param object
	 */
	public void removeVisibleObject(final L2Object object)
	{
		if (Config.ASSERT)
		{
			assert object.getWorldRegion() == this || object.getWorldRegion() == null;
		}
		
		if (object == null)
		{
			return;
		}
		
		visibleObjects.remove(object);
		
		if (object instanceof L2PlayableInstance)
		{
			allPlayable.remove((L2PlayableInstance) object);
			
			if (allPlayable.size() == 0 && !Config.GRIDS_ALWAYS_ON)
			{
				startDeactivation();
			}
		}
	}
	
	public void addSurroundingRegion(final L2WorldRegion region)
	{
		surroundingRegions.add(region);
	}
	
	/**
	 * @return the list surroundingRegions containing all L2WorldRegion around the current L2WorldRegion
	 */
	public List<L2WorldRegion> getSurroundingRegions()
	{
		// change to return L2WorldRegion[] ?
		// this should not change after initialization, so maybe changes are not necessary
		
		return surroundingRegions;
	}
	
	public Iterator<L2PlayableInstance> iterateAllPlayers()
	{
		return allPlayable.iterator();
	}
	
	public L2ObjectSet<L2Object> getVisibleObjects()
	{
		return visibleObjects;
	}
	
	public String getName()
	{
		return "(" + tileX + ", " + tileY + ")";
	}
	
	/**
	 * Deleted all spawns in the world.
	 */
	public synchronized void deleteVisibleNpcSpawns()
	{
		LOGGER.debug("Deleting all visible NPC's in Region: " + getName());
		for (final L2Object obj : visibleObjects)
		{
			if (obj instanceof L2NpcInstance)
			{
				L2NpcInstance target = (L2NpcInstance) obj;
				target.deleteMe();
				L2Spawn spawn = target.getSpawn();
				
				if (spawn != null)
				{
					spawn.stopRespawn();
					SpawnTable.getInstance().deleteSpawn(spawn, false);
				}
				
				LOGGER.debug("Removed NPC " + target.getObjectId());
				
				target = null;
				spawn = null;
			}
		}
		if (Config.DEBUG)
		{
			LOGGER.debug("All visible NPC's deleted in Region: " + getName());
		}
	}
	
	/**
	 * @param  skill
	 * @param  x
	 * @param  y
	 * @param  z
	 * @return
	 */
	public boolean checkEffectRangeInsidePeaceZone(final L2Skill skill, final int x, final int y, final int z)
	{
		if (zoneManager != null)
		{
			final int range = skill.getEffectRange();
			final int up = y + range;
			final int down = y - range;
			final int left = x + range;
			final int right = x - range;
			
			for (final L2ZoneType e : zoneManager.getZones())
			{
				if (e instanceof L2PeaceZone)
				{
					if (e.isInsideZone(x, up, z))
					{
						return false;
					}
					
					if (e.isInsideZone(x, down, z))
					{
						return false;
					}
					
					if (e.isInsideZone(left, y, z))
					{
						return false;
					}
					
					if (e.isInsideZone(right, y, z))
					{
						return false;
					}
					
					if (e.isInsideZone(x, y, z))
					{
						return false;
					}
				}
			}
			return true;
		}
		return true;
	}
}
