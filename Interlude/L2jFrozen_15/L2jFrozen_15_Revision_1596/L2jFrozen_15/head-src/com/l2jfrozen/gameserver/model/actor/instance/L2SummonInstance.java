package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SetSummonRemainTime;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

public class L2SummonInstance extends L2Summon
{
	protected static final Logger LOGGER = Logger.getLogger(L2SummonInstance.class);
	
	private float expPenalty = 0; // exp decrease multiplier (i.e. 0.3 (= 30%) for shadow)
	private int itemConsumeId;
	private int itemConsumeCount;
	private int itemConsumeSteps;
	private final int totalLifeTime;
	private final int timeLostIdle;
	private final int timeLostActive;
	private int timeRemaining;
	private int nextItemConsumeTime;
	public int lastShowntimeRemaining; // Following FbiAgent's example to avoid sending useless packets
	
	private Future<?> summonLifeTask;
	
	public L2SummonInstance(final int objectId, final L2NpcTemplate template, final L2PcInstance owner, final L2Skill skill)
	{
		super(objectId, template, owner);
		setShowSummonAnimation(true);
		
		if (skill != null)
		{
			itemConsumeId = skill.getItemConsumeIdOT();
			itemConsumeCount = skill.getItemConsumeOT();
			itemConsumeSteps = skill.getItemConsumeSteps();
			totalLifeTime = skill.getTotalLifeTime();
			timeLostIdle = skill.getTimeLostIdle();
			timeLostActive = skill.getTimeLostActive();
		}
		else
		{
			// defaults
			itemConsumeId = 0;
			itemConsumeCount = 0;
			itemConsumeSteps = 0;
			totalLifeTime = 1200000; // 20 minutes
			timeLostIdle = 1000;
			timeLostActive = 1000;
		}
		timeRemaining = totalLifeTime;
		lastShowntimeRemaining = totalLifeTime;
		
		if (itemConsumeId == 0)
		{
			nextItemConsumeTime = -1; // do not consume
		}
		else if (itemConsumeSteps == 0)
		{
			nextItemConsumeTime = -1; // do not consume
		}
		else
		{
			nextItemConsumeTime = totalLifeTime - totalLifeTime / (itemConsumeSteps + 1);
		}
		
		// When no item consume is defined task only need to check when summon life time has ended.
		// Otherwise have to destroy items from owner's inventory in order to let summon live.
		final int delay = 1000;
		
		if (Config.DEBUG && itemConsumeCount != 0)
		{
			LOGGER.warn("L2SummonInstance: Item Consume ID: " + itemConsumeId + ", Count: " + itemConsumeCount + ", Rate: " + itemConsumeSteps + " times.");
		}
		
		if (Config.DEBUG)
		{
			LOGGER.warn("L2SummonInstance: Task Delay " + delay / 1000 + " seconds.");
		}
		
		summonLifeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new SummonLifetime(getOwner(), this), delay, delay);
	}
	
	@Override
	public final int getLevel()
	{
		return getTemplate() != null ? getTemplate().level : 0;
	}
	
	@Override
	public int getSummonType()
	{
		return 1;
	}
	
	public void setExpPenalty(final float expPenalty)
	{
		this.expPenalty = expPenalty;
	}
	
	public float getExpPenalty()
	{
		return expPenalty;
	}
	
	public int getItemConsumeCount()
	{
		return itemConsumeCount;
	}
	
	public int getItemConsumeId()
	{
		return itemConsumeId;
	}
	
	public int getItemConsumeSteps()
	{
		return itemConsumeSteps;
	}
	
	public int getNextItemConsumeTime()
	{
		return nextItemConsumeTime;
	}
	
	public int getTotalLifeTime()
	{
		return totalLifeTime;
	}
	
	public int getTimeLostIdle()
	{
		return timeLostIdle;
	}
	
	public int getTimeLostActive()
	{
		return timeLostActive;
	}
	
	public int getTimeRemaining()
	{
		return timeRemaining;
	}
	
	public void setNextItemConsumeTime(final int value)
	{
		nextItemConsumeTime = value;
	}
	
	public void decNextItemConsumeTime(final int value)
	{
		nextItemConsumeTime -= value;
	}
	
	public void decTimeRemaining(final int value)
	{
		timeRemaining -= value;
	}
	
	public void addExpAndSp(final int addToExp, final int addToSp)
	{
		getOwner().addExpAndSp(addToExp, addToSp);
	}
	
	public void reduceCurrentHp(final int damage, final L2Character attacker)
	{
		super.reduceCurrentHp(damage, attacker);
		SystemMessage sm = new SystemMessage(SystemMessageId.SUMMON_RECEIVED_DAMAGE_S2_BY_S1);
		
		if (attacker instanceof L2NpcInstance)
		{
			sm.addNpcName(((L2NpcInstance) attacker).getTemplate().npcId);
		}
		else
		{
			sm.addString(attacker.getName());
		}
		
		sm.addNumber(damage);
		getOwner().sendPacket(sm);
		sm = null;
	}
	
	@Override
	public boolean doDie(final L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if (Config.DEBUG)
		{
			LOGGER.warn("L2SummonInstance: " + getTemplate().name + " (" + getOwner().getName() + ") has been killed.");
		}
		
		if (summonLifeTask != null)
		{
			summonLifeTask.cancel(true);
			summonLifeTask = null;
		}
		return true;
		
	}
	
	/*
	 * protected void displayHitMessage(int damage, boolean crit, boolean miss) { if (crit) { getOwner().sendPacket(new SystemMessage(SystemMessage.SUMMON_CRITICAL_HIT)); } if (miss) { getOwner().sendPacket(new SystemMessage(SystemMessage.MISSED_TARGET)); } else { SystemMessage sm = new
	 * SystemMessage(SystemMessage.SUMMON_GAVE_DAMAGE_OF_S1); sm.addNumber(damage); getOwner().sendPacket(sm); } }
	 */
	
	static class SummonLifetime implements Runnable
	{
		private final L2PcInstance activeChar;
		private final L2SummonInstance summon;
		
		SummonLifetime(final L2PcInstance activeChar, final L2SummonInstance newpet)
		{
			this.activeChar = activeChar;
			summon = newpet;
		}
		
		@Override
		public void run()
		{
			if (Config.DEBUG)
			{
				LOGGER.warn("L2SummonInstance: " + summon.getTemplate().name + " (" + activeChar.getName() + ") run task.");
			}
			
			try
			{
				final double oldTimeRemaining = summon.getTimeRemaining();
				final int maxTime = summon.getTotalLifeTime();
				double newTimeRemaining;
				
				// if pet is attacking
				if (summon.isAttackingNow())
				{
					summon.decTimeRemaining(summon.getTimeLostActive());
				}
				else
				{
					summon.decTimeRemaining(summon.getTimeLostIdle());
				}
				newTimeRemaining = summon.getTimeRemaining();
				// check if the summon's lifetime has ran out
				if (newTimeRemaining < 0)
				{
					summon.unSummon(activeChar);
				}
				// check if it is time to consume another item
				else if (newTimeRemaining <= summon.getNextItemConsumeTime() && oldTimeRemaining > summon.getNextItemConsumeTime())
				{
					summon.decNextItemConsumeTime(maxTime / (summon.getItemConsumeSteps() + 1));
					
					// check if owner has enought itemConsume, if requested
					if (summon.getItemConsumeCount() > 0 && summon.getItemConsumeId() != 0 && !summon.isDead() && !summon.destroyItemByItemId("Consume", summon.getItemConsumeId(), summon.getItemConsumeCount(), activeChar, true))
					{
						summon.unSummon(activeChar);
					}
				}
				
				// prevent useless packet-sending when the difference isn't visible.
				if (summon.lastShowntimeRemaining - newTimeRemaining > maxTime / 352)
				{
					summon.getOwner().sendPacket(new SetSummonRemainTime(maxTime, (int) newTimeRemaining));
					summon.lastShowntimeRemaining = (int) newTimeRemaining;
				}
			}
			catch (final Throwable e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				if (Config.DEBUG)
				{
					LOGGER.warn("Summon of player [#" + activeChar.getName() + "] has encountered item consumption errors: " + e);
				}
			}
		}
	}
	
	@Override
	public synchronized void unSummon(final L2PcInstance owner)
	{
		if (Config.DEBUG)
		{
			LOGGER.warn("L2SummonInstance: " + getTemplate().name + " (" + owner.getName() + ") unsummoned.");
		}
		
		if (summonLifeTask != null)
		{
			summonLifeTask.cancel(true);
			summonLifeTask = null;
		}
		
		super.unSummon(owner);
	}
	
	@Override
	public boolean destroyItem(final String process, final int objectId, final int count, final L2Object reference, final boolean sendMessage)
	{
		return getOwner().destroyItem(process, objectId, count, reference, sendMessage);
	}
	
	@Override
	public boolean destroyItemByItemId(final String process, final int itemId, final int count, final L2Object reference, final boolean sendMessage)
	{
		if (Config.DEBUG)
		{
			LOGGER.warn("L2SummonInstance: " + getTemplate().name + " (" + getOwner().getName() + ") consume.");
		}
		
		return getOwner().destroyItemByItemId(process, itemId, count, reference, sendMessage);
	}
	
	@Override
	public final void sendDamageMessage(final L2Character target, final int damage, final boolean mcrit, final boolean pcrit, final boolean miss)
	{
		if (miss)
		{
			return;
		}
		
		// Prevents the double spam of system messages, if the target is the owning player.
		if (target.getObjectId() != getOwner().getObjectId())
		{
			if (pcrit || mcrit)
			{
				getOwner().sendPacket(new SystemMessage(SystemMessageId.CRITICAL_HIT_BY_SUMMONED_MOB));
			}
			
			if (getOwner().isInOlympiadMode() && target instanceof L2PcInstance && ((L2PcInstance) target).isInOlympiadMode() && ((L2PcInstance) target).getOlympiadGameId() == getOwner().getOlympiadGameId())
			{
				Olympiad.getInstance().notifyCompetitorDamage(getOwner(), damage, getOwner().getOlympiadGameId());
			}
			
			SystemMessage sm = new SystemMessage(SystemMessageId.SUMMON_GAVE_DAMAGE_S1);
			sm.addNumber(damage);
			getOwner().sendPacket(sm);
			sm = null;
		}
	}
}
