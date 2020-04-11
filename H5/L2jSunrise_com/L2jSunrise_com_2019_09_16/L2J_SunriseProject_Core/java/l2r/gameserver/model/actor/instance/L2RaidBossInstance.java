/*
 * Copyright (C) 2004-2015 L2J Server
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

import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.enums.RaidBossStatus;
import l2r.gameserver.instancemanager.RaidBossPointsManager;
import l2r.gameserver.instancemanager.RaidBossSpawnManager;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Broadcast;
import l2r.util.Rnd;

import gr.sr.achievementEngine.AchievementsManager;
import gr.sr.configsEngine.configs.impl.CustomServerConfigs;
import gr.sr.raidEngine.manager.RaidManager;

/**
 * This class manages all RaidBoss.<br>
 * In a group mob, there are one master called RaidBoss and several slaves called Minions.
 */
public class L2RaidBossInstance extends L2MonsterInstance
{
	private static final int RAIDBOSS_MAINTENANCE_INTERVAL = 30000; // 30 sec
	
	private RaidBossStatus _raidStatus;
	private boolean _useRaidCurse = true;
	private boolean _isEventRaid = false;
	
	public boolean isEventRaid()
	{
		return _isEventRaid;
	}
	
	public void setIsEventRaid(boolean isEventRaid)
	{
		_isEventRaid = isEventRaid;
	}
	
	/**
	 * Creates a raid boss.
	 * @param template the raid boss template
	 */
	public L2RaidBossInstance(L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2RaidBossInstance);
		setIsRaid(true);
		setLethalable(false);
	}
	
	@Override
	public void onSpawn()
	{
		setIsNoRndWalk(true);
		super.onSpawn();
	}
	
	@Override
	protected int getMaintenanceInterval()
	{
		return RAIDBOSS_MAINTENANCE_INTERVAL;
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		L2PcInstance player = null;
		if (killer instanceof L2PcInstance)
		{
			player = (L2PcInstance) killer;
		}
		else if (killer instanceof L2Summon)
		{
			player = ((L2Summon) killer).getOwner();
		}
		
		if (player != null)
		{
			// TODO: Find Better way! (Achievement function)
			if (getId() == AchievementsManager.getInstance().getMobId())
			{
				player.setKilledSpecificMob(true);
			}
			
			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
			
			if (CustomServerConfigs.ANNOUNCE_DEATH_REVIVE_OF_RAIDS)
			{
				Broadcast.toAllOnlinePlayers("RaidBoss Manager: " + getName() + " defeated!", true);
			}
			
			if (player.getParty() != null)
			{
				for (L2PcInstance member : player.getParty().getMembers())
				{
					RaidBossPointsManager.getInstance().addPoints(member, getId(), (getLevel() / 2) + Rnd.get(-5, 5));
					if (member.isNoble())
					{
						Hero.getInstance().setRBkilled(member.getObjectId(), getId());
					}
				}
			}
			else
			{
				RaidBossPointsManager.getInstance().addPoints(player, getId(), (getLevel() / 2) + Rnd.get(-5, 5));
				if (player.isNoble())
				{
					Hero.getInstance().setRBkilled(player.getObjectId(), getId());
				}
			}
			
			RaidManager.getInstance().onRaidDeath(this, player);
		}
		
		if (!isEventRaid())
		{
			RaidBossSpawnManager.getInstance().updateStatus(this, true);
		}
		return true;
	}
	
	/**
	 * Spawn all minions at a regular interval Also if boss is too far from home location at the time of this check, teleport it home.
	 */
	@Override
	protected void startMaintenanceTask()
	{
		if (getTemplate().getMinionData() != null)
		{
			getMinionList().spawnMinions();
		}
		
		_maintenanceTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> checkAndReturnToSpawn(), 60000, getMaintenanceInterval() + Rnd.get(5000));
	}
	
	protected void checkAndReturnToSpawn()
	{
		if (isDead() || isMovementDisabled() || !canReturnToSpawnPoint())
		{
			return;
		}
		
		final L2Spawn spawn = getSpawn();
		if (spawn == null)
		{
			return;
		}
		
		// vGodFather force return raid to spawn loc if range is higher of 9000(custom)
		if (!isInsideRadius(spawn.getX(), spawn.getY(), spawn.getZ(), Math.max(Config.MAX_DRIFT_RANGE, 9000), true, false))
		{
			teleToLocation(spawn.getLocation(), false);
		}
		
		// if (!isInCombat() && !isMovementDisabled())
		// {
		// if (!isInsideRadius(spawn.getX(), spawn.getY(), spawn.getZ(), Math.max(Config.MAX_DRIFT_RANGE, 200), true, false))
		// {
		// teleToLocation(spawn.getLocation(), 0);
		// }
		// }
	}
	
	public void setRaidStatus(RaidBossStatus status)
	{
		_raidStatus = status;
	}
	
	public RaidBossStatus getRaidStatus()
	{
		return _raidStatus;
	}
	
	@Override
	public float getVitalityPoints(int damage)
	{
		return -super.getVitalityPoints(damage) / 100;
	}
	
	@Override
	public boolean useVitalityRate()
	{
		return false;
	}
	
	public void setUseRaidCurse(boolean val)
	{
		_useRaidCurse = val;
	}
	
	@Override
	public boolean giveRaidCurse()
	{
		return _useRaidCurse;
	}
}
