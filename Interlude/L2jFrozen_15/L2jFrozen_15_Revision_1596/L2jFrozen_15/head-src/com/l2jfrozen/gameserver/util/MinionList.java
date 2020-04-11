package com.l2jfrozen.gameserver.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.L2MinionData;
import com.l2jfrozen.gameserver.model.actor.instance.L2MinionInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.util.random.Rnd;

import javolution.util.FastSet;
import main.data.memory.ObjectData;
import main.holders.objects.ObjectHolder;

/**
 * @author luisantonioa
 */
public class MinionList
{
	private static Logger LOGGER = Logger.getLogger(L2MonsterInstance.class);
	
	/** List containing the current spawned minions for this L2MonsterInstance */
	private final List<L2MinionInstance> minionReferences;
	protected Map<Long, Integer> respawnTasks = new ConcurrentHashMap<>();
	private final L2MonsterInstance master;
	
	public MinionList(final L2MonsterInstance pMaster)
	{
		minionReferences = new ArrayList<>();
		master = pMaster;
	}
	
	public int countSpawnedMinions()
	{
		synchronized (minionReferences)
		{
			return minionReferences.size();
		}
	}
	
	public int countSpawnedMinionsById(final int minionId)
	{
		int count = 0;
		synchronized (minionReferences)
		{
			for (L2MinionInstance minion : getSpawnedMinions())
			{
				if (minion == null)
				{
					continue;
				}
				
				if (minion.getNpcId() == minionId)
				{
					count++;
				}
			}
		}
		return count;
	}
	
	public boolean hasMinions()
	{
		return getSpawnedMinions().size() > 0;
	}
	
	public List<L2MinionInstance> getSpawnedMinions()
	{
		return minionReferences;
	}
	
	public void addSpawnedMinion(final L2MinionInstance minion)
	{
		synchronized (minionReferences)
		{
			minionReferences.add(minion);
		}
	}
	
	public int lazyCountSpawnedMinionsGroups()
	{
		final Set<Integer> seenGroups = new FastSet<>();
		for (final L2MinionInstance minion : getSpawnedMinions())
		{
			seenGroups.add(minion.getNpcId());
		}
		return seenGroups.size();
	}
	
	public void removeSpawnedMinion(final L2MinionInstance minion)
	{
		synchronized (minionReferences)
		{
			minionReferences.remove(minion);
		}
	}
	
	public void moveMinionToRespawnList(final L2MinionInstance minion)
	{
		final Long current = System.currentTimeMillis();
		synchronized (minionReferences)
		{
			minionReferences.remove(minion);
			if (respawnTasks.get(current) == null)
			{
				respawnTasks.put(current, minion.getNpcId());
			}
			else
			{
				// nice AoE
				for (int i = 1; i < 30; i++)
				{
					if (respawnTasks.get(current + i) == null)
					{
						respawnTasks.put(current + i, minion.getNpcId());
						break;
					}
				}
			}
		}
	}
	
	public void clearRespawnList()
	{
		respawnTasks.clear();
	}
	
	/**
	 * Manage respawning of minions for this RaidBoss.<BR>
	 * <BR>
	 */
	public void maintainMinions()
	{
		if (master == null || master.isAlikeDead())
		{
			return;
		}
		
		final Long current = System.currentTimeMillis();
		
		if (respawnTasks != null)
		{
			for (final long deathTime : respawnTasks.keySet())
			{
				final double delay = Config.RAID_MINION_RESPAWN_TIMER;
				
				if (current - deathTime > delay)
				{
					spawnSingleMinion(respawnTasks.get(deathTime));
					respawnTasks.remove(deathTime);
				}
			}
		}
	}
	
	/**
	 * Manage the spawn of all Minions of this RaidBoss.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the Minion data of all Minions that must be spawn</li>
	 * <li>For each Minion type, spawn the amount of Minion needed</li><BR>
	 */
	public void spawnMinions()
	{
		if (master == null || master.isAlikeDead())
		{
			return;
		}
		
		final List<L2MinionData> minions = master.getTemplate().getMinionData();
		
		synchronized (minionReferences)
		{
			int minionCount, minionId, minionsToSpawn;
			
			for (final L2MinionData minion : minions)
			{
				minionCount = minion.getAmount();
				minionId = minion.getMinionId();
				
				minionsToSpawn = minionCount - countSpawnedMinionsById(minionId);
				
				for (int i = 0; i < minionsToSpawn; i++)
				{
					spawnSingleMinion(minionId);
				}
			}
		}
	}
	
	/**
	 * Init a Minion and add it in the world as a visible object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the template of the Minion to spawn</li>
	 * <li>Create and Init the Minion and generate its Identifier</li>
	 * <li>Set the Minion HP, MP and Heading</li>
	 * <li>Set the Minion leader to this RaidBoss</li>
	 * <li>Init the position of the Minion and add it in the world as a visible object</li><BR>
	 * <BR>
	 * @param minionid The I2NpcTemplate Identifier of the Minion to spawn
	 */
	public void spawnSingleMinion(final int minionid)
	{
		// Get the template of the Minion to spawn
		final L2NpcTemplate minionTemplate = NpcTable.getInstance().getTemplate(minionid);
		
		// Create and Init the Minion and generate its Identifier
		final L2MinionInstance monster = new L2MinionInstance(IdFactory.getInstance().getNextId(), minionTemplate);
		
		// Set the Minion HP, MP and Heading
		monster.setCurrentHpMp(monster.getMaxHp(), monster.getMaxMp());
		monster.setHeading(master.getHeading());
		
		// Set the Minion leader to this RaidBoss
		monster.setLeader(master);
		
		ObjectHolder leader = ObjectData.get(ObjectHolder.class, master);
		if (leader != null && leader.getWorldId() != 0)
		{
			ObjectData.get(ObjectHolder.class, monster).setWorldId(leader.getWorldId());
		}
		
		// Init the position of the Minion and add it in the world as a visible object
		int spawnConstant;
		final int randSpawnLim = 170;
		int randPlusMin = 1;
		spawnConstant = Rnd.nextInt(randSpawnLim);
		// randomize +/-
		randPlusMin = Rnd.nextInt(2);
		if (randPlusMin == 1)
		{
			spawnConstant *= -1;
		}
		
		final int newX = master.getX() + spawnConstant;
		spawnConstant = Rnd.nextInt(randSpawnLim);
		// randomize +/-
		randPlusMin = Rnd.nextInt(2);
		
		if (randPlusMin == 1)
		{
			spawnConstant *= -1;
		}
		
		final int newY = master.getY() + spawnConstant;
		
		monster.spawnMe(newX, newY, master.getZ());
		
		if (Config.DEBUG)
		{
			LOGGER.debug("Spawned minion template " + minionTemplate.npcId + " with objid: " + monster.getObjectId() + " to boss " + master.getObjectId() + " ,at: " + monster.getX() + " x, " + monster.getY() + " y, " + monster.getZ() + " z");
		}
	}
}
