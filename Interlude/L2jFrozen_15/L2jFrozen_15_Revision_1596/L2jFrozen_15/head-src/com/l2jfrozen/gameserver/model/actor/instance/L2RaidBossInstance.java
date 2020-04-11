package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.managers.RaidBossPointsManager;
import com.l2jfrozen.gameserver.managers.RaidBossSpawnManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.random.Rnd;

/**
 * This class manages all RaidBoss. In a group mob, there are one master called RaidBoss and several slaves called Minions.
 * @version $Revision: 1.20.4.6 $ $Date: 2005/04/06 16:13:39 $
 */
public final class L2RaidBossInstance extends L2MonsterInstance
{
	
	/** The Constant RAIDBOSS_MAINTENANCE_INTERVAL. */
	private static final int RAIDBOSS_MAINTENANCE_INTERVAL = 20000; // 20 sec
	
	/** The raid status. */
	private RaidBossSpawnManager.StatusEnum raidStatus;
	
	/**
	 * Constructor of L2RaidBossInstance (use L2Character and L2NpcInstance constructor).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the L2Character constructor to set the template of the L2RaidBossInstance (copy skills from template to object and link calculators to NPC_STD_CALCULATOR)</li>
	 * <li>Set the name of the L2RaidBossInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li><BR>
	 * <BR>
	 * @param objectId Identifier of the object to initialized
	 * @param template the template
	 */
	public L2RaidBossInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public boolean isRaid()
	{
		return true;
	}
	
	@Override
	protected int getMaintenanceInterval()
	{
		return RAIDBOSS_MAINTENANCE_INTERVAL;
	}
	
	@Override
	public boolean doDie(final L2Character killer)
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
			SystemMessage msg = new SystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL);
			broadcastPacket(msg);
			msg = null;
			if (player.getParty() != null)
			{
				for (final L2PcInstance member : player.getParty().getPartyMembers())
				{
					RaidBossPointsManager.addPoints(member, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
				}
			}
			else
			{
				RaidBossPointsManager.addPoints(player, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
			}
		}
		
		if (!getSpawn().isCustomRaidBoss())
		{
			RaidBossSpawnManager.getInstance().updateStatus(this, true);
		}
		
		return true;
	}
	
	/**
	 * Spawn all minions at a regular interval Also if boss is too far from home location at the time of this check, teleport it home.
	 */
	@Override
	protected void manageMinions()
	{
		minionList.spawnMinions();
		minionMaintainTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			// teleport raid boss home if it's too far from home location
			L2Spawn bossSpawn = getSpawn();
			
			int rb_lock_range = Config.RBLOCKRAGE;
			if (Config.RBS_SPECIFIC_LOCK_RAGE.get(bossSpawn.getNpcid()) != null)
			{
				rb_lock_range = Config.RBS_SPECIFIC_LOCK_RAGE.get(bossSpawn.getNpcid());
			}
			
			if (rb_lock_range != -1 && !isInsideRadius(bossSpawn.getLocx(), bossSpawn.getLocy(), bossSpawn.getLocz(), rb_lock_range, true, false))
			{
				teleToLocation(bossSpawn.getLocx(), bossSpawn.getLocy(), bossSpawn.getLocz(), true);
				// healFull(); // Prevents minor exploiting with it
			}
			/*
			 * if(!isInsideRadius(bossSpawn.getLocx(), bossSpawn.getLocy(), bossSpawn.getLocz(), 5000, true, false)) { teleToLocation(bossSpawn.getLocx(), bossSpawn.getLocy(), bossSpawn.getLocz(), true); healFull(); // prevents minor exploiting with it }
			 */
			minionList.maintainMinions();
			bossSpawn = null;
		}, 60000, getMaintenanceInterval());
	}
	
	/**
	 * Sets the raid status.
	 * @param status the new raid status
	 */
	public void setRaidStatus(final RaidBossSpawnManager.StatusEnum status)
	{
		raidStatus = status;
	}
	
	/**
	 * Gets the raid status.
	 * @return the raid status
	 */
	public RaidBossSpawnManager.StatusEnum getRaidStatus()
	{
		return raidStatus;
	}
	
	/**
	 * Heal full.
	 */
	public void healFull()
	{
		super.setCurrentHp(super.getMaxHp());
		super.setCurrentMp(super.getMaxMp());
	}
}
