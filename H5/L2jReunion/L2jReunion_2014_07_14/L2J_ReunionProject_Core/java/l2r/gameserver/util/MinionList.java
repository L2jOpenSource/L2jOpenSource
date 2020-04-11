/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.util;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastSet;
import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.model.L2MinionData;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.util.Rnd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author luisantonioa, DS
 */
public class MinionList
{
	private static Logger _log = LoggerFactory.getLogger(MinionList.class);
	
	protected final L2MonsterInstance _master;
	/** List containing the current spawned minions */
	private final List<L2MonsterInstance> _minionReferences;
	/** List containing the cached deleted minions for reuse */
	protected List<L2MonsterInstance> _reusedMinionReferences = null;
	
	public MinionList(L2MonsterInstance pMaster)
	{
		if (pMaster == null)
		{
			throw new NullPointerException("MinionList: master is null");
		}
		
		_master = pMaster;
		_minionReferences = new FastList<L2MonsterInstance>().shared();
	}
	
	/**
	 * @return list of the spawned (alive) minions.
	 */
	public List<L2MonsterInstance> getSpawnedMinions()
	{
		return _minionReferences;
	}
	
	/**
	 * Manage the spawn of Minions.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the Minion data of all Minions that must be spawn</li> <li>For each Minion type, spawn the amount of Minion needed</li><BR>
	 * <BR>
	 */
	public final void spawnMinions()
	{
		if (_master.isAlikeDead())
		{
			return;
		}
		List<L2MinionData> minions = _master.getTemplate().getMinionData();
		if (minions == null)
		{
			return;
		}
		
		int minionCount, minionId, minionsToSpawn;
		for (L2MinionData minion : minions)
		{
			minionCount = minion.getAmount();
			minionId = minion.getMinionId();
			
			minionsToSpawn = minionCount - countSpawnedMinionsById(minionId);
			if (minionsToSpawn > 0)
			{
				for (int i = 0; i < minionsToSpawn; i++)
				{
					spawnMinion(minionId);
				}
			}
		}
		// remove non-needed minions
		deleteReusedMinions();
	}
	
	/**
	 * Delete all spawned minions and try to reuse them.
	 */
	public void deleteSpawnedMinions()
	{
		if (!_minionReferences.isEmpty())
		{
			for (L2MonsterInstance minion : _minionReferences)
			{
				if (minion != null)
				{
					minion.setLeader(null);
					minion.deleteMe();
					if (_reusedMinionReferences != null)
					{
						_reusedMinionReferences.add(minion);
					}
				}
			}
			_minionReferences.clear();
		}
	}
	
	/**
	 * Delete all reused minions to prevent memory leaks.
	 */
	public void deleteReusedMinions()
	{
		if (_reusedMinionReferences != null)
		{
			_reusedMinionReferences.clear();
		}
	}
	
	// hooks
	
	/**
	 * Called on the master spawn Old minions (from previous spawn) are deleted. If master can respawn - enabled reuse of the killed minions.
	 */
	public void onMasterSpawn()
	{
		deleteSpawnedMinions();
		
		// if master has spawn and can respawn - try to reuse minions
		if ((_reusedMinionReferences == null) && (_master.getTemplate().getMinionData() != null) && (_master.getSpawn() != null) && _master.getSpawn().isRespawnEnabled())
		{
			_reusedMinionReferences = new FastList<L2MonsterInstance>().shared();
		}
	}
	
	/**
	 * Called on the minion spawn and added them in the list of the spawned minions.
	 * @param minion
	 */
	public void onMinionSpawn(L2MonsterInstance minion)
	{
		_minionReferences.add(minion);
	}
	
	/**
	 * Called on the master death/delete.
	 * @param force if true - force delete of the spawned minions By default minions deleted only for raidbosses
	 */
	public void onMasterDie(boolean force)
	{
		if (_master.isRaid() || force)
		{
			deleteSpawnedMinions();
		}
	}
	
