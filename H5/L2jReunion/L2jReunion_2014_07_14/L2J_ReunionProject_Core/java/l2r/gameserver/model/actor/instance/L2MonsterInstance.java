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
package l2r.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.knownlist.MonsterKnownList;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.util.MinionList;
import l2r.util.Rnd;
import gr.reunion.achievementEngine.AchievementsManager;

/**
 * This class manages all Monsters. L2MonsterInstance:
 * <ul>
 * <li>L2MinionInstance</li>
 * <li>L2RaidBossInstance</li>
 * <li>L2GrandBossInstance</li>
 * </ul>
 */
public class L2MonsterInstance extends L2Attackable
{
	protected boolean _enableMinions = true;
	
	private L2MonsterInstance _master = null;
	private volatile MinionList _minionList = null;
	
	protected ScheduledFuture<?> _maintenanceTask = null;
	
	private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;
	
	/**
	 * Constructor of L2MonsterInstance (use L2Character and L2NpcInstance constructor).<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Call the L2Character constructor to set the _template of the L2MonsterInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)</li>
	 * <li>Set the name of the L2MonsterInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li>
	 * </ul>
	 * @param objectId the identifier of the object to initialized
	 * @param template to apply to the NPC
	 */
	public L2MonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2MonsterInstance);
		setAutoAttackable(true);
	}
	
	@Override
	public final MonsterKnownList getKnownList()
	{
		return (MonsterKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new MonsterKnownList(this));
	}
	
	/**
	 * Return True if the attacker is not another L2MonsterInstance.
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return super.isAutoAttackable(attacker);
	}
	
	/**
	 * Return True if the L2MonsterInstance is Agressive (aggroRange > 0).
	 */
	@Override
	public boolean isAggressive()
	{
		return (getAggroRange() > 0);
	}
	
	@Override
	public void onSpawn()
	{
		if (!isTeleporting())
		{
			if (getLeader() != null)
			{
				setIsNoRndWalk(true);
				setIsRaidMinion(getLeader().isRaid());
				getLeader().getMinionList().onMinionSpawn(this);
			}
			
			// delete spawned minions before dynamic minions spawned by script
			if (hasMinions())
			{
				getMinionList().onMasterSpawn();
			}
			
			startMaintenanceTask();
		}
		
		// dynamic script-based minions spawned here, after all preparations.
		super.onSpawn();
	}
	
	@Override
	public void onTeleported()
	{
		super.onTeleported();
		
		if (hasMinions())
		{
			getMinionList().onMasterTeleported();
		}
	}
	
	protected int getMaintenanceInterval()
	{
		return MONSTER_MAINTENANCE_INTERVAL;
	}
	
	/**
	 * Spawn all minions at a regular interval
	 */
	protected void startMaintenanceTask()
	{
		// maintenance task now used only for minions spawn
		if (getTemplate().getMinionData() == null)
		{
			return;
		}
		
		if (_maintenanceTask == null)
		{
			_maintenanceTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					if (_enableMinions)
					{
						getMinionList().spawnMinions();
					}
				}
			}, getMaintenanceInterval() + Rnd.get(1000));
		}
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		// TODO: Find Better way! (Achievement function)
		if ((killer != null) && killer.isPlayer())
		{
			if (getId() == AchievementsManager.getInstance().getMobId())
			{
				((L2PcInstance) killer).setKilledSpecificMob(true);
			}
		}
		
		if (_returnToSpawnTask != null)
		{
			_returnToSpawnTask.cancel(true);
		}
		
		if (_maintenanceTask != null)
		{
			_maintenanceTask.cancel(false); // doesn't do it?
			_maintenanceTask = null;
		}
		
		return true;
	}
	
	@Override
	public void deleteMe()
	{
		if (_returnToSpawnTask != null)
		{
			_returnToSpawnTask.cancel(true);
		}
		
		if (_maintenanceTask != null)
		{
			_maintenanceTask.cancel(false);
			_maintenanceTask = null;
		}
		
		if (hasMinions())
		{
			getMinionList().onMasterDie(true);
		}
		
		if (getLeader() != null)
		{
			getLeader().getMinionList().onMinionDie(this, 0);
		}
		
		super.deleteMe();
	}
	
	@Override
	public L2MonsterInstance getLeader()
	{
		return _master;
	}
	
	public void setLeader(L2MonsterInstance leader)
	{
		_master = leader;
	}
	
	public void enableMinions(boolean b)
	{
		_enableMinions = b;
	}
	
	public boolean hasMinions()
	{
		return _minionList != null;
	}
	
	public MinionList getMinionList()
	{
		if (_minionList == null)
		{
			synchronized (this)
			{
				if (_minionList == null)
				{
					_minionList = new MinionList(this);
				}
			}
		}
		
		return _minionList;
	}
	
	@Override
	public boolean isMonster()
	{
		return true;
	}
	
	/**
	 * @return true if this L2MonsterInstance (or its master) is registered in WalkingManager
	 */
	@Override
	public boolean isWalker()
	{
		return ((getLeader() == null) ? super.isWalker() : getLeader().isWalker());
	}
	
	@Override
	public boolean isRunner()
	{
		return ((getLeader() == null) ? super.isRunner() : getLeader().isRunner());
	}
	
	/**
	 * @return {@code true} if this L2MonsterInstance is not raid minion, master state otherwise.
	 */
	@Override
	public boolean giveRaidCurse()
	{
		return (isRaidMinion() && (getLeader() != null)) ? getLeader().giveRaidCurse() : super.giveRaidCurse();
	}
	
	// Valanths
	protected ScheduledFuture<?> _returnToSpawnTask = null;
	private boolean _isMoveToSpawn = false;
	
	public void returnToSpawn()
	{
		setIsMoveToSpawn(true);
		L2Spawn spawn = getSpawn();
		if (spawn == null)
		{
			return;
		}
		
		final int spawnX = spawn.getX();
		final int spawnY = spawn.getY();
		final int spawnZ = spawn.getZ();
		_returnToSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				if ((!L2MonsterInstance.this.isInCombat()) && (!L2MonsterInstance.this.isAlikeDead()) && (!L2MonsterInstance.this.isDead()))
				{
					clearAggroList();
					moveToLocation(spawnX, spawnY, spawnZ, 0);
				}
				setIsMoveToSpawn(false);
			}
		}, 15000);
	}
	
	public final boolean isMoveToSpawn()
	{
		return _isMoveToSpawn;
	}
	
	public final void setIsMoveToSpawn(boolean value)
	{
		_isMoveToSpawn = value;
	}
	
	private boolean _isPassive = false;
	
	public void setPassive(boolean state)
	{
		_isPassive = state;
	}
	
	public boolean isPassive()
	{
		return _isPassive;
	}
}
