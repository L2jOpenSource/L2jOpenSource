package com.l2jfrozen.gameserver.taskmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.AutoAttackStop;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * This class ...
 * @version $Revision: $ $Date: $
 * @author  Luca Baldi
 */
public class AttackStanceTaskManager
{
	protected static final Logger LOGGER = Logger.getLogger(AttackStanceTaskManager.class);
	
	protected Map<L2Character, Long> attackStanceTasks = new ConcurrentHashMap<>();
	
	public AttackStanceTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FightModeScheduler(), 0, 1000);
	}
	
	public static AttackStanceTaskManager getInstance()
	{
		return SingletonHolder.instance;
	}
	
	public void addAttackStanceTask(L2Character actor)
	{
		if (actor instanceof L2Summon)
		{
			final L2Summon summon = (L2Summon) actor;
			actor = summon.getOwner();
		}
		if (actor instanceof L2PcInstance)
		{
			final L2PcInstance player = (L2PcInstance) actor;
			for (final L2CubicInstance cubic : player.getCubics().values())
			{
				if (cubic.getId() != L2CubicInstance.LIFE_CUBIC)
				{
					cubic.doAction();
				}
			}
		}
		attackStanceTasks.put(actor, System.currentTimeMillis());
	}
	
	public void removeAttackStanceTask(L2Character actor)
	{
		if (actor instanceof L2Summon)
		{
			final L2Summon summon = (L2Summon) actor;
			actor = summon.getOwner();
		}
		attackStanceTasks.remove(actor);
	}
	
	public boolean getAttackStanceTask(L2Character actor)
	{
		if (actor instanceof L2Summon)
		{
			final L2Summon summon = (L2Summon) actor;
			actor = summon.getOwner();
		}
		return attackStanceTasks.containsKey(actor);
	}
	
	private class FightModeScheduler implements Runnable
	{
		protected FightModeScheduler()
		{
			// Do nothing
		}
		
		@Override
		public void run()
		{
			final Long current = System.currentTimeMillis();
			try
			{
				if (attackStanceTasks != null)
				{
					synchronized (this)
					{
						for (final L2Character actor : attackStanceTasks.keySet())
						{
							if ((current - attackStanceTasks.get(actor)) > 15000)
							{
								actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
								if (actor instanceof L2PcInstance && ((L2PcInstance) actor).getPet() != null)
								{
									((L2PcInstance) actor).getPet().broadcastPacket(new AutoAttackStop(((L2PcInstance) actor).getPet().getObjectId()));
								}
								actor.getAI().setAutoAttacking(false);
								attackStanceTasks.remove(actor);
							}
						}
					}
				}
			}
			catch (final Exception e)
			{
				// TODO: Find out the reason for exception. Unless caught here,
				// players remain in attack positions.
				LOGGER.warn("Error in FightModeScheduler: " + e.getMessage(), e);
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final AttackStanceTaskManager instance = new AttackStanceTaskManager();
	}
}