	/**
	 * Called on the minion death/delete. Removed minion from the list of the spawned minions and reuse if possible.
	 * @param minion
	 * @param respawnTime (ms) enable respawning of this minion while master is alive. -1 - use default value: 0 (disable) for mobs and config value for raids.
	 */
	public void onMinionDie(L2MonsterInstance minion, int respawnTime)
	{
		minion.setLeader(null); // prevent memory leaks
		_minionReferences.remove(minion);
		if (_reusedMinionReferences != null)
		{
			_reusedMinionReferences.add(minion);
		}
		
		final int time = respawnTime < 0 ? _master.isRaid() ? (int) Config.RAID_MINION_RESPAWN_TIMER : 0 : respawnTime;
		if ((time > 0) && !_master.isAlikeDead())
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new MinionRespawnTask(minion), time);
		}
	}
	
	/**
	 * Called if master/minion was attacked. Master and all free minions receive aggro against attacker.
	 * @param caller
	 * @param attacker
	 */
	public void onAssist(L2Character caller, L2Character attacker)
	{
		if (attacker == null)
		{
			return;
		}
		
		if (!_master.isAlikeDead() && !_master.isInCombat())
		{
			_master.addDamageHate(attacker, 0, 1);
		}
		
		final boolean callerIsMaster = caller == _master;
		int aggro = callerIsMaster ? 10 : 1;
		if (_master.isRaid())
		{
			aggro *= 10;
		}
		
		for (L2MonsterInstance minion : _minionReferences)
		{
			if ((minion != null) && !minion.isDead() && (callerIsMaster || !minion.isInCombat()))
			{
				minion.addDamageHate(attacker, 0, aggro);
			}
		}
	}
	
	/**
	 * Called from onTeleported() of the master Alive and able to move minions teleported to master.
	 */
	public void onMasterTeleported()
	{
		final int offset = 200;
		final int minRadius = (int) _master.getCollisionRadius() + 30;
		
		for (L2MonsterInstance minion : _minionReferences)
		{
			if ((minion != null) && !minion.isDead() && !minion.isMovementDisabled())
			{
				int newX = Rnd.get(minRadius * 2, offset * 2); // x
				int newY = Rnd.get(newX, offset * 2); // distance
				newY = (int) Math.sqrt((newY * newY) - (newX * newX)); // y
				if (newX > (offset + minRadius))
				{
					newX = (_master.getX() + newX) - offset;
				}
				else
				{
					newX = (_master.getX() - newX) + minRadius;
				}
				if (newY > (offset + minRadius))
				{
					newY = (_master.getY() + newY) - offset;
				}
				else
				{
					newY = (_master.getY() - newY) + minRadius;
				}
				
				minion.teleToLocation(newX, newY, _master.getZ());
			}
		}
	}
	
	private final void spawnMinion(int minionId)
	{
		if (minionId == 0)
		{
			return;
		}
		
		// searching in reused minions
		if ((_reusedMinionReferences != null) && !_reusedMinionReferences.isEmpty())
		{
			L2MonsterInstance minion;
			Iterator<L2MonsterInstance> iter = _reusedMinionReferences.iterator();
			while (iter.hasNext())
			{
				minion = iter.next();
				if ((minion != null) && (minion.getId() == minionId))
				{
					iter.remove();
					minion.refreshID();
					initializeNpcInstance(_master, minion);
					return;
				}
			}
		}
		// not found in cache
		spawnMinion(_master, minionId);
	}
	
	private final class MinionRespawnTask implements Runnable
	{
		private final L2MonsterInstance _minion;
		
		public MinionRespawnTask(L2MonsterInstance minion)
		{
			_minion = minion;
		}
		
		@Override
		public void run()
		{
			if (!_master.isAlikeDead() && _master.isVisible())
			{
				// minion can be already spawned or deleted
				if (!_minion.isVisible())
				{
					if (_reusedMinionReferences != null)
					{
						_reusedMinionReferences.remove(_minion);
					}
					
					_minion.refreshID();
					initializeNpcInstance(_master, _minion);
				}
			}
		}
	}
	
	/**
	 * Init a Minion and add it in the world as a visible object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the template of the Minion to spawn</li> <li>Create and Init the Minion and generate its Identifier</li> <li>Set the Minion HP, MP and Heading</li> <li>Set the Minion leader to this RaidBoss</li> <li>Init the position of the Minion and add it in the world as a visible object</li><BR>
	 * <BR>
	 * @param master L2MonsterInstance used as master for this minion
	 * @param minionId The L2NpcTemplate Identifier of the Minion to spawn
	 * @return
	 */
	public static final L2MonsterInstance spawnMinion(L2MonsterInstance master, int minionId)
	{
		// Get the template of the Minion to spawn
		L2NpcTemplate minionTemplate = NpcTable.getInstance().getTemplate(minionId);
		if (minionTemplate == null)
		{
			return null;
		}
		
		// Create and Init the Minion and generate its Identifier
		L2MonsterInstance minion = new L2MonsterInstance(IdFactory.getInstance().getNextId(), minionTemplate);
		return initializeNpcInstance(master, minion);
	}
	
	protected static final L2MonsterInstance initializeNpcInstance(L2MonsterInstance master, L2MonsterInstance minion)
	{
		minion.stopAllEffects();
		minion.setIsDead(false);
		minion.setDecayed(false);
		
		// Set the Minion HP, MP and Heading
		minion.setCurrentHpMp(minion.getMaxHp(), minion.getMaxMp());
		minion.setHeading(master.getHeading());
		
		// Set the Minion leader to this RaidBoss
		minion.setLeader(master);
		
		// move monster to masters instance
		minion.setInstanceId(master.getInstanceId());
		
		// Init the position of the Minion and add it in the world as a visible object
		final int offset = 200;
		final int minRadius = (int) master.getCollisionRadius() + 30;
		
		int newX = Rnd.get(minRadius * 2, offset * 2); // x
		int newY = Rnd.get(newX, offset * 2); // distance
		newY = (int) Math.sqrt((newY * newY) - (newX * newX)); // y
		if (newX > (offset + minRadius))
		{
			newX = (master.getX() + newX) - offset;
		}
		else
		{
			newX = (master.getX() - newX) + minRadius;
		}
		if (newY > (offset + minRadius))
		{
			newY = (master.getY() + newY) - offset;
		}
		else
		{
			newY = (master.getY() - newY) + minRadius;
		}
		
		minion.spawnMe(newX, newY, master.getZ());
		
		if (Config.DEBUG)
		{
			_log.info("Spawned minion template " + minion.getId() + " with objid: " + minion.getObjectId() + " to boss " + master.getObjectId() + " ,at: " + minion.getX() + " x, " + minion.getY() + " y, " + minion.getZ() + " z");
		}
		
		return minion;
	}
	
	// Statistics part
	
	private final int countSpawnedMinionsById(int minionId)
	{
		int count = 0;
		for (L2MonsterInstance minion : _minionReferences)
		{
			if ((minion != null) && (minion.getId() == minionId))
			{
				count++;
			}
		}
		return count;
	}
	
	public final int countSpawnedMinions()
	{
		return _minionReferences.size();
	}
	
	public final int lazyCountSpawnedMinionsGroups()
	{
		Set<Integer> seenGroups = new FastSet<>();
		for (L2MonsterInstance minion : _minionReferences)
		{
			if (minion == null)
			{
				continue;
			}
			
			seenGroups.add(minion.getId());
		}
		return seenGroups.size();
	}
}
