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

import l2r.Config;
import l2r.gameserver.Announcements;
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
import l2r.util.Rnd;
import gr.reunion.achievementEngine.AchievementsManager;
import gr.reunion.configsEngine.CustomServerConfigs;

/**
 * This class manages all RaidBoss.<br>
 * In a group mob, there are one master called RaidBoss and several slaves called Minions.
 */
public class L2RaidBossInstance extends L2MonsterInstance
{
	private static final int RAIDBOSS_MAINTENANCE_INTERVAL = 30000; // 30 sec
	
	private RaidBossStatus _raidStatus;
	private boolean _useRaidCurse = true;
	
	/**
	 * Constructor of L2RaidBossInstance (use L2Character and L2NpcInstance constructor).<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Call the L2Character constructor to set the _template of the L2RaidBossInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)</li>
	 * <li>Set the name of the L2RaidBossInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li>
	 * </ul>
	 * @param objectId the identifier of the object to initialized
	 * @param template to apply to the NPC
	 */
	public L2RaidBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
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
				Announcements.getInstance().handleAnnounce("RaidBoss Manager: " + getName() + " defeated!", 0, true);
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
		}
		
		RaidBossSpawnManager.getInstance().updateStatus(this, true);
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
		
		final int spawnX = spawn.getX();
		final int spawnY = spawn.getY();
		final int spawnZ = spawn.getZ();
		
		// TODO: This check is not needed. We will teleport rb to his spawn loc! if there is one...
		// if (!isInCombat() && !isMovementDisabled())
		// {
		// if (!isInsideRadius(spawnX, spawnY, spawnZ, Math.max(Config.MAX_DRIFT_RANGE, 200), true, false))
		if (!isInsideRadius(spawnX, spawnY, spawnZ, Math.max(Config.MAX_DRIFT_RANGE, 9000), true, false))
		{
			teleToLocation(spawnX, spawnY, spawnZ, false);
		}
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