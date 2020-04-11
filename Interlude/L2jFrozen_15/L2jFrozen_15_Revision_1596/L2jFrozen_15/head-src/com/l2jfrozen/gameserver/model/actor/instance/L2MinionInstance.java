package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.gameserver.ai.L2AttackableAI;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.L2WorldRegion;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * This class manages all Minions. In a group mob, there are one master called RaidBoss and several slaves called Minions.
 * @version $Revision: 1.20.4.6 $ $Date: 2005/04/06 16:13:39 $
 */
public final class L2MinionInstance extends L2MonsterInstance
{
	// private static Logger LOGGER = Logger.getLogger(L2RaidMinionInstance.class);
	
	/** The master L2Character whose depends this L2MinionInstance on. */
	private L2MonsterInstance master;
	
	/**
	 * Constructor of L2MinionInstance (use L2Character and L2NpcInstance constructor).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the L2Character constructor to set the template of the L2MinionInstance (copy skills from template to object and link calculators to NPC_STD_CALCULATOR)</li>
	 * <li>Set the name of the L2MinionInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li><BR>
	 * <BR>
	 * @param objectId Identifier of the object to initialized
	 * @param template the template
	 */
	public L2MinionInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	/**
	 * Return True if the L2Character is minion of RaidBoss.
	 * @return true, if is raid
	 */
	@Override
	public boolean isRaid()
	{
		return getLeader() instanceof L2RaidBossInstance;
	}
	
	/**
	 * Return the master of this L2MinionInstance.<BR>
	 * <BR>
	 * @return the leader
	 */
	public L2MonsterInstance getLeader()
	{
		return master;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		// Notify Leader that Minion has Spawned
		getLeader().notifyMinionSpawned(this);
		
		// check the region where this mob is, do not activate the AI if region is inactive.
		L2WorldRegion region = L2World.getInstance().getRegion(getX(), getY());
		if (region != null && !region.isActive())
		{
			((L2AttackableAI) getAI()).stopAITask();
		}
		region = null;
	}
	
	/**
	 * Set the master of this L2MinionInstance.<BR>
	 * <BR>
	 * @param leader The L2Character that leads this L2MinionInstance
	 */
	public void setLeader(final L2MonsterInstance leader)
	{
		master = leader;
	}
	
	/**
	 * Manages the doDie event for this L2MinionInstance.<BR>
	 * <BR>
	 * @param  killer The L2Character that killed this L2MinionInstance.<BR>
	 *                    <BR>
	 * @return        true, if successful
	 */
	@Override
	public boolean doDie(final L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		master.notifyMinionDied(this);
		return true;
	}
}
