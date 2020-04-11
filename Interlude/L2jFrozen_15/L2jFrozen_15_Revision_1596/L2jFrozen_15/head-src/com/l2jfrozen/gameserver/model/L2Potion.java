package com.l2jfrozen.gameserver.model;

import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * This class ...
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class L2Potion extends L2Object
{
	protected static final Logger LOGGER = Logger.getLogger(L2Character.class);
	
	// private L2Character target;
	
	private Future<?> potionhpRegTask;
	private Future<?> potionmpRegTask;
	protected int milliseconds;
	protected double effect;
	protected int duration;
	private int potion;
	protected Object mpLock = new Object();
	protected Object hpLock = new Object();
	
	class PotionHpHealing implements Runnable
	{
		L2Character instance;
		
		public PotionHpHealing(final L2Character instance)
		{
			this.instance = instance;
		}
		
		@Override
		public void run()
		{
			try
			{
				synchronized (hpLock)
				{
					double nowHp = instance.getCurrentHp();
					
					if (duration == 0)
					{
						stopPotionHpRegeneration();
					}
					if (duration != 0)
					{
						nowHp += effect;
						instance.setCurrentHp(nowHp);
						duration = duration - milliseconds / 1000;
						setCurrentHpPotion2();
					}
				}
			}
			catch (final Exception e)
			{
				LOGGER.warn("Error in hp potion task:" + e);
			}
		}
	}
	
	public L2Potion(final int objectId)
	{
		super(objectId);
	}
	
	public void stopPotionHpRegeneration()
	{
		if (potionhpRegTask != null)
		{
			potionhpRegTask.cancel(false);
		}
		
		potionhpRegTask = null;
		
		if (Config.DEBUG)
		{
			LOGGER.debug("Potion HP regen stop");
		}
	}
	
	public void setCurrentHpPotion2()
	{
		if (duration == 0)
		{
			stopPotionHpRegeneration();
		}
		
	}
	
	public void setCurrentHpPotion1(final L2Character activeChar, final int item)
	{
		potion = item;
		// target = activeChar;
		
		switch (potion)
		{
			case 1540:
				double nowHp = activeChar.getCurrentHp();
				
				nowHp += 435;
				
				if (nowHp >= activeChar.getMaxHp())
				{
					nowHp = activeChar.getMaxHp();
				}
				
				activeChar.setCurrentHp(nowHp);
				break;
			case 728:
				double nowMp = activeChar.getMaxMp();
				
				nowMp += 435;
				
				if (nowMp >= activeChar.getMaxMp())
				{
					nowMp = activeChar.getMaxMp();
				}
				
				activeChar.setCurrentMp(nowMp);
				break;
			case 726:
				milliseconds = 500;
				duration = 15;
				effect = 1.5;
				startPotionMpRegeneration(activeChar);
				break;
		}
	}
	
	class PotionMpHealing implements Runnable
	{
		L2Character instance;
		
		public PotionMpHealing(final L2Character instance)
		{
			this.instance = instance;
		}
		
		@Override
		public void run()
		{
			try
			{
				synchronized (mpLock)
				{
					double nowMp = instance.getCurrentMp();
					
					if (duration == 0)
					{
						stopPotionMpRegeneration();
					}
					
					if (duration != 0)
					{
						nowMp += effect;
						instance.setCurrentMp(nowMp);
						duration = duration - milliseconds / 1000;
						setCurrentMpPotion2();
						
					}
				}
			}
			catch (final Exception e)
			{
				LOGGER.warn("error in mp potion task:" + e);
			}
		}
	}
	
	private void startPotionMpRegeneration(final L2Character activeChar)
	{
		potionmpRegTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new PotionMpHealing(activeChar), 1000, milliseconds);
		
		if (Config.DEBUG)
		{
			LOGGER.debug("Potion MP regen Started");
		}
	}
	
	public void stopPotionMpRegeneration()
	{
		if (potionmpRegTask != null)
		{
			potionmpRegTask.cancel(false);
		}
		
		potionmpRegTask = null;
		
		if (Config.DEBUG)
		{
			LOGGER.debug("Potion MP regen stop");
		}
	}
	
	public void setCurrentMpPotion2()
	{
		if (duration == 0)
		{
			stopPotionMpRegeneration();
		}
		
	}
	
	public void setCurrentMpPotion1(final L2Character activeChar, final int item)
	{
		potion = item;
		// target = activeChar;
		//
		// switch(_potion)
		// {
		// null
		// }
	}
	
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		return false;
	}
}
