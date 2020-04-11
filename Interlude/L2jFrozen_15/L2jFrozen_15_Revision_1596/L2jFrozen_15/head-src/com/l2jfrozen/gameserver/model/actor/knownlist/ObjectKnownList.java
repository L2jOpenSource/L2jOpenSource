package com.l2jfrozen.gameserver.model.actor.knownlist;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2BoatInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.util.Util;

import main.data.memory.ObjectData;
import main.holders.objects.ObjectHolder;

public class ObjectKnownList
{
	private final L2Object activeObject;
	private Map<Integer, L2Object> knownObjects;
	
	public ObjectKnownList(final L2Object activeObject)
	{
		this.activeObject = activeObject;
	}
	
	public boolean addKnownObject(final L2Object object)
	{
		return addKnownObject(object, null);
	}
	
	public boolean addKnownObject(final L2Object object, final L2Character dropper)
	{
		if (object == null || object == getActiveObject())
		{
			return false;
		}
		
		// Check if already know object
		if (knowsObject(object))
		{
			if (!object.isVisible())
			{
				removeKnownObject(object);
			}
			return false;
		}
		
		// Check if object is not inside distance to watch object
		if (!Util.checkIfInRange(getDistanceToWatchObject(object), getActiveObject(), object, true))
		{
			return false;
		}
		
		if (ObjectData.get(ObjectHolder.class, activeObject).isDifferentWorld(object))
		{
			return false;
		}
		
		return getKnownObjects().put(object.getObjectId(), object) == null;
	}
	
	public final boolean knowsObject(final L2Object object)
	{
		if (object == null)
		{
			return false;
		}
		
		return getActiveObject() == object || getKnownObjects().containsKey(object.getObjectId());
	}
	
	/** Remove all L2Object from knownObjects */
	public void removeAllKnownObjects()
	{
		getKnownObjects().clear();
	}
	
	public boolean removeKnownObject(final L2Object object)
	{
		if (object == null)
		{
			return false;
		}
		
		return getKnownObjects().remove(object.getObjectId()) != null;
	}
	
	/**
	 * Update the knownObject and knowPlayers of the L2Character and of its already known L2Object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove invisible and too far L2Object from knowObject and if necessary from knownPlayers of the L2Character</li>
	 * <li>Add visible L2Object near the L2Character to knowObject and if necessary to knownPlayers of the L2Character</li>
	 * <li>Add L2Character to knowObject and if necessary to knownPlayers of L2Object alreday known by the L2Character</li><BR>
	 * <BR>
	 */
	public final synchronized void updateKnownObjects()
	{
		// Only bother updating knownobjects for L2Character; don't for L2Object
		if (getActiveObject() instanceof L2Character)
		{
			findCloseObjects();
			forgetObjects();
		}
	}
	
	private final void findCloseObjects()
	{
		final boolean isActiveObjectPlayable = getActiveObject() instanceof L2PlayableInstance;
		
		if (isActiveObjectPlayable)
		{
			Collection<L2Object> objects = L2World.getInstance().getVisibleObjects(getActiveObject());
			
			if (objects == null)
			{
				return;
			}
			
			// Go through all visible L2Object near the L2Character
			for (final L2Object object : objects)
			{
				if (object == null)
				{
					continue;
				}
				
				// Try to add object to active object's known objects
				// L2PlayableInstance sees everything
				addKnownObject(object);
				
				// Try to add active object to object's known objects
				// Only if object is a L2Character and active object is a L2PlayableInstance
				if (object instanceof L2Character)
				{
					object.getKnownList().addKnownObject(getActiveObject());
				}
			}
			
			objects = null;
		}
		else
		{
			Collection<L2PlayableInstance> playables = L2World.getInstance().getVisiblePlayable(getActiveObject());
			
			if (playables == null)
			{
				return;
			}
			
			// Go through all visible L2Object near the L2Character
			for (final L2Object playable : playables)
			{
				if (playable == null)
				{
					continue;
				}
				
				// Try to add object to active object's known objects
				// L2Character only needs to see visible L2PcInstance and L2PlayableInstance,
				// when moving. Other l2characters are currently only known from initial spawn area.
				// Possibly look into getDistanceToForgetObject values before modifying this approach...
				addKnownObject(playable);
			}
			
			playables = null;
		}
	}
	
	public final void forgetObjects()
	{
		// Go through knownObjects
		Collection<L2Object> knownObjects = getKnownObjects().values();
		
		if (knownObjects == null || knownObjects.size() == 0)
		{
			return;
		}
		
		for (final L2Object object : knownObjects)
		{
			if (object == null)
			{
				continue;
			}
			
			// Remove all invisible object
			// Remove all too far object
			if (!object.isVisible() || !Util.checkIfInRange(getDistanceToForgetObject(object), getActiveObject(), object, true))
			{
				if (object instanceof L2BoatInstance && getActiveObject() instanceof L2PcInstance)
				{
					if (((L2BoatInstance) object).getVehicleDeparture() == null)
					{
						//
					}
					else if (((L2PcInstance) getActiveObject()).isInBoat())
					{
						if (((L2PcInstance) getActiveObject()).getBoat() == object)
						{
							//
						}
						else
						{
							removeKnownObject(object);
						}
					}
					else
					{
						removeKnownObject(object);
					}
				}
				else
				{
					removeKnownObject(object);
				}
			}
		}
		
		knownObjects = null;
	}
	
	public L2Object getActiveObject()
	{
		return activeObject;
	}
	
	public int getDistanceToForgetObject(final L2Object object)
	{
		return 0;
	}
	
	public int getDistanceToWatchObject(final L2Object object)
	{
		return 0;
	}
	
	/**
	 * @return the knownObjects containing all L2Object known by the L2Character.
	 */
	public final Map<Integer, L2Object> getKnownObjects()
	{
		if (knownObjects == null)
		{
			knownObjects = new ConcurrentHashMap<>();
		}
		
		return knownObjects;
	}
	
	public static class KnownListAsynchronousUpdateTask implements Runnable
	{
		private final L2Object obj;
		
		public KnownListAsynchronousUpdateTask(final L2Object obj)
		{
			this.obj = obj;
		}
		
		@Override
		public void run()
		{
			if (obj != null)
			{
				obj.getKnownList().updateKnownObjects();
			}
		}
	}
}
