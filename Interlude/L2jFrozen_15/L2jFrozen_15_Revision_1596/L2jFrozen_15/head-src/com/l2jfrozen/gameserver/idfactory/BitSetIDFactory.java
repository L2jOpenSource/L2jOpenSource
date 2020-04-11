package com.l2jfrozen.gameserver.idfactory;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.PrimeFinder;

/**
 * @author Olympic, luisantonioa
 */

public class BitSetIDFactory extends IdFactory
{
	private static Logger LOGGER = Logger.getLogger(BitSetIDFactory.class);
	
	private BitSet freeIds;
	private AtomicInteger freeIdCount;
	private AtomicInteger nextFreeId;
	
	public class BitSetCapacityCheck implements Runnable
	{
		@Override
		public void run()
		{
			if (reachingBitSetCapacity())
			{
				increaseBitSetCapacity();
			}
		}
		
	}
	
	protected BitSetIDFactory()
	{
		super();
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new BitSetCapacityCheck(), 30000, 30000);
		initialize();
		LOGGER.info("IDFactory: " + freeIds.size() + " id's available.");
	}
	
	public synchronized void initialize()
	{
		try
		{
			freeIds = new BitSet(PrimeFinder.nextPrime(100000));
			freeIds.clear();
			freeIdCount = new AtomicInteger(FREE_OBJECT_ID_SIZE);
			
			for (final int usedObjectId : extractUsedObjectIDTable())
			{
				final int objectID = usedObjectId - FIRST_OID;
				if (objectID < 0)
				{
					if (Config.DEBUG)
					{
						LOGGER.warn("Object ID " + usedObjectId + " in DB is less than minimum ID of " + FIRST_OID);
					}
					continue;
				}
				freeIds.set(usedObjectId - FIRST_OID);
				freeIdCount.decrementAndGet();
			}
			
			nextFreeId = new AtomicInteger(freeIds.nextClearBit(0));
			initialized = true;
		}
		catch (final Exception e)
		{
			initialized = false;
			LOGGER.error("BitSet ID Factory could not be initialized correctly", e);
			e.printStackTrace();
		}
	}
	
	@Override
	public synchronized void releaseId(final int objectID)
	{
		if ((objectID - FIRST_OID) > -1)
		{
			freeIds.clear(objectID - FIRST_OID);
			freeIdCount.incrementAndGet();
		}
		else
		{
			LOGGER.warn("BitSet ID Factory: release objectID " + objectID + " failed (< " + FIRST_OID + ")");
		}
	}
	
	@Override
	public synchronized int getNextId()
	{
		final int newID = nextFreeId.get();
		freeIds.set(newID);
		freeIdCount.decrementAndGet();
		
		int nextFree = freeIds.nextClearBit(newID);
		
		if (nextFree < 0)
		{
			nextFree = freeIds.nextClearBit(0);
		}
		if (nextFree < 0)
		{
			if (freeIds.size() < FREE_OBJECT_ID_SIZE)
			{
				increaseBitSetCapacity();
			}
			else
			{
				throw new NullPointerException("Ran out of valid Id's.");
			}
		}
		
		nextFreeId.set(nextFree);
		
		return newID + FIRST_OID;
	}
	
	@Override
	public synchronized int size()
	{
		return freeIdCount.get();
	}
	
	protected synchronized int usedIdCount()
	{
		return (size() - FIRST_OID);
	}
	
	protected synchronized boolean reachingBitSetCapacity()
	{
		return PrimeFinder.nextPrime(usedIdCount() * 11 / 10) > freeIds.size();
	}
	
	protected synchronized void increaseBitSetCapacity()
	{
		final BitSet newBitSet = new BitSet(PrimeFinder.nextPrime(usedIdCount() * 11 / 10));
		newBitSet.or(freeIds);
		freeIds = newBitSet;
	}
}
