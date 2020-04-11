package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.knownlist.MonsterKnownList;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.MinionList;
import com.l2jfrozen.util.random.Rnd;

/**
 * This class manages all Monsters. L2MonsterInstance :<BR>
 * <BR>
 * <li>L2MinionInstance</li>
 * <li>L2RaidBossInstance</li>
 * <li>L2GrandBossInstance</li>
 * @version $Revision: 1.20.4.6 $ $Date: 2005/04/06 16:13:39 $
 */
public class L2MonsterInstance extends L2Attackable
{
	// private static Logger LOGGER = Logger.getLogger(L2MonsterInstance.class);
	
	protected final MinionList minionList;
	
	protected ScheduledFuture<?> minionMaintainTask = null;
	private ScheduledFuture<?> returnToHomeTask = null;
	
	/** The Constant MONSTER_MAINTENANCE_INTERVAL. */
	private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;
	
	/**
	 * Constructor of L2MonsterInstance (use L2Character and L2NpcInstance constructor).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the L2Character constructor to set the template of the L2MonsterInstance (copy skills from template to object and link calculators to NPC_STD_CALCULATOR)</li>
	 * <li>Set the name of the L2MonsterInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li><BR>
	 * <BR>
	 * @param objectId Identifier of the object to initialized
	 * @param template the template
	 */
	public L2MonsterInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		minionList = new MinionList(this);
	}
	
	@Override
	public final MonsterKnownList getKnownList()
	{
		if (super.getKnownList() == null || !(super.getKnownList() instanceof MonsterKnownList))
		{
			setKnownList(new MonsterKnownList(this));
		}
		return (MonsterKnownList) super.getKnownList();
	}
	
	/**
	 * Return True if the attacker is not another L2MonsterInstance.<BR>
	 * <BR>
	 * @param  attacker the attacker
	 * @return          true, if is auto attackable
	 */
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		if (attacker instanceof L2MonsterInstance)
		{
			return false;
		}
		
		return !isEventMob;
	}
	
	/**
	 * Return True if the L2MonsterInstance is Agressive (aggroRange > 0).<BR>
	 * <BR>
	 * @return true, if is aggressive
	 */
	@Override
	public boolean isAggressive()
	{
		return getTemplate().aggroRange > 0 && !isEventMob;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		if (getTemplate().getMinionData() != null)
		{
			try
			{
				for (int i = 0; i < getSpawnedMinions().size(); i++)
				{
					L2MinionInstance minion = getSpawnedMinions().get(i);
					
					if (minion == null)
					{
						continue;
					}
					
					getSpawnedMinions().remove(minion);
					minion.deleteMe();
				}
				minionList.clearRespawnList();
				
				manageMinions();
			}
			catch (final NullPointerException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
			
			switch (getTemplate().npcId)
			{
				case 12372: // baium
				{
					SocialAction sa = new SocialAction(getObjectId(), 2);
					broadcastPacket(sa);
					sa = null;
				}
			}
		}
	}
	
	/**
	 * Gets the maintenance interval.
	 * @return the maintenance interval
	 */
	protected int getMaintenanceInterval()
	{
		return MONSTER_MAINTENANCE_INTERVAL;
	}
	
	/**
	 * Spawn all minions at a regular interval.
	 */
	protected void manageMinions()
	{
		minionMaintainTask = ThreadPoolManager.getInstance().scheduleGeneral(() -> minionList.spawnMinions(), getMaintenanceInterval());
	}
	
	/**
	 * Call minions.
	 */
	public void callMinions()
	{
		if (minionList.hasMinions())
		{
			for (final L2MinionInstance minion : minionList.getSpawnedMinions())
			{
				// Get actual coords of the minion and check to see if it's too far away from this L2MonsterInstance
				if (!isInsideRadius(minion, 200, false, false))
				{
					// Get the coords of the master to use as a base to move the minion to
					final int masterX = getX();
					final int masterY = getY();
					final int masterZ = getZ();
					
					// Calculate a new random coord for the minion based on the master's coord
					int minionX = masterX + Rnd.nextInt(401) - 200;
					int minionY = masterY + Rnd.nextInt(401) - 200;
					final int minionZ = masterZ;
					while (minionX != masterX + 30 && minionX != masterX - 30 || minionY != masterY + 30 && minionY != masterY - 30)
					{
						minionX = masterX + Rnd.nextInt(401) - 200;
						minionY = masterY + Rnd.nextInt(401) - 200;
					}
					
					// Move the minion to the new coords
					if (!minion.isInCombat() && !minion.isDead() && !minion.isMovementDisabled())
					{
						minion.moveToLocation(minionX, minionY, minionZ, 0);
					}
				}
			}
		}
	}
	
	/**
	 * Call minions to assist.
	 * @param attacker the attacker
	 */
	public void callMinionsToAssist(final L2Character attacker)
	{
		if (minionList.hasMinions())
		{
			List<L2MinionInstance> spawnedMinions = minionList.getSpawnedMinions();
			if (spawnedMinions != null && spawnedMinions.size() > 0)
			{
				final Iterator<L2MinionInstance> itr = spawnedMinions.iterator();
				L2MinionInstance minion;
				while (itr.hasNext())
				{
					minion = itr.next();
					// Trigger the aggro condition of the minion
					if (minion != null && !minion.isDead())
					{
						if (this instanceof L2RaidBossInstance)
						{
							minion.addDamage(attacker, 100);
						}
						else
						{
							minion.addDamage(attacker, 1);
						}
					}
				}
				spawnedMinions = null;
				minion = null;
			}
		}
	}
	
	@Override
	public boolean doDie(final L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		stopReturnToHomeTask();
		
		if (minionMaintainTask != null)
		{
			minionMaintainTask.cancel(true); // doesn't do it?
		}
		
		if (this instanceof L2RaidBossInstance)
		{
			deleteSpawnedMinions();
		}
		
		return true;
	}
	
	public List<L2MinionInstance> getSpawnedMinions()
	{
		return minionList.getSpawnedMinions();
	}
	
	/**
	 * Gets the total spawned minions instances.
	 * @return the total spawned minions instances
	 */
	public int getTotalSpawnedMinionsInstances()
	{
		return minionList.countSpawnedMinions();
	}
	
	/**
	 * Gets the total spawned minions groups.
	 * @return the total spawned minions groups
	 */
	public int getTotalSpawnedMinionsGroups()
	{
		return minionList.lazyCountSpawnedMinionsGroups();
	}
	
	/**
	 * Notify minion died.
	 * @param minion the minion
	 */
	public void notifyMinionDied(final L2MinionInstance minion)
	{
		minionList.moveMinionToRespawnList(minion);
	}
	
	/**
	 * Notify minion spawned.
	 * @param minion the minion
	 */
	public void notifyMinionSpawned(final L2MinionInstance minion)
	{
		minionList.addSpawnedMinion(minion);
	}
	
	public boolean hasMinions()
	{
		return minionList.hasMinions();
	}
	
	@Override
	public void addDamageHate(final L2Character attacker, final int damage, final int aggro)
	{
		if (!(attacker instanceof L2MonsterInstance))
		{
			super.addDamageHate(attacker, damage, aggro);
		}
	}
	
	@Override
	public void deleteMe()
	{
		if (hasMinions())
		{
			if (minionMaintainTask != null)
			{
				minionMaintainTask.cancel(true);
			}
			
			deleteSpawnedMinions();
		}
		super.deleteMe();
	}
	
	public void deleteSpawnedMinions()
	{
		List<L2MinionInstance> minions = getSpawnedMinions();
		
		for (int i = 0; i < minions.size(); i++)
		{
			L2MinionInstance minion = minions.get(i);
			
			if (minion == null)
			{
				continue;
			}
			
			minion.abortAttack();
			minion.abortCast();
			minion.deleteMe();
		}
		
		minions.clear();
		
		minionList.clearRespawnList();
	}
	
	public void startReturnToHomeTask()
	{
		if (Config.MONSTER_RETURN_DELAY > 0)
		{
			if(getSpawn() != null)
			{
				returnToHomeTask = ThreadPoolManager.getInstance().scheduleGeneral(() ->
				{
					if (!isAlikeDead() && !isDead() && !isInCombat() && !isInsideRadius(getSpawn().getLocx(), getSpawn().getLocy(), getSpawn().getLocz() + getCollisionHeight(), Config.MAX_DRIFT_RANGE + 350, true, false))
					{
						teleToLocation(getSpawn().getLocx(), getSpawn().getLocy(), getSpawn().getLocz());
					}
				}, Config.MONSTER_RETURN_DELAY);
			}
		}
	}
	
	public void stopReturnToHomeTask()
	{
		if (returnToHomeTask != null)
		{
			returnToHomeTask.cancel(true);
		}
	}
}
