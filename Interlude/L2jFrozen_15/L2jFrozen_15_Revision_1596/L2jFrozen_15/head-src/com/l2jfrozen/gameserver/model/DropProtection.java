package com.l2jfrozen.gameserver.model;

import java.util.concurrent.ScheduledFuture;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author DrHouse
 */
public class DropProtection implements Runnable
{
	private volatile boolean isProtected = false;
	private L2PcInstance owner = null;
	private ScheduledFuture<?> task = null;
	
	private static final long PROTECTED_MILLIS_TIME = 15000;
	
	@Override
	public synchronized void run()
	{
		isProtected = false;
		owner = null;
		task = null;
	}
	
	public boolean isProtected()
	{
		return isProtected;
	}
	
	public L2PcInstance getOwner()
	{
		return owner;
	}
	
	public synchronized boolean tryPickUp(final L2PcInstance actor)
	{
		if (!isProtected)
		{
			return true;
		}
		
		if (owner == actor)
		{
			return true;
		}
		
		if (owner.getParty() != null && owner.getParty() == actor.getParty())
		{
			return true;
		}
		
		if (owner.getClan() != null && owner.getClan() == actor.getClan())
		{
			return true;
		}
		
		return false;
	}
	
	public boolean tryPickUp(final L2PetInstance pet)
	{
		return tryPickUp(pet.getOwner());
	}
	
	public synchronized void unprotect()
	{
		if (task != null)
		{
			task.cancel(false);
		}
		
		isProtected = false;
		owner = null;
		task = null;
	}
	
	public synchronized void protect(final L2PcInstance player)
	{
		unprotect();
		
		isProtected = true;
		
		if ((owner = player) == null)
		{
			throw new NullPointerException("Trying to protect dropped item to null owner");
		}
		
		task = ThreadPoolManager.getInstance().scheduleGeneral(this, PROTECTED_MILLIS_TIME);
	}
}
