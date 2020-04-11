package com.l2jfrozen.gameserver.model.zone.type;

import java.util.Collection;
import java.util.concurrent.Future;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * A damage zone
 * @author durgus
 */
public class L2DamageZone extends L2ZoneType
{
	private int damagePerSec;
	private Future<?> task;
	
	public L2DamageZone(final int id)
	{
		super(id);
		
		// Setup default damage
		damagePerSec = 100;
	}
	
	@Override
	public void setParameter(final String name, final String value)
	{
		if (name.equals("dmgSec"))
		{
			damagePerSec = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(final L2Character character)
	{
		if (task == null)
		{
			task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ApplyDamage(this), 10, 1000);
		}
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
		if (characterList.isEmpty())
		{
			task.cancel(true);
			task = null;
		}
	}
	
	protected Collection<L2Character> getCharacterList()
	{
		return characterList.values();
	}
	
	protected int getDamagePerSecond()
	{
		return damagePerSec;
	}
	
	class ApplyDamage implements Runnable
	{
		private final L2DamageZone dmgZone;
		
		ApplyDamage(final L2DamageZone zone)
		{
			dmgZone = zone;
		}
		
		@Override
		public void run()
		{
			for (final L2Character temp : dmgZone.getCharacterList())
			{
				if (temp != null && !temp.isDead() && temp instanceof L2PcInstance)
				{
					temp.reduceCurrentHp(dmgZone.getDamagePerSecond(), null);
				}
			}
		}
	}
	
	@Override
	protected void onDieInside(final L2Character character)
	{
	}
	
	@Override
	protected void onReviveInside(final L2Character character)
	{
	}
	
}
